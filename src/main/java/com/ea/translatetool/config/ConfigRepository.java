package com.ea.translatetool.config;

import java.util.Properties;

/**
 * Created by HeRui on 2019/1/1.
 */
public interface ConfigRepository {
    <T> T load(Class<T> tClass, Properties properties);
    <T> boolean storage(T t, Properties properties);
}
