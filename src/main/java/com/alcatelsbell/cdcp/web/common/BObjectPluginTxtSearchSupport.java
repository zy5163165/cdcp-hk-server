package com.alcatelsbell.cdcp.web.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/10/20
 * Time: 9:20
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface BObjectPluginTxtSearchSupport {
    public List search(String txt, Integer from, Integer limit, HttpServletRequest request, HttpServletResponse response);
    public   int searchCount(String txt, HttpServletRequest request, HttpServletResponse response);
}
