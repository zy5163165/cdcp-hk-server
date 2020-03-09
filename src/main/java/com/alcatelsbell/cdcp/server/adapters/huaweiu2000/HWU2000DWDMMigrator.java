package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.server.adapters.*;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.db.components.service.DBUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.util.log.LogUtil;
import com.alcatelsbell.nms.valueobject.BObject;
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
 * Date: 14-7-7
 * Time: 下午4:43
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HWU2000DWDMMigrator extends AbstractDBFLoader{

    HashMap<String,List<CCTP>> ctpParentChildMap = new HashMap<String, List<CCTP>>();

    HashMap<String,CPTP>  ptpMap = new HashMap<String, CPTP>();
    HashMap<String,List<CPTP>>  cardPtpMap = new HashMap<String, List<CPTP>>();
    HashMap<String,CCTP>  ctpMap = new HashMap<String, CCTP>();
    HashMap<String,List<CCrossConnect>> aptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CCrossConnect>> ptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CSection>> ptpSectionMap = new HashMap<String, List<CSection>>();
    HashMap<String,List<CCTP>> ptp_ctpMap = new HashMap<String, List<CCTP>>();
    List<CSection> cSections = new ArrayList<CSection>();
    HashMap<String,CEquipment> equipmentMap = new HashMap<String, CEquipment>();

    public HWU2000DWDMMigrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog("HWOTN_"+emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }

    private static FileLogger fLogger = new FileLogger("U2000-OTN-Device.log");

    public HWU2000DWDMMigrator(Serializable object, String emsdn) {
        this.emsdn = emsdn;
        this.resultObject = object;
        MigrateThread.thread().initLog(fLogger);
    }

    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "华为");

        logAction(emsdn + " migrateManagedElement", "同步网元", 1);
        migrateManagedElement();
        migrateSubnetwork();
//

        logAction("migrateEquipmentHolder", "同步槽道", 5);
        migrateEquipmentHolder();


//
        logAction("migrateEquipment11", "同步板卡", 10);
        migrateEquipment();
        logAction("migratePTP", "同步端口", 20);
        migratePTP();

        migrateSection();

        logAction("migrateCTP", "同步CTP", 25);
        migrateCTP();

        logAction("migrateCC", "同步CC", 40);
        migrateCC();

        logAction("migrateOMS", "同步逻辑资源", 60);
        migrateOMS();
        sd.release();
