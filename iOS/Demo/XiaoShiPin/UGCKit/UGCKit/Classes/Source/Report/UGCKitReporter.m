// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitReporter.h"

#define kHttpTimeout 30

static Class gReporter = Nil;

@implementation UGCKitReporter
+ (void)registerReporter:(Class<UGCKitReporterProtocol>)reporter {
    gReporter = reporter;
}

+ (void)report:(NSString *)type userName:(nullable NSString *)userName code:(UInt64)code msg:(NSString *)msg
{
    [gReporter report:type userName:userName code:code msg:msg];
}

@end
