package com.ea.translatetool.addit;

import com.ea.translatetool.addit.mode.ColumnPosition;
import com.ea.translatetool.addit.mode.Translate;
import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.config.ConfigRepository;
import com.ea.translatetool.config.FileConfigRepositoryImpl;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.util.IOUtil;
import com.ea.translatetool.util.PID;
import com.ea.translatetool.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class AdditAssist {
    public static void saveKeyExistTranslate(List<Translate> existList, File saveFile) throws IOException {
        List<String> translateLines = new ArrayList<>();
        translateLines.add("[");
        Collections.sort(existList, new Comparator<Translate>() {
            @Override
            public int compare(Translate o1, Translate o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        for (int i=0; i<existList.size(); ++i) {
            Translate translate = existList.get(i);
            String json = "\t{\"key\":\""+translate.getKey()+"\",\"local\":\""
                    +translate.getLocal()+"\",\"translate\":\""
                    +translate.getTranslate().replace("\\", "\\\\").replace("\"","\\")+"\"}";
            if(i < existList.size()-1)
                json += ",";
            translateLines.add(json);
        }
        translateLines.add("]");
        IOUtil.saveLinesToFile(translateLines, saveFile, null);
    }

    public static File createExistKeySaveFile(AppConfig appConfig) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
        File file = new File(appConfig.getExistKeySaveDir(), PID.getPID()+"_key_exist_"+dateFormat.format(new Date())+".json");
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if(!file.exists())
            file.createNewFile();
        return file;
    }

    public static WorkConfig createWorkConfig(AppConfig config) {
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

        workConfig.setLocalMap(loadLocalMap(config.getLocalMapFilePath()));
        workConfig.setFilePrefix(config.getFilePrefix());
        workConfig.setFileSuffix(config.getFileSuffix());
        List<File> inputPaths = new ArrayList<>();
        for (String path : config.getInPath()) {
            File file = new File(path);
            if(file.exists()) {
                inputPaths.add(file);
            }
        }
        workConfig.setInput(inputPaths);

        File outPath = new File(config.getOutPath());
        if(outPath.exists()) {
            workConfig.setOutput(outPath);
        }
        return workConfig;
    }


    public static HashMap<String, String> loadLocalMap(String localMapFilePath) {
        ConfigRepository configRepository = FileConfigRepositoryImpl.getInstance();
        Properties properties = new Properties();
        properties.put(FileConfigRepositoryImpl.CONFIG_FILE_PATH_KEY, localMapFilePath);
        return configRepository.load(HashMap.class, properties);
    }


    public static ColumnPosition calcColumnPosition(List<List<String>> excelContent, Boolean vertical,
                                                    String key, Integer localColumn, Integer translateColumn) {

        if(excelContent == null || excelContent.isEmpty()
                || excelContent.size() < 3 && excelContent.get(0).size() < 3) {
            return null;
        }

        int rows = excelContent.size();
        int columns = excelContent.get(0).size();

        ColumnPosition columnPosition = createPositionByParams(rows, columns, vertical, key, localColumn, translateColumn);
        if(columnPosition != null) {
            return columnPosition;
        }

        key = calcKey(excelContent);
        if(key == null) return null;


        float maxLocalSameLv = 0.5f, maxTranslateSameLv = 0.5f;
        float localSameLv, listDisperseLv;
        for (int i=0; i<rows; ++i) {
            localSameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_LOCAL);

            if(localSameLv >= maxLocalSameLv) {
                maxLocalSameLv = localSameLv;
                localColumn = i;
            }

            if(localSameLv > 0.9) continue;

            listDisperseLv = calcListNotSimilarLevel(excelContent.get(i));
            if(listDisperseLv >= localSameLv && listDisperseLv >= maxTranslateSameLv) {
                maxTranslateSameLv = listDisperseLv;
                translateColumn = i;
            }
        }

        float maxLocalSameLvH = 0.5f, maxTranslateLvH = 0.5f;
        int localColumnH = 0, translateColumnH = 0;
        List<String> columnContents = new ArrayList<>();
        for (int i=0; i<columns; ++i) {
            columnContents.clear();
            for (int j=0; j<rows; ++j) {
                columnContents.add(excelContent.get(j).get(i));
            }
            localSameLv = calcSimilarLevel(columnContents, GlobalConstant.REGEX_LOCAL);

            if(localSameLv >= maxLocalSameLvH) {
                maxLocalSameLvH = localSameLv;
                localColumnH = i;
            }

            if(localSameLv > 0.9) continue;

            listDisperseLv = calcListNotSimilarLevel(columnContents);
            if(listDisperseLv >= localSameLv && listDisperseLv >= maxTranslateLvH) {
                maxTranslateLvH = listDisperseLv;
                translateColumnH = i;
            }
        }

        if(maxLocalSameLv < 0.5 || maxTranslateSameLv < 0.5) {
            maxLocalSameLv = maxTranslateSameLv = 0;
        }

        if(maxLocalSameLvH >= 0.5 && maxTranslateLvH >= 0.5) {
            if(maxLocalSameLvH + maxTranslateLvH > maxLocalSameLv + maxTranslateSameLv) {
                vertical = false;
                localColumn = localColumnH;
                translateColumn = translateColumnH;
                maxLocalSameLv = maxLocalSameLvH;
                maxTranslateSameLv = maxTranslateLvH;
            }
        } else {
            vertical = true;
        }

        if(maxLocalSameLv < 0.5 || maxTranslateSameLv < 0.5) {
            return null;
        }

        columnPosition = new ColumnPosition();
        if(vertical)
            columnPosition.setTranslateColumn(GlobalConstant.Orientation.VERTICAL.ordinal());
        else
            columnPosition.setOrientation(GlobalConstant.Orientation.HORIZONTAL.ordinal());
        columnPosition.setKey(key);
        columnPosition.setLocalColumn(localColumn);
        columnPosition.setTranslateColumn(translateColumn);
        return columnPosition;
    }

    public static ColumnPosition createPositionByParams(int rows, int columns, Boolean vertical, String key, Integer localColumn, Integer translateColumn) {

        if(key != null && localColumn != null && translateColumn != null) {
            if (vertical != null && vertical && rows >= 3
                    && localColumn < rows && translateColumn < rows
                    || (vertical == null || !vertical) && columns >= 3
                    && localColumn < columns && translateColumn < columns) {

                if(key == null) return null;

                ColumnPosition columnPosition = new ColumnPosition();
                if(vertical == null || !vertical)
                    columnPosition.setTranslateColumn(GlobalConstant.Orientation.HORIZONTAL.ordinal());
                else
                    columnPosition.setOrientation(GlobalConstant.Orientation.VERTICAL.ordinal());
                columnPosition.setKey(key);
                columnPosition.setLocalColumn(localColumn);
                columnPosition.setTranslateColumn(translateColumn);
                return columnPosition;
            }
        }
        return null;
    }

    private static String calcKey(List<List<String>> excelContent) {

        HashMap<String, Integer> sameMap = new HashMap<>();
        Integer maxCount = 0;
        String mostSameString = null;
        for (int i= 0; i<excelContent.size(); ++i) {
            List<String> row = excelContent.get(i);
            for (int j=0; j<row.size(); ++j) {
                Pattern pattern = Pattern.compile(GlobalConstant.REGEX_KEY);
                String value = row.get(j);
                if(pattern.matcher(value).matches()) {
                    if(!sameMap.containsKey(value)) {
                        sameMap.put(value, 1);
                    } else {
                        Integer count = sameMap.get(value);
                        sameMap.put(value, ++count);
                        if(count > maxCount) {
                            maxCount = count;
                            mostSameString = value;
                        }
                    }
                }
            }
        }
        return mostSameString;
    }

    public static float calcListNotSimilarLevel(List<String> strings) {
        int count = 0;
        if(strings == null || strings.size() < 2) return 1;

        int right = strings.size()-1;
        String strSource;
        String strCompared;
        for (int i=0; i<right; ++i) {
            strSource = strings.get(i);
            strCompared = strings.get(i+1);
            if(strSource.isEmpty() || strCompared.isEmpty()) {
                continue;
            }
            if(!StringUtil.isSimilar(strSource, strCompared, 10, strSource.length())) {
                ++count;
            }
        }
        return 1.0f * count / strings.size();
    }

    public static float calcSimilarLevel(List<String> strings, String regex) {
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
}
