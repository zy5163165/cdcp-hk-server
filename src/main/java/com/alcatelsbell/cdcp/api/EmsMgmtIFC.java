package com.alcatelsbell.cdcp.api;

import com.alcatelsbell.nms.valueobject.sys.Ems;
import com.alcatelsbell.nms.valueobject.sys.Log;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午4:21
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface EmsMgmtIFC extends Remote {
    public Ems createEms(Ems ems) throws RemoteException;
    public void deleteEms(String emsdn) throws RemoteException;
    public Ems modifyEms(Ems ems) throws RemoteException;
    public List<Log> readEmsLogs(String emsDn, int limit) throws RemoteException;
    public boolean testEms(Ems ems) throws RemoteException;
    public String migrateEmsDBFile(String localFileUrl) throws RemoteException;
    public String manualSyncEms(String emsdn) throws RemoteException;
    public String emsNotify(Serializable notification) throws RemoteException;

    public String manualSyncDevice(String deviceDn) throws RemoteException;

}
