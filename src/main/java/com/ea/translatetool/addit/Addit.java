package com.ea.translatetool.addit;

import com.ea.translatetool.addit.exception.AlreadyExistKeyException;
import com.ea.translatetool.addit.exception.InvalidExcelContentException;
import com.ea.translatetool.addit.exception.NotFoundExcelException;
import com.ea.translatetool.addit.mode.Translation;
import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.addit.mode.WorkStage;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.util.ExcelUtil;
import com.ea.translatetool.util.IOUtil;
import com.ea.translatetool.util.LoggerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.*;

public class Addit {
    private volatile static boolean running;
    private static Addit  addit;
    private WorkConfig workConfig;
    private List<Translation> translationList;
    private List<File> sourceFiles;
    private int stage;
    private int stageCount;
    private int mode;
    private WorkStage workStage;

    private Addit() {
        translationList = new ArrayList<>();
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
        try {
            running = true;
            if(mode == 0) {
                addit.stage = 1;
                addit.stageCount = 3;
                addit.loadTranslateFiles(workConfig, callback);
                addit.stage = 2;
                addit.loadAllTranslate(workConfig, callback);
            } else if(mode == 1) {
                addit.stage = 1;
                addit.stageCount = 2;
                addit.loadAllTranslate(workConfig, callback);
            } else {
                addit.stage = 1;
                addit.stageCount = 1;
            }

            addit.stage = addit.stageCount;
            addit.doWork(workConfig, callback);
        } catch (Exception e) {
            addit.workStage.setSuccess(false);
            LoggerUtil.error(e.getMessage());
        } finally {
            if(running) {
                running = false;
                addit.onDone(callback, addit.workStage);
            }
        }
    }

    private void doWork(WorkConfig workConfig, WorkCallback callback) throws IOException {

        workStage = new WorkStage(mode, stage, stageCount, 3, "add translate to file", "", new Date(), null, true);
        if(callback != null) {
            callback.onStart(workStage);
        }

        Collections.sort(workConfig.getTranslationList(), new Comparator<Translation>() {
            @Override
            public int compare(Translation o1, Translation o2) {
                return o1.getLocal().compareTo(o2.getLocal());
            }
        });

        int index = 0;
        int total = workConfig.getTranslationList().size();
        String lastLocal = null;
        List<Translation> localAllTranslations = new ArrayList<>();
        for (Translation translation : workConfig.getTranslationList()) {
            if(!translation.getLocal().equals(lastLocal)) {
                if(null != lastLocal && !lastLocal.isEmpty()) {
                    try {
                        List<Translation> notSave = saveTranslateToFile(localAllTranslations, getLocalFile(workConfig, lastLocal),
                                workConfig.getOutType(), false);
                        if(notSave != null && !notSave.isEmpty()) {
                            if(callback != null && !callback.onError(new AlreadyExistKeyException(notSave, notSave.size()+" keys repeat."))) {
                                saveTranslateToFile(notSave, getLocalFile(workConfig, lastLocal),
                                        workConfig.getOutType(), false);
                            }
                        }
                        workStage.setSuccess(true);
                    } catch (IOException e) {
                        if(callback != null) {
                            callback.onError(e);
                        }
                        throw e;
                    }
                    localAllTranslations.clear();
                }
                lastLocal = translation.getLocal();
            }
            localAllTranslations.add(translation);
            if(callback != null) {
                callback.onProgress(++index, total);
            }
        }

        if(callback != null) {
            callback.onProgress(total, total);
        }
        workConfig.getTranslationList().clear();
        onDone(callback, workStage);
    }

    private List<Translation> saveTranslateToFile(List<Translation> localAllTranslations, File localFile, GlobalConstant.OutType outType, boolean cover) throws IOException {
        List<Translation> notSaveTranslationList = null;
        if(!cover) {
            notSaveTranslationList = new ArrayList<>();
        }

        if(!localFile.exists()) {
            if(!localFile.getParentFile().exists())
                localFile.getParentFile().mkdirs();
            localFile.createNewFile();
        }
        List<String> lines = IOUtil.readText(localFile, null);
        if(lines.isEmpty() && outType == GlobalConstant.OutType.TYPE_JSON) {
            lines.add("{");
            lines.add("}");
        }
        for (Translation translation : localAllTranslations) {
            if(!insertStringToListOnLastSameIndex(lines, translateToLineString(translation, outType), getDivisionCharByType(outType), cover)) {
                notSaveTranslationList.add(translation);
            }
        }
        if(lines.get(lines.size()-2).endsWith(",")) {
            String line = lines.get(lines.size()-2);
            lines.set(lines.size()-2, line.substring(0, line.length()-1));
        }
        IOUtil.saveLinesToFile(lines, localFile, null);
        return notSaveTranslationList;
    }

