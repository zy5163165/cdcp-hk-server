package com.alcatelsbell.cdcp.nbi.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-15
 * Time: 上午11:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_TRANSMISSIONSYSTEM")
public class CTransmissionSystem extends CdcpObject {
    private String name;
    private String nativeEmsName;
    private String layerRate;
    private String tmRate;
    private String psnType;

    private String category;
    private String additionalInfo;
    @Lob
    private String neDns;
    @Lob
    private String links;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNativeEmsName() {
        return nativeEmsName;
    }

    public void setNativeEmsName(String nativeEmsName) {
        this.nativeEmsName = nativeEmsName;
    }

    public String getLayerRate() {
        return layerRate;
    }

    public void setLayerRate(String layerRate) {
        this.layerRate = layerRate;
    }

    public String getTmRate() {
        return tmRate;
    }

    public void setTmRate(String tmRate) {
        this.tmRate = tmRate;
    }

    public String getPsnType() {
        return psnType;
    }

    public void setPsnType(String psnType) {
        this.psnType = psnType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getNeDns() {
        return neDns;
    }

    public void setNeDns(String neDns) {
        this.neDns = neDns;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }
}
