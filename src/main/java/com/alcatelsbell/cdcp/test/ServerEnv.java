package com.alcatelsbell.cdcp.test;


import com.alcatelsbell.nms.service.main.NMSMainService4Hippo;


import java.util.Properties;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午8:41
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ServerEnv {
    public static void init() throws Exception {
        NMSMainService4Hippo service = new NMSMainService4Hippo();
        service.setProperties(new Properties());
        service.start();
    }
}
