## 如何接入UGCKit

本文将一步一步教您如何接入UGCKit

- 步骤一：新建工程(Empty Activity) UGCDemo。
- 步骤二：导入UGCKit module。
- 步骤三：替换最新SDK。
- 步骤四：申请Licence。 

## 步骤一：新建工程

- 新建 Android Studio 工程，运行成功，配置 Project 的 build.gradle 。
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
    		# 拷贝开始
        classpath 'com.android.tools.build:gradle:2.2.3'
        # 拷贝结束
    }
}

allprojects {
    repositories {
    		# 拷贝开始
        flatDir {
            dirs 'src/main/jniLibs'
            dirs project(':ugckit').file('libs')
        }
        # 拷贝结束
        jcenter()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}

# 拷贝开始
ext {
    compileSdkVersion = 25
    buildToolsVersion = "25.0.2"
    supportSdkVersion = "25.4.0"
    minSdkVersion = 16
    targetSdkVersion = 23
    versionCode = 1
    versionName = "v1.0"
    proguard = true
    rootPrj = "$projectDir/.."
    ndkAbi = 'armeabi-v7a'
    noffmpeg = false
    noijkplay = false
    aekit_version = '1.0.16-cloud'
}
# 拷贝结束
```
- 配置 app 的 build.gradle 。 
```
apply plugin: 'com.android.application'

android {
    # 拷贝开始
    compileSdkVersion = rootProject.ext.compileSdkVersion
    buildToolsVersion = rootProject.ext.buildToolsVersion
    # 拷贝结束
    defaultConfig {
        applicationId = "com.tencent.qcloud.xiaoshipin"
        # 拷贝开始
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
        versionCode rootProject.ext.versionCode
        versionName rootProject.ext.versionName
        renderscriptTargetApi = 14
        renderscriptSupportModeEnabled = true
        multiDexEnabled = true
        ndk {
            abiFilters rootProject.ext.ndkAbi
        }
        # 拷贝结束
    } 
    buildTypes {
        release {
            minifyEnabled = false
            proguardFiles.add(file('proguard-rules.pro'))
        }
    } 
    dexOptions {
        javaMaxHeapSize "4g"
    }
    
    # 如果您使用的是商业版或商业版Pro，请加如下这段，基础版/精简版不需要
    packagingOptions {
        pickFirst '**/libc++_shared.so'
        doNotStrip "*/armeabi/libYTCommon.so"
        doNotStrip "*/armeabi-v7a/libYTCommon.so"
        doNotStrip "*/x86/libYTCommon.so"
        doNotStrip "*/arm64-v8a/libYTCommon.so"
    }
    # 如果您使用的是商业版或商业版Pro，请加如上这段，基础版/精简版不需要
}

dependencies {
    # 拷贝开始
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile fileTree(include: ['*.jar'], dir: 'src/main/jniLibs')
    compile 'com.mcxiaoke.volley:library:1.0.19'
    compile 'com.android.support:design:25.3.1'
    compile 'com.android.support:recyclerview-v7:25.3.1'
    compile 'com.google.code.gson:gson:2.3.1'
    compile 'com.github.bumptech.glide:glide:3.7.0'
    compile 'com.github.ctiao:dfm:0.4.4'
    # 拷贝结束
}
```

- 配置Gradle版本
```
distributionUrl=https\://services.gradle.org/distributions/gradle-3.3-all.zip
```

## 步骤二：导入UGCKit module

- 拷贝 UGCKit module 到 您新建的工程 UGCDemo 目录下。

- 在工程的 `settings.gradle`中导入 ugckit

```
include ':ugckit'
```

- 在工程 app module 中依赖 UGCKit module
```
compile project(':ugckit')
```

## 步骤三：替换最新SDK
- 拷贝SDK aar 到 UGCKit 的 libs 中
- 在 UGCKit 中 配置 SDK
```
compile(name: 'LiteAVSDK_Professional', ext: 'aar')
```

## 步骤四：申请Licence
- 在使用 UGCKit 之前要先设置 License，License的获取方法请参考 [License申请](https://cloud.tencent.com/document/product/584/20333)
- Github Demo的 `TCApplication.java` 类中的 `ugcKey` 和 `ugcLicenceUrl` 请替换成您自己的，否则部分功能无法正常使用。

## 常见问题
- 是否支持Android X？
  目前还不支持，如果项目编译有androidX的引用，请去掉

1. 修改当前项目的 gradle.properties(Project Properties)
```
android.useAndroidX=false
android.enableJetifier=false
```
1. 替换包引用
```
//import androidx.appcompat.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;

```
2. 替换xml中控件的包名，去掉androidx的相关引用
```
// androidx.constraintlayout.widget.ConstraintLayout
RelativeLayout

//app:layout_constraintBottom_toBottomOf="parent"
//app:layout_constraintLeft_toLeftOf="parent"
//app:layout_constraintRight_toRightOf="parent"
//app:layout_constraintTop_toTopOf="parent"
```
3. 去掉build.gradle中androidx
```
//implementation 'androidx.appcompat:appcompat:1.1.0'
//implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
//androidTestImplementation 'androidx.test:runner:1.2.0'
//androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
//testImplementation 'junit:junit:4.12'

```
- 出现如下编译错误，请检查 Android Studio Gradle 插件版本和Gradle版本是否匹配，[查看 Gradle 插件对应Gradle版本](https://developer.android.google.cn/studio/releases/gradle-plugin.html#updating-plugin)

UGCKit 使用的Gradle插件版本为 2.2.3 ，Gradle版本为 3.3
```
ERROR: Unable to find method 'org.gradle.api.tasks.compile.CompileOptions.setBootClasspath(Ljava/lang/String;)V'.
Possible causes for this unexpected error include:
```


关于如何使用UGCKit进行视频录制、裁剪、编辑，请看[下一篇](https://github.com/tencentyun/UGSVSDK/blob/master/Android/如何使用UGCKit.md)
