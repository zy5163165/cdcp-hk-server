package com.alcatelsbell.cdcp.server.adapters.alu;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.FlowDomainFragment;
import org.asb.mule.probe.framework.entity.IPCrossconnection;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.entity.R_TrafficTrunk_CC_Section;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.entity.TopoNode;
import org.asb.mule.probe.framework.service.Constant;

import com.alcatelsbell.cdcp.nbi.model.CCrossConnect;
import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.cdcp.nbi.model.CEquipment;
import com.alcatelsbell.cdcp.nbi.model.CPTP;
import com.alcatelsbell.cdcp.nbi.model.CPW;
import com.alcatelsbell.cdcp.nbi.model.CPWE3;
import com.alcatelsbell.cdcp.nbi.model.CPWE3_PW;
import com.alcatelsbell.cdcp.nbi.model.CPW_Tunnel;
import com.alcatelsbell.cdcp.nbi.model.CIPRoute;
import com.alcatelsbell.cdcp.nbi.model.CSection;
import com.alcatelsbell.cdcp.nbi.model.CSubnetwork;
import com.alcatelsbell.cdcp.nbi.model.CSubnetworkDevice;
import com.alcatelsbell.cdcp.nbi.model.CTunnel;
import com.alcatelsbell.cdcp.nbi.model.CTunnel_Section;
import com.alcatelsbell.cdcp.server.EMSDataTableEmptyException;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.util.DataInserter;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.DicUtil;
import com.alcatelsbell.cdcp.util.MigrateThread;
import com.alcatelsbell.nms.common.SysUtil;
import org.asb.mule.probe.framework.util.FileLogger;

public class ALUDBFMigrator extends AbstractDBFLoader {

