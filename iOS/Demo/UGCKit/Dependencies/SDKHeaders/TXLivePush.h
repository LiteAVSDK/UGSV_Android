/*
 * Module:   TXLivePusher @ TXLiteAVSDK
 *
 * Function: 腾讯云直播推流用 RTMP SDK
 *
 *
 * Version: <:Version:>
 */

#import <Foundation/Foundation.h>
#import <VideoToolbox/VideoToolbox.h>
#if TARGET_OS_IPHONE
#import <ReplayKit/ReplayKit.h>
#endif
#import "TXLivePushConfig.h"
#import "TXLivePushListener.h"
#import "TXVideoCustomProcessDelegate.h"
#import "TXAudioCustomProcessDelegate.h"
#import "TXLiveRecordListener.h"
#import "TXLiveSDKTypeDef.h"
#import <AVFoundation/AVFoundation.h>
#import "TXBeautyManager.h"

#define TX_DEPRECAETD_BEAUTY_API __deprecated_msg("Use getBeautyManager instead.")

/**
 * 直播推流类
 *
 * 主要负责将本地的音视频画面进行编码和 RTMP 推送，包含如下技术特点：
 *
 * - 针对腾讯云的推流地址，会采用 QUIC 协议进行加速，配合改进后的 BBR2 带宽测算方案，可以最大限度的利用主播的上行带宽，降低直播卡顿率。
 * - 内嵌套的 Qos 流量控制技术具备上行网络自适应能力，可以根据主播端网络的具体情况实时调节音视频数据量。
 * - 内嵌多套美颜磨皮算法（自然&光滑）和多款色彩空间滤镜（支持自定义滤镜），可以根据需要自行选择。
 * - 企业版包含了基于优图 AI 人脸识别技术的大眼、瘦脸、隆鼻以及动效挂架，只需要购买**优图 License**就可以零成本集成。
 * - 支持自定义的音视频采集和渲染，让您可以根据项目需要选择自己的音视频数据源。
 */
@interface TXLivePush : NSObject


/////////////////////////////////////////////////////////////////////////////////
//
//                      （一）SDK 基础函数
//
/////////////////////////////////////////////////////////////////////////////////

/// @name SDK 基础函数
/// @{
/**
 * 1.1 创建 TXLivePusher 示例
 *
 * @param config TXLivePushConfig 推流配置项，见 “TXLivePushConfig.h” 文件中的详细定义
 */
- (id)initWithConfig:(TXLivePushConfig *)config;

/**
 * 1.2 设置 TXLivePushConfig 推流配置项，见 “TXLivePushConfig.h” 文件中的详细定义
 */
@property(nonatomic,copy) TXLivePushConfig *config;

/**
 * 1.3 设置推流回调接口，见“TXLivePushListener.h” 文件中的详细定义
 */
@property(nonatomic,weak) id <TXLivePushListener> delegate;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                      （二）推流基础接口
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 推流基础接口
/// @{
/**
 * 2.1 启动摄像头预览
 *
 * 启动预览后并不会立刻开始 RTMP 推流，需要调用 startPush() 才能真正开始推流。
 *
 * @param view 承载视频画面的控件
 */
- (int)startPreview:(TXView *)view;

/**
 * 2.2 停止摄像头预览
 */
- (void)stopPreview;

/**
 * 2.3 启动 RTMP 推流
 *
 * 针对腾讯云的推流地址，会采用 QUIC 协议进行加速，配合改进后的 BBR2 带宽测算方案，可以最大限度的利用主播的上行带宽，降低直播卡顿率。
 *
 * @param rtmpURL 推流地址，参考文档：[获取推流地址](https://cloud.tencent.com/document/product/267/32720 )。
 *
 * @note -5 返回码代表 license 校验失败，TXLivePusher 需要 [license](https://cloud.tencent.com/document/product/454/34750) 校验通过才能工作。
 *
 * @return 0: 启动成功；-1: 启动失败；-5：license 校验失败。
 */
- (int)startPush:(NSString *)rtmpURL;

/**
 * 2.4 停止 RTMP 推流
 */
- (void)stopPush;

