package com.alcatelsbell.cdcp.nbi.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 14-4-8
 * Time: 下午2:31
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class StrRequest implements Serializable {
    private List<FieldInfo> summary;

    public List<FieldInfo> getSummary() {
        return summary;
    }

    public void setSummary(List<FieldInfo> summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "StrRequest{" +
                "summary=" + summary +
                '}';
    }
}
