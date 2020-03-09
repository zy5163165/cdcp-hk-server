package com.alcatelsbell.cdcp.nbi.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "C_PATH_CHANNEL")
public class CPath_Channel  extends CdcpObject{
    public String getPathDn() {
		return pathDn;
	}
	public void setPathDn(String pathDn) {
		this.pathDn = pathDn;
	}
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
	public Long getPathId() {
		return pathId;
	}
	public void setPathId(Long pathId) {
		this.pathId = pathId;
	}
	public Long getChannelId() {
		return channelId;
	}
	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}
	private String pathDn;
    private String channelDn;
    private String status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    
    private Long pathId;
    private Long channelId;

}
