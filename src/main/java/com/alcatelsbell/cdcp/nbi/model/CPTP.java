package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.common.crud.annotation.BField;

import javax.persistence.*;

import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-13
 * Time: 下午2:34
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_PTP")

public class CPTP extends CdcpObject
{


	private String parentDn = "";


    
    @BField(description = "用户标签",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String userLabel = "";
    @BField(description = "本地EMS名称",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String nativeEMSName = "";


    private String owner = "";

    @BField(description = "扩展信息",
    		searchType = BField.SearchType.NULLABLE
    		)
    @Column(length = 1024)
    private String additionalInfo = "";
    private String speed;
    private boolean edgePoint=false;
    @BField(description = "类型",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String type="";
    @BField(description = "端口状态",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String connectionState="";
    @BField(description = "映射类型",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String tpMappingMode="";
    @BField(description = "方向",
    		searchType = BField.SearchType.NULLABLE
    		)
    private Integer direction = -1;
    @Column(length = 2048)
    private String transmissionParams = "";
    @BField(description = "速率",
    		searchType = BField.SearchType.NULLABLE
    		)
    @Transient
    private String rate = "";
    @BField(description = "保护组",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String tpProtectionAssociation = "";
    @BField(description = "端口模式",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String portMode;
    @BField(description = "mac地址",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String macAddress;
    @BField(description = "ip地址",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String ipAddress;
    @BField(description = "端口速率",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String portRate;
    @BField(description = "工作模式",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String workingMode;
    @BField(description = "ip掩码",
    		searchType = BField.SearchType.NULLABLE
    		)
    private String ipMask;
    @BField(description = "网元名称",
    		createType = BField.CreateType.REQUIRED,
    		editType = BField.EditType.REQUIRED,
    		searchType=BField.SearchType.NULLABLE,
    		mergeType = BField.MergeType.RESERVED,
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CDevice",
            dnReferenceTransietField = "deviceName",
            dnReferenceEntityField = "nativeEmsName")
    private String deviceDn;
    @Transient
    private String deviceName;
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    private Long cardid ;
    private String layerRates;
    private Integer eoType;
    private String no;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public Integer getEoType() {
        return eoType;
    }

    public void setEoType(Integer eoType) {
        this.eoType = eoType;
    }

    public String getLayerRates() {
        return layerRates;
    }

    public void setLayerRates(String layerRates) {
        this.layerRates = layerRates;
    }

    public Long getCardid() {
        return cardid;
    }

    public void setCardid(Long cardid) {
        this.cardid = cardid;
    }

    public String getDeviceDn() {
        return deviceDn;
    }

    public void setDeviceDn(String deviceDn) {
        this.deviceDn = deviceDn;
    }

    public String getIpMask() {
        return ipMask;
    }

    public void setIpMask(String ipMask) {
        this.ipMask = ipMask;
    }

    public String getWorkingMode() {
        return workingMode;
    }

    public void setWorkingMode(String workingMode) {
        this.workingMode = workingMode;
    }

    public String getPortMode() {
        return portMode;
    }

    public void setPortMode(String portMode) {
        this.portMode = portMode;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPortRate() {
        return portRate;
    }

    public void setPortRate(String portRate) {
        this.portRate = portRate;
    }

    

    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }

    /**
     * @return the edgePoint
     */
    public boolean isEdgePoint() {
        return edgePoint;
    }
    /**
     * @param edgePoint the edgePoint to set
     */
    public void setEdgePoint(boolean edgePoint) {
        this.edgePoint = edgePoint;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * @return the connectionState
     */
    public String getConnectionState() {
        return connectionState;
    }
    /**
     * @param connectionState the connectionState to set
     */
    public void setConnectionState(String connectionState) {
        this.connectionState = connectionState;
    }
    /**
     * @return the tpMappingMode
     */
    public String getTpMappingMode() {
        return tpMappingMode;
    }
    /**
     * @param tpMappingMode the tpMappingMode to set
     */
    public void setTpMappingMode(String tpMappingMode) {
        this.tpMappingMode = tpMappingMode;
    }
    /**
     * @return the direction
     */
    public Integer getDirection() {
        return direction;
    }
    /**
     * @param direction the direction to set
     */
    public void setDirection(Integer direction) {
        this.direction = direction;
    }
    /**
     * @return the transmissionParams
     */
    public String getTransmissionParams() {
        return transmissionParams;
    }
    /**
     * @param transmissionParams the transmissionParams to set
     */
    public void setTransmissionParams(String transmissionParams) {
        this.transmissionParams = transmissionParams;
    }
    /**
     * @return the tpProtectionAssociation
     */
    public String getTpProtectionAssociation() {
        return tpProtectionAssociation;
    }
    /**
     * @param tpProtectionAssociation the tpProtectionAssociation to set
     */
    public void setTpProtectionAssociation(String tpProtectionAssociation) {
        this.tpProtectionAssociation = tpProtectionAssociation;
    }

    

    /**
     * @return the userLabel
     */
    public String getUserLabel() {
        return userLabel;
    }
    /**
     * @param userLabel the userLabel to set
     */
    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }
    /**
     * @return the nativeEMSName
     */
    public String getNativeEMSName() {
        return nativeEMSName;
    }
    /**
     * @param nativeEMSName the nativeEMSName to set
     */
    public void setNativeEMSName(String nativeEMSName) {
        this.nativeEMSName = nativeEMSName;
    }


    public void setParentDn(String parentDn) {
        this.parentDn = parentDn;
    }
    public String getParentDn() {
        return parentDn;
    }
    /**
     * @return the additionalInfo
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    /**
     * @param additionalInfo the additionalInfo to set
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }
    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
    public String getRate() {
        return rate;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PTP [additionalInfo=" + additionalInfo + ", connectionState="
                + connectionState + ", direction=" + direction + ", edgePoint="
                + edgePoint + ", emsName=" + getEmsName() + ", nativeEMSName="
                + nativeEMSName + ", owner=" + owner + ", parentDn=" + parentDn
                + ", rate=" + rate + ", tpMappingMode=" + tpMappingMode
                + ", tpProtectionAssociation=" + tpProtectionAssociation
                + ", transmissionParams=" + transmissionParams + ", type="
                + type + ", userLabel=" + userLabel + "]";
    }

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
}

