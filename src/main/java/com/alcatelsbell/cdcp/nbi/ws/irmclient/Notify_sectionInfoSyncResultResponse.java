
package com.alcatelsbell.cdcp.nbi.ws.irmclient;

import javax.xml.bind.annotation.*;


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
    "notify_sectionInfoSyncResultReturn"
})
@XmlRootElement(name = "notify_sectionInfoSyncResultResponse")
public class Notify_sectionInfoSyncResultResponse {

    @XmlElement(required = true)
    protected String notify_sectionInfoSyncResultReturn;

    /**
     * Gets the value of the notifyNeInfoSyncResultReturn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotify_sectionInfoSyncResultReturn() {
        return notify_sectionInfoSyncResultReturn;
    }

    /**
     * Sets the value of the notifyNeInfoSyncResultReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotify_sectionInfoSyncResultReturn(String value) {
        this.notify_sectionInfoSyncResultReturn = value;
    }

}
