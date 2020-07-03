package com.tencent.qcloud.xiaoshipin.userinfo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserInfoUtil {

    /**
     * @param password 用户输入密码
     * @return 有效则返回true, 无效则返回false
     */
    public static boolean isPasswordValid(String password) {
        return password.matches("[a-zA-Z0-9]{8,16}$");
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);

        for (byte b : hash) {
            int i = (b & 0xFF);
            if (i < 0x10) {
                hex.append('0');
            }
            hex.append(Integer.toHexString(i));
        }

        return hex.toString();
    }

    /**
     * @param username 用户名
     * @return 同上
     */
    public static boolean isUsernameVaild(String username) {
        return username.matches("^[a-zA-Z][a-zA-Z0-9_]{3,23}$");
    }

    // 字符串截断
    public static String getLimitString(String source, int length) {
        if (null != source && source.length() > length) {
            return source.substring(0, length) + "...";
        }
        return source;
    }

}
