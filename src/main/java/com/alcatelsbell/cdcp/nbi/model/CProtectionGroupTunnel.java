package com.alcatelsbell.cdcp.nbi.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-31
 * Time: 下午2:45
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_ProtectionGroup_Tunnel")
public class CProtectionGroupTunnel extends CdcpObject {
    private Long protectGroupId;
    private Long tunnelId;
    private String protectGroupDn;
    private String tunnelDn;


    private String status;


    public String getProtectGroupDn() {
        return protectGroupDn;
    }

    public void setProtectGroupDn(String protectGroupDn) {
        this.protectGroupDn = protectGroupDn;
    }

    public String getTunnelDn() {
        return tunnelDn;
    }

    public void setTunnelDn(String tunnelDn) {
        this.tunnelDn = tunnelDn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getProtectGroupId() {
        return protectGroupId;
    }

    public void setProtectGroupId(Long protectGroupId) {
        this.protectGroupId = protectGroupId;
    }

    public Long getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(Long tunnelId) {
        this.tunnelId = tunnelId;
    }
}
