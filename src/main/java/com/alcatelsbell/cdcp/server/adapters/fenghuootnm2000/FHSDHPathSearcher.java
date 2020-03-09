package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.adapters.CacheClass;
import com.alcatelsbell.cdcp.server.adapters.SDHUtil;
import com.alcatelsbell.cdcp.server.adapters.huaweiu2000.U2000MigratorUtil;
import com.alcatelsbell.cdcp.util.BObjectMemTable;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import org.apache.log4j.Logger;
import org.asb.mule.probe.ptn.u2000V16.nbi.job.CTPUtil;

import java.util.*;

/**
 * Created by Administrator on 2015/2/6.
 */
public class FHSDHPathSearcher {
    private HashMap<String,CSection> ptpSectionMap;
    private List<CCrossConnect> ccList = null;
   // private HashMap<String,CPTP> ptpMap = null;
    private HashMap<String,List<CCTP>> ptpCtpMap = null;
    private HashMap<String,List<CCTP>> parentChildCTPMap = null;
    private BObjectMemTable<CacheClass.T_CCrossConnect> ccTable = null;
    private Logger logger = null;
    private String emsname = null;



    private List<CCrossConnect> vc4CCList = new ArrayList<CCrossConnect>();
    public List<CPath> pathList = new ArrayList<CPath>();
    public List<CChannel> vc4Channels = new ArrayList<CChannel>();
    public List<CChannel> vc12Channels = new ArrayList<CChannel>();
    public List<CPath_CC> cPath_ccs = new ArrayList<CPath_CC>();
    public List<CPath_Channel> cPath_channels = new ArrayList<CPath_Channel>();

    public List<CRoute> vc4Routes = new ArrayList<CRoute>();

    public FHSDHPathSearcher(String emsname,HashMap<String, CSection> ptpSectionMap,
                             List<CCrossConnect> ccList,
                             HashMap<String, List<CCTP>> ptpCtpMap,
                             HashMap<String,List<CCTP>> parentChildCTPMap,
                             Logger logger) {
        this.emsname = emsname;
        this.ptpSectionMap = ptpSectionMap;
        this.ccList = ccList;
        this.ptpCtpMap = ptpCtpMap;
        this.logger = logger;
        this.parentChildCTPMap = parentChildCTPMap;
    }

    public void setCcTable(BObjectMemTable<CacheClass.T_CCrossConnect> ccTable) {
        this.ccTable = ccTable;
    }

