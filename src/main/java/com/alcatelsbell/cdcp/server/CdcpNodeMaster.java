package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.common.model.EmsBenchmark;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkItem;
import com.alcatelsbell.cdcp.nodefx.*;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.nms.common.*;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.jms.JMSSupportSpringImpl;
import com.alcatelsbell.nms.valueobject.CdcpDictionary;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import com.alcatelsbell.nms.valueobject.sys.SysNode;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.EDS_PTN;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-10
 * Time: 下午8:41
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpNodeMaster {
    private Log logger  = LogFactory.getLog(getClass());
    private MigrateManager migrateManager =   MigrateManager.getInstance();
    public CdcpNodeMaster() {
        register();
    }

    private void register() {

        JMSSupport jmsSupport = SpringContext.getInstance().getJMSSupport();
        jmsSupport.addTopicSubscriber(CDCPConstants.TOPIC_NODE,new MessageListener() {
            @Override
            public void onMessage(Message message) {
                handleNodeMessage(message);
            }
        });

        jmsSupport.addTopicSubscriber(CDCPConstants.TOPIC_EMS_SBI,new MessageListener() {
            @Override
            public void onMessage(Message message) {
                handleEmsMessage(message);
            }
        });
        jmsSupport.addTopicSubscriber(CDCPConstants.TOPIC_EMS_SBI_EVENT,new MessageListener() {
            @Override
            public void onMessage(Message message) {
                handleEmsSBIEventMessage(message);
            }
        });

        jmsSupport.addTopicSubscriber(CDCPConstants.TOPIC_LOG,new MessageListener() {
            @Override
            public void onMessage(Message message) {
                handleLogMessage(message);
            }
        });
        broadcast();
    }


    /**
     * 每隔五分钟广播EMS列表
     */
    private void broadcast() {
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        List<Ems> emsList = null;
                        try {
                            emsList = JpaClient.getInstance().findAllObjects(Ems.class);
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                        try {
                            CdcpMessage message = new CdcpMessage();
                            List monitoredEms = new ArrayList();
                            logger.info("broadcast ems ----------------");
                            for (Ems ems : emsList) {
                                 if (ems.getMonitored() == null || ems.getMonitored() == CdcpDictionary.EMSISMONITORED.YES.value) {
                                     monitoredEms.add(ems);
                                     logger.info("ems : "+ems.getDn());
                                 }
                            }
                            if (emsList != null) {
                                message.setObject(new ArrayList(monitoredEms));
                            }
                            SpringContext.getInstance().getJMSSupport().sendTopicMessage(CDCPConstants.TOPIC_SERVER_BROADCAST, message);
                        } catch (Exception e) {
                            logger.error(e,e);
                        }

                    } catch (Exception e) {
                        logger.error(e, e);
                    }

                    try {
                        Thread.sleep(5 * 60 * 1000l);
                    } catch (InterruptedException e) {
                        logger.error(e, e);
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    private void handleEmsSBIEventMessage(Message message) {
        CdcpMessage cdcpMessage = convert(message);
        Serializable object = cdcpMessage.getObject();
        if (CDCPConstants.MESSAGE_TYPE_SBI_EVENT.equals(cdcpMessage.getType())) {
            if (object instanceof SBIEvent)
                SBIEventManager.getInstance().onSBIEvent((SBIEvent)object);
            else
                logger.error("Error CdcpMessage Object,should be SBIEvent instance "+object);
        }
    }

    private void handleLogMessage(Message message) {
        CdcpMessage cdcpMessage = convert(message);
        String txt = (String) cdcpMessage.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_LOG_TXT);
        String serial = (String) cdcpMessage.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_TASK_SERIAL);

        Task task = CdcpServerUtil.findTask(serial);
        if (task == null) {
            logger.error("Task not found ! "+serial);
            return;
        }

        try {
            Integer percentage = (Integer)cdcpMessage.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_PERCENTAGE);
            if (percentage != null) {
                String ql = " update Task c set c.percentage = " + percentage + " where c.dn = '" + serial + "'";
                CdcpServerUtil.executeUpdateQl(ql);

             //   CdcpServerUtil.updateEmsStatus(task.getEmsDn(), CdcpDictionary.EMSSTATUS.OK.value);
            }
        } catch (Throwable e) {
            logger.error(e, e);
        }


        com.alcatelsbell.nms.valueobject.sys.Log log = new com.alcatelsbell.nms.valueobject.sys.Log();
        log.setTime(new Date());
        log.setDn(SysUtil.nextDN());
        log.setCategory(SysConst.LOG_CATEGORY_INTERFACE);
        log.setOperation("同步数据");

        log.setComments(txt);
        log.setContent(txt);
        log.setIpaddress("");
        log.setModule("EMS");
        log.setObject(task.getTaskObject());
        log.setDn(SysUtil.nextLongId()+"");
        log.setSource(serial);
        log.setSysname("CDCP");
        try {
            JpaClient.getInstance().saveObject(-1,log);
        } catch (Exception e) {
            logger.error(e,e);
        }


    }

    private void handleEmsMessage(Message message) {
        CdcpMessage cdcpMessage = convert(message);
        Serializable object = cdcpMessage.getObject();

        String taskSerial = (String)cdcpMessage.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_TASK_SERIAL);
        logger.info("Handle sbi result : "+taskSerial+" :"+object);
        EDS_PTN eds = (EDS_PTN)cdcpMessage.getAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_TASK_EDS);
        if (eds != null) {
            try {
                boolean valid = EDSProcessor.getInstance().processEDS(cdcpMessage, eds);
                logger.info("EDS_PTN:"+eds.getTaskSerial()+" valid = "+valid);
                if (!valid) {
                    logger.info("EDS_PTN:"+eds.getTaskSerial()+" valid = "+valid+" add = "+eds.getAdditinalInfo());
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }

        if (object != null && object instanceof FtpInfo ) {
            FtpInfo ftpInfo = (FtpInfo) object;
            migrateManager.handleSbiFinish(ftpInfo, taskSerial);
        }
        else if (object != null && object instanceof ObjectInfo ) {
            migrateManager.handleSbiFinish((ObjectInfo)object,taskSerial);
        }
        else if (object != null) {
            migrateManager.handleSbiFailed(object.toString(), taskSerial);
        } else {
            logger.error("error message object = : "+object);
        }


    }


    private void handleNodeMessage(Message message) {

                CdcpMessage cdcpMessage = convert(message);
                if (cdcpMessage != null) {
                    if (cdcpMessage.getType().equals(CDCPConstants.MESSAGE_TYPE_NODE_STARTUP)) {
                        logger.info("handleNodeStartup:"+cdcpMessage);
                        System.out.println("handleNodeStartup = " + cdcpMessage);
                        String nodeName = cdcpMessage.getNodeName();
                        String hmtlAdapterPort = cdcpMessage.getJmxHtmlPort()+"";
                        String jmxmpPort = cdcpMessage.getJmxmpPort()+"";
                        String host = cdcpMessage.getIpAddress();
                        String workPath = cdcpMessage.getWorkPath();
                        try {
                            SysNode node = updateNodeStatus(workPath,nodeName,hmtlAdapterPort,host,jmxmpPort);
                            System.out.println("update node = " + node.getDn());
                            dispatchEmses(node);
                        } catch (Exception e) {
                            logger.error(e,e);
                        }


                        Object receiveUnFinishTasks = cdcpMessage.getAttribute("ReceiveUnFinishTasks");
                        if (receiveUnFinishTasks == null || ((String)receiveUnFinishTasks).equalsIgnoreCase("true")) {
                            try {
                                dispatchUnfinishTasks(nodeName);
                            } catch (Exception e) {
                                logger.error(e, e);
                            }
                        }
                    }
                    else if (cdcpMessage.getType().equals(CDCPConstants.MESSAGE_TYPE_NODE_ALIVE)) {


                        String nodeName = cdcpMessage.getNodeName();
                        logger.info("nodeAlive:"+nodeName);
                        String hmtlAdapterPort = cdcpMessage.getJmxHtmlPort()+"";
                        String jmxmpPort = cdcpMessage.getJmxmpPort()+"";
                        String host = cdcpMessage.getIpAddress();
                        String workPath = cdcpMessage.getWorkPath();
                        try {
                            SysNode node = updateNodeStatus(workPath,nodeName,hmtlAdapterPort,host,jmxmpPort);
                        } catch (Exception e) {
                            logger.error(e,e);
                        }
                    }

                }




    }

    private void dispatchEmses(SysNode node) {
        try {
            List<Ems> emsList = JpaClient.getInstance().findObjects("select c from Ems c where c.sysNodeDn = '" + node.getDn() + "'");
            System.out.println("emsList = " + emsList.size());
            if (emsList != null) {
                System.out.println("createJmxmpProxy-> "+node.getIpaddress()+":"+node.getJmxport() );
                 MBeanProxy<NodeAdminMBean> jmxmpProxy =   CdcpServerUtil.createNodeAdminProxy(node.getIpaddress(), node.getJmxport());
                for (Ems ems : emsList) {
                    if (ems.getMonitored() == null ||
                            ems.getMonitored().intValue() == CdcpDictionary.EMSISMONITORED.YES.value
                            || ems.getDn().equals("TZ-OTNU31-1-P"))
                        jmxmpProxy.proxy.newEms(ems);
                    else {
                        logger.info("ems : "+ems.getDn()+" is not monitored ,will be ignored !");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e, e);
        }
    }

    private void dispatchUnfinishTasks(String nodeName) throws Exception {
        List<Task> tasks = JpaClient.getInstance().findObjects("select c from Task c where c.status in (" +
                Constants.TASK_STATUS_SBI_EXCEPTION + "," + Constants.TASK_STATUS_SBI_RUNNING + ") order by c.id desc");
        logger.info((tasks == null ? 0 : tasks.size()) + " sbi unfinished tasks found");
        HashSet<String> dispatchedEms = new HashSet<String>();
        for (int i = 0; i < tasks.size(); i++) {

            Task task = tasks.get(i);
            logger.info("Process task : ["+task.getTaskObject()+"] "+task.getDn());
            if (task.getScheduleId() == null || task.getScheduleId() < 0) {
                CdcpServerUtil.updateTask(task.getDn(),Constants.TASK_STATUS_ERROR,"异常采集任务，已过期作废.");
                logger.info(task.getDn()+" expired");
                continue;
            }
            String emsdn = task.getTaskObject();
            if (dispatchedEms.contains(emsdn)) {
                CdcpServerUtil.updateTask(task.getDn(),Constants.TASK_STATUS_ERROR,"异常采集任务，已过期作废.");
                logger.info(task.getDn()+" expired");
                continue;
            }

            Ems ems = CdcpServerUtil.findEms(emsdn);
            if (ems == null) {
                CdcpServerUtil.updateTask(task.getDn(),Constants.TASK_STATUS_ERROR,"(无法找到EMS:"+emsdn+")异常采集任务，已过期作废.");
                logger.error("Faild to find ems : "+emsdn);
                continue;
            }
            if (ems.getMonitored() != null && ems.getMonitored().intValue() == CdcpDictionary.EMSISMONITORED.NO.value) {
                CdcpServerUtil.updateTask(task.getDn(),Constants.TASK_STATUS_ERROR,"(EMS禁用)异常采集任务，已过期作废.");
                logger.info("Unmonitored ems : "+emsdn+" ,task : "+task.getDn()+" will be ignored");
                continue;
            }
            SysNode node = CdcpServerUtil.findNode(emsdn);
            if (ems != null && node != null && node.getName().equals(nodeName) ) {
                logger.info("Recover SBI Task : "+task.getDn());
                MBeanProxy<NodeAdminMBean> jmxmpProxy =   CdcpServerUtil.createNodeAdminProxy(node.getIpaddress(), node.getJmxport());
                try {
                    EmsJob job = new EmsJob();
                    job.setSerial(task.getDn());
                    job.setJobType(EmsJob.JOB_TYPE_SYNC_EMS);
                    job.setEms(ems);

                    jmxmpProxy.proxy.executeJob(job);
                    logger.info("createJmxmpProxy-> "+node.getIpaddress()+":"+node.getJmxport()+"\\"+ems.getDn());
                    dispatchedEms.add(emsdn);
                } catch (NodeException e) {
                    logger.error("Failed ! createJmxmpProxy-> "+node.getIpaddress()+":"+node.getJmxport()+"\\"+ems.getDn());
                    logger.error(e, e);
                } finally {
                    if (jmxmpProxy != null)
                    jmxmpProxy.close();
                }

                if (task.getStatus().intValue() == Constants.TASK_STATUS_SBI_EXCEPTION) {
                    CdcpServerUtil.updateTask(task.getDn(),Constants.TASK_STATUS_SBI_RUNNING,"接口程序重启");
                }

            }
        }
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

    private SysNode updateNodeStatus(String workPath,String nodeName,String hmtlAdapterPort,String host,String jmxmpPort) throws Exception {
        SysNode node = null;
        List<SysNode> nodes = JpaClient.getInstance().findObjects("select c from SysNode c where c.name = '"+nodeName+"'");
        if (nodes != null && nodes.size() > 0) {
//            logger.info("node existed:");
            node = nodes.get(0);
            if (!(node.getHttpport()+"").equals(hmtlAdapterPort)
                    || (node.getIpaddress() == null)
                    || (!node.getIpaddress().equals(host))
                    || (node.getJmxport() == null)
                    || (!(node.getJmxport()+"").equals(jmxmpPort)
                    || (node.getState().intValue() == SysConst.NODE_STATE_DIE)
            )
                    ) {
                node.setHttpport(hmtlAdapterPort == null ? null : Integer.parseInt(hmtlAdapterPort));
                node.setJmxport(jmxmpPort == null ? null : Integer.parseInt(jmxmpPort));
                node.setIpaddress(host);
                node.setTag2(workPath);
                node.setStatus("已启动");
                node.setState(SysConst.NODE_STATE_START);
                node.setTag1("CDCP");
                JpaClient.getInstance().saveObject(-1,node);
            }
        } else {
            logger.info("node node existed ,try to create it:"+nodeName);
            node = new SysNode();
            node.setDn(nodeName);
            node.setName(nodeName);
            node.setHttpport(hmtlAdapterPort == null ? null : Integer.parseInt(hmtlAdapterPort));
            node.setJmxport(jmxmpPort == null ? null : Integer.parseInt(jmxmpPort));
            node.setIpaddress(host);
            node.setStatus("已启动");
            node.setState(SysConst.NODE_STATE_START);
            node.setTag1("CDCP");
            node.setTag2(workPath);
            JpaClient.getInstance().saveObject(-1, node);
        }
        return node;
    }

    public static void main(String[] args) throws Exception{
        SpringContext.getInstance().setJMSSupport(new JMSSupportSpringImpl
                (new ActiveMQConnectionFactory(SysProperty.getString("activeMQUrl"))));

        new CdcpNodeMaster();
        synchronized (CdcpNodeMaster.class) {
            CdcpNodeMaster.class.wait();
        }
    }

}
