package com.ea.translatetool.addit.mode;

/**
 * Created by HeRui on 2018/12/24.
 */
public class ColumnPosition {
    private int keyColumn;
    private int localColumn;
    private int translateColumn;
    private int orientation;

    public int getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(int keyColumn) {
        this.keyColumn = keyColumn;
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
