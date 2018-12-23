package com.ea.translatetool.config;

import com.ea.translatetool.addit.Translate;

import java.io.File;
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
    private boolean vertical;
    private int keyColumn;
    private int localColumn;
    private int translateColumn;
    private List<Translate> translateList;

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

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

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

    public List<Translate> getTranslateList() {
        return translateList;
    }

    public void setTranslateList(List<Translate> translateList) {
        this.translateList = translateList;
    }
}
