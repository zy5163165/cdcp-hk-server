package com.alcatelsbell.cdcp.nbi.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-8-6
 * Time: 下午4:10
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_OMS_CC")
public class COMS_CC extends CdcpObject{
    private String omsdn;
    private String ccdn;

    public String getOmsdn() {
        return omsdn;
    }

    public void setOmsdn(String omsdn) {
        this.omsdn = omsdn;
    }

    public String getCcdn() {
        return ccdn;
    }

    public void setCcdn(String ccdn) {
        this.ccdn = ccdn;
    }
}
