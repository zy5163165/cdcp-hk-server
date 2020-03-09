package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.cdcp.nbi.model.CCTP;
import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.cdcp.nbi.model.CdcpObject;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.nms.common.CommonUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-26
 * Time: 下午4:03
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DataInserter {
    private JPASupport jpaSupport = null;
    private Long emsid = null;
    private Logger logger = MigrateThread.thread().getLogger();

    public Long getEmsid() {
        return emsid;
    }

    public void setEmsid(Long emsid) {
        this.emsid = emsid;
    }

    private Thread owner = null;

//    public DataInserter() throws Exception {
//        begin();
//    }

    public DataInserter(Long emsid) throws Exception {
        MigrateThread.thread().addDI(this);
        this.emsid = emsid;
        begin();
    }

    public void begin() throws Exception {
       // CdcpServerUtil.acquireDBLock();
        if (jpaSupport != null) {
            try {
                jpaSupport.end();
            } catch (Exception e) {
                LogUtil.error(getClass(), e, e);
            } finally {
                jpaSupport.release();
            }

        }
        jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
        jpaSupport.begin();
    }

    public boolean end() throws Exception{

        try {
            if (jpaSupport != null) {
                try {
                    jpaSupport.end();
                } catch (Exception e) {
                    logger.error(e, e);
                    jpaSupport.rollback();

                }  finally {
                    jpaSupport.release();
                    jpaSupport = null;
                }
                return true;
            }
        } catch (Exception e) {
            logger.error(e, e);
        } finally {
      //      CdcpServerUtil.releaseDBLock();
        }

        return false;

    }
    public void updateByDn(BObject bo) throws Exception {
        JPAUtil.getInstance().storeObjectByDn(jpaSupport,-1,bo);
    }
    public void updateByDn(List bo) throws Exception {
        for (Object o : bo) {
            JPAUtil.getInstance().storeObjectByDn(jpaSupport,-1,(BObject)o);
        }

    }
    HashSet<String> dns = new HashSet<String>();
    public void insert1(BObject bo) throws Exception {
        String dn = bo.getDn();
        if (dns.contains(dn)) {
            System.err.println("duplicate dn : "+dn);
        }
        dns.add(dn);
    }

    public void insertold(BObject bo) throws Exception {
        if (emsid != null && bo instanceof CdcpObject)
            ((CdcpObject) bo).setEmsid(emsid);
        if (bo instanceof CdcpObject) {
            if (((CdcpObject) bo).getSid() == null)
                ((CdcpObject) bo).setSid(DatabaseUtil.nextSID((CdcpObject) bo));
        }
        try {
            JPAUtil.getInstance().createObject(jpaSupport,-1,bo);
        } catch (Exception e) {
            MigrateThread.thread().getLogger().error("Failed to insert object :"+ bo.getClass().getName()+"="+CommonUtil.toString(bo));
            MigrateThread.thread().getLogger().error(e,e);
            throw  e;
        }
    }

    private int count = 0;
    public void insert(BObject bo) throws Exception {
        if (bo == null) return;
        if (emsid != null && bo instanceof CdcpObject)
            ((CdcpObject) bo).setEmsid(emsid);
        if (bo instanceof CdcpObject) {
            if (((CdcpObject) bo).getSid() == null)
                ((CdcpObject) bo).setSid(DatabaseUtil.nextSID((CdcpObject) bo));
        }
        try {
            JPAUtil.getInstance().createObject(jpaSupport,-1,bo);

            if (++count % 100000 == 0) {
                jpaSupport.end();
                jpaSupport.begin();
            }
        } catch (Exception e) {
            MigrateThread.thread().getLogger().error("Failed to insert object :"+ bo.getClass().getName()+"="+CommonUtil.toString(bo));
            MigrateThread.thread().getLogger().error(e,e);
            throw  e;
        } finally {

        }
    }

    public void insert(List list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof BObject)
                insert((BObject) o);
        }

    }

    public void insertWithDupCheck( List list) throws Exception {
        removeDuplicateDN(list);
        insert(list);
    }

    protected void removeDuplicateDN(List bos) {
        int count = 0;
        HashMap map = new HashMap();
        String name = null;
        for (int i = 0; i < bos.size(); i++) {
            BObject bObject = (BObject) bos.get(i);
            name = bObject.getClass().getName();
            if (map.get(bObject.getDn()) != null)
                count++;
            map.put(bObject.getDn(), bObject);
        }
        bos.clear();
        bos.addAll(map.values());
//        if (count > 0)
//        getLogger().error("DuplicateDN "+name+" count = " + count);
    }

    public void jdbcInsert() {

    }

    public static void main(String[] args) throws Exception {
        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        long t1 = System.currentTimeMillis();
        DataInserter di = new DataInserter(100l);
        for (int i = 0; i < 100000; i++) {
            CCTP cctp = new CCTP();
            cctp.setEmsName("123");
            cctp.setDn(i+":dn");
            di.insert(cctp);
        }
        di.end();
        long t = System.currentTimeMillis() - t1;
        System.out.println("t = " + t/1000+"s");


    }
}
