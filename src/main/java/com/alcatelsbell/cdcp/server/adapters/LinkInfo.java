package com.alcatelsbell.cdcp.server.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2015/1/8
 * Time: 10:39
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class LinkInfo {
    public String dn;
    public String type;
    public Object obj;
    public String otherSide;
    public String thisSide;

    public LinkInfo(String dn, String type, Object obj, String otherSide, String thisSide) {
        this.dn = dn;
        this.type = type;
        this.obj = obj;
        this.otherSide = otherSide;
        this.thisSide = thisSide;
    }
}
