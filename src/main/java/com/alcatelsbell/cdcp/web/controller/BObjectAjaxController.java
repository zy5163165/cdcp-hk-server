package com.alcatelsbell.cdcp.web.controller;


import com.alcatelsbell.cdcp.util.ReflectionUtil;
import com.alcatelsbell.cdcp.web.DefaultPlugin;
import com.alcatelsbell.cdcp.web.common.*;
import com.alcatelsbell.cdcp.web.util.CSVWriter;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.util.SortUtil;
import com.alcatelsbell.nms.valueobject.BObject;

import org.apache.commons.httpclient.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2016/8/23
 * Time: 14:21
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Controller
@RequestMapping("/bo/*")
public class BObjectAjaxController extends AbstractAjaxController {
    private Logger logger = LoggerFactory.getLogger(BObjectAjaxController.class);

    protected BObjectHelper crudSupport = null;
    private String remoteServiceName = null;


    @Autowired
    private ArrayList<BObjectPlugin> boPlugins = null;

    private BObjectPlugin defaultPlugin = new DefaultPlugin();

    public BObjectAjaxController() {
        crudSupport = new BObjectHelper();
    }

    public String getRemoteServiceName() {
        return remoteServiceName;
    }

    public void setRemoteServiceName(String remoteServiceName) {
        this.remoteServiceName = remoteServiceName;
    }

    public BObjectCRUDSupport getCrudSupport() {
        return crudSupport;
    }





