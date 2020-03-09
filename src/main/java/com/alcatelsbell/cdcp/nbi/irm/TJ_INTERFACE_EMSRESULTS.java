package com.alcatelsbell.cdcp.nbi.irm;

import java.util.Date;

public class TJ_INTERFACE_EMSRESULTS {
	public Long id;
	public String emsName;
	public Date acquisitionTime;
	public Integer transprotocolType;
	public String vender;
	public Integer resultsStatus;
	public String failReason;
	public Integer isNotice;
//	public Date endtime;

//	public Date getEndtime() {
//		return endtime;
//	}
//
//	public void setEndtime(Date endtime) {
//		this.endtime = endtime;
//	}

	public TJ_INTERFACE_EMSRESULTS(String emsName, Date acquisitionTime, Integer transprotocolType, String vender, Integer resultsStatus, String failReason, Integer isNotice) {
		this.emsName = emsName;
		this.acquisitionTime = acquisitionTime;
		this.transprotocolType = transprotocolType;
		this.vender = vender;
		this.resultsStatus = resultsStatus;
		this.failReason = failReason;
		this.isNotice = isNotice;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmsName() {
		return emsName;
	}

	public void setEmsName(String emsName) {
		this.emsName = emsName;
	}

	public Date getAcquisitionTime() {
		return acquisitionTime;
	}

	public void setAcquisitionTime(Date acquisitionTime) {
		this.acquisitionTime = acquisitionTime;
	}

	public Integer getTransprotocolType() {
		return transprotocolType;
	}

	public void setTransprotocolType(Integer transprotocolType) {
		this.transprotocolType = transprotocolType;
	}

	public String getVender() {
		return vender;
	}

	public void setVender(String vender) {
		this.vender = vender;
	}

	public Integer getResultsStatus() {
		return resultsStatus;
	}

	public void setResultsStatus(Integer resultsStatus) {
		this.resultsStatus = resultsStatus;
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

	public Integer getIsNotice() {
		return isNotice;
	}

	public void setIsNotice(Integer isNotice) {
		this.isNotice = isNotice;
	}

}
