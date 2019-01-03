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

import java.util.Properties;

public class App {
    private AppConfig appConfig;

    public static void main(String[] args) {
        App app = new App();
        try {
            app.loadAppConfig();
            app.start(args);
        } catch (Throwable t) {
            LoggerUtil.error(t.getMessage());
            t.printStackTrace();
            try {
                WindowTool windowTool = WindowTool.getInstance();
                windowTool.enableSystemMenu(WindowTool.SC_CLOSE, true);
                Thread.sleep(100_000);
            } catch (Exception e) {}
        }
    }

    private void start(String[] args) {
        if(PID.isStartWithWindowExplorer()) {
            UI.start(this);
        } else {
            CmdMode.start(this, args);
        }
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
        appConfig = configRepository.load(AppConfig.class, properties);
        System.getProperty("logLevel", appConfig.getLogLevel());
    }
}
