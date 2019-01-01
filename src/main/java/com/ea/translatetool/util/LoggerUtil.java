package com.ea.translatetool.util;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by HeRui on 2018/12/23.
 */
public class LoggerUtil {
    private final static Logger LOG = Logger.getLogger("tool");
    private final static HashMap<String, Integer> LEVELS = new HashMap<>();
    private static String logLevel = null;
    private static Integer LV = 0;

    static {
        LEVELS.put("fine", 1);
        LEVELS.put("info", 2);
        LEVELS.put("warning", 3);
        LEVELS.put("error", 4);
    }

    public static void error(String msg) {
        if(canLog("error")) {
            LOG.severe(msg);
        }
    }

    public static void warning(String msg) {
        if (canLog("warning")) {
            LOG.warning(msg);
        }
    }

    public static void info(String msg) {
        if(canLog("info")) {
            LOG.info(msg);
        }
    }

    public static void fine(String msg) {
        if(canLog("fine")) {
            LOG.fine(msg);
        }
    }

    private static boolean canLog(String level) {
        if(logLevel == null) {
            synchronized (LoggerUtil.class) {
                if(logLevel == null) {
                    logLevel = System.getProperty("logLevel");
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
