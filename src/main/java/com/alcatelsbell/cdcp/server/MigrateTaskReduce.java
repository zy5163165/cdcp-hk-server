package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.nms.corba.service.Serial;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Ronnie.Chen
 * Date: 2015/4/23
 * Time: 10:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrateTaskReduce {
    public String execute() {
        MigrateManager instance = MigrateManager.getInstance();
        LinkedBlockingQueue queue = instance.getQueue();
        Object[] tasks = queue.toArray();
        StringBuffer sb = new StringBuffer();
        sb.append("init queue size  = "+ queue.size()) ;
        HashMap<String,MigrateRunnable> serials = new HashMap<String, MigrateRunnable>();
        int count = 0;
        for (int i = tasks.length-1; i >=0; i--) {
            MigrateRunnable task = (MigrateRunnable)tasks[i];
            String serial = task.getSerial();
            if (serial.contains("ManagedElement"))
                continue;
          //  String emsdn = serial.substring(0,serial.indexOf("@"));

      //      if (serials.get(emsdn) != null) {
                queue.remove(task);
                count ++;
                sb.append(serial+" removed <br>");
       //     }
         //   serials.put(emsdn,task);
        }
        sb.append(count+++" tasks  removed ");
        sb.append("queue size  = "+ queue.size()) ;
        return sb.toString();

    }
}
