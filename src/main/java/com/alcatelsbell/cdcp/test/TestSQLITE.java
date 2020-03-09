package com.alcatelsbell.cdcp.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-19
 * Time: 上午10:47
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestSQLITE {
    private Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
      //  org.sqlite.Conn co = (Conn)conn;
        conn.createStatement().execute("create database abc");
        conn.createStatement().execute("use abc");

        String create_sql = "";


        String sql = "";
    }
}
