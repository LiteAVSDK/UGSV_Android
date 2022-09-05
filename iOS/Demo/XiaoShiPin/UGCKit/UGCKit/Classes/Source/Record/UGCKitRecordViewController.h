// Copyright (c) 2019 Tencent. All rights reserved.
#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"
#import "UGCKitWatermark.h"
#import "UGCKitResult.h"
enum TXVideoAspectRatio : NSInteger;
enum TXVideoResolution : NSInteger;
enum TXAudioSampleRate : NSInteger;

typedef NS_ENUM(NSUInteger, UGCKitRecordStyle) {
    UGCKitRecordStyleRecord,    /// 纯录制模式
    UGCKitRecordStyleDuet,      /// 分屏合拍模式
    UGCKitRecordStyleTrio       /// 三屏合拍模式
};

@interface UGCKitRecordConfig : NSObject
/// 画面比例，默认为9:16
@property (assign, nonatomic) enum TXVideoAspectRatio ratio;
/// 分辨率，默认为720p
@property (assign, nonatomic) enum TXVideoResolution resolution;
/// 比特率，默认为9600
@property (assign, nonatomic) int videoBitrate;
///音频采样率
@property (assign, nonatomic) enum TXAudioSampleRate audioSampleRate;
/// 最小时长，单位秒，默认为5
@property (assign, nonatomic) float minDuration;
/// 最长时长，单位秒，默认为30
@property (assign, nonatomic) float maxDuration;
/// 每秒帧数，默认为15
@property (assign, nonatomic) int fps;
/// 关键帧间隔，单位秒，默认为3
@property (assign, nonatomic) int gop;
/// 视频水印
@property (strong, nonatomic) UGCKitWatermark *watermark;
/// 合唱视频
@property (strong, nonatomic) NSArray<NSString *> *chorusVideos;
/// 合拍模式（纯录制 / 分屏合拍 / 三屏合拍），默认是纯录制模式
@property (assign, nonatomic) UGCKitRecordStyle recordStyle;
/// 是否开启回声消除, 默认开启
@property (assign, nonatomic) BOOL AECEnabled;
/// 是否载入草稿
@property (assign, nonatomic) BOOL recoverDraft;
/// 录满时长后是否直接调用complete回调, 默认为YES
@property (assign, nonatomic) BOOL autoComplete;
@end


/// 短视频录制VC
@interface UGCKitRecordViewController : UIViewController
- (instancetype)initWithConfig:(UGCKitRecordConfig *)config theme:(UGCKitTheme *)theme;
@property (nonatomic,copy) void (^completion)(UGCKitResult *result);
@property (assign, nonatomic) int heightF;
@end
