package com.alcatelsbell.cdcp.nbi.irm;

import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.test.JdbcUtil;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Ronnie.Chen
 * Date: 2015/1/16
 * Time: 15:56
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class IrmCollectReportUtil {
    private static Log log = LogFactory.getLog(IrmCollectReportUtil.class);
    private static BasicDataSource dataSource = null;
    public static Connection  getConnection() throws SQLException {
        if (dataSource == null) {
            dataSource = new BasicDataSource();
            dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
            dataSource.setUrl(SysProperty.getString("cdcp.irm.db.url"));
            dataSource.setUsername(SysProperty.getString("cdcp.irm.db.username"));
            dataSource.setPassword(SysProperty.getString("cdcp.irm.db.password"));
            dataSource.setInitialSize(5);
            dataSource.setMinIdle(5);
            dataSource.setMaxActive(5);
            dataSource.setValidationQuery("select count(1) from dual");
        }

        return dataSource.getConnection();
    }



    static class IdPool {
        public IdPool(long startId) {
            this.startId = startId;
            this.maxId = startId + 10000;
            this.latestUsedId = -1;
        }

        public synchronized long nextId() {
            if (latestUsedId == -1) {
                latestUsedId = startId;
                return latestUsedId;
            }
            if (latestUsedId < maxId)
                return ++latestUsedId;
            return -1;
        }


        long startId;
        long maxId;
        long latestUsedId;
    }
    private static Hashtable<String,IdPool> idPoolMap = new Hashtable<String, IdPool>();
    private static ReentrantLock lock = new ReentrantLock();
    private static IdPool initPool(Connection connection,String tableName) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("select max(id) from " + tableName);
        resultSet.next();
        long maxId = resultSet.getLong(1);
        resultSet.close();
        return new IdPool(maxId + 1);
    }
    public static long getId(Connection connection,String tableName) throws SQLException {
        IdPool idPool = idPoolMap.get(tableName);
        if (idPool == null) {
            lock.lock();

            try {
                if (idPoolMap.get(tableName) == null) {
                    idPool = initPool(connection,tableName);
                    idPoolMap.put(tableName, idPool);
                }
            } catch (SQLException e) {
                throw e;
            } catch (Throwable e) {
                log.error(e,e);
            }

            finally {
                lock.unlock();
            }
        }

        long nextId = idPool.nextId();
        if (nextId < 0) {
            idPoolMap.remove(tableName);
            return getId(connection, tableName);
        }

        return nextId;
    }


   
   public static void collectionObjectDefferencesList(Connection connection,TJ_Differenceslist tjf)
            throws Exception {
          tjf.setId(getId( connection,"TJ_DIFFERENCESLIST"));

          JdbcUtil.insertObject( connection,tjf,"TJ_DIFFERENCESLIST");
    }


    public static void emsCollectionResult( TJ_INTERFACE_EMSRESULTS ar) throws Exception {
        // TODO Auto-generated method stub

        Connection connection = null;
        try {
            connection = getConnection();
            ar.setId(getId( connection,"TJ_INTERFACE_EMSRESULTS"));
            JdbcUtil.insertObject( connection,ar,"TJ_INTERFACE_EMSRESULTS");
            connection.commit();
        } finally {
            connection.close();
        }

    }

    
    public static void  insertOtnData(TJ_INTERFACE_OTN otn) throws Exception {
        Connection connection = null;
        try {
            connection = getConnection();
        // TODO Auto-generated method stub
      //  otn.setId(SysUtil.nextLongId() % 1000000);
        otn.setId(getId( connection,"TJ_INTERFACE_OTN"));

        JdbcUtil.insertObject( connection,otn,"TJ_INTERFACE_OTN");
            connection.commit();
        } finally {
            connection.close();
        }

    }


    public static void insertPtnData( TJ_INTERFACE_PTN ptn) throws Exception {
        Connection connection = null;
        try {
            connection = getConnection();
            ptn.setId(getId( connection,"TJ_INTERFACE_PTN"));
            JdbcUtil.insertObject( connection,ptn,"TJ_INTERFACE_PTN");
            connection.commit();
        } finally {
            connection.close();
        }

    }


    public static void insertSdhData( TJ_INTERFACE_SDH sdh) throws Exception {
        Connection connection = null;
        try {
            connection = getConnection();
            sdh.setId(getId( connection,"TJ_INTERFACE_SDH"));
            JdbcUtil.insertObject( connection,sdh,"TJ_INTERFACE_SDH");
            connection.commit();
        } finally {
            connection.close();
        }
    }


    public static void main(String[] args) throws Exception {
        TJ_INTERFACE_EMSRESULTS tj_interface_emsresults = new TJ_INTERFACE_EMSRESULTS("asdf", new Date(), 1,
                "", 1, "", 0);
        emsCollectionResult(tj_interface_emsresults);


//        long t1 = System.currentTimeMillis();
//        List<Ems> emses = JpaClient.getInstance().findAllObjects(Ems.class);


//        for (Ems ems : emses) {
//            Integer result = 1;
//            String info = null;
//
//
//            TJ_INTERFACE_EMSRESULTS tj_interface_emsresults = new TJ_INTERFACE_EMSRESULTS(ems.getDn(), ems.getSynEndTime(), ems.getProtocalType(),
//                    ems.getVendordn(), result, info, 0);
//            IrmCollectReportUtil.emsCollectionResult(
//                    tj_interface_emsresults);
//            System.out.println("insert ems="+ems.getDn());
//        }
//        Connection connection = getConnection();
//        for (int i = 0; i < 10000; i++) {
//            TJ_INTERFACE_OTN otn =  new TJ_INTERFACE_OTN("emsname",i,i,1,1,1,1,1,11,new Date());
//         //   IrmCollectReportUtil.insertOtnData(connection,otn);
//
//        }
//        connection.commit();
//        long t2 = System.currentTimeMillis() - t1;
//        System.out.println("t2 = " + t2+"ms");

    }

}
