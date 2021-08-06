package com.tencent.qcloud.ugckit.module.mixrecord;

import com.tencent.qcloud.ugckit.module.record.UGCKitRecordConfig;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;

import java.util.ArrayList;
import java.util.List;

public class MixRecordConfig extends UGCKitRecordConfig {

    private String           mRecordPath;           // 视频录制的路径
    private long             mDuration;            // 跟拍视频的时长
    private int              mFps;                 // 跟拍视频的视频帧率
    private int              mRecordIndex;         // 录制视频序号
    private int              mWidth;
    private int              mHeight;
    private List<String>     mPlayPath;  // 跟拍视频的路径
    private ArrayList<Float> mVolumes;

    public MixRecordConfig() {
    }

    public void setInfo(List<String> videoPaths, int recordIndex, int width, int height, int recordAspect) {
        mPlayPath = new ArrayList<>();
        mDuration = Integer.MAX_VALUE;
        mFPS = Integer.MAX_VALUE;
        float ratio = Float.MAX_VALUE;
        for (int i = 0; i < videoPaths.size(); i++) {
            String path = videoPaths.get(i);
            TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(path);
            mPlayPath.add(path);
            if (info.fps < mFps) {
                mFps = (int) info.fps;
            }
            if (info.duration < mDuration) {
                mDuration = info.duration;
            }
//            if(ratio > (info.width*1.0f/info.height)){
//                mWidth = info.width;
//                mHeight = info.height;
//                ratio = (info.width*1.0f/info.height);
//            }
        }
        mRecordIndex = recordIndex;
        mMaxDuration = (int) mDuration;
        mFPS = mFps;
        mWidth = width;
        mHeight = height;
        mAspectRatio = recordAspect;//TXRecordCommon.VIDEO_ASPECT_RATIO_16_9
        mVolumes = new ArrayList<>(videoPaths.size() + 1);
        for (int j = 0; j < videoPaths.size() + 1; j++) {
            mVolumes.add(1.0f);
        }
    }

    public void setVolume(int index, float volume) {
        mVolumes.set(index, volume);
    }

    public ArrayList<Float> getVolumes() {
        return mVolumes;
    }

    public void updateInfo(int index, String path) {
        if (index < 0) {
            List<String> paths = new ArrayList<>();
            for (int i = 0; i < mPlayPath.size(); i++) {
                paths.add(path);
            }
            setInfo(paths, mRecordIndex, mWidth, mHeight, mAspectRatio);
            return;
        }
        if (index > mPlayPath.size()) {
            return;
        }
        if (index >= mRecordIndex) {
            index -= 1;
        }
        TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(path);
        mPlayPath.set(index, path);
        if (info.fps < mFps) {
            mFps = (int) info.fps;
        }
        if (info.duration < mDuration) {
            mDuration = info.duration;
        }
        mMaxDuration = (int) mDuration;
        mFPS = mFps;
//        if((mWidth*1.0/mHeight) > (info.width*1.0f/info.height)  ){
//            mWidth = info.width;
//            mHeight = info.height;
//        }
    }


    public void setRecordPath(String path) {
        mRecordPath = path;
    }

    public String getRecordPath() {
        return mRecordPath;
    }

    public List<String> getPaths() {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < mPlayPath.size(); i++) {
            paths.add(mPlayPath.get(i));
        }
        paths.add(mRecordIndex, mRecordPath);
        return paths;
    }

    public int getFps() {
        return mFps;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getWidth() {
        return mWidth;//
    }

    public int getHeight() {
        return mHeight;
    } //
}
