package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.nbi.model.CCTP;
import com.alcatelsbell.cdcp.util.DNUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CTP;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-6-25
 * Time: 上午10:12
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class MigrationTool {
    private static Log logger = LogFactory.getLog(MigrationTool.class);

    public DBContext loadDBFile(String path) {
        return null;
    }

    public List<CCTP> makeupVC12Ctps(CCTP vc4) {
        String portDn = DNUtil.extractPortDn(vc4.getDn());
         int j = getJ(vc4.getDn());
        List<CCTP> newCTPs = new ArrayList<CCTP>();
        for (int k = 1; k <=3 ; k++) {


            for (int l = 1; l <= 7 ; l++) {
                for (int m = 1; m <= 3; m++) {

                        CCTP newCTP = new CCTP();
                        String newDn = portDn + "@CTP:/sts3c_au4-j="+j+"/vt2_tu12-k="+k+"-l="+l+"-m="+m;
                        newCTP.setDn(newDn);
                        newCTP.setTag1("MAKEUP");
                        newCTP.setNativeEMSName("VC12-"+(21*(m-1) + 3*(l-1) + k));
                        newCTP.setPortdn(portDn);
                        newCTP.setTmRate("2M");
                        newCTP.setParentCtpdn(vc4.getDn());
                        newCTP.setRateDesc("VC12");
                        newCTPs.add(newCTP);


                }
            }
        }

        return newCTPs;
    }

    public static int getJ(String dn) {
        try {
            int i = dn.lastIndexOf("j=");
            if (i > -1) {
                int j = dn.indexOf("/",i);

                if (j > -1) {
                    return Integer.parseInt(dn.substring(i + 2, j));
                } else
                    return Integer.parseInt(dn.substring(i+2));
            }
            return -1;
        } catch (NumberFormatException e) {
            logger.error(e, e);
            return -1;
        }
    }
}