	public ALUDBFMigrator(String fileUrl, String emsdn) {
		this.fileUrl = fileUrl;
		this.emsdn = emsdn;
		MigrateThread.thread().initLog(emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

	}

	private static FileLogger logger = new FileLogger("ALU-Device.log");

	public ALUDBFMigrator(Serializable object, String emsdn) {
		this.emsdn = emsdn;
		this.resultObject = object;
		MigrateThread.thread().initLog(logger);
	}

	public void doExecute() throws Exception {
		// super.execute();
		// URL resource = HWU2000DBFLoader.class.getClassLoader().getResource("jndi.properties");
		// System.out.println(resource);

		checkEMS(emsdn, "阿尔卡特朗讯");

		logAction("migrateManagedElement", "同步网元", 1);
		migrateManagedElement();

		logAction("migrateEquipmentHolder", "同步槽道", 5);
		migrateEquipmentHolder();

		logAction("migrateEquipment", "同步板卡", 10);
		migrateEquipment();

		logAction("migratePTP", "同步端口", 20);
		migratePTP();

		logAction("migrateSection", "同步段", 25);
		migrateSection();
		//
		// logAction("migrateCC","同步交叉",30);
		// migrateCC();
		logAction("migrateFTPPTP", "同步逻辑端口", 35);
		migrateFTPPTP();

		logAction("migrateFlowDomainFragment", "同步业务", 40);
		migrateFlowDomainFragment();

		logAction("migrateRoute", "同步路由", 70);
		migrateIPRoute();

		// logAction("migrateSubnetwork", "同步子网", 80);
		// migrateSubnetwork();

		logAction("migrateProtectGroup", "同步保护组", 85);
		migrateProtectGroup();
		// checkEquipmentHolders(sd);
		// checkPTP(sd);
		// MigrateUtil.checkRoute(sd);
		logAction("migrateProtectingPWTunnel", "同步保护组", 95);
		migrateProtectingPWTunnel();
		getLogger().info("release");

		// ////////////////////////////////////////
		sd.release();
		// jpaInsertHelper.finishAndRelease();

	}

	protected void migrateEquipment() throws Exception {

		executeDelete("delete   from CEquipment c where c.emsName = '" + emsdn + "'", CEquipment.class);
		DataInserter di = new DataInserter(emsid);
		List<Equipment> equipments = sd.queryAll(Equipment.class);
		if (equipments != null && equipments.size() > 0) {
			for (Equipment equipment : equipments) {
				if (equipment.getDn().contains("slot")) {
					CEquipment cEquipment = transEquipment(equipment);
					di.insert(cEquipment);
				}
			}
		}
		di.end();
	}

	protected void migrateEquipment(String deviceDn, List<Equipment> equipments) throws Exception {

		executeDelete("delete from CEquipment c where c.dn  like '" + deviceDn + "@%'", CEquipment.class);
		DataInserter di = new DataInserter(emsid);
		if (equipments != null && equipments.size() > 0) {
			for (Equipment equipment : equipments) {
				if (equipment.getDn().contains("slot")) {
					CEquipment cEquipment = transEquipment(equipment);
					di.insert(cEquipment);
				}
			}
		}
		di.end();
	}

	protected void migrateIPRoute() throws Exception {
		// //////////////////// migrate route //////////////////////////////
		executeDelete("delete  from CTunnel_Section c where c.emsName = '" + emsdn + "'", CTunnel_Section.class);
		executeDelete("delete from CRoute c where c.emsName = '" + emsdn + "'", CIPRoute.class);
		DataInserter di = new DataInserter(emsid);
		Map<String, String> locMap = new HashMap<String, String>();
		Map<String, String> sectionMap = new HashMap<String, String>();
		List<Section> sections = sd.queryAll(Section.class);
		if (sections != null && sections.size() > 0) {
			for (Section section : sections) {
				if (section.getDn().contains("SECTION")) {
					sectionMap.put(section.getDn(), section.getNativeEMSName());
				} else if (section.getDn().contains("LOC")) {
					locMap.put(section.getNativeEMSName(), section.getDn());
				}
			}
		}
		int from = 0;
		int limit = 10000;
		while (true) {
			List<R_TrafficTrunk_CC_Section> rtcs = sd.query("select c from R_TrafficTrunk_CC_Section c", from, limit);
			if (from == 0 && (rtcs == null || rtcs.isEmpty()))
				break;
			// throw new EMSDataTableEmptyException("Table empty : R_TrafficTrunk_CC_Section");
			if (rtcs.isEmpty())
				break;
			from = from + rtcs.size();
			getLogger().info("Migrate route " + from);

			for (int i = 0; i < rtcs.size(); i++) {
				R_TrafficTrunk_CC_Section r_trafficTrunk_cc_section = rtcs.get(i);
				String trafficTrunDn = r_trafficTrunk_cc_section.getTrafficTrunDn();

				CIPRoute cRoute = new CIPRoute();
				cRoute.setEmsName(emsdn);
				cRoute.setDn(SysUtil.nextDN());
				cRoute.setTunnelDn(trafficTrunDn);
				cRoute.setTunnelId(DatabaseUtil.getSID(CTunnel.class, trafficTrunDn));
				cRoute.setEntityType(r_trafficTrunk_cc_section.getType());
				if (r_trafficTrunk_cc_section.getCcOrSectionDn() != null && !r_trafficTrunk_cc_section.getCcOrSectionDn().isEmpty()) {
					cRoute.setEntityDn(r_trafficTrunk_cc_section.getCcOrSectionDn());
				}
				cRoute.setAptp(r_trafficTrunk_cc_section.getaPtp());
				cRoute.setZptp(r_trafficTrunk_cc_section.getzPtp());

				// cRoute.setAptpId(DatabaseUtil.getSID(CPTP.class, cRoute.getAptp()));
				// cRoute.setZptpId(DatabaseUtil.getSID(CPTP.class, cRoute.getZptp()));
				cRoute.setAend(r_trafficTrunk_cc_section.getaEnd());
				cRoute.setZend(r_trafficTrunk_cc_section.getzEnd());
				cRoute.setCollectTimepoint(r_trafficTrunk_cc_section.getCreateDate());

				di.insert(cRoute);

				if (r_trafficTrunk_cc_section.getType().equals("SECTION")) {
					String sectiondn = sectionMap.get(r_trafficTrunk_cc_section.getCcOrSectionDn());
					if (sectiondn != null) {
						sectiondn = locMap.get(sectiondn);
					}
					if (sectiondn != null) {
						CTunnel_Section ts = new CTunnel_Section();
						ts.setDn(trafficTrunDn + "<>" + sectiondn);
						ts.setEmsName(emsdn);
						ts.setTunnelDn(trafficTrunDn);
						ts.setTunnelId(DatabaseUtil.getSID(CTunnel.class, trafficTrunDn));
						ts.setSectionDn(sectiondn);
						ts.setSectionId(DatabaseUtil.getSID(CSection.class, sectiondn));
						if (!DatabaseUtil.isSIDExisted(CTunnel_Section.class, ts.getDn())) // section+tunnel 可能有重复
							di.insert(ts);
					}
				}

			}

		}
		di.end();
	}

	@Override
	public CPTP transPTP(PTP ptp) {
		// EMS:ALU/zshptn02@ManagedElement:111/830@PTP:r1sr1sl2/ETHLocPort#11#1
		// EMS:ALU/zshptn02@ManagedElement:111/830@EquipmentHolder:/rack=1/shelf=1/slot=2@Equipment:1

		// EMS:ALU/zshptn02@ManagedElement:118/2115@PTP:E1-1-1-4-1
		// EMS:ALU/zshptn02@ManagedElement:118/2115@PTP:r1sr1sl3/ETHLocPort#2#1
		// EMS:ALU/zshptn02@ManagedElement:118/2115@FTP:r1sr1sl3/ETHLocPort#8#1
		// EMS:ALU/zshptn02@ManagedElement:100/1@FTP:LAG#1
		// EMS:ALU/zshptn02@ManagedElement:100/1@GTP:LAG#1
		// EMS:ALU/zshptn02@ManagedElement:100/34@PTP:PUTUO1/STM4-1-1-12-1
		// EMS:ALU/zshptn02@ManagedElement:100/34@PTP:PUTUO1/UGE1-1-1-9-1-1-1-1
		// EMS:ALU/zshptn02@ManagedElement:100/34@FTP:UGE1-1-1-9-13-3-5-1
		// EMS:ALU/zshptn02@ManagedElement:100/3@PTP:LINCHENG3/STM16-1-1-12-1
		// EMS:ALU/zshptn02@ManagedElement:100/3@PTP:LINCHENG3/UGE1-1-1-14-1-1-1-1
		// EMS:ALU/zshptn02@ManagedElement:100/3@PTP:LINCHENG3/PP10AD-1-1-19-112

		String dn = ptp.getDn();
		if (dn.contains("UGE") || dn.contains("PP10AD")) {
			return null;
		}

		CPTP cptp = new CPTP();
		cptp.setDn(dn);
		// if (dn.contains("FTP") && dn.contains("ETH")) {
		// cptp.setDn(dn.replace("FTP", "PTP"));
		// }
		String tag1 = ptp.getTag1();
		if (tag1 != null && tag1.startsWith("1-1-")) {
			String[] tags = tag1.split("-");
			String me = dn.substring(0, dn.lastIndexOf("@"));
			String carddn = null;
			String portno = null;
			if (tags.length == 3) {
				carddn = me + "@EquipmentHolder:/rack=1/shelf=1/slot=" + tags[2] + "@Equipment:1";

				if (dn.contains("/ETHLocPort#")) {
					portno = dn.substring(dn.indexOf("/ETHLocPort#") + 12, dn.lastIndexOf("#"));
				} else {
					portno = dn.substring(dn.lastIndexOf("-") + 1);
				}
			} else if (tags.length == 4) {
				carddn = me + "@EquipmentHolder:/rack=1/shelf=1/slot=" + tags[2] + "/sub-slot=" + tags[3] + "@Equipment:1";
				portno = "1";
			}
			if (carddn != null) {
				cptp.setParentDn(carddn);
				cptp.setCardid(DatabaseUtil.getSID(CEquipment.class, carddn));
			}
			cptp.setNo(portno);
		}
		if (cptp.getParentDn() == null || cptp.getParentDn().isEmpty()) {
			cptp.setParentDn(ptp.getParentDn());
		}
		if (cptp.getNo() == null || cptp.getNo().isEmpty()) {
			cptp.setNo(dn.substring(dn.lastIndexOf("#") + 1));
		}

		cptp.setCollectTimepoint(ptp.getCreateDate());
		cptp.setEdgePoint(ptp.isEdgePoint());
		cptp.setConnectionState(ptp.getConnectionState());
		cptp.setTpMappingMode(ptp.getTpMappingMode());
		cptp.setDirection(DicUtil.getPtpDirection(ptp.getDirection()));
		cptp.setTransmissionParams(ptp.getTransmissionParams());
		// cptp.setRate(ptp.getRate());
		cptp.setLayerRates(ptp.getRate());
		cptp.setTpProtectionAssociation(ptp.getTpProtectionAssociation());
		cptp.setEmsName(ptp.getEmsName());
		cptp.setUserLabel(ptp.getUserLabel());
		cptp.setNativeEMSName(ptp.getNativeEMSName());
		cptp.setOwner(ptp.getOwner());
		cptp.setAdditionalInfo(ptp.getAdditionalInfo());
		cptp.setDeviceDn(ptp.getParentDn());

		cptp.setEoType(DicUtil.getEOType(cptp.getLayerRates()));
		cptp.setSpeed(DicUtil.getSpeed(cptp.getLayerRates()));
		cptp.setType(DicUtil.getPtpType(dn, cptp.getLayerRates()));
		return cptp;
	}

	private void migrateFlowDomainFragment() throws Exception {
		executeDelete("delete from CPWE3 c where c.emsName = '" + emsdn + "'", CPWE3.class);
		executeDelete("delete from CPW c where c.emsName = '" + emsdn + "'", CPW.class);
		executeDelete("delete from CPWE3_PW c where c.emsName = '" + emsdn + "'", CPWE3_PW.class);
		executeDelete("delete from CTunnel c where c.emsName = '" + emsdn + "'", CTunnel.class);
		executeDelete("delete from CPW_Tunnel c where c.emsName = '" + emsdn + "'", CPW_Tunnel.class);

		DataInserter di = new DataInserter(emsid);
		List<FlowDomainFragment> flowDomainFragments = sd.queryAll(FlowDomainFragment.class);
		List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
		HashMap<String, SubnetworkConnection> tunnelmap = new HashMap<String, SubnetworkConnection>();
		HashMap<String, SubnetworkConnection> pwmap = new HashMap<String, SubnetworkConnection>();
		for (SubnetworkConnection snc : sncs) {
			if (snc.getRate().equals("1001")) {
				// String dn = snc.getDn();
				// dn = dn.substring(dn.lastIndexOf(":") + 1);
				// tunnelmap.put(dn, snc);
				tunnelmap.put(snc.getTag1(), snc);
			} else {
				pwmap.put(snc.getUserLabel(), snc);
			}
		}

		getLogger().info("tunnel size = " + tunnelmap.size());
		getLogger().info("pw size = " + pwmap.size());
		getLogger().info("pwe3 size = " + flowDomainFragments.size());
		Collection<SubnetworkConnection> tunnels = tunnelmap.values();
		for (Iterator<SubnetworkConnection> iterator = tunnels.iterator(); iterator.hasNext();) {
			SubnetworkConnection tunnel = iterator.next();
			CTunnel cTunnel = transTunnel(tunnel);
			cTunnel.setSid(DatabaseUtil.nextSID(cTunnel));
			di.insert(cTunnel);
		}

		int idx = 0;
		if (flowDomainFragments != null && flowDomainFragments.size() > 0) {
			for (FlowDomainFragment fdf : flowDomainFragments) {
				if (idx++ % 1000 == 0)
					getLogger().info("flowDomainFragments:" + idx);
				CPWE3 cpwe3 = transFDF(fdf);
				cpwe3.setSid(DatabaseUtil.nextSID(cpwe3));
				di.insert(cpwe3);

				String parentDn = fdf.getTag1();
				if (parentDn == null || parentDn.trim().isEmpty()) {
					getLogger().error("PWE3 parentDn is null : " + cpwe3.getDn());
					continue;
				}

				if (cpwe3.getFdfrType().equals(FDFRT_POINT_TO_POINT)) {
					SubnetworkConnection pwTrail = pwmap.get(parentDn);
					if (pwTrail == null) {
						getLogger().error("PWTrail not found : pw=" + parentDn);
						continue;
					}
					String aptp = null;
					String zptp = null;
					String apwe3ne = ptp2ne(cpwe3.getAptp());
					String zpwe3ne = ptp2ne(cpwe3.getZptp());
					if (pwTrail.getaNE().equals(apwe3ne) && pwTrail.getzNE().equals(zpwe3ne)) {
						aptp = cpwe3.getAptp();
						zptp = cpwe3.getZptp();
					} else if (pwTrail.getzNE().equals(apwe3ne) && pwTrail.getaNE().equals(zpwe3ne)) {
						aptp = cpwe3.getZptp();
						zptp = cpwe3.getAptp();
					} else {
						getLogger().error("PW do not match PWE3: pwe3=" + cpwe3.getDn());
						continue;
					}
					CPW cpw = transCPW(pwTrail, aptp, zptp);
					cpw.setSid(DatabaseUtil.nextSID(cpw));
					di.insert(cpw);

					CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
					di.insert(cpwe3_pw);

				} else if (cpwe3.getFdfrType().equals(FDFRT_POINT_TO_MULTIPOINT)) {
					String[] zptps = cpwe3.getZptps().split(Constant.listSplitReg);
					if (zptps.length == 0) {
						getLogger().error("pwe3 end is null : pwe3=" + cpwe3.getDn());
						continue;
					}
					List<String> zptplist = Arrays.asList(zptps);
					Map<String, String> ptpmap = new HashMap<String, String>();
					for (String ptp : zptplist) {
						ptpmap.put(ptp2ne(ptp), ptp);
					}
					ptpmap.put(ptp2ne(cpwe3.getAptps()), cpwe3.getAptps());
					if (ptpmap.size() != zptplist.size() + 1) {
						getLogger().error("duplicate ne : pwe3=" + cpwe3.getDn());
						continue;
					}

					List<SubnetworkConnection> cpws = new ArrayList<SubnetworkConnection>();
					String[] pwTrailDns = parentDn.split(Constant.listSplitReg);
					for (String pwTrailDn : pwTrailDns) {
						SubnetworkConnection pwTrail = pwmap.get(pwTrailDn);
						if (pwTrail == null) {
							getLogger().error("PWTrail not found : pw=" + pwTrailDn);
							continue;
						}
						cpws.add(pwTrail);
					}
					for (SubnetworkConnection pw : cpws) {
						CPW cpw = transCPW(pw, ptpmap.get(pw.getaNE()), ptpmap.get(pw.getzNE()));
						cpw.setSid(DatabaseUtil.nextSID(cpw));
						di.insert(cpw);

						CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
						di.insert(cpwe3_pw);
					}
				}
			}
		}

		// PW-TUNNEL
		Collection<SubnetworkConnection> pws = pwmap.values();
		int index = 0;
		if (pws != null && pws.size() > 0) {
			for (SubnetworkConnection pw : pws) {
				try {
					if (index++ % 1000 == 0)
						getLogger().info("pws:" + index);
					if (!DatabaseUtil.isSIDExisted(CPW.class, pw.getDn())) {
						continue;
					}

					String parentDn = pw.getParentDn();
					if (parentDn == null || parentDn.trim().isEmpty()) {
						getLogger().error("PW parentDn is null : " + pw.getDn());
						continue;
					}

					SubnetworkConnection tunnelTrail = tunnelmap.get(parentDn);
					if (tunnelTrail == null) {
						getLogger().error("TunnelTrail not found : " + parentDn);
						continue;
					}

					CPW_Tunnel cpw_tunnel = transCPW_Tunnel(pw.getEmsName(), pw.getDn(), tunnelTrail.getDn());
					di.insert(cpw_tunnel);
				} catch (Exception e) {
					getLogger().error("ERROR PW : pw=" + pw.getDn(), e);
				}
			}
		}

		flowDomainFragments.clear();
		sncs.clear();
		tunnelmap.clear();
		pwmap.clear();

		di.end();
		getLogger().info("migrateFlowDomainFragment success");

	}

	private CTunnel transTunnel(SubnetworkConnection src) {
		CTunnel des = new CTunnel();
		des.setDn(src.getDn());
		des.setCollectTimepoint(src.getCreateDate());
		des.setRerouteAllowed(src.getRerouteAllowed());
		// des.setAdministrativeState(src.getAdministrativeState());
		des.setActiveState(src.getSncState());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		// des.setTransmissionParams(src.getTransmissionParams());
		des.setNetworkRouted(src.getNetworkRouted());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());
		String aptp = src.getaPtp();
		String zptp = src.getzPtp();
		if (src.getSncType().equals(ST_SIMPLE)) {
			des.setAptp(aptp);
			des.setZptp(zptp);
			des.setAptpId(DatabaseUtil.getSID(CPTP.class, des.getAptp()));
			des.setZptpId(DatabaseUtil.getSID(CPTP.class, des.getZptp()));
		} else {
			des.setAptps(aptp);
			des.setZptps(zptp);
		}
		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndTrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());
		HashMap<String, String> at = MigrateUtil.transMapValue(src.getAdditionalInfo());
		des.setPir(at.get("CIR"));
		des.setCir(at.get("PIR"));

