// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// 水印
@interface UGCKitWatermark : NSObject
/// 图片
@property (strong, nonatomic) UIImage *image;
/// 位置
@property (assign, nonatomic) CGRect frame;
/// 时长（仅在设置片尾水印时有效）
@property (assign, nonatomic) NSTimeInterval duration;

/// 实例化水印对象
+ (instancetype)watermarkWithImage:(UIImage *)image frame:(CGRect)frame duration:(NSTimeInterval)duration;
@end

NS_ASSUME_NONNULL_END
