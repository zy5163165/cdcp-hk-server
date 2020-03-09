package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.cdcp.api.EmsMgmtClient;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-9-22
 * Time: 下午2:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SyncEmsClient {
    public static void main(String[] args) throws  Exception {
        if (args == null || args.length == 0) {
            List<Ems> allObjects = JpaClient.getInstance().findObjects("select c from Ems c order by c.id desc");

            for (int i = 0; i < allObjects.size(); i++) {
                Ems o = allObjects.get(i);
                System.out.print(o.getDn()+"      ");
                if (i % 5 == 0)
                    System.out.println();
            }
            return;

        }
        String arg = args[0];
        String[] split = arg.split(",");
        for (String ems : split) {
            String s = EmsMgmtClient.getInstance().manualSyncEms(args[0]);
            System.out.println(ems+" : "+s);
        }

    }
}
