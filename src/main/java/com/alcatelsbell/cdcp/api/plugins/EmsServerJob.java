package com.alcatelsbell.cdcp.api.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2017/1/3
 * Time: 13:52
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface EmsServerJob {
    public void executeJob(EmsServerJobContext context) throws Exception;
}
