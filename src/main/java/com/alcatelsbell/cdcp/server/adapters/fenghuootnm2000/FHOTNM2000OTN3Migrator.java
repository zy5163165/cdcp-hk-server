package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.adapters.*;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CTP;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.DBUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.util.ObjectUtil;
import com.jcraft.jsch.Logger;

import org.asb.mule.probe.framework.entity.*;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.FileLogger;
import org.asb.mule.probe.ptn.u2000V16.nbi.job.CTPUtil;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.persistence.EntityManager;

import static com.alcatelsbell.cdcp.server.adapters.CacheClass.*;
import static com.alcatelsbell.cdcp.util.MemTable.*;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author wm
 *
 */
public class FHOTNM2000OTN3Migrator  extends AbstractDBFLoader {

    HashMap<String,CSection> ptpSectionMap = new HashMap<String, CSection>();
    //	HashSet<String> CCvc3CtpSet = new HashSet<String>();
    HashSet<String> allCtpInCCSet = new HashSet<String>();
    //	HashSet<String> slotDnSet = new HashSet<String>();
    private final int ENTITY_TYPE_CC = 1;
    private final int ENTITY_TYPE_CHANNEL = 2;


    public FHOTNM2000OTN3Migrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog("FHSDH_"+emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }

    private static FileLogger fLogger = new FileLogger("FH-OTNM2000-SDH-Device.log");

    public FHOTNM2000OTN3Migrator(Serializable object, String emsdn) {
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

    private HashMap<String, CPTP> ptpMap = new HashMap<String, CPTP>();
    private HashMap<String,CChannel> highOrderCtpChannelMap = new HashMap<String, CChannel>();
    private HashMap<String,CChannel> lowOrderCtpChannelMap = new HashMap<String, CChannel>();
    //   private HashMap<String,List<CTP>> ptpCtpMap = new HashMap<String, List<CTP>>();
    private List<CChannel> cChannelList =  new ArrayList<CChannel>();
    private List<CPath> cPathList = new ArrayList<CPath>();
    private List<CPath_Channel> pathChannelList = new ArrayList<CPath_Channel>();
    private List<CPath_CC> pathCCList = new ArrayList<CPath_CC>();
    private List<CRoute> cRouteList = new ArrayList<CRoute>();
    private List<CRoute_Channel> routeChannelList = new ArrayList<CRoute_Channel>();
    private List<CRoute_CC> routeCCList = new ArrayList<CRoute_CC>();
    //  private List<CSlot> missSlotList = new ArrayList<CSlot>();

    //	Map<String, CPath> channel_pathMap = new HashMap<String, CPath>();
//	Map<String, CPath> cc_pathMap = new HashMap<String, CPath>();
    HashSet<String> pathCtpSet = new HashSet<String>();
    HashSet<String> routeCtpSet = new HashSet<String>();
//	Map<String, CRoute> channel_routeMap = new HashMap<String, CRoute>();
//	Map<String, CRoute> cc_routeMap = new HashMap<String, CRoute>();

    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "烽火");
        migrateLogical = getAttribute("logical") == null ? true : "true".equalsIgnoreCase(getAttribute("logical").toString()) ;

        if (migrateLogical) {
            try {
                long count = sd.findObjectsCount("select count(c.id) from CTP c");
                if (count == 0) {
                    migrateLogical = false;
                    getLogger().info("CTP IS EMPTY ,LOGCAL = FALSE");
                }
            } catch (Exception e) {
                getLogger().error(e, e);
            }
        }
        getLogger().info("logical = "+migrateLogical);

        logAction(emsdn + " migrateManagedElement", "同步网元", 1);
        migrateManagedElement();


        migrateSubnetwork();

        logAction("migrateEquipmentHolder", "同步槽道", 5);
        migrateEquipmentHolder();

        if (false) {
            sd.release();
            return;
        }

        logAction("migrateEquipment", "同步板卡", 10);
        migrateEquipment();
        //      insertMissSlots();
        logAction("migratePTP", "同步端口", 20);
        migratePTP();


        if (migrateLogical) {
            logAction("migrateCTP", "同步CTP", 25);
            migrateCTP();
            //migrate cc must before ctp, because need to get vc3 ctp in cc
            logAction("migrateCC", "同步交叉", 30);
            migrateCC();
        }



        logAction("migrateSection", "同步段", 30);
        migrateSection();


//
//
//        logAction("migrateProtectionSubnetwork", "同步传输系统", 32);
//        migrateProtectionSubnetwork();

        if (migrateLogical) {
            logAction("migratePathAndRoute", "同步Path和Route", 35);
            migratePathAndRoute();

            logAction("migrateEthBindingPath", "同步MSTP", 55);
            migrateEthBindingPath();
        }

//        logAction("migrateVB", "同步VB", 40);
//         migrateVB();
//
//        logAction("migrateEthBindingPath", "同步MSTP", 70);
//        migrateEthBindingPath();

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

    private HashMap<String,CDevice> cDeviceMap = new HashMap<String, CDevice>();
    @Override
    public CDevice transDevice(ManagedElement me) {

        CDevice device =  super.transDevice(me);

        cDeviceMap.put(device.getDn(),device);
        return device;
    }

    HashMap<String,CRack> rackMap = new HashMap<String, CRack>();
    @Override
    public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
        CdcpObject eh = super.transEquipmentHolder(equipmentHolder);
        if (eh instanceof CShelf) {
        String additionalInfo = equipmentHolder.getAdditionalInfo();
        String shelfType = MigrateUtil.transMapValue(additionalInfo).get("DetailKind");
        if (shelfType != null) {
        ((CShelf) eh).setShelfType(shelfType);
        }
            CRack rack = rackMap.get(((CShelf) eh).getRackDn());
//            if (rack != null)
//                ((CShelf) eh).setNo(rack.getNo() + "-" + ((CShelf) eh).getNo());
        }
        if (eh instanceof CRack) {
            String nativeEMSName = equipmentHolder.getNativeEMSName();
            if (nativeEMSName.contains("架"))
                ((CRack) eh).setNo(nativeEMSName.substring(nativeEMSName.indexOf("架") + 1));
            rackMap.put(eh.getDn(),(CRack)eh);
        }
        if (eh instanceof CSlot) {
            String nativeEMSName = equipmentHolder.getNativeEMSName();
            // SLOT_0X08

            if (nativeEMSName.contains("SLOT_0X")) {
                String slotNo = nativeEMSName.substring(nativeEMSName.indexOf("SLOT_0X") + 7);
//				((CSlot) eh).setNo(Integer.parseInt(slotNo, 16) + "");
                ((CSlot) eh).setNo(slotNo);
            }
//			slotDnSet.add(eh.getDn());
        }
        return eh;
    }

    @Override
    public CEquipment transEquipment(Equipment equipment) {
        CEquipment eq = super.transEquipment(equipment);
        //if slot not exist, generate a slot entity
//		String slotdn = eq.getSlotDn();
//		if (!slotDnSet.contains(slotdn)){
//			CSlot slot = generateSlot(eq);
//			slotDnSet.add(slot.getDn());
//			missSlotList.add(slot);
//		}
        return eq;
    }

//	private void insertMissSlots() throws Exception{
//		getLogger().info("Generate missing slots size: " + missSlotList.size());
//		DataInserter di = new DataInserter(emsid);
//		di.insert(missSlotList);
//		di.end();
//	}

