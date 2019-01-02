package com.ea.translatetool.addit;

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
import java.util.regex.Pattern;

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
                        saveTranslateToFile(localAllTranslates, getLocalFile(workConfig, lastLocal),
                                workConfig.getOutType());
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
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }
    }

    private void saveTranslateToFile(List<Translate> localAllTranslates, File localFile, GlobalConstant.OutType outType) throws IOException {
        if(!localFile.exists()) {
            if(!localFile.getParentFile().exists())
                localFile.getParentFile().mkdirs();
            localFile.createNewFile();
        }
        List<String> lines = IOUtil.readText(localFile);
        if(lines.isEmpty()) {
            addTranslateStartAndEndToList(lines, outType);
        }
        for (Translate translate : localAllTranslates) {
            insertStringToListOnLastSameIndex(lines, translateToLineString(translate, outType));
        }
        IOUtil.saveLinesToFile(lines, localFile);
    }

    private void insertStringToListOnLastSameIndex(List<String> lines, String s) {
        int lastSameIndex = lines.size();
        int lastSameLen = 0;
        int cIndex;
        for (int i=0; i<lines.size(); ++i) {
            String line = lines.get(i);
            for (cIndex=0; line.length()>=lastSameLen && cIndex<line.length() && cIndex < s.length(); ++cIndex) {
                if(line.charAt(cIndex) != s.charAt(cIndex)) {
                    break;
                }
            }
            if(cIndex >= lastSameLen) {
                lastSameIndex = i;
                if(cIndex > lastSameLen) lastSameLen = cIndex;
            }
        }
        lines.add(lastSameIndex, s);
    }

    private String translateToLineString(Translate translate, GlobalConstant.OutType outType) {
        switch (outType) {
            case TYPE_JSON:
                return translate.getKey().trim()+":\""+translate.getTranslate().trim()+"\"";
            case TYPE_XML:
                return translate.getKey().trim()+"=\""+translate.getTranslate().trim()+"\"";
            default:
                throw new IllegalArgumentException("unknown type of out.");
        }
    }

    private void addTranslateStartAndEndToList(List<String> lines, GlobalConstant.OutType outType) {
        switch (outType) {
            case TYPE_JSON:
                lines.add("{");
                lines.add("}");
                break;
            case TYPE_XML:
                break;
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

    public void loadTranslateFiles(WorkConfig workConfig, WorkCallback callback) throws NotFoundExcelException {
        List<File> inputPathList = workConfig.getInput();
        WorkStage workStage =  new WorkStage(mode, stage, stageCount, 3, "load files", "", new Date(), null);
        if(callback != null) {
            callback.onStart(workStage);
        }
        if(sourceFiles == null) {
            sourceFiles = new ArrayList<>();
        }
        sourceFiles.clear();
        for (final File file : inputPathList) {
            List<File> files =  IOUtil.fileList(file, true, new DirectoryStream.Filter<File>() {
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
            workStage.setEnd(new Date());
            callback.onDone(workStage);
        }

        System.out.println(workConfig.getColumnPositionMap());
    }

    private void parseTranslateFile(WorkConfig workConfig, File file) throws IOException, InvalidExcelContentException {
        HashMap<String, ColumnPosition> columnPositionMap = workConfig.getColumnPositionMap();
        ColumnPosition columnPosition = columnPositionMap.get(file.getAbsolutePath());
        List<List<String>> excelContent = ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0);
        List<Translate> translateList = workConfig.getTranslateList();
        if(excelContent.size() > 0) {
            if(columnPosition == null) {
                columnPosition = getSuitablePosition(excelContent,
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
                    Translate translate = new Translate();
                    translate.setKey(row.get(columnPosition.getKeyColumn()));
                    translate.setLocal(row.get(columnPosition.getLocalColumn()));
                    translate.setTranslate(row.get(columnPosition.getTranslateColumn()));
                    translateList.add(translate);
                }
            } else {
                List<String> columns = excelContent.get(0);
                for (int i = 0; i<columns.size(); ++i) {
                    Translate translate = new Translate();
                    translate.setKey(excelContent.get(columnPosition.getKeyColumn()).get(i));
                    translate.setLocal(excelContent.get(columnPosition.getLocalColumn()).get(i));
                    translate.setTranslate(excelContent.get(columnPosition.getTranslateColumn()).get(i));
                    translateList.add(translate);
                }
            }
        } else {
            throw new InvalidExcelContentException(file.getAbsolutePath());
        }
    }

    private ColumnPosition getSuitablePosition(List<List<String>> excelContent, Boolean vertical,
                                               Integer keyColumn, Integer localColumn, Integer translateColumn) {

        if(excelContent == null || excelContent.isEmpty()
                || excelContent.size() < 3 && excelContent.get(0).size() < 3) {
            return null;
        }

        int rows = excelContent.size();
        int columns = excelContent.get(0).size();

        ColumnPosition columnPosition = createPositionByParams(rows, columns, vertical, keyColumn, localColumn, translateColumn);
        if(columnPosition != null) {
            return columnPosition;
        }

        float maxKeySameLv = 0, maxLocalSameLv = 0, maxTranslateSameLv = 0;
        float keySameLv, localSameLv, listDisperseLv;
        for (int i=0; i<rows; ++i) {
            keySameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_KEY);
            localSameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_LOCAL);

            if(keySameLv >= localSameLv && keySameLv >= maxKeySameLv) {
                maxKeySameLv = keySameLv;
                keyColumn = i;
            }

            if(localSameLv >= keySameLv && localSameLv >= maxLocalSameLv) {
                maxLocalSameLv = localSameLv;
                localColumn = i;
            }

            if(keySameLv > 0.9 || localSameLv > 0.9) continue;

            listDisperseLv = calcListNotSimilarLevel(excelContent.get(i));
            if(listDisperseLv >= keySameLv && listDisperseLv >= localSameLv && listDisperseLv >= maxTranslateSameLv) {
                maxTranslateSameLv = listDisperseLv;
                translateColumn = i;
            }
        }

        float maxKeySameLvH = 0, maxLocalSameLvH = 0, maxTranslateLvH = 0;
        int keyColumnH = 0, localColumnH = 0, translateColumnH = 0;
        List<String> columnContents = new ArrayList<>();
        for (int i=0; i<columns; ++i) {
            columnContents.clear();
            for (int j=0; j<rows; ++j) {
                columnContents.add(excelContent.get(j).get(i));
            }
            keySameLv = calcSimilarLevel(columnContents, GlobalConstant.REGEX_KEY);
            localSameLv = calcSimilarLevel(columnContents, GlobalConstant.REGEX_LOCAL);

            if(keySameLv >= localSameLv && keySameLv >= maxKeySameLvH) {
                maxKeySameLvH = keySameLv;
                keyColumnH = i;
            }

            if(localSameLv >= keySameLv && localSameLv >= maxLocalSameLvH) {
                maxLocalSameLvH = localSameLv;
                localColumnH = i;
            }

            if(keySameLv > 0.9 || localSameLv > 0.9) continue;

            listDisperseLv = calcListNotSimilarLevel(columnContents);
            if(listDisperseLv >= keySameLv && listDisperseLv >= localSameLv && listDisperseLv >= maxTranslateLvH) {
                maxTranslateLvH = listDisperseLv;
                translateColumnH = i;
            }
        }

        if(maxKeySameLv < 0.5 || maxLocalSameLv < 0.5 || maxTranslateSameLv < 0.5) {
            maxKeySameLv = maxLocalSameLv = maxTranslateSameLv = 0;
        }

        if(maxKeySameLvH >= 0.5 && maxLocalSameLvH >= 0.5 && maxTranslateLvH >= 0.5) {
            if(maxKeySameLvH + maxLocalSameLvH + maxTranslateLvH > maxKeySameLv + maxLocalSameLv + maxTranslateSameLv) {
                vertical = false;
                keyColumn = keyColumnH;
                localColumn = localColumnH;
                translateColumn = translateColumnH;
                maxKeySameLv = maxLocalSameLvH;
                maxLocalSameLv = maxLocalSameLvH;
                maxTranslateSameLv = maxTranslateLvH;
            }
        } else {
            vertical = true;
        }

        if(maxKeySameLv < 0.5 || maxLocalSameLv < 0.5 || maxTranslateSameLv < 0.5) {
            return null;
        }

        columnPosition = new ColumnPosition();
        if(vertical)
            columnPosition.setTranslateColumn(GlobalConstant.Orientation.VERTICAL.ordinal());
        else
            columnPosition.setOrientation(GlobalConstant.Orientation.HORIZONTAL.ordinal());
        columnPosition.setKeyColumn(keyColumn);
        columnPosition.setLocalColumn(localColumn);
        columnPosition.setTranslateColumn(translateColumn);
        return columnPosition;
    }

    private ColumnPosition createPositionByParams(int rows, int columns,Boolean vertical, Integer keyColumn, Integer localColumn, Integer translateColumn) {
        if(keyColumn != null && localColumn != null && translateColumn != null) {
            if (vertical != null && vertical && rows >= 3
                    && keyColumn < rows && localColumn < rows && translateColumn < rows
                    || (vertical == null || !vertical) && columns >= 3
                    && keyColumn < columns && localColumn < columns && translateColumn < columns) {

                ColumnPosition columnPosition = new ColumnPosition();
                if(vertical == null || !vertical)
                    columnPosition.setTranslateColumn(GlobalConstant.Orientation.HORIZONTAL.ordinal());
                else
                    columnPosition.setOrientation(GlobalConstant.Orientation.VERTICAL.ordinal());
                columnPosition.setKeyColumn(keyColumn);
                columnPosition.setLocalColumn(localColumn);
                columnPosition.setTranslateColumn(translateColumn);
                return columnPosition;
            }
        }
        return null;
    }

    private float calcListNotSimilarLevel(List<String> strings) {
        int count = 0;
        if(strings == null || strings.size() < 2) return 1;

        int right = strings.size()-1;
        String strSource;
        String strCompared;
        for (int i=0; i<right; ++i) {
            strSource = strings.get(i);
            strCompared = strings.get(i+1);
            if(sourceFiles.isEmpty() || strCompared.isEmpty()) {
                continue;
            }
            if(!isSimilar(strSource, strCompared, 10, strSource.length())) {
                ++count;
            }
        }
        return 1.0f * count / strings.size();
    }

    private float calcSimilarLevel(List<String> strings, String regex) {
        int count = 0;
        if(strings == null || strings.isEmpty()) return 0;

        Pattern pattern = Pattern.compile(regex);
        for (String s : strings) {
            if(pattern.matcher(s).matches()) {
                ++count;
            }
        }
        return 1.0f * count / strings.size();
    }

    /**
     * 判断两个字符串相似度,可设置level
     * @param strSource 原字符串
     * @param strCompared 比较字符串
     * @param level 评分阀值
     * @param moreCount 比较字符串比原字符串多多少个限制
     * @return
     */
    public static Boolean isSimilar(String strSource,String strCompared, int level,int moreCount){
        if(strCompared.length()-strSource.length()>moreCount){
            return false;
        }
        int count=strSource.length();
        int maxSameCount=0;
        //遍历count次
        for(int i=0;i<count;i++){
            int nowSameCount=0;
            int c=0;
            int lastIndex=0;//记录上一次匹配的目标索引
            //遍历每一次的原字符串所有字段
            for(int j=i;j<strSource.length();j++){
                char charSource=strSource.charAt(j);
                for(;c<strCompared.length();c++){
                    char charCompare=strCompared.charAt(c);
                    if(charSource==charCompare){
                        nowSameCount++;
                        lastIndex=++c;//如果匹配,手动加1
                        break;
                    }
                }
                c=lastIndex;//遍历完目标字符串,记录当前匹配索引
            }
            if(nowSameCount>maxSameCount){
                maxSameCount=nowSameCount;
            }
        }
        //大于原字符串数量的情况
        if(maxSameCount>count){
            maxSameCount=count-(maxSameCount-count);
        }
        double dLv= (double)100*maxSameCount/count;
        int iLv=10*maxSameCount/count*10;
        int cha=(int)dLv-iLv;
        int yu=cha>5?1:0;
        iLv+=yu*10;
        if(iLv/10>=level){
            return true;
        }else{
            return false;
        }
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
        workConfig.setOutType(GlobalConstant.OutType.TYPE_JSON);
        workConfig.setVertical(false);
        workConfig.setFilePrefix("");
        workConfig.setFileSuffix("");

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
