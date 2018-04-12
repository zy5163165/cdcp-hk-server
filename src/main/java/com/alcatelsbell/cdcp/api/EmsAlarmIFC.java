package com.alcatelsbell.cdcp.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.asb.mule.probe.framework.entity.EDS_PTN;

import com.alcatelsbell.cdcp.common.model.EmsBenchmark;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkItem;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkList;

public interface EmsAlarmIFC extends Remote{
	
	 public List<EDS_PTN> queryByEmsName(String emsName)throws RemoteException;
	 public List<EDS_PTN>queryAllEmsPtn()throws RemoteException;
	 public int insertEmsPtn(EmsBenchmark list,EmsBenchmarkItem ebklist)throws Exception;
	 public List<EmsBenchmarkList>queryEmsBenchmarkList()throws RemoteException;
	 public List<EmsBenchmark>queryAllEmsBenchmark()throws RemoteException;
	 public List<EmsBenchmark>queryAllEmsBenchmarkByName(String emsName)throws RemoteException;
	 public List<EmsBenchmarkItem>queryEmsBenchmarkItemById(String id)throws RemoteException;
	 public List<EmsBenchmarkList>queryEmsBenchmarkListByID(Long id)throws RemoteException;
	 public int updateEmsBenchmarkPtn(EmsBenchmarkItem belist)throws Exception;
	 

}
