package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-3
 * Time: 下午4:42
 * rongrong.chen@alcatel-sbell.com.cn
 */

@Entity
@Table(name = "C_IPRoute")
public class CIPRoute extends CdcpObject{
   //@Index(name="CRoute_TunnelDn_Index")
    private String tunnelDn="";
    private Long tunnelId ;
    private String entityType="";
    @Column(length = 512)
    private String entityDn="";
    @Column(length = 512)
    private String aend = "";
    @Column(length = 512)
    private String zend = "";
    private String aptp = "";
    private String zptp = "";
    private Long aptpId;
    private Long zptpId;

    public Long getAptpId() {
        return aptpId;
    }

    public void setAptpId(Long aptpId) {
        this.aptpId = aptpId;
    }

    public Long getZptpId() {
        return zptpId;
    }

    public void setZptpId(Long zptpId) {
        this.zptpId = zptpId;
    }

    public Long getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(Long tunnelId) {
        this.tunnelId = tunnelId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;

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

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityDn() {
        return entityDn;
    }

    public void setEntityDn(String entityDn) {
        this.entityDn = entityDn;
    }

    public String getAend() {
        return aend;
    }

    public void setAend(String aend) {
        this.aend = aend;
    }

    public String getZend() {
        return zend;
    }

    public void setZend(String zend) {
        this.zend = zend;
    }

    public String getAptp() {
        return aptp;
    }

    public void setAptp(String aptp) {
        this.aptp = aptp;
    }

    public String getZptp() {
        return zptp;
    }

    public void setZptp(String zptp) {
        this.zptp = zptp;
    }
}
