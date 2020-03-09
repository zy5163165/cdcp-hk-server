package com.alcatelsbell.cdcp.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.asb.mule.probe.framework.entity.EDS_PTN;
public  interface EmsStatisticsIFC extends Remote{
               public List<EDS_PTN>getAllEmsStatistics()throws RemoteException;
               ///public EmsStatistics getAllEmsStatistics()throws RemoteException;
               public List<EDS_PTN> queryByCollectTime(List li)throws RemoteException;
               public List<EDS_PTN> queryByTime(List li)throws RemoteException; 
               public List<EDS_PTN> queryByMonth(List li)throws RemoteException; 
               public List<EDS_PTN> queryByEmsName(String emsName)throws RemoteException;
               public void QueryEmsExcel(List li)throws RemoteException; 
               public List<EDS_PTN>queryMaxEmsStatistics()throws RemoteException;
               
            
	
	
}
