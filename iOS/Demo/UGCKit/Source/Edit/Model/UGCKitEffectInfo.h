// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

@interface UGCKitEffectInfo : NSObject
@property(nonatomic,strong) UIImage  *icon;
@property(nonatomic,strong) UIImage  *selectIcon;
@property(nonatomic,strong) NSMutableArray  *animateIcons;
@property(nonatomic,assign) BOOL  isSlow;
@property(nonatomic,strong) NSString *name;
@end
