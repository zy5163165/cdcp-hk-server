package com.alcatelsbell.cdcp.util;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-31
 * Time: 上午9:03
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DicConst {
	public static final int LR_Not_Applicable = 1;
	public static final int LR_T1_and_DS1_1_5M = 2;
	public static final int LR_T2_and_DS2_6M = 3;
	public static final int LR_T3_and_DS3_45M = 4;
	public static final int LR_E1_2M = 5;
	public static final int LR_E2_8M = 6;
	public static final int LR_E3_34M = 7;
	public static final int LR_E4_140M = 8;
	public static final int LR_E5_565M = 9;
	public static final int LR_VT1_5_and_TU11_VC11 = 10;
	public static final int LR_VT2_and_TU12_VC12 = 11;
	public static final int LR_VT6_and_TU2_VC2 = 12;
	public static final int LR_Low_Order_TU3_VC3 = 13;
	public static final int LR_STS1_and_AU3_High_Order_VC3 = 14;
	public static final int LR_STS3c_and_AU4_VC4 = 15;
	public static final int LR_STS12c_and_VC4_4c = 16;
	public static final int LR_STS48c_and_VC4_16c = 17;
	public static final int LR_STS192c_and_VC4_64c = 18;
	public static final int LR_Section_OC1_STS1_and_RS_STM0 = 19;
	public static final int LR_Section_OC3_STS3_and_RS_STM1 = 20;
	public static final int LR_Section_OC12_STS12_and_RS_STM4 = 21;
	public static final int LR_Section_OC48_STS48_and_RS_STM16 = 22;
	public static final int LR_Section_OC192_STS192_and_RS_STM64 = 23;
	public static final int LR_Line_OC1_STS1_and_MS_STM0 = 24;
	public static final int LR_Line_OC3_STS3_and_MS_STM1 = 25;
	public static final int LR_Line_OC12_STS12_and_MS_STM4 = 26;
	public static final int LR_Line_OC48_STS48_and_MS_STM16 = 27;
	public static final int LR_Line_OC192_STS192_and_MS_STM64 = 28;
	public static final int LR_Optical_Channel = 40;
	public static final int LR_Optical_Multiplex_Section = 41;
	public static final int LR_Optical_Transmission_Section = 42;

	public static final int LR_ATM_NI = 43; // HW
	public static final int LR_ATM_VP = 44; // HW
	public static final int LR_ATM_VC = 45; // HW

	public static final int LR_PHYSICAL_ELECTRICAL = 46;
	public static final int LR_PHYSICAL_OPTICAL = 47;
	public static final int LR_PHYSICAL_MEDIALESS = 48;
	public static final int LR_OPTICAL_SECTION = 49;
	public static final int LR_DIGITAL_SIGNAL_RATE = 50;

	public static final int LR_Async_FOTS_150M = 51; // HW
	public static final int LR_Async_FOTS_417M = 52; // HW
	public static final int LR_Async_FOTS_560M = 53; // HW
	public static final int LR_Async_FOTS_565M = 54; // HW
	public static final int LR_Async_FOTS_1130M = 55; // HW
	public static final int LR_Async_FOTS_1G7 = 56; // HW
	public static final int LR_Async_FOTS_1G8 = 57; // HW
	public static final int LR_D1_Video = 58; // HW

	public static final int LR_ESCON = 59;
	public static final int LR_ETR = 60; // hw
	public static final int LR_Fast_Ethernet = 61;

	public static final int LR_FC_12_133M = 62; // hw
	public static final int LR_FC_25_266M = 63; // hw
	public static final int LR_FC_50_531M = 64; // hw
	public static final int LR_FC_100_1063M = 65; // hw
	public static final int LR_FDDI = 66; // hw

	public static final int LR_FICON = 67;
	public static final int LR_Gigabit_Ethernet = 68;
	public static final int LR_DS0_64K = 69;

	public static final int LR_ISDN_BRI = 70; // hw
	public static final int LR_POTS = 71; // hw

	public static final int LR_DSR_OC1_STM0 = 72;
	public static final int LR_DSR_OC3_STM1 = 73;
	public static final int LR_DSR_OC12_STM4 = 74;
	public static final int LR_DSR_OC24_STM8 = 75;
	public static final int LR_DSR_OC48_and_STM16 = 76;
	public static final int LR_DSR_OC192_and_STM64 = 77;
	public static final int LR_DSR_OC768_and_STM256 = 78;
	public static final int LR_DSR_1_5M = 79;
	public static final int LR_DSR_2M = 80;
	public static final int LR_DSR_6M = 81;
	public static final int LR_DSR_8M = 82;
	public static final int LR_DSR_34M = 83;
	public static final int LR_DSR_45M = 84;
	public static final int LR_DSR_140M = 85;
	public static final int LR_DSR_565M = 86;
	public static final int LR_DSR_Gigabit_Ethernet = 87;
	public static final int LR_Section_OC24_STS24_and_RS_STM8 = 88;
	public static final int LR_Line_OC24_STS24_and_MS_STM8 = 89;
	public static final int LR_Section_OC768_STS768_and_RS_STM256 = 90;
	public static final int LR_Line_OC768_STS768_and_MS_STM256 = 91;

	public static final int LR_STS768c_and_VC4_256c = 92;
	public static final int LR_DSR_2xSTM1 = 93;
	public static final int LR_E20_2x2M = 94;
	public static final int LR_E30_8x2M = 95;
	public static final int LR_Ethernet = 96;
	public static final int LR_DSR_Fast_Ethernet = 97;
	public static final int LR_Encapsulation = 98;
	public static final int LR_Fragment = 99;
	public static final int LR_STS6c_and_VC4_2c = 100;
	public static final int LR_STS9c_and_VC4_3c = 101;
	public static final int LR_STS21c_and_VC4_7c = 102;
	public static final int LR_STS24c_and_VC4_8c = 103;
	public static final int LR_OCH_Data_Unit_1 = 104;
	public static final int LR_OCH_Data_Unit_2 = 105;
	public static final int LR_OCH_Data_Unit_3 = 106;
	public static final int LR_OCH_Transport_Unit_1 = 107;
	public static final int LR_OCH_Transport_Unit_2 = 108;
	public static final int LR_OCH_Transport_Unit_3 = 109;
	public static final int LR_DSR_OTU1 = 110;
	public static final int LR_DSR_OTU2 = 111;
	public static final int LR_DSR_OTU3 = 112;
	public static final int LR_DSR_10Gigabit_Ethernet = 113;
	public static final int LR_DSL = 299;
	public static final int LR_DSR_DVB = 302;
	public static final int LR_DVB = 303;
	public static final int LR_RPR = 304;
	public static final int LR_LAG_Fragment = 305;
	public static final int LR_IPTV = 306;
	public static final int LR_Unknown = 307;
	public static final int LR_MPLS = 308;
	public static final int LR_T_MPLS = 309;
	public static final int LR_FC_200_2126M = 310;
	public static final int LR_FC_400_4250M = 311;
	public static final int LR_FC_800_8500M = 312;
	public static final int LR_FC_1000_10520M = 313;
	public static final int LR_T_MPLS_Channel = 1500;
	public static final int LR_T_MPLS_Section = 1501;
	public static final int LR_OCH_Transport_Unit_2e = 1600;
	public static final int LR_OCH_Data_Unit_0 = 1601;
	public static final int LR_OCH_Data_Unit_2e = 1602;
	public static final int LR_OCH_Transport_Unit_2f = 1603;
	public static final int LR_OCH_Data_Unit_2f = 1604;
	public static final int LR_OCH_Data_Unit_3e1 = 1605;
	public static final int LR_OCH_Transport_Unit_3e1 = 1606;
	public static final int LR_OCH_Transport_Unit_OPTS = 1607;
	
	public static final int LR_OCH_Data_Unit_4 = 8041; // hw extend ODU4
	public static final int LR_OCH_Transport_Unit_4 = 8042; // hw extend OTU4
	public static final int LR_OCH_Data_Unit_5G = 8006; // hw extend ODU5G
	public static final int LR_OCH_Transport_Unit_5G = 8007; // hw extend OTU5G

	public static final int LR_DSR_10Gigabit_Ethernet_LAN = 8008; // hw extend
	public static final int LR_DSR_10Gigabit_Ethernet_WAN = 8009; // hw extend
	public static final int LR_DSR_OTU4_Ethernet_WAN = 8043; // hw extend OTU4
    public static final int HWEXT_LR_OCH_Data_Unit_0 = 8031; // hw extend
	public static final int HWEXT_LR_OCH_Data_Unit_Flexible = 335; // hw extend
	public static final int LR_T_MPLS_SECTION = 1000; // alu
	public static final int LR_T_MPLS_PATH = 1001; // alu
	public static final int LR_T_MPLS_CHANNEL = 1002; // alu

	public static final int EOTYPE_ELECTRIC = 0;
	public static final int EOTYPE_OPTIC = 1;
	public static final int EOTYPE_UNKNOWN = 2;

	public static final int TYPE_FE = 0;
	public static final int TYPE_ETH = 1;
	public static final int TYPE_GE = 2;
	public static final int TYPE_E1 = 3;
	public static final int TYPE_IMA = 4;
	public static final int TYPE_LAG = 5;
	public static final int TYPE_ELECTRIC = 6;
	public static final int TYPE_OPTIC = 7;

	public static final int PTP_DIRECTION_BIDIRECTIONAL = 3;
	public static final int PTP_DIRECTION_SOURCE = 2;
	public static final int PTP_DIRECTION_SINK = 1;
    public static final int PTP_DIRECTION_NA = 4;

	public static String DIC_LAYER_RATE = "LAYER_RATE";

	public static final int CONNECTION_DIRECTION_UNI = 0;
	public static final int CONNECTION_DIRECTION_CD_BI = 1;
}
