package com.alcatelsbell.cdcp.nbi.irm;

import java.util.Date;

public class TJ_Differenceslist {
	public Long id;
	public String emsName;
	public Integer objectType;
	public String objectName;
	public Integer differencesType;
	public Date compartTime;

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

	public Integer getObjectType() {
		return objectType;
	}

	public void setObjectType(Integer objectType) {
		this.objectType = objectType;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public Integer getDifferencesType() {
		return differencesType;
	}

	public void setDifferencesType(Integer differencesType) {
		this.differencesType = differencesType;
	}

	public Date getCompartTime() {
		return compartTime;
	}

	public void setCompartTime(Date compartTime) {
		this.compartTime = compartTime;
	}

}
