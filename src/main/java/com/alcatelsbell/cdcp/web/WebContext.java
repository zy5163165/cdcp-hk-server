package com.alcatelsbell.cdcp.web;

import com.alcatelsbell.cdcp.web.common.Module;
import com.alcatelsbell.nms.common.SpringContext;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.security.LoginInfo;
import com.alcatelsbell.nms.valueobject.domain.Permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Ronnie.Chen
 * Date: 2016/8/22
 * Time: 14:38
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class WebContext implements ApplicationListener<ContextRefreshedEvent> {
    private Logger logger = LoggerFactory.getLogger(WebContext.class);

    private List<Module> modules = null;
    private HashMap<String,LoginInfo> loginInfoMap = new HashMap ();

    public HashMap<String, LoginInfo> getLoginInfoMap() {
        return loginInfoMap;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
                ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
   //     SpringContext.getInstance().setApplicationContext(applicationContext);
        logger.info("Spring application context loaded !");



//        DataSource ds = (DataSource) SpringContext.getInstance().getApplicationContext().getBean("dataSource");
//        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
//        jdbcTemplate.execute("alter table PM_DATA modify column displayvalue varchar(1024)");


//        synchronized (this) {
//            try {
//                List objects = JpaServerUtil.getInstance().findObjects("select c from Permission c where c.type='sys'");
//                if (objects == null || objects.size() == 0) {
//                    Permission permission = new Permission();
//                    permission.setType("sys");
//                    permission.setName("管理员权限");
//                    permission.setDescription("系统权限");
//                    permission.setTargetKey("Admin");
//                    JpaServerUtil.getInstance().saveObject(-1, permission);
//                }
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//            }
//        }

        if (modules != null) {
            for (Module module : modules) {
                try {
                    module.start();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }



    }

    public static void main(String[] args) {
        Matcher m = Pattern.compile("\\[([^\\[\\]]+)\\]").matcher("[abc][dff]");
        m.find();
        String group = m.group();
        System.out.println(group);
        m.find();
        System.out.println(m.group());
    }
}
