package com.alcatelsbell.cdcp.server.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-16
 * Time: 下午2:08
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpServerMessage implements Serializable {

    private ConcurrentHashMap attributes = new ConcurrentHashMap();
    private Serializable object;

    public Serializable getObject() {
        return object;
    }

    public ConcurrentHashMap getAttributes() {
        return attributes;
    }

    public void setAttributes(ConcurrentHashMap attributes) {
        this.attributes = attributes;
    }

    public void setObject(Serializable object) {
        this.object = object;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key,Object value) {
        if (value != null)
            attributes.put(key,value);
    }
}
