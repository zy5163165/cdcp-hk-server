package com.alcatelsbell.cdcp.nbi.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-15
 * Time: 下午12:32
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_TransmissionSystem_Channel")
public class CTransmissionSystem_Channel extends CdcpObject{
     private String transmissionSystemDn;
    private String channelDn;
    private String sectionDn;

    public String getSectionDn() {
        return sectionDn;
    }

    public void setSectionDn(String sectionDn) {
        this.sectionDn = sectionDn;
    }

    public String getTransmissionSystemDn() {
        return transmissionSystemDn;
    }

    public void setTransmissionSystemDn(String transmissionSystemDn) {
        this.transmissionSystemDn = transmissionSystemDn;
    }

    public String getChannelDn() {
        return channelDn;
    }

    public void setChannelDn(String channelDn) {
        this.channelDn = channelDn;
    }
}
