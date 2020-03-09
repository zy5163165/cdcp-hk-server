package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.ManagedElement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-8
 * Time: 下午9:05
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class BObjectMemTable<T> extends MemTable {

    private transient List<Method>   methodList = new ArrayList<Method>();
    private Class cls = null;
    private Log logger = LogFactory.getLog(getClass());
    public BObjectMemTable(Class cls) {

        this.cls = cls;
        init();
    }
    public BObjectMemTable(Class cls,String... indexColumns) {

        this.cls = cls;
        init();
        if (indexColumns != null) {
            for (String index : indexColumns) {
                addIndex(index);
            }
        }
    }


    public void addObjects(Collection<T> obj) {
        if (obj != null) {
            for (T t : obj) {
                addObject(t);

            }
        }
    }
    public void addObject(T obj) {
        Object[] row = new Object[columns.length];
        for (int i = 0; i < methodList.size(); i++) {
            Method method = methodList.get(i);
            try {
                 Object columnValue = method.invoke(obj);
                row[i] = columnValue;

            } catch (Exception e) {
                logger.error(e, e);
            }

        }
        try {
            add(row);

        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    private void init() {
        Method[] methods = cls.getMethods();
        LinkedList<String> colNameList = new LinkedList<String>();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();
            if (!method.getName().startsWith("get") || methodName.equals("getClass")) continue;
            String colName = methodName.substring(3);
            colName = colName.substring(0,1).toLowerCase()+colName.substring(1);
            colNameList.add(colName);
            methodList.add(method);

        }

        columns = new String[colNameList.size()];
        for (int i = 0; i < colNameList.size(); i++) {
            String s = colNameList.get(i);
            columns[i] = s;
        }

        addIndex("dn");


    }
    public T findOneObject(Condition condition) throws Exception {
        List<T> objects = findObjects(condition);
        if (objects != null && objects.size() > 0)
            return objects.get(0);
        return null;
    }

//    public T findObjectByDn(String dn) throws Exception {
//        return findOneObject(new Condition("dn","=",dn));
//    }
    public T findObjectByDn(String dn) throws Exception {

        return toObject(findRowByIndex("dn",dn));
    }

    public List findObjectByIndexColumn(String column,String value) throws Exception {

        return toObjects(findRowsByIndex(column, value));
    }



    public List<T> findObjects(Condition condition) throws Exception {
        List<MemRow> rows = findRow(condition);
        List l = new ArrayList();
        for (MemRow row : rows) {

            l.add(toObject(row));
        }
        return l;
    }

    private List<T> toObjects(List<MemRow> rows) throws  Exception {
        List list = new ArrayList();
        for (MemRow row : rows) {
            list.add(toObject(row));
        }
        return list;
    }
    private T toObject(MemRow row) throws  Exception {
        if (row == null) return null;
        Object o = cls.newInstance();
        String[] columns1 = row.columns;
        for (int i = 0; i < columns1.length; i++) {
            Object fieldValue = row.getColumnData(columns1[i]);
            Method getMethod = methodList.get(i);
            Method setMethod = cls.getMethod("s"+getMethod.getName().substring(1),getMethod.getReturnType());
            setMethod.invoke(o,fieldValue);
        }
        return (T)o;
    }

    public static void main(String[] args) throws Exception {
        JPASupport jpaSupport = JPASupportFactory.createSqliteJPASupport("D:\\cdcpdb\\SDH_HW_QUZHOU\\2014-06-18-084954-QUZ-T2000-3-P-DayMigration.db");
        List objs = JPAUtil.getInstance().findObjects(jpaSupport, "select c from ManagedElement c");
        BObjectMemTable table = new BObjectMemTable(ManagedElement.class);

        table.addIndex("productName");
        table.addObjects(objs);

        List productName = table.findObjectByIndexColumn("productName", "OptiX OSN 3500");
        Object objectByDn = table.findObjectByDn("EMS:QUZ-T2000-3-P@ManagedElement:591577");
        List id = table.findObjects(new Condition("id", "<", 10002));
        List id2 = table.findObjects(new Condition("nativeEMSName", "like", "42179"));
        System.out.println("id2 = " + id2);
    }

}
