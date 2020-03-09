package com.alcatelsbell.cdcp.nbi.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "C_CHANNEL")
public class CChannel extends CdcpObject {


	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public Integer getDirection() {
		return direction;
	}
	public void setDirection(Integer direction) {
		this.direction = direction;
	}
	public String getAend() {
		return aend;
	}
	public void setAend(String aend) {
		this.aend = aend;
	}
	public String getZend() {
		return zend;
	}
	public void setZend(String zend) {
		this.zend = zend;
	}
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

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getWaveLen() {
		return waveLen;
	}
	public void setWaveLen(String waveLen) {
		this.waveLen = waveLen;
	}
	public String getFrequencies() {
		return frequencies;
	}
	public void setFrequencies(String frequencies) {
		this.frequencies = frequencies;
	}
	public String getTimeslot() {
		return timeslot;
	}
	public void setTimeslot(String timeslot) {
		this.timeslot = timeslot;
	}
	public String getWorkType() {
		return workType;
	}
	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public String getTrail() {
		return trail;
	}
	public void setTrail(String trail) {
		this.trail = trail;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	private String rate;
	private Integer direction;
	private String aend;
	private String zend;
	private String aptp;
	private String zptp;
	private String name;
	private String waveLen;
	private String frequencies;
	private String timeslot;
	private String workType;
	private String sectionOrHigherOrderDn;
	private String trail;
	private String no;
    private String tmRate;
    private String rateDesc;
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRateDesc() {
        return rateDesc;
    }

    public void setRateDesc(String rateDesc) {
        this.rateDesc = rateDesc;
    }

    public String getTmRate() {
        return tmRate;
    }

    public void setTmRate(String tmRate) {
        this.tmRate = tmRate;
    }

    public String getSectionOrHigherOrderDn() {
        return sectionOrHigherOrderDn;
    }

    public void setSectionOrHigherOrderDn(String sectionOrHigherOrderDn) {
        this.sectionOrHigherOrderDn = sectionOrHigherOrderDn;
    }
}
