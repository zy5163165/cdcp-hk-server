package com.alcatelsbell.cdcp.nbi.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "C_MP_CTP")
public class CMP_CTP extends CdcpObject {

	public String getCtpDn() {
		return ctpDn;
	}

	public void setCtpDn(String ctpDn) {
		this.ctpDn = ctpDn;
	}

	public String getPtpDn() {
		return ptpDn;
	}

	public void setPtpDn(String ptpDn) {
		this.ptpDn = ptpDn;
	}

	public Long getCtpId() {
		return ctpId;
	}

	public void setCtpId(Long ctpId) {
		this.ctpId = ctpId;
	}

	public Long getPtpId() {
		return ptpId;
	}

	public void setPtpId(Long ptpId) {
		this.ptpId = ptpId;
	}

	private String ctpDn;
	private String ptpDn;
	private Long ctpId;
	private Long ptpId;

    private Integer isUsed;

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }
}
