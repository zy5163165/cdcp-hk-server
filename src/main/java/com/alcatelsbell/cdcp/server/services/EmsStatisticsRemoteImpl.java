package com.alcatelsbell.cdcp.server.services;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import  com.alcatelsbell.cdcp.api.EmsStatisticsIFC;

import  com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.hippo.framework.service.DefaultServiceImpl;
import com.alcatelsbell.nms.db.components.service.JPAContext;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.modules.task.model.Task;
import com.alcatelsbell.nms.valueobject.domain.PmData;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.asb.mule.probe.framework.entity.EDS_PTN;
public  class EmsStatisticsRemoteImpl extends DefaultServiceImpl implements EmsStatisticsIFC{


	public EmsStatisticsRemoteImpl() throws RemoteException {
		super();
	}
	
	public java.lang.String getJndiNamePrefix() {
		return Constants.SERVICE_NAME_CDCP_EMSSTATISTICS;
	}
	/*** 
	 * 查询全部的EMS信息
	 */
	////@Override
	/*public List<EDS_PTN> getAllEmsStatistics() throws RemoteException{
			JPAContext context = JPAContext.prepareReadOnlyContext();
			try{
				List<EDS_PTN>  emsst=JPAUtil.getInstance().findObjects(context, "select e from EDS_PTN e");
				return emsst;
			}catch (Exception e) {
				
			}
			finally {
				
				context.release();
			}
			  return null;

	}*/
	
	
	/*** 
	 * 查询全部的EMS信息
	 */
	@Override
	public List<EDS_PTN> getAllEmsStatistics() throws RemoteException{
		JPAContext context = JPAContext.prepareReadOnlyContext();
		try{
			List list = new ArrayList();
			  String sqls="select e.emsname, count(*) from EDS_PTN e where e.emsname is not null group by e.emsname having count(*)>1";
			  List elist=JPAUtil.getInstance().querySQL(context,sqls);
			 for(int i=0;i<elist.size();i++){
					Object[] ob = (Object[]) elist.get(i);
					EDS_PTN eds=new EDS_PTN();
					String emsname = (String) ob[0];
					eds.setEmsname(emsname);
					list.add(eds);
				  }
     			return list;
				}catch (Exception e) {
					
				}
				finally {
					
					context.release();
				}
				  return null;

	}
    
    
	
	
	/*** 
	 * 查询本周时间内的EMS
	 */
	@Override
	public List<EDS_PTN> queryByCollectTime(List li) throws RemoteException {
		
				 JPAContext context = JPAContext.prepareReadOnlyContext();
				 Date Startdate=new Date();//当天日期时间
			     Date Enddate=new Date();//后7天的时间
				 Calendar Startcalendar = new GregorianCalendar();
				 Calendar Endcalendar = new GregorianCalendar();
				 Startcalendar.setTime(Startdate);
				 Endcalendar.setTime(Enddate);
				 Startcalendar.add(Startcalendar.DATE,0);//当天日期时间
				 Endcalendar.add(Endcalendar.DATE,-6);//后7天的时间
				 //calendar.add(calendar.DATE,-6);//把日期往后增加一天.整数往后推,负数往前移动
				 Startdate=Startcalendar.getTime(); //这个时间就是日期往后推一天的结果 
				 Enddate=Endcalendar.getTime();
				 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				 String StartdateString = formatter.format(Startdate);
				 String EnddateString = formatter.format(Enddate);
		try{
			 List list = new ArrayList();
			 String sqls="select emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType,timeCost,additinalInfo,ctpCount from EDS_PTN e";
			 ///String sqls="select e.emsname,e.collectTime,e.taskSerial,e.neCount,e.slotCount,e.subSlotCount,e.equipmentCount,e.ptpCount,e.ftpCount,e.sectionCount,e.tunnelCount,e.pwCount,e.pwe3Count,e.routeCount,e.tunnelPG from eds_ptn e,eds_ptn m";
			 if(StartdateString != null && StartdateString != ""){
				 sqls = sqls + " where collecttime between to_date ('" + EnddateString + "','yyyy-MM-dd')";
				 
			 }
			 if(EnddateString != null && EnddateString != ""){
				 
				 sqls = sqls +" and to_date('" + StartdateString + "','yyyy-MM-dd') group by emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType,timeCost,additinalInfo,ctpCount having count(*) > 0 order by emsname,collectTime desc";
				 
			 }
			     //List ems=JPAUtil.getInstance().querySQL(support,sqls);
			     List ems=JPAUtil.getInstance().findObjects(context, sqls);
			     for(int i=0;i<ems.size();i++){
						Object[] ob = (Object[]) ems.get(i);
						EDS_PTN em=new EDS_PTN();
						String emsname = (String) ob[0];
						Date collectTime=(Date) ob[1];
						String taskSerial=(String) ob[2];
						Integer neCount= (Integer) ob[3];
						Integer slotCount = (Integer) ob[4];
						Integer subSlotCount = (Integer) ob[5];
						Integer equipmentCount = (Integer) ob[6];
						Integer ptpCount = (Integer) ob[7];
						Integer ftpCount = (Integer) ob[8];
						Integer sectionCount = (Integer) ob[9];
						Integer tunnelCount = (Integer) ob[10];
						Integer pwCount = (Integer) ob[11];
						Integer pwe3Count = (Integer) ob[12];
						Integer routeCount = (Integer) ob[13];
						Integer tunnelPG = (Integer) ob[14];
						Integer ccCount = (Integer) ob[15];
						Integer sncCount = (Integer) ob[16];
						String emsType = (String) ob[17];
						String timeCost = (String) ob[18];
						String additinalInfo = (String) ob[19];
						Integer ctpCount = (Integer) ob[20];
						 
						em.setEmsname(emsname);
						em.setCollectTime(collectTime);
						em.setTaskSerial(taskSerial);
						em.setNeCount(neCount);
						em.setSlotCount(slotCount);
						em.setSubSlotCount(subSlotCount);
						em.setEquipmentCount(equipmentCount);
						em.setPtpCount(ptpCount);
						em.setFtpCount(ftpCount);
						em.setSectionCount(sectionCount);
						em.setTunnelCount(tunnelCount);
						em.setPwCount(pwCount);
						em.setPwe3Count(pwe3Count);
						em.setRouteCount(routeCount);
						em.setTunnelPG(tunnelPG);
						em.setCcCount(ccCount);
						em.setSncCount(sncCount);
						em.setEmsType(emsType);
						em.setTimeCost(timeCost);
						em.setAdditinalInfo(additinalInfo);
						em.setCtpCount(ctpCount);
						list.add(em);
			  }
			    return list;
			}catch (Exception e) {
				e.printStackTrace();
			}
			 finally {
				
				///context.release();
			}
			  return null;
		}

	
	/*** 
	 * 根据时间段去查询
	 */
	@Override
	public List<EDS_PTN> queryByTime(List li)throws RemoteException {
		// TODO Auto-generated method stub
		  JPAContext context = JPAContext.prepareReadOnlyContext();
			 try{
				  List list = new ArrayList();
				  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				  EDS_PTN em=(EDS_PTN)li.get(0);
				  Date StartdateString = em.getCollectTime();
				  Date EnddateString = em.getCreateDate();
				  String startDate=formatter.format(StartdateString);
				  String endDate=formatter.format(EnddateString);
				 String sqls="select emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType from EDS_PTN e";			 
				 if(startDate != null){
					 sqls = sqls + " where collecttime between to_date ('" + startDate + "','yyyy-MM-dd')"; 
				 }
				 if(endDate != null){ 
					 sqls = sqls +" and to_date('" + endDate + "','yyyy-MM-dd') group by emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType having count(*) > 0 order by emsname,collectTime desc";	 
				 }
				     List ems=JPAUtil.getInstance().findObjects(context, sqls);
				     for(int i=0;i<ems.size();i++){
							Object[] ob = (Object[]) ems.get(i);
							EDS_PTN emss=new EDS_PTN();
							String emsname = (String) ob[0];
							Date collectTime = (Date) ob[1];
							String taskSerial=(String) ob[2];
							Integer neCount= (Integer) ob[3];
							Integer slotCount = (Integer) ob[4];
							Integer subSlotCount = (Integer) ob[5];
							Integer equipmentCount = (Integer) ob[6];
							Integer ptpCount = (Integer) ob[7];
							Integer ftpCount = (Integer) ob[8];
							Integer sectionCount = (Integer) ob[9];
							Integer tunnelCount = (Integer) ob[10];
							Integer pwCount = (Integer) ob[11];
							Integer pwe3Count = (Integer) ob[12];
							Integer routeCount = (Integer) ob[13];
							Integer tunnelPG = (Integer) ob[14];
							Date createDate = (Date) ob[15];
							Integer ccCount = (Integer) ob[16];
							Integer sncCount = (Integer) ob[17];
							String emsType = (String) ob[18];
							
							
							emss.setEmsname(emsname);
							emss.setCollectTime(collectTime);
							emss.setTaskSerial(taskSerial);
							emss.setNeCount(neCount);
							emss.setSlotCount(slotCount);
							emss.setSubSlotCount(subSlotCount);
							emss.setEquipmentCount(equipmentCount);
							emss.setPtpCount(ptpCount);
							emss.setFtpCount(ftpCount);
							emss.setSectionCount(sectionCount);
							emss.setTunnelCount(tunnelCount);
							emss.setPwCount(pwCount);
							emss.setPwe3Count(pwe3Count);
							emss.setRouteCount(routeCount);
							emss.setTunnelPG(tunnelPG);
							emss.setCreateDate(createDate);
							em.setCcCount(ccCount);
							em.setSncCount(sncCount);
							em.setEmsType(emsType);
							list.add(emss);
				  }
				    return list;
				}catch (Exception e) {
					e.printStackTrace();
				}
				 finally {
					
					context.release();
				}
				  return null;
		}

