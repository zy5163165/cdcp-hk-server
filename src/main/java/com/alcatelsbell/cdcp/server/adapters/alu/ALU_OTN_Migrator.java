package com.alcatelsbell.cdcp.server.adapters.alu;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.nodefx.NEWrapper;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HwDwdmUtil;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.DBUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.asb.mule.probe.framework.entity.*;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.FileLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 14-8-29
 * Time: 下午2:00
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ALU_OTN_Migrator extends AbstractDBFLoader {
    HashMap<String,List<CCTP>> ctpParentChildMap = new HashMap<String, List<CCTP>>();
    HashMap<String,SubnetworkConnection> sncMap = new HashMap<String, SubnetworkConnection>();
    HashMap<String, List<String>> snc_chid_parent_dns = new HashMap<String, List<String>>();
    HashMap<String,CPTP>  ptpMap = new HashMap<String, CPTP>();
    HashMap<String,CCTP>  ctpMap = new HashMap<String, CCTP>();
    HashMap<String,List<CCrossConnect>> aptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CCrossConnect>> ptpCCMap = new HashMap<String, List<CCrossConnect>>();

    HashMap<String,CCrossConnect> ccMap = new HashMap<String, CCrossConnect>();
    HashMap<String,List<CSection>> ptpSectionMap = new HashMap<String, List<CSection>>();
    HashMap<String,List<CCTP>> ptp_ctpMap = new HashMap<String, List<CCTP>>();
    List<CSection> cSections = new ArrayList<CSection>();
    HashMap<String,CEquipment> equipmentMap = new HashMap<String, CEquipment>();

    private static FileLogger logger = new FileLogger("ALU-OTN-Device.log");
    public ALU_OTN_Migrator(Serializable object, String emsdn) {
        this.emsdn = emsdn;
        this.resultObject = object;
        MigrateThread.thread().initLog(logger);
    }
    public ALU_OTN_Migrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog(emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }

    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "阿朗");


//        List<CTP> query = sd.query("select c from CTP c where c.dn like '%EMS:ZJ-ALU-1-OTN@ManagedElement:100/1@FTP:ODU4-1-1-71-1@%' order by c.dn");
//        StringBuffer sb = new StringBuffer();
//        for (CTP ctp : query) {
//            sb.append(ctp.getDn()+"\n");
//        }
//        System.out.println(sb.toString());


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

        migrateOMS();

        executeNativeSql("update c_ptp p set p.speed = '100G' where p.emsName = '"+emsdn+"' and exists (select c.* from c_ctp c where c.portdn = p.dn and c.tmrate = '100G')");


