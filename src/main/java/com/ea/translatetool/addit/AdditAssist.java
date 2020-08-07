package com.ea.translatetool.addit;

import com.ea.translatetool.addit.mode.Translation;
import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.config.ConfigRepository;
import com.ea.translatetool.config.FileConfigRepositoryImpl;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.util.*;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class AdditAssist {
    public static void saveKeyExistTranslation(List<Translation> existList, File saveFile) throws IOException {
        Collections.sort(existList, new Comparator<Translation>() {
            @Override
            public int compare(Translation o1, Translation o2) {
                return o1.getLocal().compareTo(o2.getLocal());
            }
        });

        List<String> translateLines = new ArrayList<>();
        translateLines.add("[");
        String lastLocal = null;
        for (int i = 0; i < existList.size(); ++i) {
            Translation translation = existList.get(i);
            if (!translation.getLocal().equals(lastLocal)) {
                if (translateLines.size() > 1) {
                    translateLines.add("\t\t]");
                    translateLines.add("\t},");
                }
                translateLines.add("\t{");
                translateLines.add("\t\t\"local\": \"" + translation.getLocal() + "\",");
                translateLines.add("\t\t\"keys\": [");
                lastLocal = translation.getLocal();
            }

            String json = "\t\t\t{\"key\":\"" + translation.getKey() + "\",\"local\":\""
                    + translation.getLocal() + "\",\"translation\":\""
                    + translation.getTranslation().replace("\\", "\\\\").replace("\"", "\\") + "\"}";
            translateLines.add(json);
        }

        if (translateLines.size() > 1) {
            translateLines.add("\t\t]");
            translateLines.add("\t}");
        }
        translateLines.add("]");
        IOUtil.saveLinesToFile(translateLines, saveFile, null);
    }

    public static File createExistKeySaveFile(AppConfig appConfig) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        File file = new File(appConfig.getExistKeySaveDir(), "(pid" + PID.getPID() + ")_exists_keys_" + dateFormat.format(new Date()) + ".json");
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if (!file.exists())
            file.createNewFile();
        return file;
    }

    public static WorkConfig createWorkConfig(AppConfig config) {
        WorkConfig workConfig = new WorkConfig();
        List<File> files = new ArrayList<>();
        for (String path : config.getInPath()) {
            files.add(new File(path));
        }
        workConfig.setInput(files);
        workConfig.setOutput(new File(config.getOutPath()));
        workConfig.setOutType(GlobalConstant.OutType.TYPE_JSON);
        TreeMap<String, String> localMap;
        try {
            localMap = loadLocalMap(config.getLocalMapFilePath(), false);
        } catch (IOException e) {
            LoggerUtil.error(e.getMessage());
            localMap = new TreeMap<>();
        }
        workConfig.setLocalMap(localMap);
        workConfig.setFilePrefix(config.getFilePrefix());
        workConfig.setFileSuffix(config.getFileSuffix());
        List<File> inputPaths = new ArrayList<>();
        for (String path : config.getInPath()) {
            File file = new File(path);
            inputPaths.add(file);
        }
        workConfig.setInput(inputPaths);

        File outPath = new File(config.getOutPath());
        if (outPath.exists()) {
            workConfig.setOutput(outPath);
        }
        workConfig.setTranslationLocatorMap(new TreeMap<String, TranslationLocator>());
        workConfig.setTranslationList(new ArrayList<Translation>());
        workConfig.setExcelFiles(new ArrayList<File>());
        return workConfig;
    }


    public static TreeMap<String, String> loadLocalMap(String localMapFilePath, boolean setToDef) throws IOException {
        File file = new File(localMapFilePath);
        if (setToDef || !file.exists() || file.isDirectory()) {
            if (file.isDirectory()) {
                file = new File(file, GlobalConstant.DEF_LOCAL_MAP_FILE_NAME);
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = WindowTool.class.getClassLoader().getResourceAsStream(GlobalConstant.DEF_LOCAL_MAP_FILE_NAME);
                outputStream = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int size;
                while ((size = inputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, size);
                }
            } catch (IOException e) {
                LoggerUtil.error(e.getMessage());
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    LoggerUtil.error(e.getMessage());
                }
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ConfigRepository configRepository = FileConfigRepositoryImpl.getInstance();
        Properties properties = new Properties();
        properties.put(FileConfigRepositoryImpl.CONFIG_FILE_PATH_KEY, localMapFilePath);
        return configRepository.load(TreeMap.class, null, properties);
    }


    public static TranslationLocator calcTranslationLocator(List<List<String>> excelContent, TreeMap<String, String> localMap, TranslationLocator locator) {

        if (excelContent == null || excelContent.isEmpty()
                || excelContent.size() < 3 && excelContent.get(0).size() < 3) {
            return null;
        }

        int rows = excelContent.size();
        int columns = excelContent.get(0).size();

        if (isValidTranslationLocator(rows, columns, locator)) {
            return locator;
        }

        String keyLocator = null;
        Integer localLocator = null;
        Integer translationLocator = null;
        Integer translationOfEnLocator = null;
        Integer orientation = GlobalConstant.Orientation.HORIZONTAL.ordinal();

        float maxKeySameLv = 0.5f, maxLocalSameLv = 0.5f;
        float keySameLv, localSameLv;
        double cad, maxCad = 0;

        for (int i = 0; i < columns && i < 3; ++i) {
            keySameLv = calcSimilarLevel(getTableColumnTexts(excelContent, i), GlobalConstant.REGEX_KEY);
            if (keySameLv >= maxKeySameLv) {
                maxKeySameLv = keySameLv;
                keyLocator = "c" + i;
            }
        }
        if (maxKeySameLv < 1.0f) {
            for (int i = 0; i < rows && i < 3; ++i) {
                keySameLv = calcSimilarLevel(excelContent.get(i), GlobalConstant.REGEX_KEY);
                if (keySameLv >= maxKeySameLv) {
                    maxKeySameLv = keySameLv;
                    keyLocator = "r" + i;
                }
            }
        }
        if (keyLocator == null) {
            return null;
        }

        for (int i = 0; i < rows; ++i) {
            localSameLv = calcSimilarLocalLevel(excelContent.get(i), localMap);
            if (localSameLv >= maxLocalSameLv) {
                maxLocalSameLv = localSameLv;
                localLocator = i;
            }
        }
        if (maxLocalSameLv < 1.0f) {
            for (int i = 0; i < columns; ++i) {
                localSameLv = calcSimilarLocalLevel(getTableColumnTexts(excelContent, i), localMap);
                if (localSameLv >= maxLocalSameLv) {
                    maxLocalSameLv = localSameLv;
                    localLocator = i;
                    orientation = GlobalConstant.Orientation.VERTICAL.ordinal();
                }
            }
        }
        if (localLocator == null) {
            return null;
        }

        int keyLocatorNum = Integer.parseInt(keyLocator.substring(1));
        if (keyLocator.startsWith("c") && orientation == GlobalConstant.Orientation.VERTICAL.ordinal()) {
            for (int i = 0; i < columns; ++i) {
                if (i == keyLocatorNum || i == localLocator) {
                    continue;
                }
                cad = calcFirstCharAverageDeviation(getTableColumnTexts(excelContent, i));
                if (cad > maxCad) {
                    maxCad = cad;
                    translationLocator = i;
                }
            }
            if (translationLocator == null) {
                return null;
            }
        } else if (keyLocator.startsWith("r") && orientation == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
            for (int i = 0; i < rows; ++i) {
                if (i == keyLocatorNum || i == localLocator) {
                    continue;
                }
                cad = calcFirstCharAverageDeviation(excelContent.get(i));
                if (cad > maxCad) {
                    maxCad = cad;
                    translationLocator = i;
                }
            }
            if (translationLocator == null) {
                return null;
            }
        }

        long minLenDistance = Long.MAX_VALUE;
        if (keyLocator.startsWith("c") && orientation == GlobalConstant.Orientation.VERTICAL.ordinal()) {
            for (int i = 0; i < columns; ++i) {
                if (i == keyLocatorNum || i == localLocator || i == translationLocator) {
                    continue;
                }
                long len = calcLengthDistance(getTableColumnTexts(excelContent, i), getTableColumnTexts(excelContent, translationLocator));
                if (len < minLenDistance) {
                    minLenDistance = len;
                    translationOfEnLocator = i;
                }
            }
            if (translationLocator == null) {
                return null;
            }
        } else if (keyLocator.startsWith("r") && orientation == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
            for (int i = 0; i < rows; ++i) {
                if (i == keyLocatorNum || i == localLocator || i == translationLocator) {
                    continue;
                }
                long len = calcLengthDistance(excelContent.get(i), excelContent.get(translationLocator));
                if (len < minLenDistance) {
                    minLenDistance = len;
                    translationOfEnLocator = i;
                }
            }
            if (translationLocator == null) {
                return null;
            }
        }

        if (translationOfEnLocator == null) {
            return null;
        }

        if (locator == null) {
            locator = new TranslationLocator();
        }
        locator.setKeyLocator(keyLocator);
        locator.setLocalLocator(localLocator);
        locator.setTranslationOfEnLocator(translationOfEnLocator);
        locator.setTranslationLocator(translationLocator);
        locator.setOrientation(orientation);

        return locator;
    }

    private static float calcSimilarLocalLevel(List<String> strings, TreeMap<String, String> localMap) {
        float count = 0;
        for (String s : strings) {
            if (localMap.containsKey(s)) {
                ++count;
            }
        }
        return count / strings.size();
    }

    public static boolean isValidTranslationLocator(int rows, int columns, TranslationLocator locator) {

        if (locator != null && locator.getKeyLocator() != null && locator.getLocalLocator() != null && locator.getTranslationLocator() != null) {
            if (locator.getOrientation() != null && locator.getOrientation() >= 0 && locator.getOrientation() < 3 && rows >= 3) {

                if (locator.getKeyLocator().startsWith("r")) {
                    int keyRow = Integer.parseInt(locator.getKeyLocator().substring(1));
                    if (keyRow > rows) {
                        return false;
                    }
                } else {
                    int keyCol = Integer.parseInt(locator.getKeyLocator().substring(1));
                    if (keyCol > columns) {
                        return false;
                    }
                }

                if (locator.getOrientation() == 0 && (locator.getLocalLocator() > columns || locator.getTranslationLocator() > columns)) {
                    return false;
                }

                if (locator.getOrientation() == 1 && (locator.getLocalLocator() > rows || locator.getTranslationLocator() > rows)) {
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    public static double calcFirstCharAverageDeviation(List<String> strings) {
        TreeSet<Character> chars = new TreeSet<>();
        if (strings == null) return 0;
        for (String string : strings) {
            if (string.length() > 1) {
                chars.add(string.charAt(1));
                if (chars.size() > 30) break;
            }
        }
        double sum = 0;
        for (Character c : chars) {
            sum += c;
        }
        double avg = sum / chars.size();
        double d = 0;
        for (Character c : chars) {
            d += Math.pow(c - avg, 2);
        }
        return Math.sqrt(d / chars.size());
    }

    public static float calcSimilarLevel(List<String> strings, String regex) {
        int count = 0;
        if (strings == null || strings.isEmpty()) return 0;

        Pattern pattern = Pattern.compile(regex);
        for (String s : strings) {
            if (pattern.matcher(s).matches()) {
                ++count;
            }
        }
        return 1.0f * count / strings.size();
    }

    public static long calcLengthDistance(List<String> strings1, List<String> strings2) {
        int distance = 0;
        if (strings1 == null || strings1.isEmpty() || strings2 == null || strings2.isEmpty()) return 0;

        for (int i = 0; i < strings1.size() && i < strings2.size(); i++) {
            String str1 = strings1.get(i);
            String str2 = strings2.get(i);
            long d1 = str1 == null ? 0 : str1.length();
            long d2 = str2 == null ? 0 : str1.length();
            distance += Math.abs(d1 - d2);
        }
        return distance;
    }

    public static List<String> getTranslationKeys(List<List<String>> excelContent, String keyLocator) {
        int num = Integer.parseInt(keyLocator.substring(1));
        List<String> keys = new ArrayList<>();
        if (keyLocator.startsWith("c")) {
            int size = excelContent.size();
            for (int i = 0; i < size; ++i) {
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
        if (locator.getOrientation() == GlobalConstant.Orientation.HORIZONTAL.ordinal()) {
            for (String local : excelContent.get(locator.getLocalLocator())) {
                locals.add(local);
            }
        } else {
            int size = excelContent.size();
            for (int i = 0; i < size; ++i) {
                locals.add(excelContent.get(i).get(locator.getLocalLocator()));
            }
        }
        return locals;
    }

    public static List<Translation> getTranslationList(String key, String local, String translateTextOfEn, String translateText, TreeMap<String, String> localMap, File file) {
        List<Translation> translations = new ArrayList<>();
        if (translateText.isEmpty()
                || local.isEmpty()
                || key.isEmpty()) {
            return translations;
        }

        String localStr = localMap.get(local);
        if (localStr == null) {
            localStr = local;
        }
        String[] localArr = localStr.split(",");
        for (String l : localArr) {
            Translation translation = new Translation();
            translation.setLocaleKey(local);
            translation.setKey(key);
            translation.setLocal(l.trim());
            translation.setTranslationOfEn(translateTextOfEn);
            translation.setTranslation(translateText);
            translation.setFile(file);
            translations.add(translation);
        }

        return translations;
    }

    public static FileFilter createExcelFileFilter() {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || (f.isFile()
                        && !f.getName().startsWith("~$")
                        && (f.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLS)
                        || f.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLSX)));
            }

            @Override
            public String getDescription() {
                return "directory;*" + ExcelUtil.SUFFIX_XLS + ";*" + ExcelUtil.SUFFIX_XLSX;
            }
        };
        return fileFilter;
    }

    public static List<String> getTableColumnTexts(List<List<String>> table, int colIndex) {
        List<String> columnContents = new ArrayList<>();
        int rows = table.size();
        for (int j = 0; j < rows; ++j) {
            columnContents.add(table.get(j).get(colIndex));
        }
        return columnContents;
    }
}
