package com.alcatelsbell.cdcp.web.plugin;

import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.cdcp.web.common.BObjectEvent;
import com.alcatelsbell.cdcp.web.common.BObjectPlugin;
import com.alcatelsbell.cdcp.web.common.RequestContext;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/25
 * Time: 13:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class EmsPlugin extends BObjectPlugin {
    private Logger logger = LoggerFactory.getLogger(EmsPlugin.class);

    public EmsPlugin() {
        List<Ems> emsList = null;
        try {
            emsList = JpaClient.getInstance().findAllObjects(Ems.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        for (Ems ems : emsList) {
            String additionalinfo = ems.getAdditionalinfo();
            HashMap<String,String> map = new HashMap();
            if (additionalinfo == null) continue;
            String[] split = additionalinfo.split(";");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                String regex = "\\|";
                String[] split1 = s.split(regex);
                if (split1.length == 2) {
                    String key = split1[0];
                    String value = split1[1];
                    map.put(key.trim(),value.trim());
                }
            }

            String corbaUrl = map.get("config_corbaUrl");
            String corbaUserName = map.get("config_corbaUserName");
            String corbaPassword = map.get("config_corbaPassword");

            if (!(assertEquals(corbaUrl,ems.getTag1()) &&
                    assertEquals(corbaUserName,ems.getTag2()) &&
                    assertEquals(corbaPassword,ems.getTag3())) )   {
                ems.setTag1(corbaUrl);
                ems.setTag2(corbaUserName);
                ems.setTag3(corbaPassword);
                try {
                    JpaClient.getInstance().saveObject(-1,ems);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private boolean assertEquals(String o1,String o2) {
        return (o1+"").equals(o2+"");
    }

    @Override
    public Class getJavaClass() {
        return Ems.class;
    }

    @Override
    public void onEvent(RequestContext context, BObjectEvent event) {
        if (event.getName().equals(BObjectEvent.UPDATE)) {
            Ems newEms = (Ems)event.getObject();
            Ems oldEms = (Ems)event.getObject2();
            String additionalinfo = newEms.getAdditionalinfo();
            if (!assertEquals(newEms.getTag1(),oldEms.getTag1())) {
                additionalinfo = additionalinfo.replace("config_corbaUrl|"+oldEms.getTag1(),"config_corbaUrl|"+newEms.getTag1());
            }
            if (!assertEquals(newEms.getTag2(),oldEms.getTag2())) {
                additionalinfo = additionalinfo.replace("config_corbaUserName|"+oldEms.getTag2(),"config_corbaUserName|"+newEms.getTag2());
            }
            if (!assertEquals(newEms.getTag3(),oldEms.getTag3())) {
                additionalinfo = additionalinfo.replace("config_corbaPassword|"+oldEms.getTag3(),"config_corbaPassword|"+newEms.getTag3());
            }
            if (!assertEquals(additionalinfo,newEms.getAdditionalinfo())) {
                newEms.setAdditionalinfo(additionalinfo);
                try {
                    JpaClient.getInstance().saveObject(-1,newEms);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }

            if (!assertEquals(oldEms.getIsMonitored()+"",newEms.getIsMonitored()+"")) {
                Integer isMonitored = newEms.getIsMonitored();
                MBeanProxy<NodeAdminMBean> proxy = null;
                try {
                    proxy = CdcpServerUtil.createNodeAdminProxy(newEms.getDn());

                    if (isMonitored != null && isMonitored == 1) {
                        proxy.proxy.newEms(newEms);
                    }
                    if (isMonitored != null && isMonitored == 0) {
                        proxy.proxy.removeEms(newEms);
                    }


                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (proxy != null) {
                        try {
                            proxy.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

            }


        }
    }
}
