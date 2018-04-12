package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.nbi.model.*;
import com.alcatelsbell.cdcp.server.adapters.CacheClass;
import com.alcatelsbell.cdcp.util.BObjectMemTable;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/2/6.
 */
public class FHSDHRouteSearcher {

    private CCrossConnect startCC;
    private BObjectMemTable<CacheClass.T_CCrossConnect> ccTable = null;
    private Logger logger = null;
    private HashMap<String,CChannel> endChannelMap = null;
    private String emsname = null;

    public List<CacheClass.T_CCrossConnect> ccs = new ArrayList<CacheClass.T_CCrossConnect>();
    public List<CChannel> channels = new ArrayList<CChannel>();

    public FHSDHRouteSearcher(String emsname,CCrossConnect startCC, BObjectMemTable<CacheClass.T_CCrossConnect> ccTable, Logger logger, HashMap<String, CChannel> endChannelMap) {
        this.emsname = emsname;
        this.startCC = startCC;
        this.ccTable = ccTable;
        this.logger = logger;
        this.endChannelMap = endChannelMap;
        ccs.add(new CacheClass.T_CCrossConnect(startCC));
    }

    private boolean log = false;
    private void log(String info) {
        if (log)
            logger.info(info);
    }

    public CRoute search() {
        String lastRouteType = "CC";
        String node = startCC.getZend();
        boolean find = false;
        int count = 0;
        while (true) {
            find = false;
            if (startCC.getAend().equals("EMS:JIH-OTNM2000-6-P@ManagedElement:134229436;78337@PTP:/rack=1166849/shelf=1/slot=3146755/port=1@CTP:/sts3c_au4-j=1/tu3_vc3-k=1/vt2_tu12-l=4-m=3")||
                    startCC.getAend().equals("EMS:JIH-OTNM2000-6-P@ManagedElement:134229436;78337@PTP:/rack=1166849/shelf=1/slot=17826833/port=1@CTP:/sts3c_au4-j=1/tu3_vc3-k=1/vt2_tu12-l=4-m=3")  )
                log = true;
            else
                log = false;

            if (node.contains("FTP") || lastRouteType.equals("CHANNEL")) {
                List<CacheClass.T_CCrossConnect> tccs = null;
                try {
                    tccs = ccTable.findObjectByIndexColumn("aend", node);
                } catch (Exception e) {
                    logger.error(e,e);
                }
                if (tccs != null && tccs.size() > 0) {
                    ccs.add(tccs.get(0));
                    find = true;
                    lastRouteType = "CC";
                    node = tccs.get(0).getZend();
                    log("find cc:"+tccs.get(0).getDn());
                    log("next node="+node);
                }
            }
            else {

                CChannel channel = endChannelMap.get(node);
                if (channel != null) {
                    channels.add(channel);
                    find = true;
                    lastRouteType = "CHANNEL";
                    node = channel.getAend().equals(node) ? channel.getZend() : channel.getAend();
                    log("find channel:"+channel.getDn());
                    log("next node="+node);
                }


            }
            if ( node.equals(startCC.getAend())) {
                log("找回start: startcc = "+startCC.getDn());
                find = false;
                break;
            }
            if (find && node.endsWith("@CTP:/vt2_tu12=1")) {
                log("find end node : "+node);
                break;
            }
            if (!find) {
                if (lastRouteType.endsWith("CC") && channels.size() > 0)
                    find = true;
                else {
                    if (emsname.equals("JIH-OTNM2000-6-P1") && lastRouteType.equals("CHANNEL") && channels.size() > 1) {
                        find = true;
                        channels.remove(channels.size()-1);
                        node = ccs.get(ccs.size()-1).getZend();
                    }  else {
                        log("上一段路由为:" + lastRouteType + ";node=" + node + " "+lastRouteType+" rtchannels = " + channels.size() + " rtccs=" + ccs.size() + " start=" + startCC.getAend());

                    }
                }
                break;
            }

            if (count++ == 100) {
                log("死循环:"+startCC.getAend());

                find = false;
                break;
            }
        }

        if (find ) {
            log("find ! ,create route : "+startCC.getAend()+"; "+node);
      //      logger.info("找到route: "+startCC.getAend()+"-"+node+" rtchannels = "+channels.size()+" rtccs="+ccs.size());
            CRoute route = createCRoute(startCC.getAend(),node);
            route.setAend(startCC.getAend());
            route.setZend(node);

            if (DNUtil.extractNEDn(route.getAend()).equals(DNUtil.extractNEDn(route.getZend())))
                return null;
            return route;
        }
        return null;
    }

    private CRoute createCRoute(String aend,String zend){
       return FHSdhUtil.createCRoute(aend,zend,emsname);
    }

}
