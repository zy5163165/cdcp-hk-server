package com.alcatelsbell.cdcp.nbi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/7/26
 * Time: 13:07
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpHeaderDecoder extends ByteToMessageDecoder {
    private Logger logger = LoggerFactory.getLogger(CdcpHeaderDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() > 0) {
            System.out.println("in.readableBytes() = " + in.readableBytes());
            int length = in.readInt();
            System.out.println("length = " + length);
            if (length == in.readableBytes())
                out.add(in);
            else {
                throw new Exception("error length = "+length+" but readable bytes="+in.readableBytes());
            }
        }
    }
}
