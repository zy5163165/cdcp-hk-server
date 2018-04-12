package com.alcatelsbell.cdcp.nbi.irm;

import java.util.Date;

public class TJ_INTERFACE_SDH {
	public Long id;
	public String emsName;
	public Integer sdhDeviceNumber;
	public Integer sdhShelfNumber;
	public Integer sdhCardNumber;
	public Integer sdhPortNumber;
	public Integer sdhSectionNumber;
	public Integer channellNumber;
	public Integer sdhRouteNumber;
	public Integer sdhEthtrunkNumber;
	public Integer sdhEthrouteNumber;
	public Integer sdhSubnetworkNumber;
	public Integer sdhTransSystemNumber;
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

	public Integer getSdhDeviceNumber() {
		return sdhDeviceNumber;
	}

	public void setSdhDeviceNumber(Integer sdhDeviceNumber) {
		this.sdhDeviceNumber = sdhDeviceNumber;
	}

	public Integer getSdhShelfNumber() {
		return sdhShelfNumber;
	}

	public void setSdhShelfNumber(Integer sdhShelfNumber) {
		this.sdhShelfNumber = sdhShelfNumber;
	}

	public Integer getSdhCardNumber() {
		return sdhCardNumber;
	}

	public void setSdhCardNumber(Integer sdhCardNumber) {
		this.sdhCardNumber = sdhCardNumber;
	}

	public Integer getSdhPortNumber() {
		return sdhPortNumber;
	}

	public void setSdhPortNumber(Integer sdhPortNumber) {
		this.sdhPortNumber = sdhPortNumber;
	}

	public Integer getSdhSectionNumber() {
		return sdhSectionNumber;
	}

	public void setSdhSectionNumber(Integer sdhSectionNumber) {
		this.sdhSectionNumber = sdhSectionNumber;
	}

	public Integer getChannellNumber() {
		return channellNumber;
	}

	public void setChannellNumber(Integer channellNumber) {
		this.channellNumber = channellNumber;
	}

	public Integer getSdhRouteNumber() {
		return sdhRouteNumber;
	}

	public void setSdhRouteNumber(Integer sdhRouteNumber) {
		this.sdhRouteNumber = sdhRouteNumber;
	}

	public Integer getSdhEthtrunkNumber() {
		return sdhEthtrunkNumber;
	}

	public void setSdhEthtrunkNumber(Integer sdhEthtrunkNumber) {
		this.sdhEthtrunkNumber = sdhEthtrunkNumber;
	}

	public Integer getSdhEthrouteNumber() {
		return sdhEthrouteNumber;
	}

	public void setSdhEthrouteNumber(Integer sdhEthrouteNumber) {
		this.sdhEthrouteNumber = sdhEthrouteNumber;
	}

	public Integer getSdhSubnetworkNumber() {
		return sdhSubnetworkNumber;
	}

	public void setSdhSubnetworkNumber(Integer sdhSubnetworkNumber) {
		this.sdhSubnetworkNumber = sdhSubnetworkNumber;
	}

	public Integer getSdhTransSystemNumber() {
		return sdhTransSystemNumber;
	}

	public void setSdhTransSystemNumber(Integer sdhTransSystemNumber) {
		this.sdhTransSystemNumber = sdhTransSystemNumber;
	}

	public Date getAcquisitionTime() {
		return AcquisitionTime;
	}

	public void setAcquisitionTime(Date acquisitionTime) {
		AcquisitionTime = acquisitionTime;
	}

}
