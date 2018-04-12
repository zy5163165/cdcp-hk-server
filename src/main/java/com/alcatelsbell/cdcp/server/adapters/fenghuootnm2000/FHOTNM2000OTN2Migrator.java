package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.common.Constants;
import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DSUtil;
import com.alcatelsbell.cdcp.util.DataInserter;
import com.alcatelsbell.cdcp.util.MigrateThread;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.db.components.service.JPASupportSpringImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.Equipment;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.util.FileLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2015/1/14
 * Time: 18:28
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FHOTNM2000OTN2Migrator extends FHOTNM2000OTNMigrator {
    private Log logger = LogFactory.getLog(getClass());
    private HashMap<String,List<CCrossConnect>> aendCCMap = new HashMap<String, List<CCrossConnect>>();
    private HashMap<String,List<CSection>> aendSectionMap = new HashMap<String, List<CSection>>();
 //   private HashMap<String,CSection> zendSectionMap = new HashMap<String, CSection>();
    private HashMap<String,List<CCrossConnect>> zendCCMap = new HashMap<String, List<CCrossConnect>>();
    private List<COMS_Section> omsSectionList = new ArrayList<COMS_Section>();
    private List<COMS_CC> comsCCList = new ArrayList<COMS_CC>();
    List<CPath_CC> cpathccs = new ArrayList<CPath_CC>();
    List<CPath_Channel> cpathChannels = new ArrayList<CPath_Channel>();
    List<CPath_Section> cPath_sections = new ArrayList<CPath_Section>();
    List<CCrossConnect> ccsToMakeup = new ArrayList<CCrossConnect>();

    List<CRoute> cRoutes = new ArrayList<CRoute>();
    List<CRoute_CC> route_ccs = new ArrayList<CRoute_CC>();
    List<CRoute_Channel> route_channels = new ArrayList<CRoute_Channel>();
    List<CRoute_Section> route_sections = new ArrayList<CRoute_Section>();

    public FHOTNM2000OTN2Migrator(String fileUrl, String emsdn) {
        super(fileUrl, emsdn);
    }


    public FHOTNM2000OTN2Migrator(Serializable object, String emsdn) {
         super(object,emsdn);
    }



    HashMap<String,CPath> cpathMap = new  HashMap<String,CPath> ();
    HashMap<String,List<CChannel>> pathSubWaveMap = new HashMap<String, List<CChannel>>();

    @Override
    public void doExecute() throws Exception {
        checkEMS(emsdn, "烽火");
        //   FHOtnUtil.testOTN(sd);
        logAction(emsdn + " migrateManagedElement", "同步网元", 1);
        //test();
        migrateManagedElement();
        migrateSubnetwork();
        logAction("migrateEquipmentHolder", "同步槽道", 5);
        migrateEquipmentHolder();
        logAction("migrateEquipment", "同步板卡", 10);
        migrateEquipment();
        logAction("migratePTP", "同步端口", 20);
        migratePTP();
        migrateSection();
        logAction("migrateCTP", "同步CTP", 25);
        migrateCTP();

            migrateCC();

        searchSncs();
            //migrateOms
        sd.release();

    }

    @Override
    public CSection transSection(Section section) {
        CSection cSection = super.transSection(section);
        if (aendSectionMap.containsKey(cSection.getAendTp())) {
            System.out.println("duplicate section aend : "+cSection.getAendTp());
        }
        DSUtil.putIntoValueList(aendSectionMap,cSection.getAendTp(),cSection);
   //     aendSectionMap.put(cSection.getAendTp(),cSection);
      //  zendSectionMap.put(cSection.getZendTp(),cSection);
        return cSection;
    }

    private CSection getSectionByAend(String aend) {
        List<CSection> cSections1 = aendSectionMap.get(aend);
        if (cSections1 != null && cSections1.size() > 0)
            return cSections1.get(0);
        return null;
    }
    private List<CSection> getSectionsByAend(String aend) {
        List<CSection> cSections1 = aendSectionMap.get(aend);

        return cSections1;
    }

    private void searchSncs() {
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

        Collection<CCTP> values = ctpMap.values();
        int routeNumber = 0;
        //DSR ctp ，搜索的起点
        List<CCTP> dsrCtps = new ArrayList<CCTP>();
        for (CCTP cctp : values) {
            if (cctp.getDn().endsWith("dsr=1") && !cctp.getDn().contains("FTP")) {
                dsrCtps.add(cctp);
            }
        }

        List<CCTP> starts = new ArrayList<CCTP>(dsrCtps);
        List<String> processedDsrs = new ArrayList<String>();
        System.out.println("starts dsrs = " + starts.size());
        for (CCTP start : starts) {
            if (processedDsrs.contains(start.getDn()))
                continue;
            List<CCrossConnect> processedCCs = new ArrayList<CCrossConnect>();
            CCTP zCtp = searchDSRRoute(start, processedCCs);
            processedDsrs.add(start.getDn());
            if (zCtp != null) {
                processedDsrs.add(zCtp.getDn());
                routeNumber ++;
            }
        }


        System.out.println("routeNumber = " + routeNumber);
        System.out.println("omsMap = " + omsMap.size());

        try {
            DataInserter di = new DataInserter(emsid);
            try {
                di.insertWithDupCheck(ccsToMakeup);
                di.insertWithDupCheck(sectionsToMakeup);
                di.updateByDn(new ArrayList(sectionsToUpdate));
                di.insert(new ArrayList(this.cpathMap.values()));
                di.insert(channels);
                di.insert(cRoutes);
                di.insert(cpathccs);
                di.insert(cpathChannels);
                di.insert(cPath_sections);
                di.insert(route_ccs);
                di.insert(route_channels);
                di.insert(route_sections);
            } catch (Exception e) {
                getLogger().error(e, e);
            }

            di.end();
        } catch (Exception e) {
            getLogger().error(e, e);
        }


    }

    private HashMap<String,CEquipment> equipmentMap = new HashMap<String, CEquipment>();
    @Override
    public CEquipment transEquipment(Equipment equipment) {
        CEquipment cEquipment = super.transEquipment(equipment);
        equipmentMap.put(cEquipment.getDn(),cEquipment);
        return cEquipment;
    }
    private void logStack(String s) {
         getLogger().info(s);
      //  System.out.println(s);
    }


    private CCTP searchDSRRoute(CCTP start,List<CCrossConnect> processedCCs ) {

        SearchContext searchContext = new SearchContext();
        String oddOrEvenNumber = null;
        String currentTP = start.getDn();
        String zend = null;
        String ochName = null;
        String oduName = null;
        String latestVDCard = "";
        String dsrNumber = "1";

        HashSet<CChannel> channelList = new HashSet<CChannel>();
        boolean nextTP = true;
        int count = 0;
        while (nextTP) {

            if (count ++ > 100) break;
            String _cardDn = DNUtil.extractCardDn(currentTP);
            CEquipment _card = equipmentMap.get(_cardDn);
            if (_card != null) {
                if (_card.getNativeEMSName().contains("VMU"))
                    latestVDCard = "VMU";
                else if (_card.getNativeEMSName().contains("ODU")) {
                    latestVDCard = "ODU";
                }
            }
            if (currentTP.equals("EMS:JH-OTNM2000-2-OTN@ManagedElement:134217743;65794@FTP:/rack=3329/shelf=2/slot=5243940/port=2   "))
                System.out.println();
            logStack("current="+currentTP);
            String cardDn = DNUtil.extractCardDn(currentTP);
            CEquipment card = equipmentMap.get(cardDn);
            if (card != null) {
                logStack("currentCard="+card.getNativeEMSName()+"\n");
            }
           // logStack("current=" + currentTP + "\n");

            if (currentTP.contains("CTP") && currentTP.contains("dsr="))
                dsrNumber = currentTP.substring(currentTP.indexOf("dsr=")+4);

            nextTP = false;
            if (isCTP(currentTP) && !currentTP.contains("FTP") && currentTP.contains("dsr=")) {

                if (!currentTP.equals(start.getDn())) {
                    //如果与开始搜索的dsr并非同一个，则表示为zend
                    zend = currentTP;
                    getLogger().info("！找到路由: aend = "+start+" zend="+zend);
                }
                List<CCrossConnect> ccs = aendCCMap.get(currentTP);
                if (ccs != null && ccs.size() > 0) {
                    for (CCrossConnect cc : ccs) {
                        if (!searchContext.ccs.contains(cc)) {
                            currentTP = cc.getZend();

                            searchContext.addCC(cc);
                            logStack(cc.getAend()+" -<<<<<<<<>>>>>>- "+cc.getZend());

                            nextTP = true;
                            break;
                        }
                    }


                } else {
                 //   getLogger().error("DSR无法找到下一个CC: ctp="+currentTP);
                }






                //FTP下的CTP两种情况，一种是正向找的时候，下一跳应该是FTP，另一种是反向找回的时候，下一跳应该是DSR
            } else if (isCTP(currentTP) && currentTP.contains("FTP")) {

                //看下一跳是不是dsr
                List<CCrossConnect> ccs = aendCCMap.get(currentTP);

                if (ccs != null) {
                    for (CCrossConnect cc : ccs) {
                        if (!searchContext.ccs.contains(cc)) {
                            //找到下一跳是DSR终点的
                            currentTP = cc.getZend();
                            searchContext.addCC(cc);
                            logStack(cc.getAend()+" -<<<<<<<<>>>>>>>>- "+cc.getZend());

                            nextTP = true;
                            break;
                        }
                    }
                }
                if (nextTP)
                    continue;


                //看下一跳是不是FTP
                String ftp = DNUtil.extractPortDn(currentTP);
                ccs = aendCCMap.get(ftp);
                if (ccs != null) {
                    for (CCrossConnect cc : ccs) {
                        if (!searchContext.ccs.contains(cc)) {
                            //找到下一跳是DSR终点的
                            currentTP = cc.getZend();
                            searchContext.addCC(cc);

                            nextTP = true;
                            break;
                        }
                    }
                }



            }  else if (!isCTP(currentTP) && currentTP.contains("FTP")) {
                //当前是FTP，找下一个FTP 或者 找FTP下面的dsr,如果dsrnumber有值的话

                List<CCrossConnect> ccs = aendCCMap.get(currentTP);
                if (ccs != null && ccs.size() > 0) {
                    for (CCrossConnect cc : ccs) {
                        if (!searchContext.ccs.contains(cc)) {
                            nextTP = true;
                            searchContext.addCC(cc);
                            logStack(cc.getAend()+" -<<<<<>>>>>>- "+cc.getZend());

                            currentTP = cc.getZend();
                            break;
                        }
                    }
                }

                if (!nextTP && dsrNumber != null){
                    List<CCTP> cctps = ptp_ctpMap.get(currentTP);
                    if (cctps != null) {
                        for (CCTP cctp : cctps) {
                            if (cctp.getDn().contains("dsr=") && cctp.getDn().substring(cctp.getDn().indexOf("dsr=") + 4).equals(dsrNumber)) {
                                nextTP = true;
                                currentTP = cctp.getDn();
                                break;
                            }
                        }
                    }
                }


            }   else if (isCTP(currentTP) && currentTP.contains("odu")) {

                try {
                    oduName = DNUtil.extractCTPSimpleName(currentTP);
                } catch (Exception e) {
                    getLogger().error(e,e);
                }

                if (!latestVDCard.equals("ODU")) {
                    String ochPtp = currentTP.substring(0,currentTP.indexOf("/",currentTP.indexOf("och=")));
                    CSection cSection = getSectionByAend(ochPtp);
                    if (cSection != null && !searchContext.sectionList.contains(cSection)) {
                        nextTP = true;
                        searchContext.addSection(cSection);
                        logStack(cSection.getAendTp()+" - -************- - "+cSection.getZendTp());
                        currentTP = cSection.getZendTp();

                    }
                }

                //**

                if (!nextTP ) {

                    List<CCrossConnect> ccs = aendCCMap.get(currentTP);
                    if (ccs != null) {
                        for (CCrossConnect cc : ccs) {
                            if (!searchContext.ccs.contains(cc)) {
                                nextTP = true;
                                searchContext.addCC(cc);
                                logStack(cc.getAend() + " -<<<<>>>>>- " + cc.getZend());

                                currentTP = cc.getZend();
                                break;
                            }
                        }
                    }
                }


                if (!nextTP) {
                    String ptp = DNUtil.extractPortDn(currentTP);
                    String ftp = ptp.replaceAll("PTP","FTP");
                    List<CCrossConnect> cCrossConnects = aendCCMap.get(ftp);
                    if (cCrossConnects != null) {
                        CCrossConnect cc = null;
                        if (cCrossConnects.size() == 1) {
                             cc = cCrossConnects.get(0);
                        }
                        else for (CCrossConnect c : cCrossConnects) {
                            if (c.getZend().contains("dsr") && c.getZend().substring(c.getZend().indexOf("dsr=") + 4).equals(dsrNumber)) {
                                cc = c;
                                break;
                            }
                        }
                        if (cc != null) {
                            nextTP = true;
                            searchContext.addCC(cc);
                            logStack(cc.getAend() + " -<<<<>>>>>- " + cc.getZend());

                            currentTP = cc.getZend();
                        }
                    }
                }



                //如果是OCH的ctp，且非oms下的och
            }  else if (isCTP(currentTP) && !currentTP.contains("oms=")  && currentTP.contains("och=") && !currentTP.substring(currentTP.indexOf("och=")).contains("/")) {
                if (latestVDCard.equals("ODU")) {
                    //刚经过分波，要去找子ctp odu

                        List<CCTP> cctps = ctpParentChildMap.get(currentTP);

                        for (CCTP cctp : cctps) {
                            try {
                                if (DNUtil.extractCTPSimpleName(cctp.getDn()).equals(oduName)){
                                    nextTP = true;
                                    currentTP = cctp.getDn();
                                }
                            } catch (Exception e) {
                                getLogger().error(e,e);
                            }
                        }

                } else {


                    //如果是OCH的ctp，且非oms下的och
                    List<CCrossConnect> ccs = aendCCMap.get(currentTP);
                    if (ccs != null) {
                        for (CCrossConnect cc : ccs) {
                            if (!searchContext.ccs.contains(cc)) {
                                nextTP = true;
                                searchContext.addCC(cc);
                                logStack(cc.getAend() + " -<<<<<<>>>>- " + cc.getZend());

                                currentTP = cc.getZend();
                                break;
                            }
                        }
                    }

                    //如果未找到交叉
                    if (!nextTP) {
                        CSection cSection = getSectionByAend(currentTP);
                        if (cSection != null && !searchContext.sectionList.contains(cSection)) {
                            nextTP = true;
                            searchContext.addSection(cSection);
                            logStack(cSection.getAendTp() + " - -************- - " + cSection.getZendTp());
                            currentTP = cSection.getZendTp();

                        }
                    }
                }



            } else if (isCTP(currentTP) && currentTP.contains("oms=") && currentTP.contains("och=")) {


                if (latestVDCard.equals("VMU")) {
                    searchContext.computer.startOms(currentTP);
                }
                if (latestVDCard.equals("VMU")) {
                    searchContext.computer.endOms(currentTP);
                }
                try {
                    String ctpName = DNUtil.extractCTPSimpleName(currentTP);
                    ochName = ctpName;
                } catch (Exception e) {
                    getLogger().error(e,e);
                }

//                CCrossConnect makeupCC = makeupCC(currentTP, currentTP.substring(0, currentTP.indexOf("/och=")), emsdn);
//                ccsToMakeup.add(makeupCC);
//                searchContext.addCC(makeupCC);

                currentTP = DNUtil.extractPortDn(currentTP);
                CSection section = getSectionByAend(currentTP);

                if (section != null) {
                    if (!searchContext.sectionList.contains(section)) {
                        searchContext.addSection(section);
                        nextTP = true;
                        currentTP = section.getZendTp();
                        logStack(section.getAendTp() + " - -************- " + section.getZendTp());
                    }
                }

//                else {
//                         section = zendSectionMap.get(currentTP);
//                    if (section != null && !sectionList.contains(section)) {
//                        sectionList.add(section);
//                        nextTP = true;
//                        currentTP = section.getAendTp();
//                        logStack(section.getAendTp()+" -section- "+section.getAendTp());
//                    }
//                }


                //如果是纯PTP
            } else if (!isCTP(currentTP)) {

                if (_card != null) {
                    if (_card.getNativeEMSName().equals("ITL50_OTN")) {  //奇偶合波板
                        String portNumber = DNUtil.extractPortNumber(currentTP);
                        String lastTp = currentTP;
                        if (oddOrEvenNumber == null) {
                            oddOrEvenNumber = portNumber ;
                            currentTP = currentTP.substring(0,currentTP.lastIndexOf("/"))+"/port=1";
                        } else {
                            currentTP = currentTP.substring(0,currentTP.lastIndexOf("/"))+"/port="+oddOrEvenNumber;
                        }

                        CSection sc = makeupSection(lastTp, currentTP);
                        searchContext.addSection(sc);

                    }


                    /**
                     *   LINE I/O：1550nm信号光的输入输出口，其中I来自于OBA(或OLA)单盘，O输出到OPA(或OLA)单盘；（PB1）
                         MAIN I/O：主用光纤线路的输入/输出口；（PB2）
                         PROT I/O：备用光纤线路的输入输出口；（PB3）
                     */
                    if (_card.getNativeEMSName().equals("OLP_OTN")) {
                        String nb = DNUtil.extractPortNumber(currentTP);
                        String lastTp = currentTP;
                        if (nb.equals("1")) { //LINE
                            currentTP = currentTP.substring(0,currentTP.lastIndexOf("/"))+"/port=2";
                        } else if (nb.equals("2")) {   //MAIN
                            currentTP = currentTP.substring(0,currentTP.lastIndexOf("/"))+"/port=1";
                        } else if (nb.equals("3")) {   //PORT
                            currentTP = currentTP.substring(0,currentTP.lastIndexOf("/"))+"/port=1";
                        }

                        CSection sc = makeupSection(lastTp, currentTP);
                        searchContext.addSection(sc);

                    }

                    /**
                     *  入端口，找到对应的分波CTP
                     */
                    if (_card.getNativeEMSName().contains("ODU")) {

                        List<CCTP> cctps = ptp_ctpMap.get(currentTP);
                        for (CCTP cctp : cctps) {
                            try {
                                if (DNUtil.extractCTPSimpleName(cctp.getDn()).equals(ochName)) {
                                    currentTP = cctp.getDn();
                                    nextTP = true;
                                }
                            } catch (Exception e) {
                               getLogger().error(e,e);
                            }
                        }
                    }

                }

                CSection section = getSectionByAend(currentTP);
                if (section != null) {
                    if (!searchContext.sectionList.contains(section)) {
                        searchContext.addSection(section);
                        nextTP = true;
                        currentTP = section.getZendTp();

                        if (_card.getNativeEMSName().equals("OLP_OTN")) {
                            logStack(section.getAendTp() + " -###################################- " + section.getZendTp());
                        }  else
                            logStack(section.getAendTp() + " -************- " + section.getZendTp());
                    }
                }
            }





            if (!nextTP) {
                if (zend == null)
                    getLogger().error("查找失败 count="+count+" nextTP = " + currentTP + " starttp = "+start.getDn());
            }


            //各种情况都处理完



        }

        if (zend != null) {
            getLogger().error("查找成功 count="+count+" start = "+start.getDn());
            searchContext.end();
            CRoute route = U2000MigratorUtil.createRoute(emsdn, start.getDn(), zend, ctpMap);
            route.setCategory("DSR");
            cRoutes.add(route);
            for (CCrossConnect cc : searchContext.ccs) {
                if (cc != null)
                    this.route_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn,cc.getDn(),route));
                else
                    System.out.println();
            }

            for (CChannel subWave : searchContext.subWaves) {
                this.route_channels.add(U2000MigratorUtil.createCRoute_Channel(emsdn, subWave, route));
            }

            for (CSection section : searchContext.sectionList) {
                 this.route_sections.add(U2000MigratorUtil.createCRoute_Section(emsdn,section.getDn(),route));
            }


            return ctpMap.get(zend);
        } else
            return null;

    }

    private boolean isCTP(String dn) {
        return dn.contains("@CTP");
    }

    private CSection getSection(Collection<CSection> cs,String atp,String ztp) {
        for (CSection c : cs) {
            if (c.getAendTp().equals(atp) && c.getZendTp().endsWith(ztp))
                return c;
            if (c.getAendTp().equals(ztp) && c.getZendTp().endsWith(atp))
                return c;
        }

        return null;
    }



    private CCrossConnect getRevertCC(CCrossConnect cc) {
        String aend = cc.getAend();
        String zend = cc.getZend();
        List<CCrossConnect> ccs = aendCCMap.get(zend);
        if (ccs != null) {
            for (CCrossConnect cCrossConnect : ccs) {
                if (cCrossConnect.getZend().equals(aend))
                    return cCrossConnect;
            }
        }
        return null;
    }
