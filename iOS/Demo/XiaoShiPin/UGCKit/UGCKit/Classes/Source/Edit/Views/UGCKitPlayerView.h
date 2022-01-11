// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"

@protocol VideoPreviewDelegate <NSObject>
@optional
- (void)onVideoPlay;
- (void)onVideoPause;
- (void)onVideoResume;
- (void)onVideoPlayProgress:(CGFloat)time;
- (void)onVideoPlayFinished;

@optional
- (void)onVideoEnterBackground;
- (void)onVideoWillEnterForeground;
@end

@interface UGCKitPlayerView : UIView

@property(nonatomic,weak) id<VideoPreviewDelegate> delegate;
@property(nonatomic,strong) UIView *renderView;
@property(nonatomic, readonly, assign) BOOL isPlaying;


- (instancetype)initWithFrame:(CGRect)frame coverImage:(UIImage *)image theme:(UGCKitTheme *)theme;


- (void)setPlayBtnHidden:(BOOL)isHidden;

- (void)setPlayBtn:(BOOL)videoIsPlay;

- (void)playVideo;

- (void)removeNotification;
- (void)stopObservingAudioNotification;
- (void)startObservingAudioNotification;

@end
