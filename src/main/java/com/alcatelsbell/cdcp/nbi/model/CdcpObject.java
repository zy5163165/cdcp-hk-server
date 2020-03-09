package com.alcatelsbell.cdcp.nbi.model;

import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;
import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-24
 * Time: 上午10:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
@MappedSuperclass
public class CdcpObject extends BObject {
    @Column(unique = true, nullable = false)
    private Long sid;
    private Long emsid;

    public Long getEmsid() {
        return emsid;
    }

    public void setEmsid(Long emsid) {
        this.emsid = emsid;
    }

    @BField(description = "EMS名称",
            createType = BField.CreateType.REQUIRED,
            editType = BField.EditType.HIDE,
            searchType=BField.SearchType.NULLABLE,
            mergeType = BField.MergeType.RESERVED,
            dnField="name",
            dnReferenceEntityName = "com.alcatelsbell.cdcp.nbi.model.CEMS")
    private String emsName;

    public String getEmsName() {
        return emsName;
    }

    public void setEmsName(String emsName) {
        this.emsName = emsName;
    }

    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }
}
