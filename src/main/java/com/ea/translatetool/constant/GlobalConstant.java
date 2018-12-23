package com.ea.translatetool.constant;

public interface GlobalConstant {
    String CONFIG_FILE_PATH = "toolConfig.properties";

    interface Path {
        String[] INPUT = {"excelFiles/"};
        String OUTPUT = "translateFiles/";
    }

    interface AppConfigDefaultValue {
        String EXIST_KEY_SAVE_DIR = "";
        String LOG_SAVE_DIR = "";
        String LOG_LEVEL = "INFO";
        String[] IN_PATH = {""};
        String OUT_PATH = "";
        String FILE_PREFIX = "";
        String FILE_SUFFIX = "";
        boolean IS_COVER_KEY = false;
    }

    enum OutType {
        TYPE_JSON("json"),
        TYPE_XML("xml");

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
