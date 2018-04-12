package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-13
 * Time: 下午2:34
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_DEVICE1", schema = "CDCP5")
public class CDevice extends CdcpObject {

//	@BField(description = "网元名称",searchType = BField.SearchType.NULLABLE)
    private String nativeEmsName;
    private String neVersion;



    private String location;
//    @BField(description = "产品名称",searchType = BField.SearchType.NULLABLE)
    private String productName;
    private String supportedRates;
//    @BField(description = "用户标签",searchType = BField.SearchType.NULLABLE)
    private String userLabel;

    @Column(length = 2048)
//    @BField(description = "扩展信息",searchType = BField.SearchType.NULLABLE)
    private String additionalInfo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;

//    @BField(description = "最大支持速率")
    private String maxTransferRate;

//    @BField(description = "管理IP地址")
    private String ipAddress;
    
//    @BField(description = "网元类型",searchType = BField.SearchType.NULLABLE)
    private String type;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMaxTransferRate() {
        return maxTransferRate;
    }

    public void setMaxTransferRate(String maxTransferRate) {
        this.maxTransferRate = maxTransferRate;
    }

    public String getNativeEmsName() {
        return nativeEmsName;
    }

    public void setNativeEmsName(String nativeEmsName) {
        this.nativeEmsName = nativeEmsName;
    }

    public String getNeVersion() {
        return neVersion;
    }

    public void setNeVersion(String neVersion) {
        this.neVersion = neVersion;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSupportedRates() {
        return supportedRates;
    }

    public void setSupportedRates(String supportedRates) {
        this.supportedRates = supportedRates;
    }

    public String getUserLabel() {
        return userLabel;
    }

    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    public String getType() {
		return type;
	}
    public void setType(String type) {
		this.type = type;
	}

}
