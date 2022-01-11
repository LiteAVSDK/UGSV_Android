// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitResult.h"

@implementation UGCKitResult
+ (instancetype)cancelledResult
{
    UGCKitResult *result = [[UGCKitResult alloc] init];
    result.cancelled = YES;
    return result;
}
@end
