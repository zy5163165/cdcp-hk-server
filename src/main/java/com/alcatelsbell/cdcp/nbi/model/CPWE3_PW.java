package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-4
 * Time: 下午12:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_PWE3_PW")
public class CPWE3_PW  extends CdcpObject{
    @Column(length = 512)
    private String pwe3Dn;
    private String pwDn;
    private String protectionType;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    
    private Long pwId;
    private Long pwe3Id;

    public Long getPwe3Id() {
        return pwe3Id;
    }

    public void setPwe3Id(Long pwe3Id) {
        this.pwe3Id = pwe3Id;
    }

    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }

    public String getPwe3Dn() {
        return pwe3Dn;
    }

    public void setPwe3Dn(String pwe3Dn) {
        this.pwe3Dn = pwe3Dn;
    }


    public String getProtectionType() {
        return protectionType;
    }

    public void setProtectionType(String protectionType) {
        this.protectionType = protectionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	public String getPwDn() {
		return pwDn;
	}

	public void setPwDn(String pwDn) {
		this.pwDn = pwDn;
	}

	public Long getPwId() {
		return pwId;
	}

	public void setPwId(Long pwId) {
		this.pwId = pwId;
	}

}
