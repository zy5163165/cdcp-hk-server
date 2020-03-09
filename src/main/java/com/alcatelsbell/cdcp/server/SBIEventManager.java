package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.nodefx.*;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.ems.EmsExceptionManager;
import com.alcatelsbell.nms.interfaces.publics.EmsDataCache;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.valueobject.CdcpDictionary;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Ronnie.Chen
 * Date: 14-3-12
 * Time: 上午10:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SBIEventManager {
    private static SBIEventManager ourInstance = new SBIEventManager();
    private Log logger = LogFactory.getLog(getClass());
    public static SBIEventManager getInstance() {
        return ourInstance;
    }
    private LinkedBlockingQueue<SBIEvent> notificationQueue = new LinkedBlockingQueue(10000);

    private LinkedBlockingQueue<SBIEvent> exceptionQueue = new LinkedBlockingQueue(10000);


    private SBIEventManager() {
        startConsumers();
    }

    private void startConsumers() {
        Runnable notificationConsumer = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SBIEvent event = null;
                    try {
                        event = notificationQueue.take();
                    } catch (InterruptedException e) {
                        logger.error(e, e);
                    }
                    processNotification(event);
                }
            }
        };

        Runnable exceptionConsumer = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SBIEvent event = null;
                    try {
                        event = exceptionQueue.take();
                    } catch (InterruptedException e) {
                        logger.error(e, e);
                    }
                    processException(event);
                }
            }
        };
        new Thread(notificationConsumer).start();
        new Thread(exceptionConsumer).start();

    }

    private void processNotification(SBIEvent event) {
        try {
            CdcpServerUtil.syncDevice(event.getEmsDn(),event.getDeviceDn());
        } catch (NodeException e) {
            logger.error(e, e);
        }
    }
    private void processException(SBIEvent event) {
        String emsDn = event.getEmsDn();
        HashMap dataMap = event.getDataMap();
        if (dataMap != null) {
            String exceptionCode = (String)dataMap.get("EXCEPTION_CODE");
            String exceptionDetail = (String)dataMap.get("EXCEPTION_DETAIL");
            Boolean recover = (Boolean)dataMap.get("RECOVER");

            if (exceptionCode != null && recover != null) {
                EmsExceptionManager.getInstance().fireEmsException(emsDn,exceptionCode,exceptionDetail,recover);
            }
        }

    }


    public void onSBIEvent(SBIEvent event) {
        logger.info("Receive SBIEvent : "+event);
         if (SBIEvent.TYPE_NOTIFICATION.equals(event.getType())) {
             notificationQueue.offer(event);
         } else if (SBIEvent.TYPE_EXCEPTION.equals(event.getType())) {
            exceptionQueue.offer(event);
        }
    }
}
