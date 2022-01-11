// Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>
#import "UGCKitEffectInfo.h"
#import "UGCKitTheme.h"

NS_ASSUME_NONNULL_BEGIN

@interface UGCKitVideoEffectManager : NSObject
+ (NSArray<UGCKitEffectInfo *> *)effectInfosWithTheme:(UGCKitTheme *)theme;
@end

NS_ASSUME_NONNULL_END
