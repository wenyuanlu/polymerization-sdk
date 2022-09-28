package com.corpize.sdk.mobads.utils;


import android.text.TextUtils;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * author: yh
 * date: 2019-12-13 11:06
 * description: AES加密
 */
public class AESUtil {

    private static final String CipherMode = "AES/CBC/PKCS5Padding";    // 算法/模式/填充
    private static final String AesKey     = "j5&9pX@~)~!R(^3L";        // 秘钥

    /**
     * 数据加密
     */
    public static String encrypt (String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }

        try {
            Cipher cipher = Cipher.getInstance(CipherMode);
            // 获取秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(AesKey.getBytes("utf-8"), "AES");
            // 获取偏移量
            IvParameterSpec iv = createIV(AesKey);
            // IvParameterSpec iv = new IvParameterSpec(AesKey.getBytes("UTF-8"));
            // 使用加密模式初始化 密钥
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            // 按单部分操作加密数据
            byte[] encrypt = cipher.doFinal(content.getBytes("UTF-8"));

            return parseByte2HexStr(encrypt);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 数据加密,动态秘钥加密
     */
    public static String encrypt (String content, String token) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(token)) {
            return "";
        }

        try {
            Cipher cipher = Cipher.getInstance(CipherMode);
            // 获取秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(token.getBytes("utf-8"), "AES");
            // 获取偏移量
            IvParameterSpec iv = createIV(token);
            //IvParameterSpec iv = new IvParameterSpec(token.getBytes("UTF-8"));
            // 使用加密模式初始化 密钥
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            // 按单部分操作加密数据
            byte[] encrypt = cipher.doFinal(content.getBytes("UTF-8"));

            return parseByte2HexStr(encrypt);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 数据加密,动态秘钥加密
     */
    public static String encrypt (String content, String token, String mode) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(token)) {
            return "";
        }

        try {
            Cipher cipher = Cipher.getInstance(mode);
            // 获取秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(token.getBytes("utf-8"), "AES");
            // 获取偏移量
            //IvParameterSpec iv = createIV(token);
            //IvParameterSpec iv = new IvParameterSpec(token.getBytes("UTF-8"));
            // 使用加密模式初始化 密钥
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            // 按单部分操作加密数据
            byte[] encrypt = cipher.doFinal(content.getBytes("UTF-8"));

            return parseByte2HexStr(encrypt);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 数据解密
     */
    public static String decrypt (String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }

        try {
            Cipher cipher = Cipher.getInstance(CipherMode);
            // 获取秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(AesKey.getBytes("utf-8"), "AES");
            // 获取偏移量
            IvParameterSpec iv = createIV(AesKey);
            //IvParameterSpec iv = new IvParameterSpec(AesKey.getBytes("UTF-8"));
            // 使用解密模式初始化 密钥
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            // 按单部分操作解密数据
            byte[] decrypt = cipher.doFinal(parseHexStr2Byte(data));

            return new String(decrypt, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 数据解密 动态秘钥解密
     */
    public static String decrypt (String data, String token) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(token)) {
            return "";
        }

        try {
            //token 0 - 16 为解密密钥,16 - 32 为偏移量
            String key    = token.substring(0, 16);
            String pian   = token.substring(16, 32);
            Cipher cipher = Cipher.getInstance(CipherMode);
            // 获取秘钥
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("utf-8"), "AES");
            // 获取偏移量
            IvParameterSpec iv = createIV(pian);
            //IvParameterSpec iv = new IvParameterSpec(token.getBytes("UTF-8"));
            // 使用解密模式初始化 密钥
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            // 按单部分操作解密数据
            byte[] decrypt = cipher.doFinal(Base64.decode(data));

            return new String(decrypt, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 将二进制转换成16进制
     */
    public static String parseByte2HexStr (byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     */
    public static byte[] parseHexStr2Byte (String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low  = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 将Base64转换为二进制
     */
    public static byte[] parse642Byte (String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low  = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 创建偏移量
     */
    private static IvParameterSpec createIV (String token) {
        byte[] data = null;
        if (token == null) {
            token = "";
        }
        StringBuffer sb = new StringBuffer(16);
        sb.append(token);
        String s = null;
        while (sb.length() < 16) {
            sb.append(" ");// 偏移量长度不够16补足到16
        }
        s = sb.substring(0, 16);// 截取16位偏移量
        try {
            data = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new IvParameterSpec(data);
    }

}

