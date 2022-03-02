package com.tencent.qcloud.ugckit.utils;

/**
 * Version 1.0
 * <p>
 * Date: 2013-10-29 17:43
 * Author: yonnielu
 * <p>
 * Copyright © 1998-2013 Tencent Technology (Shenzhen) Company Ltd.
 */

/**
 * Http状态异常
 */
public class HttpStatusException extends Exception {
    public HttpStatusException(String detailMessage) {
        super(detailMessage);
    }
}
