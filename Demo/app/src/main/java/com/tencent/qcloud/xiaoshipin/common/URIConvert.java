package com.tencent.qcloud.xiaoshipin.common;

import android.net.Uri;
import android.text.TextUtils;

import com.blankj.utilcode.util.UriUtils;

import java.io.File;

/**
 * 用于将URI转换为文件路径
 * @author kevinxlhua
 */

public class URIConvert {

    public static String getFilePathByUri(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        File file = UriUtils.uri2File(Uri.parse(uri));
        if (file != null) {
            return file.getAbsolutePath();
        }
        return uri;
    }

}
