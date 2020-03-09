package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.meta.MetaConfig;
import com.alcatelsbell.nms.valueobject.sys.SysNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-16
 * Time: 下午9:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestMgrJPA {
    private Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) throws Exception {
        String[] locations = {"appserver-spring.xml"};
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactory");
        jpaSupport.begin();
        for (int i = 0; i < 500000; i++) {
            MetaConfig mc = new MetaConfig();
            mc.setDn(SysUtil.nextDN());
            mc.setName("");
            JPAUtil.getInstance().createObject(jpaSupport,-1,mc);
        }
        jpaSupport.end();

    }
}
