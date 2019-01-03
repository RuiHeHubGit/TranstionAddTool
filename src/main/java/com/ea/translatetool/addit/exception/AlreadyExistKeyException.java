package com.ea.translatetool.addit.exception;

import com.ea.translatetool.addit.mode.Translate;

import java.util.List;

public class AlreadyExistKeyException extends Exception{
    List<Translate> existList;
    public AlreadyExistKeyException(List<Translate> repeatList, String msg) {
        super(msg);
        this.existList = repeatList;
    }

    public List<Translate> getExistList() {
        return existList;
    }
}
