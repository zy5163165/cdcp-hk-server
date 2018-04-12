package com.alcatelsbell.cdcp.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2016/10/21
 * Time: 9:37
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CancelableImporterTask{
    private String group;

    private boolean cancel = false;
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public CancelableImporterTask(String group) {
        this.group = group;
    }


    public void checkCancel() throws TaskCanceledException {
        if (cancel) throw new TaskCanceledException(group);
    }

    public void cancel() {
        this.cancel = true;
    }

    private Logger logger = LoggerFactory.getLogger(CancelableImporterTask.class);



}
