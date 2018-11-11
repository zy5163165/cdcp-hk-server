package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-3
 * Time: 上午11:17
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_Section")

public class CSection extends CdcpObject
{

	@BField(description = "速率",searchType = BField.SearchType.NULLABLE)
    private String rate="";
	@BField(description = "方向",searchType = BField.SearchType.NULLABLE)
    private Integer direction= -1;
	private String speed;
	@BField(description = "A端端口",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CPTP",
            dnReferenceTransietField = "APTPDN",
            dnReferenceEntityField = "DN")
    private String aendTp;

    @Transient
    private String APTPDN;
    
    
    @BField(description = "Z端端口",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CPTP",
            dnReferenceTransietField = "ZPTPDN",
            dnReferenceEntityField = "DN")
    private String zendTp = "";
    @Transient
    private String ZPTPDN;

    private Long aptpId;
    private Long zptpId;
    private String type;
    private String omsDn;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOmsDn() {
        return omsDn;
    }

    public void setOmsDn(String omsDn) {
        this.omsDn = omsDn;
    }

    public Long getAptpId() {
        return aptpId;
    }

    public void setAptpId(Long aptpId) {
        this.aptpId = aptpId;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public Long getZptpId() {
        return zptpId;
    }

    public void setZptpId(Long zptpId) {
        this.zptpId = zptpId;
    }

    @Temporal(TemporalType.TIMESTAMP)
	@BField(description = "采集时间",searchType = BField.SearchType.NULLABLE)
    private Date collectTimepoint;

    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }

    /**
     * @return the rate
     */
    public String getRate() {
        return rate;
    }
    /**
     * @param rate the rate to set
     */
    public void setRate(String rate) {
        this.rate = rate;
    }
    /**
     * @return the direction
     */
    public Integer getDirection() {
        return direction;
    }
    /**
     * @param direction the direction to set
     */
    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public String getAendTp() {
        return aendTp;
    }

    public void setAendTp(String aendTp) {
        this.aendTp = aendTp;
    }

    public String getZendTp() {
        return zendTp;
    }

    public void setZendTp(String zendTp) {
        this.zendTp = zendTp;
    }

    private String parentDn = "";



    private String userLabel = "";

    private String nativeEMSName = "";


    private String owner = "";


    private String additionalInfo = "";

    /**
     * @return the userLabel
     */
    public String getUserLabel() {
        return userLabel;
    }
    /**
     * @param userLabel the userLabel to set
     */
    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }
    /**
     * @return the nativeEMSName
     */
    public String getNativeEMSName() {
        return nativeEMSName;
    }
    /**
     * @param nativeEMSName the nativeEMSName to set
     */
    public void setNativeEMSName(String nativeEMSName) {
        this.nativeEMSName = nativeEMSName;
    }


    public void setParentDn(String parentDn) {
        this.parentDn = parentDn;
    }
    public String getParentDn() {
        return parentDn;
    }
    /**
     * @return the additionalInfo
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    /**
     * @param additionalInfo the additionalInfo to set
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }
    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    private String memo;
    
    public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Section [aEndTP=" + aendTp + ", additionalInfo="
                + additionalInfo + ", direction=" + direction + ", emsName="
                + getEmsName() + ", nativeEMSName=" + nativeEMSName + ", owner="
                + owner + ", parentDn=" + parentDn + ", rate=" + rate
                + ", userLabel=" + userLabel + ", zEndTP=" + zendTp + "]";
    }
}
