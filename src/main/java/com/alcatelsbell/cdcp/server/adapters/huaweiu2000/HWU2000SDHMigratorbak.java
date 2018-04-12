package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.SDHRouteComputationUnit;
import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.DBUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import org.asb.mule.probe.framework.entity.*;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.FileLogger;
import org.asb.mule.probe.ptn.u2000V16.nbi.job.CTPUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static com.alcatelsbell.cdcp.server.adapters.CacheClass.*;
import static com.alcatelsbell.cdcp.util.MemTable.*;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-7
 * Time: 上午11:14
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HWU2000SDHMigratorbak  extends AbstractDBFLoader {

    public HWU2000SDHMigratorbak(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog("HWSDH_"+emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }

    private static FileLogger fLogger = new FileLogger("U2000-SDH-Device.log");

    public HWU2000SDHMigratorbak(Serializable object, String emsdn) {
        this.emsdn = emsdn;
        this.resultObject = object;
        MigrateThread.thread().initLog(fLogger);
    }



    protected Class[] getStatClss() {
        return new Class[]{CCrossConnect.class, CChannel.class, CPath.class, CRoute.class, CPath_Channel.class,
                CPath_CC.class, CRoute_Channel.class,
                CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class, CVirtualBridge.class,
                CMP_CTP.class, CEthTrunk.class, CStaticRoute.class, CEthRoute.class, CEthTrunk_SDHRoute.class,
                CEthRoute_StaticRoute.class, CEthRoute_ETHTrunk.class, CSection.class, CCTP.class, CDevice.class, CPTP.class, CTransmissionSystem.class, CTransmissionSystem_Channel.class};
    }



    private BObjectMemTable<T_CCrossConnect> ccTable = new BObjectMemTable(T_CCrossConnect.class,"aend","zend");
    private BObjectMemTable<T_CTP> ctpTable = new BObjectMemTable(T_CTP.class,"portdn","parentCtp");
 //   private BObjectMemTable sectionTable = new BObjectMemTable(Section.class);
    private BObjectMemTable<T_CRoute> cRouteTable = new BObjectMemTable(T_CRoute.class);
  //  private BObjectMemTable sncTable = new BObjectMemTable(SubnetworkConnection.class);

     private HashMap<String,CChannel> highOrderCtpChannelMap = new HashMap<String, CChannel>();
    private HashMap<String,CChannel> lowOrderCtpChannelMap = new HashMap<String, CChannel>();
 //   private HashMap<String,List<CTP>> ptpCtpMap = new HashMap<String, List<CTP>>();
    private List<CChannel> cChannelList =  new ArrayList<CChannel>();

    private List<CSection> cSections = new ArrayList<CSection>();
    private HashMap<String,List<CChannel>> vc4ChannelMap = new HashMap<String, List<CChannel>>();


    public CDevice transDevice(ManagedElement me) {
        CDevice device = super.transDevice(me);
        if (device.getProductName() != null && device.getProductName().equals("VNE"))
            device.setProductName("VNE_SDH");
        return device;
    }

    private void manInsertTransmissionChannels() throws Exception {
        List<CTransmissionSystem_Channel>   ts_channels = new ArrayList<CTransmissionSystem_Channel>();
        List<ProtectionSubnetworkLink> protectionSubnetworkLinks = sd.queryAll(ProtectionSubnetworkLink.class);
        List<CPath> cpaths = findObjects(CPath.class, "select c from CPath c");
        List<CSection> sections = findObjects(CSection.class, "select c from CSection c");
        for (ProtectionSubnetworkLink link : protectionSubnetworkLinks) {
            for (CSection section : sections) {
                if ((section.getAendTp().equals(link.getSrcTp()) &&
                        section.getZendTp().equals(link.getSinkTp()) ) ||
                        (section.getAendTp().equals(link.getSinkTp()) &&
                                section.getZendTp().equals(link.getSrcTp()))) {
                    CTransmissionSystem_Channel sc = new CTransmissionSystem_Channel();
                    sc.setTransmissionSystemDn(link.getProtectionSubnetworkDn());
                    sc.setSectionDn(section.getDn());
                    sc.setEmsName(emsdn);
                    sc.setDn(SysUtil.nextDN());
                    ts_channels.add(sc);
                }
            }


//            for (CPath cpath : cpaths) {
//                if ((cpath.getAptp() != null && cpath.getAptp().equals(link.getSinkTp()))) {
//
//                }
//                if ((cpath.getAptp().equals(link.getSrcTp()) &&
//                        cpath.getZptp().equals(link.getSinkTp()) ) ||
//                        (cpath.getAptp().equals(link.getSinkTp()) &&
//                                cpath.getZptp().equals(link.getSrcTp()))) {
//                    List<CChannel> cChannels = findObjects(CPath_Channel.class,"select c from CPath_Channel c where d.pathDn='"+cpath.getDn()+"'");
//                   // List<CChannel> cChannels = cPath_ChannelMap.get(cpath.getDn());
//                    if (cChannels != null) {
//                        for (CChannel cChannel : cChannels) {
//                            CTransmissionSystem_Channel sc = new CTransmissionSystem_Channel();
//                            sc.setTransmissionSystemDn(link.getProtectionSubnetworkDn());
//                            sc.setChannelDn(cChannel.getDn());
//                            sc.setEmsName(emsdn);
//                            sc.setDn(SysUtil.nextDN());
//                            ts_channels.add(sc);
//                        }
//                    }
//
//                }
//            }

        }
        DataInserter di = new DataInserter(emsid);
        di.insert(ts_channels);
    }


    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "华为");

        migrateLogical = getAttribute("logical") == null ? true : "true".equalsIgnoreCase(getAttribute("logical").toString()) ;

        if (migrateLogical && !isTableHasData(CTP.class)) {
                    migrateLogical = false;
                    getLogger().info("CTP IS EMPTY ,LOGCAL = FALSE");
        }


        getLogger().info("logical = "+migrateLogical);

        logAction(emsdn + " migrateManagedElement", "同步网元", 2);
        migrateManagedElement();


        migrateSubnetwork();

        logAction("migrateEquipmentHolder", "同步槽道", 5);
        migrateEquipmentHolder();

        logAction("migrateEquipment", "同步板卡", 10);
        migrateEquipment();


        logAction("migratePTP", "同步端口", 20);
        migratePTP();

//
        if (migrateLogical || isTableHasData(CTP.class)) {
            logAction("migrateCTP", "同步CTP", 25);
            migrateCTP();
        }

        logAction("migrateSection", "同步段", 30);
        migrateSection();
//
//
        if (migrateLogical) {
            logAction("migrateCC", "同步交叉", 30);
            migrateCC();
            logAction("migrateProtectionSubnetwork", "同步传输系统", 32);
            migrateProtectionSubnetwork();
            logAction("migrateSNC", "同步SNC", 35);
            migrateSNC();

            logAction("migrateVB", "同步VB", 40);
            migrateVB();

            logAction("migrateEthBindingPath", "同步MSTP", 70);
            migrateEthBindingPath();
        }

//        logAction("migrateSubnetwork", "同步子网", 80);
//        migrateETHTrunk();

//        logAction("migrateProtectGroup", "同步保护组", 85);
//        migrateProtectGroup();
//        // checkEquipmentHolders(sd);
//        // checkPTP(sd);
//        // MigrateUtil.checkRoute(sd);
//        logAction("migrateProtectingPWTunnel", "同步保护组", 95);
//        migrateProtectingPWTunnel();
        getLogger().info("release");

        // ////////////////////////////////////////
        sd.release();
        ccTable.removeAll();
        // jpaInsertHelper.finishAndRelease();

    }



    public void migrateProtectionSubnetwork() throws Exception {
        executeDelete("delete from CTransmissionSystem c where c.emsName = '"+emsdn+"'",CTransmissionSystem.class);
        executeDelete("delete from CTransmissionSystem_Channel c where c.emsName = '"+emsdn+"'",CTransmissionSystem_Channel.class);
        List<ProtectionSubnetwork> protectionSubnetworks = sd.queryAll(ProtectionSubnetwork.class);
        HashMap<String,List<ProtectionSubnetworkLink>> linkMap = new HashMap<String, List<ProtectionSubnetworkLink>>();
        DataInserter di = new DataInserter(emsid);
        try {
            for (ProtectionSubnetwork protectionSubnetwork : protectionSubnetworks) {
                CTransmissionSystem ct = new CTransmissionSystem();
                ct.setDn(protectionSubnetwork.getDn());
                ct.setEmsName(emsdn);
                ct.setPsnType(protectionSubnetwork.getPsnType());
                ct.setCategory("SDH");
                ct.setAdditionalInfo(protectionSubnetwork.getAdditionalInfo());
                ct.setName(protectionSubnetwork.getNativeEmsName());
                ct.setNativeEmsName(protectionSubnetwork.getNativeEmsName());
                ct.setLayerRate(protectionSubnetwork.getLayerRate());
                ct.setTmRate(SDHUtil.getTMRate(protectionSubnetwork.getLayerRate()));
                String neIds = protectionSubnetwork.getNeIds();
                if (neIds != null) {
                    StringBuffer sb = new StringBuffer();
                    String[] split = neIds.split(Constant.listSplitReg);
                    for (String id : split) {
                        String neDn = emsdn+"@ManagedElement:"+id;
                        sb.append(neDn).append(Constant.dnSplit);
                    }
                    ct.setNeDns(sb.toString());
                }
                di.insert(ct);
            }
        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

        List<CTransmissionSystem_Channel>   ts_channels = new ArrayList<CTransmissionSystem_Channel>();
        List<ProtectionSubnetworkLink> protectionSubnetworkLinks = sd.queryAll(ProtectionSubnetworkLink.class);
        for (ProtectionSubnetworkLink link : protectionSubnetworkLinks) {

            for (CSection cSection : cSections) {
                if (cSection.getAendTp() == null || cSection.getZendTp() == null) continue;

                if ((cSection.getAendTp().equals(link.getSrcTp()) &&
                        cSection.getZendTp().equals(link.getSinkTp()) ) ||
                        (cSection.getZendTp().equals(link.getSrcTp()) &&
                                cSection.getAendTp().equals(link.getSinkTp()) )) {

                    List<CChannel> sectionChannels = vc4ChannelMap.get(cSection.getDn());
                    String vc4List = link.getVc4List();
                    if (!vc4List.isEmpty()) {
                        String[] array = vc4List.split(Constant.listSplitReg);
                        List<String> vc4s = Arrays.asList(array);
                        int vc4Number = array.length;
                        if (sectionChannels != null && sectionChannels.size() > vc4Number) {
                            for (CChannel sectionChannel : sectionChannels) {
                                String aend = sectionChannel.getAend();
                                int i = aend.indexOf("sts3c_au4-j=");
                                i = i +"sts3c_au4-j=".length();
                                int j =  aend.indexOf("/",i);
                                String vc4No = aend.substring(i);
                                if (j > -1)
                                    vc4No = aend.substring(i,j);
                                if (vc4s.contains(vc4No)) {
                                    CTransmissionSystem_Channel sc = new CTransmissionSystem_Channel();
                                    sc.setTransmissionSystemDn(link.getProtectionSubnetworkDn());
                                    sc.setChannelDn(sectionChannel.getDn());
                                    sc.setEmsName(emsdn);
                                    sc.setDn(SysUtil.nextDN());
                                    ts_channels.add(sc);
                                }
                            }
                        } else {
                            CTransmissionSystem_Channel sc = new CTransmissionSystem_Channel();
                            sc.setTransmissionSystemDn(link.getProtectionSubnetworkDn());
                            sc.setSectionDn(cSection.getDn());
                            sc.setEmsName(emsdn);
                            sc.setDn(SysUtil.nextDN());
                            ts_channels.add(sc);
                        }
                    }

               //     break;

                }
            }

        }
        DataInserter di2 = new DataInserter(emsid);
        try {
            getLogger().info("transimssion_system_channel size = "+ts_channels.size());
            di2.insert(ts_channels);
            di2.end();
        } catch (Exception e) {
            getLogger().error(e, e);
        }




    }

    private void migrateEthBindingPath() throws Exception {
        executeDelete("delete from CMP_CTP c where c.emsName = '"+emsdn+"'",CMP_CTP.class);
        executeDelete("delete from CEthRoute c where c.emsName = '"+emsdn+"'",CEthRoute.class);
        executeDelete("delete from CEthTrunk c where c.emsName = '"+emsdn+"'",CEthTrunk.class);
        executeDelete("delete from CStaticRoute c where c.emsName = '"+emsdn+"'",CStaticRoute.class);
        executeDelete("delete from CEthRoute_ETHTrunk c where c.emsName = '"+emsdn+"'",CEthRoute_ETHTrunk.class);
        executeDelete("delete from CEthTrunk_SDHRoute c where c.emsName = '"+emsdn+"'",CEthTrunk_SDHRoute.class);
        executeDelete("delete from CEthRoute_StaticRoute c where c.emsName = '"+emsdn+"'",CEthRoute_StaticRoute.class);




        MultiValueMap  mp_mac_map = new MultiValueMap ();
        MultiValueMap  mp_macList_map = new MultiValueMap ();

        HashMap<String,List<String>> mp_ctps_map = new HashMap<String, List<String>>();
        HashMap<String,String> ctp_mp_map = new HashMap<String,String>();

        //EPLAN业务
        HashMap<String,String> lp_mac_map = new HashMap<String, String>();
        HashMap<String,String> lp_mp_map = new HashMap<String, String>();


        List<HW_MSTPBindingPath> bps = sd.queryAll(HW_MSTPBindingPath.class);
        List<HW_EthService> ess = sd.queryAll(HW_EthService.class);
        List<HW_VirtualLAN> vlans = sd.queryAll(HW_VirtualLAN.class);
        List<HW_VirtualBridge> vbs = sd.queryAll(HW_VirtualBridge.class);

        if (bps == null || bps.isEmpty()) {
            getLogger().info("HW_MSTPBindingPath 无数据 ");
            return;
        }
        HashSet<String> zpaths = new HashSet<String>();
        executeDelete("delete from CMP_CTP c where c.emsName = '"+emsdn+"'",CMP_CTP.class);
        DataInserter di = new DataInserter(emsid);
        try {
            for (HW_EthService es : ess) {
                String aend = es.getaEnd();
                String zend = es.getzEnd();
                String mp = null;
                String mac = null;

                if (es.getServiceType().equals("HW_EST_EPLAN")) {
                    String atype = SDHUtil.getPortType(aend);
                    String ztype = SDHUtil.getPortType(zend);
                    if (atype.equals("lp")){
                        if (ztype.equals("mac")) lp_mac_map.put(aend,zend);
                        if (ztype.equals("mp")) lp_mp_map.put(aend,zend);
                    } else if (ztype.equals("mp")) {
                        if (atype.equals("mac")) lp_mac_map.put(zend,aend);
                        if (atype.equals("mp")) lp_mp_map.put(zend,aend);
                    }
                } else {
                    if (aend.contains("type=mp") && zend.contains("type=mac")) {
                        mp = aend;  mac = zend;
                    }
                    else if (aend.contains("type=mac") && zend.contains("type=mp")) {
                        mp = zend;  mac = aend;
                    } else {
                        getLogger().error("异常的HW_EthService, type="+es.getServiceType()+" aend="+aend+"; zend="+zend);
                        continue;
                    }

                    CStaticRoute cStaticRoute = new CStaticRoute();
                    cStaticRoute.setAptp(aend);
                    cStaticRoute.setAvlan(es.getaVlanID() + "");
                    cStaticRoute.setZptp(zend);
                    cStaticRoute.setZvlan(es.getzVlanID() + "");
                    cStaticRoute.setDn(es.getDn());
                    cStaticRoute.setEmsName(emsdn);
                    di.insert(cStaticRoute);
                    mp_mac_map.put(mp,mac,es);
                }

            }



//            if (vlans != null) {
//                for (HW_VirtualLAN vlan : vlans) {
//                    String forwardTPList = vlan.getForwardTPList();
//
//
//                }
//            }

            //@todo
            if (vbs != null && vbs.size() > 0) {
                for (HW_VirtualBridge vb : vbs) {
                    String logicalTPList = vb.getLogicalTPList();
                    String[] tpList = logicalTPList.split("@EMS");
                    List<String> macList = new ArrayList<String>();
                    List<String> mpList = new ArrayList<String>();
                    if (tpList != null) {
                        for (String tp : tpList) {
                            if (!tp.startsWith("EMS:"))
                                tp = "EMS"+tp;

                            String mac = lp_mac_map.get(tp);
                            if (mac != null) {
                                macList.add(mac);
                            } else {
                                String mp = lp_mp_map.get(tp);
                                if (mp != null) {
                                    mpList.add(mp);
                                }
                            }

                        }
                    }


                }

            }


            List<CMP_CTP> cmp_ctps = new ArrayList<CMP_CTP>();
            for (HW_MSTPBindingPath bp : bps) {
                String allPathList = bp.getAllPathList();
                String usedPathList = bp.getUsedPathList();
                String parentDn = bp.getParentDn();
                String[] allPaths = allPathList.split(Constant.listSplitReg);
                String[] usedPaths = usedPathList.split(Constant.listSplitReg);

                List<String> usedList = Arrays.asList(usedPaths);

                for (String path : allPaths) {
                    CMP_CTP cmp_ctp = new CMP_CTP();
                    cmp_ctp.setCtpDn(path);
                    cmp_ctp.setCtpId(DatabaseUtil.getSID(CCTP.class, path));
                    cmp_ctp.setPtpDn(parentDn);
                    cmp_ctp.setPtpId(DatabaseUtil.getSID(CPTP.class, parentDn));
                    cmp_ctp.setIsUsed(usedList.contains(path) ? 1 : 0);
                    cmp_ctp.setDn(parentDn + "<>" + path);
                    cmp_ctp.setEmsName(emsdn);
                    cmp_ctps.add(cmp_ctp);

                    if (usedList.contains(path))
                        putIntoList(mp_ctps_map,parentDn,path);
                    ctp_mp_map.put(path, parentDn);
                }




            }

            removeDuplicateDN(cmp_ctps);
            di.insert(cmp_ctps);

            List<CEthRoute> ethRouteList = new ArrayList<CEthRoute>();
            Set<String> mps = mp_ctps_map.keySet();
            HashSet<String> processedMps = new HashSet<String>();
            for (String mp : mps) {
                processedMps.add(mp);
                String amac = (String)mp_mac_map.get(mp,0);
                if (amac == null) {
                    errorLog("[也许没问题]根据mp,无法找到对应的mac: mp="+mp);
                    continue;
                }

                List<String> ctps = mp_ctps_map.get(mp);
                if (ctps == null) {
                    errorLog("[也许没问题]根据mp,无法找到对应的ctp, mp="+mp);
                    continue;
                }

                int bandwidh = 0;
                for (String ctp : ctps) {
                    bandwidh += SDHUtil.getCTPRateNumber(ctpTable.findObjectByDn(ctp));
                }

                String zctpDns = null;
                String allZctpDnList = "";
                List<T_CRoute> cRoutes = new ArrayList<T_CRoute>();
                for (String ctpDn : ctps) {
                    List<T_CRoute> routes = cRouteTable.findObjects
                            (new Condition("aend", "=", ctpDn).or(new Condition("zend", "=", ctpDn))
                                    .or(new Condition("aends","like",ctpDn).or(new Condition("zends","like",ctpDn))));

                    if (routes.isEmpty()) {
                        continue;
                    }
                    T_CRoute snc = routes.get(0);
                    if (ctpDn.equals(snc.getAend()) || (snc.getAends() != null && snc.getAends().contains(ctpDn) )) {
                        if (snc.getZend() != null)
                            zctpDns = snc.getZend();
                        else if (snc.getZends() != null)
                            zctpDns = snc.getZends();
                    }
                    else  {
                        if (snc.getAend() != null)
                            zctpDns = snc.getAend();
                        else if (snc.getAends() != null)
                            zctpDns = snc.getAends();
                    }

                    allZctpDnList += "||"+zctpDns;

                    cRoutes.add(snc);

                }

                if (zctpDns == null) {
                    errorLog("无法找到mp另外一端的ctp，mp="+mp+" 本端ctp size = "+ctps.size());
                } else {

                    String[] zctpDnArray = zctpDns.split(Constant.listSplitReg);
                    boolean find = false;
                    for (String zctpDn : zctpDnArray) {
                        String zmpDn = ctp_mp_map.get(zctpDn);
                        if (zmpDn == null) continue;
                        else if (processedMps.contains(zmpDn)) {
                            find = true;
                            break;
                        }
                        else {
                            find = true;
                            String zmac = (String)mp_mac_map.get(zmpDn,0);
                            if (zmac == null) {
                                errorLog("无法找到mp对应的mac，mp="+mp);
                                continue;
                            }
                            CEthTrunk cEthTrunk = new CEthTrunk();
                            cEthTrunk.setEmsName(emsdn);
                            cEthTrunk.setDn(mp + "<>" + zmpDn);
                            cEthTrunk.setAptp(mp);
                            cEthTrunk.setZptp(zmpDn);
                            cEthTrunk.setTmRate(bandwidh+"M");
                            //           cEthTrunk.setRate();
                            cEthTrunk.setAptpId(DatabaseUtil.getSID(CPTP.class, mp));
                            cEthTrunk.setZptpId(DatabaseUtil.getSID(CPTP.class, zmpDn));
                            cEthTrunk.setDirection(((HW_EthService) (mp_mac_map.get(zmpDn, 1))).getDirection());
                            cEthTrunk.setName(((HW_EthService) (mp_mac_map.get(zmpDn, 1))).getNativeEMSName());
                            //   cEthTrunk.sett
                            di.insert(cEthTrunk);


                            CEthRoute cEthRoute = new CEthRoute();
                            cEthRoute.setEmsName(emsdn);
                            cEthRoute.setName(((HW_EthService) (mp_mac_map.get(zmpDn, 1))).getNativeEMSName());
                            cEthRoute.setTmRate(bandwidh+"M");
                            cEthRoute.setDn(amac + "<>" + zmac);
                            cEthRoute.setAptp(amac);
                            cEthRoute.setZptp(zmac);
                            //           cEthRoute.setRate();
                            cEthRoute.setAptpId(DatabaseUtil.getSID(CPTP.class, amac));
                            cEthRoute.setZptpId(DatabaseUtil.getSID(CPTP.class, zmac));
                            cEthRoute.setDirection(((HW_EthService)(mp_mac_map.get(zmpDn,1))).getDirection());
                            //di.insert(cEthRoute);
                            ethRouteList.add(cEthRoute);

                            CEthRoute_ETHTrunk cEthRoute_ethTrunk = new CEthRoute_ETHTrunk();
                            cEthRoute_ethTrunk.setEthTrunkDn(cEthTrunk.getDn());
                            cEthRoute_ethTrunk.setEthRouteDn(cEthRoute.getDn());
                            cEthRoute_ethTrunk.setEthTrunkId(cEthTrunk.getSid());
                            cEthRoute_ethTrunk.setEthRouteId(cEthRoute.getSid());
                            cEthRoute_ethTrunk.setDn(SysUtil.nextDN());
                            cEthRoute_ethTrunk.setEmsName(emsdn);
                            di.insert(cEthRoute_ethTrunk);

                            HW_EthService staticRoute1 = (HW_EthService) mp_mac_map.get(mp, 1);
                            HW_EthService staticRoute2 = (HW_EthService) mp_mac_map.get(zmpDn, 1);
                            CEthRoute_StaticRoute r1 = new CEthRoute_StaticRoute();
                            r1.setEmsName(emsdn);
                            r1.setDn(SysUtil.nextDN());
                            r1.setEthRouteDn(cEthRoute.getDn());
                            r1.setEthRouteId(cEthRoute.getSid());
                            r1.setStaticRouteDn(staticRoute1.getDn());
                            r1.setStaticRouteId(DatabaseUtil.getSID(CStaticRoute.class, staticRoute1.getDn()));
                            di.insert(r1);

                            CEthRoute_StaticRoute r2 = new CEthRoute_StaticRoute();
                            r2.setEmsName(emsdn);
                            r2.setDn(SysUtil.nextDN());
                            r2.setEthRouteDn(cEthRoute.getDn());
                            r2.setEthRouteId(cEthRoute.getSid());
                            r2.setStaticRouteDn(staticRoute2.getDn());
                            r2.setStaticRouteId(DatabaseUtil.getSID(CStaticRoute.class,staticRoute2.getDn()));
                            di.insert(r2);


                            HashSet<String> sdhroutedns = new HashSet<String>();
                            for (T_CRoute cRoute : cRoutes) {
                                if (sdhroutedns.contains(cRoute.getDn()))
                                    continue;
                                CEthTrunk_SDHRoute ethTrunk_sdhRoute = new CEthTrunk_SDHRoute();
                                ethTrunk_sdhRoute.setSdhRouteDn(cRoute.getDn());
                                ethTrunk_sdhRoute.setEthTrunkDn(cEthTrunk.getDn());
                                ethTrunk_sdhRoute.setSdhRouteId(cRoute.getSid());
                                ethTrunk_sdhRoute.setEthTrunkId(cEthTrunk.getSid());
                                ethTrunk_sdhRoute.setDn(SysUtil.nextDN());
                                ethTrunk_sdhRoute.setEmsName(emsdn);

                                di.insert(ethTrunk_sdhRoute);

                                sdhroutedns.add(cRoute.getDn());
                            }


                        }

                    }

                    if (!find) {
                        errorLog("无法找到ctp对应的mp,ctp=" + allZctpDnList);
                        zctpDnArray = allZctpDnList.split(Constant.listSplitReg);
                        String portDn = ctpInSamePtps(zctpDnArray);
                        if (portDn != null && zctpDnArray.length > 1) {
                            getLogger().info("相同PORT下! zctpdns = "+allZctpDnList+" ;portdn = "+portDn);

                            CEthTrunk cEthTrunk = new CEthTrunk();
                            cEthTrunk.setEmsName(emsdn);
                            cEthTrunk.setDn(mp + "<>" + portDn);
                            cEthTrunk.setAptp(mp);
                            cEthTrunk.setZptp(portDn);
                            cEthTrunk.setTmRate(bandwidh+"M");
                            //           cEthTrunk.setRate();
                            cEthTrunk.setAptpId(DatabaseUtil.getSID(CPTP.class, mp));
                            cEthTrunk.setZptpId(DatabaseUtil.getSID(CPTP.class, portDn));
                            cEthTrunk.setDirection(((HW_EthService) (mp_mac_map.get(mp, 1))).getDirection());
                            cEthTrunk.setName(((HW_EthService) (mp_mac_map.get(mp, 1))).getNativeEMSName());
                            //   cEthTrunk.sett
                            di.insert(cEthTrunk);


                            CEthRoute cEthRoute = new CEthRoute();
                            cEthRoute.setEmsName(emsdn);
                            cEthRoute.setName(((HW_EthService) (mp_mac_map.get(mp, 1))).getNativeEMSName());
                            cEthRoute.setTmRate(bandwidh+"M");
                            cEthRoute.setDn(amac + "<>" + portDn);
                         //   cEthRoute.setDn(SysUtil.nextDN());
                            cEthRoute.setAptp(amac);
                            cEthRoute.setZptp(portDn);
                            //           cEthRoute.setRate();
                            cEthRoute.setAptpId(DatabaseUtil.getSID(CPTP.class, amac));
                            cEthRoute.setZptpId(DatabaseUtil.getSID(CPTP.class, portDn));
                            cEthRoute.setDirection(((HW_EthService)(mp_mac_map.get(mp,1))).getDirection());
                        //    di.insert(cEthRoute);
                            ethRouteList.add(cEthRoute);

                            CEthRoute_ETHTrunk cEthRoute_ethTrunk = new CEthRoute_ETHTrunk();
                            cEthRoute_ethTrunk.setEthTrunkDn(cEthTrunk.getDn());
                            cEthRoute_ethTrunk.setEthRouteDn(cEthRoute.getDn());
                            cEthRoute_ethTrunk.setEthTrunkId(cEthTrunk.getSid());
                            cEthRoute_ethTrunk.setEthRouteId(cEthRoute.getSid());
                            cEthRoute_ethTrunk.setDn(SysUtil.nextDN());
                            cEthRoute_ethTrunk.setEmsName(emsdn);
                            di.insert(cEthRoute_ethTrunk);

                            HW_EthService staticRoute1 = (HW_EthService) mp_mac_map.get(mp, 1);
                            CEthRoute_StaticRoute r1 = new CEthRoute_StaticRoute();
                            r1.setEmsName(emsdn);
                            r1.setDn(SysUtil.nextDN());
                            r1.setEthRouteDn(cEthRoute.getDn());
                            r1.setEthRouteId(cEthRoute.getSid());
                            r1.setStaticRouteDn(staticRoute1.getDn());
                            r1.setStaticRouteId(DatabaseUtil.getSID(CStaticRoute.class, staticRoute1.getDn()));
                            di.insert(r1);




                            HashSet<String> sdhroutedns = new HashSet<String>();
                            for (T_CRoute cRoute : cRoutes) {
                                if (sdhroutedns.contains(cRoute.getDn()))
                                    continue;
                                CEthTrunk_SDHRoute ethTrunk_sdhRoute = new CEthTrunk_SDHRoute();
                                ethTrunk_sdhRoute.setSdhRouteDn(cRoute.getDn());
                                ethTrunk_sdhRoute.setEthTrunkDn(cEthTrunk.getDn());
                                ethTrunk_sdhRoute.setSdhRouteId(cRoute.getSid());
                                ethTrunk_sdhRoute.setEthTrunkId(cEthTrunk.getSid());
                                ethTrunk_sdhRoute.setDn(SysUtil.nextDN());
                                ethTrunk_sdhRoute.setEmsName(emsdn);

                                di.insert(ethTrunk_sdhRoute);

                                sdhroutedns.add(cRoute.getDn());
                            }


                        }

                    }
                }


            }
            getLogger().info("ETHROUTE LIST SIZE = "+ethRouteList.size());
            di.insertWithDupCheck(ethRouteList);
        } catch (Exception e) {
            getLogger().error(e,e);
        } finally {
            di.end();
        }


    }


    private String ctpInSamePtps(String[] ctpDns) {
        String portDn = null;
        for (String ctpDn : ctpDns) {
            String s = DNUtil.extractPortDn(ctpDn);
            if (portDn != null && !portDn.equals(s))
                return null;
            if (portDn == null) portDn = s;
        }
        return portDn;
    }



    private void migrateVB() throws Exception {
        List<HW_VirtualBridge> vbs = sd.queryAll(HW_VirtualBridge.class);
        if (vbs == null || vbs.isEmpty()) {
            getLogger().info("HW_VirtualBridge 无数据 ");
            return;
        }
        executeDelete("delete from CVirtualBridge c where c.emsName = '" + emsdn + "'", CVirtualBridge.class);
        DataInserter di = new DataInserter(emsid);
        try {

            for (HW_VirtualBridge vb : vbs) {
                CVirtualBridge cvb = transVB(vb);
                di.insert(cvb);
            }

        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {

            di.end();

        }



    }

    private CVirtualBridge transVB(HW_VirtualBridge vb) {
        CVirtualBridge cvb = new CVirtualBridge();
        cvb.setName(vb.getName());
        cvb.setEmsName(emsdn);
        cvb.setAdditionalInfo(vb.getAdditionalInfo());
        cvb.setDn(vb.getDn());

        {
            String equipmentdn = vb.getDn();
            equipmentdn = equipmentdn.replaceAll("VB:","EquipmentHolder:");
            equipmentdn = equipmentdn.substring(0,equipmentdn.lastIndexOf("/")) + "@Equipment:1";
            cvb.setEquipmentDn(equipmentdn);
        }


        cvb.setLogicalTPList(vb.getLogicalTPList());
        cvb.setParentDn(vb.getParentDn());
        cvb.setUserLabel(vb.getUserLabel());

        return cvb;
    }

    protected void migrateCTP() throws Exception {
    //    executeDelete("delete  from CCTP c where c.emsName = '" + emsdn + "'", CCTP.class);
        executeTableDelete("C_CTP", emsdn);
        getLogger().info("insertCtps will start... emsid= " + emsid);
        
        long count = sd.findObjectsCount("select count(c.id) from "+CTP.class.getSimpleName()+" c");
        getLogger().info("migrateCtp size = " + count + "--" + count/10000);
        
        String sql = "select c from " + CTP.class.getName() + " c";
        for (int i=0;i<count/10000+1;i++) {
        	getLogger().info("row start : " + i);
        	List<CTP> queryCtps = sd.query(sql, i*10000, 9999);
        	List<CCTP> list = insertCtps(queryCtps);
        	getLogger().info("   row insertEnd : " + i);
            for (CCTP cctp : list) {
                ctpTable.addObject(new T_CTP(cctp));
            }
            getLogger().info("   row end : " + i);
        }
        
        
//        sd.queryAllLazy(cls, limitPerQuery)
//        
//        List<CTP> ctps = sd.queryAll(CTP.class);
//        getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
//        if (ctps != null && ctps.size() > 0) {
//
//        }
//        List<CCTP> list = insertCtps(ctps);
//        for (CCTP cctp : list) {
//            ctpTable.addObject(new T_CTP(cctp));
//        }

    }

    @Override
    public CCTP transCTP(CTP ctp) {
        if (ctp.getRate() == null || ctp.getRate().isEmpty()) {
            String dn = ctp.getDn();
            if (dn.contains("vt2_tu12")) {
                ctp.setRate("11");
            }
            if (dn.contains("vc3")) {
                ctp.setRate("13");
            }
            if (CTPUtil.isVC4(dn)) {
                ctp.setRate("15");
            }
            ctp.setDirection("D_BIDIRECTIONAL");

        }

        CCTP cctp = super.transCTP(ctp);

        if (cctp.getDn().contains("vc4_4c")) {
            cctp.setTmRate("622M");
            cctp.setRateDesc("VC4_4c");
            cctp.setRate("16");
            cctp.setNativeEMSName("VC4_4c-"+cctp.getJ());
        }

        cctp.setParentCtpdn(DNUtil.getParentCTPdn(ctp.getDn()));

        String dn = cctp.getDn();


        return cctp;
    }
    public void migrateSNC() throws Exception {
        executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
        executeDelete("delete  from CRoute_CC c where c.emsName = '" + emsdn + "'", CRoute_CC.class);
        executeDelete("delete  from CPath c where c.emsName = '" + emsdn + "'", CPath.class);
        executeDelete("delete  from CChannel c where c.emsName = '" + emsdn + "'", CChannel.class);
        executeDelete("delete  from CRoute_Channel c where c.emsName = '" + emsdn + "'", CRoute_Channel.class);
        executeDelete("delete  from CPath_CC c where c.emsName = '" + emsdn + "'", CPath_CC.class);
        executeDelete("delete  from CPath_Channel c where c.emsName = '" + emsdn + "'", CPath_Channel.class);

        try {
            List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
            //   sncTable.addObjects(sncs);
            final HashMap<String,List<R_TrafficTrunk_CC_Section>> snc_cc_section_map = new HashMap<String, List<R_TrafficTrunk_CC_Section>>();
            List<R_TrafficTrunk_CC_Section> routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
            for (R_TrafficTrunk_CC_Section _route : routeList) {
                if (_route.getType().equals("CC")) {
                    _route.setCcOrSectionDn(DNUtil.compressCCDn(_route.getCcOrSectionDn()));
                }
                String sncDn = _route.getTrafficTrunDn();
                List<R_TrafficTrunk_CC_Section> value = snc_cc_section_map.get(sncDn);
                if (value == null) {
                    value = new ArrayList<R_TrafficTrunk_CC_Section>();
                    snc_cc_section_map.put(sncDn,value);
                }
                value.add(_route);
            }


            if (sncs == null || sncs.isEmpty()) {
                getLogger().error("SubnetworkConnection is empty");
            }
            List<SubnetworkConnection> sdhRoutes = new ArrayList<SubnetworkConnection>();
            List<SubnetworkConnection> paths = new ArrayList<SubnetworkConnection>();

            List<SubnetworkConnection> e4Routes = new ArrayList<SubnetworkConnection>();


            for (SubnetworkConnection snc : sncs) {
                if ((HWDic.LR_E1_2M.value+"").equals(snc.getRate())) {
                    sdhRoutes.add(snc);
                }  else if ((HWDic.LR_STS3c_and_AU4_VC4.value+"").equals(snc.getRate())
                        ) {
                    paths.add(snc);
                }  else if ((HWDic.LR_E3_34M.value+"").equals(snc.getRate())) {
                    sdhRoutes.add(snc);
                }  else if ((HWDic.LR_E4_140M.value+"").equals(snc.getRate())
                        || (HWDic.LR_STS12c_and_VC4_4c.value+"").equals(snc.getRate())
                        || (HWDic.LR_STS48c_and_VC4_16c.value+"").equals(snc.getRate())
                        || (HWDic.LR_STS192c_and_VC4_64c.value+"").equals(snc.getRate())
                        ) {

                    String actp = snc.getaEnd();
                    String zctp = snc.getzEnd();

                    List<T_CTP> achildCtps = ctpTable.findObjectByIndexColumn("parentCtp", actp);
                    if (achildCtps != null && achildCtps.size() > 0) {
                        paths.add(snc);
                        continue;
                    }
                    List<T_CTP> zchildCtps = ctpTable.findObjectByIndexColumn("parentCtp", zctp);
                    if (zchildCtps != null && zchildCtps.size() > 0) {
                        paths.add(snc);
                        continue;
                    }


                    if (snc.getaEndTrans().startsWith("15@")) {
                     //   paths.add(snc);
                        e4Routes.add(snc);
                    }
                    else
                        sdhRoutes.add(snc);
                } else {
                    getLogger().error("Unknown rate : "+snc.getRate()+"; snc="+snc.getDn());
                }
            }

            //CTP和高阶通道的映射表，在处理SDH 路由的时候会用来设置路由所属的高阶通道
            HashMap<String,String> ctpDnHighoderpathDn = new HashMap<String, String>();
            List<CPath> cpaths = new ArrayList<CPath>();
            HashMap<String,CPath> cpathMap = new HashMap<String, CPath>();
       //     List<CChannel> cChannels = new ArrayList<CChannel>();
            List<CPath_CC> cPath_ccs = new ArrayList<CPath_CC>();
            List<CPath_Channel> cPath_channels = new ArrayList<CPath_Channel>();

            List<CRoute> cRoutes = new ArrayList<CRoute>();
            List<CRoute_CC> cRoute_ccs = new ArrayList<CRoute_CC>();
            List<CRoute_Channel> cRoute_channels = new ArrayList<CRoute_Channel>();


            HashMap<String,List<CChannel>> cPath_ChannelMap = new HashMap<String, List<CChannel>>();

            int noRoutePath = 0;
            int noRouteRoute = 0;
            //处理高阶通道
             DataInserter diForCTP = new DataInserter(emsid);
            for (SubnetworkConnection snc : paths) {
                try {
                    makeupCTP("path",snc.getaEnd().split(Constant.listSplitReg),snc.getzEnd().split(Constant.listSplitReg),diForCTP);
                    makeupCTP("path",snc.getzEnd().split(Constant.listSplitReg),snc.getaEnd().split(Constant.listSplitReg),diForCTP);
                    CPath cPath = U2000MigratorUtil.transPath(emsdn,snc);
                    cpaths.add(cPath);
                    cpathMap.put(cPath.getDn(), cPath);

                    breakupCPaths(cPath);

                    List<R_TrafficTrunk_CC_Section> routes = snc_cc_section_map.get(snc.getDn());
                    if (routes == null) {
                        noRoutePath ++;
                     //   getLogger().error("无法找到path路由: path="+snc.getDn());
                        continue;
                    }
                    SDHRouteComputationUnit computationUnit = new SDHRouteComputationUnit(getLogger(),snc,routes,ctpTable,ccTable,emsdn,true,null);
                    computationUnit.setACtpChannelMap(highOrderCtpChannelMap);
                    computationUnit.compute();
                    ctpDnHighoderpathDn.putAll(computationUnit.getCtpDnHighoderpathDn());
                    List<CChannel> channels = computationUnit.getChannels();

              //      removeDuplicateDN(channels);
                    for (CChannel channel : channels) {
                  //      cChannels.add(channel);
                        cPath_channels.add(U2000MigratorUtil.createCPath_Channel(emsdn, channel, cPath));
                    }

                    cPath_ChannelMap.put(cPath.getDn(),channels);
                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {
                           // cPath_ccs.add(U2000MigratorUtil.createCPath_CC(emsdn, route.getCcOrSectionDn(), cPath));
                            Collection<? extends String> ccs = splitCCdns(route.getCcOrSectionDn());
                            for (String cc : ccs) {
                                cPath_ccs.add(U2000MigratorUtil.createCPath_CC(emsdn, cc, cPath));
                            }
                        }
                    }
                } catch (Exception e) {
                    getLogger().error("Process Path error "+e, e);
                }
            }

            getLogger().error("无法找到path路由: size="+ noRoutePath);

           //////////////////////////////////////////// E4 Routes///////////////////////////////////////////////////////////
            for (SubnetworkConnection snc : e4Routes) {
                try {
                    CRoute cRoute = U2000MigratorUtil.transRoute(emsdn, snc);
                    cRoutes.add(cRoute);
                    cRouteTable.addObject(new T_CRoute(cRoute)); 

                    List<R_TrafficTrunk_CC_Section> routes = snc_cc_section_map.get(snc.getDn());
                    if (routes == null) {
                        noRoutePath ++;
                        //   getLogger().error("无法找到path路由: path="+snc.getDn());
                        continue;
                    }
                    SDHRouteComputationUnit computationUnit = new SDHRouteComputationUnit(getLogger(),snc,routes,ctpTable,ccTable,emsdn,true,null);
                    computationUnit.setACtpChannelMap(highOrderCtpChannelMap);
                    computationUnit.compute();
                    ctpDnHighoderpathDn.putAll(computationUnit.getCtpDnHighoderpathDn());
                    List<CChannel> channels = computationUnit.getChannels();

                    //      removeDuplicateDN(channels);
                    for (CChannel channel : channels) {
                        //      cChannels.add(channel);
                        cRoute_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, channel, cRoute));
                    }


                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {
                            // cPath_ccs.add(U2000MigratorUtil.createCPath_CC(emsdn, route.getCcOrSectionDn(), cPath));
                            Collection<? extends String> ccs = splitCCdns(route.getCcOrSectionDn());
                            for (String cc : ccs) {
                                cRoute_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, cc, cRoute));
                            }
                        }
                    }
                } catch (Exception e) {
                    getLogger().error("Process Route "+ snc.getDn()+" error "+e, e);
                }
            }

            getLogger().error("无法找到E4路由: size="+ noRoutePath);
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


