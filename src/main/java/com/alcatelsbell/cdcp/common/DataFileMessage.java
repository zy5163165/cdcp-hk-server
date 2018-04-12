package com.alcatelsbell.cdcp.common;

import com.alcatelsbell.cdcp.nodefx.FtpInfo;

import java.io.Serializable;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-2
 * Time: 下午2:22
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DataFileMessage implements Serializable {
    public static final int MESSAGE_TYPE_UPLOAD_DATAFILE = 0;
    public static final int MESSAGE_TYPE_DOWNLOAD_DATAFILE = 1;
    private int messageType;
    private FtpInfo ftpInfo = null;
    public DataFileMessage(int messageType, FtpInfo ftpInfo) {
        this.messageType = messageType;
        this.ftpInfo = ftpInfo;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public FtpInfo getFtpInfo() {
        return ftpInfo;
    }

    public void setFtpInfo(FtpInfo ftpInfo) {
        this.ftpInfo = ftpInfo;
    }
}
