package com.alcatelsbell.cdcp.web.httpd;

import com.alcatelsbell.nms.util.ThreadDumper;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Properties;

/**
 * Author: Ronnie.Chen
 * Date: 2016/12/27
 * Time: 11:43
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpAjaxHandler implements HttpRequestHandler {
    private Logger logger = LoggerFactory.getLogger(CdcpAjaxHandler.class);

    @Override
    public NanoHTTPd.Response handle(String method, String uri, Properties header, Properties parms, Properties files) {
        if (method.equals("GET") || method.equals("POST")) {
            if (uri.equalsIgnoreCase("/hello"))
                return new  NanoHTTPd.Response(NanoHTTPd.HTTP_OK, NanoHTTPd.MIME_HTML,"OK");

            if (uri.equalsIgnoreCase("/dump"))
                return  new  NanoHTTPd.Response(NanoHTTPd.HTTP_OK, NanoHTTPd.MIME_HTML, ThreadDumper.dump());


            if (uri.equalsIgnoreCase("/dump")) {
                return  new  NanoHTTPd.Response(NanoHTTPd.HTTP_OK, NanoHTTPd.MIME_JSON, ThreadDumper.dump());
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        return  new  NanoHTTPd.Response(NanoHTTPd.HTTP_OK, NanoHTTPd.MIME_HTML, "Unknown method : "+method);

    }
}
