package com.alcatelsbell.cdcp.nbi.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-8
 * Time: 下午2:31
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FieldInfo implements Serializable{
    private String fieldChName;
    private String fieldEnName;
    private String fieldContent;

    public FieldInfo() {
    }

    public FieldInfo(String fieldChName, String fieldEnName, String fieldContent) {
        this.fieldChName = fieldChName;
        this.fieldEnName = fieldEnName;
        this.fieldContent = fieldContent;
    }

    public String getFieldChName() {
        return fieldChName;
    }

    public void setFieldChName(String fieldChName) {
        this.fieldChName = fieldChName;
    }

    public String getFieldEnName() {
        return fieldEnName;
    }

    public void setFieldEnName(String fieldEnName) {
        this.fieldEnName = fieldEnName;
    }

    public String getFieldContent() {
        return fieldContent;
    }

    public void setFieldContent(String fieldContent) {
        this.fieldContent = fieldContent;
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "fieldChName='" + fieldChName + '\'' +
                ", fieldEnName='" + fieldEnName + '\'' +
                ", fieldContent='" + fieldContent + '\'' +
                '}';
    }

    public String toXML() {
        return "<fieldInfo>\n" +
                "\t<fieldChName>"+fieldChName+"</fieldChName>\n" +
                "\t<fieldEnName>"+fieldEnName+"</fieldEnName>\n" +
                "\t<fieldContent>"+fieldContent+"</fieldContent>\n" +
                " </fieldInfo>";
    }
}
