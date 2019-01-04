package com.ea.translatetool.addit.mode;

/**
 * Created by HeRui on 2018/12/24.
 */
public class ColumnPosition {
    private String key;
    private int localColumn;
    private int translateColumn;
    private int orientation;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getLocalColumn() {
        return localColumn;
    }

    public void setLocalColumn(int localColumn) {
        this.localColumn = localColumn;
    }

    public int getTranslateColumn() {
        return translateColumn;
    }

    public void setTranslateColumn(int translateColumn) {
        this.translateColumn = translateColumn;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
