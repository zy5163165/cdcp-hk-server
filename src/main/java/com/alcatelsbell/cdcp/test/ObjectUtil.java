package com.alcatelsbell.cdcp.test;

//import extendedMLSNMgr.TNetworkProtectionGroup_T;
//import globaldefs.NameAndStringValue_T;

import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.util.FileLogger;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-10
 * Time: 上午11:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ObjectUtil {

    public static void main (String[] args) {
          FileLogger fileLogger = new FileLogger("zj.log");
        List<SubnetworkConnection> gs = (List) com.alcatelsbell.nms.util.ObjectUtil.readObjectByPath("d:\\1507\\result_1460511695061");
        System.out.println(gs.size());
        for (SubnetworkConnection g : gs) {
//            if (g.getaEnd().contains("EMS:ZJ-U2000-1-SDH@ManagedElement:591325@PTP:/rack=1/shelf=1/slot=26/domain=sdh/port=1")
//                    && g.getzEnd().contains("EMS:ZJ-U2000-1-SDH@ManagedElement:590552@PTP:/rack=1/shelf=1/slot=5/domain=sdh/port=1")
//                    )
            fileLogger.info(g.getDn()+" "+g.getName()+" "+g.getaPtp()+" "+g.getzPtp()+" "+g.getNativeEMSName());
            if (g.getaEnd().contains("EMS:HZ-T2000-2-P@ManagedElement:16711981")
                    || g.getzEnd().contains("EMS:HZ-T2000-2-P@ManagedElement:16711981")
                    )

                System.out.println(g.getDn()+" "+g.getName()+" "+g.getaPtp()+" "+g.getzPtp()+" "+g.getNativeEMSName());
        }
        System.out.println("ok");
    }
//    public static void main1(String[] args) {
//        TNetworkProtectionGroup_T[] gs = (TNetworkProtectionGroup_T[]) com.alcatelsbell.nms.util.ObjectUtil.readObjectByPath("d:\\work\\ptlist_SNCP");
//        for (int i = 0; i < gs.length; i++) {
//            TNetworkProtectionGroup_T g = gs[i];
//            NameAndStringValue_T[][] sncProtectedNameList = g.sncProtectedNameList;
//            for (int j = 0; j < sncProtectedNameList.length; j++) {
//                NameAndStringValue_T[] nameAndStringValue_ts = sncProtectedNameList[j];
//                System.out.println(toString(nameAndStringValue_ts));
//            }
//            System.out.println("<>");
//            NameAndStringValue_T[][] sncProtectingNameList = g.sncProtectingNameList;
//            for (int j = 0; j < sncProtectingNameList.length; j++) {
//                NameAndStringValue_T[] nameAndStringValue_ts = sncProtectingNameList[j];
//                System.out.println(toString(nameAndStringValue_ts));
//            }
//
//            System.out.println("-------------------------------------------------------------------");
//        }
//      //  System.out.println(o);
//
//    }
//
//    private static  String toString(NameAndStringValue_T[] nvs) {
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < nvs.length; i++) {
//            NameAndStringValue_T nv = nvs[i];
//            sb.append(nv.name+"="+nv.value).append(";");
//        }
//        return sb.toString();
//    }
}