		return des;
	}

	private CPW transCPW(SubnetworkConnection pw, String aptp, String zptp) {
		CPW cpw = transPW(pw);
		cpw.setAptp(aptp);
		cpw.setZptp(zptp);
		cpw.setAptpId(DatabaseUtil.getSID(CPTP.class, cpw.getAptp()));
		cpw.setZptpId(DatabaseUtil.getSID(CPTP.class, cpw.getZptp()));
        String additionalInfo = pw.getAdditionalInfo();
        if (additionalInfo != null) {
            HashMap<String, String> map = MigrateUtil.transMapValue(additionalInfo);
            String protectionState = map.get("ProtectionState");
            if ("Active".equals(protectionState)) {
                cpw.setaWorkingMode("Working");
                cpw.setzWorkingMode("Working");
            } else {
                cpw.setaWorkingMode("Standby");
                cpw.setzWorkingMode("Standby");
            }

        }
        return cpw;
	}

	public CPW transPW(SubnetworkConnection src) {
		CPW des = new CPW();
		des.setDn(src.getDn());
		des.setCollectTimepoint(src.getCreateDate());
		des.setRerouteAllowed(src.getRerouteAllowed());
		// des.setAdministrativeState(src.getAdministrativeState());
		des.setActiveState(src.getSncState());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		// des.setTransmissionParams(src.getTransmissionParams());
		des.setNetworkRouted(src.getNetworkRouted());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());

		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndTrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());
		HashMap<String, String> at = MigrateUtil.transMapValue(src.getAdditionalInfo());
		des.setPir(at.get("CIR"));
		des.setCir(at.get("PIR"));

		return des;
	}

	@Override
	public CPWE3 transFDF(FlowDomainFragment src) {
		CPWE3 des = new CPWE3();
		des.setDn(src.getDn());
		// des.setTag1(src.getTag1());
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
		des.setRate(src.getRate());
		des.setFdfrType(src.getFdfrType());
		// EVC
		String aend = src.getaEnd();
		String zend = src.getzEnd();

		String aptp = src.getaPtp();
		String zptp = src.getzPtp();

		if (src.getFdfrType().equals(FDFRT_POINT_TO_POINT)) {
			String[] aends = aend.split(Constant.listSplitReg);
			String[] aptps = aend.split(Constant.listSplitReg);
			if (aends.length == 2) {
				des.setAend(aends[0]);
				des.setZend(aends[1]);
				des.setAptp(aptps[0]);
				des.setZptp(aptps[1]);
				des.setAptpId(DatabaseUtil.getSID(CPTP.class, des.getAptp()));
				des.setZptpId(DatabaseUtil.getSID(CPTP.class, des.getZptp()));
			} else {
				des.setAend(aend);
				des.setZend(zend);
			}
		} else {
			des.setAend(aend);
			des.setZend(zend);
			des.setAptps(aptp);
			des.setZptps(zptp);
		}

		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndtrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());

		return des;
	}

	public static void main(String[] args) throws Exception {

	}

	public CEquipment transEquipment(Equipment equipment) {
		CEquipment cequipment = super.transEquipment(equipment);
		String objectType = equipment.getInstalledEquipmentObjectType();
		if (objectType == null || objectType.isEmpty()) {
			objectType = equipment.getExpectedEquipmentObjectType();
		}
		cequipment.setNativeEMSName(objectType);
		return cequipment;
	}
}