//    private CSection getRevertSection(CSection cc) {
//        String aend = cc.getAendTp();
//        String zend = cc.getZendTp();
//        CSection ccs = aendSectionMap.get(zend);
//        if (ccs != null) {
//            return ccs;
//        }
//        return null;
//    }





    protected void migrateCC() throws Exception {
        executeDelete("delete from CCrossConnect c where c.emsName = '" + emsdn + "'", CCrossConnect.class);
        DataInserter di = new DataInserter(emsid);
        List<CCrossConnect> newCCs = new ArrayList<CCrossConnect>();
        try {
            List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
            if (ccs != null && ccs.size() > 0) {
                for (CrossConnect cc : ccs) {
//                    if (cc.getTag1() != null && cc.getTag1().equals("EXT") &&
//                            ( (!isCTP(cc.getaEndNameList()) && !cc.getaEndNameList().contains("FTP")) || (!isCTP(cc.getzEndNameList() )&& !cc.getzEndNameList().contains("FTP"))))
//                        continue;
                    cc.setDn(DNUtil.compressCCDn(cc.getDn()));


                    List<CCrossConnect> splitCCS = OTNM2000MigratorUtil.transCCS(cc, emsdn);
                    newCCs.addAll(splitCCS);

                    for (CCrossConnect ncc : splitCCS) {
                        DSUtil.putIntoValueList(aptpCCMap,ncc.getAptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getZptp(),ncc);
                        DSUtil.putIntoValueList(ptpCCMap,ncc.getAptp(),ncc);

                        DSUtil.putIntoValueList(aendCCMap,ncc.getAend(),ncc);
                        DSUtil.putIntoValueList(zendCCMap,ncc.getZend(),ncc);

                        if (ncc.getDn().equals("EMS:JH-OTNM2000-2-OTN@ManagedElement:134217757;67842@CrossConnect:/rack=6913/shelf=1/slot=24118294/port=1/och=1_/rack=7681/shelf=3/slot=5243972/port=1"))
                            System.out.println();

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

    public class OCHComputer {
        public boolean omsOpen = false;

        private boolean pathOpen = false;

        public List<CChannel> subWvs = new ArrayList<CChannel>();

      //  private String startOmsPort = null;
        private String startOch = null;

        private String startPathOdu = null;
        public void startOms(String ochCtp) {

            omsOpen = true;
            startOch = ochCtp;
        }

        public void endOms(String ochCtp) {
            omsOpen = false;

            if (startOch != null) {
                CSection oms = processOMSAndSplitWaves(startOch.substring(0,startOch.indexOf("/och=")), ochCtp.substring(0,ochCtp.indexOf("/och=")));

                for (CSection section : t_omsSections) {
                    COMS_Section coms_section = new COMS_Section();
                    coms_section.setSectiondn(section.getDn());
                    coms_section.setOmsdn(oms.getDn());
                    coms_section.setEmsName(emsdn);
                    coms_section.setDn(SysUtil.nextDN());
                    omsSectionList.add(coms_section);
                }
                t_omsSections.clear();

                for (CCrossConnect cc : t_omsCCs) {
                    COMS_CC coms_cc = new COMS_CC();
                    coms_cc.setCcdn(cc.getDn());
                    coms_cc.setOmsdn(oms.getDn());
                    coms_cc.setEmsName(emsdn);
                    coms_cc.setDn(SysUtil.nextDN());
                    comsCCList.add(coms_cc);
                }
                t_omsCCs.clear();

                List<CChannel> _waves = omsChannelMap.get(oms.getDn());
                for (CChannel wave : _waves) {
                    if (wave.getAend().equals(startOch))
                        t_waves.add(wave);
                }
            }

        }

        public boolean isPathOpen() {
            return pathOpen;
        }

        public void startPath(String oduPath) {
            pathOpen = true;
            startPathOdu = oduPath;
        }
        public CChannel endPath(String oduCtp) {
            pathOpen = false;

            String ochCtp = startPathOdu.substring(0, startPathOdu.indexOf("/odu"));
            if (ochCtp != null) {
                try {
                    String zochCtp = oduCtp.substring(0, oduCtp.indexOf("/odu"));
                    CPath cPath = processCPathAndSplitSubWaves(ochCtp, zochCtp);

                    for (CChannel t_wave : t_waves) {
                        CPath_Channel cPath_channel = U2000MigratorUtil.createCPath_Channel(emsdn,t_wave,cPath);
                        cpathChannels.add(cPath_channel);
                    }
                    t_waves.clear();

                    for (CSection t_pathSection : t_pathSections) {
                        cPath_sections.add(U2000MigratorUtil.createCPath_Section(emsdn, t_pathSection.getDn(), cPath));
                    }
                    t_pathSections.clear();

                    for (CCrossConnect t_pathCc : t_pathCcs) {
                        cpathccs.add(U2000MigratorUtil.createCPath_CC(emsdn,t_pathCc.getDn(),cPath));
                    }
                    t_pathCcs.clear();




                    List<CChannel> subWaves = pathSubWaveMap.get(cPath.getDn());
                    for (CChannel subWave : subWaves) {
                        if (subWave.getAend().equals(startPathOdu)) {
                            subWvs.add(subWave);
                            return subWave;
                        }
                    }


                } catch (Exception e) {
                    getLogger().error(e,e);
                }
            }
            return null;
        }


        public boolean isOmsOpen() {
            return omsOpen;
        }
        private HashSet<CChannel> t_waves = new HashSet<CChannel>();

        private HashSet<CSection> t_omsSections = new HashSet<CSection>();
        public void addOmsSection(CSection section) {
            t_omsSections.add(section);
        }

        private HashSet<CCrossConnect> t_omsCCs = new HashSet<CCrossConnect>();
        public void adOmsCC(CCrossConnect cc) {
             t_omsCCs.add(cc);

        }

        private HashSet<CSection> t_pathSections = new HashSet<CSection>();
        public void addPathSection(CSection section) {
            t_pathSections.add(section);
        }


        private HashSet<CCrossConnect> t_pathCcs = new HashSet<CCrossConnect>();
        public void addPathCC(CCrossConnect cc) {
            t_pathCcs.add(cc);

        }

    }

    public class SearchContext {
        public HashSet<CSection> sectionList = new HashSet<CSection>();
        public HashSet<CCrossConnect> ccs = new HashSet<CCrossConnect>();
        private HashSet<CChannel> subWaves = new HashSet<CChannel>();
        OCHComputer computer = new OCHComputer();
        public void addSection(CSection section)  {

            if  (computer.isOmsOpen())
                computer.addOmsSection(section);

            else if (computer.isPathOpen())
                computer.addPathSection(section);

            else
                sectionList.add(section);
        }



        public void addCC(CCrossConnect cc) {
            CCrossConnect revertCC = getRevertCC(cc);

            // port=3@CTP:/och=1/odu2=1   <> FTP:/rack=3073/shelf=1/slot=7341062/port=3
            if (  cc.getAend().contains("och=") && cc.getAend().contains("odu")) {
                CChannel subWave = computer.endPath(cc.getAend());
                if (subWave != null)
                    subWaves.add(subWave);
                else {
                    getLogger().error("无法找到subwave: end="+cc.getAend());
                }
            }


            if (computer.isOmsOpen()) {
                computer.adOmsCC(cc);
                if (revertCC != null)
                    computer.adOmsCC(revertCC);
            }
            else if (computer.isPathOpen()) {
                computer.addPathCC(cc);
                if (revertCC != null)
                    computer.addPathCC(revertCC);
            } else {
                ccs.add(cc);
                if (revertCC != null)
                    ccs.add(revertCC);
            }




            //FTP:/rack=3073/shelf=1/slot=7341062/port=3  <> port=3@CTP:/och=1/odu2=1
            if (  cc.getZend().contains("och=") && cc.getZend().contains("odu")) {
                computer.startPath(cc.getZend());
            }



        }

        public void end() {

        }
    }

    protected CPath processCPathAndSplitSubWaves(String aendOchTp,String zendOchTp) throws Exception {

        CPath cPath = U2000MigratorUtil.createPath(emsdn,aendOchTp,zendOchTp,ctpMap);
        if (ctpMap.get(aendOchTp) != null)
            cPath.setTmRate(ctpMap.get(aendOchTp).getTmRate());
        cPath.setRateDesc("OCH");
        cPath.setCategory("OCH");
        if (cpathMap.containsKey(cPath.getDn()))
            return cpathMap.get(cPath.getDn());
        cpathMap.put(cPath.getDn(),cPath);

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

        return cPath;

    }


    private CSection makeupSection(String aendTp,String zendTp) {
        CSection section = new CSection();
        section.setAendTp(aendTp);
        section.setZendTp(zendTp);
        section.setDirection(1);
        section.setDn(aendTp + "_" + zendTp);
        section.setTag1("MAKEUP");
        section.setRate("41");
        section.setEmsName(emsdn);
        section.setSpeed("40G");
        section.setType("OTS");
        sectionsToMakeup.add(section);
        return section;
    }


    public static void main(String[] args) throws Exception {
//        List allObjects = JpaClient.getInstance("cdcp.datajpa").findAllObjects(CDevice.class);
    //    String fileName=  "D:\\cdcpdb\\FH_2015-01-19-170000-JH-OTNM2000-2-OTN-DayMigration.db";
        String fileName = "d:\\cdcpdb\\FH_2015-01-20-233824-JH-OTNM2000-2-OTN-DayMigration.db";
        String emsdn = "JH-OTNM2000-2-OTN";
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

        FHOTNM2000OTN2Migrator loader = new FHOTNM2000OTN2Migrator (fileName, emsdn){
            public void afterExecute() {
                printTableStat();
            }
        };
        loader.execute();
        System.out.println("finished");
    }

}
