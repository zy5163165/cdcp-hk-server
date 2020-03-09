package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.db.components.service.JPAUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.FlowDomainFragment;
import org.asb.mule.probe.framework.entity.PTP;
import org.asb.mule.probe.framework.util.FileLogger;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2015/3/6
 * Time: 15:25
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SqliteDBExporter {
    private Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) throws Exception {
        FileLogger fileLogger = new FileLogger("fdfr.log");
        JPASupport support = JPASupportFactory.createSqliteJPASupport("f:\\cdcpdb\\2015-03-06-020415-ZJ-U2000-1-OTN-DayMigration.db");
        List<PTP> allObjects = JPAUtil.getInstance().findObjects(support, "select c from PTP c where c.parentDn = 'EMS:ZJ-U2000-1-OTN@ManagedElement:33554631'");
        for (PTP allObject : allObjects) {
            System.out.println(allObject.getDn() +"          "+ allObject.getNativeEMSName());
        }

    }
}
