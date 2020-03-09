package com.alcatelsbell.cdcp.web.common;

import com.alcatelsbell.nms.common.Dic;
import com.alcatelsbell.nms.common.DicEntry;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.json.JSONUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-1-24
 * Time: 上午11:21
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class XMLLoadBClasses {
    private Log logger = LogFactory.getLog(getClass());
    private HashMap<String,List<DicEntry>> dicEntries = new HashMap<String, List<DicEntry>>();
    private HashMap<String,Dic> dics = new HashMap<String, Dic>();
    private HashMap<String,ConfigurationNode> dicNodes = new HashMap<String, ConfigurationNode>();

    private static XMLLoadBClasses inst = new XMLLoadBClasses();
    public static XMLLoadBClasses getInstance() {
        return inst;
    }
    private XMLLoadBClasses() {
        try {
            init();
        } catch (ConfigurationException e) {
            logger.error(e, e);
        }
    }

    public void reload() {
        clsMap = new HashMap<Class, HashMap<String, BFieldDesc>>();
        dicEntries = new HashMap<String, List<DicEntry>>();
        dics = new HashMap<String, Dic>();
        dicNodes = new HashMap<String, ConfigurationNode>();
        try {
            init();
        } catch (ConfigurationException e) {
            logger.error(e, e);
        }
    }
    private HashMap<Class,HashMap<String, BFieldDesc>> clsMap = new HashMap<Class, HashMap<String, BFieldDesc>>();

    public HashMap<Class, HashMap<String, BFieldDesc>> getClsMap() {
        return clsMap;
    }

    public void init() throws ConfigurationException {
        XMLConfiguration xmlConfiguration = new XMLConfiguration(XMLLoadBClasses.class.getClassLoader()
                .getResource("bclass.xml"));

        xmlConfiguration.setDelimiterParsingDisabled(true);
        List<ConfigurationNode> children = xmlConfiguration.getRootNode().getChildren();
        for (int i = 0; i < children.size(); i++) {
            ConfigurationNode configurationNode = children.get(i);
            if (configurationNode.getName().equals("BClass"))
                loadDictionary(configurationNode);
        }
    }

    private void loadDictionary(ConfigurationNode bcNode) {

        String type = getAttributeValue(bcNode, "type");
        String name = getAttributeValue(bcNode,"name");
        String desc = getAttributeValue(bcNode,"desc");
        String cls = getAttributeValue(bcNode,"class");
        dics.put(name,new Dic(name, type,desc));
        dicNodes.put(name, bcNode);
        try {
            if ("BObject".equals(type)) {
                loadBObjectDesc(name, bcNode);
            }
            else {
                logger.error("type attribute not found :" + bcNode.getName());
            }
        } catch (Exception e) {
            logger.error(e+" Error loading dictionary :"+name , e);
        }
    }



    public void clear(String dicName) {
        dicEntries.remove(dicName);
    }

    JSONUtil jsonUtil = new JSONUtil();

    /**
     * @todo
     * @param bcNode
     */
    private void loadBObjectDesc(String name, ConfigurationNode bcNode) throws Exception {

        String codeField = getAttributeValue(bcNode,"keyField");
        String labelField = getAttributeValue(bcNode,"labelField");
        String cls = getAttributeValue(bcNode,"class");
        HashMap<String, BFieldDesc> map = new HashMap<String, BFieldDesc>();

        List<ConfigurationNode> children = bcNode.getChildren();
        for (ConfigurationNode child : children) {
            if (child.getName().equals("BFields")) {
                List<ConfigurationNode> bfields = child.getChildren();
                for (ConfigurationNode bfield : bfields) {
                    BFieldDesc bFieldDesc = new BFieldDesc();
                    String fieldName = getAttributeValue(bfield,"name");
                    String description = getAttributeValue(bfield,"description");
                    String fieldtype = getAttributeValue(bfield,"fieldtype");
                    String viewtype = getAttributeValue(bfield,"viewtype");
                    String searchtype = getAttributeValue(bfield,"searchtype");
                    String length = getAttributeValue(bfield,"length");
                    String edittype = getAttributeValue(bfield,"edittype");
                    String values = getAttributeValue(bfield,"values");
                    String sequence = getAttributeValue(bfield,"sequence");

                    bFieldDesc.setFieldType(fieldtype);
                    bFieldDesc.setViewType(viewtype);
                    bFieldDesc.setSearchType(searchtype);
                    bFieldDesc.setDescription(description);
                    bFieldDesc.setEditType(edittype);
                    bFieldDesc.setFieldName(fieldName);
                    bFieldDesc.setDnReferenceEntityField(getAttributeValue(bfield,"dnReferenceEntityField"));
                    bFieldDesc.setDnReferenceEntityName(getAttributeValue(bfield,"dnReferenceEntityName"));
                    bFieldDesc.setDnReferenceTransietField(getAttributeValue(bfield,"dnReferenceTransietField"));
                    if (sequence != null)
                        bFieldDesc.setSequence(Integer.parseInt(sequence));
                    else
                        bFieldDesc.setSequence(0);

                    if (values != null) {
                        List vmap = new ArrayList();
                        if (values.trim().startsWith("[")) {
                            JSONArray jsonArray = jsonUtil.createJSONArray(values);
                            for (Object o : jsonArray) {
                                JSONObject jo = (JSONObject)o;
                                HashMap vs = new HashMap();
                                vs.put("key",jo.get("key"));
                                vs.put("label",jo.get("label"));
                                if (jo.get("color") != null)
                                    vs.put("color",jo.get("color"));
                                vmap.add(vs);

                            }
                        }

                        else if (values.trim().contains("select")) {
                            List objects = JpaClient.getInstance().findObjects(values);
                            for (Object row : objects) {
                                Object[] r = (Object[]) row;
                                HashMap vs = new HashMap();
                                vs.put("key",r[0]);
                                vs.put("label",r[1]);
                                if (r.length > 2)
                                    vs.put("color",r[2]);
                                vmap.add(vs);
                            }
                        }


                        bFieldDesc.setValues(vmap);

                    }

                    map.put(fieldName,bFieldDesc);

                }
            }
        }

        clsMap.put(Class.forName(cls),map);

    }

    private String getAttributeValue(ConfigurationNode node, String attributeName){
        List<ConfigurationNode> attributes = node.getAttributes(attributeName);
        if (attributes != null && attributes.size() > 0) {
            Object value = attributes.get(0).getValue();
            return value.toString();
        }
        return null;
    }

    public static void main(String[] args) throws ConfigurationException {

        HashMap<Class, HashMap<String, BFieldDesc>> clsMap1 = new XMLLoadBClasses().getClsMap();
        for (Class aClass : clsMap1.keySet()) {

        }
    }
}
