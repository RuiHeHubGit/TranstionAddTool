package com.ea.translatetool.addit.exception;

import com.ea.translatetool.addit.mode.Translation;

import java.util.concurrent.ConcurrentLinkedQueue;

public class NoFoundLocaleException extends Exception{
    private ConcurrentLinkedQueue<Translation> translations;
    public NoFoundLocaleException(ConcurrentLinkedQueue<Translation> translations, String msg) {
        super(msg);
        this.translations = translations;
    }

    public ConcurrentLinkedQueue<Translation> getTranslations() {
        return translations;
    }
}