//	private CSlot generateSlot(CEquipment equipment){
//		CSlot cequipmentHolder = new CSlot();
//		String dn = equipment.getSlotDn();
//		if (dn.contains("/slot")) {
//			cequipmentHolder.setShelfDn(dn.substring(0, dn.indexOf("/slot")));
//			cequipmentHolder.setShelfId(DatabaseUtil.getSID(CShelf.class, cequipmentHolder.getShelfDn()));
//
//		}
//		// cequipmentHolder.setNo(slot);
//		cequipmentHolder.setNo("00");
//		cequipmentHolder.setDn(dn);
//		cequipmentHolder.setSid(equipment.getSlotId());
//		cequipmentHolder.setCollectTimepoint(new Date());
//		cequipmentHolder.setHolderType("slot");
//		cequipmentHolder.setHolderState("EMPTY");
//		if (dn.contains("@")){
//			cequipmentHolder.setParentDn(dn.substring(0, dn.indexOf("@")));
//		}
//		
//		cequipmentHolder.setEmsName(equipment.getEmsName());
//		cequipmentHolder.setUserLabel("SLOT_0X" + cequipmentHolder.getNo());
//		cequipmentHolder.setNativeEMSName("SLOT_0X" + cequipmentHolder.getNo());
//		return cequipmentHolder;
//	}


    protected void migrateCTP() throws Exception {
   //    executeDelete("delete  from CCTP c where c.emsName = '" + emsdn + "'", CCTP.class);
        executeTableDelete("C_CTP",emsdn);
        List<CTP> ctps = sd.queryAll(CTP.class);

        List<CTP> allCtps = new ArrayList<CTP>();
        HashMap<String,List<CTP>> ptpCtpMap = new HashMap<String, List<CTP>>();
        for (CTP ctp : ctps) {
            String portdn = ctp.getPortdn();
            DSUtil.putIntoValueList(ptpCtpMap,portdn,ctp);
        }


        List<Section> sections = sd.queryAll(Section.class);
        HashSet<String> sptps = new HashSet<String>();
        for (Section section : sections) {
            sptps.add(section.getaEndTP());
            sptps.add(section.getzEndTP());
        }
        for (String  sptp : sptps) {
            if (ptpCtpMap.get(sptp) == null) {
                CPTP cptp = ptpMap.get(sptp);
                if (cptp == null) {
                    getLogger().error("ptp not found:"+sptp);
                    continue;
                }

                List<CTP> vc4Ctps = SDHUtil.createVC4Ctps(cptp);
                getLogger().info("为PTP:"+sptp+" 补VC4 , size = "+(vc4Ctps == null ? null: vc4Ctps.size()));
                if (vc4Ctps != null)
                    ptpCtpMap.put(sptp,vc4Ctps);
            }
        }

        for (String portDn : ptpCtpMap.keySet()) {
            List<CTP> cs = ptpCtpMap.get(portDn);
            filterCTPS(portDn,cs);
            allCtps.addAll(cs);

            if (e1PtpMap.containsKey(portDn))
                e1PtpMap.remove(portDn);
        }
        getLogger().info(e1PtpMap.size() +" e1 ports to add default vc12 ctp");
        for (CPTP cptp : e1PtpMap.values()) {
            CTP newCTP = new CTP();
            //String newDn = portDn + "@CTP:/sts3c_au4-j="+j+"/vt2_tu12-k="+k+"-l="+l+"-m="+m;
            String newDn = cptp.getDn() + "@CTP:/vt2_tu12=1";
            newCTP.setDn(newDn);
            newCTP.setTag1("NEW");
            newCTP.setTag2("E1");
            newCTP.setNativeEMSName("VC12-1");
            newCTP.setPortdn(cptp.getDn());
            newCTP.setParentDn(cptp.getDn());
            allCtps.add(newCTP);
        }

        List<CCTP> list = insertCtps(allCtps);

        for (CCTP cctp : list) {
            ctpTable.addObject(new T_CTP(cctp));
        }
        //   insertMissedCtps();
    }

    protected List insertCtps(List<CTP> ctps) throws Exception {
        DataInserter di = new DataInserter(emsid);
        getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
        List<CCTP> cctps = new ArrayList<CCTP>();
        if (ctps != null && ctps.size() > 0) {
            for (CTP ctp : ctps) {
                CCTP cctp = transCTP(ctp);

                if (cctp != null) {
                    if (!cctp.getDn().contains(emsdn))
                        continue;
                    cctps.add(cctp);
                    if (cctp.getPortdn() == null || cctp.getPortdn().trim().isEmpty())
                        System.out.println("cctp = " + cctp.getDn());
                    if (cctp.getDn().equals("EMS:QUZ-T2000-3-P@ManagedElement:590467@PTP:/rack=1/shelf=1/slot=2/domain=sdh/port=1@CTP:/sts3c_au4-j=2"))
                        System.out.println("cctp = " + cctp);
                  //  di.insert(cctp);
                }
            }
        }

        di.insertWithDupCheck(cctps);

        di.end();
        return cctps;
    }

    public static void filterCTPS(String portDn,List<CTP> ctps) {
//      ObjectUtil.saveObject(portDn.replaceAll("/","<>"),ctps);
        List<CTP> vc4s = filterVC4(ctps);




        List<CTP> vc3s = filterVC3(ctps);
        //需要将vc3全部删除,以便补足vc12
        ctps.removeAll(vc3s);
        vc3s.clear();
        List<CTP> vc12s = filterVC12(ctps);
        System.out.println("vc4s size = " + vc4s.size());
        System.out.println("vc3s size = " + vc3s.size());
        System.out.println("vc12 size = " + vc12s.size());

        HashMap<Integer,HashMap<Integer,List<CTP>>> jkMap = new HashMap<Integer,HashMap<Integer,List<CTP>>>();

        HashSet<Integer> vc4JSet = new HashSet<Integer>();
        for (CTP vc4 : vc4s) {
            vc4JSet.add(CTPUtil.getJ(vc4.getDn()));
        }



        //     可能会有丢失的VC4
        List<CTP> newVC4S = new ArrayList<CTP>();
        for (CTP ctp : ctps) {
            int j = CTPUtil.getJ(ctp.getDn());
            if (j < 0) continue;
            if (!vc4JSet.contains(j)) {
                CTP newCTP = new CTP();
                String newDn = portDn + "@CTP:/sts3c_au4-j="+j;
                newCTP.setDn(newDn);
                newCTP.setTag1("NEW");
                newCTP.setPortdn(portDn);
                newCTP.setParentDn(portDn);

                newCTP.setNativeEMSName("VC4-"+j);
                vc4s.add(newCTP);
                newVC4S.add(newCTP);
                vc4JSet.add(j);
            }
        }
        if (newVC4S.size() > 0) {
            System.out.println(portDn+":newVC4S = " + newVC4S.size());
            ctps.addAll(newVC4S);
        }


        //////////////////////////////删除已经打散为VC12的vc3///////////////////////////////////
        if (vc12s.size() > 0) {
            for (CTP vc12 : vc12s) {

                String vc12Dn = vc12.getDn();
                if (!vc12Dn.contains("vt2_tu12"))
                    continue;
                int k = getK(vc12Dn);
                int j = CTPUtil.getJ(vc12Dn);

                HashMap<Integer,List<CTP>> kmap = jkMap.get(j);
                if (kmap == null) {
                    kmap = new HashMap<Integer,List<CTP>>();
                    jkMap.put(j,kmap);
                }

                List<CTP> list = kmap.get(k);
                if (list == null) {
                    list = new ArrayList<CTP>();
                    kmap.put(k,list);
                }
                list.add(vc12);

            }
        }

        HashSet<String> vc3KSet = new HashSet<String>();
        List<CTP> toDeleteVC3 = new ArrayList<CTP>();
        for (CTP vc3 : vc3s) {
            String dn = vc3.getDn();
            int j = CTPUtil.getJ(dn);
            int k = getK(dn);
            vc3KSet.add(j+"-"+k);
            if (jkMap.containsKey(j) && jkMap.get(j).containsKey(k)) {
                toDeleteVC3.add(vc3);
            }
        }


        //////////////////////////////删除已经打散为VC12的vc3///////////////////////////////////


        //////////////////////////////补充VC12//////////////////////////////////////////////
        List<CTP> newCTPs = new ArrayList<CTP>();
        for (Integer j : vc4JSet) {
            HashMap<Integer, List<CTP>> kmap = jkMap.get(j);
            if (kmap == null) kmap = new HashMap<Integer, List<CTP>>();
            for (int k = 1; k <=3 ; k++) {
                if (vc3KSet.contains(j+"-"+k)) continue;
                List<CTP> jkvc12s = kmap.get(k);
                for (int l = 1; l <= 7 ; l++) {
                    for (int m = 1; m <= 3; m++) {
                        if (getCTP(jkvc12s,k,l,m) == null) {
                            CTP newCTP = new CTP();
                            //String newDn = portDn + "@CTP:/sts3c_au4-j="+j+"/vt2_tu12-k="+k+"-l="+l+"-m="+m;
                            String newDn = portDn + "@CTP:/sts3c_au4-j="+j+"/tu3_vc3-k="+k+"/vt2_tu12-l="+l+"-m="+m;
                            newCTP.setDn(newDn);
                            newCTP.setTag1("NEW");
                            newCTP.setNativeEMSName("VC12-"+(21*(m-1) + 3*(l-1) + k));
                            newCTP.setPortdn(portDn);
                            newCTP.setParentDn(portDn);
                            newCTPs.add(newCTP);

                        }
                    }
                }
            }
        }
        System.out.println(portDn+":ctps = " + ctps.size());

        if (toDeleteVC3.size() > 0)
            System.out.println(portDn+":toDeleteVC3 = " + toDeleteVC3.size());
        if (newCTPs.size() > 0)
            System.out.println(portDn+":newCTPs = " + newCTPs.size());
        ctps.removeAll(toDeleteVC3);
        ctps.addAll(newCTPs);
    }

    private static CTP getCTP(List<CTP> ctps,int k,int l,int m) {
        if (ctps == null) return null;
        for (CTP ctp : ctps) {
            String dn = ctp.getDn();
            if (getK(dn) == k && getL(dn) ==l && getM(dn) == m) {
                return ctp;
            }
        }
        return null;
    }

    private static List<CTP> filterVC4(List<CTP> ctps) {
        List<CTP> vc4s = new ArrayList<CTP>();
        for (CTP ctp : ctps) {
            if (CTPUtil.isVC4(ctp.getDn()))
                vc4s.add(ctp);
        }
        return vc4s;
    }
    private static List<CTP> filterVC3(List<CTP> ctps) {
        List<CTP> vc3 = new ArrayList<CTP>();
        for (CTP ctp : ctps) {
            if (CTPUtil.isVC3(ctp.getDn()))
                vc3.add(ctp);
        }
        return vc3;
    }
    private static List<CTP> filterVC12(List<CTP> ctps) {
        List<CTP> vc12 = new ArrayList<CTP>();
        for (CTP ctp : ctps) {
            if (CTPUtil.isVC12(ctp.getDn()))
                vc12.add(ctp);
        }
        return vc12;
    }


//	private void insertMissedCtps() throws Exception {
//		int count = 0;
//		DataInserter di = new DataInserter(emsid);
//		for (String ctpdn : allCtpInCCSet){
//			T_CTP ctp = ctpTable.findObjectByDn(ctpdn);
//			if (ctp == null){
//				count++;
//				CCTP cctp = new CCTP();
//				cctp.setDn(ctpdn);
//				cctp.setRate(getCTPRate(ctpdn));
//				cctp.setPortdn(DNUtil.extractPortDn(ctpdn));
//				cctp.setParentCtpdn(getParentCTPdn(ctpdn));
//				cctp.setSid(DatabaseUtil.nextSID(cctp));
//				cctp.setCollectTimepoint(new Date());
//				cctp.setDirection(DicUtil.getPtpDirection("D_BIDIRECTIONAL"));
//		        cctp.setRateDesc(SDHUtil.rateDesc(cctp.getRate()));
//		        SDHUtil.setCTPNumber(cctp);
//				cctp.setParentDn(cctp.getPortdn());
//				cctp.setEmsName(emsdn);
//		        cctp.setTmRate(getTMRate(cctp.getRate()));
//				// cctp.setType(zctp.getType());
//				cctp.setTag1("MAKEUP");
////				cctp.setTag2(tag);
//				di.insert(cctp);
//				ctpTable.addObject(new T_CTP(cctp));
//			}
//		}
//		di.end();
//		getLogger().info("insert missed ctp in cc size = " + count);
//	}

//	private String getCTPRate(String dn){	
//		String rate="";
//        if (dn.contains("vt2_tu12")) {
//        	rate = "11";
//        	return rate;
//        }
//        if (dn.contains("vc3")) {
//            rate = "13";
//            return rate;
//        }
//        if (CTPUtil.isVC4(dn)) {
//            rate = "15";
//            return rate;
//        }		
//        return rate;
//	}

//    public List<CCTP> makeupVC12Ctps(T_CTP vc4) throws Exception {
//    	DataInserter di = new DataInserter(emsid);
//        String portDn = DNUtil.extractPortDn(vc4.getDn());
//        int j = CTPUtil.getJ(vc4.getDn());
//        List<CCTP> newCTPs = new ArrayList<CCTP>();
//        for (int k = 1; k <=3 ; k++) {
//            for (int l = 1; l <= 7 ; l++) {
//                for (int m = 1; m <= 3; m++) {
//                        CCTP newCTP = new CCTP();
//                        String newDn = portDn + "@CTP:/sts3c_au4-j="+j+"/vt2_tu12-k="+k+"-l="+l+"-m="+m;
//                       
//                        newCTP.setDn(newDn);
//                        newCTP.setTag1("MAKEUP");
//                        newCTP.setNativeEMSName("VC12-"+(21*(m-1) + 3*(l-1) + k));
//                        newCTP.setPortdn(portDn);
//                        newCTP.setParentDn(portDn);
//                        newCTP.setRate("11");
//                        newCTP.setTmRate("2M");
//                        newCTP.setParentCtpdn(vc4.getDn());
//                        newCTP.setRateDesc("VC12");
//                        newCTPs.add(newCTP);
//                        ctpTable.addObject(new T_CTP(newCTP));                        
//                }
//            }
//        }
//       
//        di.insert(newCTPs);
//        di.end();
//        return newCTPs;
//    }    

