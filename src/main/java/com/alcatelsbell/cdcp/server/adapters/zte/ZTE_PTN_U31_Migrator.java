package com.alcatelsbell.cdcp.server.adapters.zte;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.server.adapters.*;
import com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000.FHOTNM2000SDHMigrator;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWDic;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HwDwdmUtil;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.DBUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.*;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.FileLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 14-8-29
 * Time: 下午2:00
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class
ZTE_PTN_U31_Migrator extends AbstractDBFLoader {
    HashMap<String,List<CCTP>> ctpParentChildMap = new HashMap<String, List<CCTP>>();

    HashMap<String,CPTP>  ptpMap = new HashMap<String, CPTP>();
    HashMap<String,CCTP>  ctpMap = new HashMap<String, CCTP>();
    HashMap<String,List<CCrossConnect>> aptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CCrossConnect>> ptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CSection>> ptpSectionMap = new HashMap<String, List<CSection>>();
    HashMap<String,List<CCTP>> ptp_ctpMap = new HashMap<String, List<CCTP>>();
    List<CSection> cSections = new ArrayList<CSection>();
    HashMap<String,CEquipment> equipmentMap = new HashMap<String, CEquipment>();

    HashMap<String,CSection> cSectionMap = new HashMap<String, CSection>();

    public ZTE_PTN_U31_Migrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog(emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }
    private static FileLogger logger = new FileLogger("ZTE-OTN-Device.log");
    public ZTE_PTN_U31_Migrator(Serializable object, String emsdn) {
        this.emsdn = emsdn;
        this.resultObject = object;
        MigrateThread.thread().initLog(logger);
    }
    private List<CSlot> makeupSlots = new ArrayList<CSlot>();

    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "中兴");

        logAction(emsdn + " migrateManagedElement", "同步网元", 1);
        migrateManagedElement();
        migrateSubnetwork();
//

        logAction("migrateEquipmentHolder", "同步槽道", 5);
        migrateEquipmentHolder();


//
        logAction("migrateEquipment", "同步板卡", 10);
        migrateEquipment();
        logAction("migratePTP", "同步端口", 20);
        migratePTP();

        migrateSection();

        logAction("migrateCTP", "同步CTP", 25);
        migrateCTP();


        migrateCC();

    //    makeupCTP_CC_SECTION();


        logAction("migrateFlowDomainFragment", "同步业务", 25);
        migrateFlowDomainFragment();

        try {
            DataInserter di = new DataInserter(emsid);
            di.insertWithDupCheck(makeupSlots);
            di.end();
        } catch (Exception e) {
            logger.error(e, e);
        }


        sd.release();


