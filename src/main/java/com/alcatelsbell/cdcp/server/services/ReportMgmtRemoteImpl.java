package com.alcatelsbell.cdcp.server.services;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alcatelsbell.cdcp.api.ReportIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.hippo.framework.service.DefaultServiceImpl;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JPAContext;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.domain.Report;

public class ReportMgmtRemoteImpl extends DefaultServiceImpl implements ReportIFC {

	 
	public ReportMgmtRemoteImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public java.lang.String getJndiNamePrefix() {
		return Constants.SERVICE_NAME_CDCP_REPORT;
	}

	@Override
	public List<Report> getAllReport() throws RemoteException {
		// TODO Auto-generated method stub
		JPAContext _context = JPAContext.prepareReadOnlyContext();
		List<Report> reports = new ArrayList<Report>(); 
		try {
			List list = JpaClient.getInstance().querySql("select r.* from Report r");
//			List list2 = JPAUtil.getInstance().querySQL(_context, "select r.createdate,r.endtime,r.producttype,r.starttime from Report r");
			for (int i = 0; i < list.size(); i++) {
				Object[] ob = (Object[]) list.get(i);
				Report rep = new Report();
				rep.setCreateDate((Date)ob[1]);
				rep.setReportName(ob[10].toString()); 
				rep.setEndtime((Date)ob[21]); 
				rep.setProductType(ob[24].toString()); 
				rep.setStarttime((Date)ob[28]); 
				reports.add(rep);
			}
			return reports;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			_context.release();
		}
		return null;
	} 
}
