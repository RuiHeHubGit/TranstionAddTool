package com.ea.translatetool.config;

import com.ea.translatetool.util.LoggerUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
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
    public synchronized  <T> T load(Class<T> tClass, Object def, Properties pro) throws IOException {
        if(tClass == null && def == null) {
            throw new NullPointerException();
        }

        if(tClass != null && def != null && !tClass.isAssignableFrom(def.getClass())) {
            throw new IllegalArgumentException(def.getClass().getName()+" can't cast to"+tClass.getName());
        }

        T config = null;
        if(def == null) {
            try {
                config = tClass.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            config = (T) def;
        }

        File configFile = new File((String) pro.get(CONFIG_FILE_PATH_KEY));
        int count = 0;
        Field[] fields = AppConfig.class.getDeclaredFields();
        if(configFile.exists() && configFile.isFile()) {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(new FileInputStream(configFile), "utf-8"));
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
                                if (value != null) {
                                    f.setAccessible(true);
                                    try {
                                        if (boolean.class.isAssignableFrom(f.getType())) {
                                            f.set(config, Boolean.parseBoolean(value));
                                        } else if (String.class.isAssignableFrom(f.getType())) {
                                            f.set(config, value);
                                        } else if (f.getType().isArray()) {
                                            f.set(config, value.split(","));
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
                storage(config, pro);
            }
        }
        return config;
    }

    @Override
    public synchronized <T> boolean storage(T t, Properties pro) {
        File saveFile = new File((String) pro.get(CONFIG_FILE_PATH_KEY));
        if(!saveFile.exists()) {
            try {
                if(!saveFile.getParentFile().exists())
                    saveFile.getParentFile().mkdirs();
                saveFile.createNewFile();
            } catch (IOException e) {
                LoggerUtil.error(e.getMessage());
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
                if (value != null && value.getClass().isArray()) {
                    StringBuffer stringBuffer = new StringBuffer("");
                    for (Object item : (Object[]) value) {
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
                    if (value != null && f.getType().isArray()) {
                        StringBuffer stringBuffer = new StringBuffer("");
                        for (Object item : (Object[]) value) {
                            stringBuffer.append(item).append(",");
                        }
                        if(stringBuffer.length() > 0) {
                            strValue = stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString();
                        }
                    } else if(value != null){
                        strValue = value.toString();
                    }
                    properties.put(f.getName(), strValue);
                } catch (IllegalAccessException e) {
                    LoggerUtil.error(e.getMessage());
                }
            }
        }
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(saveFile), "utf-8");
            properties.store(writer, "config of tool");
        } catch (IOException e) {
            LoggerUtil.error(e.getMessage());
            return false;
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LoggerUtil.error(e.getMessage());
                }
            }
        }

        return true;
    }
}
