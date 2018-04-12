package com.alcatelsbell.cdcp.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * Author: Ronnie.Chen
 * Date: 2016/11/25
 * Time: 11:12
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class RequestContext {
    private Logger logger = LoggerFactory.getLogger(RequestContext.class);
    private HttpServletRequest request = null;
    private HashMap dataMap = new HashMap();

    public RequestContext(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HashMap getDataMap() {
        return dataMap;
    }

    public void setDataMap(HashMap dataMap) {
        this.dataMap = dataMap;
    }
}
