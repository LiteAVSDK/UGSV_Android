// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "UGCKitVideoRangeSlider.h"

/**
 视频编辑的裁剪view
 */

@protocol VideoCutViewDelegate <NSObject>
@optional
- (void)onVideoRangeTap:(CGFloat)tapTime;

- (void)onVideoRangeLeftChanged:(UGCKitVideoRangeSlider*)sender;
- (void)onVideoRangeLeftChangeEnded:(UGCKitVideoRangeSlider*)sender;

- (void)onVideoRangeCenterChanged:(UGCKitVideoRangeSlider*)sender;
- (void)onVideoRangeCenterChangeEnded:(UGCKitVideoRangeSlider*)sender;

- (void)onVideoRangeRightChanged:(UGCKitVideoRangeSlider*)sender;
- (void)onVideoRangeRightChangeEnded:(UGCKitVideoRangeSlider*)sender;

- (void)onVideoSeekChange:(UGCKitVideoRangeSlider*)sender seekToPos:(CGFloat)pos;
@end

@interface UGCKitVideoCutView : UIView

@property (nonatomic, strong)  UGCKitVideoRangeSlider *videoRangeSlider;  //缩略图条
@property (nonatomic, weak) id<VideoCutViewDelegate> delegate;
@property (nonatomic, strong)  NSMutableArray  *imageList;         //缩略图列表

- (id)initWithFrame:(CGRect)frame videoPath:(NSString *)videoPath  videoAsset:(AVAsset *)videoAsset config:(UGCKitRangeContentConfig *)config;
- (id)initWithFrame:(CGRect)frame pictureList:(NSArray *)pictureList  duration:(CGFloat)duration fps:(float)fps config:(UGCKitRangeContentConfig *)config;
- (void)updateFrame:(CGFloat)duration;
- (void)stopGetImageList;
- (void)setSelectColorInfo:(NSInteger)selectedIndex;

- (void)setPlayTime:(CGFloat)time;

- (void)setLeftPanHidden:(BOOL)isHidden;
- (void)setCenterPanHidden:(BOOL)isHidden;
- (void)setRightPanHidden:(BOOL)isHidden;

- (void)setLeftPanFrame:(CGFloat)time;
- (void)setCenterPanFrame:(CGFloat)time;
- (void)setRightPanFrame:(CGFloat)time;

- (void)setColorType:(UGCKitRangeColorType)UGCKitRangeColorType;
- (void)startColoration:(UIColor *)color alpha:(CGFloat)alpha;
- (void)stopColoration;

- (UGCKitVideoColorInfo *)removeLastColoration:(UGCKitRangeColorType)UGCKitRangeColorType;
- (void)removeColoration:(UGCKitRangeColorType)UGCKitRangeColorType index:(NSInteger)index;
@end
