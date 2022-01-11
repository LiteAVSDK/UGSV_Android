//
//  TXUGCPublishOptCenter.m
//  TXLiteAVDemo
//
//  Created by carolsuo on 2018/8/24.
//  Copyright © 2018年 Tencent. All rights reserved.
//

#import "TXUGCPublishOptCenter.h"
#import <Foundation/Foundation.h>
#import <SystemConfiguration/CaptiveNetwork.h>
#import "AFNetworkReachabilityManager.h"
#import "TVCClientInner.h"
#import "TVCCommon.h"
#import "TVCReport.h"

#define HTTPDNS_SERVER @"https://119.29.29.99/d?dn="  // httpdns服务器
#define HTTPDNS_TOKEN @"800654663"

typedef void (^TXUGCCompletion)(int result);
typedef void (^TXUGCHttpCompletion)(NSData *_Nullable data, int errCode);

static TXUGCPublishOptCenter *_shareInstance = nil;

@implementation TXUGCCosRegionInfo

- (instancetype)init {
    self = [super init];
    if (self) {
        _region = @"";
        _domain = @"";
    }
    return self;
}

@end

@implementation TXUGCPublishOptCenter

+ (instancetype)shareInstance {
    static dispatch_once_t predicate;

    dispatch_once(&predicate, ^{
      _shareInstance = [[TXUGCPublishOptCenter alloc] init];
    });
    return _shareInstance;
}

- (instancetype)init {
    if (self = [super init]) {
        _cacheMap = [[NSMutableDictionary alloc] init];
        _fixCacheMap = [[NSMutableDictionary alloc] init];
        _publishingList = [[NSMutableDictionary alloc] init];
        _isStarted = NO;
        _signature = @"";
        _cosRegionInfo = [[TXUGCCosRegionInfo alloc] init];
        [self monitorNetwork];
    }
    return self;
}

- (void)prepareUpload:(NSString *)signature
    prepareUploadComplete:(TXUGCPrepareUploadCompletion)prepareUploadComplete {
    _signature = signature;
    Boolean ret = false;
    if (!_isStarted) {
        ret = [self reFresh:prepareUploadComplete];
    }
    if (ret) {
        _isStarted = YES;
    } else {
        if (prepareUploadComplete) {
            prepareUploadComplete();
        }
    }
}

- (void)updateSignature:(NSString *)signature {
    _signature = signature;
}

//刷新httpdns
- (Boolean)reFresh:(TXUGCPrepareUploadCompletion)prepareUploadComplete {
    @synchronized(_cosRegionInfo) {
        _minCosRespTime = 0;
        _cosRegionInfo.domain = @"";
        _cosRegionInfo.region = @"";
    }

    if (_signature == nil || _signature.length == 0) {
        return false;
    }
    //清掉dns缓存
    [_cacheMap removeAllObjects];
    [_fixCacheMap removeAllObjects];

    //使用了代理，不走httpdns
    if ([self useProxy]) {
        return false;
    }

    uint64_t reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
    __weak __typeof(self) weakSelf = self;
    [self
        freshDomain:UGC_HOST
         completion:^(int result) {
           __strong __typeof(weakSelf) self = weakSelf;
           if (self) {
               [self
                   reportPublishOptResult:TVC_UPLOAD_EVENT_ID_REQUEST_VOD_DNS_RESULT
                                  errCode:result
                                   errMsg:@""
                                  reqTime:reqTime
                              reqTimeCost:([[NSDate date] timeIntervalSince1970] * 1000 - reqTime)];

               [self prepareUploadUGC];
           }

           if (prepareUploadComplete) {
               prepareUploadComplete();
           }
         }];
    return true;
}