/**
 * 2.5 暂停摄像头采集并进入垫片推流状态
 *
 * SDK 会暂时停止摄像头采集，并使用 TXLivePushConfig.pauseImg 中指定的图片作为替代图像进行推流，也就是所谓的“垫片”。
 * 这项功能常用于 App 被切到后台运行的场景，尤其是在 iOS 系统中，当 App 切到后台以后，操作系统不会再允许该 App 继续使用摄像头。
 * 此时就可以通过调用 pausePush() 进入垫片状态。
 *
 * 对于绝大多数推流服务器而言，如果超过一定时间不推视频数据，服务器会断开当前的推流链接。
 *
 * 在 TXLivePushConfig 您可以指定：
 * - pauseImg  设置后台推流的默认图片，不设置为默认黑色背景。
 * - pauseFps  设置后台推流帧率，最小值为5，最大值为20，默认10。
 * - pauseTime 设置后台推流持续时长，单位秒，默认300秒。
 *
 * @note 请注意调用顺序：startPush => ( pausePush => resumePush ) => stopPush()，错误的调用顺序会导致 SDK 表现异常。
 */
- (void)pausePush;

/**
 * 2.6 恢复摄像头采集并结束垫片推流状态
 */
- (void)resumePush;

/**
 * 2.7 查询是否正在推流
 *
 * @return YES：推流中；NO：没有在推流。
 */
- (bool)isPublishing;

/**
 * 2.8 获取当前推流的 RTMP 地址
 */
@property(nonatomic,readonly) NSString *rtmpURL;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                      （三）视频相关接口
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 视频相关接口
/// @{

/**
 * 3.1 设置视频编码质量
 *
 * 推荐设置：秀场直播 quality：HIGH_DEFINITION；adjustBitrate：NO；adjustResolution：NO。
 * 参考文档：[设定清晰度](https://cloud.tencent.com/document/product/454/7879#step-4.3A-.E8.AE.BE.E5.AE.9A.E6.B8.85.E6.99.B0.E5.BA.A6)
 *
 * @param quality            画质类型（标清，高清，超高清）
 * @param adjustBitrate      动态码率开关
 * @param adjustResolution   动态切分辨率开关
 *
 * @note adjustResolution 早期被引入是为了让 TXLivePusher 能够满足视频通话这一封闭场景下的一些需求，现已不推荐使用。
 *       如果您有视频通话的需求，可以使用我们专门为视频通话打造的 [TRTC](https://cloud.tencent.com/product/trtc) 服务。
 *       由于目前很多 H5 播放器不支持分辨率动态变化，所以开启分辨率自适应以后，会导致 H5 播放端和录制文件的很多兼容问题。
 */
- (void)setVideoQuality:(TX_Enum_Type_VideoQuality)quality
          adjustBitrate:(BOOL) adjustBitrate
       adjustResolution:(BOOL) adjustResolution;

/**
 * 3.2 切换前后摄像头（iOS）
 */
- (int)switchCamera;
#if TARGET_OS_MAC && !TARGET_OS_IPHONE
/**
 * 3.2 选择摄像头（macOS）
 */
- (void)selectCamera:(AVCaptureDevice *)camera;
#endif

/**
 * 3.3 查询当前是否为前置摄像头
 */
@property(nonatomic,readonly) BOOL frontCamera;

/**
 * 3.4 设置视频镜像效果
 *
 * 由于前置摄像头采集的画面是取自手机的观察视角，如果将采集到的画面直接展示给观众，是完全没有问题的。
 * 但如果将采集到的画面也直接显示给主播，则会跟主播照镜子时的体验完全相反，会让主播感觉到很奇怪。
 * 因此，SDK 会默认开启本地摄像头预览画面的镜像效果，让主播直播时跟照镜子时保持一个体验效果。
 *
 * setMirror 所影响的则是观众端看到的视频效果，如果想要保持观众端看到的效果跟主播端保持一致，需要开启镜像；
 * 如果想要让观众端看到正常的未经处理过的画面（比如主播弹吉他的时候有类似需求），则可以关闭镜像。
 *
 * @note 仅当前使用前置摄像头时，setMirror 接口才会生效，在使用后置摄像头时此接口无效。
 *
 * @param isMirror YES：播放端看到的是镜像画面；NO：播放端看到的是非镜像画面。
 */
- (void)setMirror:(BOOL)isMirror;