    @RequestMapping(value="getBObjectFieldDescs")
    public @ResponseBody
    HashMap<String, BFieldDesc> getBObjectFieldDescs(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        HashMap<String, BFieldDesc> map = null;
        try {
            map = crudSupport.getBObjectFieldDescs(request.getParameter("javaClassName"));
            map.remove("id");
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return map;
    }

    @RequestMapping(value="queryAllBObjectsK")
    public @ResponseBody
    List queryAllBObjects(String clsName, Integer start, Integer end, String sessionKey) throws Throwable {
        List<BObject> resultList = null;
        try {
            resultList = crudSupport.queryAllBObjects(clsName, start, end, sessionKey);
            for(BObject obj : resultList){
                BFieldParser.parseBObject(obj);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return resultList;
    }

    @RequestMapping(value="queryAllBObjects")
    public @ResponseBody
    List queryAllBObjects(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        Integer start = extractInt(request, "start");
        Integer limit = extractInt(request, "limit");
        return queryAllBObjects(request.getParameter("javaClassName"), start, start + limit-1,null);
    }


    private BObjectPlugin findPlugin(String javaClassName) {
        if (boPlugins == null) return null;
        try {
            for (BObjectPlugin boPlugin : boPlugins) {
                if (boPlugin.getJavaClass().equals(Class.forName(javaClassName))) {
                    if (boPlugin.getCrudHelper() == null) boPlugin.setCrudHelper(crudSupport);
                    if (boPlugin.getWebContext() == null) boPlugin.setWebContext(getWebContext());
                    return boPlugin;
                }
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String getSearchTxtQL(HttpServletRequest request) {
        String searchTxt = getExtendSearchTxt(request);

        if (searchTxt != null) {
            String javaClassName = request.getParameter("javaClassName");
            String ql = "select c from "+javaClassName+" c where ( 1=0 ";
            if (javaClassName != null) {
                HashMap<String, BFieldDesc> descs = null;
                try {
                    descs = crudSupport.getBObjectFieldDescs(Class.forName(javaClassName));
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(),e);
                    return null;
                }
                for (String column : descs.keySet()) {
                    BFieldDesc desc = descs.get(column);
                    if (desc.getFieldType().equals(BFieldDesc.FIELD_TYPE_STRING)) {
                        ql += " or c."+column+" like '%"+searchTxt+"%'";
                    }
                }

            }
            ql += ")";

            String sort = request.getParameter("sort");
            String sortType = request.getParameter("sortType");
            if (sort != null && sort.trim().length() > 0) {
                ql += " order by c."+sort+" "+sortType;
            }

            return ql;
        }

        return null;

    }


    @RequestMapping(value="queryObjects")
    public @ResponseBody
    List queryObjects(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        Integer start = extractInt(request, "start");
        Integer limit = extractInt(request, "limit");
        try {
            return queryObjects(request,response,start,limit);
        } catch (Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }
        return null;
    }



    private List queryObjects(HttpServletRequest request, HttpServletResponse response, int start , int limit) throws Throwable {
        String javaClassName = request.getParameter("javaClassName");
        BObjectPlugin plugin = findPlugin(javaClassName);
        if (plugin != null) {
            Object obj = plugin.interceptQuery(request,response);
            if (obj != null) return (List)obj;
        }

        String ql = getSearchTxtQL(request);
        if (ql != null) {
            return JpaServerUtil.getInstance().findObjects(ql,null,null,start,limit);
        }


        return queryObjects(javaClassName,extractFilterMap(request),start,start+limit-1,null);
    }

    @RequestMapping(value="export")
    public @ResponseBody
    List exportObjects(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        System.out.println(" ============ export ======================= ");
        Integer count = queryObjectsCount(request,response);

        int start = 0;
        String javaClassName = request.getParameter("javaClassName");
        Class<?> cls = Class.forName(javaClassName);
        String fileName = cls.getSimpleName() + ".csv";
        File file = new File(fileName);
        OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file),"gb2312");
    //    FileWriter fileWriter = new FileWriter(writer);
        CSVWriter writer = new CSVWriter(fileWriter);
        writer.writeLine(toLineValues(cls));
        while (start < count) {
            logger.info("export "+cls.getSimpleName()+" from "+start+" to "+(start+1000));
            List<Object> objects = queryObjects(request,response,start,1000);
            start += objects.size();
            for (Object object : objects) {
                writer.writeLine(toLineValues(object));
            }
        }

        writer.close();



        response.setContentType("text/plain");
        response.setHeader("Location",fileName);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        OutputStream outputStream = response.getOutputStream();
        InputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int i = -1;
        while ((i = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, i);
        }
        outputStream.flush();
        outputStream.close();
        return null;



    }

    private int toInt(String str) {
       if (str.equals(BFieldDesc.FIELD_TYPE_DATE)) {
           return 0;
       }
        if (str.equals(BFieldDesc.FIELD_TYPE_FLOAT)) {
            return 1;
        }
        if (str.equals(BFieldDesc.FIELD_TYPE_INT)) {
            return 2;
        }
        if (str.equals(BFieldDesc.FIELD_TYPE_LONG)) {
            return 3;
        }
        if (str.equals(BFieldDesc.FIELD_TYPE_STRING)) {
            return 4;
        }
        return -1;
    }

    private List<String> toLineValues(Object object) throws IllegalAccessException {

        Class cls = null;
        if (object instanceof Class)
            cls = (Class) object;
        else
            cls = object.getClass();
        HashMap<String, BFieldDesc> descs = crudSupport.getBObjectFieldDescs(cls);


        List<BFieldDesc> fields = new ArrayList();
        for (String fieldName : descs.keySet()) {
            fields.add(descs.get(fieldName));
        }
        SortUtil.sort(fields, new SortUtil.CompareAdapter() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((BFieldDesc)o1).getSequence() - ((BFieldDesc)o2).getSequence();
            }
        });

        List<String> fieldLabels = new ArrayList();
        for (BFieldDesc field : fields) {
            fieldLabels.add(field.getDescription());
        }

        if (object instanceof Class) return fieldLabels;


        List<String> lineValues = new ArrayList();
        for (BFieldDesc bFieldDesc : fields) {
            Object value = ReflectionUtil.getFieldValue(object, ReflectionUtil.getField(cls, bFieldDesc.getFieldName()));
            if (value == null) {
                lineValues.add("");
            } else {
                String fieldType = bFieldDesc.getFieldType();

                switch (toInt(fieldType)) {
                    case 0 : {
                        lineValues.add(DateUtil.formatDate((Date)value));
                        break;
                    }
                    case 1 : {
                        lineValues.add(value+"");
                        break;
                    }
                    case 2 : {
                        if (bFieldDesc.getValues() != null && bFieldDesc.getValues().size() > 0) {
                            lineValues.add(bFieldDesc.getDicValue(((Integer)value)));
                        } else
                            lineValues.add(value+"");
                        break;
                    }
                    case 3 : {
                        lineValues.add(value+"");
                        break;
                    }
                    case 4 : {
                        lineValues.add(value+"");
                        break;
                    }

                    default:{
                        lineValues.add(value+"");
                    }
                }
            }

        }
        return lineValues;
    }




    public List queryObjects(String clsName, HashMap filter, Integer start, Integer end, String sessionKey) throws Throwable {
        List<BObject> resultList = null;
        try {
            resultList = crudSupport.queryObjects(clsName, filter, start, end,sessionKey);

            for(BObject obj : resultList){
                BFieldParser.parseBObject(obj);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }


        return resultList;
    }




    @RequestMapping(value="queryObjectsCount")
    public @ResponseBody
    Integer queryObjectsCount(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String javaClassName = request.getParameter("javaClassName");

        BObjectPlugin plugin = findPlugin(javaClassName);
        if (plugin != null) {
            Object obj = plugin.interceptQueryCount(request,response);
            if (obj != null) return (Integer)obj;
        }

        String ql = getSearchTxtQL(request);
        if (ql != null) {
            return (int) JpaServerUtil.getInstance().findObjectsCount(ql.replace("select c from","select count(c.id) from"));
        }

        return queryObjectsCount(javaClassName,extractFilterMap(request), null);
    }


    @RequestMapping(value="queryObjectsCountK")
    public @ResponseBody
    Integer queryObjectsCount(String clsName, HashMap filter, String sessionKey) throws Throwable {
//        List l = crudSupport.queryObjects(clsName, filter, null, null);
//        return l == null ? 0 : l.size();
        try {
            return crudSupport.queryObjectsCount(clsName,filter,sessionKey);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    @RequestMapping(value="lazyLoad")
    public @ResponseBody
    BObject lazyLoad(BObject object) {
        try {
            if (crudSupport instanceof BObjectHelper)
                return ((BObjectHelper) crudSupport).lazyLoad(object);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @RequestMapping(value="saveBObject")
    public @ResponseBody
    BObject saveBObject(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String javaClassName = request.getParameter("javaClassName");
        Map<String,String> beanMap = extractSubMap(request.getParameterMap(),"bean");
        HashMap defaultCondition = extractSubMap(request.getParameterMap(), "defaultCondition");

        try {
            BObject object = (BObject)extract(beanMap, Class.forName(javaClassName));


            if (defaultCondition != null && defaultCondition.size() > 0) {
                Set<String> set = defaultCondition.keySet();
                for (String key : set) {
                    if (key.contains("[value]")) {
                        String value = ((String[]) defaultCondition.get(key))[0];
                        key = key.substring(0,key.indexOf("[value"));
                        ReflectionUtil.setFieldValue(object,key,value);
                    }
                }
            }

            boolean update = object.getId() != null;

            Object old = null;
            if (object != null && object.getId() != null) {
                old = JpaServerUtil.getInstance().findObjectById(object.getClass(), object.getId());
            }

            object = crudSupport.saveBObject(object);
            BObjectEvent event = new BObjectEvent(update ? BObjectEvent.UPDATE : BObjectEvent.ADD, object);


            if (update && object != null) {

                event.setObject2(old);
            }
            applyPluginEvent(request,javaClassName, event);
            return object;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        } finally {

        }
        return null;
    }

    @RequestMapping(value="newBObject")
    public @ResponseBody
    BObject newBObject(BObject object) throws Throwable {
        try {
            object.setId(null);
            applyPluginEvent(null,object.getClass().getName(),new BObjectEvent(BObjectEvent.ADD,object));
            return crudSupport.saveBObject(object);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            //SmartODNSLogUtil.getInstance().bObjectCUD(FlexSessionUtil.getCurrentOperator(),object, SmartODNSLogUtil.CUDType.create);

        }
    }

    @RequestMapping(value="deleteBObject")
    public @ResponseBody
    String deleteBObject(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String bid = request.getParameter("BOjectId");
        String javaClassName = request.getParameter("javaClassName");

        BObject object = (BObject)  crudSupport.getJpaClient().findObjectById(Class.forName(javaClassName), Long.parseLong(bid));
        try {
            crudSupport.deleteBObject(object);
            applyPluginEvent(request,javaClassName,new BObjectEvent(BObjectEvent.DELETE,object));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return "{\"result\":\"success\"}";
    }

    private void applyPluginEvent(HttpServletRequest request, String javaClassName, BObjectEvent event) {
        BObjectPlugin plugin = findPlugin(javaClassName);
        if (plugin != null) {
            plugin.onEvent(new RequestContext(request), event);
        } else if (defaultPlugin != null) {
            defaultPlugin.onEvent(new RequestContext(request), event);
        }
    }

    @RequestMapping(value="deleteBObjects")
    public @ResponseBody
    String deleteBObjects(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        String bid = request.getParameter("BOjectIds");
        String javaClassName = request.getParameter("javaClassName");
        try {
            JpaClient.getInstance().executeUpdateSQL("delete from " + javaClassName + " c where c.id in (" + bid + ")");
            applyPluginEvent(request,javaClassName,new BObjectEvent(BObjectEvent.DELETE,bid));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
//        BObject object = (BObject)JpaServerUtil.getInstance().findObjectById(Class.forName(javaClassName), Long.parseLong(bid));
//        try {
//            crudSupport.deleteBObject(object);
//        } catch (Throwable e) {
//            logger.error(e.getMessage(), e);
//        }
        return "success";
    }

//    @RequestMapping(value="deleteBObjects")
//    public @ResponseBody void deleteBObjects(List<BObject> objects) throws Exception {
//        if (objects!=null) {
//            for (int i=0;i<objects.size();i++) {
//                BObject bo=objects.get(i);
//                try {
//                    deleteBObject(bo);
//                } catch (Throwable e) {
//                    logger.error(e.getMessage(), e);
//                }
//            }
//
//        }
//
//    }



}
