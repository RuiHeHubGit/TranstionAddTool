package com.ea.translatetool;

import com.ea.translatetool.cmd.CmdMode;
import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.config.ConfigRepository;
import com.ea.translatetool.config.FileConfigRepositoryImpl;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.util.LoggerUtil;
import com.ea.translatetool.util.PID;
import com.ea.translatetool.util.WindowTool;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import static com.ea.translatetool.constant.GlobalConstant.NEED_UI_START_PRO;

public class App {
    private AppConfig appConfig;

    public static void main(String[] args) {
        App app = new App();
        try {
            app.loadAppConfig();
            app.start(args);
        } catch (Throwable t) {
            LoggerUtil.exceptionLog(t);
            try {
                WindowTool windowTool = WindowTool.getInstance();
                windowTool.enableSystemMenu(WindowTool.SC_CLOSE, true);
                Thread.sleep(100_000);
            } catch (Exception e) {}
        }
    }

    private void start(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        if(needStartWithUI()) {
            UI.start(this);
        } else {
            CmdMode.start(this, args);
        }
    }

    private boolean needStartWithUI() {
        return PID.isStartWithContain(NEED_UI_START_PRO);
    }

    public synchronized AppConfig getAppConfig() {
        if(appConfig == null) {
            loadAppConfig();
        }
        return appConfig;
    }

    private void loadAppConfig() {
        ConfigRepository configRepository = FileConfigRepositoryImpl.getInstance();
        Properties properties = new Properties();
        properties.put(FileConfigRepositoryImpl.CONFIG_FILE_PATH_KEY, GlobalConstant.CONFIG_FILE_PATH);
        try {
            appConfig = configRepository.load(AppConfig.class, createDefAppConfig(), properties);
        } catch (IOException e) {
            LoggerUtil.error(e.getMessage());
        }
        appConfig.setLogLevel(LoggerUtil.setLogLevel(appConfig.getLogLevel()));
    }

    public static AppConfig createDefAppConfig() {
        AppConfig appConfig = new AppConfig();
        appConfig.setLocalMapFilePath(GlobalConstant.AppConfigDefaultValue.LOCAL_MAP_FILE_PATH);
        appConfig.setCoverKey(GlobalConstant.AppConfigDefaultValue.IS_COVER_KEY);
        appConfig.setExistKeySaveDir(GlobalConstant.AppConfigDefaultValue.EXIST_KEY_SAVE_DIR);
        appConfig.setFilePrefix(GlobalConstant.AppConfigDefaultValue.FILE_PREFIX);
        appConfig.setFileSuffix(GlobalConstant.AppConfigDefaultValue.FILE_SUFFIX);
        appConfig.setInPath(GlobalConstant.AppConfigDefaultValue.IN_PATH);
        appConfig.setOutPath(GlobalConstant.AppConfigDefaultValue.OUT_PATH);
        appConfig.setLogSaveDir(GlobalConstant.AppConfigDefaultValue.LOG_SAVE_DIR);
        appConfig.setLogLevel(GlobalConstant.AppConfigDefaultValue.LOG_LEVEL);
        return appConfig;
    }
}
