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
@Table(name = "C_OMS_SECTION")
public class COMS_Section extends CdcpObject {
    private String omsdn;
    private String sectiondn;

    public String getOmsdn() {
        return omsdn;
    }

    public void setOmsdn(String omsdn) {
        this.omsdn = omsdn;
    }

    public String getSectiondn() {
        return sectiondn;
    }

    public void setSectiondn(String sectiondn) {
        this.sectiondn = sectiondn;
    }
}
