package com.ea.translatetool.addit;

import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.addit.mode.Translation;
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
    public static void saveKeyExistTranslation(List<Translation> existList, File saveFile) throws IOException {
        List<String> translateLines = new ArrayList<>();
        translateLines.add("[");
        Collections.sort(existList, new Comparator<Translation>() {
            @Override
            public int compare(Translation o1, Translation o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        for (int i=0; i<existList.size(); ++i) {
            Translation translation = existList.get(i);
            String json = "\t{\"key\":\""+ translation.getKey()+"\",\"local\":\""
                    + translation.getLocal()+"\",\"translation\":\""
                    + translation.getTranslation().replace("\\", "\\\\").replace("\"","\\")+"\"}";
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


    public static TranslationLocator calcTranslationLocator(List<List<String>> excelContent, TranslationLocator locator) {

        if(excelContent == null || excelContent.isEmpty()
                || excelContent.size() < 3 && excelContent.get(0).size() < 3) {
            return null;
        }

        int rows = excelContent.size();
        int columns = excelContent.get(0).size();

        if(isValidTranslationLocator(rows, columns, locator)) {
            return locator;
        }

        String keyLocator = null;
        Integer localLocator = null;
        Integer translationLocator = null;
        Integer orientation = GlobalConstant.Orientation.HORIZONTAL.ordinal();

        float maxKeySameLv = 0.5f, maxLocalSameLv = 0.5f, maxTranslationSameLv = 0.5f;
        float keySameLv, localSameLv, translationSameLv;
        List<String> columnContents = new ArrayList<>();

        for (int i = 0; i < columns && i < 3; ++i) {
            columnContents.clear();
            for (int j = 0; j < rows; ++j) {
                columnContents.add(excelContent.get(j).get(i));
            }
            keySameLv = calcSimilarLevel(columnContents, GlobalConstant.REGEX_KEY);
            if (keySameLv >= maxKeySameLv) {
                maxKeySameLv = keySameLv;
                keyLocator = "c" + i;
            }
        }
        if(maxKeySameLv < 1.0f) {
            for (int i=0; i<rows && i<3; ++i) {
                keySameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_KEY);
                if(keySameLv >= maxKeySameLv) {
                    maxKeySameLv = keySameLv;
                    keyLocator = "r"+i;
                }
            }
        }
        if(keyLocator == null) {
            return null;
        }

        for (int i=0; i<rows; ++i) {
            localSameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_LOCAL);
            if(localSameLv >= maxLocalSameLv) {
                maxLocalSameLv = localSameLv;
                localLocator = i;
            }
        }
        if(maxLocalSameLv < 1.0f) {
            for (int i = 0; i < columns; ++i) {
                columnContents.clear();
                for (int j = 0; j < rows; ++j) {
                    columnContents.add(excelContent.get(j).get(i));
                }
                localSameLv = calcSimilarLevel(columnContents, GlobalConstant.REGEX_LOCAL);
                if (localSameLv >= maxLocalSameLv) {
                    maxLocalSameLv = localSameLv;
                    localLocator = i;
                    orientation = GlobalConstant.Orientation.VERTICAL.ordinal();
                }
            }
        }
        if(localLocator == null) {
            return null;
        }

        int num = Integer.parseInt(keyLocator.substring(1));
        if(keyLocator.startsWith("c") && orientation == GlobalConstant.Orientation.VERTICAL.ordinal()) {
            for (int i=0; i<columns; ++i) {
                if(i == num || i == localLocator) {
                    continue;
                }
                columnContents.clear();
                for (int j = 0; j < rows; ++j) {
                    columnContents.add(excelContent.get(j).get(i));
                }
                translationSameLv = calcListNotSimilarLevel(columnContents);
                if(translationSameLv > maxTranslationSameLv) {
                    maxTranslationSameLv = translationSameLv;
                    translationLocator = i;
                }
            }
            if(translationLocator == null) {
                return null;
            }
        } else if(keyLocator.startsWith("r") && orientation == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
            for (int i=0; i<rows; ++i) {
                if(i == num || i == localLocator) {
                    continue;
                }
                translationSameLv = calcListNotSimilarLevel(excelContent.get(i));
                if(translationSameLv > maxTranslationSameLv) {
                    maxTranslationSameLv = translationSameLv;
                    translationLocator = i;
                }
            }
            if(translationLocator == null) {
                return null;
            }
        }

        if(locator == null) {
            locator = new TranslationLocator();
        }
        locator.setKeyLocator(keyLocator);
        locator.setLocalLocator(localLocator);
        locator.setTranslationLocator(translationLocator);
        locator.setOrientation(orientation);

        return locator;
    }

    public static boolean isValidTranslationLocator(int rows, int columns, TranslationLocator locator) {

        if(locator != null && locator.getKeyLocator() != null && locator.getLocalLocator() != null && locator.getTranslationLocator() != null) {
            if (locator.getOrientation() != null && locator.getOrientation() >= 0  && locator.getOrientation() < 3 && rows >= 3) {

                if(locator.getKeyLocator().startsWith("r")) {
                    int keyRow = Integer.parseInt(locator.getKeyLocator().substring(1));
                    if(keyRow > rows) {
                        return false;
                    }
                } else {
                    int keyCol = Integer.parseInt(locator.getKeyLocator().substring(1));
                    if(keyCol > columns) {
                        return false;
                    }
                }

                if(locator.getOrientation() == 0 && (locator.getLocalLocator() > columns || locator.getTranslationLocator() > columns)) {
                    return false;
                }

                if(locator.getOrientation() == 1 && (locator.getLocalLocator() > rows || locator.getTranslationLocator() > rows)) {
                    return false;
                }

                return true;
            }
        }

        return false;
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

    public static List<String> getTranslationKeys(List<List<String>> excelContent, String keyLocator) {
        int num = Integer.parseInt(keyLocator.substring(1));
        List<String> keys = new ArrayList<>();
        if(keyLocator.startsWith("c")) {
            int size = excelContent.size();
            for (int i=0; i<size; ++i) {
                keys.add(excelContent.get(i).get(num));
            }
        } else {
            for (String key : excelContent.get(num)) {
                keys.add(key);
            }
        }
        return keys;
    }

    public static List<String> getTranslationLocals(List<List<String>> excelContent, TranslationLocator locator) {
        List<String> locals = new ArrayList<>();
        if(locator.getOrientation() == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
            for (String key : excelContent.get(locator.getLocalLocator())) {
                locals.add(key);
            }
        } else {
            int size = excelContent.size();
            for (int i=0; i<size; ++i) {
                locals.add(excelContent.get(i).get(locator.getLocalLocator()));
            }
        }
        return locals;
    }

    public static List<Translation> getTranslationList(List<String> texts, TranslationLocator translationLocator, HashMap<String, String> localMap, String key) {
        String local = texts.get(translationLocator.getLocalLocator());
        String translateText = texts.get(translationLocator.getTranslationLocator());
        if(translateText.isEmpty()
                || !Pattern.compile(GlobalConstant.REGEX_KEY).matcher(key).matches()
                || !Pattern.compile(GlobalConstant.REGEX_LOCAL).matcher(local).matches()) {
            return null;
        }

        return getTranslationList(key, local, translateText, localMap);
    }

    public static List<Translation> getTranslationList(List<List<String>> excelContent, HashMap<String, String> localMap, String key, String local, int row, int col) {
        String translateText = excelContent.get(row).get(col);
        if(translateText.isEmpty()
                || !Pattern.compile(GlobalConstant.REGEX_KEY).matcher(key).matches()
                || !Pattern.compile(GlobalConstant.REGEX_LOCAL).matcher(local).matches()) {
            return null;
        }

        return getTranslationList(key, local, translateText, localMap);
    }

    private static List<Translation> getTranslationList(String key, String local, String translateText, HashMap<String, String> localMap) {
        List<Translation> translations = new ArrayList<>();
        String localStr = localMap.get(local);
        if(localStr == null) {
            localStr = local;
        }
        String[] localArr = localStr.split(",");
        for (String l : localArr) {
            Translation translation = new Translation();
            translation.setKey(key);
            translation.setLocal(l.trim());
            translation.setTranslation(translateText);
            translations.add(translation);
        }

        return translations;
    }
}
