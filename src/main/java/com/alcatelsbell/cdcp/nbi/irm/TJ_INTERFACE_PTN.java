package com.alcatelsbell.cdcp.nbi.irm;

import java.util.Date;

public class TJ_INTERFACE_PTN {
	public Long id;
	public String emsName;
	public Integer ptnDeviceNumber;
	public Integer ptnShelfNumber;
	public Integer ptnPortNumber;
	public Integer ptnCardNumber;
	public Integer ptnSectionNumber;
	public Integer ptnTunnelNumber;
	public Integer ptnPWE3Number;
	public Integer ptnPWNumber;
	public Integer ptnSubnetworkNumber;
	public Date AcquisitionTime;

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

	public Integer getPtnDeviceNumber() {
		return ptnDeviceNumber;
	}

	public void setPtnDeviceNumber(Integer ptnDeviceNumber) {
		this.ptnDeviceNumber = ptnDeviceNumber;
	}

	public Integer getPtnShelfNumber() {
		return ptnShelfNumber;
	}

	public void setPtnShelfNumber(Integer ptnShelfNumber) {
		this.ptnShelfNumber = ptnShelfNumber;
	}

	public Integer getPtnPortNumber() {
		return ptnPortNumber;
	}

	public void setPtnPortNumber(Integer ptnPortNumber) {
		this.ptnPortNumber = ptnPortNumber;
	}

	public Integer getPtnCardNumber() {
		return ptnCardNumber;
	}

	public void setPtnCardNumber(Integer ptnCardNumber) {
		this.ptnCardNumber = ptnCardNumber;
	}

	public Integer getPtnSectionNumber() {
		return ptnSectionNumber;
	}

	public void setPtnSectionNumber(Integer ptnSectionNumber) {
		this.ptnSectionNumber = ptnSectionNumber;
	}

	public Integer getPtnTunnelNumber() {
		return ptnTunnelNumber;
	}

	public void setPtnTunnelNumber(Integer ptnTunnelNumber) {
		this.ptnTunnelNumber = ptnTunnelNumber;
	}

	public Integer getPtnPWE3Number() {
		return ptnPWE3Number;
	}

	public void setPtnPWE3Number(Integer ptnPWE3Number) {
		this.ptnPWE3Number = ptnPWE3Number;
	}

	public Integer getPtnPWNumber() {
		return ptnPWNumber;
	}

	public void setPtnPWNumber(Integer ptnPWNumber) {
		this.ptnPWNumber = ptnPWNumber;
	}

	public Integer getPtnSubnetworkNumber() {
		return ptnSubnetworkNumber;
	}

	public void setPtnSubnetworkNumber(Integer ptnSubnetworkNumber) {
		this.ptnSubnetworkNumber = ptnSubnetworkNumber;
	}

	public Date getAcquisitionTime() {
		return AcquisitionTime;
	}

	public void setAcquisitionTime(Date acquisitionTime) {
		AcquisitionTime = acquisitionTime;
	}

}
