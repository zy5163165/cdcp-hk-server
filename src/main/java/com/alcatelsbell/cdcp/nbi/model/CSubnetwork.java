package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.valueobject.BObject;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-30
 * Time: 上午9:31
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_Subnetwork")
public class CSubnetwork extends CdcpObject {
    private Long parentSubnetworkId;
    private String parentSubnetworkDn;
    private String name;
    private String  nativeemsname;

    public String getParentSubnetworkDn() {
        return parentSubnetworkDn;
    }

    public void setParentSubnetworkDn(String parentSubnetworkDn) {
        this.parentSubnetworkDn = parentSubnetworkDn;
    }

    public Long getParentSubnetworkId() {
        return parentSubnetworkId;
    }

    public void setParentSubnetworkId(Long parentSubnetworkId) {
        this.parentSubnetworkId = parentSubnetworkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNativeemsname() {
        return nativeemsname;
    }

    public void setNativeemsname(String nativeemsname) {
        this.nativeemsname = nativeemsname;
    }
}
