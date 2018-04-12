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
@Table(name = "C_EthRoute_ETHTrunk")
public class CEthRoute_ETHTrunk extends CdcpObject{
    private String ethTrunkDn;
    private String ethRouteDn;
    private Long ethTrunkId;
    private Long ethRouteId;

    public Long getEthTrunkId() {
        return ethTrunkId;
    }

    public void setEthTrunkId(Long ethTrunkId) {
        this.ethTrunkId = ethTrunkId;
    }

    public Long getEthRouteId() {
        return ethRouteId;
    }

    public void setEthRouteId(Long ethRouteId) {
        this.ethRouteId = ethRouteId;
    }




    public String getEthTrunkDn() {
        return ethTrunkDn;
    }

    public void setEthTrunkDn(String ethTrunkDn) {
        this.ethTrunkDn = ethTrunkDn;
    }

    public String getEthRouteDn() {
        return ethRouteDn;
    }

    public void setEthRouteDn(String ethRouteDn) {
        this.ethRouteDn = ethRouteDn;
    }


}
