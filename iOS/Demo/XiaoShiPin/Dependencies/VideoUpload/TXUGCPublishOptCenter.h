//
//  TXUGCPublishOptCenter.h
//  TXLiteAVDemo
//
//  Created by carolsuo on 2018/8/24.
//  Copyright © 2018年 Tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TXUGCPublishOptCenter : NSObject

+ (instancetype)shareInstance;
@property (atomic, assign)  BOOL isStarted;
@property (strong, nonatomic) NSString * signature;
@property (strong, nonatomic) NSMutableDictionary *cacheMap;
@property (strong, nonatomic) NSMutableDictionary *fixCacheMap;
@property (strong, nonatomic) NSMutableDictionary *publishingList;
@property (strong, nonatomic) NSString *bestCosRegion;
@property (strong, nonatomic) NSString *bestCosDomain;
@property (nonatomic, assign) UInt64 minCosRespTime;

- (void)prepareUpload:(NSString *)signature;
- (NSArray *)query:(NSString *)hostname;
- (NSString *)getCosRegion;
- (BOOL)useProxy;
- (BOOL)useHttpDNS:(NSString *)hostname;
- (void)addPublishing:(NSString *)videoPath;
- (void)delPublishing:(NSString *)videoPath;
- (BOOL)isPublishingPublishing:(NSString *)videoPath;

@end
