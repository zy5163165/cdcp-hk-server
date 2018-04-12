package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-25
 * Time: 上午10:25
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DBContext {
    private JPASupport sd = null;

    public JPASupport getJpaSupport() {
        return sd;
    }

    public void setJpaSupport(JPASupport sd) {
        this.sd = sd;
    }

    private Log logger = LogFactory.getLog(getClass());
}
