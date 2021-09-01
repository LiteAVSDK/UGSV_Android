package com.tencent.qcloud.ugckit.module.effect.bubble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 加载气泡字幕的类
 */
public class TCBubbleManager {
    private static final String ROOT_DIR = "bubble";

    private static TCBubbleManager sInstance;
    private final  Context         mContext;

    public static TCBubbleManager getInstance(@NonNull Context context) {
        if (sInstance == null)
            sInstance = new TCBubbleManager(context);
        return sInstance;
    }

    private TCBubbleManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Nullable
    public List<TCBubbleInfo> loadBubbles() {
        try {
            // 获取气泡字幕总配置文件
            String rootJson = getConfigByPath(ROOT_DIR + File.separator + "bubbleList.json");
            JSONObject rootJSONObj = new JSONObject(rootJson);
            JSONArray rootJSONArr = rootJSONObj.getJSONArray("bubbleList");
            List<String> folderNameList = new ArrayList<>();
            for (int i = 0; i < rootJSONArr.length(); i++) {
                String folderName = rootJSONArr.getJSONObject(i).getString("name");
                folderNameList.add(folderName);
            }

            // 遍历获取各个气泡字幕的参数
            List<TCBubbleInfo> bubbleInfoList = new ArrayList<>(folderNameList.size());
            for (String folderName : folderNameList) {
                String folderPath = ROOT_DIR + File.separator + folderName;
                String configPath = folderPath + File.separator + "config.json";
                String iconPath = folderPath + File.separator + "icon.png";
                String bubblePath = folderPath + File.separator + "bubble.png";

                String jsonContent = getConfigByPath(configPath);
                JSONObject jsonObj = new JSONObject(jsonContent);
                int width = jsonObj.getInt("width");
                int height = jsonObj.getInt("height");
                int top = jsonObj.getInt("textTop");
                int left = jsonObj.getInt("textLeft");
                int right = jsonObj.getInt("textRight");
                int bottom = jsonObj.getInt("textBottom");
                int textSize = jsonObj.getInt("textSize");

                TCBubbleInfo info = new TCBubbleInfo();
                info.setWidth(width);
                info.setHeight(height);
                info.setDefaultSize(textSize);
                //归一化坐标
                info.setRect(top * 1.0f / height, left * 1.0f / width, right * 1.0f / width, bottom * 1.0f / height);
                info.setBubblePath(bubblePath);
                info.setIconPath(iconPath);

                bubbleInfoList.add(info);
            }
            return bubbleInfoList;
        } catch (@NonNull JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Nullable
    public Bitmap getBitmapFromAssets(@Nullable String path) {
        if (path == null) return null;
        try {
            return getBitmap(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getConfigByPath(String path) throws IOException {
        BufferedInputStream stream = getInputStreamFromAsset(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        stream.close();
        reader.close();
        return sb.toString();
    }


    private Bitmap getBitmap(String path) throws IOException {
        return BitmapFactory.decodeStream(getInputStreamFromAsset(path));
    }

    private BufferedInputStream getInputStreamFromAsset(String path) throws IOException {
        return new BufferedInputStream(mContext.getAssets().open(path));
    }
}
