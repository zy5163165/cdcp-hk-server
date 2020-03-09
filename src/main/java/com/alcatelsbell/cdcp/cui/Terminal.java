package com.alcatelsbell.cdcp.cui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Author: Ronnie.Chen
 * Date: 13-9-17
 * Time: 上午11:21
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Terminal {
    private Log logger = LogFactory.getLog(getClass());
    public static final String prompt = "<>";
    public static void main(String[] args) throws IOException {
        Console.println("**************************************************");
        Console.println("*                                                *");
        Console.println("*              CDCP Server Console 0.1           *");
        Console.println("*             Press ? for command list           *");
        Console.println("*                                                *");
        Console.println("**************************************************");
        Console.print(prompt);
        CommandHandler ifc = new CommandHandler();
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();
            s = s.trim();
            String[] split = s.split(" ");
            if (split.length > 0)
                ifc.handleCommand(split);
            Console.print(prompt);
        }
    }
}
