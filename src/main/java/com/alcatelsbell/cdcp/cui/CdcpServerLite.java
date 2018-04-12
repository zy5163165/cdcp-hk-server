package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nodefx.CDCPConstants;
import com.alcatelsbell.cdcp.nodefx.CdcpMessage;
import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.cdcp.nodefx.ObjectInfo;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.server.EDSProcessor;
import com.alcatelsbell.cdcp.server.MigrateManager;
import com.alcatelsbell.cdcp.server.MigrateRunnable;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.util.DataFileUtil;
import com.alcatelsbell.nms.common.JMSSupport;
import com.alcatelsbell.nms.common.SpringContext;
import com.alcatelsbell.nms.db.components.service.*;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.EDS_PTN;
import org.asb.mule.probe.framework.entity.ManagedElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Ronnie.Chen
 * Date: 2016/6/24
 * Time: 10:53
 * rongrong.chen@alcatel-sbell.com.cn
 *
 */
public class CdcpServerLite {
    private Log logger = LogFactory.getLog(CdcpServerLite.class);

    public void start() throws Exception {
        CdcpServerUtil.setDbFileExecutor(this.executor);
        URL resource = CdcpServerLite.class.getClassLoader().getResource("META-INF/persistence.xml");
        System.out.println("resource = " + resource);

        URL resource2 = CdcpServerLite.class.getClassLoader().getResource("appserver-spring.xml");
        System.out.println("resource2 = " + resource2);

        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);

        MigrateManager.getInstance();
        System.out.println("MigrateManager init ok !!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("ctx = " + ctx);


        JPASupportSpringImpl context = new JPASupportSpringImpl("entityManagerFactoryData");
        try
        {
            //  context.begin();
            String[] preLoadSqls = Constants.PRE_LOAD_SQLS;
            for (String sql : preLoadSqls) {
                try {
                    DBUtil.getInstance().executeNonSelectingSQL(context,sql);
                } catch (Exception e) {
                    // logger.error(e, e);
                }
            }
            preLoadSqls = Constants.ptn_sqls;
            for (String sql : preLoadSqls) {
                try {
                    DBUtil.getInstance().executeNonSelectingSQL(context,sql);
                } catch (Exception e) {
                    //  logger.error(e, e);
                }
            }
            //   context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }

        final JMSSupport jmsSupport = SpringContext.getInstance().getJMSSupport();


        jmsSupport.addTopicSubscriber(CDCPConstants.TOPIC_EMS_SBI,new MessageListener() {
            @Override
            public void onMessage(Message message) {
                handleEmsMessage(message);
            }
        });



        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        jmsSupport.sendTopicMessage("cdcp.heartbeat",new Date());
                        //logger.info("Heart beat : success");
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        logger.error(e, e);
                    }
                }
            }
        };
        new Thread(r).start();

    }

    private CdcpMessage convert(Message message) {
        if (message instanceof ObjectMessage) {
            try {
                CdcpMessage cdcpMessage = (CdcpMessage)((ObjectMessage) message).getObject();
                return cdcpMessage;
            } catch (Exception e) {
                logger.error(e,e);
            }
        }
        return null;
    }


    private void handleEmsMessage(Message message) {
        CdcpMessage cdcpMessage = convert(message);
        Serializable object = cdcpMessage.getObject();

        String taskSerial = (String)cdcpMessage.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_TASK_SERIAL);
        logger.info("Handle sbi result : "+taskSerial+" :"+object);
//        EDS_PTN eds = (EDS_PTN)cdcpMessage.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_TASK_EDS);
//        if (eds != null) {
//            try {
//                boolean valid = EDSProcessor.getInstance().processEDS(cdcpMessage, eds);
//                logger.info("EDS_PTN:"+eds.getTaskSerial()+" valid = "+valid);
//                if (!valid) {
//                    logger.info("EDS_PTN:"+eds.getTaskSerial()+" valid = "+valid+" add = "+eds.getAdditinalInfo());
//                }
//            } catch (Exception e) {
//                logger.error(e, e);
//            }
//        }

        if (object != null && object instanceof FtpInfo) {
            FtpInfo ftpInfo = (FtpInfo) object;
            handleSbiFinish(ftpInfo, taskSerial);
        }
