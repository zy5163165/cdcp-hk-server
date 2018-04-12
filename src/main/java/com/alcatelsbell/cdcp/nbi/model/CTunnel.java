package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import javax.persistence.*;

import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-3
 * Time: 下午2:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_TUNNEL")

public class CTunnel extends CdcpObject
{


    private String rerouteAllowed="";
    private String administrativeState="";
    private String activeState="";
    private Integer direction = -1;

    @Column(length = 1024)
    private String transmissionParams = "";
    private String networkRouted = "";

	private String aptps = "";
	private String zptps = "";
    private String aend = "";
    private String zend = "";
    private String aptp = "";
    private String zptp = "";
    private Long aptpId;
    private Long zptpId;

    @Column(length = 1024)
    private String aendTrans = "";
    @Column(length = 1024)
    private String zendtrans = "";
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;

    private String cir;
    private String pir;
    private String aingressLabel;
    private String zingressLabel;
    private String aegressLabel;
    private String zegressLabel;

    public String getCir() {
        return cir;
    }

    public void setCir(String cir) {
        this.cir = cir;
    }

    public String getPir() {
        return pir;
    }

    public void setPir(String pir) {
        this.pir = pir;
    }

    public String getAingressLabel() {
        return aingressLabel;
    }

    public void setAingressLabel(String aingressLabel) {
        this.aingressLabel = aingressLabel;
    }

    public String getZingressLabel() {
        return zingressLabel;
    }

    public void setZingressLabel(String zingressLabel) {
        this.zingressLabel = zingressLabel;
    }

    public String getAegressLabel() {
        return aegressLabel;
    }

    public void setAegressLabel(String aegressLabel) {
        this.aegressLabel = aegressLabel;
    }

    public String getZegressLabel() {
        return zegressLabel;
    }

    public void setZegressLabel(String zegressLabel) {
        this.zegressLabel = zegressLabel;
    }

    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }

    public Long getAptpId() {
        return aptpId;
    }

    public void setAptpId(Long aptpId) {
        this.aptpId = aptpId;
    }

    public Long getZptpId() {
        return zptpId;
    }

    public void setZptpId(Long zptpId) {
        this.zptpId = zptpId;
    }

    /**
     * @return the rerouteAllowed
     */
    public String getRerouteAllowed() {
        return rerouteAllowed;
    }
    /**
     * @param rerouteAllowed the rerouteAllowed to set
     */
    public void setRerouteAllowed(String rerouteAllowed) {
        this.rerouteAllowed = rerouteAllowed;
    }
    /**
     * @return the administrativeState
     */
    public String getAdministrativeState() {
        return administrativeState;
    }
    /**
     * @param administrativeState the administrativeState to set
     */
    public void setAdministrativeState(String administrativeState) {
        this.administrativeState = administrativeState;
    }
    /**
     * @return the activeState
     */
    public String getActiveState() {
        return activeState;
    }
    /**
     * @param activeState the activeState to set
     */
    public void setActiveState(String activeState) {
        this.activeState = activeState;
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
     * @return the networkRouted
     */
    public String getNetworkRouted() {
        return networkRouted;
    }
    /**
     * @param networkRouted the networkRouted to set
     */
    public void setNetworkRouted(String networkRouted) {
        this.networkRouted = networkRouted;
    }

    private String parentDn = "";



    private String userLabel = "";

    private String nativeEMSName = "";


    private String owner = "";

    @Column(length = 1024)
    private String additionalInfo = "";

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
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TrafficTrunk [aEnd=" + aend + ", aEndTrans=" + aendTrans
                + ", activeState=" + activeState + ", additionalInfo="
                + additionalInfo + ", administrativeState="
                + administrativeState + ", direction=" + direction
                + ", emsName=" + getEmsName() + ", nativeEMSName=" + nativeEMSName
                + ", networkRouted=" + networkRouted + ", owner=" + owner
                + ", parentDn=" + parentDn + ", rerouteAllowed="
                + rerouteAllowed + ", transmissionParams=" + transmissionParams
                + ", userLabel=" + userLabel + ", zEnd=" + zend
                + ", zEndtrans=" + zendtrans + "]";
    }


    public String getAend() {
        return aend;
    }

    public void setAend(String aend) {
        this.aend = aend;
    }

    public String getZend() {
        return zend;
    }

    public void setZend(String zend) {
        this.zend = zend;
    }

    public String getAptp() {
        return aptp;
    }

    public void setAptp(String aptp) {
        this.aptp = aptp;
    }

    public String getZptp() {
        return zptp;
    }

    public void setZptp(String zptp) {
        this.zptp = zptp;
    }

    public String getAendTrans() {
        return aendTrans;
    }

    public void setAendTrans(String aendTrans) {
        this.aendTrans = aendTrans;
    }

    public String getZendtrans() {
        return zendtrans;
    }

    public void setZendtrans(String zendtrans) {
        this.zendtrans = zendtrans;
    }
    
    public String getAptps() {
		return aptps;
	}

	public void setAptps(String aptps) {
		this.aptps = aptps;
	}

	public String getZptps() {
		return zptps;
	}

	public void setZptps(String zptps) {
		this.zptps = zptps;
	}
}
