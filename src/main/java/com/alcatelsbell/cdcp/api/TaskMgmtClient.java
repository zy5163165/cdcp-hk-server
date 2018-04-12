package com.alcatelsbell.cdcp.api;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;


import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.valueobject.sys.Log;

public class TaskMgmtClient {
	private static TaskMgmtClient ourInstance = new TaskMgmtClient();

    public static TaskMgmtClient getInstance() {
        return ourInstance;
    }
    
    private TaskMgmtIFC ifc = null;
    private TaskMgmtIFC getIFC() {
        if (ifc == null) {
            ifc = (TaskMgmtIFC) NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_TASK);
        }
        return ifc;
    }
    
    public Schedule createSchedule(Schedule schedule) throws RemoteException {
    	return getIFC().createSchedule(schedule);
    }
    public Schedule modifySchedule(Schedule schedule) throws RemoteException {
    	return getIFC().modifySchedule(schedule);
    }
    public void removeSchedule(Long scheduleId) throws RemoteException {
    	getIFC().removeSchedule(scheduleId);
    }

    public String getGlobalWarning() throws RemoteException {
    	return getIFC().getGlobalWarning();
    }

    public Task  getTask(Long scheduleId) throws RemoteException {
    	return getIFC().getTask(scheduleId);
    }

    public HashMap<Long,Task> getAllTasks() throws RemoteException {
    	return getIFC().getAllTasks();
    }

    public List<Task> getHistoryTasks(Long scheduleId) throws RemoteException {
    	return getIFC().getHistoryTasks(scheduleId);
    }
    public List<Log> getLogs(Long scheduleId) throws RemoteException {
    	return getIFC().getLogs(scheduleId);
    }

    public String syncDevice(String emsDn,String device) throws RemoteException{
        return getIFC().createSyncDeviceTask(emsDn, device);
    }
}
