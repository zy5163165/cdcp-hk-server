package com.alcatelsbell.cdcp.web.controller;


import com.alcatelsbell.cdcp.util.ReflectionUtil;
import com.alcatelsbell.cdcp.web.*;
import com.alcatelsbell.cdcp.web.common.*;
import com.alcatelsbell.nms.publics.slog.SLogUtil;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/16
 * Time: 14:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class AbstractAjaxController {
    protected static Log logger = LogFactory.getLog(AbstractAjaxController.class);



    @Autowired
    private WebContext webContext = null;

    public WebContext getWebContext() {
        return webContext;
    }

    public void setWebContext(WebContext webContext) {
        this.webContext = webContext;
    }

    protected void slog(String user, String ipaddress, String operation) {
        SLogUtil.createLog("SECURITY",operation,operation,"SECURITY",ipaddress,user,"WEB","",true);
    }

    public static Long extractLong(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        if (value != null && !value.isEmpty()) {
           return Long.parseLong(value);
        }
        return null;
    }
    public static Integer extractInt(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.error(e, e);
            }
        }
        return null;
    }

    public static HashMap extractSubMap(Map map, String name) {
        HashMap subMap = new HashMap();
        Set<String> set = map.keySet();
        for (String key : set) {
            if (key.startsWith(name+"[")) {
                int start = key.indexOf("[") + 1;
                int endIndex = key.indexOf("]", start);
                String subKey = key.substring(start, endIndex);
                if (endIndex < key.length()-1)
                    subKey += key.substring(endIndex+1);
                subMap.put(subKey,map.get(key));
            }
        }
        return subMap;
    }

    public static List<HashMap> extractSubList(Map map, String name) {
        HashMap subMap = new HashMap();
        Set<String> set = map.keySet();
        List<HashMap> list = new ArrayList();
        for (String key : set) {
            if (key.startsWith(name+"[")) {
                int start = key.indexOf("[") + 1;
                int endIndex = key.indexOf("]", start);
                String subKey = key.substring(start, endIndex);
                int idx = Integer.parseInt(subKey);
                if (endIndex < key.length()-1)
                    subKey += key.substring(endIndex+1);



                int s2 = key.indexOf("[", endIndex)+1;
                int e2 = key.indexOf("]", s2);
                String key2 = key.substring(s2,e2);

                if (e2 < key.length() -1)
                    key2 += key.substring(e2+1);

                while (list.size() < idx +1) {
                    list.add(new HashMap());
                }

                list.get(idx).put(key2,map.get(key));


            }
        }
        return list;
    }

    public static Object extract(HttpServletRequest request, Class cls) {
        return extract(request.getParameterMap(),cls);
    }

    public static Object extract(Map map, Class cls) {
        Iterator<String> parameterNames = map.keySet().iterator();
        Object obj = null;
        try {
            obj = cls.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        while (parameterNames.hasNext()) {
            String key = parameterNames.next();
            Object o = map.get(key);
            String para = o instanceof String[] ? ((String[])o)[0] : o.toString();
            Object value = null;

            if (para != null) {
                Field field = ReflectionUtils.findField(cls, key);
                if (field != null && !para.trim().isEmpty()) {
                    if (field.getType().equals(Byte.class) || field.getType().equals(byte.class)) {
                        value = Byte.parseByte(para);
                    }else if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
                        value = Integer.parseInt(para);
                    } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                        value = Long.parseLong(para);
                    }  else if (field.getType().equals(Float.class) || field.getType().equals(float.class)) {
                        value = Float.parseFloat(para);
                    } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
                        value = Double.parseDouble(para);
                    } else if (field.getType().equals(Date.class)) {
                        if (!para.trim().isEmpty()) {
                            System.out.println("date para  = " + para);
                            if (para.trim().length() == "yyyy-MM-dd HH:mm:ss".length()) {
                                try {
                                    value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(para);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }

                            else if (para.trim().length() == "yyyyMMddHHmmss".length()) {
                                try {
                                    value = new SimpleDateFormat("yyyyMMddHHmmss").parse(para);
                                } catch (ParseException e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }

                            else {
                                try {
                                    value = new Date(Long.parseLong(para));
                                } catch (NumberFormatException e2) {
                                    throw new RuntimeException(e2);
                                    //    logger.error(e1, e1);
                                }
                            }
                            System.out.println("value = " + value);
                        }
                    } else {
                        value = para;
                    }
                    try {
                        ReflectionUtil.setFieldValue(obj,field, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return obj;
    }

//    @RequestMapping(value="queryPMP",method= RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody QueryResult queryPmParas(@RequestBody QueryCondition queryCondition){
//        int currentPage = queryCondition.getCurrentPage();
//        int pageSize = queryCondition.getPageSize();
//        int start = pageSize * (currentPage - 1);
//        int limit = pageSize;
//
//        String sql = "SELECT * FROM PM_PARAMS";
//        if (limit > 0) {
//            sql = SqlUtil.getPagerSQL(sql,start,limit);
//        }
//        List list = null;
//        try {
//            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PM_PARAMS.class, sql);
//        } catch (Exception e) {
//            logger.error(e, e);
//            throw new RuntimeException(e);
//        }
//        List<PmParaData> datas = new ArrayList();
//        for (Object o : list) {
//            datas.add(new PmParaData((PM_PARAMS)o));
//        }
//        QueryResult queryResult = new QueryResult(pageSize,currentPage,datas);
//        return queryResult;
//    }

    /**
     *
     * @param value   [abc][dff]
     * @return
     */
    public static List<String> toList(String value) {
        Matcher m = Pattern.compile("\\[([^\\[\\]]+)\\]").matcher(value);
        List<String> list = new ArrayList();
        while (m.find()) {
            String group = m.group();
            list.add(group.substring(1,group.length()-1));
        }
        return list;
    }


    public static HashMap extractFilterMap(HttpServletRequest request) throws UnsupportedEncodingException {
        HashMap filters = new HashMap();

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            if (name.startsWith("filter[")) {
                String value = request.getParameter(name);

                value = URLDecoder.decode(value,"utf-8");

                name = name.substring("filter".length());
                List<String> list = toList(name);
                if (list.size() == 2) {
                    String key = list.get(0);
                    if (key.contains("%")) {
                        String k = key.substring(0,key.indexOf("%"));
                        TimePeriod period = (TimePeriod)filters.get(k);
                        if (period == null) {
                            period = new TimePeriod();
                            filters.put(k,period);
                        }
                        if (key.endsWith("%from")) {
                            try {
                                period.setFromDate(new SimpleDateFormat("yyyyMMddHHmmss").parse(value));
                            } catch (ParseException e) {
                                logger.error(e.getMessage(),e);
                            }
                        }
                        if (key.endsWith("%to")) {
                            try {
                                period.setToDate(new SimpleDateFormat("yyyyMMddHHmmss").parse(value));
                            } catch (ParseException e) {
                                logger.error(e.getMessage(),e);
                            }
                        }


                    } else if (key.contains("*")) {
                        if (list.get(1).equals("checked") && value.equals("true")) {
                            String k = key.substring(0,key.indexOf("*"));
                            List valueList = (List)filters.get(k);
                            if (valueList == null) {
                                valueList = new ArrayList();
                                filters.put(k,valueList);
                            }

                            valueList.add(Integer.parseInt(key.substring(key.indexOf("*")+1)));
                        }
                    } else {
                        filters.put(key,value);
                    }
                }


            }
        }
        String sort = request.getParameter("sort");
        String sortType = request.getParameter("sortType");
        if (sort != null && sort.trim().length() > 0) {
            filters.put("#"+sort,sortType == null ? "" : sortType);
        }

        return filters;
    }




    public static String getExtendSearchTxt(HttpServletRequest request) {
        HashMap extend = AbstractAjaxController.extractSubMap(request.getParameterMap(), "extend");
        if (extend != null && extend.size() > 0) {
            String txt = ((String[]) extend.get("searchTxt"))[0];
            if (txt != null && txt.trim().length() > 0) {
                return txt;
            }
        }
        return null;
    }


    private HashMap<String,CancelableImporterTask> cancelableTasks = new HashMap();

    protected void addCancelableTask(CancelableImporterTask task) {
        cancelableTasks.put(task.getGroup(),task);
    }

    protected void removeCancelableTask(String group) {
        cancelableTasks.remove(group);
    }

    public HashMap cancelUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HashMap map = new HashMap();
        String group = request.getParameter("group");
        CancelableImporterTask cancelableImporterTask = cancelableTasks.get(group);
        if (cancelableImporterTask != null) {
            cancelableImporterTask.cancel();
            map.put("result","true");
        }
        else
            map.put("result","false");
        return map;
    }

 
}
