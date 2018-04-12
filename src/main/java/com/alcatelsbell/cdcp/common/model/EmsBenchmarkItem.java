package com.alcatelsbell.cdcp.common.model;

import com.alcatelsbell.nms.valueobject.BObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Ronnie.Chen
 * Date: 14-10-30
 * Time: 下午3:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Table(name="S_EMSBENCHMARKITEM")
@Entity
public class EmsBenchmarkItem extends BObject{

    private String tableName;
    private Integer count;
    private Integer dvpercentage; // 偏移百分比
    private String benchmarkDn;


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getDvpercentage() {
        return dvpercentage;
    }

    public void setDvpercentage(Integer dvpercentage) {
        this.dvpercentage = dvpercentage;
    }

    public String getBenchmarkDn() {
        return benchmarkDn;
    }

    public void setBenchmarkDn(String benchmarkDn) {
        this.benchmarkDn = benchmarkDn;
    }
}
