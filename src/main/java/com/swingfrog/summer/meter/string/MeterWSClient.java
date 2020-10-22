package com.swingfrog.summer.meter.string;

import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordDecoder;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordEncoder;
import com.swingfrog.summer.protocol.websocket.WebSocketDecoder;
import com.swingfrog.summer.protocol.websocket.WebSocketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;

/**
 * {@link ProtocolConst#SERVER_PROTOCOL_WEB_SOCKET}
 */
public abstract class MeterWSClient extends AbstractMeterStringClient {

    private final boolean standard;

    public MeterWSClient(int id) {
        this(id, false);
    }

    MeterWSClient(int id, boolean standard) {
        super(id);
        this.standard = standard;
    }

    @Override
    public void syncConnect(URI uri, EventLoopGroup group) throws Exception {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        WebSocketClientHandshaker handShaker = WebSocketClientHandshakerFactory
                .newHandshaker(uri, WebSocketVersion.V13, null, true, httpHeaders);

        ClientHandler clientHandler = new ClientHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ClientInitializer(clientHandler, handShaker));
        Channel channel = bootstrap.connect(uri.getHost(),uri.getPort()).sync().channel();

        ChannelPromise handShakerFuture = channel.newPromise();
        clientHandler.setHandShakerFuture(handShakerFuture);
        if (!handShakerFuture.await(15000L)) {
            throw new RuntimeException("shake hand time out.");
        }

        setChannel(channel);
        online();
    }

    private class ClientInitializer extends ChannelInitializer<SocketChannel> {

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
            pipeline.addLast(new HttpObjectAggregator(msgLength()));
            pipeline.addLast(new WebSocketClientProtocolHandler(clientHandShaker));
            pipeline.addLast(new WebSocketDecoder());
            pipeline.addLast(new WebSocketEncoder());
            if (!standard) {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(msgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
            }
            pipeline.addLast(new StringPasswordDecoder(getCharset(), getPassword()));
            pipeline.addLast(new StringPasswordEncoder(getCharset(), getPassword()));
            pipeline.addLast(clientHandler);
        }

    }

    private class ClientHandler extends SimpleChannelInboundHandler<String> {

        private ChannelPromise handShakerFuture;

        public void setHandShakerFuture(ChannelPromise handShakerFuture) {
            this.handShakerFuture = handShakerFuture;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            offline();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            recv(msg);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                handShakerFuture.setSuccess();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

    }

}
