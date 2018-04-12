package com.alcatelsbell.cdcp.server.services;

import com.alcatelsbell.cdcp.api.TaskMgmtIFC;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.ws.CdcpNBIService;
import com.alcatelsbell.cdcp.nbi.ws.CdcpNBIService2;
import com.alcatelsbell.cdcp.nodefx.NodeException;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.server.ScheduleService;
import com.alcatelsbell.cdcp.server.snmp.PlanExecutor;
import com.alcatelsbell.hippo.framework.service.DefaultServiceImpl;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.sys.Log;


import javax.xml.ws.Endpoint;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午4:48
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TaskMgmtRemoteImpl  extends DefaultServiceImpl implements TaskMgmtIFC {

    private CdcpNBIService2 service = new  CdcpNBIService2();
    public TaskMgmtRemoteImpl() throws RemoteException {
        if (!"false".equals(SysProperty.getString("nbiWs","true"))) {
            try {
                String nbiWsUrl = SysProperty.getString("nbiWsUrl", "http://0.0.0.0:9090/cdcpnbi");
                LogUtil.info(TaskMgmtRemoteImpl.class,"URL=" + nbiWsUrl);
                Endpoint.publish(nbiWsUrl, service);
                LogUtil.info(TaskMgmtRemoteImpl.class,"Publish success.");
            } catch (Exception e) {
                LogUtil.error(this,e, e);
            }
        }
    }


    public java.lang.String getJndiNamePrefix(){
        return Constants.SERVICE_NAME_CDCP_TASK;
    }
    @Override
    public Schedule createSchedule(Schedule schedule) throws RemoteException {
//        if (schedule.getJobType().equals("XML")) {
//            String arguments = schedule.getArguments();
//            PlanExecutor planExecutor = new PlanExecutor(arguments);
//            String result = planExecutor.run();
//
//            schedule.setJobName(result);
//            return schedule;
//        }
        try {
            if (schedule.getDn() == null) schedule.setDn(SysUtil.nextDN());
            schedule = (Schedule) JpaClient.getInstance().saveObject(-1,schedule);
            ScheduleService.getInstance().applySchedule(schedule);
        } catch (Exception e) {
            logger.error(e,e);
            throw new RemoteException(e.getMessage(),e);
        }
        return schedule;
    }

    @Override
    public Schedule modifySchedule(Schedule schedule) throws RemoteException {
        try {
            schedule = (Schedule) JpaClient.getInstance().storeObjectByDn(-1, schedule);
            ScheduleService.getInstance().reSchedule(schedule);
        } catch (Throwable e) {
        	e.printStackTrace();
            logger.error(e,e);
            throw new RemoteException(e.getMessage(),e);
        }
        return schedule;
    }

    @Override
    public void removeSchedule(Long scheduleId) throws RemoteException {
        try {
            Schedule schedule = (Schedule)JpaClient.getInstance().findObjectById(Schedule.class, scheduleId);
            if (schedule != null) {
                if (schedule.getDn().contains("DEFAULT")) {
                    schedule.setStatus(Schedule.STATUS_INACTIVE);
                    JpaClient.getInstance().storeObjectByDn(-1,schedule);
                } else
                    JpaClient.getInstance().removeObject(schedule);

                ScheduleService.getInstance().destorySchedule(schedule);
            }
        } catch (Exception e) {
            logger.error(e,e);
            throw new RemoteException(e.getMessage(),e);
        }
    }

    @Override
    public String getGlobalWarning() throws RemoteException {
        //todo
        return "";
    }

    @Override
    public Task getTask(Long scheduleId) throws RemoteException {
        String ql = "select c from Task c where c.scheduleId = "+scheduleId+" order by c.id desc";
        Task task = null;
        try {
            task = (Task) JpaClient.getInstance().findOneObject(ql);
        } catch (Exception e) {
            logger.error(e,e);
            throw new RemoteException(e.getMessage(),e);
        }
        return task;
    }

    /**
     *
     * @return  <ScheduleId,Task>
     * @throws RemoteException
     */
    @Override
    public HashMap<Long, Task> getAllTasks() throws RemoteException {
        List<Task> tasks = null;
        try {
            tasks = JpaClient.getInstance().findAllObjects(Task.class);
        } catch (Exception e) {
            logger.error(e,e);
            throw new RemoteException(e.getMessage(),e);
        }
        HashMap<Long,Task> taskMap = new HashMap<Long, Task>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            Task task1 = taskMap.get(task.getScheduleId());
            if (task1 == null || task1.getCreateDate().before(task.getCreateDate())) {
                taskMap.put(task.getScheduleId(),task);
            }
        }
        return taskMap;
    }

    @Override
    public List<Task> getHistoryTasks(Long scheduleId) throws RemoteException {
        String ql = "select c from Task c where c.scheduleId = "+scheduleId+" order by c.id desc";
        try {
            return JpaClient.getInstance().findObjects(ql);
        } catch (Exception e) {
            logger.error(e,e);
            throw new RemoteException(e.getMessage(),e);
        }
    }

    @Override
    public List<Log> getLogs(Long scheduleId) throws RemoteException {
        try {
//            Schedule schedule = (Schedule)JpaClient.getInstance().findObjectById(Schedule.class,scheduleId);
            String ql = "select l from Log l,Task t where t.scheduleId = "+ scheduleId+" and t.dn = l.source order by l.id desc";

            return JpaClient.getInstance().findObjects(ql,null,null,0,100);
        } catch (Exception e) {
            logger.error(e,e);
        }
        return new ArrayList();
    }

    @Override
    public String createSyncDeviceTask(String emsDn,String deviceDn) throws RemoteException {
        try {
            return "任务已下发,任务编号:"+CdcpServerUtil.syncDevice(emsDn, deviceDn);
        } catch (NodeException e) {
            logger.error(e, e);
            throw new RemoteException(e.getMessage(),e);
        }

    }

    private CdcpNBIService2 cdcpNBIService2 = new CdcpNBIService2();

    @Override
    public String getNeListInEms(String request) throws RemoteException {
        return cdcpNBIService2.getNeListInEms(request);
    }

    @Override
    public String synchronizeNeInfoInEms(String request) throws RemoteException {
        return cdcpNBIService2.synchronizeNeInfoInEms(request);
    }


}
