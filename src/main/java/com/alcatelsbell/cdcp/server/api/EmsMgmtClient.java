package com.alcatelsbell.cdcp.server.api;

import com.alcatelsbell.cdcp.api.EmsMgmtIFC;
import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nodefx.NodeAdminMBean;
import com.alcatelsbell.cdcp.server.CdcpServerUtil;
import com.alcatelsbell.cdcp.util.MBeanProxy;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.util.NamingUtil;
import com.alcatelsbell.nms.valueobject.CdcpDictionary;
import com.alcatelsbell.nms.valueobject.sys.Ems;

import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Created by Administrator on 2015/1/6.
 */
public class EmsMgmtClient {
    private static EmsMgmtClient ourInstance = new EmsMgmtClient();

    public static EmsMgmtClient getInstance() {
        return ourInstance;
    }

    private EmsMgmtClient() {
    }

    public void createHWPTNEms(String emsname,String  host,int port,String username,String password,String node) throws RemoteException {
        EmsMgmtIFC ifc = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        Ems newEms = new Ems();

        Properties properties = new Properties();
        properties.put("emsName", emsname);
        properties.put("namingServiceIp","10.212.46.89");
        properties.put("corbaUrl","corbaloc:iiop:"+host+":"+port+"/NameService");
        properties.put("corbaTree","TMF_MTNM.Class/HUAWEI.Vendor/"+emsname+".EmsInstance/2\\.0.Version/"+emsname+".EmsSessionFactory_I");
        properties.put("corbaUserName",username);
        properties.put("corbaPassword",password);

        newEms.setName(emsname);
        newEms.setTag1("PTN");
        newEms.setSysNodeDn(node);
        newEms.setType("HWU2000");
        newEms.setVendordn("Huawei");
        newEms.setStatus(0);
        newEms.setDn(emsname);
        newEms.setProtocalType(CdcpDictionary.PROTOCALTYPE.PTN.value);
        String additionalInfo =
                "config_emsname|"+properties.get("emsName")+";"+
                        "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                        "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                        "config_corbaTree|"+properties.get("corbaTree")+";"+
                        "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                        "config_corbaPassword|"+properties.get("corbaPassword");
        newEms.setAdditionalinfo(additionalInfo);
        Ems ems = ifc.createEms(newEms);
        System.out.println("ems = " + ems.getId());

    }
    public void createZTEPTNEms(String emsname,String  host,int port,String username,String password,String node,String tag) throws RemoteException {
        if (tag == null) tag   = "OTN";
        EmsMgmtIFC ifc = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        Ems newEms = new Ems();

        Properties properties = new Properties();
        properties.put("emsName", emsname);
        properties.put("namingServiceIp","10.212.46.89");
        properties.put("corbaUrl","corbaloc:iiop:"+host+":"+port+"/NameService");
        properties.put("corbaTree","ZTE\\/T3.EMSFactory");
        properties.put("corbaUserName",username);
        properties.put("corbaPassword",password);

        newEms.setName(emsname);
        newEms.setTag1(tag);
        newEms.setSysNodeDn(node);
        newEms.setType("ZTE");
        newEms.setVendordn("ZTE");
        newEms.setStatus(0);
        newEms.setDn(emsname);
        newEms.setProtocalType(tag.equals("OTN") ? CdcpDictionary.PROTOCALTYPE.OTN.value :
                        (tag.equals("SDH") ? CdcpDictionary.PROTOCALTYPE.SDH.value : CdcpDictionary.PROTOCALTYPE.PTN.value));

                String additionalInfo =
                "config_emsname|"+properties.get("emsName")+";"+
                        "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                        "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                        "config_corbaTree|"+properties.get("corbaTree")+";"+
                        "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                        "config_corbaPassword|"+properties.get("corbaPassword");
        newEms.setAdditionalinfo(additionalInfo);
        Ems ems = ifc.createEms(newEms);
        System.out.println("ems = " + ems.getId());

    }

    public void createHWSDHEms(String emsname,String  host,int port,String username,String password) throws RemoteException {
        EmsMgmtIFC ifc = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        Ems newEms = new Ems();

        Properties properties = new Properties();
//        String emsname = "HZ-U2000-1-SDH";
//        String host = "10.70.121.165";
//        String username = "corba3";
//        String password = "Beier@123";

        properties.put("emsName", emsname);
        properties.put("namingServiceIp","10.212.46.89");

        properties.put("corbaUrl","corbaloc:iiop:"+host+":"+port+"/NameService");
        properties.put("corbaTree","TMF_MTNM.Class/HUAWEI.Vendor/"+emsname+".EmsInstance/2\\.0.Version/"+emsname+".EmsSessionFactory_I");
        properties.put("corbaUserName", username);

        properties.put("corbaPassword", password);

        newEms.setName(emsname);
        newEms.setTag1("SDH");
        newEms.setSysNodeDn("hw_node_89");
        newEms.setType("HWU2000");
        newEms.setVendordn("Huawei");
        newEms.setStatus(0);
        newEms.setDn(emsname);
        newEms.setProtocalType(CdcpDictionary.PROTOCALTYPE.SDH.value);
        String additionalInfo =
                "config_emsname|"+properties.get("emsName")+";"+
                        "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                        "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                        "config_corbaTree|"+properties.get("corbaTree")+";"+
                        "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                        "config_corbaPassword|"+properties.get("corbaPassword");
        newEms.setAdditionalinfo(additionalInfo);
        Ems ems = ifc.createEms(newEms);
        System.out.println("ems = " + ems.getId());

    }
    public void createFHSDHEms(String emsname,String host,int  port,String username,String password,String node) throws RemoteException {
        createFHOTNEms(emsname, host, port, username, password, node,"SDH");
    }

