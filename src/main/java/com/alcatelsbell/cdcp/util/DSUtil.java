package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-17
 * Time: 下午3:58
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DSUtil {
    /**
     * 把value放到key所对应的list中
     * @param map
     * @param key
     * @param value
     */
    public static void putIntoValueList(HashMap  map,Object key,Object value) {
        List list = (List)map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key,list);
        }
        list.add(value);
    }


}
