package com.ea.translatetool.config;

import com.ea.translatetool.addit.mode.ColumnPosition;
import com.ea.translatetool.addit.mode.Translate;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HeRui on 2018/12/23.
 */
public class WorkConfig {
    private String filePrefix;
    private String fileSuffix;
    private List<File> input;
    private File output;
    private String outType;
    private Boolean vertical;
    private Integer keyColumn;
    private Integer localColumn;
    private Integer translateColumn;
    private List<Translate> translateList;
    private HashMap<String, String> localMap;
    private HashMap<String, ColumnPosition> columnPositionMap;

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public List<File> getInput() {
        return input;
    }

    public void setInput(List<File> input) {
        this.input = input;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public String getOutType() {
        return outType;
    }

    public void setOutType(String outType) {
        this.outType = outType;
    }

    public Boolean getVertical() {
        return vertical;
    }

    public void setVertical(Boolean vertical) {
        this.vertical = vertical;
    }

    public Integer getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(Integer keyColumn) {
        this.keyColumn = keyColumn;
    }

    public Integer getLocalColumn() {
        return localColumn;
    }

    public void setLocalColumn(Integer localColumn) {
        this.localColumn = localColumn;
    }

    public Integer getTranslateColumn() {
        return translateColumn;
    }

    public void setTranslateColumn(Integer translateColumn) {
        this.translateColumn = translateColumn;
    }

    public List<Translate> getTranslateList() {
        return translateList;
    }

    public void setTranslateList(List<Translate> translateList) {
        this.translateList = translateList;
    }

    public HashMap<String, String> getLocalMap() {
        return localMap;
    }

    public void setLocalMap(HashMap<String, String> localMap) {
        this.localMap = localMap;
    }

    public HashMap<String, ColumnPosition> getColumnPositionMap() {
        return columnPositionMap;
    }

    public void setColumnPositionMap(HashMap<String, ColumnPosition> columnPositionMap) {
        this.columnPositionMap = columnPositionMap;
    }
}
