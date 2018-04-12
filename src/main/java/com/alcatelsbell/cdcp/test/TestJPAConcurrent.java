package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.alarm.Alarminformation;
import com.alcatelsbell.nms.valueobject.physical.Managedelement;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-18
 * Time: 下午1:03
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestJPAConcurrent implements Runnable{

    private String name = null;

    public TestJPAConcurrent(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        JPASupport jpaSupport = JPASupportFactory.createSqliteJPASupport("ccc.db");
        ((JPASupportSpringImpl)jpaSupport).setSingleThreadTx(true);
        try {
            jpaSupport.begin();
            System.out.println("begin:"+name);
            for (int i = 0; i < 1000; i++) {
                Alarminformation me = new Alarminformation();
                me.setDn(SysUtil.nextDN());
                me.setEventname(name);
                JPAUtil.getInstance().createObject(jpaSupport,-1,me);
            }

            System.out.println("end1:"+name);
            jpaSupport.end();
            System.out.println("end2:"+name);
        } catch (Exception e) {
            e.printStackTrace();
            jpaSupport.release();
        }
    }

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            TestJPAConcurrent concurrent = new TestJPAConcurrent("name_"+i);
            new Thread(concurrent).start();
        }
    }
}
