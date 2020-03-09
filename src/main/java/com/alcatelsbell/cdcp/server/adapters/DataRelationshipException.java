package com.alcatelsbell.cdcp.server.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-9
 * Time: 下午8:51
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DataRelationshipException extends Exception {
    public DataRelationshipException(String msg) {
        super("[DataRelationshipException] "+msg);
    }
}
