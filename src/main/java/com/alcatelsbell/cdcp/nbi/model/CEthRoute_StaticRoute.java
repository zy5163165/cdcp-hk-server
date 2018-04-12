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
@Table(name = "C_EthRoute_StaticRoute")
public class CEthRoute_StaticRoute extends CdcpObject{
    private String ethRouteDn;
    private String staticRouteDn;
    private Long ethRouteId;
    private Long staticRouteId;

    public String getEthRouteDn() {
        return ethRouteDn;
    }

    public void setEthRouteDn(String ethRouteDn) {
        this.ethRouteDn = ethRouteDn;
    }

    public String getStaticRouteDn() {
        return staticRouteDn;
    }

    public void setStaticRouteDn(String staticRouteDn) {
        this.staticRouteDn = staticRouteDn;
    }

    public Long getEthRouteId() {
        return ethRouteId;
    }

    public void setEthRouteId(Long ethRouteId) {
        this.ethRouteId = ethRouteId;
    }

    public Long getStaticRouteId() {
        return staticRouteId;
    }

    public void setStaticRouteId(Long staticRouteId) {
        this.staticRouteId = staticRouteId;
    }
}
