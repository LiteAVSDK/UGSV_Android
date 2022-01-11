// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"
#import "SDKHeader.h"

NS_ASSUME_NONNULL_BEGIN

@protocol UGCKitAudioEffectPanelDelegate;

@interface UGCKitAudioEffectPanel : UIView
@property (nullable, weak, nonatomic) id<UGCKitAudioEffectPanelDelegate> delegate;
- (instancetype)initWithTheme:(UGCKitTheme*)theme frame:(CGRect)frame;
@end

@protocol UGCKitAudioEffectPanelDelegate <NSObject>
@optional
- (void)audioEffectPanel:(UGCKitAudioEffectPanel *)panel didSelectReverbType:(TXVideoReverbType)index;
- (void)audioEffectPanel:(UGCKitAudioEffectPanel *)panel didSelectVoiceChangerType:(TXVideoVoiceChangerType)index;
@end

NS_ASSUME_NONNULL_END