//        else if (object != null && object instanceof ObjectInfo) {
//            migrateManager.handleSbiFinish((ObjectInfo)object,taskSerial);
//        }
//        else if (object != null) {
//            migrateManager.handleSbiFailed(object.toString(), taskSerial);
//        }
        else {
            logger.error("error message object = : "+object);
        }


    }
    public static Task findTask(String serial) {
        Task task = null;
        try {
            task = (Task) JpaServerUtil.getInstance().findObjectByDN(Task.class, serial);
        } catch (Exception e) {
            LogUtil.error(CdcpServerUtil.class, e, e);
        }
        return task;
    }
    public static void updateTask(String serial, Integer status, String desc) {
        if (desc == null)
            desc = "";
        try {
            String ql = " update Task c set c.status = " + status + " where c.dn = '" + serial + "'";
            if (status != null)
                ql = " update Task c set c.status = " + status + " , c.description = '" + desc + "' where c.dn = '" + serial + "'";

            if (status == com.alcatelsbell.cdcp.common.Constants.TASK_STATUS_MIGRATING) {
                ql = " update Task c set c.tag3 = "+System.currentTimeMillis()+", c.status = " + status + " , c.description = '" + desc + "' where c.dn = '" + serial + "'";
            }
            JpaServerUtil.getInstance().executeUpdateSQL(ql);
        } catch (Exception e) {
            LogUtil.error(CdcpServerUtil.class, e, e);
        }
    }

    public void handleSbiFinish(final FtpInfo ftpInfo, final String serial) {
        synchronized (this) {

            Task task1 =  findTask(serial);

            if (task1 == null) {
                if (!serial.contains("@"))
                    throw new RuntimeException("Exception on serial : "+serial);
                String emsdn = serial.substring(0,serial.indexOf("@"));
                registerEmsJob(emsdn,serial);
            }
            updateTask(serial, Constants.TASK_STATUS_MIGRATE_WAITING, null);

            Task task =  findTask(serial);

            task.setTag3(System.currentTimeMillis() + "");

            try {
                JpaServerUtil.getInstance().saveObject(-1,task);
            } catch (Exception e) {
                logger.error(e, e);
            }

            HashMap map = new HashMap();
            try {
                if (task != null && task.getScheduleId() != null) {
                    Schedule schedule = CdcpServerUtil.findSchedule(task.getScheduleId());
                    if (schedule != null && schedule.getArguments() != null) {
                        String[] split = schedule.getArguments().split("&");
                        for (String s : split) {
                            String k = s.substring(0,s.indexOf("="));
                            String v = s.substring(s.indexOf("=")+1);
                            map.put(k, v);

                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
            if (ftpInfo.getAttributes().get("logical") != null)
                map.put("logical",ftpInfo.getAttributes().get("logical"));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        migrateDB(ftpInfo,serial);
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
            });
            logger.info("Execute task " + serial + "," + ftpInfo);
            if (queue.size() > 0) {
                logger.info(queue.size() + " migrate task is waiting .. ");
            }
        }
        // todo
    }

    private void migrateDB(FtpInfo ftpInfo,String serial) throws Exception {
        logger.info("Migrating : "+ftpInfo+", serial: "+serial);
        String loaderStr = serial.substring(serial.lastIndexOf("#")+1);
        if (loaderStr == null) throw new RuntimeException("no loader attribute found : "+ftpInfo);
        Class loader = Class.forName(loaderStr);

        File file = DataFileUtil.downloadFile(ftpInfo);
        if (!file.exists()) throw new Exception("下载文件失败:"+ftpInfo);

        JPASupport sqliteJPASupport = JPASupportFactory.createSqliteJPASupport(file.getAbsolutePath());
        List<ManagedElement> mes = null;
        try {
            mes = JPAUtil.getInstance().findObjects(sqliteJPASupport, "select c from ManagedElement c", null, null, 0, 1);
        } catch (Exception e) {
            logger.error(e, e);
        } finally {
            sqliteJPASupport.release();
        }


        if (mes.size() > 0) {
            String emsName = mes.get(0).getEmsName();
            AbstractDBFLoader dbfLoader = (AbstractDBFLoader)
                    loader.getConstructor(String.class, String.class).newInstance(file.getAbsolutePath(),emsName);
            try {
                long count = JPAUtil.getInstance().findObjectsCount(sqliteJPASupport, CTP.class, null);
                if (count == 0)
                    dbfLoader.getAttributesMap().put("logical","false");
            } catch (Exception e) {
                logger.error(e, e);
                dbfLoader.getAttributesMap().put("logical","false");
            }
            dbfLoader.execute();
        } else {
            throw new Exception(" ManagedElement size is 0 file = "+ftpInfo.getFileName());
        }

    }


    public void registerEmsJob(String emsdn, String serial) {
        Task task = new Task();
        task.setDn(serial);
        task.setName(emsdn);
        Ems ems = null;
        try {
            ems = (Ems) JpaServerUtil.getInstance().findObjectByDN(Ems.class, emsdn);
        } catch (Exception e) {
            logger.error(e, e);
        }
        if (ems != null) {
            task.setDescription(ems.getName());
            task.setFromWhere(ems.getProtocalType());
            task.setFromWhere(ems.getProtocalType());
        }
        task.setTag1(emsdn);

        task.setTaskObject(emsdn);
        task.setStartTime(new Date());
        task.setStatus(Constants.TASK_STATUS_SBI_RUNNING);

        try {
            JpaServerUtil.getInstance().saveObject(-1, task);
        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    private LinkedBlockingQueue queue = new LinkedBlockingQueue();

    public LinkedBlockingQueue getQueue() {

        return queue;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(SysProperty.getInt("migrateThreadSize", 1), SysProperty.getInt("migrateThreadSize", 1), 10,
            TimeUnit.DAYS, queue);

    public static void main(String[] args) throws Exception {
        new CdcpServerLite().start();
        System.out.println("Cdcp server lite start success");
        synchronized (CdcpServerLite.class) {
            CdcpServerLite.class.wait();
        }

    }


}
