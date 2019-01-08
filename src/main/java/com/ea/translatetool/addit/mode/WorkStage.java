package com.ea.translatetool.addit.mode;

import java.util.Date;

/**
 * Created by HeRui on 2018/12/24.
 */
public class WorkStage {
    private int index;
    private int count;
    private int type;
    private String name;
    private String desc;
    private Date start;
    private Date end;
    private boolean success;

    public WorkStage() {}

    public WorkStage(int index, int count, int type, String name, String desc, Date start, Date end, boolean success) {
        this.index = index;
        this.count = count;
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.start = start;
        this.end = end;
        this.success = success;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