/**
 * 3.5 设置本地摄像头预览画面的旋转方向
 *
 * 该接口仅能够改变主播本地预览画面的方向，而不会改变观众端的画面效果。
 * 如果希望改变观众端看到的视频画面的方向，比如原来是540x960，希望变成960x540，则可以通过设置 TXLivePushConfig 中的 homeOrientation 来实现。
 *
 * <pre>
 * // 竖屏推流（HOME 键在下）
 * _config.homeOrientation = HOME_ORIENTATION_DOWN;
 * [_txLivePublisher setConfig:_config];
 * [_txLivePublisher setRenderRotation:0];
 * // 横屏推流（HOME 键在右）
 * _config.homeOrientation = HOME_ORIENTATION_RIGHT;
 * [_txLivePublisher setConfig:_config];
 * [_txLivePublisher setRenderRotation:90];
 * </pre>
 *
 * @param rotation 取值为0 ,90,180，270（其他值无效），表示主播端摄像头预览视频的顺时针旋转角度。
 */
- (void)setRenderRotation:(int)rotation;

/**
 * 3.6 打开后置摄像头旁边的闪关灯
 *
 * 此操作对于前置摄像头是无效的，因为绝大多数手机都没有给前置摄像头配置闪光灯。
 *
 * @param bEnable YES：打开；NO：关闭。
 * @return YES：打开成功；NO：打开失败。
 */
- (BOOL)toggleTorch:(BOOL)bEnable;

/**
 * 3.7 调整摄像头的焦距
 *
 * @param distance 焦距大小，取值范围1 - 5，默认值建议设置为1即可。
 * @note 当 distance 为1的时候为最远视角（正常镜头），当为5的时候为最近视角（放大镜头），最大值不要超过5，超过5后画面会模糊不清。
 */
- (void)setZoom:(CGFloat)distance;

/**
 * 3.8 设置手动对焦区域
 *
 * SDK 默认使用摄像头自动对焦功能，您也可以通过 TXLivePushConfig 中的 touchFocus 选项关闭自动对焦，改用手动对焦。
 * 改用手动对焦之后，需要由主播自己点击摄像头预览画面上的某个区域，来手动指导摄像头对焦。
 *
 * @note 早期 SDK 版本仅仅提供了手动和自动对焦的选择开关，并不支持设置对焦位置，3.0 版本以后，手动对焦的接口才开放出来。
 */
- (void)setFocusPosition:(CGPoint)touchPoint;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                      （四）美颜相关接口
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 美颜相关接口
/// @{

/**
 * 4.1 获取美颜管理对象
 *
 * 通过美颜管理，您可以使用以下功能：
 * - 设置"美颜风格"、“美白”、“红润”、“大眼”、“瘦脸”、“V脸”、“下巴”、“短脸”、“小鼻”、“亮眼”、“白牙”、“祛眼袋”、“祛皱纹”、“祛法令纹”等美容效果。
 * - 调整“发际线”、“眼间距”、“眼角”、“嘴形”、“鼻翼”、“鼻子位置”、“嘴唇厚度”、“脸型”
 * - 设置人脸挂件（素材）等动态效果
 * - 添加美妆
 * - 进行手势识别
 */
- (TXBeautyManager *)getBeautyManager;

/**
 * 4.2 设置美颜级别和美白级别
 *
 * @param beautyStyle 使用哪种磨皮算法，支持光滑和自然两种，光滑风格磨皮更加明显，适合秀场直播。见 “TXLiveSDKTypeDef.h” 中的 TX_Enum_Type_BeautyStyle 定义。
 * @param beautyLevel 美颜级别，取值范围0 - 9； 0表示关闭，1 - 9值越大，效果越明显。
 * @param whitenessLevel 美白级别，取值范围0 - 9；0表示关闭，1 - 9值越大，效果越明显。
 * @param ruddinessLevel 红润级别，取值范围0 - 9；0表示关闭，1 - 9值越大，效果越明显。
 */
- (void)setBeautyStyle:(TX_Enum_Type_BeautyStyle)beautyStyle
           beautyLevel:(float)beautyLevel
        whitenessLevel:(float)whitenessLevel
        ruddinessLevel:(float)ruddinessLevel TX_DEPRECAETD_BEAUTY_API;

/**
 * 4.3 设置指定素材滤镜特效
 *
 * @note 滤镜图片一定要用 png 格式，demo 用到的滤镜查找表图片位于 TXLiteAVDemo/Resource/Beauty/filter/FilterResource.bundle 中
 */
