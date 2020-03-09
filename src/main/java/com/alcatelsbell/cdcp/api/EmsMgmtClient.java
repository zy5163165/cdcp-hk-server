package com.alcatelsbell.cdcp.api;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import com.alcatelsbell.nms.valueobject.sys.Log;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-16
 * Time: 下午2:03
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class EmsMgmtClient {
    private static EmsMgmtClient ourInstance = new EmsMgmtClient();

    public static EmsMgmtClient getInstance() {
        return ourInstance;
    }

    private EmsMgmtClient() {
    }

    private EmsMgmtIFC ifc = null;
    public EmsMgmtIFC getIFC() {
        if (ifc == null) {
            ifc = (EmsMgmtIFC) NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        }
        return ifc;
    }
    public String migrate(String localfileUrl) throws RemoteException {
        return getIFC().migrateEmsDBFile(localfileUrl);
    }
    
    public Ems createEms(Ems ems) throws RemoteException {
    	return getIFC().createEms(ems);
    }
    
    public void deleteEms(String emsdn) throws RemoteException {
    	getIFC().deleteEms(emsdn);
    }
    
    public Ems modifyEms(Ems ems) throws RemoteException {
    	return getIFC().modifyEms(ems);
    }
    
    public List<Log> readEmsLogs(String emsDn, int limit) throws RemoteException {
    	return getIFC().readEmsLogs(emsDn, limit);
    }
    
    public boolean testEms(Ems ems) {
    	try {
    		boolean result=getIFC().testEms(ems);
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return false;
    }

    public String manualSyncEms(String emsdn) throws RemoteException {
       return getIFC().manualSyncEms(emsdn);
    }
    public String emsNotify(Serializable notification) throws RemoteException {
        return getIFC().emsNotify(notification);
    }

    public static void main(String[] args) {

    }

}
