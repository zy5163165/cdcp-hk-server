package com.alcatelsbell.cdcp.server;

import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.cdcp.nbi.model.relationship.RCDeviceVDevice;
import com.alcatelsbell.cdcp.nbi.model.virtualentity.VDevice;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2015/9/7
 * Time: 13:13
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class     VDeviceClient {
    private Log logger = LogFactory.getLog(getClass());

    /**
     * 网元A	所属EMS	网元B	所属EMS	主EMS
     3815-上塘枢纽楼复用器12    HZ-T2000-1	3815-上塘枢纽楼复用器12	HZ-T2000-2	HZ-T2000-1
     99-上塘枢纽楼2	HZ-T2000-1	99-上塘枢纽楼2	HZ-T2000-2	HZ-T2000-1
     3816-上塘枢纽楼4	HZ-T2000-1	3816-上塘枢纽楼4	HZ-T2000-2	HZ-T2000-1
     3828-学院路13	HZ-T2000-1	3828-学院路13	HZ-T2000-2	HZ-T2000-1
     3720-三墩1	HZ-T2000-1	3720-三墩1	HZ-T2000-2	HZ-T2000-1
     3744-上塘枢纽楼23	HZ-T2000-1	3744-上塘枢纽楼23	HZ-T2000-2	HZ-T2000-1
     96-萧山枢纽楼一（第一骨干机房）	HZ-T2000-1	96-萧山枢纽楼一（第一骨干机房）	HZ-T2000-2	HZ-T2000-1
     88-萧山3	HZ-T2000-1	88-萧山3	HZ-T2000-2	HZ-T2000-1
     3830-体育场路3	HZ-T2000-1	3830-体育场路3	HZ-T2000-2	HZ-T2000-1
     3817-上塘枢纽楼3	HZ-T2000-1	3817-上塘枢纽楼3	HZ-T2000-2	HZ-T2000-1
     3731-三墩2	HZ-T2000-1	3731-三墩2	HZ-T2000-2	HZ-T2000-1
     98-上塘枢纽楼1	HZ-T2000-1	98-上塘枢纽楼1	HZ-T2000-2	HZ-T2000-1
     97-萧山枢纽楼二（第一骨干机房）	HZ-T2000-1	97-萧山枢纽楼二（第一骨干机房）	HZ-T2000-2	HZ-T2000-1
     3743-上塘枢纽楼22	HZ-T2000-1	3743-上塘枢纽楼22	HZ-T2000-2	HZ-T2000-1
     89-萧山4	HZ-T2000-1	89-萧山4	HZ-T2000-2	HZ-T2000-1
     3745-上塘枢纽楼24	HZ-T2000-1	3745-上塘枢纽楼24	HZ-T2000-2	HZ-T2000-1
     3697-省干专线对接1(上塘枢纽楼八楼3平面)	HZ-T2000-1	3697-省干专线对接1(上塘枢纽楼八楼3平面)	HZ-T2000-2	HZ-T2000-1

     * @param  
     */

    public static void scan() throws Exception{
        JpaClient jpaClient = JpaClient.getInstance("cdcp.datajpa");
        List<VDevice> vds = jpaClient.findObjects("select c from VDevice c where c.additionalInfo is null");

        for (VDevice vd : vds) {
            List<RCDeviceVDevice> rcs = jpaClient.findObjects("select c from RCDeviceVDevice c where c.vDeviceDn = '" + vd.getDn() + "'");
            RCDeviceVDevice v = null;
            RCDeviceVDevice r = null;
            for (RCDeviceVDevice rc : rcs) {
                if (rc.getcDevicePrimaryType().equals("2"))
                    r = rc;
                else 
                    v = rc;
            }
            if (r != null && v != null) {
                JSONObject json = new JSONObject();
                json.put("cDeviceEMSName",r.getcDeviceEMSName());
                json.put("vDeviceEMSName",v.getcDeviceEMSName());
                json.put("cDeviceDn",r.getcDeviceDn());
                json.put("vDeviceDn",v.getcDeviceDn());

                json.put("cDeviceName",r.getcDeviceNativeEmsName());
                json.put("vDeviceName",v.getcDeviceNativeEmsName());

                vd.setAdditionalInfo(json.toString());
                jpaClient.saveObject(-1,vd);
            }




        }
    }
    public static VDevice createVDevice(String readDn,String realName,String realEms,String virtualDn,String virtualName,String virtualEms) throws Exception {
        VDevice vDevice = new VDevice();
        vDevice.setDn(readDn);
        vDevice.setEmsName(realEms);
        vDevice.setNativeEmsName(realName);
        vDevice.setProductName(realName);
        vDevice.setUserLabel(realName);
        vDevice.setCollectTimepoint(new Date());
        vDevice.setUseful("1");
        vDevice.setTag2("201605");
        JSONObject json = new JSONObject();
        json.put("cDeviceEMSName",realEms);
        json.put("vDeviceEMSName",virtualEms);
        json.put("cDeviceDn",readDn);
        json.put("vDeviceDn",virtualDn);

        json.put("cDeviceName",realName);
        json.put("vDeviceName",virtualName);

        vDevice.setAdditionalInfo(json.toString());



        RCDeviceVDevice rc = new RCDeviceVDevice();
        rc.setDn(readDn);
        rc.setcDeviceDn(readDn);
        rc.setcDeviceEMSName(realEms);
        rc.setcDeviceNativeEmsName(realName);
        rc.setvDeviceDn(vDevice.getDn());
        rc.setcDevicePrimaryType("2");  //2为真实  //1为虚拟
        rc.setTag2("201605");

        RCDeviceVDevice rc2 = new RCDeviceVDevice();
        rc2.setDn(virtualDn);
        rc2.setcDeviceDn(virtualDn);
        rc2.setcDeviceEMSName(virtualEms);
        rc2.setcDeviceNativeEmsName(virtualName);
        rc2.setvDeviceDn(vDevice.getDn());
        rc2.setcDevicePrimaryType("1");  //2为真实  //1为虚拟
        rc2.setTag2("201605");

        JpaClient.getInstance("cdcp.datajpa").executeUpdateSQL("delete from RCDeviceVDevice c where c.vDeviceDn = '"+vDevice.getDn()+"'");

        JpaClient.getInstance("cdcp.datajpa").storeObjectByDn(-1, vDevice);
        JpaClient.getInstance("cdcp.datajpa").storeObjectByDn(-1, rc);
        JpaClient.getInstance("cdcp.datajpa").storeObjectByDn(-1,rc2);
        return vDevice;

    }

    public static void main(String[] args) throws Exception {
        String names =
                "96-萧山枢纽楼一（第二骨干机房）\n" +
                "97-萧山枢纽楼二（第一骨干机房）";

        String[] nameArray = names.split("\n");
        System.out.println("nameArray = " + nameArray.length);
        for (String name : nameArray) {
            CDevice device1 = (CDevice) JpaClient.getInstance("cdcp.datajpa").findOneObject("select c from CDevice c where c.nativeEmsName = '"+name+"' and c.emsName = 'HZ-U2000-1-SDH'");
            CDevice device2 = (CDevice) JpaClient.getInstance("cdcp.datajpa").findOneObject("select c from CDevice c where c.nativeEmsName = '"+name+"' and c.emsName = 'HZ-U2000-2-SDH'");

            if (device1 != null && device2 != null) {
                createVDevice(device1.getDn(),device1.getNativeEmsName(),device1.getEmsName(),device2.getDn(),device2.getNativeEmsName(),device2.getEmsName());

            } else {
                if (device1 == null)
                    System.out.println("1:"+name);
                if (device2 == null)
                    System.out.println("2:"+name);

            }

        }
//        createVDevice("EMS:ZJ-U2000-1-SDH@ManagedElement:590552","728-萧山枢纽楼75-301","ZJ-U2000-1-SDH",
//                "EMS:HZ-T2000-2-P@ManagedElement:16711981","虚拟网元-萧山枢纽楼","HZ-T2000-2-P"    );
    }

    public static void main3(String[] args) throws Exception {
        createVDevice("EMS:ZJ-U2000-1-SDH@ManagedElement:590552","728-萧山枢纽楼75-301","ZJ-U2000-1-SDH",
            "EMS:HZ-T2000-2-P@ManagedElement:16711981","虚拟网元-萧山枢纽楼","HZ-T2000-2-P"    );
    }
    public static void main2(String[] args) throws Exception {
        String s = "网元A\t所属EMS\t网元B\t所属EMS\t主EMS\n" +
                "3815-上塘枢纽楼复用器12\tHZ-T2000-1-P\t3815-上塘枢纽楼复用器12\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "99-上塘枢纽楼2\tHZ-T2000-1-P\t99-上塘枢纽楼2\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3816-上塘枢纽楼4\tHZ-T2000-1-P\t3816-上塘枢纽楼4\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3828-学院路13\tHZ-T2000-1-P\t3828-学院路13\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3720-三墩1\tHZ-T2000-1-P\t3720-三墩1\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3744-上塘枢纽楼23\tHZ-T2000-1-P\t3744-上塘枢纽楼23\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "96-萧山枢纽楼一（第一骨干机房）\tHZ-T2000-1-P\t96-萧山枢纽楼一（第一骨干机房）\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "88-萧山3\tHZ-T2000-1-P\t88-萧山3\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3830-体育场路3\tHZ-T2000-1-P\t3830-体育场路3\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3817-上塘枢纽楼3\tHZ-T2000-1-P\t3817-上塘枢纽楼3\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3731-三墩2\tHZ-T2000-1-P\t3731-三墩2\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "98-上塘枢纽楼1\tHZ-T2000-1-P\t98-上塘枢纽楼1\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "97-萧山枢纽楼二（第一骨干机房）\tHZ-T2000-1-P\t97-萧山枢纽楼二（第一骨干机房）\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3743-上塘枢纽楼22\tHZ-T2000-1-P\t3743-上塘枢纽楼22\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "89-萧山4\tHZ-T2000-1-P\t89-萧山4\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3745-上塘枢纽楼24\tHZ-T2000-1-P\t3745-上塘枢纽楼24\tHZ-T2000-2-P\tHZ-T2000-1-P\n" +
//                "3697-省干专线对接1(上塘枢纽楼八楼3平面)\tHZ-T2000-1-P\t3697-省干专线对接1(上塘枢纽楼八楼3平面)\tHZ-T2000-2-P\tHZ-T2000-1-P\n";
         "";

        String[] lines = s.split("\n");
        for (String line : lines) {
            String[] split = line.split("\t");
            if (split.length != 5 || split[0].equals("网元A")) continue;
            String aname = split[0];
            String aems = split[1];
            String bname = split[2];
            String bems = split[3];
            String ems = split[4];

            CDevice aDevice = (CDevice)JpaClient.getInstance("cdcp.datajpa")
                    .findOneObject("select c from CDevice c where c.nativeEmsName = '" + aname + "' and emsname = '" + aems + "'");
            CDevice bDevice = (CDevice)JpaClient.getInstance("cdcp.datajpa")
                    .findOneObject("select c from CDevice c where c.nativeEmsName = '" + bname + "' and emsname = '" + bems + "'");


            CDevice realDevice = null;
            CDevice virtualDevice = null;
            if (ems.equals(aems)) {
                realDevice = aDevice;
                virtualDevice = bDevice;
            }
            else {
                realDevice = bDevice;
                virtualDevice = aDevice;
            }
            if (aDevice != null && bDevice != null) {
                VDevice vDevice = new VDevice();
                vDevice.setDn(realDevice.getDn());
                vDevice.setEmsName(realDevice.getEmsName());
                vDevice.setNativeEmsName(realDevice.getNativeEmsName());
                vDevice.setProductName(realDevice.getProductName());
                vDevice.setUserLabel(realDevice.getUserLabel());
                vDevice.setUseful("1");
                vDevice.setTag2("201508");


                RCDeviceVDevice rc = new RCDeviceVDevice();
                rc.setDn(realDevice.getDn());
                rc.setcDeviceDn(realDevice.getDn());
                rc.setcDeviceEMSName(realDevice.getEmsName());
                rc.setcDeviceNativeEmsName(realDevice.getNativeEmsName());
                rc.setvDeviceDn(vDevice.getDn());
                rc.setcDevicePrimaryType("2");  //2为真实  //1为虚拟
                rc.setTag2("201508");

                RCDeviceVDevice rc2 = new RCDeviceVDevice();
                rc2.setDn(virtualDevice.getDn());
                rc2.setcDeviceDn(virtualDevice.getDn());
                rc2.setcDeviceEMSName(virtualDevice.getEmsName());
                rc2.setcDeviceNativeEmsName(virtualDevice.getNativeEmsName());
                rc2.setvDeviceDn(vDevice.getDn());
                rc2.setcDevicePrimaryType("1");  //2为真实  //1为虚拟
                rc2.setTag2("201508");

                JpaClient.getInstance("cdcp.datajpa").storeObjectByDn(-1, vDevice);
                JpaClient.getInstance("cdcp.datajpa").storeObjectByDn(-1, rc);
                JpaClient.getInstance("cdcp.datajpa").storeObjectByDn(-1,rc2);

            }


        }

    }
}
