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
            //当httpdns缓存没有时，使用默认解析
            return Dns.SYSTEM.lookup(hostname);
        }

        List<InetAddress> result = new ArrayList<>();
        for (String ip : ips) {  //将ip地址数组转换成所需要的对象列表
            result.addAll(Arrays.asList(InetAddress.getAllByName(ip)));
        }
        return result;
    }
}
