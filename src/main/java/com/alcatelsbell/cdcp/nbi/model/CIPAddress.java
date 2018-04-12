package com.alcatelsbell.cdcp.nbi.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-31
 * Time: 下午2:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_IPAddress")
public class CIPAddress extends CdcpObject {
    private String ipaddress;
    private Long ptpId;

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public Long getPtpId() {
        return ptpId;
    }

    public void setPtpId(Long ptpId) {
        this.ptpId = ptpId;
    }
}
