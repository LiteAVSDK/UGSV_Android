// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitRangeContent.h"

typedef NS_ENUM(NSInteger,UGCKitRangeColorType){
    UGCKitRangeColorType_Cut,
    UGCKitRangeColorType_Effect,
    UGCKitRangeColorType_Time,
    UGCKitRangeColorType_Filter,
    UGCKitRangeColorType_Paster,
    UGCKitRangeColorType_Text
};

/**
 视频缩略条拉条
 */
@interface UGCKitVideoColorInfo : NSObject
@property (nonatomic,strong) UIView *colorView;
@property (nonatomic,assign) CGFloat startPos;
@property (nonatomic,assign) CGFloat endPos;
@property (nonatomic,assign) UGCKitRangeColorType UGCKitRangeColorType;
@end

@protocol VideoRangeSliderDelegate;

@interface UGCKitVideoRangeSlider : UIView

@property (weak) id<VideoRangeSliderDelegate> delegate;

@property (nonatomic) UIScrollView  *bgScrollView;
@property (nonatomic) UIImageView   *middleLine;
@property (nonatomic) UGCKitRangeContentConfig* appearanceConfig;
@property (nonatomic) UGCKitRangeContent *rangeContent;
@property (nonatomic) CGFloat        durationMs;
@property (nonatomic) CGFloat        currentPos;
@property (readonly)  CGFloat        leftPos;
@property (readonly)  CGFloat        rightPos;
@property (readonly)  CGFloat        centerPos;
@property (nonatomic) float          fps;

- (void)setAppearanceConfig:(UGCKitRangeContentConfig *)appearanceConfig;
- (void)setImageList:(NSArray *)images;
- (void)updateImage:(UIImage *)image atIndex:(NSUInteger)index;
- (void)setSelectColorInfo:(NSInteger)selectedIndex;

- (void)setLeftPanHidden:(BOOL)isHidden;
- (void)setCenterPanHidden:(BOOL)isHidden;
- (void)setRightPanHidden:(BOOL)isHidden;

- (void)setLeftPanFrame:(CGFloat)time;
- (void)setCenterPanFrame:(CGFloat)time;
- (void)setRightPanFrame:(CGFloat)time;

//左右滑块选择涂色
- (void)setColorType:(UGCKitRangeColorType)UGCKitRangeColorType;
- (void)startColoration:(UIColor *)color alpha:(CGFloat)alpha;
- (void)stopColoration;

//删除一段涂色
- (UGCKitVideoColorInfo *)removeLastColoration:(UGCKitRangeColorType)UGCKitRangeColorType;
- (void)removeColoration:(UGCKitRangeColorType)UGCKitRangeColorType index:(NSInteger)index;
@end


@protocol VideoRangeSliderDelegate <NSObject>
- (void)onVideoRangeTap:(CGFloat)tapTime;
- (void)onVideoRangeLeftChanged:(UGCKitVideoRangeSlider *)sender;
- (void)onVideoRangeLeftChangeEnded:(UGCKitVideoRangeSlider *)sender;
- (void)onVideoRangeCenterChanged:(UGCKitVideoRangeSlider *)sender;
- (void)onVideoRangeCenterChangeEnded:(UGCKitVideoRangeSlider *)sender;
- (void)onVideoRangeRightChanged:(UGCKitVideoRangeSlider *)sender;
- (void)onVideoRangeRightChangeEnded:(UGCKitVideoRangeSlider *)sender;
- (void)onVideoRangeLeftAndRightChanged:(UGCKitVideoRangeSlider *)sender;
- (void)onVideoRange:(UGCKitVideoRangeSlider *)sender seekToPos:(CGFloat)pos;
@end
