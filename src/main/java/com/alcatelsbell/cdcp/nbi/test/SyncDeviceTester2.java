package com.alcatelsbell.cdcp.nbi.test;

import com.alcatelsbell.cdcp.api.TaskMgmtIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.util.FileUtil;
import com.alcatelsbell.nms.util.NamingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

/**
 * Author: Ronnie.Chen
 * Date: 2014/11/13
 * Time: 13:09
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SyncDeviceTester2 {
    public static void main(String[] args) throws RemoteException, UnsupportedEncodingException {

        TaskMgmtIFC task = (TaskMgmtIFC) NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_TASK);
        String request = new String(FileUtil.readFile(new File("request.xml")),"utf-8");
        System.out.println("request = " + request);
        String s = task.synchronizeNeInfoInEms(request);
        System.out.println("s = " + s);

    }
}
