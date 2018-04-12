package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import com.alcatelsbell.cdcp.util.DicUtil;
import com.alcatelsbell.nms.common.SysUtil;
import com.alcatelsbell.nms.util.log.LogUtil;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.service.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-7
 * Time: 上午11:18
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class U2000MigratorUtil {

    public static CPTP transPTP(PTP ptp) {
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
        cptp.setRate(ptp.getRate());
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
    public static CRoute createRoute(String emsDn,String aend,String zend,HashMap<String,CCTP> ctpMap) {
        CRoute route = new CRoute();
        route.setDn(aend+"<>"+zend);
        route.setSid(DatabaseUtil.nextSID(CRoute.class));
        route.setName("业务路由");
        CCTP cctp = ctpMap.get(aend);
        route.setRate(cctp.getRate());
        route.setRateDesc(SDHUtil.rateDesc(cctp.getRate()));
        route.setTmRate(SDHUtil.getTMRate(cctp.getRate()));


            route.setAend(aend);
            route.setAptp(DNUtil.extractPortDn(aend));
            if (route.getAend().contains("CTP"))
                route.setActpId(DatabaseUtil.getSID(CCTP.class,route.getAend()));
            route.setAptpId(DatabaseUtil.getSID(CPTP.class, route.getAptp()));



            route.setZend(zend);
            route.setZptp(DNUtil.extractPortDn(zend));
            if (route.getZend().contains("CTP"))
                route.setZctpId(DatabaseUtil.getSID(CCTP.class, route.getZend()));
            route.setZptpId(DatabaseUtil.getSID(CPTP.class, route.getZptp()));




        route.setDirection(DicConst.CONNECTION_DIRECTION_CD_BI);
        route.setNativeEmsName("业务路由");
//        route.setSncType(snc.getSncType());
//        route.setSncState(snc.getSncState());
        route.setCategory("SDHROUTE");
        route.setEmsName(emsDn);
        return route;
    }
    public static CRoute transRoute(String emsDn,SubnetworkConnection snc) {
       CRoute route = new CRoute();
        route.setDn(snc.getDn());
        route.setSid(DatabaseUtil.nextSID(CRoute.class));
        route.setName(snc.getNativeEMSName());

        route.setRate(snc.getRate());
        route.setRateDesc(SDHUtil.rateDesc(snc.getRate()));
        route.setTmRate(SDHUtil.getTMRate(snc.getRate()));

        if (snc.getaEnd().contains(Constant.listSplit)) {
            route.setAends(snc.getaEnd());
            route.setAptps(snc.getaPtp());
        } else {
            route.setAend(snc.getaEnd());
            route.setAptp(snc.getaPtp());
            if (route.getAend().contains("CTP"))
                route.setActpId(DatabaseUtil.getSID(CCTP.class,route.getAend()));
            route.setAptpId(DatabaseUtil.getSID(CPTP.class, route.getAptp()));
        }


        if (snc.getzEnd().contains(Constant.listSplit)) {
            route.setZends(snc.getzEnd());
            route.setZptps(snc.getzPtp()); 
            

        } else {
            route.setZend(snc.getzEnd());
            route.setZptp(snc.getzPtp());
            if (route.getZend().contains("CTP"))
                route.setZctpId(DatabaseUtil.getSID(CCTP.class, route.getZend()));
            route.setZptpId(DatabaseUtil.getSID(CPTP.class, route.getZptp()));
        }



        route.setDirection(DicUtil.getConnectionDirection(snc.getDirection()));
        route.setNativeEmsName(snc.getNativeEMSName());
        route.setSncType(snc.getSncType());
        route.setSncState(snc.getSncState());
        route.setCategory("SDHROUTE");
        route.setEmsName(emsDn);
        return route;
    }

    public static CPath_Channel createCPath_Channel(String emsDn,CChannel channel,CPath cPath) {
        CPath_Channel crc = new CPath_Channel();
        crc.setDn(SysUtil.nextDN());
        crc.setChannelDn(channel.getDn());
        crc.setChannelId(channel.getSid());
        crc.setPathDn(cPath.getDn());
        crc.setEmsName(emsDn);
        crc.setPathId(cPath.getSid());
        return crc;
    }

    public static CPath_CC createCPath_CC(String emsDn,String ccDn,CPath cPath) {
        ccDn = DNUtil.compressCCDn(ccDn);
        CPath_CC crc = new CPath_CC();
        crc.setDn(SysUtil.nextDN());
        crc.setCcDn(ccDn);
        crc.setCcId(DatabaseUtil.getSID(CCrossConnect.class,ccDn));
        crc.setPathDn(cPath.getDn());
        crc.setEmsName(emsDn);
        crc.setPathId(cPath.getSid());
        return crc;
    }



    public static CRoute_Channel createCRoute_Channel(String emsDn,CChannel channel,CRoute cRoute) {
        CRoute_Channel crc = new CRoute_Channel();
        crc.setDn(SysUtil.nextDN());
        crc.setChannelDn(channel.getDn());
        crc.setChannelId(channel.getSid());
        crc.setRouteDn(cRoute.getDn());
        crc.setEmsName(emsDn);
        crc.setRouteId(cRoute.getSid());
        return crc;
    }

    public static CRoute_Section createCRoute_Section(String emsDn,String sectionDn,CRoute cRoute) {
        CRoute_Section crc = new CRoute_Section();
        crc.setDn(SysUtil.nextDN());
        crc.setRouteDn(cRoute.getDn());
        crc.setRouteId(cRoute.getSid());
        crc.setSectionDn(sectionDn);
        crc.setSectionId(DatabaseUtil.getSID(CSection.class,sectionDn));
        crc.setEmsName(emsDn);

        return crc;
    }

    public static CPath_Section createCPath_Section(String emsDn,String sectionDn,CPath cpath) {
        CPath_Section crc = new CPath_Section();
        crc.setDn(SysUtil.nextDN());
        crc.setPathDn(cpath.getDn());
        crc.setPathId(cpath.getSid());
        crc.setSectionDn(sectionDn);
        crc.setSectionId(DatabaseUtil.getSID(CSection.class,sectionDn));
        crc.setEmsName(emsDn);
        return crc;
    }

    public static CRoute_CC createCRoute_CC(String emsDn,String ccDn,CRoute cRoute) {
        ccDn = DNUtil.compressCCDn(ccDn);
        CRoute_CC crc = new CRoute_CC();
        crc.setCcDn(ccDn);
        crc.setDn(SysUtil.nextDN());
        crc.setCcId(DatabaseUtil.getSID(CCrossConnect.class,ccDn));
        crc.setRouteDn(cRoute.getDn());
        crc.setEmsName(emsDn);
        crc.setRouteId(cRoute.getSid());
        return crc;
    }

    public static CPath transPath(String emsDn,SubnetworkConnection snc) {
        CPath route = new CPath();
        route.setDn(snc.getDn());
        route.setSid(DatabaseUtil.nextSID(CPath.class));
        route.setName(snc.getNativeEMSName());

        route.setRate((snc.getRate()));
        route.setRateDesc(SDHUtil.rateDesc(snc.getRate()));
        route.setTmRate(SDHUtil.getTMRate(snc.getRate()));
        route.setCategory("HOP");

        if (snc.getaEnd().contains(Constant.listSplit)) {
            route.setAends(snc.getaEnd());
            route.setAptps(snc.getaPtp());
        } else {
            route.setAend(snc.getaEnd());
            route.setAptp(snc.getaPtp());
            if (route.getAend().contains("CTP"))
                 route.setActpId(DatabaseUtil.getSID(CCTP.class,route.getAend()));
            route.setAptpId(DatabaseUtil.getSID(CPTP.class, route.getAptp()));
        }


        if (snc.getzEnd().contains(Constant.listSplit)) {
            route.setZends(snc.getzEnd());
            route.setZptps(snc.getzPtp());


        } else {
            route.setZend(snc.getzEnd());
            route.setZptp(snc.getzPtp());
            if (route.getZend().contains("CTP"))
                route.setZctpId(DatabaseUtil.getSID(CCTP.class, route.getZend()));
            route.setZptpId(DatabaseUtil.getSID(CPTP.class, route.getZptp()));
        }

        route.setDirection(DicUtil.getConnectionDirection(snc.getDirection()));
        route.setEmsName(emsDn);


        return route;
    }

    public static CPath createPath(String emsDn,String aendOchTp,String zendOchTp,HashMap<String,CCTP> ctpMap) throws Exception {
        CPath route = new CPath();
        route.setDn(aendOchTp+"<>"+zendOchTp);
        route.setSid(DatabaseUtil.nextSID(CPath.class));
        route.setName("OCH");

        CCTP aCtp = ctpMap.get(aendOchTp);
        if (aCtp == null) throw new Exception("无法找到CTP :"+aendOchTp);

        route.setRate((aCtp.getRate()));
        route.setRateDesc(SDHUtil.rateDesc(aCtp.getRate()));
        route.setTmRate(SDHUtil.getTMRate(aCtp.getRate()));
        route.setCategory("HOP");


        route.setAend(aendOchTp);
        route.setAptp(DNUtil.extractPortDn(aendOchTp));
        if (route.getAend().contains("CTP"))
            route.setActpId(DatabaseUtil.getSID(CCTP.class,route.getAend()));
        route.setAptpId(DatabaseUtil.getSID(CPTP.class, route.getAptp()));




        route.setZend(zendOchTp);
        route.setZptp(DNUtil.extractPortDn(zendOchTp));
        if (route.getZend().contains("CTP"))
            route.setZctpId(DatabaseUtil.getSID(CCTP.class, route.getZend()));
        route.setZptpId(DatabaseUtil.getSID(CPTP.class, route.getZptp()));


        route.setDirection(DicConst.CONNECTION_DIRECTION_CD_BI);
        route.setEmsName(emsDn);


        return route;
    }

//    /**
//     * dn 未压缩,请注意使用
//     * @param src
//     * @param aend
//     * @param zend
//     * @return
//     */
//    public static CCrossConnect transCC(CrossConnect src,String aend,String zend) {
//        CCrossConnect des = new CCrossConnect();
////        aend = aend.replace("PTP:","CrossConnect:").replace("@CTP:", "");
////        zend = zend.replace("PTP:","CrossConnect:").replace("@CTP:", "");
////        zend = zend.substring(zend.lastIndexOf("CrossConnect:")+"CrossConnect:".length());
//
//        des.setDn( (aend + "_" + zend));
//        des.setCollectTimepoint(src.getCreateDate());
//        des.setCcType(src.getCcType());
//        des.setDirection(src.getDirection());
//        //TODO
//
//        des.setAend(aend);
//        des.setZend(zend);
//        des.setAptp(DNUtil.extractPortDn(aend));
//        des.setZptp(DNUtil.extractPortDn(zend));
//
//        des.setParentDn(src.getParentDn());
//        des.setEmsName(src.getEmsName());
//        des.setAdditionalInfo(src.getAdditionalInfo());
//        return des;
//    }

    public static CCrossConnect transCC(CrossConnect src,String aend,String zend,String emsdn) {
        CCrossConnect des = new CCrossConnect();
        des.setAptp(DNUtil.extractPortDn(aend));
        des.setZptp(DNUtil.extractPortDn(zend));
        des.setAend(aend);
        des.setZend(zend);

        des.setAptpId(DatabaseUtil.getSID(CPTP.class,des.getAptp()));
        des.setZptpId(DatabaseUtil.getSID(CPTP.class, des.getZptp()));
//        DatabaseUtil.getSID(CCTP.class,des.getAend());
//        DatabaseUtil.getSID(CCTP.class,des.getZend());


        aend = aend.replace("PTP:","CrossConnect:").replace("@CTP:", "");
        zend = zend.replace("PTP:","CrossConnect:").replace("@CTP:", "");
        if (zend.contains("CrossConnect:"))
            zend = zend.substring(zend.lastIndexOf("CrossConnect:")+"CrossConnect:".length());
        des.setDn(DNUtil.compressCCDn(aend + "<>" + zend));

        des.setCollectTimepoint(src.getCreateDate());
        des.setCcType(src.getCcType());
        des.setDirection(src.getDirection());
        //TODO




        des.setParentDn(src.getParentDn());
        if (emsdn == null) emsdn = src.getEmsName();
        des.setEmsName(emsdn);
        des.setAdditionalInfo(src.getAdditionalInfo());
        return des;
    }


    public static CSection transSection(Section section) {
        CSection csection = new CSection();
        csection.setDn(section.getDn());
        csection.setSid(DatabaseUtil.nextSID(csection));
        csection.setCollectTimepoint(section.getCreateDate());
        csection.setRate(section.getRate());
        String rate = section.getRate();
        if (rate != null) {
            int r = 0;
            try {
                r = Integer.parseInt(rate);
            } catch (NumberFormatException e) {
                LogUtil.error(U2000MigratorUtil.class, "Unknown rate :" + rate);
            }
            rate = DicUtil.getSpeedByRate(r);
            // if (r == DicConst.LR_DSR_Gigabit_Ethernet)
            // rate = "1000M";

        }
        csection.setSpeed(rate);
        csection.setDirection(DicUtil.getConnectionDirection(section.getDirection()));
        csection.setAendTp(section.getaEndTP());

        csection.setZendTp(section.getzEndTP());
        csection.setParentDn(section.getParentDn());
        csection.setEmsName(section.getEmsName());
        csection.setUserLabel(section.getUserLabel());
        csection.setNativeEMSName(section.getNativeEMSName());
        csection.setOwner(section.getOwner());
        csection.setAdditionalInfo(section.getAdditionalInfo());
        return csection;
    }

    public static List<CCrossConnect> transCCS( CrossConnect  cc,String emsdn) {
        String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
        String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);
        List newCCs = new ArrayList();
        if (actps.length == zctps.length  && actps.length == 1) {
            CCrossConnect ccc = transCC(cc, actps[0], zctps[0],emsdn);
            newCCs.add(ccc);

        }

        else if (actps.length == 1) {
            for (String zctp : zctps) {
                CCrossConnect ccc = transCC(cc, actps[0], zctp,emsdn);
                newCCs.add(ccc);
            }
        }
        else if (zctps.length == 1) {
            for (String actp : actps) {
                CCrossConnect ccc = transCC(cc, actp, zctps[0],emsdn);
                newCCs.add(ccc);
            }
        } else if (actps.length == zctps.length) {
            for (int i = 0; i < actps.length; i++) {
                CCrossConnect ccc = transCC(cc, actps[i], zctps[i],emsdn);
                if (ccc.getAend() != null) {
                    if (ccc.getAend().contains("tu12"))
                        ccc.setRate("VC12");
                    else if (ccc.getAend().contains("vc3"))
                        ccc.setRate("VC3");
                    else if (ccc.getAend().contains("au4-j"))
                        ccc.setRate("VC4");
                }

                newCCs.add(ccc);
            }
        }

        if (newCCs.size() == 1)
            ((CCrossConnect)newCCs.get(0)).setDn(cc.getDn());

        return newCCs;
    }




}
