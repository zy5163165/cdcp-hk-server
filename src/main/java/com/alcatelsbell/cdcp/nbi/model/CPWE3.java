package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-3
 * Time: 下午2:01
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_PWE3")
public class CPWE3 extends CdcpObject {

	private boolean flexible = false;
	private String networkAccessDomain = "";
	private String administrativeState = "";
	private String fdfrState = "";
	private String multipointServiceAttrParaList;
	private String multipointServiceAttrMacList;
	private String multipointServiceAttrAddInfo;
	private Integer direction;
	@Column(length = 1024)
	private String transmissionParams = "";
	private String rate = "";
	private String fdfrType = "";


	@Lob
	private String aptp = "";

	@Lob
	private String zptp = "";
	@Column(length = 1024)
	private String aendTrans = "";
	@Column(length = 1024)
	private String zendtrans = "";

	@Transient
	private String tunnelDn = null;
	private String avlanId;
	private String zvlanId;
	private String cir;
	private String pir;

	@Lob
	@Column(name = "aend")
	private String zx_aend = "";
	@Lob
	@Column(name = "zend")
	private String zx_zend = "";
	@Lob
	@Column(name = "aptps")
	private String zx_aptps = "";
	@Lob
	@Column(name = "zptps")
	private String zx_zptps = "";
	private Long aptpId;
	private Long zptpId;
	@Transient
	private Long tunnelId;

	public Long getTunnelId() {
		return tunnelId;
	}

	public void setTunnelId(Long tunnelId) {
		this.tunnelId = tunnelId;
	}

	public String getAvlanId() {
		return avlanId;
	}

	public void setAvlanId(String avlanId) {
		this.avlanId = avlanId;
	}

	public String getZvlanId() {
		return zvlanId;
	}

	public void setZvlanId(String zvlanId) {
		this.zvlanId = zvlanId;
	}

	public Long getAptpId() {
		return aptpId;
	}

	public void setAptpId(Long aptpId) {
		this.aptpId = aptpId;
	}

	public Long getZptpId() {
		return zptpId;
	}

	public void setZptpId(Long zptpId) {
		this.zptpId = zptpId;
	}

	public String getCir() {
		return cir;
	}

	public void setCir(String cir) {
		this.cir = cir;
	}

	public String getPir() {
		return pir;
	}

	public void setPir(String pir) {
		this.pir = pir;
	}

	public String getTunnelDn() {
		return tunnelDn;
	}

	public void setTunnelDn(String tunnelDn) {
		this.tunnelDn = tunnelDn;
	}

	@Temporal(TemporalType.TIMESTAMP)
	private Date collectTimepoint;

	public Date getCollectTimepoint() {
		return collectTimepoint;
	}

	public void setCollectTimepoint(Date collectTimepoint) {
		this.collectTimepoint = collectTimepoint;
	}

	/**
	 * @return the flexible
	 */
	public boolean isFlexible() {
		return flexible;
	}

	/**
	 * @param flexible
	 *            the flexible to set
	 */
	public void setFlexible(boolean flexible) {
		this.flexible = flexible;
	}

	/**
	 * @return the networkAccessDomain
	 */
	public String getNetworkAccessDomain() {
		return networkAccessDomain;
	}

	/**
	 * @param networkAccessDomain
	 *            the networkAccessDomain to set
	 */
	public void setNetworkAccessDomain(String networkAccessDomain) {
		this.networkAccessDomain = networkAccessDomain;
	}

	/**
	 * @return the administrativeState
	 */
	public String getAdministrativeState() {
		return administrativeState;
	}

	/**
	 * @param administrativeState
	 *            the administrativeState to set
	 */
	public void setAdministrativeState(String administrativeState) {
		this.administrativeState = administrativeState;
	}

	/**
	 * @return the fdfrState
	 */
	public String getFdfrState() {
		return fdfrState;
	}

	/**
	 * @param fdfrState
	 *            the fdfrState to set
	 */
	public void setFdfrState(String fdfrState) {
		this.fdfrState = fdfrState;
	}

	/**
	 * @return the direction
	 */
	public Integer getDirection() {
		return direction;
	}

	/**
	 * @param direction
	 *            the direction to set
	 */
	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	/**
	 * @return the transmissionParams
	 */
	public String getTransmissionParams() {
		return transmissionParams;
	}

	/**
	 * @param transmissionParams
	 *            the transmissionParams to set
	 */
	public void setTransmissionParams(String transmissionParams) {
		this.transmissionParams = transmissionParams;
	}

	/**
	 * @return the fdfrType
	 */
	public String getFdfrType() {
		return fdfrType;
	}