- (void)setFilter:(TXImage *)image;

/**
 * 4.4 设置滤镜浓度
 *
 * 在美女秀场等应用场景里，滤镜浓度的要求会比较高，以便更加突显主播的差异。
 * 我们默认的滤镜浓度是0.5，如果您觉得滤镜效果不明显，可以使用下面的接口进行调节。
 *
 * @param specialValue 取值范围0 - 1的浮点型数字，取值越大滤镜效果越明显，默认取值0.5。
 */
- (void)setSpecialRatio:(float)specialValue;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                      （五）企业版美颜和动效挂件
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 企业版美颜和动效挂件
/// @{

/**
 * 5.1 设置大眼级别（企业版有效，其它版本设置此参数无效）
 *
 * @param eyeScaleLevel 大眼级别，取值范围0 - 9；0表示关闭，1 - 9值越大，效果越明显。
 */
- (void)setEyeScaleLevel:(float)eyeScaleLevel TX_DEPRECAETD_BEAUTY_API;

/**
 * 5.2 设置瘦脸级别（企业版有效，其它版本设置此参数无效）
 *
 *  @param faceScaleLevel 瘦脸级别，取值范围0 - 9；0表示关闭，1 - 9值越大，效果越明显。
 */
- (void)setFaceScaleLevel:(float)faceScaleLevel TX_DEPRECAETD_BEAUTY_API;

/**
 * 5.3 设置 V 脸级别（企业版有效，其它版本设置此参数无效）
 *
 * @param faceVLevel V 脸级别，取值范围0 - 9；0表示关闭，1 - 9值越大，效果越明显。
 */
- (void)setFaceVLevel:(float)faceVLevel TX_DEPRECAETD_BEAUTY_API;

/**
 * 5.4 设置下巴拉伸或收缩（企业版有效，其它版本设置此参数无效）
 *
 * @param chinLevel 下巴拉伸或收缩级别，取值范围 -9 - 9；0 表示关闭，小于0表示收缩，大于0表示拉伸。
 */
- (void)setChinLevel:(float)chinLevel TX_DEPRECAETD_BEAUTY_API;

/**
 * 5.5 设置短脸级别（企业版有效，其它版本设置此参数无效）
 *
 * @param faceShortlevel 短脸级别，取值范围0 - 9；0表示关闭，1 - 9值越大，效果越明显。
 */
- (void)setFaceShortLevel:(float)faceShortlevel TX_DEPRECAETD_BEAUTY_API;

/**
 * 5.6 设置瘦鼻级别（企业版有效，其它版本设置此参数无效）
 *
 * @param noseSlimLevel 瘦鼻级别，取值范围0 - 9；0表示关闭，1 - 9值越大，效果越明显。
 */
- (void)setNoseSlimLevel:(float)noseSlimLevel TX_DEPRECAETD_BEAUTY_API;

/**
 * 5.7 设置绿幕背景视频（企业版有效，其它版本设置此参数无效）
 *
 * 此处的绿幕功能并非智能抠背，它需要被拍摄者的背后有一块绿色的幕布来辅助产生特效
 *
 * @param file 视频文件路径。支持 MP4；nil 表示关闭特效。
 */
- (void)setGreenScreenFile:(NSURL *)file;

/**
 * 5.8 选择使用哪一款 AI 动效挂件（企业版有效，其它版本设置此参数无效）
 *
 * @param tmplName 动效名称
 * @param tmplDir 动效所在目录
 */
- (void)selectMotionTmpl:(NSString *)tmplName inDir:(NSString *)tmplDir TX_DEPRECAETD_BEAUTY_API;

/**
 * 5.9 设置动效静音（企业版有效，其它版本设置此参数无效）
 *
 * 有些挂件本身会有声音特效，通过此 API 可以关闭这些特效播放时所带的声音效果。
 *
 * @param motionMute YES：静音；NO：不静音。
 */
- (void)setMotionMute:(BOOL)motionMute TX_DEPRECAETD_BEAUTY_API;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                      （六）音频相关接口
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 音频相关接口
/// @{

/**
 * 6.1 开启静音
 *
 * 开启静音后，SDK 并不会继续采集麦克风的声音，但是会用非常低（5kbps 左右）的码率推送伪静音数据，
 * 这样做的目的是为了兼容 H5 上的 video 标签，并让录制出来的 MP4 文件有更好的兼容性。
 *
 * @param bEnable 是否开启静音。
 */