//        logAction("migrateSection", "同步段", 25);
//        migrateSection();
//
//        logAction("migrateCTP", "同步CTP", 30);
//        migrateCTP();
//          migrateSubnetworkConnection();
        sd.release();
    }



    protected Class[] getStatClss() {
        return new Class[] { CCrossConnect.class, CChannel.class, CPath.class, CRoute.class, CPath_Channel.class,
                CPath_CC.class, CRoute_Channel.class, COMS_CC.class,COMS_Section.class,
                CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class, CVirtualBridge.class,
                CMP_CTP.class, CEthTrunk.class, CStaticRoute.class, CEthRoute.class, CEthTrunk_SDHRoute.class,
                CEthRoute_StaticRoute.class, CEthRoute_ETHTrunk.class, CSection.class,CCTP.class,CDevice.class,CPTP.class,CTransmissionSystem.class,CTransmissionSystem_Channel.class};

    }




    HashMap<String,Equipment> equipmentHashMap = null;


    @Override
    protected void insertEquipmentHolders(List<EquipmentHolder> equipmentHolders) throws Exception {
        if (equipmentHashMap == null) {
            equipmentHashMap = new HashMap<String, Equipment>();

            List<Equipment> allequipments = null;

            if (resultObject instanceof NEWrapper) {
                NEWrapper neWrapper = (NEWrapper) resultObject;
                allequipments = neWrapper.getEquipments();
            } else if (sd != null)
                allequipments = sd.queryAll(Equipment.class);
            for (Equipment allequipment : allequipments) {
                equipmentHashMap.put(allequipment.getDn(),allequipment);
            }
        }
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
            if (equipmentHolder.getHolderType().equals("rack")) {
                equipmentHolder.setHolderType("rack");
                racks.add(equipmentHolder);
                // } else if (rack != null && shelf != null && slot == null) {
            } else if (equipmentHolder.getHolderType().equals("shelf")) {
                equipmentHolder.setHolderType("shelf");
                shelfs.add(equipmentHolder);
                // } else if (rack != null && shelf != null && slot != null) {
                // if (subSlot != null) {
                // subslots.add(equipmentHolder);
                // } else {
                // slots.add(equipmentHolder);
                // }
            } else if (equipmentHolder.getHolderType().equals("slot")) {
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

            Equipment shelfEquipment = equipmentHashMap.get(cEquipmentHolder.getDn() + "@Equipment:1");
            if (shelfEquipment != null) {
                ((CShelf)cEquipmentHolder).setShelfType(shelfEquipment.getExpectedEquipmentObjectType());
            }

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

    List<CSlot> slotList = new ArrayList<CSlot>();
    public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
        if (equipmentHolder.getDn().contains("EquipmentHolder:/shelf"))
            equipmentHolder.setDn(equipmentHolder.getDn().replace("EquipmentHolder:/shelf","EquipmentHolder:/rack=1/shelf"));

        if (equipmentHolder.getAdditionalInfo() != null && equipmentHolder.getAdditionalInfo().length() > 200)
            equipmentHolder.setAdditionalInfo(null);
        CdcpObject cdcpObject = super.transEquipmentHolder(equipmentHolder);
        if (cdcpObject instanceof CSlot) {
            if (((CSlot) cdcpObject).getAcceptableEquipmentTypeList().length() > 2000)
                ((CSlot) cdcpObject).setAcceptableEquipmentTypeList(null);
            slotList.add((CSlot)cdcpObject);
        }

        (cdcpObject).setTag1(getLocation(equipmentHolder.getAdditionalInfo()));
        return cdcpObject;
    }


    public CEquipment transEquipment(Equipment equipment) {
        if (equipment.getDn().contains("EquipmentHolder:/shelf"))
            equipment.setDn(equipment.getDn().replace("EquipmentHolder:/shelf","EquipmentHolder:/rack=1/shelf"));

        CEquipment cEquipment = super.transEquipment(equipment);
        if (!cEquipment.getDn().contains("slot")) return null;

        cEquipment.setNativeEMSName(equipment.getExpectedEquipmentObjectType());
        String additionalInfo = equipment.getAdditionalInfo();
        if (additionalInfo.length() > 1500)
            cEquipment.setAdditionalInfo("");
        equipmentMap.put(cEquipment.getDn(),cEquipment);

        equipment.setTag1(getLocation(equipment.getAdditionalInfo()));

        boolean find = false;
        for (CSlot cSlot : slotList) {
            if (cSlot.getTag1().equals(equipment.getTag1()) && equipment.getDn().startsWith(cSlot.getParentDn() + "@")) {
                cEquipment.setSlotDn(cSlot.getDn());
                find = true;
            }
        }
        if (!find) {
            getLogger().error("Faild to find slot : card="+cEquipment.getDn());
        }

        return cEquipment;
    }

    //private HashMap<String,String> shortedCTPDnMap = new HashMap<String, String>();
 //   private HashMap<String,List<String>> ctpParentChildMap_4short = new HashMap<String, List<String>>();
    @Override
    public CCTP transCTP(CTP ctp) {
        CCTP cctp = super.transCTP(ctp);
        cctp.setTmRate(ALUDicUtil.getTMRate(ctp.getRate()));
        if (ctp.getParentDn().contains("@FTP")) {
            cctp.setDn(cctp.getDn().replace("@PTP","@FTP"));
        }
        if (cctp.getNativeEMSName() == null || cctp.getNativeEMSName().isEmpty()) {
            cctp.setNativeEMSName(ctp.getDn().substring(ctp.getDn().indexOf("CTP:/")+5));
        }
        String additionalInfo = cctp.getAdditionalInfo();
        Map<String, String> map = MigrateUtil.transMapValue(additionalInfo);
        //frequency:9180||
        cctp.setFrequencies(map.get("frequency"));
//        if (transmissionParams.length() > 2000)
//            cctp.setTransmissionParams(transmissionParams.substring(0, 2000));


//        String dn = cctp.getDn();
//
//        if (dn.contains("layerrate=")) {
//            int i1 = dn.indexOf("layerrate=");
//            int i2 = dn.indexOf("/",i1);
//            if (i2 > i1) {
//                String shortDn = dn.substring(0,i1) +dn.substring(i2+1);
//                shortedCTPDnMap.put(shortDn,dn);
//
//                int i = shortDn.indexOf("/", shortDn.indexOf("CTP:/") + 6);
//                if (i > -1) {
//                    String parentDn = shortDn.substring(0,shortDn.lastIndexOf("/"));
//                    cctp.setParentCtpdn(parentDn);
//                    //      DSUtil.putIntoValueList(ctpParentChildMap_4short, parentDn, cctp);
//                }
//            }
//
//
//        }
        cctp.setPortdn(cctp.getParentDn());
        DatabaseUtil.getSID(CPTP.class,cctp.getParentDn());
        return cctp;
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


                    List<CCrossConnect> splitCCS = U2000MigratorUtil.transCCS(cc, emsdn);
                    newCCs.addAll(splitCCS);

                    for (CCrossConnect ncc : splitCCS) {
                        DSUtil.putIntoValueList(aptpCCMap,ncc.getAptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getZptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getAptp(),ncc);
                        ccMap.put(ncc.getDn(),ncc);
                        if (ncc.getAdditionalInfo().length() > 200)
                            ncc.setAdditionalInfo(null);
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
            for (CTP ctp : ctps) {
                CCTP cctp = transCTP(ctp);
                if (cctp != null) {
                    cctps.add(cctp);
                }
            }
        }

        for (CCTP cctp : cctps) {

            DSUtil.putIntoValueList(ptp_ctpMap, cctp.getParentDn(), cctp);
//            if (cctp.getParentCtpdn() != null && !cctp.getParentCtpdn().isEmpty()) {
//                cctp.setParentCtpdn(shortedCTPDnMap.get(cctp.getParentCtpdn()));
//                DSUtil.putIntoValueList(ctpParentChildMap, cctp.getParentCtpdn(), cctp);
//            }
            ctpMap.put(cctp.getDn(), cctp);

        }

        for (String ptpDn : ptp_ctpMap.keySet()) {
            if (ptpDn.equals("EMS:ZJ-ALU-1-OTN@ManagedElement:100/6@PTP:112SNA1-4-2-L1"))
                System.out.println();
            List<CCTP> cctpList = ptp_ctpMap.get(ptpDn);



            String oduCtpDn = null;
            String odu4CtpDn = null;
            String otu4CtpDn = null;
            String tunnableDn = null;
            CCTP dsr = null;
            for (CCTP cctp : cctpList) {
                String ctpDn = cctp.getDn();
                if (ctpDn.contains("WXYZ"))
                    continue;
                if (cctp.getDn().contains("otu4=")) {
                    otu4CtpDn =  ctpDn;
                }

                if (  cctp.getDn().contains("odu4")
                        //|| cctp.getDn().contains("tunable")
                        ) {
                    odu4CtpDn = ctpDn;
                }
//                if (cctp.getDn().contains("odu")
//                       // &&   !cctp.getDn().contains("odu4")
//                        ) {
//                    oduCtpDn = cctp.getDn();
//                }
                if (cctp.getDn().contains("dsr")) {
                    dsr = cctp;
                }
                if (ctpDn.contains("tunable-number=")) {
                    tunnableDn = ctpDn;
                }
            }


            for (CCTP cctp : cctpList) {
                String ctpDn = cctp.getDn();
                if (cctp.getDn().contains("otu4=")) {
                    if (tunnableDn != null) {
                        DSUtil.putIntoValueList(ctpParentChildMap, tunnableDn, cctp);
                        cctp.setParentCtpdn(tunnableDn);
                    }
                }

                if (  cctp.getDn().contains("odu4")
                        ) {
                    if (otu4CtpDn != null) {
                        DSUtil.putIntoValueList(ctpParentChildMap, otu4CtpDn, cctp);
                        cctp.setParentCtpdn(otu4CtpDn);
                    }
                    else if (tunnableDn != null) {
                        DSUtil.putIntoValueList(ctpParentChildMap, tunnableDn, cctp);
                        cctp.setParentCtpdn(tunnableDn);
                    }
                }






            }


//            if (dsr != null && oduCtpDn != null) {
//                dsr.setParentCtpdn(oduCtpDn);
//                DSUtil.putIntoValueList(ctpParentChildMap,oduCtpDn,dsr);
//            } else if (dsr != null) {
//      //          System.out.println();
//            }
        }


        for (CCTP cctp : cctps) {
            if (cctp.getTmRate() != null && cctp.getTmRate().equals("100G")) {
                String parentCtpdn = cctp.getParentCtpdn();
                CCTP parentCtp = ctpMap.get(parentCtpdn);
                if (parentCtp != null)
                    parentCtp.setTmRate("100G");
            }

        }

        di.insert(cctps);



        di.end();


//        Set<String> ptps = ptp_ctpMap.keySet();
//        for (String ptp : ptps) {
//            List<CCTP> cctpList = ptp_ctpMap.get(ptp);
//            CCTP oms = null;
//            CCTP och = null;
//            if (cctpList != null) {
//                for (CCTP cctp : cctpList) {
//                    if (cctp.getDn().contains("oms=")) {
//                        oms = cctp;
//                        break;
//                    }
//
//                    if (cctp.getDn().contains("CTP:/frequency=tunable-number")) {
//                        och = cctp;
//                        break;
//                    }
//                }
//
//                for (CCTP cctp : cctpList) {
//                    if (oms != null) {
//                        if (cctp.getDn().contains("CTP:/frequency="))
//                            DSUtil.putIntoValueList(ctpParentChildMap,oms.getDn(),cctp);
//                    }
//                    if (och != null) {
//                        if (!cctp.getDn().contains("CTP:/frequency"))
//                            DSUtil.putIntoValueList(ctpParentChildMap,och.getDn(),cctp);
//                    }
//                }
//            }
//        }
        return cctps;
    }
    private String getLocation(String additionalInfo) {
        String location = additionalInfo.split(Constant.listSplitReg)[0];
        location = location.substring(location.indexOf(":")+1);
        return location;

    }
    private String getLastTwo(String location) {
        String[] split = location.split("-");
        if (split.length >= 2) {
            return split[split.length-2]+"-"+split[split.length-1];
        }
        return null;
    }
    private String getLastThree(String location) {
        String[] split = location.split("-");
        if (split.length >= 3) {
            return split[split.length-3]+"-"+split[split.length-2]+"-"+split[split.length-1];
        } else if (split.length == 2) {
            return split[split.length-2]+"-"+split[split.length-1]+"-";
        }

        return null;
    }
    @Override
    public CPTP transPTP(PTP ptp) {
//        if (ptp.getDn().equals("EMS:ZJ-ALU-1-OTN@ManagedElement:100/3@PTP:GBE10-1-1-1-2"))
//            System.out.println();

        CPTP cptp = super.transPTP(ptp);
        cptp.setDeviceDn(ptp.getParentDn());
        String dn = cptp.getDn();



        cptp.setNo(ptp.getDn().substring(ptp.getDn().indexOf("port=")+5));
        int i = dn.indexOf("/", dn.indexOf("slot="));
        String carddn = (dn.substring(0,i)+"@Equipment:1").replaceAll("PTP:","EquipmentHolder:")
                .replaceAll("FTP:","EquipmentHolder:");


        Collection<CEquipment> equipments = equipmentMap.values();
        boolean b = false;
        for (CEquipment equipment : equipments) {
            if (equipment.getDn().startsWith(ptp.getParentDn()+"@")) {
                if (!equipment.getDn().contains("slot="))
                    continue;
//                if (ptp.getNativeEMSName().contains(equipment.getNativeEMSName() + "-")) {
//                    ptp.setParentDn(equipment.getDn());
//                    b = true;
//                }

                String location = getLocation(equipment.getAdditionalInfo());
//                if (("1-"+ptp.getTag1()).startsWith(location+"-")) {
                if (getLastThree(ptp.getTag1()).startsWith(getLastTwo(location)+"-")) {
                    cptp.setParentDn(equipment.getDn());
                    b = true;
                    break;
                }
            }
        }
        if (!b) {
            getLogger().error("Faild find carddn : ptp = "+ptp.getDn());
        }
        cptp.setNo("1-"+ptp.getTag1());
        if (cptp.getNo().contains("-"))
            cptp.setNo(cptp.getNo().substring(cptp.getNo().lastIndexOf("-")+1));


        if (cptp.getNo().contains("_"))
            cptp.setNo(cptp.getNo().substring(0,cptp.getNo().indexOf("_")));

        if (cptp.getDn().contains("-"))
            cptp.setNo(cptp.getDn().substring(cptp.getDn().lastIndexOf("-")+1));
    //    cptp.setParentDn(carddn);
        cptp.setCardid(DatabaseUtil.getSID(CEquipment.class, cptp.getParentDn()));
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



    private List<SubnetworkConnection> getChildSncs (String parentSncDn) {
        List<SubnetworkConnection> childs = new ArrayList<SubnetworkConnection>();
        Set<String> chidls = snc_chid_parent_dns.keySet();
        for (String chidl : chidls) {
            List<String> parents = snc_chid_parent_dns.get(chidl);
            if (parents.contains(parentSncDn)) {
                SubnetworkConnection cs = sncMap.get(chidl);
                childs.add(cs);

            }

        }
        return childs;
    }

    public void migrateOMS() throws Exception {

        executeDelete("delete  from COMS_CC c where c.emsName = '" + emsdn + "'", COMS_CC.class);
        executeDelete("delete  from COMS_Section c where c.emsName = '" + emsdn + "'", COMS_Section.class);

   //     executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "'", CSection.class);

        executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
        executeDelete("delete  from CRoute_CC c where c.emsName = '" + emsdn + "'", CRoute_CC.class);
        executeDelete("delete  from CPath c where c.emsName = '" + emsdn + "'", CPath.class);
        executeDelete("delete  from CChannel c where c.emsName = '" + emsdn + "'", CChannel.class);
        executeDelete("delete  from CRoute_Channel c where c.emsName = '" + emsdn + "'", CRoute_Channel.class);
        executeDelete("delete  from CPath_CC c where c.emsName = '" + emsdn + "'", CPath_CC.class);
        executeDelete("delete  from CPath_Channel c where c.emsName = '" + emsdn + "'", CPath_Channel.class);
        executeDelete("delete  from CPath_Section c where c.emsName = '" + emsdn + "'", CPath_Section.class);
        executeDelete("delete  from CRoute_Section c where c.emsName = '" + emsdn + "'", CRoute_Section.class);



        List<CSection> sectionsToMakeup = new ArrayList<CSection>();

        List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
        HashMap<String, List<R_TrafficTrunk_CC_Section>> routeMap = queryTrafficTrunkCCSectionMap();

        List<COMS_CC> omsCClist = new ArrayList<COMS_CC>();
        List<COMS_Section> omsSectionList = new ArrayList<COMS_Section>();
        HashMap<String,List<String>> waveChannelRoutes = new HashMap<String, List<String>>();
        HashMap<String,List<String>> omsRoutes = new HashMap<String, List<String>>();
        for (SubnetworkConnection snc : sncs) {
            if (snc.getRate().equals("41")) {
                CSection oms = transOMS(snc);
                omsList.add(oms);
                List<R_TrafficTrunk_CC_Section> r_trafficTrunk_cc_sections = routeMap.get(snc.getDn());
                for (R_TrafficTrunk_CC_Section route : r_trafficTrunk_cc_sections) {
                    if (route.getType().equals("CC")) {
                        COMS_CC coms_cc = new COMS_CC();
                        coms_cc.setCcdn(route.getCcOrSectionDn());
                        coms_cc.setOmsdn(oms.getDn());
                        coms_cc.setEmsName(emsdn);
                        coms_cc.setDn(SysUtil.nextDN());
                        omsCClist.add(coms_cc);
                    }
                    if (route.getType().equals("SECTION")) {
                        COMS_Section coms_cc = new COMS_Section();
                        coms_cc.setSectiondn(route.getCcOrSectionDn());
                        coms_cc.setOmsdn(oms.getDn());
                        coms_cc.setEmsName(emsdn);
                        coms_cc.setDn(SysUtil.nextDN());
                        omsSectionList.add(coms_cc);
                    }

                    DSUtil.putIntoValueList(omsRoutes,oms.getDn(),route.getCcOrSectionDn());

                }


            }



        }

        DataInserter di_oms = new DataInserter(emsid);
        di_oms.insert(omsList);
        di_oms.end();





        List<CChannel> waveChannelList = null;
        try {










            getLogger().info("OMS size = "+omsList.size());





            ///////////////////////////////波道channel///////////////////////////////////////
            waveChannelList = new ArrayList<CChannel>();
            for (CSection omsSection : omsList) {
                String aendTp = omsSection.getAendTp();
                String zendTp = omsSection.getZendTp();
                List<CCTP> acctps = ptp_ctpMap.get(aendTp);
                List<CCTP> zcctps = ptp_ctpMap.get(zendTp);
                if (acctps == null || acctps.isEmpty()) {
                    getLogger().error("无法找到CTP，端口："+aendTp);
                }  else if (zcctps == null || zcctps.isEmpty()) {
                    getLogger().error("无法找到CTP，端口："+zendTp);
                }
                else {
                    for (CCTP acctp : acctps) {
                        if (acctp.getDn().contains("CTP:/frequency=")) {
                            for (CCTP zcctp : zcctps) {

                                if (zcctp.getDn().contains("CTP:/frequency=")) {
                                    String och = acctp.getDn().substring(acctp.getDn().indexOf("frequency="));
                                    String och2 = zcctp.getDn().substring(zcctp.getDn().indexOf("frequency="));


                                    if (och != null && och.equals(och2)) {

                                        CChannel waveChannel = createCChanell(omsSection, acctp, zcctp);
                                        waveChannelList.add(waveChannel);
                                          waveChannelRoutes.put(waveChannel.getDn(),omsRoutes.get(omsSection.getDn()));

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            removeDuplicateDN(waveChannelList);

            DataInserter di = new DataInserter(emsid);
            di.insert(waveChannelList);
            di.end();
        } catch (Exception e) {
            getLogger().error(e, e);
        }



        //////////////////////////////////////////////////////////////////////
        HashMap<String,List<String>> subwave_routes = new HashMap<String, List<String>>();

        List<CChannel> subWaveChannelList = new ArrayList<CChannel>();
        HashMap<String,List<CChannel>> och_subwaves = new HashMap<String, List<CChannel>>();
        List<CPath> cPaths = new ArrayList<CPath>();
        List<CRoute> cRoutes = new ArrayList<CRoute>();
        List<CRoute_CC> cRoute_ccs = new ArrayList<CRoute_CC>();
        List<CRoute_Channel> cRoute_channels = new ArrayList<CRoute_Channel>();
        List<CRoute_Section> cRoute_sections = new ArrayList<CRoute_Section>();

        List<CPath_CC> cPath_ccs = new ArrayList<CPath_CC>();
        List<CPath_Channel> cPath_channels = new ArrayList<CPath_Channel>();
        List<CPath_Section> cPath_sections = new ArrayList<CPath_Section>();


        List<SubnetworkConnection> ochList = new ArrayList<SubnetworkConnection>();
        HashSet<String> ochDns = new HashSet<String>();
        List<SubnetworkConnection> dsrList = new ArrayList<SubnetworkConnection>();

        for (SubnetworkConnection snc : sncs) {
            if (snc.getRate() != null && snc.getRate().equals(DicConst.LR_Optical_Multiplex_Section+"")  )
                continue;
            if (snc.getDn().endsWith("_597"))
                System.out.println();
            sncMap.put(snc.getDn(),snc);
            /////////////// 处理SNC的父子关系
            HashMap<String, String> stringStringHashMap = MigrateUtil.transMapValue(snc.getAdditionalInfo());
            String serverIDs = stringStringHashMap.get("serverIDs");
            if (serverIDs == null || serverIDs.trim().isEmpty()) {

                serverIDs = "";


                //  如果是serverid没有的pool，则遍历OTU，看路由中某条cc的端口和某个OTU重合，即该OTU为父亲
                if (snc.getRate().equals("334")) {
                    List<R_TrafficTrunk_CC_Section> routes = routeMap.get(snc.getDn());
                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {
                            String aend = route.getaEnd();
                            String aptp = DNUtil.extractPortDn(aend);
                            for (SubnetworkConnection _snc : sncs) {
                                if (_snc.getRate().equals("339")) {
                                    if (_snc.getaPtp().equals(aptp) || _snc.getzPtp().equals(aptp)) {
                                        serverIDs = _snc.getDn().substring(_snc.getDn().lastIndexOf(":")+1);
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }
            }

            if (serverIDs.trim().isEmpty()) {
                getLogger().error("没有serverid，snc=" + snc.getDn());
                continue;
            }

            //SNC_1036  //EMS:ZJ-ALU-1-OTN@MultiLayerSubnetwork:OTN@SubnetworkConnection:SNC_1037
            String[] split = serverIDs.split(",");
            List<String> ps = new ArrayList<String>();
            for (String s : split) {
                ps.add(snc.getDn().substring(0,snc.getDn().lastIndexOf(":"))+":"+s);
            }

            snc_chid_parent_dns.put(snc.getDn(), ps);


        }


        HashSet<String> odu4DnsDirect2Dsr = new HashSet<String>();
        for (SubnetworkConnection snc : sncs) {
            if (snc.getDn().endsWith("_597")
                    //|| snc.getDn().endsWith("_118")
                    )
                System.out.println();

            String rate = snc.getRate();

            if (rate != null) {
                if (rate.equals(DicConst.LR_Optical_Multiplex_Section+"")  )
                    continue;
                if (rate.equals("339")) { //OTU4

                    //找出子SNC，默认都是ODU4的，看它的
                    List<SubnetworkConnection> childSncs = getChildSncs(snc.getDn());
                    if (childSncs == null || childSncs.isEmpty()) {
                        getLogger().error("OTU4无子SNC,parent="+snc.getDn());
                    }
                    else if (childSncs.size() > 1) {
                        getLogger().error("OTU4有超过2个SNC,parent="+snc.getDn()+" size = "+childSncs.size());
                    } else {
                        SubnetworkConnection odu4Snc = childSncs.get(0);
                        if (!odu4Snc.getRate().equals("334"))
                            getLogger().error("OTU4的子snc，不是ODU4的: childsnc = " + odu4Snc.getDn());
                        else {

                            List<SubnetworkConnection> css = getChildSncs(odu4Snc.getDn());
                            if (css != null && css.size() == 1 && css.get(0).getRate().equals("50")) {
                                odu4DnsDirect2Dsr.add(odu4Snc.getDn());
                                //只有一条子snc，且为dsr，则OTU4作为OCH
                                ochList.add(snc);
                                ochDns.add(snc.getDn());
                            }

                        }
                    }


                }
                if (rate.equals("334")) {       //ODU4
                    if (!odu4DnsDirect2Dsr.contains(snc.getDn())) {
                        ochList.add(snc);
                        ochDns.add(snc.getDn());
                    }
                }
//                else if (rate.equals("334")) {
//                    ochList.add(snc);
//                }

                else  {
                    if (rate.equals("50"))
                        dsrList.add(snc);



                }




            }



        }
        DataInserter di2 = new DataInserter(emsid);
        for (SubnetworkConnection snc : ochList) {
            if (snc.getDn().equals("EMS:ZJ-ALU-1-OTN@MultiLayerSubnetwork:OTN@SubnetworkConnection:SNC_618"))
                System.out.println("snc = " + snc);
            CPath cPath = U2000MigratorUtil.transPath(emsdn, snc);
            cPath.setTmRate("40G");
            cPath.setRateDesc("OCH");
            cPath.setCategory("OCH");
            List<CChannel> subwaves = new ArrayList<CChannel>();
            if (snc.getaEnd().contains("CTP")) {
                CCTP actp = ctpMap.get(snc.getaEnd());
                cPath.setFrequencies(actp == null ? null : actp.getFrequencies());
                cPaths.add(cPath);
                //CChannel subwave = createCChanell(cPath, ctpMap.get(snc.getaEnd()), ctpMap.get(snc.getzEnd()));
                CCTP asideCtp = ctpMap.get(snc.getaEnd());
                CCTP zsideCtp = ctpMap.get(snc.getzEnd());
                if (asideCtp == null) {
                    getLogger().error("无法找到CTP：snc=" + snc.getDn() + "  aend=" + snc.getaEnd());
                    continue;
                }
                if (zsideCtp == null) {
                    getLogger().error("无法找到CTP：snc=" + snc.getDn() + " aend=" + snc.getaEnd());
                    continue;
                }
                 subwaves = createSubwaveChannels(cPath, asideCtp, zsideCtp);
            }  else if (snc.getaEnd().contains("FTP")) {
                cPaths.add(cPath);
                List<CCTP> actps = ptp_ctpMap.get(snc.getaEnd());
                List<CCTP> zctps = ptp_ctpMap.get(snc.getzEnd());
                if (actps != null && zctps != null)
                    subwaves = createSubwaveChannels(cPath, actps, zctps);
                else {
                    getLogger().error("无法找到子CTP snc = "+snc.getDn());
                }
            }

            subWaveChannelList.addAll(subwaves);
            och_subwaves.put(snc.getDn(),subwaves);

            List<R_TrafficTrunk_CC_Section> routes = routeMap.get(snc.getDn());
            if (routes == null || routes.isEmpty()) {
                getLogger().error("OCH路由为空：snc"+snc.getDn());
                continue;
            }

            if (snc.getRate().equals("334")) { //odu4 则要把父亲OTU4的路由也加进来
                List<String> parents = snc_chid_parent_dns.get(snc.getDn());
                if (parents != null && parents.size() > 0) {
                    String otu4Dn = parents.get(0);
                    List<R_TrafficTrunk_CC_Section> parentRoutes = routeMap.get(otu4Dn);
                    if (parentRoutes != null)
                        routes.addAll(parentRoutes);
                }
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
                    CChannel waveChannel = findCChanell(waveChannelList, route.getaEnd());
                    if (waveChannel != null ) {
                        if ( !sncChannels.contains(waveChannel))
                            sncChannels.add(waveChannel);
                        sncCCDns.add(route.getCcOrSectionDn());
                        //      ptps.add(route.getzPtp());
                    }

                    CChannel waveChannel2 = findCChanell(waveChannelList, route.getzEnd());
                    if (waveChannel2 != null  ) {
                        if ( !sncChannels.contains(waveChannel2))
                            sncChannels.add(waveChannel2);
                        sncCCDns.add(route.getCcOrSectionDn());
                        //    ptps.add(route.getaPtp());
                    }
                }

            }

            List<String> allchannel_routes = new ArrayList<String>();
            for (CChannel sncChannel : sncChannels) {
                List<String> waveRoutes = waveChannelRoutes.get(sncChannel.getDn());
                allchannel_routes.addAll(waveRoutes);
            }

            for (R_TrafficTrunk_CC_Section route : routes) {



                if (!allchannel_routes.contains(route.getCcOrSectionDn())) {
                    if (route.getType().equals("SECTION"))
                        sncSectionDns.add(route.getCcOrSectionDn());
                    else
                        sncCCDns.add(route.getCcOrSectionDn());
                }

            }



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
            if (snc.getDn().equals("EMS:ZJ-ALU-1-OTN@MultiLayerSubnetwork:OTN@SubnetworkConnection:SNC_1003"))
                System.out.println();
            CRoute cRoute = U2000MigratorUtil.transRoute(emsdn, snc);
            cRoute.setCategory("DSR");
            cRoutes.add(cRoute);
            List<CChannel> sncChannels = new ArrayList<CChannel>();
            HashSet<String> sncCCDns = new HashSet<String>();
            HashSet<String> sncSectionDns = new HashSet<String>();

            List<R_TrafficTrunk_CC_Section> routes = routeMap.get(snc.getDn());
            if (routes == null) {
                getLogger().error("Failed to find routes : snc="+snc.getDn());
                continue;
            }

           String tempDn = snc.getDn();
            String parentOchDn = null;
            //找到所有父辈的路由，找到其中自己的路由不包含的，加入到自己的路由中，（找到父辈是OCH的除外）
            while (true) {
                List<String> parentDns = snc_chid_parent_dns.get(tempDn);
                if (parentDns == null)  {
                    getLogger().error("Faild to find parent : "+tempDn);
                    break;
                }
                String parentDn = parentDns.get(0);
                if  (ochDns.contains(parentDn)) {
                    parentOchDn = parentDn; break;
                    //如果是OCH，结束
                }

                List<R_TrafficTrunk_CC_Section> parentRoutes = routeMap.get(parentDn);
                if (parentRoutes != null) {
                    for (R_TrafficTrunk_CC_Section parentRoute : parentRoutes) {
                        if (!containsRoute(routes, parentRoute.getCcOrSectionDn()))
                            routes.add(parentRoute);
                    }
                }
                tempDn = parentDn;
            }


            //在所有路由中，删除OCH的路由。
            if (parentOchDn != null) {
                List<R_TrafficTrunk_CC_Section> ochRoutes = routeMap.get(parentOchDn);
                if (ochRoutes != null) {
                    minusRoutes(routes,ochRoutes);
                }


                List<CChannel> ochChannels = och_subwaves.get(parentOchDn);

                if (ochChannels == null) {
                    getLogger().error("Faild to find och_subwaves route :"+parentOchDn);
                }

                if ( ochChannels != null && ochChannels.size() == 1) {
                    sncChannels.add(ochChannels.get(0));
                    cRoute.setTmRate(ochChannels.get(0).getTmRate());
                }
                else {
                    for (CChannel ochChannel : ochChannels) {
                        for (R_TrafficTrunk_CC_Section route : routes) {
                            if (route.getType().equals("CC")) {
                                if (route.getaEnd().equals(ochChannel.getAend()) || route.getaEnd().equals(ochChannel.getZend())
                                        || route.getzEnd().equals(ochChannel.getAend()) || route.getzEnd().equals(ochChannel.getZend())
                                        ) {
                                    sncChannels.add(ochChannel);
                                    cRoute.setTmRate(ochChannel.getTmRate());
                                    break;
                                }
                            }
                        }
                    }
                }


                HashSet<String> ftps = new HashSet<String>();
                boolean a = false;
                boolean z = false;
                for (R_TrafficTrunk_CC_Section route : routes) {
                    if (route.getType().equals("CC")) {
                        sncCCDns.add(route.getCcOrSectionDn());
                        if (!route.getaEnd().contains("@CTP"))
                            ftps.add(route.getaEnd());
                        else if (!route.getzEnd().contains("@CTP"))
                            ftps.add(route.getzEnd());


                    }
                    else
                        sncSectionDns.add(route.getCcOrSectionDn());


                    if (route.getaEnd().contains(snc.getaEnd()) || route.getzEnd().contains(snc.getaEnd()))
                          a = true;
                    if (route.getaEnd().contains(snc.getzEnd()) || route.getzEnd().contains(snc.getzEnd()))
                         z = true;

                }

                String aend = snc.getaEnd();
                String zend = snc.getzEnd();
                if (!a) {
                    for (String ftp : ftps) {
                        if (DNUtil.extractNEDn(aend).equals(DNUtil.extractNEDn(ftp))) {
                            CSection section = createSection( aend,ftp);
                            sectionsToMakeup.add(section);
                            sncSectionDns.add(section.getDn());
                        }

                    }
                }
                if (!z) {
                    for (String ftp : ftps) {
                        if (DNUtil.extractNEDn(zend).equals(DNUtil.extractNEDn(ftp))) {
                            CSection section = createSection(ftp, zend);
                            sectionsToMakeup.add(section);
                            sncSectionDns.add(section.getDn());
                        }

                    }
                }

                for (String ccDn : sncCCDns) {
                    cRoute_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, ccDn, cRoute));
                }
                for (String sectionDn : sncSectionDns) {
                    cRoute_sections.add(U2000MigratorUtil.createCRoute_Section(emsdn, sectionDn, cRoute));
                }

                for (CChannel subwaveChannel : sncChannels) {
                    cRoute_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, subwaveChannel, cRoute));
                }


                if (sncChannels == null || sncChannels.size() == 0)
                    getLogger().error("无法找到 route-channel,snc="+snc.getDn());
            }



        }




        DataInserter di = new DataInserter(emsid);
        try {
            di.insertWithDupCheck(sectionsToMakeup);
            di.insert(omsCClist);
            di.insert(omsSectionList);
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

    public    CSection createSection(String aendTp,String zendTp) {
        CSection section = new CSection();
        section.setAendTp(aendTp);
        section.setZendTp(zendTp);
        section.setDirection(1);
        section.setDn(aendTp + "_" + zendTp);
        section.setTag1("MAKEUP");
        section.setRate("47");
        section.setEmsName(emsdn);
        CPTP cptp = ptpMap.get(zendTp);
        if (cptp != null)
            section.setSpeed(cptp.getSpeed());
        else
            section.setSpeed("40G");
        section.setType("OTS");
        return section;

    }


    private boolean containsRoute(List<R_TrafficTrunk_CC_Section> routes,String ccOrSectionDn) {
        if (routes != null ) {
            for (R_TrafficTrunk_CC_Section route : routes) {
                if (route.getCcOrSectionDn().equals(ccOrSectionDn))
                    return true;
            }
        }
        return false;
    }

    private void minusRoutes(List<R_TrafficTrunk_CC_Section> allRoutes,List<R_TrafficTrunk_CC_Section> subRoutes) {
        List<R_TrafficTrunk_CC_Section> toBeRemoved = new ArrayList<R_TrafficTrunk_CC_Section>();
        for (R_TrafficTrunk_CC_Section allRoute : allRoutes) {
            if (containsRoute(subRoutes,allRoute.getCcOrSectionDn())) {
                toBeRemoved.add(allRoute);
            }
        }

        allRoutes.removeAll(toBeRemoved);
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

    private void removeDsrCtps(List<CCTP> ctps) {
        List dsrs = new ArrayList();
        for (CCTP ctp : ctps) {
             if (ctp.getDn().contains("dsr"))
                 dsrs.add(ctp);
        }
        ctps.removeAll(dsrs);
    }

    private List<CChannel> createSubwaveChannels(CPath path,CCTP pathAside,CCTP pathZside) {
//        List<CCTP> actps = ctpParentChildMap.get(pathAside.getDn());
//        List<CCTP> zctps = ctpParentChildMap.get(pathZside.getDn());
        List<CCTP> actps = findAllChildCTPS(pathAside.getDn());
        List<CCTP> zctps = findAllChildCTPS(pathZside.getDn());
        return createSubwaveChannels(path,actps,zctps);
    }

    private List<CChannel> createSubwaveChannels(CPath path,List<CCTP> actps ,List<CCTP> zctps ) {
        removeDsrCtps(actps);
        removeDsrCtps(zctps);

        List<CChannel> subwaveChannels = new ArrayList<CChannel>();

        if (actps != null && actps.size() > 0 && zctps != null && zctps.size() > 0) {
            if (actps.size() >1 && zctps.size() > 1)
                System.out.print("");
            for (CCTP actp : actps) {
                boolean match = false;
                for (CCTP zctp : zctps) {
                    String aname = actp.getDn().substring(actp.getDn().indexOf("CTP:/"));
                    String zname = zctp.getDn().substring(zctp.getDn().indexOf("CTP:/"));
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
        cChannel.setTmRate(acctp.getTmRate());
        cChannel.setRateDesc(acctp.getRateDesc());

        if (parent instanceof CPath){
            if (cChannel.getTmRate().equals("100G")) {
                ((CPath) parent).setTmRate("100G");
            }
        }

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
        section.setSpeed("100G");
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

    List<CSection> omsList = new ArrayList<CSection>();

    @Override
    public CSection transSection(Section section) {
        CSection cSection = super.transSection(section);
        cSection.setType("OTS");
        cSection.setSpeed("100G");
        cSection.setAdditionalInfo("");
        DSUtil.putIntoValueList(ptpSectionMap,cSection.getAendTp(),cSection);
        cSections.add(cSection);


        return cSection;
    }


    public CSection transOMS(SubnetworkConnection section) {
        CSection csection = new CSection();
        csection.setDn(section.getDn());
        csection.setSid(DatabaseUtil.nextSID(csection));
        csection.setCollectTimepoint(section.getCreateDate());
        csection.setRate(section.getRate());
        String rate = section.getRate();

        csection.setDirection(DicUtil.getConnectionDirection(section.getDirection()));
        csection.setAendTp(section.getaPtp());

        csection.setZendTp(section.getzPtp());
        csection.setParentDn(section.getParentDn());
        csection.setEmsName(section.getEmsName());
        csection.setUserLabel(section.getUserLabel());
        csection.setNativeEMSName(section.getNativeEMSName());
        csection.setOwner(section.getOwner());
        csection.setAdditionalInfo(section.getAdditionalInfo());


        csection.setType("OMS");
        csection.setSpeed("100G");
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



    public static void main(String[] args) throws Exception {
        FileReader fr = new FileReader("c:\\1.txt");
        BufferedReader br = new BufferedReader(fr);
        String s = br.readLine();
        String[] split = s.split(" ");
        for (String s1 : split) {
            System.out.println(s1);
        }

        String fileName=  "D:\\cdcpdb\\2015-08-12-111418-ZJ-ALU-1-OTN-DayMigration.db";
        String emsdn = "ZJ-ALU-1-OTN";
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

        ALU_OTN_Migrator loader = new ALU_OTN_Migrator(fileName, emsdn){
            public void afterExecute() {
                updateEmsStatus(Constants.CEMS_STATUS_READY);
                printTableStat();
            //    IrmsClientUtil.callIRMEmsMigrationFinished(emsdn);
            }
        };
        loader.execute();
    }

}
