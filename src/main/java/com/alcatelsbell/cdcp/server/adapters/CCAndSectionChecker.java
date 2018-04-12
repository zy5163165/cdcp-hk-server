package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.util.SqliteDelegation;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.R_TrafficTrunk_CC_Section;
import org.asb.mule.probe.framework.entity.Section;

import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-29
 * Time: 上午8:58
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CCAndSectionChecker {
    private Log logger = LogFactory.getLog(getClass());
    public void check(SqliteDelegation sd) {
        HashMap<String,CrossConnect> ccs = sd.queryAllToMap(CrossConnect.class);
        HashMap<String,Section> sections = sd.queryAllToMap(Section.class);

        List<R_TrafficTrunk_CC_Section> routes = sd.queryAll(R_TrafficTrunk_CC_Section.class);
        for (R_TrafficTrunk_CC_Section route : routes) {
            if (route.getType().equals("CC")) {
                String ccDn = route.getCcOrSectionDn();
                CrossConnect crossConnect = ccs.get(ccDn);
                if (crossConnect == null) {
                    System.out.println("ccDn = " + ccDn);
                }
            }

            if (route.getType().equals("SECTION")) {
                String sectionDn = route.getCcOrSectionDn();
                Section section = sections.get(sectionDn);
                if (section == null) {
                    System.out.println("sectionDn = " + sectionDn);
                }
            }
        }

    }

    public static void main(String[] args) {
        CCAndSectionChecker checker = new CCAndSectionChecker();
        String fileName=  "D:\\cdcpdb\\2014-07-15-114824-QUZ-T2000-3-P-DayMigration.db";
        JPASupport sqliteJPASupport = JPASupportFactory.createSqliteJPASupport(fileName);
        checker.check(new SqliteDelegation(sqliteJPASupport));
    }
}
