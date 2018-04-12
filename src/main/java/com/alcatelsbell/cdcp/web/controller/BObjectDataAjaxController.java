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
@RequestMapping("/bodata/*")
public class BObjectDataAjaxController extends BObjectAjaxController {
    private Logger logger = LoggerFactory.getLogger(BObjectDataAjaxController.class);


    public BObjectDataAjaxController() {
        crudSupport = new BObjectHelper("cdcp.datajpa");
    }



}
