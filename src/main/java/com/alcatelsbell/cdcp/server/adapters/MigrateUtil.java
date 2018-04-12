package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.IllegalDNStringException;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.valueobject.BObject;
import org.asb.mule.probe.framework.entity.*;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.CodeTool;

import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-25
 * Time: 下午3:42
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrateUtil {
	// public static HashMap transMapValue(String value) {
	// HashMap map = new HashMap();
	// if (value != null) {
	// String[] pairs = value.split("\\|\\|");
	// for (int i = 0; i < pairs.length; i++) {
	// String pair = pairs[i];
	// if (pair.contains(Constant.namevalueSplit)) {
	// String[] split = pair.split(Constant.namevalueSplit);
	// if (split != null && split.length == 2) {
	// map.put(split[0], split[1]);
	// }
	// }
	// }
	// }
	// return map;
	// }

	public static HashMap<String,String> transMapValue(String value) {
		HashMap<String,String> map = new HashMap<String,String>();
		if (value != null) {
			String[] trans = value.split(Constant.dnSplit);
			for (String tran : trans) {
				String[] pairs = tran.split(Constant.listSplitReg);
				for (String pair : pairs) {
					if (pair.contains(Constant.namevalueSplit)) {
						String[] split = pair.split(Constant.namevalueSplit);
						if (split != null && split.length == 2) {
							map.put(split[0], split[1]);
						}
					}
				}
			}
		}
		return map;
	}

	public static HashMap queryAllMap(SqliteDelegation sd, Class cls) {
		HashMap map = new HashMap();
		List list = sd.queryAll(cls);
		for (int i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			String dn = ((BObject) o).getDn();
			map.put(dn, o);
		}
		return map;
	}

    public static String getCrossCtp(String ctp,List<CCrossConnect> ccs) {
        if (ccs != null) {
            for (CCrossConnect cc : ccs) {
                if (cc.getAend().equals(ctp))
                    return cc.getZend();
                if (cc.getZend().equals(ctp))
                    return cc.getAend();
            }
        }
        return null;
    }

	public static void checkRoute(SqliteDelegation sd) {
		List<TrafficTrunk> tts = sd.queryAll(TrafficTrunk.class);
		HashMap<String, IPCrossconnection> ccs = queryAllMap(sd, IPCrossconnection.class);
		HashMap<String, Section> sections = queryAllMap(sd, Section.class);

		HashMap<String, TrafficTrunk> trunkHashMap = new HashMap<String, TrafficTrunk>();

		List<R_TrafficTrunk_CC_Section> ccss = sd.queryAll(R_TrafficTrunk_CC_Section.class);
		HashSet<String> routeTrunks = new HashSet<String>();
		for (int j = 0; j < ccss.size(); j++) {
			R_TrafficTrunk_CC_Section r_trafficTrunk_cc_section = ccss.get(j);
			String trafficTrunDn = r_trafficTrunk_cc_section.getTrafficTrunDn();
			routeTrunks.add(trafficTrunDn);
			String ccOrSectionDn = r_trafficTrunk_cc_section.getCcOrSectionDn();
			if (r_trafficTrunk_cc_section.getType().equals("CC")) {
				if (!ccs.containsKey(ccOrSectionDn)) {
					System.out.println("Failed to find cc : " + ccOrSectionDn);
				}

			}

			else if (r_trafficTrunk_cc_section.getType().equals("SECTION")) {
				if (!sections.containsKey(ccOrSectionDn)) {
					System.out.println("Failed to find section : " + ccOrSectionDn);
				}

			} else {

			}
		}

		for (int i = 0; i < tts.size(); i++) {
			TrafficTrunk trafficTrunk = tts.get(i);
			if (trafficTrunk.getDn().contains("TUNNELTRAIL")) {
				trunkHashMap.put(trafficTrunk.getDn(), trafficTrunk);
				if (!routeTrunks.contains(trafficTrunk.getDn())) {
					System.out.println(trafficTrunk.getDn() + " has no route!!!");
				}
			}
		}
	}

	public static void checkPTP(SqliteDelegation sd) {
		List<PTP> ptps = sd.queryAll(PTP.class);
		List<ManagedElement> mes = sd.queryAll(ManagedElement.class);
		HashSet<String> meNames = new HashSet<String>();
		for (int i = 0; i < ptps.size(); i++) {
			PTP ptp = ptps.get(i);
			String parentDn = ptp.getParentDn();
			meNames.add(parentDn);
		}

		for (int i = 0; i < mes.size(); i++) {
			ManagedElement managedElement = mes.get(i);

			String dn = managedElement.getDn();
			try {
				dn = simpleDN2FullDn(dn, new String[] { "EMS", "ManagedElement" });
				meNames.remove(dn);
			} catch (IllegalDNStringException e) {
				e.printStackTrace();
			}

		}

		for (String mename : meNames) {
			System.out.println(mename);
		}
	}

	public static void checkEquipmentHolders(SqliteDelegation sd) {
		List<EquipmentHolder> equipmentHolders = sd.queryAll(EquipmentHolder.class);
		List<ManagedElement> mes = sd.queryAll(ManagedElement.class);
		HashSet<String> meNames = new HashSet<String>();
		for (int i = 0; i < equipmentHolders.size(); i++) {
			EquipmentHolder equipmentHolder = equipmentHolders.get(i);
			String parentDn = equipmentHolder.getParentDn();
			meNames.add(parentDn);
		}

		for (int i = 0; i < mes.size(); i++) {
			ManagedElement managedElement = mes.get(i);

			String dn = managedElement.getDn();
			try {
				dn = simpleDN2FullDn(dn, new String[] { "EMS", "ManagedElement" });
			} catch (IllegalDNStringException e) {
				e.printStackTrace();
			}
			if (!meNames.contains(dn)) {
				System.out.println(managedElement.getDn() + "\t" + managedElement.getNativeEMSName() + "\t" + managedElement.getProductName());
			}
		}
	}

	public static String fullDn2SimpleDn(String fullDn) {
		String[] split = fullDn.split("@");
		StringBuilder simple = new StringBuilder();
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			if (s.contains(":")) {
				String value = s.substring(s.indexOf(":") + 1);
				simple.append(value);
				if (i < split.length - 1)
					simple.append("@");
			}
		}
		return simple.toString();
	}

	public static String simpleDN2FullDn(String simpleDn, String[] keys) throws IllegalDNStringException {
		if (simpleDn.isEmpty())
			return "";
		String[] split = simpleDn.split("@");
		StringBuilder full = new StringBuilder();
		if (split.length != keys.length) {
			throw new IllegalDNStringException("simplee dn :" + simpleDn + " failed to convert");
		}
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			String key = keys[i];
			full.append(key).append(":").append(s);
			if (i < split.length - 1)
				full.append("@");
		}
		return full.toString();
	}

	public static List<String> simpleDN2FullDns(String simpleDn, String[] keys) throws IllegalDNStringException {
		if (simpleDn == null || simpleDn.isEmpty()) {
			return new ArrayList();
		}
		List<String> list = new ArrayList();
		String[] split = simpleDn.split(Constant.listSplitReg);
		if (split == null || split.length == 0) {
			try {
				list.add(simpleDN2FullDn(simpleDn, keys));
			} catch (IllegalDNStringException e) {
				throw new IllegalDNStringException(e.getMessage() + " ; full dn = " + simpleDn);
			}
		} else {
			for (int i = 0; i < split.length; i++) {
				String s = split[i];
				list.add(simpleDN2FullDn(s, keys));

			}
		}
		return list;
	}

	/**
	 * /rack=1/shelf=1/domain=ptn/type=ethtrunk/port=25
	 * 
	 * @param info
	 * @param keys
	 * @return
	 */
	public static String extractLocationInfo(String info, String[] keys) {
		HashSet<String> set = new HashSet();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			set.add(key);
		}
		String[] split = info.split("/");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			if (s == null || s.trim().isEmpty() || !s.contains("="))
				continue;

			String name = s.substring(0, s.indexOf("="));
			String value = s.substring(s.indexOf("=") + 1);
			if (set.contains(name)) {
				sb.append("/").append(name).append("=").append(value);
			}

		}
		return sb.toString();
	}

	/**
	 *
	 * @param routes
	 * @param ctp
	 * @param childCTP 是否考虑交叉的一端是给出的CTP的子CTP的情况，比如给出的CTP是ge=1,交叉一端是ge=1/odu=1，也算
	 * @return
	 */
    public static LinkInfo findCC(List<R_TrafficTrunk_CC_Section> routes,String ctp, boolean childCTP) {
		if (routes== null) return null;
		for (R_TrafficTrunk_CC_Section route : routes) {
			if (route.getDn().equals("d526876e-8b38-4ddc-964e-b27b07224044"))
				System.out.println();
			if (route.getType().equals("CC") && (route.getaEnd().equals(ctp) || route.getzEnd().equals(ctp)))
				return new LinkInfo(route.getCcOrSectionDn(),"CC",route,route.getaEnd().equals(ctp) ? route.getzEnd() :route.getaEnd(),ctp);

		}

		if (childCTP) {
			//考虑子CTP的情况
			for (R_TrafficTrunk_CC_Section route : routes) {

				if (route.getType().equals("CC") && (route.getaEnd().startsWith(ctp + "/") || route.getzEnd().startsWith(ctp + "/")))
					return new LinkInfo(route.getCcOrSectionDn(),"CC",route,route.getaEnd().startsWith(ctp + "/")
							? route.getzEnd() :route.getaEnd(),ctp);
			}
		}
		return null;
	}

	public static LinkInfo findChannel(List<CChannel> channels,String ctp, boolean childCTP) {
		if (channels== null) return null;
		for (CChannel channel : channels) {
			if ( (channel.getAend().equals(ctp) || channel.getZend().equals(ctp)))
				return new LinkInfo(channel.getDn(),"CHANNEL",channel,channel.getAend().equals(ctp) ? channel.getZend() :channel.getAend(),ctp);

		}

		if (childCTP) {
			//考虑子CTP的情况
			for (CChannel channel : channels) {
				if ( (channel.getAend().startsWith(ctp + "/") || channel.getZend().startsWith(ctp + "/")))
					return new LinkInfo(channel.getDn(),"CHANNEL",channel,channel.getAend().startsWith(ctp + "/")? channel.getZend() :channel.getAend(),ctp);

			}
		}
		return null;
	}

	public static LinkInfo findChannelInSameCard(List<CChannel> channels,String ctp, boolean childCTP) {
		if (channels== null) return null;
		for (CChannel channel : channels) {
			if ( (channel.getAend().equals(ctp) || channel.getZend().equals(ctp)))
				return new LinkInfo(channel.getDn(),"CHANNEL",channel,channel.getAend().equals(ctp) ? channel.getZend() :channel.getAend(),ctp);

		}

		if (childCTP) {
			//考虑子CTP的情况
			for (CChannel channel : channels) {
				if ( (channel.getAend().startsWith(ctp + "/") || channel.getZend().startsWith(ctp + "/")))
					return new LinkInfo(channel.getDn(),"CHANNEL",channel,channel.getAend().startsWith(ctp + "/")? channel.getZend() :channel.getAend(),ctp);

			}
		}
		return null;
	}

	public static boolean ctpInSameCard(String ctpa,String ctpz) {
		ctpa = ctpa.replaceAll("direction=src","");
		ctpa = ctpa.replaceAll("direction=sink","");
		ctpz = ctpz.replaceAll("direction=src","");
		ctpz = ctpz.replaceAll("direction=sink","");
		if (DNUtil.extractNEDn(ctpa).equals(DNUtil.extractNEDn(ctpz))
				&&DNUtil.extractSlotDn(ctpa).equals(DNUtil.extractSlotDn(ctpz))
				&&DNUtil.extractRackDn(ctpa).equals(DNUtil.extractRackDn(ctpz))
				&&DNUtil.extractShelfDn(ctpa).equals(DNUtil.extractShelfDn(ctpz))
				)
			return true;
		return false;
	}





	public static void main(String[] args) {
		System.out.println("args = " + ctpInSameCard("EMS:TZ-OTNU31-1-P@ManagedElement:70127557(P)@PTP:/direction=src/rack=0/shelf=2/slot=10/port=26427393@CTP:/layerrate=41/oms=1/och=76",
				"EMS:TZ-OTNU31-1-P@ManagedElement:70127557(P)@PTP:/direction=sink/rack=0/shelf=2/slot=110/port=26509313"));
		String[] split = "/rack=1/shelf=1/domain=ptn/type=ethtrunk/port=25".split("/");
		System.out.println(fullDn2SimpleDn("EMS:HZ-U2000-2-P@ManagedElement:720903@FTP:/rack=1/shelf=1/domain=ptn/type=ethtrunk/port=11"));
	}

}
