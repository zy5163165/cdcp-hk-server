package com.alcatelsbell.cdcp.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.asb.mule.probe.framework.util.FileLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-13
 * Time: 下午1:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrateThread {
    private static MigrateThread ourInstance = new MigrateThread();

    public static MigrateThread thread() {
        return ourInstance;
    }

    private MigrateThread() {
    }

    private ThreadLocal threadLocal = new ThreadLocal();

    private ThreadLocal threadDis = new ThreadLocal();

    public void end() {

        FileLogger logger =  (FileLogger)threadLocal.get();

        List list = (List)threadDis.get();
        if (list != null) {
            if (logger != null) {
                logger.info(list.size()+" dis found ");

            }
            for (Object o : list) {
                try {
                   if ( ((DataInserter)o).end()) {
                       if (logger != null)
                           logger.info(" found unclosed dataInserter !");
                   }
                } catch (Exception e) {
                    if (logger != null)
                        logger.error(e, e);
                }
            }
        }
        threadDis.set(null);

        if (logger != null)
            logger.close();
        threadLocal.set(null);


    }
    public void initLog(String name) {
        FileLogger fileLogger = new FileLogger(name);
        threadLocal.set(fileLogger);

        List<DataInserter> diList = new ArrayList<DataInserter>();
        threadDis.set(diList);
    }

    public void addDI(DataInserter dataInserter) {
        List list = (List)threadDis.get();
        if (list != null) {
            list.add(dataInserter);
        }
    }

    public void initLog(FileLogger logger) {
        threadLocal.set(logger);
    }
    public Logger getLogger() {
        FileLogger logger =  (FileLogger)threadLocal.get();
        if (logger == null)
            return Logger.getLogger(getClass());
        return logger.getLogger();
    }

    public static void main(String[] args) {

        FileLogger fileLogger = new FileLogger("moli.log");
        fileLogger.getLogger().setLevel(Level.DEBUG);
        fileLogger.info("abc");
    }


    static class TestThread extends Thread{
        private String name;

        TestThread(String name) {
            this.name = name;
        }

        public void run() {
            MigrateThread.thread().initLog(name);
            for (int i = 0; i < 100; i++) {
                MigrateThread.thread().getLogger().info(i+"");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                }
            }

        }
    }
}
