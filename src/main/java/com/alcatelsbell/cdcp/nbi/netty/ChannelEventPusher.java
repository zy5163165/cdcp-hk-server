package com.alcatelsbell.cdcp.nbi.netty;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Ronnie.Chen
 * Date: 2016/7/27
 * Time: 9:06
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ChannelEventPusher extends Thread {
    private Logger logger = LoggerFactory.getLogger(ChannelEventPusher.class);

    private ChannelHandlerContext channelHandlerContext = null;
    public LinkedBlockingQueue<NbiMessage> eventQueue = new LinkedBlockingQueue<NbiMessage>();
    private boolean connected = true;

    public ChannelEventPusher(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public boolean isConnected() {
        return connected;
    }


    @Override
    public void run() {
        while (true) {
            NbiMessage message = null;
            try {
                message = eventQueue.take();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            try {
                channelHandlerContext.writeAndFlush(message);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                break;
            }
        }

        connected = false;

    }
}