	/**
	 * @param fdfrType
	 *            the fdfrType to set
	 */
	public void setFdfrType(String fdfrType) {
		this.fdfrType = fdfrType;
	}

	/**
	 * @return the multipointServiceAttrParaList
	 */
	public String getMultipointServiceAttrParaList() {
		return multipointServiceAttrParaList;
	}

	/**
	 * @param multipointServiceAttrParaList
	 *            the multipointServiceAttrParaList to set
	 */
	public void setMultipointServiceAttrParaList(String multipointServiceAttrParaList) {
		this.multipointServiceAttrParaList = multipointServiceAttrParaList;
	}

	/**
	 * @return the multipointServiceAttrMacList
	 */
	public String getMultipointServiceAttrMacList() {
		return multipointServiceAttrMacList;
	}

	/**
	 * @param multipointServiceAttrMacList
	 *            the multipointServiceAttrMacList to set
	 */
	public void setMultipointServiceAttrMacList(String multipointServiceAttrMacList) {
		this.multipointServiceAttrMacList = multipointServiceAttrMacList;
	}

	/**
	 * @return the multipointServiceAttrAddInfo
	 */
	public String getMultipointServiceAttrAddInfo() {
		return multipointServiceAttrAddInfo;
	}

	/**
	 * @param multipointServiceAttrAddInfo
	 *            the multipointServiceAttrAddInfo to set
	 */
	public void setMultipointServiceAttrAddInfo(String multipointServiceAttrAddInfo) {
		this.multipointServiceAttrAddInfo = multipointServiceAttrAddInfo;
	}

	@Transient
	private String parentDn = "";

	private String userLabel = "";

	@Column(length = 512)
	private String nativeEMSName = "";

	private String owner = "";

	@Column(length = 2048)
	private String additionalInfo = "";

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
	 * @param additionalInfo
	 *            the additionalInfo to set
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
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PWE3 [aEnd="   + ", aEndTrans=" + aendTrans + ", additionalInfo=" + additionalInfo + ", administrativeState=" + administrativeState
				+ ", direction=" + direction + ", emsName=" + getEmsName() + ", fdfrState=" + fdfrState + ", fdfrType=" + fdfrType + ", flexible=" + flexible
				+ ", multipointServiceAttrAddInfo=" + multipointServiceAttrAddInfo + ", multipointServiceAttrMacList=" + multipointServiceAttrMacList
				+ ", multipointServiceAttrParaList=" + multipointServiceAttrParaList + ", nativeEMSName=" + nativeEMSName + ", networkAccessDomain="
				+ networkAccessDomain + ", owner=" + owner + ", parentDn=" + parentDn + ", rate=" + rate + ", transmissionParams=" + transmissionParams
				+ ", userLabel=" + userLabel + ", zEnd="   + ", zEndtrans=" + zendtrans + "]";
	}

	public String getAptp() {
		return aptp;
	}

	public void setAptp(String aptp) {
		this.aptp = aptp;
	}

	public String getZptp() {
		return zptp;
	}

	public void setZptp(String zptp) {
		this.zptp = zptp;
	}

	public String getAend() {
		return zx_aend;
	}

	public void setAend(String aend) {
		this.zx_aend = aend;
	}

	public String getZend() {
		return zx_zend;
	}

	public void setZend(String zend) {
		this.zx_zend = zend;
	}

	public String getAendTrans() {
		return aendTrans;
	}

	public void setAendTrans(String aendTrans) {
		this.aendTrans = aendTrans;
	}

	public String getZendtrans() {
		return zendtrans;
	}

	public void setZendtrans(String zendtrans) {
		this.zendtrans = zendtrans;
	}

	public String getAptps() {
		return zx_aptps;
	}

	public void setAptps(String aptps) {
		this.zx_aptps = aptps;
	}

	public String getZptps() {
		return zx_zptps;
	}

	public void setZptps(String zptps) {
		this.zx_zptps = zptps;
	}

	public void setZx_aend(String zx_aend) {
		this.zx_aend = zx_aend;
	}

	public String getZx_zend() {
		return zx_zend;
	}

	public void setZx_zend(String zx_zend) {
		this.zx_zend = zx_zend;
	}

	public String getZx_aptps() {
		return zx_aptps;
	}

	public void setZx_aptps(String zx_aptps) {
		this.zx_aptps = zx_aptps;
	}

	public String getZx_zptps() {
		return zx_zptps;
	}

	public void setZx_zptps(String zx_zptps) {
		this.zx_zptps = zx_zptps;
	}
}
