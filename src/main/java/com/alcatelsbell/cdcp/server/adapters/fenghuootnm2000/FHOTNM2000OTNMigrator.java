package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.adapters.*;
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
import org.apache.log4j.Logger;
import org.asb.mule.probe.framework.entity.*;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.FileLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Min.d.Wu
 * Date: 14-10-16
 * Time: 下午4:43
 */
public class FHOTNM2000OTNMigrator extends AbstractDBFLoader{

    HashMap<String,List<CCTP>> ctpParentChildMap = new HashMap<String, List<CCTP>>();
    protected HashMap<String,String> deviceOMSRate = new HashMap<String, String>();
    HashMap<String,CPTP>  ptpMap = new HashMap<String, CPTP>();
    HashMap<String,CCTP>  ctpMap = new HashMap<String, CCTP>();
    HashMap<String,List<CCrossConnect>> aptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CCrossConnect>> ptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CSection>> ptpSectionMap = new HashMap<String, List<CSection>>();
    HashMap<String,List<CCTP>> ptp_ctpMap = new HashMap<String, List<CCTP>>();
    List<CSection> cSections = new ArrayList<CSection>();
    HashMap<String,CEquipment> equipmentMap = new HashMap<String, CEquipment>();

    public FHOTNM2000OTNMigrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog(emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }

    private static FileLogger logger = new FileLogger("FH-OTN-Device.log");
    public FHOTNM2000OTNMigrator(Serializable object, String emsdn) {
        this.emsdn = emsdn;
        this.resultObject = object;
        MigrateThread.thread().initLog(logger);
    }

    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "烽火");
      //   FHOtnUtil.testOTN(sd);
        logAction(emsdn + " migrateManagedElement", "同步网元", 1);
        //test();
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

        //根据CrossConnect表有无数据采用不同的处理方法
//        if (isHaveCCRecord()){
//            migrateCC();
//
//        } else {
        	migrateRouteCC();
            migrateOMS();
//        }
        sd.release();
//        logAction("migrateSection", "同步段", 25);
//        migrateSection();
//
//        logAction("migrateCTP", "同步CTP", 30);
//        migrateCTP();
//          migrateSubnetworkConnection();
    }

    protected Class[] getStatClss() {
        return new Class[]{CCrossConnect.class, CChannel.class, CPath.class, CRoute.class, CPath_Channel.class,
                CPath_CC.class, CRoute_Channel.class,
                CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class, CVirtualBridge.class,
                CMP_CTP.class, CEthTrunk.class, CStaticRoute.class, CEthRoute.class, CEthTrunk_SDHRoute.class,
                CEthRoute_StaticRoute.class, CEthRoute_ETHTrunk.class, CSection.class, CCTP.class, CDevice.class, CPTP.class, CTransmissionSystem.class, CTransmissionSystem_Channel.class};
    }





    @Override
    public CDevice transDevice(ManagedElement me) {
        CDevice device = super.transDevice(me);
        String supportedRates = me.getSupportedRates();
        if (supportedRates != null && (supportedRates.contains("334") || supportedRates.contains("339")))
            deviceOMSRate.put(me.getDn(),"100G");
        else
            deviceOMSRate.put(me.getDn(),"40G");

        return device;
    }


    HashMap<String,CRack> rackMap = new HashMap<String, CRack>();
    public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
        CdcpObject cdcpObject = super.transEquipmentHolder(equipmentHolder);
        if (cdcpObject instanceof CRack) {
            String add = ((CRack) cdcpObject).getAdditionalInfo();
            HashMap<String, String> map = MigrateUtil.transMapValue(add);
            if (map.get("Sequence") != null)
                ((CRack) cdcpObject).setNo(map.get("Sequence"));

            rackMap.put(cdcpObject.getDn(),(CRack)cdcpObject);
        }
        if (cdcpObject instanceof CSlot) {
            String additionalInfo = equipmentHolder.getAdditionalInfo();
            Map<String, String> map = MigrateUtil.transMapValue(additionalInfo);
             if (((CSlot) cdcpObject).getAcceptableEquipmentTypeList().length() > 2000)
                 ((CSlot) cdcpObject).setAcceptableEquipmentTypeList(null);
            String nativeEMSName = ((CSlot) cdcpObject).getNativeEMSName();
            if (nativeEMSName.contains("0X")) {
                String no = nativeEMSName.substring(nativeEMSName.lastIndexOf("0X") + 2);
//            if (no.length() == 2)
//                no = "0"+no.substring(1);
                ((CSlot) cdcpObject).setNo(no);
            } else if (nativeEMSName.contains("SLOT_")){
                String no = nativeEMSName.substring(nativeEMSName.lastIndexOf("SLOT_") + 5);
                ((CSlot) cdcpObject).setNo(no);

            } else {
                ((CSlot) cdcpObject).setNo(nativeEMSName);
            }
        }
        if (cdcpObject instanceof CShelf) {
            String additionalInfo = equipmentHolder.getAdditionalInfo();
            Map<String, String> map = MigrateUtil.transMapValue(additionalInfo);
            ((CShelf) cdcpObject).setShelfType(map.get("DetailKind"));
            String rackDn = ((CShelf) cdcpObject).getRackDn();
            CRack rack = rackMap.get(rackDn);
            if (rack != null)
                ((CShelf) cdcpObject).setNo(rack.getNo()+"-"+((CShelf) cdcpObject).getNo());
            //     ((CShelf) cdcpObject).setNo(equipmentHolder.getNativeEMSName());
        }
        return cdcpObject;
    }
    public CEquipment transEquipment(Equipment equipment) {
        CEquipment cEquipment = super.transEquipment(equipment);
        String additionalInfo = equipment.getAdditionalInfo();
        equipmentMap.put(cEquipment.getDn(),cEquipment);
        return cEquipment;
    }

    @Override
    public CCTP transCTP(CTP ctp) {
        CCTP cctp = super.transCTP(ctp);
        if (cctp.getNativeEMSName() == null || cctp.getNativeEMSName().isEmpty()) {
            cctp.setNativeEMSName(ctp.getDn().substring(ctp.getDn().indexOf("CTP:/")+5));
        }
        String transmissionParams = cctp.getTransmissionParams();
        Map<String, String> map = MigrateUtil.transMapValue(transmissionParams);
        cctp.setFrequencies(map.get("Frequency"));
        if (transmissionParams.length() > 2000)
            cctp.setTransmissionParams(transmissionParams.substring(0, 2000));


//        String dn = cctp.getDn();
//        int i = dn.indexOf("/", dn.indexOf("CTP:/") + 6);
//        if (i > -1) {
//            String parentDn = dn.substring(0,dn.lastIndexOf("/"));
//            cctp.setParentCtpdn(parentDn);
//            DSUtil.putIntoValueList(ctpParentChildMap,parentDn,cctp);
//        }

        setCTPRateDescAndTmRate(cctp);

        return cctp;
    }

    protected void setCTPRateDescAndTmRate(CCTP cctp) {

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
        if (oduName.contains("odu2e")) {
            cctp.setTmRate("10G");
            cctp.setRateDesc("ODU2E");
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

        if (oduName.contains("dsr")) {
            CPTP cptp = ptpMap.get(DNUtil.extractPortDn(ctpDn));
            if (cptp != null)
                cctp.setTmRate(cptp.getSpeed());

            cctp.setRateDesc("DSR");
        }
        if (oduName.contains("ethernet")) {
            CPTP cptp = ptpMap.get(DNUtil.extractPortDn(ctpDn));
            if (cptp != null)
                cctp.setTmRate(cptp.getSpeed());

            cctp.setRateDesc("ethernet");
        }



//        if (ctpDn.contains("odu0="))  {
//            cctp.setTmRate("1.25G");
//            cctp.setRateDesc("ODU0");
//        }

    }

    
    protected boolean isHaveCCRecord() {
        int ccCount = ((Number)sd.query("select count(*) from CrossConnect").get(0)).intValue();
        if (ccCount == 0) return false;
        return true;
    }
    
    protected void migrateRouteCC() throws Exception {
        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        DataInserter di = new DataInserter(emsid);
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        HashSet<String> ccDnSet = new HashSet<String>();
        try {
            List<R_TrafficTrunk_CC_Section> routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
            if (routeList == null || routeList.isEmpty()){
            	return;
            }
        	for (R_TrafficTrunk_CC_Section route : routeList){        		
        		if (route.getType().equals("CC") && !ccDnSet.contains(route.getCcOrSectionDn())){
        			ccDnSet.add(route.getCcOrSectionDn());
        			newCCs.add(OTNM2000MigratorUtil.createCrossConnect(emsdn, route));
        		}           		
        	}

            removeDuplicateDN(newCCs);
            di.insert(newCCs);

        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

    }


    protected void migrateCC() throws Exception {
        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        DataInserter di = new DataInserter(emsid);
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        try {
            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));


                    List<CCrossConnect> splitCCS = OTNM2000MigratorUtil.transCCS(cc, emsdn);
                    newCCs.addAll(splitCCS);

                    for (CCrossConnect ncc : splitCCS) {
                        DSUtil.putIntoValueList(aptpCCMap,ncc.getAptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getZptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getAptp(),ncc);
                    }
                }
            }

            removeDuplicateDN(newCCs);
            di.insert(newCCs);

        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

    }


