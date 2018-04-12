package com.alcatelsbell.cdcp.nbi.model;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-10
 * Time: 下午2:15
 * rongrong.chen@alcatel-sbell.com.cn
 */

import com.alcatelsbell.nms.valueobject.BObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "C_ProtectionGroup")
public class CProtectionGroup extends CdcpObject {

	private String parentDn = "";
	private String userLabel = "";
	private String nativeEMSName = "";
	private String owner = "";
	private String protectionGroupType = "";
	private String protectionSchemeState = "";
	private String reversionMode = "";
	private String rate;

	private String pgpParameters = "";
	@Column(length = 1024)
	private String protectedList = "";
	@Column(length = 1024)
	private String protectingList = "";

	/**
	 * @return the parentDn
	 */
	public String getParentDn() {
		return parentDn;
	}

	/**
	 * @param parentDn
	 *            the parentDn to set
	 */
	public void setParentDn(String parentDn) {
		this.parentDn = parentDn;
	}

	/**
	 * @return the userLabel
	 */
	public String getUserLabel() {
		return userLabel;
	}

	/**
	 * @param userLabel
	 *            the userLabel to set
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
	 * @param nativeEMSName
	 *            the nativeEMSName to set
	 */
	public void setNativeEMSName(String nativeEMSName) {
		this.nativeEMSName = nativeEMSName;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the protectionGroupType
	 */
	public String getProtectionGroupType() {
		return protectionGroupType;
	}

	/**
	 * @param protectionGroupType
	 *            the protectionGroupType to set
	 */
	public void setProtectionGroupType(String protectionGroupType) {
		this.protectionGroupType = protectionGroupType;
	}

	/**
	 * @return the protectionSchemeState
	 */
	public String getProtectionSchemeState() {
		return protectionSchemeState;
	}

	/**
	 * @param protectionSchemeState
	 *            the protectionSchemeState to set
	 */
	public void setProtectionSchemeState(String protectionSchemeState) {
		this.protectionSchemeState = protectionSchemeState;
	}

	/**
	 * @return the reversionMode
	 */
	public String getReversionMode() {
		return reversionMode;
	}

	/**
	 * @param reversionMode
	 *            the reversionMode to set
	 */
	public void setReversionMode(String reversionMode) {
		this.reversionMode = reversionMode;
	}

	/**
	 * @return the rate
	 */
	public String getRate() {
		return rate;
	}

	/**
	 * @param rate
	 *            the rate to set
	 */
	public void setRate(String rate) {
		this.rate = rate;
	}

	/**
	 * @return the pgpParameters
	 */
	public String getPgpParameters() {
		return pgpParameters;
	}

	/**
	 * @param pgpParameters
	 *            the pgpParameters to set
	 */
	public void setPgpParameters(String pgpParameters) {
		this.pgpParameters = pgpParameters;
	}

	/**
	 * @return the protectedList
	 */
	public String getProtectedList() {
		return protectedList;
	}

	/**
	 * @param protectedList
	 *            the protectedList to set
	 */
	public void setProtectedList(String protectedList) {
		this.protectedList = protectedList;
	}

	/**
	 * @return the protectingList
	 */
	public String getProtectingList() {
		return protectingList;
	}

	/**
	 * @param protectingList
	 *            the protectingList to set
	 */
	public void setProtectingList(String protectingList) {
		this.protectingList = protectingList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProtectionGroup [emsName=" + getEmsName() + ", nativeEMSName=" + nativeEMSName + ", owner=" + owner + ", parentDn=" + parentDn
				+ ", pgpParameters=" + pgpParameters + ", protectedList=" + protectedList + ", protectingList=" + protectingList + ", protectionGroupType="
				+ protectionGroupType + ", protectionSchemeState=" + protectionSchemeState + ", rate=" + rate + ", reversionMode=" + reversionMode
				+ ", userLabel=" + userLabel + "]";
	}

}
