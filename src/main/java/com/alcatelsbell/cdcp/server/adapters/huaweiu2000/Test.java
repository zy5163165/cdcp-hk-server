package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.nbi.model.CIPRoute;
import com.alcatelsbell.cdcp.nbi.model.CTunnel_Section;
import com.alcatelsbell.cdcp.server.adapters.MigrateUtil;
import com.alcatelsbell.cdcp.test.ServerEnv;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import com.alcatelsbell.nms.db.components.service.JpaServerUtil;
import com.alcatelsbell.nms.util.ObjectUtil;
import com.alcatelsbell.nms.valueobject.physical.Route;
//import org.asb.mule.probe.ptn.u2000V16.entity.FlowDomainFragment;
//import org.asb.mule.probe.ptn.u2000V16.entity.ManagedElement;

import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-3
 * Time: 上午10:27
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Test {
    private static String formatPtpdnOld(String fullname) {
        String ptpInfo = null;
        int i = -1;
        String prefix = null;
        if (fullname.contains("/rack=")) {
            i = fullname.indexOf("/rack=");
            ptpInfo = fullname.substring(i);

        }


        if (i < 0 )  {
            if (!fullname.isEmpty())
                System.out.println("unable to format ptp : "+fullname);
            return fullname;
        }
        prefix = fullname.substring(0,i);


        String suffix = MigrateUtil.extractLocationInfo(ptpInfo, new String[]{"rack", "shelf", "slot", "sub_slot", "port"});
        return (prefix + suffix);
    }
    public static void main(String[] args) throws Exception {
            Object o = ObjectUtil.readObjectByPath("d:\\1\\result_1431393563435");

        //SectionId=[63435] TunnelId=[149643] SectionDn=[EMS:TZ-U2000-1-P@TopologicalLink:2013-01-10 03:02:57 - 7929]
 // TunnelDn=[EMS:TZ-U2000-1-P@Flowdomain:1@TrafficTrunk:TUNNELTRAIL=746] Emsid=[6] EmsName=[TZ-U2000-1-P]
 // Sid=[948276] Id=[7172274] Oid=[0] CreateDate=[Fri Sep 13 15:09:40 CST 2013]
 // UpdateDate=[Fri Sep 13 15:09:40 CST 2013]
 // FromWhere=[0] Dn=[58fa7cfc-4058-4ce8-9b46-2ef223f2adf9]
        CTunnel_Section route = new CTunnel_Section();
        route.setSectionId(63435l);
        route.setTunnelId(149643l);
        route.setSectionDn("EMS:TZ-U2000-1-P@TopologicalLink:2013-01-10 03:02:57 - 7929");
        route.setTunnelDn("EMS:TZ-U2000-1-P@Flowdomain:1@TrafficTrunk:TUNNELTRAIL=746");
        route.setEmsid(6l);
        route.setEmsName("TZ-U2000-1-P");
        route.setSid(948276l);
        route.setId(7172274l);
        route.setOid(0);
        route.setCreateDate(new Date());
        route.setUpdateDate(new Date());
        route.setFromWhere(0);
        route.setDn("58fa7cfc-4058-4ce8-9b46-2ef223f2adf9");
        ServerEnv.init();
        JpaServerUtil.getInstance().saveObject(-1,route);
        System.out.println("ok");
     }
//
//    public static void processFlowDomainFragment(FlowDomainFragment fdf,JPASupport sqliteJPASupport) {
//        String transmissionParams = fdf.getTransmissionParams();
//        System.out.println("transmissionParams = " + transmissionParams);
//
//    }
}


