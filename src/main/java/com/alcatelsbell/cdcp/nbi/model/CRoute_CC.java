package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "C_ROUTE_CC")
public class CRoute_CC extends CdcpObject {
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
	public Long getCcId() {
		return ccId;
	}
	public void setCcId(Long ccId) {
		this.ccId = ccId;
	}
	public String getRouteDn() {
		return routeDn;
	}
	public void setRouteDn(String routeDn) {
		this.routeDn = routeDn;
	}
	public Long getRouteId() {
		return routeId;
	}
	public void setRouteId(Long routeId) {
		this.routeId = routeId;
	}
	private String routeDn;
	private String ccDn;
	private String status;
	@Temporal(TemporalType.TIMESTAMP)
	private Date collectTimepoint;

	private Long routeId;
	private Long ccId;

}
