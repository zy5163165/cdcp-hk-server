package com.alcatelsbell.cdcp.server.adapters.zte;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.server.adapters.*;
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
        ZTE_OTNU31_OTN_Migrator extends AbstractDBFLoader {
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

    public ZTE_OTNU31_OTN_Migrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog(emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }
    private static FileLogger logger = new FileLogger("ZTE-OTN-Device.log");
    public ZTE_OTNU31_OTN_Migrator(Serializable object, String emsdn) {
        this.emsdn = emsdn;
        this.resultObject = object;
        MigrateThread.thread().initLog(logger);
    }

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

        makeupCTP_CC_SECTION();

        migrateOMS();

        executeNativeSql("update c_ptp p set p.speed = '100G' where p.emsName = '"+emsdn+"' and exists (select c.* from c_ctp c where c.portdn = p.dn and c.tmrate = '100G')");

        sd.release();
//        logAction("migrateSection", "同步段", 25);
//        migrateSection();
//
//        logAction("migrateCTP", "同步CTP", 30);
//        migrateCTP();
//          migrateSubnetworkConnection();
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
        return new Class[]{CCrossConnect.class, CChannel.class, CPath.class, CRoute.class, CPath_Channel.class,
                CPath_CC.class, CRoute_Channel.class,
                CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class, CVirtualBridge.class,
                CMP_CTP.class, CEthTrunk.class, CStaticRoute.class, CEthRoute.class, CEthTrunk_SDHRoute.class,
                CEthRoute_StaticRoute.class, CEthRoute_ETHTrunk.class, CSection.class, CCTP.class, CDevice.class, CPTP.class, CTransmissionSystem.class, CTransmissionSystem_Channel.class};
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


    public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
        HashMap<String, String> addMap = MigrateUtil.transMapValue(equipmentHolder.getAdditionalInfo());
        if (equipmentHolder.getAdditionalInfo() != null && equipmentHolder.getAdditionalInfo().length() > 200)
            equipmentHolder.setAdditionalInfo(null);
        CdcpObject cdcpObject = super.transEquipmentHolder(equipmentHolder);
        if (cdcpObject instanceof CSlot) {
            if (((CSlot) cdcpObject).getAcceptableEquipmentTypeList().length() > 2000)
                ((CSlot) cdcpObject).setAcceptableEquipmentTypeList(null);
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

        CCTP cctp = super.transCTP(ctp);

        setCTPRateDescAndTmRate(cctp);
        if (cctp.getTmRate() == null || cctp.getTmRate().isEmpty()) {
            String defaultOpRate = getDefaultTmRate(cctp.getDn());
            cctp.setTmRate(defaultOpRate);
        }
       // if (cctp.getTmRate().equals("1G")) cctp.setTmRate("1.25G");

        cctp.setPortdn(cctp.getParentDn());
        if (cctp.getNativeEMSName() == null || cctp.getNativeEMSName().isEmpty()) {
            cctp.setNativeEMSName(ctp.getDn().substring(ctp.getDn().indexOf("CTP:/")+5));
        }
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

        if (dn.contains("layerrate=")) {
            int i1 = dn.indexOf("layerrate=");
            int i2 = dn.indexOf("/",i1);
            if (i2 > i1) {
                String shortDn = dn.substring(0,i1) +dn.substring(i2+1);
                shortedCTPDnMap.put(shortDn,dn);

                int i = shortDn.indexOf("/", shortDn.indexOf("CTP:/") + 6);
                if (i > -1) {
                    String parentDn = shortDn.substring(0,shortDn.lastIndexOf("/"));
                    cctp.setParentCtpdn(parentDn);
                    //      DSUtil.putIntoValueList(ctpParentChildMap_4short, parentDn, cctp);
                }
            }


        }


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
            di.insert(makeupCTPS);
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

    public void migrateOMS() throws Exception {

        executeDelete("delete  from COMS_CC c where c.emsName = '" + emsdn + "'", COMS_CC.class);
        executeDelete("delete  from COMS_Section c where c.emsName = '" + emsdn + "'", COMS_Section.class);

    //    executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "'", CSection.class);

        executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
        executeDelete("delete  from CRoute_CC c where c.emsName = '" + emsdn + "'", CRoute_CC.class);
        executeDelete("delete  from CPath c where c.emsName = '" + emsdn + "'", CPath.class);
        executeDelete("delete  from CChannel c where c.emsName = '" + emsdn + "'", CChannel.class);
        executeDelete("delete  from CRoute_Channel c where c.emsName = '" + emsdn + "'", CRoute_Channel.class);
        executeDelete("delete  from CPath_CC c where c.emsName = '" + emsdn + "'", CPath_CC.class);
        executeDelete("delete  from CPath_Channel c where c.emsName = '" + emsdn + "'", CPath_Channel.class);
        executeDelete("delete  from CPath_Section c where c.emsName = '" + emsdn + "'", CPath_Section.class);
        executeDelete("delete  from CRoute_Section c where c.emsName = '" + emsdn + "'", CRoute_Section.class);
        executeDelete("delete  from CCTP c where c.emsName = '" + emsdn + "' and c.tag1='MAKEUP_SNC'", CCTP.class);




        List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
        HashMap<String, List<R_TrafficTrunk_CC_Section>> routeMap = queryTrafficTrunkCCSectionMap();







        List<CChannel> waveChannelList = null;
        try {


            List<CSection> omsList = new ArrayList<CSection>();
            List<CSection> updateOTS = new ArrayList<CSection>();
            List<COMS_CC> omsCClist = new ArrayList<COMS_CC>();
            List<COMS_Section> omsSectionList = new ArrayList<COMS_Section>();


            for (SubnetworkConnection snc : sncs) {
                String rate = snc.getRate();
                if (rate.equals(DicConst.LR_Optical_Multiplex_Section+"")) {
                    CSection oms = transSection(snc);
                    oms.setType("OMS");

                    omsList.add(oms);
                    List<R_TrafficTrunk_CC_Section> ccAndSections = routeMap.get(snc.getDn());
                    if (ccAndSections == null || ccAndSections.size() == 0) {
                        getLogger().error("无法找到 section,OMS="+oms.getDn());
                        continue;
                    }
                    for (R_TrafficTrunk_CC_Section ccAndSection : ccAndSections) {
                        if (ccAndSection.getType().equals("SECTION")) {

                            if (checkIsRedundantSection(ccAndSections,ccAndSection))
                                continue;

                            //   ((CSection) ccAndSection).setOmsDn(oms.getDn());
                            //    updateOTS.add((CSection) ccAndSection);
                            COMS_Section coms_section = new COMS_Section();
                            coms_section.setDn(SysUtil.nextDN());
                            coms_section.setOmsdn(oms.getDn());
                            coms_section.setSectiondn(ccAndSection.getCcOrSectionDn());
                            coms_section.setEmsName(emsdn);
                            omsSectionList.add(coms_section);
                        }
                        if (ccAndSection.getType().equals("CC")) {
                            //   ((CSection) ccAndSection).setOmsDn(oms.getDn());
                            //    updateOTS.add((CSection) ccAndSection);
                            COMS_CC coms_section = new COMS_CC();
                            coms_section.setDn(SysUtil.nextDN());
                            coms_section.setOmsdn(oms.getDn());
                            coms_section.setCcdn(ccAndSection.getCcOrSectionDn());
                            coms_section.setEmsName(emsdn);
                            omsCClist.add(coms_section);
                        }
                    }

                }
            }


            getLogger().info("OMS size = "+omsList.size());
            removeDuplicateDN(omsList);
            DataInserter di = new DataInserter(emsid);
            di.insert(omsList);
            di.insert(omsCClist);
            di.insert(omsSectionList);
            //       di.updateByDn(updateOTS);
            di.end();





            ///////////////////////////////波道channel///////////////////////////////////////
            waveChannelList = new ArrayList<CChannel>();
            for (CSection cSection : omsList) {
                List<CChannel> wcList = new ArrayList<CChannel>();
                if (cSection.getDn().equals("EMS:TZ-OTNU31-1-P@MultiLayerSubnetwork:1@SubnetworkConnection:612005410000007322"))
                    System.out.println();
                String aendTp = cSection.getAendTp();
                String zendTp = cSection.getZendTp();
                List<CCTP> acctps = ptp_ctpMap.get(aendTp);
                List<CCTP> zcctps = ptp_ctpMap.get(zendTp);
                if (acctps == null || acctps.isEmpty()) {
                    getLogger().error("无法找到CTP，端口："+aendTp);
                }  else if (zcctps == null || zcctps.isEmpty()) {
                    getLogger().error("无法找到CTP，端口："+zendTp);
                }
                else {
                    for (CCTP acctp : acctps) {
                        boolean find = false;
//                        for (CCTP zcctp : zcctps) {
//
//                             if (zcctp.getDn().contains("och=0"))
//                                 continue;
//                            String och = DNUtil.extractOCHno(acctp.getDn());
//                            String och2 = DNUtil.extractOCHno(zcctp.getDn());
//
//
//                            if (och != null  && och.equals(och2)) {
//                                wcList.add(createCChanell(cSection,acctp, zcctp));
//
//                                 find = true;
//                                break;
//                            }
//                        }


                        if (!acctp.getDn().endsWith("och=0") && !find && acctp.getUserLabel().contains("fr=")) {
                           String afr =  acctp.getUserLabel().substring(acctp.getUserLabel().lastIndexOf("fr="));
                            for (CCTP zcctp : zcctps) {
                                if (!zcctp.getDn().endsWith("och=0") && zcctp.getUserLabel().contains("fr=")) {
                                   String zfr = zcctp.getUserLabel().substring(zcctp.getUserLabel().lastIndexOf("fr="));
                                    if (afr.trim().equalsIgnoreCase(zfr.trim())) {
                                        wcList.add(createCChanell(cSection,acctp, zcctp));
                                    }
                                }
                            }
                        }
                    }
                }
                waveChannelList.addAll(wcList);
            }

            removeDuplicateDN(waveChannelList);

            di = new DataInserter(emsid);
            di.insert(waveChannelList);
            di.end();
        } catch (Exception e) {
            getLogger().error(e, e);
        }



        //////////////////////////////////////////////////////////////////////
        HashMap<String,List<String>> subwave_routes = new HashMap<String, List<String>>();

        List<CChannel> subWaveChannelList = new ArrayList<CChannel>();

        List<CPath> cPaths = new ArrayList<CPath>();
        List<CRoute> cRoutes = new ArrayList<CRoute>();
        List<CRoute_CC> cRoute_ccs = new ArrayList<CRoute_CC>();
        List<CRoute_Channel> cRoute_channels = new ArrayList<CRoute_Channel>();
        List<CRoute_Section> cRoute_sections = new ArrayList<CRoute_Section>();

        List<CPath_CC> cPath_ccs = new ArrayList<CPath_CC>();
        List<CPath_Channel> cPath_channels = new ArrayList<CPath_Channel>();
        List<CPath_Section> cPath_sections = new ArrayList<CPath_Section>();
        List<CSection> sectionsToMakeUp = new ArrayList<CSection>();
        List<CCTP> ctpsToMakeUp = new ArrayList<CCTP>();
        List<SubnetworkConnection> ochList = new ArrayList<SubnetworkConnection>();
        List<SubnetworkConnection> dsrList = new ArrayList<SubnetworkConnection>();
        for (SubnetworkConnection snc : sncs) {


            String rate = snc.getRate();

            if (rate != null) {
                if (rate.equals(DicConst.LR_Optical_Multiplex_Section+"") || rate.equals(DicConst.LR_Optical_Transmission_Section+""))
                    continue;
                if (rate.equals(DicConst.LR_Optical_Channel+"")) {
                     ochList.add(snc);
                 }

                else   {
                    dsrList.add(snc);
                }
            }
        }
        DataInserter di2 = new DataInserter(emsid);
        for (SubnetworkConnection snc : ochList) {
            if (snc.getDn().equals("EMS:TZ-OTNU31-1-P@MultiLayerSubnetwork:1@SubnetworkConnection:612005410000007332"))
                System.out.println();
            CPath cPath = U2000MigratorUtil.transPath(emsdn, snc);
            cPath.setTmRate("40G");
            cPath.setRateDesc("OCH");
            cPath.setCategory("OCH");
            CCTP actp = ctpMap.get(snc.getaEnd());
            cPath.setFrequencies(actp == null ? null : actp.getFrequencies());
            cPaths.add(cPath);
            //CChannel subwave = createCChanell(cPath, ctpMap.get(snc.getaEnd()), ctpMap.get(snc.getzEnd()));
            CCTP asideCtp = ctpMap.get(snc.getaEnd());
            CCTP zsideCtp = ctpMap.get(snc.getzEnd());
            if (asideCtp == null) {
                getLogger().error("无法找到CTP：snc="+snc.getDn()+"  aend="+snc.getaEnd());
                continue;
            }
            if (zsideCtp == null) {
                getLogger().error("无法找到CTP：snc="+snc.getDn()+" aend="+snc.getaEnd());
                continue;
            }
            List<CChannel> subwaves = createSubwaveChannels(cPath, asideCtp, zsideCtp);

            subWaveChannelList.addAll(subwaves);


            List<R_TrafficTrunk_CC_Section> routes = routeMap.get(snc.getDn());
            if (routes == null || routes.isEmpty()) {
                getLogger().error("OCH路由为空：snc"+snc.getDn());
                continue;
            }
            List<CChannel> sncChannels = new ArrayList<CChannel>();
            HashSet<String> sncSectionDns = new HashSet<String>();
            HashSet<String> sncCCDns = new HashSet<String>();


            String aend =  snc.getaEnd();
            String zend = snc.getzEnd();

            if (snc.getDn().equals("EMS:TZ-OTNU31-1-P@MultiLayerSubnetwork:1@SubnetworkConnection:612005410000005457"))
                System.out.println("channels = " );
            boolean b = searchOCHRoute(snc,aend,zend,waveChannelList,routes,sncChannels,sncCCDns);
            if (!b) {
                sncChannels.clear();
                sncCCDns.clear();
                b = searchOCHRoute(snc,zend,aend,waveChannelList,routes,sncChannels,sncCCDns);
            }

            if (b)
                getLogger().info("OCH搜索路由成功： snc = "+snc.getDn());
            else
                getLogger().info("OCH搜索路由失败： snc = "+snc.getDn());

////            HashSet ptps = new HashSet();
//            for (R_TrafficTrunk_CC_Section route : routes) {
//                //将子波和路由的关系放入map中
//                for (CChannel subwave : subwaves) {
//                    DSUtil.putIntoValueList(subwave_routes,subwave.getDn(),route.getCcOrSectionDn());
//                }
//
//
//                if ("CC".equals(route.getType())) {
//                    CChannel waveChannel = findCChanell(waveChannelList, route.getaEnd());
//                    if (waveChannel != null ) {
//                        if ( !sncChannels.contains(waveChannel))
//                            sncChannels.add(waveChannel);
//                        sncCCDns.add(route.getCcOrSectionDn());
//                        //      ptps.add(route.getzPtp());
//                    }
//
//                    CChannel waveChannel2 = findCChanell(waveChannelList, route.getzEnd());
//                    if (waveChannel2 != null  ) {
//                        if ( !sncChannels.contains(waveChannel2))
//                            sncChannels.add(waveChannel2);
//                        sncCCDns.add(route.getCcOrSectionDn());
//                        //    ptps.add(route.getaPtp());
//                    }
//                }
//
//            }



            for (String ccDn : sncCCDns) {
                cPath_ccs.add(U2000MigratorUtil.createCPath_CC(emsdn, ccDn, cPath));
            }

            for (String sectionDn : sncSectionDns) {
                cPath_sections.add(U2000MigratorUtil.createCPath_Section(emsdn, sectionDn, cPath));
            }

            for (CChannel subwaveChannel : sncChannels) {
                cPath_channels.add(U2000MigratorUtil.createCPath_Channel(emsdn, subwaveChannel, cPath));
            }


//            if (sncCCDns == null || sncCCDns.size() == 0)
//                getLogger().error("无法找到 cc,snc="+snc.getDn());
//            if (sncSectionDns == null || sncSectionDns.size() == 0)
//                getLogger().error("无法找到 section,snc="+snc.getDn());
            if (sncChannels == null || sncChannels.size() == 0)
                getLogger().error("无法找到 path-channel,snc="+snc.getDn());
        }

        di2.insert(subWaveChannelList);
        di2.end();




        for (SubnetworkConnection snc : dsrList) {
            if (checkEndContainsSubCtp(snc.getaEnd()) || checkEndContainsSubCtp(snc.getzEnd())) continue;




            String aend = snc.getaEnd();
            String zend = snc.getzEnd();
            if (!DatabaseUtil.isSIDExisted(CCTP.class,aend)) {
                CCTP a = makeupCTP(aend, snc.getDn());
                a.setTag1("MAKEUP_SNC");
                ctpsToMakeUp.add(a);
            }
            if (!DatabaseUtil.isSIDExisted(CCTP.class,zend)) {
                CCTP z = makeupCTP(zend, snc.getDn());
                z.setTag1("MAKEUP_SNC");
                ctpsToMakeUp.add(z);
            }





            CRoute cRoute = U2000MigratorUtil.transRoute(emsdn, snc);
            cRoute.setCategory("DSR");
            cRoutes.add(cRoute);

            List<CChannel> sncChannels = new ArrayList<CChannel>();
            HashSet<String> sncCCDns = new HashSet<String>();
            HashSet<String> sncSectionDns = new HashSet<String>();
            //     HashSet ptps = new HashSet();
            List<R_TrafficTrunk_CC_Section> routes = routeMap.get(snc.getDn());
            if (routes == null || routes.isEmpty()) {
                getLogger().error("DSR路由为空：snc"+snc.getDn());
                continue;
            }

            boolean b = searchOCHRoute(snc,aend,zend,subWaveChannelList,routes,sncChannels,sncCCDns);
            if (!b)
                b = searchOCHRoute(snc,zend,aend,subWaveChannelList,routes,sncChannels,sncCCDns);

            if (b)
                getLogger().info("搜索ROUTE路由成功 snc="+snc.getDn());
            else
                getLogger().info("!!!搜索ROUTE路由失败 snc="+snc.getDn());



            for (String ccDn : sncCCDns) {
                cRoute_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, ccDn, cRoute));
            }


            for (String sectionDn : sncSectionDns) {
                if (!cSectionMap.containsKey(sectionDn)) {
                    sectionsToMakeUp.add(createSection(sectionDn));

                    //EMS:TZ-OTNU31-1-P@TopologicalLink:/d=src/ManagedElement{70127685(P)}FTP{/direction=sink/rack=0/shelf=7/slot=24/port=78151743}_/d=sink/ManagedElement{70127685(P)}FTP{/direction=src/rack=0/shelf=7/slot=4/port=78200862}

                }

            }

            for (String sectionDn : sncSectionDns) {
                cRoute_sections.add(U2000MigratorUtil.createCRoute_Section(emsdn, sectionDn, cRoute));



            }

            for (CChannel subwaveChannel : sncChannels) {
                cRoute_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, subwaveChannel, cRoute));
            }

