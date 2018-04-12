package com.alcatelsbell.cdcp.web.common;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2016/9/14
 * Time: 16:20
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Utils {
    public static final String EMPTY_STRING = "";
    /**  */
    public static boolean notEmpty(String string) {
        return null != string && !EMPTY_STRING.equals(string);
    }

    public static boolean notEmpty(List<?> list) {
        return null != list && !list.isEmpty();
    }

    public static boolean notEmpty(Map<?, ?> map) {
        return null != map && !map.isEmpty();
    }

    public static boolean notEmpty(Collection<?> collection) {
        return null != collection && !collection.isEmpty();
    }

    public static boolean notEmpty(String[] array) {
        return null != array && array.length > 0;
    }

    public static boolean notEmpty(short[] array) {
        return null != array && array.length > 0;
    }

    public static boolean notEmpty(long[] array) {
        return null != array && array.length > 0;
    }

    public static Throwable getRootCause(Throwable e) {
        if (e.getCause() != null) {
            return getRootCause(e.getCause());
        }
        return e;
    }

    public static void main(String[] args) throws Exception {
        String s = URLDecoder.decode("asdfasdfad","utf-8");
        System.out.println("s = " + s);
    }
}
