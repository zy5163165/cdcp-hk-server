package com.alcatelsbell.cdcp.common;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-31
 * Time: 上午10:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class VendorDicEntry {
    public int vendorValue;
    public String desc;
    public String name;
    public int value;
    public String type;

    public VendorDicEntry(String type,int vendorValue, String desc, String name, int value) {
        this.vendorValue = vendorValue;
        this.desc = desc;
        this.name = name;
        this.value = value;
        this.type = type;
    }
    public VendorDicEntry(String type,int vendorValue, int value) {
        this.vendorValue = vendorValue;
        this.value = value;
        this.type = type;this.value = value;


    }
}
