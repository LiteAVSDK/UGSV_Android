package com.tencent.xmagic;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.tencent.effect.beautykit.TEBeautyKit;
import com.tencent.effect.beautykit.model.TEUIProperty;
import com.tencent.effect.beautykit.utils.LogUtils;
import com.tencent.effect.beautykit.view.panelview.TEPanelView;
import com.tencent.xmagic.utils.BitmapUtil;
import com.tencent.xmagic.utils.UriUtils;

import java.io.File;

public class CustomPropertyManager {

    private static final String TAG = CustomPropertyManager.class.getName();

    public static final int TE_CHOOSE_PHOTO_SEG_CUSTOM = 2002;
    public static String PICK_CONTENT_ALL = "image/*|video/*";


    private Handler handler = new Handler(Looper.getMainLooper());
    private TEUIProperty customProperty = null;

    private TEBeautyKit beautyKit = null;

    private TEPanelView tePanelView = null;


    public void setBeautyKit(TEBeautyKit beautyKit) {
        this.beautyKit = beautyKit;
    }

    public void setData(TEUIProperty customProperty, TEBeautyKit beautyKit, TEPanelView tePanelView) {
        this.customProperty = customProperty;
        this.beautyKit = beautyKit;
        this.tePanelView = tePanelView;
    }

    public void pickMedia(Activity activity, int requestCode, String pickContent) {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, pickContent);
        activity.startActivityForResult(intentToPickPic, requestCode); // 打开相册，选择图片
    }


    public void onActivityResult(Context context, int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            String filePath = null;
            if (data != null) {
                filePath = UriUtils.getFilePathByUri(context.getApplicationContext(), data.getData());
            } else {
                LogUtils.e(TAG, "the data and filePath is null ");
                return;
            }
            if (requestCode == TE_CHOOSE_PHOTO_SEG_CUSTOM) {  //表示自定义分割
                setCustomSegParam(context, filePath);
            }
        } else {
            customProperty = null;
        }
    }


    private void setCustomSegParam(Context context, String filePath) {
        if (customProperty != null && customProperty.sdkParam != null && customProperty.sdkParam.extraInfo != null
                && (!TextUtils.isEmpty(filePath)) && new File(filePath).exists()) {
            if (filePath.endsWith("jpg")
                    || filePath.endsWith("JPG")
                    || filePath.endsWith("PNG")
                    || filePath.endsWith("png")
                    || filePath.endsWith("jpeg")
                    || filePath.endsWith("JPEG")) {
                BitmapUtil.compressImage(context.getApplicationContext(), filePath, imgPath -> {
                    customProperty.sdkParam.extraInfo.put(TEUIProperty.TESDKParam.EXTRA_INFO_KEY_BG_TYPE,
                            TEUIProperty.TESDKParam.EXTRA_INFO_BG_TYPE_IMG);
                    customProperty.sdkParam.extraInfo.put(TEUIProperty.TESDKParam.EXTRA_INFO_KEY_BG_PATH, imgPath);
                    beautyKit.setEffect(customProperty.sdkParam);
                    handler.post(() -> {
                        tePanelView.checkPanelViewItem(customProperty);
                        customProperty = null;
                    });
                });
            } else {
                customProperty.sdkParam.extraInfo.put(TEUIProperty.TESDKParam.EXTRA_INFO_KEY_BG_TYPE,
                        TEUIProperty.TESDKParam.EXTRA_INFO_BG_TYPE_VIDEO);
                customProperty.sdkParam.extraInfo.put(TEUIProperty.TESDKParam.EXTRA_INFO_KEY_BG_PATH, filePath);
                beautyKit.setEffect(customProperty.sdkParam);
                handler.post(() -> {
                    tePanelView.checkPanelViewItem(customProperty);
                    customProperty = null;
                });
            }
        } else {
            customProperty = null;
        }
    }


}
