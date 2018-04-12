package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.CdcpObject;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-3-11
 * Time: 上午9:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DBDataUtil {
    private static Log logger = LogFactory.getLog(DBDataUtil.class);

    public static JPASupport createJPASupport() {
        return new JPASupportSpringImpl("entityManagerFactoryData");
    }

    public static  void executeQL(String ql) {
        logger.info("execute : "+ql);
        JPASupport jpaSupport = createJPASupport();
        try {
            jpaSupport.begin();
            JPAUtil.getInstance().executeQL(jpaSupport,ql);
            jpaSupport.end();
        } catch (Exception e) {
            try {
                jpaSupport.rollback();
            } catch (Exception e1) {
                logger.error(e1, e1);
            }
            logger.error(e, e);
        } finally {
            jpaSupport.release();
            logger.info("finish : ");
        }
    }

    public static void storeObject(BObject object) {
        JPASupport jpaSupport = createJPASupport();
        try {
            jpaSupport.begin();
            JPAUtil.getInstance().saveObject(jpaSupport,-1,object);
            jpaSupport.end();
        } catch (Exception e) {
            try {
                jpaSupport.rollback();
            } catch (Exception e1) {
                logger.error(e1, e1);
            }
            logger.error(e, e);
        } finally {
            jpaSupport.release();
        }
    }
    public static void removeObject(BObject object) {
        JPASupport jpaSupport = createJPASupport();
        try {
            jpaSupport.begin();
            JPAUtil.getInstance().removeObject(jpaSupport,-1,object);
            jpaSupport.end();
        } catch (Exception e) {
            try {
                jpaSupport.rollback();
            } catch (Exception e1) {
                logger.error(e1, e1);
            }
            logger.error(e, e);
        } finally {
            jpaSupport.release();
        }
    }

    public static void saveObjects(List  objects) {
        JPASupport jpaSupport = createJPASupport();
        try {
            jpaSupport.begin();
            for (Object object : objects) {
                JPAUtil.getInstance().saveObject(jpaSupport,-1,(BObject)object);
            }

            jpaSupport.end();
        } catch (Exception e) {
            try {
                jpaSupport.rollback();
            } catch (Exception e1) {
                logger.error(e1, e1);
            }
            logger.error(e, e);
        } finally {
            jpaSupport.release();
        }
    }

    public static List findObjects(String strSql) throws Exception {
        long t1 = System.currentTimeMillis();
        JPASupport jpaSupport = createJPASupport();
        List l = null;
        try {
            l = JPAUtil.getInstance().findObjects(jpaSupport,strSql);
        } catch (Exception e) {
            logger.error(e, e);
        } finally {
            jpaSupport.release();
        }

        long t2 = (System.currentTimeMillis() - t1)/1000;
        if (t2 > 2)
            logger.error("execute : "+strSql+" spend : "+ t2 +"s");
        return l;
    }

    public static BObject findObjectByDn(Class cls,String dn) throws Exception {
        JPASupport jpaSupport = createJPASupport();

        try {
            BObject o = JPAUtil.getInstance().findObjectByDn(jpaSupport,-1,cls,dn);
            return o;
        } catch (Exception e) {
            logger.error(e, e);
        } finally {
            jpaSupport.release();
        }
        return null;
    }
    public static Object findOneObject(String strSql) throws Exception {
        JPASupport jpaSupport = createJPASupport();
        List l = null;
        try {
            l = JPAUtil.getInstance().findObjects(jpaSupport,strSql);
            if (l == null || l.isEmpty()) {
                logger.info("Empty result:"+strSql);
            }
        } catch (Exception e) {
            logger.error(e, e);
        } finally {
            jpaSupport.release();
        }

        if (l != null && l.size() > 0)
            return l.get(0);
        return null;
    }

    public static long readSID(Class cls,String dn) throws Exception {
        BObject objectByDn = findObjectByDn(cls, dn);
        if (objectByDn instanceof CdcpObject)
            return ((CdcpObject) objectByDn).getSid();
        logger.error("[ERROR!!!!]"+cls.getName()+" not found dn = "+dn);
        return -1;
    }

}
