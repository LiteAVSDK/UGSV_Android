// Copyright (c) 2019 Tencent. All rights reserved.

#ifndef UGCKitPituMotionManager_h
#define UGCKitPituMotionManager_h

#import <Foundation/Foundation.h>
@interface UGCKitPituMotion : NSObject
@property (readonly, nonatomic) NSString *identifier;
@property (readonly, nonatomic) NSString *name;
@property (readonly, nonatomic) NSURL *url;
- (instancetype)initWithId:(NSString *)identifier name:(NSString *)name url:(NSString *)address;
@end

@interface UGCKitPituMotionManager : NSObject
@property (readonly, nonatomic) NSArray<UGCKitPituMotion *> * motionPasters;
@property (readonly, nonatomic) NSArray<UGCKitPituMotion *> * cosmeticPasters;
@property (readonly, nonatomic) NSArray<UGCKitPituMotion *> * gesturePasters;
@property (readonly, nonatomic) NSArray<UGCKitPituMotion *> * backgroundRemovalPasters;

+ (instancetype)sharedInstance;
- (UGCKitPituMotion *)motionWithIdentifier:(NSString *)identifier;

@end

#endif /* Header_h */
