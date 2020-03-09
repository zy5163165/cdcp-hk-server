package com.alcatelsbell.cdcp.web.common;

/**
 * Author: Ronnie.Chen
 * Date: 12-11-20
 * Time: 下午4:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface BObjectClassDescriptor {

    /**
     *  字段是否需要显示的
     * @return
     */
    public boolean isBFieldVisible(String fieldName) ;

    /**
     * 获得描述的类
     * @return
     */
    public Class getClass1() ;

}
