
//
//  WHPingTester.h
//  BigVPN
//
//  Created by wanghe on 2017/5/11.
//  Copyright © 2017年 wanghe. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "QCloudSimplePing.h"
@class QCloudPingTester;
@protocol WHPingDelegate <NSObject>
@optional
- (void)pingTester:(QCloudPingTester *)pingTest didPingSucccessWithTime:(float)time withError:(NSError *)error;
@end

@interface QCloudPingTester : NSObject <SimplePingDelegate>
@property (nonatomic, weak, readwrite) id<WHPingDelegate> delegate;
@property (nonatomic, readonly) NSString *ip;
@property (nonatomic, readonly) NSString *host;
@property (nonatomic) dispatch_semaphore_t sema;
- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithIp:(NSString *)ip host:(NSString *)host fulfil:(dispatch_semaphore_t)sema NS_DESIGNATED_INITIALIZER;

- (void)startPing;
- (void)stopPing;
@end

typedef NS_ENUM(NSUInteger, WHPingStatus) {
    WHPingStatusSending = 0 << 0,
    WHPingStatusTimeout = 1 << 1,
    WHPingStatusSended = 2 << 2,
};

@interface QCloudPingItem : NSObject
//@property(nonatomic, assign) WHPingStatus status;
@property (nonatomic, assign) uint16_t sequence;

@end