//    public List<CCTP> makeupVC12Ctps(T_CTP vc4, List<T_CTP> childCtps) throws Exception {
//    	DataInserter di = new DataInserter(emsid);
//        String portDn = DNUtil.extractPortDn(vc4.getDn());
//        String preK = "/vt2_tu12-k=";
//        String preL = "-l=";
//        String preM = "-m=";
//        if (childCtps.get(0).getDn().contains("tu3_vc3")){
//        	preK = "/tu3_vc3-k=";
//        	preL = "/vt2_tu12-l=";
//        	preM = "-m=";
//        }
//        HashSet<String> childDns = new HashSet<String>();
//        for (T_CTP childCtp : childCtps){
//        	childDns.add(childCtp.getDn());
//        }
//        int j = CTPUtil.getJ(vc4.getDn());
//        List<CCTP> newCTPs = new ArrayList<CCTP>();
//        for (int k = 1; k <=3 ; k++) {
//            for (int l = 1; l <= 7 ; l++) {
//                for (int m = 1; m <= 3; m++) {
//                        CCTP newCTP = new CCTP();
//                        String newDn = portDn + "@CTP:/sts3c_au4-j="+j+preK+k+preL+l+preM+m;
//                        if (childDns.contains(newDn)){
//                        	continue;
//                        }
//                        newCTP.setDn(newDn);
//                        newCTP.setTag1("MAKEUP");
//                        newCTP.setNativeEMSName("VC12-"+(21*(m-1) + 3*(l-1) + k));
//                        newCTP.setPortdn(portDn);
//                        newCTP.setParentDn(portDn);
//                        newCTP.setRate("11");
//                        newCTP.setTmRate("2M");
//                        newCTP.setParentCtpdn(vc4.getDn());
//                        newCTP.setRateDesc("VC12");
//                        newCTPs.add(newCTP);
//                        ctpTable.addObject(new T_CTP(newCTP));                        
//                }
//            }
//        }
//       
//        di.insert(newCTPs);
//        di.end();
//        return newCTPs;
//    }    



