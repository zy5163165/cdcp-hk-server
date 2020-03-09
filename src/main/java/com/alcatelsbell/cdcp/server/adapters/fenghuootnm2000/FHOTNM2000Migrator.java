package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.IllegalDNStringException;
import com.alcatelsbell.cdcp.server.RelationDataNotFoundException;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.asb.mule.probe.framework.entity.*;
import org.asb.mule.probe.framework.service.Constant;

import org.asb.mule.probe.framework.util.FileLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-30
 * Time: 下午2:40
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FHOTNM2000Migrator extends AbstractDBFLoader {

	public FHOTNM2000Migrator(String fileUrl, String emsdn) {
		this.fileUrl = fileUrl;
		this.emsdn = emsdn;
		MigrateThread.thread().initLog("FHPTN_"+emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
	}

	private static FileLogger logger = new FileLogger("FH-Device.log");

	public FHOTNM2000Migrator(Serializable object, String emsdn) {
		this.emsdn = emsdn;
		this.resultObject = object;
		MigrateThread.thread().initLog(logger);
	}

	public void doExecute() throws Exception {

		checkEMS(emsdn, "烽火");

		logAction("migrateManagedElement", "同步网元", 1);
		migrateManagedElement();
		logAction("migrateEquipmentHolder", "同步槽道", 10);
		migrateEquipmentHolder();
		logAction("migrateEquipment", "同步板卡", 20);
		migrateEquipment();

		logAction("migratePTP", "同步端口", 30);
		migratePTP();

		logAction("migrateSection", "同步段", 40);
		migrateSection();

		if (migrateLogical &&
				!isTableHasData(FlowDomainFragment.class) && !isTableHasData(CrossConnect.class)) {
			migrateLogical = false;
			getLogger().info("migratelogical = false ,becase fdfr and cc is null");
		}


		if (migrateLogical) {

			logAction("migrateFlowDomainFragment", "同步业务", 50);
			migrateFlowDomainFragment();

			logAction("migrateRoute", "同步路由", 70);
			migrateIPRoute();

			logAction("migrateSubnetwork", "同步子网", 80);
			migrateSubnetwork();

			logAction("migrateProtectionGroup", "同步保护组", 90);
			migrateProtectGroup();

			logAction("migrateProtectingPWTunnel", "同步保护组", 95);
			migrateProtectingPWTunnel();

			getLogger().info("release");
		}

		// ////////////////////////////////////////
		sd.release();

	}

	private HashMap queryAllMap(SqliteDelegation sd, Class cls) {
		HashMap map = new HashMap();
		List list = sd.queryAll(cls);
		for (int i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			String dn = ((BObject) o).getDn();
			map.put(dn, o);
		}
		return map;
	}

	// private void checkRoute(SqliteDelegation sd) {
	// List<TrafficTrunk> tts = sd.queryAll(TrafficTrunk.class);
	// HashSet set = new HashSet();
	// List<IPRoute> routes = sd.queryAll(IPRoute.class);
	// for (int i = 0; i < routes.size(); i++) {
	// IPRoute ipRoute = routes.get(i);
	// String trafficTrunkDn = ipRoute.getTrafficTrunkDn();
	// if (trafficTrunkDn != null) {
	// set.add(trafficTrunkDn);
	// }
	// }
	//
	//
	//
	//
	//
	//
	// for (int i = 0; i < tts.size(); i++) {
	// TrafficTrunk trafficTrunk = tts.get(i);
	// if (trafficTrunk.getTag2().equals("309")) {
	// if (!set.contains(trafficTrunk.getDn())) {
	// System.out.println(trafficTrunk.getNativeEMSName());
	// }
	// }
	// }
	// }

	private void checkPTP(SqliteDelegation sd) {
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
				dn = MigrateUtil.simpleDN2FullDn(dn, new String[] { "EMS", "ManagedElement" });
				meNames.remove(dn);
			} catch (IllegalDNStringException e) {
				e.printStackTrace();
			}

		}

		for (String mename : meNames) {
			System.out.println(mename);
		}
	}

	private void checkEquipmentHolders(SqliteDelegation sd) {
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
			// try {
			// dn = simpleDN2FullDn(dn,new String[]{"EMS", "ManagedElement"});
			// } catch (IllegalDNStringException e) {
			// e.printStackTrace();
			// }
			if (!meNames.contains(dn)) {
				System.out.println(managedElement.getDn() + "\t" + managedElement.getNativeEMSName() + "\t" + managedElement.getProductName());
			}
		}
	}

	// private void migrateProtectionGroup() throws Exception {
	// executeDelete("delete from CProtectionGroup c where c.emsName = '" + emsdn + "'", CProtectionGroup.class);
	// executeDelete("delete from CProtectionGroupTunnel c where c.emsName = '" + emsdn + "'", CProtectionGroupTunnel.class);
	// DataInserter di = new DataInserter(emsid);
	//
	// // List list = (List) com.alcatelsbell.nms.util.ObjectUtil.readObjectByPath("d:\\work\\ptlist_SNCP");
	// // ManagedElementDataTask task = new ManagedElementDataTask();
	// // List<ProtectionGroup> pgs = task.transProtectGroup(list);
	//
	// List<ProtectionGroup> pgs = sd.queryAll(ProtectionGroup.class);
	//
	// List<CProtectionGroup> cpps = new ArrayList<CProtectionGroup>();
	// for (int i = 0; i < pgs.size(); i++) {
	// ProtectionGroup protectionGroup = pgs.get(i);
	// CProtectionGroup cpg = transProtectionGroup(protectionGroup);
	// di.insert(cpg);
	// cpps.add(cpg);
	// }
	//
	// // /////////////////////////插入关联表///////////////////////////////////
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
	// cProtectionGroupTunnel.setDn(SysUtil.nextDN());
	// // cProtectionGroupTunnel.setSid(DatabaseUtil.nextSID(cProtectionGroupTunnel));
	// cProtectionGroupTunnel.setProtectGroupId(cProtectionGroup.getSid());
	// cProtectionGroupTunnel.setProtectGroupDn(cProtectionGroup.getDn());
	// cProtectionGroupTunnel.setTunnelDn(protecting);
	// cProtectionGroupTunnel.setEmsName(emsdn);
	// cProtectionGroupTunnel.setTunnelId(DatabaseUtil.getSID(CTunnel.class, protecting));
	// cProtectionGroupTunnel.setStatus("PROTECTING");
	// di.insert(cProtectionGroupTunnel);
	// }
	// }
	//
	// if (protecteds != null && protecteds.length > 0) {
	// for (int j = 0; j < protecteds.length; j++) {
	// String protectedd = protecteds[j];
	// CProtectionGroupTunnel cProtectionGroupTunnel = new CProtectionGroupTunnel();
	// cProtectionGroupTunnel.setDn(SysUtil.nextDN());
	// // cProtectionGroupTunnel.setSid(DatabaseUtil.nextSID(cProtectionGroupTunnel));
	// cProtectionGroupTunnel.setProtectGroupId(cProtectionGroup.getSid());
	// cProtectionGroupTunnel.setProtectGroupDn(cProtectionGroup.getDn());
	// cProtectionGroupTunnel.setTunnelId(DatabaseUtil.getSID(CTunnel.class, protectedd));
	// cProtectionGroupTunnel.setTunnelDn(protectedd);
	// cProtectionGroupTunnel.setEmsName(emsdn);
	// cProtectionGroupTunnel.setStatus("PROTECTED");
	// di.insert(cProtectionGroupTunnel);
	// }
	// }
	// }
	//
	// di.end();
	//
	// }
	//
	// private CProtectionGroup transProtectionGroup(ProtectionGroup src) {
	// CProtectionGroup des = new CProtectionGroup();
	// des.setDn(src.getDn());
	// des.setDn(SysUtil.nextDN());
	// des.setTag1(src.getDn());
	// des.setSid(DatabaseUtil.nextSID(des));
	// // des.setTag1(src.getDn());
	// // des.setCollectTimepoint(src.getCollectTimepoint());
	// des.setParentDn(src.getParentDn());
	// des.setEmsName(emsdn);
	// des.setUserLabel(src.getUserLabel());
	// des.setNativeEMSName(src.getNativeEMSName());
	// des.setOwner(src.getOwner());
	// des.setProtectionGroupType(src.getProtectionGroupType());
	// des.setProtectionSchemeState(src.getProtectionSchemeState());
	// des.setReversionMode(src.getReversionMode());
	// des.setRate(src.getRate());
	// des.setPgpParameters(src.getPgpParameters());
	// String protectedList = src.getProtectedList();
	// if (protectedList.contains(Constant.listSplit))
	// protectedList = protectedList.substring(0, protectedList.indexOf(Constant.listSplit));
	// des.setProtectedList(protectedList);
	// String protectingList = src.getProtectingList();
	// if (protectingList.contains(Constant.listSplit))
	// protectingList = protectingList.substring(0, protectingList.indexOf(Constant.listSplit));
	// des.setProtectingList(protectingList);
	// return des;
	// }

	private void migrateCTP(SqliteDelegation sd, JpaInsertHelper jpaInsertHelper) throws Exception {
		executeDelete("delete   from CCTP c where c.emsName = '" + emsdn + "'", CCTP.class);
		List<CTP> ctps = sd.queryAll(CTP.class);
		if (ctps != null && ctps.size() > 0) {
			for (CTP ctp : ctps) {
				CCTP cctp = transCTP(ctp);
				jpaInsertHelper.insertBObject(cctp);
			}
		}
		ctps.clear();
	}

	protected void migratePTP() throws Exception {
		if (!isTableHasData(PTP.class))
			return;
		executeDelete("delete  from CPTP c where c.emsName = '" + emsdn + "'", CPTP.class);
		executeDelete("delete from CIPAddress c where c.emsName = '" + emsdn + "'", CIPAddress.class);
		executeDelete("delete from CFTP_PTP c where c.emsName = '" + emsdn + "'", CFTP_PTP.class);
		List<PTP> ptps = sd.queryAll(PTP.class);
		insertPtps(ptps);
	}

	protected void migratePTP(String deviceDn, List<PTP> ptps) throws Exception {
		executeDelete("delete from CPTP c where c.dn  like '" + deviceDn + "@%'", CPTP.class);
		executeDelete("delete from CFTP_PTP c where c.ptpDn  like '" + deviceDn + "@%'", CFTP_PTP.class);
		insertPtps(ptps);
	}

	private void insertPtps(List<PTP> ptps) throws Exception {
		DataInserter di = new DataInserter(emsid);
		getLogger().info("migratePtp size = " + (ptps == null ? null : ptps.size()));
		List<CPTP> cptps = new ArrayList<CPTP>();
		if (ptps != null && ptps.size() > 0) {
			for (PTP ptp : ptps) {
				CPTP cptp = transPTP(ptp);
				if (cptp != null) {
					cptps.add(cptp);
				}
			}
		}

		this.removeDuplicateDN(cptps);
		for (int i = 0; i < cptps.size(); i++) {
			CPTP cptp = cptps.get(i);
			di.insert(cptp);
			if (cptp.getIpAddress() != null && !cptp.getIpAddress().isEmpty()) {
				CIPAddress ip = new CIPAddress();
				ip.setDn(SysUtil.nextDN());
				ip.setSid(DatabaseUtil.nextSID(ip));
				ip.setEmsName(emsdn);
				ip.setEmsid(emsid);
				ip.setIpaddress(cptp.getIpAddress());
				ip.setPtpId(DatabaseUtil.getSID(CPTP.class, cptp.getDn()));
				di.insert(ip);
			}


		}
		di.end();

		  di = new DataInserter(emsid);
		for (int i = 0; i < cptps.size(); i++) {
			CPTP cptp = cptps.get(i);
			if (cptp.getDn().equals("EMS:JH-OTNM2000-1-PTN@ManagedElement:134220892;76033@FTP:/port=104"))
				System.out.println();

			if (cptp.getTransmissionParams() != null && cptp.getDn().contains("FTP")) {
				//305@LAGNumber1:S-3-14||LAGNumber2:S-14-14||@

				String transmissionParams = cptp.getTransmissionParams();
				int idx = 1;
				while (true) {
					String key = "LAGNumber"+(idx++);
					if (transmissionParams.contains(key)) {
						try {
							int beginIndex = transmissionParams.indexOf(key) + key.length() + 1;
							String str = transmissionParams.substring(beginIndex,transmissionParams.indexOf("||",beginIndex));
							if (!str.contains("-")) continue;
							String[] split = str.split("-");
							if (split.length == 2) {
								split = str.replace("S","S-").split("-");
							}
							String slot = split[1];
							String port = split[2];
							String slotIn10 = slot;
							Integer it = Integer.parseInt(slot);
							if (slotNoHex)
								slot = Integer.toHexString(it).toUpperCase();

							if (slot.length() == 1) slot = "0"+slot;
							String ql1 = "select c from CSlot c where  c.no = '" + slot + "' and c.parentDn = '" + cptp.getDeviceDn() + "'";
							List<CSlot> slots = findObjects(CSlot.class, ql1);
							getLogger().info("ftp=" + cptp.getDn());
							getLogger().info("sql=" + ql1);
							getLogger().info("result = " + (slots == null ? null : slots.size()));

							if (slotNoHex && (slots == null || slots.isEmpty())) {
								ql1 = "select c from CSlot c where  c.no = '" + slot.toLowerCase() + "' and c.parentDn = '" + cptp.getDeviceDn() + "'";
								slots = findObjects(CSlot.class, ql1);
								getLogger().info("ftp=" + cptp.getDn());
								getLogger().info("sql=" + ql1);
								getLogger().info("result = " + (slots == null ? null : slots.size()));
							}

							if (slotNoHex && (slots == null || slots.isEmpty())) {
								ql1 = "select c from CSlot c where  c.no = '" + slotIn10 + "' and c.parentDn = '" + cptp.getDeviceDn() + "'";
								slots = findObjects(CSlot.class, ql1);
								getLogger().info("ftp=" + cptp.getDn());
								getLogger().info("sql=" + ql1);
								getLogger().info("result = " + (slots == null ? null : slots.size()));
							}

							if (slots != null) {
								CSlot cSlot = slots.get(0);
								String ql  ="select c from CPTP c where c.parentDn like '"+cSlot.getDn()+"%' and c.no = '"+port+"' and c.dn like '%PTP%'";
								List<CPTP> ls = findObjects(CPTP.class, ql);
								getLogger().info("2ql=" + ql1);
								getLogger().info("2result = " + (ls == null ? null : ls.size()));
								if (ls != null && ls.size() > 0) {
									CPTP p = ls.get(0);

									CFTP_PTP cftp_ptp = new CFTP_PTP();
									cftp_ptp.setDn(SysUtil.nextDN());
									cftp_ptp.setEmsName(emsdn);
									cftp_ptp.setFtpDn(cptp.getDn());
									cftp_ptp.setPtpDn(p.getDn());
									cftp_ptp.setFtpId(DatabaseUtil.getSID(CPTP.class, cftp_ptp.getFtpDn()));
									cftp_ptp.setPtpId(DatabaseUtil.getSID(CPTP.class, cftp_ptp.getPtpDn()));
									cftp_ptp.setRate(cptp.getRate());
									cftp_ptp.setTransmissionParams(cptp.getTransmissionParams());
									cftp_ptp.setTpMappingMode(cptp.getTpMappingMode());

									di.insert(cftp_ptp);
								}
							}
						} catch (Exception e) {

							getLogger().error("error lagnumber : "+transmissionParams);
							getLogger().error(e,e);
						}
						//S-3-14
					}  else
						break;
				}
			}
		}

		di.end();

	}

	private void migrateFlowDomainFragment() throws Exception {
		executeDelete("delete from CPWE3 c where c.emsName = '" + emsdn + "'", CPWE3.class);
		executeDelete("delete from CPW c where c.emsName = '" + emsdn + "'", CPW.class);
		executeDelete("delete from CPWE3_PW c where c.emsName = '" + emsdn + "'", CPWE3_PW.class);
		executeDelete("delete from CTunnel c where c.emsName = '" + emsdn + "'", CTunnel.class);
		executeDelete("delete from CPW_Tunnel c where c.emsName = '" + emsdn + "'", CPW_Tunnel.class);
		// executeDelete("delete  from CTunnel_Section c where c.emsName = '" + emsdn + "'", CTunnel_Section.class);
		// executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
		// HashMap<String, String> trafficTrunkDnMap = new HashMap<String, String>();

        List<CPWE3> cpwe3s = new ArrayList<CPWE3>();
        List<CPW> cpwes = new ArrayList<CPW>();
        List<CPWE3_PW> cpwe3_pws = new ArrayList<CPWE3_PW>();
        List<CTunnel> cTunnels = new ArrayList<CTunnel>();
        List<CPW_Tunnel> cpw_tunnels = new ArrayList<CPW_Tunnel>();


        try {
            List<FlowDomainFragment> flowDomainFragments = sd.queryAll(FlowDomainFragment.class);
            HashMap<String, FlowDomainFragment> tunnelmap = new HashMap<String, FlowDomainFragment>();
            HashMap<String, FlowDomainFragment> pwmap = new HashMap<String, FlowDomainFragment>();
            List<FlowDomainFragment> pwe3s = new ArrayList<FlowDomainFragment>();
            for (FlowDomainFragment fdfr : flowDomainFragments) {
				if (fdfr.getParentDn() != null)
					fdfr.setParentDn(fdfr.getParentDn().replaceAll("_",","));
                String trailid = fdfr.getTag2();
                if (trailid == null) {
                    continue;
                }
                // String identifier = traildn.substring(traildn.indexOf("FlowDomainFragment:") + 19);
                if (fdfr.getRate().equals("309")) {
                    tunnelmap.put(trailid, fdfr);
                } else if (fdfr.getRate().equals("1500")) {
                    pwmap.put(trailid, fdfr);
                } else {
                    pwe3s.add(fdfr);
                }
            }

            Collection<FlowDomainFragment> tunnels = tunnelmap.values();
            for (Iterator<FlowDomainFragment> iterator = tunnels.iterator(); iterator.hasNext();) {
                FlowDomainFragment tunnel = iterator.next();
                CTunnel cTunnel = transTunnel(tunnel);
                cTunnels.add(cTunnel);
            }
            getLogger().info("tunnel size = " + tunnelmap.size());
            getLogger().info("pw size = " + pwmap.size());
            getLogger().info("pwe3 size = " + pwe3s.size());

            int idx = 0;
            if (pwe3s != null && pwe3s.size() > 0) {
                // List<CPWE3> cpwe3s = new ArrayList<CPWE3>();
                for (FlowDomainFragment fdf : pwe3s) {
                    if (idx++ % 1000 == 0)
                        getLogger().info("flowDomainFragments:" + idx);
                    CPWE3 cpwe3 = null;
                    try {
                        cpwe3 = transFDF(fdf);
                        cpwe3.setSid(DatabaseUtil.nextSID(cpwe3));
                        cpwe3s.add(cpwe3);
                    } catch (Exception e) {
                        LogUtil.error(e, e);
                        continue;
                    }

                    String pwTrailID = fdf.getParentDn();
                    if (pwTrailID == null || pwTrailID.trim().isEmpty()) {
                        getLogger().error("PWE3 parentDn is null : pwe3=" + cpwe3.getDn());
                        continue;
                    }
					if (cpwe3.getDn().equals("EMS:JH-OTNM2000-1-PTN@FlowDomain:1@FlowDomainFragment:587221898"))
						System.out.println();
					if (cpwe3.getFdfrType().equals(FDFRT_POINT_TO_POINT)  || cpwe3.getFdfrType().equals(FDFRT_POINT_TO_MULTIPOINT)) {
						FlowDomainFragment pwTrail = pwmap.get(pwTrailID);
                        if (pwTrail == null) {
							if (pwTrailID.contains(",")) {
								try {
									processMutiPWS(pwTrailID,cpwe3,pwmap,cpwes,cpwe3_pws);
								} catch (Exception e) {
									logger.error(e, e);
								}
								continue;
							} else {
								getLogger().error("PW not found : pwe3=" + cpwe3.getDn());
								continue;
							}
                        }
                        String parentID = pwTrail.getParentDn();
                        if (parentID == null) {
                            getLogger().error("PW parentDn is null : pw=" + pwTrailID);
                            continue;
                        }
						FlowDomainFragment opwTrail = pwmap.get(parentID);
                        if (opwTrail != null) {
                            // parentID = opwTrail.getParentDn();
                            pwTrail = opwTrail;
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
                        cpw.setAvlanId(cpwe3.getAvlanId());
                        cpw.setZvlanId(cpwe3.getZvlanId());

                        cpw.setSid(DatabaseUtil.nextSID(cpw));
                        cpwes.add(cpw);

                        CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
                        cpwe3_pws.add(cpwe3_pw);
                    } else if (cpwe3.getFdfrType().equals(FDFRT_MULTIPOINT)) {
                        String[] aends = cpwe3.getAend().split(Constant.listSplitReg);
                        String[] vlans = fdf.getTag3().split(Constant.listSplitReg);
                        Map<String, String> vlanmap = new HashMap<String, String>();
                        if (aends.length == vlans.length) {
                            for (int i = 0; i < aends.length; i++) {
                                String aptp = aends[i];
                                if (aptp.contains("@CTP")) {
                                    aptp = aptp.substring(0, aptp.indexOf("@CTP"));
                                }
                                vlanmap.put(aptp, vlans[i]);
                            }
                        } else {
                            getLogger().error("Error vlans: pwe3=" + cpwe3.getDn());
                        }
                        String[] aptps = cpwe3.getAptps().split(Constant.listSplitReg);
                      //  Map<String, String> ptpmap = new HashMap<String, String>();
						HashMap<String, List<String>> nePtpsmap = new HashMap<String,  List<String>>();
                        for (String ptp : aptps) {
                            // TODO
//                            ptpmap.put(ptp2ne(ptp), ptp);
//                            ptpmap.put(ptp, ptp);

							DSUtil.putIntoValueList(nePtpsmap,ptp2ne(ptp),ptp);

                        }

						HashMap<String,Object> ptpUsed = new HashMap<String, Object>();

                        String[] pwids = pwTrailID.split(",");
                        if (pwTrailID.contains("|"))
                        	pwids = pwTrailID.split("\\|");
                        for (String pwid : pwids) {
                            FlowDomainFragment pwTrail = pwmap.get(pwid);
                            if (pwTrail == null) {
                                getLogger().error("无法找到对应的pwTrail,pwid="+pwid);
                                continue;
                            }
                            String pwaptp = pwTrail.getaPtp();
                            String pwzptp = pwTrail.getzPtp();
//                            if (pwaptp.contains("FTP")) {
//                                pwaptp = pwaptp.replace("FTP", "PTP");
//                            }
//                            if (pwzptp.contains("FTP")) {
//                                pwzptp = pwzptp.replace("FTP", "PTP");
//                            }
//                            String aptp = ptpmap.get(pwaptp);
//                            if (aptp == null) {
//                                aptp = ptpmap.get(ptp2ne(pwaptp));
//                            }
//                            String zptp = ptpmap.get(pwzptp);
//                            if (zptp == null) {
//                                zptp = ptpmap.get(ptp2ne(pwzptp));
//                            }

							List<String> apwptps = nePtpsmap.get(ptp2ne(pwaptp));
							List<String> zpwptps = nePtpsmap.get(ptp2ne(pwzptp));

							boolean find = false;
							for (String aptp : apwptps) {
								for (String zptp : zpwptps) {

									// 靠网元去匹配aptps里的端口，会匹配出多个，其实已经对应了多个同名pw，故只随机取第一个，剩下的让
									//下一个pw匹配该端口。
									// 如果没这个逻辑，本来是1:2的，会出现四条pw，其中两两重复。
									if (ptpUsed.containsKey(aptp+"<>"+zptp) || ptpUsed.containsKey(zptp+"<>"+aptp))
										continue;
									CPW cpw = transCPW(pwTrail, aptp, zptp);
									cpw.setAvlanId(vlanmap.get(aptp));
									cpw.setZvlanId(vlanmap.get(zptp));

									cpw.setSid(DatabaseUtil.nextSID(cpw));
									cpwes.add(cpw);

									CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
									cpwe3_pws.add(cpwe3_pw);

									ptpUsed.put(aptp+"<>"+zptp,cpw);
									ptpUsed.put(zptp+"<>"+aptp,cpw);
									find = true;
									break;
								}
								if (find) break;
							}


                        }
                    }
                }
            }

            // PW-TUNNEL
            Collection<FlowDomainFragment> pws = pwmap.values();
            int index = 0;
            if (pws != null && pws.size() > 0) {
                for (FlowDomainFragment pw : pws) {
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
                        FlowDomainFragment opwTrail = pwmap.get(parentDn);
                        if (opwTrail != null) {
                            parentDn = opwTrail.getParentDn();
                        }

                        FlowDomainFragment tunnelTrail = tunnelmap.get(parentDn);
                        if (tunnelTrail == null) {
                            getLogger().error("TunnelTrail not found : pw=" + pw.getDn());
                            continue;
                        }
                        CPW_Tunnel cpw_tunnel = transCPW_Tunnel(pw.getEmsName(), pw.getDn(), tunnelTrail.getDn());
                        cpw_tunnels.add(cpw_tunnel);
                    } catch (Exception e) {
                        getLogger().error("ERROR PW : pw=" + pw.getDn(), e);
                    }
                }
            }

            flowDomainFragments.clear();
            tunnelmap.clear();
            pwmap.clear();
            pwe3s.clear();
        } catch (Exception e) {
            getLogger().error(e, e);
        }
        DataInserter di = new DataInserter(emsid);

        removeDuplicateDN(cpwe3s);
        removeDuplicateDN(cpwes);
        removeDuplicateDN(cpwe3_pws);
        removeDuplicateDN(cTunnels);
        removeDuplicateDN(cpw_tunnels);

        di.insert(cpwe3s);
        di.insert(cpwes);
        di.insert(cpwe3_pws);
        di.insert(cTunnels);
        di.insert(cpw_tunnels);

        di.end();

		// //////////////////// migrate route //////////////////////////////
		// List<IPRoute> ipRoutes = sd.queryAll(IPRoute.class);
		// List<Section> sections = sd.queryAll(Section.class);
		// HashMap<String, Section> sectionAend = new HashMap<String, Section>();
		// HashMap<String, Section> sectionZend = new HashMap<String, Section>();
		// HashSet section_tunnel = new HashSet();
		// for (int i = 0; i < sections.size(); i++) {
		// Section section = sections.get(i);
		// sectionAend.put(section.getaEndTP(), section);
		// sectionZend.put(section.getzEndTP(), section);
		// }
		//
		// if (ipRoutes != null && ipRoutes.size() > 0) {
		// for (int i = 0; i < ipRoutes.size(); i++) {
		// IPRoute ipRoute = ipRoutes.get(i);
		// CRoute cRoute = new CRoute();
		// cRoute.setSid(DatabaseUtil.nextSID(CRoute.class));
		// cRoute.setDn(SysUtil.nextDN());
		// cRoute.setEmsName(emsdn);
		// cRoute.setEmsid(emsid);
		// cRoute.setTunnelDn(trafficTrunkDnMap.get(ipRoute.getTrafficTrunkDn()));
		// if (!map309Dn.containsKey(cRoute.getTunnelDn())) {
		// // 可能是业务路由，先不管
		// continue;
		// }
		// cRoute.setTunnelId(DatabaseUtil.getSID(CTunnel.class, cRoute.getTunnelDn()));
		// // EMS:JH-OTNM2000-1-PTN@ManagedElement:134217732;66050@FTP:/rack=1025/shelf=1/slot=7341062/port=1@CTP:/t_mpls=100
		// String aend = ipRoute.getAend();
		// if (aend.contains("@CTP")) {
		// String aptp = aend.substring(0, aend.indexOf("@CTP"));
		// cRoute.setAptp(aptp);
		// cRoute.setAptpId(DatabaseUtil.getSID(CPTP.class, aptp));
		// }
		//
		// String zend = ipRoute.getAend();
		// if (zend.contains("@CTP")) {
		// String zptp = zend.substring(0, aend.indexOf("@CTP"));
		// cRoute.setZptp(zptp);
		// cRoute.setZptpId(DatabaseUtil.getSID(CPTP.class, zptp));
		// }
		//
		// cRoute.setAend(ipRoute.getAend());
		// cRoute.setZend(ipRoute.getZend());
		// cRoute.setCollectTimepoint(ipRoute.getCreateDate());
		//
		// di.insert(cRoute);
		//
		// // ip route 只保存网元内交叉，可以根据PTP，获取到section
		// String ptp = null;
		// if (ipRoute.getAend().contains("@PTP"))
		// ptp = cRoute.getAptp();
		// else if (ipRoute.getAend().contains("@PTP"))
		// ptp = cRoute.getZptp();
		//
		// if (ptp != null) {
		// Section section = sectionZend.get(ptp);
		// if (section == null)
		// section = sectionAend.get(ptp);
		// if (section != null) {
		// CTunnel_Section ts = new CTunnel_Section();
		// ts.setDn(SysUtil.nextDN());
		// ts.setEmsName(emsdn);
		// ts.setTunnelDn(cRoute.getTunnelDn());
		// ts.setTunnelId(DatabaseUtil.getSID(CTunnel.class, cRoute.getTunnelDn()));
		// ts.setSectionDn(section.getDn());
		// ts.setSectionId(DatabaseUtil.getSID(CSection.class, section.getDn()));
		//
		// String key = ts.getSectionDn() + "@" + ts.getTunnelDn();
		//
		// if (!section_tunnel.contains(key)) {
		// di.insert(ts);
		// section_tunnel.add(key);
		// }
		//
		// }
		// }
		// }
		// }

		//
		// List<R_TrafficTrunk_CC_Section> rtcs = sd.queryAll(R_TrafficTrunk_CC_Section.class);
		// if (rtcs == null || rtcs.isEmpty())
		// throw new EMSDataTableEmptyException("Table empty : R_TrafficTrunk_CC_Section");
		// // HashMap<String,List> trafficTrunkRouteMap = new HashMap<String, List>();
		// for (int i = 0; i < rtcs.size(); i++) {
		// R_TrafficTrunk_CC_Section r_trafficTrunk_cc_section = rtcs.get(i);
		// String trafficTrunDn = r_trafficTrunk_cc_section.getTrafficTrunDn();
		// // List routes = trafficTrunkRouteMap.get(trafficTrunDn);
		// // if (routes == null) {
		// // routes = new ArrayList();
		// // trafficTrunkRouteMap.put(trafficTrunDn,routes);
		// // }
		// // routes.add(r_trafficTrunk_cc_section);
		//
		//
		// CRoute cRoute = new CRoute();
		// cRoute.setDn(SysUtil.nextDN());
		// cRoute.setTunnelDn(trafficTrunDn);
		// cRoute.setEntityType(r_trafficTrunk_cc_section.getType());
		// if (r_trafficTrunk_cc_section.getCcOrSectionDn() != null && !r_trafficTrunk_cc_section.getCcOrSectionDn().isEmpty())
		// cRoute.setEntityDn(r_trafficTrunk_cc_section.getCcOrSectionDn());
		//
		// cRoute.setAptp(r_trafficTrunk_cc_section.getaPtp());
		// cRoute.setZptp(r_trafficTrunk_cc_section.getzPtp());
		// cRoute.setAend(r_trafficTrunk_cc_section.getaEnd());
		// cRoute.setZend(r_trafficTrunk_cc_section.getzEnd());
		// cRoute.setCollectTimepoint(r_trafficTrunk_cc_section.getCreateDate());
		//
		// jpaInsertHelper.insertBObject(cRoute);
		//
		//
		// }

	}

	private String  findAptp (FlowDomainFragment pw,CPWE3 cpwe3) {
		String aptpstr = cpwe3.getAptp();
		String zptpstr = cpwe3.getZptp();

		if (aptpstr != null) {
			String[] aptps = aptpstr.split(Constant.listSplitReg);
			for (String aptp : aptps) {
				if (ptp2ne(aptp).equals(ptp2ne(pw.getaPtp())))
					return aptp;
			}
		}

		if (zptpstr != null) {
			String[] aptps = zptpstr.split(Constant.listSplitReg);
			for (String aptp : aptps) {
				if (ptp2ne(aptp).equals(ptp2ne(pw.getaPtp())))
					return aptp;
			}
		}
		return null;
	}

	private String  findZptp (FlowDomainFragment pw,CPWE3 cpwe3) {
		String aptpstr = cpwe3.getAptp();
		String zptpstr = cpwe3.getZptp();

		if (aptpstr != null) {
			String[] aptps = aptpstr.split(Constant.listSplitReg);
			for (String aptp : aptps) {
				if (ptp2ne(aptp).equals(ptp2ne(pw.getzPtp())))
					return aptp;
			}
		}

		if (zptpstr != null) {
			String[] aptps = zptpstr.split(Constant.listSplitReg);
			for (String aptp : aptps) {
				if (ptp2ne(aptp).equals(ptp2ne(pw.getzPtp())))
					return aptp;
			}
		}
		return null;
	}

	private void processMutiPWS(String pwIds,CPWE3 cpwe3,HashMap<String, FlowDomainFragment> pwmap,List cpwes,List cpwe3_pws) {
		String[] pwids = pwIds.split(",");
		if (pwIds.contains("|"))
			pwids = pwIds.split("\\|");
		for (String pwTrailID : pwids) {
			FlowDomainFragment pwTrail = pwmap.get(pwTrailID);
			if (pwTrail == null) {
				getLogger().error("faild to find pw=" + pwTrailID);
				continue;
			}
			String parentID = pwTrail.getParentDn();
			if (parentID == null) {
				getLogger().error("PW parentDn is null : pw=" + pwTrailID);
				continue;
			}
			FlowDomainFragment opwTrail = pwmap.get(parentID);
			if (opwTrail != null) {
				// parentID = opwTrail.getParentDn();
				pwTrail = opwTrail;
			}
			String aptp = pwTrail.getaPtp();
			String zptp = pwTrail.getzPtp();

			boolean a_long = false;
			boolean z_long = false;
			if (cpwe3.getAptp() != null && cpwe3.getAptp().isEmpty() && cpwe3.getZx_aptps() != null) {
				cpwe3.setAptp(cpwe3.getZx_aptps());
				a_long = true;
			}
			if (cpwe3.getZptp() != null && cpwe3.getZptp().isEmpty() && cpwe3.getZx_zptps() != null) {
				cpwe3.setZptp(cpwe3.getZx_zptps());
				z_long = true;
			}

			if (cpwe3.getAptp() != null && cpwe3.getZptp() != null && cpwe3.getAptp().contains("@")&& cpwe3.getZptp().contains("@")) {
				aptp = findAptp(pwTrail,cpwe3);
				zptp = findZptp(pwTrail, cpwe3);

				if (aptp == null) aptp =  pwTrail.getaPtp();
				if (zptp == null) zptp =  pwTrail.getzPtp();
			}

			if (a_long)
				cpwe3.setAptp("");
			if (z_long)
				cpwe3.setZptp("");

			CPW cpw = transCPW(pwTrail, aptp, zptp);
			cpw.setAvlanId(cpwe3.getAvlanId());
			cpw.setZvlanId(cpwe3.getZvlanId());

			cpw.setSid(DatabaseUtil.nextSID(cpw));
			cpwes.add(cpw);

			CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
			cpwe3_pws.add(cpwe3_pw);

		}

	}

	private String end2ptpDn(String end) {

		if (end.indexOf("port=") > 0 && end.indexOf("@", end.indexOf("port=")) > 0)
			end = end.substring(0, end.indexOf("@", end.indexOf("port=")));

		if (!end.contains("EMS")) {
			try {
				end = MigrateUtil.simpleDN2FullDn(end, new String[] { "EMS", "ManagedElement", "PTP" });
			} catch (IllegalDNStringException e) {
				LogUtil.error(getClass(), e, e);
			}
		}
		return end;
	}

	private CPW transCPW(FlowDomainFragment pw, String aptp, String zptp) {
		CPW cpw = transPW(pw);
		cpw.setAptp(aptp);
		cpw.setZptp(zptp);
        if (cpw.getAptp() != null)
		    cpw.setAptpId(DatabaseUtil.getSID(CPTP.class, cpw.getAptp()));
        else {
            getLogger().error("APTP is null: "+pw.getDn());
        }
        if (cpw.getZptp() != null)
		    cpw.setZptpId(DatabaseUtil.getSID(CPTP.class, cpw.getZptp()));
        else {
            getLogger().error("ZPTP is null: "+pw.getDn());
        }
        if (pw.getTag1() != null) {
            String tag1 = pw.getTag1();
            if (tag1 .equals("NBI_PGT_NONE")) {
                cpw.setaWorkingMode("Working");
                cpw.setzWorkingMode("Working");
            } else  {
                cpw.setzWorkingMode(tag1);
            }
        }  else {
            cpw.setaWorkingMode("Working");
            cpw.setzWorkingMode("Working");
        }
        cpw.setTag1(pw.getTag1());
		return cpw;
	}

	private HashMap<String,Integer> pwDnMap = new HashMap<String, Integer>();

	public CPW transPW(FlowDomainFragment src) {
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
		des.setAdministrativeState(src.getAdministrativeState());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		des.setTransmissionParams(src.getTransmissionParams());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());
        des.setTag1(src.getTag1());
		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndtrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());

		String transmissionParams = des.getTransmissionParams();

		HashMap<String, String> tt = MigrateUtil.transMapValue(transmissionParams);

		String cir = tt.get("IngressCIR");
		if (cir != null && !cir.equals("0"))
			des.setCir(cir + "M");
		String pir = tt.get("IngressPIR");
		if (pir != null && !pir.equals("10000"))
			des.setPir(pir + "M");

		return des;
	}

	public CTunnel transTunnel(FlowDomainFragment src) {
		CTunnel des = new CTunnel();
		des.setDn(src.getDn());
		des.setSid(DatabaseUtil.nextSID(des));
		des.setCollectTimepoint(src.getCreateDate());
		des.setAdministrativeState(src.getAdministrativeState());
		des.setActiveState(src.getFdfrState());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		des.setTransmissionParams(src.getTransmissionParams());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());
        des.setTag1(src.getTag1());
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
		String transmissionParams = des.getTransmissionParams();

		HashMap<String, String> at = MigrateUtil.transMapValue(aendTrans);
		HashMap<String, String> zt = MigrateUtil.transMapValue(zendtrans);
		HashMap<String, String> tt = MigrateUtil.transMapValue(transmissionParams);
		des.setAegressLabel(at.get("EgressLabel"));
		des.setAingressLabel(at.get("IngressLabel"));

		des.setZegressLabel(zt.get("EgressLabel"));
		des.setZingressLabel(zt.get("IngressLabel"));
		String cir = tt.get("IngressCIR");
		if (cir != null && !cir.equals("0"))
			des.setCir(cir + "M");
		String pir = tt.get("IngressPIR");
		if (pir != null && !pir.equals("10000"))
			des.setPir(pir + "M");

		return des;
	}

	@Override
	public CPWE3 transFDF(FlowDomainFragment src) {
		CPWE3 des = new CPWE3();
		// des.setDn(src.getaEnd() + "||" + src.getzEnd());
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
        des.setTag1(src.getTag1());
		String aendTrans = des.getAendTrans();
		String zendtrans = des.getZendtrans();
		String transmissionParams = des.getTransmissionParams();

		HashMap<String, String> at = MigrateUtil.transMapValue(aendTrans);
		HashMap<String, String> zt = MigrateUtil.transMapValue(zendtrans);
		HashMap<String, String> tt = MigrateUtil.transMapValue(transmissionParams);

		String cir = tt.get("IngressCIR");
		if (cir != null && !cir.equals("0"))
			des.setCir(cir + "M");
		String pir = tt.get("IngressPIR");
		if (pir != null && !pir.equals("10000"))
			des.setPir(pir + "M");

		String avid = at.get("VLANID");
		String zvid = zt.get("VLANID");
		des.setAvlanId(avid);
		des.setZvlanId(zvid);
		return des;
	}
	private boolean slotNoHex = false;
	@Override
	public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
		CdcpObject eh = super.transEquipmentHolder(equipmentHolder);
		// if (eh instanceof CShelf) {
		// String additionalInfo = equipmentHolder.getAdditionalInfo();
		// String shelfType = transMapValue(additionalInfo).get("DetailKind");
		// if (shelfType != null) {
		// ((CShelf) eh).setShelfType(shelfType);
		// }
		// }
		if (eh instanceof CRack) {
			String nativeEMSName = equipmentHolder.getNativeEMSName();
			if (nativeEMSName.contains("架"))
				((CRack) eh).setNo(nativeEMSName.substring(nativeEMSName.indexOf("架") + 1));
		}
		if (eh instanceof CSlot) {
			String nativeEMSName = equipmentHolder.getNativeEMSName();
			String slotNo = null;
			HashMap<String, String> add = MigrateUtil.transMapValue(equipmentHolder.getAdditionalInfo());
			//Sequence:0X1c||
			if (add.containsKey("Sequence") && add.get("Sequence") != null && add.get("Sequence").trim().length() > 0)  {
				String sequence = add.get("Sequence");
				if (sequence.contains("0X")) {
					slotNo = sequence.substring(sequence.indexOf("0X") + 2);
					slotNoHex = true;
				}
				else if (sequence.contains("0x")) {
					slotNo = sequence.substring(sequence.indexOf("0x") + 2);
					slotNoHex = true;
				} else {
					slotNo = sequence;
				}
			}
		    else {
				// SLOT_0X08

				if (nativeEMSName.contains("SLOT_0X")) {
					slotNo = nativeEMSName.substring(nativeEMSName.indexOf("SLOT_0X") + 7);
					slotNoHex = true;
//				((CSlot) eh).setNo(Integer.parseInt(slotNo, 16) + "");

				} else if (nativeEMSName.contains("SLOT_0x")) {
					slotNo = nativeEMSName.substring(nativeEMSName.indexOf("SLOT_0x") + 7);
					slotNoHex = true;
//				((CSlot) eh).setNo(Integer.parseInt(slotNo, 16) + "");
				//	((CSlot) eh).setNo(slotNo);
				} else if (nativeEMSName.contains("SLOT_")) {
					slotNo = nativeEMSName.substring(nativeEMSName.indexOf("SLOT_") + 5);
//				((CSlot) eh).setNo(Integer.parseInt(slotNo, 16) + "");
				//	((CSlot) eh).setNo(slotNo);
				}
			}

			((CSlot) eh).setNo(slotNo);


		}
		return eh;
	}

	protected String getShelfType(String equipmentHolderDN, String additionalInfo) {
		String shelfType = transMapValue(additionalInfo).get("DetailKind");
		if (shelfType != null && shelfType.length() > 0) {
			return shelfType;
		}
		return super.getShelfType(equipmentHolderDN, additionalInfo);
	}

	@Override
	public CPTP transPTP(PTP ptp) {
		CPTP cptp = new CPTP();

		String dn = ptp.getDn();
		cptp.setDn(dn);

		if (dn.contains("slot")) {
			if (dn.contains("rack") && dn.contains("/port=")) {
				String slot = dn.substring(dn.indexOf("/rack"), dn.indexOf("/port="));
				String me = dn.substring(0, dn.lastIndexOf("@"));
				String carddn = me + "@EquipmentHolder:" + slot + "@Equipment:1";
				if (slot.toLowerCase().contains("slot")) {
					cptp.setParentDn(carddn);
					cptp.setCardid(DatabaseUtil.getSID(CEquipment.class, carddn));
				}
			}
		}
		if (cptp.getParentDn() == null || cptp.getParentDn().isEmpty()) {
			cptp.setParentDn(ptp.getParentDn());
		}
		if (dn.contains("port=")) {
			cptp.setNo(dn.substring(dn.lastIndexOf("port=") + 5));
		}
		cptp.setCollectTimepoint(ptp.getCreateDate());
		cptp.setEdgePoint(ptp.isEdgePoint());
		cptp.setType(ptp.getType());
		cptp.setConnectionState(ptp.getConnectionState());
		cptp.setTpMappingMode(ptp.getTpMappingMode());
		cptp.setDirection(DicUtil.getPtpDirection(ptp.getDirection()));
		cptp.setTransmissionParams(ptp.getTransmissionParams());
		cptp.setLayerRates(ptp.getRate());
		cptp.setTpProtectionAssociation(ptp.getTpProtectionAssociation());
		cptp.setEmsName(ptp.getEmsName());
		cptp.setUserLabel(ptp.getUserLabel());
		cptp.setNativeEMSName(ptp.getNativeEMSName());
		cptp.setOwner(ptp.getOwner());
		cptp.setAdditionalInfo(ptp.getAdditionalInfo());
		// cptp.setTag1(ptp.getTag1());

		// String temp = cptp.getTag1();
		// if (temp.startsWith("EMS:"))
		// temp = temp.substring(4);
		// if (temp.contains("@PTP"))
		// temp = temp.substring(0,temp.indexOf("@PTP"));
		// else if (temp.contains("@FTP"))
		// temp = temp.substring(0,temp.indexOf("@FTP"));
		// temp = temp.replaceAll("ManagedElement:","");

		cptp.setDeviceDn(ptp.getParentDn());

		// Map<String, String> map = transMapValue(ptp.getTransmissionParams());
		// Map<String, String> map2 = new HashMap<String, String>();
		// Iterator<String> iterator = map.keySet().iterator();
		// String layerrate = null;
		// while (iterator.hasNext()) {
		// String next = iterator.next();
		// String value = map.get(next);
		// if (next.contains("@"))
		// next = next.substring(next.indexOf("@") + 1);
		// map2.put(next, value);
		// }
		// cptp.setPortMode(map2.get("PortMode"));
		// cptp.setPortRate(map2.get("AdministrativeSpeedRate"));
		// cptp.setWorkingMode(map2.get("WorkingMode"));
		// cptp.setMacAddress(map2.get("MACAddress"));
		// cptp.setIpAddress(map2.get("IPAddress"));
		// cptp.setIpMask(map2.get("IPMask"));
		// String transmissionParams = ptp.getTransmissionParams();
		// HashSet lr = new HashSet();
		// if (transmissionParams.contains("@")) {
		// layerrate = transmissionParams.substring(0, transmissionParams.indexOf("@"));
		// lr.add(layerrate);
		// }
		// if (!lr.isEmpty()) {
		// Iterator iterator1 = lr.iterator();
		// StringBuffer sb = new StringBuffer();
		// while (iterator1.hasNext()) {
		// Object next = iterator1.next();
		// Integer fhlr = Integer.parseInt(next.toString());
		// Integer sysvalue = FHDic.getMappedValue(DicConst.DIC_LAYER_RATE, fhlr);
		// sb.append(sysvalue).append("||");
		// }
		// cptp.setLayerRates(sb.toString());
		// }
		cptp.setEoType(DicUtil.getEOType(cptp.getLayerRates()));
		cptp.setSpeed(DicUtil.getSpeed(cptp.getLayerRates()));
		// HashMap<String, String> addMap = transMapValue(ptp.getAdditionalInfo());
		// if (addMap.get("SupportedPortType") != null && addMap.get("SupportedPortType").contains("Optical"))
		// cptp.setEoType(DicConst.EOTYPE_OPTIC);

		// cptp.setType(addMap.get("EntityClass"));
		cptp.setType(DicUtil.getPtpType(dn, cptp.getLayerRates()));
		return cptp; // To change body of created methods use File | Settings | File Templates.
	}

	public static void main2(String[] args) {
		// String sp = "\\|\\|";
		// System.out.println(sp);
		// String[] split = "abc||".split("\\|\\|");
		printSetMethods("src", ProtectionGroup.class, "des", CProtectionGroup.class);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(System.getProperty("java.class.path"));//系统的classpaht路径
		System.getProperties().list(System.out);
//		String nativeEMSName = "SLOT_0X13";
//		if (nativeEMSName.contains("SLOT_0X")) {
//			String slotNo = nativeEMSName.substring(nativeEMSName.indexOf("SLOT_0X") + 7);
//			System.out.println(Integer.parseInt(slotNo, 16) + "");
//		}
//
//		String s = "架1";
//		System.out.println(s.substring(s.indexOf("架") + 1));
		String[] locations = { "appserver-spring.xml" };
		ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
		FHOTNM2000Migrator loader = new FHOTNM2000Migrator("F:\\cdcpdb\\2015-09-10-170001-JX-OTNM2000-1-PTN-DayMigration.db",
				"LS-OTNM2000-1-PTN");
		loader.execute();

	}

	private static void printSetMethods(String srcName, Class srcCls, String desName, Class desClass) {
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

	private HashMap<String, String> transMapValue(String value) {
		HashMap map = new HashMap();
		String[] pairs = value.split("\\|\\|");
		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			if (pair.contains(Constant.namevalueSplit)) {
				String[] split = pair.split(Constant.namevalueSplit);
				if (split != null && split.length == 2) {
					map.put(split[0], split[1]);
				}
			}
		}
		return map;
	}

}
