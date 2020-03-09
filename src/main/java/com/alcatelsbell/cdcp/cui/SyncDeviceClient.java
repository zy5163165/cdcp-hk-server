package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.cdcp.api.EmsMgmtClient;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2015/4/9
 * Time: 15:19
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SyncDeviceClient {
    public static void main(String[] args) throws  Exception {
        if (args == null || args.length == 0) {
            BufferedReader br = new BufferedReader(new FileReader("sync-nes.txt"));
            while (true) {
                String s = br.readLine();
                if (s == null) break;

                String nedn = s.trim();

                String task = null;
                try {
                    task = EmsMgmtClient.getInstance().getIFC().manualSyncDevice(nedn);
                } catch (RemoteException e) {
                    System.err.println("Failed ! "+nedn+" : "+e.getMessage());
                }
                System.out.println("Success : Task = "+task);
            }
            return;

        }
        else {
            String nedn = args[0].trim();
            String task = null;
            try {
                task = EmsMgmtClient.getInstance().getIFC().manualSyncDevice(nedn);
            } catch (RemoteException e) {
                System.err.println("Failed ! "+nedn+" : "+e.getMessage());
            }
            System.out.println("Success : Task = "+task);
        }
    }
}
