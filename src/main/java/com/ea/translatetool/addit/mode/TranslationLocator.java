package com.ea.translatetool.addit.mode;

/**
 * Created by HeRui on 2018/12/24.
 */
public class TranslationLocator {
    private String keyLocator;
    private Integer localLocator;
    private Integer translationLocator;
    private Integer orientation;


    public String getKeyLocator() {
        return keyLocator;
    }

    public void setKeyLocator(String keyLocator) {
        this.keyLocator = keyLocator;
    }

    public Integer getLocalLocator() {
        return localLocator;
    }

    public void setLocalLocator(Integer localLocator) {
        this.localLocator = localLocator;
    }

    public Integer getTranslationLocator() {
        return translationLocator;
    }

    public void setTranslationLocator(Integer translationLocator) {
        this.translationLocator = translationLocator;
    }

    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer orientation) {
        this.orientation = orientation;
    }
}
