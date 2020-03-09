package com.alcatelsbell.cdcp.server.services;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.asb.mule.probe.framework.entity.EDS_PTN;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import  com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.common.model.EmsBenchmark;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkItem;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkList;
import com.alcatelsbell.cdcp.api.EmsAlarmIFC;
import com.alcatelsbell.hippo.framework.service.DefaultServiceImpl;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPAContext;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.domain.EmsAlarm;

public  class EmsAlarmRemoteImpl extends DefaultServiceImpl implements EmsAlarmIFC{

	public EmsAlarmRemoteImpl() throws RemoteException {
		super();
		
	}
	
	public java.lang.String getJndiNamePrefix() {
		return Constants.SERVICE_NAME_CDCP_EMSALARM;
	}
    
	
	
	////根据EMSName 查询当前的EDS-PTN
	@Override
	public List<EDS_PTN> queryByEmsName(String emsName) throws RemoteException {
		// TODO Auto-generated method stub
		 JPAContext context = JPAContext.prepareReadOnlyContext();
		
		 try{
				  List elist = new ArrayList();
				      String sqls="select * from (select emsname,collectTime,taskSerial,neCount,slotCount,subSlotCount,equipmentCount,ptpCount,ftpCount,sectionCount,tunnelCount,pwCount,pwe3Count,routeCount,tunnelPG,ccCount,sncCount,emsType,timeCost,additinalInfo,ctpCount, row_number() over (partition by emsname order by emsname,collectTime desc)rn from eds_ptn) where rn=1 and emsname is not null";
				   ///String sqls="select t.emsname,t.collectTime,t.taskSerial,t.neCount,t.slotCount,t.subSlotCount,t.equipmentCount,t.ptpCount,t.ftpCount,t.sectionCount,t.tunnelCount,t.pwCount,t.pwe3Count,t.routeCount,t.tunnelPG,t.ccCount,t.sncCount,t.emsType,t.timeCost,t.additinalInfo,t.ctpCount from EDS_PTN t where t.id=(select max(id) from EDS_PTN";
				   if(emsName != null && emsName != ""){
						 sqls = sqls + " and emsname like '%" + emsName + "%'";
					 }
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

	
	/**
	 * 查询出所有的EMS
	 */
	
	@Override
	public List<EDS_PTN> queryAllEmsPtn() throws RemoteException {
		// TODO Auto-generated method stub
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
          * 编辑PTN
          */

		public int updateEmsBenchmarkPtn(EmsBenchmarkItem belist)throws Exception{
				 JPAContext context = JPAContext.prepareReadOnlyContext();
				try{
					
						String tableNameObject =belist.getTableName();
		            	String benchmarkDnObject=belist.getBenchmarkDn();
		            	String checkEMSName=tableNameObject.substring(0, tableNameObject.indexOf("@emsnameInput"));
		            	String neCount=tableNameObject.substring(tableNameObject.indexOf("@emsnameInput")+13, tableNameObject.indexOf("@neCountInput"));
		            	String slotCount=tableNameObject.substring(tableNameObject.indexOf("@neCountInput")+13, tableNameObject.indexOf("@slotCountInput"));
		            	String subSlotCount=tableNameObject.substring(tableNameObject.indexOf("@slotCountInput")+15, tableNameObject.indexOf("@subSlotCountInput"));
		            	String equipmentCount=tableNameObject.substring(tableNameObject.indexOf("@subSlotCountInput")+18, tableNameObject.indexOf("@equipmentCountInput"));
		            	String ptpCount=tableNameObject.substring(tableNameObject.indexOf("@equipmentCountInput")+20, tableNameObject.indexOf("@ptpCountInput"));
		            	String ftpCount=tableNameObject.substring(tableNameObject.indexOf("@ptpCountInput")+14, tableNameObject.indexOf("@ftpCountInput"));
		            	String sectionCount=tableNameObject.substring(tableNameObject.indexOf("@ftpCountInput")+14, tableNameObject.indexOf("@sectionCountInput"));
		            	String tunnelCount=tableNameObject.substring(tableNameObject.indexOf("@sectionCountInput")+18, tableNameObject.indexOf("@tunnelCountInput"));
		            	String pwCount=tableNameObject.substring(tableNameObject.indexOf("@tunnelCountInput")+17, tableNameObject.indexOf("@pwCountInput"));
		            	String pwe3Count=tableNameObject.substring(tableNameObject.indexOf("@pwCountInput")+13, tableNameObject.indexOf("@pwe3CountInput"));
		            	String routeCount=tableNameObject.substring(tableNameObject.indexOf("@pwe3CountInput")+15, tableNameObject.indexOf("@routeCountInput"));
		            	String tunnelPG=tableNameObject.substring(tableNameObject.indexOf("@routeCountInput")+16, tableNameObject.indexOf("@tunnelPGInput"));
		            	String ccCount=tableNameObject.substring(tableNameObject.indexOf("@tunnelPGInput")+14, tableNameObject.indexOf("@ccCountInput"));
		            	String sncCount=tableNameObject.substring(tableNameObject.indexOf("@ccCountInput")+13, tableNameObject.indexOf("@sncCountInput"));
		            	String ctpCount=tableNameObject.substring(tableNameObject.indexOf("@sncCountInput")+14, tableNameObject.indexOf("@ctpCountInput"));
		            	
		            	
		            	String EMSNameID=tableNameObject.substring(tableNameObject.indexOf("@ctpCountInput")+14, tableNameObject.indexOf("@IdInput"));
		            	
		            	
		            	
		            	String dvpercentage=benchmarkDnObject.substring(0, benchmarkDnObject.indexOf("@neCountCount"));
		            	String slotCounttage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@neCountCount")+13, benchmarkDnObject.indexOf("@slotCountCount"));
		            	String subSlotCounttage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@slotCountCount")+15, benchmarkDnObject.indexOf("@subSlotCountCount"));
		            	String equipmentCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@subSlotCountCount")+18, benchmarkDnObject.indexOf("@equipmentCountCount"));
		            	String ptpCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@equipmentCountCount")+20, benchmarkDnObject.indexOf("@ptpCountCount"));
		            	String ftpCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ptpCountCount")+14, benchmarkDnObject.indexOf("@ftpCountCount"));
		            	String sectionCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ftpCountCount")+14, benchmarkDnObject.indexOf("@sectionCountCount"));
		            	String tunnelCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@sectionCountCount")+18, benchmarkDnObject.indexOf("@tunnelCountCount"));
		            	String pwCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@tunnelCountCount")+17, benchmarkDnObject.indexOf("@pwCountCount"));
		            	String pwe3CountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@pwCountCount")+13, benchmarkDnObject.indexOf("@pwe3CountCount"));
		            	String routeCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@pwe3CountCount")+15, benchmarkDnObject.indexOf("@routeCountCount"));
		            	String tunnelPGTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@routeCountCount")+16, benchmarkDnObject.indexOf("@tunnelPGCount"));
		            	String ccCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@tunnelPGCount")+14, benchmarkDnObject.indexOf("@ccCountCount"));
		            	String sncCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ccCountCount")+13, benchmarkDnObject.indexOf("@sncCountCount"));
		            	String ctpCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@sncCountCount")+14, benchmarkDnObject.indexOf("@ctpCountCount"));
		            	
		            	
		            	
		            	String neID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ctpCountCount")+14, benchmarkDnObject.indexOf("@neID"));
			            String slotID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@neID")+5, benchmarkDnObject.indexOf("@slotID"));
		            	String subSlotID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@slotID")+7, benchmarkDnObject.indexOf("@subSlotID"));
		            	String equipmentID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@subSlotID")+10, benchmarkDnObject.indexOf("@equipmentID"));
		            	String ptpID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@equipmentID")+12, benchmarkDnObject.indexOf("@ptpID"));
		            	String ftpID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ptpID")+6, benchmarkDnObject.indexOf("@ftpID"));
		            	String sectionID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ftpID")+6, benchmarkDnObject.indexOf("@sectionID"));
		            	String tunnelID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@sectionID")+10, benchmarkDnObject.indexOf("@tunnelID"));
		            	String pwID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@tunnelID")+9, benchmarkDnObject.indexOf("@pwID"));
		            	String pwe3ID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@pwID")+5, benchmarkDnObject.indexOf("@pwe3ID"));
		            	String routeID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@pwe3ID")+7, benchmarkDnObject.indexOf("@routeID"));
		            	String tunnelPGID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@routeID")+8, benchmarkDnObject.indexOf("@tunnelPGID"));
		            	String ccID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@tunnelPGID")+11, benchmarkDnObject.indexOf("@ccID"));
		            	String sncID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ccID")+5, benchmarkDnObject.indexOf("@sncID"));
		            	String ctpID=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@sncID")+6, benchmarkDnObject.indexOf("@ctpID"));
		            	
		            	///1neID
						context.begin();
						long neids=Long.parseLong(neID); 
						////EmsBenchmarkItem emsbk=new EmsBenchmarkItem();
						//Long neids=Long.valueOf(neID).longValue();
						//String count=belist.getCount().toString();
						///String dvpercentage=belist.getDvpercentage().toString();
						////JPAUtil.getInstance().executeQL(context,"update EmsBenchmarkItem set count ='"+count+"',dvpercentage ='"+dvpercentage+"' where id='"+ids+"'");///修改****里面count字段是特殊字段要用修改语句的话要修改数据库
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + neids);
						context.end();
						
						///2slotID
						context.begin();
						Long slotIds=Long.valueOf(slotID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + slotIds);
						context.end();
						///3subSlotID
						context.begin();
						Long subSlotids=Long.valueOf(subSlotID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + subSlotids);
						context.end();
						///4equipmentID
						context.begin();
						Long equipmentids=Long.valueOf(equipmentID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + equipmentids);
						context.end();
						///5ptpID
						context.begin();
						Long ptpids=Long.valueOf(ptpID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + ptpids);
						context.end();
						
						////6ftpID
						context.begin();
						Long ftpids=Long.valueOf(ftpID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + ftpids);
						context.end();
						///7sectionID
						context.begin();
						Long sectionids=Long.valueOf(sectionID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + sectionids);
						context.end();
						
						///8tunnelID
						context.begin();
						Long tunnelids=Long.valueOf(tunnelID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + tunnelids);
						context.end();
						
						///9pwID
						context.begin();
						Long pwids=Long.valueOf(pwID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + pwids);
						context.end();
						
						///10.pwe3ID
						context.begin();
						Long pwe3ids=Long.valueOf(pwe3ID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + pwe3ids);
						context.end();
						////11.routeID
						context.begin();
						Long routeids=Long.valueOf(routeID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + routeids);
						context.end();
						////12.tunnelPGID
						context.begin();
						Long tunnelPGids=Long.valueOf(tunnelPGID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + tunnelPGids);
						context.end();
						///13.ccID
						context.begin();
						Long ccids=Long.valueOf(ccID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + ccids);
						context.end();
						////14.sncID
						context.begin();
						Long sncids=Long.valueOf(sncID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + sncids);
						context.end();
						////15.ctpID
						context.begin();
						Long ctpids=Long.valueOf(ctpID).longValue();
						JPAUtil.getInstance().executeQL(context, "delete from  EmsBenchmarkItem p where p.id=" + ctpids);
						context.end();
						
						//1.neCount
						EmsBenchmarkItem item=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    item.setId(belist.getId());
	            	    	    item.setDn(SysUtil.nextDN());
	            	    	    item.setVersion(1L);
	            	    	    item.setFromWhere(belist.getFromWhere());
	            	    	    item.setBenchmarkDn(EMSNameID);
	            	    	    item.setTableName("neCount");
				            	item.setCount(Integer.parseInt(neCount));
				            	item.setDvpercentage(Integer.parseInt(dvpercentage));
	            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, item);
	            	    	    context.end();
	            	     
	            	     
	            	      ///2.slotCount
	            	    
	            	    	  EmsBenchmarkItem eblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    eblist.setId(belist.getId());
	            	    	    eblist.setDn(SysUtil.nextDN());
	            	    	    eblist.setVersion(2L);
	            	    	    eblist.setFromWhere(belist.getFromWhere());
	            	    	    eblist.setBenchmarkDn(EMSNameID);
	            	    	    eblist.setTableName("slotCount");
	            	    	    eblist.setCount(Integer.parseInt(slotCount));
	            	    	    eblist.setDvpercentage(Integer.parseInt(slotCounttage));
	            	    	    EmsBenchmarkItem kslist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, eblist);
	            	    	    context.end();
	            	    
	            	     
	            	      ////subSlotCount**3
	            	     
		            	    	    EmsBenchmarkItem blist=new EmsBenchmarkItem();
		            	    	    context.begin();
		            	    	    blist.setId(belist.getId());
		            	    	    blist.setDn(SysUtil.nextDN());
		            	    	    blist.setVersion(3L);
		            	    	    blist.setFromWhere(belist.getFromWhere());
		            	    	    blist.setBenchmarkDn(EMSNameID);
		            	    	    blist.setTableName("subSlotCount");
		            	    	    blist.setCount(Integer.parseInt(subSlotCount));
		            	    	    blist.setDvpercentage(Integer.parseInt(subSlotCounttage));
		            	    	    EmsBenchmarkItem itlist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, blist);
		            	    	    context.end();
	            	     
	            	      
	            	      //equipmentCount##4
	            	     
	            	    	    EmsBenchmarkItem dlist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    dlist.setId(belist.getId());
	            	    	    dlist.setDn(SysUtil.nextDN());
	            	    	    dlist.setVersion(4L);
	            	    	    dlist.setFromWhere(belist.getFromWhere());
	            	    	    dlist.setBenchmarkDn(EMSNameID);
	            	    	    dlist.setTableName("equipmentCount");
	            	    	    dlist.setCount(Integer.parseInt(equipmentCount));
	            	    	    dlist.setDvpercentage(Integer.parseInt(equipmentCountTage));
	            	    	    EmsBenchmarkItem markli=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, dlist);
	            	    	    context.end();
          	         
