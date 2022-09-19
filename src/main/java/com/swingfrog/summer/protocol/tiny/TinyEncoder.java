package com.swingfrog.summer.protocol.tiny;

import com.swingfrog.summer.protocol.tiny.msg.Tiny;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class TinyEncoder extends MessageToMessageEncoder<Tiny> {

    private final String charset;

    public TinyEncoder(String charset) {
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Tiny msg, List<Object> out) throws Exception {
        out.add(msg.getByteBuf(ctx.alloc(), charset));
    }

}