//            List<CTransmissionSystem_Channel>   ts_channels = new ArrayList<CTransmissionSystem_Channel>();
//            List<ProtectionSubnetworkLink> protectionSubnetworkLinks = sd.queryAll(ProtectionSubnetworkLink.class);
//            for (ProtectionSubnetworkLink link : protectionSubnetworkLinks) {
//
//                for (CPath cpath : cpaths) {
//
//                    HashSet aptps = new HashSet();
//                    HashSet zptps = new HashSet();
//                    if (cpath.getAptp() != null) {
//                        aptps.add(cpath.getAptp());
//                    } else if (cpath.getAptps() != null) {
//                        aptps.addAll(Arrays.asList(cpath.getAptps().split(Constant.listSplitReg)));
//                    }
//
//                    if (cpath.getZptp() != null) {
//                        zptps.add(cpath.getZptp());
//                    } else if (cpath.getZptps() != null) {
//                        zptps.addAll(Arrays.asList(cpath.getZptps().split(Constant.listSplitReg)));
//                    }
//                    if ((aptps.contains(link.getSrcTp()) &&
//                            zptps.contains(link.getSinkTp()) ) ||
//                            (aptps.contains(link.getSinkTp()) &&
//                                    zptps.contains(link.getSrcTp()))) {
//                        List<CChannel> cChannels = cPath_ChannelMap.get(cpath.getDn());
//                        if (cChannels != null) {
//                            for (CChannel cChannel : cChannels) {
//                                CTransmissionSystem_Channel sc = new CTransmissionSystem_Channel();
//                                sc.setTransmissionSystemDn(link.getProtectionSubnetworkDn());
//                                sc.setChannelDn(cChannel.getDn());
//                                sc.setEmsName(emsdn);
//                                sc.setDn(SysUtil.nextDN());
//                                ts_channels.add(sc);
//                            }
//                        }
//
//                    }
//                }
//
//            }


            for (SubnetworkConnection snc : sdhRoutes) {
                try {
                    makeupCTP("route",snc.getaEnd().split(Constant.listSplitReg),snc.getzEnd().split(Constant.listSplitReg),diForCTP);
                    makeupCTP("route",snc.getzEnd().split(Constant.listSplitReg),snc.getaEnd().split(Constant.listSplitReg),diForCTP);
                    CRoute cRoute = U2000MigratorUtil.transRoute(emsdn,snc);



                    List<R_TrafficTrunk_CC_Section> routes = snc_cc_section_map.get(snc.getDn());
                    if (routes == null) {
                        noRouteRoute ++;
                  //      getLogger().error("无法找到route路由: route="+snc.getDn());
                        continue;
                    }
                    SDHRouteComputationUnit computationUnit = new SDHRouteComputationUnit(getLogger(),snc,routes,ctpTable,ccTable,emsdn,false,cpathMap);
                    computationUnit.setCtpDnHighoderpathDn(ctpDnHighoderpathDn);
                    computationUnit.setACtpChannelMap(lowOrderCtpChannelMap);
                    computationUnit.compute();
                    List<CChannel> lowOrderChannels = computationUnit.getChannels();
                    removeDuplicateDN(lowOrderChannels);
                    if (lowOrderChannels == null || lowOrderChannels.isEmpty()) {
                        getLogger().error("无法找到Route的Channel: route="+snc.getDn());
                        continue;
                    }

                    cRouteTable.addObject(new T_CRoute(cRoute));
                    cRoutes.add(cRoute);

                    for (CChannel channel : lowOrderChannels) {
                        cRoute_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, channel, cRoute));
                    }
                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {
                         //   cRoute_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, route.getCcOrSectionDn(), cRoute));
                            Collection<? extends String> ccs = splitCCdns(route.getCcOrSectionDn());
                            for (String cc : ccs) {
                                cRoute_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, cc, cRoute));
                            }
                        }
                    }
                } catch (Exception e) {
                    getLogger().error("Process Route Error "+e, e);
                }
            }

            getLogger().error("无法找到route路由: size="+ noRouteRoute);
            diForCTP.end();


            DataInserter di = new DataInserter(emsid);
            removeDuplicateDN(cChannelList);
            di.insert(cChannelList);
       //     di.insert(ts_channels);
            di.insert(cRoutes);
            di.insert(cRoute_ccs);
            di.insert(cRoute_channels);

            di.insert(cpaths);
            di.insert(cPath_ccs);
            di.insert(cPath_channels);
            di.end();
        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {

        }

    }


    private Collection<? extends String> splitCCdns(String ccOrSectionDn) {
        List<String> ccdns = new ArrayList<String>();
        String[] az = ccOrSectionDn.split("<>");
        if (az != null && az.length == 2) {
            String a = az[0];
            String z = az[1];
            if (a.indexOf("/rack=") != a.lastIndexOf("/rack=")) {
                int i = a.lastIndexOf("/rack=");
                String a1 = a.substring(0,i);
                String a2 = a.substring(i);
                ccdns.add(a1+"<>"+z);
                ccdns.add(a2+"<>"+z);
            } else if (z.indexOf("/rack=") != z.lastIndexOf("/rack=")) {
                int i = z.lastIndexOf("/rack=");
                String z1 = z.substring(0,i);
                String z2 = z.substring(i);
                ccdns.add(a+"<>"+z1);
                ccdns.add(a+"<>"+z2);
            } else {
                ccdns.add(ccOrSectionDn);
            }
        } else {
            getLogger().error("strange ccdn : " + ccOrSectionDn);
            ccdns.add(ccOrSectionDn);
        }
        return ccdns;

    }



    private void breakupCPaths(CPath path) {
        String aends = path.getAend();
        if (aends == null || aends.isEmpty())
            aends = path.getAends();
        String zends = path.getZend();
        if (zends == null || zends.isEmpty())
            zends = path.getZends();

        if (aends == null || aends.isEmpty() || zends == null || zends.isEmpty()) {
            getLogger().error("CPATH 有一端为空，"+path.getDn());
            return;
        }

        String[] aendCtps = aends.split(Constant.listSplitReg);
        String[] zendCtps = zends.split(Constant.listSplitReg);

        for (String aend : aendCtps) {
            for (String zend : zendCtps) {
                if (aend != null && zend != null) {
                    if (CTPUtil.isVC4(aend) && CTPUtil.isVC4(zend)) {

                        try {
                            List<T_CTP> achildCtps = ctpTable.findObjectByIndexColumn("parentCtp", aend);
                            List<T_CTP> zchildCtps = ctpTable.findObjectByIndexColumn("parentCtp", zend);
                            for (T_CTP achildCtp : achildCtps) {
                                for (T_CTP zchildCtp : zchildCtps) {
                                    String asimpleName = DNUtil.extractCTPSimpleName(achildCtp.getDn());
                                    if (asimpleName.contains("/") && !asimpleName.endsWith("/"))
                                        asimpleName = asimpleName.substring(asimpleName.lastIndexOf("/"));
                                    String zsimpleName = DNUtil.extractCTPSimpleName(zchildCtp.getDn());
                                    if (zsimpleName.contains("/") && !zsimpleName.endsWith("/"))
                                        zsimpleName = zsimpleName.substring(zsimpleName.lastIndexOf("/"));
                                    //sts3c_au4-j=2/vt2_tu12-k=3-l=5-m=1
                                    if (asimpleName.equals(zsimpleName)){
                                        createCChannel(achildCtp,zchildCtp,path);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            getLogger().error(e, e);
                        }

                    }
                }
            }
        }



    }


    @Override
    public  CPTP transPTP(PTP ptp) {
        CPTP cptp = U2000MigratorUtil.transPTP(ptp);
        if (cptp.getType().equals("LP")) {
           String vbdn = cptp.getDn();
            //EMS:NBO-T2000-10-P@ManagedElement:328648@PTP:/rack=1/shelf=1/slot=2/domain=eth/type=lp/vb=1/port=11

            String vb = DNUtil.extractValue(vbdn,"vb");
            vbdn = vbdn.replaceAll("PTP:", "VB:");
            int i = vbdn.indexOf("/",vbdn.indexOf("slot"));
            vbdn = vbdn.substring(0,i)+"/vb="+vb;

            cptp.setOwner(vbdn);

            //EMS:NBO-T2000-10-P@ManagedElement:598847@VB:/rack=1/shelf=1/slot=3/vb=2
        }

        if (cptp.getDn().contains("type=mp") && "40G".equals(cptp.getSpeed())) {
                cptp.setSpeed("");
        } else if ("40G".equals(cptp.getSpeed())) {
            cptp.setSpeed("1000M");
        }


        if (cptp.getEoType() == DicConst.EOTYPE_ELECTRIC && "OPTICAL".equals(cptp.getType()))
            cptp.setType("ELECTRICAL");
        return cptp;
    }

    @Override
    protected void migrateSection() throws Exception {
        executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "'", CSection.class);
        DataInserter di = new DataInserter(emsid);
        List<Section> sections = sd.queryAll(Section.class);

        if (sections != null && sections.size() > 0) {
            for (Section section : sections) {
                CSection csection = transSection(section);
                csection.setSid(DatabaseUtil.nextSID(csection));
                // csection.setSid(toSid(Long.parseLong(section.getDn().substring(section.getDn().lastIndexOf(" - ") + 3))));
                String aendtp = csection.getAendTp();
                String zendtp = csection.getZendTp();
                if (aendtp.contains("CTP") || zendtp.contains("CTP")) {
                    continue;
                }
                csection.setAptpId(DatabaseUtil.getSID(CPTP.class, aendtp));
                csection.setZptpId(DatabaseUtil.getSID(CPTP.class, zendtp));
                csection.setType("OMS");
                di.insert(csection);

                cSections.add(csection);
                //sectionTable.addObject(section);
            }
        }
        di.end();

        breakupSections(cSections);
        getLogger().info("打散高阶时隙数:" + highOrderCtpChannelMap.size());
    }


    public void breakupSections(List<CSection> sections) {
        for (CSection section : sections) {
            String aendTp = section.getAendTp();
            String zendTp = section.getZendTp();

//            List<CCTP> actps = this.findObjects(CCTP.class, "select c from CCTP c where c.portdn = '" + aendTp + "'");
//            List<CCTP> zctps = this.findObjects(CCTP.class, "select c from CCTP c where c.portdn = '" + zendTp + "'");

            List<T_CTP> actps = null;
            List<T_CTP> zctps = null;
            try {
                actps = ctpTable.findObjectByIndexColumn("portdn",aendTp);
                zctps = ctpTable.findObjectByIndexColumn("portdn", zendTp);
            } catch (Exception e) {
                getLogger().error(e, e);
            }
            if (actps == null) {
                getLogger().error("无法找到端口下的ctp:"+aendTp);
                continue;
            }
            if (zctps == null) {
                getLogger().error("无法找到端口下的ctp:"+zendTp);
                continue;
            }
            for (T_CTP actp : actps) {
                if (CTPUtil.isVC4(actp.getDn())) {
                    int j = CTPUtil.getJ(actp.getDn());

                    for (T_CTP zctp : zctps) {
                        if (CTPUtil.isVC4(zctp.getDn()) && (CTPUtil.getJ(zctp.getDn()) == j)) {
                             createCChannel(actp,zctp,section);
                        }
                    }
                }
            }
        }

    }

    private void createCChannel(T_CTP aCtp, T_CTP zCtp,Object parent)  {
        String aSideCtp = aCtp.getDn();
        String zSideCtp = zCtp.getDn();
        String duplicateDn = (zSideCtp+"<>"+aSideCtp);
//        if (channelMap.get(duplicateDn)!= null)
//            return;





        String nativeEMSName = null;
        String rate = null;
        if (aCtp != null) {
            nativeEMSName = aCtp.getNativeEMSName();
            rate = aCtp.getRate();
        } else if (zCtp != null) {
            nativeEMSName = zCtp.getNativeEMSName();
            rate = zCtp.getRate();
        }
        CChannel cChannel = new CChannel();
        cChannel.setDn(aSideCtp + "<>" + zSideCtp);
        cChannel.setSid(DatabaseUtil.nextSID(CChannel.class));
        cChannel.setAend(aSideCtp);
        cChannel.setZend(zSideCtp);
  //      cChannel.setSectionOrHigherOrderDn(sectionRoute.getCcOrSectionDn());
        cChannel.setName(nativeEMSName);
        cChannel.setNo(nativeEMSName);
        cChannel.setRate(rate);




        cChannel.setTmRate(SDHUtil.getTMRate(rate));
        cChannel.setRateDesc(SDHUtil.rateDesc(rate));

//        if (ctp != null)
//            cChannel.setDirection(ctp.getDirection());




        cChannel.setAptp(aCtp.getPortdn());
        cChannel.setZptp(aCtp.getPortdn());
        cChannel.setEmsName(emsdn);
        if (parent instanceof CSection) {
            cChannel.setCategory("SDH高阶时隙");
            cChannel.setDirection(((CSection)parent).getDirection());
            cChannel.setSectionOrHigherOrderDn(((CSection)parent).getDn());
            highOrderCtpChannelMap.put(cChannel.getAend(), cChannel);
            highOrderCtpChannelMap.put(cChannel.getZend(), cChannel);

            DSUtil.putIntoValueList(vc4ChannelMap, ((CSection) parent).getDn(),cChannel);

        }
        if (parent instanceof CPath) {
            cChannel.setCategory("SDH低阶时隙");
            cChannel.setSectionOrHigherOrderDn(((CPath)parent).getDn());
            cChannel.setDirection(((CPath)parent).getDirection());
            lowOrderCtpChannelMap.put(cChannel.getAend(),cChannel);
            lowOrderCtpChannelMap.put(cChannel.getZend(),cChannel);

        }


        cChannelList.add(cChannel);

    }

    protected void migrateCCOld() throws Exception {
        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        DataInserter di = new DataInserter(emsid);
        try {
            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));
                    CCrossConnect ccc = transCC(cc);
                    ccc.setSid(DatabaseUtil.nextSID(CCrossConnect.class));
                    if (ccc.getDn().length() > 240)
                        System.out.println("ccc = " + ccc.getDn());
                    di.insert(ccc);

                    ccTable.addObject(new T_CCrossConnect(ccc));


                    String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
                    String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);

//                    makeupCTP(actps,zctps,di);
//                    makeupCTP(actps,zctps,di);

                }
            }
        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

    }

    protected void migrateCC() throws Exception {
     //   executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        executeTableDelete("C_CROSSCONNECT",emsdn);
        DataInserter di = new DataInserter(emsid);
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        try {
            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));



                    String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
                    String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);

                    newCCs.addAll(U2000MigratorUtil.transCCS(cc,emsdn));
                    makeupCTP("CC", actps, zctps, di);
                    makeupCTP("CC", zctps, actps, di);

                }
            }

            removeDuplicateDN(newCCs);
            for (CCrossConnect ccc : newCCs) {
                if (ccc.getId() != null)
                    System.out.println("ccc = " + ccc);
                di.insert(ccc);
                ccTable.addObject(new T_CCrossConnect(ccc));
            }

        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

    }

    private void makeupCTP (String tag,String[] actps,String[] zctps,DataInserter di) throws Exception {
//        for (String actp : actps) {
//            T_CTP ctp = ctpTable.findObjectByDn(actp);
//            if (ctp == null) {
//                T_CTP zctp = ctpTable.findObjectByDn(zctps[0]);
//                if (zctp != null) {
//                    CCTP cctp = new CCTP();
//                    cctp.setDn(actp);
//                    cctp.setNativeEMSName(zctp.getNativeEMSName());
////                    cctp.setDirection((zctp.getDirection()));
//                     cctp.setRate(zctp.getRate());
//                    cctp.setRateDesc(SDHUtil.rateDesc(zctp.getRate()));
//                    cctp.setTmRate(SDHUtil.getTMRate(zctp.getRate()));
//                    cctp.setNativeEMSName(zctp.getNativeEMSName());
//                    cctp.setEmsName(emsdn);
//                    cctp.setPortdn(DNUtil.extractPortDn(actp));
//                //    cctp.setType(zctp.getType());
//                    cctp.setTag1("MAKEUP");
//                    cctp.setTag2(tag);
//                    di.insert(cctp);
//
//
//                    ctpTable.addObject(new T_CTP(cctp));
//                }
//
//            }
//        }

    }




