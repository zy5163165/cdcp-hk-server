package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.nms.util.log.LogUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.asb.mule.probe.framework.service.Constant;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-30
 * Time: 下午4:55
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DicUtil {
	// public DicUtil() {
	// Vector parametersVector = new Vector();
	// for (int i = 0; i < transmissionParameters.length; i++)
	// {
	// short rate = t2gConvertRate(transmissionParameters[i].layer);
	// if (rate != DicConst.LR_NOT_APPLICABLE.value)
	// {
	// parametersVector.addElement(new gxlu_transmissionParameters.LayeredParameters_T(rate,
	// TmfDNFactory.t2g_NSVList(transmissionParameters[i].transmissionParams)));
	// }
	// }
	// gxlu_transmissionParameters.LayeredParameters_T[] parameters = new gxlu_transmissionParameters.LayeredParameters_T[parametersVector.size()];
	// parametersVector.copyInto(parameters);
	// return parameters;
	// }
	//
	//
	// public static short t2gConvertRate(short rate)
	// {
	// Integer gxluRate = (Integer) rateMap.get(new Integer(rate));
	// if (null == gxluRate)
	// {
	// UtilityMgr.error("Need Map Vendor LayerRate " + rate);
	// return gxlu_transmissionParameters.LR_NOT_APPLICABLE.value;
	// }
	// else
	// {
	// return gxluRate.shortValue();
	// }
	// }
	// static {
	// rateMap.put(new Integer(1), new Integer(gxlu_transmissionParameters.LR_NOT_APPLICABLE.value));
	// rateMap.put(new Integer(5), new Integer(gxlu_transmissionParameters.LR_E1.value));
	// rateMap.put(new Integer(6), new Integer(gxlu_transmissionParameters.LR_E2.value));
	// rateMap.put(new Integer(7), new Integer(gxlu_transmissionParameters.LR_E3.value));
	// rateMap.put(new Integer(8), new Integer(gxlu_transmissionParameters.LR_E4.value));
	// rateMap.put(new Integer(11), new Integer(gxlu_transmissionParameters.LR_VC12.value));
	// rateMap.put(new Integer(13), new Integer(gxlu_transmissionParameters.LR_VC3.value));
	// rateMap.put(new Integer(14), new Integer(gxlu_transmissionParameters.LR_VC3.value));
	// rateMap.put(new Integer(15), new Integer(gxlu_transmissionParameters.LR_VC4.value));
	// rateMap.put(new Integer(16), new Integer(gxlu_transmissionParameters.LR_VC4_4C.value));
	// rateMap.put(new Integer(17), new Integer(gxlu_transmissionParameters.LR_VC4_16C.value));
	// rateMap.put(new Integer(18), new Integer(gxlu_transmissionParameters.LR_VC4_64C.value));
	// rateMap.put(new Integer(20), new Integer(gxlu_transmissionParameters.LR_RS_STM_1.value));
	// rateMap.put(new Integer(21), new Integer(gxlu_transmissionParameters.LR_RS_STM_4.value));
	// rateMap.put(new Integer(22), new Integer(gxlu_transmissionParameters.LR_RS_STM_16.value));
	// rateMap.put(new Integer(23), new Integer(gxlu_transmissionParameters.LR_RS_STM_64.value));
	// rateMap.put(new Integer(25), new Integer(gxlu_transmissionParameters.LR_MS_STM_1.value));
	// rateMap.put(new Integer(26), new Integer(gxlu_transmissionParameters.LR_MS_STM_4.value));
	// rateMap.put(new Integer(27), new Integer(gxlu_transmissionParameters.LR_MS_STM_16.value));
	// rateMap.put(new Integer(28), new Integer(gxlu_transmissionParameters.LR_MS_STM_64.value));
	//
	// rateMap.put(new Integer(40), new Integer(gxlu_transmissionParameters.LR_OCH.value));
	// rateMap.put(new Integer(41), new Integer(gxlu_transmissionParameters.LR_OMS.value));
	// rateMap.put(new Integer(42), new Integer(gxlu_transmissionParameters.LR_OTS.value));
	//
	// rateMap.put(new Integer(46), new Integer(gxlu_transmissionParameters.LR_PHYSICAL_ELECTRICAL.value));
	// rateMap.put(new Integer(47), new Integer(gxlu_transmissionParameters.LR_PHYSICAL_OPTICAL.value));
	// rateMap.put(new Integer(50), new Integer(50));
	// rateMap.put(new Integer(61), new Integer(gxlu_transmissionParameters.LR_FAST_ETHERNET.value));
	// rateMap.put(new Integer(68), new Integer(gxlu_transmissionParameters.LR_GB_ETHERNET.value));
	//
	// rateMap.put(new Integer(73), new Integer(85));
	// rateMap.put(new Integer(74), new Integer(86));
	//
	// rateMap.put(new Integer(76), new Integer(87));
	// rateMap.put(new Integer(77), new Integer(88));
	// rateMap.put(new Integer(78), new Integer(120));
	//
	// rateMap.put(new Integer(72), new Integer(7));
	// rateMap.put(new Integer(79), new Integer(65));
	// rateMap.put(new Integer(80), new Integer(5));
	// rateMap.put(new Integer(81), new Integer(50));
	// rateMap.put(new Integer(82), new Integer(6));
	// rateMap.put(new Integer(83), new Integer(7));
	// rateMap.put(new Integer(84), new Integer(4));
	// rateMap.put(new Integer(85), new Integer(8));
	// rateMap.put(new Integer(86), new Integer(9));
	// rateMap.put(new Integer(87), new Integer(68));
	//
	// rateMap.put(new Integer(75), new Integer(124));
	// rateMap.put(new Integer(88), new Integer(125));
	// rateMap.put(new Integer(89), new Integer(124));
	//
	// }
	// 以下都是用国郎的层速率字典

	// 判断光电的

	private static HashSet unkownLRs = new HashSet();
	public static Integer getEOType(String dn,String layerRates) {
	  	if (dn.contains("FTP")) return DicConst.EOTYPE_UNKNOWN;
		return getEOType(layerRates);
	}
	public static Integer getEOType(String layerRates) {
		// if (layerRates == null)
		// return DicConst.EOTYPE_UNKNOWN;
		try {
			List<Integer> list = convertLayerRateList(layerRates);
			for (int rate : list) {
				if (rate == DicConst.LR_PHYSICAL_ELECTRICAL) {
					return DicConst.EOTYPE_ELECTRIC;
				} else if (rate == DicConst.LR_PHYSICAL_OPTICAL) {
					return DicConst.EOTYPE_OPTIC;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if (!unkownLRs.contains(layerRates)) {
		// LogUtil.error(DicUtil.class, "!!!!!!!!!!!!!!!!!! Unknow layrate : " + layerRates);
		// unkownLRs.add(layerRates);
		// }
		return DicConst.EOTYPE_UNKNOWN;
	}

	public static void main(String[] args) {
		System.out.println(getPtpType("97||96"));
	}

	public static String getSpeed(String layerRates) {
		List<Integer> list = convertLayerRateList(layerRates);
		for (int rate : list) {
			if (rate == DicConst.LR_PHYSICAL_OPTICAL || rate == DicConst.LR_OPTICAL_SECTION|| rate == DicConst.LR_PHYSICAL_OPTICAL )
				continue;
			String speedByRate = getSpeedByRate(rate);
			if (speedByRate != null)
				return speedByRate;
		}
		return "";
	}

	public static String getPtpType(String dn, String layerRates) {
		String type = getPtpType(layerRates);
		if (dn.contains("FTP")) {
			if (type != null && (type.equals("IMA") || type.equals("LAG"))) {
				return type;
			}
			return "LOGICAL";
		}

        if (dn.contains("type=lp/"))
            return "LP";
        if (dn.contains("type=mp/"))
            return "mp";
        if (dn.contains("type=mac/"))
            return "mac";
		return type;
	}

	private static String getPtpType(String layerRates) {
		String type = "OPTICAL";
		try {
			List<Integer> list = convertLayerRateList(layerRates);
			if (list.contains(DicConst.LR_E1_2M) || list.contains(DicConst.LR_DSR_2M)) {
				type = "E1";
			} else if (list.contains(DicConst.LR_DSR_Fast_Ethernet)) {
				type = "FE";
			} else if (list.contains(DicConst.LR_DSR_Gigabit_Ethernet)
					) {
				type = "GE";
			} else if (list.contains(DicConst.LR_Ethernet) || list.contains(DicConst.LR_DSR_10Gigabit_Ethernet)  || list.contains(DicConst.LR_DSR_10Gigabit_Ethernet_LAN)) {
				type = "ETH";
			} else if (list.contains(DicConst.LR_PHYSICAL_ELECTRICAL)) {
				type = "ELECTRICAL";
			} else if (list.contains(DicConst.LR_PHYSICAL_OPTICAL) || list.contains(DicConst.LR_DSR_OC3_STM1) || list.contains(DicConst.LR_DSR_OC12_STM4)
					|| list.contains(DicConst.LR_DSR_OC48_and_STM16)
                    || list.contains(DicConst.LR_OCH_Data_Unit_1)
                    || list.contains(DicConst.LR_OCH_Data_Unit_2)
                    || list.contains(DicConst.LR_OCH_Data_Unit_3)
                    || list.contains(DicConst.LR_OCH_Transport_Unit_1)
                    || list.contains(DicConst.LR_OCH_Transport_Unit_2)
                    || list.contains(DicConst.LR_OCH_Transport_Unit_3)

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

	public static int getPtpDirection(String dir) {
        if (dir == null) return DicConst.PTP_DIRECTION_BIDIRECTIONAL;
		if (dir.equals("D_SOURCE"))
			return DicConst.PTP_DIRECTION_SOURCE;
		if (dir.equals("D_SINK"))
			return DicConst.PTP_DIRECTION_SINK;
		if (dir.equals("D_BIDIRECTIONAL"))
			return DicConst.PTP_DIRECTION_BIDIRECTIONAL;
        if (dir.equals("D_NA"))
            return DicConst.PTP_DIRECTION_NA;
		return -1;
	}

	public static int getConnectionDirection(String dir) {
		if (dir.equals("CD_UNI"))
			return DicConst.CONNECTION_DIRECTION_UNI;
		if (dir.equals("CD_BI"))
			return DicConst.CONNECTION_DIRECTION_CD_BI;
		return -1;
	}

	public static List<Integer> convertLayerRateList(String layerRates) {
		List<Integer> list = new ArrayList<Integer>();
		if (layerRates != null) {
			String[] rates = layerRates.split(Constant.listSplitReg);
			for (String rate : rates) {
				try {
					if (rate != null && !rate.trim().isEmpty()) {
						list.add(Integer.parseInt(rate));
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
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

		if (r == 331) {
		  	return "10G";
		}

		if (r == 330) {
			return "2.5G";
		}

		if (r == 334 || r == 339)
			return "100G";

		if (r == DicConst.LR_Line_OC192_STS192_and_MS_STM64) {
			return "10G";
		}

        return rate;
	}

	// public static byte convertLayerRateToCapacity(short rate)
	// {
	// byte result = SysConst.PORTTYPE_CAPACITY_UNKNOWN;
	//
	// switch (rate)
	// {
	// case SysConst.LAYERRATE_LR_E1:
	// result = SysConst.PORTTYPE_CAPACITY_2M;
	// break;
	// case SysConst.LAYERRATE_LR_E2:
	// result = SysConst.PORTTYPE_CAPACITY_8M;
	// break;
	// case SysConst.LAYERRATE_LR_E3:
	// result = SysConst.PORTTYPE_CAPACITY_34M;
	// break;
	// case SysConst.LAYERRATE_LR_E4:
	// result = SysConst.PORTTYPE_CAPACITY_140M;
	// break;
	// case SysConst.LAYERRATE_LR_FASTETHERNET:
	// result = SysConst.PORTTYPE_CAPACITY_100M;
	// break;
	// case SysConst.LAYERRATE_LR_GBETHERNET:
	// result = SysConst.PORTTYPE_CAPACITY_1G;
	// break;
	// case SysConst.LAYERRATE_LR_E5:
	// result = SysConst.PORTTYPE_CAPACITY_565M;
	// break;
	// case SysConst.LAYERRATE_LR_RS_STM_1:
	// case SysConst.LAYERRATE_LR_MS_STM_1:
	// result = SysConst.PORTTYPE_CAPACITY_155M;
	// break;
	// case SysConst.LAYERRATE_LR_RS_STM_4:
	// case SysConst.LAYERRATE_LR_MS_STM_4:
	// result = SysConst.PORTTYPE_CAPACITY_622M;
	// break;
	// case SysConst.LAYERRATE_LR_RS_STM_8:
	// case SysConst.LAYERRATE_LR_MS_STM_8:
	// result = SysConst.PORTTYPE_CAPACITY_1G;
	// break;
	// case SysConst.LAYERRATE_LR_RS_STM_16:
	// case SysConst.LAYERRATE_LR_MS_STM_16:
	// result = SysConst.PORTTYPE_CAPACITY_2G;
	// break;
	// case SysConst.LAYERRATE_LR_RS_STM_64:
	// case SysConst.LAYERRATE_LR_MS_STM_64:
	// result = SysConst.PORTTYPE_CAPACITY_10G;
	// break;
	// case LR_DS0_64K:
	// result = SysConst.PORTTYPE_CAPACITY_64K;
	// break;
	// case LR_128K:
	// result = SysConst.PORTTYPE_CAPACITY_128K;
	// break;
	// }
	// return result;
	// }
	//

}
