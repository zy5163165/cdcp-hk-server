package com.alcatelsbell.cdcp.api.plugins;

import com.alcatelsbell.cdcp.nbi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2017/1/4
 * Time: 12:39
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Example extends SimpleBObjectMigrator {
    private Logger logger = LoggerFactory.getLogger(Example.class);

    @Override
    protected Map<Class<? extends CdcpObject>, List<CdcpObject>> getBData(EmsServerJobContext context) throws Exception {
        Map<Class<? extends CdcpObject>, List<CdcpObject>> map = new HashMap();

        List devices = new ArrayList();
        for (int i = 0; i < 100; i++) {
             CDevice device = new CDevice();
            device.setDn("device:"+i);
            devices.add(device);

        }

        List ptps = new ArrayList();
        for (int i = 0; i < 100; i++) {
            CPTP device = new CPTP();
            device.setDn("ptp:"+i);
            ptps.add(device);

        }

        map.put(CDevice.class, devices);
        map.put(CPTP.class, ptps);
        return map;
    }
}
