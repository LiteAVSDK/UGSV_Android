// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"

@protocol UGCKitVideoRecordMusicViewDelegate <NSObject>
-(void)onBtnMusicSelected;
-(void)onBtnMusicStoped;
-(void)onBGMValueChange:(CGFloat)percent;
-(void)onVoiceValueChange:(CGFloat)percent;
-(void)onBGMRangeChange:(CGFloat)startPercent endPercent:(CGFloat)endPercent;

@end

@interface UGCKitVideoRecordMusicView : UIView
@property(nonatomic,weak) id<UGCKitVideoRecordMusicViewDelegate> delegate;
-(instancetype)initWithFrame:(CGRect)frame needEffect:(BOOL)needEffect theme:(UGCKitTheme *)theme;
-(void)resetVolume;
-(void)resetCutView;
-(void)resetSiderView;
-(void)freshCutView:(CGFloat)duration;
@end
