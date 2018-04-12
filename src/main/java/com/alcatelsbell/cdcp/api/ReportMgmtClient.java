package com.alcatelsbell.cdcp.api;
  

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alcatelsbell.cdcp.api.EmsStatisticsIFC;
import com.alcatelsbell.cdcp.api.ReportIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.valueobject.domain.Report;

public class ReportMgmtClient {
	private Log logger = LogFactory.getLog(this.getClass());
 

	/**
	 * 查询所有报表
	 * 
	 * @return
	 */
	public List<Report> getAllReportInit() {
		 List<Report> list = new ArrayList<Report>();
		try { 
			ReportIFC rep=(ReportIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_REPORT);
			list=rep.getAllReport();
			return list;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			logger.error(e, e);
		}
		return list;
	}
	 

}
