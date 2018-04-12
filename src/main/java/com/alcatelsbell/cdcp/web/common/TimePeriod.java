package com.alcatelsbell.cdcp.web.common;

import java.util.Date;

/**
 * User: Ronnie
 * Date: 12-2-13
 * Time: 下午4:52
 */
public class TimePeriod {
    private Date fromDate;
    private Date toDate;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}
