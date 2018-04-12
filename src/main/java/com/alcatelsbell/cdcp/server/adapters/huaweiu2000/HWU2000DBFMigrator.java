package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.valueobject.BObject;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.FlowDomainFragment;
import org.asb.mule.probe.framework.entity.IPCrossconnection;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.entity.PWTrail;
import org.asb.mule.probe.framework.entity.R_FTP_PTP;
import org.asb.mule.probe.framework.entity.TrafficTrunk;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.FileLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alcatelsbell.cdcp.nbi.model.CCrossConnect;
import com.alcatelsbell.cdcp.nbi.model.CEquipment;
import com.alcatelsbell.cdcp.nbi.model.CFTP_PTP;
import com.alcatelsbell.cdcp.nbi.model.CPTP;
import com.alcatelsbell.cdcp.nbi.model.CPW;
import com.alcatelsbell.cdcp.nbi.model.CPWE3;
import com.alcatelsbell.cdcp.nbi.model.CPWE3_PW;
import com.alcatelsbell.cdcp.nbi.model.CPW_Tunnel;
import com.alcatelsbell.cdcp.nbi.model.CTunnel;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-13
 * Time: 下午2:43
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HWU2000DBFMigrator extends AbstractDBFLoader {

	public HWU2000DBFMigrator(String fileUrl, String emsdn) {
		this.fileUrl = fileUrl;
		this.emsdn = emsdn;
		MigrateThread.thread().initLog("HWPTN_"+emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

	}

	private static FileLogger logger = new FileLogger("U2000-PTN-Device.log");

	public HWU2000DBFMigrator(Serializable object, String emsdn) {
		this.emsdn = emsdn;
		this.resultObject = object;
		MigrateThread.thread().initLog(logger);
	}

	public void doExecute() throws Exception {
		// super.execute();
		// URL resource = HWU2000DBFLoader.class.getClassLoader().getResource("jndi.properties");
		// System.out.println(resource);

		checkEMS(emsdn, "华为");

		logAction(emsdn + " migrateManagedElement", "同步网元", 1);
		migrateManagedElement();

		logAction("migrateEquipmentHolder", "同步槽道", 5);
		migrateEquipmentHolder();

		logAction("migrateEquipment", "同步板卡", 10);
		migrateEquipment();
		logAction("migratePTP", "同步端口", 20);
		migratePTP();
		logAction("migrateSection", "同步段", 25);
		migrateSection();

		if (migrateLogical &&
				!isTableHasData(FlowDomainFragment.class) && !isTableHasData(CrossConnect.class)) {
			migrateLogical = false;
			getLogger().info("migratelogical = false ,becase fdfr and cc is null");
		}

		if (migrateLogical) {

			logAction("migrateCTP", "同步CTP", 30);
			migrateCTP();

			logAction("migrateMPCTP", "同步MPCTP", 35);
			migrateMPCTP();

			logAction("migrateCC", "同步交叉", 30);
			migrateCC();

			logAction("migrateFTPPTP", "同步逻辑端口", 35);
			migrateFTPPTP();

			logAction("migrateFlowDomainFragment", "同步业务", 40);
			migrateFlowDomainFragment();

			logAction("migrateRoute", "同步路由", 70);
			migrateIPRoute();

			logAction("migrateSubnetwork", "同步子网", 80);
			migrateSubnetwork();

			logAction("migrateProtectGroup", "同步保护组", 85);
			migrateProtectGroup();
			// checkEquipmentHolders(sd);
			// checkPTP(sd);
			// MigrateUtil.checkRoute(sd);
			logAction("migrateProtectingPWTunnel", "同步保护组", 95);
			migrateProtectingPWTunnel();
			getLogger().info("release");
		}

		// ////////////////////////////////////////
		sd.release();
		// jpaInsertHelper.finishAndRelease();

	}

	private static String formatPtpdn(String fullname) {
		String ptpInfo = null;
		int i = -1;
		String prefix = null;
		if (fullname.contains("/rack=")) {
			i = fullname.indexOf("/rack=");
			ptpInfo = fullname.substring(i);

		}

		if (i < 0) {
			if (!fullname.isEmpty())
				System.out.println("unable to format ptp : " + fullname);
			return fullname;
		}
		prefix = fullname.substring(0, i);

		String suffix = MigrateUtil.extractLocationInfo(ptpInfo, new String[] { "rack", "shelf", "slot", "sub_slot", "port" });
		return (prefix + suffix);
	}

	private String formatPtpdnOld(String fullname) {
		String ptpInfo = null;
		int i = -1;
		String prefix = null;
		if (fullname.contains("PTP:")) {
			i = fullname.indexOf("PTP:");
			ptpInfo = fullname.substring(i + 4);

		} else if (fullname.contains("FTP:")) {
			i = fullname.indexOf("FTP:");
			ptpInfo = fullname.substring(i + 4);
		}

		if (i < 0) {
			if (!fullname.isEmpty())
				getLogger().error("unable to format ptp : " + fullname);
			return fullname;
		}
		prefix = fullname.substring(0, i);

		String suffix = MigrateUtil.extractLocationInfo(ptpInfo, new String[] { "rack", "shelf", "slot", "sub_slot", "port" });
		return (prefix + suffix);
	}

	@Override
	public CPTP transPTP(PTP ptp) {
		CPTP cptp = new CPTP();
		String dn = ptp.getDn();
		cptp.setDn(dn);
		if (dn.contains("slot")) {
			String slot = "";
			if (dn.contains("/domain")) {
				slot = dn.substring(dn.indexOf("/rack"), dn.indexOf("/domain"));
			} else if (dn.contains("/type")) {
				slot = dn.substring(dn.indexOf("/rack"), dn.indexOf("/type"));
			}
			String me = dn.substring(0, dn.lastIndexOf("@"));
			String carddn = me + "@EquipmentHolder:" + slot + "@Equipment:1";
			if (slot.toLowerCase().contains("slot")) {
				cptp.setParentDn(carddn);
				cptp.setCardid(DatabaseUtil.getSID(CEquipment.class, carddn));
			}
		}
		if (cptp.getParentDn() == null || cptp.getParentDn().isEmpty()) {
			cptp.setParentDn(ptp.getParentDn());
		}

		if (dn.contains("port=")) {
			if (dn.contains("cli_name")) {
				cptp.setNo(dn.substring(dn.lastIndexOf("port=") + 5, dn.indexOf("/cli_name")));
			} else {
				cptp.setNo(dn.substring(dn.lastIndexOf("port=") + 5));
			}
		}

		cptp.setCollectTimepoint(ptp.getCreateDate());
		cptp.setEdgePoint(ptp.isEdgePoint());
		// cptp.setType(ptp.getType());
		cptp.setConnectionState(ptp.getConnectionState());
		cptp.setTpMappingMode(ptp.getTpMappingMode());
		cptp.setDirection(DicUtil.getPtpDirection(ptp.getDirection()));
		cptp.setTransmissionParams(ptp.getTransmissionParams());
		// cptp.setRate(ptp.getRate());
		cptp.setLayerRates(ptp.getRate());
		cptp.setTpProtectionAssociation(ptp.getTpProtectionAssociation());
		// cptp.setParentDn(ptp.getParentDn());
		cptp.setEmsName(ptp.getEmsName());
		cptp.setUserLabel(ptp.getUserLabel());
		cptp.setNativeEMSName(ptp.getNativeEMSName());
		cptp.setOwner(ptp.getOwner());
		cptp.setAdditionalInfo(ptp.getAdditionalInfo());

		// String temp = cptp.getDn();
		// if (temp.startsWith("EMS:"))
		// temp = temp.substring(4);
		// if (temp.contains("@PTP"))
		// temp = temp.substring(0,temp.indexOf("@PTP"));
		// else if (temp.contains("@FTP"))
		// temp = temp.substring(0,temp.indexOf("@FTP"));
		// temp = temp.replaceAll("ManagedElement:","");
		//
		// if (temp.contains("@"))
		// temp = temp.substring(0,temp.lastIndexOf("@"));
		cptp.setDeviceDn(ptp.getParentDn());
		// cptp.setParentDn(temp);

		if (cptp.getDn().contains("type=tunnelif"))
			cptp.setParentDn(cptp.getDeviceDn());

		Map<String, String> map = MigrateUtil.transMapValue(ptp.getTransmissionParams());
		cptp.setPortMode(map.get("PortMode"));
		cptp.setPortRate(map.get("PortRate"));
		cptp.setWorkingMode(map.get("WorkingMode"));
		cptp.setMacAddress(map.get("MACAddress"));
		cptp.setIpAddress(map.get("IPAddress"));
		cptp.setIpMask(map.get("IPMask"));
		cptp.setEoType(DicUtil.getEOType(cptp.getLayerRates()));
		cptp.setSpeed(DicUtil.getSpeed(cptp.getLayerRates()));
		cptp.setType(DicUtil.getPtpType(dn, cptp.getLayerRates()));
		return cptp; // To change body of created methods use File | Settings | File Templates.
	}

	// private void migrateProtectGroup() throws Exception {
	// executeDelete("delete from CProtectionGroup c where c.emsName = '" + emsdn + "'", CProtectionGroup.class);
	// executeDelete("delete from CProtectionGroupTunnel c where c.emsName = '" + emsdn + "'", CProtectionGroupTunnel.class);
	// DataInserter di = new DataInserter(emsid);
	// List<R_TrafficTrunk_CC_Section> rtcs = sd.queryAll(R_TrafficTrunk_CC_Section.class);
	// List<ProtectionGroup> pgs = sd.queryAll(ProtectionGroup.class);
	// HashMap<String, R_TrafficTrunk_CC_Section> ccMap = new HashMap<String, R_TrafficTrunk_CC_Section>();
	// List<CProtectionGroup> cpps = new ArrayList<CProtectionGroup>();
	// for (int i = 0; i < rtcs.size(); i++) {
	// R_TrafficTrunk_CC_Section r_trafficTrunk_cc_section = rtcs.get(i);
	// if (r_trafficTrunk_cc_section.getType().equals("CC"))
	// ccMap.put(r_trafficTrunk_cc_section.getCcOrSectionDn(), r_trafficTrunk_cc_section);
	// }
	//
	// for (int i = 0; i < pgs.size(); i++) {
	// ProtectionGroup protectionGroup = pgs.get(i);
	// String protectedList = protectionGroup.getProtectedList();
	// String protectedCC = protectedList;
	// if (protectedList.lastIndexOf("EMS:" + emsdn) > 1)
	// protectedCC = protectedList.substring(0, protectedList.lastIndexOf("EMS:" + emsdn));
	//
	// if (protectedCC.endsWith("||"))
	// protectedCC = protectedCC.substring(0, protectedCC.length() - 2);
	// String protectingList = protectionGroup.getProtectingList();
	// String protectingCC = protectingList;
	// if (protectingList.lastIndexOf("EMS:" + emsdn) > 1)
	// protectingCC = protectingList.substring(0, protectingList.lastIndexOf("EMS:" + emsdn));
	// if (protectingCC.endsWith("||"))
	// protectingCC = protectingCC.substring(0, protectingCC.length() - 2);
	//
	// R_TrafficTrunk_CC_Section protectedR = null;
	// R_TrafficTrunk_CC_Section protectingR = null;
	// try {
	// // protectedCC = MigrateUtil.simpleDN2FullDn(protectedCC, new String[]{"EMS", "ManagedElement", "IPCrossConnection"});
	// protectedR = ccMap.get(protectedCC);
	// // protectingCC = MigrateUtil.simpleDN2FullDn(protectingCC, new String[]{"EMS", "ManagedElement", "IPCrossConnection"});
	// protectingR = ccMap.get(protectingCC);
	// } catch (Exception e) {
	// getLogger().error(e, e);
	// continue;
	// }
	//
	// if (protectedR == null)
	// System.err.println("faild to find route : " + protectedCC);
	// if (protectingR == null)
	// System.err.println("faild to find route : " + protectingCC);
	//
	// if (protectedR != null && protectingR != null) {
	// CProtectionGroup pg = new CProtectionGroup();
	// pg.setDn(protectedR.getTrafficTrunDn() + "<>" + protectingR.getTrafficTrunDn());
	// pg.setSid(DatabaseUtil.nextSID(pg));
	// pg.setProtectedList(protectedR.getTrafficTrunDn());
	// pg.setProtectingList(protectingR.getTrafficTrunDn());
	// pg.setEmsName(emsdn);
	// pg.setProtectionGroupType(protectionGroup.getProtectionGroupType());
	// pg.setPgpParameters(protectionGroup.getPgpParameters());
	// pg.setRate(protectionGroup.getRate());
	// pg.setReversionMode(protectionGroup.getReversionMode());
	// cpps.add(pg);
	// }
	//
	// }
	//
	// removeDuplicateDN(cpps);
	// di.insert(cpps);
	//
	// for (int i = 0; i < cpps.size(); i++) {
	// CProtectionGroup cProtectionGroup = cpps.get(i);
	// String protectingList = cProtectionGroup.getProtectingList();
	// String protectedList = cProtectionGroup.getProtectedList();
	// String[] protectings = protectingList.split("\\|\\|");
	// String[] protecteds = protectedList.split("\\|\\|");
	// if (protectings != null && protectings.length > 0) {
	// for (int j = 0; j < protectings.length; j++) {
	// String protecting = protectings[j];
	// CProtectionGroupTunnel cProtectionGroupTunnel = new CProtectionGroupTunnel();
	// cProtectionGroupTunnel.setDn(cProtectionGroup.getDn() + "<>" + protecting);
	// // cProtectionGroupTunnel.setSid(DatabaseUtil.nextSID(cProtectionGroupTunnel));
	// cProtectionGroupTunnel.setProtectGroupId(cProtectionGroup.getSid());
	// cProtectionGroupTunnel.setProtectGroupDn(cProtectionGroup.getDn());
	// cProtectionGroupTunnel.setTunnelId(DatabaseUtil.getSID(CTunnel.class, protecting));
	// cProtectionGroupTunnel.setTunnelDn(protecting);
	// cProtectionGroupTunnel.setEmsName(emsdn);
	// cProtectionGroupTunnel.setStatus("PROTECTING");
	// if (!DatabaseUtil.isSIDExisted(CProtectionGroupTunnel.class, cProtectionGroupTunnel.getDn()))
	// di.insert(cProtectionGroupTunnel);
	// }
	// }
	//
	// if (protecteds != null && protecteds.length > 0) {
	// for (int j = 0; j < protecteds.length; j++) {
	// String protectedd = protecteds[j];
	// CProtectionGroupTunnel cProtectionGroupTunnel = new CProtectionGroupTunnel();
	// cProtectionGroupTunnel.setDn(cProtectionGroup.getDn() + "<>" + protectedd);
	// // cProtectionGroupTunnel.setSid(DatabaseUtil.nextSID(cProtectionGroupTunnel));
	// cProtectionGroupTunnel.setProtectGroupId(cProtectionGroup.getSid());
	// cProtectionGroupTunnel.setProtectGroupDn(cProtectionGroup.getDn());
	// cProtectionGroupTunnel.setTunnelId(DatabaseUtil.getSID(CTunnel.class, protectedd));
	// cProtectionGroupTunnel.setTunnelDn(protectedd);
	// cProtectionGroupTunnel.setStatus("PROTECTED");
	// cProtectionGroupTunnel.setEmsName(emsdn);
	// if (!DatabaseUtil.isSIDExisted(CProtectionGroupTunnel.class, cProtectionGroupTunnel.getDn()))
	// di.insert(cProtectionGroupTunnel);
	// }
	// }
	// }
	//
	// di.end();
	// }


	private void migrateFlowDomainFragment() throws Exception {
		executeDelete("delete from CPWE3 c where c.emsName = '" + emsdn + "'", CPWE3.class);
		executeDelete("delete from CPW c where c.emsName = '" + emsdn + "'", CPW.class);
		executeDelete("delete from CPWE3_PW c where c.emsName = '" + emsdn + "'", CPWE3_PW.class);
		executeDelete("delete from CTunnel c where c.emsName = '" + emsdn + "'", CTunnel.class);
		executeDelete("delete from CPW_Tunnel c where c.emsName = '" + emsdn + "'", CPW_Tunnel.class);
		// executeDelete("delete  from CTunnel_Section c where c.emsName = '" + emsdn + "'", CTunnel_Section.class);
		// executeDelete("delete from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
		DataInserter di = new DataInserter(emsid);
		List<FlowDomainFragment> flowDomainFragments = sd.queryAll(FlowDomainFragment.class);
		List<PWTrail> pwTrails = sd.queryAll(PWTrail.class);
		List<TrafficTrunk> trafficTrunks = sd.queryAll(TrafficTrunk.class);
		HashMap<String, PWTrail> pwtrailMap = new HashMap<String, PWTrail>();
		HashMap<String, TrafficTrunk> tunnelMap = new HashMap<String, TrafficTrunk>();
		HashMap<String, TrafficTrunk> pwMap = new HashMap<String, TrafficTrunk>();
		for (TrafficTrunk trafficTrunk : trafficTrunks) {
			String rate = trafficTrunk.getRate();
			String dn = trafficTrunk.getDn();
			if (rate.equals("8010")) {
				pwMap.put(dn, trafficTrunk);
			} else if (rate.equals("8011")) {
				tunnelMap.put(dn, trafficTrunk);
			}
		}
		for (PWTrail pw : pwTrails) {
			pwtrailMap.put(pw.getDn(), pw);
		}

		// ////////////////////////////// 插入Tunnel
		getLogger().info("tunnel size = " + tunnelMap.size());
		Collection<TrafficTrunk> tunnels = tunnelMap.values();
		for (Iterator<TrafficTrunk> iterator = tunnels.iterator(); iterator.hasNext();) {
			TrafficTrunk tunnel = iterator.next();
			CTunnel cTunnel = transTunnel(tunnel);
			cTunnel.setSid(DatabaseUtil.nextSID(cTunnel));
			di.insert(cTunnel);
		}
		getLogger().info("pw size = " + pwMap.size());
		getLogger().info("pwe3 size = " + flowDomainFragments.size());
		Collection<TrafficTrunk> pws = pwMap.values();
		List<CPWE3> cpwe3List = new ArrayList();
		List<CPW> cpwList = new ArrayList();
		List<CPWE3_PW> cpwe3pwList = new ArrayList();
		// PWE3-PW
		int idx = 0;
		if (flowDomainFragments != null && flowDomainFragments.size() > 0) {
			for (FlowDomainFragment fdf : flowDomainFragments) {
				if (fdf.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@FlowdomainFragment:VPLS=6") || fdf.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@FlowdomainFragment:PWE3TRAIL=9927"))
					System.out.println();
				if (idx++ % 1000 == 0)
					getLogger().info("flowDomainFragments:" + idx);
				try {
					CPWE3 cpwe3 = transFDF(fdf);
					cpwe3List.add(cpwe3);

					String pwe3dn = fdf.getDn();
					String parentDn = fdf.getParentDn();

					List<CPW> currentCpws = new ArrayList<CPW>();

					//只处理点到点的
					if (fdf.getFdfrType().equals(FDFRT_POINT_TO_POINT) ) {
						String aptp = fdf.getaPtp();
						String zptp = fdf.getzPtp();

						if (aptp == null) aptp = "";
						if (zptp == null ) zptp = "";

						//点对点的时候需要判断两端是否为空，如果是多点的话就无所谓
						if (fdf.getFdfrType().equals(FDFRT_POINT_TO_POINT)) {
							if (aptp == null || aptp.trim().isEmpty() || zptp == null || zptp.trim().isEmpty()) {
								getLogger().error("PW-APS PWE3 end is null : pwe3=" + pwe3dn);
								continue;
							}
							if (aptp.indexOf("port") < 0 || zptp.indexOf("port") < 0) {
								getLogger().error("PW-APS PWE3 end is error : pwe3=" + pwe3dn);
								continue;
							}
						}


						if (aptp.contains(Constant.listSplit) || zptp.contains(Constant.listSplit)) {
							// PW-APS
							if (parentDn == null || parentDn.trim().isEmpty()) {
								getLogger().error("PW-APS PWE3 parentDn is null : pwe3=" + pwe3dn);
								continue;
							}
							String[] pwDns = parentDn.split(Constant.listSplitReg);
							List<TrafficTrunk> cpws = new ArrayList<TrafficTrunk>();
							for (String pwDn : pwDns) {
								TrafficTrunk pw = pwMap.get(pwDn);
								if (pw == null) {
									getLogger().error("PW not found : pw=" + pwDn);
									continue;
								}
								cpws.add(pw);
							}

							for (TrafficTrunk pw : cpws) {
								String pwdn = pw.getDn();
								if (pwdn.equals("EMS:ZSH-U2000-1-PTN@Flowdomain:1@TrafficTrunk:PWTRAIL=23"))
									System.out.println();
								String pwane = pw.getaNE();
								String pwzne = pw.getzNE();
								if (pwane == null || pwane.trim().isEmpty() || pwzne == null || pwzne.trim().isEmpty()) {
									getLogger().error("PW-APS PW end is null : pw=" + pwdn);
									continue;
								}
								PWTrail pwtrail = pwtrailMap.get(pwdn);
								// String newPWE3dn = pwe3dn + "<>" + pwdn;
								// CPWE3 cpwe3 = null;
								CPW cpw = null;
								if (aptp.contains(Constant.listSplit)) {


									String[]  ptps = fdf.getaPtp().split(Constant.listSplitReg);
									if (fdf.getzPtp() != null && fdf.getzPtp().length() > 0) {
										String[] zptps = fdf.getzPtp().split(Constant.listSplitReg);

										String[] aptps = ptps;

										ptps = new String[aptps.length + zptps.length];
										System.arraycopy(aptps,0,ptps,0,aptps.length);
										System.arraycopy(zptps,0,ptps,aptps.length,zptps.length);

									}

									if (fdf.getFdfrType().equals("FDFRT_MULTIPOINT")) {
										String _aptp = pw.getaPtp();
										String _zptp = pw.getzPtp();

										String aPWE3ptp = findPtpWithNEDn(ptps, DNUtil.extractNEDn(_aptp));
										if (aPWE3ptp != null) _aptp = aPWE3ptp;
										String zpwe3Ptp = findPtpWithNEDn(ptps, DNUtil.extractNEDn(_zptp));
										if (zpwe3Ptp != null) _zptp = zpwe3Ptp;
										cpw =  transCPW(pw, _aptp, _zptp, pwtrail, cpwe3);
									} else {        //点到点

										String aptp1 = getPtp1(aptp);
										String aptp2 = getPtp2(aptp);

										String ane1 = ptp2ne(aptp1);
										String ane2 = ptp2ne(aptp2);
										String zne = ptp2ne(zptp);
										if ((ane1.equals(pwane) && zne.equals(pwzne)) || (ane1.equals(pwzne) && zne.equals(pwane))) {
											// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp1, zptp);
											cpw = transCPW(ane1, zne, pwane, pwzne, pw, aptp1, zptp, pwtrail, cpwe3);
										} else if ((ane2.equals(pwane) && zne.equals(pwzne)) || (ane2.equals(pwzne) && zne.equals(pwane))) {
											// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp2, zptp);
											cpw = transCPW(ane2, zne, pwane, pwzne, pw, aptp2, zptp, pwtrail, cpwe3);
										} else if ((ane1.equals(pwane) && ane2.equals(pwzne)) || (ane1.equals(pwzne) && ane2.equals(pwane))) {
											// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp1, aptp2);
											cpw = transCPW(ane1, ane2, pwane, pwzne, pw, aptp1, aptp2, pwtrail, cpwe3);
										} else {
											getLogger().error("PW do not match PWE3: pw=" + pwdn);
											continue;
										}
									}
								} else {
									String zptp1 = getPtp1(zptp);
									String zptp2 = getPtp2(zptp);

									String zne1 = ptp2ne(zptp1);
									String zne2 = ptp2ne(zptp2);
									String ane = ptp2ne(aptp);
									if ((ane.equals(pwane) && zne1.equals(pwzne)) || (ane.equals(pwzne) && zne1.equals(pwane))) {
										// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp, zptp1);
										cpw = transCPW(ane, zne1, pwane, pwzne, pw, aptp, zptp1, pwtrail, cpwe3);
									} else if ((ane.equals(pwane) && zne2.equals(pwzne)) || (ane.equals(pwzne) && zne2.equals(pwane))) {
										// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp, zptp2);
										cpw = transCPW(ane, zne2, pwane, pwzne, pw, aptp, zptp2, pwtrail, cpwe3);
									} else if ((zne1.equals(pwane) && zne2.equals(pwzne)) || (zne1.equals(pwzne) && zne2.equals(pwane))) {
										// cpwe3 = transCPWE3(fdf, newPWE3dn, zptp1, zptp2);
										cpw = transCPW(zne1, zne2, pwane, pwzne, pw, zptp1, zptp2, pwtrail, cpwe3);
									} else {
										getLogger().error("PW do not match PWE3: pw=" + pwdn);
										continue;
									}
								}
								if (cpw != null) {
									cpw.setSid(DatabaseUtil.nextSID(cpw));
									cpwList.add(cpw);

									currentCpws.add(cpw);

									CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
									cpwe3pwList.add(cpwe3_pw);
								}
							}
						} else {
							// POINT_TO_POINT
							// CPWE3 cpwe3 = transCPWE3(fdf, pwe3dn, aptp, zptp);
							// cpwe3.setSid(DatabaseUtil.nextSID(cpwe3));
							// di.insert(cpwe3);
							//
							if (parentDn == null || parentDn.trim().isEmpty()) {
								//如果是同网元内的CPWE3，虚拟出一个PW来。
								if (DNUtil.extractNEDn(cpwe3.getAptp()).equals(DNUtil.extractNEDn(cpwe3.getZptp()))) {
									TrafficTrunk tpw = new TrafficTrunk();
									tpw.setDn(cpwe3.getDn()+"_PW");
									tpw.setaEnd(cpwe3.getAend());
									tpw.setzEnd(cpwe3.getZend());
									tpw.setaPtp(cpwe3.getAptp());
									tpw.setzPtp(cpwe3.getZptp());
									tpw.setNativeEMSName(cpwe3.getNativeEMSName());
									tpw.setaNE(DNUtil.extractNEDn(cpwe3.getAptp()));
									tpw.setzNE(DNUtil.extractNEDn(cpwe3.getZptp()));
									tpw.setUserLabel(cpwe3.getNativeEMSName());
									tpw.setDirection("CD_BI");
									tpw.setEmsName(emsdn);
									pwMap.put(tpw.getDn(),tpw);
									cpwe3.setParentDn(tpw.getDn());
									parentDn = tpw.getDn();

								}
							}

							if (parentDn == null || parentDn.trim().isEmpty()) {
								getLogger().error("PWE3 parentDn is null : pwe3=" + fdf.getDn());
								continue;
							}

							if (parentDn.indexOf(Constant.listSplit) < 0) {
								TrafficTrunk pw = pwMap.get(parentDn);
								if (pw == null) {
									getLogger().error("PW not found : pw=" + parentDn);
									continue;
								}

								String apwe3ne = ptp2ne(cpwe3.getAptp());
								String zpwe3ne = ptp2ne(cpwe3.getZptp());
								if (pw.getaNE().equals(apwe3ne) && pw.getzNE().equals(zpwe3ne)) {
									aptp = cpwe3.getAptp();
									zptp = cpwe3.getZptp();
								} else if (pw.getzNE().equals(apwe3ne) && pw.getaNE().equals(zpwe3ne)) {
									aptp = cpwe3.getZptp();
									zptp = cpwe3.getAptp();
								} else {

									PWTrail pwTrail = pwtrailMap.get(pw.getDn());

									if (pwTrail != null) {
										// pwtrail 两端可能有多个网元
										if (pwTrail.getaNE().contains(apwe3ne) && pwTrail.getzNE().contains(zpwe3ne)) {
											aptp = cpwe3.getAptp();
											zptp = cpwe3.getZptp();
										}  else if (pwTrail.getzNE().contains(apwe3ne) && pwTrail.getaNE().contains(zpwe3ne)) {
											aptp = cpwe3.getZptp();
											zptp = cpwe3.getAptp();
										}
									}

									if (aptp.isEmpty() || zptp.isEmpty()) {
										getLogger().error("PW do not match PWE3: pwe3=" + cpwe3.getDn());
										continue;
									}
								}

								CPW cpw = transCPW(pw, aptp, zptp, pwtrailMap.get(pw.getDn()), cpwe3);
								if (pw.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@TrafficTrunk:PWTRAIL=74"))
									getLogger().info("74p2p: cpwdn = "+cpw.getDn());
								cpw.setSid(DatabaseUtil.nextSID(cpw));
								cpwList.add(cpw);
								currentCpws.add(cpw);

								CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
								cpwe3pwList.add(cpwe3_pw);
							} else {
								String[] pwDns = parentDn.split(Constant.listSplitReg);
								for (String pwDn : pwDns) {
									TrafficTrunk pw = pwMap.get(pwDn);
									if (pw == null) {
										getLogger().error("PW not found : pw=" + pw);
										continue;
									}

									String apwe3ne = ptp2ne(cpwe3.getAptp());
									String zpwe3ne = ptp2ne(cpwe3.getZptp());
									if (pw.getaNE().equals(apwe3ne) && pw.getzNE().equals(zpwe3ne)) {
										aptp = cpwe3.getAptp();
										zptp = cpwe3.getZptp();
									} else if (pw.getzNE().equals(apwe3ne) && pw.getaNE().equals(zpwe3ne)) {
										aptp = cpwe3.getZptp();
										zptp = cpwe3.getAptp();
									} else {
										getLogger().error("PW do not match PWE3: pwe3=" + cpwe3.getDn());
										continue;
									}

									CPW cpw = transCPW(pw, aptp, zptp, pwtrailMap.get(pw.getDn()), cpwe3);
									cpw.setSid(DatabaseUtil.nextSID(cpw));
									cpwList.add(cpw);
									currentCpws.add(cpw);
									CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
									cpwe3pwList.add(cpwe3_pw);
								}
							}
						}




					} else if (fdf.getFdfrType().equals("FDFRT_MULTIPOINT")){       // add by ronnie 150904

						String aptp = fdf.getaPtp();
						String zptp = fdf.getzPtp();

						if (aptp == null) aptp = "";
						if (zptp == null ) zptp = "";

						if (parentDn == null) {
							getLogger().info("parentdn is null : fdf = "+fdf.getDn());
							continue;
						}

						String ptps = aptp.contains(Constant.listSplit) ? aptp : zptp;

						String[] ptpDns = ptps.split(Constant.listSplitReg);



						String[] pwDns = parentDn.split(Constant.listSplitReg);

						List<TrafficTrunk> cpws = new ArrayList<TrafficTrunk>();
						HashMap<String,TrafficTrunk> neRelations = new HashMap<String,TrafficTrunk>();
						for (String pwDn : pwDns) {
							HashSet<String> apwNEDns = new HashSet<String>();
							HashSet<String> zpwNEDns = new HashSet<String>();
							TrafficTrunk pw = pwMap.get(pwDn);
							if (pw == null) {
								getLogger().error("PW not found : pw=" + pwDn);
								continue;
							}
							cpws.add(pw);
							if (pw.getaNE() != null) {
								String[] dns = pw.getaNE().split(Constant.listSplitReg);
								for (String dn : dns) {
									apwNEDns.add(dn);
								}
							}

							if (pw.getzNE() != null) {
								String[] dns = pw.getzNE().split(Constant.listSplitReg);
								for (String dn : dns) {
									zpwNEDns.add(dn);
								}
							}

							for (String apwNEDn : apwNEDns) {
								for (String zpwNEDn : zpwNEDns) {
									neRelations.put(apwNEDn + "<>" + zpwNEDn, pw);
							//		neRelations.put(zpwNEDn+"<>"+apwNEDn,pw);
								}
							}
						}


						for (String ptpDn : ptpDns) {
							for (String ptpDn2 : ptpDns) {
								String ne1 = DNUtil.extractNEDn(ptpDn);
								String ne2 = DNUtil.extractNEDn(ptpDn2);
								if (!ne1.equals(ne2)) {
									TrafficTrunk pw = neRelations.get(ne1 + "<>" + ne2);
									if (pw != null) {

										CPW cpw = transCPW(pw,ptpDn,ptpDn2,pwtrailMap.get(pw.getDn()),cpwe3);
										if (pw.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@TrafficTrunk:PWTRAIL=74"))
											getLogger().info("74: cpwdn = "+cpw.getDn());
										cpw.setDn(cpw.getDn());
										cpw.setSid(DatabaseUtil.nextSID(cpw));
										cpwList.add(cpw);
										currentCpws.add(cpw);
										CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
										cpwe3pwList.add(cpwe3_pw);
									}
								 }
							}
						}





					} else {
						getLogger().error("ERROR PWE3TYPE : pwe3=" + fdf.getDn());
					}

					// 如果伪线Z端为空
					if (cpwe3.getZptp() == null || cpwe3.getZptp().isEmpty()) {
						StringBuffer zp = new StringBuffer();
						for (int i = 0; i < currentCpws.size(); i++) {
							CPW currentCpw = currentCpws.get(i);
							String _zptp = currentCpw.getZptp();
							zp.append(_zptp);
							if (i < currentCpws.size()-1)
								zp.append("||");
						}

						cpwe3.setZptp(zp.toString());
					}


				} catch (Exception e) {
					getLogger().error("ERROR PWE3 : pwe3=" + fdf.getDn(), e);
				}
			}
		}

		for (CPW cpw : cpwList) {
			cpw.setEmsName(emsdn);
		}

		removeDuplicateDN(cpwe3List);
		removeDuplicateDN(cpwList);
		removeDuplicateDN(cpwe3pwList);

		di.insert(cpwe3List);
		di.insert(cpwList);
		di.insert(cpwe3pwList);


		List<CPW_Tunnel> cpw_tunnelList = new ArrayList<CPW_Tunnel>();
		// PW-TUNNEL
		int index = 0;
		if (cpwList != null && cpwList.size() > 0) {
			for (CPW pw : cpwList) {
				try {
					if (index++ % 1000 == 0)
						getLogger().info("pws:" + index);

					// CPW cpw = transPW(pw);
					// if (!DatabaseUtil.isSIDExisted(CPW.class, cpw.getDn())) {
					// di.insert(cpw);
					// }
					if (!DatabaseUtil.isSIDExisted(CPW.class, pw.getDn())) {
						continue;
					}

					String parentDn = pw.getParentDn();
					if (parentDn == null || parentDn.isEmpty()) {
						PWTrail pwTrail = pwtrailMap.get(pw.getDn().contains("__")? pw.getDn().substring(0,pw.getDn().indexOf("__")): pw.getDn());

						if (pwTrail != null)
							parentDn = pwTrail.getParentDn();
						else
							getLogger().error("---pwTrail is null : " + pw.getDn()+" pwtrail size = "+pwtrailMap.size());
					}

					if (parentDn == null || parentDn.trim().isEmpty()) {
						getLogger().error("---PW parentDn is null : " + pw.getDn());
						continue;
					}
					String[] tunnelDns = parentDn.split(Constant.listSplitReg);
					for (String tunnelDn : tunnelDns) {
						TrafficTrunk tunnel = tunnelMap.get(tunnelDn);
						if (tunnel == null) {
							getLogger().error("Tunnel not found : Tunnel=" + tunnelDn);
							continue;
						}

						CPW_Tunnel cpw_tunnel = transCPW_Tunnel(pw.getEmsName(), pw.getDn(), tunnel.getDn());
						cpw_tunnelList.add(cpw_tunnel);
					}
				} catch (Exception e) {
					getLogger().error("ERROR PW : pw=" + pw.getDn(), e);
				}
			}
		}

		removeDuplicateDN(cpw_tunnelList);
		di.insert(cpw_tunnelList);

		flowDomainFragments.clear();
		pws.clear();
		trafficTrunks.clear();
		pwtrailMap.clear();
		tunnelMap.clear();

		di.end();
		getLogger().info("migrate migrateFlowDomainFragment success");

		// String[] pwTrailDns = parentDn.split(Constant.listSplitReg);
		// for (String pwTrailDn : pwTrailDns) {
		// if (pwTrailDn == null || pwTrailDn.trim().isEmpty()) {
		// getLogger().error("Faild to find parentPWDn : cpwe3=" + cpwe3.getDn());
		// continue;
		// }
		// TrafficTrunk pwTrail = pwtrailMap.get(pwTrailDn);
		// if (pwTrail == null) {
		// getLogger().error("PWTrail not found : " + pwTrailDn);
		// continue;
		// }
		// // HZ-U2000-2-P@1@TUNNELTRAIL=403208
		// String pwTrailParentDn = pwTrail.getParentDn();
		//
		// String[] tunnelDns = pwTrailParentDn.split(Constant.listSplitReg);
		// // List<String> tunnelDns = MigrateUtil.simpleDN2FullDns(pwTrailParentDn, new String[]{"EMS", "Flowdomain", "TrafficTrunk"});
		// if (tunnelDns == null || tunnelDns.length == 0) {
		// getLogger().error("Faild to find parentTunnelDn : pwTrail=" + pwTrail.getDn());
		// continue;
		// }
		// for (String tunnelDn : tunnelDns) {
		// if (tunnelDn == null || tunnelDn.trim().isEmpty()) {
		// getLogger().error("Faild to find parentTunnelDn : pwTrail=" + pwTrail.getDn());
		// continue;
		// }
		// TrafficTrunk tunnelTrail = tunnelsMap.get(tunnelDn);
		// if (tunnelTrail == null) {
		// getLogger().error("TunnelTrail not found : " + tunnelDn);
		// continue;
		// }
		//
		// // cpwe3.setParentDn(tunnelDn);
		// // cpwe3.setTunnelDn(tunnelDn);
		// // cpwe3.setTunnelId(DatabaseUtil.getSID(CTunnel.class, tunnelDn));
		//
		// CPWE3_TUNNEL cpwe3_tunnel = new CPWE3_Tunnel();
		// // cpwe3_tunnel.setDn(SysUtil.nextDN());
		// cpwe3_tunnel.setDn(cpwe3.getDn() + "<>" + tunnelDn);
		// cpwe3_tunnel.setCollectTimepoint(cpwe3.getCollectTimepoint());
		// cpwe3_tunnel.setPwe3Dn(cpwe3.getDn());
		// cpwe3_tunnel.setPwe3Id(DatabaseUtil.getSID(CPWE3.class, cpwe3.getDn()));
		// cpwe3_tunnel.setTunnelDn(tunnelDn);
		// cpwe3_tunnel.setTunnelId(DatabaseUtil.getSID(CTunnel.class, tunnelDn));
		// cpwe3_tunnel.setEmsName(cpwe3.getEmsName());
		// // 一条PWE3关联两条PW，这两条PW关联同一条TUNNEL，会造成DN重复
		// if (!DatabaseUtil.isSIDExisted(CPW_Tunnel.class, cpwe3_tunnel.getDn())) {
		// di.insert(cpwe3_tunnel);
		// }
		// }
		//
		// // di.insert(cpwe3);
		//
		// }
		// }
		// }

	}

	private String findPtpWithNEDn(String[] ptpList,String ne) {
		for (String ptp : ptpList) {
			if (ptp.startsWith(ne+"@"))
				return ptp;
		}
		return null;
	}

	// @Override
	// public CSection transSection(Section section) {
	// CSection cc = super.transSection(section);
	// cc.setAendTp(formatPtpdn(cc.getAendTp()));
	// cc.setZendTp(formatPtpdn(cc.getZendTp()));
	// return cc;
	// }

	@Override
	public CTunnel transTunnel(TrafficTrunk src) {
		CTunnel des = new CTunnel();
		des.setDn(src.getDn());
		des.setCollectTimepoint(src.getCreateDate());
		des.setRerouteAllowed(src.getRerouteAllowed());
		des.setAdministrativeState(src.getAdministrativeState());
		des.setActiveState(src.getActiveState());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		des.setTransmissionParams(src.getTransmissionParams());
		des.setNetworkRouted(src.getNetworkRouted());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());
		String aptp = src.getaPtp();
		String zptp = src.getzPtp();
		des.setAptp(aptp);
		des.setZptp(zptp);
		des.setAptpId(DatabaseUtil.getSID(CPTP.class, des.getAptp()));
		des.setZptpId(DatabaseUtil.getSID(CPTP.class, des.getZptp()));

		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndtrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());
		String aend = des.getAend();
		String zend = des.getZend();
		String aendTrans = des.getAendTrans();
		String zendtrans = des.getZendtrans();
		if (aend.contains("outLabel="))
			des.setAegressLabel(aend.substring(aend.indexOf("outLabel=") + 9));
		if (zend.contains("inLabel="))
			des.setZingressLabel(zend.substring(zend.indexOf("inLabel=") + 8));

		HashMap<String, String> at = MigrateUtil.transMapValue(aendTrans);
		des.setAingressLabel(at.get("RevInLabel"));

		HashMap<String, String> zt = MigrateUtil.transMapValue(zendtrans);
		des.setZegressLabel(zt.get("RevOutLabel"));

		des.setPir("");
		des.setCir("");

		return des;
	}

	@Override
	public CPWE3 transFDF(FlowDomainFragment src) {
		CPWE3 des = new CPWE3();
		des.setDn(src.getDn());
		des.setCollectTimepoint(src.getCreateDate());
		des.setFlexible(src.isFlexible());
		des.setNetworkAccessDomain(src.getNetworkAccessDomain());
		des.setAdministrativeState(src.getAdministrativeState());
		des.setFdfrState(src.getFdfrState());
		des.setMultipointServiceAttrParaList(src.getMultipointServiceAttrParaList());
		des.setMultipointServiceAttrMacList(src.getMultipointServiceAttrMacList());
		des.setMultipointServiceAttrAddInfo(src.getMultipointServiceAttrAddInfo());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		des.setTransmissionParams(src.getTransmissionParams());
		if (des.getTransmissionParams() != null && des.getTransmissionParams().length() > 800)
			des.setTransmissionParams("");
		des.setRate(src.getRate());
		des.setFdfrType(src.getFdfrType());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());

		String aptp = src.getaPtp();
		String zptp = src.getzPtp();
		if (aptp.contains(Constant.listSplit) || zptp.contains(Constant.listSplit)) {
			des.setAptps(aptp);
			des.setZptps(zptp);
		} else {
			des.setAptp(aptp);
			des.setZptp(zptp);
			des.setAptpId(DatabaseUtil.getSID(CPTP.class, des.getAptp()));
			des.setZptpId(DatabaseUtil.getSID(CPTP.class, des.getZptp()));
		}
		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndtrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());

		String aendTrans = des.getAendTrans();
		String zendtrans = des.getZendtrans();
		HashMap<String, String> at = MigrateUtil.transMapValue(aendTrans);
		des.setAvlanId(at.get("IVID"));

		HashMap<String, String> zt = MigrateUtil.transMapValue(zendtrans);
		des.setZvlanId(zt.get("IVID"));

		return des;
	}

	// private CPWE3 transCPWE3(FlowDomainFragment fdfr, String newPWE3dn, String aptp, String zptp) {
	// CPWE3 cpwe3 = transFDF(fdfr);
	// cpwe3.setDn(newPWE3dn);
	// cpwe3.setAptp(aptp);
	// cpwe3.setZptp(zptp);
	// cpwe3.setAptpId(DatabaseUtil.getSID(CPTP.class, cpwe3.getAptp()));
	// cpwe3.setZptpId(DatabaseUtil.getSID(CPTP.class, cpwe3.getZptp()));
	// return cpwe3;
	// }

	private CPW transCPW(String pwe3Ane, String pwe3Zne, String pwAne, String pwZne, TrafficTrunk pw, String aptp, String zptp, PWTrail pwtrail, CPWE3 cpwe3) {
		CPW cpw = null;
		if (pwe3Ane.equals(pwAne) && pwe3Zne.equals(pwZne)) {
			cpw = transCPW(pw, aptp, zptp, pwtrail, cpwe3);
		} else {
			cpw = transCPW(pw, zptp, aptp, pwtrail, cpwe3);
		}
		return cpw;
	}

	private CPW transCPW(TrafficTrunk pw, String aptp, String zptp, PWTrail pwtrail, CPWE3 cpwe3) {
		CPW cpw = transPW(pw);
		cpw.setAptp(aptp);
		cpw.setZptp(zptp);
		cpw.setAptpId(DatabaseUtil.getSID(CPTP.class, cpw.getAptp()));
		cpw.setZptpId(DatabaseUtil.getSID(CPTP.class, cpw.getZptp()));

		cpw.setAvlanId(cpwe3.getAvlanId());
		cpw.setZvlanId(cpwe3.getZvlanId());
		if (pwtrail != null) {
			cpw.setApwid(pwtrail.getApwid());
			cpw.setZpwid(pwtrail.getZpwid());
			cpw.setaWorkingMode(pwtrail.getaWorkingMode());
			cpw.setzWorkingMode(pwtrail.getzWorkingMode());
		}
		return cpw;
	}

	private String getPtp1(String ptp) {
		return ptp.substring(0, ptp.indexOf(Constant.listSplit));
	}

	private String getPtp2(String ptp) {
		return ptp.substring(ptp.indexOf(Constant.listSplit) + 2);
	}


	private HashMap<String,Integer> pwDnMap = new HashMap<String, Integer>();
	public CPW transPW(TrafficTrunk src) {

		CPW des = new CPW();
		String dn = src.getDn();
		if (pwDnMap.get(dn) != null) {
			des.setDn(dn + "__"+ (pwDnMap.get(dn)+1));
			pwDnMap.put(dn,(pwDnMap.get(dn)+1));
		} else {
			des.setDn(dn);
			pwDnMap.put(dn,0);
		}
		des.setCollectTimepoint(src.getCreateDate());
		des.setRerouteAllowed(src.getRerouteAllowed());
		des.setAdministrativeState(src.getAdministrativeState());
		des.setActiveState(src.getActiveState());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		des.setTransmissionParams(src.getTransmissionParams());
		des.setNetworkRouted(src.getNetworkRouted());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());
		// des.setAptp(src.getaPtp());
		// des.setZptp(src.getzPtp());
		// des.setAptpId(DatabaseUtil.getSID(CPTP.class, des.getAptp()));
		// des.setZptpId(DatabaseUtil.getSID(CPTP.class, des.getZptp()));

		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndtrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());

		// des.setApwid(src.getApwid());
		// des.setZpwid(src.getZpwid());
		// des.setaWorkingMode(src.getaWorkingMode());
		// des.setzWorkingMode(src.getzWorkingMode());

		return des;
	}

	public static void main2(String[] args) {
		// String sp = "\\|\\|";
		// System.out.println(sp);
		// String[] split = "abc||".split("\\|\\|");
		printSetMethods("src", TrafficTrunk.class, "des", CTunnel.class);
	}

	public static void main(String[] args) throws Exception {
		String[] split = "asdf".split("\\|\\|");
		String s = "EMS:HZ-U2000-2-P@ManagedElement:917597@PTP:/rack=1/shelf=1/slot=2/domain=ptn/type=physical/port=1||";
		if (s.contains("||"))
			s = s.substring(0, s.indexOf("||"));
		System.out.println(s);
		// DatabaseUtil.getSID(ManagedElement.class,"adf");
		// URL resource = HWU2000DBFLoader.class.getClassLoader().getResource("appserver-spring.xml");
		// System.out.println("resource = " + resource);
		// resource = HWU2000DBFLoader.class.getClassLoader().getResource("META-INF/persistence.xml");
		// System.out.println("resource = " + resource);
		String[] locations = { "appserver-spring.xml" };
		ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
		HWU2000DBFMigrator loader = new HWU2000DBFMigrator("f:/cdcpdb/2015-10-09-170000-NB-U2000-1-P-DayMigration.db", "NB-U2000-1-P");
		loader.execute();
	}

	public static void printSetMethods(String srcName, Class srcCls, String desName, Class desClass) {
		System.out.println(desClass.getSimpleName() + " " + desName + " = new " + desClass.getSimpleName() + "();");
		Field[] declaredFields = srcCls.getDeclaredFields();
		String bigName1 = "Dn";
		System.out.println(desName + ".set" + bigName1 + "(" + srcName + ".get" + bigName1 + "());");
		bigName1 = "CollectTimepoint";
		System.out.println(desName + ".set" + bigName1 + "(" + srcName + ".get" + bigName1 + "());");
		for (int i = 0; i < declaredFields.length; i++) {
			Field declaredField = declaredFields[i];
			String fieldName = declaredField.getName();
			String bigName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			System.out.println(desName + ".set" + bigName + "(" + srcName + ".get" + bigName + "());");
		}
	}

	@Override
	public CFTP_PTP transFTP_PTP(String emsdn, R_FTP_PTP r_ftp_ptp) {
		// R_FTP_PTP r_ftp_ptp = list.get(i);
		CFTP_PTP cftp_ptp = new CFTP_PTP();
		cftp_ptp.setDn(r_ftp_ptp.getDn());
		cftp_ptp.setEmsName(emsdn);
		// cftp_ptp.setFtpDn(formatPtpdn(r_ftp_ptp.getFtpDn()));
		// cftp_ptp.setPtpDn(formatPtpdn(r_ftp_ptp.getPtpDn()));
		cftp_ptp.setFtpDn(r_ftp_ptp.getFtpDn());
		cftp_ptp.setPtpDn(r_ftp_ptp.getPtpDn());
		cftp_ptp.setFtpId(DatabaseUtil.getSID(CPTP.class, cftp_ptp.getFtpDn()));
		cftp_ptp.setPtpId(DatabaseUtil.getSID(CPTP.class, cftp_ptp.getPtpDn()));
		cftp_ptp.setRate(r_ftp_ptp.getRate());
		cftp_ptp.setTransmissionParams(r_ftp_ptp.getTransmissionParams());
		cftp_ptp.setTpMappingMode(r_ftp_ptp.getTpMappingMode());
		return cftp_ptp;
	}

	public CEquipment transEquipment(Equipment equipment) {
		CEquipment cequipment = super.transEquipment(equipment);
		String nativeEMSName = null;
		String objectType = equipment.getInstalledEquipmentObjectType();
		if (objectType != null && objectType.contains("(")) {
			nativeEMSName = objectType.substring(0, objectType.indexOf("("));
		} else {
			nativeEMSName = equipment.getNativeEMSName();
		}
		cequipment.setNativeEMSName(nativeEMSName);
		return cequipment;
	}

}
