package com.ea.translatetool.util;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HeRui on 2018/12/24.
 */
public class IOUtil {
    public static List<File> fileList(File file, boolean deep, DirectoryStream.Filter<File> filter) throws IOException {
        List<File> fileList = new ArrayList<>();
        scanPath(file, fileList, deep, filter);
        return fileList;
    }

    private static void scanPath(File file, List<File> fileList, boolean deep, DirectoryStream.Filter<File> filter) throws IOException {
        if(file.exists()) {
            if (deep && file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    scanPath(f, fileList, deep, filter);
                }
            } else if(file.isFile()) {
                if(filter != null && !filter.accept(file)) return;
                fileList.add(file);
            }
        }

    }

    public static List<String> readText(File localFile) throws IOException {
        List<String> lines = null;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(localFile));
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

    public static void saveLinesToFile(List<String> lines, File localFile) throws IOException {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(localFile));
            for (String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
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
