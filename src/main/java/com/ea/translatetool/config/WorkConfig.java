package com.ea.translatetool.config;

import com.ea.translatetool.addit.mode.ColumnPosition;
import com.ea.translatetool.addit.mode.Translate;
import com.ea.translatetool.constant.GlobalConstant;

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
    private GlobalConstant.OutType outType;
    private Boolean vertical;
    private String key;
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

    public GlobalConstant.OutType getOutType() {
        return outType;
    }

    public void setOutType(GlobalConstant.OutType outType) {
        this.outType = outType;
    }

    public Boolean getVertical() {
        return vertical;
    }

    public void setVertical(Boolean vertical) {
        this.vertical = vertical;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
