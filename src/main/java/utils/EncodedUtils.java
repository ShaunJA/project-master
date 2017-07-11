package utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/**
 * Created by ShaunAJ on 2017/7/11.
 */
public class EncodedUtils {
    public static final String DES_KEY_STR = "missapp20161207";

    private static Cipher getDESCipher(int mode) throws Throwable {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(DES_KEY_STR.getBytes());
        // 创建一个密匙工厂，然后用它把DESKeySpec转换成
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES");
        // 用密匙初始化Cipher对象
        cipher.init(mode, securekey, random);
        return cipher;
    }

    /**
     * DES加密
     *
     * @param maps MAP参数
     * @return
     */
    public static String DESEncrypt(Map<?, ?> maps) {
        try {
            Cipher cipher = EncodedUtils.getDESCipher(Cipher.ENCRYPT_MODE);
            // 正式执行加密操作
            return SignUtil.bytesToHexString(cipher.doFinal(JsonUtils.toJSONString(maps).getBytes("UTF-8")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密DES字符串
     *
     * @param str 加密的DES字符串
     * @return
     */
    public static <T> T DESDecrypt(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        try {
            // Cipher对象实际完成加密操作
            Cipher cipher = EncodedUtils.getDESCipher(Cipher.DECRYPT_MODE);
            // 正式执行加密操作
            String result = new String(cipher.doFinal(SignUtil.hexStringToBytes(str)), "UTF-8");
            return JsonUtils.toObject(result, clazz);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * base64解码
     *
     * @param str 待解码字符串
     * @return base64解码后的字符串
     */
    public static String BASE64Decode(String str) {
        if (!StringUtils.isEmpty(str)) {
            try {
                return new String(Base64.getDecoder().decode(str), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * base64编码
     *
     * @param str 待编码字符串
     * @return base64编码后的字符串
     */
    public static String BASE64Encode(String str) {
        if (!StringUtils.isEmpty(str)) {
            try {
                return new String(Base64.getEncoder().encode(str.getBytes()), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}