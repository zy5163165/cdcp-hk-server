package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.FHOTNM2000Migrator;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000DBFMigrator;
import com.alcatelsbell.cdcp.util.MigrateThread;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.persistence.EntityManager;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Author: Ronnie.Chen
 * Date: 14-10-13
 * Time: 下午2:06
 * rongrong.chen@alcatel-sbell.com.cn
 */

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.server.adapters.DBDataUtil;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWU2000DBFMigrator;
import com.alcatelsbell.cdcp.util.MigrateThread;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.persistence.EntityManager;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
public class BatchMigrateDBs {
    protected static void updateEmsStatus(String emsdn,int status) {
        JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");

        try {
            jpaSupport.begin();
            JPAUtil.getInstance().executeQL(jpaSupport, "update CEMS c set c.status = " + status + " where c.dn = '" + emsdn + "'");
            jpaSupport.end();
        } catch (Exception e) {
            logger.error(e, e);
            jpaSupport.rollback();

        } finally {
            jpaSupport.release();
        }
    }
    private static Log logger = LogFactory.getLog(BatchMigrateDBs.class);
    private static void migrateHWPtnDB(String fileUrl,final String emsDn) throws Exception {
        HWU2000DBFMigrator loader = new HWU2000DBFMigrator(fileUrl,
                emsDn){
            public void doExecute() throws Exception {

                checkEMS(emsdn, "华为");
                logAction("migrateRoute", "同步路由", 70);
                migrateIPRoute();
                getLogger().info("release");

                // ////////////////////////////////////////
                sd.release();

            }


            public void afterExecute() {
        //        printTableStat();
            }
            public void afterExecute1() {
                printTableStat();

                BatchMigrateDBs.updateEmsStatus(emsDn, Constants.CEMS_STATUS_READY);
                try {
                    CdcpServerUtil.sendMigrateLogMessage(taskSerial, emsdn, "同步结束", 100);
                } catch (Exception e) {
                    getLogger().error(e, e);
                }
                MigrateThread.thread().end();

                if (true) {
                    logger.info("MigrateFinish ,callIrmAutoMigrate");
                    JPASupport ctx = DBDataUtil.createJPASupport();
                    try {
                        EntityManager entityManager = ctx.getEntityManager();
                        if (entityManager instanceof HibernateEntityManager) {
                            Session session = ((HibernateEntityManager) entityManager).getSession();
                            session.doWork(new Work() {
                                @Override
                                public void execute(Connection connection) throws SQLException {
                                    CallableStatement cstmt = connection.prepareCall("{call callIrmAutoMigrate(?)}");
                                    cstmt.setString(1, emsDn);
                                    cstmt.execute();
                                    logger.info("callIrmAutoMigrate sucess");
                                }
                            });
                        } else {
                            logger.error("Not Hibernate Entity Manager :" + entityManager);
                        }
                    } catch (HibernateException e) {
                        logger.error(e, e);
                    } finally {
                        ctx.release();
                    }
                }

            }

        };
        loader.execute();
    }

    private static void migrateFHPtnDB(String fileUrl,final String emsDn) throws Exception {
        FHOTNM2000Migrator loader = new FHOTNM2000Migrator(fileUrl,
                emsDn){
            public void doExecute() throws Exception {

                checkEMS(emsdn, "烽火");
                logAction("migrateRoute", "同步路由", 70);
                migrateIPRoute();
                getLogger().info("release");

                // ////////////////////////////////////////
                sd.release();

            }
            public void afterExecute() {
                printTableStat();
            }
            public void afterExecute1() {
                printTableStat();

                BatchMigrateDBs.updateEmsStatus(emsDn, Constants.CEMS_STATUS_READY);
                try {
                    CdcpServerUtil.sendMigrateLogMessage(taskSerial, emsdn, "同步结束", 100);
                } catch (Exception e) {
                    getLogger().error(e, e);
                }
                MigrateThread.thread().end();

                if (true) {
                    logger.info("MigrateFinish ,callIrmAutoMigrate");
                    JPASupport ctx = DBDataUtil.createJPASupport();
                    try {
                        EntityManager entityManager = ctx.getEntityManager();
                        if (entityManager instanceof HibernateEntityManager) {
                            Session session = ((HibernateEntityManager) entityManager).getSession();
                            session.doWork(new Work() {
                                @Override
                                public void execute(Connection connection) throws SQLException {
                                    CallableStatement cstmt = connection.prepareCall("{call callIrmAutoMigrate(?)}");
                                    cstmt.setString(1, emsDn);
                                    cstmt.execute();
                                    logger.info("callIrmAutoMigrate sucess");
                                }
                            });
                        } else {
                            logger.error("Not Hibernate Entity Manager :" + entityManager);
                        }
                    } catch (HibernateException e) {
                        logger.error(e, e);
                    } finally {
                        ctx.release();
                    }
                }

            }

        };
        loader.execute();
    }
    public static void main(String[] args) throws Exception {
        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);

        String dirpath = "/data/sqlitedb/PTN/FENGHUO";

        if (args[0].equals("hwptn")) {
            dirpath = "/data/sqlitedb/PTN/HUAWEI";
        }
        if (args[0].equals("fhptn")) {
            dirpath = "/data/sqlitedb/PTN/FENGHUO";
        }

        File file = new File(dirpath);
        File[] emss = file.listFiles();
        for (File ems : emss) {
            if (ems.isDirectory()) {
                String emsdn = ems.getName();
                if (args != null && args.length > 1) {
                    if (!ems.getName().equals(args[1])) continue;
                }
                File[] files = ems.listFiles();

                File f = null;
                if (files != null) {
                    for (File file1 : files) {
                        if (f == null) f = file1;
                        else if (file1.lastModified() > f.lastModified())
                            f = file1;
                    }
                }
                System.out.println("[INFO]Migrating ========== "+emsdn+" : "+f.getAbsolutePath());
                logger.info("[INFO]Migrating ========== "+emsdn+" : "+f.getAbsolutePath());
                if (args[0].equals("hwptn")) {
                    migrateHWPtnDB(f.getAbsolutePath(), emsdn);
                }
                else if   (args[0].equals("fhptn")) {
                    migrateFHPtnDB(f.getAbsolutePath(),emsdn);
                }
            }
        }

    }

}
