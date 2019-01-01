package com.ea.translatetool.config;

import java.io.IOException;

/**
 * Created by HeRui on 2019/1/1.
 */
public interface ConfigRepository {
    <T> T load(Class<T> tClass);
    <T> boolean storage(T t);
}
