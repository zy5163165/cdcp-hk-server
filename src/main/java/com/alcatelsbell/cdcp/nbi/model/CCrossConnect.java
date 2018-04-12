package com.alcatelsbell.cdcp.nbi.model;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-3
 * Time: 下午10:09
 * rongrong.chen@alcatel-sbell.com.cn
 */



import javax.persistence.*;

import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.tools.ant.taskdefs.Length;
import org.hibernate.annotations.Index;

import java.util.Date;


@Entity
@Table(name = "C_CROSSCONNECT")

public class CCrossConnect extends CdcpObject
{


    private String ccType="";
    private String administrativeState="";
    private String activeState="";
    private String direction = "";
    @Column(length = 1024)
    private String transmissionParams = "";
    private String rate="";
    private String aend = "";
    private String zend = "";
    private String aptp = "";
    private String zptp = "";
    private String aendTrans = "";
    private String zendtrans = "";
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectTimepoint;
    private Long aptpId;
    private Long zptpId;


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


    public Date getCollectTimepoint() {
        return collectTimepoint;
    }

    public void setCollectTimepoint(Date collectTimepoint) {
        this.collectTimepoint = collectTimepoint;
    }
    /**
     * @return the ccType
     */
    public String getCcType() {
        return ccType;
    }
    /**
     * @param ccType the ccType to set
     */
    public void setCcType(String ccType) {
        this.ccType = ccType;
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
    public String getDirection() {
        return direction;
    }
    /**
     * @param direction the direction to set
     */
    public void setDirection(String direction) {
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


    private String parentDn = "";



    private String userLabel = "";

    private String nativeEMSName = "";


    private String owner = "";


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

    public void setRate(String rate) {
        this.rate = rate;
    }
    public String getRate() {
        return rate;
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

    @Override
    public String toString() {
        return "CCrossConnect{" +
                "ccType='" + ccType + '\'' +
                ", administrativeState='" + administrativeState + '\'' +
                ", activeState='" + activeState + '\'' +
                ", direction='" + direction + '\'' +
                ", transmissionParams='" + transmissionParams + '\'' +
                ", rate='" + rate + '\'' +
                ", aend='" + aend + '\'' +
                ", zend='" + zend + '\'' +
                ", aptp='" + aptp + '\'' +
                ", zptp='" + zptp + '\'' +
                ", aendTrans='" + aendTrans + '\'' +
                ", zendtrans='" + zendtrans + '\'' +
                ", parentDn='" + parentDn + '\'' +
                ", emsName='" + getEmsName() + '\'' +
                ", userLabel='" + userLabel + '\'' +
                ", nativeEMSName='" + nativeEMSName + '\'' +
                ", owner='" + owner + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }


}
