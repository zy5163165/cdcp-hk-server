package com.alcatelsbell.cdcp.nbi.netty;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Set;

/**
 * Author: Ronnie.Chen
 * Date: 2016/7/26
 * Time: 15:40
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ChannelEventPusherManager {
    private Logger logger = LoggerFactory.getLogger(ChannelEventPusherManager.class);

    private Hashtable<String,ChannelEventPusher> pushers = new Hashtable<String, ChannelEventPusher>();
    public void handleMessage(ChannelHandlerContext ctx,NbiMessage message) {
         if (message.getMessageType().equals("Receive_Alarm")) {
             String channelId = ctx.channel().id().asLongText();
             ChannelEventPusher pusher = pushers.get(channelId);
             if (pusher == null) {
                 synchronized (pushers) {
                     pusher = pushers.get(channelId);
                     if (pusher == null) {
                         pusher = new ChannelEventPusher(ctx);
                         pushers.put(channelId,pusher);
                         pusher.start();
                     }
                 }
             }
         }
    }


    public void notifyAlarm(NbiMessage alarm) {
        Set<String> keys = pushers.keySet();
        for (String key : keys) {
            ChannelEventPusher pusher = pushers.get(key);
            if (!pusher.isConnected()) {
                pushers.remove(key);
            } else {
                pusher.eventQueue.offer(alarm);
            }
        }
    }

}
