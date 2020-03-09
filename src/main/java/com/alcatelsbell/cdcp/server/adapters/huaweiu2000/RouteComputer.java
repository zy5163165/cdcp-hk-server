package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.util.SqliteDelegation;
import org.asb.mule.probe.framework.entity.TrafficTrunk;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-26
 * Time: 下午1:37
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class RouteComputer {
    public void computeTrafficTrunk(SqliteDelegation sd ,TrafficTrunk trafficTrunk) {

        //HZ-U2000-2-P@1@TUNNELTRAIL=411627
        String parentDN = trafficTrunk.getParentDn();
        if (parentDN != null) {
            String tunnelTraidId = parentDN.substring(parentDN.lastIndexOf("@")+1);
            TrafficTrunk tunnelTrail = (TrafficTrunk) sd.queryOneObject("select c from TrafficTrunk c where c.dn like '%" + tunnelTraidId + "'");
            String aptp = tunnelTrail.getaPtp();
            String zptp = tunnelTrail.getzPtp();

        }
    }
}
