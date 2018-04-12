package com.alcatelsbell.cdcp.server.v3;

import com.alcatelsbell.cdcp.nodefx.EmsJob;
import com.alcatelsbell.cdcp.server.MigrateManager;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.MigrateThread;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.util.SysProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 2016/12/27
 * Time: 9:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public abstract class SBIFileLoader extends AbstractDBFLoader{
    private String JOB_TYPE = null;
    public SBIFileLoader(String emsName,File file) {
        this.emsdn = emsName;
        MigrateThread.thread().initLog(emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
        this.fileUrl = file.getAbsolutePath();
    }

    public void execute() throws Exception {
        checkSuspend();
        long startTime = System.currentTimeMillis();




          try {
            DatabaseUtil.reset();
            JOB_TYPE = EmsJob.JOB_TYPE_SYNC_EMS;
            if (!new File(fileUrl).exists()) throw new Exception("数据文件不存在，同步终止");



            getLogger().info("Using Loader:"+this.getClass().getName());
            checkDataFile();
            getLogger().info("数据校验文件合格:"+fileUrl);
            getLogger().info("attrubites = "+this.getAttributesMap());

            //	getLogger().info("migrate logical = "+migrateLogical);

            doExecute();
         //   afterExecute();

        } catch (Exception e) {
            getLogger().error(e, e);


            throw e;
        }   finally {


            try {
                if (!SysProperty.getString("cdcp.migrate.deleteDBFile","true").equalsIgnoreCase("false")) {
                    if (!fileUrl.endsWith("-mt.db")) {
                        getLogger().info("Delete file : " + fileUrl);
                        boolean delete = new File(fileUrl).getAbsoluteFile().delete();
                        getLogger().info("Delete file : " + fileUrl + " = " + delete);
                    }
                }
                int size = MigrateManager.getInstance().getQueue().size();
                long cost = System.currentTimeMillis() - startTime;
                getLogger().info("spend : "+((cost) /  (1000l * 60l))+" minutes");
                getLogger().info(size+" tasks is waiting to be processed !");



            } catch (Throwable e) {
                getLogger().error(e, e);
            }
            MigrateThread.thread().end();

        }
    }


}