//    protected  CCrossConnect transCC(CrossConnect src) {
//        CCrossConnect des = new CCrossConnect();
//        des.setDn(src.getDn());
//        des.setCollectTimepoint(src.getCreateDate());
//        des.setCcType(src.getCcType());
//        des.setDirection(src.getDirection());
//        //TODO
//
//        String aend = src.getaEndNameList();
//        String zend = src.getzEndNameList();
//        if (aend.contains(Constant.listSplit)) {
//            String[] split = aend.split(Constant.listSplitReg);
//            des.setAend(split[0]);
//            if (split != null && split.length == 2) {
//                des.setAend2(split[1]);
//            }
//            if (split != null && split.length == 3) {
//                des.setAend3(split[2]);
//            }
//
//            String aendtp = src.getaEndTP();
//            String[] split1 = aendtp.split(Constant.listSplitReg);
//            des.setAptp(split1[0]);
//            if (split1.length == 2)
//                des.setAptp2(split1[1]);
//
//        } else {
//            des.setAend(aend);
//            des.setAptp(src.getaEndTP());
//
//        }
//
//        if (des.getAend().length() > 200) {
//            System.out.println("aend = " + des.getAend());
//        }
//
//
//        if (zend.contains(Constant.listSplit)) {
//            String[] split = zend.split(Constant.listSplitReg);
//            des.setZend(split[0]);
//            if (split != null && split.length == 2) {
//                des.setZend2(split[1]);
//            }
//            if (split != null && split.length > 2) {
//                des.setZend3(split[2]);
//            }
//
//            String zendtp = src.getzEndTP();
//            String[] split1 = zendtp.split(Constant.listSplitReg);
//            des.setZptp(split1[0]);
//            if (split1.length > 1)
//                des.setZptp2(split1[1]);
//        } else {
//            des.setZend(zend);
//            des.setZptp(src.getzEndTP());
//
//        }
//
//
//        des.setParentDn(src.getParentDn());
//         des.setEmsName(emsdn);
//        des.setAdditionalInfo(src.getAdditionalInfo());
//        return des;
//    }




    public static void main(String[] args) throws Exception {
        String s = "EMS:NBO-T2000-10-P@ManagedElement:1245313@PTP:/rack=1/shelf=1/slot=2/domain=eth/type=mac/port=2@EMS:NBO-T2000-10-P@ManagedElement:1245313@PTP:/rack=1/shelf=1/slot=2/domain=eth/type=mp/port=2";

        String[] split = s.split("@EMS");
        System.out.println();
//        FileLogger fileLogger = new FileLogger("fff.log");
//        fileLogger.info("abc");
        URL resource = HWU2000SDHMigratorbak.class.getClassLoader().getResource("META-INF/persistence.xml");
        System.out.println("resource = " + resource);
        String fileName=  "D:\\cdcpdb\\nm\\2015-01-22-095201-Huawei.HuHeHaoTe2-DayMigration.db";
        String emsdn = "Huawei.HuHeHaoTe2";
        if (args != null && args.length > 0)
            fileName = args[0];
        if (args != null && args.length > 1)
            emsdn = args[1];
        boolean b = true;


//        String[] split = "abc".split(Constant.listSplitReg);
        //   int length = "[EMS:NBO-T2000-10-P@ManagedElement:598826@PTP:/rack=1/shelf=1/slot=11/domain=sdh/port=1@CTP:/sts3c_au4-j=3/vt2_tu12-k=3-l=3-m=3] ".length();
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

        HWU2000SDHMigratorbak loader = new HWU2000SDHMigratorbak (fileName, emsdn){
            public void afterExecute() {
                updateEmsStatus(Constants.CEMS_STATUS_READY);
                printTableStat();
                IrmsClientUtil.callIRMEmsMigrationFinished(emsdn);
            }
        };
        loader.execute();
    }


}
