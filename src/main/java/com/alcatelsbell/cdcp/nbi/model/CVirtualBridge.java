package com.alcatelsbell.cdcp.nbi.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-4
 * Time: 上午10:17
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_VirtualBridge")
public class CVirtualBridge extends CdcpObject{
    private String name;
    private String userLabel;
    private String nativeEMSName;
    private String owner;
    @Lob
    private String logicalTPList;
    private String additionalInfo;
    private String parentDn;
    private String equipmentDn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserLabel() {
        return userLabel;
    }

    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    public String getNativeEMSName() {
        return nativeEMSName;
    }

    public void setNativeEMSName(String nativeEMSName) {
        this.nativeEMSName = nativeEMSName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLogicalTPList() {
        return logicalTPList;
    }

    public void setLogicalTPList(String logicalTPList) {
        this.logicalTPList = logicalTPList;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getParentDn() {
        return parentDn;
    }

    public void setParentDn(String parentDn) {
        this.parentDn = parentDn;
    }


    public String getEquipmentDn() {
        return equipmentDn;
    }

    public void setEquipmentDn(String equipmentDn) {
        this.equipmentDn = equipmentDn;
    }
}
