package com.alcatelsbell.cdcp.api;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.EDS_PTN;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.cdcp.common.Constants;
public class EmsStatisticsAcounts {
	
	private static  final Log logger = LogFactory.getLog(EDS_PTN.class);
	
	/***
	 * 查询全部
	 */
	public List<EDS_PTN> getAllEmsStatistics(){
		
		 List<EDS_PTN>  list=new ArrayList<EDS_PTN>();
		   try{
			   EmsStatisticsIFC emsst=(EmsStatisticsIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSSTATISTICS);
			   list=emsst.getAllEmsStatistics();
			   return list;
			   
		   } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e, e);
			}
			return list;
		
		
		
	}
	
	
	/***
	 * 查询当前在操作的ＥＭＳ
	 * @return
	 */
	public List<EDS_PTN> queryMaxEmsStatistics(){
		 List<EDS_PTN>  li=new ArrayList<EDS_PTN>();
		try{
			   EmsStatisticsIFC et=(EmsStatisticsIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSSTATISTICS);
			   li=et.queryMaxEmsStatistics();
			   return li;
			  
		   } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e, e);
			}
			return li;
		
		
	}
	
	/***
	 * 查询前一周的ＥＭＳ
	 * @param collectTime
	 * @return
	 */
	public List<EDS_PTN> queryByCollectTime(List li){
		try{
			EmsStatisticsIFC emsifc=(EmsStatisticsIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSSTATISTICS);
			List adapter = this.convertJavaEmsStatistics(li);
			return emsifc.queryByCollectTime(adapter);
			
		} 
		catch (Exception e) {
				e.printStackTrace();
				logger.error(e, e);
			}
			return null;	 
	}
	
	/***
	 * 时间段查询
	 * @param li
	 * @return
	 */
	public List<EDS_PTN> queryByTime(List li){
		try{
			EmsStatisticsIFC emsifc=(EmsStatisticsIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSSTATISTICS);
			List EMSList = this.convertJavaEmsStatistics(li);
			return emsifc.queryByTime(EMSList);
			
		} 
		catch (Exception e) {
				e.printStackTrace();
				logger.error(e, e);
			}
			return null;	
	
	}
	
	
	/**** 
	 * 根据每月时间查询
	 * @param list
	 * @return
	 */
	public List<EDS_PTN> queryByMonth(List list){
		try{
			EmsStatisticsIFC emsifc=(EmsStatisticsIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSSTATISTICS);
			List MonthList = this.convertJavaEmsStatistics(list);
			return emsifc.queryByMonth(MonthList);
			
		} 
		catch (Exception e) {
				e.printStackTrace();
				logger.error(e, e);
			}
			return null;	
	}
	
	/*** 
	 * 根据emsName查询所有的
	 * @param emsName
	 * @return
	 */
	public List<EDS_PTN> queryByEmsName(String emsName){
		 
		try{
		   EmsStatisticsIFC emsifc=(EmsStatisticsIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSSTATISTICS);
		   return emsifc.queryByEmsName(emsName);
		}
	    catch (Exception e) {
			e.printStackTrace();
			logger.error(e, e);
		}
		return null;	
	}
	
	
	/*** 
	 * EMS导出为Excel格式
	 * @param li
	 */
	public void QueryEmsExcel(List li){
		  try{
			  EmsStatisticsIFC emsifc=(EmsStatisticsIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMSSTATISTICS);
			   emsifc.QueryEmsExcel(li);
		  }
		  catch (Exception e) {
				e.printStackTrace();
				logger.error(e, e);
			}
			
		
		
		
	}
	/*** 
	 * 转化为flase那边的集合
	 * @param list
	 * @return
	 */
	private List convertJavaEmsStatistics(List list){
        List adapter= new ArrayList();
        EDS_PTN   javaManagedelement=(EDS_PTN)list.get(0);
		adapter.add(javaManagedelement);
		return adapter;
}
	
	
}
