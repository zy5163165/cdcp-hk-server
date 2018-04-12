package com.alcatelsbell.cdcp.nbi.model;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-10
 * Time: 下午9:22
 * rongrong.chen@alcatel-sbell.com.cn
 */



import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.alcatelsbell.nms.valueobject.BObject;





@Entity
@Table(name = "C_FTP_PTP")

public class CFTP_PTP extends CdcpObject
{


    private String ftpDn="";
    private String tpMappingMode="";
    private String rate="";
    private String transmissionParams = "";
    private String ptpDn="";
    private Long ftpId;
    private Long ptpId;

    public Long getFtpId() {
        return ftpId;
    }

    public void setFtpId(Long ftpId) {
        this.ftpId = ftpId;
    }

    public Long getPtpId() {
        return ptpId;
    }

    public void setPtpId(Long ptpId) {
        this.ptpId = ptpId;
    }

    /**
     * @return the ftpDn
     */
    public String getFtpDn() {
        return ftpDn;
    }
    /**
     * @param ftpDn the ftpDn to set
     */
    public void setFtpDn(String ftpDn) {
        this.ftpDn = ftpDn;
    }
    /**
     * @return the tpMappingMode
     */
    public String getTpMappingMode() {
        return tpMappingMode;
    }
    /**
     * @param tpMappingMode the tpMappingMode to set
     */
    public void setTpMappingMode(String tpMappingMode) {
        this.tpMappingMode = tpMappingMode;
    }
    /**
     * @return the rate
     */
    public String getRate() {
        return rate;
    }
    /**
     * @param rate the rate to set
     */
    public void setRate(String rate) {
        this.rate = rate;
    }
    /**
     * @return the transmissionParams
     */
    public String getTransmissionParams() {
        return transmissionParams;
    }
    /**
     * @param transmissionParams the transmissionParams to set
     */
    public void setTransmissionParams(String transmissionParams) {
        this.transmissionParams = transmissionParams;
    }
    public void setPtpDn(String ptpDn) {
        this.ptpDn = ptpDn;
    }
    public String getPtpDn() {
        return ptpDn;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "R_FTP_PTP [ftpDn=" + ftpDn + ", ptpDn=" + ptpDn + ", rate="
                + rate + ", tpMappingMode=" + tpMappingMode
                + ", transmissionParams=" + transmissionParams + "]";
    }

}