//    protected List insertCtps(List<CTP> ctps) throws Exception {
//        DataInserter di = new DataInserter(emsid);
//        getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
//        List<CCTP> cctps = new ArrayList<CCTP>();
//        if (ctps != null && ctps.size() > 0) {
//            for (CTP ctp : ctps) {
//                CCTP cctp = transCTP(ctp);
//                if (cctp != null) {
//                    cctps.add(cctp);
//                    DSUtil.putIntoValueList(ptp_ctpMap, cctp.getParentDn(), cctp);
//                    ctpMap.put(cctp.getDn(),cctp);
//                    di.insert(cctp);
//                }
//            }
//        }
//
//        di.end();
//        return cctps;
//    }

    @Override
    protected List insertCtps(List<CTP> ctps) throws Exception {
        DataInserter di = new DataInserter(emsid);
        getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
        List<CCTP> cctps = new ArrayList<CCTP>();
        HashMap<String,List<CCTP>> portCtps = new HashMap<String, List<CCTP>>();
        if (ctps != null && ctps.size() > 0) {
            for (CTP ctp : ctps) {
                CCTP cctp = transCTP(ctp);
                if (cctp != null) {
                    cctps.add(cctp);
                    DSUtil.putIntoValueList(ptp_ctpMap, cctp.getParentDn(), cctp);
                    ctpMap.put(cctp.getDn(),cctp);
                    if (cctp.getPortdn() == null || cctp.getPortdn().trim().isEmpty())
                        System.out.println("cctp = " + cctp.getDn());
                    DSUtil.putIntoValueList(portCtps,ctp.getPortdn(),cctp);

                }
            }
        }

        for (String portDn : portCtps.keySet()) {
            List<CCTP> cs = portCtps.get(portDn);
            List<CCTP> toBeRemoved = new ArrayList<CCTP>();
            for (CCTP c : cs) {
                if (containsParentCtp(cs,c.getDn()) != null && containsSubCtps(cs,c.getDn())) {
                    toBeRemoved.add(c);
                }
            }
            cs.removeAll(toBeRemoved);
            cctps.removeAll(toBeRemoved);

            for (CCTP c : cs) {
                CCTP parent = containsParentCtp(cs, c.getDn());
                if (parent != null) {
                    c.setParentCtpdn(parent.getDn());
                    DSUtil.putIntoValueList(ctpParentChildMap,parent.getDn(),c);
                }

            }
        }
        di.insert(cctps);
        di.end();
        return cctps;
    }
    protected   boolean containsSubCtps(List<CCTP> ctps,String ctpdn) {
        for (CCTP ctp : ctps) {
            if (!ctp.getDn().equals(ctpdn) && ctp.getDn().startsWith(ctpdn+"/"))
                return true;
        }
        return false;
    }
    protected  CCTP containsParentCtp(List<CCTP> ctps,String ctpdn) {
        for (CCTP ctp : ctps) {
            if (!ctp.getDn().equals(ctpdn) && ctpdn.startsWith(ctp.getDn() + "/"))
                return ctp;
        }
        return null;
    }
    protected  boolean containsCtps(List<CCTP> ctps,String ctpdn) {
        for (CCTP ctp : ctps) {
            if (ctp.getDn().equals(ctpdn) )
                return true;
        }
        return false;
    }

    protected String  getDefaultTmRate(String ctpDn) {
        String deviceDn = DNUtil.extractNEDn(ctpDn);
        String dft =  deviceOMSRate.get(deviceDn);
        if (dft == null) dft = "40G";
        return dft;


    }

    @Override
    public CPTP transPTP(PTP ptp) {
        CPTP cptp = super.transPTP(ptp);

        String dn = cptp.getDn();
        cptp.setNo(ptp.getDn().substring(ptp.getDn().indexOf("port=")+5));
        int i = dn.indexOf("/", dn.indexOf("slot="));
        String carddn = (dn.substring(0,i)+"@Equipment:1").replaceAll("PTP:","EquipmentHolder:")
                .replaceAll("FTP:","EquipmentHolder:");
        cptp.setParentDn(carddn);
        cptp.setLayerRates(ptp.getRate());
        cptp.setType(DicUtil.getPtpType(cptp.getDn(),cptp.getLayerRates()));
        if (ptp.getDn().contains("FTP"))
            cptp.setType("LOGICAL");
        cptp.setSpeed(getSpeed(cptp.getLayerRates()));

        if (cptp.getRate().equals("46"))
            System.out.println();
        if (cptp.getSpeed() == null) {
            if (cptp.getRate().equals("46")) {
                if (cptp.getNativeEMSName().contains("2M"))
                    cptp.setSpeed("2M");
                else
                    cptp.setSpeed("100M");
            } else
                cptp.setSpeed(getDefaultTmRate(cptp.getDn()));
        }


        cptp.setDeviceDn(ptp.getParentDn());
        cptp.setEoType(DicUtil.getEOType(cptp.getDn(),cptp.getLayerRates()));
            cptp.setTag3(ptp.getId() + "");
        if (cptp.getEoType() == DicConst.EOTYPE_ELECTRIC && "OPTICAL".equals(cptp.getType()))
            cptp.setType("ELECTRICAL");
         ptpMap.put(cptp.getDn() ,cptp);
        return cptp;
    }

    public static String getSpeed(String layerRates) {
        List<Integer> list = DicUtil.convertLayerRateList(layerRates);
        for (int rate : list) {
            if (rate == DicConst.LR_PHYSICAL_OPTICAL || rate == DicConst.LR_OPTICAL_SECTION|| rate == DicConst.LR_PHYSICAL_OPTICAL )
                continue;
            String speedByRate = DicUtil.getSpeedByRate(rate);
            if (speedByRate != null)
                return speedByRate;
        }
        return null;
    }


    protected String getSpeed(float g) {
        if (g >= 10 && g < 15)
            return "10G";
        if (g >= 1 && g < 1.5)
            return "1G";
        if (g >= 2 && g < 3)
            return "2.5G";
        return g+"G";

    }



    protected HashMap<String,CSection> omsMap = new HashMap<String, CSection>();
    protected List<CChannel> channels = new ArrayList<CChannel>();
    protected HashMap<String,List<CChannel>> omsChannelMap = new HashMap<String, List<CChannel>>();
    protected List<CSection> sectionsToMakeup = new ArrayList<CSection>();
    public void migrateOMS() throws Exception {

        executeDelete("delete  from COMS_CC c where c.emsName = '" + emsdn + "'", COMS_CC.class);
        executeDelete("delete  from COMS_Section c where c.emsName = '" + emsdn + "'", COMS_Section.class);
        executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
        executeDelete("delete  from CRoute_CC c where c.emsName = '" + emsdn + "'", CRoute_CC.class);
        executeDelete("delete  from CPath c where c.emsName = '" + emsdn + "'", CPath.class);
        executeDelete("delete  from CChannel c where c.emsName = '" + emsdn + "'", CChannel.class);
        executeDelete("delete  from CRoute_Channel c where c.emsName = '" + emsdn + "'", CRoute_Channel.class);
        executeDelete("delete  from CPath_CC c where c.emsName = '" + emsdn + "'", CPath_CC.class);
        executeDelete("delete  from CPath_Channel c where c.emsName = '" + emsdn + "'", CPath_Channel.class);
        executeDelete("delete  from CPath_Section c where c.emsName = '" + emsdn + "'", CPath_Section.class);
        executeDelete("delete  from CRoute_Section c where c.emsName = '" + emsdn + "'", CRoute_Section.class);


        List<Section> sections = sd.queryAll(Section.class);
        List<Section> ochSections = new ArrayList<Section>();
        List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
        List<SubnetworkConnection> ochList = new ArrayList<SubnetworkConnection>();
        List<SubnetworkConnection> oduList = new ArrayList<SubnetworkConnection>();
        List<SubnetworkConnection> dsrList = new ArrayList<SubnetworkConnection>();
        HashMap<String, List<R_TrafficTrunk_CC_Section>> routeMap = queryTrafficTrunkCCSectionMap(sd);
        HashMap<String,SubnetworkConnection> sncMap = new HashMap<String, SubnetworkConnection>();


        List<CCrossConnect> ccsToMakeup = new ArrayList<CCrossConnect>();

        List<CPath> cpaths = new ArrayList<CPath>();
        List<CRoute> cRoutes = new ArrayList<CRoute>();
        List<CPath_CC> cpathccs = new ArrayList<CPath_CC>();
        List<CPath_Channel> cpathChannels = new ArrayList<CPath_Channel>();
        List<CPath_Section> cPath_sections = new ArrayList<CPath_Section>();

        HashMap<String,List<CChannel>> pathSubWaveMap = new HashMap<String, List<CChannel>>();

        HashSet<String> ochDns = new HashSet<String>();


        for (Section section : sections) {
            if (section.getRate().equals("40")) {
                ochSections.add(section);
            }
        }

        for (SubnetworkConnection snc : sncs) {
            sncMap.put(snc.getDn(),snc);
            if (snc.getRate().equals("40")) {
                ochList.add(snc);
            } else if (snc.getRate().equals("104") || snc.getRate().equals("105") || snc.getRate().equals("106")
                    || snc.getRate().equals("1602")
                    ) {
                oduList.add(snc);
            } else {
                dsrList.add(snc);
            }
        }


        getLogger().info("OCH 数量 = "+ochList.size());
        for (SubnetworkConnection och : ochList) {
            ochDns.add(och.getDn());
            if (och.getDn().equals("EMS:SHX-OTNM2000-1-OTN@MultiLayerSubnetwork:1@SubnetworkConnection:504367806_520096575"))
                System.out.println();

            CPath cPath = U2000MigratorUtil.transPath(emsdn, och);
            if (ctpMap.get(och.getaEnd()) != null)
                cPath.setTmRate(ctpMap.get(och.getaEnd()).getTmRate());
            cPath.setRateDesc("OCH");
            cPath.setCategory("OCH");
            cpaths.add(cPath);

            List<CCTP> actps = ctpParentChildMap.get(cPath.getAend());
            List<CCTP> zctps = ctpParentChildMap.get(cPath.getZend());
            if (cPath.getAend().equals("EMS:SHX-OTNM2000-1-OTN@ManagedElement:134217867;71945@PTP:/rack=19969/shelf=1/slot=14681101/port=1@CTP:/och=1/"))
                System.out.println();
            int subwaveNum = 0;
            if (actps == null || zctps == null) {
                getLogger().error("无子CTP:"+cPath.getAend()+" -- "+cPath.getZend());
            } else {
                for (CCTP actp : actps) {
                    for (CCTP zctp : zctps) {
                        if (DNUtil.extractCTPSimpleName(actp.getDn()).equals(DNUtil.extractCTPSimpleName(zctp.getDn()))) {
                            CChannel subwave = createCChanell(cPath, actp, zctp);
                            channels.add(subwave);
                            DSUtil.putIntoValueList(pathSubWaveMap, cPath.getDn(), subwave);
                            subwaveNum++;
                        }
                    }
                }
            }

            if (subwaveNum == 0)
                getLogger().error("PATH:"+ cPath.getDn()+" 子波数为零！！");


            List<R_TrafficTrunk_CC_Section> routes = routeMap.get(och.getDn());
            if (routes == null) {
                getLogger().info("OCH route 为空:"+och.getDn()+" size = ");
                continue;
            }


            removeDuplicateRoutes(routes);
            if (routes.size() % 2 != 0) {
                getLogger().info("OCH route 不是偶数:" + och.getDn() + " size = " + routes.size());
                continue;
            }

            String ochAend = och.getaEnd();
            String ochZend = och.getzEnd();

            String lastNode = och.getaEnd();
            Iterator<R_TrafficTrunk_CC_Section> iterator = routes.iterator();
            while (iterator.hasNext()) {
                R_TrafficTrunk_CC_Section route = iterator.next();
                String routea = route.getaEnd();
                String routez = route.getzEnd();

                if (getCTPType(lastNode).equals("och") && getCTPType(routea).equals("och")) {
                    //先看是不是终结了
                    LinkInfo s = findOCHSection(lastNode, ochZend, sections);
                    if (s != null) {
                        CPath_Section cPath_section = U2000MigratorUtil.createCPath_Section(emsdn, s.dn, cPath);
                        cPath_sections.add(cPath_section);
                        lastNode = ochZend; //开始反向找
                    }


                    LinkInfo ochSection = findOCHSectionByAend(lastNode, sections);
                    if (ochSection != null) {
                        CPath_Section cPath_section = U2000MigratorUtil.createCPath_Section(emsdn, ochSection.dn, cPath);
                        cPath_sections.add(cPath_section);
                        lastNode = routez;
                    }
                }

                else if (getCTPType(lastNode).equals("oms") && getCTPType(routea).equals("och")) {
                    getLogger().error("异常!找到OMS-OCH，snc="+och.getDn()+" route="+route.getId());
                    continue;
                }

                else if (getCTPType(lastNode).equals("oms") && getCTPType(routea).equals("oms")) {
                    CSection oms = processOMSAndSplitWaves(lastNode,routea);
                    List<CChannel> waves = omsChannelMap.get(oms.getDn());
                    boolean findWave = false;
                    for (CChannel wave : waves) {
                        if (wave.getZend().equals(routez)) {
                            cpathChannels.add(U2000MigratorUtil.createCPath_Channel(emsdn,wave,cPath));
                            cPath.setTmRate(wave.getTmRate());
                            findWave = true;
                            break;
                        }
                    }
                    if (!findWave) {
                        getLogger().error("无法找到波道:path="+cPath.getDn()+",oms="+oms.getDn());
                    }
                    lastNode = routez;
                }

                else if (getCTPType(lastNode).equals("och") && getCTPType(routea).equals("oms")) {
                    getLogger().error("异常!找到OCH-OMS，snc="+och.getDn()+" route="+route.getId());
                    continue;
                }







            }

            LinkInfo ochSection = findOCHSection(lastNode, ochAend, sections);
            if (ochSection != null)  {
                cPath_sections.add(U2000MigratorUtil.createCPath_Section(emsdn,ochSection.dn,cPath));
                getLogger().error("路由搜索成功:" + och.getDn());
            } else {
                getLogger().error("最后一段路由无法找到");
            }


        }

        List<CRoute_CC> route_ccs = new ArrayList<CRoute_CC>();
        List<CRoute_Channel> route_channels = new ArrayList<CRoute_Channel>();
        List<CRoute_Section> route_sections = new ArrayList<CRoute_Section>();

        for (SubnetworkConnection dsr : dsrList) {
            if (dsr.getDn().equals("EMS:SHX-OTNM2000-1-OTN@MultiLayerSubnetwork:1@SubnetworkConnection:504367803_520096572"))
                System.out.println();
            CRoute cRoute = U2000MigratorUtil.transRoute(emsdn, dsr);
            cRoute.setCategory("DSR");
            if (ctpMap.get(dsr.getaEnd()) != null)
                cRoute.setTmRate(ctpMap.get(dsr.getaEnd()).getTmRate());
            else if (ptpMap.get(dsr.getaEnd()) != null) {
                cRoute.setTmRate(ptpMap.get(dsr.getaEnd()).getSpeed());
            }

            cRoutes.add(cRoute);

            boolean findPathChannel = false;
            List<String> parentDns = getParentSncDn(dsr);
            List<R_TrafficTrunk_CC_Section> dsrRoutes = routeMap.get(dsr.getDn());
            List<String> oduFtps = new ArrayList<String>();
            List<String> ochsnc = null;
            List<R_TrafficTrunk_CC_Section> routeCCs = new ArrayList<R_TrafficTrunk_CC_Section>();
            if (!parentDns.isEmpty()) {
                if (!ochDns.contains(parentDns.get(0))) {
                    for (String parentDn : parentDns) {


                        SubnetworkConnection odusnc = sncMap.get(parentDn);
                        if (odusnc == null) {
                            getLogger().error("Error to find parentdn : dsr = "+dsr.getDn()+",parentdn = "+parentDn);
                            continue;
                        }
                        List<R_TrafficTrunk_CC_Section> oduRoutes = routeMap.get(parentDn);
                        if (oduRoutes != null && oduRoutes.size() > 0) {
                            for (R_TrafficTrunk_CC_Section oduRoute : oduRoutes) {
                                if (oduRoute.getType().equals("CC")) {
                                    CCrossConnect cc = makeupCC(oduRoute.getaEnd(), oduRoute.getzEnd(), emsdn);
                                    ccsToMakeup.add(cc);
                                    route_ccs.add(createCRoute_CC(emsdn, cc.getDn(), cRoute));


                                        routeCCs.add(oduRoute);
                                }

                                if (oduRoute.getaEnd().contains("FTP")) {
                                    oduFtps.add(DNUtil.extractPortDn(oduRoute.getaEnd()));
                                }
                                if (oduRoute.getzEnd().contains("FTP")) {
                                    oduFtps.add(DNUtil.extractPortDn(oduRoute.getzEnd()));
                                }
                            }
                        } else {
                            getLogger().error("ODU snc 无法找到路由:" + parentDn);
                        }

                        ochsnc = getParentSncDn(odusnc);


                    }
                } else {
                    ochsnc = parentDns;  //DSR的父亲直接就是OCH
                }

                if (dsrRoutes != null) {
                    for (R_TrafficTrunk_CC_Section dsrRoute : dsrRoutes) {
                        if (dsrRoute.getType().equals("CC"))
                            routeCCs.add(dsrRoute);
                    }
                }

                if (ochsnc != null && !ochsnc.isEmpty()) {
                    HashSet<String> subwaves = new HashSet<String>();
                    for (String och : ochsnc) {
                        for (R_TrafficTrunk_CC_Section cc : routeCCs) {
                            List<CChannel> waves = pathSubWaveMap.get(och);
                            if (waves != null) {
                                for (CChannel wave : waves) {
                                    if (wave.getAend().equals(cc.getaEnd()) || wave.getZend().equals(cc.getzEnd())
                                            || wave.getAend().equals(cc.getzEnd()) || wave.getZend().equals(cc.getaEnd())
                                            ) {
                                        if (!subwaves.contains(wave.getDn())) {    //剔除重复的
                                            findPathChannel = true;
                                            subwaves.add(wave.getDn());
                                            cRoute.setTmRate(wave.getTmRate());
                                            route_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, wave, cRoute));
                                        }
                                    }
                                }
                            } else {
                                getLogger().error("OCH找不到子波:"+och);
                            }
                        }

                    }
                } else {
                 //   getLogger().error("无法找到ChannelIdList odu="+odusnc.getDn());
                }
            }  else {
                getLogger().error("无法找到ChannelIdList dsr="+dsr.getDn());
            }

            if (dsrRoutes != null) {
                if (!findPathChannel) {
                    for (R_TrafficTrunk_CC_Section dsrRoute : dsrRoutes) {
                        for (String pathDn : pathSubWaveMap.keySet()) {
                            List<CChannel> subwaves = pathSubWaveMap.get(pathDn);
                            for (CChannel subwave : subwaves) {
                                if (subwave.getAend().equals(dsrRoute.getaEnd())
                                        ||subwave.getAend().equals(dsrRoute.getzEnd())
                                        ||subwave.getZend().equals(dsrRoute.getaEnd())
                                        ||subwave.getZend().equals(dsrRoute.getzEnd())


                                        ) {
                                    cRoute.setTmRate(subwave.getTmRate());
                                    route_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, subwave, cRoute));


                                }
                            }
                        }
                    }
                }

                for (R_TrafficTrunk_CC_Section dsrRoute : dsrRoutes) {
                    CCrossConnect newcc = makeupCC(dsrRoute.getaEnd(), dsrRoute.getzEnd(), emsdn);
                    ccsToMakeup.add(newcc);
                    route_ccs.add(createCRoute_CC(emsdn, newcc.getDn(), cRoute));
                    String ftp = null;
                    if (dsrRoute.getaEnd().contains("FTP"))
                        ftp = DNUtil.extractPortDn(dsrRoute.getaEnd());
                    if (dsrRoute.getzEnd().contains("FTP"))
                        ftp = DNUtil.extractPortDn(dsrRoute.getzEnd());


                    if (ftp != null) {
                        for (String oduFtp : oduFtps) {
                            if (DNUtil.extractNEDn(oduFtp).equals(DNUtil.extractNEDn(ftp))) {
                                CSection section = new CSection();
                                section.setAendTp(ftp);
                                section.setZendTp(oduFtp);
                                section.setDirection(1);
                                section.setDn(ftp + "_" + oduFtp);
                                section.setTag1("MAKEUP");
                                section.setRate("41");
                                section.setEmsName(emsdn);
                                section.setSpeed("40G");
                                section.setType("OTS");
                                sectionsToMakeup.add(section);
                                route_sections.add(createCRoute_Section(emsdn, section.getDn(), cRoute));
                            }
                        }
                    }
                }
            }




        }




        DataInserter di = new DataInserter(emsid);
        try {
        	di.insertWithDupCheck(ccsToMakeup);
            di.insertWithDupCheck(sectionsToMakeup);

            di.updateByDn(new ArrayList(sectionsToUpdate));

            di.insertWithDupCheck(cpaths);
            di.insertWithDupCheck(channels);
            di.insertWithDupCheck(cRoutes);
            di.insertWithDupCheck(cpathccs);
            di.insertWithDupCheck(cpathChannels);
            di.insertWithDupCheck(cPath_sections);
            di.insertWithDupCheck(route_ccs);
            di.insertWithDupCheck(route_channels);
            di.insertWithDupCheck(route_sections);
        } catch (Exception e) {
            getLogger().error(e, e);
        }

        di.end();

    }

    private List<CSection> findAllARelatedSections(String aendTp) {
        List<CSection> sections = new ArrayList<CSection>();
        boolean findNext = false;
        do {
              findNext = false;
            List<CSection> cs = ptpSectionMap.get(aendTp);
            if (cs == null && aendTp.contains("oms")) {
                aendTp = DNUtil.extractPortDn(aendTp);
                cs = ptpSectionMap.get(DNUtil.extractPortDn(aendTp));
            }
            if (cs != null) {
                for (CSection c : cs) {
                    if (c.getAendTp().equals(aendTp)) {
                        if (!sections.contains(c)) {
                            sections.add(c);
                            aendTp = c.getZendTp();
                            findNext = true;
                        }
                    }
                }
            }
        } while (findNext);
        return sections;
    }

    private List<CSection> findAllZRelatedSections(String zendTp) {
        List<CSection> sections = new ArrayList<CSection>();
        boolean findNext = false;
        do {
            findNext = false;
            List<CSection> cs = ptpSectionMap.get(zendTp);
            if (cs == null && zendTp.contains("oms")) {
                zendTp = DNUtil.extractPortDn(zendTp);
                cs = ptpSectionMap.get(zendTp);
            }
            if (cs != null) {
                for (CSection c : cs) {
                    if (c.getZendTp().equals(zendTp)) {
                        if (!sections.contains(c)) {
                            sections.add(c);
                            zendTp = c.getAendTp();
                            findNext = true;
                        }
                    }
                }
            }
        } while (findNext);
        return sections;
    }


    protected HashSet<CSection> sectionsToUpdate = new HashSet<CSection>();
    protected CSection processOMSAndSplitWaves(String aendOMSCtp,String zendOMSCtp) {

        CSection oms = new CSection();
        oms.setDn(DNUtil.extractPortDn(aendOMSCtp)+"_"+DNUtil.extractPortDn(zendOMSCtp));
        oms.setAendTp(DNUtil.extractPortDn(aendOMSCtp));
        oms.setZendTp(DNUtil.extractPortDn(zendOMSCtp));
        if (!omsMap.containsKey(oms.getDn())) {
            omsMap.put(oms.getDn(),oms);
            oms.setDirection(0);
            oms.setEmsName(emsdn);
            oms.setSpeed(getDefaultTmRate(oms.getAendTp()));
            oms.setRate("41");
            oms.setType("OMS");

            List<CSection> cs1 = findAllARelatedSections(oms.getAendTp()+"@CTP:/oms=1");
            List<CSection> cs2 = findAllZRelatedSections(oms.getZendTp()+"@CTP:/oms=1");
            HashSet<CSection> cs = new HashSet<CSection>();
            cs.addAll(cs1);
            cs.addAll(cs2);
            for (CSection c : cs) {
                c.setOmsDn(oms.getDn());
                sectionsToUpdate.add(c);
            }


            List<CCTP> asubCtps = ctpParentChildMap.get(aendOMSCtp);
            List<CCTP> zsubCtps = ctpParentChildMap.get(zendOMSCtp);
            List<CChannel> omsWaves = new ArrayList<CChannel>();
            if (asubCtps != null && zsubCtps != null) {
                for (CCTP asubCtp : asubCtps) {
                    for (CCTP zsubCtp : zsubCtps) {
                        String adn = asubCtp.getDn();
                        String zdn = zsubCtp.getDn();
                        if (adn.contains("och=") && zdn.contains("och=")) {
                            if (DNUtil.extractOCHno(adn).equals(DNUtil.extractOCHno(zdn))) {
                                CChannel wave = createCChanell(oms, asubCtp, zsubCtp);
                                channels.add(wave);
                                omsWaves.add(wave);

                            }
                        }
                    }
                }
            } else {
                getLogger().error("OMS无法划分子波:"+oms.getDn());
            }
            omsChannelMap.put(oms.getDn(),omsWaves);
            sectionsToMakeup.add(oms);
        }
        return omsMap.get(oms.getDn());
    }


    public static CRoute_Section createCRoute_Section(String emsDn,String sectionDn,CRoute cRoute) {
        CRoute_Section crc = new CRoute_Section();
        crc.setDn(SysUtil.nextDN());
        crc.setRouteDn(cRoute.getDn());
        crc.setRouteId(cRoute.getSid());
        crc.setSectionDn(sectionDn);
    //    crc.setSectionId(DatabaseUtil.getSID(CSection.class,sectionDn));
        crc.setEmsName(emsDn);

        return crc;
    }






    public static CRoute_CC createCRoute_CC(String emsDn,String ccDn,CRoute cRoute) {
        ccDn = DNUtil.compressCCDn(ccDn);
        CRoute_CC crc = new CRoute_CC();
        crc.setCcDn(ccDn);
        crc.setDn(SysUtil.nextDN());
//        crc.setCcId(DatabaseUtil.getSID(CCrossConnect.class,ccDn));
        crc.setRouteDn(cRoute.getDn());
        crc.setEmsName(emsDn);
        crc.setRouteId(cRoute.getSid());
        return crc;
    }
    