    public void createFHOTNEms(String emsname,String host,int  port,String username,String password,String node,String tag) throws RemoteException {
        if (tag == null) tag = "OTN";
        EmsMgmtIFC ifc = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        Ems newEms = new Ems();

        Properties properties = new Properties();

        properties.put("emsName", emsname);
        properties.put("namingServiceIp","10.212.46.89");
        properties.put("corbaUrl","corbaloc:iiop:"+host+":"+port+"/NameService");
        properties.put("corbaTree","WRI/EMS_1\\/SESSIONFACTORY.SESSIONFACTORY");
        properties.put("corbaUserName",username);
        properties.put("corbaPassword",password);

        newEms.setName(emsname);
        newEms.setTag1(tag);
        newEms.setSysNodeDn(node);
        newEms.setType("FH");
        newEms.setVendordn("fenghuo");
        newEms.setStatus(0);
        newEms.setDn(emsname);
        newEms.setProtocalType(tag.equals("OTN") ? CdcpDictionary.PROTOCALTYPE.OTN.value :
                (tag.equals("SDH") ? CdcpDictionary.PROTOCALTYPE.SDH.value : CdcpDictionary.PROTOCALTYPE.PTN.value)

        );
        String additionalInfo =
                "config_emsname|"+properties.get("emsName")+";"+
                        "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                        "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                        "config_corbaTree|"+properties.get("corbaTree")+";"+
                        "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                        "config_corbaPassword|"+properties.get("corbaPassword");
        newEms.setAdditionalinfo(additionalInfo);
        Ems ems = ifc.createEms(newEms);
        System.out.println("ems = " + ems.getId());

    }


    public void createHWOTNEms(String emsname,String  host,int port,String username,String password,String node) throws RemoteException {
        EmsMgmtIFC ifc = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        Ems newEms = new Ems();

        Properties properties = new Properties();
        properties.put("emsName",emsname);
        properties.put("namingServiceIp","10.212.46.89");
        properties.put("corbaUrl","corbaloc:iiop:"+host+":"+port+"/NameService");
        properties.put("corbaTree","TMF_MTNM.Class/HUAWEI.Vendor/"+emsname+".EmsInstance/2\\.0.Version/"+emsname+".EmsSessionFactory_I");
        properties.put("corbaUserName",username);
        properties.put("corbaPassword",password);

        newEms.setName(emsname);
        newEms.setTag1("OTN");
        newEms.setSysNodeDn(node);
        newEms.setType("HWU2000");
        newEms.setVendordn("Huawei");
        newEms.setStatus(0);
        newEms.setDn(emsname);
        newEms.setProtocalType(CdcpDictionary.PROTOCALTYPE.OTN.value);
        String additionalInfo =
                "config_emsname|"+properties.get("emsName")+";"+
                        "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                        "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                        "config_corbaTree|"+properties.get("corbaTree")+";"+
                        "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                        "config_corbaPassword|"+properties.get("corbaPassword");
        newEms.setAdditionalinfo(additionalInfo);
        Ems ems = ifc.createEms(newEms);
        System.out.println("ems = " + ems.getId());

    }