- (void)setMute:(BOOL)bEnable;

/**
 * 6.2 播放背景音乐
 *
 * SDK 会将背景音乐和麦克风采集的声音进行混合并一起推送到云端。
 *
 * @param path 本地音乐文件路径
 * @return YES：成功；NO：失败。
 */
- (BOOL)playBGM:(NSString *)path;

/**
 * 6.3 播放背景音乐（高级版本）
 *
 * @param path 本地音乐文件路径
 * @param beginNotify 播放开始的回调
 * @param progressNotify 播放进度回调
 * @param completeNotify 播放完毕回调
 * @return YES：成功；NO：失败。
 */
- (BOOL)   playBGM:(NSString *)path
   withBeginNotify:(void (^)(NSInteger errCode))beginNotify
withProgressNotify:(void (^)(NSInteger progressMS,NSInteger durationMS))progressNotify
 andCompleteNotify:(void (^)(NSInteger errCode))completeNotify;

/**
 * 6.4 停止播放背景音乐
 */
- (BOOL)stopBGM;

/**
 * 6.5 暂停播放背景音乐
 */
- (BOOL)pauseBGM;

/**
 * 6.6 继续播放背景音乐
 */
- (BOOL)resumeBGM;

/**
 * 6.7 获取背景音乐文件的总时长，单位是毫秒
 * @param path 音乐文件路径，如果 path 为空，那么返回当前正在播放的背景音乐的时长。
 */
- (int)getMusicDuration:(NSString *)path;

/**
 * 6.8 设置混音时背景音乐的音量大小，仅在播放背景音乐混音时使用。
 *
 * @param volume 音量大小，1为正常音量，范围是0 - 1之间的浮点数。
 * @return YES：成功；NO：失败。
 */
- (BOOL)setBGMVolume:(float)volume;

/**
 * 6.9 设置混音时麦克风音量大小，仅在播放背景音乐混音时使用。
 *
 * @param volume 音量大小，1为正常音量，范围是0 - 1之间的浮点数。
 * @return YES：成功；NO：失败。
 */
- (BOOL)setMicVolume:(float)volume;

/**
 * 6.10 调整背景音乐的音调高低
 *
 * @param pitch 音调，默认值是0.0f，范围是-1 - 1之间的浮点数；
 * @return YES：成功；NO：失败。
 */
- (BOOL)setBGMPitch:(float)pitch;

/**
 * 6.11 设置混响效果
 *
 * @param reverbType 混响类型，详见 “TXLiveSDKTypeDef.h” 中的 TXReverbType 定义。
 * @return YES：成功；NO：失败。
 */
- (BOOL)setReverbType:(TXReverbType)reverbType;

/**
 * 6.12 设置变声类型
 *
 * @param voiceChangerType 混响类型，详见 “TXLiveSDKTypeDef.h” 中的 voiceChangerType 定义。
 * @return YES：成功；NO：失败。
 */
- (BOOL)setVoiceChangerType:(TXVoiceChangerType)voiceChangerType;

/**
 * 6.13 指定背景音乐的播放位置
 *
 * @note 请尽量避免频繁地调用该接口，因为该接口可能会再次读写 BGM 文件，耗时稍高。
 *       例如：当配合进度条使用时，请在进度条拖动完毕的回调中调用，而避免在拖动过程中实时调用。
 *
 * @param position 背景音乐的播放位置，单位ms。
 *
 * @return 结果是否成功，YES：成功；NO：失败。
 */
- (BOOL)setBGMPosition:(NSInteger)position;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                    （七）本地录制和截图
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 本地录制接口
/// @{

/**
 * 7.1 录制回调接口，详见 "TXLiveRecordTypeDef.h" 中的 TXLiveRecordListener 定义。
 */
@property (nonatomic,weak) id<TXLiveRecordListener>  recordDelegate;

/**
 * 7.2 开始录制短视频
 *
 * @note 1. 只有启动推流后才能开始录制，非推流状态下启动录制无效。
 *       2. 出于安装包体积的考虑，仅专业版和企业版两个版本的 LiteAVSDK 支持该功能，直播精简版仅定义了接口但并未实现。
 *       3. 录制过程中请勿动态切换分辨率和软硬编，会有很大概率导致生成的视频异常。
 *
 * @param videoPath 视频录制后存储路径
 * @return 0：成功；-1：videoPath 为空；-2：上次录制尚未结束，请先调用 stopRecord；-3：推流尚未开始。
 */
