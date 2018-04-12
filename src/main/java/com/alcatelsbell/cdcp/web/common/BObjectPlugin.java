package com.alcatelsbell.cdcp.web.common;

import com.alcatelsbell.cdcp.web.WebContext;
import com.alcatelsbell.cdcp.web.controller.AbstractAjaxController;
import com.alcatelsbell.nms.security.LoginInfo;
import com.alcatelsbell.nms.valueobject.domain.Permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/10/19
 * Time: 8:27
 * rongrong.chen@alcatel-sbell.com.cn
 */
public abstract class BObjectPlugin {
    private Logger logger = LoggerFactory.getLogger(BObjectPlugin.class);
    private BObjectHelper crudHelper = null;
    private WebContext webContext = null;

    public WebContext getWebContext() {
        return webContext;
    }

    public void setWebContext(WebContext webContext) {
        this.webContext = webContext;
    }

    public BObjectHelper getCrudHelper() {
        return crudHelper;
    }

    public void setCrudHelper(BObjectHelper crudHelper) {
        this.crudHelper = crudHelper;
    }

    public abstract Class getJavaClass();

    public abstract void onEvent(RequestContext context,BObjectEvent event);
    public Object interceptQuery(HttpServletRequest request, HttpServletResponse response) {
        HashMap extend = AbstractAjaxController.extractSubMap(request.getParameterMap(), "extend");
        if (extend != null && extend.size() > 0) {
            String txt = ((String[]) extend.get("searchTxt"))[0];
            if (txt != null && txt.trim().length() > 0) {
                if (this instanceof BObjectPluginTxtSearchSupport) {
                    Integer start = AbstractAjaxController.extractInt(request, "start");
                    Integer limit = AbstractAjaxController.extractInt(request, "limit");
                    return ((BObjectPluginTxtSearchSupport)this).search(txt,start,limit,request,response);
                }

            }
        }
        return null;
    }

    public Object interceptQueryCount(HttpServletRequest request, HttpServletResponse response) {
        HashMap extend = AbstractAjaxController.extractSubMap(request.getParameterMap(), "extend");
        if (extend != null && extend.size() > 0) {
            String txt = ((String[]) extend.get("searchTxt"))[0];
            if (txt != null && txt.trim().length() > 0) {
                if (this instanceof BObjectPluginTxtSearchSupport) {
                    return ((BObjectPluginTxtSearchSupport)this).searchCount(txt,request,response);
                }

            }
        }
        return null;
    }







    protected List<Long> getRegionValueList(HttpServletRequest request)  {
        String sessionid = request.getParameter("sessionid");


        if (sessionid == null|| sessionid.isEmpty()) return null;


        LoginInfo loginInfo = getWebContext().getLoginInfoMap().get(sessionid);
        if (loginInfo == null || loginInfo.getOperator() == null) {
            List ids = new ArrayList();
            ids.add(-1);
            return ids;
        }
        List<Permission> permissions = (List)loginInfo.getOperator().getUserObject();
        List<Long> ids = new ArrayList();
        if (permissions != null) {
            for (Permission permission : permissions) {

                if ("Region".equals(permission.getAppmodule())) {
                    long regionValue = permission.getAppliedProject();
                    ids.add(regionValue);
                }
                if ("Admin".equalsIgnoreCase(permission.getTargetKey())) {
                    return null;
                }

            }
        }

        return ids;
    }

    protected boolean isAdmin(HttpServletRequest request)  {
        String sessionid = request.getParameter("sessionid");


        if (sessionid == null|| sessionid.isEmpty()) return false;


        LoginInfo loginInfo = getWebContext().getLoginInfoMap().get(sessionid);
        if (loginInfo == null || loginInfo.getOperator() == null) {
            List ids = new ArrayList();
            ids.add(-1);
            return false;
        }
        List<Permission> permissions = (List)loginInfo.getOperator().getUserObject();
        List<Long> ids = new ArrayList();
        if (permissions != null) {
            for (Permission permission : permissions) {
                if ("Admin".equalsIgnoreCase(permission.getTargetKey())) {
                    return true;
                }

            }
        }

        return false;
    }



    protected List<Long> getCustomerIdList(HttpServletRequest request)  {
        String sessionid = request.getParameter("sessionid");


        if (sessionid == null|| sessionid.isEmpty()) return null;


        LoginInfo loginInfo = getWebContext().getLoginInfoMap().get(sessionid);
        if (loginInfo == null || loginInfo.getOperator() == null) {
            List ids = new ArrayList();
            ids.add(-1);
            return ids;
        }
        List<Permission> permissions = (List)loginInfo.getOperator().getUserObject();
        List<Long> ids = new ArrayList();
        if (permissions != null) {
            for (Permission permission : permissions) {
                if ("R_Customer".equals(permission.getAppmodule())) {
                    long customerId = permission.getAppliedProject();
                    ids.add(customerId);
                }
//                if ("Region".equals(permission.getAppmodule())) {
//                    long regionValue = permission.getAppliedProject();
//                    ids.add(customerId);
//                }
                if ("Admin".equalsIgnoreCase(permission.getTargetKey())) {
                    return null;
                }

            }
        }

        return ids;
    }

    protected String getRegionValueInString(HttpServletRequest request) {
        List<Long> regionValueList = getRegionValueList(request);
        if (regionValueList != null && regionValueList.size() > 0) {
            String sb = "(-1";
            for (Long aLong : regionValueList) {
                sb += ","+aLong;
            }
            return sb+")";
        } else {
            return null;
        }
    }

    protected String getCustomerIdString(HttpServletRequest request)  {
        String sessionid = request.getParameter("sessionid");
        LoginInfo loginInfo = getWebContext().getLoginInfoMap().get(sessionid);
        List<Permission> permissions = (List)loginInfo.getOperator().getUserObject();
        StringBuffer sb = new StringBuffer("(-1");
        if (permissions != null) {
            for (Permission permission : permissions) {
                if ("R_Customer".equals(permission.getAppmodule())) {
                    long customerId = permission.getAppliedProject();
                    sb.append(",").append(customerId);
                }
                if ("Admin".equalsIgnoreCase(permission.getTargetKey())) {
                    return null;
                }

            }
        }
        sb.append(") ");
        return sb.toString();
    }

}
