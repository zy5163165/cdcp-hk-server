package com.alcatelsbell.cdcp.server.adapters;

import com.alcatelsbell.cdcp.util.BObjectMemTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asb.mule.probe.framework.entity.CrossConnect;
import org.asb.mule.probe.framework.entity.Section;
import org.asb.mule.probe.framework.entity.SubnetworkConnection;
import org.asb.mule.probe.framework.service.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-9
 * Time: 下午10:38
 * rongrong.chen@alcatel-sbell.com.cn
 */

/**
 *
 *  根据CC和section计算出所有的channel和snc
 */
public class SDHSncComputationUnit {
    private Log logger = LogFactory.getLog(getClass());

    private BObjectMemTable<CrossConnect> ccTable = null;
    private BObjectMemTable<Section> sectionTable = null;
    private List<CrossConnect> ccList = null;
    private List<SubnetworkConnection> sncs = new ArrayList<SubnetworkConnection>();

    public void compute() {

    }

    List<String> processedCCs = new ArrayList<String>();
    private void computeFromCC(CrossConnect cc) {
        String[] actps = cc.getaEndNameList().split(Constant.listSplitReg);
        String[] zctps = cc.getzEndNameList().split(Constant.listSplitReg);

        String actp = actps[0];
        String zctp = zctps[0];

        CrossConnect nextCC = findCC(actp);
        if (nextCC == null) {
            Section nextSection = findSection(actp);
        }


    }

    private CrossConnect findCC(String actp) {

        return null;
    }

    private Section findSection(String actp) {
        return null;
    }


}