//        logAction("migrateSection", "同步段", 25);
//        migrateSection();
//
//        logAction("migrateCTP", "同步CTP", 30);
//        migrateCTP();
//          migrateSubnetworkConnection();
    }

    private String transSncDn(String src) {
        String s = src.replaceAll("\\{", ":").replaceAll("\\}", "@");
        if (s.endsWith("@"))
            s = s.substring(0,s.length()-1);
        if (s.endsWith("@;"))
            s = s.substring(0,s.length()-2);
        return s;
    }
    private void migrateFlowDomainFragment() throws Exception {
        executeDelete("delete from CPWE3 c where c.emsName = '" + emsdn + "'", CPWE3.class);
        executeDelete("delete from CPW c where c.emsName = '" + emsdn + "'", CPW.class);
        executeDelete("delete from CPWE3_PW c where c.emsName = '" + emsdn + "'", CPWE3_PW.class);
        executeDelete("delete from CTunnel c where c.emsName = '" + emsdn + "'", CTunnel.class);
        executeDelete("delete from CTunnel_Section c where c.emsName = '" + emsdn + "'", CTunnel_Section.class);
        executeDelete("delete from CPW_Tunnel c where c.emsName = '" + emsdn + "'", CPW_Tunnel.class);
        // executeDelete("delete  from CTunnel_Section c where c.emsName = '" + emsdn + "'", CTunnel_Section.class);
        // executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
        // HashMap<String, String> trafficTrunkDnMap = new HashMap<String, String>();

        HashMap<String,List> trafficTrunkRouteMap = new HashMap<String, List>();
         List<R_TrafficTrunk_CC_Section> rtcs = sd.queryAll(R_TrafficTrunk_CC_Section.class);
         if (rtcs == null || rtcs.isEmpty())
          rtcs = new ArrayList<R_TrafficTrunk_CC_Section>();
         // HashMap<String,List> trafficTrunkRouteMap = new HashMap<String, List>();
        for (int i = 0; i < rtcs.size(); i++) {
            R_TrafficTrunk_CC_Section r_trafficTrunk_cc_section = rtcs.get(i);
            String trafficTrunDn = r_trafficTrunk_cc_section.getTrafficTrunDn();
            List routes = trafficTrunkRouteMap.get(trafficTrunDn);
            if (routes == null) {
                routes = new ArrayList();
                trafficTrunkRouteMap.put(trafficTrunDn, routes);
            }
            routes.add(r_trafficTrunk_cc_section);
        }




        List<CPWE3> cpwe3s = new ArrayList<CPWE3>();
        List<CPW> cpwes = new ArrayList<CPW>();
        List<CPWE3_PW> cpwe3_pws = new ArrayList<CPWE3_PW>();
        List<CTunnel> cTunnels = new ArrayList<CTunnel>();
        List<CPW_Tunnel> cpw_tunnels = new ArrayList<CPW_Tunnel>();
        List<CTunnel_Section> cTunnelSections = new ArrayList<CTunnel_Section>();

        HashMap<String, SubnetworkConnection> tunnelmap = new HashMap<String, SubnetworkConnection>();
        HashMap<String, SubnetworkConnection> pwmap = new HashMap<String, SubnetworkConnection>();
        List<FlowDomainFragment> pwe3s = new ArrayList<FlowDomainFragment>();

        List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
        for (SubnetworkConnection snc : sncs) {
            snc.setaPtp(DNUtil.extractPortDn(snc.getaEnd()));
            snc.setzPtp(DNUtil.extractPortDn(snc.getzEnd()));
            String additionalInfo = snc.getAdditionalInfo();
            HashMap<String, String> map = MigrateUtil.transMapValue(additionalInfo);
            String belongSNC = map.get("BelongSNC");

            if (belongSNC == null) {
                tunnelmap.put(snc.getDn(),snc);
            } else {
                String parentdn = transSncDn(belongSNC);
                snc.setParentDn(parentdn);
                pwmap.put(snc.getDn(), snc);
            }
        }

        List<FlowDomainFragment> flowDomainFragments = sd.queryAll(FlowDomainFragment.class);
        for (FlowDomainFragment fdfr : flowDomainFragments) {
            fdfr.setaPtp(DNUtil.extractPortDn(fdfr.getaEnd()));
            fdfr.setzPtp(DNUtil.extractPortDn(fdfr.getzEnd()));

            pwe3s.add(fdfr);
        }

        try {


            Collection<SubnetworkConnection> tunnels = tunnelmap.values();
            for (Iterator<SubnetworkConnection> iterator = tunnels.iterator(); iterator.hasNext();) {
                SubnetworkConnection tunnel = iterator.next();
                CTunnel cTunnel = transTunnel(tunnel);

                List<R_TrafficTrunk_CC_Section> routes = trafficTrunkRouteMap.get(cTunnel.getDn());

                for (R_TrafficTrunk_CC_Section route : routes) {
                    if ("SECTION".equals(route.getType())) {
                        CTunnel_Section cTunnel_section = new CTunnel_Section();
                        cTunnel_section.setDn(SysUtil.nextDN());
                        cTunnel_section.setSectionDn(route.getCcOrSectionDn());
                        cTunnel_section.setTunnelDn(cTunnel.getDn());
                        cTunnel_section.setEmsName(emsdn);
                        cTunnelSections.add(cTunnel_section);
                    }

                }
                cTunnels.add(cTunnel);
            }
            getLogger().info("tunnel size = " + tunnelmap.size());
            getLogger().info("pw size = " + pwmap.size());
            getLogger().info("pwe3 size = " + pwe3s.size());


            HashSet<String> usedPWs = new HashSet<String>();

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

                    String additionalInfo = fdf.getAdditionalInfo();
                    HashMap<String, String> map = MigrateUtil.transMapValue(additionalInfo);
                    String belongSNC = map.get("BelongSNC");
                    String pwTrailID = null;
                    if (belongSNC != null)
                        pwTrailID = transSncDn(belongSNC);
                    if (pwTrailID == null || pwTrailID.trim().isEmpty()) {
                        getLogger().error("PWE3 parentDn is null : pwe3=" + cpwe3.getDn());
                        continue;
                    }

                    if (true) {
                        SubnetworkConnection pwTrail = pwmap.get(pwTrailID);
                        if (pwTrail != null) {
                            CPW cpw = transPW(pwTrail);
                            cpw.setAvlanId(cpwe3.getAvlanId());
                            cpw.setZvlanId(cpwe3.getZvlanId());
                            cpw.setSid(DatabaseUtil.nextSID(cpw));
                            cpw.setParentDn(pwTrail.getParentDn());


                            cpwes.add(cpw);
                            CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
                            cpwe3_pws.add(cpwe3_pw);
                            usedPWs.add(pwTrailID);
                        }
                    }


                }
            }


            for (SubnetworkConnection pw : pwmap.values()) {
                if (!usedPWs.contains(pw.getDn())) {
                    CPW cpw = transPW(pw);
                    cpw.setParentDn(pw.getParentDn());


                    cpwes.add(cpw);
                }
            }

            // PW-TUNNEL
            Collection<SubnetworkConnection> pws = pwmap.values();
            int index = 0;
            if (pws != null && pws.size() > 0) {
                for (SubnetworkConnection pw : pws) {
                    if (pw.getDn().equals("EMS:ZJ-ZTE-1-PTN@MultiLayerSubnetwork:1@SubnetworkConnection:1303361120000001091"))
                        System.out.println();
                    try {
                        if (index++ % 1000 == 0)
                            getLogger().info("pws:" + index);

                        String parentDn = pw.getParentDn();
                        if (parentDn == null || parentDn.trim().isEmpty()) {
                            getLogger().error("PW parentDn is null : " + pw.getDn());
                            continue;
                        }

                            String[] parents = parentDn.split(";");


                        for (String _parentDn : parents) {
                            if (_parentDn.endsWith("@")) _parentDn = _parentDn.substring(0,_parentDn.length()-1);
                            SubnetworkConnection opwTrail = pwmap.get(_parentDn);
                            if (opwTrail != null) {
                                _parentDn = opwTrail.getParentDn();
                            }

                            SubnetworkConnection tunnelTrail = tunnelmap.get(_parentDn);
                            if (tunnelTrail == null) {
                                getLogger().error("TunnelTrail not found : pw=" + pw.getDn());
                                continue;
                            }
                            CPW_Tunnel cpw_tunnel = transCPW_Tunnel(pw.getEmsName(), pw.getDn(), tunnelTrail.getDn());
                            cpw_tunnels.add(cpw_tunnel);
                        }


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
        di.insert(cTunnelSections);

        di.end();
    }


    public CPW transPW(SubnetworkConnection src) {
        CPW des = new CPW();
        String dn = src.getDn();
//        if (pwDnMap.get(dn) != null) {
//            des.setDn(dn + "__"+ (pwDnMap.get(dn)+1));
//            pwDnMap.put(dn,(pwDnMap.get(dn)+1));
//        } else {
//            des.setDn(dn);
//            pwDnMap.put(dn,0);
//        }
        des.setDn(dn);
        des.setCollectTimepoint(src.getCreateDate());
        //des.setAdministrativeState(src.getAdministrativeState());
        des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
       // des.setTransmissionParams(src.getTransmissionParams());
        des.setAend(src.getaEnd());
        des.setZend(src.getzEnd());
        des.setTag1(src.getTag1());
        des.setAptp(src.getaPtp());
        des.setZptp(src.getzPtp());
        des.setAendTrans(src.getaEndTrans());
        des.setZendtrans(src.getzEndTrans());
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

    public CTunnel transTunnel(SubnetworkConnection src) {
        CTunnel des = new CTunnel();
        des.setDn(src.getDn());
        des.setSid(DatabaseUtil.nextSID(des));
        des.setCollectTimepoint(src.getCreateDate());
        //des.setAdministrativeState(src.getAdministrativeState());
        //des.setActiveState(src.getFdfrState());
        des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
        des.setTransmissionParams(src.getAdditionalInfo());
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
        des.setZendtrans(src.getzEndTrans());
        des.setParentDn(src.getParentDn());
        des.setEmsName(src.getEmsName());
        des.setUserLabel(src.getUserLabel());
        des.setNativeEMSName(src.getNativeEMSName());
        des.setOwner(src.getOwner());
        des.setAdditionalInfo(src.getAdditionalInfo());

        String aendTrans = des.getAendTrans();
        String zendtrans = des.getZendtrans();
        String transmissionParams = des.getTransmissionParams();

//        HashMap<String, String> at = MigrateUtil.transMapValue(aendTrans);
//        HashMap<String, String> zt = MigrateUtil.transMapValue(zendtrans);
//        HashMap<String, String> tt = MigrateUtil.transMapValue(transmissionParams);
//        des.setAegressLabel(at.get("EgressLabel"));
//        des.setAingressLabel(at.get("IngressLabel"));
//
//        des.setZegressLabel(zt.get("EgressLabel"));
//        des.setZingressLabel(zt.get("IngressLabel"));
//        String cir = tt.get("IngressCIR");
//        if (cir != null && !cir.equals("0"))
//            des.setCir(cir + "M");
//        String pir = tt.get("IngressPIR");
//        if (pir != null && !pir.equals("10000"))
//            des.setPir(pir + "M");

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



    List<R_TrafficTrunk_CC_Section> routeList = null;
    private void makeupCTP_CC_SECTION() {
        executeDelete("delete  from CCTP c where c.emsName = '" + emsdn + "' and c.tag1='MAKEUP_SECTION'", CCTP.class);
        routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
        List<CTP> newCCTPs = new ArrayList<CTP>();
        for (R_TrafficTrunk_CC_Section route : routeList) {
            if (route.getType().equals("CC")) {
                String ccDn = route.getCcOrSectionDn();
                String actpdn = route.getaEnd();
                String zctpdn = route.getzEnd();
                if (!DatabaseUtil.isSIDExisted(CCrossConnect.class,ccDn)) {
                    CCrossConnect cc = new CCrossConnect();
                    cc.setDn(ccDn);
                    cc.setAend(route.getaEnd());
                    cc.setZend(route.getzEnd());
                    cc.setAptp(route.getaPtp());
                    cc.setZptp(route.getzPtp());
                    cc.setDirection("CD_UNI");
                    cc.setEmsName(emsdn);




                }

                for (String ctpDn : new String[]{actpdn,zctpdn}) {
                    if (!DatabaseUtil.isSIDExisted(CCTP.class,ctpDn)) {
                        CTP ctp = new CTP();
                        ctp.setDn(ctpDn);
                        ctp.setParentDn(DNUtil.extractPortDn(ctpDn));
                        ctp.setEmsName(emsdn);
                        ctp.setTag1("MAKEUP_SECTION");
                        newCCTPs.add(ctp);
                    }
                }

            }
        }

        getLogger().info("new ctp size = "+newCCTPs.size());
        removeDuplicateDN(newCCTPs);
        try {
            insertCtps(newCCTPs);
        } catch (Exception e) {
            getLogger().error(e, e);
        }


    }


    protected Class[] getStatClss() {
        return new Class[]{CCrossConnect.class, CPWE3_Tunnel.class, CTunnel_Section.class,
                CPWE3_PW.class, CPW_Tunnel.class,
                CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class,
                CTunnel_Section.class, CPWE3.class,
                CPW.class, CTunnel.class, CSection.class, CCTP.class, CDevice.class, CPTP.class, CTransmissionSystem.class, CTransmissionSystem_Channel.class};
    }

    @Override
    protected void insertEquipmentHolders(List<EquipmentHolder> equipmentHolders) throws Exception {
        if (shelfTypeMap != null) {
            shelfTypeMap.clear();
            shelfTypeMap = null;
        }
        DataInserter di = new DataInserter(emsid);

        // // ////////////////// 将EH分类///////////////////
        List<EquipmentHolder> racks = new ArrayList<EquipmentHolder>();
        List<EquipmentHolder> shelfs = new ArrayList<EquipmentHolder>();
        List<EquipmentHolder> slots = new ArrayList<EquipmentHolder>();
        List<EquipmentHolder> subslots = new ArrayList<EquipmentHolder>();

        for (int i = 0; i < equipmentHolders.size(); i++) {
            EquipmentHolder equipmentHolder = equipmentHolders.get(i);


            // String dn = equipmentHolder.getDn();
            // String rack = CodeTool.extractValue(dn, "rack");
            // String shelf = CodeTool.extractValue(dn, "shelf");
            // String slot = CodeTool.extractValue(dn, "slot");
            // String subSlot = CodeTool.extractValue(dn, "sub_slot");

            // if (rack != null && shelf == null) {
            if (equipmentHolder.getHolderType().equals("1")) {
                equipmentHolder.setHolderType("rack");
                racks.add(equipmentHolder);
                // } else if (rack != null && shelf != null && slot == null) {
            } else if (equipmentHolder.getHolderType().equals("2")) {
                equipmentHolder.setHolderType("shelf");
                shelfs.add(equipmentHolder);
                // } else if (rack != null && shelf != null && slot != null) {
                // if (subSlot != null) {
                // subslots.add(equipmentHolder);
                // } else {
                // slots.add(equipmentHolder);
                // }
            } else if (equipmentHolder.getHolderType().equals("3")) {
                equipmentHolder.setHolderType("slot");
                slots.add(equipmentHolder);
            }
//            else if (equipmentHolder.getHolderType().equals("sub_slot")) {
//				subslots.add(equipmentHolder);
//			}
        }
        // ////////////////// 将EH分类///////////////////

        for (EquipmentHolder equipmentHolder : racks) {

            CdcpObject cEquipmentHolder = transEquipmentHolder(equipmentHolder);
            di.insert(cEquipmentHolder);
        }
        for (EquipmentHolder equipmentHolder : shelfs) {
            CdcpObject cEquipmentHolder = transEquipmentHolder(equipmentHolder);
            di.insert(cEquipmentHolder);
        }
        for (EquipmentHolder equipmentHolder : slots) {
            CdcpObject cEquipmentHolder = transEquipmentHolder(equipmentHolder);
            di.insert(cEquipmentHolder);
        }
        for (EquipmentHolder equipmentHolder : subslots) {
            CdcpObject cEquipmentHolder = transEquipmentHolder(equipmentHolder);
            di.insert(cEquipmentHolder);
        }
        di.end();

    }

    public CDevice transDevice(ManagedElement me) {
        HashMap<String, String> addMap = MigrateUtil.transMapValue(me.getAdditionalInfo());
        CDevice device = super.transDevice(me);

        String usedTransportCapacity = addMap.get("UsedTransportCapacity");
        if (usedTransportCapacity != null && usedTransportCapacity.contains("*") && usedTransportCapacity.contains("G")) {
            try {
                int number = Integer.parseInt(usedTransportCapacity.substring(0, usedTransportCapacity.indexOf("*")));
                float g = Float.parseFloat(usedTransportCapacity.substring(usedTransportCapacity.indexOf("*") + 1, usedTransportCapacity.indexOf("G")));
                device.setMaxTransferRate((int)(number * g)+"G");
                deviceOMSRate.put(me.getDn(),g);
            } catch (NumberFormatException e) {
                getLogger().error(e, e);
            }
        }
        return device;
    }

    private HashMap<String,Float> deviceOMSRate = new HashMap<String, Float>();


    private HashMap<String,CSlot> cslotMap = new HashMap<String, CSlot>();
    public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
        HashMap<String, String> addMap = MigrateUtil.transMapValue(equipmentHolder.getAdditionalInfo());
        if (equipmentHolder.getAdditionalInfo() != null && equipmentHolder.getAdditionalInfo().length() > 200)
            equipmentHolder.setAdditionalInfo(null);
        CdcpObject cdcpObject = super.transEquipmentHolder(equipmentHolder);
        if (cdcpObject instanceof CSlot) {
            if (((CSlot) cdcpObject).getAcceptableEquipmentTypeList().length() > 2000)
                ((CSlot) cdcpObject).setAcceptableEquipmentTypeList(null);
            cslotMap.put(cdcpObject.getDn(),(CSlot) cdcpObject);
        }

        if (cdcpObject instanceof CShelf) {
            ((CShelf) cdcpObject).setShelfType(addMap.get("ShelfType"));
        }

        return cdcpObject;
    }
    public CEquipment transEquipment(Equipment equipment) {
        CEquipment cEquipment = super.transEquipment(equipment);
        cEquipment.setNativeEMSName(equipment.getInstalledEquipmentObjectType());
        String additionalInfo = equipment.getAdditionalInfo();
        if (additionalInfo.length() > 1500)
            cEquipment.setAdditionalInfo("");
        equipmentMap.put(cEquipment.getDn(),cEquipment);


        if (cEquipment.getDn().contains("slot=255@")){
            CSlot cequipmentHolder = new CSlot();

                cequipmentHolder.setShelfDn(DNUtil.extractShelfDn(cEquipment.getDn()));
                cequipmentHolder.setShelfId(DatabaseUtil.getSID(CShelf.class, cequipmentHolder.getShelfDn()));


            // cequipmentHolder.setNo(slot);
            cequipmentHolder.setNo("255");


            String slotDn = cEquipment.getDn();
            if (slotDn.contains("@Equipment"))
                slotDn = slotDn.substring(0,slotDn.lastIndexOf("@Equipment"));
            cequipmentHolder.setDn(slotDn);
            cequipmentHolder.setSid(DatabaseUtil.nextSID(cequipmentHolder));

            cequipmentHolder.setCollectTimepoint(equipment.getCreateDate());
//            cequipmentHolder.setHolderType(equipment.gete());
//            cequipmentHolder.setExpectedOrInstalledEquipment(equipmentHolder.getExpectedOrInstalledEquipment());
//            cequipmentHolder.setAcceptableEquipmentTypeList(equipmentHolder.getAcceptableEquipmentTypeList());

            if (cequipmentHolder.getAcceptableEquipmentTypeList() != null && cequipmentHolder.getAcceptableEquipmentTypeList().length() > 1500)
                cequipmentHolder.setAcceptableEquipmentTypeList("");

//            cequipmentHolder.setHolderState(equipmentHolder.getHolderState());
            cequipmentHolder.setParentDn(equipment.getParentDn());
            cequipmentHolder.setEmsName(equipment.getEmsName());
//            cequipmentHolder.setUserLabel(equipmentHolder.getUserLabel());
            cequipmentHolder.setNativeEMSName(equipment.getNativeEMSName());
            cequipmentHolder.setOwner(equipment.getOwner());
            cequipmentHolder.setAdditionalInfo(equipment.getAdditionalInfo());

            if (!cslotMap.containsKey(cequipmentHolder.getDn()))
                makeupSlots.add(cequipmentHolder);
        }

        return cEquipment;
    }
    private CCrossConnect makeupCC(String aend,String zend) {
        CrossConnect cc = new CrossConnect();
        cc.setDn(SysUtil.nextDN());
        cc.setaEndNameList(aend);
        cc.setzEndNameList(zend);
        cc.setaEndTP(DNUtil.extractPortDn(aend));
        cc.setzEndTP(DNUtil.extractPortDn(zend));
        cc.setCcType("ST_SIMPLE");
        cc.setDirection("CD_UNI");
        cc.setEmsName(emsdn);
        cc.setParentDn(DNUtil.extractNEDn(aend));
        CCrossConnect cCrossConnect = transCC(cc);
        cCrossConnect.setTag1("MAKEUP");
        return cCrossConnect;
    }
    private CCTP makeupCTP( String dn,String ccDn) throws Exception {
        CCTP ctp = new CCTP();
        ctp.setDn(dn);
        ctp.setEmsName(emsdn);

        try {
            ctp.setNativeEMSName(DNUtil.extractCTPSimpleName(dn));
        } catch (Exception e) {

        }
        ctp.setDirection(DicConst.PTP_DIRECTION_BIDIRECTIONAL);
        //      ctp.setRate(zctp.getRate());
//        ctp.setRateDesc(SDHUtil.rateDesc(zctp.getRate()));
//        ctp.setTmRate(SDHUtil.getTMRate(zctp.getRate()));

        ctp.setPortdn(DNUtil.extractPortDn(dn));
        boolean b = DatabaseUtil.isSIDExisted(CPTP.class, ctp.getPortdn());
        if (!b) {
            getLogger().error("FTP PORT NOT FOUND : "+ctp.getPortdn());
        }
        //    cctp.setType(zctp.getType());
        ctp.setTag1("MAKEUP");
        ctp.setTag2(ccDn);
        setCTPRateDescAndTmRate(ctp);

        return ctp;
    }
    private String  getDefaultTmRate(String ctpDn) {
        String deviceDn = DNUtil.extractNEDn(ctpDn);
        Float g = deviceOMSRate.get(deviceDn);
        if (g == null) g = 100f;
        String gs = g+"";
        if (gs.endsWith(".0"))
            gs = g.intValue()+"";
        String defaultOpRate = gs+"G";
        return defaultOpRate;
    }
    private void setCTPRateDescAndTmRate(CCTP cctp) {

        String defaultOpRate = getDefaultTmRate(cctp.getDn());

        String ctpDn = cctp.getDn();
        String oduName = ctpDn.substring(ctpDn.lastIndexOf("/")+1);
        if (oduName.contains("odu0")) {
            //   cctp.setTmRate("1.25G");
            cctp.setTmRate("1G");
            cctp.setRateDesc("ODU0");
        }

        if (oduName.contains("odu1")) {
            cctp.setTmRate("2.5G");
            cctp.setRateDesc("ODU1");
        }

        if (oduName.contains("odu2")) {
            cctp.setTmRate("10G");
            cctp.setRateDesc("ODU2");
        }

        if (oduName.contains("odu3")) {
            cctp.setTmRate("40G");
            cctp.setRateDesc("ODU3");
        }
        if (oduName.contains("odu4")) {
            cctp.setTmRate("100G");
            cctp.setRateDesc("ODU4");
        }
        if (oduName.contains("ge")) {
            cctp.setTmRate("1G");
            cctp.setRateDesc("GE");
        }

        if (oduName.contains("10ge")) {
            cctp.setTmRate("10G");
            cctp.setRateDesc("10GE");
        }

        if (ctpDn.contains("100ge"))  {
            cctp.setTmRate("100G");
            cctp.setRateDesc("ODU4");
        }

        if (oduName.contains("oms"))  {
            cctp.setTmRate(defaultOpRate);
            cctp.setRateDesc("OMS");
        }
        if (oduName.contains("ots"))  {
            cctp.setTmRate(defaultOpRate);
            cctp.setRateDesc("OTS");
        }
        if (oduName.contains("och"))  {
            cctp.setTmRate(defaultOpRate);
            cctp.setRateDesc("OCH");
        }

        if (oduName.contains("osc"))  {
            cctp.setTmRate(defaultOpRate);
            cctp.setRateDesc("OSC");
        }

//        if (ctpDn.contains("odu0="))  {
//            cctp.setTmRate("1.25G");
//            cctp.setRateDesc("ODU0");
//        }

    }

    private HashMap<String,String> shortedCTPDnMap = new HashMap<String, String>();
    //   private HashMap<String,List<String>> ctpParentChildMap_4short = new HashMap<String, List<String>>();
    @Override
    public CCTP transCTP(CTP ctp) {
        String ctpDn = ctp.getDn();
        if (ctpDn.contains("layerrate")) {
            int beginIndex = ctpDn.indexOf("layerrate=") + "layerrate=".length();
            ctp.setRate(ctpDn.substring(beginIndex, ctpDn.indexOf("/", beginIndex)));
        }


        CCTP cctp = new CCTP();
        cctp.setDn(ctp.getDn());

        cctp.setPortdn(ctp.getPortdn());
        cctp.setParentCtpdn(ctp.getParentCtpdn());
        cctp.setSid(DatabaseUtil.nextSID(cctp));
        cctp.setCollectTimepoint(ctp.getCreateDate());
        cctp.setEdgePoint(ctp.isEdgePoint());
        cctp.setType(ctp.getType());
        cctp.setConnectionState(ctp.getConnectionState());
        cctp.setTpMappingMode(ctp.getTpMappingMode());
        cctp.setDirection(DicUtil.getPtpDirection(ctp.getDirection()));
        cctp.setTransmissionParams(ctp.getTransmissionParams());
        if (ctp.getTransmissionParams() != null && ctp.getTransmissionParams().contains("@"))
            ctp.setRate(cctp.getTransmissionParams().substring(0,ctp.getTransmissionParams().indexOf("@")));
        cctp.setRate(ctp.getRate());

        cctp.setRateDesc(SDHUtil.rateDesc(ctp.getRate()));

        if (cctp.getRateDesc() == null || cctp.getRateDesc().isEmpty()) {
            //getLogger().error("RateDesc is null : rate=" + ctp.getRate());
        }
        cctp.setTmRate(SDHUtil.getTMRate(ctp.getRate()));
        if (cctp.getTmRate() == null || cctp.getTmRate().isEmpty()) {
            //	getLogger().error("getTmRate is null : rate=" + ctp.getRate());
        }
//        SDHUtil.setCTPNumber(cctp);


        cctp.setTpProtectionAssociation(ctp.getTpProtectionAssociation());
        cctp.setParentDn(ctp.getParentDn());
        cctp.setEmsName(emsdn);
        cctp.setUserLabel(ctp.getUserLabel());
        cctp.setNativeEMSName(ctp.getNativeEMSName());
        cctp.setOwner(ctp.getOwner());
        cctp.setAdditionalInfo(ctp.getAdditionalInfo());



      //  setCTPRateDescAndTmRate(cctp);
//        if (cctp.getTmRate() == null || cctp.getTmRate().isEmpty()) {
//            String defaultOpRate = getDefaultTmRate(cctp.getDn());
//            cctp.setTmRate(defaultOpRate);
//        }
        // if (cctp.getTmRate().equals("1G")) cctp.setTmRate("1.25G");

        cctp.setPortdn(cctp.getParentDn());
//        if (cctp.getNativeEMSName() == null || cctp.getNativeEMSName().isEmpty()) {
//            cctp.setNativeEMSName(ctp.getDn().substring(ctp.getDn().indexOf("CTP:/")+5));
//        }
        String additionalInfo = cctp.getAdditionalInfo();

        Map<String, String> tmMap = MigrateUtil.transMapValue(cctp.getTransmissionParams());
        if (cctp.getTransmissionParams() != null && cctp.getTransmissionParams().length() > 0) {
            // System.out.println();
        }
        String tunedFrequency = tmMap.get("TunedFrequency");
        if (tunedFrequency != null) {
            cctp.setFrequencies(tunedFrequency);
        } else {
            Map<String, String> map = MigrateUtil.transMapValue(additionalInfo);
            String nativeEMSName = map.get("nativeEMSName");
            if (nativeEMSName != null) {
                ctp.setNativeEMSName(nativeEMSName);
                if (nativeEMSName.contains("fr=")) {
                    int beginIndex = nativeEMSName.indexOf("fr=") + 3;
                    String freq = nativeEMSName.substring(beginIndex,nativeEMSName.indexOf("}",beginIndex));
                    if (!freq.contains("tunable"))
                        cctp.setFrequencies(freq);
                }
            }
        }



        //    cctp.setFrequencies(map.get("Frequency"));
        if (additionalInfo.length() > 2000)
            cctp.setTransmissionParams(additionalInfo.substring(0, 2000));


        String dn = cctp.getDn();



        return cctp;
    }


    protected void migrateCC() throws Exception {
        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        executeDelete("delete  from CCTP c where c.emsName = '" + emsdn + "' and c.tag1='MAKEUP'", CCTP.class);
        DataInserter di = new DataInserter(emsid);
        List<CCTP> makeupCTPS = new ArrayList<CCTP>();
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        try {
            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));


                    List<CCrossConnect> splitCCS = U2000MigratorUtil.transCCS(cc, emsdn);
                    newCCs.addAll(splitCCS);

                    for (CCrossConnect ncc : splitCCS) {
                        DSUtil.putIntoValueList(aptpCCMap,ncc.getAptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getZptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getAptp(),ncc);

                        if (!DatabaseUtil.isSIDExisted(CCTP.class,ncc.getAend()))
                            makeupCTPS.add(makeupCTP(ncc.getAend(),cc.getDn()));
                        if (!DatabaseUtil.isSIDExisted(CCTP.class,ncc.getZend()))
                            makeupCTPS.add(makeupCTP(ncc.getZend(),cc.getDn()));


                        if (ncc.getAdditionalInfo().length() > 200)
                            ncc.setAdditionalInfo(null);
                    }
                }
            }

            removeDuplicateDN(newCCs);
            removeDuplicateDN(makeupCTPS);
//            di.insert(makeupCTPS);
            di.insert(newCCs);


        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

    }

    @Override
    protected List insertCtps(List<CTP> ctps) throws Exception {
        DataInserter di = new DataInserter(emsid);
        getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
        List<CCTP> cctps = new ArrayList<CCTP>();
        if (ctps != null && ctps.size() > 0) {
            for (CTP ctp : ctps) {
                CCTP cctp = transCTP(ctp);
                if (cctp != null) {
                    cctps.add(cctp);
                }
            }
        }

        for (CCTP cctp : cctps) {

            DSUtil.putIntoValueList(ptp_ctpMap, cctp.getParentDn(), cctp);
            if (cctp.getParentCtpdn() != null && !cctp.getParentCtpdn().isEmpty()) {
                cctp.setParentCtpdn(shortedCTPDnMap.get(cctp.getParentCtpdn()));
                DSUtil.putIntoValueList(ctpParentChildMap, cctp.getParentCtpdn(), cctp);
            }
            ctpMap.put(cctp.getDn(), cctp);
            di.insert(cctp);
        }

        di.end();
        return cctps;
    }

    @Override
    public CPTP transPTP(PTP ptp) {
        String rate = ptp.getRate();

        CEquipment _card = equipmentMap.get(DNUtil.extractCardDn(ptp.getDn()));
        String num = null;

        if (_card != null)
            num = MigrateUtil.transMapValue(_card.getAdditionalInfo()).get("Basic_PortNum");
        if (num != null && Integer.parseInt(num) > 100) {
            //MON要的
            if (rate != null && !ptp.getUserLabel().equals("MON") && (
                    (rate.equals("1||") && ptp.getDn().contains("PTP")) ||
//                rate.contains("1533||") ||       //OAC
//                rate.contains("1535||") ||      //  lr-OP(1535)


                            //                       rate.contains("1500||") ||     //光监控通道 OSC

                            rate.contains("4177||") ||    //shell-in
                            rate.contains("4178||") ||   //shell-out

                            rate.contains("4192||") ||        //风扇
                            rate.contains("4193||") ||           //光开光
                            rate.contains("4194||") ||      //单板供电单元
                            rate.contains("4195||") ||       //单板存储单元
                            rate.contains("4196||") ||       //二层交换单元
                            rate.contains("4208||")     //监控通道电接口
                    //   rate.contains("4178||") ||


            ))
                return null;
        }



        CPTP cptp = super.transPTP(ptp);

        String dn = cptp.getDn();


        // cptp.setNo(ptp.getUserLabel());
        // cptp.setNo(ptp.getDn().substring(ptp.getDn().indexOf("port=")+5));
        int i = dn.indexOf("/", dn.indexOf("slot="));
        String carddn = (dn.substring(0,i)+"@Equipment:1").replaceAll("PTP:","EquipmentHolder:")
                .replaceAll("FTP:","EquipmentHolder:");

        carddn = carddn.replaceAll("direction=src/","");
        carddn = carddn.replaceAll("direction=sink/","");

        cptp.setParentDn(carddn);
        cptp.setCardid(DatabaseUtil.getSID(CEquipment.class,carddn));
        cptp.setLayerRates(ptp.getRate());
        cptp.setType(ZTEDicUtil.getPtpType(cptp.getLayerRates()));
        if (cptp.getDn().contains("FTP")) cptp.setType("LOGICAL");
        cptp.setSpeed(ZTEDicUtil.getSpeed(cptp.getLayerRates()));
        cptp.setDeviceDn(ptp.getParentDn());
        if (cptp.getSpeed() == null) cptp.setSpeed("40G");
        CEquipment card = equipmentMap.get(carddn);
        HashMap<String, String> addMap = new HashMap<String, String>();
        if (card != null) {
            String additionalInfo = card.getAdditionalInfo();
            addMap = MigrateUtil.transMapValue(additionalInfo);
        }
        //     cptp.setNo(addMap.get("nativeEMSName"));


        HashMap<String, String> ptpaddMap = MigrateUtil.transMapValue(ptp.getAdditionalInfo());
        String nativeEMSName = ptpaddMap.get("nativeEMSName");
        if (nativeEMSName != null && nativeEMSName.contains(")")) {
            cptp.setNo(nativeEMSName.substring(nativeEMSName.lastIndexOf(")")+1));
            cptp.setNativeEMSName(nativeEMSName);
        }
        else {
            String seriesNo = ptpaddMap.get("SeriesNo");//SeriesNo=1103_1
            if (seriesNo != null && seriesNo.contains("_"))
                cptp.setNo(seriesNo.substring(seriesNo.lastIndexOf("_")+1));
            else
                cptp.setNo(nativeEMSName);
        }


        if (card != null) {


            String key  = "Port_"+cptp.getNo()+"_SFP";
            String value = addMap.get(key);
            if (value != null && value.contains("Mb/s")) {
                String size = value.substring(0, value.indexOf("Mb/s"));
                int g10 = Integer.parseInt(size) / 100;
                cptp.setSpeed(getSpeed((float)g10/10f));
            }
            //Port_3_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)
            //AlarmSeverity:||HardwareVersion:VER.B||Port_1_SFP:11100Mb/s-1558.58nm-LC-40km(SMF)||Port_1_SFP_BarCode:||Port_3_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_3_SFP_BarCode:||Port_4_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_4_SFP_BarCode:1QU202105135308||Port_5_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_5_SFP_BarCode:1QU202105135221||Port_6_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_6_SFP_BarCode:1QU202105135236||
        }


        cptp.setEoType(ZTEDicUtil.getEOType(cptp.getLayerRates()));
        if (cptp.getAdditionalInfo() != null && cptp.getAdditionalInfo().contains("PortServiceType:Electronic"))
            cptp.setEoType(DicConst.EOTYPE_ELECTRIC);
        if (cptp.getAdditionalInfo() != null && cptp.getAdditionalInfo().contains("PortServiceType:Optical"))
            cptp.setEoType(DicConst.EOTYPE_OPTIC);
        if (cptp.getDn().contains("FTP"))
            cptp.setEoType(DicConst.EOTYPE_UNKNOWN);
        cptp.setTag3(ptp.getId() + "");
        if (cptp.getDn().contains("direction=sink"))
            cptp.setDirection(DicConst.PTP_DIRECTION_SINK);
        if (cptp.getDn().contains("direction=src"))
            cptp.setDirection(DicConst.PTP_DIRECTION_SOURCE);
        if (cptp.getEoType() == DicConst.EOTYPE_ELECTRIC && "OPTICAL".equals(cptp.getType()))
            cptp.setType("ELECTRICAL");
        ptpMap.put(cptp.getDn() ,cptp);
        return cptp;
    }

    private String getSpeed(float g) {
        if (g >= 10 && g < 15)
            return "10G";
        if (g >= 1 && g < 1.5)
            return "1G";
        if (g >= 2 && g < 3)
            return "2.5G";
        return g+"G";

    }



    private String getCTPType(String ctp) {
        int i = ctp.lastIndexOf("=");
        int j =ctp.lastIndexOf("/");
        if (i > 0 && j > 0) {
            return ctp.substring(j+1,i);
        }
        return "";
    }

    private List<CCrossConnect> ccsToMakeup = new ArrayList<CCrossConnect>();
     private boolean checkEndContainsSubCtp(String end) {
        String[] ctps = end.split(Constant.listSplitReg);
        for (String ctp : ctps) {
            List<CCTP> cctps = ctpParentChildMap.get(ctp);
            if (cctps != null && !cctps.isEmpty())
                return true;
        }
        return false;
    }


    private void debug(String s)   {
        //   System.out.println(s);
    }
    private void debug(CPTP ptp)   {
        // System.out.println(ptp.getTag3());
    }




    private int getDuplicateCount(List<String> list1 ,List<String> list2) {
        int count = 0;
        for (String s : list1) {
            for (String s1 : list2) {
                if (s.equals(s1))
                    count ++;
            }
        }
        return count;
    }
    private CChannel findSubwaveCChanell(List<CChannel> channels,String ctp,List<R_TrafficTrunk_CC_Section> dsrRoutes,HashMap<String,List<String>> subwave_routes) {
        List<CChannel> cChanells = findCChanells(channels, ctp);
        if (cChanells != null && cChanells.size() == 1)
            return cChanells.get(0);
        else if (cChanells != null && cChanells.size() > 1) {
            CChannel c;
            int count = 0;
            for (CChannel cChanell : cChanells) {
                List<String> subWaveRoutes = subwave_routes.get(cChanell.getDn());

                List<String> dsrouteDns = new ArrayList<String>();
                for (R_TrafficTrunk_CC_Section dsrRoute : dsrRoutes) {
                    dsrouteDns.add(dsrRoute.getCcOrSectionDn());
                }

                if (dsrouteDns.containsAll(subWaveRoutes)) return cChanell;
                // getDuplicateCount(dsro)
            }
            getLogger().error("根据ctp找到多个子波，但无法区分：ctp="+ctp);
            return cChanells.get(0);
        }
        return null;
    }

    private CChannel findCChanell(List<CChannel> channels,String ctp) {
        if (ctp .equals("EMS:HZ-U2000-3-P@ManagedElement:4063249@PTP:/rack=1/shelf=3145738/slot=20/domain=wdm/port=1@CTP:/och=1/otu2=1"))
            System.out.println("ctp = " + ctp);
        List<CChannel> cChanells = findCChanells(channels, ctp);
        if (cChanells != null && cChanells.size() > 0)
            return cChanells.get(0);
        return null;
    }
    private List<CChannel> findCChanellsByPtp(List<CChannel> channels,String ptp) {
        List<CChannel> cs = new ArrayList<CChannel>();
//        if (ctp.contains("och=")){
//            int idx =ctp.indexOf("/", ctp.indexOf("och="));
//            if (idx > -1)
//                ctp = ctp.substring(0,idx);
//        }
        for (CChannel channel : channels) {
            if (channel.getSectionOrHigherOrderDn().equals("EMS:HZ-U2000-3-P@MultiLayerSubnetwork:1@SubnetworkConnection:2011-12-21 06:15:08 - 24796 -wdm"))
                System.out.print("");
            if ( (channel.getAend().contains(ptp)) || (channel.getZend().contains(ptp)) )
                cs.add(channel);
        }
        return cs;

    }
    private List<CChannel> findCChanells(List<CChannel> channels,String ctp) {
        List<CChannel> cs = new ArrayList<CChannel>();
//        if (ctp.contains("och=")){
//            int idx =ctp.indexOf("/", ctp.indexOf("och="));
//            if (idx > -1)
//                ctp = ctp.substring(0,idx);
//        }
        for (CChannel channel : channels) {
            if (channel.getSectionOrHigherOrderDn().equals("EMS:HZ-U2000-3-P@MultiLayerSubnetwork:1@SubnetworkConnection:2011-12-21 06:15:08 - 24796 -wdm"))
                System.out.print("");
            if (ctp.equals(channel.getAend()) || ctp.equals(channel.getZend()))
                cs.add(channel);
        }
        return cs;

    }
    private List<CCTP> findAllChildCTPS(String parentCtp) {

        List<CCTP> all = new ArrayList<CCTP>();
        List<CCTP> cctps = ctpParentChildMap.get(parentCtp);
        if (cctps != null) {
            //   all.addAll(cctps);
            for (CCTP cctp : cctps) {
                List<CCTP> c = findAllChildCTPS(cctp.getDn());
                if (c != null && c.size() > 0) {
                    all.addAll(c);
                } else {
                    all.add(cctp);
                }
            }
        }
        return all;
    }

    private  HashMap<String,List<R_TrafficTrunk_CC_Section>>  queryTrafficTrunkCCSectionMap() {
        final HashMap<String,List<R_TrafficTrunk_CC_Section>> snc_cc_section_map = new HashMap<String, List<R_TrafficTrunk_CC_Section>>();
        //    List<R_TrafficTrunk_CC_Section> routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
        for (R_TrafficTrunk_CC_Section _route : routeList) {
//            if (_route.getType().equals("CC")) {
//                _route.setCcOrSectionDn(DNUtil.compressCCDn(_route.getCcOrSectionDn()));
//            }
            String sncDn = _route.getTrafficTrunDn();
            List<R_TrafficTrunk_CC_Section> value = snc_cc_section_map.get(sncDn);
            if (value == null) {
                value = new ArrayList<R_TrafficTrunk_CC_Section>();
                snc_cc_section_map.put(sncDn,value);
            }
            value.add(_route);
        }
        return snc_cc_section_map;
    }



    protected void migrateSection() throws Exception {
        executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "'", CSection.class);
        DataInserter di = new DataInserter(emsid);
        List<Section> sections = sd.queryAll(Section.class);
        if (sections != null && sections.size() > 0) {
            for (Section section : sections) {
//                if (section.getDn().contains("FTP"))
//                    System.out.println();
//                if (section.getaEndTP().contains("FTP") || section.getzEndTP().contains("FTP"))
//                    continue;
//                if (!section.getRate().equals("42") && !section.getRate().equals("1535"))
//                    continue;
                CSection csection = transSection(section);
                if (section == null) continue;
                csection.setType("OTS");
                csection.setSid(DatabaseUtil.nextSID(csection));
                // csection.setSid(toSid(Long.parseLong(section.getDn().substring(section.getDn().lastIndexOf(" - ") + 3))));
                String aendtp = csection.getAendTp();
                String zendtp = csection.getZendTp();
                if (aendtp.contains("CTP") || zendtp.contains("CTP")) {
                    continue;
                }
                csection.setAptpId(DatabaseUtil.getSID(CPTP.class, aendtp));
                csection.setZptpId(DatabaseUtil.getSID(CPTP.class, zendtp));

                String aBindTP = null;
                String zBindTP = null;
                try {
                    String additionalInfo = section.getAdditionalInfo();
                    HashMap<String, String> map = MigrateUtil.transMapValue(additionalInfo);
                    aBindTP = map.get("ABindTP");
                    zBindTP = map.get("ZBindTP");
                } catch (Exception e) {
                    logger.error(e, e);
                }

                if (aBindTP != null && zBindTP != null) {
                    csection.setDirection(DicConst.CONNECTION_DIRECTION_UNI);
                }

                di.insert(csection);
                cSectionMap.put(csection.getDn(),csection);


                try {

                    if (aBindTP != null && zBindTP != null && !aBindTP.trim().isEmpty() && !zBindTP.trim().isEmpty()) {

                        if (aBindTP.endsWith("}")) {
                            aBindTP = aBindTP.substring(0,aBindTP.length()-1);
                            aBindTP = aBindTP.replaceAll("\\{", ":").replaceAll("\\}", "@");
                        }

                        if (zBindTP.endsWith("}")) {
                            zBindTP = zBindTP.substring(0,zBindTP.length()-1);
                            zBindTP = zBindTP.replaceAll("\\{", ":").replaceAll("\\}", "@");
                        }


                        CSection cSection2 = createSection(aBindTP,zBindTP);
                        cSection2.setDirection(DicConst.CONNECTION_DIRECTION_UNI);
                        cSection2.setUserLabel(csection.getUserLabel());
                        cSection2.setNativeEMSName(csection.getNativeEMSName());
                        if (!cSectionMap.containsKey(cSection2.getDn())) {
                            di.insert(cSection2);
                            cSectionMap.put(cSection2.getDn(),cSection2);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
        di.end();
    }


    @Override
    public CSection transSection(Section section) {

        CSection cSection = super.transSection(section);
        cSection.setType("OTS");
        cSection.setSpeed("40G");
        cSection.setAdditionalInfo("");
        DSUtil.putIntoValueList(ptpSectionMap,cSection.getAendTp(),cSection);
        cSections.add(cSection);
        return cSection;
    }

    public boolean isSameDeviceSection(String sectionDn) {

        String[] split = sectionDn.split("/d=");
        String ems = split[0].substring(0,split[0].indexOf("@"));
        String aptp  = split[1].substring(4,split[1].length()-1);
        String zptp  = split[2].substring(5,split[2].length());
        String ame =aptp.substring(0,aptp.lastIndexOf("TP"));
        String zme =zptp.substring(0,zptp.lastIndexOf("TP"));
        return (ame.equals(zme));
    }
    /*
    EMS:TZ-OTNU31-1-P@TopologicalLink:/d=src/ManagedElement{70127685(P)}FTP{/direction=sink/rack=0/shelf=7/slot=24/port=78151743}
    _/d=sink/ManagedElement{70127685(P)}FTP{/direction=src/rack=0/shelf=7/slot=4/port=78200862}
     */
    public    CSection createSection(String sectionDn) {
        String[] split = sectionDn.split("/d=");
        String ems = split[0].substring(0,split[0].indexOf("@"));
        String aptp  = split[1].substring(4,split[1].length()-1);
        String zptp  = split[2].substring(5,split[2].length());

        String aptpDn = ems + "@" +aptp.replaceAll("\\{",":").replaceAll("}","");
        String zptpDn = ems + "@" +zptp.replaceAll("\\{",":").replaceAll("}","");

        CSection csection = new CSection();
        csection.setDn(sectionDn);
        csection.setSid(DatabaseUtil.nextSID(csection));
        csection.setCollectTimepoint(new Date());
        csection.setRate("42");
        csection.setTag1("MAKEUP");
        csection.setSpeed("40G");
        csection.setDirection(DicConst.CONNECTION_DIRECTION_UNI);
        csection.setAendTp(aptpDn);

        csection.setZendTp(zptpDn);
        //     csection.setParentDn(section.getParentDn());
        csection.setEmsName(emsdn);
        csection.setUserLabel("");
        csection.setNativeEMSName("");
        csection.setOwner("");
        csection.setAdditionalInfo("");
        csection.setType("OTS");
        csection.setSpeed("40G");
        DSUtil.putIntoValueList(ptpSectionMap,csection.getAendTp(),csection);
        return csection;


    }

    public    CSection createSection(String aptpDn,String zptpDn) {


        CSection csection = new CSection();
        csection.setDn(aptpDn+"<>"+zptpDn);
        csection.setSid(DatabaseUtil.nextSID(csection));
        csection.setCollectTimepoint(new Date());
        csection.setRate("42");
        csection.setTag1("MAKEUP2ADD");
        csection.setSpeed("40G");
        csection.setDirection(DicConst.CONNECTION_DIRECTION_UNI);
        csection.setAendTp(aptpDn);

        csection.setZendTp(zptpDn);
        //     csection.setParentDn(section.getParentDn());
        csection.setEmsName(emsdn);
        csection.setUserLabel("");
        csection.setNativeEMSName("");
        csection.setOwner("");
        csection.setAdditionalInfo("");
        csection.setType("OTS");
        csection.setSpeed("40G");
        DSUtil.putIntoValueList(ptpSectionMap,csection.getAendTp(),csection);
        return csection;


    }

    public CSection transSection(SubnetworkConnection section) {
        CSection csection = new CSection();
        csection.setDn(section.getDn());
        csection.setSid(DatabaseUtil.nextSID(csection));
        csection.setCollectTimepoint(section.getCreateDate());
        csection.setRate(section.getRate());
        String rate = section.getRate();

        csection.setDirection(DicUtil.getConnectionDirection(section.getDirection()));
        csection.setAendTp(section.getaPtp());
        DatabaseUtil.getSID(CPTP.class,csection.getAendTp());
        csection.setZendTp(section.getzPtp());
        DatabaseUtil.getSID(CPTP.class,csection.getZendTp());
        csection.setParentDn(section.getParentDn());
        csection.setEmsName(section.getEmsName());
        csection.setUserLabel(section.getUserLabel());
        csection.setNativeEMSName(section.getNativeEMSName());
        csection.setOwner(section.getOwner());
        csection.setAdditionalInfo(section.getAdditionalInfo());


        csection.setType("OTS");
        csection.setSpeed("40G");
        csection.setAdditionalInfo("");
        DSUtil.putIntoValueList(ptpSectionMap,csection.getAendTp(),csection);
        cSections.add(csection);
        return csection;
    }




//    protected void migrateOTS() throws Exception {
//        executeDelete("delete  from COTS c where c.emsName = '" + emsdn + "'", COTS.class);
//        DataInserter di = new DataInserter(emsid);
//        List<Section> sections = sd.queryAll(Section.class);
//        if (sections != null && sections.size() > 0) {
//            for (Section section : sections) {
//                COTS csection = transOTS(section);
//                csection.setSid(DatabaseUtil.nextSID(csection));
//                // csection.setSid(toSid(Long.parseLong(section.getDn().substring(section.getDn().lastIndexOf(" - ") + 3))));
//                String aendtp = csection.getAendTp();
//                String zendtp = csection.getZendTp();
//                if (aendtp.contains("CTP") || zendtp.contains("CTP")) {
//                    continue;
//                }
//                csection.setAptpId(DatabaseUtil.getSID(CPTP.class, aendtp));
//                csection.setZptpId(DatabaseUtil.getSID(CPTP.class, zendtp));
//                di.insert(csection);
//            }
//        }
//        di.end();
//    }


    /**
     *
     * @param routes
     * @param section
     * @return
     */
    private boolean checkIsRedundantSection(List<R_TrafficTrunk_CC_Section> routes,R_TrafficTrunk_CC_Section section) {
        if (section.getDn().equals("2338aae2-9b21-413f-af92-c68c3141ae66"))
            System.out.println();
        if (!isSameDeviceSection(section.getCcOrSectionDn()))    //如果两段在不同设备上，必然不冗余
            return false;

        int sectionNumber = 0;
        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getType().equals("SECTION"))
                sectionNumber ++;
        }

        if (sectionNumber == 1) return false; // 路由里只有一根段，必然不冗余


        //如果段的两端游离 ,则为冗余
        boolean alone = true;
        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getCcOrSectionDn().equals(section.getCcOrSectionDn()))
                continue;

            if (route.getType().equals("SECTION")) {
                if (route.getaEnd().equals(section.getaEnd())
                        || route.getzEnd().equals(section.getzEnd())
                        ||route.getaEnd().equals(section.getzEnd())
                        ||route.getzEnd().equals(section.getaEnd()) ) {
                    alone = false;
                    break;
                }

            }
            if (route.getType().equals("CC")) {
                if (route.getaPtp().equals(section.getaEnd())
                        || route.getzPtp().equals(section.getzEnd())
                        ||route.getaPtp().equals(section.getzEnd())
                        ||route.getzPtp().equals(section.getaEnd()) ) {
                    alone = false;
                    break;
                }

            }
        }

        if (alone) return true;

        return false;

    }



    public static void main2(String[] args) throws Exception {

        String fileName=  "D:\\cdcpdb\\2014-12-06-152500-TZ-OTNU31-1-P-DayMigration.db";
        String emsdn = "TZ-OTNU31-1-P";
        if (args != null && args.length > 0)
            fileName = args[0];
        if (args != null && args.length > 1)
            emsdn = args[1];
        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        JPASupportSpringImpl context = new JPASupportSpringImpl("entityManagerFactoryData");
        try
        {
            context.begin();
            String[] preLoadSqls = Constants.PRE_LOAD_SQLS;
            for (String sql : preLoadSqls) {

                DBUtil.getInstance().executeNonSelectingSQL(context,sql);
            }
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }

        ZTE_OTNU31_OTN_Migrator loader = new ZTE_OTNU31_OTN_Migrator (fileName, emsdn){
            public void afterExecute() {
                updateEmsStatus(Constants.CEMS_STATUS_READY);
                printTableStat();
                IrmsClientUtil.callIRMEmsMigrationFinished(emsdn);
            }
        };
        loader.execute();
    }

    public static void main(String[] args) throws Exception {
        String fileName = "I:\\1610\\2016-10-28-194438-ZJ-ZTE-1-PTN-DayMigration.db";
        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        JPASupportSpringImpl context = new JPASupportSpringImpl("entityManagerFactoryData");
        try
        {
            context.begin();
            String[] preLoadSqls = Constants.PRE_LOAD_SQLS;
            for (String sql : preLoadSqls) {
                DBUtil.getInstance().executeNonSelectingSQL(context,sql);
            }
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }

        ZTE_PTN_U31_Migrator loader = new ZTE_PTN_U31_Migrator (fileName, "ZJ-ZTE-1-PTN"){
            public void afterExecute() {
                printTableStat();
            }
        };
        loader.execute();
    }

}