//        logAction("migrateSection", "同步段", 25);
//        migrateSection();
//
//        logAction("migrateCTP", "同步CTP", 30);
//        migrateCTP();
//          migrateSubnetworkConnection();
    }



    private HashMap<String,EquipmentHolder> allShelfMap = new HashMap<String, EquipmentHolder>();

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
        List<EquipmentHolder> subshelfs = new ArrayList<EquipmentHolder>();
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
            if (equipmentHolder.getHolderType().equals("rack")) {
                racks.add(equipmentHolder);
                // } else if (rack != null && shelf != null && slot == null) {
            } else if (equipmentHolder.getHolderType().equals("shelf")) {
                shelfs.add(equipmentHolder);
            }else if (equipmentHolder.getHolderType().equals("sub_shelf")) {
                subshelfs.add(equipmentHolder);
            }

            else if (equipmentHolder.getHolderType().equals("slot")) {
                slots.add(equipmentHolder);
            } else if (equipmentHolder.getHolderType().equals("sub_slot")) {
                subslots.add(equipmentHolder);
            }
        }
        // ////////////////// 将EH分类///////////////////

        for (EquipmentHolder shelf : shelfs) {
            allShelfMap.put(shelf.getDn(),shelf);
        }

        for (EquipmentHolder equipmentHolder : racks) {
            CdcpObject cEquipmentHolder = transEquipmentHolder(equipmentHolder);
            di.insert(cEquipmentHolder);
        }
        for (EquipmentHolder equipmentHolder : subshelfs) {
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

    private HashMap<String,String> deviceProductName = new HashMap<String, String>();
    @Override
    public CDevice transDevice(ManagedElement me) {
             CDevice cDevice = super.transDevice(me);
        deviceProductName.put(me.getDn(),cDevice.getProductName());
        if (cDevice.getProductName() != null && cDevice.getProductName().equals("VNE"))
            cDevice.setProductName("VNE_OTN");
        return cDevice;
    }

    protected Class[] getStatClss() {
        return new Class[]{CCrossConnect.class, CChannel.class, CPath.class, CRoute.class, CPath_Channel.class,
                CPath_CC.class, CRoute_Channel.class,
                CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class, CVirtualBridge.class,
                CMP_CTP.class, CEthTrunk.class, CStaticRoute.class, CEthRoute.class, CEthTrunk_SDHRoute.class,
                CEthRoute_StaticRoute.class, CEthRoute_ETHTrunk.class, CSection.class, CCTP.class, CDevice.class, CPTP.class, CTransmissionSystem.class, CTransmissionSystem_Channel.class};
    }

    HashSet<String> shelfsContainsSub = new HashSet<String>();
    public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
        CdcpObject cdcpObject = super.transEquipmentHolder(equipmentHolder);
        if (cdcpObject instanceof CSlot) {
             if (((CSlot) cdcpObject).getAcceptableEquipmentTypeList().length() > 2000)
                 ((CSlot) cdcpObject).setAcceptableEquipmentTypeList(null);
        }
        if (cdcpObject instanceof CShelf) {
            if (cdcpObject.getDn().contains("sub_shelf")) {
                if (cdcpObject.getTag1() != null) {
                    shelfsContainsSub.add(cdcpObject.getTag1());
                }
            }

            if (shelfsContainsSub.contains(cdcpObject.getDn())) {
                return null;
            }

            String additionalInfo = equipmentHolder.getAdditionalInfo();
            Map<String, String> map = MigrateUtil.transMapValue(additionalInfo);
            String shelftype = map.get("ShelfType");
       //     if (shelftype == null)
                shelftype = map.get("ProductName");
            if (shelftype == null) {
                shelftype = deviceProductName.get(((CShelf) cdcpObject).getParentDn());
            }
//            if (shelftype == null)
//                shelftype = map.get("ShelfType");
            ((CShelf) cdcpObject).setShelfType(shelftype);
            String no = equipmentHolder.getNativeEMSName();
            if (no != null && no.startsWith("Shelf-"))
                no = no.substring(6);
            if (no != null && no.startsWith("Slot-"))
                no = no.substring(5);
            ((CShelf) cdcpObject).setNo(no);

            if (cdcpObject.getDn().contains("sub_shelf")) {
                EquipmentHolder shelf = allShelfMap.get(cdcpObject.getTag1());
                String add = shelf.getAdditionalInfo();
                Map<String, String> m = MigrateUtil.transMapValue(add);
                String st = m.get("ProductName");
                if (st == null)
                    st = m.get("ShelfType");
                if (shelftype != null)
                    ((CShelf) cdcpObject).setShelfType(shelftype);
                else
                    ((CShelf) cdcpObject).setShelfType(st);
                String dn = cdcpObject.getDn();
                if (shelf != null)
                    ((CShelf) cdcpObject).setNo(shelf.getNativeEMSName()+"-"+dn.substring(dn.lastIndexOf("=")+1));
            }

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

        if (cctp.getFrequencies() == null || cctp.getFrequencies().equals("0.000") ) {
            CPTP omsPort = ptpMap.get(ctp.getPortdn());
            if (omsPort.getTag2() != null) {
                String seq = DNUtil.extractOCHno(ctp.getDn());
                if (seq != null) {
                    if (omsPort.getTag2().equals("E")) {
                        cctp.setFrequencies(HwDwdmUtil.getEvenFrequence(Integer.parseInt(seq)));
                    } else if (omsPort.getTag2().equals("O")) {
                        cctp.setFrequencies(HwDwdmUtil.getOddFrequence(Integer.parseInt(seq)));
                    }
                    cctp.setTag2(omsPort.getTag2());
                }

            }
        }
        String dn = cctp.getDn();
        int i = dn.indexOf("/", dn.indexOf("CTP:/") + 6);
        if (i > -1) {
            String parentDn = dn.substring(0,dn.lastIndexOf("/"));
            cctp.setParentCtpdn(parentDn);
            DSUtil.putIntoValueList(ctpParentChildMap,parentDn,cctp);
        }

        return cctp;
    }


    protected void migrateCC() throws Exception {
        List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);

        if (isEmptyResult(ccs)) {
            return;
        }

        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        DataInserter di = new DataInserter(emsid);
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        try {

            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
                    String _dn = cc.getDn();
                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));


                    List<CCrossConnect> splitCCS = U2000MigratorUtil.transCCS(cc, emsdn);
                    newCCs.addAll(splitCCS);

                    for (CCrossConnect ncc : splitCCS) {
                        ncc.setRate(MigrateUtil.transMapValue(cc.getAdditionalInfo()).get("ClientType"));


                        if (_dn.contains("odu4="))
                            ncc.setRate("odu4");

                        if (_dn.contains("otu3="))
                            ncc.setRate("otu3");

                        if (_dn.contains("odu3="))
                            ncc.setRate("odu3");

                        if (_dn.contains("otu2="))
                            ncc.setRate("otu2");

                        if (_dn.contains("odu2="))
                            ncc.setRate("odu2");

                        if (_dn.contains("odu1="))
                            ncc.setRate("odu1");

                        if (_dn.contains("odu0="))
                            ncc.setRate("odu0");

                        if (_dn.contains("dsr="))
                            ncc.setRate("Client");


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

    @Override
    protected List insertCtps(List<CTP> ctps) throws Exception {
        DataInserter di = new DataInserter(emsid);
        getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
        List<CCTP> cctps = new ArrayList<CCTP>();
        if (ctps != null && ctps.size() > 0) {

            HashMap<String,List<CTP>> portCtps = new HashMap<String, List<CTP>>();
            for (CTP ctp : ctps) {
                DSUtil.putIntoValueList(portCtps,ctp.getPortdn(),ctp);
            }

            Set<String> ptpDns = portCtps.keySet();

            for (String ptpDn : ptpDns) {
                List<CTP> p_ctps = portCtps.get(ptpDn);
                if (ptpDn.equals("EMS:HUZ-U2000-1-OTN@ManagedElement:4063258@PTP:/rack=1/shelf=3145772/slot=35/domain=wdm/port=3"))
                    System.out.println();
                processCtpsInSamePtp(p_ctps);
                for (CTP ctp : p_ctps) {
                    CCTP cctp = transCTP(ctp);
                    if (cctp != null) {
                        cctps.add(cctp);
                        DSUtil.putIntoValueList(ptp_ctpMap, cctp.getParentDn(), cctp);
                        ctpMap.put(cctp.getDn(),cctp);
                        di.insert(cctp);
                    }
                }
            }



        }

        di.end();
        return cctps;
    }

    private void processCtpsInSamePtp(List<CTP> p_ctps) {

        try {
            List<CTP> tobeRemoved = new ArrayList<CTP>();
            for (CTP p_ctp : p_ctps) {
                String dn = p_ctp.getDn();
                String odu2 = getOduValue(dn,"odu2");
                String odu1 = getOduValue(dn,"odu1");
                String odu0 = getOduValue(dn,"odu0");
                String och = getOduValue(dn,"och");


                if (odu2 != null && odu0 != null) {
                    int n_odu1 = (Integer.parseInt(odu0)+1)/2;

                    for (CTP pCtp : p_ctps) {
                        String _dn = pCtp.getDn();
                        if (och.equals(getOduValue(_dn,"och"))
                                && odu2.equals(getOduValue(_dn,"odu2"))
                                && (n_odu1+"").equals(getOduValue(_dn,"odu1"))
                                ){
                             if (!tobeRemoved.contains(pCtp))
                                 tobeRemoved.add(pCtp);
                        }
                    }
                }

            }

            p_ctps.removeAll(tobeRemoved);
        } catch ( Exception e) {
            getLogger().error(e, e);
        }
    }

    private static String getOduValue(String dn,String key) {
        if (dn.contains(key)) {
            int idx = dn.indexOf(key);
            int idx2 = dn.indexOf("/",idx);
            if (idx2 > -1) {
                return dn.substring(idx+(key+"=").length(),idx2);
            } else
                return dn.substring(idx+(key+"=").length());
        }
        return null;
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
        cptp.setSpeed(DicUtil.getSpeed(cptp.getLayerRates()));
        if (cptp.getSpeed() == null) cptp.setSpeed("40G");
        CEquipment card = equipmentMap.get(carddn);
        if (card != null) {
            String additionalInfo = card.getAdditionalInfo();
            HashMap<String, String> map = MigrateUtil.transMapValue(additionalInfo);
            String key  = "Port_"+cptp.getNo()+"_SFP";
            String value = map.get(key);
            if (value != null && value.contains("Mb/s")) {
                String size = value.substring(0, value.indexOf("Mb/s"));
                int g10 = Integer.parseInt(size) / 100;
                cptp.setSpeed(getSpeed((float)g10/10f));
            }
            //Port_3_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)
            //AlarmSeverity:||HardwareVersion:VER.B||Port_1_SFP:11100Mb/s-1558.58nm-LC-40km(SMF)||Port_1_SFP_BarCode:||Port_3_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_3_SFP_BarCode:||Port_4_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_4_SFP_BarCode:1QU202105135308||Port_5_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_5_SFP_BarCode:1QU202105135221||Port_6_SFP:2500Mb/s-1310nm-LC-15km(0.009mm)||Port_6_SFP_BarCode:1QU202105135236||
        }


        cptp.setEoType(DicUtil.getEOType(cptp.getDn(),cptp.getLayerRates()));
        cptp.setTag3(ptp.getId() + "");
        if (cptp.getEoType() == DicConst.EOTYPE_ELECTRIC && "OPTICAL".equals(cptp.getType()))
            cptp.setType("ELECTRICAL");

        if (cptp.getEoType() == DicConst.EOTYPE_UNKNOWN && "OPTICAL".equals(cptp.getType()))
            cptp.setEoType(DicConst.EOTYPE_OPTIC);


         ptpMap.put(cptp.getDn() ,cptp);
        DSUtil.putIntoValueList(cardPtpMap,carddn,cptp);

        return cptp;
    }

    private String getSpeed(float g) {
        if (g >= 10 && g < 15)
            return "10G";
        if (g >= 1 && g < 1.5)
            return "1.25G";
        if (g >= 2 && g < 3)
            return "2.5G";
        return g+"G";

    }





    public void migrateOMS() throws Exception {
        if (!isTableHasData(SubnetworkConnection.class))
            return;
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



        List<CChannel> waveChannelList = null;
        try {
//            for (CrossConnect cc : ccs) {
//                if (cc.getaEndTP().equals(cc.getzEndTP()))
//                    System.out.println("cc = " + cc);
//                List<String> aends = DNUtil.merge(cc.getaEndNameList().split(Constant.listSplitReg));
//                List<String> zends = DNUtil.merge(cc.getzEndNameList().split(Constant.listSplitReg));
//                for (String aend : aends) {
//                    for (String zend : zends) {
//                        CCrossConnect ncc = U2000MigratorUtil.transCC(cc, aend, zend);
//                        ncc.setId(cc.getId());
//                        DSUtil.putIntoValueList(aptpCCMap,ncc.getAptp(),ncc);
//    //                    DSUtil.putIntoValueList(aptpCCMap,ncc.getZptp(),ncc);
//                    }
//                }
//            }
//            List<CSection> cSections = new ArrayList<CSection>();
//            for (Section section : sections) {
//                CSection cSection = U2000MigratorUtil.transSection(section);
//                cSection.setId(section.getId());
//                cSections.add(cSection);
//                DSUtil.putIntoValueList(ptpSectionMap,cSection.getAendTp(),cSection);
//         //       DSUtil.putIntoValueList(ptpSectionMap,cSection.getZendTp(),cSection);
//            }


            List<CSection> omsList = new ArrayList<CSection>();
            List<CSection> updateOTS = new ArrayList<CSection>();
            List<COMS_CC> omsCClist = new ArrayList<COMS_CC>();
            List<COMS_Section> omsSectionList = new ArrayList<COMS_Section>();
            for (CSection cSection : cSections) {
                String aendTp = cSection.getAendTp();
                CPTP aptp = ptpMap.get(aendTp);
                if (aptp == null) {
                   continue;
                }
                if (HwDwdmUtil.isOMSRate(aptp.getRate())) {
                    PathFindAlgorithm pathFindAlgorithm = new PathFindAlgorithm(getLogger(), aptpCCMap, ptpSectionMap, ptpMap);
             //       System.out.println("startPtp="+aptp.getDn());
                    pathFindAlgorithm.findSingleDirection(cSection,cSection.getZendTp());
                    if (!pathFindAlgorithm.endPtp.isEmpty()) {
                        if (pathFindAlgorithm.endPtp.size() > 1)
                            getLogger().error("找到超过两个ENDPTP："+aptp.getDn());

//                        if (aptp.getDn().equals("EMS:HZ-U2000-3-P@ManagedElement:4063234@PTP:/rack=1/shelf=3145761/slot=401/domain=wdm/port=1"))
//                            System.out.println( );
//                        if (aptp.getDn().equals("EMS:HZ-U2000-3-P@ManagedElement:4063243@PTP:/rack=1/shelf=3145731/slot=1/domain=wdm/port=1"))
//                            System.out.println( );


                        CSection oms = createOMS(aptp, ptpMap.get(pathFindAlgorithm.endPtp.get(0)));
                        omsList.add(oms);
                         PathFindAlgorithm.FindStack  findStacks = pathFindAlgorithm.findStacks.get(0);
                        List ccAndSections = findStacks.ccAndSections;
                        if (ccAndSections == null || ccAndSections.size() == 0)
                            getLogger().error("无法找到 section,OMS="+oms.getDn());
                        for (Object ccAndSection : ccAndSections) {
                            if (ccAndSection instanceof CSection) {
                             //   ((CSection) ccAndSection).setOmsDn(oms.getDn());
                            //    updateOTS.add((CSection) ccAndSection);
                                COMS_Section coms_section = new COMS_Section();
                                coms_section.setDn(SysUtil.nextDN());
                                coms_section.setOmsdn(oms.getDn());
                                coms_section.setSectiondn(((CSection) ccAndSection).getDn());
                                coms_section.setEmsName(emsdn);
                                omsSectionList.add(coms_section);
                            }
                            if (ccAndSection instanceof CCrossConnect) {
                                //   ((CSection) ccAndSection).setOmsDn(oms.getDn());
                                //    updateOTS.add((CSection) ccAndSection);
                                COMS_CC coms_section = new COMS_CC();
                                coms_section.setDn(SysUtil.nextDN());
                                coms_section.setOmsdn(oms.getDn());
                                coms_section.setCcdn(((CCrossConnect) ccAndSection).getDn());
                                coms_section.setEmsName(emsdn);
                                omsCClist.add(coms_section);
                            }
                        }

                    } else {
                        getLogger().error("无法找到OMS： ptp="+aptp.getDn());
                    }
    //                System.out.println("startPtp=" + aptp.getDn());
    //                System.out.println("endPtp=" + pathFindAlgorithm.endPtp+" size="+pathFindAlgorithm.endPtp.size());
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
                String aendTp = cSection.getAendTp();
                String zendTp = cSection.getZendTp();
                List<CCTP> acctps = ptp_ctpMap.get(aendTp);
                List<CCTP> zcctps = ptp_ctpMap.get(zendTp);
                if (acctps == null || acctps.isEmpty() || zcctps == null) {
                    getLogger().error("无法找到CTP，端口："+aendTp);
                } else {
                    for (CCTP acctp : acctps) {
                        for (CCTP zcctp : zcctps) {


                            String och = DNUtil.extractOCHno(acctp.getDn());
                            String och2 = DNUtil.extractOCHno(zcctp.getDn());


                            if (och != null && och.equals(och2)) {


//
//                                String asideCTP = MigrateUtil.getCrossCtp(acctp.getDn(), ptpCCMap.get(acctp.getPortdn()));
//
//                                String zsideCTP = MigrateUtil.getCrossCtp(zcctp.getDn(), ptpCCMap.get(zcctp.getPortdn()));
//                           //     waveChannelList.add(createCChanell(cSection, acctp, zcctp));
//                                if (asideCTP == null) {
//                                    getLogger().error("无法找到OMS两端交叉到波道的ctp:"+acctp.getDn());
//                                }
//                                if (zsideCTP == null) {
//                                    getLogger().error("无法找到OMS两端交叉到波道的ctp:"+zcctp.getDn());
//                                }
//                                String dd = "EMS:HZ-U2000-3-P@ManagedElement:4063255@PTP:/rack=1/shelf=3145753/slot=101/domain=wdm/port=14@CTP:/och=1";
//                                if (asideCTP.equals(dd) || zsideCTP.equals(dd))
//                                    System.out.println("debug = " + dd);
//                                if (asideCTP != null && zsideCTP != null)
//                                    waveChannelList.add(createCChanell(cSection, ctpMap.get(asideCTP), ctpMap.get(zsideCTP)));
                                waveChannelList.add(createCChanell(cSection,acctp, zcctp));


                                break;
                            }
                        }
                    }
                }
            }
            removeDuplicateDN(waveChannelList);

            di = new DataInserter(emsid);
            di.insert(waveChannelList);
            di.end();
        } catch (Exception e) {
            getLogger().error(e, e);
        }

        HashMap<String,List<CChannel>> ctpWaveChannel = new HashMap<String, List<CChannel>>();
        for (CChannel cChannel : waveChannelList) {
            DSUtil.putIntoValueList(ctpWaveChannel,cChannel.getAend(),cChannel);
            DSUtil.putIntoValueList(ctpWaveChannel,cChannel.getZend(),cChannel);
        }




        //////////////////////////////////////////////////////////////////////
        HashMap<String,List<String>> subwave_routes = new HashMap<String, List<String>>();

        List<CChannel> subWaveChannelList = new ArrayList<CChannel>();
        List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
        List<CPath> cPaths = new ArrayList<CPath>();
        List<CRoute> cRoutes = new ArrayList<CRoute>();
        List<CRoute_CC> cRoute_ccs = new ArrayList<CRoute_CC>();
        List<CRoute_Channel> cRoute_channels = new ArrayList<CRoute_Channel>();
        List<CRoute_Section> cRoute_sections = new ArrayList<CRoute_Section>();

        List<CPath_CC> cPath_ccs = new ArrayList<CPath_CC>();
        List<CPath_Channel> cPath_channels = new ArrayList<CPath_Channel>();
        List<CPath_Section> cPath_sections = new ArrayList<CPath_Section>();

        HashMap<String, List<R_TrafficTrunk_CC_Section>> routeMap = queryTrafficTrunkCCSectionMap();
        List<SubnetworkConnection> ochList = new ArrayList<SubnetworkConnection>();
        List<SubnetworkConnection> dsrList = new ArrayList<SubnetworkConnection>();
        for (SubnetworkConnection snc : sncs) {
             checkSuspend();

            String rate = snc.getRate();

            if (rate != null) {
                if (rate.equals(HWDic.LR_Optical_Channel.value+"")) {
//                    if (checkEndContainsSubCtp(snc.getaEnd()) || checkEndContainsSubCtp(snc.getzEnd())) {
                        ochList.add(snc);
//                    }
//                    else dsrList.add(snc);


                }
                else if (rate.equals(HWDic.LR_DSR_10Gigabit_Ethernet.value+"")) {
                    dsrList.add(snc);
                }
                else if (rate.equals(HWDic.LR_DSR_OC48_and_STM16.value+"")) {
                    dsrList.add(snc);
                }
                else if (rate.equals(HWDic.LR_DSR_OC192_and_STM64.value+"")) {
                    dsrList.add(snc);
                }
                else if (rate.equals(HWDic.LR_DSR_Gigabit_Ethernet.value+"")) {
                    dsrList.add(snc);
                }

                else if (rate.equals(DicConst.LR_OCH_Data_Unit_1+"")) {
                    dsrList.add(snc);
                }

                else if (rate.equals(DicConst.LR_OCH_Data_Unit_2+"")) {
                    dsrList.add(snc);
                }

                else if (rate.equals(DicConst.LR_OCH_Data_Unit_3+"")) {
                    dsrList.add(snc);
                }
                else {
                    getLogger().error("unkonwn rate dsr : "+snc.getRate());
                }


            }
        }


        DataInserter di2 = new DataInserter(emsid);
        for (SubnetworkConnection snc : ochList) {
            checkSuspend();
            if (snc.getDn().equals("EMS:HZ-U2000-3-P@MultiLayerSubnetwork:1@SubnetworkConnection:2011-12-21 06:15:08 - 24796 -wdm"))
                System.out.println("snc = " + snc);
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

//            HashSet ptps = new HashSet();
             for (R_TrafficTrunk_CC_Section route : routes) {
                 //将子波和路由的关系放入map中
                 for (CChannel subwave : subwaves) {
                     DSUtil.putIntoValueList(subwave_routes,subwave.getDn(),route.getCcOrSectionDn());
                 }


                if ("CC".equals(route.getType())) {
                    CChannel waveChannel = findCChanell(ctpWaveChannel, route.getaEnd());
                    if (waveChannel != null ) {
                        if ( !sncChannels.contains(waveChannel))
                            sncChannels.add(waveChannel);
                        sncCCDns.add(route.getCcOrSectionDn());
                  //      ptps.add(route.getzPtp());
                    }

                    CChannel waveChannel2 = findCChanell(ctpWaveChannel, route.getzEnd());
                    if (waveChannel2 != null  ) {
                        if ( !sncChannels.contains(waveChannel2))
                            sncChannels.add(waveChannel2);
                        sncCCDns.add(route.getCcOrSectionDn());
                    //    ptps.add(route.getaPtp());
                    }
                }

            }

            boolean b = searchOCHRoute(routes, sncCCDns, sncSectionDns);


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
                getLogger().error("无法找到 channel,snc="+snc.getDn());
        }

        di2.insert(subWaveChannelList);
        di2.end();

        HashMap<String,List<CChannel>> ctpSubwaveChannel = new HashMap<String, List<CChannel>>();
        for (CChannel cChannel : subWaveChannelList) {
            DSUtil.putIntoValueList(ctpSubwaveChannel,cChannel.getAend(),cChannel);
            DSUtil.putIntoValueList(ctpSubwaveChannel,cChannel.getZend(),cChannel);
        }



        for (SubnetworkConnection snc : dsrList) {
            checkSuspend();
            long t1 = System.currentTimeMillis();

            if (checkEndContainsSubCtp(snc.getaEnd()) || checkEndContainsSubCtp(snc.getzEnd())) {
                getLogger().error("checkEndContainsSubCtp:"+snc.getNativeEMSName());
                continue;
            }

            debug(snc.getDn());

            String aend = snc.getaEnd();
            String zend = snc.getzEnd();
            CRoute cRoute = U2000MigratorUtil.transRoute(emsdn, snc);
            cRoute.setCategory("DSR");
            cRoutes.add(cRoute);
            if  (snc.getaEnd().startsWith("EMS:ZJ-U2000-1-OTN@ManagedElement:33554554") && cRoute.getNativeEmsName().contains("Client"))
                System.out.println("route:"+snc.getDn());

            List<CChannel> sncChannels = new ArrayList<CChannel>();
            HashSet<String> sncCCDns = new HashSet<String>();
            HashSet<String> sncSectionDns = new HashSet<String>();
       //     HashSet ptps = new HashSet();
            List<R_TrafficTrunk_CC_Section> routes = routeMap.get(snc.getDn());
            if (routes == null || routes.isEmpty()) {
                getLogger().error("DSR路由为空：snc"+snc.getDn());
                continue;
            }
            List<String> subChannelRoutes = new ArrayList<String>();
            if (routes != null && routes.size() > 0) {
                for (R_TrafficTrunk_CC_Section route : routes) {
                     if ("CC".equals(route.getType())) {
                       //  CChannel subWaveChannel = findCChanell(subWaveChannelList, route.getaEnd());
                         CChannel subWaveChannel = findSubwaveCChanell(ctpSubwaveChannel, route.getaEnd(), routes, subwave_routes);
                         if (subWaveChannel != null && !sncChannels.contains(subWaveChannel)) {
                             sncChannels.add(subWaveChannel);
                             sncCCDns.add((route.getCcOrSectionDn()));
                             List<String> s = subwave_routes.get(subWaveChannel.getDn());
                             if (s != null) subChannelRoutes.addAll(s);


                             //          ptps.add(route.getzPtp());
                         }
                         //CChannel subWaveChannel2 = findCChanell(subWaveChannelList, route.getzEnd());
                         CChannel subWaveChannel2 = findSubwaveCChanell(ctpSubwaveChannel, route.getzEnd(), routes, subwave_routes);
                         if (subWaveChannel2 != null &&!sncChannels.contains(subWaveChannel2)) {
                             sncChannels.add(subWaveChannel2);
                             sncCCDns.add((route.getCcOrSectionDn()));

                             List<String> s = subwave_routes.get(subWaveChannel2.getDn());
                             if (s != null) subChannelRoutes.addAll(s);
                      //       ptps.add(route.getaPtp() );
                         }
                     }
                }
            }




            //直接根据两端ctp来找

            CChannel subwaveCChanell = findSubwaveCChanell(ctpSubwaveChannel, snc.getaEnd(), routes, subwave_routes);

            if (subwaveCChanell != null && !sncChannels.contains(subwaveCChanell)) {
                sncChannels.add(subwaveCChanell);
                List<String> s = subwave_routes.get(subwaveCChanell.getDn());
                if (s != null) subChannelRoutes.addAll(s);
            }


            CChannel subwaveCChanel2 = findSubwaveCChanell(ctpSubwaveChannel, snc.getzEnd(), routes, subwave_routes);
            if (subwaveCChanel2 != null && !sncChannels.contains(subwaveCChanel2)) {
                sncChannels.add(subwaveCChanel2);
                List<String> s = subwave_routes.get(subwaveCChanell.getDn());
                if (s != null) subChannelRoutes.addAll(s);
            }


            if (sncChannels.size() > 0) {
                for (R_TrafficTrunk_CC_Section route : routes) {
                    //这条路由不在子波的路由中
                    if (!subChannelRoutes.contains(route.getCcOrSectionDn())) {
                        if (route.getType().equals("CC")) {
                            sncCCDns.add(route.getCcOrSectionDn());
                        }
                        else if (route.getType().equals("SECTION")) {
                            sncSectionDns.add(route.getCcOrSectionDn());
                        }
                    }
                }
            }

//            else {  //找不到子波，当做OCH处理
//                getLogger().error("无法找到子波:snc="+snc.getDn());
//                for (R_TrafficTrunk_CC_Section route : routes) {
//                    if ("CC".equals(route.getType())) {
//                        CChannel waveChannel = findCChanell(waveChannelList, route.getaEnd());
//                        if (waveChannel != null && !sncChannels.contains(waveChannel)) {
//                            sncChannels.add(waveChannel);
//                            sncCCDns.add((route.getCcOrSectionDn()));
//                        }
//                        CChannel waveChannel2 = findCChanell(subWaveChannelList, route.getzEnd());
//                        if (waveChannel2 != null &&!sncChannels.contains(waveChannel2)) {
//                            sncChannels.add(waveChannel2);
//                            sncCCDns.add((route.getCcOrSectionDn()));
//                        }
//                    }
//                }
//                searchOCHRoute(routes, sncCCDns, sncSectionDns);
//
//            }


              


            for (String ccDn : sncCCDns) {
               cRoute_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, ccDn, cRoute));
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
                getLogger().error("无法找到 channel,snc="+snc.getDn());

            long t2 = System.currentTimeMillis() - t1;
            getLogger().info("process dsr : "+snc.getDn()+" spend : "+t2+"ms");

        }

        DataInserter di = new DataInserter(emsid);
        try {

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

    private boolean searchOCHRoute(List<R_TrafficTrunk_CC_Section> routes, HashSet<String> sncCCDnsHolder, HashSet<String> sncSectionDnsHolder) {
        HashSet<String> sectionPorts = new HashSet<String>();
        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getType().equals("CC")){
                String aptpDn = route.getaPtp();
                String zptpDn = route.getzPtp();
                CPTP aptp = ptpMap.get(aptpDn);
                CPTP zptp = ptpMap.get(zptpDn);
                if (aptp == null || zptp == null) continue;
                if (aptp.getRate().contains("41")) {
                    sectionPorts.add(zptp.getDn());
                }
                else if (zptp.getRate().contains("41")) {
                    sectionPorts.add(aptp.getDn());
                }

                if ("SAME_ORDER".equals(route.getTag1())) {
                    if (route.getaEnd().contains("oms=")) {
                        continue;
                    }
                    sncCCDnsHolder.add(route.getCcOrSectionDn());

                    if (!aptp.getRate().contains("41")) {
                        sectionPorts.add(aptp.getDn());
                    }
                    if (!zptp.getRate().contains("41")) {
                        sectionPorts.add(zptp.getDn());
                    }
                }

            }


        }

        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getType().equals("SECTION")) {
                if (sectionPorts.contains(route.getaEnd()) || sectionPorts.contains(route.getzEnd()))
                    sncSectionDnsHolder.add(route.getCcOrSectionDn());
            }

        }
          return true;
    }


    private boolean search3(List<R_TrafficTrunk_CC_Section> routes,SubnetworkConnection snc,List<String[]> azs,HashSet<String> sncCCDnsHolder,HashSet<String> sncSectionDnsHolder) {
        String aptp = snc.getaPtp();
        debug("start"+aptp);
        int count = 0;
        while (true) {
            for (String[] az : azs) {
                if (az[0].equals(aptp)) {
                    aptp = az[1];
                    debug(ptpMap.get(aptp));
                }
            }

            for (R_TrafficTrunk_CC_Section route : routes) {
                if (route.getType().equals("SECTION")){
                    if (route.getaEnd().equals(aptp)) {
                        aptp = route.getzEnd();
                        debug(ptpMap.get(aptp));
                        break;
                    }
                }
                else if (route.getType().equals("CC")) {
                    if (route.getaPtp().equals(aptp))  {
                        aptp = route.getzPtp();
                        debug(ptpMap.get(aptp));
                        break;
                    }

                }

            }


            if (aptp.equals(snc.getzPtp())) {
                debug("Found!");
                return true;
            }
            if (count++ == 100) return false;
        }

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
    private CChannel findSubwaveCChanell(HashMap<String,List<CChannel>> ctpChannels,String ctp,List<R_TrafficTrunk_CC_Section> dsrRoutes,HashMap<String,List<String>> subwave_routes) {
        List<CChannel> cChanells = findCChanells(ctpChannels, ctp);
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

                if (subWaveRoutes != null && dsrouteDns.containsAll(subWaveRoutes)) return cChanell;
               // getDuplicateCount(dsro)
            }
            getLogger().error("根据ctp找到多个子波，但无法区分：ctp="+ctp);
            return cChanells.get(0);
        }
        return null;
    }

    private CChannel findCChanell(HashMap<String,List<CChannel>>  ctpChannels,String ctp) {
        if (ctp .equals("EMS:HZ-U2000-3-P@ManagedElement:4063249@PTP:/rack=1/shelf=3145738/slot=20/domain=wdm/port=1@CTP:/och=1/otu2=1"))
            System.out.println("ctp = " + ctp);
        List<CChannel> cChanells = findCChanells(ctpChannels, ctp);
        if (cChanells != null && cChanells.size() > 0)
            return cChanells.get(0);
        return null;
    }

