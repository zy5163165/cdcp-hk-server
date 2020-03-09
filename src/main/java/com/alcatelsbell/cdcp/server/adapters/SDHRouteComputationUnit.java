package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.CChannel;
import com.alcatelsbell.cdcp.nbi.model.CPath;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.HWDic;
import com.alcatelsbell.cdcp.util.BObjectMemTable;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import org.apache.log4j.Logger;
import org.asb.mule.probe.framework.entity.R_TrafficTrunk_CC_Section;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.service.Constant;

import static com.alcatelsbell.cdcp.server.adapters.CacheClass.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-9
 * Time: 下午5:17
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SDHRouteComputationUnit {

    private Logger logger = null;
    private List<R_TrafficTrunk_CC_Section> routes = null;
    private BObjectMemTable<T_CTP> ctpTable = null;
    private BObjectMemTable<T_CCrossConnect> ccTable = null;
    private List<CChannel> channels = new ArrayList<CChannel>();
    private String emsDn = null;
    private boolean isHighOrderPath;
    private HashMap<String,String> ctpDnHighoderpathDn = new HashMap<String, String>();
    private HashMap<String,CPath> pathMap = null;

    //计算出来的低阶时隙
    private HashMap<String,CChannel> channelMap = new HashMap<String, CChannel>();
    private SubnetworkConnection snc = null;


//    private HashMap<String,CChannel> highOrderChannelMap = new HashMap<String, CChannel>();

    private HashMap<String,CChannel> actpChannelMap = new HashMap<String, CChannel>();


    //如果是计算高阶通道的话，pathMap可为null
    public SDHRouteComputationUnit(Logger logger,SubnetworkConnection snc,
                                   List<R_TrafficTrunk_CC_Section> routes,
                                   BObjectMemTable<T_CTP> ctpTable,
                                   BObjectMemTable<T_CCrossConnect> ccTable,
                                   String emsDn, boolean isHighOrderPath,HashMap<String,CPath> pathMap) {
        this.logger = logger;
        this.routes = routes;
        this.ctpTable = ctpTable;
        this.emsDn = emsDn;
        this.isHighOrderPath = isHighOrderPath;
        this.snc = snc;
        this.ccTable = ccTable;


        this.pathMap = pathMap;
    }

    public void setACtpChannelMap(HashMap<String, CChannel> ctpChannelMap) {
        this.actpChannelMap = ctpChannelMap;
    }

    public HashMap<String, String> getCtpDnHighoderpathDn() {
        return ctpDnHighoderpathDn;
    }

    public void setCtpDnHighoderpathDn(HashMap<String, String> ctpDnHighoderpathDn) {
        this.ctpDnHighoderpathDn = ctpDnHighoderpathDn;
    }

    public List<CChannel> getChannels() {
        return channels;
    }

//    public void setHighOrderChannelMap(HashMap<String, CChannel> highOrderChannelMap) {
//        this.highOrderChannelMap = highOrderChannelMap;
//    }

    public void compute() throws Exception {
        if (routes == null) return;
//        try {
//            makeUpBothSideCCs();
//        } catch (Exception e) {
//            logger.error(e, e);
//        }
        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getType().equals("SECTION")) {
                String aptp = route.getaEnd();
                String zptp = route.getzEnd();
                if (isHighOrderPath) {     // 计算高阶通道的两端的第一根时隙
                    if (aptp.equals(snc.getaPtp())) {
                        for (String ctp : snc.getaEnd().split(Constant.listSplitReg))
                            createHighOrderChannel(ctp,zptp+"@CTP:"+DNUtil.extractCTPSimpleName(ctp),route);
                    }
                    if (zptp.equals(snc.getaPtp())) {
                        for (String ctp : snc.getaEnd().split(Constant.listSplitReg))
                            createHighOrderChannel(ctp,aptp+"@CTP:"+DNUtil.extractCTPSimpleName(ctp),route);
                    }
                    if (aptp.equals(snc.getzPtp())) {
                        for (String ctp : snc.getzEnd().split(Constant.listSplitReg))
                            createHighOrderChannel(ctp,zptp+"@CTP:"+DNUtil.extractCTPSimpleName(ctp),route);
                    }
                    if (zptp.equals(snc.getzPtp())) {
                        for (String ctp : snc.getzEnd().split(Constant.listSplitReg))
                            createHighOrderChannel(ctp,aptp+"@CTP:"+DNUtil.extractCTPSimpleName(ctp),route);
                    }
                }
//                List<String> aSideCtps = findRelatedCrossConnectCtpdns(aptp);
//                List<String> zSideCtps = findRelatedCrossConnectCtpdns(zptp);
//
//                try {
//                    createChannels(aSideCtps,zSideCtps,route);
//                } catch (Exception e) {
//                    logger.error(e, e);
//                }
            }
        }

        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getType().equals("CC")) {
                String aend = route.getaEnd();
                String zend = route.getzEnd();
                if (aend != null) {
                    String[] actps = aend.split(Constant.listSplitReg);
                    for (String actp : actps) {
                        CChannel cChannel = actpChannelMap.get(actp);
                        if (cChannel != null)
                            channelMap.put(cChannel.getDn(),cChannel);
                    }

                }
                if (zend != null) {
                    String[] zctps = zend.split(Constant.listSplitReg);
                    for (String zctp : zctps) {
                        CChannel cChannel = actpChannelMap.get(zctp);
                        if (cChannel != null)
                            channelMap.put(cChannel.getDn(),cChannel);
                    }
                }

            }
        }
        channels.addAll(channelMap.values());
    }




    private void createChannels(List<String> aSideCtps, List<String> zSideCtps,R_TrafficTrunk_CC_Section sectionRoute) throws Exception {
        if (!isHighOrderPath) {
            for (String aSideCtp : aSideCtps) {
                if (!SDHUtil.isVC4Ctp(aSideCtp)) {
                    createLowOrderChannel(aSideCtp);
                }
            }
            for (String zSideCtp : zSideCtps) {
                if (!SDHUtil.isVC4Ctp(zSideCtp)) {
                    createLowOrderChannel(zSideCtp);
                }
            }

        }  else {
            for (String aSideCtp : aSideCtps) {
                for (String zSideCtp : zSideCtps) {
                    String aSideCtpName = DNUtil.extractCTPSimpleName(aSideCtp);
                    String zSideCtpName = DNUtil.extractCTPSimpleName(zSideCtp);

                    if (aSideCtpName.equals(zSideCtpName)) {
                        createHighOrderChannel(aSideCtp, zSideCtp, sectionRoute);
                        ctpDnHighoderpathDn.put(aSideCtp,sectionRoute.getTrafficTrunDn());
                        ctpDnHighoderpathDn.put(zSideCtp,sectionRoute.getTrafficTrunDn());
                    }
                }
            }
        }

    }

    private void createLowOrderChannel(String aSideCtp) throws Exception {
        String vc4Ctp = SDHUtil.getVC4Ctp(aSideCtp);
        String vc4pathDn = ctpDnHighoderpathDn.get(vc4Ctp);
        if (vc4pathDn != null) {
            CPath cPath = pathMap.get(vc4pathDn);
            String simpleName = DNUtil.extractCTPSimpleName(aSideCtp);
            CChannel cChannel = new CChannel();
            cChannel.setDn(vc4pathDn+"<>"+simpleName);
            cChannel.setSid(DatabaseUtil.nextSID(CChannel.class));
            cChannel.setAend(cPath.getAend());
            cChannel.setZend(cPath.getZend());
            cChannel.setSectionOrHigherOrderDn(cPath.getDn());
            cChannel.setName(simpleName);
            cChannel.setNo(simpleName);
            String rate = HWDic.LR_VT2_and_TU12_VC12.value+"" ;
            if (aSideCtp.contains("tu12")) {
                rate = HWDic.LR_VT2_and_TU12_VC12.value+"" ;
            } else if (aSideCtp.contains("vc3"))
                rate = HWDic.LR_Low_Order_TU3_VC3.value+"" ;
            cChannel.setRate(rate);
            cChannel.setTmRate(SDHUtil.getTMRate(rate));
            cChannel.setRateDesc(SDHUtil.rateDesc(rate));
            cChannel.setDirection(cPath.getDirection());
            //cChannel.setDirection(DicConst.PTP_DIRECTION_BIDIRECTIONAL);
            cChannel.setAptp(cPath.getAptp());
            cChannel.setZptp(cPath.getZptp());
            cChannel.setCategory("SDH低阶时隙");
            cChannel.setEmsName(emsDn);
            channelMap.put(cChannel.getDn(), cChannel);

        } else {
            logger.error("无法找到高阶通道,ctp = "+aSideCtp);
        }
    }




    private void createHighOrderChannel(String aSideCtp, String zSideCtp, R_TrafficTrunk_CC_Section sectionRoute) throws Exception {
        CChannel cChannel = actpChannelMap.get(aSideCtp);
//        if (cChannel == null) {
//            cChannel = highOrderChannelMap.get(zSideCtp +"<>"+aSideCtp);
//        }

        if (cChannel == null) {
            logger.error("无法找到高阶时隙:actp="+aSideCtp+" zctp="+zSideCtp);
            return;
        }

 //       cChannel.setSectionOrHigherOrderDn(sectionRoute.getCcOrSectionDn());
        channelMap.put(cChannel.getDn(), cChannel);
    }


    private void createHighOrderChannel_Old(String aSideCtp, String zSideCtp, R_TrafficTrunk_CC_Section sectionRoute) throws Exception {
       String duplicateDn = (zSideCtp+"<>"+aSideCtp);
        if (channelMap.get(duplicateDn)!= null)
            return;

        T_CTP aCtp = ctpTable.findObjectByDn(aSideCtp);
        T_CTP zCtp = ctpTable.findObjectByDn(zSideCtp);
        T_CTP ctp = aCtp;
        if (aCtp == null && zCtp == null) {
            logger.error("无法找到aCTP："+aSideCtp+";无法找到CTP："+zSideCtp);
         //   throw new DataRelationshipException("无法找到aCTP："+aSideCtp+";无法找到CTP："+zSideCtp);
        }

        if (ctp == null && zCtp != null)
            ctp = zCtp;

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
        cChannel.setSectionOrHigherOrderDn(sectionRoute.getCcOrSectionDn());
        cChannel.setName(nativeEMSName);
        cChannel.setNo(nativeEMSName);
        cChannel.setRate(rate);
        cChannel.setCategory("SDH高阶时隙");
        cChannel.setTmRate(SDHUtil.getTMRate(rate));
        cChannel.setRateDesc(SDHUtil.rateDesc(rate));
//        if (ctp != null)
//            cChannel.setDirection(ctp.getDirection());
        cChannel.setDirection(DicConst.CONNECTION_DIRECTION_CD_BI);
        cChannel.setAptp(sectionRoute.getaEnd());
        cChannel.setZptp(sectionRoute.getzEnd());
        cChannel.setEmsName(emsDn);

        channelMap.put(cChannel.getDn(), cChannel);

    }

