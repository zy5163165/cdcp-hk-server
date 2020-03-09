package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.nbi.ws.irmclient.IrmsClientUtil;
import com.alcatelsbell.cdcp.server.adapters.*;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CCrossConnect;
import com.alcatelsbell.cdcp.server.adapters.CacheClass.T_CTP;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-7
 * Time: 下午4:43
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FHOTNM2000DWDMMigrator extends AbstractDBFLoader{

    HashMap<String,List<CCTP>> ctpParentChildMap = new HashMap<String, List<CCTP>>();

    HashMap<String,CPTP>  ptpMap = new HashMap<String, CPTP>();
    HashMap<String,CCTP>  ctpMap = new HashMap<String, CCTP>();
    HashMap<String,List<CCrossConnect>> aptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CCrossConnect>> ptpCCMap = new HashMap<String, List<CCrossConnect>>();
    HashMap<String,List<CSection>> ptpSectionMap = new HashMap<String, List<CSection>>();
    HashMap<String,List<CCTP>> ptp_ctpMap = new HashMap<String, List<CCTP>>();
    List<CSection> cSections = new ArrayList<CSection>();
    HashMap<String,CEquipment> equipmentMap = new HashMap<String, CEquipment>();
    private HashMap<String,CChannel> ctpWaveMap = new HashMap<String, CChannel>();
    private HashMap<String,CChannel> ctpSubWaveMap = new HashMap<String, CChannel>();
    private HashMap<String,CCrossConnect>  ctpCCMap = new HashMap<String, CCrossConnect>();
    private List<CPath> cPathList = new ArrayList<CPath>();
    Map<String, CSection> waveSectionMap = new HashMap<String, CSection>();
    HashMap<String, List<CSection>> pathSectionMap = new HashMap<String, List<CSection>>();
    HashMap<String, List<CSection>> subwave_sectionMap = new HashMap<String, List<CSection>>();

    
    public FHOTNM2000DWDMMigrator(String fileUrl, String emsdn) {
        this.fileUrl = fileUrl;
        this.emsdn = emsdn;
        MigrateThread.thread().initLog(emsdn + "." + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");

    }

    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "烽火");

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
        
        logAction("migrateSection", "同步段", 25);
        migrateSection();

        logAction("migrateCTP", "同步CTP", 30);
        migrateCTP();

        logAction("migrateCC", "同步CC", 35);
        migrateCC();

        migrateOMS();
        sd.release();

    }
    protected Class[] getStatClss() {
        return        new Class[] { CCrossConnect.class, CChannel.class, CPath.class, CRoute.class, CPath_Channel.class,
                CPath_CC.class, CRoute_Channel.class,
                CRoute_CC.class, CSubnetwork.class, CSubnetworkDevice.class, CVirtualBridge.class,
                CMP_CTP.class, CEthTrunk.class, CStaticRoute.class, CEthRoute.class, CEthTrunk_SDHRoute.class,
                CEthRoute_StaticRoute.class, CEthRoute_ETHTrunk.class, CSection.class,CCTP.class,CDevice.class,CPTP.class,CTransmissionSystem.class,CTransmissionSystem_Channel.class};

    }





    public CdcpObject transEquipmentHolder(EquipmentHolder equipmentHolder) {
        CdcpObject cdcpObject = super.transEquipmentHolder(equipmentHolder);
        if (cdcpObject instanceof CSlot) {
             if (((CSlot) cdcpObject).getAcceptableEquipmentTypeList().length() > 2000)
                 ((CSlot) cdcpObject).setAcceptableEquipmentTypeList(null);
        }
        if (cdcpObject instanceof CShelf) {
            String additionalInfo = equipmentHolder.getAdditionalInfo();
            Map<String, String> map = MigrateUtil.transMapValue(additionalInfo);
            ((CShelf) cdcpObject).setShelfType(map.get("ProductName"));
            ((CShelf) cdcpObject).setNo(equipmentHolder.getNativeEMSName());
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
                        ctpCCMap.put(ncc.getAend(), ncc);
                        ctpCCMap.put(ncc.getZend(), ncc);
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
                    DSUtil.putIntoValueList(ptp_ctpMap, cctp.getParentDn(), cctp);
                    ctpMap.put(cctp.getDn(),cctp);
                    di.insert(cctp);
                }
            }
        }

        di.end();
        return cctps;
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
        

        List<CChannel> waveChannelList = null;
        try {



            List<CSection> omsList = new ArrayList<CSection>();
            List<CSection> updateOTS = new ArrayList<CSection>();
            List<COMS_CC> omsCClist = new ArrayList<COMS_CC>();
            List<COMS_Section> omsSectionList = new ArrayList<COMS_Section>();
            for (CSection cSection : cSections) {

                String aendTp = cSection.getAendTp();

                CPTP aptp = ptpMap.get(aendTp);
                if (isOMSPtp(aptp)) {
                	getLogger().info("OMS PTP："+aptp.getDn());
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
                if (acctps == null || acctps.isEmpty()) {
                    getLogger().error("无法找到CTP，端口："+aendTp);
                } else {
                    for (CCTP acctp : acctps) {
                        for (CCTP zcctp : zcctps) {


                            String och = DNUtil.extractOCHno(acctp.getDn());
                            String och2 = DNUtil.extractOCHno(zcctp.getDn());


                            if (och != null && och.equals(och2)) {
                            	CChannel channel = createCChanell(cSection,acctp, zcctp);
                                waveChannelList.add(channel);
                                ctpWaveMap.put(acctp.getDn(), channel);
                                ctpWaveMap.put(zcctp.getDn(), channel);
                                waveSectionMap.put(channel.getDn(), cSection);
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

        //find and create paths and routes
        try{
        	 createPaths(waveChannelList);    	
        }catch (Exception e) {
            getLogger().error(e, e);
        }

    }
    
    private boolean isOMSPtp(CPTP ptp){
    	List<CCTP> ctps = ptp_ctpMap.get(ptp.getDn());
    	if (ctps == null || ctps.isEmpty()){
    		return false;
    	}
    	for (CCTP ctp : ctps){
    		if (FHDwdmUtil.isOMSRate(ctp.getRate())){
    			return true;
    		}
    	}
    	return false;
    }
    
    private void createPaths(List<CChannel> waveChannelList) {   	
    	Collection<CChannel> highChannels = waveChannelList;
    	List<CPath_Channel> pathChannelList = new ArrayList<CPath_Channel>();
    	List<CPath_CC> pathCCList = new ArrayList<CPath_CC>();
    	List<CPath_Section> pathSectionList = new ArrayList<CPath_Section>();
    	List<CChannel> subWaveList = new ArrayList<CChannel>();
    	if (highChannels == null || highChannels.isEmpty()){
    		return;
    	}
    	Map<String, CPath> channel_pathMap = new HashMap<String, CPath>();
    	
    	for (CChannel channel : highChannels){
    		if (channel_pathMap.containsKey(channel.getDn())){
    			continue;
    		}
    		List<CChannel> pathChannels = new ArrayList<CChannel>();
    		List<CCrossConnect> pathCCs = new ArrayList<CCrossConnect>();    		
    		CCTP aSideCtp = null;
    		CCTP zSideCtp = null;
			try {
				aSideCtp = ctpMap.get(channel.getAend());
				zSideCtp = ctpMap.get(channel.getZend());
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
    		pathChannels.add(channel);   		
    //		getLogger().info("path channel aside ctp:" + aSideCtp.getDn());
    		CCTP aEndCtp = getNextChannel(aSideCtp, pathChannels, pathCCs,ctpWaveMap);
    		while (aEndCtp != null){
    			aEndCtp = getNextChannel(aEndCtp, pathChannels, pathCCs, ctpWaveMap);
    		}
    		if (aEndCtp == null){
    			aEndCtp = aSideCtp;   			
    		}
//    		getLogger().info("path aside end ctp:" + aEndCtp.getDn());
//    		getLogger().info(" path channel zside ctp:" + zSideCtp.getDn());
    		CCTP zEndCtp = getNextChannel(zSideCtp, pathChannels, pathCCs, ctpWaveMap);
    		while (zEndCtp != null){
    			zEndCtp = getNextChannel(zEndCtp, pathChannels, pathCCs, ctpWaveMap);
    		}
    		if (zEndCtp == null){
    			zEndCtp = zSideCtp;   			
    		}
//    		getLogger().info("path zside end ctp:" + zEndCtp.getDn());
    		//开始创建Path
    		CPath path = createCPath(aEndCtp, zEndCtp, channel);    		
    		cPathList.add(path);
    		for (CChannel pathChannel : pathChannels){
    			channel_pathMap.put(pathChannel.getDn(), path);
    			pathChannelList.add(OTNM2000MigratorUtil.createCPath_Channel(emsdn, pathChannel, path));
    			if (waveSectionMap.containsKey(pathChannel.getDn())){
    				pathSectionList.add(OTNM2000MigratorUtil.createCPath_Section(emsdn, waveSectionMap.get(pathChannel.getDn()).getDn(), path));
    				DSUtil.putIntoValueList(pathSectionMap, path.getDn(), waveSectionMap.get(pathChannel.getDn()));
    			}
    		}

    		for (CCrossConnect cc : pathCCs){
    			pathCCList.add(OTNM2000MigratorUtil.createCPath_CC(emsdn, cc.getDn(), path));
    		}
    		
    		List<CChannel> subwaves = createSubwaveChannels(path, aEndCtp, zEndCtp);
    		subWaveList.addAll(subwaves);
    	}
    	
    	getLogger().info("create path size:" + cPathList.size());
    	getLogger().info("create path_channel size:" + pathChannelList.size());
    	getLogger().info("create path_cc size:" + pathCCList.size());
    	getLogger().info("create path_section size:" + pathSectionList.size());
    	

        DataInserter di;
		try {
			di = new DataInserter(emsid);
	        di.insert(cPathList);
	        di.insert(pathChannelList);
	        di.insert(pathCCList);
	        di.insert(pathSectionList);
	        di.end();
		} catch (Exception e) {
            getLogger().error(e, e);
        }
   	
    	//create routes
    	createRoutes(subWaveList);
    }
    
    private void createRoutes(List<CChannel> subwaveList) {

    	Collection<CChannel> lowOrderChannels = subwaveList;
    	if (lowOrderChannels == null || lowOrderChannels.isEmpty()){
    		return;
    	}
    	Map<String, CRoute> channel_routeMap = new HashMap<String, CRoute>();
    	
    	List<CRoute> cRouteList = new ArrayList<CRoute>();
		List<CRoute_Channel> routeChannelList = new ArrayList<CRoute_Channel>();
		List<CRoute_CC> routeCCList = new ArrayList<CRoute_CC>();
		List<CRoute_Section> routeSectionList = new ArrayList<CRoute_Section>();
		for (CChannel channel : lowOrderChannels){
    		if (channel_routeMap.containsKey(channel.getDn())){
    			continue;
    		}
    		List<CChannel> routeChannels = new ArrayList<CChannel>();
    		List<CCrossConnect> routeCCs = new ArrayList<CCrossConnect>();    		
    		CCTP aSideCtp = null;
    		CCTP zSideCtp = null;
			try {
				aSideCtp = ctpMap.get(channel.getAend());
				zSideCtp = ctpMap.get(channel.getZend());
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
   // 		getLogger().info("route channel aside ctp:" + aSideCtp.getDn());
    		CCTP aEndCtp = getNextChannel(aSideCtp, routeChannels, routeCCs, ctpSubWaveMap);
    		while (aEndCtp != null){
    			aEndCtp = getNextChannel(aEndCtp, routeChannels, routeCCs, ctpSubWaveMap);
    		}
    		if (aEndCtp == null){
    			aEndCtp = aSideCtp;  			
    		}
//    		getLogger().info("route aside end ctp:" + aEndCtp.getDn());
//    		getLogger().info(" route channel zside ctp:" + zSideCtp.getDn());
    		CCTP zEndCtp = getNextChannel(zSideCtp, routeChannels, routeCCs, ctpSubWaveMap);
    		while (zEndCtp != null){
    			zEndCtp = getNextChannel(zEndCtp, routeChannels, routeCCs, ctpSubWaveMap);
    		}
    		if (zEndCtp == null){
    			zEndCtp = zSideCtp;
    			
    		}
//    		getLogger().info("route zside end ctp:" + zEndCtp.getDn());
    		//开始创建route
    		CRoute route = createCRoute(aEndCtp, zEndCtp, channel);
    		cRouteList.add(route);
    		for (CChannel routeChannel : routeChannels){
    			channel_routeMap.put(routeChannel.getDn(), route);
    			routeChannelList.add(OTNM2000MigratorUtil.createCRoute_Channel(emsdn, routeChannel, route));
    			List<CSection> sections = subwave_sectionMap.get(routeChannel.getDn());
    			if (!sections.isEmpty()){
    				for (CSection section : sections){
        				//routeSectionList
    					routeSectionList.add(OTNM2000MigratorUtil.createCRoute_Section(emsdn, section.getDn(), route));					
    				}

    			}
    		}
    		for (CCrossConnect cc : routeCCs){
    		    routeCCList.add(OTNM2000MigratorUtil.createCRoute_CC(emsdn, cc.getDn(), route));
    		}   		
    	}
		
    	getLogger().info("create route size:" + cRouteList.size());
    	getLogger().info("create route_channel size:" + routeChannelList.size());
    	getLogger().info("create route_cc size:" + routeCCList.size());
    	getLogger().info("create route_section size:" + routeSectionList.size());
		
        DataInserter di = null;
        try {
        	di = new DataInserter(emsid);
			di.insert(cRouteList);
	        di.insert(routeChannelList);
	        di.insert(routeCCList);
	        di.insert(routeSectionList);
	        di.end();     

		} catch (Exception e) {
            getLogger().error(e, e);
        }

    }
    
    /**
     * 递归寻找下一段channel
     * @param lastCtp，最后一个CTP
     * @param ctp，当前CTP
     * @param pathChannelList 
     * @param pathCCList
     * @return
     */
    private CCTP getNextChannel(CCTP ctp, List<CChannel> pathChannelList, List<CCrossConnect> pathCCList, Map<String, CChannel> channelMap){
    	CChannel nextChannel = null;
       	CCTP crossCtp = null; 
		CCrossConnect cc = null;
		CCTP nextChannelCtp = null;
		CCTP lastCtp = null;
		try {
			////找交叉连接的CTP
			cc = ctpCCMap.get(ctp.getDn());
    		if (cc != null){
    			if (!pathCCList.contains(cc)){
    				if (ctp.getDn().equals(cc.getAend())){
    					crossCtp = ctpMap.get(cc.getZend());    
    				} else {
    					crossCtp = ctpMap.get(cc.getAend());
    				}       										
    			}
    			if (crossCtp == null){
    				 getLogger().error("无法找到CrossConnect:" + cc.getDn() + "的Z端CTP:"+ cc.getZend());
    			}
    		} 
    		if (crossCtp != null){
    			if (!crossCtp.getRate().equals(ctp.getRate())){
    				crossCtp = null;
    			}
    		}
		} catch (Exception e) {
			 getLogger().error(e, e);
		}
	    ////找交叉连接的CTP完成
		
		////根据交叉连接找下一段Channel
		if (crossCtp != null){
//			getLogger().info("cc crossCtp:" + crossCtp.getDn());
			nextChannel = channelMap.get(crossCtp.getDn());
			if (nextChannel != null && !pathChannelList.contains(nextChannel) &&
					crossCtp.getRate().equals(nextChannel.getRate())){
				try {
					if (crossCtp.getDn().equals(nextChannel.getAend())){	
						nextChannelCtp = ctpMap.get(nextChannel.getZend());
					} else{
						nextChannelCtp = ctpMap.get(nextChannel.getAend());
					}
				} catch (Exception e) {
					getLogger().error(e, e);
				}
				if (nextChannelCtp != null){
					getLogger().info("next Channel Ctp:" + nextChannelCtp.getDn());
					pathChannelList.add(nextChannel);
					pathCCList.add(cc);
					return nextChannelCtp;
				}
			}
		} 
		return null;
    }

    /**
     * 根据一端CTP获取与其交叉连接的另外一端CTP
     * @param ctp
     * @return
     */
//    private CCTP getCrossCtp(CCTP ctp, List<CCrossConnect> pathCCList) {
//    	CCTP crossCtp = null; 
//    	String crossCtpdn = null;
//		List<CCrossConnect> ccs;
//		CCrossConnect cc = null;
//		try {
//			ccs = ptpCCMap.get(ctp.getDn());
//    		if (ccs != null && !ccs.isEmpty()){
//    			//认为一个ctp只有一个cc
//    			cc = ccs.get(0);
//    			if (ctp.getDn().equals(cc.getAend())){
//    				crossCtp = ctpMap.get(cc.getZend());
//    			} else {
//    				crossCtp = ctpMap.get(cc.getAend());
//    			}
//    			if (crossCtp == null){
//    				 getLogger().error("无法找到CrossConnect:" + cc.getDn() + "的Z端CTP:"+ crossCtpdn);
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

    /**
     * 
     * @param aCtp
     * @param zCtp
     * @param channel
     */
    private CPath createCPath(CCTP aCtp, CCTP zCtp, CChannel channel){
        CPath route = new CPath();
        route.setDn(aCtp.getDn() + "<>" + zCtp.getDn());
        route.setSid(DatabaseUtil.nextSID(CPath.class));
        route.setName(channel.getName());
        route.setRate(channel.getRate());
        route.setRateDesc(channel.getRateDesc());
        route.setTmRate(channel.getTmRate());
        route.setCategory("OCH");
        
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
    private CRoute createCRoute(CCTP aCtp, CCTP zCtp, CChannel channel){
        CRoute route = new CRoute();
        route.setDn(aCtp.getDn() + "<>" + zCtp.getDn());
        route.setSid(DatabaseUtil.nextSID(CRoute.class));
        route.setName(channel.getName());
        route.setRate(channel.getRate());
        route.setRateDesc(channel.getRateDesc());
        route.setTmRate(channel.getTmRate());
        route.setCategory("DSR");
        
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
                        CChannel subwaveChannel = createCChanell(path,actp,zctp);
                        subwaveChannels.add(subwaveChannel);
                        ctpSubWaveMap.put(actp.getDn(), subwaveChannel);
                        ctpSubWaveMap.put(zctp.getDn(), subwaveChannel); 
                        subwave_sectionMap.put(subwaveChannel.getDn(), pathSectionMap.get(path.getDn()));
                    }
                }

                if (!match) {
                    for (CCTP zctp : zctps) {
                    	 CChannel subwaveChannel = createCChanell(path,actp,zctp);
                        subwaveChannels.add(subwaveChannel);
                        ctpSubWaveMap.put(actp.getDn(), subwaveChannel);
                        ctpSubWaveMap.put(zctp.getDn(), subwaveChannel);  
                        subwave_sectionMap.put(subwaveChannel.getDn(), pathSectionMap.get(path.getDn()));
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

            cChannel.setWaveLen( FHDwdmUtil.getWaveLength( (acctp.getFrequencies())));

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
//        List allObjects = JpaClient.getInstance("cdcp.datajpa").findAllObjects(CDevice.class);
        String fileName=  "E:\\work\\2014-06-19-085642-HUZ-OTNM2000-7-P-DayMigration.db";
        String emsdn = "ZJ-OTNM2000-1-P";
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

        FHOTNM2000DWDMMigrator loader = new FHOTNM2000DWDMMigrator (fileName, emsdn){
            public void afterExecute() {
                updateEmsStatus(Constants.CEMS_STATUS_READY);
                printTableStat();
                IrmsClientUtil.callIRMEmsMigrationFinished(emsdn);
            }
        };
        loader.execute();
    }


}
