package com.tencent.qcloud.ugckit.module.upload.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

/**
 * Created by carolsuo on 2018/8/15.
 */

public class HttpDNS implements Dns {
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {

        List<String> ips = TXUGCPublishOptCenter.getInstance().query(hostname);  //获取HttpDNS解析结果
        if (ips == null || ips.size() == 0) {
            // When there is no HTTPDNS cache, use default resolution.
            return Dns.SYSTEM.lookup(hostname);
        }

        List<InetAddress> result = new ArrayList<>();
        for (String ip : ips) {
            result.addAll(Arrays.asList(InetAddress.getAllByName(ip)));
        }
        return result;
    }
}
