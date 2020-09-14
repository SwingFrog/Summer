package com.swingfrog.summer.server;

import com.swingfrog.summer.config.ServerConfig;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordDecoder;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordEncoder;
import com.swingfrog.summer.protocol.protobuf.ProtobufPasswordDecoder;
import com.swingfrog.summer.protocol.protobuf.ProtobufPasswordEncoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineDecoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineEncoder;
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
                pipeline.addLast(new StringPasswordLineDecoder(config.getMsgLength(), config.getCharset(), config.getPassword()));
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
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_STANDARD_PROTOBUF:
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketUriFilter(serverContext));
                pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
                pipeline.addLast(new WebSocketDecoder());
                pipeline.addLast(new WebSocketEncoder());
                pipeline.addLast(new ProtobufPasswordDecoder(config.getPassword()));
                pipeline.addLast(new ProtobufPasswordEncoder(config.getPassword()));
                pipeline.addLast(new ServerProtobufHandler(serverContext));
                break;
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD_PROTOBUF:
                pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new ProtobufPasswordDecoder(config.getPassword()));
                pipeline.addLast(new ProtobufPasswordEncoder(config.getPassword()));
                pipeline.addLast(new ServerProtobufHandler(serverContext));
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
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_STANDARD:
            case ProtocolConst.SERVER_PROTOCOL_WEB_SOCKET_STANDARD_PROTOBUF:
            case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD_PROTOBUF:
                return true;
        }
        return false;
    }

}
