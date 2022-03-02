package com.tencent.qcloud.ugckit.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Android 9 已经去掉org.apache.http.legacy 这个库，使用 HTTPURLConnection
 */
public class TCHttpURLClient {

    private static class Holder {
        public static final TCHttpURLClient INSTANCE = new TCHttpURLClient();
    }

    public static TCHttpURLClient getInstance() {
        return Holder.INSTANCE;
    }

    private TCHttpURLClient() {
    }


    /**
     * get请求
     *
     * @param urlStr
     * @param callback
     */
    public void get(final String urlStr, final OnHttpCallback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(urlStr);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.connect();
                    InputStream in = connection.getInputStream();
                    if (in == null) {
                        if (callback != null)
                            callback.onError();
                        return;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    if (callback != null)
                        callback.onSuccess(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.onError();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * post json数据请求
     *
     * @param urlStr
     * @param callback
     */
    public void postJson(final String urlStr, final String json, final OnHttpCallback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.connect();

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(json.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    InputStream in = connection.getInputStream();
                    if (in == null) {
                        if (callback != null)
                            callback.onError();
                        return;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    if (callback != null)
                        callback.onSuccess(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.onError();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    public interface OnHttpCallback {
        void onSuccess(String result);

        void onError();
    }

}
