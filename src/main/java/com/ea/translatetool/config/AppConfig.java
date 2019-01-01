package com.ea.translatetool.config;

/**
 * Created by HeRui on 2018/12/23.
 */
public class AppConfig {
    private String localMapFilePath;
    private String existKeySaveDir;
    private String logSaveDir;
    private String logLevel;
    private String[] inPath;
    private String outPath;
    private String filePrefix;
    private String fileSuffix;
    private boolean isCoverKey;

    public String getLocalMapFilePath() {
        return localMapFilePath;
    }

    public void setLocalMapFilePath(String localMapFilePath) {
        this.localMapFilePath = localMapFilePath;
    }

    public String getExistKeySaveDir() {
        return existKeySaveDir;
    }

    public void setExistKeySaveDir(String existKeySaveDir) {
        this.existKeySaveDir = existKeySaveDir;
    }

    public String getLogSaveDir() {
        return logSaveDir;
    }

    public void setLogSaveDir(String logSaveDir) {
        this.logSaveDir = logSaveDir;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String[] getInPath() {
        return inPath;
    }

    public void setInPath(String[] inPath) {
        this.inPath = inPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

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

    public boolean isCoverKey() {
        return isCoverKey;
    }

    public void setCoverKey(boolean coverKey) {
        this.isCoverKey = coverKey;
    }
}
