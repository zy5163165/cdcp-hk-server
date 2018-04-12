package com.alcatelsbell.cdcp.server.adapters.huaweiu2000;

import com.alcatelsbell.cdcp.server.adapters.*;

import com.alcatelsbell.cdcp.server.dbset.CrossConnectTable;
import com.alcatelsbell.cdcp.server.dbset.SectionTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.Section;

import static com.alcatelsbell.cdcp.server.adapters.DBTable.RowHandler;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-25
 * Time: 上午10:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class HWU2000SDHMigration extends MigrationTemplate{
    private Log logger = LogFactory.getLog(getClass());
    private String dbFilePath = "D:\\cdcp\\hw_ningbo_sdh\\2014-06-16-200130-NBO-T2000-10-P-DayMigration.db";
    public static void main(String[] args) {
        new HWU2000SDHMigration().run();
    }

    private void run() {
        loadDBFile(dbFilePath);


        CrossConnectTable ccDBTable =  loadTable(CrossConnectTable.class);
        ccDBTable.queryAll();

        SectionTable sectionTable = loadTable(SectionTable.class);


        System.out.println("sectionTable = " + sectionTable);
   //     SectionTable sectionTable = loadTable(Section.class);


        sectionTable.eachRow(new RowHandler<Section>() {public void each(Section section, DBContext ctx) {
            System.out.println("Section = " + section.getDn());

        }});




    }




}