- (void)freshDomain:(NSString *)domain completion:(TXUGCCompletion)completion {
    NSString *reqUrl  = [HTTPDNS_SERVER stringByAppendingFormat:@"%@%@%@", domain, @"&token=", HTTPDNS_TOKEN];

    __weak __typeof(self) weakSelf = self;

    [self sendHttpRequest:reqUrl
                   method:@"GET"
                     body:nil
               completion:^(NSData *_Nullable data, int errCode) {
                 __strong __typeof(weakSelf) self = weakSelf;
                 if (self == nil) {
                     if (completion) {
                         completion(-1);
                     }
                     return;
                 }

                 if (data == nil) {
                     if (completion) {
                         completion(-1);
                     }
                     return;
                 }

                 NSString *ips = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
                 NSLog(@"httpdns domain[%@] ips[%@]", domain, ips);

                 NSArray *ipLists = [ips componentsSeparatedByString:@";"];
                 [self.cacheMap setValue:ipLists forKey:domain];

                 if (completion) {
                     completion(errCode);
                 }
               }];
}

//监控网络接入变化
- (void)monitorNetwork {
    //网络切换的时候刷新一下httpdns
    [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        switch (status) {
            case AFNetworkReachabilityStatusUnknown:
                NSLog(@"Unknown");
                break;
            case AFNetworkReachabilityStatusNotReachable:
                NSLog(@"%No network");
                break;
            case AFNetworkReachabilityStatusReachableViaWWAN:
                NSLog(@"3G|4G");
                [self reFresh:nil];
                break;
            case AFNetworkReachabilityStatusReachableViaWiFi:
                NSLog(@"WiFi");
                [self reFresh:nil];
                break;
            default:
                break;
        }

    }];
}

// 添加指定域名的ip列表，ip列表是后台返回的
- (void)addDomainDNS:(NSString *)domain ipLists:(NSArray *)ipLists {
    if ([self useProxy]) {
        return;
    }

    if ([ipLists count] == 0) {
        return;
    }

    [_fixCacheMap setValue:ipLists forKey:domain];
}

// 获取指定域名对应的ipLists
- (NSArray *)query:(NSString *)hostname {
    if ([[_cacheMap objectForKey:hostname] count] > 0) {
        return [_cacheMap objectForKey:hostname];
    } else if ([[_fixCacheMap objectForKey:hostname] count] > 0) {
        return [_fixCacheMap objectForKey:hostname];
    }

    return nil;
}

- (NSString *)getCosRegion {
    return _cosRegionInfo.region;
}

//是否使用了代理
- (BOOL)useProxy {
    CFDictionaryRef dicRef = CFNetworkCopySystemProxySettings();
    if (NULL == dicRef) return NO;

    const CFStringRef proxyCFstr =
        (const CFStringRef)CFDictionaryGetValue(dicRef, (const void *)kCFNetworkProxiesHTTPProxy);
    NSString *proxy = (__bridge NSString *)proxyCFstr;
    CFRelease(dicRef);
    if (proxy != nil) {
        //使用了代理
        return YES;
    }
    //没有使用代理
    return NO;
}

//是否使用了httpdns
- (BOOL)useHttpDNS:(NSString *)hostname {
    if ([self query:hostname] != nil) {
        return YES;
    }
    return NO;
}

- (void)addPublishing:(NSString *)videoPath {
    [_publishingList setValue:[NSNumber numberWithBool:YES] forKey:videoPath];
}

- (void)delPublishing:(NSString *)videoPath {
    [_publishingList removeObjectForKey:videoPath];
}

- (BOOL)isPublishingPublishing:(NSString *)videoPath {
    return [[_publishingList objectForKey:videoPath] boolValue];
}

