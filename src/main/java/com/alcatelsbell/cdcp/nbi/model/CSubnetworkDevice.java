package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.valueobject.BObject;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-30
 * Time: 上午9:32
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_SubnetworkDevice")
public class CSubnetworkDevice extends CdcpObject {
    private Long subnetworkId;
    private Long deviceId;
    private String subnetworkDn;
    private String deviceDn;

    public String getSubnetworkDn() {
        return subnetworkDn;
    }

    public void setSubnetworkDn(String subnetworkDn) {
        this.subnetworkDn = subnetworkDn;
    }

    public String getDeviceDn() {
        return deviceDn;
    }

    public void setDeviceDn(String deviceDn) {
        this.deviceDn = deviceDn;
    }

    public Long getSubnetworkId() {
        return subnetworkId;
    }

    public void setSubnetworkId(Long subnetworkId) {
        this.subnetworkId = subnetworkId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
}
