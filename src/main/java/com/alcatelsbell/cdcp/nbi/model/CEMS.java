package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 13-6-13
 * Time: 下午2:34
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
@Table(name = "C_EMS")
public class CEMS  extends CdcpObject {
	@BField(description = "EMS名称",searchType = BField.SearchType.NULLABLE)
    private String name;
	@BField(description = "本地EMS名称",searchType = BField.SearchType.NULLABLE)
    private String cnName;

    private String vendor;
    private Integer status;
    private Integer prefecture;

    public Integer getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(Integer prefecture) {
        this.prefecture = prefecture;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }
}