// 预上传（UGC接口）
- (void)prepareUploadUGC {
    NSString *reqUrl =
        [NSString stringWithFormat:@"https://%@/v3/index.php?Action=PrepareUploadUGC", UGC_HOST];
    NSLog(@"prepareUploadUGC reqUrl[%@]", reqUrl);

    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    [dic setValue:TVCVersion forKey:@"clientVersion"];
    [dic setValue:_signature forKey:@"signature"];

    NSError *error = nil;
    NSData *body = [NSJSONSerialization dataWithJSONObject:dic options:0 error:&error];
    if (error) {
        return;
    }
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    uint64_t reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
    __weak __typeof(self) weakSelf = self;
    [self sendHttpRequest:reqUrl
                   method:@"POST"
                     body:body
               completion:^(NSData *_Nullable data, int errCode) {
                 __strong __typeof(weakSelf) self = weakSelf;
                 if (self) {
                     [self reportPublishOptResult:TVC_UPLOAD_EVENT_ID_REQUEST_PREPARE_UPLOAD_RESULT
                                          errCode:errCode
                                           errMsg:@""
                                          reqTime:reqTime
                                      reqTimeCost:([[NSDate date] timeIntervalSince1970] * 1000 -
                                                   reqTime)];
                     [self parsePrepareUploadRsp:data];
                 }
                 dispatch_semaphore_signal(semaphore);
               }];
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
}

- (void)parsePrepareUploadRsp:(NSData *)rspData {
    if (rspData == nil) {
        return;
    }

    NSError *error = nil;
    id ret = [NSJSONSerialization JSONObjectWithData:rspData
                                             options:NSJSONReadingAllowFragments
                                               error:&error];
    if (error || !ret || ![ret isKindOfClass:[NSDictionary class]]) {
        return;
    }

    NSDictionary *dic = ret;
    NSLog(@"parsePrepareUploadRsp rspData[%@]", dic);

    int code = -1;
    if ([[dic objectForKey:@"code"] isKindOfClass:[NSNumber class]]) {
        code = [[dic objectForKey:@"code"] intValue];
    }
    if (code != 0) {
        return;
    }

    NSArray *cosArray = nil;
    if ([[dic objectForKey:@"data"] isKindOfClass:[NSDictionary class]]) {
        NSDictionary *data = [dic objectForKey:@"data"];
        if (data && [[data objectForKey:@"cosRegionList"] isKindOfClass:[NSArray class]]) {
            cosArray = [data objectForKey:@"cosRegionList"];
        }
    }

    if (cosArray == nil || cosArray.count <= 0) {
        NSLog(@"parsePrepareUploadRsp cosRegionList is null!");
        return;
    }

    // 最多并发4个请求
    int maxThreadCount = MIN(4, (int)cosArray.count);
    NSOperationQueue *operationQueue = [[NSOperationQueue alloc] init];
    operationQueue.maxConcurrentOperationCount = maxThreadCount;

    uint64_t reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
    __weak __typeof(self) weakSelf = self;
    for (int i = 0; i < cosArray.count; ++i) {
        if ([cosArray[i] isKindOfClass:[NSDictionary class]]) {
            NSDictionary *cosInfo = cosArray[i];
            [operationQueue addOperationWithBlock:^{
              NSString *region = (NSString *)[cosInfo objectForKey:@"region"];
              NSString *domain = (NSString *)[cosInfo objectForKey:@"domain"];
              NSString *ips = (NSString *)[cosInfo objectForKey:@"ip"];
              __strong __typeof(weakSelf) self = weakSelf;
              if (self) {
                  if (region.length > 0 && domain.length > 0) {
                      [self getCosDNS:domain ips:ips];
                      [self detectBestCosIP:domain region:region];
                  }
              }
            }];
        }
    }

    [operationQueue waitUntilAllOperationsAreFinished];
    Boolean isRegionEmpty = (self.cosRegionInfo.region == nil);
    NSString *errMsg =
        (isRegionEmpty ? @""
                       : [NSString stringWithFormat:@"%@|%@", self.cosRegionInfo.region,
                                                    self.cosRegionInfo.domain]);
    [self reportPublishOptResult:TVC_UPLOAD_EVENT_ID_DETECT_DOMAIN_RESULT
                         errCode:(isRegionEmpty ? 1 : 0)errMsg:errMsg
                         reqTime:reqTime
                     reqTimeCost:([[NSDate date] timeIntervalSince1970] * 1000 - reqTime)];
}

