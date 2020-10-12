package com.swingfrog.summer.test.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ProtobufDecoder;
import com.swingfrog.summer.protocol.protobuf.ProtobufEncoder;
import com.swingfrog.summer.protocol.protobuf.ProtobufMgr;
import com.swingfrog.summer.protocol.protobuf.proto.CommonProto;
import com.swingfrog.summer.protocol.websocket.WebSocketDecoder;
import com.swingfrog.summer.protocol.websocket.WebSocketEncoder;
import com.swingfrog.summer.test.protobuf.proto.TestProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClientTest {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        URI websocketURI = new URI("ws://127.0.0.1:8828/protobuf");
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        WebSocketClientHandshaker handShaker = WebSocketClientHandshakerFactory
                .newHandshaker(websocketURI, WebSocketVersion.V13, null, true, httpHeaders);

        ClientHandler clientHandler = new ClientHandler();

        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ClientInitializer(clientHandler, handShaker));
            Channel channel = bootstrap.connect(websocketURI.getHost(),websocketURI.getPort()).sync().channel();

            ChannelPromise handShakerFuture = channel.newPromise();
            clientHandler.setHandShakerFuture(handShakerFuture);
            if (!handShakerFuture.await(10000L)) {
                System.err.println("error");
            }

            ProtobufMgr.get().registerMessage(0, CommonProto.HearBeat_Resp_0.getDefaultInstance());
            ProtobufMgr.get().registerMessage(103, TestProto.Notice_Push_103.getDefaultInstance());

            CommonProto.HeartBeat_Req_0 req = CommonProto.HeartBeat_Req_0.getDefaultInstance();
            for (;;) {
                write(channel, req);
                Thread.sleep(10000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            workGroup.shutdownGracefully().sync();
        }
    }

    private static class ClientInitializer extends ChannelInitializer<SocketChannel> {

        private final ClientHandler clientHandler;
        private final WebSocketClientHandshaker clientHandShaker;

        public ClientInitializer(ClientHandler clientHandler, WebSocketClientHandshaker clientHandShaker) {
            this.clientHandler = clientHandler;
            this.clientHandShaker = clientHandShaker;
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpClientCodec());
            pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 10));
            pipeline.addLast(new WebSocketClientProtocolHandler(clientHandShaker));
            pipeline.addLast(new WebSocketDecoder());
            pipeline.addLast(new WebSocketEncoder());
            pipeline.addLast(new ProtobufDecoder());
            pipeline.addLast(new ProtobufEncoder());
            pipeline.addLast(clientHandler);
        }

    }

    private static class ClientHandler extends SimpleChannelInboundHandler<Protobuf> {

        private ChannelPromise handShakerFuture;

        public void setHandShakerFuture(ChannelPromise handShakerFuture) {
            this.handShakerFuture = handShakerFuture;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("channelActive");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Protobuf msg) {
            int messageId = msg.getId();
            byte[] bytes = msg.getBytes();
            Message messageTemplate = ProtobufMgr.get().getMessageTemplate(messageId);
            if (messageTemplate == null) {
                System.out.println("messageId:" + messageId + " message template not exist");
                return;
            }
            try {
                Message message = messageTemplate.getParserForType().parseFrom(bytes);
                recv(messageId, message);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                handShakerFuture.setSuccess();
                System.out.println("HANDSHAKE_COMPLETE");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }

    private static void write(Channel channel, Message message) {
        Integer messageId = ProtobufMgr.get().getMessageId(message.getClass());
        if (messageId == null) {
            System.out.println(message.getClass().getSimpleName() + " message id not exist");
            return;
        }
        channel.writeAndFlush(Protobuf.of(messageId, message));
    }

    private static void recv(int messageId, Message message) {
        if (message instanceof CommonProto.HearBeat_Resp_0) {
            CommonProto.HearBeat_Resp_0 resp = (CommonProto.HearBeat_Resp_0) message;
            System.out.println("hearBeat: " + resp.getTime());
        } else if (message instanceof TestProto.Notice_Push_103) {
            TestProto.Notice_Push_103 resp = (TestProto.Notice_Push_103) message;
            System.out.println("notice:" + resp.getValue());
        }
    }

}
