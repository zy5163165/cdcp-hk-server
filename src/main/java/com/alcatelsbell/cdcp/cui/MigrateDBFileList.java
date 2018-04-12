package com.alcatelsbell.cdcp.cui;

import com.alcatelsbell.nms.util.SortUtil;
import com.alcatelsbell.nms.util.SysProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2015/2/6
 * Time: 10:30
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrateDBFileList {
    private Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) {
        if (args == null || args.length == 0) args = new String[]{"D:\\docs2"};
        File dir = new File(args[0]);
        String emss = SysProperty.getString("migratedb.ems", "");
        String[] split = emss.split(";");
        HashSet<String> emsSet = new HashSet<String>();
        for (String ems : split) {
            emsSet.add(ems);
            System.out.println("ems = " + ems);
        }

        File[] files = dir.listFiles();
        for (File emsDir : files) {
            File[] dbs = emsDir.listFiles();
            if (dbs == null) continue;
            List<File> fs = new ArrayList<File>(Arrays.asList(dbs));
            SortUtil.sort(fs, new SortUtil.CompareAdapter() {
                @Override
                public int compare(Object o1, Object o2) {

                    long d1 = ((File) o1).lastModified();
                    long d2 = ((File) o2).lastModified();

                    Calendar c1 = Calendar.getInstance();
                    c1.setTimeInMillis(d1);

                    Calendar c2 = Calendar.getInstance();
                    c2.setTimeInMillis(d2);

                    if (c2.get(Calendar.YEAR) != c1.get(Calendar.YEAR))
                        return  c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);

                    return c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);

                }
            });

            File file = fs.get(0);
            System.out.println("file = " + file.getAbsolutePath());
        }


    }
}
