package com.swingfrog.summer.meter.string;

import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordDecoder;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.URI;

/**
 * {@link ProtocolConst#SERVER_PROTOCOL_LENGTH_FIELD}
 */
public abstract class MeterLFClient extends AbstractMeterStringClient {

    protected MeterLFClient(int id) {
        super(id);
    }

    @Override
    public void syncConnect(URI uri, EventLoopGroup group) throws Exception {
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
            pipeline.addLast(new StringPasswordDecoder(getCharset(), getPassword()));
            pipeline.addLast(new StringPasswordEncoder(getCharset(), getPassword()));
            pipeline.addLast(clientHandler);
        }

    }

    private class ClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            offline();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            recv(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

    }

}
