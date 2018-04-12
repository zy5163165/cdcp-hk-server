package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.nbi.model.CEMS;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-10-14
 * Time: 上午8:51
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DataChecker {
    private Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) throws Exception {
        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        JPASupport jpaSupport = new JPASupportSpringImpl("entityManagerFactoryData");

        List<CEMS> cemses = JPAUtil.getInstance().findAllObjects(jpaSupport, CEMS.class);
        for (CEMS cemse : cemses) {
        //    queryCount(jpaSupport,"select c from CTunnel c")
        }

    }

    public static int queryCount( JPASupport jpaSupport,String ql) throws Exception {
        List objects = JPAUtil.getInstance().findObjects(jpaSupport, ql);
        return ((Number)objects.get(0)).intValue();
    }
}
