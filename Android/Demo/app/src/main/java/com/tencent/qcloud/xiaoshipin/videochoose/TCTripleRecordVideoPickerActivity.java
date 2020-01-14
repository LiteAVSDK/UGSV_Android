package com.tencent.qcloud.xiaoshipin.videochoose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;
import com.tencent.qcloud.ugckit.module.picker.view.IPickerLayout;
import com.tencent.qcloud.xiaoshipin.R;

import java.util.ArrayList;

public class TCTripleRecordVideoPickerActivity extends Activity {
    private TripleRecordVideoPicker mTripleRecordVideoPicker;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initWindowParam();
        // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
        setTheme(R.style.PickerActivityTheme);
        setContentView(R.layout.activity_video_triple_picker);
        mTripleRecordVideoPicker = (TripleRecordVideoPicker) findViewById(R.id.video_choose_layout);
        mTripleRecordVideoPicker.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTripleRecordVideoPicker.setOnPickerListener(new IPickerLayout.OnPickerListener() {
            @Override
            public void onPickedList(ArrayList list) {
                TCVideoFileInfo fileInfo = (TCVideoFileInfo)list.get(0);
                Intent data = new Intent();
                data.putExtra("file",fileInfo.getFilePath());
                setResult(RESULT_OK,data);
                finish();
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
        mTripleRecordVideoPicker.pauseRequestBitmap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTripleRecordVideoPicker.resumeRequestBitmap();
    }
}
