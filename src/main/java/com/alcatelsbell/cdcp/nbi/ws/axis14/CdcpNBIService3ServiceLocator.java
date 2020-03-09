/**
 * CdcpNBIService3ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.alcatelsbell.cdcp.nbi.ws.axis14;

public class CdcpNBIService3ServiceLocator extends org.apache.axis.client.Service implements CdcpNBIService3Service {

    public CdcpNBIService3ServiceLocator() {
    }


    public CdcpNBIService3ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public CdcpNBIService3ServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for nbi
    private String nbi_address = "http://localhost:8080/axis/services/nbi";

    public String getnbiAddress() {
        return nbi_address;
    }

    // The WSDD service name defaults to the port name.
    private String nbiWSDDServiceName = "nbi";

    public String getnbiWSDDServiceName() {
        return nbiWSDDServiceName;
    }

    public void setnbiWSDDServiceName(String name) {
        nbiWSDDServiceName = name;
    }

    public CdcpNBIService3 getnbi() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(nbi_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getnbi(endpoint);
    }

    public CdcpNBIService3 getnbi(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            NbiSoapBindingStub _stub = new NbiSoapBindingStub(portAddress, this);
            _stub.setPortName(getnbiWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setnbiEndpointAddress(String address) {
        nbi_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (CdcpNBIService3.class.isAssignableFrom(serviceEndpointInterface)) {
                NbiSoapBindingStub _stub = new NbiSoapBindingStub(new java.net.URL(nbi_address), this);
                _stub.setPortName(getnbiWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("nbi".equals(inputPortName)) {
            return getnbi();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://ws.nbi.cdcp.alcatelsbell.com", "CdcpNBIService3Service");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://ws.nbi.cdcp.alcatelsbell.com", "nbi"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {
        
if ("nbi".equals(portName)) {
            setnbiEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
