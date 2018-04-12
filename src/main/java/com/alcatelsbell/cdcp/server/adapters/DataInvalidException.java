package com.alcatelsbell.cdcp.server.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-9
 * Time: 下午8:57
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DataInvalidException extends Exception {
    public DataInvalidException(String msg) {
        super("[DataInvalidException] "+msg);
    }
}
