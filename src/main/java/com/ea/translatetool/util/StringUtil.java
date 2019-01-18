package com.ea.translatetool.util;

public class StringUtil {

    /**
     * Populate a string of the specified length with the specified string
     * @param s
     * @param length
     * @return
     */
    public static String createStringFromString(String s, int length) {
        if(length <= 0 || s == null ||  s.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(s);
        while (stringBuilder.length() < length) {
            stringBuilder.append(s);
        }
        if(stringBuilder.length() == length) {
            return stringBuilder.toString();
        }
        return stringBuilder.substring(0, length);
    }
}
