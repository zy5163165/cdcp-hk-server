package com.alcatelsbell.cdcp.api.plugins;

import com.alcatelsbell.cdcp.nbi.model.CdcpObject;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.nms.db.components.service.JPAContext;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2017/1/3
 * Time: 13:54
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class EmsServerJobContext {
    private Logger logger = LoggerFactory.getLogger(EmsServerJobContext.class);
    private Ems ems = null;

    public Ems getEms() {
        return ems;
    }

    public void setEms(Ems ems) {
        this.ems = ems;
    }

    public void insert(CdcpObject bObject) throws Exception {
        long sid = DatabaseUtil.getSID(bObject.getClass(),bObject.getDn());
        if (bObject instanceof CdcpObject)
            ((CdcpObject) bObject).setSid(sid);
        JpaServerUtil.getInstance().saveObject(-1,bObject);
    }

    public void udpateById(CdcpObject bObject) throws Exception {
        JpaServerUtil.getInstance().saveObject(-1,bObject);
    }
    public void udpateByDn(CdcpObject bObject) throws Exception {
        JpaServerUtil.getInstance().storeObjectByDn(-1,bObject);
    }

    public void createLog(String operation,String content) {
        CdcpServerUtil.createEmsLog(ems.getDn(),operation,content);
    }

    public void insert(List<CdcpObject> bObjects) throws Exception {
        JPAContext context = JPAContext.prepareContext(-1);
        try {
            for (BObject bObject : bObjects) {
                long sid = DatabaseUtil.getSID(bObject.getClass(),bObject.getDn());
                if (bObject instanceof CdcpObject)
                    ((CdcpObject) bObject).setSid(sid);
                JPAUtil.getInstance().saveObject(context,-1,bObject);
            }
            context.end();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            context.rollback();
            throw e;
        } finally {
            context.release();
        }
    }

    public void executeQl(String ql,Map map) throws Exception {
        JpaServerUtil.getInstance().executeDeleteSQL(ql,map);
    }

    public void success() {

    }

    public void fail(Exception exception) {

    }
}
