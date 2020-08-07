package com.ea.translatetool.config;

import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.addit.mode.Translation;
import com.ea.translatetool.constant.GlobalConstant;

import java.io.File;
import java.util.*;
import java.util.TreeMap;

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
    private TreeMap<String, String> localMap;
    private TreeMap<String, TranslationLocator> translationLocatorMap;
    private List<File> excelFiles;
    private Set<String> ignoreLocaleSet;
    AppConfig appConfig;

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

    public TreeMap<String, String> getLocalMap() {
        return localMap;
    }

    public void setLocalMap(TreeMap<String, String> localMap) {
        this.localMap = localMap;
    }

    public TreeMap<String, TranslationLocator> getTranslationLocatorMap() {
        return translationLocatorMap;
    }

    public void setTranslationLocatorMap(TreeMap<String, TranslationLocator> translationLocatorMap) {
        this.translationLocatorMap = translationLocatorMap;
    }

    public List<File> getExcelFiles() {
        return excelFiles;
    }

    public void setExcelFiles(List<File> excelFiles) {
        this.excelFiles = excelFiles;
    }

    public Set<String> getIgnoreLocaleSet() {
        return ignoreLocaleSet;
    }

    public void setIgnoreLocaleSet(Set<String> ignoreLocaleSet) {
        this.ignoreLocaleSet = ignoreLocaleSet;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
}
