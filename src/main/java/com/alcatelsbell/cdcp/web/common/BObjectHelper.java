package com.alcatelsbell.cdcp.web.common;

import com.alcatelsbell.nms.common.CommonUtil;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.security.LoginInfo;
import com.alcatelsbell.nms.security.client.SecurityClient;
import com.alcatelsbell.nms.valueobject.BObject;
import com.alcatelsbell.nms.valueobject.domain.Operator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * User: Ronnie
 * Date: 12-2-13
 * Time: 下午3:40
 */
public class BObjectHelper implements BObjectCRUDSupport {
    private Log logger = LogFactory.getLog(getClass());
    private JpaClient jpaClient = null;

    public BObjectHelper() {
        jpaClient = JpaClient.getInstance();
    }

    public BObjectHelper(String remoteServiceName) {
        jpaClient = JpaClient.getInstance(remoteServiceName);
    }

    public JpaClient getJpaClient() {
        return jpaClient;
    }
    @Override
    public HashMap<String,BFieldDesc> getBObjectFieldDescs(String clsName) throws Exception {
        try {
            return getBObjectFieldDescs(Class.forName(clsName));
        } catch (Exception e) {
            logger.error(e,e);
            throw e;
        }
    }
    

    public List queryAllBObjects(String clsName, Integer start, Integer end) throws Exception {
        return queryAllBObjects(clsName,start,end,null);
    }

    @Override
    public List queryAllBObjects(String clsName, Integer start, Integer end, String sessionKey) throws Exception {
        try {
            return findObjects(sessionKey,clsName, "select c from " + clsName + " c order by c.id desc", "c", null, start, end);
        } catch (Exception e) {
            logger.error(e,e);
            throw  e;
        }
    }


    public List queryObjects(String clsName, HashMap filter, Integer start, Integer end) throws Exception {
        return queryObjects(clsName,filter, start, end,false,null);
    }

    @Override
    public List queryObjects(String clsName, HashMap filter, Integer start, Integer end, String sessionKey) throws Exception {
        return queryObjects(clsName,filter, start, end,false,sessionKey);
    }

    public List queryObjects(String clsName, HashMap filter, Integer start, Integer end, boolean precise) throws Exception {
        return queryObjects(clsName,filter,start,end,precise,null);

    }

        /**
        *
        * @param clsName
        * @param filter     key: fieldName value: String/List<Integer> /TimePeriod
        * @param start
        * @param end
        * @return
        * @throws Exception
        */
    @Override
    public List queryObjects(String clsName, HashMap filter, Integer start, Integer end, boolean precise, String sessionKey) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (filter == null) filter = new HashMap();
        HashMap map = new HashMap();
        sb.append("select c from "+clsName+" c where 1=1 ");
        //如果是任务单查询需要添加限制条件,登录者只能看到与本人有关的工单（liuhaichen）
        if(clsName.equals("com.alcatelsbell.nms.valueobject.flow.WorkSheet") && !sessionKey.equals("sa")){
        	StringBuffer sf = new StringBuffer();
        	sf.append("SELECT o FROM Operator o WHERE o.loginName = '").append(sessionKey).append("'");
    		List<Operator> operatorLst = jpaClient.findObjects(sf.toString());
    		Operator currentOperator = (Operator)operatorLst.get(0);
    		sb.append(" and (c.creatorDn = '").append(currentOperator.getDn()).append("' ");
            sb.append(" or c.assignerDn = '").append(currentOperator.getDn()).append("' ");
            sb.append(" or c.implementerDn = '").append(currentOperator.getDn()).append("' ");
            sb.append(" or c.departmentDn = '").append(currentOperator.getDepartment()).append("') ");
        }
        Iterator keys = filter.keySet().iterator();
        boolean sortFlag=false;
        String sortField=null;
        String sortType="asc";//asc,desc
        while (keys.hasNext()) {
            String field = (String)keys.next();
            Object value = filter.get(field);
            if (value instanceof String)
                if (value.toString().startsWith("@"))//过滤条件以'@'开头的话则以精确方式进行查询 by 张强
                	{
                		sb.append(" and c."+field+" = '"+value.toString().substring(1)+"'");
                	}
                else if (field.toString().startsWith("#"))//过滤条件以'#'开头的话则以进行排序查询 by 张强
                {
                	sortFlag=true;
                	sortField=field.toString().substring(1);//示例#field,field1 asc
                	sortType=value.toString();
                }
                else
                {
                	sb.append(" and c."+field+" like '%"+value+"%'");
                }
            else if (value instanceof Integer) {
            	sb.append(" and c."+field+" = "+value.toString());
            }

            else if (value instanceof List) {
                sb.append(" and c."+field+" in "+toInString((List)value));
            }
            else if (value instanceof TimePeriod) {
                if (((TimePeriod) value).getFromDate() != null) {
                    sb.append(" and c."+field+" >= :date1");
                    map.put("date1",((TimePeriod) value).getFromDate());
                }
                if (((TimePeriod) value).getToDate() != null) {
                    sb.append(" and c."+field+" <= :date2");
                    map.put("date2",((TimePeriod) value).getToDate());
                }

            } else if (value instanceof Map && ((Map) value).size() > 0) {
                String cd = " and (";
                Map m = (Map) value;
                Set<String> set = m.keySet();
                int idx = 0;
                for (String _fd : set) {
                    Object _value = m.get(_fd);
                    String condition = getCondition(_fd, _value, map);
                    if (idx ++ == 0) {
                        cd += condition;
                    } else {
                        cd += " or " +condition;
                    }
                }
                cd += ")";

                sb.append(cd);
            }
        }

