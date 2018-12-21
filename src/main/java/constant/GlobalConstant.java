package constant;

public interface GlobalConstant {
    interface PATH {
        String[] INPUT = {"excelFiles/"};
        String OUTPUT = "translateFiles/";
    }

    interface PARAM_KEY {
        String INPUT = "input";
        String OUTPUT = "output";
        String OUT_TYPE = "out-type";
        String VERTICAL = "vertical";
        String KEY_COLUMN = "key-column";
        String LOCAL_COLUMN = "local-column";
        String TRANSLATE_COLUMN = "translate-column";
    }

    enum OUT_TYPE {
        TYPE_JSON("json"),
        TYPE_XML("xml");

        private String value;
        OUT_TYPE(String typeName) {
            this.value = typeName;
        }
    }
}
