package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-4
 * Time: 上午11:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_StaticRoute")
public class CStaticRoute extends CdcpObject {
    private String aptp;
    private String zptp;
    private String avlan;
    private String zvlan;

    public String getAptp() {
        return aptp;
    }

    public void setAptp(String aptp) {
        this.aptp = aptp;
    }

    public String getZptp() {
        return zptp;
    }

    public void setZptp(String zptp) {
        this.zptp = zptp;
    }

    public String getAvlan() {
        return avlan;
    }

    public void setAvlan(String avlan) {
        this.avlan = avlan;
    }

    public String getZvlan() {
        return zvlan;
    }

    public void setZvlan(String zvlan) {
        this.zvlan = zvlan;
    }
}
