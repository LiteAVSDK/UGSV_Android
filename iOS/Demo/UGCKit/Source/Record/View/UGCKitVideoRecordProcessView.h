// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"

@interface UGCKitVideoRecordProcessView : UIView
@property (assign, nonatomic) BOOL minimumTimeTipHidden;
-(instancetype)initWithTheme:(UGCKitTheme *)theme
                       frame:(CGRect)frame
                 minDuration:(NSTimeInterval)minDuration
                 maxDuration:(NSTimeInterval)maxDuration;

- (void)setMinDuration:(NSTimeInterval)minDuration
           maxDuration:(NSTimeInterval)maxDuration;

-(void)update:(CGFloat)progress;

-(void)pause;

-(void)pauseAtTime:(CGFloat)time;

-(void)prepareDeletePart;

-(void)cancelDelete;

-(void)comfirmDeletePart;

-(void)deleteAllPart;
@end
