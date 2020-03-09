package com.alcatelsbell.cdcp.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.EDS_PTN;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.common.model.EmsBenchmark;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkItem;
import com.alcatelsbell.cdcp.common.model.EmsBenchmarkList;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.valueobject.domain.EmsAlarm;

public class EmsAlarmAcounts {
	private static  final Log logger = LogFactory.getLog(EDS_PTN.class);
	
	private static  final Log loglist = LogFactory.getLog(EmsAlarm.class);
	
	
	
	/*** 
	 * 根据EMSName 查询当前的EDS-PTN
	 * @param emsName
	 * @return
	 */
	public List<EDS_PTN> queryByEmsName(String emsName){
		try{
			EmsAlarmIFC emsifc=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
			   return emsifc.queryByEmsName(emsName);
			}
		    catch (Exception e) {
				e.printStackTrace();
				logger.error(e, e);
			}
			return null;	
		
	}
	
	/*** 
	 *  查询出所有的EMS
	 * @return
	 */
	public List<EDS_PTN> queryAllEmsPtn(){
		List<EDS_PTN>  list=new ArrayList<EDS_PTN>();
		   try{
			   EmsAlarmIFC emsst=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
			   list=emsst.queryAllEmsPtn();
			   return list;
			   
		   } catch (Exception e) {
				
				e.printStackTrace();
				logger.error(e, e);
			}
			return list;
		
		
	}
	
	
	/*** 
	 * 保存PTN
	 * @param list
	 * @return
	 */
	public int insertEmsPtn(EmsBenchmark list,EmsBenchmarkItem ebklist){
		  try{
		   EmsAlarmIFC ifc=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
		   return ifc.insertEmsPtn(list,ebklist);
		  }
		   catch (Throwable e) {
			   loglist.error(e.getMessage(),e);
			}
			return -1;
	}
	
	 /***编辑PTN
	  * 
	  * @param list
	  * @param belist
	  * @return
	  */
	 public int updateEmsBenchmarkPtn(EmsBenchmarkItem belist){
		try{
			 EmsAlarmIFC ifc=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
			  return ifc.updateEmsBenchmarkPtn(belist);
	   }
		   catch (Throwable e) {
			   loglist.error(e.getMessage(),e);
			}
			return -1;
	}
	
	/*** 
	 * 从flex类型转换为java类型
	 * @param list
	 * @return
	 */
	private List convertJavaEmsAlarm(EmsBenchmark list){
		      List elist= new ArrayList();
		      EmsBenchmark ebk=new EmsBenchmark();
		      ebk.setId(list.getId());
		      ebk.setDn(list.getDn());
		      ebk.setFromWhere(list.getFromWhere());
		      ebk.setEmsname(list.getEmsname());
		      ebk.setAdditinalInfo(list.getAdditinalInfo());
		      ebk.setStatus(list.getStatus());
		      elist.add(ebk);
		      return elist;
	}
	
	/*** 
	 * 从flex类型转换为java类型
	 * @param list
	 * @return
	 */
	private List convertJavaEbki(EmsBenchmarkItem ebklist){
		      List adapter= new ArrayList();
		      EmsBenchmarkItem ek=new EmsBenchmarkItem();
		      ek.setId(ebklist.getId());
		      ek.setDn(ebklist.getDn());
		      ek.setFromWhere(ebklist.getFromWhere());
		      ek.setCount(ebklist.getCount());
		      ek.setTableName(ebklist.getTableName());
		      ek.setBenchmarkDn(ebklist.getBenchmarkDn());
		      adapter.add(ek);
		      return adapter;
	}
	
	
	/*** 
	 * PTN查询
	 * @return
	 */
	public List<EmsBenchmarkList> queryEmsBenchmarkList(){
		       List<EmsBenchmarkList> list=new ArrayList<EmsBenchmarkList>();
		       try{ 
				       EmsAlarmIFC emsst=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
					   list=emsst.queryEmsBenchmarkList();
					   return list;
		       } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e, e);
				}
				return list;
			
	}
	
	/***
	 * 查询所有的EmsBenchmark信息
	 * @return
	 */
	public List<EmsBenchmark> queryAllEmsBenchmark(){
		  List<EmsBenchmark> list=new ArrayList<EmsBenchmark>();
	       try{ 
			       EmsAlarmIFC emsst=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
				   list=emsst.queryAllEmsBenchmark();
				   return list;
	       } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e, e);
			}
			return list;
		
		
		
	}
	
	/*** 
	 * 根据EMSName去查询相应的EmsBenchmark
	 * @return
	 */
	public List<EmsBenchmark> queryAllEmsBenchmarkByName(String emsName){
		 List<EmsBenchmark> list=new ArrayList<EmsBenchmark>();
		 try{ 
			 EmsAlarmIFC emsst=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
			 return emsst.queryAllEmsBenchmarkByName(emsName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e, e);
				}
				return list;
		
	}
	
	/*** 
	 * 根据ID去查询EmsBenchmarkItem信息
	 * @param id
	 * @return
	 */
	public List<EmsBenchmarkItem> queryEmsBenchmarkItemById(String id){
		List<EmsBenchmarkItem> list=new ArrayList<EmsBenchmarkItem>();
		 try{ 
			 EmsAlarmIFC emsst=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
			 return emsst.queryEmsBenchmarkItemById(id);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e, e);
				}
				return list;
		
	}
	
	/****
	 * 根据ID查询出EmsBenchmarkList实体信息
	 * @param id
	 * @return
	 */
	public List<EmsBenchmarkList> queryEmsBenchmarkListByID(Long id){
		    List<EmsBenchmarkList> list=new ArrayList<EmsBenchmarkList>();
			try{ 
				       EmsAlarmIFC emsst=(EmsAlarmIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSALARM);
					   list=emsst.queryEmsBenchmarkListByID(id);
					   return list;
		       } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e, e);
			}
			return list;
		
	}
		
		
	

}
