// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

@interface UGCKitBGMProgressView : UIView
@property (strong, nonatomic, readonly) UILabel *label;
@property (assign, nonatomic) float progress;
@property (strong, nonatomic) UIColor *progressBackgroundColor;
- (instancetype)initWithFrame:(CGRect)frame bgImage:(UIImage *)bgImage;
@end
