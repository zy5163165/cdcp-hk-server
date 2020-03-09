package com.alcatelsbell.cdcp.web.common;

import java.io.Serializable;

/**
 * Author: Ronnie.Chen
 * Date: 2016/10/19
 * Time: 11:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class BObjectEvent implements Serializable {
    public static final String ADD = "ADD";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";

    private String name;
    private Object object;
    private Object object2;

    public BObjectEvent(String name, Object object) {
        this.name = name;
        this.object = object;
    }

    public BObjectEvent(String name, Object object, Object object2) {
        this.name = name;
        this.object = object;
        this.object2 = object2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject2() {
        return object2;
    }

    public void setObject2(Object object2) {
        this.object2 = object2;
    }
}
