package com.ea.translatetool;

import com.ea.translatetool.cmd.CmdMode;
import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.util.PID;
import com.ea.translatetool.util.WindowTool;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;

public class App {
    private AppConfig appConfig;

    public static void main(String[] args) {
        App app = new App();
        try {
            app.loadAppConfig();
            app.test();
            app.start(args);
        } catch (Throwable t) {
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
            UI.startUI(this);
        } else {
            CmdMode.start(this, args);
        }
    }

    private synchronized void loadAppConfig() throws IOException {
        appConfig= new AppConfig();
        appConfig.setCoverKey(GlobalConstant.AppConfigDefaultValue.IS_COVER_KEY);
        appConfig.setExistKeySaveDir(GlobalConstant.AppConfigDefaultValue.EXIST_KEY_SAVE_DIR);
        appConfig.setFilePrefix(GlobalConstant.AppConfigDefaultValue.FILE_PREFIX);
        appConfig.setFileSuffix(GlobalConstant.AppConfigDefaultValue.FILE_SUFFIX);
        appConfig.setInPath(GlobalConstant.AppConfigDefaultValue.IN_PATH);
        appConfig.setOutPath(GlobalConstant.AppConfigDefaultValue.OUT_PATH);
        appConfig.setLogSaveDir(GlobalConstant.AppConfigDefaultValue.LOG_SAVE_DIR);
        appConfig.setLogLevel(GlobalConstant.AppConfigDefaultValue.LOG_LEVEL);

        File configFile = new File(GlobalConstant.CONFIG_FILE_PATH);
        int count = 0;
        Field[] fields = AppConfig.class.getDeclaredFields();
        if(configFile.exists() && configFile.isFile()) {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(new FileInputStream(configFile)));
            Set<Object> keySet = properties.keySet();
            if(keySet.size() > 0) {
                for (Object key : keySet) {
                    for (Field f : fields) {
                        if(f.getName().equalsIgnoreCase(key.toString())) {
                            String value = (String) properties.get(key);
                            if(value != null && !value.isEmpty()) {
                                f.setAccessible(true);
                                try {
                                    if (boolean.class.isAssignableFrom(f.getType())) {
                                        f.set(appConfig, Boolean.parseBoolean(value));
                                    } else if(String.class.isAssignableFrom(f.getType())){
                                        f.set(appConfig, value);
                                    } else if(String.class.isAssignableFrom(f.getType())){
                                        f.set(appConfig, value.split(","));
                                    }
                                    ++count;
                                } catch (Exception e) {}
                            }
                        }
                    }
                }
            }
        }
        if(count < fields.length) {
            saveAppConfig(appConfig);
        }
    }

    private synchronized void saveAppConfig(AppConfig appConfig) throws IOException {
        File saveFile = new File(GlobalConstant.CONFIG_FILE_PATH);
        if(!saveFile.exists()) {
            saveFile.createNewFile();
        }
        Field[] fields = AppConfig.class.getDeclaredFields();
        Properties properties = new Properties();
        for (Field f : fields) {
            try {
                f.setAccessible(true);
                Object value = f.get(appConfig);
                String strValue;
                if(f.getType().isArray() && ((String[]) value).length > 0) {
                    StringBuffer stringBuffer = new StringBuffer("");
                    for (String item : (String[]) value) {
                        stringBuffer.append(item).append(",");
                    }
                    strValue = stringBuffer.deleteCharAt(stringBuffer.length()-1).toString();
                } else {
                    strValue = value.toString();
                }
                properties.put(f.getName(), strValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(saveFile);
            properties.store(writer, "config of tool");
        } catch (IOException e) {
            throw e;
        } finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    private void test() {
       try {
           WindowTool windowTool = WindowTool.getInstance();
           windowTool.isWindow(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