//            if (sncCCDns == null || sncCCDns.size() == 0)
//                getLogger().error("无法找到 cc,snc="+snc.getDn());
//            if (sncSectionDns == null || sncSectionDns.size() == 0)
//                getLogger().error("无法找到 section,snc="+snc.getDn());
            if (sncChannels == null || sncChannels.size() == 0)
                    getLogger().error("无法找到 route-channel,snc="+snc.getDn());

        }

        DataInserter di = new DataInserter(emsid);
        try {
            di.insert(sectionsToMakeUp);
            removeDuplicateDN(ctpsToMakeUp);
            di.insert(ctpsToMakeUp);
            di.insert(ccsToMakeup);
            di.insert(cPaths);
            di.insert(cRoutes);
            di.insert(cPath_ccs);
            di.insert(cPath_channels);
            di.insert(cPath_sections);
            di.insert(cRoute_ccs);
            di.insert(cRoute_channels);
            di.insert(cRoute_sections);
        } catch (Exception e) {
            getLogger().error(e, e);
        }

        di.end();

    }
    private List<CCrossConnect> ccsToMakeup = new ArrayList<CCrossConnect>();
    private boolean searchOCHRoute(SubnetworkConnection snc,String aend,String zend,List<CChannel> waveChannelList,
                                   List<R_TrafficTrunk_CC_Section> _routes,List<CChannel> sncChannels,HashSet<String> sncCCDns) {
        List<CChannel> channels = new ArrayList<CChannel>(waveChannelList);
        List<R_TrafficTrunk_CC_Section> routes = new ArrayList<R_TrafficTrunk_CC_Section>(_routes);
        int i = 0;
        while (true) {
            if (i++ > 100) return false;      //防止栈溢出
            if (aend.equals("EMS:TZ-OTNU31-1-P@ManagedElement:70127586(P)@PTP:/direction=sink/rack=0/shelf=2/slot=10/port=26509313@CTP:/layerrate=41/oms=1/och=67"))
                System.out.println();
            if (aend.equals(zend)){

                return true;
            }
            LinkInfo cc = MigrateUtil.findChannel(channels,aend,true);
            if (cc != null) {
                aend = cc.otherSide;
                channels.remove(cc.obj);
                sncChannels.add((CChannel)cc.obj);
            }
            else {
                cc = MigrateUtil.findCC(routes, aend, true);
                if (cc != null) {
                    aend = cc.otherSide;
                    routes.remove(cc.obj);      //routes中去掉这条cc
                    sncCCDns.add(cc.dn);
                }
            }
            boolean makeup = false;
            if (cc == null) {
                makeup = true;
                String zz = null;
                R_TrafficTrunk_CC_Section _route = null;
                String aendpt = DNUtil.extractPortDn(aend);
                String aendType = getCTPType(aend);
                for (R_TrafficTrunk_CC_Section route : routes) {
                    if (route.getType().equals("CC")) {

                        if (!route.getaPtp().equals(aendpt) && getCTPType(route.getaEnd()).equals(aendType) && MigrateUtil.ctpInSameCard(route.getaEnd(),aend)) {
                            zz = route.getaEnd();
                            _route = route;
                            break;
                        }
                        if (!route.getzPtp().equals(aendpt) && getCTPType(route.getzEnd()).equals(aendType) && MigrateUtil.ctpInSameCard(route.getzEnd(),aend)) {
                            zz = route.getzEnd();
                            _route = route;
                            break;
                        }
                    }
                }

                if (zz == null) {
                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {

                            if (!route.getaPtp().equals(aendpt) && getCTPType(route.getaEnd()).equals("oms") && MigrateUtil.ctpInSameCard(route.getaEnd(),aend)) {
                                String omsPtp = route.getaPtp();
                                List<CCTP> omsCtps = ptp_ctpMap.get(omsPtp);
                                for (CCTP ctp : omsCtps) {
                                    String och = DNUtil.extractOCHno(ctp.getDn());
                                    if (och != null && och.equals(DNUtil.extractOCHno(aend))) {
                                        zz = ctp.getDn();
                                        break;
                                    }
                                }

                              //  zz = route.getaEnd();
                                _route = route;
                                break;
                            }
                            if (!route.getzPtp().equals(aendpt) && getCTPType(route.getzEnd()).equals("oms") && MigrateUtil.ctpInSameCard(route.getzEnd(),aend)) {
                                String omsPtp = route.getzPtp();
                                List<CCTP> omsCtps = ptp_ctpMap.get(omsPtp);
                                if (omsCtps == null)
                                    omsCtps = new ArrayList<CCTP>();
                                for (CCTP ctp : omsCtps) {
                                    String och = DNUtil.extractOCHno(ctp.getDn());
                                    if (och != null && och.equals(DNUtil.extractOCHno(aend))) {
                                        zz = ctp.getDn();
                                        break;
                                    }
                                }
                                _route = route;
                                break;
                            }
                        }
                    }
                }
                if (zz != null) {
                    try {
                        if (makeup && DNUtil.extractCTPSimpleName(_route.getaEnd()).equals(DNUtil.extractCTPSimpleName(_route.getzEnd()))) {

                        }
                        else
                             routes.remove(_route);
                    } catch (Exception e) {
                        getLogger().error(e, e);
                    }

                    ccsToMakeup.add(makeupCC(aend, zz));

                    aend = zz;
                    cc = new LinkInfo(null,null,null,null,null);

                }
            }

            if (cc == null) {

                return false;
            }
        }

    }

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
    private List<CChannel> createSubwaveChannels(CPath path,CCTP pathAside,CCTP pathZside) {
//        List<CCTP> actps = ctpParentChildMap.get(pathAside.getDn());
//        List<CCTP> zctps = ctpParentChildMap.get(pathZside.getDn());
        List<CCTP> actps = findAllChildCTPS(pathAside.getDn());
        List<CCTP> zctps = findAllChildCTPS(pathZside.getDn());
        List<CChannel> subwaveChannels = new ArrayList<CChannel>();

        if (actps != null && actps.size() > 0 && zctps != null && zctps.size() > 0) {
            if (actps.size() >1 && zctps.size() > 1)
                System.out.print("");
            for (CCTP actp : actps) {
                boolean match = false;
                for (CCTP zctp : zctps) {
                    String aname = actp.getDn().substring(pathAside.getDn().length());
                    String zname = zctp.getDn().substring(pathZside.getDn().length());
                    if (aname.equals(zname)) {
                        match = true;
                        subwaveChannels.add(createCChanell(path,actp,zctp));
                    }
                }

                if (!match) {
                    for (CCTP zctp : zctps) {
                        subwaveChannels.add(createCChanell(path,actp,zctp));
                    }
                }
            }
        } else {
            //           getLogger().error("无法找到path两端的子ctp，path="+path.getDn());

            //      subwaveChannels.add(createCChanell(path,pathAside,pathZside));

        }
//        if (subwaveChannels.size() == 0)  {
//            getLogger().error("打散OCH到子波失败，path="+path.getDn());
//            for (CCTP actp : actps) {
//                getLogger().error("actp="+actp.getDn());
//            }
//            getLogger().error("--------------------------------------------");
//            for (CCTP zctp :zctps) {
//                getLogger().error("zctp="+zctp.getDn());
//            }
//            getLogger().error("*****************************************************");
//        }
        return subwaveChannels;

    }
    private CChannel createCChanell(BObject parent,CCTP acctp, CCTP zcctp) {
        String aSideCtp = acctp.getDn();
        String zSideCtp = zcctp.getDn();
        CChannel cChannel = new CChannel();
        cChannel.setDn(aSideCtp + "<>" + zSideCtp);
        cChannel.setSid(DatabaseUtil.nextSID(CChannel.class));
        cChannel.setAend(aSideCtp);
        cChannel.setZend(zSideCtp);
        cChannel.setSectionOrHigherOrderDn(parent.getDn());
        if (parent instanceof CSection)
            cChannel.setName("och="+DNUtil.extractOCHno(acctp.getDn()));
        if (parent instanceof CPath) {
            cChannel.setName(((CPath) parent).getName());
            cChannel.setNo(DNUtil.extractOCHno(acctp.getDn()));
        }


        cChannel.setRate(acctp.getRate());
        if (parent instanceof CSection)
            cChannel.setCategory("波道");
        if (parent instanceof CPath)
            cChannel.setCategory("子波道");
        cChannel.setTmRate(SDHUtil.getTMRate(acctp.getRate()));
        cChannel.setRateDesc("OCH");
        if (acctp.getDn().contains("100ge"))  {
            cChannel.setRateDesc("ODU4");
            cChannel.setTmRate("100G");
        }
        if (acctp.getDn().contains("odu4="))  {
            cChannel.setRateDesc("ODU4");
            cChannel.setTmRate("100G");
        }
        if (acctp.getDn().contains("odu3="))  {
            cChannel.setRateDesc("ODU3");
            cChannel.setTmRate("40G");
        }
        if (acctp.getDn().contains("odu2="))  {
            cChannel.setRateDesc("ODU2");
            cChannel.setTmRate("10G");
        }

        if (acctp.getDn().contains("odu2e="))  {
            cChannel.setRateDesc("ODU2e");
            cChannel.setTmRate("10G");
        }
        if (acctp.getDn().contains("odu1="))  {
            cChannel.setRateDesc("ODU1");
            cChannel.setTmRate("2.5G");
        }

        if (acctp.getDn().contains("odu0="))  {
            cChannel.setRateDesc("ODU0");
            cChannel.setTmRate("1.25G");
        }





     //   cChannel.setRateDesc(SDHUtil.rateDesc(acctp.getRate()));


        cChannel.setFrequencies(acctp.getFrequencies());

        cChannel.setWaveLen( HwDwdmUtil.getWaveLength( (acctp.getFrequencies())));

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

    private CSection createOMS( CPTP aptp, CPTP zptp) throws Exception {
        CSection section = new CSection();
        section.setType("OMS");
        section.setAendTp(aptp.getDn());
        section.setZendTp(zptp.getDn());
        section.setDirection(0);
        section.setAptpId(aptp.getId());
        section.setZptpId(zptp.getId());
        section.setDn( aptp.getDn() + "<>" + zptp.getDn());
        section.setEmsName(emsdn);
        section.setSpeed("40G");
        section.setRate("41");
        return section;

    }

    private void findNext(String ptpDn,List<String> processedCC,List<String> processedSection,List<Section> resultSectionList,HashMap<String,CCrossConnect> ccMap) {
        if (ccMap.get(ptpDn) != null) {
            CCrossConnect cc = ccMap.get(ptpDn);
        }
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
                if (section.getDn().contains("FTP"))
                    System.out.println();
                if (section.getaEndTP().contains("FTP") || section.getzEndTP().contains("FTP"))
                    continue;
                if (!section.getRate().equals("42") && !section.getRate().equals("1535"))
                     continue;
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

    public static void main(String[] args) {
        String aBindTp =  "EMS{ZJ-ZTE-1-P}ManagedElement{63(P)}PTP{/direction=sink/rack=0/shelf=1/slot=22/port=26378241}";
        aBindTp = aBindTp.substring(0,aBindTp.length()-1);
        aBindTp = aBindTp.replaceAll("\\{",":").replaceAll("\\}","@");
        System.out.println("aBindTp = " + aBindTp);
    }

}
