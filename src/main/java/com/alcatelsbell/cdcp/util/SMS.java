package com.alcatelsbell.cdcp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-10-1
 * Time: 下午9:25
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SMS implements Comparable<SMS>{
    private String time = "";
    private String number;
    private String txt;

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("d:\\works\\received.cah");
      //  File file = new File("d:\\works\\sms.cah");
        InputStreamReader fr = new InputStreamReader(fis,"gb2312");
        BufferedReader br = new BufferedReader(fr);
        List<SMS> smsList = new ArrayList<SMS>();
        SMS current = null;
        while (true) {
            String s = br.readLine();
            if (s == null) break;
        //    System.out.println(s);
            String[] split = s.split("\u0000");
            for (String s1 : split) {
                if (s1 != null && !s1.trim().isEmpty()) {
                    s1 = s1.trim();
                    if (s1.contains("200")) {
                        if (current != null)
                            smsList.add(current);
                        current = new SMS();
                        current.time = s1;
                    } else {
                        if (current != null) {
                            if (current.txt == null) current.txt = "";
                            current.txt = current.txt+s1+" ";
                        }
                    }
                    //System.out.println(s1);
                }
            }
            System.out.println(split.length);
        }
        Collections.sort(smsList);
        for (SMS sms : smsList) {
            System.out.println("time="+sms.time);
            System.out.println("" + sms.txt);
            System.out.println("-----------------------------------------------------------");
        }
    }

    @Override
    public int compareTo(SMS o) {
        return time.compareTo(o.time);
    }
}
