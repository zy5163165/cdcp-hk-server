package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.cdcp.nbi.model.CdcpObject;
import com.alcatelsbell.nms.common.CommonUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-10
 * Time: 下午3:51
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpDBWorker {
    private JPASupport jpaSupport = null;
    private Long emsid = null;
    private String emsDn = null;
    private Logger logger = MigrateThread.thread().getLogger();

    public Long getEmsid() {
        return emsid;
    }

    public void setEmsid(Long emsid) {
        this.emsid = emsid;
    }

//    public DataInserter() throws Exception {
//        begin();
//    }

    public CdcpDBWorker(Long emsid,String emsDn) throws Exception {
        this.emsid = emsid;
        this.emsDn = emsDn;
        begin();
    }


    public void begin() throws Exception {
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
    public void rollback() {
        jpaSupport.rollback();
    }

    public void end() throws Exception{
        if (jpaSupport != null) {
            try {
                jpaSupport.end();
            } catch (Exception e) {
                logger.error(e, e);
            }  finally {
                jpaSupport.release();
                jpaSupport = null;
            }
        }

    }

    public void insert(BObject bo) throws Exception {
        if (emsid != null && bo instanceof CdcpObject)  {
            ((CdcpObject) bo).setEmsid(emsid);
            if (emsDn != null)
                ((CdcpObject) bo).setEmsName(emsDn);
        }
        if (bo instanceof CdcpObject) {
            if (((CdcpObject) bo).getSid() == null)
                ((CdcpObject) bo).setSid(DatabaseUtil.nextSID((CdcpObject) bo));
        }
        try {
            JPAUtil.getInstance().createObject(jpaSupport,-1,bo);
        } catch (Exception e) {
            MigrateThread.thread().getLogger().error("Failed to insert object :"+ bo.getClass().getName()+"="+ CommonUtil.toString(bo));
            MigrateThread.thread().getLogger().error(e,e);
            throw  e;
        }
    }

    public void insert(List list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof BObject)
                insert((BObject) o);
        }

    }

}
