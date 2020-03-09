package com.alcatelsbell.cdcp.server.adapters.alu;

import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.service.Constant;

import java.util.HashSet;

/**
 * Author: Ronnie.Chen
 * Date: 2015/1/7
 * Time: 10:40
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ALUDicUtil {
    private static  Log logger = LogFactory.getLog(SDHUtil.class);
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
                        String tmRate = SDHUtil.getTMRate(rate);
                        if (tmRate != null) return tmRate;
                    }
                }
            } else {
                rate = Integer.parseInt(rateStr);

                String tmRate = SDHUtil.getTMRate(rate);
                if (tmRate != null) return tmRate;
                rates.add(rateStr);
            }

            if (rates.contains(""+ DicConst.LR_Optical_Channel) ||
                    rates.contains(""+DicConst.LR_DIGITAL_SIGNAL_RATE) ||
                    rates.contains(""+DicConst.LR_Optical_Transmission_Section) ||
                    rates.contains(""+DicConst.LR_Optical_Channel) ||
                    rates.contains(""+DicConst.LR_OPTICAL_SECTION) ||
                    rates.contains(""+DicConst.LR_PHYSICAL_OPTICAL) ||
                    rates.contains(""+DicConst.LR_Optical_Multiplex_Section) )

                return "100G";
            else {
                logger.error("Unknow rate = "+rateStr);

            }
        } catch ( Exception e) {
            return null;
        }
        return null;
    }

}
