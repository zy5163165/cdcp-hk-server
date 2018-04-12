package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.cdcp.nbi.model.CPTP;
import com.alcatelsbell.cdcp.nbi.model.CdcpObject;
import com.alcatelsbell.cdcp.server.IllegalDNStringException;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.util.SysProperty;


import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-25
 * Time: 上午11:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DatabaseUtil {
    private static String sidcheck = SysProperty.getString("logger.sidcheck");
    private static HashMap<Class,AtomicLong> uidMap = new HashMap<Class, AtomicLong>();
    public static void reset() {
        SIDCache.clear();
    }
    public static boolean  isSIDExisted(Class cls,String dn) {
        Long sid = SIDCache.getSID(cls, dn);
        return sid != null;
    }
    public static Long getSID(Class cls, String dn) {
        if (cls.equals(CPTP.class)) {
            if (!dn.contains("EMS:")) {
                try {
                    dn = MigrateUtil.simpleDN2FullDn(dn,new String[]{"EMS","ManagedElement","PTP"});
                } catch (IllegalDNStringException e) {
          //          MigrateThread.thread().getLogger().error("ERROR simple dn : " + dn, e);
                }
            }
        }
        Long sid = SIDCache.getSID(cls, dn);
        if (sid != null) return sid;
        sid = nextSID(cls);
        SIDCache.addSID(cls,dn,sid);
        if (sidcheck == null || sidcheck.equalsIgnoreCase("true"))
            MigrateThread.thread().getLogger().error("Failed to find sid : " + cls + "; dn = " + dn + " ---- " + SysUtil.getCurrentStackInfo());
        return sid;
    }
    public static Long nextSID(CdcpObject object) {
        Class cls = object.getClass();
        String dn = object.getDn();
        Long sid = SIDCache.getSID(cls, dn);
        if (sid != null) return sid;
        sid = nextSID(cls);
        SIDCache.addSID(cls,dn,sid);
        return sid;
    }
    public static Long nextSID(Class cls) {
        AtomicLong atomicLong = uidMap.get(cls);
        if (atomicLong == null) {
            synchronized (DatabaseUtil.class) {
                atomicLong = uidMap.get(cls);
                if (atomicLong == null) {
                    JPASupport jpaSupport = createDataJpasupport();
                    List list = null;
                    try {
                        list = JPAUtil.getInstance().queryQL(jpaSupport, "select max(c.sid) from " + cls.getName() + " c");
                    } catch (Exception e) {
                        MigrateThread.thread().getLogger().error(e, e);
                    } finally {
                        jpaSupport.release();
                    }

                    if (list == null || list.isEmpty() || list.get(0) == null)
                        atomicLong = new AtomicLong(0);
                    else
                        atomicLong = new AtomicLong((Long)list.get(0));
                    uidMap.put(cls,atomicLong);
                }

            }
        }

        return atomicLong.incrementAndGet();
        //return SysUtil.nextLongId();
    }

    public static JPASupport createDataJpasupport() {
        JPASupportSpringImpl entityManagerFactoryData = new JPASupportSpringImpl("entityManagerFactoryData");
        return entityManagerFactoryData;
    }

    public static void main(String[] args) {
        DatabaseUtil.getSID(CPTP.class,"EMS:WZ-U2000-1-P@ManagedElement:16713019@PTP:/rack=1/shelf=1/slot=1/domain=ptn/type=physical/port=1");
    }
}
