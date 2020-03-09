package com.alcatelsbell.cdcp.common.model;

import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-10-30
 * Time: 下午3:38
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Table(name="S_EMSBENCHMARK")
@Entity
public class EmsBenchmark extends BObject {
    private static final long serialVersionUID = -1L;
    private String emsname;

    @BField(description = "备注")
    @Column(length = 2048)
    private String additinalInfo;

    @BField(description = "状态")
    private Integer status = 1;       // 1生效  0无效


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getEmsname() {
        return emsname;
    }

    public void setEmsname(String emsname) {
        this.emsname = emsname;
    }

    public String getAdditinalInfo() {
        return additinalInfo;
    }

    public void setAdditinalInfo(String additinalInfo) {
        this.additinalInfo = additinalInfo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
