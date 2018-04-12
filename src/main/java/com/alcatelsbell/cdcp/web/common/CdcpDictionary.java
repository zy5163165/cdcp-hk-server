package com.alcatelsbell.cdcp.web.common;


import com.alcatelsbell.cdcp.web.common.annotation.*;

/**
 * Author: Ronnie.Chen
 * Date: 2016/11/13
 * Time: 11:39
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CdcpDictionary {

    @DicClass
    public static class ScheduleStatus {
        @DicItem(desc = "禁用",code = "禁用")
        public static final int PASSIVE  = 0;

        @DicItem(desc = "激活",code = "激活")
        public static final int ACTIVE  = 1;
    }

    @DicClass
    public static class DicGroup {
        @DicItem(desc = "区域",code = "区域")
        public static final int dicRegion  = 0;

        @DicItem(desc = "客户所属行业",code = "客户所属行业")
        public static final int dicCusIndustry  = 1;
    }


}
