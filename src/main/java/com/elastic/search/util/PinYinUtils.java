package com.elastic.search.util;

import net.sourceforge.pinyin4j.PinyinHelper;

public class PinYinUtils {

    /**
     * 中文转简写拼音
     *      北京===>bj
     *      北京欢迎你===>bjhyn
     * @param words
     * @return
     */
    public static String chineseToSimplePY(String words){
        String convert = "";
        for (int j = 0; j < words.length(); j++) {
            char word = words.charAt(j);
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert += pinyinArray[0].charAt(0);
            } else {
                convert += word;
            }
        }
        return convert;
    }


    /**
     * 判断字符串是否为中文
     * @param str
     * @return
     */
    public static boolean isAllChinese(String str){
        for (int i = 0; i < str.length(); i++) {  //遍历所有字符
            char ch = str.charAt(i);
            if(ch < 0x4E00 ||ch > 0x9FA5){  //中文在unicode编码中所在的区间为0x4E00-0x9FA5
                return false;  //不在这个区间，说明不是中文字符，返回false
            }
        }

        return true;  //全部在中文区间，说明全部是中文字符，返回true
    }

    public static void main(String[] args) {
        System.out.println(isAllChinese("你好世界"));  //true
        System.out.println(isAllChinese("hello世界"));  //false
        System.out.println(isAllChinese("你好word"));  //false
        System.out.println(isAllChinese("你好 世界"));  //false
        System.out.println(chineseToSimplePY("张三"));
    }

}