//    protected String getPtpByCtp(String ctpDn){
//    	int index = ctpDn.indexOf("CTP");
//    	if (index < 0){
//    		if (ctpDn.contains("FTP")){
//    			index = ctpDn.length();
//    		}
//    	}
//    	return ctpDn.substring(0, index);
//    }
//    
    protected CSection getSection(String aptp, String zptp){
    	List<CSection> sections = ptpSectionMap.get(aptp);
    	if (sections == null || sections.isEmpty()){
    		return null;
    	}
    	for (CSection section : sections){
    		if ((aptp.equals(section.getAendTp()) && zptp.equals(section.getZendTp())) ||
    				(aptp.equals(section.getZendTp()) && zptp.equals(section.getAendTp()))){
    			return section;
    		}
    	}
    	return null;
    }

    protected boolean checkEndContainsSubCtp(String end) {
        String[] ctps = end.split(Constant.listSplitReg);
        for (String ctp : ctps) {
            List<CCTP> cctps = ctpParentChildMap.get(ctp);
            if (cctps != null && !cctps.isEmpty())
                return true;
        }
        return false;
    }


 
    protected CChannel findCChanell(List<CChannel> channels,String ctp) {
        List<CChannel> cChanells = findCChanells(channels, ctp);
        if (cChanells != null && cChanells.size() > 0)
            return cChanells.get(0);
        return null;
    }

    protected List<CChannel> findCChanells(List<CChannel> channels,String ctp) {
        List<CChannel> cs = new ArrayList<CChannel>();
//        if (ctp.contains("och=")){
//            int idx =ctp.indexOf("/", ctp.indexOf("och="));
//            if (idx > -1)
//                ctp = ctp.substring(0,idx);
//        }
        for (CChannel channel : channels) {
            if (ctp.equals(channel.getAend()) || ctp.equals(channel.getZend()))
                cs.add(channel);
        }
        return cs;

    }
    protected List<CCTP> findAllChildCTPS(String parentCtp) {

        List<CCTP> all = new ArrayList<CCTP>();
        List<CCTP> cctps = ctpParentChildMap.get(parentCtp);
        if (cctps != null) {
            all.addAll(cctps);
            for (CCTP cctp : cctps) {
                List<CCTP> c = findAllChildCTPS(cctp.getDn());
                if (c != null) {
                    all.addAll(c);
                }
            }
        }
        return all;
    }

    protected CChannel createCChanell(BObject parent,CCTP acctp, CCTP zcctp) {
        String aSideCtp = acctp.getDn();
        String zSideCtp = zcctp.getDn();
        CChannel cChannel = new CChannel();
        cChannel.setDn(aSideCtp + "<>" + zSideCtp+"@CHANNEL:1");
        cChannel.setSid(DatabaseUtil.nextSID(CChannel.class));
        cChannel.setAend(aSideCtp);
        cChannel.setZend(zSideCtp); 
        cChannel.setSectionOrHigherOrderDn(parent.getDn());
        if (parent instanceof CSection)
            cChannel.setName("och="+DNUtil.extractOCHno(acctp.getDn()));
        if (parent instanceof CPath)
            cChannel.setName(((CPath) parent).getName());

        cChannel.setNo(DNUtil.extractOCHno(acctp.getDn()));
        cChannel.setRate(acctp.getRate());
        if (parent instanceof CSection)
            cChannel.setCategory("波道");
        if (parent instanceof CPath)
            cChannel.setCategory("子波道");
        cChannel.setTmRate(SDHUtil.getTMRate(acctp.getRate()));
        cChannel.setRateDesc(SDHUtil.rateDesc(acctp.getRate()));

        cChannel.setFrequencies(acctp.getFrequencies());

            cChannel.setWaveLen(FHDwdmUtil.getWaveLength( (acctp.getFrequencies())));

//        if (ctp != null)
//            cChannel.setDirection(ctp.getDirection());
        cChannel.setDirection(DicConst.CONNECTION_DIRECTION_CD_BI);
        cChannel.setAptp(acctp.getParentDn());
        cChannel.setZptp(zcctp.getParentDn());

        CPTP aptp = ptpMap.get(acctp.getParentDn());
        CPTP zptp = ptpMap.get(zcctp.getParentDn());
        if (aptp != null && zptp != null)
            cChannel.setTag3(aptp.getTag3()+"-"+zptp.getTag3());
        cChannel.setEmsName(emsdn);
        return cChannel;
    }





    protected  HashMap<String,List<R_TrafficTrunk_CC_Section>>  queryTrafficTrunkCCSectionMap() {
        final HashMap<String,List<R_TrafficTrunk_CC_Section>> snc_cc_section_map = new HashMap<String, List<R_TrafficTrunk_CC_Section>>();
        List<R_TrafficTrunk_CC_Section> routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
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



    @Override
    protected void migrateSection() throws Exception {
        executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "'", CSection.class);
        DataInserter di = new DataInserter(emsid);
        List<Section> sections = sd.queryAll(Section.class);
        if (sections != null && sections.size() > 0) {
            for (Section section : sections) {
                if (section.getRate().equals("1"))
                    continue;
                CSection csection = transSection(section);
                csection.setType("OTS");
                csection.setSid(DatabaseUtil.nextSID(csection));
                // csection.setSid(toSid(Long.parseLong(section.getDn().substring(section.getDn().lastIndexOf(" - ") + 3))));
//                String aendtp = csection.getAendTp();
//                String zendtp = csection.getZendTp();

//                csection.setAptpId(DatabaseUtil.getSID(CPTP.class, aendtp));
//                csection.setZptpId(DatabaseUtil.getSID(CPTP.class, zendtp));
                di.insert(csection);
            }
        }
        di.end();
    }


    @Override
    public CSection transSection(Section section) {
        CSection csection = new CSection();
        csection.setDn(section.getDn());
        csection.setSid(DatabaseUtil.nextSID(csection));
        csection.setCollectTimepoint(section.getCreateDate());
        csection.setRate(section.getRate());
        String rate = section.getRate();
        if (rate != null) {
            int r = 0;
            try {
                r = Integer.parseInt(rate);
            } catch (NumberFormatException e) {
                LogUtil.error(getClass(), "Unknown rate :" + rate);
            }
            rate = DicUtil.getSpeedByRate(r);
//            if (rate == null)  {
//                if (!unkownRates.contains(section.getRate())) {
//                    LogUtil.error(getClass(), "Unknown rate for speed:" + section.getRate());
//
//                    unkownRates.add(section.getRate());
//                }
//            }
//            // if (r == DicConst.LR_DSR_Gigabit_Ethernet)
//            // rate = "1000M";

        }
        csection.setSpeed(rate);
        csection.setDirection(DicUtil.getConnectionDirection(section.getDirection()));


        csection.setParentDn(section.getParentDn());
        csection.setEmsName(section.getEmsName());
        csection.setUserLabel(section.getUserLabel());
        csection.setNativeEMSName(section.getNativeEMSName());
        csection.setOwner(section.getOwner());
        csection.setAdditionalInfo(section.getAdditionalInfo());

        csection.setAendTp( (section.getaEndTP()));
        csection.setZendTp( (section.getzEndTP()));

//        csection.setAendTp(DNUtil.extractPortDn(section.getaEndTP()));
//        csection.setZendTp(DNUtil.extractPortDn(section.getzEndTP()));
//        DatabaseUtil.getSID(CPTP.class,csection.getAendTp());
//
//        DatabaseUtil.getSID(CPTP.class,csection.getZendTp());
//        if (FHDwdmUtil.isOMSRate(cSection.getRate())){
//        	cSection.setType("OMS");
//        } else {
        csection.setType("OTS");
//        }
        csection.setSpeed(getDefaultTmRate(csection.getAendTp()));
        DSUtil.putIntoValueList(ptpSectionMap,csection.getAendTp(),csection);
        DSUtil.putIntoValueList(ptpSectionMap,csection.getZendTp(),csection);
        cSections.add(csection);
        return csection;
    }




    protected static List<String> getParentSncDn(SubnetworkConnection snc) {
        HashMap<String, String> addMap = MigrateUtil.transMapValue(snc.getAdditionalInfo());
        String parentId = addMap.get("ChannelIdList");
        List<String> dns = new ArrayList<String>();
        if (parentId != null) {
            String[] parentIds = parentId.split("\\|");
            for (String  id : parentIds) {
                dns.add(snc.getDn().substring(0, snc.getDn().lastIndexOf(":") + 1) + id);
            }

        }
        return dns;
    }

    public static  String getCTPType(String ctp) {
        int i = ctp.lastIndexOf("/");
        if (i > -1) {

            String substring = ctp.substring(i + 1);
            if (substring.indexOf("=") > 0) {
                substring = substring.substring(0, substring.indexOf("="));
                return substring;
            }
        }
        return "";

    }


    public static LinkInfo findOCHSectionByAend(String aend,List<Section> ochSections) {
        for (Section ochSection : ochSections) {
            if (ochSection.getaEndTP().equals(aend))
                return new LinkInfo(ochSection.getDn(),"section",ochSection,ochSection.getzEndTP(),aend);
        }
        return null;
    }

    public static  LinkInfo findOCHSection(String aend,String zend,List<Section> ochSections) {
        for (Section ochSection : ochSections) {
            if ((ochSection.getaEndTP().equals(aend) && ochSection.getzEndTP().equals(zend))
                    ||( ochSection.getzEndTP().equals(aend)&& ochSection.getaEndTP().equals(zend)
            )
                    )
                return new LinkInfo(ochSection.getDn(),"section",ochSection,ochSection.getzEndTP(),aend);
        }
        return null;
    }

    public static  LinkInfo findOCHSectionByZend(String zend,List<Section> ochSections) {
        for (Section ochSection : ochSections) {
            if (ochSection.getzEndTP().equals(zend))
                return new LinkInfo(ochSection.getDn(),"section",ochSection,ochSection.getaEndTP(),zend);
        }
        return null;
    }

    public static void removeDuplicateRoutes(List<R_TrafficTrunk_CC_Section> routes) {
        HashSet<R_TrafficTrunk_CC_Section> toBeRemoved = new HashSet<R_TrafficTrunk_CC_Section>();
        HashSet<String> rt = new HashSet<String>();
        for (R_TrafficTrunk_CC_Section route : routes) {
            String key = route.getaEnd() + "--" + route.getzEnd();
            if (rt.contains(key))
                toBeRemoved.add(route);
            else
                rt.add(key);
        }

        routes.removeAll(toBeRemoved);
    }



    public static HashMap<String,List<R_TrafficTrunk_CC_Section>> queryTrafficTrunkCCSectionMap(SqliteDelegation sd) {
        List<R_TrafficTrunk_CC_Section> routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
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

    protected static CCrossConnect makeupCC(String aend,String zend,String emsdn) {
        CrossConnect cc = new CrossConnect();
        cc.setDn(aend+"_"+zend.substring(zend.lastIndexOf("@")));
        cc.setaEndNameList(aend);
        cc.setzEndNameList(zend);
        cc.setaEndTP(DNUtil.extractPortDn(aend));
        cc.setzEndTP(DNUtil.extractPortDn(zend));
        cc.setCcType("ST_SIMPLE");
        cc.setDirection("CD_UNI");
        cc.setEmsName(emsdn);
        cc.setParentDn(DNUtil.extractNEDn(aend));
        CCrossConnect cCrossConnect = transCC(cc,emsdn);
        cCrossConnect.setTag1("MAKEUP");
        return cCrossConnect;
    }

    protected static CCrossConnect transCC(CrossConnect src,String emsdn) {
        CCrossConnect des = new CCrossConnect();
        des.setDn(src.getDn());
        des.setCollectTimepoint(src.getCreateDate());
        des.setCcType(src.getCcType());
        des.setDirection(src.getDirection());
        //TODO
        des.setAend(src.getaEndNameList());
        des.setZend(src.getzEndNameList());
        des.setAptp(src.getaEndTP());
        des.setZptp(src.getzEndTP());
        des.setParentDn(src.getParentDn());
        des.setEmsName(emsdn);
        des.setAdditionalInfo(src.getAdditionalInfo());
        return des;
    }



    public static void main(String[] args) throws Exception {
//        List allObjects = JpaClient.getInstance("cdcp.datajpa").findAllObjects(CDevice.class);
        String fileName=  "F:\\cdcpdb\\2015-08-24-214502-SHX-OTNM2000-1-OTN-DayMigration.db";
        String emsdn = "SHX-OTNM2000-1-OTN";
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

              //  DBUtil.getInstance().executeNonSelectingSQL(context,sql);
            }
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }

        FHOTNM2000OTNMigrator loader = new FHOTNM2000OTNMigrator (fileName, emsdn){
            public void afterExecute() {
                printTableStat();
            }
        };
        loader.execute();
    }


}
