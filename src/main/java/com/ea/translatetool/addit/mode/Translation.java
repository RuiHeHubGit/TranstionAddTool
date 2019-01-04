package com.ea.translatetool.addit.mode;

/**
 * Created by HeRui on 2018/12/22.
 */
public class Translation {
    private String key;
    private String local;
    private String translation;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Override
    public String toString() {
        return "Translation{\n" +
                "key='" + key + "\'\n" +
                ", local='" + local + "\'\n" +
                ", translation='" + translation + "\'\n" +
                '}';
    }
}
