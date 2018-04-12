package com.alcatelsbell.cdcp.web.model;

import com.alcatelsbell.cdcp.web.common.CdcpDictionary;
import com.alcatelsbell.nms.common.annotation.DicGroupMapping;
import com.alcatelsbell.nms.common.crud.annotation.BField;
import com.alcatelsbell.nms.valueobject.BObject;


import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Author: Ronnie.Chen
 * Date: 2016/9/14
 * Time: 15:25
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class D_Dictionary extends BObject {

    @BField(description = "描述",sequence = 2)
    @Column(name = "description")
    public String desc;

    @BField(description = "值",sequence = 3)
    public int value;

    @BField(description = "编码",sequence = 4)
    public String code;

//    @BField(description = "颜色")
    public String color;

//
//    @BField(description = "字典组",sequence = 1,searchType = BField.SearchType.NULLABLE)
//    @DicGroupMapping(definitionClass = SmasDictionary.DicGroup.class,groupName = "")
    @Column(name = "groupname")
    public String group;

    @BField(description = "字典组",sequence = 1,searchType = BField.SearchType.NULLABLE)
    @DicGroupMapping(definitionClass = CdcpDictionary.DicGroup.class,groupName = "")
    public Integer groupno;

    public D_Dictionary() {
    }

    public D_Dictionary(String desc, int value, String code, String color, String group) {
        this.desc = desc;
        this.value = value;
        this.code = code;
        this.color = color;
        this.group = group;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String toString() {
        return "" + this.value;
    }

    public Integer getGroupno() {
        return groupno;
    }

    public void setGroupno(Integer groupno) {
        this.groupno = groupno;
    }
}
