package com.alcatelsbell.cdcp.server;

/**
 * Author: Ronnie.Chen
 * Date: 13-5-20
 * Time: 上午10:11
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FtpFileDownloadException extends Exception {
    public FtpFileDownloadException(String msg,Throwable e) {
        super(msg,e);
    }
}
