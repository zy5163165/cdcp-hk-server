package com.alcatelsbell.cdcp.common.model;

import java.util.Date;

import com.alcatelsbell.nms.common.crud.annotation.BField;

public class EmsBenchmarkList {
	
	
	                private Long id;
	                private Date updatedate;
	                private Long version;
	                public Long getVersion() {
						return version;
					}
					public void setVersion(Long version) {
						this.version = version;
					}
					public Date getUpdatedate() {
						return updatedate;
					}
					public void setUpdatedate(Date updatedate) {
						this.updatedate = updatedate;
					}
					public Long getId() {
						return id;
					}
					public void setId(Long id) {
						this.id = id;
					}
					private String emsname;
	                private String additinalInfo;
	                private Integer status; 
	                
	                
	                private String tableName;
				    private Integer count;
				    private Integer dvpercentage; // 偏移百分比
				    private String benchmarkDn;
				    
				    
				    @BField(description = "NE总数")
				    private Integer neCount;
				    @BField(description = "SLOT总数")
				    private Integer slotCount;
				    @BField(description = "SUBSLOT总数")
				    private Integer subSlotCount;
				    @BField(description = "EQUIPMENT总数")
				    private Integer equipmentCount;
				    @BField(description = "PTP总数")
				    private Integer ptpCount;
				    @BField(description = "FTP总数")
				    private Integer ftpCount;
				    @BField(description = "SECTION总数")
				    private Integer sectionCount;
				    @BField(description = "TUNNEL总数")
				    private Integer tunnelCount;
				    @BField(description = "PW总数")
				    private Integer pwCount;
				    @BField(description = "PWE3总数")
				    private Integer pwe3Count;
				    @BField(description = "ROUTE总数")
				    private Integer routeCount;

				    @BField(description = "TUNNELPG总数")
				    private Integer tunnelPG;

				    @BField(description = "网元交叉总数")
				    private Integer ccCount;
				    @BField(description = "子网交叉总数")
				    private Integer sncCount;
				    
				    @BField(description = "CTP总数")
				    private Integer ctpCount;

				    
				    
				    private Integer neCountTage; ///NE总数的偏差率
				    private Integer slotCountTage; ///slotCount总数的偏差率
				    private Integer subSlotCountTage; ///subSlotCount总数的偏差率
				    private Integer equipmentCountTage; ///equipmentCount总数的偏差率
				    private Integer ptpCountTage; ///ptpCount总数的偏差率
				    private Integer ftpCountTage; ///ftpCount总数的偏差率
				    private Integer sectionCountTage; ///sectionCount总数的偏差率
				    private Integer tunnelCountTage; ///tunnelCount总数的偏差率
				    private Integer pwCountTage; ///pwCount总数的偏差率
				    private Integer pwe3CountTage; ///pwe3Count总数的偏差率
				    private Integer routeCountTage; ///routeCount总数的偏差率
				    private Integer tunnelPGTage; ///tunnelPG总数的偏差率
				    private Integer ccCountTage; ///ccCount总数的偏差率
				    private Integer sncCountTage; ///sncCount总数的偏差率
				    private Integer ctpCountTage; ///ctpCount总数的偏差率
				    
				    
				    
				    private Long neID;
				    private Long slotID;
				    private Long subSlotID;
				    private Long equipmentID;
				    private Long ptpID;
				    private Long ftpID;
				    private Long sectionID;
				    private Long tunnelID;
				    private Long pwID;
				    private Long pwe3ID;
				    private Long routeID;
				    private Long tunnelPGID;
				    private Long ccID;
				    private Long sncID;
				    private Long ctpID;
				    
				    
				   
					public Long getNeID() {
						return neID;
					}
					public void setNeID(Long neID) {
						this.neID = neID;
					}
					public Long getSlotID() {
						return slotID;
					}
					public void setSlotID(Long slotID) {
						this.slotID = slotID;
					}
					public Long getSubSlotID() {
						return subSlotID;
					}
					public void setSubSlotID(Long subSlotID) {
						this.subSlotID = subSlotID;
					}
					
