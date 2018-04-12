package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.FHOTNM2000SDHMigrator;
import com.alcatelsbell.cdcp.util.BObjectMemTable;
import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.util.*;
import com.alcatelsbell.nms.util.ObjectUtil;
//import managedElement.ManagedElement_T;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.*;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-11
 * Time: 下午3:25
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TTT extends HelloWorld{
    private Log logger = LogFactory.getLog(getClass());
    public void m1() {
        System.out.println("m1");
        m2();
    }

     void m2() {
        System.out.println("m2");
        m3();
    }

    public static void main(String[] args) {

    }
}
