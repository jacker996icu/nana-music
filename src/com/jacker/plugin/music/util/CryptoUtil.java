package com.jacker.plugin.music.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.jacker.plugin.music.dto.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;

/**
 * 网易云音乐参数加密算法
 */
public class CryptoUtil {
    // 密钥
    private static String nonce = "0CoJUm6Qyw8W8jud";

    // 偏移量
    private static String ivParameter = "0102030405060708";

    // 公共密钥
    private static String pubKey = "010001";

    // 基本指数
    private static String modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";

    public static final String eapiKey = "e82ckenh8dichen8";
    /**
     * 随机字符串
     *
     * @param length 长度：取16
     * @return
     */
    public static String createSecretKey(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 字符串转换为16进制字符串
     *
     * @param s 字符串
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * RSA 加密采用非常规填充方式，既不是PKCS1也不是PKCS1_OAEP，网易的做法是直接向前补0
     *
     * @param str
     * @param size
     * @return
     */
    public static String zfill(String str, int size) {
        while (str.length() < size) str = "0" + str;
        return str;
    }

    /**
     * AES加密
     * 此处使用AES-128-CBC加密模式，key需要为16位
     *
     * @param content 加密内容
     * @param sKey    偏移量
     * @return
     */
    public static String aesEncrypt(String content, String sKey) {
        try {
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            // 获取cipher对象，getInstance("算法/工作模式/填充模式")
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // 采用AES方式将密码转化成密钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(sKey.getBytes(), "AES");
            // 初始化偏移量
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            // cipher对象初始化 init（“加密/解密,密钥，偏移量”）
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            // 数据处理
            byte[] encryptedBytes = cipher.doFinal(byteContent);
            // 此处使用BASE64做转码功能，同时能起到2次加密的作用
            return new Base64().encodeToString(encryptedBytes);
        } catch (Exception e) {

        }
        return "";
    }

    public static String encrypt(String content, String aesKey) {
        try {
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(byteContent);
            return bytesToHexString(encrypted).toUpperCase();
        } catch (Exception e) {

        }
        return "";
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return "";
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * RSA 加密
     *
     * @param secKey 随机16位字符串
     * @return
     */
    public static String rsaEncrypt(String secKey) {
        // 需要先将加密的消息翻转，再进行加密
        secKey = new StringBuffer(secKey).reverse().toString();
        // 转十六进制字符串
        String secKeyHex = stringToHexString(secKey);
        // 指定基数的数值字符串转换为BigInteger表示形式
        BigInteger biText = new BigInteger(secKeyHex, 16);
        BigInteger biEx = new BigInteger(pubKey, 16);
        BigInteger biMod = new BigInteger(modulus, 16);
        // 次方并求余（biText^biEx mod biMod is ?）
        BigInteger bigInteger = biText.modPow(biEx, biMod);
        return zfill(bigInteger.toString(16), 256);
    }

    public static HashMap<String, String> getDataWapi(Map<String, String> param) {
        String content = JackSonUtil.convertJson(param);
        HashMap<String, String> data = new HashMap<>();
        String secKey = createSecretKey(16);
        // 二次AES加密、加密模式都是CBC加密
        // 第一次加密使用content和nonce进行加密
        // 第二次使用第一次加密结果和16位随机字符串进行加密
        String params = aesEncrypt((aesEncrypt(content, nonce)), secKey);
        // RSA 加密
        String encSecKey = rsaEncrypt(secKey);
        data.put("params", params);
        data.put("encSecKey", encSecKey);
        return data;
    }

    public static HashMap<String, String> getDataEapi(String url, Map<String, Object> param) {
        String content = JackSonUtil.convertJson(param);
        String message = "nobody" + url + "use" + content + "md5forencrypt";
        String digest = md5(message);
        String da = url + "-36cd479b6b5-" + content + "-36cd479b6b5-" + digest;
        HashMap<String, String> data = new HashMap<>();
        String params = encrypt(da, eapiKey);
        data.put("params", params);
        return data;
    }

    public static String md5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }






}