					public Long getEquipmentID() {
						return equipmentID;
					}
					public void setEquipmentID(Long equipmentID) {
						this.equipmentID = equipmentID;
					}
					public Long getPtpID() {
						return ptpID;
					}
					public void setPtpID(Long ptpID) {
						this.ptpID = ptpID;
					}
					public Long getFtpID() {
						return ftpID;
					}
					public void setFtpID(Long ftpID) {
						this.ftpID = ftpID;
					}
					public Long getSectionID() {
						return sectionID;
					}
					public void setSectionID(Long sectionID) {
						this.sectionID = sectionID;
					}
					public Long getTunnelID() {
						return tunnelID;
					}
					public void setTunnelID(Long tunnelID) {
						this.tunnelID = tunnelID;
					}
					public Long getPwID() {
						return pwID;
					}
					public void setPwID(Long pwID) {
						this.pwID = pwID;
					}
					public Long getPwe3ID() {
						return pwe3ID;
					}
					public void setPwe3ID(Long pwe3id) {
						pwe3ID = pwe3id;
					}
					public Long getRouteID() {
						return routeID;
					}
					public void setRouteID(Long routeID) {
						this.routeID = routeID;
					}
					public Long getTunnelPGID() {
						return tunnelPGID;
					}
					public void setTunnelPGID(Long tunnelPGID) {
						this.tunnelPGID = tunnelPGID;
					}
					public Long getCcID() {
						return ccID;
					}
					public void setCcID(Long ccID) {
						this.ccID = ccID;
					}
					public Long getSncID() {
						return sncID;
					}
					public void setSncID(Long sncID) {
						this.sncID = sncID;
					}
					public Long getCtpID() {
						return ctpID;
					}
					public void setCtpID(Long ctpID) {
						this.ctpID = ctpID;
					}
					public Integer getNeCount() {
						return neCount;
					}
					public void setNeCount(Integer neCount) {
						this.neCount = neCount;
					}
					public Integer getSlotCount() {
						return slotCount;
					}
					public void setSlotCount(Integer slotCount) {
						this.slotCount = slotCount;
					}
					public Integer getSubSlotCount() {
						return subSlotCount;
					}
					public void setSubSlotCount(Integer subSlotCount) {
						this.subSlotCount = subSlotCount;
					}
					public Integer getEquipmentCount() {
						return equipmentCount;
					}
					public void setEquipmentCount(Integer equipmentCount) {
						this.equipmentCount = equipmentCount;
					}
					public Integer getPtpCount() {
						return ptpCount;
					}
					public void setPtpCount(Integer ptpCount) {
						this.ptpCount = ptpCount;
					}
					public Integer getFtpCount() {
						return ftpCount;
					}
					public void setFtpCount(Integer ftpCount) {
						this.ftpCount = ftpCount;
					}
					public Integer getSectionCount() {
						return sectionCount;
					}
					public void setSectionCount(Integer sectionCount) {
						this.sectionCount = sectionCount;
					}
					public Integer getTunnelCount() {
						return tunnelCount;
					}
					public void setTunnelCount(Integer tunnelCount) {
						this.tunnelCount = tunnelCount;
					}
					public Integer getPwCount() {
						return pwCount;
					}
					public void setPwCount(Integer pwCount) {
						this.pwCount = pwCount;
					}
					public Integer getPwe3Count() {
						return pwe3Count;
					}
					public void setPwe3Count(Integer pwe3Count) {
						this.pwe3Count = pwe3Count;
					}
					public Integer getRouteCount() {
						return routeCount;
					}
					public void setRouteCount(Integer routeCount) {
						this.routeCount = routeCount;
					}
					public Integer getTunnelPG() {
						return tunnelPG;
					}
					public void setTunnelPG(Integer tunnelPG) {
						this.tunnelPG = tunnelPG;
					}
					public Integer getCcCount() {
						return ccCount;
					}
					public void setCcCount(Integer ccCount) {
						this.ccCount = ccCount;
					}
					public Integer getSncCount() {
						return sncCount;
					}
					public void setSncCount(Integer sncCount) {
						this.sncCount = sncCount;
					}
					public Integer getCtpCount() {
						return ctpCount;
					}
					public void setCtpCount(Integer ctpCount) {
						this.ctpCount = ctpCount;
					}
					public Integer getNeCountTage() {
						return neCountTage;
					}
					public void setNeCountTage(Integer neCountTage) {
						this.neCountTage = neCountTage;
					}
					public Integer getSlotCountTage() {
						return slotCountTage;
					}
					public void setSlotCountTage(Integer slotCountTage) {
						this.slotCountTage = slotCountTage;
					}
					public Integer getSubSlotCountTage() {
						return subSlotCountTage;
					}
					public void setSubSlotCountTage(Integer subSlotCountTage) {
						this.subSlotCountTage = subSlotCountTage;
					}
					public Integer getEquipmentCountTage() {
						return equipmentCountTage;
					}
					public void setEquipmentCountTage(Integer equipmentCountTage) {
						this.equipmentCountTage = equipmentCountTage;
					}
					public Integer getPtpCountTage() {
						return ptpCountTage;
					}
					public void setPtpCountTage(Integer ptpCountTage) {
						this.ptpCountTage = ptpCountTage;
					}
					public Integer getFtpCountTage() {
						return ftpCountTage;
					}
					public void setFtpCountTage(Integer ftpCountTage) {
						this.ftpCountTage = ftpCountTage;
					}
					public Integer getSectionCountTage() {
						return sectionCountTage;
					}
					public void setSectionCountTage(Integer sectionCountTage) {
						this.sectionCountTage = sectionCountTage;
					}
					public Integer getTunnelCountTage() {
						return tunnelCountTage;
					}
					public void setTunnelCountTage(Integer tunnelCountTage) {
						this.tunnelCountTage = tunnelCountTage;
					}
					public Integer getPwCountTage() {
						return pwCountTage;
					}
					public void setPwCountTage(Integer pwCountTage) {
						this.pwCountTage = pwCountTage;
					}
					public Integer getPwe3CountTage() {
						return pwe3CountTage;
					}
					public void setPwe3CountTage(Integer pwe3CountTage) {
						this.pwe3CountTage = pwe3CountTage;
					}
					public Integer getRouteCountTage() {
						return routeCountTage;
					}
					public void setRouteCountTage(Integer routeCountTage) {
						this.routeCountTage = routeCountTage;
					}
					public Integer getTunnelPGTage() {
						return tunnelPGTage;
					}
					public void setTunnelPGTage(Integer tunnelPGTage) {
						this.tunnelPGTage = tunnelPGTage;
					}
					public Integer getCcCountTage() {
						return ccCountTage;
					}
					public void setCcCountTage(Integer ccCountTage) {
						this.ccCountTage = ccCountTage;
					}
					public Integer getSncCountTage() {
						return sncCountTage;
					}
					public void setSncCountTage(Integer sncCountTage) {
						this.sncCountTage = sncCountTage;
					}
					public Integer getCtpCountTage() {
						return ctpCountTage;
					}
					public void setCtpCountTage(Integer ctpCountTage) {
						this.ctpCountTage = ctpCountTage;
					}
					
				    public String getEmsname() {
						return emsname;
					}
					public void setEmsname(String emsname) {
						this.emsname = emsname;
					}
					public String getAdditinalInfo() {
						return additinalInfo;
					}
					public void setAdditinalInfo(String additinalInfo) {
						this.additinalInfo = additinalInfo;
					}
					public Integer getStatus() {
						return status;
					}
					public void setStatus(Integer status) {
						this.status = status;
					}
					public String getTableName() {
						return tableName;
					}
					public void setTableName(String tableName) {
						this.tableName = tableName;
					}
					public Integer getCount() {
						return count;
					}
					public void setCount(Integer count) {
						this.count = count;
					}
					public Integer getDvpercentage() {
						return dvpercentage;
					}
					public void setDvpercentage(Integer dvpercentage) {
						this.dvpercentage = dvpercentage;
					}
					public String getBenchmarkDn() {
						return benchmarkDn;
					}
					public void setBenchmarkDn(String benchmarkDn) {
						this.benchmarkDn = benchmarkDn;
					}
					
}
