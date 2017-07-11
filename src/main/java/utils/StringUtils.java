package utils;

/**
 * Created by ShaunAJ on 2017/7/11.
 */
public class StringUtils {
    /**
     * 字符串是否为空
     * @param str 需要验证的字符串
     * @return 布尔值
     */
    public static boolean isEmpty(String str) {
        return null==str || "".equals(str);
    }
}
