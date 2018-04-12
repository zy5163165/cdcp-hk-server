package com.alcatelsbell.cdcp.api;

import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.valueobject.sys.Log;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午4:22
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface TaskMgmtIFC extends Remote {

    /**
     *
     * @param schedule
     *        schedule.taskObjects  多个emsdn用 # 分割
     *        timeType
     *                   public static final int TIME_TYPE_FIX = 0;
     *                   public static final int TIME_TYPE_CRON = 1;
     *                   public static final int TIME_TYPE_CYCLE = 2;
     *        timeExpression
     *                   参考  ScheduleService的 applySchedule方法
     * @return
     * @throws RemoteException
     */
    public Schedule createSchedule(Schedule schedule) throws RemoteException;
    public Schedule modifySchedule(Schedule schedule) throws RemoteException;
    public void removeSchedule(Long scheduleId) throws RemoteException;

    public String getGlobalWarning() throws RemoteException;

    public Task  getTask(Long scheduleId) throws RemoteException;

    public HashMap<Long,Task> getAllTasks() throws RemoteException;

    public List<Task> getHistoryTasks(Long scheduleId) throws RemoteException;
    public List<Log> getLogs(Long scheduleId) throws RemoteException;

    public String createSyncDeviceTask(String emsDn,String deviceDn) throws RemoteException;


    /**
     *
     * @param request
     * @return
     * @throws RemoteException
     */
    public String getNeListInEms(String request) throws RemoteException;

    /**
     *
     * @param request
     * @return
     * @throws RemoteException
     */
    public String synchronizeNeInfoInEms(String request)  throws RemoteException;

}
