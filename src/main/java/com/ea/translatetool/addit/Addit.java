package com.ea.translatetool.addit;

import com.ea.translatetool.addit.exception.AlreadyExistKeyException;
import com.ea.translatetool.addit.exception.InvalidExcelContentException;
import com.ea.translatetool.addit.exception.NotFoundExcelException;
import com.ea.translatetool.addit.mode.ColumnPosition;
import com.ea.translatetool.addit.mode.Translate;
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
        try {
            running = true;
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
        } catch (NotFoundExcelException e) {
            LoggerUtil.error(e.getMessage());
        } catch (InvalidExcelContentException e) {
            LoggerUtil.error(e.getMessage());
        } catch (IOException e) {
            LoggerUtil.error(e.getMessage());
        } finally {
            running = false;
        }
    }

    private void doWork(WorkConfig workConfig, WorkCallback callback) {

        WorkStage workStage =new WorkStage(mode, stage, stageCount, 3, "add translate to file", "", new Date(), null);
        if(callback != null) {
            callback.onStart(workStage);
        }

        Collections.sort(workConfig.getTranslateList(), new Comparator<Translate>() {
            @Override
            public int compare(Translate o1, Translate o2) {
                return o1.getLocal().compareTo(o2.getLocal());
            }
        });

        int index = 0;
        int total = workConfig.getTranslateList().size();
        String lastLocal = null;
        List<Translate> localAllTranslates = new ArrayList<>();
        for (Translate translate : workConfig.getTranslateList()) {
            if(!translate.getLocal().equals(lastLocal)) {
                if(null != lastLocal && !lastLocal.isEmpty()) {
                    try {
                        List<Translate> notSave = saveTranslateToFile(localAllTranslates, getLocalFile(workConfig, lastLocal),
                                workConfig.getOutType(), false);
                        if(notSave != null && !notSave.isEmpty()) {
                            if(callback != null && !callback.onError(new AlreadyExistKeyException(notSave, notSave.size()+" keys repeat."))) {
                                saveTranslateToFile(notSave, getLocalFile(workConfig, lastLocal),
                                        workConfig.getOutType(), false);
                            }
                        }
                    } catch (IOException e) {
                        if(callback != null && callback.onError(e)) {
                            return;
                        }
                    }
                    localAllTranslates.clear();
                }
                lastLocal = translate.getLocal();
            }
            localAllTranslates.add(translate);
            if(callback != null) {
                callback.onProgress(++index, total);
            }
        }

        if(callback != null) {
            callback.onProgress(total, total);
        }
        if(callback != null) {
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }
        workConfig.getTranslateList().clear();
    }

    private List<Translate> saveTranslateToFile(List<Translate> localAllTranslates, File localFile, GlobalConstant.OutType outType, boolean cover) throws IOException {
        List<Translate> notSaveTranslateList = null;
        if(!cover) {
            notSaveTranslateList = new ArrayList<>();
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
        for (Translate translate : localAllTranslates) {
            if(!insertStringToListOnLastSameIndex(lines, translateToLineString(translate, outType), getDivisionCharByType(outType), cover)) {
                notSaveTranslateList.add(translate);
            }
        }
        if(lines.get(lines.size()-2).endsWith(",")) {
            String line = lines.get(lines.size()-2);
            lines.set(lines.size()-2, line.substring(0, line.length()-1));
        }
        IOUtil.saveLinesToFile(lines, localFile, null);
        return notSaveTranslateList;
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

    private String translateToLineString(Translate translate, GlobalConstant.OutType outType) {
        String key = translate.getKey().trim();
        String translateText = translate.getTranslate().replace("\"", "\\\"").trim();
        translateText = translateText;
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
                workConfig.getFilePrefix()+local+workConfig.getFileSuffix()+"."+workConfig.getOutType().getValue());
    }

    private void mergeNewAddTranslate(WorkConfig workConfig) {
        this.workConfig = workConfig;
        translateList = new ArrayList<>();
        List<Translate> list =  workConfig.getTranslateList();
        if(list != null && !list.isEmpty())
            translateList.addAll(list);
    }

    public void loadTranslateFiles(WorkConfig workConfig, WorkCallback callback) throws NotFoundExcelException, IOException {
        List<File> inputPathList = workConfig.getInput();
        WorkStage workStage =  new WorkStage(mode, stage, stageCount, 3, "load files", "", new Date(), null);
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
            if(callback != null && callback.onError(exception)) {
                throw exception;
            } else {
                throw exception;
            }
        }
        if(callback != null) {
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }
    }

    private void loadAllTranslate(WorkConfig workConfig, WorkCallback callback) throws IOException, InvalidExcelContentException {
        WorkStage workStage = new WorkStage(mode, stage, stageCount, 3, "load translate", "", new Date(), null);
        if(callback != null) {
            callback.onStart(workStage);
        }
        if(workConfig.getTranslateList() == null) {
            workConfig.setTranslateList(new ArrayList<Translate>());
        }
        if(workConfig.getColumnPositionMap() == null) {
            workConfig.setColumnPositionMap(new HashMap<String, ColumnPosition>());
        }
        for (int i=0; i<sourceFiles.size(); ++i) {
            try {
                parseTranslateFile(workConfig, sourceFiles.get(i));
            } catch (Exception e) {
                if(callback != null && callback.onError(e)) {
                    throw e;
                } else {
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
        if(callback != null) {
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }
    }

    private void parseTranslateFile(WorkConfig workConfig, File file) throws IOException, InvalidExcelContentException {
        HashMap<String, ColumnPosition> columnPositionMap = workConfig.getColumnPositionMap();
        HashMap<String, String> localMap = workConfig.getLocalMap();
        ColumnPosition columnPosition = columnPositionMap.get(file.getAbsolutePath());
        List<List<String>> excelContent = ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0);
        List<Translate> translateList = workConfig.getTranslateList();
        if(excelContent.size() > 0) {
            if(columnPosition == null) {
                columnPosition = AdditAssist.calcColumnPosition(excelContent,
                        workConfig.getVertical(),
                        workConfig.getKeyColumn(),
                        workConfig.getLocalColumn(),
                        workConfig.getTranslateColumn());
                if(columnPosition == null) {
                    throw new InvalidExcelContentException(file.getAbsolutePath());
                }
                columnPositionMap.put(file.getAbsolutePath(), columnPosition);
            }

            if (columnPosition.getOrientation() == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
                for (List<String> row : excelContent) {
                    String key = row.get(columnPosition.getKeyColumn());
                    String local = row.get(columnPosition.getLocalColumn());
                    String translateText = row.get(columnPosition.getTranslateColumn());
                    if(localMap.containsKey(local)) {
                        local = localMap.get(local);
                    }
                    if(key.indexOf('.') <= 0 && local.indexOf('_') <= 0) continue;
                    Translate translate = new Translate();
                    translate.setKey(key);
                    translate.setLocal(local);
                    translate.setTranslate(translateText);
                    if(translate.getTranslate().equals("[N/A]")) continue;
                    translateList.add(translate);
                }
            } else {
                List<String> columns = excelContent.get(0);
                for (int i = 0; i<columns.size(); ++i) {
                    String key = excelContent.get(columnPosition.getKeyColumn()).get(i);
                    String local = excelContent.get(columnPosition.getLocalColumn()).get(i);
                    String translateText = excelContent.get(columnPosition.getTranslateColumn()).get(i);
                    if(localMap.containsKey(local)) {
                        local = localMap.get(local);
                    }
                    if(key.indexOf('.') <= 0 && local.indexOf('_') <= 0) continue;
                    Translate translate = new Translate();
                    translate.setKey(key);
                    translate.setLocal(local);
                    translate.setTranslate(translateText);
                    if(translate.getTranslate().equals("[N/A]")) continue;
                    translateList.add(translate);
                }
            }
        } else {
            throw new InvalidExcelContentException(file.getAbsolutePath());
        }
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
