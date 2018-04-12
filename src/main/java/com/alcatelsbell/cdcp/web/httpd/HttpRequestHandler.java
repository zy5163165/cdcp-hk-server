package com.alcatelsbell.cdcp.web.httpd;

import java.util.Properties;

/**
 * Author: Ronnie.Chen
 * Date: 2016/5/19
 * Time: 8:53
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface HttpRequestHandler {
    public NanoHTTPd.Response handle(String method, String uri, Properties header, Properties parms, Properties files) ;
}
