package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "C_PATH_CC")
public class CPath_CC extends CdcpObject {
	public String getPathDn() {
		return pathDn;
	}
	public void setPathDn(String pathDn) {
		this.pathDn = pathDn;
	}
	public String getCcDn() {
		return ccDn;
	}
	public void setCcDn(String ccDn) {
		this.ccDn = ccDn;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCollectTimepoint() {
		return collectTimepoint;
	}
	public void setCollectTimepoint(Date collectTimepoint) {
		this.collectTimepoint = collectTimepoint;
	}
	public Long getPathId() {
		return pathId;
	}
	public void setPathId(Long pathId) {
		this.pathId = pathId;
	}
	public Long getCcId() {
		return ccId;
	}
	public void setCcId(Long ccId) {
		this.ccId = ccId;
	}
	private String pathDn;
	private String ccDn;
	private String status;
	@Temporal(TemporalType.TIMESTAMP)
	private Date collectTimepoint;

	private Long pathId;
	private Long ccId;

}
