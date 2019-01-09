package com.ea.translatetool.util;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by HeRui on 2018/12/23.
 */
public class LoggerUtil {
    public final static String LOG_LEVEL = "logLevel";
    public final static String LEVEL_FINE = "fine";
    public final static String LEVEL_INFO = "info";
    public final static String LEVEL_WARNING = "warning";
    public final static String LEVEL_ERROR = "error";

    private final static Logger LOG = Logger.getLogger("tool");
    private final static HashMap<String, Integer> LEVELS = new HashMap<>();
    private static String logLevel = null;
    private static Integer LV = 0;

    static {
        LEVELS.put(LEVEL_FINE, 1);
        LEVELS.put(LEVEL_INFO, 2);
        LEVELS.put(LEVEL_WARNING, 3);
        LEVELS.put(LEVEL_ERROR, 4);
    }

    public static void error(String msg) {
        if(canLog(LEVEL_ERROR)) {
            LOG.severe(msg);
        }
    }

    public static void warning(String msg) {
        if (canLog(LEVEL_WARNING)) {
            LOG.warning(msg);
        }
    }

    public static void info(String msg) {
        if(canLog(LEVEL_INFO)) {
            LOG.info(msg);
        }
    }

    public static void fine(String msg) {
        if(canLog(LEVEL_FINE)) {
            LOG.fine(msg);
        }
    }

    public static String exceptionLog(Throwable t) {
        if(t == null) return null;
        String msg;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        t.printStackTrace(printStream);
        msg = outputStream.toString();
        error(msg);
        printStream.close();
        try {
            outputStream.close();
        } catch (IOException e) {
            error(e.getMessage());
        }
        return msg;
    }

    private static boolean canLog(String level) {
        if(logLevel == null) {
            synchronized (LoggerUtil.class) {
                if(logLevel == null) {
                    logLevel = System.getProperty(LOG_LEVEL);
                    if(logLevel != null) {
                        Integer lv = LEVELS.get(logLevel);
                        if(lv != null) LV = lv;
                    }
                }
            }
        }

        if(logLevel == null) return true;

        Integer lv = LEVELS.get(level);
        if(lv == null) lv = 0;

        return lv >= LEVELS.get(logLevel);
    }
}