// 发送head请求探测
- (void)detectBestCosIP:(NSString *)domain region:(NSString *)region {
    NSString *reqUrl = [NSString stringWithFormat:@"http://%@", domain];
    NSLog(@"detectDomain reqUrl[%@]", reqUrl);

    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    __weak __typeof(self) weakSelf = self;

    UInt64 beginTs = (UInt64)([[NSDate date] timeIntervalSince1970] * 1000);
    [self sendHttpRequest:reqUrl
                   method:@"HEAD"
                     body:nil
               completion:^(NSData *_Nullable data, int errCode) {
                 __strong __typeof(weakSelf) self = weakSelf;
                 if (self != nil) {
                     if (errCode == 0) {
                         UInt64 endTs = (UInt64)([[NSDate date] timeIntervalSince1970] * 1000);
                         UInt64 cosTs = (endTs - beginTs);
                         NSLog(@"detectBestCosIP domain = %@, result = %d, timeCos = %llu", domain, errCode, cosTs);
                         @synchronized(self->_cosRegionInfo) {
                             if (self.minCosRespTime == 0 || cosTs < self.minCosRespTime) {
                                 self.minCosRespTime = cosTs;
                                 self.cosRegionInfo.region = region;
                                 self.cosRegionInfo.domain = domain;
                                 NSLog(@"detectBestCosIP bestCosDomain = %@, bestCosRegion = %@, timeCos = %llu", domain, region, cosTs);
                             }
                         }
                     }
                 }
                 dispatch_semaphore_signal(semaphore);
               }];

    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
}

- (void)getCosDNS:(NSString *)domain ips:(NSString *)ips {
    //返回的ip列表为空，首先执行httpdns
    if (ips.length == 0) {
        dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
        [self freshDomain:domain
               completion:^(int result) {
                 dispatch_semaphore_signal(semaphore);
               }];
        dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    } else {
        NSArray *ipLists = [ips componentsSeparatedByString:@";"];
        [self addDomainDNS:domain ipLists:ipLists];
    }
}

// 简单包装http请求
- (void)sendHttpRequest:(NSString *)reqUrl
                 method:(NSString *)method
                   body:(NSData *)body
             completion:(TXUGCHttpCompletion)completion {
    // create request
    NSURL *url = [NSURL URLWithString:reqUrl];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = method;
    if (body != nil) {
        [request setValue:[NSString stringWithFormat:@"%ld", (long)[body length]]
            forHTTPHeaderField:@"Content-Length"];
        [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
        [request setHTTPBody:body];
    }

    NSURLSessionConfiguration *initCfg = [NSURLSessionConfiguration defaultSessionConfiguration];
    [initCfg setTimeoutIntervalForRequest:5];

    NSURLSession *session = [NSURLSession sessionWithConfiguration:initCfg
                                                          delegate:nil
                                                     delegateQueue:nil];
    __weak NSURLSession *wis = session;

    NSURLSessionTask *dnsTask =
        [session dataTaskWithRequest:request
                   completionHandler:^(NSData *_Nullable data, NSURLResponse *_Nullable response,
                                       NSError *_Nullable error) {
                     // invalid NSURLSession
                     [wis invalidateAndCancel];

                     if (error) {
                         if (completion) {
                             completion(nil, (int)error.code);
                         }
                         return;
                     }

                     if (completion) {
                         completion(data, 0);
                     }
                   }];
    [dnsTask resume];
}

- (void)reportPublishOptResult:(int)reqType
                       errCode:(int)errCode
                        errMsg:(NSString *)errMsg
                       reqTime:(uint64_t)reqTime
                   reqTimeCost:(uint64_t)reqTimeCost {
    TVCReportInfo *reportInfo = [[TVCReportInfo alloc] init];
    reportInfo.reqType = reqType;
    reportInfo.errCode = errCode;
    reportInfo.errMsg = errMsg;
    reportInfo.reqTime = reqTime;
    reportInfo.reqTimeCost = reqTimeCost;

    [[TVCReport shareInstance] addReportInfo:reportInfo];
}

@end
