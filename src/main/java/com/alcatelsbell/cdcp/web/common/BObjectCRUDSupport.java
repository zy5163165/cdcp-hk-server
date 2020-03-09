package com.alcatelsbell.cdcp.web.common;


import com.alcatelsbell.nms.valueobject.BObject;

import java.util.HashMap;
import java.util.List;

/**
 * User: Ronnie
 * Date: 12-5-21
 * Time: 下午3:14
 */
public interface BObjectCRUDSupport {
    public HashMap<String,  BFieldDesc> getBObjectFieldDescs(String clsName) throws Exception;

    public List queryAllBObjects(String clsName, Integer start, Integer end, String sessionKey) throws Exception;

    public List queryObjects(String clsName, HashMap filter, Integer start, Integer end, String sessionKey) throws Exception;
    public List queryObjects(String clsName, HashMap filter, Integer start, Integer end, boolean precise, String sessionKey) throws Exception;

    public int queryObjectsCount(String clsName, HashMap filter, String sessionKey) throws Exception;

    public BObject saveBObject(BObject object) throws Exception;

    public void deleteBObject(BObject object) throws Exception;

    public HashMap<String,  BFieldDesc> getBObjectFieldDescs(Class cls);
}
