package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-4
 * Time: 下午12:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_PW_Tunnel")
public class CPW_Tunnel  extends CdcpObject{
    @Column(length = 512)
    private String pwDn;
    private String tunnelDn;
    private String protectionType;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    
    private Long tunnelId;
    private Long pwId;

    public Long getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(Long tunnelId) {
        this.tunnelId = tunnelId;
    }


    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }


    public String getTunnelDn() {
        return tunnelDn;
    }

    public void setTunnelDn(String tunnelDn) {
        this.tunnelDn = tunnelDn;
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
