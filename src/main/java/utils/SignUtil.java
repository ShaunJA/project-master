package utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * 用来校验签名的工具
 * Created by ShaunAJ on 2017/7/11.
 */
public class SignUtil {
    public static final int SIGN_TIME_OUT = 15;

    /**
     * 对比参数签名是否正确
     *
     * @param params 输入的参数
     * @return 签名是否正确
     */
    public static boolean verify(Map<String, String> params) {
        if (null == params) {
            return false;
        }
        String sign = DigestUtils.md5Hex(createLinkString(paraFilter(params)).concat(EncodedUtils.DES_KEY_STR));
        String time_stamp = params.get("time_stamp");
        if (!"MD5".equals(params.get("sign_type"))) {
            // 签名加密方法不支持
            return false;
        } else if (StringUtils.isEmpty(time_stamp) || (!StringUtils.isEmpty(time_stamp) && !time_stamp.matches("^[0-9]*$"))) {
            // 授权超时
            return false;
        } else if (((Long.valueOf(time_stamp) / 1000 + SIGN_TIME_OUT) < System.currentTimeMillis() / 1000)
                || (System.currentTimeMillis() / 1000) - (Long.valueOf(time_stamp) / 1000) > SIGN_TIME_OUT) {
            // 授权超时,默认15秒
            return false;
        }
        if (sign.equals(params.get("sign"))) {
            return true;
        }

        return false;
    }

    /**
     * 把byte数组转换为16进制字符串
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 16进制字符串转换为byte数组
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 除去数组中的空值和签名参数
     *
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String preStr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                preStr = preStr + key + "=" + value;
            } else {
                preStr = preStr + key + "=" + value + "&";
            }
        }
        return preStr;
    }

    public static String getSign(Map<String, String> params, String DES_KEY_STR) {
        return DigestUtils.md5Hex(createLinkString(paraFilter(params)).concat(DES_KEY_STR));
    }



    public static void main(String[] args) {
        Map<String, String> maps = new HashMap<>();
        maps.put("liveId", "1433");
        maps.put("token", "b97029a4fa6b3e770ea0816c3dcf65fc");
        maps.put("gId", "1");
        maps.put("giftCount", "3");
        maps.put("giftName", "666");
        maps.put("endTime", "32");
        maps.put("sign_type", "MD5");
        maps.put("startTime", "12");
        maps.put("time_stamp", String.valueOf(System.currentTimeMillis()));
        maps.put("sign", getSign(maps, EncodedUtils.DES_KEY_STR));


        if (!SignUtil.verify(maps)) {
            System.out.println("签名错误");
        }else {
            System.out.println("签名正确");
        }

        String str = EncodedUtils.DESEncrypt(maps);//加密
        System.out.println("str:" + str);
        System.out.println(EncodedUtils.DESDecrypt(str, Map.class));//解密
    }
}
