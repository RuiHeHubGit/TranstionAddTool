package com.ea.translatetool.util;

public class StringUtil {

    /**
     * 用指定字符串填充一个指定长度的字符串
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

    /**
     * 判断两个字符串相似度,可设置level
     * @param strSource 原字符串
     * @param strCompared 比较字符串
     * @param level 评分阀值
     * @param moreCount 比较字符串比原字符串多多少个限制
     * @return
     */
    public static Boolean isSimilar(String strSource,String strCompared, int level,int moreCount){
        if(strCompared.length()-strSource.length()>moreCount){
            return false;
        }
        int count=strSource.length();
        int maxSameCount=0;
        //遍历count次
        for(int i=0;i<count;i++){
            int nowSameCount=0;
            int c=0;
            int lastIndex=0;//记录上一次匹配的目标索引
            //遍历每一次的原字符串所有字段
            for(int j=i;j<strSource.length();j++){
                char charSource=strSource.charAt(j);
                for(;c<strCompared.length();c++){
                    char charCompare=strCompared.charAt(c);
                    if(charSource==charCompare){
                        nowSameCount++;
                        lastIndex=++c;//如果匹配,手动加1
                        break;
                    }
                }
                c=lastIndex;//遍历完目标字符串,记录当前匹配索引
            }
            if(nowSameCount>maxSameCount){
                maxSameCount=nowSameCount;
            }
        }
        //大于原字符串数量的情况
        if(maxSameCount>count){
            maxSameCount=count-(maxSameCount-count);
        }
        double dLv= (double)100*maxSameCount/count;
        int iLv=10*maxSameCount/count*10;
        int cha=(int)dLv-iLv;
        int yu=cha>5?1:0;
        iLv+=yu*10;
        if(iLv/10>=level){
            return true;
        }else{
            return false;
        }
    }
}
