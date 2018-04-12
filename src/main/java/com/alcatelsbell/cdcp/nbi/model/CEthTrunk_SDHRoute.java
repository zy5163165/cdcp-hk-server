package com.alcatelsbell.cdcp.nbi.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-4
 * Time: 上午11:07
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_EthTrunk_SDHRoute")
public class CEthTrunk_SDHRoute extends CdcpObject {
    private String ethTrunkDn;
    private String sdhRouteDn;
    private Long ethTrunkId;
    private Long sdhRouteId;

    public String getEthTrunkDn() {
        return ethTrunkDn;
    }

    public void setEthTrunkDn(String ethTrunkDn) {
        this.ethTrunkDn = ethTrunkDn;
    }

    public String getSdhRouteDn() {
        return sdhRouteDn;
    }

    public void setSdhRouteDn(String sdhRouteDn) {
        this.sdhRouteDn = sdhRouteDn;
    }

    public Long getEthTrunkId() {
        return ethTrunkId;
    }

    public void setEthTrunkId(Long ethTrunkId) {
        this.ethTrunkId = ethTrunkId;
    }

    public Long getSdhRouteId() {
        return sdhRouteId;
    }

    public void setSdhRouteId(Long sdhRouteId) {
        this.sdhRouteId = sdhRouteId;
    }
}
