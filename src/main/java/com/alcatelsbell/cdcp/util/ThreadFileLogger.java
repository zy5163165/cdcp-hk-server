package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-13
 * Time: 下午1:31
 * rongrong.chen@alcatel-sbell.com.cn
 */


import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.asb.mule.probe.framework.util.FileLogger;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-10
 * Time: 上午10:18
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ThreadFileLogger implements Log{
    public static final String ATTRIBUTE_LOG_NAME = "ATTRIBUTE_LOG_NAME";
    ThreadLocal tl = new ThreadLocal();
    public ThreadFileLogger(String name) {
        FileLogger fl = new FileLogger(name+".log");
        tl.set(fl);
    }

    private Logger getLogger() {

        return (Logger)tl.get();
    }

    @Override
    public void debug(Object o) {

    }

    @Override
    public void debug(Object o, Throwable throwable) {

    }

    @Override
    public void error(Object o) {

    }

    @Override
    public void error(Object o, Throwable throwable) {

    }

    @Override
    public void fatal(Object o) {

    }

    @Override
    public void fatal(Object o, Throwable throwable) {

    }

    @Override
    public void info(Object o) {

    }

    @Override
    public void info(Object o, Throwable throwable) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public boolean isFatalEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void trace(Object o) {

    }

    @Override
    public void trace(Object o, Throwable throwable) {

    }

    @Override
    public void warn(Object o) {

    }

    @Override
    public void warn(Object o, Throwable throwable) {

    }
}
