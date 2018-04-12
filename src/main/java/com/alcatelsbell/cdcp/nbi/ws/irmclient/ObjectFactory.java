
package com.alcatelsbell.cdcp.nbi.ws.irmclient;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.alcatelsbell.cdcp.nbi.ws.irmclient package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.alcatelsbell.cdcp.nbi.ws.irmclient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NotifyNeInfoSyncResult }
     * 
     */
    public NotifyNeInfoSyncResult createNotifyNeInfoSyncResult() {
        return new NotifyNeInfoSyncResult();
    }

    /**
     * Create an instance of {@link NotifyNeInfoSyncResultResponse }
     * 
     */
    public NotifyNeInfoSyncResultResponse createNotifyNeInfoSyncResultResponse() {
        return new NotifyNeInfoSyncResultResponse();
    }


    /**
     * Create an instance of {@link NotifyNeInfoSyncResult }
     *
     */
    public Notify_sectionInfoSyncResult createNotify_sectionInfoSyncResult() {
        return new Notify_sectionInfoSyncResult();
    }

    /**
     * Create an instance of {@link NotifyNeInfoSyncResultResponse }
     *
     */
    public Notify_sectionInfoSyncResultResponse createNotify_sectionInfoSyncResultResponse() {
        return new Notify_sectionInfoSyncResultResponse();
    }


}
