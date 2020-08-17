package com.alcatelsbell.cdcp.server.adapters.alu;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.nodefx.NEWrapper;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.CTPUtil;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.server.adapters.SDHRouteComputationUnit;
import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CCrossConnect;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CRoute;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CTP;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWDic;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HwDwdmUtil;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.cronjob.Detect;
import com.alcatelsbell.nms.db.components.service.DBUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.valueobject.BObject;

import org.apache.commons.lang.StringUtils;
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
    HashMap<String,String> deviceNameMap = new HashMap<String, String>();
    HashMap<String,HashMap<String, String>> deviceCardMap = new HashMap<String,HashMap<String, String>>();
    
    // SDH的全局变量
    private BObjectMemTable<T_CCrossConnect> ccTable = new BObjectMemTable(T_CCrossConnect.class,"aend","zend");
    private BObjectMemTable<T_CTP> ctpTable = new BObjectMemTable(T_CTP.class,"portdn","parentCtp","ratedesc");
    private BObjectMemTable<T_CRoute> cRouteTable = new BObjectMemTable(T_CRoute.class);

    private HashMap<String,CChannel> highOrderCtpChannelMap = new HashMap<String, CChannel>();
    private HashMap<String,CChannel> lowOrderCtpChannelMap = new HashMap<String, CChannel>();
    private HashMap<String,List<CChannel>> sectionChannelMap = new HashMap<String, List<CChannel>>();
    private List<CChannel> cChannelList =  new ArrayList<CChannel>();
    
    HashMap<String,SubnetworkConnection> sncMap = new HashMap<String, SubnetworkConnection>();
    
//    private List<CSection> cSections = new ArrayList<CSection>();
//    HashMap<String,CSection> sdhSectionMap = new HashMap<String, CSection>();
//    private HashMap<String,List<CChannel>> vc4ChannelMap = new HashMap<String, List<CChannel>>();

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

        // 0.通用实体入库
     	try {
     		logAction(emsdn + " migrateManagedElement", "同步网元", 1);
            migrateManagedElement();
            migrateSubnetwork();

            logAction("migrateEquipmentHolder", "同步槽道", 5);
            migrateEquipmentHolder();

            logAction("migrateEquipment", "同步板卡", 10);
            migrateEquipment();
            
            logAction("migratePTP", "同步端口", 20);
            migratePTP();

     	} catch (Exception e) {
            getLogger().error(e, e);
        }

        // SDH/OTN
        logAction("migrateCTP", "同步CTP", 25);
        migrateCTP();
        
        logAction("migrateSection", "同步段", 25);
        migrateSection();

        logAction("migrateCC", "同步交叉", 25);
        migrateCC();

        logAction("migrateOMS", "同步SNC", 25);
        migrateSNC();
        
        cSections.removeAll(cSections);
		ctpTable.removeAll();
		ccTable.removeAll();
		cRouteTable.removeAll();
		cChannelList.removeAll(cChannelList);
		highOrderCtpChannelMap.clear();
		lowOrderCtpChannelMap.clear();
		sectionChannelMap.clear();
		cChannelList.clear();
		deviceCardMap.clear();
		deviceNameMap.clear();
//		vc4ChannelMap.clear();
		
		ctpTable = null;
		ccTable = null;
		cRouteTable = null;
		cChannelList = null;
		highOrderCtpChannelMap = null;
		lowOrderCtpChannelMap = null;
		sectionChannelMap = null;
		deviceCardMap = null;
		deviceNameMap = null;
//		vc4ChannelMap = null;
        
        // PTN先不处理
//        logAction("migrateFTPPTP", "同步逻辑端口", 35);
//		migrateFTPPTP();
//
//		logAction("migrateFlowDomainFragment", "同步业务", 40);
//		migrateFlowDomainFragment();
//
//		logAction("migrateRoute", "同步路由", 70);
//		migrateIPRoute();
//
//		logAction("migrateProtectGroup", "同步保护组", 85);
//		migrateProtectGroup();
//		 MigrateUtil.checkRoute(sd);
//		logAction("migrateProtectingPWTunnel", "同步保护组", 95);
//		migrateProtectingPWTunnel();
		
		
		getLogger().info("同步结束-release");

        
