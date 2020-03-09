package com.alcatelsbell.cdcp.web.common;

import com.alcatelsbell.nms.valueobject.alarm.VendorAlarmLib;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Author: Ronnie.Chen
 * Date: 12-10-12
 * Time: 下午1:45
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class BObjectCRUDContext {
    private static BObjectCRUDContext ourInstance = new BObjectCRUDContext();
    private Hashtable<Class,List<BObjectCRUDInteceptor>> table = new Hashtable();

    private Hashtable<Class,  BObjectClassDescriptor> descriptorHashtable = new Hashtable<Class,   BObjectClassDescriptor>();
    public static BObjectCRUDContext getInstance() {
        return ourInstance;
    }

    private BObjectCRUDContext() {
    }

    public void addBObjectCRUDInteceptor(Class bobjectCls, BObjectCRUDInteceptor inteceptor) {
        if (table.get(bobjectCls) == null) table.put(bobjectCls,new Vector());
        table.get(bobjectCls).add(inteceptor);
    }

    public void removeBObjectCRUDInteceptor(Class bobjectCls, BObjectCRUDInteceptor inteceptor) {
        if (table.get(bobjectCls) != null)
            table.get(bobjectCls).remove(inteceptor);
    }

    public List<BObjectCRUDInteceptor> getInteceptors(Class bobjectcls) {
        return table.get(bobjectcls);
    }

    public static void main(String[] args) {
        BObjectCRUDContext.getInstance().addBObjectCRUDInteceptor(VendorAlarmLib.class,new BObjectCRUDInteceptor() {
            @Override
            public void fireOnCRUDEvent(CRUDEvent event) throws Exception {
                if (event.getEventType() == CRUDEvent.EVENT_TYPE_CREATE) {
                    VendorAlarmLib lib = (VendorAlarmLib) event.getObject();
                }
            }
        });
    }

    public  BObjectClassDescriptor getBObjectClassDescriptor(Class cls) {
        return descriptorHashtable.get(cls);
    }

    public void addBObjectClassDescriptor( BObjectClassDescriptor descriptor) {
        if (descriptor != null) {
            descriptorHashtable.put(descriptor.getClass1(),descriptor);
        }
    }
}
