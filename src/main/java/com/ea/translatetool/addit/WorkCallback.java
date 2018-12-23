package com.ea.translatetool.addit;

/**
 * Created by HeRui on 2018/12/23.
 */
public interface WorkCallback {
    void onStart();
    void onProgress(long complete, long total);
    void onDone();
    void onError(Throwable t);
}
