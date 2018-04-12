package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.nms.db.components.service.JPAUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-25
 * Time: 下午12:51
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DBTable<T> {
    private Log logger = LogFactory.getLog(getClass());
    private List data = null;
    private DBContext ctx= null;

    public DBContext getCtx() {
        return ctx;
    }

    public void setCtx(DBContext ctx) {
        this.ctx = ctx;
    }

    public static Class getCls() {
       return DBTable.getCls();
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public DBTable() {

    }
    public void queryAll() {
        try {
            data = JPAUtil.getInstance().findAllObjects(ctx.getJpaSupport(), (getCls()));
        } catch (Exception e) {
            logger.error(e, e);
        }

    }

    public T queryByDn(String dn) {
        try {
            return (T)JPAUtil.getInstance().findObjectByDn(ctx.getJpaSupport(),-1,getCls(),dn);
        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }
    public void eachRow(RowHandler processor) {
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                Object o = data.get(i);
                DBContext ctx = null;
                processor.each(o,ctx);

            }
        }
    }

    static public interface RowHandler<T> {
        public void each(T row,DBContext ctx) ;
    }






}


