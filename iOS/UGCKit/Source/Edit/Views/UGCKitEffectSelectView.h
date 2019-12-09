// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "SDKHeader.h"
#import "UGCKitEffectInfo.h"

@protocol UGCKitEffectSelectViewDelegate <NSObject>
-(void)onEffectBtnBeginSelect:(UIButton *)btn;
-(void)onEffectBtnEndSelect:(UIButton *)btn;
-(void)onEffectBtnSelected:(UIButton *)btn;
@end

@interface UGCKitEffectSelectView : UIView
@property (nonatomic,weak) id <UGCKitEffectSelectViewDelegate> delegate;
@property (assign, nonatomic) NSInteger selectedIndex;
/// 抬起手指时是否还原未选中状态
@property (nonatomic) BOOL momentary;
- (void)setEffectList:(NSArray<UGCKitEffectInfo *> *)effecList;
- (void)setEffectList:(NSArray<UGCKitEffectInfo *> *)effecList momentary:(BOOL)momentary;
@end
