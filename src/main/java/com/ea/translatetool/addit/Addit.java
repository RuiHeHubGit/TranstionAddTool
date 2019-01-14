package com.ea.translatetool.addit;

import com.ea.translatetool.addit.exception.AlreadyExistKeyException;
import com.ea.translatetool.addit.exception.InvalidExcelContentException;
import com.ea.translatetool.addit.exception.NoFoundLocaleException;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Addit {
    public final static int WORK_SCAN_FILE = 0;
    public final static int WORK_CALC_LOCATOR = 1;
    public final static int WORK_LOAD_TRANSLATION = 2;
    public final static int WORK_TRANSLATION_TO_FILE = 3;

    private volatile static boolean running;
    private static Addit  addit;
    private int stage;
    private int stageCount;
    private WorkStage workStage;
    private static ConcurrentLinkedQueue<Translation> noFoundLocaleList;

    private Addit() {}

    private synchronized static Addit getInstance() {
        if(addit == null) {
            addit = new Addit();
        }
        return addit;
    }

    public static Object doWork(WorkConfig workConfig, int startWork, int endWork, WorkCallback callback) {
        Object result = null;
        Addit addit = getInstance();
        try {
            running = true;
            addit.stage = 0;
            addit.stageCount = endWork-startWork+1;
            for (; startWork <= endWork; ++startWork) {
                addit.stage++;
                switch (startWork) {
                    case WORK_SCAN_FILE:
                        result = addit.scanTranslateFiles(workConfig, callback);
                        break;
                    case WORK_CALC_LOCATOR:
                        addit.calcLocator(workConfig, callback);
                        break;
                    case WORK_LOAD_TRANSLATION:
                        addit.loadAllTranslate(workConfig, callback);
                        break;
                    case WORK_TRANSLATION_TO_FILE:
                        addit.translationToFile(workConfig, callback);
                        break;
                }
            }
        } catch (Exception e) {
            addit.workStage.setSuccess(false);
            LoggerUtil.error(e.getMessage());
        } finally {
            if(running) {
                running = false;
                addit.onDone(callback, addit.workStage);
            }
        }
        return result;
    }

    private void calcLocator(WorkConfig workConfig, WorkCallback callback) {
        workStage = new WorkStage(stage, stageCount, WORK_CALC_LOCATOR, "calc locator", "", new Date(), null, true);
        List<File> excelFiles = workConfig.getExcelFiles();
        if(excelFiles.size() > 0) {
            if(callback != null) {
                callback.onProgress(0, excelFiles.size(), "");
            }
        }

        int index = 0;
        TreeMap<String, TranslationLocator> translationLocatorMap = workConfig.getTranslationLocatorMap();
        for (File file : excelFiles) {
            if(translationLocatorMap.containsKey(file.getAbsolutePath())) {
                continue;
            }
            try {
                TranslationLocator translationLocator = AdditAssist.calcTranslationLocator(
                        ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0), workConfig.getLocalMap(), null);
                if(translationLocator != null) {
                    translationLocatorMap.put(file.getAbsolutePath(), translationLocator);
                }
                if(callback != null) {
                    callback.onProgress(++index, excelFiles.size(), file.getName());
                }
            } catch (Throwable t) {
                LoggerUtil.error(t.getMessage());
                if(callback != null) {
                    callback.onError(t);
                }
            }
        }
        onDone(callback, workStage);
    }

    private void translationToFile(WorkConfig workConfig, WorkCallback callback) throws IOException {
        workStage = new WorkStage(stage, stageCount, WORK_TRANSLATION_TO_FILE, "add translate to file", "", new Date(), null, true);
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
                            if(callback == null || callback.onError(new AlreadyExistKeyException(notSave, notSave.size()+" keys repeat."))) {
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
                callback.onProgress(++index, total, "");
            }
        }

        if(callback != null) {
            callback.onProgress(total, total, "");
        }
        workConfig.getTranslationList().clear();
        onDone(callback, workStage);
    }

    private List<Translation> saveTranslateToFile(List<Translation> localAllTranslations, File localFile, GlobalConstant.OutType outType, boolean cover) throws IOException {
        List<Translation> notSaveTranslationList = null;

        if(!localFile.exists()) {
            if(!localFile.getParentFile().exists())
                if(!localFile.getParentFile().mkdirs()) throw new IOException("create dir failed, dir:"+localFile.getParentFile());
            if(!localFile.createNewFile()) {
                LoggerUtil.error("create file failed, file:"+localFile);
            }
        }

        List<String> lines = IOUtil.readText(localFile, null);
        if(lines.isEmpty() && outType == GlobalConstant.OutType.TYPE_JSON) {
            lines.add("{");
            lines.add("}");
        }

        for (Translation translation : localAllTranslations) {
            if(!insertStringToListOnLastSameIndex(lines, translateToLineString(translation, outType, lines), getDivisionCharByType(outType), cover)) {
                if(notSaveTranslationList == null) {
                    notSaveTranslationList = new ArrayList<>();
                }
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

    private String translateToLineString(Translation translation, GlobalConstant.OutType outType, List<String> lines) {
        String key = translation.getKey().trim();
        String translateText = translation.getTranslation().replace("\"", "\\\"").trim();

        switch (outType) {
            case TYPE_JSON:
                String preSpace = "    ";
                Pattern preSpacePattern = Pattern.compile("^(\\s+)\".+");
                for (String line : lines) {
                    Matcher matcher = preSpacePattern.matcher(line);
                    if(matcher.matches()) {
                        preSpace = matcher.group(1);
                        break;
                    }
                }
                return preSpace+"\""+key+"\":\""+translateText+"\",";
            case TYPE_PRO:
                return key+"=\""+translateText+"\"";
            default:
                throw new IllegalArgumentException("unknown type of out.");
        }
    }

    private File getLocalFile(WorkConfig workConfig, String local) {
        return new File(workConfig.getOutput(),
                workConfig.getFilePrefix()+local+workConfig.getFileSuffix()+workConfig.getOutType().getValue());
    }

    private List<File> scanTranslateFiles(WorkConfig workConfig, final WorkCallback callback) throws NotFoundExcelException {
        final List<File> inputPathList = workConfig.getInput();
        List<File> newList = new ArrayList<>();
        workStage =  new WorkStage(stage, stageCount, WORK_SCAN_FILE, "scan files", "", new Date(), null, true);
        if(callback != null) {
            callback.onStart(workStage);
        }

        int i = 0;
        if(callback != null) {
            callback.onProgress(0, inputPathList.size(), "");
        }
        for (final File file : inputPathList) {
            final int index = i;
            List<File> files =  IOUtil.fileList(file, true, new DirectoryStream.Filter<File>() {
                @Override
                public boolean accept(File entry) {
                    return entry.isFile()
                            && !entry.getName().startsWith("~$")
                            && (entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLS)
                            || entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLSX));
                }
            }, new IOUtil.ScanCallBack() {
                @Override
                public void onNewDir(File dir) {
                    if(callback != null) {
                        callback.onProgress(index, inputPathList.size(), "child dir:"+dir.getName());
                    }
                }
            });
            newList.addAll(files);
            if(callback != null) {
                callback.onProgress(++i, inputPathList.size(), file.getName());
            }
        }

        List<File> resultList = new ArrayList<>();
        List<File> excelFiles = workConfig.getExcelFiles();
        for (File file : newList) {
            if(!excelFiles.contains(file)) {
                excelFiles.add(file);
                resultList.add(file);
            }
        }
        if(callback != null) {
            callback.onProgress(i, inputPathList.size(), "");
        }
        onDone(callback, workStage);

        return resultList;
    }

    private void loadAllTranslate(WorkConfig workConfig, WorkCallback callback) throws IOException, InvalidExcelContentException, NotFoundExcelException, NoFoundLocaleException {
        if(noFoundLocaleList == null) {
            noFoundLocaleList = new ConcurrentLinkedQueue<>();
        } else {
            noFoundLocaleList.clear();
        }

        workConfig.getTranslationList().clear();

        List<File> excelFiles = workConfig.getExcelFiles();
        if(excelFiles.isEmpty()) {
            throw new NotFoundExcelException("not excel file");
        }
        workStage = new WorkStage(stage, stageCount, WORK_LOAD_TRANSLATION, "load translate", "", new Date(), null, true);
        if(callback != null) {
            callback.onStart(workStage);
        }
        int index = 0;
        for (File excelFile : excelFiles) {
            try {
                loadTranslateFromFile(workConfig, excelFile);
            } catch (Exception e) {
                if(null == callback || callback.onError(e)) {
                    throw e;
                }
            }

            if(callback != null) {
                callback.onProgress(++index, excelFiles.size(), excelFile.getName());
            }
        }

        if(callback != null) {
            callback.onProgress(excelFiles.size(), excelFiles.size(), "");
        }

        HashMap<String, String> localMap = workConfig.getLocalMap();
        Set<String> ignoreLocaleSet = workConfig.getIgnoreLocaleSet();
        for (Translation translation : workConfig.getTranslationList()) {
            if(!localMap.containsKey(translation.getLocaleKey())
                    && (ignoreLocaleSet == null || !ignoreLocaleSet.contains(translation.getLocaleKey()))) {
                noFoundLocaleList.add(translation);
            }
        }
        if(!noFoundLocaleList.isEmpty()) {
            NoFoundLocaleException notFoundExcelException = new NoFoundLocaleException (noFoundLocaleList,
                    "had locale not found in locale map. count:"+noFoundLocaleList.size());
            if(null == callback || callback.onError(notFoundExcelException)) {
                throw notFoundExcelException;
            }
        }
        workStage.setSuccess(true);
        onDone(callback, workStage);
    }

    private static void loadTranslateFromFile(WorkConfig workConfig, File file) throws IOException, InvalidExcelContentException {
        TranslationLocator translationLocator = workConfig.getTranslationLocatorMap().get(file.getAbsolutePath());
        HashMap<String, String> localMap = workConfig.getLocalMap();
        if(translationLocator == null) {
            translationLocator = AdditAssist.calcTranslationLocator(ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0),
                    localMap, null);
            if(translationLocator == null) {
                throw new InvalidExcelContentException("parse failed:" + file.getAbsolutePath());
            }
        }

        List<Translation> translationList = workConfig.getTranslationList();
        List<List<String>> excelContent = ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0);
        if(excelContent.size() > 0) {
            List<String> keys = AdditAssist.getTranslationKeys(excelContent, translationLocator.getKeyLocator());

            if (translationLocator.getKeyLocator().startsWith("c")
                    && translationLocator.getOrientation() == GlobalConstant.Orientation.VERTICAL.ordinal()) {
                for (int i=0; i<excelContent.size(); ++i) {
                    List<String> row = excelContent.get(i);
                    List<Translation> translations = AdditAssist.getTranslationList(keys.get(i),
                            row.get(translationLocator.getLocalLocator()),
                            row.get(translationLocator.getTranslationLocator()), localMap, file);
                    if(translations != null) {
                        translationList.addAll(translations);
                    }
                }
            } else if(translationLocator.getKeyLocator().startsWith("r")
                    && translationLocator.getOrientation() == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
                int columns = excelContent.get(0).size();
                for (int i = 0; i<columns; ++i) {
                    List<String> columnTexts = AdditAssist.getTableColumnTexts(excelContent, i);
                    List<Translation> translations = AdditAssist.getTranslationList(keys.get(i),
                            columnTexts.get(translationLocator.getLocalLocator()),
                            columnTexts.get(translationLocator.getTranslationLocator()), localMap, file);
                    columnTexts.clear();
                    if(translations != null) {
                        translationList.addAll(translations);
                    }
                }
            } else {
                List<String> locals = AdditAssist.getTranslationLocals(excelContent, translationLocator);
                for (int i=0; i<keys.size(); ++i) {
                    for (int j=0; j<locals.size(); ++j) {
                        List<Translation> translations;
                        if(translationLocator.getOrientation() == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
                            if(AdditAssist.calcListNotSimilarLevel(excelContent.get(i)) < 0.5) {
                                break;
                            }
                            translations = AdditAssist.getTranslationList(keys.get(i), locals.get(j), excelContent.get(i).get(j), localMap, file);
                        } else {
                            if(AdditAssist.calcListNotSimilarLevel(AdditAssist.getTableColumnTexts(excelContent, i)) < 0.5) {
                                break;
                            }
                            translations = AdditAssist.getTranslationList(keys.get(i), locals.get(j), excelContent.get(j).get(i), localMap, file);
                        }
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

    private void onDone(WorkCallback callback, WorkStage workStage) {
        if(callback != null) {
            workStage.setEnd(new Date());
            if(workStage.getIndex() == workStage.getCount()) {
                running = false;
            }
            callback.onDone(workStage);
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public static ConcurrentLinkedQueue<Translation> getNoFoundLocaleList() {
        return noFoundLocaleList;
    }
}
