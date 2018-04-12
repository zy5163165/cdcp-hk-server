package com.alcatelsbell.cdcp.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;
import java.io.*;
import java.util.Properties;

/**
 * Author: Ronnie.Chen
 * Date: 14-1-27
 * Time: 下午1:34
 * rongrong.chen@alcatel-sbell.com.cn
 */


@WebService(targetNamespace = "http://ws.apache.org/axis2")
@SOAPBinding(style = Style.RPC)
public class WebServiceProvider {
    private Log logger = LogFactory.getLog(getClass());
    /**
     *
     * @return
     *     returns EsopSync
     */
    @WebMethod
    public String getName(String name) {
        return name+":newname";
    }

    public static void main(String[] args) {
      //  find("javax.xml.ws.spi.Provider","com.sun.xml.internal.ws.spi.ProviderImpl");
        Endpoint endpoint_Smart2NM11 = Endpoint.publish("http://localhost:8081/hello", new WebServiceProvider());
        System.out.println("OK");
    }

    static Object find(String factoryId, String fallbackClassName)
    {
        ClassLoader classLoader;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Exception x) {
            throw new WebServiceException(x.toString(), x);
        }

        String serviceId = "META-INF/services/" + factoryId;
        // try to find services in CLASSPATH
        try {
            InputStream is=null;
            if (classLoader == null) {
                is=ClassLoader.getSystemResourceAsStream(serviceId);
            } else {
                is=classLoader.getResourceAsStream(serviceId);
            }

            if( is!=null ) {
                BufferedReader rd =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String factoryClassName = rd.readLine();
                rd.close();

                if (factoryClassName != null &&
                        ! "".equals(factoryClassName)) {
                    return newInstance(factoryClassName, classLoader);
                }
            }
        } catch( Exception ex ) {
        }


        // try to read from $java.home/lib/jaxws.properties
        try {
            String javah=System.getProperty( "java.home" );
            String configFile = javah + File.separator +
                    "lib" + File.separator + "jaxws.properties";
            File f=new File( configFile );
            if( f.exists()) {
                Properties props=new Properties();
                props.load( new FileInputStream(f));
                String factoryClassName = props.getProperty(factoryId);
                return newInstance(factoryClassName, classLoader);
            }
        } catch(Exception ex ) {
        }


        // Use the system property
        try {
            String systemProp =
                    System.getProperty( factoryId );
            if( systemProp!=null) {
                return newInstance(systemProp, classLoader);
            }
        } catch (SecurityException se) {
        }

        if (fallbackClassName == null) {
            throw new WebServiceException(
                    "Provider for " + factoryId + " cannot be found", null);
        }

        return newInstance(fallbackClassName, classLoader);
    }

    private static Object newInstance(String className,
                                      ClassLoader classLoader)
    {
        try {
            Class spiClass = Class.forName(className);
            return spiClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new WebServiceException(
                    "Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new WebServiceException(
                    "Provider " + className + " could not be instantiated: " + x,
                    x);
        }
    }

}