        if (sortFlag) {
        	sb.append(" order by "+sortField+" "+sortType);
        } else {
            sb.append(" order by c.id desc ");
        }
        try {
            return findObjects(sessionKey,clsName, sb.toString(), "c", map, start, end);
        } catch (Exception e) {
            logger.error(e,e);
            throw e;
        }
    }


    private String getCondition(String field, Object value, HashMap map) {
        if (value instanceof String)
            if (value.toString().startsWith("@"))//过滤条件以'@'开头的话则以精确方式进行查询 by 张强
            {
                return (" c."+field+" = '"+value.toString().substring(1)+"'");
            }
//            else if (field.toString().startsWith("#"))//过滤条件以'#'开头的话则以进行排序查询 by 张强
//            {
//                sortFlag=true;
//                sortField=field.toString().substring(1);//示例#field,field1 asc
//                sortType=value.toString();
//            }
            else
            {
                return ("   c."+field+" like '%"+value+"%'");
            }
        else if (value instanceof Integer) {
            return ("   c."+field+" = "+value.toString());
        }

        else if (value instanceof List) {
            return ("   c."+field+" in "+toInString((List)value));
        }
        else if (value instanceof TimePeriod) {
            if (((TimePeriod) value).getFromDate() != null) {

                map.put("date1",((TimePeriod) value).getFromDate());
                return ("   c."+field+" >= :date1");
            }
            if (((TimePeriod) value).getToDate() != null) {

                map.put("date2",((TimePeriod) value).getToDate());
                return ("   c."+field+" <= :date2");
            }

        }
        return "";
    }


    public int queryObjectsCount(String clsName, HashMap filter) throws Exception {
        return queryObjectsCount(clsName,filter,null);
    }

    @Override
    public int queryObjectsCount(String clsName, HashMap filter, String sessionKey) throws Exception {
        StringBuffer sb = new StringBuffer();
        HashMap map = new HashMap();
        sb.append("select count(c) from "+clsName+" c where 1=1 ");
        if (filter == null) filter = new HashMap();
        Iterator keys = filter.keySet().iterator();
        boolean sortFlag=false;
        String sortField=null;
        String sortType="asc";//asc,desc
        while (keys.hasNext()) {
            String field = (String)keys.next();
            Object value = filter.get(field);
            if (value instanceof String)
                if (value.toString().startsWith("@"))//过滤条件以'@'开头的话则以精确方式进行查询 by 张强
                	{
                		sb.append(" and c."+field+" = '"+value.toString().substring(1)+"'");
                	}
                else if (field.toString().startsWith("#"))//过滤条件以'#'开头的话则以进行排序查询 by 张强
                {
                	sortFlag=true;
                	sortField=field.toString().substring(1);//示例#field,field1 asc
                	sortType=value.toString();
                }
                else
                {
                	sb.append(" and c."+field+" like '%"+value+"%'");
                }
                	
                    
            else if (value instanceof List) {
                sb.append(" and c."+field+" in "+toInString((List)value));
            }
            else if (value instanceof TimePeriod) {
                if (((TimePeriod) value).getFromDate() != null) {
                    sb.append(" and c."+field+" >= :date1");
                    map.put("date1",((TimePeriod) value).getFromDate());
                }
                if (((TimePeriod) value).getToDate() != null) {
                    sb.append(" and c."+field+" <= :date2");
                    map.put("date2",((TimePeriod) value).getToDate());
                }

            } else if (value instanceof Map && ((Map) value).size() > 0) {
                String cd = " and (";
                Map m = (Map) value;
                Set<String> set = m.keySet();
                int idx = 0;
                for (String _fd : set) {
                    Object _value = m.get(_fd);
                    String condition = getCondition(_fd, _value, map);
                    if (idx ++ == 0) {
                        cd += condition;
                    } else {
                        cd += " or " +condition;
                    }
                }
                cd += ")";

                sb.append(cd);
            }

        }

        if (sortFlag) {
        	sb.append(" order by "+sortField+" "+sortType);
        }

        try {
            List l = findObjects(sessionKey,clsName, sb.toString(), "c", map, null, null);
            return (l == null|| l.size() == 0) ? 0 : ((Long)l.get(0)).intValue();
        } catch (Exception e) {
            logger.error(e,e);
            throw e;
        }
    }

    private List findObjects(String sessionKey, String clsName, String ql, String entityAlias, HashMap dataMap, Integer start, Integer end) {
        Integer limit = end;
        if (end != null && start != null && end > start)
            limit = end - start +1;
        ql = process(clsName,ql,entityAlias,sessionKey);
        try {
            return jpaClient.findObjects(ql,null,dataMap,start,limit);
        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }

    private String process(String clsName, String ql, String entityAlias, String sessionKey) {
        if (sessionKey == null || sessionKey.isEmpty() || sessionKey.equals("-1"))
            return ql;

        if (!ql.contains(" where "))
             ql+=" where 1=1 ";
        if (clsName.equals("com.alcatelsbell.nms.valueobject.domain.RRegion")) {
            ql += " and "+entityAlias+".dn in ("+getRegionPermissions(sessionKey)+")";
        }
        if (clsName.equals("com.alcatelsbell.nms.valueobject.odn.Rack")) {
            ql += " and "+entityAlias+".dn in ("+getRackPermissions(sessionKey)+")";
        }
        if (clsName.equals("com.alcatelsbell.nms.valueobject.odn.Shelf")) {
            ql += " and "+entityAlias+".dn in ("+getShelfPermissions(sessionKey)+")";
        }
        if (clsName.equals("com.alcatelsbell.nms.valueobject.odn.Card")) {
            ql += " and "+entityAlias+".dn in ("+getCardPermissions(sessionKey)+")";
        }
        if (clsName.equals("com.alcatelsbell.nms.valueobject.odn.Port")) {
            ql += " and "+entityAlias+".dn in ("+getPortPermissions(sessionKey)+")";
        }
        return ql;
    }

    private String getRegionPermissions(String sessionKey) {

        long operatorId = 0;
        Operator operator = null;
        try {
              operator = getOperator(sessionKey);
            operatorId = operator == null ? -1 : operator.getId();
        } catch (Exception e) {
            logger.error(e, e);
            return "-1";
        }
        String ql =
                "select p.targetKey from Permission p,PermissionAssign a,RoleAssign r where p.type = 'REGION' and r.operatorid = "
                        +operatorId+" and r.roleid = a.roleid and a.permissionid = p.id";
        if (operator != null && operator.getLoginName().equalsIgnoreCase("sa"))
            ql =
                    "select p.dn from RRegion p";
        return ql;
    }

    private String getRackPermissions(String sessionKey) {


        Operator operator = null;
        try {
            operator = getOperator(sessionKey);

        } catch (Exception e) {
            logger.error(e, e);
            return "-1";
        }
        String ql = null;

        if (operator != null && operator.getLoginName().equalsIgnoreCase("sa"))
            ql = "select p.dn from Rack p";

        else ql = "select p.dn from Rack p where p.room_dn is null or p.room_dn "+getRoomPermissionInString(operator);
        return ql;
    }

    private String getShelfPermissions(String sessionKey) {


        Operator operator = null;
        try {
            operator = getOperator(sessionKey);

        } catch (Exception e) {
            logger.error(e, e);
            return "-1";
        }
        String ql = null;

        if (operator != null && operator.getLoginName().equalsIgnoreCase("sa"))
            ql = "select p.dn from Shelf p";

        else ql = "select p.dn from Shelf p,Rack r where p.rack_dn = r.dn and r.room_dn "+getRoomPermissionInString(operator);
        return ql;
    }
    private String getCardPermissions(String sessionKey) {


        Operator operator = null;
        try {
            operator = getOperator(sessionKey);

        } catch (Exception e) {
            logger.error(e, e);
            return "-1";
        }
        String ql = null;

        if (operator != null && operator.getLoginName().equalsIgnoreCase("sa"))
            ql = "select p.dn from Card p";

        else ql = "select d.dn from Card d, Shelf p,Rack r where d.shelf_dn = p.dn and p.rack_dn = r.dn and r.room_dn "+getRoomPermissionInString(operator);
        return ql;
    }
    private String getPortPermissions(String sessionKey) {


        Operator operator = null;
        try {
            operator = getOperator(sessionKey);

        } catch (Exception e) {
            logger.error(e, e);
            return "-1";
        }
        String ql = null;

        if (operator != null && operator.getLoginName().equalsIgnoreCase("sa"))
            ql = "select p.dn from Port p";

        else ql = "select p.dn from Port p,Rack r where p.meDn = r.dn and r.room_dn "+getRoomPermissionInString(operator);
        return ql;
    }

    private String getRoomPermissionInString(Operator operator) {
        long operatorId = operator == null ? -1 : operator.getId();

        String ql = null;

        if (operator != null && operator.getLoginName().equalsIgnoreCase("sa"))
            ql = "select p.dn from RRegion p";
        else ql = "select p.targetKey from Permission p,PermissionAssign a,RoleAssign r where p.type = 'REGION' and r.operatorid = "
                +operatorId+" and r.roleid = a.roleid and a.permissionid = p.id";
        List<String> l = null;
        try {
            l = jpaClient.findObjects("select r.dn from Room r where r.region_dn in ("+ql+")");
        } catch (Exception e) {
            logger.error(e,e);
        }
        StringBuffer sb = new StringBuffer("in ( 'null' ");
        if (l != null) {
            for (String regionDn : l) {
                sb.append(", '").append(regionDn).append("'");
            }
        }
        sb.append(" )");
        return sb.toString();
    }


    private long getOperatorId(String loginName) throws Exception {
        LoginInfo loginInfo = SecurityClient.getInstance().getLoginInfo(loginName);
        if (loginInfo != null)
            return loginInfo.getOperator().getId();
        else {
            List<Operator> l = jpaClient.findObjects("select c from Operator c where c.loginName = '"+loginName+"'");
            if (l != null && l.size() > 0)
                return l.get(0).getId();
        }
        return -1;
    }

    private Operator getOperator(String loginName) throws Exception {
        LoginInfo loginInfo = SecurityClient.getInstance().getLoginInfo(loginName);
        if (loginInfo != null)
            return loginInfo.getOperator();
        else {
            List<Operator> l = jpaClient.findObjects("select c from Operator c where c.loginName = '"+loginName+"'");
            if (l != null && l.size() > 0)
                return l.get(0);
        }
        return null;
    }


    @Override
    public BObject saveBObject(BObject object) throws Exception {

        try {
        	if (object.getDn()==null) {
        		object.setDn(SysUtil.nextDN());
        	}
        	
            BObject result = jpaClient.saveObject(-1,object);
            if (object.getId() == null)
                fireObjectCreate(object);
            else
                fireObjectModify(object);
            return result;
        } catch (Exception e) {
            logger.error(e,e);
            throw  e;
        }
    }

    @Override
    public void deleteBObject(BObject object) throws Exception {
        try {
            jpaClient.removeObject(object);
            fireObjectDelete(object);
        } catch (Exception e) {
            logger.error(e,e);
            throw e;
        }
    }

    private String toInString(Collection list) {
        String str = "(";
        for (Object o : list) {

        	if (o.getClass()==String.class) {
        		str += "'"+o+"',";
        	} else {
        		str += ""+o+",";
        	}
            
        }
        
        if (str.endsWith(",")) {
        	str=str.substring(0, str.length()-1)+")";
        } else {
        	str += ")";
        }
        

        return str;
    }




    @Override
    public HashMap<String,BFieldDesc> getBObjectFieldDescs(Class cls) {
        XMLLoadBClasses.getInstance().reload();
        HashMap<String, BFieldDesc> mp = XMLLoadBClasses.getInstance().getClsMap().get(cls);
        if (mp != null) return mp;
        HashMap<String,BFieldDesc> map = new HashMap<String, BFieldDesc>();
//        Field[] fields = cls.getDeclaredFields();
//        if (fields != null) {
//            for (Field field : fields) {
//                if (field.getAnnotation(BField.class) != null) {
//                    BFieldDesc bfd = new BFieldDesc(field);
//                    map.put(field.getName(),bfd);
//                }
//            }
//        }
        BObjectClassDescriptor bObjectClassDescriptor = BObjectCRUDContext.getInstance().getBObjectClassDescriptor(cls);
        List<Field> fields = getAllBFields(cls);
        if (fields != null) {
            int idx = 0;
            for (Field field : fields) {
                if (bObjectClassDescriptor == null || bObjectClassDescriptor.isBFieldVisible(field.getName())) {
                    BFieldDesc bfd = new BFieldDesc(field);
                    if (bfd.getSequence() == 100) bfd.setSequence(idx++);
                    map.put(field.getName(),bfd);
                }
            }
        }
        return map;
    }

    private List<Field> getAllBFields(Class cls) {
        List<Field> allFields = new ArrayList<Field>();
        Field[] fields = cls.getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                if (field.getAnnotation(BField.class) != null) {
                    BField bField = field.getAnnotation(BField.class);
                    if (bField.description() != null && !bField.description().isEmpty())
                        allFields.add(field);
                }
            }
        }
        if (cls.getSuperclass() != null) {
            allFields.addAll(getAllBFields(cls.getSuperclass()));
        }

        return allFields;
    }

    private void fireObjectCreate(BObject object) {
       List<BObjectCRUDInteceptor> inteceptors = BObjectCRUDContext.getInstance().
               getInteceptors(object.getClass());
       if (inteceptors != null) {
           for (BObjectCRUDInteceptor inteceptor : inteceptors) {
               try {
                   inteceptor.fireOnCRUDEvent(new BObjectCRUDInteceptor.CRUDEvent
                           (BObjectCRUDInteceptor.CRUDEvent.EVENT_TYPE_CREATE,object));
               } catch (Exception e) {
                   logger.error(e, e);
               }
           }
       }
    }
    private void fireObjectModify(BObject object) {
        List<BObjectCRUDInteceptor> inteceptors = BObjectCRUDContext.getInstance().
                getInteceptors(object.getClass());
        if (inteceptors != null) {
            for (BObjectCRUDInteceptor inteceptor : inteceptors) {
                try {
                    inteceptor.fireOnCRUDEvent(new BObjectCRUDInteceptor.CRUDEvent
                            (BObjectCRUDInteceptor.CRUDEvent.EVENT_TYPE_MODIFY,object));
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
    }
    private void fireObjectDelete(BObject object) {
        List<BObjectCRUDInteceptor> inteceptors = BObjectCRUDContext.getInstance().
                getInteceptors(object.getClass());
        if (inteceptors != null) {
            for (BObjectCRUDInteceptor inteceptor : inteceptors) {
                try {
                    inteceptor.fireOnCRUDEvent(new BObjectCRUDInteceptor.CRUDEvent
                            (BObjectCRUDInteceptor.CRUDEvent.EVENT_TYPE_DELETE,object));
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
    }

    public BObject lazyLoad(BObject bObject) {
        List<Field> allBFields = getAllBFields(bObject.getClass());
        if (allBFields != null) {
            for (Field field : allBFields) {
                BField bField = field.getAnnotation(BField.class);
                if (bField.lazyLoadExp() != null && bField.lazyLoadExp().length() > 0) {
                    String lazyLoadExp = bField.lazyLoadExp();
                    Object value = excuteLazyLoadExp(bObject,lazyLoadExp);
                    boolean oldAccessible = field.isAccessible();
                    field.setAccessible(true);
                    try {
                        field.set(bObject, value);
                    } catch (IllegalAccessException e) {

                    }
                    field.setAccessible(oldAccessible);
                }
            }
        }

        return bObject;
    }

    private Object excuteLazyLoadExp(BObject bObject, String lazyLoadExp) {
        lazyLoadExp = lazyLoadExp.trim();
        if (lazyLoadExp.startsWith("jpql://")) {
            String jpql = lazyLoadExp.substring("jpql://".length());
            List<String> jpqlParas = findJpqlParas(jpql);
            HashMap map = new HashMap();
            for (String para : jpqlParas) {
                try {
                    Object filedValue = CommonUtil.getInstance().getFiledValue(bObject, para);
                    if (filedValue == null)
                        return null;

                    if (filedValue instanceof String) {
                        if (((String) filedValue).isEmpty())
                            return null;
                    }
                        map.put(para, filedValue);
                } catch (Exception e) {
                    logger.error(e,e);
                }
            }
            List l = null;
            try {
                l = jpaClient.findObjects(jpql,null, map,null,null);
            } catch (Exception e) {
                logger.error("failed to execute jpql", e);
            }
            if (l!= null && l.size() > 0) return l.get(0);
        }

        return null;
    }

    private List<String> findJpqlParas(String jpql) {
        List list = new ArrayList();
        while (true) {
            if (jpql.contains(":")) {
                int idx = jpql.indexOf(":");
                int idx2 = jpql.indexOf(")",idx);
                if (idx2 < 0)
                    idx2 = jpql.indexOf(" ",idx);


                String para = null;
                if (idx2 > 0)
                    para = jpql.substring(idx+1,idx2);
                else
                    para = jpql.substring(idx+1);
                list.add(para);
                jpql = jpql.substring(idx+1);
            } else {
                break;
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
//        List<RRegion> l = new BObjectHelper().queryAllBObjects("com.alcatelsbell.nms.valueobject.domain.RRegion",0,10,"one");
//        List<RRegion> l2 = new BObjectHelper().queryAllBObjects("com.alcatelsbell.nms.valueobject.domain.RRegion",0,10,"one");
//        for (RRegion region : l)
//            System.out.println(region.getRegionname());
//        HashMap map = new BObjectHelper().getBObjectFieldDescs(RackType.class);
//        System.out.println("map = " + map);

//        Port p = (Port) jpaClient.findObjectByDN(Port.class,"OP90010000001011290007<>2<>4");
//        BObjectHelper bObjectHelper = new BObjectHelper();
//        bObjectHelper.lazyLoad(p);
//        System.out.println(p);
    }
}
