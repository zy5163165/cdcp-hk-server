package com.alcatelsbell.cdcp.nbi.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-4
 * Time: 上午10:01
 * rongrong.chen@alcatel-sbell.com.cn
 */

@Entity
@Table(name = "C_InnerTopology")
public class CInnerTopology extends CdcpObject{
    private String aptp = "";
    private String zptp = "";
    private String actp = "";
    private String zctp = "";

    private Long aptpId;
    private Long zptpId;
    private Long actpId;
    private Long zctpId;

    private String name;
    private String rate;
    private String direction;

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

    public String getActp() {
        return actp;
    }

    public void setActp(String actp) {
        this.actp = actp;
    }

    public String getZctp() {
        return zctp;
    }

    public void setZctp(String zctp) {
        this.zctp = zctp;
    }

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

    public Long getActpId() {
        return actpId;
    }

    public void setActpId(Long actpId) {
        this.actpId = actpId;
    }

    public Long getZctpId() {
        return zctpId;
    }

    public void setZctpId(Long zctpId) {
        this.zctpId = zctpId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
