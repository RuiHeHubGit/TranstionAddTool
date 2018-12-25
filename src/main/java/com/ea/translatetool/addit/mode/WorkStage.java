package com.ea.translatetool.addit.mode;

import java.util.Date;

/**
 * Created by HeRui on 2018/12/24.
 */
public class WorkStage {
    private int group;
    private int index;
    private int count;
    private int type;
    private String name;
    private String desc;
    private Date start;
    private Date end;

    public WorkStage() {}

    public WorkStage(int group, int index, int count, int type, String name, String desc, Date start, Date end) {
        this.group = group;
        this.index = index;
        this.count = count;
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.start = start;
        this.end = end;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
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
}
