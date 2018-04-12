/**
 * NbiSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.alcatelsbell.cdcp.nbi.ws.axis14;

public class NbiSoapBindingSkeleton implements CdcpNBIService3, org.apache.axis.wsdl.Skeleton {
    private CdcpNBIService3 impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "args"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ws.nbi.cdcp.alcatelsbell.com", "ArrayOf_soapenc_string"), String[].class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("main", _params, null);
        _oper.setElementQName(new javax.xml.namespace.QName("http://ws.nbi.cdcp.alcatelsbell.com", "main"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("main") == null) {
            _myOperations.put("main", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("main")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "request"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("synchronizeNeInfoInEms", _params, new javax.xml.namespace.QName("", "synchronizeNeInfoInEmsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://ws.nbi.cdcp.alcatelsbell.com", "synchronizeNeInfoInEms"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("synchronizeNeInfoInEms") == null) {
            _myOperations.put("synchronizeNeInfoInEms", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("synchronizeNeInfoInEms")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "request"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), String.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getNeListInEms", _params, new javax.xml.namespace.QName("", "getNeListInEmsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        _oper.setElementQName(new javax.xml.namespace.QName("http://ws.nbi.cdcp.alcatelsbell.com", "getNeListInEms"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getNeListInEms") == null) {
            _myOperations.put("getNeListInEms", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getNeListInEms")).add(_oper);
    }

    public NbiSoapBindingSkeleton() {
        this.impl = new NbiSoapBindingImpl();
    }

    public NbiSoapBindingSkeleton(CdcpNBIService3 impl) {
        this.impl = impl;
    }
    public void main(String[] args) throws java.rmi.RemoteException
    {
        impl.main(args);
    }

    public String synchronizeNeInfoInEms(String request) throws java.rmi.RemoteException
    {
        String ret = impl.synchronizeNeInfoInEms(request);
        return ret;
    }

    public String getNeListInEms(String request) throws java.rmi.RemoteException
    {
        String ret = impl.getNeListInEms(request);
        return ret;
    }

}
