package com.alcatelsbell.cdcp.web;

import com.alcatelsbell.nms.util.SysProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

/**
 * Author: Ronnie.Chen
 * Date: 2015/5/7
 * Time: 14:31
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class JettyMain {
    private Log logger = LogFactory.getLog(getClass());

    public void start() throws Exception {

        int port = SysProperty.getInt("jetty.port", 8000);
        Server server = new Server(port);
        logger.info("--------------------- [start] JettyMain port = "+port+" -------------------------" );
        String dir = "webapp";
        if (!new File(dir).exists())
            dir = ("../webapp");

        WebAppContext webAppContext = new WebAppContext();

        webAppContext.setContextPath("/");
        webAppContext.setDescriptor(dir+"/WEB-INF/web.xml");
        webAppContext.setResourceBase(dir);
        webAppContext.setConfigurationDiscovered(true);
        webAppContext.setParentLoaderPriority(true);
        webAppContext.setClassLoader(getClass().getClassLoader());
        server.setHandler(webAppContext);

        // 以下代码是关键
  //      webAppContext.setClassLoader(applicationContext.getClassLoader());

//        XmlWebApplicationContext xmlWebAppContext = new XmlWebApplicationContext();
//        xmlWebAppContext.setParent(applicationContext);
//        xmlWebAppContext.setConfigLocation("");
//        xmlWebAppContext.setServletContext(webAppContext.getServletContext());
//        xmlWebAppContext.refresh();
//
//        webAppContext.setAttribute(
//                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
//                xmlWebAppContext);

        server.start();
        logger.info("webAppContext = " + webAppContext);
        logger.info("start sucess");
        server.join();

    }

    public static void main(String[] args) throws Exception {
        String s = "123123|1231231";
        if (s.contains("|")) {
            String[] split = s.split("\\|");
            System.out.println("split = " + split);
        }
        new JettyMain().start();
    }
}
