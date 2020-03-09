package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;

import org.asb.mule.probe.framework.entity.CTP;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;


import javax.persistence.EntityManager;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-26
 * Time: 下午1:40
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SqliteDelegation {

    private JPASupport jpaSupport = null;

    public SqliteDelegation(JPASupport jpaSupport) {
        this.jpaSupport = jpaSupport;
    }

    public void release() {
        try {
            jpaSupport.release();
            JPASupportFactory.releaseSqliteDataSource(jpaSupport);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List query(String ql) {
        try {
            return JPAUtil.getInstance().findObjects(jpaSupport,ql);
        } catch (Exception e) {
            MigrateThread.thread().getLogger().error(  e, e);
        }
        return null;
    }

    public long findObjectsCount(String hql) throws Exception {
        long result = -5677600560883171328L;

        try
        {
            List l = JPAUtil.getInstance().findObjects(jpaSupport,hql);
            if (l != null && l.size() > 0) {
                Object n = l.get(0);
                if (n instanceof Number) {
                    return ((Number)n).longValue();
                }
            }

        } catch (Exception ex) {

            throw ex;
        }
        return result;
    }
    public List queryAll(Class cls) {
//        try {
//            return JPAUtil.getInstance().findAllObjects(jpaSupport, cls);
//        } catch (Exception e) {
//            MigrateThread.thread().getLogger().error(  e, e);
//        }
//        return null;
        return queryAllByJDBC(cls);
    }

    public List queryAllByJDBC(final Class cls) {
        final List result = new ArrayList();
        try {
            EntityManager entityManager = jpaSupport.getEntityManager();
            if (entityManager instanceof HibernateEntityManager) {
                Session session = ((HibernateEntityManager) entityManager).getSession();

                session.doWork(new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        Table annotation = (Table)cls.getAnnotation(Table.class);

                        try {
                            jdbcQueryAll(connection,cls,annotation == null ? cls.getSimpleName(): annotation.name(),result);
                        } catch (Exception e) {
                            throw new SQLException(e.getMessage(),e);
                        }
                        //  logger.info("callIrmAutoMigrate sucess");
                    }
                });
            } else {
                throw new Exception("Not Hibernate Entity Manager :" + entityManager);
            }
        } catch (HibernateException e) {
            MigrateThread.thread().getLogger().error(e, e);
        } catch (Exception e) {
            MigrateThread.thread().getLogger().error(e, e);
        }
        return result;
    }


    private void jdbcQueryAll(Connection conn,Class cls,String tableName,List result) throws Exception {
        Statement stat = conn.createStatement();
        ResultSet resultSet1 = stat.executeQuery("select * from "+tableName);
        try {
            HashMap<Integer,Method>  setMethods = new HashMap<Integer, Method>();
            HashMap<Integer,Class>  types = new HashMap<Integer, Class>();


            int columnCount = resultSet1.getMetaData().getColumnCount();
            for (int i = 1; i <=columnCount ; i++) {
                // int columnType = resultSet1.getMetaData().getColumnType(i);
                String columnName = resultSet1.getMetaData().getColumnName(i);
                Method[] methods = cls.getMethods();
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

            while (resultSet1.next()) {
                Object ctp = cls.newInstance();
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
                        else  if (aClass.equals(Short.class) || aClass.equals(short.class)) {
                            value =  resultSet1.getShort(i);
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
                result.add(ctp);

            }
        } catch (Exception e) {
           throw e;
        }  finally {
            if (resultSet1 != null) {
                resultSet1.close();
            }

            if (stat != null) {
                stat.close();
            }
        }


    }

    public HashMap queryAllToMap(Class cls) {
        List<BObject> list = queryAll(cls);
        HashMap map = new HashMap();
        if (list != null) {
            for (BObject bObject : list) {
                map.put(bObject.getDn(),bObject);
            }

        }
        return map;
    }

    public List queryAllLazy(Class cls,int limitPerQuery) {
        List result = new ArrayList();
        int start = 0;
        int limit = limitPerQuery;
        while (true) {
            List objects = null;
            try {
                objects = JPAUtil.getInstance().findObjects(jpaSupport, "select c from " + cls.getName() + " c", null, null, start, limit);
            } catch (Exception e) {
                MigrateThread.thread().getLogger().error(e, e);
            }
            result.addAll(objects);
            if (objects.size() < limitPerQuery)
                return result;
            start += limit;
        }
     }

    public void queryAllLazy(Class cls,int limitPerQuery,LazyQueryHandler handler) {

        int start = 0;
        int limit = limitPerQuery;
        while (true) {
            List objects = null;
            try {
                objects = JPAUtil.getInstance().findObjects(jpaSupport, "select c from " + cls.getName() + " c", null, null, start, limit);
            } catch (Exception e) {
                MigrateThread.thread().getLogger().error(e, e);
            }
            if (objects != null && !objects.isEmpty())
                 handler.handle(objects);
            if (objects.size() < limitPerQuery)
                return ;
            start += limit;
        }
    }
     

    public Object queryOneObject(String ql) {
        List l = query(ql);
        if (l != null && l.size() > 0)
            return l.get(0);
        return null;
    }

    public List query(String ql,int from ,int limit) {
        try {
            return JPAUtil.getInstance().findObjects(jpaSupport,ql,null,null,from,limit);
        } catch (Exception e) {
            MigrateThread.thread().getLogger().error(  e, e);
        }
        return null;
    }

    public   interface LazyQueryHandler<T> {
        public void handle(List<T> result);
    }
}