	            	    //ptpCount##5
	            	   
	            	    	    EmsBenchmarkItem eslist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    eslist.setId(belist.getId());
	            	    	    eslist.setDn(SysUtil.nextDN());
	            	    	    eslist.setVersion(5L);
	            	    	    eslist.setFromWhere(belist.getFromWhere());
	            	    	    eslist.setBenchmarkDn(EMSNameID);
	            	    	    eslist.setTableName("ptpCount");
	            	    	    eslist.setCount(Integer.parseInt(ptpCount));
	            	    	    eslist.setDvpercentage(Integer.parseInt(ptpCountTage));
	            	    	    EmsBenchmarkItem marklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, eslist);
	            	    	    context.end();
          	       
          	         
	            	    //ftpCount##6
	            	      
	            	    	    EmsBenchmarkItem flist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    flist.setId(belist.getId());
	            	    	    flist.setDn(SysUtil.nextDN());
	            	    	    flist.setVersion(6L);
	            	    	    flist.setFromWhere(belist.getFromWhere());
	            	    	    flist.setBenchmarkDn(EMSNameID);
	            	    	    flist.setTableName("ftpCount");
	            	    	    flist.setCount(Integer.parseInt(ftpCount));
	            	    	    flist.setDvpercentage(Integer.parseInt(ftpCountTage));
	            	    	    EmsBenchmarkItem  mark=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, flist);
	            	    	    context.end();
          	       
	            	      ///sectionCount##7
	            	     
	            	    	    EmsBenchmarkItem jlist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    jlist.setId(belist.getId());
	            	    	    jlist.setDn(SysUtil.nextDN());
	            	    	    jlist.setVersion(7L);
	            	    	    jlist.setFromWhere(belist.getFromWhere());
	            	    	    jlist.setBenchmarkDn(EMSNameID);
	            	    	    jlist.setTableName("sectionCount");
	            	    	    jlist.setCount(Integer.parseInt(sectionCount));
	            	    	    jlist.setDvpercentage(Integer.parseInt(sectionCountTage));
	            	    	    EmsBenchmarkItem fithlist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, jlist);
	            	    	    context.end();
          	         
	            	      ///tunnelCount##8
	            	      
	            	    	    EmsBenchmarkItem kblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    kblist.setId(belist.getId());
	            	    	    kblist.setDn(SysUtil.nextDN());
	            	    	    kblist.setVersion(8L);
	            	    	    kblist.setFromWhere(belist.getFromWhere());
	            	    	    kblist.setBenchmarkDn(EMSNameID);
	            	    	    kblist.setTableName("tunnelCount");
	            	    	    kblist.setCount(Integer.parseInt(tunnelCount));
	            	    	    kblist.setDvpercentage(Integer.parseInt(tunnelCountTage));
	            	    	    EmsBenchmarkItem foutlist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, kblist);
	            	    	    context.end();
          	        
          	          ///pwCount##9
	            	     
	            	    	    EmsBenchmarkItem mblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    mblist.setId(belist.getId());
	            	    	    mblist.setDn(SysUtil.nextDN());
	            	    	    mblist.setVersion(9L);
	            	    	    mblist.setFromWhere(belist.getFromWhere());
	            	    	    mblist.setBenchmarkDn(EMSNameID);
	            	    	    mblist.setTableName("pwCount");
	            	    	    mblist.setCount(Integer.parseInt(pwCount));
	            	    	    mblist.setDvpercentage(Integer.parseInt(pwCountTage));
	            	    	    EmsBenchmarkItem  two=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, mblist);
	            	    	    context.end();
          	       
	            	    
	            	      ///pwe3Count##10
	            	      
	            	    	    EmsBenchmarkItem nblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    nblist.setId(belist.getId());
	            	    	    nblist.setDn(SysUtil.nextDN());
	            	    	    nblist.setVersion(10L);
	            	    	    nblist.setFromWhere(belist.getFromWhere());
	            	    	    nblist.setBenchmarkDn(EMSNameID);
	            	    	    nblist.setTableName("pwe3Count");
	            	    	    nblist.setCount(Integer.parseInt(pwe3Count));
	            	    	    nblist.setDvpercentage(Integer.parseInt(pwe3CountTage));
	            	    	    EmsBenchmarkItem solt=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, nblist);
	            	    	    context.end();
          	        
	            	      ////routeCount##11
	            	     
	            	    	    EmsBenchmarkItem lblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    lblist.setId(belist.getId());
	            	    	    lblist.setDn(SysUtil.nextDN());
	            	    	    lblist.setVersion(11L);
	            	    	    lblist.setFromWhere(belist.getFromWhere());
	            	    	    lblist.setBenchmarkDn(EMSNameID);
	            	    	    lblist.setTableName("routeCount");
	            	    	    lblist.setCount(Integer.parseInt(routeCount));
	            	    	    lblist.setDvpercentage(Integer.parseInt(routeCountTage));
	            	    	    EmsBenchmarkItem los=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, lblist);
	            	    	    context.end();
          	       
	                      ///tunnelPG##12
	            	      
	            	    	    EmsBenchmarkItem oblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    oblist.setId(belist.getId());
	            	    	    oblist.setDn(SysUtil.nextDN());
	            	    	    oblist.setVersion(12L);
	            	    	    oblist.setFromWhere(belist.getFromWhere());
	            	    	    oblist.setBenchmarkDn(EMSNameID);
	            	    	    oblist.setTableName("tunnelPG");
	            	    	    oblist.setCount(Integer.parseInt(tunnelPG));
	            	    	    oblist.setDvpercentage(Integer.parseInt(tunnelPGTage));
	            	    	    EmsBenchmarkItem trr=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, oblist);
	            	    	    context.end();
          	       
	            	     ///ccCount##13
	            	     
	            	    	    EmsBenchmarkItem qblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    qblist.setId(belist.getId());
	            	    	    qblist.setDn(SysUtil.nextDN());
	            	    	    qblist.setVersion(13L);
	            	    	    qblist.setFromWhere(belist.getFromWhere());
	            	    	    qblist.setBenchmarkDn(EMSNameID);
	            	    	    qblist.setTableName("ccCount");
	            	    	    qblist.setCount(Integer.parseInt(ccCount));
	            	    	    qblist.setDvpercentage(Integer.parseInt(ccCountTage));
	            	    	    EmsBenchmarkItem tt=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, qblist);
	            	    	    context.end();
          	        
	            	      ///sncCount##14
	            	     
	            	    	    EmsBenchmarkItem rblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    rblist.setId(belist.getId());
	            	    	    rblist.setDn(SysUtil.nextDN());
	            	    	    rblist.setVersion(14L);
	            	    	    rblist.setFromWhere(belist.getFromWhere());
	            	    	    rblist.setBenchmarkDn(EMSNameID);
	            	    	    rblist.setTableName("sncCount");
	            	    	    rblist.setCount(Integer.parseInt(sncCount));
	            	    	    rblist.setDvpercentage(Integer.parseInt(sncCountTage));
	            	    	    EmsBenchmarkItem st=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, rblist);
	            	    	    context.end();
          	         
	            	      ///ctpCount##15
	            	     
	            	    	    EmsBenchmarkItem sblist=new EmsBenchmarkItem();
	            	    	    context.begin();
	            	    	    sblist.setId(belist.getId());
	            	    	    sblist.setDn(SysUtil.nextDN());
	            	    	    sblist.setVersion(15L);
	            	    	    sblist.setFromWhere(belist.getFromWhere());
	            	    	    sblist.setBenchmarkDn(EMSNameID);
	            	    	    sblist.setTableName("ctpCount");
	            	    	    sblist.setCount(Integer.parseInt(ctpCount));
	            	    	    sblist.setDvpercentage(Integer.parseInt(ctpCountTage));
	            	    	    EmsBenchmarkItem lt=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, sblist);
	            	    	    context.end();
          	         
						
							
				    return 1;
				}catch (Exception e) {
						context.rollback();
						e.printStackTrace();
					}
					finally {
						context.release();
					}
					return -1; 
				}
	
	    
	
        /*** 
         * PTN保存
         */
		@Override
		public int insertEmsPtn(EmsBenchmark list,EmsBenchmarkItem ebklist) throws Exception {
			        JPAContext context = JPAContext.prepareReadOnlyContext();
			        try{
						context.begin();
						EmsBenchmark emsbk=new EmsBenchmark();
						emsbk.setId(list.getId());
						emsbk.setDn(SysUtil.nextDN());
						emsbk.setFromWhere(list.getFromWhere());
						emsbk.setEmsname(list.getEmsname());
						emsbk.setAdditinalInfo(list.getAdditinalInfo());
						emsbk.setStatus(list.getStatus());
						EmsBenchmark elist=(EmsBenchmark)JPAUtil.getInstance().saveObject(context, -1, emsbk);
					    context.end();
						
						///String sqls="select s.id,s.emsname from S_EMSBENCHMARK s where s.emsname ='" +list.getEmsname()+"'";
						  String sqls="SELECT MAX(id) FROM s_emsbenchmark";
						  List klist=JPAUtil.getInstance().querySQL(context,sqls);
						       String BenchmarkDn=klist.get(0).toString();
				            	EmsBenchmarkItem eb=new EmsBenchmarkItem();
				            	String tableNameObject =ebklist.getTableName();
				            	String benchmarkDnObject=ebklist.getBenchmarkDn();
				            	String checkEMSName=tableNameObject.substring(0, tableNameObject.indexOf("@emsnameInput"));
				            	String neCount=tableNameObject.substring(tableNameObject.indexOf("@emsnameInput")+13, tableNameObject.indexOf("@neCountInput"));
				            	String slotCount=tableNameObject.substring(tableNameObject.indexOf("@neCountInput")+13, tableNameObject.indexOf("@slotCountInput"));
				            	String subSlotCount=tableNameObject.substring(tableNameObject.indexOf("@slotCountInput")+15, tableNameObject.indexOf("@subSlotCountInput"));
				            	String equipmentCount=tableNameObject.substring(tableNameObject.indexOf("@subSlotCountInput")+18, tableNameObject.indexOf("@equipmentCountInput"));
				            	String ptpCount=tableNameObject.substring(tableNameObject.indexOf("@equipmentCountInput")+20, tableNameObject.indexOf("@ptpCountInput"));
				            	String ftpCount=tableNameObject.substring(tableNameObject.indexOf("@ptpCountInput")+14, tableNameObject.indexOf("@ftpCountInput"));
				            	String sectionCount=tableNameObject.substring(tableNameObject.indexOf("@ftpCountInput")+14, tableNameObject.indexOf("@sectionCountInput"));
				            	String tunnelCount=tableNameObject.substring(tableNameObject.indexOf("@sectionCountInput")+18, tableNameObject.indexOf("@tunnelCountInput"));
				            	String pwCount=tableNameObject.substring(tableNameObject.indexOf("@tunnelCountInput")+17, tableNameObject.indexOf("@pwCountInput"));
				            	String pwe3Count=tableNameObject.substring(tableNameObject.indexOf("@pwCountInput")+13, tableNameObject.indexOf("@pwe3CountInput"));
				            	String routeCount=tableNameObject.substring(tableNameObject.indexOf("@pwe3CountInput")+15, tableNameObject.indexOf("@routeCountInput"));
				            	String tunnelPG=tableNameObject.substring(tableNameObject.indexOf("@routeCountInput")+16, tableNameObject.indexOf("@tunnelPGInput"));
				            	String ccCount=tableNameObject.substring(tableNameObject.indexOf("@tunnelPGInput")+14, tableNameObject.indexOf("@ccCountInput"));
				            	String sncCount=tableNameObject.substring(tableNameObject.indexOf("@ccCountInput")+13, tableNameObject.indexOf("@sncCountInput"));
				            	String ctpCount=tableNameObject.substring(tableNameObject.indexOf("@sncCountInput")+14, tableNameObject.indexOf("@ctpCountInput"));
				            	
				            	
				            	
				            	String dvpercentage=benchmarkDnObject.substring(0, benchmarkDnObject.indexOf("@neCountCount"));
				            	String slotCounttage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@neCountCount")+13, benchmarkDnObject.indexOf("@slotCountCount"));
				            	String subSlotCounttage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@slotCountCount")+15, benchmarkDnObject.indexOf("@subSlotCountCount"));
				            	String equipmentCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@subSlotCountCount")+18, benchmarkDnObject.indexOf("@equipmentCountCount"));
				            	String ptpCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@equipmentCountCount")+20, benchmarkDnObject.indexOf("@ptpCountCount"));
				            	String ftpCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ptpCountCount")+14, benchmarkDnObject.indexOf("@ftpCountCount"));
				            	String sectionCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ftpCountCount")+14, benchmarkDnObject.indexOf("@sectionCountCount"));
				            	String tunnelCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@sectionCountCount")+18, benchmarkDnObject.indexOf("@tunnelCountCount"));
				            	String pwCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@tunnelCountCount")+17, benchmarkDnObject.indexOf("@pwCountCount"));
				            	String pwe3CountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@pwCountCount")+13, benchmarkDnObject.indexOf("@pwe3CountCount"));
				            	String routeCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@pwe3CountCount")+15, benchmarkDnObject.indexOf("@routeCountCount"));
				            	String tunnelPGTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@routeCountCount")+16, benchmarkDnObject.indexOf("@tunnelPGCount"));
				            	String ccCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@tunnelPGCount")+14, benchmarkDnObject.indexOf("@ccCountCount"));
				            	String sncCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@ccCountCount")+13, benchmarkDnObject.indexOf("@sncCountCount"));
				            	String ctpCountTage=benchmarkDnObject.substring(benchmarkDnObject.indexOf("@sncCountCount")+14, benchmarkDnObject.indexOf("@ctpCountCount"));
				            	  
				            		 //neCount
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    context.begin();
				            	    	    eb.setId(ebklist.getId());
							            	eb.setDn(SysUtil.nextDN());
							            	eb.setVersion(1L);
							            	eb.setFromWhere(ebklist.getFromWhere());
							            	eb.setBenchmarkDn(BenchmarkDn);
							            	eb.setTableName("neCount");
							            	eb.setCount(Integer.parseInt(neCount));
				            	    	    eb.setDvpercentage(Integer.parseInt(dvpercentage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, eb);
				            	    	    context.end();
				            	      }
				            	     
				            	      ///slotCount
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	  EmsBenchmarkItem eblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    eblist.setId(ebklist.getId());
				            	    	    eblist.setDn(SysUtil.nextDN());
				            	    	    eblist.setVersion(2L);
				            	    	    eblist.setFromWhere(ebklist.getFromWhere());
				            	    	    eblist.setBenchmarkDn(BenchmarkDn);
				            	    	    eblist.setTableName("slotCount");
				            	    	    eblist.setCount(Integer.parseInt(slotCount));
				            	    	    eblist.setDvpercentage(Integer.parseInt(slotCounttage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, eblist);
				            	    	    context.end();
				            	      }
				            	     
				            	      ////subSlotCount**3
				            	      if(list.getEmsname().equals(checkEMSName)){
					            	    	    EmsBenchmarkItem blist=new EmsBenchmarkItem();
					            	    	    context.begin();
					            	    	    blist.setId(ebklist.getId());
					            	    	    blist.setDn(SysUtil.nextDN());
					            	    	    blist.setVersion(3L);
					            	    	    blist.setFromWhere(ebklist.getFromWhere());
					            	    	    blist.setBenchmarkDn(BenchmarkDn);
					            	    	    blist.setTableName("subSlotCount");
					            	    	    blist.setCount(Integer.parseInt(subSlotCount));
					            	    	    blist.setDvpercentage(Integer.parseInt(subSlotCounttage));
					            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, blist);
					            	    	    context.end();
				            	      }
				            	      
				            	      //equipmentCount##4
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem dlist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    dlist.setId(ebklist.getId());
				            	    	    dlist.setDn(SysUtil.nextDN());
				            	    	    dlist.setVersion(4L);
				            	    	    dlist.setFromWhere(ebklist.getFromWhere());
				            	    	    dlist.setBenchmarkDn(BenchmarkDn);
				            	    	    dlist.setTableName("equipmentCount");
				            	    	    dlist.setCount(Integer.parseInt(equipmentCount));
				            	    	    dlist.setDvpercentage(Integer.parseInt(equipmentCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, dlist);
				            	    	    context.end();
			            	        }
				            	      
				            	    //ptpCount##5
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem eslist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    eslist.setId(ebklist.getId());
				            	    	    eslist.setDn(SysUtil.nextDN());
				            	    	    eslist.setVersion(5L);
				            	    	    eslist.setFromWhere(ebklist.getFromWhere());
				            	    	    eslist.setBenchmarkDn(BenchmarkDn);
				            	    	    eslist.setTableName("ptpCount");
				            	    	    eslist.setCount(Integer.parseInt(ptpCount));
				            	    	    eslist.setDvpercentage(Integer.parseInt(ptpCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, eslist);
				            	    	    context.end();
			            	        }
			            	         
				            	    //ftpCount##6
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem flist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    flist.setId(ebklist.getId());
				            	    	    flist.setDn(SysUtil.nextDN());
				            	    	    flist.setVersion(6L);
				            	    	    flist.setFromWhere(ebklist.getFromWhere());
				            	    	    flist.setBenchmarkDn(BenchmarkDn);
				            	    	    flist.setTableName("ftpCount");
				            	    	    flist.setCount(Integer.parseInt(ftpCount));
				            	    	    flist.setDvpercentage(Integer.parseInt(ftpCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, flist);
				            	    	    context.end();
			            	        }
				            	      ///sectionCount##7
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem jlist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    jlist.setId(ebklist.getId());
				            	    	    jlist.setDn(SysUtil.nextDN());
				            	    	    jlist.setVersion(7L);
				            	    	    jlist.setFromWhere(ebklist.getFromWhere());
				            	    	    jlist.setBenchmarkDn(BenchmarkDn);
				            	    	    jlist.setTableName("sectionCount");
				            	    	    jlist.setCount(Integer.parseInt(sectionCount));
				            	    	    jlist.setDvpercentage(Integer.parseInt(sectionCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, jlist);
				            	    	    context.end();
			            	        }
				            	      
				            	      ///tunnelCount##8
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem kblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    kblist.setId(ebklist.getId());
				            	    	    kblist.setDn(SysUtil.nextDN());
				            	    	    kblist.setVersion(8L);
				            	    	    kblist.setFromWhere(ebklist.getFromWhere());
				            	    	    kblist.setBenchmarkDn(BenchmarkDn);
				            	    	    kblist.setTableName("tunnelCount");
				            	    	    kblist.setCount(Integer.parseInt(tunnelCount));
				            	    	    kblist.setDvpercentage(Integer.parseInt(tunnelCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, kblist);
				            	    	    context.end();
			            	        }
			            	          ///pwCount##9
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem mblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    mblist.setId(ebklist.getId());
				            	    	    mblist.setDn(SysUtil.nextDN());
				            	    	    mblist.setVersion(9L);
				            	    	    mblist.setFromWhere(ebklist.getFromWhere());
				            	    	    mblist.setBenchmarkDn(BenchmarkDn);
				            	    	    mblist.setTableName("pwCount");
				            	    	    mblist.setCount(Integer.parseInt(pwCount));
				            	    	    mblist.setDvpercentage(Integer.parseInt(pwCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, mblist);
				            	    	    context.end();
			            	        }  
				            	    
				            	      ///pwe3Count##10
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem nblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    nblist.setId(ebklist.getId());
				            	    	    nblist.setDn(SysUtil.nextDN());
				            	    	    nblist.setVersion(10L);
				            	    	    nblist.setFromWhere(ebklist.getFromWhere());
				            	    	    nblist.setBenchmarkDn(BenchmarkDn);
				            	    	    nblist.setTableName("pwe3Count");
				            	    	    nblist.setCount(Integer.parseInt(pwe3Count));
				            	    	    nblist.setDvpercentage(Integer.parseInt(pwe3CountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, nblist);
				            	    	    context.end();
			            	        }  
				            	      ////routeCount##11
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem lblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    lblist.setId(ebklist.getId());
				            	    	    lblist.setDn(SysUtil.nextDN());
				            	    	    lblist.setVersion(11L);
				            	    	    lblist.setFromWhere(ebklist.getFromWhere());
				            	    	    lblist.setBenchmarkDn(BenchmarkDn);
				            	    	    lblist.setTableName("routeCount");
				            	    	    lblist.setCount(Integer.parseInt(routeCount));
				            	    	    lblist.setDvpercentage(Integer.parseInt(routeCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, lblist);
				            	    	    context.end();
			            	        }  
				                      ///tunnelPG##12
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem oblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    oblist.setId(ebklist.getId());
				            	    	    oblist.setDn(SysUtil.nextDN());
				            	    	    oblist.setVersion(12L);
				            	    	    oblist.setFromWhere(ebklist.getFromWhere());
				            	    	    oblist.setBenchmarkDn(BenchmarkDn);
				            	    	    oblist.setTableName("tunnelPG");
				            	    	    oblist.setCount(Integer.parseInt(tunnelPG));
				            	    	    oblist.setDvpercentage(Integer.parseInt(tunnelPGTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, oblist);
				            	    	    context.end();
			            	        }  
				            	     ///ccCount##13
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem qblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    qblist.setId(ebklist.getId());
				            	    	    qblist.setDn(SysUtil.nextDN());
				            	    	    qblist.setVersion(13L);
				            	    	    qblist.setFromWhere(ebklist.getFromWhere());
				            	    	    qblist.setBenchmarkDn(BenchmarkDn);
				            	    	    qblist.setTableName("ccCount");
				            	    	    qblist.setCount(Integer.parseInt(ccCount));
				            	    	    qblist.setDvpercentage(Integer.parseInt(ccCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, qblist);
				            	    	    context.end();
			            	        }  
				            	      ///sncCount##14
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem rblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    rblist.setId(ebklist.getId());
				            	    	    rblist.setDn(SysUtil.nextDN());
				            	    	    rblist.setVersion(14L);
				            	    	    rblist.setFromWhere(ebklist.getFromWhere());
				            	    	    rblist.setBenchmarkDn(BenchmarkDn);
				            	    	    rblist.setTableName("sncCount");
				            	    	    rblist.setCount(Integer.parseInt(sncCount));
				            	    	    rblist.setDvpercentage(Integer.parseInt(sncCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, rblist);
				            	    	    context.end();
			            	        }  
				            	      ///ctpCount##15
				            	      if(list.getEmsname().equals(checkEMSName)){
				            	    	    EmsBenchmarkItem sblist=new EmsBenchmarkItem();
				            	    	    context.begin();
				            	    	    sblist.setId(ebklist.getId());
				            	    	    sblist.setDn(SysUtil.nextDN());
				            	    	    sblist.setVersion(15L);
				            	    	    sblist.setFromWhere(ebklist.getFromWhere());
				            	    	    sblist.setBenchmarkDn(BenchmarkDn);
				            	    	    sblist.setTableName("ctpCount");
				            	    	    sblist.setCount(Integer.parseInt(ctpCount));
				            	    	    sblist.setDvpercentage(Integer.parseInt(ctpCountTage));
				            	    	    EmsBenchmarkItem emsebklist=(EmsBenchmarkItem)JPAUtil.getInstance().saveObject(context, -1, sblist);
				            	    	    context.end();
			            	        }  
				            	      
						
						   return 1;
					    }			
						catch (Exception e) {
							context.rollback();
							e.printStackTrace();
						}
						finally {
							context.release();
						}
						return -1; 
		}
        
		
		
		/*** 
		 * 查询所有的PTN信息
		 */
		@Override
		public List<EmsBenchmarkList> queryEmsBenchmarkList()
				throws RemoteException {
			List list = new ArrayList();
			JPAContext context = JPAContext.prepareReadOnlyContext();
			try{
				 ///List elist=JPAUtil.getInstance().findObjects(context,sqls);
				String sqls="select se.id,se.updatedate,se.emsname,se.status,em.benchmarkdn,em.count,em.dvpercentage,em.tablename  from s_emsbenchmark se,s_emsbenchmarkitem em where se.id=em.benchmarkdn";
				List elist=JPAUtil.getInstance().querySQL(context,sqls);
				BigDecimal bd = null;
				 for(int i=0;i<elist.size();i++){
						Object[] ob = (Object[]) elist.get(i);
				         EmsBenchmarkList blist=new EmsBenchmarkList();
				         bd=(BigDecimal)ob[0];
				         Long   id = bd.longValue();
				         ////Long   id = (Long) ob[0];
				         Date updatedate= (Date) ob[1];
				         String emsname= (String) ob[2];
				         bd=(BigDecimal)ob[3];
				         Integer status =bd.intValue();
				         ///Integer status =(Integer) ob[3];
				         String benchmarkDn=(String) ob[4];
				         ///Integer count=(Integer) ob[5];
				         bd=(BigDecimal)ob[5];
				         Integer count=bd.intValue();
				         ///Integer dvpercentage=(Integer) ob[6];
				         bd=(BigDecimal)ob[6];
				         Integer dvpercentage=bd.intValue();
				         String tableName=(String) ob[7];
				         
				         blist.setId(id);
				         blist.setUpdatedate(updatedate);
				         blist.setEmsname(emsname);
				         blist.setStatus(status);
				         blist.setBenchmarkDn(benchmarkDn);
				         blist.setCount(count);
				         blist.setDvpercentage(dvpercentage);
				         blist.setTableName(tableName);
				         list.add(blist);
				         
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
		 * 查询所有的EmsBenchmark信息
		 */
		@Override
		public List<EmsBenchmark> queryAllEmsBenchmark() throws RemoteException {
			JPAContext context = JPAContext.prepareReadOnlyContext();
			try{
				  List<EmsBenchmark> pmde=JPAUtil.getInstance().findObjects(context, "select pm from  EmsBenchmark pm order by to_number(id) desc");
				  return pmde;
				
			}catch (Exception e) {
				// TODO: handle exception
			}
			finally {
				
				context.release();
			}
			  return null;
		}
         
		
		/***
		 * 根据EMSName去查询相应的EmsBenchmark
		 */
		@Override
		public List<EmsBenchmark> queryAllEmsBenchmarkByName(String emsName)
				throws RemoteException {
			JPAContext context = JPAContext.prepareReadOnlyContext();
			try{
				 
				String sqls="select p from EmsBenchmark p";
				if (emsName != null && emsName != "") {
					sqls = sqls + " where emsname like '%" + emsName + "%'";
				}
				List<EmsBenchmark> emsch=JPAUtil.getInstance().findObjects(context, sqls);
				return emsch;
			}catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				
				context.release();
			}
			  return null;
		}
         
		
		/*** 
		 * 根据ID去查询EmsBenchmarkItem信息
		 */
		@Override
		public List<EmsBenchmarkItem> queryEmsBenchmarkItemById(String id)
				throws RemoteException {
			JPAContext context = JPAContext.prepareReadOnlyContext();
			try{
				 
				String sqls="select p from EmsBenchmarkItem p";
				if (id != null && id != "") {
					sqls = sqls + " where benchmarkDn like '%" + id + "%'";
				}
				List<EmsBenchmarkItem> emsch=JPAUtil.getInstance().findObjects(context, sqls);
				return emsch;
			}catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				
				context.release();
			}
			  return null;
		}
        
		
		/*** 
		 * 根据ID查询出EmsBenchmarkList实体信息
		 */
		@Override
		public List<EmsBenchmarkList> queryEmsBenchmarkListByID(Long id)
				throws RemoteException {
			// TODO Auto-generated method stub
			int sumes=0;
			List list = new ArrayList();
			JPAContext context = JPAContext.prepareReadOnlyContext();
			try{
				String sqls="select em.benchmarkdn,em.count,em.dvpercentage,em.tablename,em.version,em.id from s_emsbenchmarkitem em";
				if(id != null){
					 sqls = sqls + " where em.benchmarkdn like '%" + id + "%' order by version asc";					 
				 }
				List elist=JPAUtil.getInstance().querySQL(context,sqls);
				BigDecimal bd = null;
				 for(int i=0;i<elist.size();i++){
						Object[] ob = (Object[]) elist.get(i);
				         EmsBenchmarkList blist=new EmsBenchmarkList();
				         String benchmarkDn=(String) ob[0];
				         bd=(BigDecimal)ob[1];
				         Integer count=bd.intValue();
				         bd=(BigDecimal)ob[2];
				         Integer dvpercentage=bd.intValue();
				         String tableName=(String) ob[3];		       
				         bd=(BigDecimal)ob[4];
				         Long   version = bd.longValue();
				         bd=(BigDecimal)ob[5];
				         Long ids=bd.longValue();
				         
				         blist.setBenchmarkDn(benchmarkDn);
				         ///blist.setCount(count);
				         if(i==0){
				        	 blist.setNeCount(count);
				        	 blist.setNeCountTage(dvpercentage);
				        	 blist.setNeID(ids);
				         }
				         if(i==1){
				        	 blist.setSlotCount(count);
				        	 blist.setSlotCountTage(dvpercentage);
				        	 blist.setSlotID(ids);
				         }
				         if(i==2){
				        	 blist.setSubSlotCount(count);
				        	 blist.setSubSlotCountTage(dvpercentage);
				        	 blist.setSubSlotID(ids);
				         }
				         if(i==3){
				        	 blist.setEquipmentCount(count);
				        	 blist.setEquipmentCountTage(dvpercentage);
				        	 blist.setEquipmentID(ids);
				         }
				         if(i==4){
				        	
				        	 blist.setPtpCount(count);
				        	 blist.setPtpCountTage(dvpercentage);
				        	 blist.setPtpID(ids);
				         }
				         if(i==5){
				        	
				        	 blist.setFtpCount(count);
				        	 blist.setFtpCountTage(dvpercentage);
				        	 blist.setFtpID(ids);
				         }
				         if(i==6){
				        	
				        	 blist.setSectionCount(count);
				        	 blist.setSectionCountTage(dvpercentage);
				        	 blist.setSectionID(ids);
				         }
				         if(i==7){
				        
				        	 blist.setTunnelCount(count);
				        	 blist.setTunnelCountTage(dvpercentage);
				        	 blist.setTunnelID(ids);
				         }
				         if(i==8){
				        	 blist.setPwCount(count);
				        	 blist.setPwCountTage(dvpercentage);
				        	 blist.setPwID(ids);
				         }
				         if(i==9){
				        	 blist.setPwe3Count(count);
				        	 blist.setPwe3CountTage(dvpercentage);
				        	 blist.setPwe3ID(ids);
				         }
				         if(i==10){
				        	
				        	 blist.setRouteCount(count);
				        	 blist.setRouteCountTage(dvpercentage);
				        	 blist.setRouteID(ids);
				         }
				         if(i==11){
				        	
				        	 blist.setTunnelPG(count);
				        	 blist.setTunnelPGTage(dvpercentage);
				        	 blist.setTunnelPGID(ids);
				         }
				         if(i==12){
				        	 
				        	 blist.setCcCount(count);
				        	 blist.setCcCountTage(dvpercentage);
				        	 blist.setCcID(ids);
				         }
				         if(i==13){
				        	 blist.setSncCount(count);
				        	 blist.setSncCountTage(dvpercentage);
				        	 blist.setSncID(ids);
				         }
				         if(i==14){
				        	 blist.setCtpCount(count);
				        	 blist.setCtpCountTage(dvpercentage);
				        	 blist.setCtpID(ids);
				         }
				      
				         ///blist.setDvpercentage(dvpercentage);
				         ///blist.setTableName(tableName);
				        /// blist.setVersion(version);
				         list.add(blist);
				 }
				
				 return list;
				}catch (Exception e) {
					
				}
				finally {
					
					context.release();
				}
				return null;
				
				
		}


	public int EditEmsPtn(EmsBenchmark list, EmsBenchmarkItem belist) throws Exception {
		return 0;
	}
}