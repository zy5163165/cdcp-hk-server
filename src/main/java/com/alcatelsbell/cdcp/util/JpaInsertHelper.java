package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-2
 * Time: 下午3:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class JpaInsertHelper {
    private Logger logger  =  MigrateThread.thread().getLogger();
    private LinkedBlockingQueue<BObject> queue = new LinkedBlockingQueue();
    private  Runnable consumer = null;
    private JPASupport support = null;
    private boolean flag = true;
    private boolean finish = false;
    public JpaInsertHelper(JPASupport jpaSupport) {
        this.support = jpaSupport;
    }

    public void finishAndRelease() {
       finish = true;
        consumerThread.interrupt();
    }



    private Thread consumerThread = null;
    public void insertBObjects(List objects) throws Exception {
        for (Iterator<BObject> iterator = objects.iterator(); iterator.hasNext(); ) {
            BObject next = iterator.next();
            insertBObject(next);
        }
    }
    public void insertBObject(BObject object) throws Exception {
        if (!flag) throw new Exception("Stop producer , database exception .");
        if (finish) throw new Exception("Already finished ");
        queue.offer(object);

        if (consumer == null) {
            consumer = new Runnable() {
                private List temp = new ArrayList() ;
                 @Override
                public void run() {
                    while (true) {
                        if (finish && !queue.isEmpty()) {
                            logger.info("Waiting for finish , queue size = "+queue.size());
                        }
                        if (finish && queue.isEmpty())
                            handleFinish();
                        try {
                            BObject bo = queue.take();
                            int consumeSize = queue.size() > 10000 ? 10000 : queue.size();

                            support.begin();
                            temp.add(bo);
                            JPAUtil.getInstance().createObject(support,-1,bo);
                            for (int i = 0; i < consumeSize; i++) {
                                bo = queue.take();
                                temp.add(bo);
                                JPAUtil.getInstance().createObject(support,-1,bo);

                            }
                            support.end();
                            temp.clear();
                        } catch (InterruptedException e) {
                            handleFinish();
                        }
                        catch (Exception e) {
                            logger.error("Exception on inserting ------------------");
                            for (int i = 0; i < temp.size(); i++) {
                                BObject bObject = (BObject) temp.get(i);
                                logger.error(bObject);
                            }
                            logger.error("---------------------- ------------------");
                            logger.error(e,e);
                            if (true)
                                System.exit(1);
                            flag = false;
                        }
                        //    BObject bo = queue.take();

                    }
                }
            };
            consumerThread = new Thread(consumer);
            consumerThread.start();
        }
    }

    private void handleFinish() {
        logger.info("JpaInsertHelper Finished");
        support.release();
    }

}
