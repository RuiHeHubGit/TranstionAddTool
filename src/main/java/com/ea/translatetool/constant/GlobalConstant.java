package com.ea.translatetool.constant;

public interface GlobalConstant {
    String[] NEED_UI_START_PRO = {"^(?!cmd)[\\w\\d]+\\.exe"};
    String[] NEED_HIDE_PRO = {"^(?!idea|eclipse)[\\w\\d]+\\.exe"};
    String CONFIG_FILE_PATH = "config/toolConfig.properties";
    String DEF_LOCAL_MAP_FILE_NAME = "localMap.properties";

    String REGEX_KEY = "[a-z_.\\d]{10,60}";
    String REGEX_LOCAL = "([a-zA-Z]{2,8}_[a-zA-Z]{2,8})";

    interface AppConfigDefaultValue {
        String LOCAL_MAP_FILE_PATH = "config/localMap.properties";
        String EXIST_KEY_SAVE_DIR = "existKey/";
        String LOG_SAVE_DIR = "logs/";
        String LOG_LEVEL = "INFO";
        String[] IN_PATH = {"excelFiles/"};
        String OUT_PATH = "translateFiles/";
        String FILE_PREFIX = "";
        String FILE_SUFFIX = "";
        boolean IS_COVER_KEY = true;
    }

    enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    enum OutType {
        TYPE_JSON(".json"),
        TYPE_PRO(".properties");

        private String value;
        OutType(String typeName) {
            this.value = typeName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
