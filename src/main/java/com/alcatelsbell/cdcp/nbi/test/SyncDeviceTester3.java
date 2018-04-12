package com.alcatelsbell.cdcp.nbi.test;

import com.alcatelsbell.cdcp.api.TaskMgmtIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.util.FileUtil;
import com.alcatelsbell.nms.util.NamingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

/**
 * Author: Ronnie.Chen
 * Date: 2014/12/22
 * Time: 15:17
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SyncDeviceTester3 {
    public static void main(String[] args) throws RemoteException, UnsupportedEncodingException {

        TaskMgmtIFC task = (TaskMgmtIFC) NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_TASK);
        String request = new String(FileUtil.readFile(new File("nbi.xml")),"utf-8");
        String[] split = request.split("<strRequest>");
        for (String s : split) {
            if (s.contains("</strRequest>")) {
                s =  "<strRequest>" + s.substring(0,s.indexOf("</strRequest>")+"</strRequest>".length());
                System.out.println(s);
                try {
                String result = task.synchronizeNeInfoInEms(s);
                System.out.println("result = " + result);

                    Thread.sleep(10000l);
                } catch (InterruptedException e) {

                }
            }
        }

    //    System.out.println("request = " + request);
        //     String s = task.synchronizeNeInfoInEms(request);
        //     System.out.println("s = " + s);

    }}