//	@Override 
//    protected List insertCtps(List<CTP> ctps) throws Exception{
//		DataInserter di = new DataInserter(emsid);
//		getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
//		List<CCTP> cctps = new ArrayList<CCTP>();
//		if (ctps != null && ctps.size() > 0) {
//			for (CTP ctp : ctps) {
//				CCTP cctp = transCTP(ctp);
//				if (cctp != null) {
////					//if vc3 has cc, reserve; otherwise, drop
////					if (CTPUtil.isVC3(ctp.getDn()) && !CCvc3CtpSet.contains(ctp.getDn())){		
////		//				getLogger().debug("vc3Ctp not in cc:" + ctp.getDn());
////						continue;
////					}
////					
////					//如果vc12对应的vc3有cc，则删除vc12
////					if (CTPUtil.isVC12(ctp.getDn())){
////						String vc3 = ctp.getDn().substring(0, ctp.getDn().lastIndexOf("/vt2_tu12"));
////						if (CCvc3CtpSet.contains(vc3)){
////							getLogger().info("vc3 has cc, delete vc12:" + ctp.getDn());
////							continue;
////						}
////					}
//					
//					
//					cctps.add(cctp);
////					if (cctp.getPortdn() == null || cctp.getPortdn().trim().isEmpty())
////						System.out.println("cctp = " + cctp.getDn());
////					if (cctp.getDn().equals("EMS:QUZ-T2000-3-P@ManagedElement:590467@PTP:/rack=1/shelf=1/slot=2/domain=sdh/port=1@CTP:/sts3c_au4-j=2"))
////						System.out.println("cctp = " + cctp);
//					di.insert(cctp);
//				}
//			}
//		}
//
//		di.end();
//        return cctps;
//    }
    private HashMap<String,List<CCTP>> ptpCCtpMap = new HashMap<String, List<CCTP>>();
    private HashMap<String,List<CCTP>> parentChildCTPMap = new HashMap<String, List<CCTP>>();
    @Override
    public CCTP transCTP(CTP ctp) {
        if (ctp.getRate() == null || ctp.getRate().isEmpty()) {
            String dn = ctp.getDn();
            if (dn.contains("vt2_tu12")) {
                ctp.setRate("11");
            }
            else if (dn.contains("vc3")) {
                ctp.setRate("13");
            }
            else if (CTPUtil.isVC4(dn)) {
                ctp.setRate("15");
            }
            ctp.setDirection("D_BIDIRECTIONAL");

        }
        ctp.setPortdn(ctp.getParentDn());
        CCTP cctp = super.transCTP(ctp);
        cctp.setParentCtpdn(getParentCTPdn(ctp.getDn()));
        cctp.setTmRate(getTMRate(cctp.getRate()));
        cctp.setTag1(ctp.getTag1());

        DSUtil.putIntoValueList(ptpCCtpMap,ctp.getPortdn(),cctp);
        DSUtil.putIntoValueList(parentChildCTPMap,cctp.getParentCtpdn(),cctp);
        return cctp;
    }

    private String getTMRate(String rateStr) {
        try {
            if (rateStr == null || rateStr.isEmpty()) return "";
            int rate = -1;
            HashSet<String> rates = new HashSet<String>();
            if (rateStr.contains(Constant.listSplit)) {
                String[] split = rateStr.split(Constant.listSplitReg);

                if (split != null) {

                    for (String s : split) {
                        rates.add(s);
                        rate = Integer.parseInt(s);
                        String tmRate = SDHUtil.getTMRate(rate);
                        if (tmRate != null) return tmRate;
                    }
                }
            } else {
                rate = Integer.parseInt(rateStr);

                String tmRate = SDHUtil.getTMRate(rate);
                if (tmRate != null) return tmRate;
                rates.add(rateStr);
            }
        } catch ( Exception e) {
            return null;
        }
        return null;
    }

    protected static String getParentCTPdn(String ctpDn) {
        int i = ctpDn.indexOf("CTP:/");
        if (i > 0) {
            int vc4 = ctpDn.indexOf("/sts3c_au4");
            int j = ctpDn.indexOf("/vt2_tu12");
            int k = ctpDn.indexOf("/tu3_vc3");
            if (vc4 > 0){
                if (j > 0 ){
                    if (k > 0){
                        return ctpDn.substring(0, k);
                    } else {
                        return ctpDn.substring(0, j);
                    }
                }
                if (k > 0 ){
                    return ctpDn.substring(0, k);
                }
            }
        }
        return null;
    }
    private String toPtps(String ends) {
        String[] ctps = ends.split(Constant.listSplitReg);
        String ptps = "";
        for (String ctp : ctps) {
            ptps += DNUtil.extractPortDn(ctp)+"||";
        }
        return ptps.substring(0,ptps.length()-2);
    }

    public void migratePathAndRoute() throws Exception {
        executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
        executeDelete("delete  from CRoute_CC c where c.emsName = '" + emsdn + "'", CRoute_CC.class);
        executeDelete("delete  from CPath c where c.emsName = '" + emsdn + "'", CPath.class);
        executeDelete("delete  from CChannel c where c.emsName = '" + emsdn + "'", CChannel.class);
        executeDelete("delete  from CRoute_Channel c where c.emsName = '" + emsdn + "'", CRoute_Channel.class);
        executeDelete("delete  from CPath_CC c where c.emsName = '" + emsdn + "'", CPath_CC.class);
        executeDelete("delete  from CPath_Channel c where c.emsName = '" + emsdn + "'", CPath_Channel.class);



        FHSDHPathSearcher searcher = new FHSDHPathSearcher(emsdn,ptpSectionMap,ccList,ptpCCtpMap,parentChildCTPMap,getLogger());
        searcher.setCcTable(ccTable);
        searcher.search();

        getLogger().info("path size = "+searcher.pathList.size());
        getLogger().info("vc4 route size = "+searcher.vc4Routes.size());
        for (CRoute vc4Route : searcher.vc4Routes) {
            getLogger().info("vc4route : aend = "+vc4Route.getAend()+" zend = "+vc4Route.getZend());
        }
        getLogger().info("vc4channel size = " + searcher.vc4Channels.size());

        getLogger().info("vc12 channel size = " + searcher.vc12Channels.size());
        cChannelList.addAll(searcher.vc4Channels);
        cChannelList.addAll(searcher.vc12Channels);
        cPathList.addAll(searcher.pathList);
        pathCCList.addAll(searcher.cPath_ccs);
        pathChannelList.addAll(searcher.cPath_channels);

//        ObjectUtil.saveObject("path",searcher.pathList);
//        ObjectUtil.saveObject("vc4",searcher.vc4Channels);
//        ObjectUtil.saveObject("vc12",searcher.vc12Channels);
        HashMap<String,CChannel> endChannelMap = new HashMap<String, CChannel>();
        for (CChannel channel : searcher.vc12Channels) {
            endChannelMap.put(channel.getAend(),channel);
            endChannelMap.put(channel.getZend(),channel);
        }
        HashMap<String,CRoute> routeMap = new HashMap<String, CRoute>();

        for (CRoute vc4Route : searcher.vc4Routes) {
            routeMap.put(vc4Route.getDn(),vc4Route);
        }
        for (CCrossConnect cc : ccList) {
         //   getLogger().info("trystartcc:"+cc.getDn()+";aend="+cc.getAend());
            if (cc.getAend().endsWith("@CTP:/vt2_tu12=1") || (cc.getAend().contains("tu12") && !ptpSectionMap.containsKey(DNUtil.extractPortDn(cc.getAend())))) {
             //   getLogger().info("startcc:"+cc.getDn()+";aend="+cc.getAend());
                FHSDHRouteSearcher searcher1 = new FHSDHRouteSearcher(emsdn,cc,ccTable,getLogger(),endChannelMap);
                try {
                    CRoute route = searcher1.search();
                    if (route != null) {
                        String aend = route.getAend();
                        String zend = route.getZend();
                        String revertDn = route.getZend()+"<>"+route.getAend();




                        HashMap<String, HashSet> map = new HashMap<String, HashSet>();
                        map.put("cc",new HashSet());
                        map.put("channel",new HashSet());
                        route.setUserObject(map);
                        for (T_CCrossConnect tcc : searcher1.ccs)
                            map.get("cc").add(tcc.getDn());
                        for (CChannel channel : searcher1.channels)
                            map.get("channel").add(channel);


                        if (friendCtpMap.containsKey(aend)) {
                            List<String> aendFriends = friendCtpMap.get(aend);
                            String aends = aend;
                            for (String aendFriend : aendFriends) {
                                aends += "||"+aendFriend;
                                List<CacheClass.T_CCrossConnect> ccs = ccTable.findObjectByIndexColumn("aend", aendFriend);
                                for (T_CCrossConnect t_cCrossConnect : ccs) {
                                    map.get("cc").add(t_cCrossConnect.getDn());
                                }

                            }
                            route.setAend(null);
                            route.setAends(aends);
                            route.setAptp(null);
                            route.setAptps(toPtps(aends));

                            getLogger().info(route.getDn()+" update "+"aends="+aends);
                        }


                        if (friendCtpMap.containsKey(zend)) {
                            List<String> zendFriends = friendCtpMap.get(zend);
                            String zends = zend;
                            for (String zendFriend : zendFriends) {
                                zends += "||"+zendFriend;
                                List<CacheClass.T_CCrossConnect> ccs = ccTable.findObjectByIndexColumn("zend", zendFriend);
                                for (T_CCrossConnect t_cCrossConnect : ccs) {
                                    map.get("cc").add(t_cCrossConnect.getDn());
                                }
                            }
                            route.setZend(null);
                            route.setZends(zends);
                            route.setZptp(null);
                            route.setZptps(toPtps(zends));
                            getLogger().info(route.getDn()+" update "+"zends="+zends);
                        }



                        if (routeMap.containsKey(revertDn)) {
                            CRoute reverRoute = routeMap.get(revertDn);
                            HashMap<String,HashSet> mp = (HashMap)reverRoute.getUserObject();
                            mp.get("cc").addAll(map.get("cc"));
                            mp.get("channel").addAll(map.get("channel"));

                        } else
                            routeMap.put(route.getDn(),route);





                    }
                } catch (Exception e) {
                    getLogger().error(e,e);
                }
            }
        }

        HashSet<String> toDelete = new HashSet<String>();
        HashMap<String,CRoute> ctpRouteMap = new HashMap<String, CRoute>();
        for (String routeDn : routeMap.keySet()) {
            CRoute cRoute = routeMap.get(routeDn);
            String aend = cRoute.getAend();
            String zend = cRoute.getZend();

            if (aend == null || aend.isEmpty()) aend = cRoute.getAends();
            if (zend == null || zend.isEmpty()) zend = cRoute.getZends();

            String key1 = aend+"<>"+zend;
            String key2 = zend+"<>"+aend;
            if (ctpRouteMap.containsKey(aend)) {
                CRoute old = ctpRouteMap.get(aend);
                if (getCC(old).size() < getCC(cRoute).size())
                    toDelete.add(old.getDn());
                else
                    toDelete.add(cRoute.getDn());
            }

            if (ctpRouteMap.containsKey(zend)) {
                CRoute old = ctpRouteMap.get(zend);
                if (getCC(old).size() < getCC(cRoute).size())
                    toDelete.add(old.getDn());
                else
                    toDelete.add(cRoute.getDn());
            }

            if (!toDelete.contains(routeDn)) {
                ctpRouteMap.put(aend, cRoute);
                ctpRouteMap.put(zend, cRoute);
            }
        }
        getLogger().info(toDelete.size()+" to remove!!!");
        for (String dn : toDelete) {
            routeMap.remove(dn);
            getLogger().info("remove:"+dn);
        }
        for (CRoute cRoute : routeMap.values()) {
            cRouteList.add(cRoute);
            for (String ccDn : getCC(cRoute)) {
                routeCCList.add(U2000MigratorUtil.createCRoute_CC(emsdn,ccDn,cRoute));
            }
            for (CChannel channel : getCChannel(cRoute)) {
                routeChannelList.add(U2000MigratorUtil.createCRoute_Channel(emsdn, channel,cRoute));
            }
        }

        getLogger().info(" route size = "+cRouteList.size());








        try {
//            createPaths();
//            createRoutes();

            DataInserter di = new DataInserter(emsid);
            di.insertWithDupCheck(cChannelList);
            di.insertWithDupCheck(cPathList);
            di.insertWithDupCheck(pathCCList);
            di.insertWithDupCheck(pathChannelList);
            di.insertWithDupCheck(cRouteList);
            di.insertWithDupCheck(routeCCList);
            di.insertWithDupCheck(routeChannelList);

            di.end();
        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {

        }
    }

    private HashSet<String> getCC(CRoute route) {
        return (HashSet)((HashMap) route.getUserObject()).get("cc");
    }
    private HashSet<CChannel> getCChannel(CRoute route) {
        return (HashSet)((HashMap) route.getUserObject()).get("channel");
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

//        if (path.getZend().equals("EMS:HUZ-OTNM2000-7-P@ManagedElement:134247228;66576@PTP:/rack=1341697/shelf=1/slot=22021123/port=1@CTP:/sts3c_au4-j=6")){
//        	getLogger().info("start to break up path:" + path.getDn());
//        }
        String[] aendCtps = aends.split(Constant.listSplitReg);
        String[] zendCtps = zends.split(Constant.listSplitReg);

        for (String aend : aendCtps) {
            for (String zend : zendCtps) {
                if (aend != null && zend != null) {
                    if (CTPUtil.isVC4(aend) && CTPUtil.isVC4(zend)) {

                        try {
                            HashSet<String> usedCtpdns = new HashSet<String>();
                            List<T_CTP> achildCtps = ctpTable.findObjectByIndexColumn("parentCtp", aend);
                            List<T_CTP> zchildCtps = ctpTable.findObjectByIndexColumn("parentCtp", zend);
//                            if (path.getZend().equals("EMS:HUZ-OTNM2000-7-P@ManagedElement:134247228;66576@PTP:/rack=1341697/shelf=1/slot=22021123/port=1@CTP:/sts3c_au4-j=6")){
//                            	getLogger().info("a child ctp size:" + achildCtps.size());
//                            	getLogger().info("z child ctp size:" + zchildCtps.size());
//                            }
                            for (T_CTP achildCtp : achildCtps) {
                                for (T_CTP zchildCtp : zchildCtps) {
//                                    if (path.getZend().equals("EMS:HUZ-OTNM2000-7-P@ManagedElement:134247228;66576@PTP:/rack=1341697/shelf=1/slot=22021123/port=1@CTP:/sts3c_au4-j=6")){
//                                    	getLogger().info("a child ctp :" + achildCtp.getDn());
//                                    	getLogger().info("z child ctp size:" + achildCtp.getDn());
//                                    }
                                    if (usedCtpdns.contains(achildCtp.getDn()) || usedCtpdns.contains(zchildCtp.getDn())){
                                        continue;
                                    }

                                    //VC3
                                    if (CTPUtil.isVC3(achildCtp.getDn()) && CTPUtil.isVC3(zchildCtp.getDn())){
                                        String adn = DNUtil.extractCTPSimpleName(achildCtp.getDn());
                                        String zdn = DNUtil.extractCTPSimpleName(zchildCtp.getDn());
                                        if (getK(adn) == getK(zdn)){
                                            createCChannel(achildCtp,zchildCtp,path);
                                            usedCtpdns.add(achildCtp.getDn());
                                            usedCtpdns.add(zchildCtp.getDn());
                                        }
                                    }
                                    if (CTPUtil.isVC12(achildCtp.getDn()) && CTPUtil.isVC12(zchildCtp.getDn())){
                                        String adn = DNUtil.extractCTPSimpleName(achildCtp.getDn());
                                        String zdn = DNUtil.extractCTPSimpleName(zchildCtp.getDn());

                                        //因为CTPUtil中的getk方法对从vc3打散的vc12不支持，所以自己写方法
                                        if (getK(adn) == getK(zdn)
                                                && getL(adn) == getL(zdn)
                                                && getM(adn) == getM(zdn)){
                                            createCChannel(achildCtp,zchildCtp,path);
                                            usedCtpdns.add(achildCtp.getDn());
                                            usedCtpdns.add(zchildCtp.getDn());
//                                            if (path.getZend().equals("EMS:HUZ-OTNM2000-7-P@ManagedElement:134247228;66576@PTP:/rack=1341697/shelf=1/slot=22021123/port=1@CTP:/sts3c_au4-j=6")){
//                                            	getLogger().info("create channel, a end ctp:" + achildCtp.getDn());
//                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            getLogger().info("error aend: " + aend);
                            getLogger().info("error zend: " + zend);
                            getLogger().error(e, e);
                        }

                    }
                }
            }
        }



    }

    private static int getK(String simpleDn){
        char c = simpleDn.charAt(simpleDn.lastIndexOf("k=") + 2);
        return Integer.parseInt(c+"");
    }

    private static int getL(String simpleDn){
        char c = simpleDn.charAt(simpleDn.lastIndexOf("l=") + 2);
        return Integer.parseInt(c+"");
    }

    private static int getM(String simpleDn){
        char c = simpleDn.charAt(simpleDn.lastIndexOf("m=") + 2);
        return Integer.parseInt(c+"");
    }

    private HashMap<String,CPTP> e1PtpMap = new HashMap<String,CPTP>();
    @Override
    public CPTP transPTP(PTP ptp) {
        CPTP cptp = new CPTP();

        String dn = ptp.getDn();
        if (dn.startsWith(":@ManagedElement:"))
            dn = "EMS:"+emsdn + dn.substring(1);
        cptp.setDn(dn);

        if (ptp.getRate() != null && (ptp.getRate().endsWith("||80")
                ||  ptp.getRate().startsWith("80||")
                ||  ptp.getRate().equals("80")
                ||  ptp.getRate().contains("||80||"))

                ) {
            e1PtpMap.put(dn,cptp);
        }

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
        cptp.setEoType(getEOType(cptp.getLayerRates()));
        if (cptp.getDn().contains("FTP"))
            cptp.setEoType(DicConst.EOTYPE_UNKNOWN);
        cptp.setSpeed(getSpeed(cptp.getLayerRates()));
        // HashMap<String, String> addMap = transMapValue(ptp.getAdditionalInfo());
        // if (addMap.get("SupportedPortType") != null && addMap.get("SupportedPortType").contains("Optical"))
        // cptp.setEoType(DicConst.EOTYPE_OPTIC);

        // cptp.setType(addMap.get("EntityClass"));
        cptp.setType(getPtpType(dn, cptp.getLayerRates()));
        ptpMap.put(cptp.getDn(), cptp);
        cptp.setRate(ptp.getRate());
        if (cptp.getEoType() == DicConst.EOTYPE_ELECTRIC && "OPTICAL".equals(cptp.getType()))
            cptp.setType("ELECTRICAL");
        return cptp; // To change body of created methods use File | Settings | File Templates.
    }

    private Integer getEOType(String layerRates){
        List<Integer> list = DicUtil.convertLayerRateList(layerRates);
        for (int rate : list) {
            if (rate == DicConst.LR_PHYSICAL_ELECTRICAL || rate == DicConst.LR_E1_2M || rate == DicConst.LR_DSR_2M) {
                return DicConst.EOTYPE_ELECTRIC;
            } else if (rate == DicConst.LR_PHYSICAL_OPTICAL ||
                    rate == DicConst.LR_Section_OC3_STS3_and_RS_STM1 ||
                    rate == DicConst.LR_Section_OC12_STS12_and_RS_STM4 ||
                    rate == DicConst.LR_Section_OC48_STS48_and_RS_STM16 ||
                    rate == DicConst.LR_Section_OC192_STS192_and_RS_STM64) {
                return DicConst.EOTYPE_OPTIC;
            }
        }
        return DicConst.EOTYPE_UNKNOWN;
    }

    private String getPtpType(String dn, String layerRates) {
        if (dn.contains("FTP"))
            return ("LOGICAL");
        String type = DicUtil.getPtpType(dn,layerRates);
        if (dn.contains("FTP")) {
            if (type != null) {
                return type;
            }
            return "LOGICAL";
        }

        if (dn.contains("type=lp/"))
            return "LP";
        if (dn.contains("type=mp/"))
            return "mp";
        if (dn.contains("type=mac/"))
            return "mac";
        return type;
    }

    private String getSpeed(String layerRates) {
        List<Integer> list = DicUtil.convertLayerRateList(layerRates);
        for (int rate : list) {
            if (rate == DicConst.LR_PHYSICAL_OPTICAL || rate == DicConst.LR_OPTICAL_SECTION)
                continue;
            String speedByRate = DicUtil.getSpeedByRate(rate);
            if (speedByRate != null)
                return speedByRate;
        }
        return null;
    }

    @Override
    protected void migrateSection() throws Exception {
        executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "'", CSection.class);
        DataInserter di = new DataInserter(emsid);
        List<Section> sections = sd.queryAll(Section.class);
        List<CSection> cSections = new ArrayList<CSection>();
        if (sections != null && sections.size() > 0) {
            for (Section section : sections) {
                CSection csection = transSection(section);
                if (ptpSectionMap.containsKey(csection.getAendTp()) || ptpSectionMap.containsKey(csection.getZendTp())){
                    continue;
                }
                //   csection.setSid(DatabaseUtil.nextSID(csection));
                // csection.setSid(toSid(Long.parseLong(section.getDn().substring(section.getDn().lastIndexOf(" - ") + 3))));
                String aendtp = csection.getAendTp();
                String zendtp = csection.getZendTp();
                if (aendtp.contains("CTP") || zendtp.contains("CTP")) {
                    continue;
                }
//                //补足a、z端PTP
//                if (!ptpMap.containsKey(aendtp)){
//                	di.insert(generatePTP(aendtp, csection));
//                }
//                if (!ptpMap.containsKey(zendtp)){
//                	di.insert(generatePTP(zendtp, csection));
//                }
                csection.setAptpId(DatabaseUtil.getSID(CPTP.class, aendtp));
                csection.setZptpId(DatabaseUtil.getSID(CPTP.class, zendtp));
           //     csection.setType("OMS");


                String ane = DNUtil.extractNEDn(csection.getAendTp());
                String zne = DNUtil.extractNEDn(csection.getZendTp());
                getLogger().info("ane="+ane+";zne="+zne);
                if (ane != null && zne != null) {
                    CDevice adevice = cDeviceMap.get(ane);
                    CDevice zdevice = cDeviceMap.get(zne);
                    if (adevice != null && adevice.getProductName().contains(" WDM ") && zdevice != null && zdevice.getProductName().contains(" WDM "))
                        csection.setType("OTS");
                    else
                        csection.setType("OMS");
                    getLogger().info("adevice:"+(adevice == null ? null:adevice.getProductName())+"--"+"zdevice:"+(zdevice == null ? null:zdevice.getProductName()));
                } else
                    csection.setType("OMS");

                di.insert(csection);
                ptpSectionMap.put(csection.getAendTp(), csection);
                ptpSectionMap.put(csection.getZendTp(), csection);
                cSections.add(csection);
                //sectionTable.addObject(section);
            }
        }
        di.end();

        breakupSections(cSections);
        getLogger().info("breakup high order channel size:" + highOrderCtpChannelMap.size());
    }

    private CPTP generatePTP(String dn,CSection section){
        CPTP cptp = new CPTP();
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
        if (dn.contains("port=")) {
            cptp.setNo(dn.substring(dn.lastIndexOf("port=") + 5));
        }
        cptp.setCollectTimepoint(new Date());
        cptp.setRate(section.getRate());
        cptp.setDirection(DicConst.PTP_DIRECTION_BIDIRECTIONAL);
        //	cptp.setTransmissionParams(ptp.getTransmissionParams());
        cptp.setLayerRates(section.getRate());
//		cptp.setTpProtectionAssociation(ptp.getTpProtectionAssociation());
        cptp.setEmsName(section.getEmsName());
        //	cptp.setUserLabel(ptp.getUserLabel());
        //	cptp.setNativeEMSName(ptp.getNativeEMSName());
//		cptp.setOwner(ptp.getOwner());
        //	cptp.setAdditionalInfo(ptp.getAdditionalInfo());

        cptp.setDeviceDn(DNUtil.extractNEDn(dn));
        cptp.setEoType(getEOType(cptp.getLayerRates()));
        cptp.setSpeed(getSpeed(cptp.getLayerRates()));
        // HashMap<String, String> addMap = transMapValue(ptp.getAdditionalInfo());
        // if (addMap.get("SupportedPortType") != null && addMap.get("SupportedPortType").contains("Optical"))
        // cptp.setEoType(DicConst.EOTYPE_OPTIC);

        // cptp.setType(addMap.get("EntityClass"));
        cptp.setTag1("MAKEUP");
        cptp.setType(getPtpType(dn, cptp.getLayerRates()));
        ptpMap.put(cptp.getDn(), cptp);
        return cptp; // To change body of created methods use File | Settings | File Templates.
    }

    public void breakupSections(List<CSection> sections) {
        for (CSection section : sections) {
            //  	getLogger().info("begin breakup section:" + section.getDn());
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

    private void createPaths(){
        Collection<CChannel> highChannels = highOrderCtpChannelMap.values();
        if (highChannels == null || highChannels.isEmpty()){
            return;
        }

        for (CChannel channel : highChannels){
            if (pathCtpSet.contains(channel.getAend()) || pathCtpSet.contains(channel.getZend())){
                continue;
            }
            List<CChannel> routeChannels = new ArrayList<CChannel>();
            List<T_CCrossConnect> routeCCs = new ArrayList<T_CCrossConnect>();
            HashSet<String> passedCtpSet = new HashSet<String>();
            T_CTP aSideCtp = null;
            T_CTP zSideCtp = null;
            try {
                aSideCtp = ctpTable.findObjectByDn(channel.getAend());
                zSideCtp = ctpTable.findObjectByDn(channel.getZend());
            } catch (Exception e) {
                getLogger().error(e, e);
            }
            if (aSideCtp == null){
                getLogger().error("无法找到channel:" + channel.getDn() + "的A端CTP:"+ channel.getAend());
                continue;
            }
            if (zSideCtp == null){
                getLogger().error("无法找到channel:" + channel.getDn() + "的Z端CTP:"+ channel.getZend());
                continue;
            }
            routeChannels.add(channel);
            passedCtpSet.add(aSideCtp.getDn());
            passedCtpSet.add(zSideCtp.getDn());

            PathCtp aSidePathCtp = new PathCtp(aSideCtp, ENTITY_TYPE_CHANNEL);
            PathCtp zSidePathCtp = new PathCtp(zSideCtp, ENTITY_TYPE_CHANNEL);
            PathCtp aEndPathCtp = getNextCtp(aSideCtp, routeChannels, routeCCs, passedCtpSet, pathCtpSet, highOrderCtpChannelMap);
            PathCtp lastAPathCtp = aSidePathCtp;
            while (aEndPathCtp != null){
                lastAPathCtp = aEndPathCtp;
                aEndPathCtp = getNextCtp(lastAPathCtp.ctp, routeChannels, routeCCs, passedCtpSet, pathCtpSet, highOrderCtpChannelMap);
            }
            aEndPathCtp = lastAPathCtp;

//    		getLogger().info("path aside end ctp:" + aEndCtp.getDn());
//    		getLogger().info(" path channel zside ctp:" + zSideCtp.getDn());
            PathCtp zEndPathCtp = getNextCtp(zSideCtp, routeChannels, routeCCs, passedCtpSet, pathCtpSet, highOrderCtpChannelMap);
            PathCtp lastZPathCtp = zSidePathCtp;
            while (zEndPathCtp != null){
                lastZPathCtp = zEndPathCtp;
                zEndPathCtp = getNextCtp(lastZPathCtp.ctp, routeChannels, routeCCs, passedCtpSet, pathCtpSet, highOrderCtpChannelMap);
            }
            zEndPathCtp = lastZPathCtp;
            // if az end is not channel, path is not created
            if (aEndPathCtp.pathType != ENTITY_TYPE_CHANNEL){
                //getLogger().info("aend  is not channel, path is not created, aendCtp:" + aEndPathCtp.ctp.getDn());
                continue;
            }
            if (zEndPathCtp.pathType != ENTITY_TYPE_CHANNEL){
                //	getLogger().info("zend is not channel, path is not created, zendCtp:" + zEndPathCtp.ctp.getDn());
                continue;
            }

            //如果两端channel还连了CC，则不生成path
            if (allCtpInCCSet.contains(aEndPathCtp.ctp.getDn()) || allCtpInCCSet.contains(zEndPathCtp.ctp.getDn())){
                getLogger().info("aend or zend has cc, path is not created");
                continue;
            }


            List<T_CTP> actps = null;
            List<T_CTP> zctps = null;
            try {
                actps = ctpTable.findObjectByIndexColumn("parentCtp",aEndPathCtp.ctp.getDn());
                zctps = ctpTable.findObjectByIndexColumn("parentCtp", zEndPathCtp.ctp.getDn());
            } catch (Exception e) {
                getLogger().error(e, e);
            }
            //如果两端ctp均没被打散，则不形成path
            if ((actps == null || actps.isEmpty()) && (zctps == null || zctps.isEmpty())){
                //	getLogger().info("aend is not splitted, path is not created, aendCtp:" + aEndPathCtp.ctp.getDn());
                continue;
            }
//			try {
//				// 如果a端ctp未被打散而z端被打散了，则补足a端
//				if (actps == null || actps.isEmpty()) {
//					List<CCTP> vc12CTPs = makeupVC12Ctps(aEndPathCtp.ctp);
//					getLogger().info("makeup path aend vc12 ctps size:" + vc12CTPs.size() + ", actp:" + aEndPathCtp.ctp.getDn());      
//				} else if (zctps == null || zctps.isEmpty()) {
//					// 如果z端ctp未被打散而a端被打散了，则补足z端
//					List<CCTP> vc12CTPs = makeupVC12Ctps(zEndPathCtp.ctp);
//					getLogger().info("makeup path zend vc12 ctps size:" + vc12CTPs.size() + ", zctp:" + zEndPathCtp.ctp.getDn());  
//				} else if (actps.size() != 63 || actps.size() != 63){
//					List<CCTP> aMakeupCtps = makeupVC12Ctps(aEndPathCtp.ctp, actps);
//					getLogger().info("makeup path aend vc12 ctps size:" + aMakeupCtps.size() + ", actp:" + aEndPathCtp.ctp.getDn());  
//					List<CCTP> zMakeupCtps = makeupVC12Ctps(zEndPathCtp.ctp, zctps);
//					getLogger().info("makeup path zend vc12 ctps size:" + zMakeupCtps.size() + ", zctp:" + zEndPathCtp.ctp.getDn());  
//				} 
//			} catch (Exception e) {
//				getLogger().error(e.getMessage(), e);
//			}


//    		getLogger().info("path zside end ctp:" + zEndCtp.getDn());
            //开始创建Path
            CPath path = createCPath(aEndPathCtp.ctp, zEndPathCtp.ctp, channel);
            cPathList.add(path);

            for (CChannel pathChannel : routeChannels){
                pathCtpSet.add(pathChannel.getAend());
                pathCtpSet.add(pathChannel.getZend());
                pathChannelList.add(OTNM2000MigratorUtil.createCPath_Channel(emsdn, pathChannel, path));
            }
            for (T_CCrossConnect cc : routeCCs){
                pathCtpSet.add(cc.getAend());
                pathCtpSet.add(cc.getZend());
                pathCCList.add(OTNM2000MigratorUtil.createCPath_CC(emsdn, cc.getDn(), path));
            }
        }
        getLogger().info("create path size:" + cPathList.size());
        getLogger().info("create path_channel size:" + pathChannelList.size());
        getLogger().info("create path_cc size:" + pathCCList.size());
    }
    private void createRoutes2(){

    }
    private void createRoutes(){
        for (CPath path : cPathList){
            breakupCPaths(path);
        }
        Collection<CChannel> lowOrderChannels = lowOrderCtpChannelMap.values();
        if (lowOrderChannels == null || lowOrderChannels.isEmpty()){
            return;
        }

        for (CChannel channel : lowOrderChannels){
            if (routeCtpSet.contains(channel.getAend()) || routeCtpSet.contains(channel.getZend())){
                continue;
            }
            List<CChannel> routeChannels = new ArrayList<CChannel>();
            List<T_CCrossConnect> routeCCs = new ArrayList<T_CCrossConnect>();
            HashSet<String> passedCtpSet = new HashSet<String>();

            //	List<CChannel> routeChannels = new ArrayList<CChannel>();
            //	List<T_CCrossConnect> routeCCs = new ArrayList<T_CCrossConnect>();
            T_CTP aSideCtp = null;
            T_CTP zSideCtp = null;
            try {
                aSideCtp = ctpTable.findObjectByDn(channel.getAend());
                zSideCtp = ctpTable.findObjectByDn(channel.getZend());
            } catch (Exception e) {
                getLogger().error(e, e);
            }
            if (aSideCtp == null){
                getLogger().error("无法找到channel:" + channel.getDn() + "的A端CTP:"+ channel.getAend());
                continue;
            }
            if (zSideCtp == null){
                getLogger().error("无法找到channel:" + channel.getDn() + "的Z端CTP:"+ channel.getZend());
                continue;
            }
            routeChannels.add(channel);
            passedCtpSet.add(aSideCtp.getDn());
            passedCtpSet.add(zSideCtp.getDn());
            PathCtp aSidePathCtp = new PathCtp(aSideCtp, ENTITY_TYPE_CHANNEL);
            PathCtp zSidePathCtp = new PathCtp(zSideCtp, ENTITY_TYPE_CHANNEL);
            // 		getLogger().info("route channel aside ctp:" + aSideCtp.getDn());
            PathCtp aEndPathCtp = getNextCtp(aSideCtp, routeChannels, routeCCs, passedCtpSet, routeCtpSet, lowOrderCtpChannelMap);
            PathCtp lastAPathCtp = aSidePathCtp;
            while (aEndPathCtp != null){
                lastAPathCtp = aEndPathCtp;
                aEndPathCtp = getNextCtp(lastAPathCtp.ctp, routeChannels, routeCCs, passedCtpSet, routeCtpSet, lowOrderCtpChannelMap);
            }
            aEndPathCtp = lastAPathCtp;

            PathCtp zEndPathCtp = getNextCtp(zSideCtp, routeChannels, routeCCs, passedCtpSet, routeCtpSet, lowOrderCtpChannelMap);
            PathCtp lastZPathCtp = zSidePathCtp;
            while (zEndPathCtp != null){
                lastZPathCtp = zEndPathCtp;
                zEndPathCtp = getNextCtp(lastZPathCtp.ctp, routeChannels, routeCCs, passedCtpSet, routeCtpSet, lowOrderCtpChannelMap);
            }
            zEndPathCtp = lastZPathCtp;

            //如果channel往外找不到任何cc，则不生成route
            if (aEndPathCtp.ctp.getDn().equals(aSideCtp.getDn()) && zEndPathCtp.ctp.getDn().equals(zSideCtp.getDn())){
                continue;
            }
            if (aEndPathCtp.pathType != ENTITY_TYPE_CC || zEndPathCtp.pathType != ENTITY_TYPE_CC){
                //getLogger().info("aend is not cc, route is not created, aendCtp:" + aEndPathCtp.ctp.getDn());
                continue;
            }

            if (lowOrderCtpChannelMap.containsKey(aEndPathCtp.ctp.getDn()) || lowOrderCtpChannelMap.containsKey(zEndPathCtp.ctp.getDn())){
                getLogger().info("aend or zend cc has channel, route is not created. aend:" + aEndPathCtp.ctp.getDn() + ", zend:" + zEndPathCtp.ctp.getDn());
                continue;
            }

            if (ptpSectionMap.containsKey(DNUtil.extractPortDn(aEndPathCtp.ctp.getDn()))
                    || ptpSectionMap.containsKey(DNUtil.extractPortDn(zEndPathCtp.ctp.getDn()))){
                getLogger().info("aend or zend cc has section, route is not created. aend:" + aEndPathCtp.ctp.getDn() + ", zend:" + zEndPathCtp.ctp.getDn());
                continue;
            }

//    		getLogger().info("route zside end ctp:" + zEndCtp.getDn());
            //开始创建route
            CRoute route = createCRoute(aEndPathCtp.ctp, zEndPathCtp.ctp, channel);
            cRouteList.add(route);

            for (CChannel routeChannel : routeChannels){
                routeCtpSet.add(routeChannel.getAend());
                routeCtpSet.add(routeChannel.getZend());
                routeChannelList.add(OTNM2000MigratorUtil.createCRoute_Channel(emsdn, routeChannel, route));
            }

            for (T_CCrossConnect cc : routeCCs){
                routeCCList.add(OTNM2000MigratorUtil.createCRoute_CC(emsdn, cc.getDn(), route));
                routeCtpSet.add(cc.getAend());
                routeCtpSet.add(cc.getZend());
            }
        }
        getLogger().info("create route size:" + cRouteList.size());
        getLogger().info("create route_channel size:" + routeChannelList.size());
        getLogger().info("create route_cc size:" + routeCCList.size());
    }

    private class PathCtp{
        public PathCtp(T_CTP ctp, int pathType){
            this.ctp = ctp;
            this.pathType = pathType;
        }
        T_CTP ctp;
        int pathType;
    }

    /**
     * 寻找path的下一个ctp
     * @param ctp
     * @param  ，当前已经过的channel
     * @param  ，当前已经过的cc
     * @return
     */
    private PathCtp getNextCtp(T_CTP ctp, List<CChannel> channelList, List<T_CCrossConnect> ccList,
                               HashSet<String> currentPassedCtpSet, HashSet<String> totalPassedCtpSet, HashMap<String,CChannel> channelMap){
        //   	CChannel nextChannel = null;
//		List<T_CCrossConnect> ccs;
        T_CCrossConnect nextcc = null;
        T_CTP nextCtp = null;
//		T_CTP lastCtp = null;
        try {
            ////找交叉连接的CTP
            List<T_CCrossConnect> ccs = ccTable.findObjectByIndexColumn("aend", ctp.getDn());
            if (ccs != null && !ccs.isEmpty()){
                for (T_CCrossConnect cc : ccs){
                    if (!currentPassedCtpSet.contains(cc.getZend())
                            && !totalPassedCtpSet.contains(cc.getZend())){
                        nextCtp = ctpTable.findObjectByDn(cc.getZend());
                        if (nextCtp == null) {
                            getLogger().error(
                                    "can't find CrossConnect's Zend CTP:" + cc.getZend());
                        } else {
                            nextcc = cc;
                            break;
                        }
                    }
                }

            } else {
                ccs = ccTable.findObjectByIndexColumn("zend", ctp.getDn());
                if (ccs != null && !ccs.isEmpty()){
                    for (T_CCrossConnect cc : ccs){
                        if (!currentPassedCtpSet.contains(cc.getAend())
                                && !totalPassedCtpSet.contains(cc.getAend())){
                            nextCtp = ctpTable.findObjectByDn(cc.getAend());
                            if (nextCtp == null){
                                getLogger().error("can't find CrossConnect's Aend CTP:"+ cc.getAend());
                            } else {
                                nextcc = cc;
                                break;
                            }
                        }
                    }
                }
            }
            if (nextCtp != null){
                currentPassedCtpSet.add(nextCtp.getDn());
                ccList.add(nextcc);
                PathCtp pathctp = new PathCtp(nextCtp, ENTITY_TYPE_CC);
                return pathctp;
            } else {
                // no ccs , look for sections
                CChannel nextChannel = channelMap.get(ctp.getDn());
                if (nextChannel != null ) {
                    try {
                        if (ctp.getDn().equals(nextChannel.getAend())
                                && !currentPassedCtpSet.contains(nextChannel.getZend())
                                && !totalPassedCtpSet.contains(nextChannel.getZend())) {
                            nextCtp = ctpTable.findObjectByDn(nextChannel
                                    .getZend());
                        } else if (ctp.getDn().equals(nextChannel.getZend())
                                && !currentPassedCtpSet.contains(nextChannel.getAend())
                                && !totalPassedCtpSet.contains(nextChannel.getAend())) {
                            nextCtp = ctpTable.findObjectByDn(nextChannel
                                    .getAend());
                        }
                    } catch (Exception e) {
                        getLogger().error(e, e);
                    }
                    if (nextCtp != null) {
                        // getLogger().info("next Channel Ctp:" +
                        // nextChannelCtp.getDn());
                        channelList.add(nextChannel);
                        currentPassedCtpSet.add(nextCtp.getDn());
                        PathCtp pathctp = new PathCtp(nextCtp, ENTITY_TYPE_CHANNEL);
                        return pathctp;
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error(e, e);
        }
//	    ////找交叉连接的CTP完成
//		
//		////根据交叉连接找下一段Channel
//		if (crossCtp != null){
////			getLogger().info("cc crossCtp:" + crossCtp.getDn());
//			nextChannel = channnelMap.get(crossCtp.getDn());
//			if (nextChannel != null && !pathChannelMap.containsKey(nextChannel.getDn()) 
//					&& !channel_routeMap.containsKey(nextChannel.getDn())
//					&& crossCtp.getRate().equals(nextChannel.getRate())){
//				try {
//					if (crossCtp.getDn().equals(nextChannel.getAend())){	
//						nextChannelCtp = ctpTable.findObjectByDn(nextChannel.getZend());
//					} else{
//						nextChannelCtp = ctpTable.findObjectByDn(nextChannel.getAend());
//					}
//				} catch (Exception e) {
//					getLogger().error(e, e);
//				}
//				if (nextChannelCtp != null){
//	//				getLogger().info("next Channel Ctp:" + nextChannelCtp.getDn());
//					pathChannelMap.put(nextChannel.getDn(), nextChannel);
//					pathCCMap.put(cc.getDn(), cc);
//					return nextChannelCtp;
//				}
//			}
//		} 
        return null;
    }


//    /**
//     * 寻找route的下一个ctp
//     * @param ctp
//     * @param passedChannelMap，当前已经过的channel
//     * @param passedCCMap，当前已经过的cc
//     * @return
//     */
//    private T_CTP getNextRouteCtp(T_CTP ctp, HashMap<String, CChannel> passedChannelMap, HashMap<String, T_CCrossConnect> passedCCMap){
// //   	CChannel nextChannel = null;
////		List<T_CCrossConnect> ccs;
//		T_CCrossConnect nextcc = null;
//		T_CTP nextCtp = null;
////		T_CTP lastCtp = null;
//		try {
//			////找交叉连接的CTP
//			List<T_CCrossConnect> ccs = ccTable.findObjectByIndexColumn("aend", ctp.getDn());
//    		if (ccs != null && !ccs.isEmpty()){
//    			for (T_CCrossConnect cc : ccs){
//        			if (!passedCCMap.containsKey(cc.getAend()) && !passedCCMap.containsKey(cc.getZend())
//        					&& !cc_routeMap.containsKey(cc.getAend()) && !cc_routeMap.containsKey(cc.getZend())){
//        				
//            			nextCtp = ctpTable.findObjectByDn(cc.getZend());    	
//    					if (nextCtp == null) {
//    						getLogger().error(
//    								"can't find CrossConnect's Zend CTP:" + cc.getZend());
//    					} else {
//            				nextcc = cc;
//    						break;
//    					}
//        			}  				
//    			}
//
//    		} else {
//    			ccs = ccTable.findObjectByIndexColumn("zend", ctp.getDn());
//    			if (ccs != null && !ccs.isEmpty()){
//    				for (T_CCrossConnect cc : ccs){
//            			if (!passedCCMap.containsKey(cc.getAend()) && !passedCCMap.containsKey(cc.getZend())
//            					&& !cc_routeMap.containsKey(cc.getAend()) && !cc_routeMap.containsKey(cc.getZend())){
//            				
//            				nextCtp = ctpTable.findObjectByDn(cc.getAend());    	
//                			if (nextCtp == null){
//                  				 getLogger().error("can't find CrossConnect's Aend CTP:"+ cc.getAend());
//                  			    } else {
//                  			    	nextcc = cc;
//                  			    	break;
//                  			    }
//            			}     					
//    				}
//    			}
//    		} 
//    		if (nextCtp != null){
//    			passedCCMap.put(nextcc.getAend(), nextcc);
//    			passedCCMap.put(nextcc.getZend(), nextcc);
//			} else {
//				// no ccs , look for sections
//				CChannel nextChannel = lowOrderCtpChannelMap.get(ctp.getDn());
//
//				if (nextChannel != null
//						&& !passedChannelMap.containsKey(nextChannel.getAend()) && !passedChannelMap.containsKey(nextChannel.getZend())	
//						&& !channel_routeMap.containsKey(nextChannel.getAend()) && !channel_routeMap.containsKey(nextChannel.getZend())) {
//					try {
//						if (ctp.getDn().equals(nextChannel.getAend())) {
//							nextCtp = ctpTable.findObjectByDn(nextChannel
//									.getZend());
//						} else {
//							nextCtp = ctpTable.findObjectByDn(nextChannel
//									.getAend());
//						}
//					} catch (Exception e) {
//						getLogger().error(e, e);
//					}
//					if (nextCtp != null) {
//						// getLogger().info("next Channel Ctp:" +
//						// nextChannelCtp.getDn());
//						passedChannelMap.put(nextChannel.getAend(), nextChannel);
//						passedChannelMap.put(nextChannel.getZend(), nextChannel);
//					}
//				}
//			}
//    		if (nextCtp != null){
//    			return nextCtp;
//    		}
//		} catch (Exception e) {
//			 getLogger().error(e, e);
//		}
////	    ////找交叉连接的CTP完成
////		
////		////根据交叉连接找下一段Channel
////		if (crossCtp != null){
//////			getLogger().info("cc crossCtp:" + crossCtp.getDn());
////			nextChannel = channnelMap.get(crossCtp.getDn());
////			if (nextChannel != null && !pathChannelMap.containsKey(nextChannel.getDn()) 
////					&& !channel_routeMap.containsKey(nextChannel.getDn())
////					&& crossCtp.getRate().equals(nextChannel.getRate())){
////				try {
////					if (crossCtp.getDn().equals(nextChannel.getAend())){	
////						nextChannelCtp = ctpTable.findObjectByDn(nextChannel.getZend());
////					} else{
////						nextChannelCtp = ctpTable.findObjectByDn(nextChannel.getAend());
////					}
////				} catch (Exception e) {
////					getLogger().error(e, e);
////				}
////				if (nextChannelCtp != null){
////	//				getLogger().info("next Channel Ctp:" + nextChannelCtp.getDn());
////					pathChannelMap.put(nextChannel.getDn(), nextChannel);
////					pathCCMap.put(cc.getDn(), cc);
////					return nextChannelCtp;
////				}
////			}
////		} 
//		return null;
//    }

//    /**
//     * 根据一端CTP获取与其交叉连接的另外一端CTP
//     * @param ctp
//     * @return
//     */
//    private T_CTP getCrossCtp(T_CTP ctp, List<T_CCrossConnect> pathCCList) {
//    	T_CTP crossCtp = null; 
//    	String crossCtpdn = null;
//		List<T_CCrossConnect> ccs;
//		T_CCrossConnect cc = null;
//		try {
//			ccs = ccTable.findObjectByIndexColumn("aend", ctp.getDn());
//    		if (ccs != null && !ccs.isEmpty()){
//    			//认为一个ctp只有一个cc
//    			cc = ccs.get(0);
//    			crossCtpdn = cc.getZend();
//    			crossCtp = ctpTable.findObjectByDn(crossCtpdn);
//    			if (crossCtp == null){
//    				 getLogger().error("can't find CrossConnect's Zend CTP:"+ crossCtpdn);
//    			}
//    		} else {
//    			ccs = ccTable.findObjectByIndexColumn("zend", ctp.getDn());
//    			if (ccs != null && !ccs.isEmpty()){
//    				cc = ccs.get(0);
//    				crossCtpdn = cc.getAend();
//    				crossCtp = ctpTable.findObjectByDn(crossCtpdn);
//        			if (crossCtp == null){
//       				 getLogger().error("can't find CrossConnect's Aend CTP:"+ crossCtpdn);
//       			    }
//    			}    			
//    		}
//    		if (crossCtp != null){
//    			//如果两端ctp速率不同，则不往下找
//    			if (!ctp.getRate().equals(crossCtp.getRate())){
//    				crossCtp = null;
//    			} else{
//    				pathCCList.add(cc);
//    			}
//    		}
//		} catch (Exception e) {
//			 getLogger().error(e, e);
//		}
//		return crossCtp;
//	}

    /**
     *
     * @param aCtp
     * @param zCtp
     * @param channel
     */
    private CPath createCPath(T_CTP aCtp, T_CTP zCtp, CChannel channel){
        CPath route = new CPath();
        route.setDn(aCtp.getDn() + "<>" + zCtp.getDn());
        route.setSid(DatabaseUtil.nextSID(CPath.class));
        route.setName(channel.getName());
        route.setRate(channel.getRate());
        route.setRateDesc(channel.getRateDesc());
        route.setTmRate(channel.getTmRate());
        route.setCategory("HOP");

        route.setAend(aCtp.getDn());
        route.setAptp(aCtp.getPortdn());
        route.setActpId(DatabaseUtil.getSID(CCTP.class,route.getAend()));
        route.setAptpId(DatabaseUtil.getSID(CPTP.class, route.getAptp()));

        route.setZend(zCtp.getDn());
        route.setZptp(zCtp.getPortdn());
        route.setZctpId(DatabaseUtil.getSID(CCTP.class, route.getZend()));
        route.setZptpId(DatabaseUtil.getSID(CPTP.class, route.getZptp()));

        route.setDirection(channel.getDirection());
        route.setEmsName(emsdn);
        return route;
    }

    /**
     *
     * @param aCtp
     * @param zCtp
     * @param channel
     */
    private CRoute createCRoute(T_CTP aCtp, T_CTP zCtp, CChannel channel){
        CRoute route = new CRoute();
        route.setDn(aCtp.getDn() + "<>" + zCtp.getDn());
        route.setSid(DatabaseUtil.nextSID(CRoute.class));
        route.setName(channel.getName());
        route.setRate(channel.getRate());
        route.setRateDesc(channel.getRateDesc());
        route.setTmRate(channel.getTmRate());
        route.setCategory("SDHROUTE");

        route.setAend(aCtp.getDn());
        route.setAptp(aCtp.getPortdn());
        route.setActpId(DatabaseUtil.getSID(CCTP.class,route.getAend()));
        route.setAptpId(DatabaseUtil.getSID(CPTP.class, route.getAptp()));

        route.setZend(zCtp.getDn());
        route.setZptp(zCtp.getPortdn());
        route.setZctpId(DatabaseUtil.getSID(CCTP.class, route.getZend()));
        route.setZptpId(DatabaseUtil.getSID(CPTP.class, route.getZptp()));

        route.setDirection(channel.getDirection());
        route.setEmsName(emsdn);
        return route;
    }

    private void createCChannel(T_CTP aCtp, T_CTP zCtp,Object parent)  {
        String aSideCtp = aCtp.getDn();
        String zSideCtp = zCtp.getDn();
        //       String duplicateDn = (zSideCtp+"<>"+aSideCtp);
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
            //         getLogger().info("create high order channel：" + cChannel.getDn());

        }
        if (parent instanceof CPath) {
            cChannel.setCategory("SDH低阶时隙");
            cChannel.setSectionOrHigherOrderDn(((CPath)parent).getDn());
            cChannel.setDirection(((CPath)parent).getDirection());
            lowOrderCtpChannelMap.put(cChannel.getAend(),cChannel);
            lowOrderCtpChannelMap.put(cChannel.getZend(),cChannel);
            //      getLogger().info("create low order channel：" + cChannel.getDn());
        }


        cChannelList.add(cChannel);

    }

//    protected void migrateCCOld() throws Exception {
//        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
//        DataInserter di = new DataInserter(emsid);
//        try {
//            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
//            if (ccs != null && ccs.size() > 0) {
//                for (CrossConnect cc : ccs) {
//                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));
//                    CCrossConnect ccc = transCC(cc);
//                    ccc.setSid(DatabaseUtil.nextSID(CCrossConnect.class));
//                    if (ccc.getDn().length() > 240)
//                        System.out.println("ccc = " + ccc.getDn());
//                    di.insert(ccc);
//
//                    ccTable.addObject(new T_CCrossConnect(ccc));
//
//
//                    String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
//                    String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);
//
////                    makeupCTP(actps,zctps,di);
////                    makeupCTP(actps,zctps,di);
//
//                }
//            }
//        } catch (Exception e) {
//            getLogger().error(e, e);
//        } finally {
//            di.end();
//        }
//
//    }
    private List<CCrossConnect> ccList = new ArrayList<CCrossConnect>();


    private HashMap<String,List<String>> friendCtpMap = new HashMap<String, List<String>>();
    private void addCtpFriends(String[] ctps) {
        for (String ctp : ctps) {
            for (String _ctp : ctps) {
                if (ctp.equals(_ctp)) continue;
                DSUtil.putIntoValueList(friendCtpMap,ctp,_ctp);
            }
        }
    }
    protected void migrateCC() throws Exception {
      //  executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        executeTableDelete("C_CROSSCONNECT",emsdn);
        DataInserter di = new DataInserter(emsid);
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        try {
            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));

                    //如果cc两端不存在，则不迁移此cc
//                    if (!DatabaseUtil.isSIDExisted(CCTP.class, cc.getaEndNameList()) ||
//                            !DatabaseUtil.isSIDExisted(CCTP.class, cc.getzEndNameList())){
//                        continue;
//                    }
                    List<CCrossConnect> cccs = OTNM2000MigratorUtil.transCCS(cc, emsdn);
                    for (CCrossConnect ccc : cccs) {
//                        if (!DatabaseUtil.isSIDExisted(CCTP.class, ccc.getAend()) ||
//                                !DatabaseUtil.isSIDExisted(CCTP.class, ccc.getZend())){
//                            continue;
//                        }
                        newCCs.add(ccc);
                    }
            //        newCCs.addAll(cccs);
                    //                   addVC3CTP(cc);
                    addCtpInCC(cc);
//                    makeupCTP("CC", actps, zctps, di);
//                    makeupCTP("CC", zctps, actps, di);

                    String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
                    String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);
                    if (actps.length > 1) {
                        addCtpFriends(actps);
                    }
                    if (zctps.length > 1) {
                        addCtpFriends(zctps);
                    }

                }
            }

            removeDuplicateDN(newCCs);
            for (CCrossConnect ccc : newCCs) {
//                if (ccc.getId() != null)
//                    System.out.println("ccc = " + ccc);
                di.insert(ccc);
                ccTable.addObject(new T_CCrossConnect(ccc));
                ccList.add(ccc);
            }

        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

    }





//    private void addVC3CTP(CrossConnect cc) {
//        String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
//        String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);
//        if (actps == null || zctps == null){
//        	return;
//        }
//    	for (String actp : actps){
//    		if (CTPUtil.isVC3(actp)){
//    			CCvc3CtpSet.add(actp);
//    		}
//    	}
//    	for (String zctp : zctps){
//    		if (CTPUtil.isVC3(zctp)){
//    			CCvc3CtpSet.add(zctp);
//    		}
//    	}
//	}

    private void addCtpInCC(CrossConnect cc){
        String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
        String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);
        if (actps == null || zctps == null){
            return;
        }
        for (String actp : actps){
            if (!allCtpInCCSet.contains(actp)){
                allCtpInCCSet.add(actp);
            }
        }
        for (String zctp : zctps){
            if (!allCtpInCCSet.contains(zctp)){
                allCtpInCCSet.add(zctp);
            }
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
//        HashMap<String,String> lp_mac_map = new HashMap<String, String>();
//        HashMap<String,String> lp_mp_map = new HashMap<String, String>();


        List<IPCrossconnection> ipccs = sd.queryAll(IPCrossconnection.class);
        List<CMP_CTP> cmp_ctps = new ArrayList<CMP_CTP>();

        if (ipccs == null || ipccs.isEmpty()) {
            getLogger().info("IPCrossconnection 无数据 ");
            return;
        }
        HashSet<String> zpaths = new HashSet<String>();
        executeDelete("delete from CMP_CTP c where c.emsName = '"+emsdn+"'",CMP_CTP.class);
        DataInserter di = new DataInserter(emsid);
        try {
            for (IPCrossconnection es : ipccs) {
                String aend = es.getaEnd();
                String zend = es.getzEnd();

                if ((zend == null || zend.isEmpty()) && aend.contains(Constant.listSplit)) {
                    String[] split = aend.split(Constant.listSplitReg);
                    aend = split[0];
                    zend = split[1];
                }

                if (zend == null || zend.isEmpty()) {
                    getLogger().info("zend is null : ipcc dn = "+es.getDn());
                    continue;
                }

                mp_mac_map.put(zend,aend,es);

                    String transmissionParams = es.getTransmissionParams();
                    String vlanid = MigrateUtil.transMapValue(transmissionParams).get("PVID");
                    CStaticRoute cStaticRoute = new CStaticRoute();
                    cStaticRoute.setAptp(aend);
                    cStaticRoute.setAvlan(vlanid);
                    cStaticRoute.setZptp(zend);
                    cStaticRoute.setZvlan(vlanid);
                    cStaticRoute.setDn(es.getDn());
                    cStaticRoute.setEmsName(emsdn);
                    di.insert(cStaticRoute);


                String ftpDn = DNUtil.extractPortDn(zend);
                getLogger().info(zend+"extract ftpdn = "+ftpDn);
                List<T_CCrossConnect> ccs = ccTable.findObjects
                        (new Condition("aend", "like", ftpDn+"").or(new Condition("zend", "like", ftpDn+""))
                                );

                getLogger().info("find ccs = "+ccs.size());
                for (T_CCrossConnect cc : ccs) {
                    String otherCtp = null;
                    if (cc.getAend().endsWith(ftpDn) || cc.getAend().contains(ftpDn+"@CTP") || cc.getAend().contains(ftpDn+"||")
                   || cc.getZend().endsWith(ftpDn) || cc.getZend().contains(ftpDn+"@CTP") || cc.getZend().contains(ftpDn+"||")
                            ) {
                       // System.out.println();
                    }
                    else {
                        getLogger().info("invalid cc : "+cc.getDn());
                        continue;
                    }

                    if (cc.getAend().contains(ftpDn+"")) {
                        otherCtp = cc.getZend();
                    } else {
                        otherCtp = cc.getAend();
                    }




                    CMP_CTP cmp_ctp = new CMP_CTP();
                    cmp_ctp.setCtpDn(otherCtp);
                    cmp_ctp.setPtpDn(ftpDn);

                    cmp_ctp.setIsUsed(1);
                    cmp_ctp.setDn(ftpDn + "<>" + otherCtp);
                    cmp_ctp.setEmsName(emsdn);
                    cmp_ctps.add(cmp_ctp);


                    putIntoList(mp_ctps_map,ftpDn,otherCtp);
                    ctp_mp_map.put(otherCtp, ftpDn);

                }
                //      mp_mac_map.put(mp,mac,es);


            }



//            if (vlans != null) {
//                for (HW_VirtualLAN vlan : vlans) {
//                    String forwardTPList = vlan.getForwardTPList();
//
//
//                }
//            }


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
                       //     cEthTrunk.setDirection(((IPCrossconnection) (mp_mac_map.get(zmpDn, 1))).getDirection());
                            cEthTrunk.setName(((IPCrossconnection) (mp_mac_map.get(zmpDn, 1))).getNativeEMSName());
                            //   cEthTrunk.sett
                            di.insert(cEthTrunk);


                            CEthRoute cEthRoute = new CEthRoute();
                            cEthRoute.setEmsName(emsdn);
                            cEthRoute.setName(((IPCrossconnection) (mp_mac_map.get(zmpDn, 1))).getNativeEMSName());
                            cEthRoute.setTmRate(bandwidh+"M");
                            cEthRoute.setDn(amac + "<>" + zmac);
                            cEthRoute.setAptp(amac);
                            cEthRoute.setZptp(zmac);
                            //           cEthRoute.setRate();
                            cEthRoute.setAptpId(DatabaseUtil.getSID(CPTP.class, amac));
                            cEthRoute.setZptpId(DatabaseUtil.getSID(CPTP.class, zmac));
                            cEthRoute.setDirection(((IPCrossconnection)(mp_mac_map.get(zmpDn,1))).getDirection());
                            di.insert(cEthRoute);

                            CEthRoute_ETHTrunk cEthRoute_ethTrunk = new CEthRoute_ETHTrunk();
                            cEthRoute_ethTrunk.setEthTrunkDn(cEthTrunk.getDn());
                            cEthRoute_ethTrunk.setEthRouteDn(cEthRoute.getDn());
                            cEthRoute_ethTrunk.setEthTrunkId(cEthTrunk.getSid());
                            cEthRoute_ethTrunk.setEthRouteId(cEthRoute.getSid());
                            cEthRoute_ethTrunk.setDn(SysUtil.nextDN());
                            cEthRoute_ethTrunk.setEmsName(emsdn);
                            di.insert(cEthRoute_ethTrunk);

                            IPCrossconnection staticRoute1 = (IPCrossconnection) mp_mac_map.get(mp, 1);
                            IPCrossconnection staticRoute2 = (IPCrossconnection) mp_mac_map.get(zmpDn, 1);
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


    public static void truncateTable(JPASupport ctx,final String sql) throws SQLException {
        EntityManager entityManager = ctx.getEntityManager();

        HibernateEntityManager hibernateEntityManager = (HibernateEntityManager)entityManager;
//        entityManager.getTransaction().begin();
        hibernateEntityManager.getSession().doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                int i = connection.prepareStatement(sql).executeUpdate();
                System.out.print(i);

            }
        });

    }

    public static void main(String[] args) throws Exception {
        String fileName=  "F:\\cdcpdb\\sdh0-mt.db";
        String emsdn = "SHX-OTNM2000-8-P";
        if (args != null && args.length > 0)
            fileName = args[0];
        if (args != null && args.length > 1)
            emsdn = args[1];
        boolean b = true;
        String[] split = "abc".split(Constant.listSplitReg);
        // int length = "[EMS:NBO-T2000-10-P@ManagedElement:598826@PTP:/rack=1/shelf=1/slot=11/domain=sdh/port=1@CTP:/sts3c_au4-j=3/vt2_tu12-k=3-l=3-m=3] ".length();
        String[] locations = { "appserver-spring.xml" };
        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
        JPASupportSpringImpl context = new JPASupportSpringImpl("entityManagerFactoryData");
        try
        {
            context.begin();
            String sql = "delete from c_ctp where emsname='ZJ-U2000-1-OTN' and rownum < 10000";
            truncateTable(context,sql);
//            String[] preLoadSqls = Constants.PRE_LOAD_SQLS;
//            for (String sql : preLoadSqls) {
//                DBUtil.getInstance().executeNonSelectingSQL(context,sql);
//            }
            context.end();
        } catch (Exception ex) {
            context.rollback();
            throw ex;
        } finally {
            context.release();
        }

        FHOTNM2000OTN3Migrator loader = new FHOTNM2000OTN3Migrator (fileName, emsdn){
            public void afterExecute() {
                printTableStat();
            }
        };
        loader.execute();


    }


}
