package com.alcatelsbell.cdcp.server;
import com.alcatelsbell.cdcp.api.plugins.EmsServerJob;
import com.alcatelsbell.cdcp.api.plugins.EmsServerJobContext;
import com.alcatelsbell.cdcp.nodefx.EmsJob;
import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import com.alcatelsbell.nms.valueobject.sys.SysNode;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 上午10:56
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class EmsSBIJob implements Job {
    public static final String ATTRIBUTE_SCHEDULE = "schedule";
    private Log logger = LogFactory.getLog(getClass());
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
       synchronized (EmsSBIJob.class) {
           try {
               doExecute(jobExecutionContext);
           } catch (Throwable e) {
               logger.error(e, e);
           }
       }


    }

    private void doPluginExecute(JobExecutionContext jobExecutionContext) {
        try {
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            Schedule schedule = (Schedule) jobDataMap.get(ATTRIBUTE_SCHEDULE);

            String taskObjects = schedule.getTaskObjects();
            String[] dns = taskObjects.split("#");
            for (int i = 0; i < dns.length; i++) {
                String emsdn = dns[i];
                String serial = emsdn + "@" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
                try {
                    Ems ems = (Ems) JpaClient.getInstance().findObjectByDN(Ems.class, emsdn);
                    if (ems == null) throw new Exception("无法找到EMS："+emsdn);
                    MigrateManager.getInstance().registerEmsJob(schedule,emsdn,serial);




                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void doExecute(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Schedule schedule = (Schedule) jobDataMap.get(ATTRIBUTE_SCHEDULE);

        String taskObjects = schedule.getTaskObjects();
        String[] dns = taskObjects.split("#");
        for (int i = 0; i < dns.length; i++) {
            String emsdn = dns[i];
            String serial = emsdn+"@"+ new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());


            MBeanProxy<NodeAdminMBean> jmxmpProxy = null;
            try {
                Ems ems = (Ems)JpaClient.getInstance().findObjectByDN(Ems.class,emsdn);
                if (ems == null) throw new Exception("无法找到EMS："+emsdn);
                MigrateManager.getInstance().registerEmsJob(schedule,emsdn,serial);


                String emsversion = ems.getEmsversion();
                if ("JavaClass".equals(emsversion)) {
                    String controlName = ems.getControlName();
                    if (controlName != null) {
                        EmsServerJob emsServerJob = (EmsServerJob) Class.forName(controlName).newInstance();
                        EmsServerJobContext context = new EmsServerJobContext();
                        context.setEms(ems);
                        CdcpServerUtil.createEmsLog(ems.getDn(), "采集任务开始", "采集任务开始");
                        try {
                            emsServerJob.executeJob(context);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            CdcpServerUtil.createEmsLog(ems.getDn(), "采集任务异常", "采集任务异常:"+e.getMessage());
                        }
                        CdcpServerUtil.createEmsLog(ems.getDn(), "采集任务结束", "采集任务结束");
                    }
                } else {
                    String sysNodeDn = ems.getSysNodeDn();
                    SysNode sysNode = (SysNode)JpaClient.getInstance().findObjectByDN(SysNode.class,sysNodeDn);
                    if (sysNode == null) throw new Exception("无法找到EMS："+emsdn+" 对应的采集节点:"+sysNodeDn);
                    logger.info("createJmxmpProxy-> "+sysNode.getIpaddress()+":"+sysNode.getJmxport()+"\\"+ems.getDn());
                    jmxmpProxy =   CdcpServerUtil.createNodeAdminProxy(sysNode.getIpaddress(), sysNode.getJmxport());
                    EmsJob job = new EmsJob();
                    job.setSerial(serial);
                    job.setEms(ems);
                    job.setJobType(EmsJob.JOB_TYPE_SYNC_EMS);
                    HashMap map = new HashMap();
                    if (schedule.getArguments() != null) {
                        String[] split = schedule.getArguments().split("&");

                        for (String s : split) {
                            String k = s.substring(0,s.indexOf("="));
                            String v = s.substring(s.indexOf("=")+1);
                            map.put(k, v);

                        }


                    }


                    int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == java.util.Calendar.FRIDAY)
                        map.put("logical","true");

                    ems.setUserObject(map);


                    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY ) {
                        logger.info("Today is  weekend , job cancelled !");
                    }
                    else {
                        jmxmpProxy.proxy.executeJob(job);
                    }
                }
            }  catch (Exception e) {
                logger.error(e, e);
                MigrateManager.getInstance().handleSbiFailed(e.getMessage(), serial);
            }  finally {
                if (jmxmpProxy != null)
                    try {
                        jmxmpProxy.close();
                    } catch (IOException e) {
                        logger.error(e, e);
                    }
            }

        }
    }



    public static void main(String[] args) {
        System.out.println(DateUtil.formatDate(new Date(),"yyyyMMdd-HHmmss"));

    }
}