//        executeNativeSql("update c_ptp p set p.speed = '100G' where p.emsName = '"+emsdn+"' and exists (select c.* from c_ctp c where c.portdn = p.dn and c.tmrate = '100G')");

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
    public CDevice transDevice(ManagedElement me) {
    	CDevice device = super.transDevice(me);
    	deviceNameMap.put(device.getDn(), device.getNativeEmsName());
    	return device;
    }
    
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
            else if (equipmentHolder.getHolderType().equals("sub_slot")) {
				subslots.add(equipmentHolder);
			}
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
//        if (!cEquipment.getDn().contains("slot")) return null;

        // 填板卡的型号
        cEquipment.setNativeEMSName(equipment.getInstalledEquipmentObjectType());
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
        
        String deviceName = deviceNameMap.get(equipment.getParentDn());
        if (!Detect.notEmpty(deviceName)) {
        	deviceName = deviceNameMap.get(StringUtils.substringBefore(equipment.getDn(), "@EquipmentHolder"));
        }
        if (Detect.notEmpty(deviceName)) {
        	HashMap<String,String> cardNameMap = deviceCardMap.get(deviceName);
        	if (Detect.notEmpty(cardNameMap)) {
        		cardNameMap.put(cEquipment.getNativeEMSName(), cEquipment.getDn());
        	} else {
        		cardNameMap = new HashMap<String,String>();
        		cardNameMap.put(cEquipment.getNativeEMSName(), cEquipment.getDn());
        		deviceCardMap.put(deviceName, cardNameMap);
        	}
        } else {
        	getLogger().error("Faild to find cardDeviceName: " + cEquipment.getDn());
        }

        return cEquipment;
    }

    @Override
    public CCTP transCTP(CTP ctp) {
        CCTP cctp = super.transCTP(ctp);
        
        cctp.setPortdn(ctp.getParentDn());
        cctp.setTmRate(ALUDicUtil.getTMRate(ctp.getRate()));
        if (ctp.getParentDn().contains("@FTP")) {
            cctp.setDn(cctp.getDn().replace("@PTP","@FTP"));
        }
        if (cctp.getNativeEMSName() == null || cctp.getNativeEMSName().isEmpty()) {
            cctp.setNativeEMSName(ctp.getDn().substring(ctp.getDn().indexOf("CTP:/")+5));
        }
        if ("28".equals(ctp.getRate())) { // 层速率28是“LR_Line_OC192_STS192_and_MS_STM64	STM-64 multiplex section	VC64”
        	cctp.setRateDesc("10GE");
        	cctp.setTmRate("10G");
        }
        
        String additionalInfo = cctp.getAdditionalInfo();
        if (additionalInfo.length() > 2000)
            cctp.setAdditionalInfo(additionalInfo.substring(0, 2000));
        String transmissionParams = cctp.getTransmissionParams();
        if (transmissionParams.length() > 2000)
            cctp.setTransmissionParams(transmissionParams.substring(0, 2000));
        
        cctp.setPortdn(cctp.getParentDn());
        DatabaseUtil.getSID(CPTP.class,cctp.getParentDn());
        
//        cctp.setParentCtpdn(DNUtil.getParentCTPdn(ctp.getDn()));
        
        // VC4/VC12解析JKLM
		if (StringUtils.contains(cctp.getRateDesc(), "VC")) {
			cctp.setJ(String.valueOf(this.getJ(ctp.getDn())));
			if (StringUtils.equals(cctp.getRateDesc(), "VC3") || StringUtils.equals(cctp.getRateDesc(), "VC12")) {
				cctp.setK(String.valueOf(this.getK(ctp.getDn())));
				if (StringUtils.equals(cctp.getRateDesc(), "VC12")) {
					cctp.setL(String.valueOf(this.getL(ctp.getDn())));
					cctp.setM(String.valueOf(this.getM(ctp.getDn())));
					cctp.setParentCtpdn(getParentCTPdn(ctp.getDn()));
				}
			}
		}
        
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
            for (CCrossConnect ccc : newCCs) {
//                if (ccc.getId() != null)
//                    System.out.println("ccc = " + ccc);
//                di.insert(ccc);
                ccTable.addObject(new T_CCrossConnect(ccc));
            }
            di.insert(newCCs);
            
            ccs.clear();
            newCCs.clear();

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
                    if (cctp.getTmRate() != null && cctp.getTmRate().equals("100G")) {
                        String parentCtpdn = cctp.getParentCtpdn();
                        CCTP parentCtp = ctpMap.get(parentCtpdn);
                        if (parentCtp != null)
                            parentCtp.setTmRate("100G");
                    }
                    
                    ctpMap.put(cctp.getDn(), cctp);
                    ctpTable.addObject(new T_CTP(cctp));
                }
            }
        }

        di.insert(cctps);
        di.end();

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
        }
        cptp.setEoType(DicUtil.getEOType(cptp.getDn(),cptp.getLayerRates()));
        if ("OPTICAL".equalsIgnoreCase(cptp.getType())) {
        	cptp.setEoType(1);
        }
        cptp.setTag3(ptp.getId() + "");
        if (cptp.getEoType() == DicConst.EOTYPE_ELECTRIC && "OPTICAL".equals(cptp.getType()))
            cptp.setType("ELECTRICAL");
        ptpMap.put(cptp.getDn() ,cptp);
        
        if (Detect.notEmpty(deviceCardMap)) {
        	String suffix = StringUtils.substringAfter(ptp.getDn(), "@PTP:");//EQX-TSS-01/PP10AD-1-1-17-30
        	String[] names = StringUtils.split(suffix, "/");
        	if (Detect.notEmpty(names) && names.length == 2) {
        		String deviceName = names[0];
    	        String cardName = StringUtils.substringBeforeLast(names[1], "-");
    	        HashMap<String, String> cardMap = deviceCardMap.get(deviceName);
    	        if (Detect.notEmpty(cardMap)) {
    	        	String cardDn = cardMap.get(cardName);
    	        	if (Detect.notEmpty(cardDn)) {
    	        		cptp.setParentDn(cardDn);
    	        	} else {
    	        		getLogger().error("ptp dn get no card: " + ptp.getDn());
    	        	}
    	        } else {
    	        	getLogger().error("ptp dn get no device: " + ptp.getDn());
    	        }
        	} else {
        		getLogger().error("ptp dn is wrong: " + ptp.getDn());
        	}
        } else {
        	getLogger().error("deviceCardMap is null!");
        }
        
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


    @Override
    protected void migrateSection() throws Exception {
    	if (!isTableHasData(Section.class))
			return;
		executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "'", CSection.class);
		DataInserter di = new DataInserter(emsid);
		List<Section> sections = sd.queryAll(Section.class);
		List<CSection> cSections = new ArrayList<CSection>();
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
				di.insert(csection);
				cSections.add(csection);
			}
		}
		di.end();

		breakupSections(cSections);
    }

    List<CSection> omsList = new ArrayList<CSection>();

    @Override
    public CSection transSection(Section section) {
        CSection cSection = super.transSection(section);
        cSection.setType("SdhSection");
//        cSection.setSpeed("100G");
        String additionalInfo = cSection.getAdditionalInfo();
        if (additionalInfo.length() > 1500)
        	cSection.setAdditionalInfo(additionalInfo.substring(0, 1500));
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
//        String rate = section.getRate();

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
//            List<SubnetworkConnection> sncs = sd.query("select c from SubnetworkConnection c where dn like '%-sdh%' ");
            //   sncTable.addObjects(sncs);
            final HashMap<String,List<R_TrafficTrunk_CC_Section>> snc_cc_section_map = new HashMap<String, List<R_TrafficTrunk_CC_Section>>();
            List<R_TrafficTrunk_CC_Section> routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
//            List<R_TrafficTrunk_CC_Section> routeList = sd.query("select c from R_TrafficTrunk_CC_Section c where aEnd like '%domain=sdh%'");
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
            	sncMap.put(snc.getDn(), snc);
            	
                if ((HWDic.LR_E1_2M.value+"").equals(snc.getRate()) 
                		|| (HWDic.LR_VT2_and_TU12_VC12.value+"").equals(snc.getRate())//11
                		|| (HWDic.LR_DSR_10Gigabit_Ethernet.value+"").equals(snc.getRate())//113
                		|| (HWDic.LR_Ethernet.value+"").equals(snc.getRate())) {//96
                    sdhRoutes.add(snc);
                }  else if ((HWDic.LR_STS3c_and_AU4_VC4.value+"").equals(snc.getRate())) {//15
                    paths.add(snc);
                }  else if ((HWDic.LR_E3_34M.value+"").equals(snc.getRate())
                		|| (HWDic.LR_T3_and_DS3_45M.value+"").equals(snc.getRate())) {
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
                    	e4Routes.add(snc);
                } else {
                	System.out.println("SNC Unknown rate : "+snc.getRate()+"; snc="+snc.getDn());
                    getLogger().error("SNC Unknown rate : "+snc.getRate()+"; snc="+snc.getDn());
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
                    CPath cPath = U2000MigratorUtil.transPath(emsdn,snc);
                    cpaths.add(cPath);
                    cpathMap.put(cPath.getDn(), cPath);

                    breakupCPaths(cPath);

                    List<R_TrafficTrunk_CC_Section> routes = snc_cc_section_map.get(snc.getDn());
                    if (routes == null) {
                        noRoutePath ++;
                        continue;
                    }
                    SDHRouteComputationUnit computationUnit = new SDHRouteComputationUnit(getLogger(),snc,routes,ctpTable,ccTable,emsdn,true,null);
                    computationUnit.setACtpChannelMap(highOrderCtpChannelMap);
                    computationUnit.compute();
                    ctpDnHighoderpathDn.putAll(computationUnit.getCtpDnHighoderpathDn());
                    List<CChannel> channels = computationUnit.getChannels();

                    for (CChannel channel : channels) {
                        cPath_channels.add(U2000MigratorUtil.createCPath_Channel(emsdn, channel, cPath));
                    }

                    cPath_ChannelMap.put(cPath.getDn(),channels);
                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {
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
                    cRoute.setTag1(MigrateUtil.transMapValue(snc.getAdditionalInfo()).get("Customer"));
                    cRoutes.add(cRoute);
                    cRouteTable.addObject(new T_CRoute(cRoute)); 

                    List<R_TrafficTrunk_CC_Section> routes = snc_cc_section_map.get(snc.getDn());
                    if (routes == null) {
                        noRoutePath ++;
                        continue;
                    }
                    SDHRouteComputationUnit computationUnit = new SDHRouteComputationUnit(getLogger(),snc,routes,ctpTable,ccTable,emsdn,true,null);
                    computationUnit.setACtpChannelMap(highOrderCtpChannelMap);
                    computationUnit.compute();
                    ctpDnHighoderpathDn.putAll(computationUnit.getCtpDnHighoderpathDn());
                    List<CChannel> channels = computationUnit.getChannels();

                    for (CChannel channel : channels) {
                        cRoute_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, channel, cRoute));
                    }


                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {
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

            for (SubnetworkConnection snc : sdhRoutes) {
                try {
                    CRoute cRoute = U2000MigratorUtil.transRoute(emsdn,snc);
                    cRoute.setTag1(MigrateUtil.transMapValue(snc.getAdditionalInfo()).get("Customer"));

                    List<R_TrafficTrunk_CC_Section> routes = snc_cc_section_map.get(snc.getDn());
                    if (routes == null) {
                        noRouteRoute ++;
                        continue;
                    }
                    SDHRouteComputationUnit computationUnit = new SDHRouteComputationUnit(getLogger(),snc,routes,ctpTable,ccTable,emsdn,false,cpathMap);
                    computationUnit.setCtpDnHighoderpathDn(ctpDnHighoderpathDn);
                    computationUnit.setACtpChannelMap(lowOrderCtpChannelMap);
                    computationUnit.compute();
                    List<CChannel> lowOrderChannels = computationUnit.getChannels();
                    removeDuplicateDN(lowOrderChannels);

                    cRouteTable.addObject(new T_CRoute(cRoute));
                    cRoutes.add(cRoute);

                    for (CChannel channel : lowOrderChannels) {
                        cRoute_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, channel, cRoute));
                    }
                    for (R_TrafficTrunk_CC_Section route : routes) {
                        if (route.getType().equals("CC")) {
                            Collection<? extends String> ccs = splitCCdns(route.getCcOrSectionDn());
                            for (String cc : ccs) {
                                cRoute_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, cc, cRoute));
                            }
                        }
                    }
                    
                    if (!Detect.notEmpty(lowOrderChannels)) {
                    	String sectionDn = this.getSectionDn(snc, 0);
                    	if (Detect.notEmpty(sectionDn)) {
                    		List<CChannel> channels = sectionChannelMap.get(sectionDn);
                    		if (Detect.notEmpty(channels)) {
                    			for (CChannel channel : channels) {
                                    cRoute_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, channel, cRoute));
                                }
                    		}
                    	} else {
                    		getLogger().error("cannot find sectionDn: " + snc.getDn());
                    	}
                    	
