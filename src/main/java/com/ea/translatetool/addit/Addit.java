package com.ea.translatetool.addit;

import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Addit {
    WorkConfig workConfig;
    private List<Translate> translateList;
    private List<File> sourceFiles;
    private volatile static boolean running;

    public static boolean isRunning() {
        return running;
    }

    public static void start(WorkConfig workConfig, WorkCallback callback) {
        Addit addit = new Addit();
        addit.init(workConfig);
        addit.doWork(workConfig, callback);
    }

    private void doWork(WorkConfig workConfig, WorkCallback callback) {
        running = true;
        if(callback != null) {
            callback.onStart();
        }
        // TODO: 2018/12/23
        // test start
        for (int i=0; i<=100; ++i) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                running = false;
                callback.onError(e);
                return;
            }
            if(callback != null) {
                callback.onProgress(i, 100);
            }
        }
        // test end
        running = false;
        if(callback != null) {
            callback.onDone();
        }
    }

    private void init(WorkConfig workConfig) {
        this.workConfig = workConfig;
        translateList = new ArrayList<>();
        List<Translate> list =  workConfig.getTranslateList();
        if(list != null && Collection.class.isAssignableFrom(list.getClass())) {
            for (Object item : (Collection)list) {
                if(item != null && Translate.class.isAssignableFrom(item.getClass())) {
                    translateList.add((Translate) item);
                } else {
                    //TODO //add log
                }
            }
        }

        loadTranslateFromExcel(workConfig);
    }

    private void loadTranslateFromExcel(WorkConfig workConfig) {

    }


    public static WorkConfig getDefaultWorkConfig() {
        WorkConfig workConfig = new WorkConfig();
        HashMap<String, Object> params = new HashMap<String, Object>();
        List<File> files = new ArrayList<>();
        for (String path : GlobalConstant.AppConfigDefaultValue.IN_PATH) {
            files.add(new File(path));
        }
        workConfig.setInput(files);
        workConfig.setOutput(new File(GlobalConstant.AppConfigDefaultValue.OUT_PATH));
        workConfig.setOutType(GlobalConstant.OutType.TYPE_JSON.getValue());
        workConfig.setVertical(false);
        workConfig.setKeyColumn(0);
        workConfig.setLocalColumn(1);
        workConfig.setTranslateColumn(2);
        return workConfig;
    }
}
