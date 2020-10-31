package com.swingfrog.summer.meter.protobuf;

import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ProtobufDecoder;
import com.swingfrog.summer.protocol.protobuf.ProtobufEncoder;
import com.swingfrog.summer.protocol.protobuf.RespProtobufMgr;
import com.swingfrog.summer.protocol.protobuf.proto.CommonProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.URI;

/**
 * {@link ProtocolConst#SERVER_PROTOCOL_LENGTH_FIELD_PROTOBUF}
 */
public abstract class MeterLFPClient extends AbstractMeterProtobufClient {

    public MeterLFPClient(int id) {
        super(id);
    }

    @Override
    public void syncConnect(URI uri, EventLoopGroup group) throws Exception {
        RespProtobufMgr.get().registerMessage(ProtocolConst.PROTOBUF_HEART_BEAT_REQ_ID, CommonProto.HeartBeat_Resp_0.getDefaultInstance());
        RespProtobufMgr.get().registerMessage(ProtocolConst.PROTOBUF_ERROR_CODE_RESP_ID, CommonProto.ErrorCode_Resp_1.getDefaultInstance());

        ClientHandler clientHandler = new ClientHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ClientInitializer(clientHandler));
        Channel channel = bootstrap.connect(uri.getHost(),uri.getPort()).sync().channel();

        setChannel(channel);
        online();
    }

    private class ClientInitializer extends ChannelInitializer<SocketChannel> {

        private final ClientHandler clientHandler;

        public ClientInitializer(ClientHandler clientHandler) {
            this.clientHandler = clientHandler;
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(msgLength(), 0, 4, 0, 4));
            pipeline.addLast(new LengthFieldPrepender(4));
            pipeline.addLast(new ProtobufDecoder());
            pipeline.addLast(new ProtobufEncoder());
            pipeline.addLast(clientHandler);
        }

    }

    private class ClientHandler extends SimpleChannelInboundHandler<Protobuf> {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            offline();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Protobuf msg) {
            recv(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

    }

}
