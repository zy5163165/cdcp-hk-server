package com.alcatelsbell.cdcp.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2015/4/10
 * Time: 11:02
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface CdcpOverviewMBean {
     public String showEmsOverview();

     public String showMigrateTasks();

     public String showMigrateHistory();


     public String showTaskHistorys();

     public String querySqlMgr(String sql);

     public String querySqlData(String sql);

     public String showEmsMigrateLog(String emsdn) ;

     public String syncDevice(String deviceDn) ;

     public String syncDeviceSection(String deviceDn) ;

     public String syncEmsByName(String emsdn,String paras) ;
     public String syncEmsByNodeDn(String nodeDn,String paras);



     public String scheduleEmsWithCronExp(String emsdn,String cron);

     public String changeUserPassword(String emsdn,String user,String password);
     public String removeEms(String emsdn) ;
     public String callIrmSuccess(String taskDn);
     public String callIrmSectionSuccess(String taskDn2);
     public String listUnfinishDeviceTasks(String hours) ;
     public String runUnfinishDeviceTasks(String hours);
}
