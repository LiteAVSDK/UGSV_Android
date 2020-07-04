// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

#define kScaleX [UIScreen mainScreen].bounds.size.width / 375
#define kScaleY [UIScreen mainScreen].bounds.size.height / 667

@interface UIView (UGCKitAdditions)

// Position of the top-left corner in superview's coordinates
@property CGFloat ugckit_x;
@property CGFloat ugckit_y;
@property CGFloat ugckit_top;
@property CGFloat ugckit_bottom;
@property CGFloat ugckit_left;
@property CGFloat ugckit_right;


// Setting size keeps the position (top-left corner) constant
@property CGFloat ugckit_width;
@property CGFloat ugckit_height;

@end

