package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.cdcp.api.EmsMgmtClient;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.server.message.CdcpServerMessage;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.jms.JMSSupportSpringImpl;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-16
 * Time: 下午2:29
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrateDBFile {
    private Log logger = LogFactory.getLog(getClass());


    public static void main(String[] args) {
        String activeMQUrl = SysProperty.getString("env.activemq_url");
        if (activeMQUrl == null)
            System.err.println("Missing property $env.activemq_url in system.properties, you may not read the migrate " +
                    "result");

        String fileUrl = "D://2018-01-09-111621-Huawei.U2000-DayMigration.db";
        if (args == null || args.length != 1) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (fileUrl == null) {
                System.out.println("Please input DB File Path : ");
                String s = null;
                try {
                    s = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (new File(s).exists())
                    fileUrl = s;
            }
        }
        else
            fileUrl = args[0];


        JMSSupportSpringImpl jmsSupport = new JMSSupportSpringImpl(new ActiveMQConnectionFactory(activeMQUrl));

        try {
            final String serial = EmsMgmtClient.getInstance().migrate(fileUrl);
            System.out.println("Task serial = "+serial);
            jmsSupport.addTopicSubscriber(Constants.TOPIC_SERVER_MIGRATE_LOG,new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if (message instanceof ObjectMessage) {
                        try {
                            CdcpServerMessage message1 = (CdcpServerMessage) ((ObjectMessage) message).getObject();
                            Object txt = message1.getAttribute(Constants.SERVER_MESSAGE_ATTRIBUTE_MIGRATE_TXT);
                            Object emsname = message1.getAttribute(Constants.SERVER_MESSAGE_ATTRIBUTE_EMS_DN);
                            Object taskSerial = message1.getAttribute(Constants.SERVER_MESSAGE_ATTRIBUTE_MIGRATE_TASK_SERIAL);
                            Object percentage = message1.getAttribute(Constants.SERVER_MESSAGE_ATTRIBUTE_MIGRATE_PERCENTAGE);

                            if (taskSerial != null && taskSerial.equals(serial)) {
                                 System.out.println("["+emsname+"] ["+percentage+"%]"+ txt);
                                if ((Integer)percentage == 100)
                                    return;
                             }
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });

        } catch (RemoteException e) {
            e.printStackTrace();
        }




    }
}
