# 如何使用UGCKit
- 如果您还不知道如何接入UGCKit，请看[上一篇](https://github.com/tencentyun/UGSVSDK/blob/master/Android/如何接入UGCKit.md)
- 如果您还不知道如何申请Licence，请看[如何申请Licence](https://cloud.tencent.com/document/product/584/20333)


本文将一步一步教您如何使用UGCKit
- 步骤一：设置Licence，初始化UGCKit
- 步骤二：如何使用 UGCKit 进行视频录制。
- 步骤三：如何使用 UGCKit 进行视频导入。
- 步骤四：如何使用 UGCKit 进行视频裁剪。
- 步骤五：如何使用 UGCKit 进行视频特效编辑。 


## 步骤一：设置Licence，初始化UGCKit
- 在 Application.java 中设置Licence，初始化UGCKit
```
// 设置Licence
TXUGCBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);
// 初始化UGCKit
UGCKit.init(this);
```

## 步骤二：如何使用 UGCKit 进行视频录制

![图片描述](https://main.qcloudimg.com/raw/6d2996c86edf6a796b681580f3c1fb05.png)

- 新建录制 xml， 加入如下配置 

``` xml
<com.tencent.qcloud.ugckit.UGCKitVideoRecord
    android:id="@+id/video_record_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
- 在`res/values/styles.xml`中新建空的录制主题，继承 UGCKit 默认录制主题
```
<style name="RecordActivityTheme" parent="RecordStyle"/>
```
- 新建录制Activity ，继承 `FragmentActivity`，实现接口`ActivityCompat.OnRequestPermissionsResultCallback`，获取 UGCKitVideoRecord 对象并设置回调方法

``` java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
    setTheme(R.style.RecordActivityTheme);
    setContentView(R.layout.activity_video_record);
    // 获取UGCKitVideoRecord 
    mUGCKitVideoRecord = (UGCKitVideoRecord) findViewById(R.id.video_record_layout);
    // 设置录制监听
    mUGCKitVideoRecord.setOnRecordListener(new IVideoRecordKit.OnRecordListener() {
        @Override
        public void onRecordCanceled() {
            // 录制被取消
        }

        @Override
        public void onRecordCompleted(UGCKitResult result) {
            // 录制完成回调
        }
    });
}

@Override
protected void onStart() {
    super.onStart();
    // 判断是否开启了“相机”和“录音权限”(如何判断权限，参考Github/Demo示例)
    if (hasPermission()) {
        // UGCKit接管录制的生命周期（关于更多UGCKit接管录制生命周期的方法，参考Github/Demo示例）
        mUGCKitVideoRecord.start();
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        mUGCKitVideoRecord.start();
    }
}
```

##  步骤三：如何使用 UGCKit 进行视频导入
![图片描述](https://main.qcloudimg.com/raw/a06caa5a974ff7b129255710840148e1.png)

- 新建xml，加入如下配置

```xml
 <com.tencent.qcloud.ugckit.UGCKitVideoPicker
        android:id="@+id/video_picker"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
- 在`res/values/styles.xml`中新建空的主题，继承 UGCKit 默认视频导入主题
```
<style name="PickerActivityTheme" parent="PickerStyle"/>
```
- 新建activity，继承 Activity，获取 UGCKitVideoPicker 对象，设置对象回调

``` java
@Override
public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
    setTheme(R.style.PickerActivityTheme);
    setContentView(R.layout.activity_video_picker);
    // 获取UGCKitVideoPicker
    mUGCKitVideoPicker = (UGCKitVideoPicker) findViewById(R.id.video_picker);
    // 设置视频选择监听
    mUGCKitVideoPicker.setOnPickerListener(new IPickerLayout.OnPickerListener() {
        @Override
        public void onPickedList(ArrayList<TCVideoFileInfo> list) {
            // UGCKit返回选择的视频路径集合
        }
    });
}
```

## 步骤四：如何使用 UGCKit 进行视频裁剪

![图片描述](https://main.qcloudimg.com/raw/b7ada99f174e21e09e7b9c78e96c1858.png)

- 新建 xml ，加入如下配置

```xml
<com.tencent.qcloud.ugckit.UGCKitVideoCut
        android:id="@+id/video_cutter"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
- 在`res/values/styles.xml`中新建空的主题，继承 UGCKit 默认编辑主题
```
<style name="EditerActivityTheme" parent="EditerStyle"/>
```
- 新建 Activity ，实现接口`FragmentActivity`，获取 UGCKitVideoCut 对象，并设置回调方法
```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
    setTheme(R.style.EditerActivityTheme);
    setContentView(R.layout.activity_video_cut);
    mUGCKitVideoCut = (UGCKitVideoCut) findViewById(R.id.video_cutter);
    // 获取上一个界面视频导入传过来的视频源路径
    mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
    // UGCKit设置视频源路径
    mUGCKitVideoCut.setVideoPath(mVideoPath);
    // 设置视频生成的监听
    mUGCKitVideoCut.setOnCutListener(new IVideoCutKit.OnCutListener() {
        
        @Override
        public void onCutterCompleted(UGCKitResult ugcKitResult) {
            // 视频裁剪进度条执行完成后调用
        }

        @Override
        public void onCutterCanceled() {
            // 取消裁剪时被调用
        }
    });
}

@Override
protected void onResume() {
    super.onResume();
    // UGCKit接管裁剪界面的生命周期（关于更多UGCKit接管裁剪生命周期的方法，参考Github/Demo示例）
    mUGCKitVideoCut.startPlay();
}
```

## 步骤五：如何使用 UGCKit 进行视频特效编辑

![图片描述](https://main.qcloudimg.com/raw/dbe2669117f1015bf994705cc76deed8.jpg)

- 在编辑 activity 的 xml 中加入如下配置
``` xml
<com.tencent.qcloud.ugckit.UGCKitVideoEdit
        android:id="@+id/video_edit"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
- 新建编辑 Activity ，继承 `FragmentActivity`，获取 UGCKitVideoEdit 对象并设置回调方法 

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 必须在代码中设置主题(setTheme)或者在AndroidManifest中设置主题(android:theme)
    setTheme(R.style.EditerActivityTheme);
    setContentView(R.layout.activity_video_editer);
    // 设置视频源路径（非必须，如果上个界面是裁剪界面，且设置setVideoEditFlag(true)则可以不用设置视频源）
    mVideoPath = getIntent().getStringExtra(UGCKitConstants.VIDEO_PATH);
    mUGCKitVideoEdit = (UGCKitVideoEdit) findViewById(R.id.video_edit);
    if (!TextUtils.isEmpty(mVideoPath)) {
        mUGCKitVideoEdit.setVideoPath(mVideoPath);
    }
    // 初始化播放器
    mUGCKitVideoEdit.initPlayer();
    mUGCKitVideoEdit.setOnVideoEditListener(new IVideoEditKit.OnEditListener() {
        @Override
        public void onEditCompleted(UGCKitResult ugcKitResult) {
            // 视频编辑完成
        }

        @Override
        public void onEditCanceled() {
            
        }
    });
}

@Override
protected void onResume() {
    super.onResume();
    // UGCKit接管编辑界面的生命周期（关于更多UGCKit接管编辑生命周期的方法，参考Github/Demo示例）
    mUGCKitVideoEdit.start();
}
```

关于如何定制UGCKit主题，请看[下一篇](https://github.com/tencentyun/UGSVSDK/blob/master/Android/如何定制UGCKit主题.md)