//    private String getHighOrderCtp(String aSideCtp) throws  Exception {
//       if (SDHUtil.isVC4Ctp(aSideCtp)) {
//           return null;
//           //throw new DataRelationshipException("尝试获取VC4 CTP的高阶CTP : ctp="+aSideCtp);
//       }
//        String vc4CtpDn = SDHUtil.getVC4Ctp(aSideCtp);
//
//        T_CTP vc4Ctp = ctpTable.findObjectByDn(vc4CtpDn);
//        if (vc4Ctp != null)
//            return vc4Ctp.getDn();
//        return null;
//    }
//
//
//    /**
//     *   查找和PTP关联的CC路由
//     * @param ptp
//     * @return
//     */
//    private List<R_TrafficTrunk_CC_Section> findRelatedCCRoutes(String ptp) {
//        List<R_TrafficTrunk_CC_Section> ccRoutes = new ArrayList<R_TrafficTrunk_CC_Section>();
//        for (R_TrafficTrunk_CC_Section route : routes) {
//            if (route.getType().equals("CC")) {
//                if (ptp.equals(route.getaPtp()) || ptp.equals(route.getzPtp())) {
//                    ccRoutes.add(route);
//                }
//            }
//        }
//        return ccRoutes;
//    }
    /**
     *   查找和PTP关联的CC路由上的ctp
     * @param ptp
     * @return
     */
    private List<String> findRelatedCrossConnectCtpdns(String ptp) {
        List<String> ctps = new ArrayList<String>();
        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getType().equals("CC")) {
                if (ptp.equals(route.getaPtp()) ) {
                    ctps.add(route.getaEnd());
                }

                if (ptp.equals(route.getzPtp())) {
                    ctps.add(route.getzEnd());
                }
            }
        }

        //查看snc两端ctp
        if (ctps.isEmpty()) {
            String sncACTP = snc.getaEnd();
            String sncZCTP = snc.getzEnd();
            if (ptp.equals(DNUtil.extractPortDn(sncACTP)))
                ctps.add(sncACTP);
            else if (ptp.equals(DNUtil.extractPortDn(sncZCTP)))
                ctps.add(sncZCTP);
        }
        return ctps;
    }
}