-(int) startRecord:(NSString *)videoPath;

/**
 * 7.3 结束录制短视频，当停止推流后，如果视频还在录制中，SDK 内部会自动结束录制。
 *  @return 0：成功； -1：不存在录制任务。
 */
-(int) stopRecord;

/**
 * 7.4 推流过程中本地截图
 *
 * @param snapshotCompletionBlock 截图完成的回调函数
 */
- (void)snapshot:(void (^)(TXImage *))snapshotCompletionBlock;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                     （八）自定义采集和处理
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 自定义采集和处理
/// @{
/**
 * 8.1 自定义视频采集，向 SDK 发送自己采集的视频数据。
 *
 * 在自定义视频采集模式下，SDK 不再继续从摄像头采集图像，只保留编码和发送能力，您需要定时地发送自己采集的 SampleBuffer。
 * 要开启自定义视频采集，需要完成如下两个步骤：
 *
 * 1. 开启自定义采集：给 TXLivePushConfig 中的 customModeType 属性增加 CUSTOM_MODE_VIDEO_CAPTURE 选项，代表开启自定义视频采集。
 * 2. 设定视频分辨率：将 TXLivePushConfig 中的 sampleBufferSize 属性设置为您期望的分辨率。
 *   如果期望编码分辨率跟采集分辨率一致，可以不设置 sampleBufferSize 属性，而是将 autoSampleBufferSize 设置为 YES。
 *
 * @note 1. 开启自定义视频采集后，即无需再调用 startPreview 来开启摄像头采集。
 *       2. SDK 内部有简单的帧率控制，如果发送太快时 SDK 会自动丢弃多余的帧率；如果超时不发送，SDK 会不断地重复发送的最后一帧。
 *
 * @param sampleBuffer 向 SDK 发送的 SampleBuffer
 */
- (void)sendVideoSampleBuffer:(CMSampleBufferRef)sampleBuffer;

/**
 * 8.2 自定义音频采集，向 SDK 发送自己采集的音频 PCM 数据。
 *
 * 在自定义音频采集模式下，SDK 不再继续从麦克风采集声音，只保留编码和发送能力，您需要定时地发送自己采集的声音数据（PCM 格式）
 * 要开启自定义音频采集，需要完成如下两个步骤：
 *
 * 1. 开启自定义采集：给 TXLivePushConfig 中的 customModeType 属性增加 CUSTOM_MODE_AUDIO_CAPTURE 选项，代表开启自定义音频采集。
 * 2. 设定音频采样率：将 TXLivePushConfig 中的 audioSampleRate 属性设置为您期望的音频采样率，audioChannels 设置为期望的声道数，默认值：1（单声道）。
 *
 * @note SDK 对每次传入的 PCM buffer 大小有严格要求，每一个采样点要求是16位宽。
 *       如果是单声道，请保证传入的 PCM 长度为2048；如果是双声道，请保证传入的 PCM 长度为4096。
 *
 * @param data 要发送的 PCM buffer
 * @param len 数据长度
 */
- (void)sendCustomPCMData:(unsigned char *)data len:(unsigned int)len;

/**
 * 8.3 自定义音频采集，向 SDK 发送自己采集的音频数据。
 *
 * 相比于 sendCustomPCMData，sendAudioSampleBuffer 主要用于 ReplayKit 录屏推流的场景。
 * 要开启自定义音频采集，需要完成如下两个步骤：
 *
 * 1. 开启自定义采集：给 TXLivePushConfig 中的 customModeType 属性增加 CUSTOM_MODE_AUDIO_CAPTURE 选项，代表开启自定义音频采集。
 * 2. 设定音频采样率：将 TXLivePushConfig 中的 audioSampleRate 属性设置为您期望的音频采样率，audioChannels 设置为期望的声道数，默认值：1（单声道）。
 *
 * 当使用 ReplayKit 做录屏推流时，iOS 的 ReplayKit 接口会回调两种类型的声音数据：
 * - RPSampleBufferTypeAudioApp，也就是要录制的 App 的声音数据。
 * - RPSampleBufferTypeAudioMic，也就是要录制的麦克风的声音数据。
 *
 * 当您通过 sendAudioSampleBuffer 向 SDK 调用各种类型的声音数据时，SDK 内部会进行混流，否则只会发送一路的声音数据。
 *
 * @param sampleBuffer 采集到的声音 sampleBuffer
 * @param sampleBufferType RPSampleBufferTypeAudioApp：ReplayKit 采集到的 App 声音；RPSampleBufferTypeAudioMic：ReplayKit 采集到的麦克风声音。
 */
