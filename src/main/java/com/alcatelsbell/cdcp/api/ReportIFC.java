package com.alcatelsbell.cdcp.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.alcatelsbell.nms.valueobject.domain.Report;

public interface ReportIFC extends Remote{
	public List<Report> getAllReport() throws RemoteException;
	
}
