// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

@interface TCBGMProgressView : UIView
@property (strong, nonatomic, readonly) UILabel *label;
@property (assign, nonatomic) float progress;
@property (strong, nonatomic) UIColor *progressBackgroundColor;
@end
