package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.ManagedElement;

import javax.management.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2015/4/9
 * Time: 12:54
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpServerDynamicMBean implements DynamicMBean {
    private Log logger = LogFactory.getLog(getClass());


    private File dbDir = new File("../files");
    public CdcpServerDynamicMBean() {
        if (!dbDir.exists())
            dbDir.mkdirs();
    }

    @Override
    public Object getAttribute(String attribute_name)
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        // 检查属性是否为空
        if (attribute_name == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException(
                    "Attribute name cannot be null"),
                    "Cannot invoke a getter of " + dClassName
                            + " with null attribute name");
        }

        return null;
    }


    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    }

    @Override
    public AttributeList getAttributes(String[] attributeNames) {
        if (attributeNames == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException(
                    "attributeNames[] cannot be null"),
                    "Cannot invoke a getter of " + dClassName);
        }
        AttributeList resultList = new AttributeList();
        if (attributeNames.length == 0)
            return resultList;
        for (int i = 0; i < attributeNames.length; i++) {
            try {
                Object value = getAttribute((String) attributeNames[i]);
                resultList.add(new Attribute(attributeNames[i], value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (resultList);
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        if (attributes == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException(
                    "AttributeList attributes cannot be null"),
                    "Cannot invoke a setter of " + dClassName);
        }
        AttributeList resultList = new AttributeList();
        if (attributes.isEmpty())
            return resultList;
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Attribute attr = (Attribute) i.next();
            try {
                setAttribute(attr);
                String name = attr.getName();
                Object value = getAttribute(name);
                resultList.add(new Attribute(name, value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (resultList);
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {

        final String _fileName = actionName;
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                File file = new File(dbDir,_fileName);
//                JPASupport sqliteJPASupport = JPASupportFactory.createSqliteJPASupport(file.getAbsolutePath());
//                List<ManagedElement> mes = JPAUtil.getInstance().findObjects(sqliteJPASupport, "select c from ManagedElement c", null, null, 0, 1);
//                if (mes.size() > 0) {
//                    String emsName = mes.get(0).getEmsName();
//                    AbstractDBFLoader dbfLoader = (AbstractDBFLoader)
//                            loader.getConstructor(String.class, String.class).newInstance(file.getAbsolutePath(),emsName);
//                    dbfLoader.execute();
//                } else {
//                    throw new Exception(" ManagedElement size is 0 file = "+ftpInfo.getFileName());
//                }
//            }
//        };
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return buildDynamicMBeanInfo();
    }

    private String state = "initial state";
    private int nbChanges = 0;
    private int nbResets = 0;
    private String dClassName = this.getClass().getName();
    private String dDescription = "Simple implementation of a dynamic MBean.";





    private MBeanInfo buildDynamicMBeanInfo() {
        MBeanInfo dMBeanInfo = null;
        MBeanOperationInfo[] dOperations = null;
        MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
        MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[0];
        List<MBeanOperationInfo> operationInfoList = new ArrayList<MBeanOperationInfo>();
        int idx = 0;
        File[] dbs = dbDir.listFiles();
        for (File dbFile : dbs) {


                MBeanParameterInfo[] params = new MBeanParameterInfo[]{new MBeanParameterInfo("params","java.lang.String","params")};
                MBeanOperationInfo operationInfo =  new MBeanOperationInfo(
                        dbFile.getName(),
                        dbFile.getName(),
                        params, "String", MBeanOperationInfo.ACTION);
                operationInfoList.add(operationInfo);





        }

        dOperations = new MBeanOperationInfo[operationInfoList.size()];
        for (int i = 0; i < operationInfoList.size(); i++) {
            dOperations[i] = operationInfoList.get(i);
        }

        Constructor[] constructors = this.getClass().getConstructors();
        dConstructors[0] = new MBeanConstructorInfo(
                "SimpleDynamic(): Constructs a SimpleDynamic object",
                constructors[0]);
        //    MBeanParameterInfo[] params = null;
        //    dOperations = ( MBeanOperationInfo[] )asArray(operationInfoList);
        dMBeanInfo = new MBeanInfo(dClassName, dDescription, dAttributes,
                dConstructors, dOperations, new MBeanNotificationInfo[0]);
        return dMBeanInfo;
    }

    private static Object[] asArray(List l ) {
        Object[] array = new Object[l.size()];
        for (int i = 0; i < l.size(); i++) {
            array[i] = l.get(i);
        }
        return array;
    }

    public static void main(String[] args) {
        List<String> l = new ArrayList();
        l.add("adsf");
        String[] s = ( String[] )l.toArray();
        System.out.println("s = " + s);
    }

}
