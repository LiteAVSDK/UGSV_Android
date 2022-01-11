// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitMedia.h"
NS_ASSUME_NONNULL_BEGIN

@interface UGCKitResult : NSObject
@property (assign, nonatomic, getter=isCancelled) BOOL cancelled;
@property (assign, nonatomic) NSInteger code;
@property (strong, nonatomic, nullable) NSDictionary *info;
@property (strong, nonatomic, nullable) UGCKitMedia  *media;
@property (strong, nonatomic, nullable) UIImage   *coverImage;
+ (instancetype)cancelledResult;
@end

NS_ASSUME_NONNULL_END
