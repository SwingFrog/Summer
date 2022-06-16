package com.swingfrog.summer.server;

import com.swingfrog.summer.config.ServerConfig;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordDecoder;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordEncoder;
import com.swingfrog.summer.protocol.protobuf.ProtobufDecoder;
import com.swingfrog.summer.protocol.protobuf.ProtobufEncoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineDecoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineEncoder;
import com.swingfrog.summer.protocol.tiny.TinyDecoder;
import com.swingfrog.summer.protocol.tiny.TinyEncoder;
import com.swingfrog.summer.protocol.websocket.WebSocketDecoder;
import com.swingfrog.summer.protocol.websocket.WebSocketEncoder;
import com.swingfrog.summer.protocol.websocket.WebSocketUriFilter;
import com.swingfrog.summer.server.exception.NotFoundProtocolException;
import com.swingfrog.summer.web.WebRequestHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ServerContext serverContext;

    public ServerInitializer(ServerContext serverContext) {
        this.serverContext = serverContext;
        if (!checkProtocol()) {
            throw new NotFoundProtocolException(serverContext.getConfig().getProtocol());
        }
    }

    @Override
    protected void initChannel(SocketChannel sc) {
        ChannelPipeline pipeline = sc.pipeline();
        ServerConfig config = serverContext.getConfig();
        String protocol = config.getProtocol();
        switch (protocol) {
            case ProtocolConst.SERVER_PROTOCOL_STRING_LINE:
                pipeline.addLast(new LineBasedFrameDecoder(config.getMsgLength()));
                pipeline.addLast(new StringPasswordLineDecoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new StringPasswordLineEncoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new ServerStringHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketUriFilter(serverContext));
                pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
                pipeline.addLast(new WebSocketDecoder());
                pipeline.addLast(new WebSocketEncoder());
                pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new StringPasswordDecoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new StringPasswordEncoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new ServerStringHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD:
                pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new StringPasswordDecoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new StringPasswordEncoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new ServerStringHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_HTTP:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebRequestHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketUriFilter(serverContext));
                pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
                pipeline.addLast(new WebSocketDecoder());
                pipeline.addLast(new WebSocketEncoder());
                pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new ProtobufDecoder());
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new ServerProtobufHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD_PROTOBUF:
                pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new ProtobufDecoder());
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new ServerProtobufHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_STANDARD:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketUriFilter(serverContext));
                pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
                pipeline.addLast(new WebSocketDecoder());
                pipeline.addLast(new WebSocketEncoder());
                pipeline.addLast(new StringPasswordDecoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new StringPasswordEncoder(config.getCharset(), config.getPassword()));
                pipeline.addLast(new ServerStringHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF_STANDARD:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketUriFilter(serverContext));
                pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
                pipeline.addLast(new WebSocketDecoder());
                pipeline.addLast(new WebSocketEncoder());
                pipeline.addLast(new ProtobufDecoder());
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new ServerProtobufHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD_TINY:
                pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new TinyDecoder(config.getCharset()));
                pipeline.addLast(new TinyEncoder(config.getCharset()));
                pipeline.addLast(new ServerTinyHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_TINY:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketUriFilter(serverContext));
                pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
                pipeline.addLast(new WebSocketDecoder());
                pipeline.addLast(new WebSocketEncoder());
                pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new TinyDecoder(config.getCharset()));
                pipeline.addLast(new TinyEncoder(config.getCharset()));
                pipeline.addLast(new ServerTinyHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_TINY_STANDARD:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketUriFilter(serverContext));
                pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
                pipeline.addLast(new WebSocketDecoder());
                pipeline.addLast(new WebSocketEncoder());
                pipeline.addLast(new TinyDecoder(config.getCharset()));
                pipeline.addLast(new TinyEncoder(config.getCharset()));
                pipeline.addLast(new ServerTinyHandler(serverContext));
                break;
            default:
                throw new NotFoundProtocolException(config.getProtocol());
        }
    }

    private boolean checkProtocol() {
        String protocol = serverContext.getConfig().getProtocol();
        switch (protocol) {
            case ProtocolConst.SERVER_PROTOCOL_STRING_LINE:
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET:
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD:
            case ProtocolConst.SERVER_PROTOCOL_HTTP:
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF:
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD_PROTOBUF:
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_STANDARD:
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF_STANDARD:
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD_TINY:
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_TINY:
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_TINY_STANDARD:
                return true;
        }
        return false;
    }

}
