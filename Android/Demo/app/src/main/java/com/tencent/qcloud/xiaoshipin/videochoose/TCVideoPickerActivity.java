package com.tencent.qcloud.xiaoshipin.videochoose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.module.picker.view.IPickerLayout;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.ugckit.UGCKitVideoPicker;
import com.tencent.qcloud.xiaoshipin.videoeditor.TCVideoCutActivity;
import com.tencent.qcloud.xiaoshipin.videojoiner.TCVideoJoinerActivity;

import java.util.ArrayList;

public class TCVideoPickerActivity extends Activity {
    private UGCKitVideoPicker mUGCKitVideoPicker;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initWindowParam();
        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.PickerActivityTheme);
        setContentView(R.layout.activity_video_picker);
        mUGCKitVideoPicker = (UGCKitVideoPicker) findViewById(R.id.video_choose_layout);
        mUGCKitVideoPicker.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mUGCKitVideoPicker.setOnPickerListener(new IPickerLayout.OnPickerListener() {
            @Override
            public void onPickedList(ArrayList list) {
                int size = list.size();
                if (size == 0) {
                    return;
                } else if (size == 1) {
                    TCVideoFileInfo fileInfo = (TCVideoFileInfo) list.get(0);
                    startVideoCutActivity(fileInfo);
                } else {
                    startVideoJoinActivity(list);
                }
            }
        });
    }

    private void initWindowParam() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUGCKitVideoPicker.pauseRequestBitmap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUGCKitVideoPicker.resumeRequestBitmap();
    }

    public void startVideoCutActivity(TCVideoFileInfo fileInfo) {
        Intent intent = new Intent(this, TCVideoCutActivity.class);
        intent.putExtra(UGCKitConstants.VIDEO_PATH, fileInfo.getFilePath());
        startActivity(intent);
    }

    public void startVideoJoinActivity(ArrayList<TCVideoFileInfo> videoPathList) {
        Intent intent = new Intent(this, TCVideoJoinerActivity.class);
        intent.putExtra(UGCKitConstants.INTENT_KEY_MULTI_CHOOSE, videoPathList);
        startActivity(intent);
        finish();
    }

}
