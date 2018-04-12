package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.cdcp.nbi.model.CEMS;
import com.alcatelsbell.nms.db.components.client.JpaClient;
import com.alcatelsbell.nms.valueobject.sys.Ems;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-1-15
 * Time: 上午10:47
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestClient {
    private Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) throws  Exception {

        List<Ems>  emses = JpaClient.getInstance().findAllObjects(Ems.class);
        List<CEMS> cEmses = JpaClient.getInstance("cdcp.datajpa").findAllObjects(CEMS.class);


        HashMap<String,String> types = new HashMap<String, String>();
        for (Ems emse : emses) {
            types.put(emse.getDn(),emse.getTag1());
        }


        for (CEMS cEmse : cEmses) {
            String tag1 = types.get(cEmse.getDn());
            if (tag1 != null) {
                cEmse.setTag1(tag1);
                String sql = " update CEMS set tag1 = '" + tag1 + "' where dn = '" + cEmse.getDn() + "'";
                JpaClient.getInstance("cdcp.datajpa").executeUpdateSQL(sql);
            }
        }
    }
}
