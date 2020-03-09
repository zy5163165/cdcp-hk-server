package com.alcatelsbell.cdcp.nbi.model;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "C_PATH")
public class CPath extends CdcpObject {

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
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
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
	public String getHigherOrderPath() {
		return higherOrderPath;
	}
	public void setHigherOrderPath(String higherOrderPath) {
		this.higherOrderPath = higherOrderPath;
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
	private String section;
	private String name;
	private String waveLen;
	private String frequencies;
	private String timeslot;
	private String workType;
	private String higherOrderPath;
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

    private Long actpId;
    private Long zctpId;
    private Long aptpId;
    private Long zptpId;
    @Lob
    private String aptps = "";
    @Lob
    private String zptps = "";

    @Lob
    private String aends = "";
    @Lob
    private String zends = "";

    public String getAptps() {
        return aptps;
    }

    public void setAptps(String aptps) {
        this.aptps = aptps;
    }

    public String getZptps() {
        return zptps;
    }

    public void setZptps(String zptps) {
        this.zptps = zptps;
    }

    public String getAends() {
        return aends;
    }

    public void setAends(String aends) {
        this.aends = aends;
    }

    public String getZends() {
        return zends;
    }

    public void setZends(String zends) {
        this.zends = zends;
    }

    public Long getActpId() {
        return actpId;
    }

    public void setActpId(Long actpId) {
        this.actpId = actpId;
    }

    public Long getZctpId() {
        return zctpId;
    }

    public void setZctpId(Long zctpId) {
        this.zctpId = zctpId;
    }

    public Long getAptpId() {
        return aptpId;
    }

    public void setAptpId(Long aptpId) {
        this.aptpId = aptpId;
    }

    public Long getZptpId() {
        return zptpId;
    }

    public void setZptpId(Long zptpId) {
        this.zptpId = zptpId;
    }

    public String getTmRate() {
        return tmRate;
    }

    public void setTmRate(String tmRate) {
        this.tmRate = tmRate;
    }
}
