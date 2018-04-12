package com.alcatelsbell.cdcp.nbi.model;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-10
 * Time: 上午9:42
 * rongrong.chen@alcatel-sbell.com.cn
 */




import javax.persistence.*;

import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import java.util.Date;


@Entity
@Table(name = "C_CTP", schema = "CDCP5")

public class CCTP extends CdcpObject
{

    @Index(name = "IDX_CCTP_PORTDN")
	private String portdn="";
	private String parentCtpdn="";
	private String value="";

    private boolean edgePoint=false;
    private String type="";
    private String connectionState="";
    private String tpMappingMode="";
    private Integer direction = -1;

    @Column(length = 2048)
    private String transmissionParams = "";
    private String tpProtectionAssociation = "";
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    private String rate = "";
    private String j;
    private String k;
    private String l;
    private String m;
    private String frequencies;


    private String tmRate;
    private String rateDesc;

    public String getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(String frequencies) {
        this.frequencies = frequencies;
    }

    public String getRateDesc() {
        return rateDesc;
    }

    public void setRateDesc(String rateDesc) {
        this.rateDesc = rateDesc;
    }

    public String getJ() {
        return j;
    }

    public void setJ(String j) {
        this.j = j;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public String getL() {
        return l;
    }

    public void setL(String l) {
        this.l = l;
    }

    public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }



    public String getTmRate() {
        return tmRate;
    }

    public void setTmRate(String tmRate) {
        this.tmRate = tmRate;
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

    private String parentDn = "";


    private String userLabel = "";

    private String nativeEMSName = "";


    private String owner = "";

    @Column(length = 1024)
    private String additionalInfo = "";
    /**
     * @return the emsName
     */

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
    
	public String getPortdn() {
		return portdn;
	}

	public void setPortdn(String portdn) {
		this.portdn = portdn;
	}

	public String getParentCtpdn() {
		return parentCtpdn;
	}

	public void setParentCtpdn(String parentCtpdn) {
		this.parentCtpdn = parentCtpdn;
	}

	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
