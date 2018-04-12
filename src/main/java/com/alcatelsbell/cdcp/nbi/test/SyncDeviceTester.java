package com.alcatelsbell.cdcp.nbi.test;

import com.alcatelsbell.cdcp.api.TaskMgmtIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.util.NamingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-1
 * Time: 下午4:37
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SyncDeviceTester {
    private Log logger = LogFactory.getLog(getClass());
    public static void main(String[] args) throws RemoteException {
        String emsName = null;
        String neDn = null;
        if (args.length >= 2) {
                   emsName = args[0];
            neDn = args[1];
            System.out.println("neDn = " + neDn);
            System.out.println("emsName = " + emsName);
        }   else {
            System.out.println(" args is empty ,please input emsname and nedn");
            return;
        }
        TaskMgmtIFC task = (TaskMgmtIFC) NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_TASK);
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<strRequest>\n" +
                "\t\n" +
                "\t<summary>\n" +
                "\t\t<fieldInfo>\n" +
                "\t\t\t<fieldChName>工程名称</fieldChName>\n" +
                "\t\t\t<fieldEnName>projectName</fieldEnName>\n" +
                "\t\t\t<fieldContent>"+ SysUtil.nextLongId()+"</fieldContent>\n" +
                "\t\t</fieldInfo>\n" +
                "\t\t<fieldInfo>\n" +
                "\t\t\t<fieldChName>EMS名称</fieldChName>\n" +
                "\t\t\t<fieldEnName>emsName</fieldEnName>\n" +
                "\t\t\t<fieldContent>"+emsName+"</fieldContent>\n" +
                "\t\t</fieldInfo>\n" +
                "\t</summary>\n" +
                "\t\n" +
                "\t<recordInfo>\n" +
                "\t\t<fieldInfo>\n" +
                "\t\t\t<fieldChName>网元Dn</fieldChName>\n" +
                "\t\t\t<fieldEnName>neDn</fieldEnName>\n" +
                "\t\t\t<fieldContent>"+neDn+"</fieldContent>\n" +
                "\t\t</fieldInfo>\n" +
                "\t</recordInfo>\n" +
                "\n" +
                "\t\n" +
                "</strRequest>";
        System.out.println("request = " + request);
        String s = task.synchronizeNeInfoInEms(request);
        System.out.println("s = " + s);
    }
}
