package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.cdcp.nbi.model.CCrossConnect;
import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.ext.sqlite.SQLiteDialect;
import com.alcatelsbell.nms.valueobject.alarm.Alarminformation;
import org.asb.mule.probe.framework.entity.*;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.SessionImpl;

import javax.persistence.EntityManager;
import javax.persistence.Table;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-18
 * Time: 下午4:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestJPA2 {
    public static void run() throws Exception {

//        long t1 = System.currentTimeMillis();
//        jpaSupport.begin();
//        for (int i = 0; i < 10; i++) {
//            ManagedElement me = new ManagedElement();
//            me.setDn(SysUtil.nextDN());
//            me.setNativeEMSName("name" + i);
//
//            JPAUtil.getInstance().createObject(jpaSupport,-1,me);
//
//        }
//        jpaSupport.end();
//        EntityManager entityManager = jpaSupport.getEntityManager();
////        SessionImpl connection = (SessionImpl)entityManager.unwrap(SessionImplementor.class);
////        connection.close();
////   //     connection.connection().close();
////
////        boolean closed = connection.isClosed();
////        System.out.println("closed = " + closed);
//        //connection.close();
//        jpaSupport.release();

    }
    public static void main2(String[] args)  throws Exception {
        Class cls = CTP.class;
        Table annotation = (Table) cls.getAnnotation(Table.class);
        if (args == null || args.length == 0) {
            args = new String[]{"d:\\cdcpdb\\FH_2014-10-15-021613-SHX-OTNM2000-1-OTN-DayMigration.db"};
        }
        long t1 = System.currentTimeMillis();
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:"+args[0]);
        Statement stat = conn.createStatement();
        ResultSet resultSet1 = stat.executeQuery("select * from CTP");
        HashMap<Integer,Method>  setMethods = new HashMap<Integer, Method>();
        HashMap<Integer,Class>  types = new HashMap<Integer, Class>();


        int columnCount = resultSet1.getMetaData().getColumnCount();
        for (int i = 1; i <=columnCount ; i++) {
            // int columnType = resultSet1.getMetaData().getColumnType(i);
            String columnName = resultSet1.getMetaData().getColumnName(i);
            Method[] methods = CTP.class.getMethods();
            for (Method method : methods) {
                if (method.getName().toLowerCase().equals("set"+columnName.toLowerCase()) && method.getParameterTypes() != null && method.getParameterTypes().length == 1) {
                    setMethods.put(i,method);
                    types.put(i,method.getParameterTypes()[0]);
                }


            }

        }
//        SQLiteDialect sqLiteDialect = new SQLiteDialect();
//        sqLiteDialect.getCastTypeName(2);
//        Method[] methods = CTP.class.getMethods();
//        for (Method method : methods) {
//            if (method.getName().startsWith("set") && method.getParameterTypes() != null && method.getParameterTypes().length == 1) {
//                Class<?>[] parameterTypes = method.getParameterTypes();
//                Class<?> type = parameterTypes[0];
//                if (type.equals(String.class)) {
//
//                }
//            }
//        }
        List list = new ArrayList();
        while (resultSet1.next()) {
            CTP ctp = new CTP();
            for (int i = 1; i <= columnCount; i++) {
                Class aClass = types.get(i);
                Object value = null;

                if (aClass != null) {
                    if (aClass.equals(Integer.class) || aClass.equals(int.class)) {
                        value = resultSet1.getInt(i);
                    } else  if (aClass.equals(Long.class) || aClass.equals(long.class)) {
                        value = resultSet1.getLong(i);
                    }  else  if (aClass.equals(String.class) ) {
                        value = resultSet1.getString(i);
                    } else  if (aClass.equals(Float.class) || aClass.equals(float.class)) {
                        value = resultSet1.getFloat(i);
                    } else  if (aClass.equals(Double.class) || aClass.equals(double.class)) {
                        value = resultSet1.getDouble(i);
                    } else  if (aClass.equals(java.util.Date.class)) {
                        value = new java.util.Date(resultSet1.getDate(i).getTime());
                    } else  if (aClass.equals(java.sql.Date.class)) {
                        value =  resultSet1.getDate(i);
                    } else  if (aClass.equals(Byte.class) || aClass.equals(byte.class)) {
                        value =  resultSet1.getByte(i);
                    }else  if (aClass.equals(Boolean.class) || aClass.equals(boolean.class)) {
                        value =  resultSet1.getBoolean(i);
                    }


                    else {
                        throw new Exception("unknow type : "+aClass);
                    }

                }
                if (value != null) {
                    Method method = setMethods.get(i);
                    method.invoke(ctp,value);
                }


            }
            list.add(ctp);

        }
        System.out.println("list = " + list.size());
        long t2 = System.currentTimeMillis();
        System.out.println("t = " + (t2-t1)/1000 +"s");
    }
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("[0-9]{4}[0-9]{1,2}[0-3][0-9][-][0-9]{4}");
        Matcher matcher = pattern.matcher("ENB-PM-V2.6.0-EpRpDynS1uEnb-20151229-0345.xml.gz");

        String dateStr = null;
        if(matcher.find()){
            dateStr = matcher.group(0);
        }
        System.out.println("dateStr = " + dateStr);

    }
}
