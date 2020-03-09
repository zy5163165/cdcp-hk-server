package com.alcatelsbell.cdcp.test;


import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nodefx.CDCPConstants;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;

import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-12
 * Time: 下午7:54
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class InitEmsData {
    public static void main(String[] args) throws Exception {
        ServerEnv.init();
        List list = new ArrayList();
        SAXBuilder builder = new SAXBuilder();
        File dir = new File("d:\\work\\xml");
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            Ems ems = new Ems();
            if (file.getName().startsWith("HW")) {
                ems.setType(CDCPConstants.EMS_TYPE_HWU2000_PTN);
                ems.setVendordn("Huawei");
            } else {
                ems.setType(CDCPConstants.EMS_TYPE_FENGHUOOTNM2000_PTN);
                ems.setVendordn("Fenghuo");
            }
            Document xmlDoc = builder.build(file);
            Element rootElement = xmlDoc.getRootElement();
            List children = rootElement.getChildren();
            for (int j = 0; j < children.size(); j++) {
                Element o = (Element)children.get(j);
                List children1 = o.getChildren();
                HashMap<String,String> properties = new HashMap<String, String>();
                for (int k = 0; k < children1.size(); k++) {
                    Element o1 =  (Element)children1.get(k);
                    Attribute attribute = o1.getAttribute("name");
                    String name = attribute.getValue();

                    String value = o1.getAttribute("value").getValue();
                    properties.put(name,value);
                }

                if (properties.get("emsName") != null) {
                    ems.setDn(properties.get("emsName"));
                    ems.setName(properties.get("emsName"));
                //    ems.setVendordn("");
                    ems.setStatus(Constants.EMS_STATUS_NORMAL);
                    ems.setSysNodeDn("hw1");
                    String additionalInfo =
                            "config_emsname|"+properties.get("emsName")+";"+
                            "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                                    "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                                    "config_corbaTree|"+properties.get("corbaTree")+";"+
                                    "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                                    "config_corbaPassword|"+properties.get("corbaPassword");
                    ems.setAdditionalinfo(additionalInfo);

                    list.add(ems);

                    break;
                }

            }

        }
        System.out.println(list.size());

        for (int i = 0; i < list.size(); i++) {
            Ems ems = (Ems) list.get(i);
            JpaServerUtil.getInstance().saveObject(-1,ems);
        }

    }
}
