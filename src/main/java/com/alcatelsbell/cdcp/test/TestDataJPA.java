package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-26
 * Time: 下午3:02
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestDataJPA {
    public static void main(String[] args) throws Exception {
        String[] locations = {"appserver-spring.xml"};
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        for (int i = 0; i < 11; i++) {
            System.out.println(i);
            JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
            jpaSupport.begin();
            JPAUtil.getInstance().findObjectById(jpaSupport, CDevice.class,1);

        }

        JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");
        int count = 1000000;
        long t1 = System.currentTimeMillis();
//        for (int i = 0; i < count; i++) {
//            CDevice cDevice = new CDevice();
//            cDevice.setDn(i+":dn");
//            jpaSupport.begin();
//            JPAUtil.getInstance().saveObject(jpaSupport,-1,cDevice);
//            jpaSupport.end();
//
//        }

        long t2 = System.currentTimeMillis();
        System.out.println("spend time : "+(t2-t1)+"ms");

          t1 = System.currentTimeMillis();
        jpaSupport.begin();
        for (int i = 0; i < count; i++) {
            CDevice cDevice = new CDevice();
            cDevice.setDn(SysUtil.nextDN());
            cDevice.setSid(SysUtil.nextLongId());
            JPAUtil.getInstance().saveObject(jpaSupport,-1,cDevice);
//            if (i % 1000 == 0) {
//                jpaSupport.getEntityManager().flush();
//                jpaSupport.getEntityManager().clear();
//            }
            if (i % 10000 == 0) {
                System.out.println(i);
            }

        }
        jpaSupport.end();
          t2 = System.currentTimeMillis();
        System.out.println("spend time : "+(t2-t1)+"ms");

    }
}
