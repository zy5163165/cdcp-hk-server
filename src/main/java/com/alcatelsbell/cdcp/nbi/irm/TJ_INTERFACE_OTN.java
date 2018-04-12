package com.alcatelsbell.cdcp.nbi.irm;

import java.util.Date;

public class TJ_INTERFACE_OTN {
	public Long id;
	public String emsName;
	public Integer otnDeviceNumber;
	public Integer otnShelfNumber;
	public Integer otnCardNumber;
	public Integer otnPortNumber;
	public Integer otnOtsNumber;
	public Integer otnOmsNumber;
	public Integer otnOchNumber;
	public Integer otnRouteNumber;
	public Date acquisitionTime;

	public TJ_INTERFACE_OTN(String emsName, Integer otnDeviceNumber, Integer otnShelfNumber, Integer otnCardNumber, Integer otnPortNumber, Integer otnOtsNumber, Integer otnOmsNumber, Integer otnOchNumber, Integer otnRouteNumber, Date acquisitionTime) {
		this.emsName = emsName;
		this.otnDeviceNumber = otnDeviceNumber;
		this.otnShelfNumber = otnShelfNumber;
		this.otnCardNumber = otnCardNumber;
		this.otnPortNumber = otnPortNumber;
		this.otnOtsNumber = otnOtsNumber;
		this.otnOmsNumber = otnOmsNumber;
		this.otnOchNumber = otnOchNumber;
		this.otnRouteNumber = otnRouteNumber;
		this.acquisitionTime = acquisitionTime;
	}

	public TJ_INTERFACE_OTN() {
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

	public Integer getOtnDeviceNumber() {
		return otnDeviceNumber;
	}

	public void setOtnDeviceNumber(Integer otnDeviceNumber) {
		this.otnDeviceNumber = otnDeviceNumber;
	}

	public Integer getOtnShelfNumber() {
		return otnShelfNumber;
	}

	public void setOtnShelfNumber(Integer otnShelfNumber) {
		this.otnShelfNumber = otnShelfNumber;
	}

	public Integer getOtnCardNumber() {
		return otnCardNumber;
	}

	public void setOtnCardNumber(Integer otnCardNumber) {
		this.otnCardNumber = otnCardNumber;
	}

	public Integer getOtnPortNumber() {
		return otnPortNumber;
	}

	public void setOtnPortNumber(Integer otnPortNumber) {
		this.otnPortNumber = otnPortNumber;
	}

	public Integer getOtnOtsNumber() {
		return otnOtsNumber;
	}

	public void setOtnOtsNumber(Integer otnOtsNumber) {
		this.otnOtsNumber = otnOtsNumber;
	}

	public Integer getOtnOmsNumber() {
		return otnOmsNumber;
	}

	public void setOtnOmsNumber(Integer otnOmsNumber) {
		this.otnOmsNumber = otnOmsNumber;
	}

	public Integer getOtnOchNumber() {
		return otnOchNumber;
	}

	public void setOtnOchNumber(Integer otnOchNumber) {
		this.otnOchNumber = otnOchNumber;
	}

	public Integer getOtnRouteNumber() {
		return otnRouteNumber;
	}

	public void setOtnRouteNumber(Integer otnRouteNumber) {
		this.otnRouteNumber = otnRouteNumber;
	}

	public Date getAcquisitionTime() {
		return acquisitionTime;
	}

	public void setAcquisitionTime(Date acquisitionTime) {
		this.acquisitionTime = acquisitionTime;
	}

}
