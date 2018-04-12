package com.alcatelsbell.cdcp.server.adapters.fenghuootnm2000;

import com.alcatelsbell.cdcp.nbi.model.CCTP;
import com.alcatelsbell.cdcp.nbi.model.CPTP;
import com.alcatelsbell.cdcp.nbi.model.CRoute;
import com.alcatelsbell.cdcp.util.DNUtil;
import com.alcatelsbell.cdcp.util.DatabaseUtil;
import com.alcatelsbell.cdcp.util.DicConst;
import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.ptn.u2000V16.nbi.job.CTPUtil;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2015/1/20
 * Time: 9:57
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FHSdhUtil {
    private Log logger = LogFactory.getLog(getClass());
    public static void testRoute() {
        String filePath = "d:\\cdcpdb\\FH_2015-01-19-141245-HUZ-OTNM2000-7-P-DayMigration.db";
        SqliteDelegation sd = new SqliteDelegation(JPASupportFactory.createSqliteJPASupport(filePath));
        List<CrossConnect> ccs = sd.queryAll(CrossConnect.class);
        List<CTP> ctps = sd.queryAll(CTP.class);
        List<Section> sections = sd.queryAll(Section.class);



    }

    public static void breakupSections(List<Section> sections) {
        for (Section section : sections) {
//            String aendTp = section.getaEndTP();
//            String zendTp = section.getzEndTP();
//
////            List<CCTP> actps = this.findObjects(CCTP.class, "select c from CCTP c where c.portdn = '" + aendTp + "'");
////            List<CCTP> zctps = this.findObjects(CCTP.class, "select c from CCTP c where c.portdn = '" + zendTp + "'");
//
//            List<T_CTP> actps = null;
//            List<T_CTP> zctps = null;
//            try {
//                actps = ctpTable.findObjectByIndexColumn("portdn",aendTp);
//                zctps = ctpTable.findObjectByIndexColumn("portdn", zendTp);
//            } catch (Exception e) {
//                getLogger().error(e, e);
//            }
//            if (actps == null) {
//                getLogger().error("无法找到端口下的ctp:"+aendTp);
//                continue;
//            }
//            if (zctps == null) {
//                getLogger().error("无法找到端口下的ctp:"+zendTp);
//                continue;
//            }
//            for (T_CTP actp : actps) {
//                if (CTPUtil.isVC4(actp.getDn())) {
//                    int j = CTPUtil.getJ(actp.getDn());
//
//                    for (T_CTP zctp : zctps) {
//                        if (CTPUtil.isVC4(zctp.getDn()) && (CTPUtil.getJ(zctp.getDn()) == j)) {
//                            createCChannel(actp,zctp,section);
//                        }
//                    }
//                }
//            }
        }

    }

    public static CRoute createCRoute(String aend,String zend,String emsname){
        CRoute route = new CRoute();
        route.setDn(aend + "<>" + zend);
        route.setSid(DatabaseUtil.nextSID(CRoute.class));
   //     route.setName("VC12");
        route.setRate(DicConst.LR_VT2_and_TU12_VC12+"");
        route.setRateDesc("VC12");
        route.setTmRate("2M");
        route.setCategory("SDHROUTE");

        route.setAend(aend);
        route.setAptp(DNUtil.extractPortDn(aend));
        route.setActpId(DatabaseUtil.getSID(CCTP.class,route.getAend()));
        route.setAptpId(DatabaseUtil.getSID(CPTP.class, route.getAptp()));

        route.setZend(zend);
        route.setZptp(DNUtil.extractPortDn(zend));
        route.setZctpId(DatabaseUtil.getSID(CCTP.class, route.getZend()));
        route.setZptpId(DatabaseUtil.getSID(CPTP.class, route.getZptp()));

        route.setDirection(DicConst.CONNECTION_DIRECTION_CD_BI);
        route.setEmsName(emsname);
        return route;
    }

}