    public void search() {
        for (CCrossConnect cc : ccList) {
            if (!cc.getAend().contains("vc3") && !cc.getAend().contains("tu12"))
                vc4CCList.add(cc);
        }


       // List<Link> links = new ArrayList<Link>();
        HashMap<String,Link> endLinkMap = new HashMap<String, Link>();
        for (CSection cSection : ptpSectionMap.values()) {
            if (cSection.getDn().equals("EMS:JIH-OTNM2000-6-P@TopologicalLink:570425871"))
                System.out.println();
            String aendTp = cSection.getAendTp();
            String zendTp = cSection.getZendTp();

//            CPTP aptp = ptpMap.get(aendTp);
//            CPTP zptp = ptpMap.get(zendTp);

            List<CCTP> actps = ptpCtpMap.get(aendTp);
            List<CCTP> zctps = ptpCtpMap.get(zendTp);

            if (actps == null || zctps == null) {
                logger.info("一端端口未被打散,section="+cSection.getDn());
                continue;
            }

            for (CCTP actp : actps) {
                for (CCTP zctp : zctps) {
                    if (CTPUtil.isVC4(actp.getDn()) && CTPUtil.isVC4(zctp.getDn())) {
                        if (CTPUtil.getJ(actp.getDn()) == CTPUtil.getJ(zctp.getDn())) {
                            Link link = new Link();
                            link.aend = actp.getDn();
                            link.zend = zctp.getDn();
                           // link.obj = cSection;
                            CChannel channel = createVC4Channel(actp,zctp,cSection);
                            vc4Channels.add(channel);
                            link.channels.add(channel);
                            link.obj = channel;
                        //    links.add(link);
                            endLinkMap.put(link.aend,link);
                            endLinkMap.put(link.zend,link);
                        }
                    }
                }
            }


        }

        for (CCrossConnect vc4CC : vc4CCList) {
            String aend = vc4CC.getAend();
            String zend = vc4CC.getZend();
            Link linka = endLinkMap.get(aend);
            Link linkz = endLinkMap.get(zend);

            if (linka != linkz && linka != null && linkz != null) {
                Link link = new Link(linka,linkz,vc4CC);
                endLinkMap.remove(aend);
                endLinkMap.remove(zend);
                endLinkMap.put(link.aend,link);
                endLinkMap.put(link.zend,link);

            } else if (linka == null && linkz != null) {
              //  endLinkMap.remove(zend);
                linkz.maybeRoute = true;

            } else if (linka != null && linkz == null) {
               // endLinkMap.remove(aend);
                linka.maybeRoute = true;
            }
        }
    //    HashSet<Link> links = new HashSet<Link>(endLinkMap.values());
        for (Link link : endLinkMap.values()) {

            CRoute vc4Route = isVC4Route(link.aend, link.zend, link.channels, link.ccs);
            if (emsname.equals("JIH-OTNM2000-6-P")) {
                if (vc4Route == null)
                    vc4Route = isVC4Route(link.zend, link.aend, link.channels, link.ccs);
            }
            if (vc4Route != null) {
                vc4Routes.add(vc4Route);
                continue;
            } else if (link.maybeRoute) {
                logger.info("maybe route : aend = "+link.aend+" zend = "+link.zend);
                continue;
            }

            CPath path = createCPath(link);
     //       logger.info("找到path:"+path.getAend()+"-"+path.getZend()+" channels="+link.channels.size());
            pathList.add(path);
            for (CCrossConnect cc : link.ccs) {
                cPath_ccs.add(U2000MigratorUtil.createCPath_CC(emsname,cc.getDn(),path));
            }

            for (CChannel channel : link.channels) {
                cPath_channels.add(U2000MigratorUtil.createCPath_Channel(emsname, channel, path));
            }

            vc12Channels.addAll(breakupPath(path));
        }


    }

    private CRoute isVC4Route(String aend,String zend,List<CChannel> channels,List<CCrossConnect> ccs) {
        List<CacheClass.T_CCrossConnect> accs = null;
        List<CacheClass.T_CCrossConnect> zccs = null;
        try {
            accs = ccTable.findObjectByIndexColumn("zend", aend);
            zccs = ccTable.findObjectByIndexColumn("aend", zend);
        } catch (Exception e) {
            logger.error(e, e);
        }
        if (accs != null && zccs != null && accs.size() > 0 && zccs.size() > 0) {
            CRoute route = FHSdhUtil.createCRoute(accs.get(0).getAend(),zccs.get(0).getZend(),emsname);
            route.setName("VC4");
            route.setRate(DicConst.LR_STS3c_and_AU4_VC4+"");
            route.setRateDesc("VC4");
            route.setTmRate("155M");
            route.setCategory("SDHROUTE");

            HashMap<String, HashSet> map = new HashMap<String, HashSet>();
            map.put("cc",new HashSet());
            map.put("channel",new HashSet());
            route.setUserObject(map);


            map.get("cc").add(accs.get(0).getDn());
            map.get("cc").add(zccs.get(0).getDn());
            if (ccs != null) {
                for (CCrossConnect cc : ccs) {
                    map.get("cc").add(cc.getDn());
                }
            }
            if (channels != null)
                map.get("channel").addAll(channels);
            return route;
        }
        return null;
    }

    private Collection<? extends CChannel> breakupPath(CPath path) {

        List<CChannel> vc12s = new ArrayList<CChannel>();
        List<CCTP> actps = parentChildCTPMap.get(path.getAend());
        List<CCTP> zctps = parentChildCTPMap.get(path.getZend());

        if (actps != null && zctps != null) {
            for (CCTP actp : actps) {
                for (CCTP zctp : zctps) {
                    if (!getVC12Name(actp.getDn()).isEmpty() && getVC12Name(actp.getDn()).equals(getVC12Name(zctp.getDn()))) {
                        CChannel vc12Channel = createVC12Channel(actp, zctp, path);
                        vc12s.add(vc12Channel);

                    }
                }
            }
        }
        return vc12s;

    }

    private static String getVC12Name(String dn) {
        if (dn.contains("sts3c_au4-j")){
            int i = dn.indexOf("/", dn.indexOf("sts3c_au4-j"));
            String s = dn.substring(i+1);

            return s;
        }
        return "";
    }

