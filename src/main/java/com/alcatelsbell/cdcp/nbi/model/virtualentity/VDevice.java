package com.alcatelsbell.cdcp.nbi.model.virtualentity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;

@Entity
@Table(name = "V_Device")
public class VDevice extends BObject {
	@BField(description = "本地EMS名称",searchType = BField.SearchType.NULLABLE)
    private String nativeEmsName;
    private String neVersion;

    @Index(name = "Index_CDevice_EmsName")
    private String emsName;
    @BField(description = "所在地",searchType = BField.SearchType.NULLABLE)
    private String location;
    @BField(description = "产品名称",searchType = BField.SearchType.NULLABLE)
    private String productName;
    @BField(description = "支持速率",searchType = BField.SearchType.NULLABLE)
    private String supportedRates;
    @BField(description = "用户标签",searchType = BField.SearchType.NULLABLE)
    private String userLabel;
    
    @BField(description = "是否生效",searchType = BField.SearchType.NULLABLE)
    private String useful;

    @Column(length = 1024)
    @BField(description = "扩展信息",searchType = BField.SearchType.NULLABLE)
    private String additionalInfo;

    @Temporal(TemporalType.TIMESTAMP)
    @BField(description = "配置时间",searchType = BField.SearchType.NULLABLE)
    private Date collectTimepoint;

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

	public String getEmsName() {
		return emsName;
	}

	public void setEmsName(String emsName) {
		this.emsName = emsName;
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
	
	public String getUseful() {
		return useful;
	}
	public void setUseful(String useful) {
		this.useful = useful;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public Date getCollectTimepoint() {
		return collectTimepoint;
	}

	public void setCollectTimepoint(Date collectTimepoint) {
		this.collectTimepoint = collectTimepoint;
	}
}
