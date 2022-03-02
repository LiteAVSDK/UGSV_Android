package com.tencent.qcloud.ugckit.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;

/**
 * 时间工具类
 */
public class DateTimeUtil {
    /**
     * 时间格式化
     */
    public static String formattedTime(long second) {
        String hs, ms, ss, formatTime;

        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h < 10) {
            hs = "0" + h;
        } else {
            hs = "" + h;
        }

        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }

        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        formatTime = hs + ":" + ms + ":" + ss;
        return formatTime;
    }

    /**
     * 毫秒转换为 分：秒 ---  00：00形式
     *
     * @param millsecond
     * @return
     */
    public static String millsecondToMinuteSecond(int millsecond) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        String mmss = formatter.format(millsecond);
        return mmss;
    }

    public static String duration(long durationMs) {
        long duration = durationMs / 1000;
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);

        String durationValue;

        if (h == 0) {
            durationValue = asTwoDigit(m) + ":" + asTwoDigit(s);
        } else {
            durationValue = asTwoDigit(h) + ":" + asTwoDigit(m) + ":" + asTwoDigit(s);
        }
        return durationValue;
    }

    @NonNull
    public static String asTwoDigit(long digit) {
        String value = "";

        if (digit < 10) {
            value = "0";
        }

        value += String.valueOf(digit);
        return value;
    }
}
