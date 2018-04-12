package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-9
 * Time: 下午3:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MultiValueMap extends HashMap {
    private Log logger = LogFactory.getLog(getClass());

    public void put(Object key,Object... values){
         super.put(key,values);
    }

    public Object get(Object key,int idx) {
        Object o = get(key);
        if (o instanceof Object[])
            return ((Object[])o)[idx];
        return o;
    }


    public static void main(String[] args) {
        MultiValueMap map = new MultiValueMap();
        map.put("a","a1","a2");
        map.put("b","b1","b2");
        map.put("c","c1");
        System.out.println("map = " + map.get("b",1));


    }

}
