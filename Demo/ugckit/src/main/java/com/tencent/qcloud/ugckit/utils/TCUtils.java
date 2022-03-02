package com.tencent.qcloud.ugckit.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.tencent.qcloud.ugckit.component.circlebmp.TCGlideCircleTransform;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TCUtils {

    public static String md5(@NonNull String string) {

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
            if (i < 0x10) hex.append('0');
            hex.append(Integer.toHexString(i));
        }

        return hex.toString();
    }

    // 字符串截断
    @Nullable
    public static String getLimitString(@Nullable String source, int length) {
        if (null != source && source.length() > length) {
            return source.substring(0, length) + "...";
        }
        return source;
    }

    /**
     * 圆角显示图片
     *
     * @param context  一般为activtiy
     * @param view     图片显示类
     * @param url      图片url
     * @param defResId 默认图 id
     */
    public static void showPicWithUrl(@Nullable Context context, @Nullable ImageView view, String url, int defResId) {
        if (context == null || view == null) {
            return;
        }
        try {
            if (TextUtils.isEmpty(url)) {
                view.setImageResource(defResId);
            } else {
                RequestManager req = Glide.with(context);
                req.load(url).placeholder(defResId).transform(new TCGlideCircleTransform(context)).into(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
