package com.alcatelsbell.cdcp.nbi.netty;

import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2016/7/26.
 */
public class EmsAlarmManager {
    private static EmsAlarmManager ourInstance = new EmsAlarmManager();

    public static EmsAlarmManager getInstance() {
        return ourInstance;
    }

    private EmsAlarmManager() {
    }


    private Hashtable<String,LinkedBlockingQueue> table = new Hashtable<String, LinkedBlockingQueue>();
    public void addEmsAlarm(String emsName,Object alarmData) {
        LinkedBlockingQueue queue = table.get(emsName);
        if (queue == null) {
            synchronized (table) {
                queue = table.get(emsName);
                if (queue == null) {
                    queue = new LinkedBlockingQueue();
                    table.put(emsName,queue);
                }
            }
        }

        queue.offer(alarmData);
    }
}
