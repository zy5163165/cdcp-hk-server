package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.common.VendorDicEntry;
import com.alcatelsbell.cdcp.util.DicConst;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-31
 * Time: 上午10:32
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FHDic {
    public static final VendorDicEntry LR_T1_and_DS1_1_5M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,2 , DicConst.LR_T1_and_DS1_1_5M);
    public static final VendorDicEntry LR_T2_and_DS2_6M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,3, DicConst.LR_T2_and_DS2_6M);
    public static final VendorDicEntry LR_T3_and_DS3_45M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,4, DicConst.LR_T3_and_DS3_45M);
    public static final VendorDicEntry LR_E1_2M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,5, DicConst.LR_E1_2M);
    public static final VendorDicEntry LR_E2_8M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,6, DicConst.LR_E2_8M);
    public static final VendorDicEntry LR_E3_34M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,7, DicConst.LR_E3_34M);
    public static final VendorDicEntry LR_E4_140M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,8, DicConst.LR_E4_140M);
    public static final VendorDicEntry LR_E5_565M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,9, DicConst.LR_E5_565M);
    public static final VendorDicEntry LR_VT1_5_and_TU11_VC11= new VendorDicEntry(DicConst.DIC_LAYER_RATE,10, DicConst.LR_VT1_5_and_TU11_VC11);
    public static final VendorDicEntry LR_VT2_and_TU12_VC12= new VendorDicEntry(DicConst.DIC_LAYER_RATE,11, DicConst.LR_VT2_and_TU12_VC12);
    public static final VendorDicEntry LR_VT6_and_TU2_VC2= new VendorDicEntry(DicConst.DIC_LAYER_RATE,12, DicConst.LR_VT6_and_TU2_VC2);
    public static final VendorDicEntry LR_Low_Order_TU3_VC3= new VendorDicEntry(DicConst.DIC_LAYER_RATE,13, DicConst.LR_Low_Order_TU3_VC3);
    public static final VendorDicEntry LR_STS1_and_AU3_High_Order_VC3= new VendorDicEntry(DicConst.DIC_LAYER_RATE,14, DicConst.LR_STS1_and_AU3_High_Order_VC3);
    public static final VendorDicEntry LR_STS3c_and_AU4_VC4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,15, DicConst.LR_STS3c_and_AU4_VC4);
    public static final VendorDicEntry LR_STS12c_and_VC4_4c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,16, DicConst.LR_STS12c_and_VC4_4c);
    public static final VendorDicEntry LR_STS48c_and_VC4_16c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,17, DicConst.LR_STS48c_and_VC4_16c);
    public static final VendorDicEntry LR_STS192c_and_VC4_64c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,18, DicConst.LR_STS192c_and_VC4_64c);
    public static final VendorDicEntry LR_Section_OC1_STS1_and_RS_STM0= new VendorDicEntry(DicConst.DIC_LAYER_RATE,19, DicConst.LR_Section_OC1_STS1_and_RS_STM0);
    public static final VendorDicEntry LR_Section_OC3_STS3_and_RS_STM1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,20, DicConst.LR_Section_OC3_STS3_and_RS_STM1);
    public static final VendorDicEntry LR_Section_OC12_STS12_and_RS_STM4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,21, DicConst.LR_Section_OC12_STS12_and_RS_STM4);
    public static final VendorDicEntry LR_Section_OC48_STS48_and_RS_STM16= new VendorDicEntry(DicConst.DIC_LAYER_RATE,22, DicConst.LR_Section_OC48_STS48_and_RS_STM16);
    public static final VendorDicEntry LR_Section_OC192_STS192_and_RS_STM64= new VendorDicEntry(DicConst.DIC_LAYER_RATE,23, DicConst.LR_Section_OC192_STS192_and_RS_STM64);
    public static final VendorDicEntry LR_Line_OC1_STS1_and_MS_STM0= new VendorDicEntry(DicConst.DIC_LAYER_RATE,24, DicConst.LR_Line_OC1_STS1_and_MS_STM0);
    public static final VendorDicEntry LR_Line_OC3_STS3_and_MS_STM1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,25, DicConst.LR_Line_OC3_STS3_and_MS_STM1);
    public static final VendorDicEntry LR_Line_OC12_STS12_and_MS_STM4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,26, DicConst.LR_Line_OC12_STS12_and_MS_STM4);
    public static final VendorDicEntry LR_Line_OC48_STS48_and_MS_STM16= new VendorDicEntry(DicConst.DIC_LAYER_RATE,27, DicConst.LR_Line_OC48_STS48_and_MS_STM16);
    public static final VendorDicEntry LR_Line_OC192_STS192_and_MS_STM64= new VendorDicEntry(DicConst.DIC_LAYER_RATE,28, DicConst.LR_Line_OC192_STS192_and_MS_STM64);
    public static final VendorDicEntry LR_Optical_Channel= new VendorDicEntry(DicConst.DIC_LAYER_RATE,40, DicConst.LR_Optical_Channel);
    public static final VendorDicEntry LR_Optical_Multiplex_Section= new VendorDicEntry(DicConst.DIC_LAYER_RATE,41, DicConst.LR_Optical_Multiplex_Section);
    public static final VendorDicEntry LR_Optical_Transmission_Section= new VendorDicEntry(DicConst.DIC_LAYER_RATE,42, DicConst.LR_Optical_Transmission_Section);
    public static final VendorDicEntry LR_PHYSICAL_ELECTRICAL= new VendorDicEntry(DicConst.DIC_LAYER_RATE,46, DicConst.LR_PHYSICAL_ELECTRICAL);
    public static final VendorDicEntry LR_PHYSICAL_OPTICAL= new VendorDicEntry(DicConst.DIC_LAYER_RATE,47, DicConst.LR_PHYSICAL_OPTICAL);
    public static final VendorDicEntry LR_PHYSICAL_MEDIALESS= new VendorDicEntry(DicConst.DIC_LAYER_RATE,48, DicConst.LR_PHYSICAL_MEDIALESS);
    public static final VendorDicEntry LR_OPTICAL_SECTION= new VendorDicEntry(DicConst.DIC_LAYER_RATE,49, DicConst.LR_OPTICAL_SECTION);
    public static final VendorDicEntry LR_DIGITAL_SIGNAL_RATE= new VendorDicEntry(DicConst.DIC_LAYER_RATE,50, DicConst.LR_DIGITAL_SIGNAL_RATE);
    public static final VendorDicEntry LR_ESCON= new VendorDicEntry(DicConst.DIC_LAYER_RATE,59, DicConst.LR_ESCON);
    public static final VendorDicEntry LR_Fast_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,61, DicConst.LR_Fast_Ethernet);
    public static final VendorDicEntry LR_FICON= new VendorDicEntry(DicConst.DIC_LAYER_RATE,67, DicConst.LR_FICON);
    public static final VendorDicEntry LR_Gigabit_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,68, DicConst.LR_Gigabit_Ethernet);
    public static final VendorDicEntry LR_DS0_64K= new VendorDicEntry(DicConst.DIC_LAYER_RATE,69, DicConst.LR_DS0_64K);
    public static final VendorDicEntry LR_DSR_OC1_STM0= new VendorDicEntry(DicConst.DIC_LAYER_RATE,72, DicConst.LR_DSR_OC1_STM0);
    public static final VendorDicEntry LR_DSR_OC3_STM1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,73, DicConst.LR_DSR_OC3_STM1);
    public static final VendorDicEntry LR_DSR_OC12_STM4= new VendorDicEntry(DicConst.DIC_LAYER_RATE,74, DicConst.LR_DSR_OC12_STM4);
    public static final VendorDicEntry LR_DSR_OC24_STM8= new VendorDicEntry(DicConst.DIC_LAYER_RATE,75, DicConst.LR_DSR_OC24_STM8);
    public static final VendorDicEntry LR_DSR_OC48_and_STM16= new VendorDicEntry(DicConst.DIC_LAYER_RATE,76, DicConst.LR_DSR_OC48_and_STM16);
    public static final VendorDicEntry LR_DSR_OC192_and_STM64= new VendorDicEntry(DicConst.DIC_LAYER_RATE,77, DicConst.LR_DSR_OC192_and_STM64);
    public static final VendorDicEntry LR_DSR_OC768_and_STM256= new VendorDicEntry(DicConst.DIC_LAYER_RATE,78, DicConst.LR_DSR_OC768_and_STM256);
    public static final VendorDicEntry LR_DSR_1_5M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,79, DicConst.LR_DSR_1_5M);
    public static final VendorDicEntry LR_DSR_2M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,80, DicConst.LR_DSR_2M);
    public static final VendorDicEntry LR_DSR_6M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,81, DicConst.LR_DSR_6M);
    public static final VendorDicEntry LR_DSR_8M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,82, DicConst.LR_DSR_8M);
    public static final VendorDicEntry LR_DSR_34M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,83, DicConst.LR_DSR_34M);
    public static final VendorDicEntry LR_DSR_45M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,84, DicConst.LR_DSR_45M);
    public static final VendorDicEntry LR_DSR_140M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,85, DicConst.LR_DSR_140M);
    public static final VendorDicEntry LR_DSR_565M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,86, DicConst.LR_DSR_565M);
    public static final VendorDicEntry LR_DSR_Gigabit_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,87, DicConst.LR_DSR_Gigabit_Ethernet);
    public static final VendorDicEntry LR_Section_OC24_STS24_and_RS_STM8= new VendorDicEntry(DicConst.DIC_LAYER_RATE,88, DicConst.LR_Section_OC24_STS24_and_RS_STM8);
    public static final VendorDicEntry LR_Line_OC24_STS24_and_MS_STM8= new VendorDicEntry(DicConst.DIC_LAYER_RATE,89, DicConst.LR_Line_OC24_STS24_and_MS_STM8);
    public static final VendorDicEntry LR_Section_OC768_STS768_and_RS_STM256= new VendorDicEntry(DicConst.DIC_LAYER_RATE,90, DicConst.LR_Section_OC768_STS768_and_RS_STM256);
    public static final VendorDicEntry LR_Line_OC768_STS768_and_MS_STM256= new VendorDicEntry(DicConst.DIC_LAYER_RATE,91, DicConst.LR_Line_OC768_STS768_and_MS_STM256);
    public static final VendorDicEntry LR_STS768c_and_VC4_256c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,92, DicConst.LR_STS768c_and_VC4_256c);
    public static final VendorDicEntry LR_DSR_2xSTM1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,93, DicConst.LR_DSR_2xSTM1);
    public static final VendorDicEntry LR_E20_2x2M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,94, DicConst.LR_E20_2x2M);
    public static final VendorDicEntry LR_E30_8x2M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,95, DicConst.LR_E30_8x2M);
    public static final VendorDicEntry LR_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,96, DicConst.LR_Ethernet);
    public static final VendorDicEntry LR_DSR_Fast_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,97, DicConst.LR_DSR_Fast_Ethernet);
    public static final VendorDicEntry LR_Encapsulation= new VendorDicEntry(DicConst.DIC_LAYER_RATE,98, DicConst.LR_Encapsulation);
    public static final VendorDicEntry LR_Fragment= new VendorDicEntry(DicConst.DIC_LAYER_RATE,99, DicConst.LR_Fragment);
    public static final VendorDicEntry LR_STS6c_and_VC4_2c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,100, DicConst.LR_STS6c_and_VC4_2c);
    public static final VendorDicEntry LR_STS9c_and_VC4_3c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,101, DicConst.LR_STS9c_and_VC4_3c);
    public static final VendorDicEntry LR_STS21c_and_VC4_7c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,102, DicConst.LR_STS21c_and_VC4_7c);
    public static final VendorDicEntry LR_STS24c_and_VC4_8c= new VendorDicEntry(DicConst.DIC_LAYER_RATE,103, DicConst.LR_STS24c_and_VC4_8c);
    public static final VendorDicEntry LR_OCH_Data_Unit_1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,104, DicConst.LR_OCH_Data_Unit_1);
    public static final VendorDicEntry LR_OCH_Data_Unit_2= new VendorDicEntry(DicConst.DIC_LAYER_RATE,105, DicConst.LR_OCH_Data_Unit_2);
    public static final VendorDicEntry LR_OCH_Data_Unit_3= new VendorDicEntry(DicConst.DIC_LAYER_RATE,106, DicConst.LR_OCH_Data_Unit_3);
    public static final VendorDicEntry LR_OCH_Transport_Unit_1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,107, DicConst.LR_OCH_Transport_Unit_1);
    public static final VendorDicEntry LR_OCH_Transport_Unit_2= new VendorDicEntry(DicConst.DIC_LAYER_RATE,108, DicConst.LR_OCH_Transport_Unit_2);
    public static final VendorDicEntry LR_OCH_Transport_Unit_3= new VendorDicEntry(DicConst.DIC_LAYER_RATE,109, DicConst.LR_OCH_Transport_Unit_3);
    public static final VendorDicEntry LR_DSR_OTU1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,110, DicConst.LR_DSR_OTU1);
    public static final VendorDicEntry LR_DSR_OTU2= new VendorDicEntry(DicConst.DIC_LAYER_RATE,111, DicConst.LR_DSR_OTU2);
    public static final VendorDicEntry LR_DSR_OTU3= new VendorDicEntry(DicConst.DIC_LAYER_RATE,112, DicConst.LR_DSR_OTU3);
    public static final VendorDicEntry LR_DSR_10Gigabit_Ethernet= new VendorDicEntry(DicConst.DIC_LAYER_RATE,113, DicConst.LR_DSR_10Gigabit_Ethernet);
    public static final VendorDicEntry LR_DSL= new VendorDicEntry(DicConst.DIC_LAYER_RATE,299, DicConst.LR_DSL);
    public static final VendorDicEntry LR_DSR_DVB= new VendorDicEntry(DicConst.DIC_LAYER_RATE,302, DicConst.LR_DSR_DVB);
    public static final VendorDicEntry LR_DVB= new VendorDicEntry(DicConst.DIC_LAYER_RATE,303, DicConst.LR_DVB);
    public static final VendorDicEntry LR_RPR= new VendorDicEntry(DicConst.DIC_LAYER_RATE,304, DicConst.LR_RPR);
    public static final VendorDicEntry LR_LAG_Fragment= new VendorDicEntry(DicConst.DIC_LAYER_RATE,305, DicConst.LR_LAG_Fragment);
    public static final VendorDicEntry LR_IPTV= new VendorDicEntry(DicConst.DIC_LAYER_RATE,306, DicConst.LR_IPTV);
    public static final VendorDicEntry LR_Unknown= new VendorDicEntry(DicConst.DIC_LAYER_RATE,307, DicConst.LR_Unknown);
    public static final VendorDicEntry LR_MPLS= new VendorDicEntry(DicConst.DIC_LAYER_RATE,308, DicConst.LR_MPLS);
    public static final VendorDicEntry LR_T_MPLS= new VendorDicEntry(DicConst.DIC_LAYER_RATE,309, DicConst.LR_T_MPLS);
    public static final VendorDicEntry LR_FC_200_2126M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,310, DicConst.LR_FC_200_2126M);
    public static final VendorDicEntry LR_FC_400_4250M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,311, DicConst.LR_FC_400_4250M);
    public static final VendorDicEntry LR_FC_800_8500M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,312, DicConst.LR_FC_800_8500M);
    public static final VendorDicEntry LR_FC_1000_10520M= new VendorDicEntry(DicConst.DIC_LAYER_RATE,313, DicConst.LR_FC_1000_10520M);
    public static final VendorDicEntry LR_T_MPLS_Channel= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1500, DicConst.LR_T_MPLS_Channel);
    public static final VendorDicEntry LR_T_MPLS_Section= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1501, DicConst.LR_T_MPLS_Section);
    public static final VendorDicEntry LR_OCH_Transport_Unit_2e= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1600, DicConst.LR_OCH_Transport_Unit_2e);
    public static final VendorDicEntry LR_OCH_Data_Unit_0= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1601, DicConst.LR_OCH_Data_Unit_0);
    public static final VendorDicEntry LR_OCH_Data_Unit_2e= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1602, DicConst.LR_OCH_Data_Unit_2e);
    public static final VendorDicEntry LR_OCH_Transport_Unit_2f= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1603, DicConst.LR_OCH_Transport_Unit_2f);
    public static final VendorDicEntry LR_OCH_Data_Unit_2f= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1604, DicConst.LR_OCH_Data_Unit_2f);
    public static final VendorDicEntry LR_OCH_Data_Unit_3e1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1605, DicConst.LR_OCH_Data_Unit_3e1);
    public static final VendorDicEntry LR_OCH_Transport_Unit_3e1= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1606, DicConst.LR_OCH_Transport_Unit_3e1);
    public static final VendorDicEntry LR_OCH_Transport_Unit_OPTS= new VendorDicEntry(DicConst.DIC_LAYER_RATE,1607, DicConst.LR_OCH_Transport_Unit_OPTS);


    public static int getMappedValue(String type,int vendorValue) {
        Field[] declaredFields = FHDic.class.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            try {
                Field declaredField = declaredFields[i];
                Object value = declaredField.get(FHDic.class);
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


    public static void main(String[] args) throws  Exception {
        System.out.println(FHDic.getMappedValue(DicConst.DIC_LAYER_RATE, 1605));
        BufferedReader br = new BufferedReader(new FileReader("d:\\work\\fh.txt"));
        while (true) {
            String s = br.readLine();
            if (s == null) break;
//            String name = s.substring(s.indexOf("VendorDicEntry") +4 ,s.indexOf("="));
//            System.out.println(s + ",DicConst."+name+");");
            s = s.substring(0,s.indexOf(","));
            String name = s.substring(s.lastIndexOf(" "));
            String value = s.substring(s.indexOf("* ")+2,s.indexOf("="));
            System.out.println("    public static final VendorDicEntry "+name+"= new VendorDicEntry(DicConst.DIC_LAYER_RATE,"+value+",DicConst."+name+");");
        }
    }
}
