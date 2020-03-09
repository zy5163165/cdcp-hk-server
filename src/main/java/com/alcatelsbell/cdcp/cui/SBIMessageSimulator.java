package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.cdcp.nodefx.CDCPConstants;
import com.alcatelsbell.cdcp.nodefx.CdcpMessage;
import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.nms.common.JMSSupport;
import com.alcatelsbell.nms.util.jms.JMSSupportSpringImpl;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Author: Ronnie.Chen
 * Date: 2016/6/29
 * Time: 14:43
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SBIMessageSimulator {
    private Logger logger = LoggerFactory.getLogger(SBIMessageSimulator.class);

    public static void main(String[] args) throws Exception {

        String file = "msg.properties";
        if (args != null && args.length > 0) {
            file = args[0];
            if (!file.endsWith(".properties"))
                file = file+".properties";
        }
        System.out.println("file="+file);
        Properties properties = new Properties();
        properties.load(SBIMessageSimulator.class.getClassLoader().getResourceAsStream(file));
        JMSSupport jmsSupport = new JMSSupportSpringImpl(new ActiveMQConnectionFactory(properties.getProperty("activemqURL")));
        CdcpMessage message = new CdcpMessage();
        message.setType(CDCPConstants.MESSAGE_TYPE_TASK_INFO);
//        FtpInfo ftpInfo = new FtpInfo("root",
//                "bel;1145",
//                "136.224.243.143",
//                22,
//                "/opt/backup/data/res/SDH/ZTE/hlbezteotn",
//                "2016-06-29-114705-hlbezteotn-DayMigration.db");

        FtpInfo ftpInfo = new FtpInfo(properties.getProperty("username"),properties.getProperty("password"),
                properties.getProperty("host"),Integer.parseInt(properties.getProperty("port")),properties.getProperty("path"),
                properties.getProperty("filename"));
        ftpInfo.setType(properties.getProperty("type"));
        String taskSerial = properties.getProperty("taskSerial");
        message.setObject(ftpInfo);
        message.setAttribute(CDCPConstants.MESSAGE_ATTRIBUTE_TASK_SERIAL,taskSerial);
        jmsSupport.sendTopicMessage(CDCPConstants.TOPIC_EMS_SBI, message);
        System.out.println("send message = "+ftpInfo+"type="+ftpInfo.getType());
    }
}
