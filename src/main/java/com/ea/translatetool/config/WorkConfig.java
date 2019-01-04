package com.ea.translatetool.config;

import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.addit.mode.Translation;
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
    private List<Translation> translationList;
    private HashMap<String, String> localMap;
    private HashMap<String, TranslationLocator> translationLocatorMap;

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

    public List<Translation> getTranslationList() {
        return translationList;
    }

    public void setTranslationList(List<Translation> translationList) {
        this.translationList = translationList;
    }

    public HashMap<String, String> getLocalMap() {
        return localMap;
    }

    public void setLocalMap(HashMap<String, String> localMap) {
        this.localMap = localMap;
    }

    public HashMap<String, TranslationLocator> getTranslationLocatorMap() {
        return translationLocatorMap;
    }

    public void setTranslationLocatorMap(HashMap<String, TranslationLocator> translationLocatorMap) {
        this.translationLocatorMap = translationLocatorMap;
    }
}
