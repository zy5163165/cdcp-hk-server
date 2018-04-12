package com.alcatelsbell.cdcp.nbi.model.virtualentity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.alcatelsbell.cdcp.nbi.model.CSection;
import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;


@Entity
@Table(name = "V_Section")
public class VSection extends BObject {
	@BField(description = "A端端口",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		searchType=BField.SearchType.NULLABLE,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CPTP",
            dnReferenceTransietField = "APTPDN",
            dnReferenceEntityField = "dn")
    private String aendTp;

    @Transient
    private String APTPDN;
    @BField(description = "A端EMS名称",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		searchType=BField.SearchType.NULLABLE,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CEMS",
            dnField="name")
    private String AEMSName;

    @BField(description = "A端网元名称",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		searchType=BField.SearchType.NULLABLE,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CDevice",
            dnReferenceTransietField = "ADeviceName",
            dnReferenceEntityField = "nativeEmsName")
    private String ADeviceDn;
    
    @Transient
    private String ADeviceName;
    
    
    @BField(description = "Z端端口",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		searchType=BField.SearchType.NULLABLE,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CPTP",
            dnReferenceTransietField = "ZPTPDN",
            dnReferenceEntityField = "dn")
    private String zendTp = "";
    @Transient
    private String ZPTPDN;
    @BField(description = "Z端EMS名称",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		searchType=BField.SearchType.NULLABLE,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CEMS",
            dnField="name")
    private String ZEMSName;
    @BField(description = "Z端网元名称",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		searchType=BField.SearchType.NULLABLE,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CDevice",
            dnReferenceTransietField = "ZDeviceName",
            dnReferenceEntityField = "nativeEmsName")
    private String ZDeviceDn;
    @Transient
    private String ZDeviceName;
	public String getAendTp() {
		return aendTp;
	}
	public void setAendTp(String aendTp) {
		this.aendTp = aendTp;
	}
	public String getAPTPDN() {
		return APTPDN;
	}
	public void setAPTPDN(String aPTPDN) {
		APTPDN = aPTPDN;
	}
	public String getZendTp() {
		return zendTp;
	}
	public void setZendTp(String zendTp) {
		this.zendTp = zendTp;
	}
	public String getZPTPDN() {
		return ZPTPDN;
	}
	public void setZPTPDN(String zPTPDN) {
		ZPTPDN = zPTPDN;
	}
	public String getAEMSName() {
		return AEMSName;
	}
	public void setAEMSName(String aEMSName) {
		AEMSName = aEMSName;
	}
	public String getADeviceDn() {
		return ADeviceDn;
	}
	public void setADeviceDn(String aDeviceDn) {
		ADeviceDn = aDeviceDn;
	}
	public String getZEMSName() {
		return ZEMSName;
	}
	public void setZEMSName(String zEMSName) {
		ZEMSName = zEMSName;
	}
	public String getZDeviceDn() {
		return ZDeviceDn;
	}
	public void setZDeviceDn(String zDeviceDn) {
		ZDeviceDn = zDeviceDn;
	}
	public String getADeviceName() {
		return ADeviceName;
	}
	public void setADeviceName(String aDeviceName) {
		ADeviceName = aDeviceName;
	}
	public String getZDeviceName() {
		return ZDeviceName;
	}
	public void setZDeviceName(String zDeviceName) {
		ZDeviceName = zDeviceName;
	}
	
}
