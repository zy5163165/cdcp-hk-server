package com.alcatelsbell.cdcp.nbi.model;


import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "C_EquipmentHolder")

public class CEquipmentHolder extends CdcpObject
{
	
	
	private boolean alarmReportingIndicator=true;
	private String holderType="";
	private String expectedOrInstalledEquipment="";

    @Column(length = 1024)
	private String acceptableEquipmentTypeList="";
	private String holderState = "";
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;

    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }
	
	
	/**
	 * @return the alarmReportingIndicator
	 */
	public boolean isAlarmReportingIndicator() {
		return alarmReportingIndicator;
	}
	/**
	 * @param alarmReportingIndicator the alarmReportingIndicator to set
	 */
	public void setAlarmReportingIndicator(boolean alarmReportingIndicator) {
		this.alarmReportingIndicator = alarmReportingIndicator;
	}
	/**
	 * @return the holderType
	 */
	public String getHolderType() {
		return holderType;
	}
	/**
	 * @param holderType the holderType to set
	 */
	public void setHolderType(String holderType) {
		this.holderType = holderType;
	}
	/**
	 * @return the expectedOrInstalledEquipment
	 */
	public String getExpectedOrInstalledEquipment() {
		return expectedOrInstalledEquipment;
	}
	/**
	 * @param expectedOrInstalledEquipment the expectedOrInstalledEquipment to set
	 */
	public void setExpectedOrInstalledEquipment(String expectedOrInstalledEquipment) {
		this.expectedOrInstalledEquipment = expectedOrInstalledEquipment;
	}
	/**
	 * @return the acceptableEquipmentTypeList
	 */
	public String getAcceptableEquipmentTypeList() {
		return acceptableEquipmentTypeList;
	}
	/**
	 * @param acceptableEquipmentTypeList the acceptableEquipmentTypeList to set
	 */
	public void setAcceptableEquipmentTypeList(String acceptableEquipmentTypeList) {
		this.acceptableEquipmentTypeList = acceptableEquipmentTypeList;
	}
	/**
	 * @return the holderState
	 */
	public String getHolderState() {
		return holderState;
	}
	/**
	 * @param holderState the holderState to set
	 */
	public void setHolderState(String holderState) {
		this.holderState = holderState;
	}
	private String parentDn = "";


	
	private String userLabel = "";
	
	private String nativeEMSName = "";
	
	
	private String owner = "";
	

	private String additionalInfo = "";
	/**
	 * @return the emsName
	 */

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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */

	
}
