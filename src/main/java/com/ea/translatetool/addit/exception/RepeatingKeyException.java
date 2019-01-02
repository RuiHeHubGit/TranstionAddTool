package com.ea.translatetool.addit.exception;

import com.ea.translatetool.addit.mode.Translate;

import java.util.List;

public class RepeatingKeyException extends Exception{
    List<Translate> repeatList;
    public RepeatingKeyException(List<Translate> repeatList, String msg) {
        super(msg);
        this.repeatList = repeatList;
    }

    public List<Translate> getRepeatList() {
        return repeatList;
    }
}
