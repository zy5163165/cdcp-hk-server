package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.server.snmp.PlanExecutor;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 14-8-26
 * Time: 下午1:22
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CycleJob extends Thread{
    private Log logger = LogFactory.getLog(getClass());
    private long intervalMilis = 1;
    private Schedule schedule;

    private Ems ems ;
    public CycleJob(Schedule schedule,long intervalMilis,Ems ems) {

        this.intervalMilis = intervalMilis;
        this.ems = ems;
        this.schedule = schedule;
    }

    public void run() {
        while (true) {
            String serial =  ems.getDn()+"@"+ new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            MigrateManager.getInstance().registerEmsJob(schedule,ems.getDn(),serial);

            try {
                String result = new PlanExecutor(ems.getControlName()).run();
                System.out.println("CycleJob finish:" + serial);
                MigrateManager.getInstance().handleMigrateFinish(serial, false,true);
                String ql = " update Task c set c.tag3 = '" + result+"'" + " where c.dn = '" + serial + "'";;
                System.out.println("ql = " + ql);
                CdcpServerUtil.executeUpdateQl(ql);
                try {
                    Thread.sleep(intervalMilis);
                } catch (InterruptedException e) {
                    logger.error(e, e);
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }

}