    public void createHWOTNEms1() throws RemoteException {
        EmsMgmtIFC ifc = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        Ems newEms = new Ems();

        Properties properties = new Properties();
        String emsname = "JX-U2000-1-OTN";
        properties.put("emsName", emsname);
        properties.put("namingServiceIp","10.212.46.89");
        properties.put("corbaUrl","corbaloc:iiop:10.73.92.252:12001/NameService");
        properties.put("corbaTree","TMF_MTNM.Class/HUAWEI.Vendor/"+emsname
                +".EmsInstance/2\\.0.Version/"+emsname+".EmsSessionFactory_I");
        properties.put("corbaUserName","corba3");
        properties.put("corbaPassword","Corba3$zj123");

        newEms.setName(emsname);
        newEms.setSysNodeDn("hw2_node_89");
        newEms.setType("HWU2000");
        newEms.setVendordn("Huawei");
        newEms.setStatus(0);
        newEms.setDn(emsname);
        newEms.setProtocalType(CdcpDictionary.PROTOCALTYPE.OTN.value);
        String additionalInfo =
                "config_emsname|"+properties.get("emsName")+";"+
                        "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                        "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                        "config_corbaTree|"+properties.get("corbaTree")+";"+
                        "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                        "config_corbaPassword|"+properties.get("corbaPassword");
        newEms.setAdditionalinfo(additionalInfo);
        Ems ems = ifc.createEms(newEms);
        System.out.println("ems = " + ems.getId());

    }
    public void createEms() throws RemoteException {
        EmsMgmtIFC ifc = (EmsMgmtIFC)NamingUtil.getAnyOneService(Constants.SERVICE_NAME_CDCP_EMS);
        Ems newEms = new Ems();

        Properties properties = new Properties();
        properties.put("emsName","ZJ-U2000-1-PTN");
        properties.put("namingServiceIp","10.212.46.82");
        properties.put("corbaUrl","corbaloc:iiop:10.212.51.117:12001/NameService");
        properties.put("corbaTree","TMF_MTNM.Class/HUAWEI.Vendor/ZJ-U2000-1-PTN.EmsInstance/2\\.0.Version/ZJ-U2000-1-PTN.EmsSessionFactory_I");
        properties.put("corbaUserName","corba1");
        properties.put("corbaPassword","Corba1#123");

        newEms.setName("ZJ-U2000-1-PTN");
        newEms.setSysNodeDn("hw_node_82");
        newEms.setType("HWU2000");
        newEms.setVendordn("Huawei");
        newEms.setStatus(0);
        newEms.setTag1("PTN");
        newEms.setDn("ZJ-U2000-1-PTN");
        newEms.setProtocalType(CdcpDictionary.PROTOCALTYPE.PTN.value);
        String additionalInfo =
                "config_emsname|"+properties.get("emsName")+";"+
                        "config_namingServiceHost|"+properties.get("namingServiceIp")+";"+
                        "config_corbaUrl|"+properties.get("corbaUrl")+";"+
                        "config_corbaTree|"+properties.get("corbaTree")+";"+
                        "config_corbaUserName|"+properties.get("corbaUserName")+";"+
                        "config_corbaPassword|"+properties.get("corbaPassword");
        newEms.setAdditionalinfo(additionalInfo);
        Ems ems = ifc.createEms(newEms);
        System.out.println("ems = " + ems.getId());
    }

    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 0 && args.length == 7) {
            String type = args[0];
            String emsdn = args[1];
            String ip = args[2];
            String port = args[3];
            String user = args[4];
            String password = args[5];
            String node = args[6];

            if (type.equalsIgnoreCase("hwsdh"))
                EmsMgmtClient.getInstance().createHWSDHEms(emsdn,ip,Integer.parseInt(port),user,password);
            else if (type.equalsIgnoreCase("hwotn"))
                EmsMgmtClient.getInstance().createHWOTNEms(emsdn,ip,Integer.parseInt(port),user,password,node);
            else if  (type.equalsIgnoreCase("hwptn"))
                EmsMgmtClient.getInstance().createHWPTNEms(emsdn,ip,Integer.parseInt(port),user,password,node);
            else if (type.equalsIgnoreCase("fhsdh"))
                EmsMgmtClient.getInstance().createFHSDHEms(emsdn,ip,Integer.parseInt(port),user,password,node);
            else if (type.equalsIgnoreCase("fhptn"))
                EmsMgmtClient.getInstance().createFHOTNEms(emsdn,ip,Integer.parseInt(port),user,password,node,"PTN");
            else if (type.equalsIgnoreCase("fhotn"))
                EmsMgmtClient.getInstance().createFHOTNEms(emsdn,ip,Integer.parseInt(port),user,password,node,"OTN");
            else if (type.equalsIgnoreCase("zteotn"))
                EmsMgmtClient.getInstance().createZTEPTNEms(emsdn,ip,Integer.parseInt(port),user,password,node,"OTN");
            else if (type.equalsIgnoreCase("zteptn"))
                EmsMgmtClient.getInstance().createZTEPTNEms(emsdn,ip,Integer.parseInt(port),user,password,node,"PTN");
            else
                System.err.println("unkown ems type : "+ type+" should be hwsdh/hwotn/hwptn/fhsdh/fhptn/fhotn/zteotn");


        } else {
            System.err.println("Bad params, params length should be 7");
        }
  //     EmsMgmtClient.getInstance().createHWSDHEms();
        //EmsMgmtClient.getInstance().createHWSDHEms("QZ-U2000-1-SDH","10.80.95.67",12003,"corba3","Corba$zj123");
//        EmsMgmtClient.getInstance().createFHOTNEms("HZ-OTNM2000-1-POTN","10.72.82.108",3075,"corba3","Corba$zj123");
//        EmsMgmtClient.getInstance().createFHOTNEms
//                (
//                        "ZJ-OTNM2000-1-OTN",
//                        "10.211.9.76",
//                        3075,
//                        "corba3",
//                        "Corba$zj123",
//                        "fh_node_89","OTN"
//                );
//
//        String emsDn = "LSH-U2000-1-SDH";
//        MBeanProxy<NodeAdminMBean> nodeAdminProxy = CdcpServerUtil.createNodeAdminProxy(emsDn);
//        Ems ems = CdcpServerUtil.findEms(emsDn);
//        nodeAdminProxy.proxy.newEms(ems);

    }
}
