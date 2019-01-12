package com.ea.translatetool.config;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by HeRui on 2019/1/1.
 */
public interface ConfigRepository {
    <T> T load(Class<T> tClass, Object def, Properties properties) throws IOException;
    <T> boolean storage(T t, Properties properties);
}
