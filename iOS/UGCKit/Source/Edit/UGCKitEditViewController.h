// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitMedia.h"
#import "UGCKitResult.h"
#import "UGCKitTheme.h"
#import "UGCKitWatermark.h"

typedef NS_ENUM(NSInteger, TCEditRotation) {
    TCEditRotation0,
    TCEditRotation90,
    TCEditRotation180,
    TCEditRotation270
};

typedef NS_ENUM(NSInteger, UGCKitGenerateMode) {
    UGCKitGenerateModeDefault,
    UGCKitGenerateModeTwoPass
};

enum TXVideoCompressed : NSInteger;

/// 视频编辑参数
@interface UGCKitEditConfig : NSObject
/// 视频的旋转角度
@property (assign, nonatomic) TCEditRotation rotation;
/// 压缩分辨率
@property (assign, nonatomic) enum TXVideoCompressed compressResolution;
/// 视频码率（kbps)，小于0时会自动判断
@property (assign, nonatomic) int videoBitrate;
/// 视频水印
@property (strong, nonatomic) UGCKitWatermark *watermark;
/// 片尾水印
@property (strong, nonatomic) UGCKitWatermark *tailWatermark;
/// 生成模式
@property (assign, nonatomic) UGCKitGenerateMode generateMode;
/// 以默认配置初始化
- (instancetype)init;
@end

/// 视频编辑器
@interface UGCKitEditViewController : UIViewController
@property (assign, nonatomic) UGCKitGenerateMode generateMode;

- (instancetype)initWithMedia:(UGCKitMedia *)asset config:(UGCKitEditConfig *)config theme:(UGCKitTheme *)theme;

/// 点击下一步时的回调，您可以在此进行业务的交互逻辑，如让用户选择是否保存等，
/// 完成后调用 finish(YES) 开始生成视频，finish(NO)来取取消操作
/// 视频生成的结果会通过 completion 回调。
/// 如果不设置此回调，会直接生成视频并通过 completion 进行回调。
@property (copy, nonatomic) void(^onTapNextButton)(void(^finish)(BOOL shouldGenerate));
/// 视频生成完成回调
@property (copy, nonatomic) void(^completion)(UGCKitResult *result);

@end
