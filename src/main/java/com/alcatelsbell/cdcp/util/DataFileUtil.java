package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nodefx.CDCPConstants;
import com.alcatelsbell.cdcp.nodefx.FtpInfo;
import com.alcatelsbell.nms.interfaces.utils.FtpHelper;
import com.alcatelsbell.nms.util.SysProperty;
import com.alcatelsbell.nms.util.protocol.FtpFunc;
import com.alcatelsbell.nms.util.protocol.SFtpFunc;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import org.springframework.util.Assert;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-2
 * Time: 上午10:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DataFileUtil {
    private static String ftpHost = SysProperty.getString(CDCPConstants.SYSTEM_PROPERTY_FTP_HOST);
    private static String ftpUser = SysProperty.getString(CDCPConstants.SYSTEM_PROPERTY_FTP_USER);
    private static String ftpPassword = SysProperty.getString(CDCPConstants.SYSTEM_PROPERTY_FTP_PASSWORD);
    private static String ftpRemotePath = SysProperty.getString("ftpRemotePath");


//    private static String ftpHost = SysProperty.getString(Constants.SYSPRO_FTP_HOST);
//    private static String ftpUser = SysProperty.getString(Constants.SYSPRO_FTP_USER);
//    private static String ftpPassword = SysProperty.getString(Constants.SYSPRO_FTP_PASSWORD);
//    private static String ftpRemotePath = SysProperty.getString(Constants.SYSPRO_FTP_REMOTE_PATH);

    public static void uploadDataFile(File dataFile,String emsdn,int dataFileType) {
        FtpHelper.getIntance().uploadData(getFtpHost(),getFtpUser(),getFtpPassword(),
                getFtpRemotePath(),getFileRemotePath(emsdn,dataFileType,getFtpRemotePath()),dataFile.getAbsolutePath());
    }

    public static File downloadFile(FtpInfo ftpInfo) throws Exception {
        File file = new File("db");
        if (!file.exists() || ! file.isDirectory())
            file.mkdir();

        if (ftpInfo.getType() != null && ftpInfo.getType().equals(FtpInfo.TYPE_FTP)) {
            FtpFunc ftpFunc = new FtpFunc(ftpInfo.getHost(),ftpInfo.getUsername(),ftpInfo.getPassword());
            ftpFunc.connect();
            ftpFunc.downloadFile2(ftpInfo.getRemoteFilePath()+"/"+ftpInfo.getFileName(),"db/"+ftpInfo.getFileName());
        } else if (ftpInfo.getType() != null && ftpInfo.getType().equals(FtpInfo.TYPE_SFTP)){
            SFtpFunc sFtpFunc = new SFtpFunc();
            ChannelSftp connect = (ChannelSftp) sFtpFunc.connect(ftpInfo.getHost(), ftpInfo.getPort(), ftpInfo.getUsername(), ftpInfo.getPassword());
            try {
                sFtpFunc.download(ftpInfo.getRemoteFilePath(),ftpInfo.getFileName(),"db/"+ftpInfo.getFileName(),connect);
            } catch (Exception e) {
                throw e;
            } finally {




                if (connect.getSession() != null) {
                    try {
                        connect.getSession().disconnect();
                    } catch (JSchException e) {
                        throw  e;
                    }
                }

                try {
                    connect.exit();
                } catch (Exception e) {
                    //   throw  e;
                }
            }

        } else {
            //无法识别，认为是本地文件
            return new File(ftpInfo.getFileName());
        }
      //  File.createTempFile()
        return new File("db/"+ftpInfo.getFileName());

    }


    public static void main(String[] args) throws Exception {
        FtpInfo ftpInfo = new FtpInfo("ftpuser","123456","135.251.223.204",21,"cdcp/PTN","2013-09-03-161253-JH-OTNM2000-1-PTN-DayMigration.db");
        downloadFile(ftpInfo);
    }

    private static String getFileRemotePath(String emsdn,int dataFileType,String remotePath) {
        if (!remotePath.endsWith("/"))
            remotePath = remotePath+ "/";

        String dateString = new SimpleDateFormat("yyyy.MM.dd").format(new Date());

        return remotePath + dateString+"/"+emsdn+"/"+dataFileType+"/";
    }

    public static String getFtpHost() {
        Assert.isNull(ftpHost,"Please check ftp.host is setting in system.properties");
        return ftpHost;
    }

    public static String getFtpUser() {
        Assert.isNull(ftpUser,"Please check ftp.user is setting in system.properties");
        return ftpUser;
    }
    public static String getFtpPassword() {
        Assert.isNull(ftpPassword,"Please check ftp.password is setting in system.properties");
        return ftpPassword;
    }
    public static String getFtpRemotePath() {
        Assert.isNull(ftpRemotePath,"Please check ftp.remotePath is setting in system.properties");
        return ftpRemotePath;
    }

}
