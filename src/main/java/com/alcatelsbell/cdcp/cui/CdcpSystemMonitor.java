package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.util.mail.MailUtil;
import com.alcatelsbell.nms.valueobject.alarm.Alarminformation;
import com.alcatelsbell.nms.valueobject.sys.Ems;
//import tryworks.api.PmClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 12-11-5
 * Time: 下午4:42
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpSystemMonitor extends JFrame {
    private JTextArea logArea = new JTextArea(15,15);
    private JTextArea mailArea = new JTextArea(15,15);
    private JTextField mails = new JTextField(50);
    private JButton startBtn = new JButton("开始");
    private JButton processBtn = new JButton("我处理了");

    public CdcpSystemMonitor() {
        initUI();
        initAction();
    }

    private void initAction() {

//        startBtn.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
        startBtn.setEnabled(false);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                handleException("NMS邮件监控客户端启动！");
                while (true) {
                    check();
                    try {
                        Thread.sleep(10 * 60 * 1000);
                    } catch (InterruptedException e1) {

                    }
                }
            }
        };
        new Thread(r).start();
//            }
//        });

        processBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleException("已有人处理");

            }
        });
    }
    private Date latestSDHAlarmTime = null;
    private List<String> errorEmses = new ArrayList<String>();
    private int latestClearDay = -1;
    private void check() {
        Calendar instance = Calendar.getInstance();
        if (instance.get(Calendar.HOUR_OF_DAY) == 8 && instance.get(Calendar.DAY_OF_YEAR) != latestClearDay) {
             errorEmses.clear();
            latestClearDay =  instance.get(Calendar.DAY_OF_YEAR);
        }
        java.util.List<Ems> emsList = null;
        try {
            emsList = JpaClient.getInstance().findObjects("select c from Ems c where c.status != 0",null,null,0,10);


            List<Ems> errors = new ArrayList<Ems>();
            if (emsList != null && emsList.size() > 0 ){
                for (Ems ems : emsList) {
                    if (!errorEmses.contains(ems.getDn())) {
                        errors.add(ems);
                        errorEmses.add(ems.getDn());
                    }
                }
            }

            if (errors.size() > 0) {
                String s = "";
                for (Ems error : errors) {
                    s+=error.getDn()+";";
                }
                handleException("EMS异常:"+s);
            }



        } catch (Exception e) {
            logArea.append("Exception : ");
            handleException("严重！ 服务器连接失败！");
        }


    }
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private void handleException(String info) {
        mailArea.append(sdf.format(new Date())+" --- "+"发送邮件:"+info+"\n");
        String[] adds = mails.getText().split(";");
        try {
     //       PmClient.getInstance().sendMail(mails.getText(),info,info);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
     //   MailUtil.sendDirectMail(info, info, adds);
    }

    private void handleLog(String info) {
        logArea.append(sdf.format(new Date())+" --- "+info+"\n");
    }


    private void initUI() {
        JScrollPane jScrollPane1 = new JScrollPane(logArea);
        JScrollPane jScrollPane2 = new JScrollPane(mailArea);
        mails.setText("rongrong.chen@alcatel-sbell.com.cn;4278246@qq.com");
        JPanel panel = new JPanel();
        panel.add(new JLabel("邮件地址:"));
        panel.add(mails);
        panel.add(startBtn);
        panel.add(processBtn);
        this.getContentPane().add(jScrollPane1, BorderLayout.NORTH);
        this.getContentPane().add(panel, BorderLayout.SOUTH);
        this.getContentPane().add(jScrollPane2, BorderLayout.CENTER);

        this.pack();

    }

    public static void main(String[] args) {
        CdcpSystemMonitor systemMonitor = new CdcpSystemMonitor();
        systemMonitor.setVisible(true);

    }
}
