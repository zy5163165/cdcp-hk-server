package com.alcatelsbell.cdcp.server.adapters.zte;

import com.alcatelsbell.cdcp.util.DicConst;
import com.alcatelsbell.cdcp.util.DicUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2014/12/8
 * Time: 13:42
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ZTEDicUtil {
    public static Integer getEOType(String layerRates) {
        // if (layerRates == null)
        // return DicConst.EOTYPE_UNKNOWN;
        try {
            List<Integer> list = DicUtil.convertLayerRateList(layerRates);
            for (int rate : list) {
                if (rate == DicConst.LR_PHYSICAL_ELECTRICAL
                        ||rate == 4166 ||  rate == 4165
                        ||rate == 4208       ||  rate == 5  ||  rate == 6  ||  rate == 7  ||  rate == 8

                        ) {
                    return DicConst.EOTYPE_ELECTRIC;
                } else if (rate == DicConst.LR_PHYSICAL_OPTICAL
                        || rate == DicConst.LR_Optical_Channel
                        || rate == DicConst.LR_Optical_Multiplex_Section
                        || rate == DicConst.LR_Optical_Transmission_Section
                        || rate == 4192 || rate == 1535 || rate == 1533 || rate == 1500
                        || rate == 1
                        || rate == 4177
                        || rate == 4178
                        ) {
                    return DicConst.EOTYPE_OPTIC;
                }
            }

            // for (int i = 0; i < list.length; i++) {
            // if (list[i] == DicConst.LR_PHYSICAL_ELECTRICAL || list[i] == DicConst.LR_DSR_Fast_Ethernet || list[i] == DicConst.LR_Ethernet
            // // || list[i] == DicConst.LR_DS0_64K || list[i] == DicConst.LR_DS0_64K
            // //
            // // || list[i] == DicConst.LR_DS0_64K
            // // || list[i] == DicConst.LR_DSR_2M
            // // // || list[i] == DicConst.LR_128K
            // // // || list[i] == DicConst.LAYERRATE_LR_E0
            // // || list[i] == DicConst.LR_E1_2M || list[i] == DicConst.LR_E2_8M || list[i] == DicConst.LR_E3_34M || list[i] == DicConst.LR_E4_140M
            // // || list[i] == DicConst.LR_E5_565M
            // ) {
            // return DicConst.EOTYPE_ELECTRIC;
            // }
            // if (list[i] == DicConst.LR_PHYSICAL_OPTICAL
            // // || list[i] == DicConst.LR_DSR_Gigabit_Ethernet
            // // || list[i] == DicConst.LR_Section_OC48_STS48_and_RS_STM16 || list[i] == DicConst.LR_Section_OC3_STS3_and_RS_STM1
            // // || list[i] == DicConst.LR_Section_OC48_STS48_and_RS_STM16 || list[i] == DicConst.LR_Section_OC48_STS48_and_RS_STM16
            // // || list[i] == DicConst.LR_Section_OC48_STS48_and_RS_STM16 || list[i] == DicConst.LR_DSR_10Gigabit_Ethernet
            // // || list[i] == DicConst.LR_OCH_Data_Unit_1 || list[i] == DicConst.LR_DSR_Gigabit_Ethernet || list[i] == DicConst.LR_DSR_Gigabit_Ethernet
            // )
            //
            // //
            // // //|| list[i] == SysConst.LAYERRATE_LR_RS_STM_1
            // // //|| list[i] == SysConst.LAYERRATE_LR_RS_STM_4
            // // || list[i] == DicConst.LAYERRATE_LR_RS_STM_16
            // // || list[i] == DicConst.LAYERRATE_LR_RS_STM_64
            // // // || list[i] == SysConst.LAYERRATE_LR_MS_STM_1
            // // // || list[i] == SysConst.LAYERRATE_LR_MS_STM_4
            // // || list[i] == DicConst.LAYERRATE_LR_MS_STM_16
            // // || list[i] == DicConst.LAYERRATE_LR_MS_STM_64)
            // {
            // return DicConst.EOTYPE_OPTIC;
            // }
            // }
            // System.out.println(
            // "Neight 46 nor 47 in list, can't identify the eotype");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // if (!unkownLRs.contains(layerRates)) {
        // LogUtil.error(DicUtil.class, "!!!!!!!!!!!!!!!!!! Unknow layrate : " + layerRates);
        // unkownLRs.add(layerRates);
        // }
        return DicConst.EOTYPE_UNKNOWN;
    }


    public static String getSpeed(String layerRates) {
        List<Integer> list = DicUtil.convertLayerRateList(layerRates);
        for (int rate : list) {
            if (rate == DicConst.LR_PHYSICAL_OPTICAL || rate == DicConst.LR_OPTICAL_SECTION|| rate == DicConst.LR_PHYSICAL_OPTICAL )
                continue;
            String speedByRate = getSpeedByRate(rate);
            if (speedByRate != null)
                return speedByRate;
        }
        return "40G";
    }

    public static String getPtpType(String layerRates) {
        String type = "OPTICAL";
        try {
            List<Integer> list = DicUtil.convertLayerRateList(layerRates);
            if (
                    list.contains(4165)
                    || list.contains(4166)) {
                return "LOGICAL";
            }
            if (list.contains(DicConst.LR_E1_2M) || list.contains(DicConst.LR_DSR_2M)) {
                type = "E1";
            } else if (list.contains(DicConst.LR_DSR_Fast_Ethernet)) {
                type = "FE";
            } else if (list.contains(DicConst.LR_DSR_Gigabit_Ethernet) || list.contains(DicConst.LR_DSR_10Gigabit_Ethernet_LAN)
                    || list.contains(DicConst.LR_DSR_10Gigabit_Ethernet)) {
                type = "GE";
            } else if (list.contains(DicConst.LR_Ethernet)) {
                type = "ETH";
            } else if (list.contains(DicConst.LR_PHYSICAL_ELECTRICAL)

                    || list.contains(4208)



                    ) {
                type = "ELECTRICAL";
            } else if (list.contains(DicConst.LR_PHYSICAL_OPTICAL) || list.contains(DicConst.LR_DSR_OC3_STM1) || list.contains(DicConst.LR_DSR_OC12_STM4)
                    || list.contains(DicConst.LR_DSR_OC48_and_STM16)
                    || list.contains(DicConst.LR_OCH_Data_Unit_1)
                    || list.contains(DicConst.LR_OCH_Data_Unit_2)
                    || list.contains(DicConst.LR_OCH_Data_Unit_3)
                    || list.contains(DicConst.LR_OCH_Transport_Unit_1)
                    || list.contains(DicConst.LR_OCH_Transport_Unit_2)
                    || list.contains(DicConst.LR_OCH_Transport_Unit_3)
                    || list.contains(4177)
                    || list.contains(4178)

                    ) {
                type = "OPTICAL";
            } else if (list.contains(DicConst.LR_ATM_NI)) {
                type = "IMA";
            } else if (list.contains(DicConst.LR_LAG_Fragment)) {
                type = "LAG";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }



    public static String getSpeedByRate(int r) {
        String rate = null;
        if (r == DicConst.LR_DSR_Gigabit_Ethernet)
            return "1G";
        if (r == DicConst.LR_DSR_Fast_Ethernet)
            return "100M";
        if (r == DicConst.LR_DSR_10Gigabit_Ethernet_LAN || r == DicConst.LR_DSR_10Gigabit_Ethernet_WAN)
            return "10G";
        if (r == DicConst.LR_E1_2M) {
            return "2M";
        }
        if (r == DicConst.LR_E2_8M) {
            return "8M";
        }
        if (r == DicConst.LR_E3_34M) {
            return "34M";
        }
        if (r == DicConst.LR_E4_140M || r == DicConst.LR_STS3c_and_AU4_VC4) {
            return "155M";
        }
        if (r == DicConst.LR_E5_565M) {
            return "565M";
        }

        if (r == DicConst.LR_Section_OC48_STS48_and_RS_STM16) {
            return "2.5G";
        }

        if (r == DicConst.LR_Section_OC192_STS192_and_RS_STM64) {
            return "10G";
        }

        //
        if (r == DicConst.LR_DSR_2M) {
            return "2M";
        }
        if (r == DicConst.LR_DSR_OC3_STM1 || r == DicConst.LR_Section_OC3_STS3_and_RS_STM1 || r == DicConst.LR_Line_OC3_STS3_and_MS_STM1
                || r == DicConst.LR_STS3c_and_AU4_VC4) {
            return "155M";
        }
        if (r == DicConst.LR_DSR_OC12_STM4) {
            return "622M";
        }

        if (r == DicConst.LR_DSR_OC24_STM8) {
            return "1.5G";
        }
        if (r == DicConst.LR_DSR_OC192_and_STM64) {
            return "10G";
        }


        if (r == DicConst.LR_Section_OC12_STS12_and_RS_STM4) {
            return "622M";
        }
        if (r == DicConst.LR_DSR_OC48_and_STM16) {
            return "2.5G";
        }
        if (r == DicConst.LR_DSR_10Gigabit_Ethernet) {
            return "10G";
        }

        if (r == DicConst.LR_OCH_Data_Unit_0) {
            return "1.25G";
        }
        if (r == DicConst.LR_OCH_Data_Unit_1) {
            return "2.5G";
        }
        if (r == DicConst.LR_OCH_Data_Unit_2) {
            return "10G";
        }
        if (r == DicConst.LR_OCH_Data_Unit_3) {
            return "40G";
        }
        if (r == DicConst.LR_OCH_Transport_Unit_1) {
            return "2.5G";
        }
        if (r == DicConst.LR_OCH_Transport_Unit_2) {
            return "10G";
        }
        if (r == DicConst.LR_OCH_Transport_Unit_3) {
            return "40G";
        }

        if (r == 4165 || r == 4166 || r == 4208)
            return "1G";


        return rate;
    }

    public static void main(String[] args) {
        String sectionDn = "EMS:TZ-OTNU31-1-P@TopologicalLink:/d=src/ManagedElement{70127685(P)}FTP{/direction=sink/rack=0/shelf=7/slot=24/port=78151743}_/d=sink/ManagedElement{70127685(P)}FTP{/direction=src/rack=0/shelf=7/slot=4/port=78200862}";
        String[] split = sectionDn.split("/d=");
        String s = split[2].replaceAll("\\{",":").replaceAll("}","");

        System.out.println("split = " + split);
    }


}
