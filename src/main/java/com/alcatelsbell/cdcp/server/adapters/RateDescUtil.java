package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWDic;
import com.alcatelsbell.cdcp.util.DicConst;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.service.Constant;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2014/11/20
 * Time: 10:11
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class RateDescUtil {
    private Log logger = LogFactory.getLog(getClass());
    public static String getRateDesc(String rates) {
        if (rates == null) return null;
        String[] split = rates.split(Constant.listSplitReg);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < split.length; i++) {
            String r = split[i];
            if (i > 0) sb.append("||");
            if (r.equals(HWDic.LR_STS3c_and_AU4_VC4.value+"")) {
                sb.append("VC4");
            }
            if (r.equals(HWDic.LR_STS12c_and_VC4_4c.value+"")) {
                sb.append("VC4_4c");
            }
            if (r.equals(HWDic.LR_Low_Order_TU3_VC3.value+"")) {
                sb.append("VC3");
            }
            if (r.equals(HWDic.LR_VT2_and_TU12_VC12.value+"")) {
                sb.append("VC12");
                return "VC12";
            }
            if (r.equals(HWDic.LR_E1_2M.value+"")) {
                sb.append("E1");
            }
            if (r.equals(HWDic.LR_E3_34M.value+"")) {
                sb.append("E3");
            }
            if (r.equals(HWDic.LR_E4_140M.value+"")) {
                sb.append("E4");
            }
            if (r.equals(HWDic.LR_E2_8M.value+"")) {
                sb.append("E2");
            }
            if (r.equals(HWDic.LR_Section_OC1_STS1_and_RS_STM0.value+"")) {
                sb.append("STM0");
            }
            if (r.equals(HWDic.LR_Section_OC3_STS3_and_RS_STM1.value+"")) {
                sb.append("STM1");
            }
            if (r.equals(HWDic.LR_Section_OC12_STS12_and_RS_STM4.value+"")) {
                sb.append("STM4");
            }
            if (r.equals(HWDic.LR_Section_OC48_STS48_and_RS_STM16.value+"")) {
                sb.append("STM16");
            }
            if (r.equals(HWDic.LR_Section_OC192_STS192_and_RS_STM64.value+"")) {
                sb.append("STM64");
            }

            if (r.equals(DicConst.HWEXT_LR_OCH_Data_Unit_0)) {
                sb.append("ODU0");
            }
            if (r.equals(DicConst.LR_Optical_Channel+"")) {
                sb.append("OC");
            }
            if (r.equals(DicConst.LR_DSR_OC48_and_STM16+"")) {
                sb.append("STM16");
            }
            if (r.equals(DicConst.LR_DSR_OC192_and_STM64+"")) {
                sb.append("STM64");
            }
            if (r.equals(DicConst.LR_DSR_Gigabit_Ethernet+"")) {
                sb.append("GE");
            }
            if (r.equals(DicConst.LR_DSR_10Gigabit_Ethernet+"")) {
                sb.append("10GE");
            }
            if (r.equals(DicConst.LR_OCH_Data_Unit_0+"")) {
                return("ODU0");
            }
            if (r.equals(DicConst.LR_OCH_Data_Unit_1+"")) {
                return ("ODU1");
            }
            if (r.equals(DicConst.LR_OCH_Data_Unit_2+"")) {
                return ("ODU2");
            }
            if (r.equals(DicConst.LR_OCH_Data_Unit_3+"")) {
                return ("ODU3");
            }
            if (r.equals(DicConst.LR_OCH_Transport_Unit_1+"")) {
                sb.append("OTU1");
            }
            if (r.equals(DicConst.LR_OCH_Transport_Unit_2+"")) {
                sb.append("OTU2");
            }
            if (r.equals(DicConst.LR_OCH_Transport_Unit_3+"")) {
                sb.append("OTU3");
            }
            if (r.equals(DicConst.HWEXT_LR_OCH_Data_Unit_Flexible+"")) {
                sb.append("ODU-Flexible");
            }

            if (r.equals("331"))
                sb.append("ODU2E") ;
            if (r.equals("330"))
                sb.append("ODU") ;


            if (r.equals("334"))
                sb.append("ODU4") ;
            if (r.equals("339"))
                sb.append("OTU4") ;

            if (r.equals("41"))
                sb.append("OMS") ;
            if (r.equals("42"))
                sb.append("OTS") ;
            if (r.equals("50"))
                sb.append("DSR") ;
            if (r.equals("80"))
                sb.append("DSR") ;
            if (r.equals("10105"))     //zte
                sb.append("ODU0");
            if (r.equals("10117"))          //zte
                sb.append("ODU2e");
            if (r.equals("1500"))
                sb.append("OSC"); //zte
        }
        String desc =  sb.toString();

//        if (desc.isEmpty()) {
//            List<String> strings = Arrays.asList(split);
//            if (strings.contains(DicConst.LR_PHYSICAL_OPTICAL+""))
//                return "OPTICAL";
//        }
        return desc;


    }
}
