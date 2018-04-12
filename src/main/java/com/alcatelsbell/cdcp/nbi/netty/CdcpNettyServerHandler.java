/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.alcatelsbell.cdcp.nbi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;

/**
 * Handles both client-side and server-side handler depending on which
 * constructor was called.
 */
public class CdcpNettyServerHandler extends ChannelInboundHandlerAdapter {
    private ChannelEventPusherManager channelEventPusherManager = new ChannelEventPusherManager();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Echo back the received object to the client.
        if (msg instanceof ByteBuf && ((ByteBuf) msg).readableBytes() > 0) {
            int length = ((ByteBuf) msg).readableBytes();
            System.out.println("i = " + length);


            length = ((ByteBuf) msg).readInt();
            System.out.println("length = " + length);
//
            byte[] bs = new byte[length];
            ((ByteBuf) msg).readBytes(bs);

            try {
                NbiMessage message = NbiMessage.parse(bs);
                channelEventPusherManager.handleMessage(ctx,message);
                String id = ctx.channel().id().asLongText();
                System.out.println("id = " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        else {
            System.out.println("ctx = " + ctx);
        }
        System.out.println(msg);
    //    ctx.write(msg);
        //ctx.writeAndFlush("<abc></abc>");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