    private Character getDivisionCharByType(GlobalConstant.OutType outType) {
        switch (outType) {
            case TYPE_JSON:
                return ':';
            case TYPE_PRO:
                return '=';
            default:
                throw new IllegalArgumentException("unknown type of out.");
        }
    }

    private boolean insertStringToListOnLastSameIndex(List<String> lines, String s, Character limitChar, boolean cover) {
        int lastSameIndex = lines.size();
        int lastSameLen = 0;
        int cIndex;
        for (int i=0; i<lines.size(); ++i) {
            String line = lines.get(i).trim();
            String source = s.trim();
            for (cIndex=0; line.length()>=lastSameLen && cIndex<line.length() && cIndex < source.length(); ++cIndex) {
                if(line.charAt(cIndex) != source.charAt(cIndex)) {
                    break;
                }
                if(limitChar != null && line.charAt(cIndex) == limitChar) {
                    if(!cover) {
                        return false;
                    }
                    lines.set(i, s);
                    return true;
                }
            }
            if(cIndex >= lastSameLen) {
                lastSameIndex = i;
                if(cIndex > lastSameLen) lastSameLen = cIndex;
            }
        }
        lines.add(lastSameIndex, s);
        return true;
    }

    private String translateToLineString(Translation translation, GlobalConstant.OutType outType) {
        String key = translation.getKey().trim();
        String translateText = translation.getTranslation().replace("\"", "\\\"").trim();
        switch (outType) {
            case TYPE_JSON:
                return "\t\""+key+"\":\""+translateText+"\",";
            case TYPE_PRO:
                return key+"=\""+translateText+"\"";
            default:
                throw new IllegalArgumentException("unknown type of out.");
        }
    }

    private File getLocalFile(WorkConfig workConfig, String local) {
        return new File(workConfig.getOutput(),
                workConfig.getFileNamePrefix()+local+workConfig.getFileNameSuffix()+workConfig.getOutType().getValue());
    }

    private void mergeNewAddTranslate(WorkConfig workConfig) {
        this.workConfig = workConfig;
        translationList = new ArrayList<>();
        List<Translation> list =  workConfig.getTranslationList();
        if(list != null && !list.isEmpty())
            translationList.addAll(list);
    }

