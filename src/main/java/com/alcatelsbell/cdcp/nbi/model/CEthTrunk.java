package com.alcatelsbell.cdcp.nbi.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-4
 * Time: 上午11:00
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_EthTrunk")
public class CEthTrunk extends CdcpObject {
    private String name;
    private String direction;
    private String rate;
    private String aptp;
    private String zptp;
    private Long aptpId;
    private Long zptpId;
    private String tmRate;

    public String getTmRate() {
        return tmRate;
    }

    public void setTmRate(String tmRate) {
        this.tmRate = tmRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
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
}
