package com.tencent.qcloud.ugckit.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.tencent.qcloud.ugckit.UGCKitImpl;
import com.tencent.qcloud.ugckit.R;

import com.tencent.rtmp.TXLog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * http请求封装，用于和业务server连接
 * 内部使用volley组件
 */
public class TCHttpEngine {
    private static final String TAG = TCHttpEngine.class.getSimpleName();
    private static final int HTTPTIMEOUT = 10000;

    private TCHttpEngine() {
    }

    private static class TCHttpEngineHolder {
        @NonNull
        private static TCHttpEngine instance = new TCHttpEngine();
    }

    @NonNull
    public static TCHttpEngine getInstance() {
        return TCHttpEngineHolder.instance;
    }

    public void initContext(@NonNull Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    private RequestQueue mRequestQueue;

    /**
     * 取消所有http请求
     */
    public void cancel() {
        mRequestQueue.cancelAll(TAG);
    }

    public void get(String url, @Nullable final Listener listener) {
        JsonObjectRequest jr = new JsonObjectRequest(Request.Method.GET, url, "", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (listener != null) {
                        listener.onResponse(0, null, response);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onResponse(-1, getErrorMessage(error), null);
                }
            }
        });
        jr.setRetryPolicy(new DefaultRetryPolicy(HTTPTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (mRequestQueue != null) {
            mRequestQueue.add(jr);
        }
    }

    public static String getErrorMessage(Object error) {
        if (error instanceof TimeoutError) {
            return UGCKitImpl.getAppContext().getResources().getString(R.string.tc_http_engine_get_error_message_connect_server_failed);
        } else if (isServerProblem(error)) {
            return handleServerError(error);
        } else if (isNetworkProblem(error)) {
            return UGCKitImpl.getAppContext().getResources().getString(R.string.tc_http_engine_get_error_message_no_network_connection);
        }
        return UGCKitImpl.getAppContext().getResources().getString(R.string.tc_http_engine_get_error_message_network_anomaly);
    }

    private static boolean isNetworkProblem(Object error) {
        return (error instanceof NetworkError)
                || (error instanceof NoConnectionError);
    }

    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError)
                || (error instanceof AuthFailureError);
    }

    private static String handleServerError(Object err) {
        VolleyError error = (VolleyError) err;

        NetworkResponse response = error.networkResponse;
        if (response != null) {
            TXLog.w(TAG, "HTTP Req error, error code:" + response.statusCode);
            switch (response.statusCode) {
                case 404:
                case 422:
                case 401:
                    try {
                        // server might return error like this { "error":
                        // "Some error occured" }
                        // Use "Gson" to parse the result
                        HashMap<String, String> result = new Gson().fromJson(
                                new String(response.data),
                                new TypeToken<Map<String, String>>() {
                                }.getType());

                        if (result != null && result.containsKey("error")) {
                            return result.get("error");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // invalid request
                    return error.getMessage();

                default:
                    return UGCKitImpl.getAppContext().getResources().getString(R.string.tc_http_engine_handle_server_error_connect_server_fail);
            }
        }
        return UGCKitImpl.getAppContext().getResources().getString(R.string.tc_http_engine_handle_server_error_network_anomaly);
    }

    public interface Listener {
        void onResponse(int retCode, String retMsg, JSONObject retData);
    }
}
