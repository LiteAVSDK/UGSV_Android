// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface UGCKitMedia : NSObject
@property (readonly, nonatomic) CGSize size;
@property (readonly, nonatomic) BOOL isVideo;
@property (readonly, nonatomic) AVAsset *videoAsset;
@property (readonly, nonatomic) NSString *videoPath;
@property (readonly, nonatomic) NSArray<UIImage *> *images;

+ (instancetype)mediaWithVideoPath:(NSString *)path;
+ (instancetype)mediaWithAVAsset:(AVAsset *)asset;
+ (instancetype)mediaWithImages:(NSArray<UIImage *> *)images canvasSize:(CGSize)canvasSize;

@end

NS_ASSUME_NONNULL_END