//                    	List<CChannel> channels = sectionChannelMap.get(key);
                    }
                    
                } catch (Exception e) {
                    getLogger().error("Process Route Error "+e, e);
                }
            }
            sdhRoutes.clear();

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
            
            routeList.clear();
            sncs.clear();
            paths.clear();
            e4Routes.clear();
            
            cRoutes.clear();
            cRoute_ccs.clear();
            cRoute_channels.clear();
            cpaths.clear();
            cPath_ccs.clear();
            cPath_channels.clear();
            
        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {

        }
	}
	
	public String getSectionDn(SubnetworkConnection snc, int count) {
		String serverIDs = MigrateUtil.transMapValue(snc.getAdditionalInfo()).get("serverIDs");
    	if (serverIDs.startsWith("TRAIL_")) {
    		String sncDn = "EMS:ALU/Presentation1@MultiLayerSubnetwork:SDH@SubnetworkConnection:" + serverIDs;
    		if (count > 5) {
    			return null;
    		} else {
    			count++;
    			SubnetworkConnection newSnc = sncMap.get(sncDn);
    			return getSectionDn(newSnc, count);
    		}
    	} else if (serverIDs.startsWith("CONNECT_")){
    		String sectionDn = "EMS:ALU/Presentation1@TopologicalLink:" + serverIDs;
    		return sectionDn;
    	} else {
    		getLogger().error("unknown serverID starts... ");
			return null;
    	}
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
                if ("VC4".equals(actp.getRateDesc())) {
                    int j = getJ(actp.getDn());
//                    String aj = StringUtils.substring(StringUtils.removeEnd(actp.getNativeEMSName(), " cap"), actp.getNativeEMSName().length()-2);
                    for (T_CTP zctp : zctps) {
                        if ("VC4".equals(zctp.getRateDesc()) && (getJ(zctp.getDn()) == j)) {
//                        	String zj =  StringUtils.substring(StringUtils.removeEnd(zctp.getNativeEMSName(), " cap"), zctp.getNativeEMSName().length()-2);
//                        	if (Detect.notEmpty(aj) && aj.equals(zj)) {
                        		createCChannel(actp,zctp,section);
//                        	}
                        }
                    }
                }
            }
        }

    }

    private void createCChannel(T_CTP aCtp, T_CTP zCtp,Object parent)  {
        String nativeEMSName = null;
        String rate = null;
        if (aCtp != null) {
            nativeEMSName = aCtp.getNativeEMSName();
            rate = aCtp.getRate();
        } else if (zCtp != null) {
            nativeEMSName = zCtp.getNativeEMSName();
            rate = zCtp.getRate();
        }
        String aSideCtp = aCtp.getDn();
        String zSideCtp = zCtp.getDn();
        String duplicateDn = (zSideCtp+"<>"+aSideCtp);
//      if (channelMap.get(duplicateDn)!= null)
//      return;
        
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

        cChannel.setAptp(aCtp.getPortdn());
        cChannel.setZptp(aCtp.getPortdn());
        cChannel.setEmsName(emsdn);
        if (parent instanceof CSection) {
            cChannel.setCategory("SDH高阶时隙");
            cChannel.setDirection(((CSection)parent).getDirection());
            cChannel.setSectionOrHigherOrderDn(((CSection)parent).getDn());
            highOrderCtpChannelMap.put(cChannel.getAend(), cChannel);
            highOrderCtpChannelMap.put(cChannel.getZend(), cChannel);
            
            DSUtil.putIntoValueList(sectionChannelMap, ((CSection) parent).getDn(),cChannel);
//            DSUtil.putIntoValueList(vc4ChannelMap, ((CSection) parent).getDn(),cChannel);
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
    
    private void breakupCPaths(CPath path) throws Exception {
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
                	T_CTP actp = ctpTable.findObjectByDn(aend);
                	T_CTP zctp = ctpTable.findObjectByDn(zend);
                    if ("VC4".equals(actp.getRateDesc()) && "VC4".equals(zctp.getRateDesc())) {

                        try {
                            List<T_CTP> achildCtps = ctpTable.findObjectByIndexColumn("parentCtp", aend);
                            List<T_CTP> zchildCtps = ctpTable.findObjectByIndexColumn("parentCtp", zend);
                            for (T_CTP achildCtp : achildCtps) {
                                for (T_CTP zchildCtp : zchildCtps) {
                                    String asimpleName = DNUtil.extractCTPSimpleName(achildCtp.getDn());
                                    if (asimpleName.contains(" ") && !asimpleName.endsWith(" "))
                                        asimpleName = StringUtils.substringAfterLast(asimpleName, " ");
                                    String zsimpleName = DNUtil.extractCTPSimpleName(zchildCtp.getDn());
                                    if (zsimpleName.contains(" ") && !zsimpleName.endsWith(" "))
                                    	zsimpleName = StringUtils.substringAfterLast(zsimpleName, " ");
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
    
    public int getJ(String dn) {
		try {
			String newDn = StringUtils.removeEnd(dn, " cap");
			String suffix = StringUtils.substringAfterLast(newDn, " ");
			if (!Detect.notEmpty(suffix)) {
				getLogger().error("无法解析J的CTPdn：" + dn);
				return -1;
			}
			suffix = StringUtils.substringBefore(suffix, "/");
			return Integer.parseInt(suffix);
		} catch (NumberFormatException e) {
			getLogger().error("Error getJ : dn=" + dn);
			getLogger().error(e, e);
			return -1;
		}
	}

	public int getK(String dn) {
		try {
			String newDn = StringUtils.removeEnd(dn, " cap");
			String suffix = StringUtils.substringAfterLast(newDn, " ");
			if (!Detect.notEmpty(suffix)) {
				getLogger().error("无法解析K的CTPdn：" + dn);
				return -1;
			}
			String newSuffix = StringUtils.substringAfter(suffix, "/");
			if (!Detect.notEmpty(newSuffix)) {
				getLogger().error("无法解析K的CTPdn2：" + dn);
				return -1;
			}
			String k = StringUtils.substringBefore(newSuffix, "/");
			return Integer.parseInt(k);
		} catch (NumberFormatException e) {
			getLogger().error("Error getK : dn=" + dn);
			getLogger().error(e, e);
			return -1;
		}
	}

	public int getL(String dn) {
		try {
			String newDn = StringUtils.removeEnd(dn, " cap");
			String suffix = StringUtils.substringAfterLast(newDn, " ");
			if (!Detect.notEmpty(suffix)) {
				getLogger().error("无法解析L的CTPdn：" + dn);
				return -1;
			}
			String newSuffix = StringUtils.substringAfter(suffix, "/");
			if (!Detect.notEmpty(newSuffix)) {
				getLogger().error("无法解析L的CTPdn2：" + dn);
				return -1;
			}
			String lm = StringUtils.substringAfter(newSuffix, "/");
			if (!Detect.notEmpty(lm)) {
				getLogger().error("无法解析L的CTPdn3：" + dn);
				return -1;
			}
			String l = StringUtils.substringBefore(lm, ".");
			return Integer.parseInt(l);
		} catch (NumberFormatException e) {
			getLogger().error("Error getL : dn=" + dn);
			getLogger().error(e, e);
			return -1;
		}
	}

	public int getM(String dn) {
		try {
			String newDn = StringUtils.removeEnd(dn, " cap");
			String suffix = StringUtils.substringAfterLast(newDn, " ");
			if (!Detect.notEmpty(suffix)) {
				getLogger().error("无法解析L的CTPdn：" + dn);
				return -1;
			}
			String newSuffix = StringUtils.substringAfter(suffix, "/");
			if (!Detect.notEmpty(newSuffix)) {
				getLogger().error("无法解析L的CTPdn2：" + dn);
				return -1;
			}
			String lm = StringUtils.substringAfter(newSuffix, "/");
			if (!Detect.notEmpty(lm)) {
				getLogger().error("无法解析L的CTPdn3：" + dn);
				return -1;
			}
			String m = StringUtils.substringAfter(lm, ".");
			return Integer.parseInt(m);
		} catch (NumberFormatException e) {
			getLogger().error("Error getL : dn=" + dn);
			getLogger().error(e, e);
			return -1;
		}
	}
	
	public String getParentCTPdn(String ctpDn) {
		String newDn = StringUtils.removeEnd(ctpDn, " cap");
		String suffix = StringUtils.substringAfterLast(newDn, " ");
		if (!Detect.notEmpty(suffix)) {
			getLogger().error("无法解析ParentCTP的CTPdn：" + ctpDn);
			return null;
		}
		String newSuffix = StringUtils.substringBefore(suffix, "/");
		if (!Detect.notEmpty(newSuffix)) {
			getLogger().error("无法解析ParentCTP的CTPdn2：" + ctpDn);
			return null;
		}
		String start = StringUtils.substringBeforeLast(newDn, " ");
		return start + " " + newSuffix;
    }


    public static void main(String[] args) throws Exception {
//        FileReader fr = new FileReader("c:\\1.txt");
//        BufferedReader br = new BufferedReader(fr);
//        String s = br.readLine();
//        String[] split = s.split(" ");
//        for (String s1 : split) {
//            System.out.println(s1);
//        }

        String fileName=  "D:\\ALU-Presentation1-mt.db";
        String emsdn = "ALU/Presentation1";
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
