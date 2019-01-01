package com.ea.translatetool.addit;

import com.ea.translatetool.addit.mode.Translate;
import com.ea.translatetool.addit.mode.WorkStage;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.util.ExcelUtil;
import com.ea.translatetool.util.IOUitl;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.*;

public class Addit {
    private volatile static boolean running;
    private static Addit  addit;
    WorkConfig workConfig;
    private List<Translate> translateList;
    private List<File> sourceFiles;
    private int stage;
    private int stageCount;
    private int mode;

    private Addit() {
        translateList = new ArrayList<>();
        sourceFiles = new ArrayList<>();
    }

    public synchronized static Addit getInstance() {
        if(addit == null) {
            addit = new Addit();
        }
        return addit;
    }

    public static void start(WorkConfig workConfig, int mode, WorkCallback callback) {
        Addit addit = getInstance();
        addit.mergeNewAddTranslate(workConfig);
        addit.mode = mode;
        if(mode == 0) {
            addit.stage = 1;
            addit.stageCount = 3;
            addit.loadTranslateFiles(workConfig, callback);
            addit.stage = 2;
            addit.loadAllTranslate(workConfig, callback);
        } else if(mode == 2) {
            addit.stage = 1;
            addit.stageCount = 2;
            addit.loadAllTranslate(workConfig, callback);
        } else {
            addit.stage = 1;
            addit.stageCount = 1;
        }

        addit.stage = addit.stageCount;
        addit.doWork(workConfig, callback);
    }

    private void doWork(WorkConfig workConfig, WorkCallback callback) {
        running = true;
        WorkStage workStage =new WorkStage(mode, stage, stageCount, 3, "test", "", new Date(), null);
        if(callback != null) {
            callback.onStart(workStage);
        }
        // TODO: 2018/12/23
        // test start
        int total = 300;
        for (int i=0; i<=total; ++i) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                running = false;
                if(callback != null && callback.onError(e)) {
                    return;
                }
            }
            if(callback != null) {
                callback.onProgress(i, total);
            }
        }
        // test end
        running = false;
        if(callback != null) {
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }
    }

    private void mergeNewAddTranslate(WorkConfig workConfig) {
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
    }

    public List<File> loadTranslateFiles(WorkConfig workConfig, WorkCallback callback) {
        List<File> inputPathList = workConfig.getInput();
        WorkStage workStage =  new WorkStage(mode, stage, stageCount, 3, "load files", "", new Date(), null);
        if(callback != null) {
            callback.onStart(workStage);
        }
        if(sourceFiles == null) {
            sourceFiles = new ArrayList<>();
        }
        for (final File file : inputPathList) {
            List<File> files =  IOUitl.fileList(file, false, new DirectoryStream.Filter<File>() {
                @Override
                public boolean accept(File entry) throws IOException {
                    if(entry.isFile()
                            && (entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLS)
                                    || entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLSX))) {
                        return true;
                    }
                    return false;
                }
            });
            sourceFiles.addAll(files);
        }
        if(callback != null) {
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }
        return sourceFiles;
    }

    private void loadAllTranslate(WorkConfig workConfig, WorkCallback callback) {
        WorkStage workStage = new WorkStage(mode, stage, stageCount, 3, "load translate", "", new Date(), null);
        if(callback != null) {
            callback.onStart(workStage);
        }
        for (int i=0; i<sourceFiles.size(); ++i) {
            try {
                parseTranslateFile(workConfig, sourceFiles.get(i));
            } catch (Exception e) {
                if(callback != null && callback.onError(e)) {
                    return;
                }
            }

            if(callback != null) {
                callback.onProgress(i, sourceFiles.size());
            }
        }

        if(callback != null) {
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }
    }

    private void parseTranslateFile(WorkConfig workConfig, File file) {
        // TODO: 2018/12/24
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

    public static boolean isRunning() {
        return running;
    }

    public List<File> getSourceFiles() {
        return sourceFiles;
    }

    public List<Translate> getTranslateList() {
        return translateList;
    }

    public WorkConfig getWorkConfig() {
        return workConfig;
    }
}
