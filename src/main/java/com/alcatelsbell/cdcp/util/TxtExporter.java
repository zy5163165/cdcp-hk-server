package com.alcatelsbell.cdcp.util;

import com.alcatelsbell.nms.common.CommonUtil;
import com.alcatelsbell.nms.db.components.service.JPASupport;
import com.alcatelsbell.nms.db.components.service.JPASupportFactory;
import com.alcatelsbell.nms.util.tablefacade.JpaTableDelegation;
import org.asb.mule.probe.framework.entity.CTP;
import org.asb.mule.probe.framework.entity.*;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-10
 * Time: 上午10:57
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TxtExporter {
    public static void main(String[] args) throws IOException {
        //CustomerFlexService.class.getClassLoader().getResource("jndi.properties").toString();
        String filePath = "d:\\work\\20130705233402-JH-OTNM2000-1-PTN-DayMigration.db";
        filePath = "d:\\work\\20130703221001-HZ-U2000-2-P-DayMigration.db";
        JPASupport sqliteJPASupport = JPASupportFactory.createSqliteJPASupport(filePath);
        SqliteDelegation sd = new SqliteDelegation(sqliteJPASupport);
        List list = sd.queryAll(CTP.class);
        export(list);


          list = sd.queryAll(Equipment.class);
        export(list);

          list = sd.queryAll(EquipmentHolder.class);
        export(list);

          list = sd.queryAll(FlowDomainFragment.class);
        export(list);

          list = sd.queryAll(IPCrossconnection.class);
        export(list);

          list = sd.queryAll(IPRoute.class);
        export(list);

        list = sd.queryAll(ManagedElement.class);
        export(list);

        list = sd.queryAll(ProtectionGroup.class);
        export(list);

        list = sd.queryAll(PTP.class);
        export(list);


        list = sd.queryAll(PTP.class);
        export(list);

        list = sd.queryAll(R_FTP_PTP.class);
        export(list);

        list = sd.queryAll(R_TrafficTrunk_CC_Section.class);
        export(list);

        list = sd.queryAll(Section.class);
        export(list);

        list = sd.queryAll(TrafficTrunk.class);
        export(list);

    }

    private static void export(List list) throws IOException {
        FileWriter fw = null;
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            Class  aClass = o.getClass();
            String simpleName = aClass.getSimpleName();


            if (fw == null) {
                System.out.println("simpleName = " + simpleName);
                fw = new FileWriter(simpleName+".data");
                Field[] declaredFields = aClass.getDeclaredFields();
                for (int j = 0; j < declaredFields.length; j++) {
                    Field declaredField = declaredFields[j];
                    fw.write(declaredField.getName()+"\t");
                }
                fw.write("\n");
            }


            Field[] declaredFields = aClass.getDeclaredFields();


            for (int i1 = 0; i1 < declaredFields.length; i1++) {
                Field declaredField = declaredFields[i1];
                Object objectFieldValue = CommonUtil.getObjectFieldValue(declaredField, o);
                fw.write(objectFieldValue+"\t");


            }
            fw.write("\n");
        }

    }
}
