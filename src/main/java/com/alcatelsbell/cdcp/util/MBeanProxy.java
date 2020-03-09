package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.cdcp.nodefx.CDCPConstants;
import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.nms.interfaces.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.*;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-16
 * Time: 下午3:22
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MBeanProxy<T> {
    public T proxy = null;
    private JMXConnector jmxConnector = null;
    public String name = null;
    public static Log logger = LogFactory.getLog(MBeanProxy.class);

    public JMXConnector getJmxConnector() {
        return jmxConnector;
    }

    public void setJmxConnector(JMXConnector jmxConnector) {
        this.jmxConnector = jmxConnector;
    }

    public static MBeanProxy create(Class cls,String oname,String host,int port) throws IOException, MalformedObjectNameException {
        MBeanProxy proxy1 = new MBeanProxy();
        JMXServiceURL url = new JMXServiceURL(Constants.JMXMP_PROTOCAL,host,port);
     //   JMXConnector cs = JMXConnectorFactory.connect(url);
        JMXConnector cs = connectWithTimeout(url,10,TimeUnit.SECONDS);
   //     JMXConnectorFactory.connect()
        MBeanServerConnection msc = cs.getMBeanServerConnection();
        ObjectName objectName = new ObjectName(oname);
        proxy1.proxy = (JMX.newMBeanProxy(msc, objectName, cls));
        proxy1.setJmxConnector(cs);
        proxy1.name = cls.getName()+"-"+oname+"-"+host+"-"+port;
        proxy1.logger.info("proxy created : "+proxy1.name);
        return proxy1;
    }


    static JMXConnector connectWithTimeout(JMXServiceURL url, long timeout, TimeUnit unit)  {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final JMXServiceURL _url = url;
        Future<JMXConnector> future = executor.submit(new Callable<JMXConnector>() {
            public JMXConnector call() throws IOException {
                return JMXConnectorFactory.connect(_url);
            }
        });
        try {
            return future.get(timeout, unit);
        } catch ( Exception e) {
            logger.error(e,e);
        }
        return null;
    }

    /**
     * 长连接，调用完后需要释放
     * @throws IOException
     */
    public void close() throws IOException {
        if (jmxConnector != null) {
            jmxConnector.close();
            logger.info("proxy closed : "+name);
        }
    }

}
