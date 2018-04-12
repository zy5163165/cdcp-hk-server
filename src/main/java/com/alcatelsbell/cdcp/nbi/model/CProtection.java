package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-4
 * Time: 下午12:30
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_Protection")
public class CProtection extends CdcpObject {
    private String protectionType;
    private String protectedEntityDn;
    private String protectingEntityDn;



    public String getProtectionType() {
        return protectionType;
    }

    public void setProtectionType(String protectionType) {
        this.protectionType = protectionType;
    }

    public String getProtectedEntityDn() {
        return protectedEntityDn;
    }

    public void setProtectedEntityDn(String protectedEntityDn) {
        this.protectedEntityDn = protectedEntityDn;
    }

    public String getProtectingEntityDn() {
        return protectingEntityDn;
    }

    public void setProtectingEntityDn(String protectingEntityDn) {
        this.protectingEntityDn = protectingEntityDn;
    }

}
