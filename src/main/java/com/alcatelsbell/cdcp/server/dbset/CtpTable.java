package com.alcatelsbell.cdcp.server.dbset;

import com.alcatelsbell.cdcp.server.adapters.DBTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.CrossConnect;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-25
 * Time: 下午6:54
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CtpTable extends DBTable {
    private Log logger = LogFactory.getLog(getClass());

    public static Class getCls() {
        return CTP.class;
    }
}