#if TARGET_OS_IPHONE
- (void)sendAudioSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType;
#endif

/**
 * 8.4 要求 SDK 发送静音数据
 *
 * 该函数配合 sendAudioSampleBuffer 使用，在 InApp 类型录制切后台场合时需要调用，系统屏幕录制不需要。
 *
 * @param muted YES；静音；NO；关闭静音。
 */
- (void)setSendAudioSampleBufferMuted:(BOOL)muted;

/**
 * 8.5 自定义视频处理回调
 *
 * 自定义视频采集和自定义视频处理不能同时开启，与自定义视频采集不同，自定义视频处理依然是由 SDK 采集摄像头的画面，
 * 但 SDK 会通过 TXVideoCustomProcessDelegate（见“TXVideoCustomProcessDelegate.h”）回调将数据回调给您的 App 进行二次加工。
 *
 * 如果要开启自定义视频处理，需要给 TXLivePushConfig 中的 customModeType 属性增加 CUSTOM_MODE_VIDEO_PREPROCESS 选项。
 *
 * @note 出于性能和稳定性考虑，一般不建议开启此特性。
 */
@property(nonatomic,weak) id <TXVideoCustomProcessDelegate> videoProcessDelegate;

/**
 * 8.5 自定义视频处理回调
 *
 * 自定义音频采集和自定义音频处理不能同时开启，与自定义音频采集不同，自定义音频处理依然是由 SDK 采集麦克风的声音，
 * 但 SDK 会通过 TXAudioCustomProcessDelegate（见“TXAudioCustomProcessDelegate.h”）回调将数据回调给您的 App 进行二次加工。
 *
 * 如果要开启自定义音频处理，需要给 TXLivePushConfig 中的 customModeType 属性增加 CUSTOM_MODE_AUDIO_PREPROCESS 选项。
 *
 * @note 出于性能和稳定性考虑，一般不建议开启此特性。
 */
@property(nonatomic,weak) id <TXAudioCustomProcessDelegate> audioProcessDelegate;

/// @}

/////////////////////////////////////////////////////////////////////////////////
//
//                      （九）更多实用接口
//
/////////////////////////////////////////////////////////////////////////////////

/// @name 更多实用接口
/// @{

/**
 * 9.1 发送 SEI 消息，播放端（TXLivePlayer）通过 onPlayEvent（EVT_PLAY_GET_MESSAGE）来接收该消息。
 *
 * 本接口是将数据直接塞入视频数据头中，因此不能太大（几个字节比较合适），一般常用于塞入自定义时间戳等信息。
 *
 * @note - sendMessage 已经不推荐使用，会导致 H5 播放器产生兼容性问题，请使用 sendMessageEx。
 *       - 若您使用过 sendMessage，不推荐立刻升级到 sendMessageEx。
 *       - sendMessageEx 发送消息给旧版本的5.0 及以前的 SDK 版本时，消息会无法正确解析，但播放不受影响。
 */
- (BOOL)sendMessageEx:(NSData *) data;
- (void)sendMessage:(NSData *) data;

/**
 * 9.2 打开包含视频状态信息的调试浮层，该浮层一般用于 SDK 调试期间，外发版本请不要打开。
 */
- (void)showVideoDebugLog:(BOOL)isShow;

/**
 * 9.3 设置调试浮层在视频 view 上的位置。
 */
- (void)setLogViewMargin:(TXEdgeInsets)margin;

/**
 * 9.4 设置推流是否覆盖时钟
 *
 * 开始后可以在 TXLivePlayer 中的仪表盘显示画面延迟时间
 * @note 需要双方的时间相同才能获取准确的延迟时间
 */
- (void)setEnableClockOverlay:(BOOL)enabled;

/**
 * 9.5 获取当前推流画面是否有覆盖时钟
 */
- (BOOL)enableClockOverlay;

/// @}

@end
