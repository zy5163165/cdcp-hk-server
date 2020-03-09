package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.cdcp.api.TaskMgmtClient;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.modules.task.model.Schedule;
import com.alcatelsbell.nms.util.NamingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-17
 * Time: 下午2:16
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CommandHandler  {
    private Log logger = LogFactory.getLog(getClass());
    public String handleCommand(String... commands ) throws RemoteException {
        String command = commands[0];
        if (command.equals("?") || command.equals("help")) {
            Console.println("ls");
            Console.println("pwd");
            Console.println("list");
            Console.println("status / status [name]");
            Console.println("startAll");
            Console.println("start [name]");
            Console.println("stopAll");
            Console.println("stop [name]");
            Console.println("cd [containerDir]");
            Console.println("exit");

        }

        if (command.equals("schedule ")) {
            handleSchedule(commands);
        }
        return "";
    }

    private void handleSchedule(String[] commands) {
        Schedule schedule = new Schedule();
        schedule.setJobType("XML");
        schedule.setArguments(commands[0]);
        Schedule schedule1 = null;
        try {
            schedule1 = TaskMgmtClient.getInstance().createSchedule(schedule);
            Console.println(schedule1.getJobName());
        } catch (RemoteException e) {
            Console.println("任务失败...");
        }

    }

    public static void main(String[] args) {
        new CommandHandler().handleSchedule(new String[]{"snmp-device-schedule1.xml"});
    }
}
