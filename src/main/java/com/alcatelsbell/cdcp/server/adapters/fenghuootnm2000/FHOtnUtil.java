package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.adapters.LinkInfo;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.*;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.log4j.Logger;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.R_TrafficTrunk_CC_Section;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;

import java.util.*;

/**
 * Created by Administrator on 2015/1/12.
 */
public class FHOtnUtil {

    public static  void testOTN(SqliteDelegation sd,String emsdn,HashMap<String,List<CCTP>> ctpParentChildMap) {
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
        List<CPath_CC> cpathccs = new ArrayList<CPath_CC>();
        List<CPath_Channel> cpathChannels = new ArrayList<CPath_Channel>();
        List<CPath_Section> cPath_sections = new ArrayList<CPath_Section>();
        List<CChannel> channels = new ArrayList<CChannel>();
        HashMap<String,CSection> omsMap = new HashMap<String, CSection>();


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
            if (och.getDn().equals("EMS:SHX-OTNM2000-1-OTN@MultiLayerSubnetwork:1@SubnetworkConnection:504367806_520096575"))
                System.out.println();

            CPath cPath = U2000MigratorUtil.transPath(emsdn, och);
            cPath.setTmRate("40G");
            cPath.setRateDesc("OCH");
            cPath.setCategory("OCH");

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
                    CSection oms = new CSection();
                    oms.setDn(DNUtil.extractPortDn(lastNode)+"_"+DNUtil.extractPortDn(routea));
                    oms.setAendTp(DNUtil.extractPortDn(lastNode));
                    oms.setZendTp(DNUtil.extractPortDn(routea));
                    if (!omsMap.containsKey(oms.getDn())) {
                        omsMap.put(oms.getDn(),oms);
                        oms.setDirection(0);
                        oms.setEmsName(emsdn);
                        oms.setSpeed("40G");
                        oms.setRate("41");
                    }

                    //sncChannelList.add(lastNode + "<>" + routea);
                    lastNode = routez;
                }

                else if (getCTPType(lastNode).equals("och") && getCTPType(routea).equals("oms")) {
                    getLogger().error("异常!找到OCH-OMS，snc="+och.getDn()+" route="+route.getId());
                    continue;
                }







            }

            LinkInfo ochSection = findOCHSection(lastNode, ochAend, sections);
            if (ochSection != null)  {
            //    sncSectionList.add(ochSection.dn);
            //    getLogger().error("路由搜索成功:  channel="+sncChannelList.size()+",section="+sncSectionList.size()+","+och.getDn());
            } else {
                getLogger().error("最后一段路由无法找到");
            }


         }
        List<CRoute> routes = new ArrayList<CRoute>();
        List<CRoute_CC> route_ccs = new ArrayList<CRoute_CC>();
        List<CRoute_Channel> route_channels = new ArrayList<CRoute_Channel>();
        List<CRoute_Section> route_sections = new ArrayList<CRoute_Section>();

        for (SubnetworkConnection dsr : dsrList) {
            CRoute cRoute = U2000MigratorUtil.transRoute(emsdn, dsr);
            cRoute.setCategory("DSR");
            routes.add(cRoute);


            List<String> parentDns = getParentSncDn(dsr);
            List<R_TrafficTrunk_CC_Section> dsrRoutes = routeMap.get(dsr.getDn());
            if (!parentDns.isEmpty()) {
                for (String parentDn : parentDns) {


                    SubnetworkConnection odusnc = sncMap.get(parentDn);
                    List<R_TrafficTrunk_CC_Section> oduRoutes = routeMap.get(parentDn);
                    for (R_TrafficTrunk_CC_Section oduRoute : oduRoutes) {
                        if (oduRoute.getType().equals("CC")) {
                            CCrossConnect cc = makeupCC(oduRoute.getaEnd(), oduRoute.getzEnd(), emsdn);
                            ccsToMakeup.add(cc);
                            route_ccs.add(U2000MigratorUtil.createCRoute_CC(emsdn, cc.getDn(), cRoute));
                        }



                    }


                }
            }  else {
                getLogger().error("无法找到ChannelIdList dsr="+dsr.getDn());
            }

    }


    }

    private static List<String> getParentSncDn(SubnetworkConnection snc) {
        HashMap<String, String> addMap = MigrateUtil.transMapValue(snc.getAdditionalInfo());
        String parentId = addMap.get("ChannelIdList");
        List<String> dns = new ArrayList<String>();
        if (parentId != null) {
            String[] parentIds = parentId.split("|");
            for (String  id : parentIds) {
                dns.add(snc.getDn().substring(0, snc.getDn().lastIndexOf(":") + 1) + id);
            }

        }
        return null;
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

    public static Logger getLogger() {
        return MigrateThread.thread().getLogger();
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

    private static CCrossConnect makeupCC(String aend,String zend,String emsdn) {
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

    private CChannel createCChanell(BObject parent,CCTP acctp, CCTP zcctp,String emsdn) {
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

   //     cChannel.setWaveLen( HwDwdmUtil.getWaveLength( (acctp.getFrequencies())));

//        if (ctp != null)
//            cChannel.setDirection(ctp.getDirection());
        cChannel.setDirection(DicConst.CONNECTION_DIRECTION_CD_BI);
        cChannel.setAptp(acctp.getParentDn());
        cChannel.setZptp(zcctp.getParentDn());

//        CPTP aptp = ptpMap.get(acctp.getParentDn());
//        CPTP zptp = ptpMap.get(zcctp.getParentDn());
//        if (aptp != null && zptp != null)
//            cChannel.setTag3(aptp.getTag3()+"-"+zptp.getTag3());
        cChannel.setEmsName(emsdn);
        return cChannel;
    }


}
