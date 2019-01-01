package com.ea.translatetool.config;

import com.ea.translatetool.constant.GlobalConstant;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by HeRui on 2019/1/1.
 */
public class FileConfigRepositoryImpl implements ConfigRepository {
    public final static String CONFIG_FILE_PATH_KEY = "configFilePath";
    private static FileConfigRepositoryImpl fileConfigRepository;

    private FileConfigRepositoryImpl() {}

    public synchronized static FileConfigRepositoryImpl getInstance() {
        if(fileConfigRepository == null) {
            fileConfigRepository = new FileConfigRepositoryImpl();
        }
        return fileConfigRepository;
    }

    @Override
    public synchronized  <T> T load(Class<T> tClass, Properties pro) {
        T config = null;
        try {
            config = tClass.newInstance();
            if(tClass == AppConfig.class) {
                AppConfig appConfig = (AppConfig) config;
                appConfig.setLocalMapFilePath(GlobalConstant.AppConfigDefaultValue.LOCAL_MAP_FILE_PATH);
                appConfig.setCoverKey(GlobalConstant.AppConfigDefaultValue.IS_COVER_KEY);
                appConfig.setExistKeySaveDir(GlobalConstant.AppConfigDefaultValue.EXIST_KEY_SAVE_DIR);
                appConfig.setFilePrefix(GlobalConstant.AppConfigDefaultValue.FILE_PREFIX);
                appConfig.setFileSuffix(GlobalConstant.AppConfigDefaultValue.FILE_SUFFIX);
                appConfig.setInPath(GlobalConstant.AppConfigDefaultValue.IN_PATH);
                appConfig.setOutPath(GlobalConstant.AppConfigDefaultValue.OUT_PATH);
                appConfig.setLogSaveDir(GlobalConstant.AppConfigDefaultValue.LOG_SAVE_DIR);
                appConfig.setLogLevel(GlobalConstant.AppConfigDefaultValue.LOG_LEVEL);
            }

            File configFile = new File((String) pro.get(CONFIG_FILE_PATH_KEY));
            int count = 0;
            Field[] fields = AppConfig.class.getDeclaredFields();
            if(configFile.exists() && configFile.isFile()) {
                Properties properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(configFile)));
                Set<Object> keySet = properties.keySet();
                if(keySet.size() > 0) {
                    if(Map.class.isAssignableFrom(tClass)) {
                        Map mapConfig = (Map) config;
                        for (Object key : keySet) {
                            mapConfig.put(key, properties.get(key));
                        }
                    } else {
                        for (Object key : keySet) {
                            for (Field f : fields) {
                                if (f.getName().equalsIgnoreCase(key.toString())) {
                                    String value = (String) properties.get(key);
                                    if (value != null && !value.isEmpty()) {
                                        f.setAccessible(true);
                                        try {
                                            if (boolean.class.isAssignableFrom(f.getType())) {
                                                f.set(config, Boolean.parseBoolean(value));
                                            } else if (String.class.isAssignableFrom(f.getType())) {
                                                f.set(config, value);
                                            } else if (String.class.isAssignableFrom(f.getType())) {
                                                f.set(config, value.split(","));
                                            }
                                            ++count;
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                            }
                        }
                        if(count < fields.length) {
                            storage(config, pro);
                        }
                    }
                }
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    @Override
    public synchronized <T> boolean storage(T t, Properties pro) {
        File saveFile = new File((String) pro.get(CONFIG_FILE_PATH_KEY));
        if(!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        Properties properties = new Properties();
        if(Map.class.isAssignableFrom(t.getClass())) {
            Map mapConfig = (Map) t;
            Set keySet = mapConfig.keySet();
            for (Object key : keySet) {
                Object value = mapConfig.get(key);
                String strValue = "";
                if (value != null && value.getClass().isArray() && ((String[]) value).length > 0) {
                    StringBuffer stringBuffer = new StringBuffer("");
                    for (String item : (String[]) value) {
                        stringBuffer.append(item).append(",");
                    }
                    strValue = stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString();
                } else if(value != null){
                    strValue = value.toString();
                }
                properties.put(key, strValue);
            }
        } else {
            Field[] fields = AppConfig.class.getDeclaredFields();
            for (Field f : fields) {
                try {
                    f.setAccessible(true);
                    Object value = f.get(t);
                    String strValue = "";
                    if (value != null && f.getType().isArray() && ((String[]) value).length > 0) {
                        StringBuffer stringBuffer = new StringBuffer("");
                        for (String item : (String[]) value) {
                            stringBuffer.append(item).append(",");
                        }
                        strValue = stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString();
                    } else if(value != null){
                        strValue = value.toString();
                    }
                    properties.put(f.getName(), strValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(saveFile);
            properties.store(writer, "config of tool");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
