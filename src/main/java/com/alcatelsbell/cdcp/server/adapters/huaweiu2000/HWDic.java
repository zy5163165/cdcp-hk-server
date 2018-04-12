package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.common.VendorDicEntry;
import com.alcatelsbell.cdcp.util.DicConst;

import java.lang.reflect.Field;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-31
 * Time: 上午10:57
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HWDic {
    public static final VendorDicEntry  LR_Not_Applicable= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1 , DicConst. LR_Not_Applicable);
    public static final VendorDicEntry  LR_T1_and_DS1_1_5M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,2 , DicConst. LR_T1_and_DS1_1_5M);
    public static final VendorDicEntry  LR_T2_and_DS2_6M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,3 , DicConst. LR_T2_and_DS2_6M);
    public static final VendorDicEntry  LR_T3_and_DS3_45M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,4 , DicConst. LR_T3_and_DS3_45M);
    public static final VendorDicEntry  LR_E1_2M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,5 , DicConst. LR_E1_2M);
    public static final VendorDicEntry  LR_E2_8M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,6 , DicConst. LR_E2_8M);
    public static final VendorDicEntry  LR_E3_34M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,7 , DicConst. LR_E3_34M);
    public static final VendorDicEntry  LR_E4_140M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,8 , DicConst. LR_E4_140M);
    public static final VendorDicEntry  LR_E5_565M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,9 , DicConst. LR_E5_565M);
    public static final VendorDicEntry  LR_VT1_5_and_TU11_VC11= new VendorDicEntry(DicConst.DIC_LAYER_RATE,10 , DicConst. LR_VT1_5_and_TU11_VC11);
    public static final VendorDicEntry  LR_VT2_and_TU12_VC12= new VendorDicEntry(DicConst.DIC_LAYER_RATE,11 , DicConst. LR_VT2_and_TU12_VC12);
    public static final VendorDicEntry  LR_VT6_and_TU2_VC2= new VendorDicEntry(DicConst.DIC_LAYER_RATE,12 , DicConst. LR_VT6_and_TU2_VC2);
    public static final VendorDicEntry  LR_Low_Order_TU3_VC3= new VendorDicEntry(DicConst.DIC_LAYER_RATE,13 , DicConst. LR_Low_Order_TU3_VC3);
    public static final VendorDicEntry  LR_STS1_and_AU3_High_Order_VC3= new VendorDicEntry(DicConst.DIC_LAYER_RATE,14 , DicConst. LR_STS1_and_AU3_High_Order_VC3);
    public static final VendorDicEntry  LR_STS3c_and_AU4_VC4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,15 , DicConst. LR_STS3c_and_AU4_VC4);
    public static final VendorDicEntry  LR_STS12c_and_VC4_4c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,16 , DicConst. LR_STS12c_and_VC4_4c);
    public static final VendorDicEntry  LR_STS48c_and_VC4_16c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,17 , DicConst. LR_STS48c_and_VC4_16c);
    public static final VendorDicEntry  LR_STS192c_and_VC4_64c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,18 , DicConst. LR_STS192c_and_VC4_64c);
    public static final VendorDicEntry  LR_Section_OC1_STS1_and_RS_STM0= new VendorDicEntry(DicConst.DIC_LAYER_RATE,19 , DicConst. LR_Section_OC1_STS1_and_RS_STM0);
    public static final VendorDicEntry  LR_Section_OC3_STS3_and_RS_STM1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,20 , DicConst. LR_Section_OC3_STS3_and_RS_STM1);
    public static final VendorDicEntry  LR_Section_OC12_STS12_and_RS_STM4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,21 , DicConst. LR_Section_OC12_STS12_and_RS_STM4);
    public static final VendorDicEntry  LR_Section_OC48_STS48_and_RS_STM16= new VendorDicEntry(DicConst.DIC_LAYER_RATE,22 , DicConst. LR_Section_OC48_STS48_and_RS_STM16);
    public static final VendorDicEntry  LR_Section_OC192_STS192_and_RS_STM64= new VendorDicEntry(DicConst.DIC_LAYER_RATE,23 , DicConst. LR_Section_OC192_STS192_and_RS_STM64);
    public static final VendorDicEntry  LR_Line_OC1_STS1_and_MS_STM0= new VendorDicEntry(DicConst.DIC_LAYER_RATE,24 , DicConst. LR_Line_OC1_STS1_and_MS_STM0);
    public static final VendorDicEntry  LR_Line_OC3_STS3_and_MS_STM1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,25 , DicConst. LR_Line_OC3_STS3_and_MS_STM1);
    public static final VendorDicEntry  LR_Line_OC12_STS12_and_MS_STM4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,26 , DicConst. LR_Line_OC12_STS12_and_MS_STM4);
    public static final VendorDicEntry  LR_Line_OC48_STS48_and_MS_STM16= new VendorDicEntry(DicConst.DIC_LAYER_RATE,27 , DicConst. LR_Line_OC48_STS48_and_MS_STM16);
    public static final VendorDicEntry  LR_Line_OC192_STS192_and_MS_STM64= new VendorDicEntry(DicConst.DIC_LAYER_RATE,28 , DicConst. LR_Line_OC192_STS192_and_MS_STM64);
    public static final VendorDicEntry  LR_Optical_Channel= new VendorDicEntry(DicConst.DIC_LAYER_RATE,40 , DicConst. LR_Optical_Channel);
    public static final VendorDicEntry  LR_Optical_Multiplex_Section= new VendorDicEntry(DicConst.DIC_LAYER_RATE,41 , DicConst. LR_Optical_Multiplex_Section);
    public static final VendorDicEntry  LR_Optical_Transmission_Section= new VendorDicEntry(DicConst.DIC_LAYER_RATE,42 , DicConst. LR_Optical_Transmission_Section);
    public static final VendorDicEntry  LR_ATM_NI= new VendorDicEntry(DicConst.DIC_LAYER_RATE,43 , DicConst. LR_ATM_NI);
    public static final VendorDicEntry  LR_ATM_VP= new VendorDicEntry(DicConst.DIC_LAYER_RATE,44 , DicConst. LR_ATM_VP);
    public static final VendorDicEntry  LR_ATM_VC= new VendorDicEntry(DicConst.DIC_LAYER_RATE,45 , DicConst. LR_ATM_VC);
    public static final VendorDicEntry  LR_PHYSICAL_ELECTRICAL= new VendorDicEntry(DicConst.DIC_LAYER_RATE,46 , DicConst. LR_PHYSICAL_ELECTRICAL);
    public static final VendorDicEntry  LR_PHYSICAL_OPTICAL= new VendorDicEntry(DicConst.DIC_LAYER_RATE,47 , DicConst. LR_PHYSICAL_OPTICAL);
    public static final VendorDicEntry  LR_PHYSICAL_MEDIALESS= new VendorDicEntry(DicConst.DIC_LAYER_RATE,48 , DicConst. LR_PHYSICAL_MEDIALESS);
    public static final VendorDicEntry  LR_OPTICAL_SECTION= new VendorDicEntry(DicConst.DIC_LAYER_RATE,49 , DicConst. LR_OPTICAL_SECTION);
    public static final VendorDicEntry  LR_DIGITAL_SIGNAL_RATE= new VendorDicEntry(DicConst.DIC_LAYER_RATE,50 , DicConst. LR_DIGITAL_SIGNAL_RATE);
    public static final VendorDicEntry  LR_Async_FOTS_150M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,51 , DicConst. LR_Async_FOTS_150M);
    public static final VendorDicEntry  LR_Async_FOTS_417M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,52 , DicConst. LR_Async_FOTS_417M);
    public static final VendorDicEntry  LR_Async_FOTS_560M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,53 , DicConst. LR_Async_FOTS_560M);
    public static final VendorDicEntry  LR_Async_FOTS_565M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,54 , DicConst. LR_Async_FOTS_565M);
    public static final VendorDicEntry  LR_Async_FOTS_1130M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,55 , DicConst. LR_Async_FOTS_1130M);
    public static final VendorDicEntry  LR_Async_FOTS_1G7= new VendorDicEntry(DicConst.DIC_LAYER_RATE,56 , DicConst. LR_Async_FOTS_1G7);
    public static final VendorDicEntry  LR_Async_FOTS_1G8= new VendorDicEntry(DicConst.DIC_LAYER_RATE,57 , DicConst. LR_Async_FOTS_1G8);
    public static final VendorDicEntry  LR_D1_Video= new VendorDicEntry(DicConst.DIC_LAYER_RATE,58 , DicConst. LR_D1_Video);
    public static final VendorDicEntry  LR_ESCON= new VendorDicEntry(DicConst.DIC_LAYER_RATE,59 , DicConst. LR_ESCON);
    public static final VendorDicEntry  LR_ETR= new VendorDicEntry(DicConst.DIC_LAYER_RATE,60 , DicConst. LR_ETR);
    public static final VendorDicEntry  LR_Fast_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,61 , DicConst. LR_Fast_Ethernet);
    public static final VendorDicEntry  LR_FC_12_133M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,62 , DicConst. LR_FC_12_133M);
    public static final VendorDicEntry  LR_FC_25_266M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,63 , DicConst. LR_FC_25_266M);
    public static final VendorDicEntry  LR_FC_50_531M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,64 , DicConst. LR_FC_50_531M);
    public static final VendorDicEntry  LR_FC_100_1063M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,65 , DicConst. LR_FC_100_1063M);
    public static final VendorDicEntry  LR_FDDI= new VendorDicEntry(DicConst.DIC_LAYER_RATE,66 , DicConst. LR_FDDI);
    public static final VendorDicEntry  LR_FICON= new VendorDicEntry(DicConst.DIC_LAYER_RATE,67 , DicConst. LR_FICON);
    public static final VendorDicEntry  LR_Gigabit_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,68 , DicConst. LR_Gigabit_Ethernet);
    public static final VendorDicEntry  LR_DS0_64K= new VendorDicEntry(DicConst.DIC_LAYER_RATE,69 , DicConst. LR_DS0_64K);
    public static final VendorDicEntry  LR_ISDN_BRI= new VendorDicEntry(DicConst.DIC_LAYER_RATE,70 , DicConst. LR_ISDN_BRI);
    public static final VendorDicEntry  LR_POTS= new VendorDicEntry(DicConst.DIC_LAYER_RATE,71 , DicConst. LR_POTS);
    public static final VendorDicEntry  LR_DSR_OC1_STM0= new VendorDicEntry(DicConst.DIC_LAYER_RATE,72 , DicConst. LR_DSR_OC1_STM0);
    public static final VendorDicEntry  LR_DSR_OC3_STM1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,73 , DicConst. LR_DSR_OC3_STM1);
    public static final VendorDicEntry  LR_DSR_OC12_STM4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,74 , DicConst. LR_DSR_OC12_STM4);
    public static final VendorDicEntry  LR_DSR_OC24_STM8= new VendorDicEntry(DicConst.DIC_LAYER_RATE,75 , DicConst. LR_DSR_OC24_STM8);
    public static final VendorDicEntry  LR_DSR_OC48_and_STM16= new VendorDicEntry(DicConst.DIC_LAYER_RATE,76 , DicConst. LR_DSR_OC48_and_STM16);
    public static final VendorDicEntry  LR_DSR_OC192_and_STM64= new VendorDicEntry(DicConst.DIC_LAYER_RATE,77 , DicConst. LR_DSR_OC192_and_STM64);
    public static final VendorDicEntry  LR_DSR_OC768_and_STM256= new VendorDicEntry(DicConst.DIC_LAYER_RATE,78 , DicConst. LR_DSR_OC768_and_STM256);
    public static final VendorDicEntry  LR_DSR_1_5M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,79 , DicConst. LR_DSR_1_5M);
    public static final VendorDicEntry  LR_DSR_2M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,80 , DicConst. LR_DSR_2M);
    public static final VendorDicEntry  LR_DSR_6M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,81 , DicConst. LR_DSR_6M);
    public static final VendorDicEntry  LR_DSR_8M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,82 , DicConst. LR_DSR_8M);
    public static final VendorDicEntry  LR_DSR_34M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,83 , DicConst. LR_DSR_34M);
    public static final VendorDicEntry  LR_DSR_45M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,84 , DicConst. LR_DSR_45M);
    public static final VendorDicEntry  LR_DSR_140M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,85 , DicConst. LR_DSR_140M);
    public static final VendorDicEntry  LR_DSR_565M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,86 , DicConst. LR_DSR_565M);
    public static final VendorDicEntry  LR_DSR_Gigabit_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,87 , DicConst. LR_DSR_Gigabit_Ethernet);
    public static final VendorDicEntry  LR_Section_OC24_STS24_and_RS_STM8= new VendorDicEntry(DicConst.DIC_LAYER_RATE,88 , DicConst. LR_Section_OC24_STS24_and_RS_STM8);
    public static final VendorDicEntry  LR_Line_OC24_STS24_and_MS_STM8= new VendorDicEntry(DicConst.DIC_LAYER_RATE,89 , DicConst. LR_Line_OC24_STS24_and_MS_STM8);
    public static final VendorDicEntry  LR_Section_OC768_STS768_and_RS_STM256= new VendorDicEntry(DicConst.DIC_LAYER_RATE,90 , DicConst. LR_Section_OC768_STS768_and_RS_STM256);
    public static final VendorDicEntry  LR_Line_OC768_STS768_and_MS_STM256= new VendorDicEntry(DicConst.DIC_LAYER_RATE,91 , DicConst. LR_Line_OC768_STS768_and_MS_STM256);
    public static final VendorDicEntry  LR_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,96 , DicConst. LR_Ethernet);
    public static final VendorDicEntry  LR_DSR_10Gigabit_Ethernet_LAN= new VendorDicEntry(DicConst.DIC_LAYER_RATE,8008 , DicConst. LR_DSR_10Gigabit_Ethernet_LAN);
    public static final VendorDicEntry  LR_DSR_10Gigabit_Ethernet_WAN= new VendorDicEntry(DicConst.DIC_LAYER_RATE,8009 , DicConst. LR_DSR_10Gigabit_Ethernet_WAN);


    public static final VendorDicEntry  LR_DSR_10Gigabit_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,113 , DicConst. LR_DSR_10Gigabit_Ethernet);



    public static int getMappedValue(String type,int vendorValue) {
        Field[] declaredFields = HWDic.class.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            try {
                Field declaredField = declaredFields[i];
                Object value = declaredField.get(HWDic.class);
                if (value instanceof  VendorDicEntry) {
                    if (((VendorDicEntry) value).type.equals(type)) {
                        if (((VendorDicEntry) value).vendorValue == vendorValue)
                            return ((VendorDicEntry) value).value;
                    }
                }
            } catch (IllegalAccessException e) {

            }
        }
        return -1;
    }

}
