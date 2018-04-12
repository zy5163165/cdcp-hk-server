package com.alcatelsbell.cdcp.api.plugins;

import com.alcatelsbell.cdcp.nbi.model.CdcpObject;
import com.alcatelsbell.nms.valueobject.BObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2017/1/3
 * Time: 14:07
 * rongrong.chen@alcatel-sbell.com.cn
 */
public abstract class SimpleBObjectMigrator implements EmsServerJob {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeJob(EmsServerJobContext context) throws Exception {
        Map<Class<? extends CdcpObject>, List<CdcpObject>> bData = getBData(context);
        StringBuffer sb = new StringBuffer();
        for (Class allBClass : bData.keySet()) {
            List<CdcpObject> data = bData.get(allBClass);
            for (CdcpObject cdcpObject : data) {
                cdcpObject.setEmsName(context.getEms().getDn());
            }
            sb.append(allBClass.getSimpleName()).append(":").append(data.size()).append(";");
            logger.info("Processing : "+allBClass+" data size = "+ (data == null ? null : data.size()));
            context.executeQl("delete from "+allBClass.getName()+" where emsName = '"+context.getEms().getDn()+"'",null);
            context.insert(data);
        }
        context.createLog("处理数据",sb.toString());
    }


    protected abstract Map<Class<? extends CdcpObject>,List<CdcpObject>> getBData(EmsServerJobContext context) throws Exception;
}