    public void loadTranslateFiles(WorkConfig workConfig, WorkCallback callback) throws NotFoundExcelException, IOException {
        List<File> inputPathList = workConfig.getInput();
        workStage =  new WorkStage(mode, stage, stageCount, 3, "load files", "", new Date(), null, true);
        if(callback != null) {
            callback.onStart(workStage);
        }
        if(sourceFiles == null) {
            sourceFiles = new ArrayList<>();
        }
        sourceFiles.clear();
        int i = 0;
        for (final File file : inputPathList) {
            List<File> files =  IOUtil.fileList(file, true, new DirectoryStream.Filter<File>() {
                @Override
                public boolean accept(File entry) throws IOException {
                    if(entry.isFile()
                            && !entry.getName().startsWith("~$")
                            && (entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLS)
                                    || entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLSX))) {
                        return true;
                    }
                    return false;
                }
            });
            sourceFiles.addAll(files);
            if(callback != null) {
                callback.onProgress(++i, sourceFiles.size());
            }
        }

        if(callback != null) {
            callback.onProgress(sourceFiles.size(), sourceFiles.size());
        }

        if(sourceFiles.isEmpty()) {
            NotFoundExcelException exception = new NotFoundExcelException("Not found file of excel in the specific path.");
            if(callback != null) callback.onError(exception);
            throw exception;
        }
        
        onDone(callback, workStage);
    }

    private void loadAllTranslate(WorkConfig workConfig, WorkCallback callback) throws IOException, InvalidExcelContentException {
        workStage = new WorkStage(mode, stage, stageCount, 3, "load translate", "", new Date(), null, true);
        if(callback != null) {
            callback.onStart(workStage);
        }
        for (int i=0; i<sourceFiles.size(); ++i) {
            try {
                parseTranslateFile(workConfig, sourceFiles.get(i));
            } catch (Exception e) {
                if(null != callback && callback.onError(e)) {
                    throw e;
                }
            }

            if(callback != null) {
                callback.onProgress(i, sourceFiles.size());
            }
        }

        if(callback != null) {
            callback.onProgress(sourceFiles.size(), sourceFiles.size());
        }
        workStage.setSuccess(true);
        onDone(callback, workStage);
    }

    private void parseTranslateFile(WorkConfig workConfig, File file) throws IOException, InvalidExcelContentException {
        HashMap<String, TranslationLocator> translationLocatorMap = workConfig.getTranslationLocatorMap();
        HashMap<String, String> localMap = workConfig.getLocalMap();
        TranslationLocator translationLocator = translationLocatorMap.get(file.getAbsolutePath());
        List<List<String>> excelContent = ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0);
        List<Translation> translationList = workConfig.getTranslationList();

        if(excelContent.size() > 0) {
            if(translationLocator == null) {
                translationLocator = AdditAssist.calcTranslationLocator(excelContent, translationLocatorMap.get(file.getAbsolutePath()));
                if(translationLocator == null) {
                    throw new InvalidExcelContentException("parse failed:"+file.getAbsolutePath());
                }
                translationLocatorMap.put(file.getAbsolutePath(), translationLocator);
            }

            List<String> keys = AdditAssist.getTranslationKeys(excelContent, translationLocator.getKeyLocator());

            if (translationLocator.getKeyLocator().startsWith("c")
                    && translationLocator.getOrientation() == GlobalConstant.Orientation.VERTICAL.ordinal()) {
                for (int i=0; i<excelContent.size(); ++i) {
                    List<String> row = excelContent.get(i);
                    List<Translation> translations = AdditAssist.getTranslationList(row, translationLocator, localMap, keys.get(i));
                    if(translations != null) {
                        translationList.addAll(translations);
                    }
                }
            } else if(translationLocator.getKeyLocator().startsWith("r")
                    && translationLocator.getOrientation() == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
                int columns = excelContent.get(0).size();
                List<String> columnTexts = new ArrayList<>();
                for (int i = 0; i<columns; ++i) {
                    for (int j=0; j < excelContent.size(); ++j) {
                        columnTexts.add(excelContent.get(j).get(i));
                    }
                    List<Translation> translations = AdditAssist.getTranslationList(columnTexts, translationLocator, localMap, keys.get(i));
                    columnTexts.clear();
                    if(translations != null) {
                        translationList.addAll(translations);
                    }
                }
            } else {
                List<String> locals = AdditAssist.getTranslationLocals(excelContent, translationLocator);
                for (int i=0; i<keys.size(); ++i) {
                    for (int j=0; j<locals.size(); ++j) {
                        List<Translation> translations = null;
                        if(translationLocator.getOrientation() == GlobalConstant.Orientation.HORIZONTAL.ordinal())
                            translations = AdditAssist.getTranslationList(excelContent, localMap, keys.get(i), locals.get(j), i, j);
                        else
                            translations = AdditAssist.getTranslationList(excelContent, localMap, keys.get(i), locals.get(j), j, i);
                        if(translations != null) {
                            translationList.addAll(translations);
                        }
                    }
                }
            }
        } else {
            throw new InvalidExcelContentException("parse failed:"+file.getAbsolutePath());
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public List<File> getSourceFiles() {
        return sourceFiles;
    }

    public List<Translation> getTranslationList() {
        return translationList;
    }

    public WorkConfig getWorkConfig() {
        return workConfig;
    }

    private void onDone(WorkCallback callback, WorkStage workStage) {
        if(callback != null) {
            workStage.setEnd(new Date());
            if(workStage.getIndex() == workStage.getCount()) {
                running = false;
            }
            callback.onDone(workStage);
        }
    }
}