    private CPath createCPath(Link link) {
        CPath path = new CPath();
        path.setDn(link.aend+"<>"+link.zend+"@PATH:1");
        path.setAend(link.aend);
        path.setZend(link.zend);


        path.setSid(DatabaseUtil.nextSID(CPath.class));
    //    path.setName("VC4");
        path.setRate(DicConst.LR_STS3c_and_AU4_VC4+"");
        path.setRateDesc("VC4");
        path.setTmRate("155M");
        path.setCategory("HOP");


        path.setAptp(DNUtil.extractPortDn(path.getAend()));
        path.setActpId(DatabaseUtil.getSID(CCTP.class,path.getAend()));
        path.setAptpId(DatabaseUtil.getSID(CPTP.class, path.getAptp()));


        path.setZptp(DNUtil.extractPortDn(path.getZend()));
        path.setZctpId(DatabaseUtil.getSID(CCTP.class, path.getZend()));
        path.setZptpId(DatabaseUtil.getSID(CPTP.class, path.getZptp()));

        path.setDirection(DicConst.CONNECTION_DIRECTION_CD_BI);
        path.setEmsName(emsname);
        return path;
    }

    private CChannel createVC4Channel(CCTP actp, CCTP zctp, CSection cSection) {
        CChannel channel = new CChannel();
        channel.setSectionOrHigherOrderDn(cSection.getDn());
        channel.setAend(actp.getDn());
        channel.setZend(zctp.getDn());
        channel.setCategory("SDH高阶时隙");

        channel.setDn(actp.getDn() + "<>" + zctp.getDn());
        channel.setSid(DatabaseUtil.nextSID(CChannel.class));
        channel.setName(actp.getNativeEMSName());
        channel.setNo(actp.getNativeEMSName());
        channel.setRate(DicConst.LR_STS3c_and_AU4_VC4+"");
        channel.setTmRate("155M");
        channel.setRateDesc("VC4");
        channel.setAptp(actp.getPortdn());
        channel.setZptp(zctp.getPortdn());
        channel.setEmsName(emsname);

        channel.setDirection(((CSection)cSection).getDirection());
        channel.setSectionOrHigherOrderDn(((CSection)cSection).getDn());
        return channel;
    }
    private CChannel createVC12Channel(CCTP actp, CCTP zctp, CPath path) {
        CChannel channel = new CChannel();
        channel.setSectionOrHigherOrderDn(path.getDn());
        channel.setAend(actp.getDn());
        channel.setZend(zctp.getDn());
        channel.setCategory("SDH低阶时隙");

        channel.setDn(actp.getDn() + "<>" + zctp.getDn());
        channel.setSid(DatabaseUtil.nextSID(CChannel.class));
        channel.setName(actp.getNativeEMSName());
        channel.setNo(actp.getNativeEMSName());
        channel.setRate(DicConst.LR_VT2_and_TU12_VC12+"");
        channel.setTmRate("2M");
        channel.setRateDesc("VC12");
        channel.setAptp(actp.getPortdn());
        channel.setZptp(zctp.getPortdn());
        channel.setEmsName(emsname);

        channel.setDirection(((CPath)path).getDirection());
        return channel;
    }

    class Link {
        public boolean maybeRoute = false;
        public String aend;
        public String zend;
        public List<CCrossConnect> ccs =  null;
        public List<CChannel> channels = null;
        public Object obj;

        public Link() {
            ccs = new ArrayList<CCrossConnect>();
            channels = new ArrayList<CChannel>();
        }
        public Link(Link linka ,Link linkz,CCrossConnect cc) {
            ccs = new ArrayList<CCrossConnect>();
            channels = new ArrayList<CChannel>();
            ccs.addAll(linka.ccs);
            channels.addAll(linka.channels);
            ccs.addAll(linkz.ccs);
            channels.addAll(linkz.channels);
            ccs.add(cc);
            if (cc.getAend().equals(linka.aend))
                aend = linka.zend;
            else if (cc.getAend().equals(linka.zend))
                aend = linka.aend;

            if (cc.getZend().equals(linkz.aend))
                zend = linkz.zend;
            else if (cc.getZend().equals(linkz.zend))
                zend = linkz.aend;
        }

    }
}
