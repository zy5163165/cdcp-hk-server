package com.alcatelsbell.cdcp.server.services;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-11
 * Time: 上午9:21
 * rongrong.chen@alcatel-sbell.com.cn
 */

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.hippo.framework.service.DefaultServiceImpl;
import com.alcatelsbell.nms.common.SysConst;
import com.alcatelsbell.nms.db.api.JpaRemoteIFC;
import com.alcatelsbell.nms.db.components.service.*;
import com.alcatelsbell.nms.util.JVMRegistry;
import com.alcatelsbell.nms.valueobject.BObject;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * User: Ronnie
 * Date: 12-7-24
 * Time: 下午1:57
 */

import com.alcatelsbell.hippo.framework.service.DefaultServiceImpl;
import com.alcatelsbell.nms.common.SysConst;
import com.alcatelsbell.nms.db.api.JpaRemoteIFC;
import com.alcatelsbell.nms.util.JVMRegistry;
import com.alcatelsbell.nms.valueobject.BObject;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class CdcpDataJPARemoteImpl extends DefaultServiceImpl implements JpaRemoteIFC {
    private String beanId = "entityManagerFactoryData";
    public CdcpDataJPARemoteImpl() throws RemoteException
    {
        JVMRegistry.getInstance().registerObject(getClass().getName(),this);
    }
    public java.lang.String getJndiNamePrefix(){
        return Constants.SERVICE_NAME_CDCP_DATAJPA;
    }
    public BObject saveObject(long sessionKey, BObject obj) throws RemoteException, Exception {
        BObject result = null;

        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            result = JPAUtil.getInstance().saveObject(context, sessionKey, obj);
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }
        return result;
    }


    String[] sqls = new String[] {"create index idx_csubnetwork2ems on c_subnetwork(emsname)",

            "create index idx_csubnetworkdeviceshelf2ems on c_subnetworkdevice(emsname)",
            "create unique index uk_csubnetworkdevice on c_subnetworkdevice(devicedn,subnetworkdn)",

            "create index idx_cdevice2ems on c_device(emsname)",

            "create index idx_cshelf2parentdn on c_shelf(parentdn)",
            "create index idx_cslot2shelf on c_slot(shelfdn)",
            "create index idx_cslot2parent on c_slot(parentdn)",
            "create index idx_crack2parent on c_rack(parentdn)",

            "create index idx_cslot2parentslot on c_slot(parentslotdn)",

            "create index idx_cequipment2ems on c_equipment(emsname)",

            "create index idx_cptp2ems on c_ptp(emsname)",
            "create index idx_cptp2device on c_ptp(devicedn)",

            "create index idx_cctp2ems on c_ctp(emsname)",

       //     "create index idx_cftpptp2ptpdn on c_ftp_ptp(ptpdn)",

            "create unique index uk_cftpptp on c_ftp_ptp(ptpdn,ftpdn)",
            "create index idx_cftpptp2ems on c_ftp_ptp(emsname)",
            
            "create unique index uk_cmpctp on c_mp_ctp(ptpdn,ctpdn)",
            "create index idx_cmpctp2ems on c_mp_ctp(emsname)",

            "create index idx_csection2ems on c_section(emsname)",

            "create index idx_ctunnel2ems on c_tunnel(emsname)",
            "create index idx_croute2ems on c_route(emsname)",
            "create index idx_croute2ems on C_IPRoute(emsname)",

            "create index idx_ctunnelsection2ems on c_tunnel_section(emsname)",
       //     --需要看为什么unique index创建失败，不合理
    "create unique index idx_ctunnelsection on c_tunnel_section(sectiondn,tunneldn)",

            "create index idx_cprotectiongroup2ems on c_protectiongroup(emsname)",
            "create index idx_cprotectiongrouptunnel2ems on c_protectiongroup_tunnel(emsname)",
            "create unique index uk_cprotectiongrouptunnel on c_protectiongroup_tunnel(protectgroupdn,tunneldn)",

            "create index idx_cpwe32ems on c_pwe3(emsname)",
            "create index idx_cpw2ems on c_pw(emsname)",
            "create index idx_cpwe3pw2ems on c_pwe3_pw(emsname)",
            "create index idx_cpwtunnel2ems on c_pw_tunnel(emsname)",
            "create unique index uk_cpwe3pw on c_pwe3_pw(pwdn,pwe3dn)",
            "create unique index uk_cpwtunnel on c_pw_tunnel(tunneldn,pwdn)",
            
            "create index  idx_cslot2cardn on  c_slot(carddn)",
            "create index  idx_cequipment2parent on  c_equipment(parentdn)",

            "alter table C_TUNNEL_SECTION modify (dn varchar(512))",
            "alter table C_PWE3_TUNNEL modify (dn varchar(512))" ,
            "alter table C_PWE3 modify (zptp varchar(2048))" ,
            "alter table C_CHANNEL modify (dn varchar(512))",
            "alter table C_PATH_CHANNEL modify (channelDn varchar(512))" ,
            "alter table C_ROUTE_CHANNEL modify (channelDn varchar(512))"



    };
    public void start() {
        for (int i = 0; i < sqls.length; i++) {
            String sql = sqls[i];
            try {
                executeNativeSql(sql);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        for (int i = 0; i < Constants.PRE_LOAD_SQLS.length; i++) {
            String sql = Constants.PRE_LOAD_SQLS[i];
            try {
                executeNativeSql(sql);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    public BObject storeObjectByDn(long sessionKey, BObject obj) throws RemoteException, Exception {
        BObject result = null;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            result = JPAUtil.getInstance().storeObjectByDn(context, sessionKey, obj);
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }
        return result;
    }

    public BObject storeObjectByKeys(long sessionKey, BObject _obj, String keys) throws RemoteException, Exception
    {
        BObject result = null;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            result = JPAUtil.getInstance().storeObjectByKeys(context, sessionKey, _obj, keys);
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }
        return result;
    }

    public List findAllObjects(Class cls) throws Exception
    {
        List result = null;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            result = DBUtil.getInstance().queryAllObjects(context, cls);

        } catch (Exception ex) {

            throw ex;
        } finally {
            context.release();
        }
        return result;
    }

    //    public long findObjectsNumByFilter(Class cls, Object filter) throws Exception {
    //      long result = -5677600560883171328L;
    //      JPAContext context = JPAContext.prepareReadOnlyContext();
    //      try
    //      {
    //        result = JPAUtil.getInstance().findObjectsNumByFilter(context, cls, filter);
    //        context.end();
    //      } catch (Exception ex) {
    //        context.rollback();
    //
    //        throw ex;
    //      } finally {
    //        context.release();
    //      }
    //      return result;
    //    }

    //    public List findObjectsByFilter(Class cls, Object filter, Integer start, Integer limit) throws Exception
    //    {
    //      List result = null;
    //      JPAContext context = JPAContext.prepareReadOnlyContext();
    //      try
    //      {
    //        result = JPAUtil.getInstance().findObjectsByFilter(context, cls, filter, start, limit);
    //        context.end();
    //      } catch (Exception ex) {
    //        context.rollback();
    //
    //        throw ex;
    //      } finally {
    //        context.release();
    //      }
    //      return result;
    //    }

    //    public List findObjectsByFilter(String strSql, Class cls, Object filter, Integer start, Integer limit) throws Exception
    //    {
    //      List result = null;
    //      JPAContext context = JPAContext.prepareReadOnlyContext();
    //      try
    //      {
    //        result = JPAUtil.getInstance().findObjectsByFilter(context, strSql, cls, filter, start, limit);
    //        context.end();
    //      } catch (Exception ex) {
    //        context.rollback();
    //
    //        throw ex;
    //      } finally {
    //        context.release();
    //      }
    //      return result;
    //    }

    public List<Object> findObjectsByMapTable(Class cls, BObject bobj, Class mapClass, String[] mapkeys) throws Exception
    {
        List result = null;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            result = JPAUtil.getInstance().findObjectsByMapTable(context, cls, bobj, mapClass, mapkeys);
        }
        catch (Exception ex) {

            throw ex;
        } finally {
            context.release();
        }
        return result;
    }

    public List findObjects(String strSql, String strPrefix, Map mapValue, Integer start, Integer limit) throws Exception
    {
   //     Thread.sleep(10000l);
        long t1 = System.currentTimeMillis();
        List result = null;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            result = JPAUtil.getInstance().findObjects(context, strSql, strPrefix, mapValue, start, limit);
        }
        catch (Exception ex) {

            throw ex;
        } finally {
            context.release();
        }
        long t2 = System.currentTimeMillis();
        logger.info("query:"+strSql+" spend "+(t2-t1)+"ms");
        return result;
    }

    public long findObjectsCount(Class cls, Map mapValue) throws Exception {
        long result = -5677600560883171328L;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            result = JPAUtil.getInstance().findObjectsCount(context, cls, mapValue);

        } catch (Exception ex) {

            throw ex;
        } finally {
            context.release();
        }
        return result;
    }

    @Override
    public long findObjectsCount(String hql) throws Exception {
        long result = -5677600560883171328L;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            List l = JPAUtil.getInstance().findObjects(context,hql);
            if (l != null && l.size() > 0) {
                Object n = l.get(0);
                if (n instanceof Number) {
                    return ((Number)n).longValue();
                }
            }

        } catch (Exception ex) {

            throw ex;
        } finally {

        }
        return result;
    }


    @Override
    public Object findObjectById(Class cls, long id)  throws Exception {
        if (cls == null || id < 0) return null;
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            List l = JPAUtil.getInstance().findObjects(context,"select c from "+cls.getName()+" as c where c.id = "+id);
            if (l != null && l.size() > 0) {
                Object n = l.get(0);
                return n;
            }

        } catch (Exception ex) {

            throw ex;
        } finally {
            context.release();
        }
        return null;
    }

    @Override
    public Object findObjectByDN(Class cls, String dn)  throws Exception {

        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            List l = JPAUtil.getInstance().findObjects(context,"select c from "+cls.getName()+" as c where c.dn = '"+dn+"'");
            if (l != null && l.size() > 0) {
                Object n = l.get(0);
                return n;
            }

        } catch (Exception ex) {

            throw ex;
        } finally {
            context.release();
        }
        return null;
    }

    @Override
    public void  executeNativeSql(String sql) throws Exception{
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            DBUtil.getInstance().executeNonSelectingSQL(context,sql);
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }
    }
    public List  executeSql(String sql) throws Exception{
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            return DBUtil.getInstance().querySQL(context,sql);
        } catch (Exception ex) {

            throw ex;
        } finally {

            context.release();
        }
    }
    public List  executeSql(String sql,Class clazz) throws Exception{
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            return DBUtil.getInstance().querySQL(context,sql,clazz);
        } catch (Exception ex) {

            throw ex;
        } finally {

            context.release();
        }
    }
    public int  executeUpdateSQL(String sql) throws Exception{
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            return DBUtil.getInstance().executeUpdateSQL(context, sql);
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.end();
            context.release();
        }
    }

    @Override
    public void deleteObject(BObject obj) throws Exception {
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            JPAUtil.getInstance().deleteObject(context,-1,obj);
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.end();
            context.release();
        }
    }

    @Override
    public void removeObject(BObject obj) throws Exception {
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            JPAUtil.getInstance().removeObject(context,-1,obj);
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.end();
            context.release();
        }
    }


    /**
     * 测试删除
     */
    @Override
    public void executeDeleteSQL(String sql, Map mapValue) throws Exception {
        JPASupport context = new JPASupportSpringImpl(beanId);
        try
        {
            context.begin();
            JPAUtil.getInstance().executeQL(context, sql ,mapValue);
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.end();
            context.release();
        }
    }

    public void saveBinaryObject(byte[] bs,String key) throws Exception {
        BinaryObjectUtil.saveObject(key, bs);
    }
    public byte[] readBinaryObject(String key) throws Exception{
        return BinaryObjectUtil.readObject(key);
    }

    public List readBinaryObjectAll() throws Exception{
        return BinaryObjectUtil.readObjectAll();
    }
}
