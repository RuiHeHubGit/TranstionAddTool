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
        Integer orientation = GlobalConstant.Orientation.VERTICAL.ordinal();

        float maxKeySameLv = 0.5f, maxLocalSameLv = 0.5f, maxTranslationSameLv = 0.5f;
        float keySameLv, localSameLv, translationSameLv;
        for (int i=0; i<rows; ++i) {
            keySameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_KEY);
            localSameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_LOCAL);
            translationSameLv = calcListNotSimilarLevel(excelContent.get(i));

            if(keySameLv > localSameLv && keySameLv > translationSameLv) {
                if(keySameLv >= maxKeySameLv) {
                    maxKeySameLv = keySameLv;
                    keyLocator = "h"+i;
                }
            } else if(localSameLv > keySameLv && localSameLv > translationSameLv) {
                if(localSameLv >= maxLocalSameLv) {
                    maxLocalSameLv = localSameLv;
                    localLocator = i;
                }
            } else if(translationSameLv > keySameLv && translationSameLv > localSameLv) {
                if(translationSameLv > maxTranslationSameLv) {
                    maxKeySameLv = translationSameLv;
                    translationLocator = i;
                }
            }
        }


        String keyLocatorH = null;
        Integer localLocatorH = null;
        Integer translationLocatorH = null;
        float maxKeySameLvH = 0.5f, maxLocalSameLvH = 0.5f, maxTranslationSameLvH = 0.5f;
        float keySameLvH, localSameLvH, translationSameLvH;
        if(maxKeySameLv < 0.99 && maxLocalSameLv < 0.99 && maxTranslationSameLv < 0.99) {
            List<String> columnContents = new ArrayList<>();
            for (int i=0; i<columns; ++i) {
                columnContents.clear();
                for (int j = 0; j < rows; ++j) {
                    columnContents.add(excelContent.get(j).get(i));
                }

                keySameLvH = calcSimilarLevel(columnContents, GlobalConstant.REGEX_KEY);
                localSameLvH = calcSimilarLevel(columnContents, GlobalConstant.REGEX_LOCAL);
                translationSameLvH = calcListNotSimilarLevel(columnContents);

                if(keySameLvH > localSameLvH && keySameLvH > translationSameLvH) {
                    if(keySameLvH >= maxKeySameLvH) {
                        maxKeySameLvH = keySameLvH;
                        keyLocatorH = "r"+i;
                    }
                } else if(localSameLvH > keySameLvH && localSameLvH > translationSameLvH) {
                    if(localSameLvH >= maxLocalSameLvH) {
                        maxLocalSameLvH = localSameLvH;
                        localLocatorH = i;
                    }
                } else if(translationSameLvH > keySameLvH && translationSameLvH > localSameLvH) {
                    if(translationSameLvH > maxTranslationSameLvH) {
                        maxKeySameLvH = translationSameLvH;
                        translationLocatorH = i;
                    }
                }
            }
        }

        if(maxKeySameLvH > maxKeySameLv) {
            keyLocator = keyLocatorH;
        }

        if(maxLocalSameLvH > 0.5 && maxTranslationSameLvH > 0.5) {
            if(maxLocalSameLvH + maxTranslationSameLvH > maxLocalSameLvH + maxTranslationSameLvH) {
                orientation = GlobalConstant.Orientation.HORIZONTAL.ordinal();
                localLocator = localLocatorH;
                translationLocator = translationLocatorH;
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

    public static String getSingleKey(List<List<String>> excelContent) {
        HashMap<String, Integer> sameMap = new HashMap<>();
        Integer maxCount = 0;
        String mostSameKey = null;
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
                            mostSameKey = value;
                        }
                    }
                }
            }
        }
        return mostSameKey;
    }

    public static List<String> getTranslationKeys(List<List<String>> excelContent, String keyLocator, String singleKey) {
        int size;
        int num = Integer.parseInt(keyLocator.substring(1));
        List<String> keys = new ArrayList<>();

        Pattern pattern = Pattern.compile(GlobalConstant.REGEX_KEY);
        if(keyLocator.startsWith("c")) {
            size = excelContent.size();
            for (int i=0; i<size; ++i) {
                String key = excelContent.get(num).get(i);
                if(pattern.matcher(key).matches())
                    keys.add(key);
                else
                    keys.add(singleKey);
            }
        } else {
            for (String key : excelContent.get(num)) {
                if(pattern.matcher(key).matches())
                    keys.add(key);
                else
                    keys.add(singleKey);
            }
        }
        return keys;
    }

    public static Translation createTranslation(List<String> texts, TranslationLocator translationLocator, HashMap<String, String> localMap, String key) {
        String local = texts.get(translationLocator.getLocalLocator());
        String translateText = texts.get(translationLocator.getTranslationLocator());
        if(!translateText.isEmpty()) {
            return null;
        }
        if(!Pattern.compile(GlobalConstant.REGEX_LOCAL).matcher(key).matches()) {
            return null;
        }
        if(key.indexOf('.') <= 0 || local.indexOf('_') <= 0 || "[N/A]".equals(translateText)) {
            return null;
        }

        if(localMap.containsKey(local)) {
            local = localMap.get(local);
        }
        Translation translation = new Translation();
        translation.setKey(key);
        translation.setLocal(local);
        translation.setTranslation(translateText);

        return translation;
    }
}
