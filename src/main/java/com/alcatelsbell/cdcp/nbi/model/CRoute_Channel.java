package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "C_ROUTE_CHANNEL")
public class CRoute_Channel  extends CdcpObject{
	public String getChannelDn() {
		return channelDn;
	}
	public void setChannelDn(String channelDn) {
		this.channelDn = channelDn;
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
	public Long getChannelId() {
		return channelId;
	}
	public void setChannelId(Long channelId) {
		this.channelId = channelId;
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
    private String channelDn;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    
    private Long routeId;
    private Long channelId;

}
