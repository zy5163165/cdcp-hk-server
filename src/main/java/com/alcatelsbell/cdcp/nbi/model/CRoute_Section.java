package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "C_ROUTE_SECTION")
public class CRoute_Section  extends CdcpObject{
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
	public String getSectionDn() {
		return sectionDn;
	}
	public void setSectionDn(String sectionDn) {
		this.sectionDn = sectionDn;
	}
	public Long getSectionId() {
		return sectionId;
	}
	public void setSectionId(Long sectionId) {
		this.sectionId = sectionId;
	}
	private String routeDn;
    private String sectionDn;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    
    private Long routeId;
    private Long sectionId;

}
