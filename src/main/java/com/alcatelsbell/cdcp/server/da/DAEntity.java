package com.alcatelsbell.cdcp.server.da;

import com.alcatelsbell.cdcp.nbi.model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/1/15.
 */
public class DAEntity implements Serializable {
    public DAEntity(Class cls, int code, String ql) {
        this.cls = cls;
        this.code = code;
        this.ql = ql;
    }

    public Class cls;
    public int code;
    public String ql;

    @Override
    public String toString() {
        return cls.getSimpleName()+":"+code;
    }




}
