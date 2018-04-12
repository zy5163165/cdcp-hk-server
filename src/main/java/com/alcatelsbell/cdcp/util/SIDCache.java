package com.alcatelsbell.cdcp.util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-29
 * Time: 下午1:25
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SIDCache {
    private static ConcurrentHashMap<Class,HashMap> hashMap = new ConcurrentHashMap();
    public static void clear() {
        hashMap.clear();
    }

    public static void addSID(Class cls,String dn,Long sid) {
        if (hashMap.get(cls) == null) {
            synchronized (hashMap) {
                if (hashMap.get(cls) == null) {
                    hashMap.put(cls,new HashMap());
                }
            }
            hashMap.get(cls).put(dn,sid);
        }  else
            hashMap.get(cls).put(dn,sid);
    }

    public static Long getSID(Class cls,String dn) {
        HashMap map = hashMap.get(cls);
        if (map != null) {
            return (Long) map.get(dn);
        }
        return null;
    }

    public static ConcurrentHashMap getCache() {
        return hashMap;
    }
}
