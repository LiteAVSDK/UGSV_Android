// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"

//TXTransitionType_LefRightSlipping,     //左右滑动
//TXTransitionType_UpDownSlipping,       //上下滑动
//TXTransitionType_Enlarge,              //放大
//TXTransitionType_Narrow,               //缩小
//TXTransitionType_RotationalScaling,    //旋转缩放
//TXTransitionType_FadeinFadeout,        //淡入淡出

@protocol TransitionViewDelegate <NSObject>
- (void)onVideoTransitionLefRightSlipping;
- (void)onVideoTransitionUpDownSlipping;
- (void)onVideoTransitionEnlarge;
- (void)onVideoTransitionNarrow;
- (void)onVideoTransitionRotationalScaling;
- (void)onVideoTransitionFadeinFadeout;
@end

@interface UGCKitPhotoTransitionToolbar : UIView
@property(nonatomic,weak) id<TransitionViewDelegate> delegate;
@property(nonatomic, strong) UGCKitTheme *theme;
- (instancetype)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme;
@end
