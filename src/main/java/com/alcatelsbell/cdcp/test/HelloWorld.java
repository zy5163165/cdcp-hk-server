package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.cdcp.nodefx.EmsSBIClassloader;
import com.alcatelsbell.cdcp.server.MigrateManager;
import com.alcatelsbell.cdcp.server.MigrateRunnable;
import com.alcatelsbell.hippo.framework.naming.NamingService;
import com.alcatelsbell.nms.util.SysProperty;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-6
 * Time: 上午10:10
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HelloWorld {
    public static void main(String[] args) throws Exception {
        System.out.println(SysProperty.getString("cdcp.irm.db.url"));

        URL resource = HelloWorld.class.getClassLoader().getResource("com/alcatelsbell/hippo/framework/naming/NamingService.class");
        System.out.println(resource);
        com.alcatelsbell.hippo.framework.naming.NamingService ss = new NamingService();
        com.alcatelsbell.hippo.framework.naming.NamingService service = new NamingService();
        System.out.println("service = " + service);
        //CaseObject object = new CaseObject();
        while (true) {
            Random random = new Random();
            execute(random.nextInt(4000));

            //object.execute(random.nextInt(4000));
        }



    }
    public void m3() {
        System.out.println("m3");
    }
    public static Integer execute(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
        }
        System.out.println("sleep time is=>"+sleepTime);
        return 0;
    }

    public void run() {
//        EmsSBIClassloader classloader = new EmsSBIClassloader(getClass().getClassLoader());
//        classloader.addFile(new File("../lib-nex/nex.jar"));
//        Object object  = classloader.loadClass("com.alcatelsbell.cdcp.server.MigrateTaskReduce").newInstance();
//        return object.getClass().getMethod("execute").invoke();
    }
}