/**
 * NbiSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.alcatelsbell.cdcp.nbi.ws.axis14;

import com.alcatelsbell.cdcp.api.TaskMgmtIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.nms.util.NamingUtil;

public class NbiSoapBindingImpl implements CdcpNBIService3{
    public void main(String[] args) throws java.rmi.RemoteException {
    }

    public String synchronizeNeInfoInEms(String request) throws java.rmi.RemoteException {
        return ((TaskMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_TASK)).synchronizeNeInfoInEms(request);

    }

    public String getNeListInEms(String request) throws java.rmi.RemoteException {
        return ((TaskMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_TASK)).getNeListInEms(request);
    }

}
