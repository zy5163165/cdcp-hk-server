package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;

import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.EquipmentHolder;
import org.asb.mule.probe.framework.entity.FlowDomainFragment;
import org.asb.mule.probe.framework.entity.HW_EthService;
import org.asb.mule.probe.framework.entity.HW_MSTPBindingPath;
import org.asb.mule.probe.framework.entity.HW_VirtualBridge;
import org.asb.mule.probe.framework.entity.HW_VirtualLAN;
import org.asb.mule.probe.framework.entity.ManagedElement;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.entity.PWTrail;
import org.asb.mule.probe.framework.entity.ProtectionSubnetwork;
import org.asb.mule.probe.framework.entity.ProtectionSubnetworkLink;
import org.asb.mule.probe.framework.entity.R_TrafficTrunk_CC_Section;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.entity.TrafficTrunk;
import org.asb.mule.probe.framework.service.Constant;
import org.asb.mule.probe.framework.util.FileLogger;
import org.asb.mule.probe.ptn.u2000V16.nbi.job.CTPUtil;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.jdbc.Work;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.CCTP;
import com.alcatelsbell.cdcp.nbi.model.CChannel;
import com.alcatelsbell.cdcp.nbi.model.CCrossConnect;
import com.alcatelsbell.cdcp.nbi.model.CDevice;
import com.alcatelsbell.cdcp.nbi.model.CEquipment;
import com.alcatelsbell.cdcp.nbi.model.CEthRoute;
import com.alcatelsbell.cdcp.nbi.model.CEthRoute_ETHTrunk;
import com.alcatelsbell.cdcp.nbi.model.CEthRoute_StaticRoute;
import com.alcatelsbell.cdcp.nbi.model.CEthTrunk;
import com.alcatelsbell.cdcp.nbi.model.CEthTrunk_SDHRoute;
import com.alcatelsbell.cdcp.nbi.model.CIPAddress;
import com.alcatelsbell.cdcp.nbi.model.CMP_CTP;
import com.alcatelsbell.cdcp.nbi.model.COMS_CC;
import com.alcatelsbell.cdcp.nbi.model.COMS_Section;
import com.alcatelsbell.cdcp.nbi.model.CPTP;
import com.alcatelsbell.cdcp.nbi.model.CPW;
import com.alcatelsbell.cdcp.nbi.model.CPWE3;
import com.alcatelsbell.cdcp.nbi.model.CPWE3_PW;
import com.alcatelsbell.cdcp.nbi.model.CPW_Tunnel;
import com.alcatelsbell.cdcp.nbi.model.CPath;
import com.alcatelsbell.cdcp.nbi.model.CPath_CC;
import com.alcatelsbell.cdcp.nbi.model.CPath_Channel;
import com.alcatelsbell.cdcp.nbi.model.CPath_Section;
import com.alcatelsbell.cdcp.nbi.model.CRack;
import com.alcatelsbell.cdcp.nbi.model.CRoute;
import com.alcatelsbell.cdcp.nbi.model.CRoute_CC;
import com.alcatelsbell.cdcp.nbi.model.CRoute_Channel;
import com.alcatelsbell.cdcp.nbi.model.CRoute_Section;
import com.alcatelsbell.cdcp.nbi.model.CSection;
import com.alcatelsbell.cdcp.nbi.model.CShelf;
import com.alcatelsbell.cdcp.nbi.model.CSlot;
import com.alcatelsbell.cdcp.nbi.model.CStaticRoute;
import com.alcatelsbell.cdcp.nbi.model.CSubnetwork;
import com.alcatelsbell.cdcp.nbi.model.CSubnetworkDevice;
import com.alcatelsbell.cdcp.nbi.model.CTransmissionSystem;
import com.alcatelsbell.cdcp.nbi.model.CTransmissionSystem_Channel;
import com.alcatelsbell.cdcp.nbi.model.CTunnel;
import com.alcatelsbell.cdcp.nbi.model.CTunnel_Section;
import com.alcatelsbell.cdcp.nbi.model.CVirtualBridge;
import com.alcatelsbell.cdcp.nbi.model.CdcpObject;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.server.adapters.AbstractDBFLoader;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CCrossConnect;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CRoute;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CTP;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.server.adapters.PathFindAlgorithm;
import com.alcatelsbell.cdcp.server.adapters.SDHRouteComputationUnit;
import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.util.BObjectMemTable;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DSUtil;
import com.alcatelsbell.cdcp.util.DataInserter;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import com.alcatelsbell.cdcp.util.DicUtil;
import com.alcatelsbell.cdcp.util.MemTable.Condition;
import com.alcatelsbell.cdcp.util.MigrateThread;
import com.alcatelsbell.cdcp.util.MultiValueMap;
import com.alcatelsbell.nms.common.Detect;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.DBUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import com.alcatelsbell.nms.valueobject.BObject;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-7
 * Time: 上午11:14
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HWU2000SDHMigrator  extends AbstractDBFLoader {

    public HWU2000SDHMigrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog("HWSDH_"+emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }

    private static FileLogger fLogger = new FileLogger("U2000-SDH-Device.log");

    public HWU2000SDHMigrator(Serializable object, String emsdn) {
        this.emsdn = emsdn;
        this.resultObject = object;
        MigrateThread.thread().initLog(fLogger);
    }

	protected Class[] getStatClss() {
		return new Class[] { CCrossConnect.class, CChannel.class, CPath.class, CRoute.class, CPath_Channel.class,
				CPath_CC.class, CRoute_Channel.class, CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class,
				CVirtualBridge.class, CMP_CTP.class, CEthTrunk.class, CStaticRoute.class, CEthRoute.class,
				CEthTrunk_SDHRoute.class, CEthRoute_StaticRoute.class, CEthRoute_ETHTrunk.class, CSection.class,
				CCTP.class, CDevice.class, CPTP.class, CTransmissionSystem.class, CTransmissionSystem_Channel.class };
	}

	// SDH的全局变量
    private BObjectMemTable<T_CCrossConnect> ccTable = new BObjectMemTable(T_CCrossConnect.class,"aend","zend");
    private BObjectMemTable<T_CTP> ctpTable = new BObjectMemTable(T_CTP.class,"portdn","parentCtp");
    private BObjectMemTable<T_CRoute> cRouteTable = new BObjectMemTable(T_CRoute.class);

    private HashMap<String,CChannel> highOrderCtpChannelMap = new HashMap<String, CChannel>();
    private HashMap<String,CChannel> lowOrderCtpChannelMap = new HashMap<String, CChannel>();
    private List<CChannel> cChannelList =  new ArrayList<CChannel>();

    private List<CSection> cSections = new ArrayList<CSection>();
    private HashMap<String,List<CChannel>> vc4ChannelMap = new HashMap<String, List<CChannel>>();

    // OTN的全局变量
    HashMap<String,List<CCTP>> ctpParentChildMap = new HashMap<String, List<CCTP>>();

    HashMap<String,CPTP>  ptpMap = new HashMap<String, CPTP>();
    HashMap<String,List<CPTP>>  cardPtpMap = new HashMap<String, List<CPTP>>();
    HashMap<String,CCTP>  ctpMap = new HashMap<String, CCTP>();
    HashMap<String,List<CCrossConnect>> aptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CCrossConnect>> ptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CSection>> ptpSectionMap = new HashMap<String, List<CSection>>();
    HashMap<String,List<CCTP>> ptp_ctpMap = new HashMap<String, List<CCTP>>();
//    List<CSection> cSections = new ArrayList<CSection>();
    
    // PTN的全局变量
    private HashMap<String,Integer> pwDnMap = new HashMap<String, Integer>();
    
    HashMap<String,CEquipment> equipmentMap = new HashMap<String, CEquipment>();
    
    @Override
    public CDevice transDevice(ManagedElement me) {
        CDevice device = super.transDevice(me);
        if (device.getProductName() != null && device.getProductName().equals("VNE"))
            device.setProductName("VNE_SDH");
        return device;
    }

    @Override
    public CEquipment transEquipment(Equipment equipment) {
        CEquipment cEquipment = super.transEquipment(equipment);
        equipmentMap.put(cEquipment.getDn(),cEquipment);
        return cEquipment;
    }

    @Override
	public void doExecute() throws Exception {
		checkEMS(emsdn, "华为");
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		System.out.println("Migrate Start at : " + df.format(new Date()));// new Date()为获取当前系统时间
		getLogger().info("Migrate Start at : " + df.format(new Date()));
		
		migrateLogical = getAttribute("logical") == null ? true : "true".equalsIgnoreCase(getAttribute("logical").toString());

		if (migrateLogical && !isTableHasData(CTP.class)) {
			migrateLogical = false;
			getLogger().info("CTP IS EMPTY ,LOGCAL = FALSE");
		}

		getLogger().info("logical = " + migrateLogical);

//		System.out.println("test start...");
//		List<CTP> ctps = sd.query("select c from CTP c where dn like '%domain=wdm%'");
//		System.out.println("ctps size=" + ctps.size());
//		
//        if (ctps != null && ctps.size() > 0) {
//            HashMap<String,List<CTP>> portCtps = new HashMap<String, List<CTP>>();
//            for (CTP ctp : ctps) {
//                DSUtil.putIntoValueList(portCtps,ctp.getPortdn(),ctp);
//            }
//            Set<String> ptpDns = portCtps.keySet();
//            for (String ptpDn : ptpDns) {
//                List<CTP> p_ctps = portCtps.get(ptpDn);
//                processCtpsInSamePtp(p_ctps);
//                for (CTP ctp : p_ctps) {
//                    CCTP cctp = transOtnCTP(ctp);
//                }
//            }
//        }
		
		// 0.通用实体入库
		logAction(emsdn + " migrateManagedElement", "同步网元", 1);
		System.out.println("同步网元");
		migrateManagedElement();

		logAction(emsdn + " migrateManagedElement", "同步子网", 3);
		System.out.println("同步子网");
		migrateSubnetwork();

		logAction("migrateEquipmentHolder", "同步槽道", 5);
		System.out.println("同步槽道");
		migrateEquipmentHolder();

		logAction("migrateEquipment", "同步板卡", 10);
		System.out.println("同步板卡");
		migrateEquipment();

		logAction("migratePTP", "同步端口", 20);
		System.out.println("同步端口");
		migratePTP();
		
		// 1.SDH入库
		getLogger().info("SDH入库 : Start...");
		System.out.println("SDH入库 : Start...");

		if (migrateLogical || isTableHasData(CTP.class)) {
			logAction("migrateCTP", "同步CTP", 25);
			System.out.println("同步CTP at : " + df.format(new Date()));
			migrateSdhCTP();
		}

		logAction("migrateSection", "同步段", 30);
		System.out.println("同步段 at : " + df.format(new Date()));
		migrateSdhSection();

		if (migrateLogical) {
			logAction("migrateCC", "同步交叉", 30);
			System.out.println("同步交叉");
			migrateSdhCC();

			logAction("migrateProtectionSubnetwork", "同步传输系统", 32);
			migrateProtectionSubnetwork();

			logAction("migrateSNC", "同步SNC", 35);
			migrateSNC();

			logAction("migrateVB", "同步VB", 40);
			migrateVB();

			logAction("migrateEthBindingPath", "同步MSTP", 70);
			migrateEthBindingPath();
		}

		cSections.removeAll(cSections);
		ctpTable.removeAll();
		ccTable.removeAll();
		cRouteTable.removeAll();
		cChannelList.removeAll(cChannelList);
		highOrderCtpChannelMap.clear();
		lowOrderCtpChannelMap.clear();
		cChannelList.clear();
		vc4ChannelMap.clear();

		getLogger().info("SDH入库 : End...");
		System.out.println("SDH入库 : End...");

		// 2.OTN入库
		getLogger().info("OTN入库 : Start...");
		System.out.println("OTN入库 : Start...");

		logAction("migrateSection", "同步段", 25);
		System.out.println("同步段");
		migrateOtnSection();

		if (migrateLogical) {
			logAction("migrateCTP", "同步CTP", 30);
			System.out.println("同步CTP");
			migrateOtnCTP();

			logAction("migrateCC", "同步交叉", 30);
			System.out.println("同步交叉");
			migrateOtnCC();

			logAction("migrateOMS", "同步逻辑资源", 60);
			System.out.println("同步逻辑资源");
			migrateOMS();

		}

		cSections.removeAll(cSections);
		ctpParentChildMap.clear();
		ptpMap.clear();
		cardPtpMap.clear();
		ctpMap.clear();
		aptpCCMap.clear();
		ptpCCMap.clear();
		ptpSectionMap.clear();
		ptp_ctpMap.clear();

		getLogger().info("OTN入库 : End...");
		System.out.println("OTN入库 : End...");

		// 3.PTN入库
		getLogger().info("PTN入库 : Start...");
		System.out.println("PTN入库 : Start...");

		logAction("migrateSection", "同步段", 25);
		System.out.println("同步段");
		migratePtnSection();

		if (migrateLogical && !isTableHasData(FlowDomainFragment.class) && !isTableHasData(CrossConnect.class)) {
			migrateLogical = false;
			getLogger().info("migratelogical = false ,becase fdfr and cc is null");
		}
		if (migrateLogical) {
			// PTN没有CTP、交叉、以及HW_MSTPBindingPath
//			logAction("migrateMPCTP", "同步MPCTP", 35);
//			System.out.println("同步MPCTP");
//			migrateMPCTP();
//			
//			logAction("migrateCTP", "同步CTP", 30);
//			System.out.println("同步CTP");
//			migratePtnCTP();
//			
//			logAction("migrateCC", "同步交叉", 30);
//			System.out.println("同步交叉");
//			migratePtnCC();
			
			logAction("migrateFTPPTP", "同步逻辑端口", 35);
			System.out.println("同步逻辑端口");
			migrateFTPPTP();
			
			logAction("migrateFlowDomainFragment", "同步业务", 40);
			System.out.println("同步业务");
			migrateFlowDomainFragment();

			logAction("migrateRoute", "同步路由", 70);
			System.out.println("同步路由");
			migrateIPRoute();

			logAction("migrateProtectGroup", "同步保护组", 85);
			System.out.println("同步保护组");
			migrateProtectGroup();
			
			logAction("migrateProtectingPWTunnel", "同步保护组2", 95);
			System.out.println("同步保护组2");
			migrateProtectingPWTunnel();
			
		}

		getLogger().info("PTN入库 : End...");
		System.out.println("PTN入库 : End...");

		System.out.println("Migrate End at : " + df.format(new Date()));// new Date()为获取当前系统时间
		getLogger().info("Migrate End at : " + df.format(new Date()));
		
		getLogger().info("release");

		// ////////////////////////////////////////
		sd.release();

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
                    sectionChannels.clear();

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
    
	protected void migratePtnCTP() throws Exception {
		// executeDelete("delete from CCTP c where c.emsName = '" + emsdn + "'", CCTP.class);

		// List<CTP> ctps = sd.queryAll(CTP.class);
		List<CTP> ctps = sd.query("select c from CTP c where dn like '%domain=ptn%'");
		if (isEmptyResult(ctps))
			return;

		String condition = " and dn like '%domain=ptn%' ";
		executeTableDeleteByType("C_CTP", emsdn, condition);
		getLogger().info("insertCtps will start... emsid= " + emsid);
		insertCtps(ctps);
	}

	protected void migrateSdhCTP() throws Exception {
		// executeDelete("delete from CCTP c where c.emsName = '" + emsdn + "'",
		// CCTP.class);
		
//		List<CTP> ctps = sd.queryAll(CTP.class);
		List<CTP> ctps = sd.query("select c from CTP c where dn like '%domain=sdh%'");
		if (!Detect.notEmpty(ctps)) {
			getLogger().info("migrateSdhCtp.size is null!");
			System.out.println("migrateSdhCtp.size is null!");
			return;
		}
		
		String condition = " and dn like '%domain=sdh%' ";
		getLogger().info("migrateSdhCtp delete...");
		executeTableDeleteByType("C_CTP", emsdn, condition);
		
//		List<CrossConnect> crossConnects = sd.query("select c from CrossConnect c where dn like '%domain=sdh%'");
//		System.out.println("migrateSdhCtp.crossConnects is " + crossConnects.size());
//		processCTP(ctps, crossConnects);
		
		getLogger().info("migrateSdhCtp insert...");
		insertSdhCtps(ctps);
		getLogger().info("migrateSdhCtp ctps.size = " + (Detect.notEmpty(ctps)?ctps.size():0));
		
//		Iterator<CCTP> iterator = list.iterator();
//		while (iterator.hasNext()) {
//			CCTP cctp = iterator.next();
//			ctpTable.addObject(new T_CTP(cctp));
//			iterator.remove();
//		}
//		getLogger().info("migrateSdhCtp list.size = " + (Detect.notEmpty(list)?list.size():0));
		
//		for (CCTP cctp : list) {
//			ctpTable.addObject(new T_CTP(cctp));
//		}

	}
	
	protected void insertSdhCtps(List<CTP> ctps) throws Exception {
		DataInserter di = new DataInserter(emsid);
		getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
//		List<CCTP> cctps = new ArrayList<CCTP>();
		if (ctps != null && ctps.size() > 0) {
			
			for (CTP ctp : ctps) {
				CCTP cctp = transCTP(ctp);
				if (cctp != null) {
					di.insert(cctp);
					ctpTable.addObject(new T_CTP(cctp));
				}
			}
			ctps.clear();
			
//			Iterator<CTP> iterator = ctps.iterator();
//			while (iterator.hasNext()) {
//				CTP ctp = iterator.next();
//				
//				CCTP cctp = transCTP(ctp);
//				if (cctp != null) {
////					cctps.add(cctp);
//					di.insert(cctp);
//					ctpTable.addObject(new T_CTP(cctp));
//				}
//				
//				iterator.remove();
//			}
		}

		di.end();
//        return cctps;
	}
	
    protected void migrateCTPbak() throws Exception {
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

    }

	public static void processCTP(List<CTP> ctps, List<CrossConnect> ccs) {
		HashMap<String, CTP> ctpMap = new HashMap<String, CTP>();
		List<String> ccEnds = new ArrayList<>();
		
		for (CTP ctp : ctps) {
			ctpMap.put(ctp.getDn(), ctp);
		}
		if (!ccs.isEmpty()) {
			for (CrossConnect cc : ccs) {
				String aends = cc.getaEndNameList();
				if (aends != null) {
					String[] dns = aends.split(Constant.listSplitReg);
					for (String dn : dns) {
						ccEnds.add(dn);
						if (!ctpMap.containsKey(dn)) {
							CTP newCTP = newCTP(dn);

							ctps.add(newCTP);
							ctpMap.put(dn, newCTP);
						}
					}
				}

				String zends = cc.getzEndNameList();
				if (zends != null) {
					String[] dns = zends.split(Constant.listSplitReg);
					for (String dn : dns) {
						ccEnds.add(dn);
						if (!ctpMap.containsKey(dn)) {
							CTP newCTP = newCTP(dn);

							ctps.add(newCTP);
							ctpMap.put(dn, newCTP);
						}
					}
				}
			}
		}
		System.out.println("migrateSdhCtp.ccEnds is " + ccEnds.size());
		
		HashMap<String, List<CTP>> portCtpMap = new HashMap<String, List<CTP>>();
		for (CTP ctp : ctps) {
			putIntoValueList(portCtpMap, ctp.getPortdn(), ctp);
		}

		ctps.clear();
		Set<String> ports = portCtpMap.keySet();
		for (String port : ports) {
			List<CTP> ctpList = portCtpMap.get(port);
			filterCTPS(port, ctpList, ccEnds);

			ctps.addAll(ctpList);
		}
	}
	public static void filterCTPS(String portDn, List<CTP> ctps, List<String> ccEnds) {
		// ObjectUtil.saveObject(portDn.replaceAll("/","<>"),ctps);
		List<CTP> vc4s = filterVC4(ctps);
		List<CTP> vc3s = filterVC3(ctps);
		List<CTP> vc12s = filterVC12(ctps);
//		System.out.println("vc4s size = " + vc4s.size());
//		System.out.println("vc3s size = " + vc3s.size());
//		System.out.println("vc12 size = " + vc12s.size());

		HashMap<Integer, HashMap<Integer, List<CTP>>> jkMap = new HashMap<Integer, HashMap<Integer, List<CTP>>>();

		HashSet<Integer> vc4JSet = new HashSet<Integer>();
		for (CTP vc4 : vc4s) {
			vc4JSet.add(CTPUtil.getJ(vc4.getDn()));
		}

		// 可能会有丢失的VC4
		List<CTP> newVC4S = new ArrayList<CTP>();
		for (CTP ctp : ctps) {
			int j = CTPUtil.getJ(ctp.getDn());
			if (j < 0)
				continue;
			if (!vc4JSet.contains(j)) {
				CTP newCTP = new CTP();
				String newDn = portDn + "@CTP:/sts3c_au4-j=" + j;
				newCTP.setDn(newDn);
				newCTP.setTag1("NEW");
				newCTP.setPortdn(portDn);
				newCTP.setParentDn(portDn);
				newCTP.setNativeEMSName("VC4-" + j);
				newCTP.setUserLabel("VC4-" + j);
				vc4s.add(newCTP);
				newVC4S.add(newCTP);
				vc4JSet.add(j);
			}
		}
		if (newVC4S.size() > 0) {
//			System.out.println(portDn + ":newVC4S = " + newVC4S.size());
			ctps.addAll(newVC4S);
		}

		////////////////////////////// 删除已经打散为VC12的vc3///////////////////////////////////
		if (vc12s.size() > 0) {
			for (CTP vc12 : vc12s) {

				String vc12Dn = vc12.getDn();
				if (!vc12Dn.contains("vt2_tu12-k="))
					continue;
				int k = CTPUtil.getK(vc12Dn);
				int j = CTPUtil.getJ(vc12Dn);

				HashMap<Integer, List<CTP>> kmap = jkMap.get(j);
				if (kmap == null) {
					kmap = new HashMap<Integer, List<CTP>>();
					jkMap.put(j, kmap);
				}

				List<CTP> list = kmap.get(k);
				if (list == null) {
					list = new ArrayList<CTP>();
					kmap.put(k, list);
				}
				list.add(vc12);

			}
		}

		HashSet<String> vc3KSet = new HashSet<String>();
		List<CTP> toDeleteVC3 = new ArrayList<CTP>();
		for (CTP vc3 : vc3s) {
			String dn = vc3.getDn();
			int j = CTPUtil.getJ(dn);
			int k = CTPUtil.getK(dn);
			vc3KSet.add(j + "-" + k);
			if (jkMap.containsKey(j) && jkMap.get(j).containsKey(k)) {
				List<CTP> vc12FromVc3 = jkMap.get(j).get(k);
				if (ccEnds.contains(dn)) {
					toDeleteVC3.addAll(vc12FromVc3);
				} else {
					toDeleteVC3.add(vc3);
				}
			}
			
		}

		////////////////////////////// 删除已经打散为VC12的vc3///////////////////////////////////

		HashMap<String, List<String>> vc4vc12map = new HashMap<String, List<String>>();

		for (CTP vc12 : vc12s) {

			String vc12dn = vc12.getDn();
			if (vc12dn.contains("/vt2_tu12")) {
				String vc4dn = vc12dn.substring(0, vc12dn.indexOf("/vt2_tu12"));
				List l = vc4vc12map.get(vc4dn);
				if (l == null) {
					l = new ArrayList();
					vc4vc12map.put(vc4dn, l);
				}
				l.add(vc12dn);

			}
		}

		////////////////////////////// 补充VC12//////////////////////////////////////////////
		List<CTP> newCTPs = new ArrayList<CTP>();
		for (Integer j : vc4JSet) {

			String vc4dn = portDn + "@CTP:/sts3c_au4-j=" + j;
			if (!vc4vc12map.containsKey(vc4dn))
				continue; // 如果该vc4下一个vc12都没有，则无视

			HashMap<Integer, List<CTP>> kmap = jkMap.get(j);
			if (kmap == null)
				kmap = new HashMap<Integer, List<CTP>>();
			for (int k = 1; k <= 3; k++) {
				if (vc3KSet.contains(j + "-" + k))
					continue;
				List<CTP> jkvc12s = kmap.get(k);
				for (int l = 1; l <= 7; l++) {
					for (int m = 1; m <= 3; m++) {
						if (CTPUtil.getCTP(jkvc12s, k, l, m) == null) {
							CTP newCTP = new CTP();
							String newDn = portDn + "@CTP:/sts3c_au4-j=" + j + "/vt2_tu12-k=" + k + "-l=" + l + "-m=" + m;
							newCTP.setDn(newDn);
							newCTP.setTag1("NEW");
							newCTP.setNativeEMSName("VC12-" + (21 * (m - 1) + 3 * (l - 1) + k));
							newCTP.setPortdn(portDn);
							newCTP.setParentDn(portDn);
							newCTPs.add(newCTP);

						}
					}
				}
			}
		}
		System.out.println(portDn + ":ctps = " + ctps.size());

		if (toDeleteVC3.size() > 0)
			System.out.println(portDn + ":toDeleteVC3 = " + toDeleteVC3.size());
		if (newCTPs.size() > 0)
			System.out.println(portDn + ":newCTPs = " + newCTPs.size());
		ctps.removeAll(toDeleteVC3);
		ctps.addAll(newCTPs);

		CTPUtil.filterVC4C(portDn, ctps);
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
	public static void putIntoValueList(HashMap map, Object key, Object value) {
		List list = (List) map.get(key);
		if (list == null) {
			list = new ArrayList();
			map.put(key, list);
		}
		list.add(value);
	}
	public static CTP newCTP(String dn) {
		CTP newCTP = new CTP();
		String portDn = extractPortDn(dn);

		newCTP.setDn(dn);
		newCTP.setTag1("NEW-CC");
		newCTP.setPortdn(portDn);
		newCTP.setParentDn(portDn);
		if (CTPUtil.isVC44C(dn))
			newCTP.setNativeEMSName("VC4_4c-" + CTPUtil.getJ(dn));
		else if (CTPUtil.isVC4(dn))
			newCTP.setNativeEMSName("VC4-" + CTPUtil.getJ(dn));
		else if (CTPUtil.isVC12(dn))
			newCTP.setNativeEMSName(
					"VC12-" + (21 * (CTPUtil.getM(dn) - 1) + 3 * (CTPUtil.getL(dn) - 1) + CTPUtil.getK(dn)));
		else if (CTPUtil.isVC3(dn))
			newCTP.setNativeEMSName("VC3-" + CTPUtil.getK(dn));

		return newCTP;
	}
	public static String extractPortDn(String endDn) {
		if (endDn.contains("@CTP"))
			return endDn.substring(0, endDn.indexOf("@CTP"));
		if (endDn.contains("port=")) {
			int end = endDn.indexOf("/" + endDn.indexOf("port="));
			if (end > -1)
				return endDn.substring(0, end);
		}
		return endDn;
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

        return cctp;
    }
    
	protected void migrateOtnCTP() throws Exception {
		// executeDelete("delete from CCTP c where c.emsName = '" + emsdn + "'",
		// CCTP.class);

//		List<CTP> ctps = sd.queryAll(CTP.class);
		List<CTP> ctps = sd.query("select c from CTP c where dn like '%domain=wdm%'");
		if (isEmptyResult(ctps))
			return;

		String condition = " and dn like '%domain=wdm%' ";
		executeTableDeleteByType("C_CTP", emsdn, condition);
		getLogger().info("insertOtnCtps will start... emsid= " + emsid);
		
//		List<CrossConnect> crossConnects = sd.query("select c from CrossConnect c where dn like '%domain=wdm%'");
//		System.out.println("migrateOtnCtp.crossConnects is " + crossConnects.size());
//		processCTP(ctps, crossConnects);
		
		insertOtnCtps(ctps);
	}
	protected void insertOtnCtps(List<CTP> ctps) throws Exception {
        DataInserter di = new DataInserter(emsid);
        getLogger().info("migrateCtp size = " + (ctps == null ? null : ctps.size()));
//        List<CCTP> cctps = new ArrayList<CCTP>();
        if (ctps != null && ctps.size() > 0) {

            HashMap<String,List<CTP>> portCtps = new HashMap<String, List<CTP>>();
            for (CTP ctp : ctps) {
                DSUtil.putIntoValueList(portCtps,ctp.getPortdn(),ctp);
            }
            ctps.clear();

            Set<String> ptpDns = portCtps.keySet();

            for (String ptpDn : ptpDns) {
                List<CTP> p_ctps = portCtps.get(ptpDn);
                //20180423 这个逻辑暂时屏蔽，如下：
                /**
                 * otn ctp的过滤，浙江也是这样的过滤逻辑么？
                 * 我觉得cmi可以放开，因为目前数据量里面，已经存在otn 自ctp重复，导致子波各种速率大量冲突的情况。
                 * 我看了浙江的手机也是这样，所以，cmi就索性放开得了
                 */
//                processCtpsInSamePtp(p_ctps);
                for (CTP ctp : p_ctps) {
                    CCTP cctp = transOtnCTP(ctp);
                    if (cctp != null) {
//                        cctps.add(cctp);
                        DSUtil.putIntoValueList(ptp_ctpMap, cctp.getParentDn(), cctp);
                        ctpMap.put(cctp.getDn(),cctp);
                        di.insert(cctp);
                    }
                }
            }
            portCtps.clear();
        }

        di.end();
//        return cctps;
    }
	public CCTP transOtnCTP(CTP ctp) {
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
            if (omsPort != null && omsPort.getTag2() != null) {
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
    
    public void migrateSNC() throws Exception {
        executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "' and dn like '%-sdh%'", CRoute.class);
        executeDelete("delete  from CRoute_CC c where c.emsName = '" + emsdn + "' and routeDn like '%-sdh%'", CRoute_CC.class);
        executeDelete("delete  from CPath c where c.emsName = '" + emsdn + "' and dn like '%-sdh%'", CPath.class);
        executeDelete("delete  from CChannel c where c.emsName = '" + emsdn + "' and aend like '%domain=sdh%'", CChannel.class);
        executeDelete("delete  from CRoute_Channel c where c.emsName = '" + emsdn + "' and routeDn like '%-sdh%'", CRoute_Channel.class);
        executeDelete("delete  from CPath_CC c where c.emsName = '" + emsdn + "' and pathDn like '%-sdh%'", CPath_CC.class);
        executeDelete("delete  from CPath_Channel c where c.emsName = '" + emsdn + "' and pathDn like '%-sdh%'", CPath_Channel.class);

        try {
//        	List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
            List<SubnetworkConnection> sncs = sd.query("select c from SubnetworkConnection c where dn like '%-sdh%' ");
            //   sncTable.addObjects(sncs);
            final HashMap<String,List<R_TrafficTrunk_CC_Section>> snc_cc_section_map = new HashMap<String, List<R_TrafficTrunk_CC_Section>>();
//            List<R_TrafficTrunk_CC_Section> routeList = sd.queryAll(R_TrafficTrunk_CC_Section.class);
            List<R_TrafficTrunk_CC_Section> routeList = sd.query("select c from R_TrafficTrunk_CC_Section c where aEnd like '%domain=sdh%'");
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
            routeList.clear();

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
            sncs.clear();

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
            paths.clear();

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
            e4Routes.clear();

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
                    cRoute.setTag1(MigrateUtil.transMapValue(snc.getAdditionalInfo()).get("Customer"));


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
    protected void migratePTP() throws Exception {
		if (!isTableHasData(PTP.class))
			return;
		executeDelete("delete  from CPTP c where c.emsName = '" + emsdn + "'", CPTP.class);
		executeDelete("delete from CIPAddress c where c.emsName = '" + emsdn + "'", CIPAddress.class);
		List<PTP> ptps = sd.queryAll(PTP.class);
		insertPtps(ptps);
	}
    
    private void insertPtps(List<PTP> ptps) throws Exception {
		DataInserter di = new DataInserter(emsid);
		getLogger().info("migratePtp size = " + (ptps == null ? null : ptps.size()));
		List<CPTP> cptps = new ArrayList<CPTP>();
		if (ptps != null && ptps.size() > 0) {
			for (PTP ptp : ptps) {
				CPTP cptp = null;
				if (ptp.getDn().contains("domain=sdh")||ptp.getDn().contains("domain=eth")) {
					cptp = transSdhPTP(ptp);
				}
				if (ptp.getDn().contains("domain=wdm")) {
					cptp = transOtnPTP(ptp);
				}
				if (ptp.getDn().contains("domain=ptn")) {
					cptp = transPtnPTP(ptp);
				}
				
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

	}

	public CPTP transSdhPTP(PTP ptp) {
		CPTP cptp = U2000MigratorUtil.transPTP(ptp);
		if (cptp.getType().equals("LP")) {
			String vbdn = cptp.getDn();

			String vb = DNUtil.extractValue(vbdn, "vb");
			vbdn = vbdn.replaceAll("PTP:", "VB:");
			int i = vbdn.indexOf("/", vbdn.indexOf("slot"));
			vbdn = vbdn.substring(0, i) + "/vb=" + vb;

			cptp.setOwner(vbdn);

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
	
	public CPTP transOtnPTP(PTP ptp) {
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
	
	public CPTP transPtnPTP(PTP ptp) {
		CPTP cptp = new CPTP();
		String dn = ptp.getDn();
		cptp.setDn(dn);
		if (dn.contains("slot")) {
			String slot = "";
			if (dn.contains("/domain")) {
				slot = dn.substring(dn.indexOf("/rack"), dn.indexOf("/domain"));
			} else if (dn.contains("/type")) {
				slot = dn.substring(dn.indexOf("/rack"), dn.indexOf("/type"));
			}
			String me = dn.substring(0, dn.lastIndexOf("@"));
			String carddn = me + "@EquipmentHolder:" + slot + "@Equipment:1";
			if (slot.toLowerCase().contains("slot")) {
				cptp.setParentDn(carddn);
				cptp.setCardid(DatabaseUtil.getSID(CEquipment.class, carddn));
			}
		}
		if (cptp.getParentDn() == null || cptp.getParentDn().isEmpty()) {
			cptp.setParentDn(ptp.getParentDn());
		}

		if (dn.contains("port=")) {
			if (dn.contains("cli_name")) {
				cptp.setNo(dn.substring(dn.lastIndexOf("port=") + 5, dn.indexOf("/cli_name")));
			} else {
				cptp.setNo(dn.substring(dn.lastIndexOf("port=") + 5));
			}
		}

		cptp.setCollectTimepoint(ptp.getCreateDate());
		cptp.setEdgePoint(ptp.isEdgePoint());
		// cptp.setType(ptp.getType());
		cptp.setConnectionState(ptp.getConnectionState());
		cptp.setTpMappingMode(ptp.getTpMappingMode());
		cptp.setDirection(DicUtil.getPtpDirection(ptp.getDirection()));
		cptp.setTransmissionParams(ptp.getTransmissionParams());
		// cptp.setRate(ptp.getRate());
		cptp.setLayerRates(ptp.getRate());
		cptp.setTpProtectionAssociation(ptp.getTpProtectionAssociation());
		// cptp.setParentDn(ptp.getParentDn());
		cptp.setEmsName(ptp.getEmsName());
		cptp.setUserLabel(ptp.getUserLabel());
		cptp.setNativeEMSName(ptp.getNativeEMSName());
		cptp.setOwner(ptp.getOwner());
		cptp.setAdditionalInfo(ptp.getAdditionalInfo());

		// String temp = cptp.getDn();
		// if (temp.startsWith("EMS:"))
		// temp = temp.substring(4);
		// if (temp.contains("@PTP"))
		// temp = temp.substring(0,temp.indexOf("@PTP"));
		// else if (temp.contains("@FTP"))
		// temp = temp.substring(0,temp.indexOf("@FTP"));
		// temp = temp.replaceAll("ManagedElement:","");
		//
		// if (temp.contains("@"))
		// temp = temp.substring(0,temp.lastIndexOf("@"));
		cptp.setDeviceDn(ptp.getParentDn());
		// cptp.setParentDn(temp);

		if (cptp.getDn().contains("type=tunnelif"))
			cptp.setParentDn(cptp.getDeviceDn());

		Map<String, String> map = MigrateUtil.transMapValue(ptp.getTransmissionParams());
		cptp.setPortMode(map.get("PortMode"));
		cptp.setPortRate(map.get("PortRate"));
		cptp.setWorkingMode(map.get("WorkingMode"));
		cptp.setMacAddress(map.get("MACAddress"));
		cptp.setIpAddress(map.get("IPAddress"));
		cptp.setIpMask(map.get("IPMask"));
		cptp.setEoType(DicUtil.getEOType(cptp.getLayerRates()));
		cptp.setSpeed(DicUtil.getSpeed(cptp.getLayerRates()));
		cptp.setType(DicUtil.getPtpType(dn, cptp.getLayerRates()));
		return cptp; // To change body of created methods use File | Settings | File Templates.
	}

    protected void migrateSdhSection() throws Exception {
        executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "' and (aEndTP like '%domain=sdh%' or aEndTP like '%domain=eth%')", CSection.class);
        DataInserter di = new DataInserter(emsid);
//        List<Section> sections = sd.queryAll(Section.class);
        List<Section> sections = sd.query("select c from Section c where aEndTP like '%domain=sdh%' or aEndTP like '%domain=eth%'");

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
//                csection.setType("OMS");
                csection.setType("SdhSection");
                di.insert(csection);

                cSections.add(csection);
                //sectionTable.addObject(section);
            }
            sections.clear();
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
    
    protected void migrateOtnSection() throws Exception {
		if (!isTableHasData(Section.class))
			return;
		executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "' and aEndTP like '%domain=wdm%'", CSection.class);
		DataInserter di = new DataInserter(emsid);
//		List<Section> sections = sd.queryAll(Section.class);
		List<Section> sections = sd.query("select c from Section c where aEndTP like '%domain=wdm%'");
		if (sections != null && sections.size() > 0) {
			for (Section section : sections) {
				CSection csection = transOtnSection(section);
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
			}
			sections.clear();
		}
		di.end();
	}
    
    public CSection transOtnSection(Section section) {
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

    protected void migratePtnSection() throws Exception {
		if (!isTableHasData(Section.class))
			return;
		executeDelete("delete  from CSection c where c.emsName = '" + emsdn + "' and aEndTP like '%domain=ptn%' ", CSection.class);
		DataInserter di = new DataInserter(emsid);
//		List<Section> sections = sd.queryAll(Section.class);
		List<Section> sections = sd.query("select c from Section c where aEndTP like '%domain=ptn%'");
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
				csection.setType("PtnSection");
				di.insert(csection);
			}
		}
		di.end();
	}
    
    protected void migratePtnCC() throws Exception {
//		List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
		List<CrossConnect> ccs = sd.query("select c from CrossConnect c where dn like '%domain=ptn%'");

		if (isEmptyResult(ccs))
			return;
		
		String condition = " and dn like '%domain=ptn%' ";
		executeTableDeleteByType("C_CROSSCONNECT",emsdn,condition);
	//	executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
		DataInserter di = new DataInserter(emsid);



		if (ccs != null && ccs.size() > 0) {
			for (CrossConnect cc : ccs) {
				CCrossConnect ccc = transCC(cc);
				ccc.setSid(DatabaseUtil.nextSID(CCrossConnect.class));
				di.insert(ccc);
			}
		}
		di.end();
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

    protected void migrateSdhCC() throws Exception {
     //   executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
    	String condition = " and dn like '%domain=sdh%' ";
        executeTableDeleteByType("C_CROSSCONNECT",emsdn,condition);
        DataInserter di = new DataInserter(emsid);
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        try {
//            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
            List<CrossConnect> ccs = sd.query("select c from CrossConnect c where dn like '%domain=sdh%'");
            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
                	if (!"remove".equals(cc.getTag3())) { //有标记的交叉不入库
                		cc.setDn(DNUtil.compressCCDn(cc.getDn()));

                        String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
                        String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);

                        newCCs.addAll(U2000MigratorUtil.transCCS(cc,emsdn));
                        makeupCTP("CC", actps, zctps, di);
                        makeupCTP("CC", zctps, actps, di);
                	} else {
                		getLogger().info("有标记的交叉不入库: " + cc.getDn());
                	}

                }
            }
            ccs.clear();

            removeDuplicateDN(newCCs);
            for (CCrossConnect ccc : newCCs) {
                if (ccc.getId() != null)
                    System.out.println("ccc = " + ccc);
                di.insert(ccc);
                ccTable.addObject(new T_CCrossConnect(ccc));
            }
            newCCs.clear();

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

    protected void migrateOtnCC() throws Exception {
//        List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
    	List<CrossConnect> ccs = sd.query("select c from CrossConnect c where dn like '%domain=wdm%'");

        if (isEmptyResult(ccs)) {
            return;
        }

        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "' and dn like '%domain=wdm%' ", CCrossConnect.class);
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
                ccs.clear();
            }

            removeDuplicateDN(newCCs);
            di.insert(newCCs);
            newCCs.clear();

        } catch (Exception e) {
            getLogger().error(e, e);
        } finally {
            di.end();
        }

    }

    /**
     * OTN的逻辑资源入库方法群
     * @throws Exception
     */
    public void migrateOMS() throws Exception {

        if (!isTableHasData(SubnetworkConnection.class))
            return;
        executeDelete("delete  from COMS_CC c where c.emsName = '" + emsdn + "'", COMS_CC.class);
        executeDelete("delete  from COMS_Section c where c.emsName = '" + emsdn + "'", COMS_Section.class);
        
        executeDelete("delete  from CRoute c where c.emsName = '" + emsdn + "' and dn like '%-wdm%'", CRoute.class);
        executeDelete("delete  from CRoute_CC c where c.emsName = '" + emsdn + "' and routeDn like '%-wdm%'", CRoute_CC.class);
        executeDelete("delete  from CPath c where c.emsName = '" + emsdn + "' and dn like '%-wdm%'", CPath.class);
        executeDelete("delete  from CChannel c where c.emsName = '" + emsdn + "' and aend like '%domain=wdm%'", CChannel.class);
        executeDelete("delete  from CRoute_Channel c where c.emsName = '" + emsdn + "' and routeDn like '%-wdm%'", CRoute_Channel.class);
        executeDelete("delete  from CPath_CC c where c.emsName = '" + emsdn + "' and pathDn like '%-wdm%'", CPath_CC.class);
        executeDelete("delete  from CPath_Channel c where c.emsName = '" + emsdn + "' and pathDn like '%-wdm%'", CPath_Channel.class);
        
        executeDelete("delete  from CPath_Section c where c.emsName = '" + emsdn + "'", CPath_Section.class);
        executeDelete("delete  from CRoute_Section c where c.emsName = '" + emsdn + "'", CRoute_Section.class);

//        List<Section> sections = sd.queryAll(Section.class);
        List<Section> sections = sd.query("select c from Section c where aEndTP like '%domain=wdm%'");

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
            
            omsCClist.clear();
            omsSectionList.clear();
            
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
            omsList.clear();
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
        waveChannelList.clear();

        //////////////////////////////////////////////////////////////////////
        HashMap<String,List<String>> subwave_routes = new HashMap<String, List<String>>();

        List<CChannel> subWaveChannelList = new ArrayList<CChannel>();
//        List<SubnetworkConnection> sncs = sd.queryAll(SubnetworkConnection.class);
        List<SubnetworkConnection> sncs = sd.query("select c from SubnetworkConnection c where dn like '%-wdm%'");
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

            String aend = snc.getaEnd();
            String zend = snc.getzEnd();
            CRoute cRoute = U2000MigratorUtil.transRoute(emsdn, snc);
            cRoute.setCategory("DSR");
            cRoute.setTag1(MigrateUtil.transMapValue(snc.getAdditionalInfo()).get("Customer"));
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
	private List<CChannel> createSubwaveChannels(CPath path, CCTP pathAside, CCTP pathZside) {
		// List<CCTP> actps = ctpParentChildMap.get(pathAside.getDn());
		// List<CCTP> zctps = ctpParentChildMap.get(pathZside.getDn());
		List<CCTP> actps = findAllChildCTPS(pathAside.getDn());
		List<CCTP> zctps = findAllChildCTPS(pathZside.getDn());
		List<CChannel> subwaveChannels = new ArrayList<CChannel>();

		if (actps != null && actps.size() > 0 && zctps != null && zctps.size() > 0) {
			if (actps.size() > 1 && zctps.size() > 1)
				System.out.print("");
			for (CCTP actp : actps) {
				boolean match = false;
				for (CCTP zctp : zctps) {
					String aname = actp.getDn().substring(pathAside.getDn().length());
					String zname = zctp.getDn().substring(pathZside.getDn().length());
					if (aname.equals(zname)) {
						match = true;
						subwaveChannels.add(createCChanell(path, actp, zctp));
					}
				}

				if (!match) {
					for (CCTP zctp : zctps) {
						subwaveChannels.add(createCChanell(path, actp, zctp));
					}
				}
			}
		} else {
			// getLogger().error("无法找到path两端的子ctp，path="+path.getDn());

			// subwaveChannels.add(createCChanell(path,pathAside,pathZside));

		}
		// if (subwaveChannels.size() == 0) {
		// getLogger().error("打散OCH到子波失败，path="+path.getDn());
		// for (CCTP actp : actps) {
		// getLogger().error("actp="+actp.getDn());
		// }
		// getLogger().error("--------------------------------------------");
		// for (CCTP zctp :zctps) {
		// getLogger().error("zctp="+zctp.getDn());
		// }
		// getLogger().error("*****************************************************");
		// }
		return subwaveChannels;
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
	private CChannel findCChanell(HashMap<String,List<CChannel>>  ctpChannels,String ctp) {
        if (ctp .equals("EMS:HZ-U2000-3-P@ManagedElement:4063249@PTP:/rack=1/shelf=3145738/slot=20/domain=wdm/port=1@CTP:/och=1/otu2=1"))
            System.out.println("ctp = " + ctp);
        List<CChannel> cChanells = findCChanells(ctpChannels, ctp);
        if (cChanells != null && cChanells.size() > 0)
            return cChanells.get(0);
        return null;
    }

	private HashMap<String, List<R_TrafficTrunk_CC_Section>> queryTrafficTrunkCCSectionMap() {
		final HashMap<String, List<R_TrafficTrunk_CC_Section>> snc_cc_section_map = new HashMap<String, List<R_TrafficTrunk_CC_Section>>();
		// List<R_TrafficTrunk_CC_Section> routeList =
		// sd.queryAll(R_TrafficTrunk_CC_Section.class);
		List<R_TrafficTrunk_CC_Section> routeList = sd.query("select c from R_TrafficTrunk_CC_Section c where aEnd like '%domain=wdm%'");
		for (R_TrafficTrunk_CC_Section _route : routeList) {
			// if (_route.getType().equals("CC")) {
			// _route.setCcOrSectionDn(DNUtil.compressCCDn(_route.getCcOrSectionDn()));
			// }
			String sncDn = _route.getTrafficTrunDn();
			List<R_TrafficTrunk_CC_Section> value = snc_cc_section_map.get(sncDn);
			if (value == null) {
				value = new ArrayList<R_TrafficTrunk_CC_Section>();
				snc_cc_section_map.put(sncDn, value);
			}
			value.add(_route);
		}
		return snc_cc_section_map;
	}
    private CChannel findSubwaveCChanell(HashMap<String,List<CChannel>> ctpChannels,String ctp,List<R_TrafficTrunk_CC_Section> dsrRoutes,HashMap<String,List<String>> subwave_routes) {
        List<CChannel> cChanells = findCChanells(ctpChannels, ctp);
        if (cChanells != null && cChanells.size() == 1)
            return cChanells.get(0);
        else if (cChanells != null && cChanells.size() > 1) {
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
    private List<CChannel> findCChanells(HashMap<String,List<CChannel>> ctpChannels,String ctp) {
        List<CChannel> cChannels = ctpChannels.get(ctp);
        if (cChannels != null) return cChannels;
        return new ArrayList<CChannel>();
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
    private boolean checkEndContainsSubCtp(String end) {
        String[] ctps = end.split(Constant.listSplitReg);
        for (String ctp : ctps) {
            List<CCTP> cctps = ctpParentChildMap.get(ctp);
            if (cctps != null && !cctps.isEmpty())
                return true;
        }
        return false;
    }
    
    /**
     * PTN的伪线入库方法群
     * @throws Exception
     */
    private void migrateFlowDomainFragment() throws Exception {
		executeDelete("delete from CPWE3 c where c.emsName = '" + emsdn + "'", CPWE3.class);
		executeDelete("delete from CPW c where c.emsName = '" + emsdn + "'", CPW.class);
		executeDelete("delete from CPWE3_PW c where c.emsName = '" + emsdn + "'", CPWE3_PW.class);
		executeDelete("delete from CTunnel c where c.emsName = '" + emsdn + "'", CTunnel.class);
		executeDelete("delete from CPW_Tunnel c where c.emsName = '" + emsdn + "'", CPW_Tunnel.class);
		// executeDelete("delete  from CTunnel_Section c where c.emsName = '" + emsdn + "'", CTunnel_Section.class);
		// executeDelete("delete from CRoute c where c.emsName = '" + emsdn + "'", CRoute.class);
		DataInserter di = new DataInserter(emsid);
		List<FlowDomainFragment> flowDomainFragments = sd.queryAll(FlowDomainFragment.class);
		List<PWTrail> pwTrails = sd.queryAll(PWTrail.class);
		List<TrafficTrunk> trafficTrunks = sd.queryAll(TrafficTrunk.class);
		HashMap<String, PWTrail> pwtrailMap = new HashMap<String, PWTrail>();
		HashMap<String, TrafficTrunk> tunnelMap = new HashMap<String, TrafficTrunk>();
		HashMap<String, TrafficTrunk> pwMap = new HashMap<String, TrafficTrunk>();
		for (TrafficTrunk trafficTrunk : trafficTrunks) {
			String rate = trafficTrunk.getRate();
			String dn = trafficTrunk.getDn();
			if (rate.equals("8010")) {
				pwMap.put(dn, trafficTrunk);
			} else if (rate.equals("8011")) {
				tunnelMap.put(dn, trafficTrunk);
			}
		}
		for (PWTrail pw : pwTrails) {
			pwtrailMap.put(pw.getDn(), pw);
		}

		// ////////////////////////////// 插入Tunnel
		getLogger().info("tunnel size = " + tunnelMap.size());
		Collection<TrafficTrunk> tunnels = tunnelMap.values();
		for (Iterator<TrafficTrunk> iterator = tunnels.iterator(); iterator.hasNext();) {
			TrafficTrunk tunnel = iterator.next();
			CTunnel cTunnel = transTunnel(tunnel);
			cTunnel.setSid(DatabaseUtil.nextSID(cTunnel));
			di.insert(cTunnel);
		}
		getLogger().info("pw size = " + pwMap.size());
		getLogger().info("pwe3 size = " + flowDomainFragments.size());
		Collection<TrafficTrunk> pws = pwMap.values();
		List<CPWE3> cpwe3List = new ArrayList();
		List<CPW> cpwList = new ArrayList();
		List<CPWE3_PW> cpwe3pwList = new ArrayList();
		// PWE3-PW
		int idx = 0;
		if (flowDomainFragments != null && flowDomainFragments.size() > 0) {
			for (FlowDomainFragment fdf : flowDomainFragments) {
				if (fdf.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@FlowdomainFragment:VPLS=6") || fdf.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@FlowdomainFragment:PWE3TRAIL=9927"))
					System.out.println();
				if (idx++ % 1000 == 0)
					getLogger().info("flowDomainFragments:" + idx);
				try {
					CPWE3 cpwe3 = transFDF(fdf);
					cpwe3List.add(cpwe3);

					String pwe3dn = fdf.getDn();
					String parentDn = fdf.getParentDn();

					List<CPW> currentCpws = new ArrayList<CPW>();

					//只处理点到点的
					if (fdf.getFdfrType().equals(FDFRT_POINT_TO_POINT) ) {
						String aptp = fdf.getaPtp();
						String zptp = fdf.getzPtp();

						if (aptp == null) aptp = "";
						if (zptp == null ) zptp = "";

						//点对点的时候需要判断两端是否为空，如果是多点的话就无所谓
						if (fdf.getFdfrType().equals(FDFRT_POINT_TO_POINT)) {
							if (aptp == null || aptp.trim().isEmpty() || zptp == null || zptp.trim().isEmpty()) {
								getLogger().error("PW-APS PWE3 end is null : pwe3=" + pwe3dn);
								continue;
							}
							if (aptp.indexOf("port") < 0 || zptp.indexOf("port") < 0) {
								getLogger().error("PW-APS PWE3 end is error : pwe3=" + pwe3dn);
								continue;
							}
						}

						if (aptp.contains(Constant.listSplit) || zptp.contains(Constant.listSplit)) {
							// PW-APS
							if (parentDn == null || parentDn.trim().isEmpty()) {
								getLogger().error("PW-APS PWE3 parentDn is null : pwe3=" + pwe3dn);
								continue;
							}
							String[] pwDns = parentDn.split(Constant.listSplitReg);
							List<TrafficTrunk> cpws = new ArrayList<TrafficTrunk>();
							for (String pwDn : pwDns) {
								TrafficTrunk pw = pwMap.get(pwDn);
								if (pw == null) {
									getLogger().error("PW not found : pw=" + pwDn);
									continue;
								}
								cpws.add(pw);
							}

							for (TrafficTrunk pw : cpws) {
								String pwdn = pw.getDn();
								if (pwdn.equals("EMS:ZSH-U2000-1-PTN@Flowdomain:1@TrafficTrunk:PWTRAIL=23"))
									System.out.println();
								String pwane = pw.getaNE();
								String pwzne = pw.getzNE();
								if (pwane == null || pwane.trim().isEmpty() || pwzne == null || pwzne.trim().isEmpty()) {
									getLogger().error("PW-APS PW end is null : pw=" + pwdn);
									continue;
								}
								PWTrail pwtrail = pwtrailMap.get(pwdn);
								// String newPWE3dn = pwe3dn + "<>" + pwdn;
								// CPWE3 cpwe3 = null;
								CPW cpw = null;
								if (aptp.contains(Constant.listSplit)) {


									String[]  ptps = fdf.getaPtp().split(Constant.listSplitReg);
									if (fdf.getzPtp() != null && fdf.getzPtp().length() > 0) {
										String[] zptps = fdf.getzPtp().split(Constant.listSplitReg);

										String[] aptps = ptps;

										ptps = new String[aptps.length + zptps.length];
										System.arraycopy(aptps,0,ptps,0,aptps.length);
										System.arraycopy(zptps,0,ptps,aptps.length,zptps.length);

									}

									if (fdf.getFdfrType().equals("FDFRT_MULTIPOINT")) {
										String _aptp = pw.getaPtp();
										String _zptp = pw.getzPtp();

										String aPWE3ptp = findPtpWithNEDn(ptps, DNUtil.extractNEDn(_aptp));
										if (aPWE3ptp != null) _aptp = aPWE3ptp;
										String zpwe3Ptp = findPtpWithNEDn(ptps, DNUtil.extractNEDn(_zptp));
										if (zpwe3Ptp != null) _zptp = zpwe3Ptp;
										cpw =  transCPW(pw, _aptp, _zptp, pwtrail, cpwe3);
									} else {        //点到点

										String aptp1 = getPtp1(aptp);
										String aptp2 = getPtp2(aptp);

										String ane1 = ptp2ne(aptp1);
										String ane2 = ptp2ne(aptp2);
										String zne = ptp2ne(zptp);
										if ((ane1.equals(pwane) && zne.equals(pwzne)) || (ane1.equals(pwzne) && zne.equals(pwane))) {
											// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp1, zptp);
											cpw = transCPW(ane1, zne, pwane, pwzne, pw, aptp1, zptp, pwtrail, cpwe3);
										} else if ((ane2.equals(pwane) && zne.equals(pwzne)) || (ane2.equals(pwzne) && zne.equals(pwane))) {
											// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp2, zptp);
											cpw = transCPW(ane2, zne, pwane, pwzne, pw, aptp2, zptp, pwtrail, cpwe3);
										} else if ((ane1.equals(pwane) && ane2.equals(pwzne)) || (ane1.equals(pwzne) && ane2.equals(pwane))) {
											// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp1, aptp2);
											cpw = transCPW(ane1, ane2, pwane, pwzne, pw, aptp1, aptp2, pwtrail, cpwe3);
										} else {
											getLogger().error("PW do not match PWE3: pw=" + pwdn);
											continue;
										}
									}
								} else {
									String zptp1 = getPtp1(zptp);
									String zptp2 = getPtp2(zptp);

									String zne1 = ptp2ne(zptp1);
									String zne2 = ptp2ne(zptp2);
									String ane = ptp2ne(aptp);
									if ((ane.equals(pwane) && zne1.equals(pwzne)) || (ane.equals(pwzne) && zne1.equals(pwane))) {
										// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp, zptp1);
										cpw = transCPW(ane, zne1, pwane, pwzne, pw, aptp, zptp1, pwtrail, cpwe3);
									} else if ((ane.equals(pwane) && zne2.equals(pwzne)) || (ane.equals(pwzne) && zne2.equals(pwane))) {
										// cpwe3 = transCPWE3(fdf, newPWE3dn, aptp, zptp2);
										cpw = transCPW(ane, zne2, pwane, pwzne, pw, aptp, zptp2, pwtrail, cpwe3);
									} else if ((zne1.equals(pwane) && zne2.equals(pwzne)) || (zne1.equals(pwzne) && zne2.equals(pwane))) {
										// cpwe3 = transCPWE3(fdf, newPWE3dn, zptp1, zptp2);
										cpw = transCPW(zne1, zne2, pwane, pwzne, pw, zptp1, zptp2, pwtrail, cpwe3);
									} else {
										getLogger().error("PW do not match PWE3: pw=" + pwdn);
										continue;
									}
								}
								if (cpw != null) {
									cpw.setSid(DatabaseUtil.nextSID(cpw));
									cpwList.add(cpw);

									currentCpws.add(cpw);

									CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
									cpwe3pwList.add(cpwe3_pw);
								}
							}
						} else {
							// POINT_TO_POINT
							// CPWE3 cpwe3 = transCPWE3(fdf, pwe3dn, aptp, zptp);
							// cpwe3.setSid(DatabaseUtil.nextSID(cpwe3));
							// di.insert(cpwe3);
							//
							if (parentDn == null || parentDn.trim().isEmpty()) {
								//如果是同网元内的CPWE3，虚拟出一个PW来。
								if (DNUtil.extractNEDn(cpwe3.getAptp()).equals(DNUtil.extractNEDn(cpwe3.getZptp()))) {
									TrafficTrunk tpw = new TrafficTrunk();
									tpw.setDn(cpwe3.getDn()+"_PW");
									tpw.setaEnd(cpwe3.getAend());
									tpw.setzEnd(cpwe3.getZend());
									tpw.setaPtp(cpwe3.getAptp());
									tpw.setzPtp(cpwe3.getZptp());
									tpw.setNativeEMSName(cpwe3.getNativeEMSName());
									tpw.setaNE(DNUtil.extractNEDn(cpwe3.getAptp()));
									tpw.setzNE(DNUtil.extractNEDn(cpwe3.getZptp()));
									tpw.setUserLabel(cpwe3.getNativeEMSName());
									tpw.setDirection("CD_BI");
									tpw.setEmsName(emsdn);
									pwMap.put(tpw.getDn(),tpw);
									cpwe3.setParentDn(tpw.getDn());
									parentDn = tpw.getDn();

								}
							}

							if (parentDn == null || parentDn.trim().isEmpty()) {
								getLogger().error("PWE3 parentDn is null : pwe3=" + fdf.getDn());
								continue;
							}

							if (parentDn.indexOf(Constant.listSplit) < 0) {
								TrafficTrunk pw = pwMap.get(parentDn);
								if (pw == null) {
									getLogger().error("PW not found : pw=" + parentDn);
									continue;
								}

								String apwe3ne = ptp2ne(cpwe3.getAptp());
								String zpwe3ne = ptp2ne(cpwe3.getZptp());
								if (pw.getaNE().equals(apwe3ne) && pw.getzNE().equals(zpwe3ne)) {
									aptp = cpwe3.getAptp();
									zptp = cpwe3.getZptp();
								} else if (pw.getzNE().equals(apwe3ne) && pw.getaNE().equals(zpwe3ne)) {
									aptp = cpwe3.getZptp();
									zptp = cpwe3.getAptp();
								} else {

									PWTrail pwTrail = pwtrailMap.get(pw.getDn());

									if (pwTrail != null) {
										// pwtrail 两端可能有多个网元
										if (pwTrail.getaNE().contains(apwe3ne) && pwTrail.getzNE().contains(zpwe3ne)) {
											aptp = cpwe3.getAptp();
											zptp = cpwe3.getZptp();
										}  else if (pwTrail.getzNE().contains(apwe3ne) && pwTrail.getaNE().contains(zpwe3ne)) {
											aptp = cpwe3.getZptp();
											zptp = cpwe3.getAptp();
										}
									}

									if (aptp.isEmpty() || zptp.isEmpty()) {
										getLogger().error("PW do not match PWE3: pwe3=" + cpwe3.getDn());
										continue;
									}
								}

								CPW cpw = transCPW(pw, aptp, zptp, pwtrailMap.get(pw.getDn()), cpwe3);
								if (pw.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@TrafficTrunk:PWTRAIL=74"))
									getLogger().info("74p2p: cpwdn = "+cpw.getDn());
								cpw.setSid(DatabaseUtil.nextSID(cpw));
								cpwList.add(cpw);
								currentCpws.add(cpw);

								CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
								cpwe3pwList.add(cpwe3_pw);
							} else {
								String[] pwDns = parentDn.split(Constant.listSplitReg);
								for (String pwDn : pwDns) {
									TrafficTrunk pw = pwMap.get(pwDn);
									if (pw == null) {
										getLogger().error("PW not found : pw=" + pw);
										continue;
									}

									String apwe3ne = ptp2ne(cpwe3.getAptp());
									String zpwe3ne = ptp2ne(cpwe3.getZptp());
									if (pw.getaNE().equals(apwe3ne) && pw.getzNE().equals(zpwe3ne)) {
										aptp = cpwe3.getAptp();
										zptp = cpwe3.getZptp();
									} else if (pw.getzNE().equals(apwe3ne) && pw.getaNE().equals(zpwe3ne)) {
										aptp = cpwe3.getZptp();
										zptp = cpwe3.getAptp();
									} else {
										getLogger().error("PW do not match PWE3: pwe3=" + cpwe3.getDn());
										continue;
									}

									CPW cpw = transCPW(pw, aptp, zptp, pwtrailMap.get(pw.getDn()), cpwe3);
									cpw.setSid(DatabaseUtil.nextSID(cpw));
									cpwList.add(cpw);
									currentCpws.add(cpw);
									CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
									cpwe3pwList.add(cpwe3_pw);
								}
							}
						}




					} else if (fdf.getFdfrType().equals("FDFRT_MULTIPOINT")){       // add by ronnie 150904

						String aptp = fdf.getaPtp();
						String zptp = fdf.getzPtp();

						if (aptp == null) aptp = "";
						if (zptp == null ) zptp = "";

						if (parentDn == null) {
							getLogger().info("parentdn is null : fdf = "+fdf.getDn());
							continue;
						}

						String ptps = aptp.contains(Constant.listSplit) ? aptp : zptp;

						String[] ptpDns = ptps.split(Constant.listSplitReg);



						String[] pwDns = parentDn.split(Constant.listSplitReg);

						List<TrafficTrunk> cpws = new ArrayList<TrafficTrunk>();
						HashMap<String,TrafficTrunk> neRelations = new HashMap<String,TrafficTrunk>();
						for (String pwDn : pwDns) {
							HashSet<String> apwNEDns = new HashSet<String>();
							HashSet<String> zpwNEDns = new HashSet<String>();
							TrafficTrunk pw = pwMap.get(pwDn);
							if (pw == null) {
								getLogger().error("PW not found : pw=" + pwDn);
								continue;
							}
							cpws.add(pw);
							if (pw.getaNE() != null) {
								String[] dns = pw.getaNE().split(Constant.listSplitReg);
								for (String dn : dns) {
									apwNEDns.add(dn);
								}
							}

							if (pw.getzNE() != null) {
								String[] dns = pw.getzNE().split(Constant.listSplitReg);
								for (String dn : dns) {
									zpwNEDns.add(dn);
								}
							}

							for (String apwNEDn : apwNEDns) {
								for (String zpwNEDn : zpwNEDns) {
									neRelations.put(apwNEDn + "<>" + zpwNEDn, pw);
							//		neRelations.put(zpwNEDn+"<>"+apwNEDn,pw);
								}
							}
						}


						for (String ptpDn : ptpDns) {
							for (String ptpDn2 : ptpDns) {
								String ne1 = DNUtil.extractNEDn(ptpDn);
								String ne2 = DNUtil.extractNEDn(ptpDn2);
								if (!ne1.equals(ne2)) {
									TrafficTrunk pw = neRelations.get(ne1 + "<>" + ne2);
									if (pw != null) {

										CPW cpw = transCPW(pw,ptpDn,ptpDn2,pwtrailMap.get(pw.getDn()),cpwe3);
										if (pw.getDn().equals("EMS:NB-U2000-1-P@Flowdomain:1@TrafficTrunk:PWTRAIL=74"))
											getLogger().info("74: cpwdn = "+cpw.getDn());
										cpw.setDn(cpw.getDn());
										cpw.setSid(DatabaseUtil.nextSID(cpw));
										cpwList.add(cpw);
										currentCpws.add(cpw);
										CPWE3_PW cpwe3_pw = transCPWE3_PW(cpwe3, cpw);
										cpwe3pwList.add(cpwe3_pw);
									}
								 }
							}
						}

					} else {
						getLogger().error("ERROR PWE3TYPE : pwe3=" + fdf.getDn());
					}

					// 如果伪线Z端为空
					if (cpwe3.getZptp() == null || cpwe3.getZptp().isEmpty()) {
						StringBuffer zp = new StringBuffer();
						for (int i = 0; i < currentCpws.size(); i++) {
							CPW currentCpw = currentCpws.get(i);
							String _zptp = currentCpw.getZptp();
							zp.append(_zptp);
							if (i < currentCpws.size()-1)
								zp.append("||");
						}

						cpwe3.setZptp(zp.toString());
					}


				} catch (Exception e) {
					getLogger().error("ERROR PWE3 : pwe3=" + fdf.getDn(), e);
				}
			}
		}

		for (CPW cpw : cpwList) {
			cpw.setEmsName(emsdn);
		}

		removeDuplicateDN(cpwe3List);
		removeDuplicateDN(cpwList);
		removeDuplicateDN(cpwe3pwList);

		di.insert(cpwe3List);
		di.insert(cpwList);
		di.insert(cpwe3pwList);


		List<CPW_Tunnel> cpw_tunnelList = new ArrayList<CPW_Tunnel>();
		// PW-TUNNEL
		int index = 0;
		if (cpwList != null && cpwList.size() > 0) {
			for (CPW pw : cpwList) {
				try {
					if (index++ % 1000 == 0)
						getLogger().info("pws:" + index);

					// CPW cpw = transPW(pw);
					// if (!DatabaseUtil.isSIDExisted(CPW.class, cpw.getDn())) {
					// di.insert(cpw);
					// }
					if (!DatabaseUtil.isSIDExisted(CPW.class, pw.getDn())) {
						continue;
					}

					String parentDn = pw.getParentDn();
					if (parentDn == null || parentDn.isEmpty()) {
						PWTrail pwTrail = pwtrailMap.get(pw.getDn().contains("__")? pw.getDn().substring(0,pw.getDn().indexOf("__")): pw.getDn());

						if (pwTrail != null)
							parentDn = pwTrail.getParentDn();
						else
							getLogger().error("---pwTrail is null : " + pw.getDn()+" pwtrail size = "+pwtrailMap.size());
					}

					if (parentDn == null || parentDn.trim().isEmpty()) {
						getLogger().error("---PW parentDn is null : " + pw.getDn());
						continue;
					}
					String[] tunnelDns = parentDn.split(Constant.listSplitReg);
					for (String tunnelDn : tunnelDns) {
						TrafficTrunk tunnel = tunnelMap.get(tunnelDn);
						if (tunnel == null) {
							getLogger().error("Tunnel not found : Tunnel=" + tunnelDn);
							continue;
						}

						CPW_Tunnel cpw_tunnel = transCPW_Tunnel(pw.getEmsName(), pw.getDn(), tunnel.getDn());
						cpw_tunnelList.add(cpw_tunnel);
					}
				} catch (Exception e) {
					getLogger().error("ERROR PW : pw=" + pw.getDn(), e);
				}
			}
		}

		removeDuplicateDN(cpw_tunnelList);
		di.insert(cpw_tunnelList);

		flowDomainFragments.clear();
		pws.clear();
		trafficTrunks.clear();
		pwtrailMap.clear();
		tunnelMap.clear();

		di.end();
		getLogger().info("migrate migrateFlowDomainFragment success");

		// String[] pwTrailDns = parentDn.split(Constant.listSplitReg);
		// for (String pwTrailDn : pwTrailDns) {
		// if (pwTrailDn == null || pwTrailDn.trim().isEmpty()) {
		// getLogger().error("Faild to find parentPWDn : cpwe3=" + cpwe3.getDn());
		// continue;
		// }
		// TrafficTrunk pwTrail = pwtrailMap.get(pwTrailDn);
		// if (pwTrail == null) {
		// getLogger().error("PWTrail not found : " + pwTrailDn);
		// continue;
		// }
		// // HZ-U2000-2-P@1@TUNNELTRAIL=403208
		// String pwTrailParentDn = pwTrail.getParentDn();
		//
		// String[] tunnelDns = pwTrailParentDn.split(Constant.listSplitReg);
		// // List<String> tunnelDns = MigrateUtil.simpleDN2FullDns(pwTrailParentDn, new String[]{"EMS", "Flowdomain", "TrafficTrunk"});
		// if (tunnelDns == null || tunnelDns.length == 0) {
		// getLogger().error("Faild to find parentTunnelDn : pwTrail=" + pwTrail.getDn());
		// continue;
		// }
		// for (String tunnelDn : tunnelDns) {
		// if (tunnelDn == null || tunnelDn.trim().isEmpty()) {
		// getLogger().error("Faild to find parentTunnelDn : pwTrail=" + pwTrail.getDn());
		// continue;
		// }
		// TrafficTrunk tunnelTrail = tunnelsMap.get(tunnelDn);
		// if (tunnelTrail == null) {
		// getLogger().error("TunnelTrail not found : " + tunnelDn);
		// continue;
		// }
		//
		// // cpwe3.setParentDn(tunnelDn);
		// // cpwe3.setTunnelDn(tunnelDn);
		// // cpwe3.setTunnelId(DatabaseUtil.getSID(CTunnel.class, tunnelDn));
		//
		// CPWE3_TUNNEL cpwe3_tunnel = new CPWE3_Tunnel();
		// // cpwe3_tunnel.setDn(SysUtil.nextDN());
		// cpwe3_tunnel.setDn(cpwe3.getDn() + "<>" + tunnelDn);
		// cpwe3_tunnel.setCollectTimepoint(cpwe3.getCollectTimepoint());
		// cpwe3_tunnel.setPwe3Dn(cpwe3.getDn());
		// cpwe3_tunnel.setPwe3Id(DatabaseUtil.getSID(CPWE3.class, cpwe3.getDn()));
		// cpwe3_tunnel.setTunnelDn(tunnelDn);
		// cpwe3_tunnel.setTunnelId(DatabaseUtil.getSID(CTunnel.class, tunnelDn));
		// cpwe3_tunnel.setEmsName(cpwe3.getEmsName());
		// // 一条PWE3关联两条PW，这两条PW关联同一条TUNNEL，会造成DN重复
		// if (!DatabaseUtil.isSIDExisted(CPW_Tunnel.class, cpwe3_tunnel.getDn())) {
		// di.insert(cpwe3_tunnel);
		// }
		// }
		//
		// // di.insert(cpwe3);
		//
		// }
		// }
		// }

	}
    private String findPtpWithNEDn(String[] ptpList,String ne) {
		for (String ptp : ptpList) {
			if (ptp.startsWith(ne+"@"))
				return ptp;
		}
		return null;
	}
    private CPW transCPW(String pwe3Ane, String pwe3Zne, String pwAne, String pwZne, TrafficTrunk pw, String aptp, String zptp, PWTrail pwtrail, CPWE3 cpwe3) {
		CPW cpw = null;
		if (pwe3Ane.equals(pwAne) && pwe3Zne.equals(pwZne)) {
			cpw = transCPW(pw, aptp, zptp, pwtrail, cpwe3);
		} else {
			cpw = transCPW(pw, zptp, aptp, pwtrail, cpwe3);
		}
		return cpw;
	}
	private CPW transCPW(TrafficTrunk pw, String aptp, String zptp, PWTrail pwtrail, CPWE3 cpwe3) {
		CPW cpw = transPW(pw);
		cpw.setAptp(aptp);
		cpw.setZptp(zptp);
		cpw.setAptpId(DatabaseUtil.getSID(CPTP.class, cpw.getAptp()));
		cpw.setZptpId(DatabaseUtil.getSID(CPTP.class, cpw.getZptp()));

		cpw.setAvlanId(cpwe3.getAvlanId());
		cpw.setZvlanId(cpwe3.getZvlanId());
		if (pwtrail != null) {
			cpw.setApwid(pwtrail.getApwid());
			cpw.setZpwid(pwtrail.getZpwid());
			cpw.setaWorkingMode(pwtrail.getaWorkingMode());
			cpw.setzWorkingMode(pwtrail.getzWorkingMode());
		}
		return cpw;
	}
	public CPW transPW(TrafficTrunk src) {

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
		des.setRerouteAllowed(src.getRerouteAllowed());
		des.setAdministrativeState(src.getAdministrativeState());
		des.setActiveState(src.getActiveState());
		des.setDirection(DicUtil.getConnectionDirection(src.getDirection()));
		des.setTransmissionParams(src.getTransmissionParams());
		des.setNetworkRouted(src.getNetworkRouted());
		des.setAend(src.getaEnd());
		des.setZend(src.getzEnd());
		// des.setAptp(src.getaPtp());
		// des.setZptp(src.getzPtp());
		// des.setAptpId(DatabaseUtil.getSID(CPTP.class, des.getAptp()));
		// des.setZptpId(DatabaseUtil.getSID(CPTP.class, des.getZptp()));

		des.setAendTrans(src.getaEndTrans());
		des.setZendtrans(src.getzEndtrans());
		des.setParentDn(src.getParentDn());
		des.setEmsName(src.getEmsName());
		des.setUserLabel(src.getUserLabel());
		des.setNativeEMSName(src.getNativeEMSName());
		des.setOwner(src.getOwner());
		des.setAdditionalInfo(src.getAdditionalInfo());

		// des.setApwid(src.getApwid());
		// des.setZpwid(src.getZpwid());
		// des.setaWorkingMode(src.getaWorkingMode());
		// des.setzWorkingMode(src.getzWorkingMode());

		return des;
	}
	private String getPtp1(String ptp) {
		return ptp.substring(0, ptp.indexOf(Constant.listSplit));
	}
	private String getPtp2(String ptp) {
		return ptp.substring(ptp.indexOf(Constant.listSplit) + 2);
	}
	
	/*
	 * PTN的路由入库
	 */
	protected void migrateIPRoute() throws Exception {
		if (!isTableHasData(R_TrafficTrunk_CC_Section.class))
			return;
		// //////////////////// migrate route //////////////////////////////
		executeDelete("delete  from CTunnel_Section c where c.emsName = '" + emsdn + "'", CTunnel_Section.class);
		// executeDelete("delete from CRoute c where c.emsName = '" + emsdn + "'", CIPRoute.class);
		DataInserter di = new DataInserter(emsid);
		int from = 0;
		int limit = 10000;
		while (true) {
//			List<R_TrafficTrunk_CC_Section> rtcs = sd.query("select c from R_TrafficTrunk_CC_Section c", from, limit);
			List<R_TrafficTrunk_CC_Section> rtcs = sd.query("select c from R_TrafficTrunk_CC_Section c where aEnd like '%domain=ptn%'", from, limit);
			if (from == 0 && (rtcs == null || rtcs.isEmpty())) {
				// throw new EMSDataTableEmptyException("Table empty : R_TrafficTrunk_CC_Section");
				return;
			}
			if (rtcs.isEmpty())
				break;
			from = from + rtcs.size();
			getLogger().info("Migrate route " + from);
			long t1 = System.currentTimeMillis();
			for (int i = 0; i < rtcs.size(); i++) {
				R_TrafficTrunk_CC_Section r_trafficTrunk_cc_section = rtcs.get(i);
				String trafficTrunDn = r_trafficTrunk_cc_section.getTrafficTrunDn();
				// 资管没有用到这部分数据
				// CIPRoute cRoute = new CIPRoute();
				// cRoute.setEmsName(emsdn);
				// // cRoute.setDn(SysUtil.nextDN());
				// cRoute.setTunnelDn(trafficTrunDn);
				// cRoute.setTunnelId(DatabaseUtil.getSID(CTunnel.class, trafficTrunDn));
				// cRoute.setEntityType(r_trafficTrunk_cc_section.getType());
				// if (r_trafficTrunk_cc_section.getCcOrSectionDn() != null && !r_trafficTrunk_cc_section.getCcOrSectionDn().isEmpty()) {
				// cRoute.setEntityDn(r_trafficTrunk_cc_section.getCcOrSectionDn());
				// }
				// cRoute.setAptp(r_trafficTrunk_cc_section.getaPtp());
				// cRoute.setZptp(r_trafficTrunk_cc_section.getzPtp());
				//
				// cRoute.setAptpId(DatabaseUtil.getSID(CPTP.class, cRoute.getAptp()));
				// cRoute.setZptpId(DatabaseUtil.getSID(CPTP.class, cRoute.getZptp()));
				// cRoute.setAend(r_trafficTrunk_cc_section.getaEnd());
				// cRoute.setZend(r_trafficTrunk_cc_section.getzEnd());
				// cRoute.setCollectTimepoint(r_trafficTrunk_cc_section.getCreateDate());
				//
				// cRoute.setDn(cRoute.getTunnelDn() + "_" + cRoute.getEntityDn());
				// di.insert(cRoute);

				if (r_trafficTrunk_cc_section.getType().equals("SECTION")) {
					CTunnel_Section ts = new CTunnel_Section();
					ts.setDn(trafficTrunDn + "<>" + r_trafficTrunk_cc_section.getCcOrSectionDn());
					ts.setEmsName(emsdn);
					ts.setTunnelDn(trafficTrunDn);
					ts.setTunnelId(DatabaseUtil.getSID(CTunnel.class, trafficTrunDn));
					ts.setSectionDn(r_trafficTrunk_cc_section.getCcOrSectionDn());
					ts.setSectionId(DatabaseUtil.getSID(CSection.class, r_trafficTrunk_cc_section.getCcOrSectionDn()));
					if (!DatabaseUtil.isSIDExisted(CTunnel_Section.class, ts.getDn())) // section+tunnel 可能有重复
						di.insert(ts);
				}

			}
			rtcs.clear();
		}
		di.end();

	}
	
	protected void executeTableDeleteByType(final String tableName,final String emsname,final String condition) throws Exception{
		getLogger().info("executeTableDelete : "+tableName+" ems="+emsname);
		
		JPASupport ctx = new JPASupportSpringImpl("entityManagerFactoryData");
		getLogger().info("entityManagerFactoryData : success");
		try {
			EntityManager entityManager = ctx.getEntityManager();

			HibernateEntityManager hibernateEntityManager = (HibernateEntityManager)entityManager;
			final AtomicBoolean finish = new AtomicBoolean(false);
			while (!finish.get()) {

                entityManager.getTransaction().begin();
                hibernateEntityManager.getSession().doWork(new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        String sql = "delete from "+tableName+" where emsname='"+emsname+"' and rownum < 10000 " + condition;
                        int i = connection.prepareStatement(sql).executeUpdate();
                        if (i == 0)
                            finish.set(true);

                    }
                });
                entityManager.getTransaction().commit();
            }
			
			getLogger().info("executeTableDelete : "+tableName+" ems="+emsname+"-end..");
		} catch (Exception e) {
			getLogger().info("error : "+e.getMessage());
			throw e;
		} finally {
			ctx.release();
		}
		getLogger().info("executeTableDelete : end true...");

		return;
	}
	
	public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {

		String dn = equipmentHolder.getDn();
		String no = dn.substring(dn.lastIndexOf("=") + 1);

		if (equipmentHolder.getHolderType().equals("rack")) {
			CRack cequipmentHolder = new CRack();
			cequipmentHolder.setCdeviceId(DatabaseUtil.getSID(CDevice.class,
					equipmentHolder.getDn().substring(0, equipmentHolder.getDn().indexOf("@EquipmentHolder"))));
			cequipmentHolder.setDn(equipmentHolder.getDn());
			cequipmentHolder.setSid(DatabaseUtil.nextSID(cequipmentHolder));
			cequipmentHolder.setNo(no);
			cequipmentHolder.setCollectTimepoint(equipmentHolder.getCreateDate());
			cequipmentHolder.setHolderType(equipmentHolder.getHolderType());
			cequipmentHolder.setExpectedOrInstalledEquipment(equipmentHolder.getExpectedOrInstalledEquipment());
			cequipmentHolder.setAcceptableEquipmentTypeList(equipmentHolder.getAcceptableEquipmentTypeList());
			cequipmentHolder.setHolderState(equipmentHolder.getHolderState());
			cequipmentHolder.setParentDn(equipmentHolder.getParentDn());
			cequipmentHolder.setEmsName(equipmentHolder.getEmsName());
			cequipmentHolder.setUserLabel(equipmentHolder.getUserLabel());
			cequipmentHolder.setNativeEMSName(equipmentHolder.getNativeEMSName());
			cequipmentHolder.setOwner(equipmentHolder.getOwner());
			cequipmentHolder.setAdditionalInfo(equipmentHolder.getAdditionalInfo());

			return cequipmentHolder;
		}  else if (equipmentHolder.getHolderType().equals("shelf") || equipmentHolder.getHolderType().equals("sub_shelf")) {
			CShelf cequipmentHolder = new CShelf();
			if (dn.contains("/shelf")) {
				cequipmentHolder.setRackDn(dn.substring(0, dn.indexOf("/shelf")));
				cequipmentHolder.setRackId(DatabaseUtil.getSID(CRack.class, cequipmentHolder.getRackDn()));
			}

			if (equipmentHolder.getHolderType().equals("sub_shelf")) {
				cequipmentHolder.setTag1(dn.substring(0,dn.indexOf("/sub_shelf=")));
			} else {
				// 20180122，香港华为入库逻辑，有sub_shelf的shelf不做入库
				List<EquipmentHolder> subShelfs = sd.query("select c from EquipmentHolder c where dn like '" + dn + "/sub_shelf=%'");
				if (Detect.notEmpty(subShelfs)) {
					return null;
				}
			}

			cequipmentHolder.setNo(no);
			cequipmentHolder.setDn(equipmentHolder.getDn());
			cequipmentHolder.setSid(DatabaseUtil.nextSID(cequipmentHolder));

			cequipmentHolder.setCollectTimepoint(equipmentHolder.getCreateDate());
			cequipmentHolder.setHolderType(equipmentHolder.getHolderType());
			cequipmentHolder.setExpectedOrInstalledEquipment(equipmentHolder.getExpectedOrInstalledEquipment());
			cequipmentHolder.setAcceptableEquipmentTypeList(equipmentHolder.getAcceptableEquipmentTypeList());
			cequipmentHolder.setHolderState(equipmentHolder.getHolderState());
			cequipmentHolder.setParentDn(equipmentHolder.getParentDn());
			cequipmentHolder.setEmsName(equipmentHolder.getEmsName());
			cequipmentHolder.setUserLabel(equipmentHolder.getUserLabel());
			cequipmentHolder.setNativeEMSName(equipmentHolder.getNativeEMSName());
			cequipmentHolder.setOwner(equipmentHolder.getOwner());
			cequipmentHolder.setAdditionalInfo(equipmentHolder.getAdditionalInfo());
			cequipmentHolder.setShelfType(getShelfType(equipmentHolder.getParentDn(), equipmentHolder.getAdditionalInfo()));
			return cequipmentHolder;
		} else if (equipmentHolder.getHolderType().equals("slot") || equipmentHolder.getHolderType().equals("sub_slot")) {
			CSlot cequipmentHolder = new CSlot();
			if (dn.contains("/slot")) {
				cequipmentHolder.setShelfDn(dn.substring(0, dn.indexOf("/slot")));
				cequipmentHolder.setShelfId(DatabaseUtil.getSID(CShelf.class, cequipmentHolder.getShelfDn()));

			}
			cequipmentHolder.setNo(no);
			if (equipmentHolder.getHolderType().equals("sub_slot")) {
				String cardDn = dn.substring(0, dn.indexOf("/sub")) + "@Equipment:1";
				cequipmentHolder.setCardDn(cardDn);
			}

			cequipmentHolder.setDn(equipmentHolder.getDn());
			cequipmentHolder.setSid(DatabaseUtil.nextSID(cequipmentHolder));

			cequipmentHolder.setCollectTimepoint(equipmentHolder.getCreateDate());
			cequipmentHolder.setHolderType(equipmentHolder.getHolderType());
			cequipmentHolder.setExpectedOrInstalledEquipment(equipmentHolder.getExpectedOrInstalledEquipment());
			cequipmentHolder.setAcceptableEquipmentTypeList(equipmentHolder.getAcceptableEquipmentTypeList());

			if (cequipmentHolder.getAcceptableEquipmentTypeList() != null && cequipmentHolder.getAcceptableEquipmentTypeList().length() > 1500)
				cequipmentHolder.setAcceptableEquipmentTypeList("");

			cequipmentHolder.setHolderState(equipmentHolder.getHolderState());
			cequipmentHolder.setParentDn(equipmentHolder.getParentDn());
			cequipmentHolder.setEmsName(equipmentHolder.getEmsName());
			cequipmentHolder.setUserLabel(equipmentHolder.getUserLabel());
			cequipmentHolder.setNativeEMSName(equipmentHolder.getNativeEMSName());
			cequipmentHolder.setOwner(equipmentHolder.getOwner());
			cequipmentHolder.setAdditionalInfo(equipmentHolder.getAdditionalInfo());
			return cequipmentHolder;
		}
		return null;
	}
	

    public static void main(String[] args) throws Exception {
        URL resource = HWU2000SDHMigrator.class.getClassLoader().getResource("META-INF/persistence.xml");
        System.out.println("resource = " + resource);
        String fileName=  "D:\\hwu2000-mt.db";
//        fileName = "/home/zongyu/opt/hwu2000-mt.db";
        System.out.println(fileName);
        String emsdn = "Huawei/U2000";
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

        HWU2000SDHMigrator loader = new HWU2000SDHMigrator (fileName, emsdn){
            public void afterExecute() {
                updateEmsStatus(Constants.CEMS_STATUS_READY);
                printTableStat();
                IrmsClientUtil.callIRMEmsMigrationFinished(emsdn);
            }
        };
        loader.execute();
    }


}
