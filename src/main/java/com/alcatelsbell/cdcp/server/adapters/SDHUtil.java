package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.CCTP;
import com.alcatelsbell.cdcp.nbi.model.CPTP;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWDic;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import com.alcatelsbell.cdcp.util.MemTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.R_TrafficTrunk_CC_Section;
import org.asb.mule.probe.framework.service.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-8
 * Time: 下午3:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SDHUtil {
    private static  Log logger = LogFactory.getLog(SDHUtil.class);
    public static String[] anotherSideCtpList(CrossConnect cc,String ctp) {
        if (cc.getaEndNameList().contains(ctp))
            return splitCtpList(cc.getzEndNameList());

        else if (cc.getzEndNameList().contains("ctp"))
            return splitCtpList(cc.getaEndNameList());

        return null;
    }

    public static boolean isVC4Ctp(String ctpDn) throws Exception {
        if (!ctpDn.contains("CTP:/"))
            throw new DataInvalidException("无效CTP:"+ctpDn);
        int i = ctpDn.indexOf("CTP:/sts3c_au4-j=");
        if (i < 0) return false;
        int i1 = ctpDn.indexOf("/", i + 6);
        if (i1 > -1) {
            return false;
        }
        return true;
    }
    public static String getVC4Ctp(String ctpDn) throws Exception {
        return ctpDn.substring(0,ctpDn.lastIndexOf("/"));
    }

    //EMS:HUZ-OTNM2000-7-P@ManagedElement:134217960;106508@PTP:/rack=26881/shelf=1/slot=1049601/port=1@CTP:/sts3c_au4-j=1
    public static List<String> createVC4CtpDns(CPTP cptp) {
        if (cptp == null) return new ArrayList<String>();

        String rateDesc = cptp.getRate();
        if (containsRate(rateDesc,20) || containsRate(rateDesc,25)) {//stm-1   155
              return createCtpDns(cptp.getDn(),1,1);
        }

        else if (containsRate(rateDesc,21) || containsRate(rateDesc,26)) {//stm-2   622
            return createCtpDns(cptp.getDn(),1,4);
        }

        else if (containsRate(rateDesc,22) || containsRate(rateDesc,27)) {//stm-3   2.5
            return createCtpDns(cptp.getDn(),1,16);
        }

        else if (containsRate(rateDesc,23) || containsRate(rateDesc,28)) {//stm-4   10
            return createCtpDns(cptp.getDn(),1,64);
        }
        return new ArrayList<String>();

    }

    public static List<CTP> createVC4Ctps(CPTP cptp) {
        List<String> ctpdns = createVC4CtpDns(cptp);
        List<CTP> ctps = new ArrayList<CTP>();
        for (String ctpdn : ctpdns) {
            CTP newCTP = new CTP();
            String newDn = ctpdn;
            newCTP.setDn(newDn);
            newCTP.setTag1("NEW");
            newCTP.setParentDn(cptp.getDn());
            newCTP.setPortdn(cptp.getDn());
            newCTP.setNativeEMSName("VC4-"+ctpdn.substring(ctpdn.lastIndexOf("=")));
            ctps.add(newCTP);

        }
        return ctps;
    }

    public static List<String> createCtpDns(String ptpDn,int jfrom ,int jto) {
        List<String> list = new ArrayList<String>();
        for (; jfrom <= jto ; jfrom++) {
            list.add(ptpDn+"@CTP:/sts3c_au4-j="+jfrom);
        }
       return list;
    }

    public static boolean containsRate(String rateStr ,int rate) {
        String[] split = rateStr.split(Constant.listSplitReg);
        for (String s : split) {
            if (s.equals(rate+""))
                return true;
        }
        return false;
    }



    public static String getPortType(String tp) {
        int i = tp.indexOf("type=");
        return tp.substring(i+5,tp.indexOf("/", i));
    }

    public static String[] anotherSideCtpList(R_TrafficTrunk_CC_Section cc,String ctp) {
        if (cc.getaEnd().contains(ctp))
            return splitCtpList(cc.getzEnd());

        else if (cc.getzEnd().contains("ctp"))
            return splitCtpList(cc.getaEnd());

        return null;
    } 
    public static  String getTMRate(String rateStr) {
        try {
            if (rateStr == null || rateStr.isEmpty()) return "";
            int rate = -1;
            HashSet<String> rates = new HashSet<String>();
            if (rateStr.contains(Constant.listSplit)) {
                String[] split = rateStr.split(Constant.listSplitReg);

                if (split != null) {

                    for (String s : split) {
                        rates.add(s);
                        rate = Integer.parseInt(s);
                        String tmRate = getTMRate(rate);
                        if (tmRate != null) return tmRate;
                    }
                }
            } else {
                rate = Integer.parseInt(rateStr);

                String tmRate = getTMRate(rate);
                if (tmRate != null) return tmRate;
                rates.add(rateStr);
            }

            if (rates.contains(""+DicConst.LR_Optical_Channel) ||
                    rates.contains(""+DicConst.LR_DIGITAL_SIGNAL_RATE) ||
                    rates.contains(""+DicConst.LR_Optical_Transmission_Section) ||
                    rates.contains(""+DicConst.LR_Optical_Channel) ||
                    rates.contains(""+DicConst.LR_OPTICAL_SECTION) ||
                    rates.contains(""+DicConst.LR_PHYSICAL_OPTICAL) ||
                    rates.contains(""+DicConst.LR_Optical_Multiplex_Section) )

                return "40G";
            else {
          //      logger.error("Unknow rate = "+rateStr);

            }
        } catch ( Exception e) {
            return null;
        }
        return null;
    }

    public static String getTMRate (int rate) {
        if (HWDic.LR_STS3c_and_AU4_VC4.value == rate) {
            return "155M";
        }

        else if (HWDic.LR_Low_Order_TU3_VC3.value == rate) {
            return "34M";
        }

        else if (HWDic.LR_VT2_and_TU12_VC12.value == rate) {
            return "2M";
        }

        else if (HWDic.LR_E1_2M.value == rate) {
            return "2M";
        }
        else if (HWDic.LR_E4_140M.value == rate) {
            return "155M";
        }
        else if (HWDic.LR_E3_34M.value == rate) {
            return "34M";
        }
        else if (HWDic.LR_E2_8M.value == rate) {
            return "8M";
        }
        else if (HWDic.LR_E5_565M.value == rate) {
            return "565M";
        }
        else if (HWDic.LR_DSR_OC3_STM1.value == rate) { //73
            return "155M";
        }
        else if (HWDic.LR_DSR_OC12_STM4.value == rate || HWDic.LR_STS12c_and_VC4_4c.value == rate) { //74
            return "622M";
        }
        else if (HWDic.LR_DSR_OC24_STM8.value == rate) { // 75
            return "1.25G";
        }
        else if (HWDic.LR_DSR_OC48_and_STM16.value == rate) { //76
            return "2.5G";
        }
        else if (HWDic.LR_DSR_OC192_and_STM64.value == rate) { //77
            return "10G";
        }
        else if (HWDic.LR_DSR_OC768_and_STM256.value == rate) { //78
            return "40G";
        }
            
            else if (DicConst.LR_DSR_2M == rate) {
                return "2M";
            }

        else if (rate == DicConst.LR_OCH_Data_Unit_0) {
            return "1.25G";
        }
        else if (rate == DicConst.LR_OCH_Data_Unit_1) {
            return "2.5G";
        }
        else if (rate == DicConst.LR_OCH_Data_Unit_2) {
            return "10G";
        }
        else if (rate == DicConst.LR_OCH_Data_Unit_3) {
            return "40G";
        }
        else if (rate == DicConst.LR_OCH_Transport_Unit_1) {
            return "2.5G";
        }
        else if (rate == DicConst.LR_OCH_Transport_Unit_2) {
            return "10G";
        }
        else if (rate == DicConst.LR_OCH_Transport_Unit_3) {
            return "40G";
        }

        else if (rate == DicConst.LR_DSR_Gigabit_Ethernet){ //87
            return "1.25G";
        }
        else if (rate == DicConst.LR_DSR_OTU1){ //110
            return "2.5G";
        }
        else if (rate == DicConst.LR_DSR_OTU2){ //111
            return "2.5G";
        }
        else if (rate == DicConst.LR_DSR_10Gigabit_Ethernet){ //113
            return "10G";
        }
        else if (rate == DicConst.LR_DSR_10Gigabit_Ethernet_LAN){ //8008
            return "10G";
        }
        else if (rate == DicConst.LR_DSR_10Gigabit_Ethernet_WAN){ //8009
            return "10G";
        }
        else if (rate == DicConst.LR_DSR_OTU4_Ethernet_WAN){ //huawei OTU4 8043
            return "100G";
        }


        else if (rate == DicConst.HWEXT_LR_OCH_Data_Unit_0) {
            return "1.25G";
        }
        else if (rate == 331) {    // alu odu2e
            return "10G";
        }
        else if (rate == 330) {    //alu odu0
            return "1.25G";
        }
        else if (rate == 334) {    //alu odu4
            return "100G";
        }
        else if (rate == 339) {    //alu otu4
            return "100G";
        }

        else if (rate == 10105) {    //zte odu0
            return "1.25G";
        }

        else if (rate == 10117) {    //zte odu2e
            return "10G";
        }

        else if (rate == 1500) {    //zte odu2e
            return "40G";
        }

        else if (rate == DicConst.HWEXT_LR_OCH_Data_Unit_Flexible)
           return "40G";
        return null;

    }
    public static String rateDesc(String rate) {
        return RateDescUtil.getRateDesc(rate);
    }


    public static void setCTPNumber(CCTP ctp) {
        ctp.setJ("-");
        ctp.setK("-");
        ctp.setL("-");
        ctp.setM("-");
        try {
            String ctpSimpleName = DNUtil.extractCTPSimpleName(ctp.getDn());
            String[] split = ctpSimpleName.split("/");

            for (String s : split) {
                if (s.contains("sts3c_au4-j=")) {
                     ctp.setJ(s.substring(s.indexOf("sts3c_au4-j=") + "sts3c_au4-j=".length()));
                }
                if (s.contains("sts12c_vc4_4c=")) {
                    ctp.setJ(s.substring(s.indexOf("sts12c_vc4_4c=") + "sts12c_vc4_4c=".length()));
                }
                if (s.contains("tu3_vc3-k=")) {
                    ctp.setK(s.substring(s.indexOf("tu3_vc3-k=") + "tu3_vc3-k=".length()));
                }
                if (s.contains("vt2_tu12-")) {
                    String value = s.substring(s.indexOf("vt2_tu12-")+"vt2_tu12-".length());
                    String[] split1 = value.split("-");
                    for (String s1 : split1) {
                        if (s1.contains("k="))
                            ctp.setK(s1.substring(s1.indexOf("k=")+2));
                        if (s1.contains("l="))
                            ctp.setL(s1.substring(s1.indexOf("l=" )+ 2));
                        if (s1.contains("m="))
                            ctp.setM(s1.substring(s1.indexOf("m=" )+ 2));

                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析CTP JKLM失败 ctp="+ctp.getDn(), e);
        }

    }


    /*
     section 两端端口的ctp
EMS:QUZ-T2000-3-P@ManagedElement:590598@PTP:/rack=1/shelf=1/slot=2/domain=sdh/port=1@CTP:/sts3c_au4-j=1
EMS:QUZ-T2000-3-P@ManagedElement:590598@PTP:/rack=1/shelf=1/slot=2/domain=sdh/port=1@CTP:/sts3c_au4-j=1/tu3_vc3-k=3
EMS:QUZ-T2000-3-P@ManagedElement:590598@PTP:/rack=1/shelf=1/slot=2/domain=sdh/port=1@CTP:/sts3c_au4-j=1/vt2_tu12-k=3-l=3-m=3


EMS:QUZ-T2000-3-P@ManagedElement:591293@PTP:/rack=1/shelf=1/slot=1/domain=sdh/port=2@CTP:/sts3c_au4-j=1
EMS:QUZ-T2000-3-P@ManagedElement:591293@PTP:/rack=1/shelf=1/slot=1/domain=sdh/port=2@CTP:/sts3c_au4-j=1/tu3_vc3-k=3
EMS:QUZ-T2000-3-P@ManagedElement:591293@PTP:/rack=1/shelf=1/slot=1/domain=sdh/port=2@CTP:/sts3c_au4-j=1/vt2_tu12-k=3-l=3-m=3
     */
    public static CTP findRelatedCtp(String ctpDn,String portDn,HashMap<String,List<CTP>> ptpCtpMap) throws Exception {
        List<CTP> ctps = ptpCtpMap.get(portDn);
        String ctpSimpleName = DNUtil.extractCTPSimpleName(ctpDn);
        if (ctps != null) {
            for (CTP ctp : ctps) {
                String dn = ctp.getDn();
                String s = DNUtil.extractCTPSimpleName(dn);
                if (s.equals(ctpSimpleName))
                    return ctp;

            }
        }

        return null;
    }

    public static String[] splitCtpList(String ctpList) {
        if (ctpList.contains(Constant.listSplitReg))
            return ctpList.split(Constant.listSplitReg);
        else return new String[]{ctpList};
    }

    public static void main(String[] args) {
        System.out.println("getTMRate(\"15\") = " + rateDesc("11"));
        CPTP ctp = new CPTP();
        ctp.setRate("47||20||25||15");
        String dn = "EMS:HUZ-OTNM2000-7-P@ManagedElement:134217960;106508@PTP:/rack=26881/shelf=1/slot=1049601/port=1@CTP:/sts3c_au4-j=1/tu3_vc3-k=1/vt2_tu12-l=3-m=2";
//        dn = "EMS:QUZ-T2000-3-P@ManagedElement:591080@PTP:/rack=1/shelf=1/slot=1/domain=sdh/port=1@CTP:/sts3c_au4-j=1/vt2_tu12-k=2-l=4-m=1";
//        dn = "EMS:QUZ-T2000-3-P@ManagedElement:591443@PTP:/rack=1/shelf=1/slot=1/domain=sdh/port=1@CTP:/sts3c_au4-j=1/tu3_vc3-k=1";
//        dn = "EMS:QUZ-T2000-3-P@ManagedElement:591049@PTP:/rack=1/shelf=1/slot=1/domain=sdh/port=1@CTP:/sts3c_au4-j=1";
        dn = "EMS:HUZ-OTNM2000-7-P@ManagedElement:134243071;103938@PTP:/rack=828417/shelf=1/slot=4195335/port=1";
        ctp.setDn(dn);
      createVC4Ctps(ctp);
    }


    public static int getCTPRateNumber(CacheClass.T_CTP ctp) {
        try {
            if (ctp == null) return 0;
            String rate = ctp.getRate();
            String tmRate = getTMRate(rate);
            if (tmRate.contains("M")) {
                tmRate = tmRate.substring(0,tmRate.indexOf("M"));
                return Integer.parseInt(tmRate);
            }
        } catch (Exception e) {
            logger.error("Unkonwn rate = "+ctp.getRate());
        }
        return 0;
    }
}
