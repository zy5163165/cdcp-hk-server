package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.api.EmsMgmtClient;
import com.alcatelsbell.cdcp.api.EmsMgmtIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.nodefx.CorbaEms;
import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.cdcp.nodefx.NodeException;
import com.alcatelsbell.cdcp.server.CdcpOverviewMBean;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.cdcp.util.MigrateThread;

import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.*;
import com.alcatelsbell.nms.interfaces.publics.EmsDataCache;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.CollectionUtil;
import com.alcatelsbell.nms.util.FileUtil;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.util.ObjectUtil;
import com.alcatelsbell.nms.valueobject.CdcpDictionary;
import com.alcatelsbell.nms.valueobject.sys.Ems;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;
import org.quartz.SchedulerException;

import javax.management.*;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Author: Ronnie.Chen
 * Date: 2015/4/10
 * Time: 11:03
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpOverview implements CdcpOverviewMBean {
    private Log logger = LogFactory.getLog(getClass());

    @Override
    public String showEmsOverview() {

        try {
            List<Ems> emsList = JpaServerUtil.getInstance().findObjects("select c from Ems c order by c.synEndTime desc");
            HashMap<Object,List> groups = group(emsList, "protocalType");

            StringBuffer sb = new StringBuffer();

            sb.append(getDomainEms("PTN",groups.get(CdcpDictionary.PROTOCALTYPE.PTN.value)));
            sb.append(getDomainEms("SDH",groups.get(CdcpDictionary.PROTOCALTYPE.SDH.value)));
            sb.append(getDomainEms("WDM",groups.get(CdcpDictionary.PROTOCALTYPE.WDM.value)));
            sb.append(getDomainEms("OTN",groups.get(CdcpDictionary.PROTOCALTYPE.OTN.value)));

            return sb.toString();
        } catch (Exception e) {
            logger.error(e, e);
            return e.getMessage();
        }
    }

    @Override
    public String showMigrateTasks() {
        LinkedBlockingQueue queue = MigrateManager.getInstance().getQueue();
        ThreadPoolExecutor executor = MigrateManager.getInstance().getExecutor();
        int activeCount = executor.getActiveCount();
        long taskCount = executor.getTaskCount();
        long completedTaskCount = executor.getCompletedTaskCount();

        StringBuffer sb = new StringBuffer();
        sb.append("taskCount = "+taskCount+"; complete = "+completedTaskCount+"").append("<br>");
        sb.append("activeCount="+activeCount).append("<br>");
        sb.append("<br>");
        sb.append("queue size = " + queue.size() + "<br>");

        Iterator iterator = queue.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof MigrateRunnable) {
                sb.append(dateToString(((MigrateRunnable) next).getTime())+" - ");
                sb.append(((MigrateRunnable) next).getSerial()+":");
                sb.append(((MigrateRunnable) next).getSbiResult());
                sb.append("<br>");
                sb.append("<br>");

            }

        }
        return sb.toString();
    }
    private List<Task> queryUnfinishedDeviceTasks(int hours) throws Exception {
        HashMap map = new HashMap();
        map.put("date",new Date(new Date().getTime() - 3600l * 1000l * hours));
        List<Task> tasks =JpaServerUtil.getInstance().findObjects("select c from Task c where c.name like 'DEVICE:%' and c.status in (7,3) and c.startTime > :date",null,map,null,null);
        return tasks;
    }

    public String listUnfinishDeviceTasks(String hours) {
        if (hours == null || hours.trim().isEmpty()) hours = ""+(24 * 3);
        List<Task> tasks = null;
        try {
            tasks = queryUnfinishedDeviceTasks(Integer.parseInt(hours));
        } catch (Exception e) {
            logger.error(e, e);
            return "Exception:"+e.getMessage();
        }
        StringBuffer sb = new StringBuffer("list tasks in "+hours+" hours !<br>");
        for (Task task : tasks) {
            sb.append(task.getName()+" - "+task.getStartTime()+" <br>");
        }
        return sb.toString();
    }

    public String runUnfinishDeviceTasks(String hours) {
        if (hours == null || hours.trim().isEmpty()) hours = ""+(24 * 3);
        List<Task> tasks = null;
        try {
            tasks = queryUnfinishedDeviceTasks(Integer.parseInt(hours));
        } catch (Exception e) {
            logger.error(e, e);
            return "Exception:"+e.getMessage();
        }
        StringBuffer sb = new StringBuffer("list tasks in "+hours+" hours !<br>");
        for (Task task : tasks) {
            try {
                String result = executeDeviceTask(task);
                sb.append(task.getName()+" - "+ result+" <br>");
            } catch (NodeException e) {
                logger.error(e, e);
                sb.append(task.getName()+" - ERROR : "+ e.getMessage()+" <br>");
            }

        }
        return sb.toString();
    }

    private String executeDeviceTask(Task task) throws NodeException {
        return CdcpServerUtil.syncDeviceTask(task);
    }

    @Override
    public String showTaskHistorys() {
        List<Task> objects = null;
        try {
            objects = JpaServerUtil.getInstance().findObjects("select c from Task c where c.name not like 'DEVICE:%' and c.startTime is not null order by c.startTime desc", null, null, 0, 500);
        } catch (Exception e) {
            logger.error(e, e);
            return e.getMessage();
        }

        StringBuffer sb = new StringBuffer();
        for (Task task : objects) {
            Ems ems = null;
            try {
                ems = CdcpServerUtil.findEms(task.getTaskObject());
            } catch (Exception e) {
                logger.error(e, e);
            }
            if (ems != null) {
                sb.append("["+getDomain(ems.getProtocalType())+"] - ");
            }

            sb.append("["+task.getTaskObject()+"]");
            if (task.getStatus() == Constants.TASK_STATUS_FINISHED) {
                if (task.getEndTime() != null && task.getStartTime() != null) {
                    long t1 = task.getEndTime().getTime() - task.getStartTime().getTime();
                    float f = (float)(t1 / 100000);

                    double hour =  formatDouble(f/ 36f);
                    sb.append(" <font color=\"#00FF00\">Task Finish</font> time = "+hour+" hour ,end@"+task.getEndTime());
                } else
                    sb.append(" <font color=\"#00FF00\">Task Finish</font> time = ");

            } else if (task.getStatus() == Constants.TASK_STATUS_MIGRATING_EXCEPTION){
                sb.append("<font color=\"#FF0000\"> !MIG EXP</font> :"+task.getDescription());
            } else if (task.getStatus() == Constants.TASK_STATUS_SBI_EXCEPTION){
                sb.append("<font color=\"#FF00FF\"> !SBI EXP :</font>"+task.getDescription());
            }else if (task.getStatus() == Constants.TASK_STATUS_SBI_RUNNING){
                sb.append(" SBI RUNNING : "+task.getDescription());
            }

            sb.append("<br>");
        }
        return sb.toString();
    }

    public static double formatDouble(double db) {
        if (!(db > 0)) return 0.0d;
        if (db == Double.NaN)
            return 0;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        try {
            if (db < 0.01 && db != 0)
                return 0.01;
            else
                return Double.parseDouble(df.format(db));
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }
    @Override
    public String querySqlMgr(String sql) {

        return query(JPASupportFactory.createJPASupport(),sql);
    }

    @Override
    public String querySqlData(String sql) {
        return query(new JPASupportSpringImpl("entityManagerFactoryData"),sql);
    }

    @Override
    public String showEmsMigrateLog(String emsdn) {
        File dir = new File("../logs");
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            File f = null;
            for (File file : files) {
                if (file.getName().contains(emsdn)) {
                    if (f == null)
                        f = file;
                    else {
                        if (f.lastModified() < file.lastModified())
                            f = file;
                    }


                }
            }

            if (f != null) {
                byte[] bs = FileUtil.readFile(f);
                return new String(bs);
            }
        }

        return "log not found";

    }

    @Override
    public String syncDevice(String deviceDn) {
        EmsMgmtIFC anyOneService = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        String s = null;
        try {
            s = anyOneService.manualSyncDevice(deviceDn);
        } catch (Exception e) {
            logger.error(e, e);
            return "error : "+e;
        }
        return s;
    }

    @Override
    public String syncDeviceSection(String deviceDn) {
        String emsdn = deviceDn.substring(deviceDn.indexOf("EMS:")+4,deviceDn.indexOf("@"));
        try {
            return CdcpServerUtil.syncDeviceSection(emsdn,deviceDn);
        } catch (NodeException e) {
            logger.error(e, e);
            return e.toString();
        }

    }

    @Override
    public String syncEmsByName(String emsdn, String paras) {
        String s = null;
        try {
            s = EmsMgmtClient.getInstance().manualSyncEms(emsdn + (paras == null || paras.isEmpty() ? "" : "?"+paras));
        } catch (Exception e) {
            return "ERROR:"+e.toString();
        }
        return s;
    }

    @Override
    public String syncEmsByNodeDn(String nodeDn, String paras) {
        StringBuffer sb = new StringBuffer();
        List<Ems> emsList = null;
        try {
            emsList = JpaServerUtil.getInstance().findObjects("select c from Ems c where c.sysNodeDn = '" + nodeDn + "'");
        } catch (Exception e) {
            return e.toString();
        }
        if (emsList != null) {
            for (Ems ems : emsList) {
                sb.append(ems.getDn()+":");
                String s = null;
                try {
                    s = EmsMgmtClient.getInstance().manualSyncEms(ems.getDn() + (paras == null || paras.isEmpty() ? "" : "?" + paras));
                    sb.append(s);
                } catch (Exception e) {
                    sb.append(e.toString());
                }

                sb.append("<br>");

            }
        }
        return sb.toString();
    }


    public String initEmsSchedule(String emsdn,String cron) {
        if (cron == null || cron.trim().isEmpty())
            cron = "0 10 17 * * ?";
        Schedule schedule = new Schedule();
        schedule.setTaskObjects(emsdn);
        schedule.setTimeType(Schedule.TIME_TYPE_CRON);
        schedule.setTimeExpression(cron);
        schedule.setStatus(Schedule.STATUS_ACTIVE);
        schedule.setJobType("MIGRATE-RESOURCE");
        schedule.setDn("DEFAULT_" + emsdn);

        try {
            Object objectByDN = JpaClient.getInstance().findObjectByDN(Schedule.class, schedule.getDn());
            if (objectByDN == null) {
                schedule = (Schedule)JpaClient.getInstance().saveObject(-1, schedule);
            }  else {
                return "Schedule : "+schedule.getDn()+" already  existed !";
            }
        } catch (Exception e) {
            logger.error(e, e);
        }

        try {
            ScheduleService.getInstance().reSchedule(schedule);
        } catch (Exception e) {
            logger.error(e, e);
            return "ERROR  : "+e.toString();
        }

        return "Success , schedule cron = "+schedule.getTimeExpression();
    }

    @Override
    public String scheduleEmsWithCronExp(String emsdn, String cron) {
        if (cron == null) cron = "0 10 17 * * ?";
        List<Schedule> objects = null;
        try {
            objects = JpaClient.getInstance().findObjects("select c from Schedule c where taskObjects = '" + emsdn + "'");
        } catch (Exception e) {
            logger.error(e, e);
        }
        if (objects != null && objects.size() > 0 ) {
            for (Schedule schedule : objects) {
                schedule.setTimeExpression(cron);

                try {
                    schedule = (Schedule)JpaClient.getInstance().saveObject(-1, schedule);
                    ScheduleService.getInstance().reSchedule(schedule);
                } catch (Exception e) {
                    logger.error(e, e);
                    return "ERROR  : "+e.toString();
                }
            }
        }  else
            return initEmsSchedule(emsdn,cron);
        return "reschedule success";
    }

    @Override
    public String changeUserPassword(String emsdn, String user, String password) {
        try {
            Ems ems = CdcpServerUtil.findEms(emsdn);
            String additionalinfo = ems.getAdditionalinfo();

            CorbaEms corbaEms = new CorbaEms(ems);
            String corbaUserName = corbaEms.getCorbaUserName();
            String corbaPassword = corbaEms.getCorbaPassword();

            additionalinfo = additionalinfo.replace("config_corbaUserName|"+corbaUserName,"config_corbaUserName|"+user);
            additionalinfo = additionalinfo.replace("config_corbaPassword|"+corbaPassword,"config_corbaPassword|"+password);
            ems.setAdditionalinfo(additionalinfo);
            ems = (Ems)JpaServerUtil.getInstance().saveObject(-1,ems);

            MBeanProxy<NodeAdminMBean> nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(ems.getDn());

            if (nodeAdminProxy != null) {
                try {
                    nodeAdminProxy.proxy.updateEms(ems);
                } catch (NodeException e) {
                    logger.error(e, e);
                } finally {
                    nodeAdminProxy.close();
                }
            }


        } catch (Exception e) {
            logger.error(e, e);
        }

        //    CdcpNodeMaster


        return null;
    }

    @Override
    public String removeEms(String emsdn) {
        try {
            executeDeleteMgr("delete  from Ems c where c.dn = '"+emsdn+"'");
            executeDeleteMgr("delete  from Schedule c where c.taskObjects = '"+emsdn+"'");
            executeDelete("delete  from CDevice c where c.emsName = '" + emsdn + "'");
            executeDelete("delete  from CPTP c where c.emsName = '" + emsdn + "'");
            executeDelete("delete  from CEms c where c.dn = '" + emsdn + "'");
        } catch (Exception e) {
            logger.error(e, e);
            return e.toString();
        }
        return "Removed Success";
    }

    @Override
    public String callIrmSuccess(String taskDn) {
        Task task = CdcpServerUtil.findTask(taskDn);
        IrmsClientUtil.callBackIRM(task, 0, "");
        return "callback success";
    }

    @Override
    public String callIrmSectionSuccess(String projectName){
         IrmsClientUtil.callBackIRMDeviceSectionSync("projectName",0,"");
        return "success";
    }

    protected void executeDelete(String ql) {
        logger.info("Delete QL : " + ql);
        JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
        try {
            jpaSupport.begin();
            JPAUtil.getInstance().executeQL(jpaSupport, ql);
            jpaSupport.end();
        } catch (Exception e) {
            logger.error(e, e);
            jpaSupport.rollback();
        } finally {
            jpaSupport.release();
        }
    }
    protected void executeDeleteMgr(String ql) {
        logger.info("Delete QL : " + ql);
        JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
        try {
            jpaSupport.begin();
            JPAUtil.getInstance().executeQL(jpaSupport, ql);
            jpaSupport.end();
        } catch (Exception e) {
            logger.error(e, e);
            jpaSupport.rollback();
        } finally {
            jpaSupport.release();
        }
    }

    private String query(JPASupport jpaSupport,final String sql) {

        final StringBuffer sb = new StringBuffer();
        try {
            EntityManager entityManager = jpaSupport.getEntityManager();
            if (entityManager instanceof HibernateEntityManager) {
                Session session = ((HibernateEntityManager) entityManager).getSession();

                session.doWork(new Work() {
                    @Override
                    public void execute(Connection conn) throws SQLException {
                        PreparedStatement stat = conn.prepareStatement(sql);
                        ResultSet resultSet = stat.executeQuery();

                        int columnCount = resultSet.getMetaData().getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = resultSet.getMetaData().getColumnName(i);
                            sb.append("[" + i + "]" + columnName + " ");

                        }
                        sb.append("<br>");
                        int row = 1;
                        while (resultSet.next() && row < 10000) {
                            sb.append(row++ + " : ");
                            for (int i = 1; i <= columnCount; i++) {
                                Object object = resultSet.getObject(i);
                                //   System.out.print(row++ + " : ");
                                sb.append("[" + i + "]" + object);
                            }
                            sb.append("<br>");
                            sb.append("-----------------------------------------------------------------------<br>");
                        }

                        resultSet.close();
                        stat.close();                    }
                });
            } else {
                throw new Exception("Not Hibernate Entity Manager :" + entityManager);
            }
        } catch ( Exception e) {
             return e.getMessage();
        }
        return sb.toString();

    }

    private String getDomain(Integer pt) {
        if (pt == null) return "PTN";
        String domain = "";
        if (pt == CdcpDictionary.PROTOCALTYPE.PTN.value)
            return "PTN";
        if (pt == CdcpDictionary.PROTOCALTYPE.SDH.value)
            return "SDH";
        if (pt == CdcpDictionary.PROTOCALTYPE.WDM.value)
            return "WDM";
        if (pt == CdcpDictionary.PROTOCALTYPE.OTN.value)
            return "OTN";
        return "";

    }
    @Override
    public String showMigrateHistory() {
        List<Task> objects = null;
        try {
            objects = JpaServerUtil.getInstance().findObjects("select c from Task c where c.name not like 'DEVICE:%' and c.endTime is not null order by c.endTime desc", null, null, 0, 500);
        } catch (Exception e) {
            logger.error(e, e);
            return e.getMessage();
        }

        StringBuffer sb = new StringBuffer();
        for (Task task : objects) {
            Ems ems = null;
            try {
                ems = CdcpServerUtil.findEms(task.getTaskObject());
            } catch (Exception e) {
                logger.error(e, e);
            }
            if (ems != null) {
                sb.append("["+getDomain(ems.getProtocalType())+"] - ");
            }

            sb.append("["+task.getTaskObject()+"] ");
            sb.append("     ");
            if (task.getStatus() == Constants.TASK_STATUS_FINISHED) {
                if (task.getEndTime() != null) {
                    if (task.getTag3() != null) {
                        long t1 = task.getEndTime().getTime() - Long.parseLong(task.getTag3());
                        float f = (float) (t1 / 100000);


                        double hour =  formatDouble(f/ 36f);
                        sb.append("<font color=\"#00FF00\">  MIG OK </font> time = " + hour + " hour ,end@" + task.getEndTime());
                    } else
                        sb.append("<font color=\"#00FF00\">  MIG OK </font> time = ? hour ,end@" + task.getEndTime());

                } else
                    sb.append(" MIG OK");

            } else if (task.getStatus() == Constants.TASK_STATUS_MIGRATING_EXCEPTION){
                sb.append("<font color=\"#FF0000\"> !MIG EXP :\"</font>\""+task.getDescription() );
            } else if (task.getStatus() == Constants.TASK_STATUS_SBI_EXCEPTION){
                sb.append(" !SBI EXP :"+task.getDescription());
            }
            sb.append("<br>");
        }
        return sb.toString();
    }

    public static CdcpOverview bind(String domainName) {
        CdcpOverview overview = new CdcpOverview();
        try {
            ArrayList<MBeanServer> mBeanServerList = MBeanServerFactory.findMBeanServer(null);
            MBeanServer mbeanServer = null;
            if (mBeanServerList.size() > 0) {
                mbeanServer  = mBeanServerList.get(0);
                ObjectName ifcON = new ObjectName(domainName +":name=CdcpOverview");
                mbeanServer.registerMBean(overview, ifcON);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return overview;
    }


    private String getDomainEms(String domain,List<Ems> emsList) {
        if (emsList == null) return "";
        StringBuffer sb = new StringBuffer("-------------- "+domain+"\r\n<br>");
        HashMap<Object, List> map = group(emsList, "vendordn");
        for (Object vendor : map.keySet()) {
            sb.append(vendor).append("\r\n<br>").append("<br>");

            List<Ems> list = map.get(vendor);
            for (Ems ems : list) {
                sb.append(ems.getStatus() == 0 ? "<font color=\"#00FF00\">OK</font>" : "<font color=\"#FF0000\">!ERROR!</font>").append("   ");
                sb.append(ems.getDn()+" SYNTIME: "+dateToString(ems.getSynEndTime()));

                if (ems.getStatus() != 0 ) {
                    sb.append(ems.getExceptionCode()+":"+ems.getExceptionDetail());
                }
                sb.append("\r\n<br>");
            }
            sb.append("\r\n<br>");
        }
        sb.append("<br>");
        return sb.toString();
    }

    private String dateToString(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }


    public static HashMap<Object,List>  group(Collection collection,String fieldName) {
        HashMap<Object,List> map = new HashMap<Object, List>();
        for (Object o : collection) {
            try {
                Object value = o.getClass().getMethod("get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1)).invoke(o);
                if (value != null) {
                    List c = map.get(value);
                    if (c == null) {
                        c = new ArrayList();
                        map.put(value,c);
                    }
                    c.add(o);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map ;
    }

    public static void main(String[] args) {
        Object o = ObjectUtil.readObjectByPath("d:\\1507\\HZ-U2000-1-OTN.ne");
        System.out.println("o = " + o);
    }



}
