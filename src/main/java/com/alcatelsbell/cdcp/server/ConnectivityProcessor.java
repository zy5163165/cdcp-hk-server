package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.nbi.model.CChannel;
import com.alcatelsbell.cdcp.nbi.model.CCrossConnect;
import com.alcatelsbell.cdcp.nbi.model.CSection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.util.FileLogger;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-9-23
 * Time: 下午9:57
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ConnectivityProcessor {
    private FileLogger logger = null;
    private List<CChannel> channels;
    private List<CCrossConnect> ccs;
    private List<CSection> sections;


    public void error(String info) {
        if (logger != null)
            logger.error(info);
    }
    public void process() {

    }
}
