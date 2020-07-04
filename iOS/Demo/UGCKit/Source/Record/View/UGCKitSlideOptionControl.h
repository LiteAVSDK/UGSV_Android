// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"
NS_ASSUME_NONNULL_BEGIN

@interface UGCKitSlideOptionControl : UIControl
- (instancetype)initWithFrame:(CGRect)frame theme:(UGCKitTheme*)theme options:(NSArray<NSString *> *)options;
@property (nonatomic, assign) NSUInteger selectedIndex;
@property (nonatomic, strong) NSIndexSet *disabledIndexes;
@end

NS_ASSUME_NONNULL_END
