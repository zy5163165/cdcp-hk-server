package com.alcatelsbell.cdcp.web.model;

import java.io.Serializable;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/5/6
 * Time: 16:00
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class QueryResult implements Serializable {

    private int pageSize = 100;
    private int currentPage = 1;
    private List columns;
    private List rows;
    private int total = 0;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }


    public QueryResult(int total, int pageSize, int currentPage,List columns, List rows) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.columns = columns;
        this.rows = rows;
        this.total = total;
    }

    public QueryResult(int total, int pageSize, int currentPage, List rows) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.rows = rows;
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List getColumns() {
        return columns;
    }

    public void setColumns(List columns) {
        this.columns = columns;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