	/**
	 * 查询本月内的EMS信息
	 */
	@Override
	public List<EDS_PTN> queryByMonth(List li) throws RemoteException {
				JPAContext context = JPAContext.prepareReadOnlyContext();
				 Date StartMonthdate=new Date();//当天日期时间
			     Date EndMonthdate=new Date();
				 Calendar Startcalendar = new GregorianCalendar();
				 Calendar Endcalendar = new GregorianCalendar();
				 Startcalendar.setTime(StartMonthdate);
				 Endcalendar.setTime(EndMonthdate);
				 Startcalendar.add(Startcalendar.DATE,0);
				 Endcalendar.add(Endcalendar.DATE,-30);
				 StartMonthdate=Startcalendar.getTime(); 
				 EndMonthdate=Endcalendar.getTime();
				 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				 String StartMonth = formatter.format(StartMonthdate);
				 String EndMonth = formatter.format(EndMonthdate);
				 try{
					 List list = new ArrayList();
					 String sqls="select emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType,timeCost,additinalInfo,ctpCount from EDS_PTN e";
					 if(StartMonth != null && StartMonth != ""){
						 sqls = sqls + " where collecttime between to_date ('" + EndMonth + "','yyyy-MM-dd')";	 
					 }
					 if(EndMonth != null && EndMonth != ""){ 
						 sqls = sqls +" and to_date('" + StartMonth + "','yyyy-MM-dd') group by emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType,timeCost,additinalInfo,ctpCount having count(*) > 0 order by emsname,collectTime desc";	 
					 }
					     List ems=JPAUtil.getInstance().findObjects(context, sqls);
					     for(int i=0;i<ems.size();i++){
								Object[] ob = (Object[]) ems.get(i);
								EDS_PTN em=new EDS_PTN();
								String emsname = (String) ob[0];
								Date collectTime=(Date) ob[1];
								String taskSerial=(String) ob[2];
								Integer neCount= (Integer) ob[3];
								Integer slotCount = (Integer) ob[4];
								Integer subSlotCount = (Integer) ob[5];
								Integer equipmentCount = (Integer) ob[6];
								Integer ptpCount = (Integer) ob[7];
								Integer ftpCount = (Integer) ob[8];
								Integer sectionCount = (Integer) ob[9];
								Integer tunnelCount = (Integer) ob[10];
								Integer pwCount = (Integer) ob[11];
								Integer pwe3Count = (Integer) ob[12];
								Integer routeCount = (Integer) ob[13];
								Integer tunnelPG = (Integer) ob[14];
								Integer ccCount = (Integer) ob[15];
								Integer sncCount = (Integer) ob[16];
								String emsType = (String) ob[17];
								String timeCost = (String) ob[18];
								String additinalInfo = (String) ob[19];
								Integer ctpCount = (Integer) ob[20];
								
								
								
								em.setEmsname(emsname);
								em.setCollectTime(collectTime);
								em.setTaskSerial(taskSerial);
								em.setNeCount(neCount);
								em.setSlotCount(slotCount);
								em.setSubSlotCount(subSlotCount);
								em.setEquipmentCount(equipmentCount);
								em.setPtpCount(ptpCount);
								em.setFtpCount(ftpCount);
								em.setSectionCount(sectionCount);
								em.setTunnelCount(tunnelCount);
								em.setPwCount(pwCount);
								em.setPwe3Count(pwe3Count);
								em.setRouteCount(routeCount);
								em.setTunnelPG(tunnelPG);
								em.setCcCount(ccCount);
								em.setSncCount(sncCount);
								em.setEmsType(emsType);
								em.setTimeCost(timeCost);
								em.setAdditinalInfo(additinalInfo);
								em.setCtpCount(ctpCount);
								list.add(em);
					  }
					    return list;
					}catch (Exception e) {
						e.printStackTrace();
					}
					 finally {
						
						context.release();
					}
					  return null;
				}

	
	/*** 
	 * 根据emsName去查询所有的
	 */
	@Override
	public List<EDS_PTN> queryByEmsName(String emsName)
			throws RemoteException {
		// TODO Auto-generated method stub
		    JPAContext context = JPAContext.prepareReadOnlyContext();
		    try{
				List list = new ArrayList();
				String sqls="select e.emsname,e.collectTime,e.taskSerial,e.neCount,e.slotCount,e.subSlotCount,e.equipmentCount,e.ptpCount,e.ftpCount,e.sectionCount,e.tunnelCount,e.pwCount,e.pwe3Count,e.routeCount,e.tunnelPG,e.ccCount,e.sncCount,e.emsType,e.timeCost,e.additinalInfo,e.ctpCount from EDS_PTN e";
				 if(emsName != null && emsName != ""){
					 sqls = sqls + " where e.emsname like '%" + emsName + "%' and e.emsname is not null order by collecttime desc";					 
				 }
				 List ems=JPAUtil.getInstance().findObjects(context, sqls);
			     for(int i=0;i<ems.size();i++){
						Object[] ob = (Object[]) ems.get(i);
						EDS_PTN em=new EDS_PTN();
						String emsname = (String) ob[0];
						Date collectTime=(Date) ob[1];
						String taskSerial=(String) ob[2];
						Integer neCount= (Integer) ob[3];
						Integer slotCount = (Integer) ob[4];
						Integer subSlotCount = (Integer) ob[5];
						Integer equipmentCount = (Integer) ob[6];
						Integer ptpCount = (Integer) ob[7];
						Integer ftpCount = (Integer) ob[8];
						Integer sectionCount = (Integer) ob[9];
						Integer tunnelCount = (Integer) ob[10];
						Integer pwCount = (Integer) ob[11];
						Integer pwe3Count = (Integer) ob[12];
						Integer routeCount = (Integer) ob[13];
						Integer tunnelPG = (Integer) ob[14];
						Integer ccCount = (Integer) ob[15];
						Integer sncCount = (Integer) ob[16];
						String emsType = (String) ob[17];
						String timeCost = (String) ob[18];
						String additinalInfo = (String) ob[19];
						Integer ctpCount = (Integer) ob[20];
						
						
						em.setEmsname(emsname);
						em.setCollectTime(collectTime);
						em.setTaskSerial(taskSerial);
						em.setNeCount(neCount);
						em.setSlotCount(slotCount);
						em.setSubSlotCount(subSlotCount);
						em.setEquipmentCount(equipmentCount);
						em.setPtpCount(ptpCount);
						em.setFtpCount(ftpCount);
						em.setSectionCount(sectionCount);
						em.setTunnelCount(tunnelCount);
						em.setPwCount(pwCount);
						em.setPwe3Count(pwe3Count);
						em.setRouteCount(routeCount);
						em.setTunnelPG(tunnelPG);
						em.setCcCount(ccCount);
						em.setSncCount(sncCount);
						em.setEmsType(emsType);
						em.setTimeCost(timeCost);
						em.setAdditinalInfo(additinalInfo);
						em.setCtpCount(ctpCount);
						list.add(em);
			  }
				return list;
			}catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				
				context.release();
			}
			  return null;
	  }

	
	/*** 
	 * 导出EMS统计信息为excel格式
	 */
	@Override
	public void QueryEmsExcel(List li) throws RemoteException {
		// TODO Auto-generated method stub
				List<EDS_PTN> ems=queryByMonth(li);
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = wb.createSheet("EMS统计信息");
				HSSFRow row = sheet.createRow((int)0);
				HSSFCellStyle style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				HSSFCell cell = row.createCell(0);
				cell.setCellValue("EMS名称");
				cell.setCellStyle(style);
				cell = row.createCell(1);
				cell.setCellValue("采集时间");
				cell.setCellStyle(style);
				cell = row.createCell(2);
				cell.setCellValue("任务编号");
				cell.setCellStyle(style);
				cell = row.createCell(3);
				cell.setCellValue("NE总数");
				cell.setCellStyle(style);
				cell = row.createCell(4);
				cell.setCellValue("SLOT总数");
				cell.setCellStyle(style);
				cell = row.createCell(5);
				cell.setCellValue("SUBSLOT总数");
				cell.setCellStyle(style);
				cell = row.createCell(6);
				cell.setCellValue("EQUIPMENT总数");
				cell.setCellStyle(style);
				cell = row.createCell(7);
				cell.setCellValue("PTP总数");
				cell.setCellStyle(style);
				cell = row.createCell(8);
				cell.setCellValue("FTP总数");
				cell.setCellStyle(style);
				cell = row.createCell(9);
				cell.setCellValue("SECTION总数");
				cell.setCellStyle(style);
				cell = row.createCell(10);
				cell.setCellValue("TUNNEL总数");
				cell.setCellStyle(style);
				cell = row.createCell(11);
				cell.setCellValue("PW总数");
				cell.setCellStyle(style);
				cell = row.createCell(12);
				cell.setCellValue("PWE3总数");
				cell.setCellStyle(style);
				cell = row.createCell(13);
				cell.setCellValue("ROUTE总数");
				cell.setCellStyle(style);
				cell = row.createCell(14);
				cell.setCellValue("TUNNELPG总数");
				cell = row.createCell(15);
				cell.setCellValue("网元交叉总数");
				cell = row.createCell(16);
				cell.setCellValue("子网交叉总数");
				cell = row.createCell(17);
				cell.setCellValue("EMS类型");
				cell.setCellStyle(style);
				
				cell = row.createCell(18);
				cell.setCellValue("总时间");
				cell.setCellStyle(style);
				
				cell = row.createCell(19);
				cell.setCellValue("备注");
				cell.setCellStyle(style);
				
				cell = row.createCell(20);
				cell.setCellValue("CTP总数");
				cell.setCellStyle(style);
				
			 for(int i=0;i<ems.size();i++){
					row = sheet.createRow(i+1);
					EDS_PTN emsstat=ems.get(i);
					row.createCell(0).setCellValue(emsstat.getEmsname());
					row.createCell(1).setCellValue(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(emsstat.getCollectTime()));
					row.createCell(2).setCellValue(emsstat.getTaskSerial());
					row.createCell(3).setCellValue(emsstat.getNeCount());
					row.createCell(4).setCellValue(emsstat.getSlotCount());
					row.createCell(5).setCellValue(emsstat.getSubSlotCount());
					row.createCell(6).setCellValue(emsstat.getEquipmentCount());
					row.createCell(7).setCellValue(emsstat.getPtpCount());
					row.createCell(8).setCellValue(emsstat.getFtpCount());
					row.createCell(9).setCellValue(emsstat.getSectionCount());
					row.createCell(10).setCellValue(emsstat.getTunnelCount());
					row.createCell(11).setCellValue(emsstat.getPwCount());
					row.createCell(12).setCellValue(emsstat.getPwe3Count());
					row.createCell(13).setCellValue(emsstat.getRouteCount());
					row.createCell(14).setCellValue(emsstat.getTunnelPG());
					if(emsstat.getCcCount() !=null){
						row.createCell(15).setCellValue(emsstat.getCcCount());
					}else{
						row.createCell(15).setCellValue(0);
					}
					if(emsstat.getSncCount() !=null){
						row.createCell(16).setCellValue(emsstat.getSncCount());
					}else{
						
						row.createCell(16).setCellValue(0);
					}
					
					row.createCell(17).setCellValue(emsstat.getEmsType());
					row.createCell(18).setCellValue(emsstat.getTimeCost());
					row.createCell(19).setCellValue(emsstat.getAdditinalInfo());
					if(emsstat.getCtpCount() !=null){
						 row.createCell(20).setCellValue(emsstat.getCtpCount());
					}else{
						row.createCell(20).setCellValue(0);
					}
					
			 }
			 try{ 
				 FileOutputStream fout= new FileOutputStream(new File("D:\\EMS.xls"));
						 ///getRealPath("/")+"/scripts/EMS.xls"));
				 wb.write(fout);
					fout.flush();
					fout.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
	  }
	
	/*** 
	 * 查询正在使用的EMS
	 */
	@Override
	public List<EDS_PTN> queryMaxEmsStatistics() throws RemoteException {
		// TODO Auto-generated method stub
		JPAContext context = JPAContext.prepareReadOnlyContext();
		try{
			List elist = new ArrayList();
			   String sqls="select * from (select emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType,timeCost,additinalInfo,ctpCount, row_number() over (partition by emsname order by emsname,collectTime desc)rn from eds_ptn) where rn=1 and emsname is not null";
			     List es=JPAUtil.getInstance().querySQL(context,sqls);
			       for(int i=0;i<es.size();i++){
							Object[] ob = (Object[]) es.get(i);
							EDS_PTN edsptn=new EDS_PTN();
							BigDecimal bd = null;
							String emsname = (String) ob[0];
							Date collectTime=(Date) ob[1];
							//Timestamp tt = new Timestamp(collectTime.getTime());
							String taskSerial=(String) ob[2];
							bd=(BigDecimal)ob[3];
							Integer neCount= bd.intValue();
							bd=(BigDecimal)ob[4];
							Integer slotCount= bd.intValue();
							bd=(BigDecimal)ob[5];
							Integer subSlotCount= bd.intValue();
							bd=(BigDecimal)ob[6];
							Integer equipmentCount= bd.intValue();
							bd=(BigDecimal)ob[7];
							Integer ptpCount= bd.intValue();
							bd=(BigDecimal)ob[8];
							Integer ftpCount= bd.intValue();
							bd=(BigDecimal)ob[9];
							Integer sectionCount= bd.intValue();
							bd=(BigDecimal)ob[10];
							Integer tunnelCount= bd.intValue();
							bd=(BigDecimal)ob[11];
							Integer pwCount= bd.intValue();
							bd=(BigDecimal)ob[12];
							Integer pwe3Count= bd.intValue();
							bd=(BigDecimal)ob[13];
							Integer routeCount= bd.intValue();
							bd=(BigDecimal)ob[14];
							Integer tunnelPG= bd.intValue();
							
							//bd=(BigDecimal)ob[15];
							if((BigDecimal)ob[15] !=null){
								bd=(BigDecimal)ob[15];
							}else{
								bd.setScale(0);
							}
							Integer ccCount= bd.intValue();
								
							if((BigDecimal)ob[16] !=null){
								bd=(BigDecimal)ob[16];
							}else{
								bd.setScale(0);
							}
		                    Integer sncCount= bd.intValue();
								
						
							String emsType = (String) ob[17];
							String timeCost = (String) ob[18];
							String additinalInfo = (String) ob[19];
							
							if((BigDecimal)ob[20] !=null){
								  bd=(BigDecimal)ob[20];
								
							}else {
								 bd.setScale(0);
							}
							
							Integer ctpCount= bd.intValue();
							
							
							edsptn.setEmsname(emsname);
							edsptn.setCollectTime(collectTime);
							edsptn.setTaskSerial(taskSerial);
							edsptn.setNeCount(neCount);
							edsptn.setSlotCount(slotCount);
							edsptn.setSubSlotCount(subSlotCount);
							edsptn.setEquipmentCount(equipmentCount);
							edsptn.setPtpCount(ptpCount);
							edsptn.setFtpCount(ftpCount);
							edsptn.setSectionCount(sectionCount);
							edsptn.setTunnelCount(tunnelCount);
							edsptn.setPwCount(pwCount);
							edsptn.setPwe3Count(pwe3Count);
							edsptn.setRouteCount(routeCount);
							edsptn.setTunnelPG(tunnelPG);
							
							edsptn.setCcCount(ccCount);
							edsptn.setSncCount(sncCount);
							edsptn.setEmsType(emsType);
							edsptn.setTimeCost(timeCost);
							edsptn.setAdditinalInfo(additinalInfo);
							edsptn.setCtpCount(ctpCount);
							elist.add(edsptn);
							
				  }
				  return elist;
		}catch (Exception e) {
			
		}
		finally {
			
			context.release();
		}
		  return null;
	    }
   }
