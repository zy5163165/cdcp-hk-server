package com.alcatelsbell.cdcp.test;

import com.alcatelsbell.nms.common.SysUtil;

import java.util.HashMap;

/**
 * Author: Ronnie.Chen
 * Date: 13-7-26
 * Time: 下午2:34
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestObjectSize {
    public static void main(String[] args) {
        HashMap map = new HashMap();
        while (true) {
            map.put(SysUtil.nextDN(),SysUtil.nextLongId());
            if (map.size() % 10000 == 0)
                System.out.println(map.size());
        }
    }
}
