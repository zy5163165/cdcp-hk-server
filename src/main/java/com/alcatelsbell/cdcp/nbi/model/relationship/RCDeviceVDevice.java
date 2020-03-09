package com.alcatelsbell.cdcp.nbi.model.relationship;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.cdcp.nbi.model.virtualentity.VDevice;
import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;
import com.alcatelsbell.nms.valueobject.physical.config.RackType;

@Entity
@Table(name = "R_CDEVICEVDEVICE")
public class RCDeviceVDevice extends BObject {

	@BField(description = "网元名称", createType = BField.CreateType.REQUIRED, editType = BField.EditType.REQUIRED, mergeType = BField.MergeType.RESERVED, dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CDevice", dnReferenceTransietField = "cDeviceNativeEmsName", dnReferenceEntityField = "nativeEmsName")
	private String cDeviceDn;

	@Transient
	private String cDeviceNativeEmsName;

	@BField(description = "网元EMS名称", searchType = BField.SearchType.NULLABLE)
	private String cDeviceEMSName;

	@BField(description = "虚拟网元名称", createType = BField.CreateType.REQUIRED, editType = BField.EditType.REQUIRED, mergeType = BField.MergeType.RESERVED, dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.virtualentity.VDevice", dnReferenceTransietField = "vDeviceNativeEmsName", dnReferenceEntityField = "nativeEmsName")
	private String vDeviceDn;
	
	@BField(description = "网元主次", searchType = BField.SearchType.NULLABLE)
	private String cDevicePrimaryType;

	@Transient
	private String vDeviceNativeEmsName;

	public String getcDeviceDn() {
		return cDeviceDn;
	}

	public void setcDeviceDn(String cDeviceDn) {
		this.cDeviceDn = cDeviceDn;
	}

	public String getcDeviceNativeEmsName() {
		return cDeviceNativeEmsName;
	}

	public void setcDeviceNativeEmsName(String cDeviceNativeEmsName) {
		this.cDeviceNativeEmsName = cDeviceNativeEmsName;
	}

	public String getcDeviceEMSName() {
		return cDeviceEMSName;
	}

	public void setcDeviceEMSName(String cDeviceEMSName) {
		this.cDeviceEMSName = cDeviceEMSName;
	}

	public String getvDeviceDn() {
		return vDeviceDn;
	}

	public void setvDeviceDn(String vDeviceDn) {
		this.vDeviceDn = vDeviceDn;
	}

	public String getvDeviceNativeEmsName() {
		return vDeviceNativeEmsName;
	}

	public void setvDeviceNativeEmsName(String vDeviceNativeEmsName) {
		this.vDeviceNativeEmsName = vDeviceNativeEmsName;
	}

	public String getcDevicePrimaryType() {
		return cDevicePrimaryType;
	}

	public void setcDevicePrimaryType(String cDevicePrimaryType) {
		this.cDevicePrimaryType = cDevicePrimaryType;
	}
	
	
}
