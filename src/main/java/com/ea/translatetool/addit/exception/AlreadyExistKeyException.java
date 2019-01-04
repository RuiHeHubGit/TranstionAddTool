package com.ea.translatetool.addit.exception;

import com.ea.translatetool.addit.mode.Translation;

import java.util.List;

public class AlreadyExistKeyException extends Exception{
    List<Translation> existList;
    public AlreadyExistKeyException(List<Translation> repeatList, String msg) {
        super(msg);
        this.existList = repeatList;
    }

    public List<Translation> getExistList() {
        return existList;
    }
}
