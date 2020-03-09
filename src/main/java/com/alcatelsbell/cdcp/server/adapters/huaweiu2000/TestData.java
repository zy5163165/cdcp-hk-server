package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.HW_EthService;
import org.asb.mule.probe.framework.entity.HW_VirtualBridge;
import org.asb.mule.probe.framework.entity.HW_VirtualLAN;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.service.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-12
 * Time: 下午10:06
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestData {

    private SqliteDelegation sd = null;
    public TestData() {
        init();
    }

    private void init() {
        String fileName=  "D:\\cdcpdb\\2014-07-08-150351-NBO-T2000-10-P-DayMigration.db\\2014-07-08-150351-NBO-T2000-10-P-DayMigration.db";
        sd = new SqliteDelegation(JPASupportFactory.createSqliteJPASupport(fileName));
    }

    public void testMSTP() {
        List<HW_VirtualLAN> virtualLANs = sd.queryAll(HW_VirtualLAN.class);
        HashMap<String,List> map = new HashMap();
        for (HW_VirtualLAN virtualLAN : virtualLANs) {
            String forwardTPList = virtualLAN.getForwardTPList();
            String[] tpList = forwardTPList.split("@EMS");
            int macCount = 0;
            String typeS = "";
            for (String tp : tpList) {
                if (!tp.startsWith("EMS"))
                    tp = "EMS"+tp;
                int i = tp.indexOf("type=");
                typeS += tp.substring(i+5,tp.indexOf("/",i))+" : ";
            }

            List l = map.get(typeS);
            if ( l == null) {
                l = new ArrayList();
                map.put(typeS,l);
            }
            l.add(typeS);



//            System.out.println("macCount = " + macCount);
//            logger.info("macCount = " + macCount);
        }
        Set<String> strings = map.keySet();
        for (String string : strings) {
            System.out.println(string+" : "+map.get(string).size());
        }


    }
    private String getType(String tp) {
        if (!tp.startsWith("EMS"))
            tp = "EMS"+tp;
        int i = tp.indexOf("type=");
        return tp.substring(i+5,tp.indexOf("/",i));
    }

    public void testEthService() {
        List<HW_EthService> virtualLANs = sd.queryAll(HW_EthService.class);
        HashMap<String,List> map = new HashMap();
        for (HW_EthService service : virtualLANs) {
            String aend = service.getaEnd();
            String zend = service.getzEnd();
            String typeS = service.getServiceType()+"--"+ getType(aend)+"<>"+getType(zend);

            List l = map.get(typeS);
            if ( l == null) {
                l = new ArrayList();
                map.put(typeS,l);
            }
            l.add(typeS);



//            System.out.println("macCount = " + macCount);
//            logger.info("macCount = " + macCount);
        }
        Set<String> strings = map.keySet();
        for (String string : strings) {
            System.out.println(string+" : "+map.get(string).size());
        }
    }

    public void testEthService2() {
        HashMap<String,String> lp_mac_map = new HashMap<String, String>();
        HashMap<String,String> lp_mp_map = new HashMap<String, String>();
        List<HW_EthService> ethServices = sd.queryAll(HW_EthService.class);
        HashMap<String,List> map = new HashMap();
        for (HW_EthService es : ethServices) {
            String aend = es.getaEnd();
            String zend = es.getzEnd();
            String mp = null;
            String mac = null;
            if (es.getServiceType().equals("HW_EST_EPLAN")) {
                String atype = SDHUtil.getPortType(aend);
                String ztype = SDHUtil.getPortType(zend);
                if (atype.equals("lp")){
                    if (ztype.equals("mac")) lp_mac_map.put(aend,zend);
                    if (ztype.equals("mp")) lp_mp_map.put(aend,zend);
                } else if (ztype.equals("mp")) {
                    if (atype.equals("mac")) lp_mac_map.put(zend,aend);
                    if (atype.equals("mp")) lp_mp_map.put(zend,aend);
                }
            }
        }

        HashMap<String,Integer> count = new HashMap<String, Integer>();
        List<HW_VirtualLAN> virtualLANs = sd.queryAll(HW_VirtualLAN.class);
        for (HW_VirtualLAN virtualLAN : virtualLANs) {
            String forwardTPList = virtualLAN.getForwardTPList();
            String[] tpList = forwardTPList.split("@EMS");
            String s = "";
            for (String tp : tpList) {
                if (!tp.startsWith("EMS"))
                    tp = "EMS"+tp;
                String type = SDHUtil.getPortType(tp);
                if (type.equals("lp")) {
                    String real = lp_mac_map.get(tp);
                    if (real == null) {
                        real = lp_mp_map.get(tp);
                        if (real != null) {
                            s += "mp-";
                        } else{

                                System.out.println("lp not found");

                        }
                    } else {
                        s+="mac-";
                    }

                } else {
                    s += type+"-";
                }
            }
            if (s.equals("mac-mp-mac-mp-mp-mp-mp-mp-"))
                System.out.println("virtualLAN = " + virtualLAN.getDn());
            Integer integer = count.get(s);
            if (integer == null) {
                integer = 1;
                count.put(s,integer);
            } else {
                count.put(s,integer+1);
            }


        }
        Set<String> strings = count.keySet();
        for (String string : strings) {
            System.out.println(string+" = "+count.get(string));
        }

    }
    public void testSNC() {
        List<SubnetworkConnection> list = sd.queryAll(SubnetworkConnection.class);
        for (SubnetworkConnection subnetworkConnection : list) {
            String aend = subnetworkConnection.getaEnd();
            String zend = subnetworkConnection.getzEnd();
            String[] as = aend.split(Constant.listSplitReg);
            String[] zs = zend.split(Constant.listSplitReg);
            if (as.length != 1) {
                System.out.println("snc="+subnetworkConnection.getDn()+" aend = "+aend);
            }
            if (zs.length != 1) {
                System.out.println("snc="+subnetworkConnection.getDn()+" zend = "+zend);
            }

        }
    }
    public void testVB2() {
        List<HW_VirtualBridge> virtualLANs = sd.queryAll(HW_VirtualBridge.class);
        HashMap<String,List> map = new HashMap();
        int count = virtualLANs.size();
        int i = 0;
        for (HW_VirtualBridge vb : virtualLANs) {
            String logicalTPList = vb.getLogicalTPList();
            String[] split = logicalTPList.split("@EMS");
            String tp = "@EMS"+split[1];
            tp = tp.substring(1,tp.lastIndexOf("/port"));

            List query = sd.query("select c from HW_EthService c where c.aEnd like '" + tp + "%' or c.zEnd like '" + tp + "%'");
            if (query != null && query.size() > 2) {
                System.out.println("count="+(++i)+"/"+count+" size="+query.size());
                System.out.println("vb = " + vb.getDn());
                System.out.println("logicalTPList = " + logicalTPList);
            }   else {

            }
            System.out.println("count="+(++i)+"/"+count+" size="+query.size());

        }
    }
    public void testVB() {
        List<HW_VirtualBridge> virtualLANs = sd.queryAll(HW_VirtualBridge.class);
        HashMap<String,List> map = new HashMap();
        for (HW_VirtualBridge vb : virtualLANs) {
            String forwardTPList = vb.getLogicalTPList();
            String[] tpList = forwardTPList.split("@EMS");
            int macCount = 0;
            String typeS = "";
            for (String tp : tpList) {
                if (!tp.startsWith("EMS"))
                    tp = "EMS"+tp;
                int i = tp.indexOf("type=");
                typeS += tp.substring(i+5,tp.indexOf("/",i))+" : ";
            }

            List l = map.get(typeS);
            if ( l == null) {
                l = new ArrayList();
                map.put(typeS,l);
            }
            l.add(typeS);



//            System.out.println("macCount = " + macCount);
//            logger.info("macCount = " + macCount);
        }
        Set<String> strings = map.keySet();
        for (String string : strings) {
            System.out.println(string+" : "+map.get(string).size());
        }

    }


    public static void main(String[] args) {
        new TestData().testEthService2();
    }
    private Log logger = LogFactory.getLog(getClass());
}
