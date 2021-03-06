package com.ea.translatetool.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.regex.Pattern;

public final class PID {
    enum ProcessAttr {
        PROCESS_ID("ProcessId"),
        PARENT_PROCESS_ID("ParentProcessId"),
        NAME("Name"),
        OS_NAME("OSName"),
        STATUS("Status"),
        THREAD_COUNT("ThreadCount"),
        WINDOWS_VERSION("WindowsVersion"),
        SESSION_ID("SessionId");

        final String value;
        ProcessAttr(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * 获取当前进程ID
     * @return
     */
    public static String getPID() {
        String pid = System.getProperty("pid");
        if (pid == null) {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String processName = runtimeMXBean.getName();
            if (processName.indexOf('@') != -1) {
                pid = processName.substring(0, processName.indexOf('@'));
            } else {
                pid = null;
            }
        }
        return pid;
    }

    /**
     * 获取进程信息
     * @return
     */
    public static String getProcessAttr(String pid, ProcessAttr attrKey) {
        String result = null;
        if(pid == null || attrKey == null) {
            return null;
        }

        BufferedReader br = null;
        try {
            String cmd ="wmic process where ProcessId="+pid+" get "+attrKey;
            br = new BufferedReader(
                    new InputStreamReader(
                            Runtime.getRuntime().exec(cmd).getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if(line.length() > 0) {
                    result = line;
                }
            }
        } catch (IOException e) {
            LoggerUtil.error(e.getMessage());
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LoggerUtil.error(e.getMessage());
                }
            }
        }
        return result;
    }


    /**
     * 判断应用是否是用window explorer打开的
     * @return
     */
    public static boolean isStartWithWindowExplorer() {
        return isStartWithContain("explorer.exe");
    }

    /**
     * 判断是否是以指定进程运行的
     * @param pidNameRegex
     * @return
     */
    public static boolean isStartWithContain(String ... pidNameRegex) {
        String pid = getPID();
        String parentProcessName = getProcessAttr(getProcessAttr(pid, ProcessAttr.PARENT_PROCESS_ID), ProcessAttr.NAME);
        if(parentProcessName == null || parentProcessName.isEmpty()) {
            return false;
        }
        for (String name : pidNameRegex) {
            Pattern pattern = Pattern.compile(name);
            if(pattern.matcher(parentProcessName).matches()) {
                return true;
            }
        }
        return false;
    }
}
