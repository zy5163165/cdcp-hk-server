package com.alcatelsbell.cdcp.server.v3;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/26.
 */
public class FileInfo implements Serializable {
    private File _file;

    public FileInfo(File _file) {
        this._file = _file;
    }

    public File getFile() {
        return _file;
    }

}
