// Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
@protocol UGCKitReporterProtocol <NSObject>
+ (void)report:(NSString *)type userName:(nullable NSString *)userName code:(UInt64)code  msg:(NSString *)msg;
@end

@interface UGCKitReporter : NSObject <UGCKitReporterProtocol>
+ (void)registerReporter:(Class<UGCKitReporterProtocol>)reporter;
@end

NS_ASSUME_NONNULL_END
