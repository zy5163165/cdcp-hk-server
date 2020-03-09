package com.alcatelsbell.cdcp.web.controller;

import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.cdcp.nbi.model.relationship.RCDeviceVDevice;
import com.alcatelsbell.cdcp.nbi.model.virtualentity.VDevice;
import com.alcatelsbell.cdcp.server.VDeviceClient;
import com.alcatelsbell.cdcp.web.model.QueryResult;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.util.mail.MailUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/15
 * Time: 20:34
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Controller
@RequestMapping("/api/*")
public class CdcpAjaxController extends AbstractAjaxController {
 //   private FileSystem fileSystem = FileSystem.getFileSystem("fs");
  //  private JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();

//    @RequestMapping(value="insert",method= RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody
//    PM_DATA insert(@RequestBody PM_DATA pm_data) throws IOException {
//        pm_data.setId(111l);
//        return pm_data;
//    }

    public CdcpAjaxController() {
        try {
            VDeviceClient.scan();
        } catch (Exception e) {
            logger.error(e, e);
        }
    }


    @RequestMapping(value="sendMail")
    public @ResponseBody
    HashMap queryEntity(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String toAddress = request.getParameter("toAddress");
        if (toAddress == null)
            toAddress = "4278246@qq.com;rongrong.chen@alcatel-sbell.com.cn";
        String title = request.getParameter("title");
        String body = request.getParameter("body");
   //     MailUtil.sendDirectMail(title, body, toAddress.split(";"));
        HashMap map = new HashMap();
        map.put("result","success");
        return map;
    }

    @RequestMapping(value="queryAllEms")
    public @ResponseBody
    QueryResult queryAllEms(HttpServletRequest request, HttpServletResponse response) throws IOException{
        try {
            Integer page = extractInt(request,"page");
            Integer pageSize = extractInt(request,"pageSize");
            List<Ems> allEms = JpaServerUtil.getInstance().findObjects("select c from Ems c",null,null,page == null ? null : (page-1)*pageSize,pageSize == null ? null : pageSize);
            int total = (int)JpaServerUtil.getInstance().findObjectsCount("SELECT count(c.id) FROM Ems c");
            return new QueryResult(total,10,10,allEms);
        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }
    private JpaClient dataClient = JpaClient.getInstance("cdcp.datajpa");

    @RequestMapping(value="queryEms")
    public @ResponseBody
    List queryEms(HttpServletRequest request, HttpServletResponse response) throws IOException{
        try {
            String input = request.getParameter("input");

            List list = JpaServerUtil.getInstance().findObjects("select c from Ems c where c.dn like '%"+input+"%' or c.name like '%"+input+"%' ");
            if (list.size() > 20) list = list.subList(0,20);
            logger.info(input+" ,list="+list.size());
            return list;
         //   dataClient.findObjects("select c from ")

        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }

    @RequestMapping(value="saveCV")
    public @ResponseBody
    VDevice saveCDeviceVDevice(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String bean = request.getParameter("bean");
        JSONObject json = JSONObject.fromObject(bean);
        String rEms = json.getString("cDeviceEMSName");

        RCDeviceVDevice rcv = new RCDeviceVDevice();
        try {
            String rDeviceDn = json.getString("cDeviceDn");
            CDevice rDevice = (CDevice) dataClient.findObjectByDN(CDevice.class, rDeviceDn);
            String rDeviceName = rDevice.getNativeEmsName();


            String vDeviceDn = json.getString("vDeviceDn");
            CDevice vDevice = (CDevice) dataClient.findObjectByDN(CDevice.class, vDeviceDn);
            String vDeviceName = vDevice.getNativeEmsName();
            return VDeviceClient.createVDevice(rDeviceDn,rDevice.getNativeEmsName(),
                    rDevice.getEmsName(),
                    vDeviceDn,vDevice.getNativeEmsName(),
                    vDevice.getEmsName());


        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;



    }


        @RequestMapping(value="queryDevice")
    public @ResponseBody
    List queryDevice(HttpServletRequest request, HttpServletResponse response) throws IOException{
        try {
            String input = request.getParameter("input");
            String ems = request.getParameter("ems");

            String strSql = "select c from CDevice c where 1=1 ";
            if (ems != null && ems.length() > 0)
                strSql += " and c.emsName = '" + ems + "'";
            strSql+=" and (c.dn like '%" + input + "%' or c.nativeEmsName like '%" + input + "%' )";
            List list = dataClient.findObjects(strSql,null,null,0,20);
          //  if (list.size() > 20) list = list.subList(0,20);
            return list;
            //   dataClient.findObjects("select c from ")

        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }








}
