package com.ea.translatetool.util;

import javax.xml.bind.annotation.XmlType;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HeRui on 2018/12/24.
 */
public class IOUtil {
    private static final String DEF_ENCODE = "utf-8";

    public interface ScanCallBack {
        void onNewDir(File dir);
    }

    public static List<File> fileList(File file, boolean deep, DirectoryStream.Filter<File> filter, ScanCallBack callBack) {
        List<File> fileList = new ArrayList<>();
        scanPath(file, fileList, deep, filter, callBack);
        return fileList;
    }

    private static void scanPath(File file, List<File> fileList, boolean deep, DirectoryStream.Filter<File> filter, ScanCallBack callBack) {
        if(file.exists()) {
            if(file.isFile()) {
                boolean accept = false;
                try {
                    accept = filter.accept(file);
                } catch (IOException e) {
                    LoggerUtil.error(e.getMessage());
                }
                if(filter != null && !accept) return;
                fileList.add(file);
            } else {
                if(callBack != null) {
                    callBack.onNewDir(file);
                }
                File[] files = file.listFiles();
                for (File f:files) {
                    if(!deep && f.isDirectory()) {
                        continue;
                    }
                    scanPath(f, fileList, deep, filter, callBack);
                }
            }
        }

    }

    public static List<String> readText(File localFile, String encoding) throws IOException {
        if(encoding == null) {
            encoding = DEF_ENCODE;
        }
        List<String> lines = null;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(localFile), encoding));
            lines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return lines;
    }

    public static void saveLinesToFile(List<String> lines, File localFile, String encoding) throws IOException {
        if(encoding == null) {
            encoding = DEF_ENCODE;
        }
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localFile), encoding));
            for (int i=0; i<lines.size(); ++i) {
                bufferedWriter.write(lines.get(i));
                if(i + 1 < lines.size()) {
                    bufferedWriter.write("\n");
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }
}
