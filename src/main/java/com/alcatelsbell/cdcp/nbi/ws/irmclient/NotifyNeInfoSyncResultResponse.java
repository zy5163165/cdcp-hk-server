
package com.alcatelsbell.cdcp.nbi.ws.irmclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="notifyNeInfoSyncResultReturn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "notifyNeInfoSyncResultReturn"
})
@XmlRootElement(name = "notifyNeInfoSyncResultResponse")
public class NotifyNeInfoSyncResultResponse {

    @XmlElement(required = true)
    protected String notifyNeInfoSyncResultReturn;

    /**
     * Gets the value of the notifyNeInfoSyncResultReturn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotifyNeInfoSyncResultReturn() {
        return notifyNeInfoSyncResultReturn;
    }

    /**
     * Sets the value of the notifyNeInfoSyncResultReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotifyNeInfoSyncResultReturn(String value) {
        this.notifyNeInfoSyncResultReturn = value;
    }

}
