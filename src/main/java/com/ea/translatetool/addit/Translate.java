package com.ea.translatetool.addit;

/**
 * Created by HeRui on 2018/12/22.
 */
public class Translate {
    private String key;
    private String local;
    private String translate;

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

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    @Override
    public String toString() {
        return "Translate{\n" +
                "key='" + key + "\'\n" +
                ", local='" + local + "\'\n" +
                ", translate='" + translate + "\'\n" +
                '}';
    }
}
