package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;


/**
 * Author: Ronnie.Chen
 * Date: 14-7-16
 * Time: 下午9:19
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HwDwdmUtil {
    private Log logger = LogFactory.getLog(getClass());
    public static boolean isOMSRate(String rate) {
        if (rate == null) return false;
        return rate.equals(HWDic.LR_Optical_Multiplex_Section.value+"")
                || rate.startsWith(HWDic.LR_Optical_Multiplex_Section.value + "||")
                || rate.endsWith("||" + HWDic.LR_Optical_Multiplex_Section.value)
                || rate.contains("||" + HWDic.LR_Optical_Multiplex_Section.value +"||");

    }
    public static HashMap<String,String> wavelength_freq_map = new HashMap();
    static
    {
        wavelength_freq_map.put("196.050","1529.16") ;
        wavelength_freq_map.put("196.000","1529.55") ;
        wavelength_freq_map.put("195.950","1529.94");
        wavelength_freq_map.put("195.900","1530.33")  ;
        wavelength_freq_map.put("195.850","1530.72")  ;
        wavelength_freq_map.put("195.800","1531.12")  ;
        wavelength_freq_map.put("195.750","1531.51")  ;
        wavelength_freq_map.put("195.700","1531.90")   ;
        wavelength_freq_map.put("195.650","1532.29")    ;
        wavelength_freq_map.put("195.600","1532.68")    ;
        wavelength_freq_map.put("195.550","1533.07")    ;
        wavelength_freq_map.put("195.500","1533.47")     ;
        wavelength_freq_map.put("195.450","1533.86")    ;
        wavelength_freq_map.put("195.400","1534.25")     ;
        wavelength_freq_map.put("195.350","1534.64")   ;
        wavelength_freq_map.put("195.300","1535.04")    ;
        wavelength_freq_map.put("195.250","1535.43")   ;
        wavelength_freq_map.put("195.200","1535.82")   ;
        wavelength_freq_map.put("195.150","1536.22")   ;
        wavelength_freq_map.put("195.100","1536.61")  ;

        wavelength_freq_map.put("195.050","1537.00");
        wavelength_freq_map.put("195.000","1537.40");
        wavelength_freq_map.put("194.950","1537.79");
        wavelength_freq_map.put("194.900","1538.19");
        wavelength_freq_map.put("194.850","1538.58");
        wavelength_freq_map.put("194.800","1538.98");
        wavelength_freq_map.put("194.750","1539.37");
        wavelength_freq_map.put("194.700","1539.77");
        wavelength_freq_map.put("194.650","1540.16");
        wavelength_freq_map.put("194.600","1540.56");
        wavelength_freq_map.put("194.550","1540.95");
        wavelength_freq_map.put("194.500","1541.35");
        wavelength_freq_map.put("194.450","1541.75");
        wavelength_freq_map.put("194.400","1542.14");
        wavelength_freq_map.put("194.350","1542.54");
        wavelength_freq_map.put("194.300","1542.94");
        wavelength_freq_map.put("194.250","1543.33");
        wavelength_freq_map.put("194.200","1543.72");
        wavelength_freq_map.put("194.150","1544.13");
        wavelength_freq_map.put("194.100","1544.53");

        wavelength_freq_map.put("194.050","1544.92");
        wavelength_freq_map.put("194.000","1545.32");
        wavelength_freq_map.put("193.950","1545.72");
        wavelength_freq_map.put("193.900","1546.12");
        wavelength_freq_map.put("193.850","1546.52");
        wavelength_freq_map.put("193.800","1546.92");
        wavelength_freq_map.put("193.750","1547.32");
        wavelength_freq_map.put("193.700","1547.72");
        wavelength_freq_map.put("193.650","1548.11");
        wavelength_freq_map.put("193.600","1548.51");
        wavelength_freq_map.put("193.550","1548.91");
        wavelength_freq_map.put("193.500","1549.32");
        wavelength_freq_map.put("193.450","1549.72");
        wavelength_freq_map.put("193.400","1550.12");
        wavelength_freq_map.put("193.350","1550.52");
        wavelength_freq_map.put("193.300","1550.92");
        wavelength_freq_map.put("193.250","1551.32");
        wavelength_freq_map.put("193.200","1551.72");
        wavelength_freq_map.put("193.150","1552.12");
        wavelength_freq_map.put("193.100","1552.52");

        wavelength_freq_map.put("193.050","1552.93");
        wavelength_freq_map.put("193.000","1553.33");
        wavelength_freq_map.put("192.950","1553.73");
        wavelength_freq_map.put("192.900","1554.13");
        wavelength_freq_map.put("192.850","1554.54");
        wavelength_freq_map.put("192.800","1554.94");
        wavelength_freq_map.put("192.750","1555.34");
        wavelength_freq_map.put("192.700","1555.75");
        wavelength_freq_map.put("192.650","1556.15");
        wavelength_freq_map.put("192.600","1556.55");
        wavelength_freq_map.put("192.550","1556.96");
        wavelength_freq_map.put("192.500","1557.36");
        wavelength_freq_map.put("192.450","1557.77");
        wavelength_freq_map.put("192.400","1558.17");
        wavelength_freq_map.put("192.350","1558.58");
        wavelength_freq_map.put("192.300","1558.98");
        wavelength_freq_map.put("192.250","1559.39");
        wavelength_freq_map.put("192.200","1559.79");
        wavelength_freq_map.put("192.150","1560.20");
        wavelength_freq_map.put("192.100","1560.61");
    }

    public static String getOddFrequence(int portSeq) {
        float f = 196.00f - (portSeq -1) * 0.1f;
        if ((f+"").length() > 6)
            return (f+"").substring(0,6);
        else return f+"";

    }

    public static String getEvenFrequence(int portSeq) {
        float f = 196.05f - (portSeq -1) * 0.1f;
        if ((f+"").length() > 6)
            return (f+"").substring(0,6);
        else return f+"";

    }

    public static String getWaveLength(String frequencies) {
        String aDouble = wavelength_freq_map.get(frequencies);
        return aDouble ;
    }

    public static void main(String[] args) {
        System.out.println(getEvenFrequence(40));
    }
}
