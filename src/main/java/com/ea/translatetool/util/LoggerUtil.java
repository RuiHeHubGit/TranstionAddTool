package com.ea.translatetool.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by HeRui on 2018/12/23.
 */
public class LoggerUtil {
    public final static String LEVEL_FINE = "fine";
    public final static String LEVEL_INFO = "info";
    public final static String LEVEL_WARNING = "warning";
    public final static String LEVEL_ERROR = "error";
    private static final String DEF_LEVEL = LEVEL_INFO;
    private static String logPath = "logs";

    private final static Logger LOG = Logger.getLogger("tool");
    private final static HashMap<String, Integer> LEVELS = new HashMap<>();
    private static String logLevel = LEVEL_INFO;
    private volatile static boolean debug;
    private static HashMap<String, BufferedWriter> writerMap;

    static {
        LEVELS.put(LEVEL_FINE, 1);
        LEVELS.put(LEVEL_INFO, 2);
        LEVELS.put(LEVEL_WARNING, 3);
        LEVELS.put(LEVEL_ERROR, 4);

        LOG.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                String msg = "["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"_tid"+record.getThreadID()+"] "
                        +record.getMessage();
                writeLogToFile(msg, record.getLevel().getName());
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }

    public static void error(String msg) {
        if(canLog(LEVEL_ERROR)) {
            LOG.severe(msg);
            if(debug) {
                StackTraceElement[] stackElements = new Throwable().getStackTrace();
                if (stackElements != null) {
                    for (int i = 0; i < stackElements.length; i++) {
                        System.err.println("" + stackElements[i]);
                    }
                }
            }
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
            logLevel = DEF_LEVEL;
        }

        if(logLevel == null) return true;

        Integer lv = LEVELS.get(level);

        return lv >= LEVELS.get(logLevel);
    }

    public static String setLogLevel(String logLevel) {
        if(logLevel == null) {
            return LoggerUtil.logLevel;
        }
        logLevel = logLevel.toLowerCase();
        if(LEVELS.containsKey(logLevel)) {
            LoggerUtil.logLevel = logLevel;
        }
        return LoggerUtil.logLevel;
    }

    public static void setDebug(boolean debug) {
        LoggerUtil.debug = debug;
    }

    public static boolean getDebug() {
        return debug;
    }

    private synchronized static void writeLogToFile(String text, String logLevel) {
        if(writerMap == null) {
            writerMap = new HashMap<>();
            ShutdownHandler.addShutdownHandler(new ShutdownHandler() {
                @Override
                public void run() {
                    Collection<BufferedWriter> writers = writerMap.values();
                    for (BufferedWriter w : writers) {
                        System.out.println("save log file");
                        if(w != null) {
                            try {
                                w.close();
                            } catch (IOException e) {
                                error(e.getMessage());
                            }
                        }
                    }
                }
            });
        }

        BufferedWriter writer = writerMap.get(logLevel);
        if(writer == null && !writerMap.containsKey(logLevel)) {
            try {
                if(!logPath.endsWith("\\") && !logPath.endsWith("/")) {
                    logPath += File.separator;
                }
                String fileName = logLevel+"_"+new SimpleDateFormat("yyyy.MM.dd").format(new Date())+".log";
                File logFile =  new File(logPath+logLevel, fileName);
                if(!logFile.getParentFile().exists()) {
                    logFile.getParentFile().mkdirs();
                }
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "utf-8"));
                writerMap.put(logLevel, writer);
            } catch (FileNotFoundException e) {
                writerMap.put(logLevel, null);
                error(e.getMessage());
            } catch (IOException e) {
                writerMap.put(logLevel, null);
                error(e.getMessage());
            }
        } else {
            try {
                writer.write(text);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                writerMap.put(logLevel, null);
                error(e.getMessage());
                try {
                    writer.close();
                } catch (IOException e1) {
                    error(e1.getMessage());
                }
            }
        }
    }

    public static void setLogPath(String logSaveDir) {
        LoggerUtil.logPath = logSaveDir;
    }
}
