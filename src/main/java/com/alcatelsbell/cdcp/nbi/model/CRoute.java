package com.alcatelsbell.cdcp.nbi.model;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "C_ROUTE")
public class CRoute extends CdcpObject {

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
	public String getWorkType() {
		return workType;
	}
	public void setWorkType(String workType) {
		this.workType = workType;
	}
	public String getNativeEmsName() {
		return nativeEmsName;
	}
	public void setNativeEmsName(String nativeEmsName) {
		this.nativeEmsName = nativeEmsName;
	}
	public String getSncType() {
		return sncType;
	}
	public void setSncType(String sncType) {
		this.sncType = sncType;
	}
	public String getSncState() {
		return sncState;
	}
	public void setSncState(String sncState) {
		this.sncState = sncState;
	}
	private String rate;
	private Integer direction;
	private String aend;
	private String zend;
	private String aptp;
	private String zptp;
	private String name;
	private String workType;
	private String nativeEmsName;
	private String sncType = "";
	private String sncState = "";
    private Long actpId;
    private Long zctpId;
    private Long aptpId;
    private Long zptpId;
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

    @Lob
    private String aptps = "";
    @Lob
    private String zptps = "";

    @Lob
    private String aends = "";
    @Lob
    private String zends = "";


}
