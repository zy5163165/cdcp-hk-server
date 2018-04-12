package com.alcatelsbell.cdcp.nbi.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-25
 * Time: 下午4:28
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_TUNNEL_SECTION")
public class CTunnel_Section extends CdcpObject{
    private Long sectionId;
    private Long tunnelId;
    private String sectionDn;
    private String tunnelDn;

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(Long tunnelId) {
        this.tunnelId = tunnelId;
    }

    public String getSectionDn() {
        return sectionDn;
    }

    public void setSectionDn(String sectionDn) {
        this.sectionDn = sectionDn;
    }

    public String getTunnelDn() {
        return tunnelDn;
    }

    public void setTunnelDn(String tunnelDn) {
        this.tunnelDn = tunnelDn;
    }
}