//    private List<CChannel> findCChanells(List<CChannel> channels,String ctp) {
//        List<CChannel> cs = new ArrayList<CChannel>();
//        for (CChannel channel : channels) {
//
//            if (ctp.equals(channel.getAend()) || ctp.equals(channel.getZend()))
//                cs.add(channel);
//        }
//        return cs;
//
//    }
    private List<CChannel> findCChanells(HashMap<String,List<CChannel>> ctpChannels,String ctp) {
        List<CChannel> cChannels = ctpChannels.get(ctp);
        if (cChannels != null) return cChannels;
        return new ArrayList<CChannel>();

    }
    private List<CCTP> findAllChildCTPS(String parentCtp) {

        List<CCTP> all = new ArrayList<CCTP>();
        List<CCTP> cctps = ctpParentChildMap.get(parentCtp);
        if (cctps != null) {
          //  all.addAll(cctps);
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
        if(parent instanceof CSection && (cChannel.getFrequencies() == null || cChannel.getFrequencies().equals("0.000"))) {
            System.out.println();
        }
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
        super.migrateSection();

    }


    @Override
    public CSection transSection(Section section) {
        CSection cSection = super.transSection(section);
        cSection.setType("OTS");
        cSection.setSpeed("40G");
        DSUtil.putIntoValueList(ptpSectionMap,cSection.getAendTp(),cSection);

        try {
            CPTP aptp = ptpMap.get(cSection.getAendTp());
            CPTP zptp = ptpMap.get(cSection.getZendTp());

            if (aptp != null && zptp != null) {
                if (zptp.getNativeEMSName().contains("RE/TE"))
                    aptp.setTag2("E");
                else if (zptp.getNativeEMSName().contains("RO/TO"))
                    aptp.setTag2("O");

                String cardDn = aptp.getParentDn();
                CEquipment card = equipmentMap.get(cardDn);
                if (card != null && (card.getNativeEMSName().contains("M40") || card.getNativeEMSName().contains("D40"))) {
                    List<CPTP> ptps = cardPtpMap.get(cardDn);
                    if (ptps != null) {
                        for (CPTP ptp : ptps) {
                            ptp.setTag2(aptp.getTag2());
                        }
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error(e,e);
        }

        cSections.add(cSection);
        return cSection;
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



    public static void main(String[] args) throws Exception {

        String dn  = "EMS:HUZ-U2000-1-OTN@ManagedElement:4063258@PTP:/rack=1/shelf=3145772/slot=35/domain=wdm/port=3@CTP:/och=1/odu2=1/odu0=1";
        String odu2 = getOduValue(dn, "odu2");
        String odu0 = getOduValue(dn, "odu0");
//        List allObjects = JpaClient.getInstance("cdcp.datajpa").findAllObjects(CDevice.class);
        String fileName=  "d:\\1507\\2016-05-31-170000-TZ-U2000-2-OTN-DayMigration.db";
        String emsdn = "TZ-U2000-2-OTN";
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

        final HWU2000DWDMMigrator loader = new HWU2000DWDMMigrator (fileName, emsdn){
            public void afterExecute() {
                updateEmsStatus(Constants.CEMS_STATUS_READY);
                printTableStat();
          //      IrmsClientUtil.callIRMEmsMigrationFinished(emsdn);
            }
        };
        loader.execute();

//        Thread t = new Thread(){
//            public void run() {
//                try {
//                    loader.execute();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        };
//        t.start();


        Thread.sleep(10000l);
        System.out.println("try to interrupt !!!!");

    }


}
