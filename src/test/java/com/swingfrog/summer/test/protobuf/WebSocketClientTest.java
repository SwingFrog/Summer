package com.swingfrog.summer.test.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.protobuf.ProtobufMgr;
import com.swingfrog.summer.protocol.protobuf.proto.CommonProto;
import com.swingfrog.summer.test.protobuf.proto.TestProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClientTest {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        URI websocketURI = new URI("ws://127.0.0.1:8828/protobuf");
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        WebSocketClientHandshaker handShaker = WebSocketClientHandshakerFactory
                .newHandshaker(websocketURI, WebSocketVersion.V13, null, true, httpHeaders);

        ClientHandler clientHandler = new ClientHandler(handShaker);

        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ClientInitializer(clientHandler));
            Channel channel = bootstrap.connect(websocketURI.getHost(),websocketURI.getPort()).sync().channel();
            ChannelPromise handShakerFuture = channel.newPromise();
            clientHandler.setHandShakerFuture(handShakerFuture);
            handShaker.handshake(channel);
            handShakerFuture.sync();

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

        public ClientInitializer(ClientHandler clientHandler) {
            this.clientHandler = clientHandler;
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpClientCodec());
            pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 10));
            pipeline.addLast(clientHandler);
        }

    }

    private static class ClientHandler extends SimpleChannelInboundHandler<Object> {

        private final WebSocketClientHandshaker handShaker;
        private ChannelPromise handShakerFuture;

        public ClientHandler(WebSocketClientHandshaker handShaker) {
            this.handShaker = handShaker;
        }

        public void setHandShakerFuture(ChannelPromise handShakerFuture) {
            this.handShakerFuture = handShakerFuture;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            Channel ch = ctx.channel();
            FullHttpResponse response;
            if (!handShaker.isHandshakeComplete()) {
                try {
                    response = (FullHttpResponse) msg;
                    //握手协议返回，设置结束握手
                    handShaker.finishHandshake(ch, response);
                    //设置成功
                    handShakerFuture.setSuccess();
                    System.out.println("WebSocket Client connected! response headers[sec-websocket-extensions]:{}" + response.headers());
                } catch (WebSocketHandshakeException var7) {
                    FullHttpResponse res = (FullHttpResponse) msg;
                    String errorMsg = String.format("WebSocket Client failed to connect,status:%s,reason:%s", res.status(), res.content().toString(CharsetUtil.UTF_8));
                    handShakerFuture.setFailure(new Exception(errorMsg));
                }
            } else if (msg instanceof FullHttpResponse) {
                response = (FullHttpResponse) msg;
                throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
            } else {
                WebSocketFrame frame = (WebSocketFrame) msg;
                if (frame instanceof BinaryWebSocketFrame) {
                    BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;

                    ByteBuf buf = binaryFrame.content().retain();
                    int messageId = buf.readInt();
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
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
            }
        }

    }

    private static void write(Channel channel, Message message) {
        byte[] bytes = message.toByteArray();
        ByteBuf buf = Unpooled.buffer(4 + bytes.length);
        Integer messageId = ProtobufMgr.get().getMessageId(message.getClass());
        if (messageId == null) {
            System.out.println(message.getClass().getSimpleName() + " message id not exist");
            return;
        }
        buf.writeInt(messageId);
        buf.writeBytes(bytes);
        BinaryWebSocketFrame binaryFrame = new BinaryWebSocketFrame(buf.retain());
        channel.writeAndFlush(binaryFrame);
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
