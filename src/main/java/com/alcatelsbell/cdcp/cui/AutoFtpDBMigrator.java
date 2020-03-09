package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.cdcp.nodefx.FtpUtil;
import com.alcatelsbell.cdcp.server.MigrateManager;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.util.DataFileUtil;
import com.alcatelsbell.nms.db.components.service.*;
import com.alcatelsbell.nms.util.LocalProperties;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.protocol.SFtpFunc;
import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.ManagedElement;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2014/12/2
 * Time: 16:36
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class AutoFtpDBMigrator {
    private Log logger = LogFactory.getLog(getClass());
    public void run() {
        String host = SysProperty.getString("ftpHost");
        String user = SysProperty.getString("ftpUser");
        String password = SysProperty.getString("ftpPassword");
        String ftpRootPath = SysProperty.getString("ftpRootPath","/");
        int port = SysProperty.getInt("ftpPort", 21);

        String ftpType = SysProperty.getString("ftpType", "FTP");

        /**
         *    db.dir.1.path = /opt/data/HWU2000
         *    db.dir.1.loader = com.xxx.xxx
         */
        Properties dirs = SysProperty.getProperties("ftp.db.dir");
        System.out.println("dirs = " + dirs.size());
        LocalProperties times = LocalProperties.load("dbtime.properties");

        if (ftpType.equals("FTP")) {
//            FtpInfo ftpInfo = new FtpInfo(user, password, host, port, ftpRootPath + "/" + domain + "/" + vendor + "/" + city, file.getName());
//            ftpInfo.setType(FtpInfo.TYPE_FTP);

        }  else if (ftpType.equals("SFTP")) {

            SFtpFunc sFtpFunc = new SFtpFunc();
            ChannelSftp connect = null;
            try {
                connect = sFtpFunc.connect(host, port, user, password);
            } catch (Exception e) {
                logger.error(e, e);
            }
            System.out.println(host+ " connected  "+" dirs.stringPropertyNames() = "+ dirs.stringPropertyNames().size());
        //    List<ChannelSftp.LsEntry> entries = new ArrayList<ChannelSftp.LsEntry>();
            HashMap<String,ChannelSftp.LsEntry>   entries = new HashMap<String, ChannelSftp.LsEntry>();
            for (String key : dirs.stringPropertyNames()) {
                if (key.endsWith(".path")) {

                    try {
                        String dir = dirs.getProperty(key);
                        String prefix = key.substring(0,key.indexOf(".path"));


                        int t = times.getIntProperty(  key,-1);
                        ChannelSftp.LsEntry entry = null;
                        System.out.println("ls : "+dir);
                        Vector<ChannelSftp.LsEntry>  ls = connect.ls( dir);
                        System.out.println("fileSize = " + ls.size());
                        for (ChannelSftp.LsEntry l : ls) {
                            if (l.getFilename().equals(".") || l.getFilename().equals(".."))
                                continue;
                            int mTime = l.getAttrs().getMTime();
                            if (mTime > t) {
                                t = mTime;
                                entry = l;
                            }
                        }

                        if (entry != null) {
                            entries.put(key, entry);
                            System.out.println("find file : " + entry.getFilename());



                        }
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }

            }

            connect.disconnect();

            for (String key : entries.keySet()) {
                String prefix = key.substring(0,key.indexOf(".path"));
                String loader = dirs.getProperty(prefix+".loader");
                String dir = dirs.getProperty(key);
                System.out.println("process key : "+key+"="+dir);

                ChannelSftp.LsEntry entry = entries.get(key);
                FtpInfo ftpInfo = new FtpInfo(user, password, host, port,  dir, entry.getFilename());
                ftpInfo.setType(ftpType);
                try {
                    migrateDB(ftpInfo, Class.forName(loader));
                    times.setProperty(key, entry.getAttrs().getMTime()+"");
                    times.save();
                } catch (Exception e) {
                    logger.error(e, e);
                }

            }






        }

    }

    private void migrateDB(FtpInfo ftpInfo,Class loader) throws Exception {
        logger.info("Migrating : "+ftpInfo);
        File file = DataFileUtil.downloadFile(ftpInfo);
        if (!file.exists()) throw new Exception("下载文件失败:"+ftpInfo);

        JPASupport sqliteJPASupport = JPASupportFactory.createSqliteJPASupport(file.getAbsolutePath());
        List<ManagedElement> mes = JPAUtil.getInstance().findObjects(sqliteJPASupport, "select c from ManagedElement c", null, null, 0, 1);
        if (mes.size() > 0) {
            String emsName = mes.get(0).getEmsName();
            AbstractDBFLoader dbfLoader = (AbstractDBFLoader)
                    loader.getConstructor(String.class, String.class).newInstance(file.getAbsolutePath(),emsName);
            dbfLoader.execute();
        } else {
            throw new Exception(" ManagedElement size is 0 file = "+ftpInfo.getFileName());
        }
    }

    public static void main(String[] args) throws Exception {
        URL resource = AutoFtpDBMigrator.class.getClassLoader().getResource("META-INF/persistence.xml");
        System.out.println("resource = " + resource);

        URL resource2 = AutoFtpDBMigrator.class.getClassLoader().getResource("appserver-spring.xml");
        System.out.println("resource2 = " + resource2);

        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);

        MigrateManager.getInstance();
        System.out.println("MigrateManager init ok !!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("ctx = " + ctx);


        JPASupportSpringImpl context = new JPASupportSpringImpl("entityManagerFactoryData");
        try
        {
          //  context.begin();
            String[] preLoadSqls = Constants.PRE_LOAD_SQLS;
            for (String sql : preLoadSqls) {
                try {
                    DBUtil.getInstance().executeNonSelectingSQL(context,sql);
                } catch (Exception e) {
                   // logger.error(e, e);
                }
            }
            preLoadSqls = Constants.ptn_sqls;
            for (String sql : preLoadSqls) {
                try {
                    DBUtil.getInstance().executeNonSelectingSQL(context,sql);
                } catch (Exception e) {
                  //  logger.error(e, e);
                }
            }
         //   context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }
        System.out.println("AutoFtpDBMigrator start 0000000000000000000" );
        AutoFtpDBMigrator migrator = new AutoFtpDBMigrator();
         while (true) {
            migrator.run();
             try {
                 Thread.sleep(60000l * 5);
             } catch (InterruptedException e) {

             }
         }

    }

    public static void main2(String[] args) throws Exception {
        SFtpFunc sFtpFunc = new SFtpFunc();
        ChannelSftp connect = sFtpFunc.connect("135.251.223.141", 22, "sbell", "Sbell@123");
        Vector<ChannelSftp.LsEntry> ls = connect.ls(".");
        System.out.println("ls = " + ls);
    }
}
