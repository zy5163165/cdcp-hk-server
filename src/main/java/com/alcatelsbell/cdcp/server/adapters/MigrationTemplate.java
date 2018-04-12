package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-25
 * Time: 下午1:37
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrationTemplate {
    private Log logger = LogFactory.getLog(getClass());

    private JPASupport sqliteJPASupport = null;

    protected void loadDBFile(String dbFilePath) {
        sqliteJPASupport = JPASupportFactory.createSqliteJPASupport(dbFilePath);
    }

    public void close()  {
        if (sqliteJPASupport != null) {
            sqliteJPASupport.release();
        }
    }

    protected<T extends DBTable> T loadTable(Class cls) {
        DBContext ctx = new DBContext();
        ctx.setJpaSupport(sqliteJPASupport);
        try {
            T table =  (T)cls.newInstance();
            table.setCtx(ctx);
            return table;
        } catch ( Exception e) {
            logger.error(e, e);
        }
        return null;

    }
}
