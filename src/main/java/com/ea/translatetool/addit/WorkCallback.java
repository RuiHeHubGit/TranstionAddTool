package com.ea.translatetool.addit;

import com.ea.translatetool.addit.mode.WorkStage;

/**
 * Created by HeRui on 2018/12/23.
 */
public interface WorkCallback {
    void onStart(WorkStage stage);
    void onProgress(long complete, long total);
    void onDone(WorkStage stage);
    boolean onError(Throwable t);
}
