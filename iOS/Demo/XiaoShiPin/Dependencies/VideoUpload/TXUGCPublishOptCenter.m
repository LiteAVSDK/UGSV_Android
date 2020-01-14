//
//  TXUGCPublishOptCenter.m
//  TXLiteAVDemo
//
//  Created by carolsuo on 2018/8/24.
//  Copyright © 2018年 Tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SystemConfiguration/CaptiveNetwork.h>
#import "TXUGCPublishOptCenter.h"
#import "TVCClientInner.h"
#import "AFNetworkReachabilityManager.h"

#define HTTPDNS_SERVER    @"http://119.29.29.29/d?dn="         // httpdns服务器

typedef void (^TXUGCCompletion)(int result);
typedef void (^TXUGCHttpCompletion)(NSData* _Nullable data, int errCode);

static TXUGCPublishOptCenter *_shareInstance = nil;

@implementation TXUGCPublishOptCenter

+ (instancetype)shareInstance {
    static dispatch_once_t predicate;
    
    dispatch_once(&predicate, ^{
        _shareInstance = [[TXUGCPublishOptCenter alloc] init];
    });
    return _shareInstance;
}

- (instancetype)init {
    if (self = [super init])
    {
        _cacheMap = [[NSMutableDictionary alloc] init];
        _fixCacheMap = [[NSMutableDictionary alloc] init];
        _publishingList = [[NSMutableDictionary alloc] init];
        _isStarted = NO;
        _signature = @"";
    }
    return self;
}

- (void)prepareUpload:(NSString*)signature {
    _signature = signature;
    if (!_isStarted) {
        _isStarted = YES;
        [self reFresh];
        [self monitorNetwork];
    }
}

//刷新httpdns
- (void)reFresh {
    _bestCosRegion = @"";
    _bestCosDomain = @"";
    _minCosRespTime = 0;
    
    //清掉dns缓存
    [_cacheMap removeAllObjects];
    [_fixCacheMap removeAllObjects];
    
    //使用了代理，不走httpdns
    if ([self useProxy]) {
        return;
    }
    
    __weak __typeof(self) weakSelf = self;
    [self freshDomain:UGC_HOST completion:^(int result) {
        [weakSelf prepareUploadUGC];
    }];
}

- (void)freshDomain:(NSString *)domain completion:(TXUGCCompletion)completion {
    NSString *reqUrl = [HTTPDNS_SERVER stringByAppendingString:domain];
    
    __weak __typeof(self)weakSelf = self;
    [self sendHttpRequest:reqUrl method:@"GET" body:nil completion:^(NSData * _Nullable data, int errCode) {
        if (data == nil) {
            if (completion) {
                completion(-1);
            }
            return;
        }
        __strong __typeof(weakSelf) self = weakSelf;
        if (self == nil) {
            if (completion) {
                completion(-1);
            }
            return;
        }
        
        NSString* ips = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        NSLog(@"httpdns domain[%@] ips[%@]", domain, ips);
        
        NSArray *ipLists = [ips componentsSeparatedByString:@";"];
        [self.cacheMap setValue:ipLists forKey:domain];
        
        if (completion) {
            completion(0);
        }
    }];
}

//监控网络接入变化
- (void)monitorNetwork {
    //网络切换的时候刷新一下httpdns
    [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        switch (status) {
            case AFNetworkReachabilityStatusUnknown:
                NSLog(@"未知");
                break;
            case AFNetworkReachabilityStatusNotReachable:
                NSLog(@"没有网络");
                break;
            case AFNetworkReachabilityStatusReachableViaWWAN:
                NSLog(@"3G|4G");
                [self reFresh];
                break;
            case AFNetworkReachabilityStatusReachableViaWiFi:
                NSLog(@"WiFi");
                [self reFresh];
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
    return [_bestCosRegion copy];
}

//是否使用了代理
- (BOOL)useProxy {
    CFDictionaryRef dicRef = CFNetworkCopySystemProxySettings();
    if (NULL == dicRef) return NO;
    
    const CFStringRef proxyCFstr = (const CFStringRef)CFDictionaryGetValue(dicRef, (const void*)kCFNetworkProxiesHTTPProxy);
    NSString* proxy = (__bridge NSString *)proxyCFstr;
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
    NSString *reqUrl = [NSString stringWithFormat:@"https://%@/v3/index.php?Action=PrepareUploadUGC", UGC_HOST];
    NSLog(@"prepareUploadUGC reqUrl[%@]", reqUrl);
    
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    [dic setValue:TVCVersion forKey:@"clientVersion"];
    [dic setValue:_signature forKey:@"signature"];
    
    NSError *error = nil;
    NSData *body = [NSJSONSerialization dataWithJSONObject:dic options:0 error:&error];
    if (error) {
        return;
    }
    
    __weak __typeof(self) weakSelf = self;
    [self sendHttpRequest:reqUrl method:@"POST" body:body completion:^(NSData * _Nullable data, int errCode) {
        [weakSelf parsePrepareUploadRsp:data];
    }];
}

- (void)parsePrepareUploadRsp:(NSData *)rspData {
    if (rspData == nil) {
        return;
    }
    
    NSError *error = nil;
    id ret = [NSJSONSerialization JSONObjectWithData:rspData options:NSJSONReadingAllowFragments error:&error];
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
    
    for (int i = 0; i < cosArray.count; ++i) {
        if ([cosArray[i] isKindOfClass:[NSDictionary class]]) {
            NSDictionary *cosInfo = cosArray[i];
            NSString *region = (NSString *)[cosInfo objectForKey:@"region"];
            NSString *domain = (NSString *)[cosInfo objectForKey:@"domain"];
            int isAcc = [[cosInfo objectForKey:@"isAcc"] intValue];
            NSString *ips = (NSString *)[cosInfo objectForKey:@"ip"];
            
            if (region.length > 0 && domain.length > 0) {
                [self getCosDNS:region domain:domain isAcc:isAcc ips:ips];
            }
        }
    }
}

// 发送head请求探测
- (void)detectDomain:(NSString *)domain completion:(TXUGCCompletion)completion {
    NSString *reqUrl = [NSString stringWithFormat:@"http://%@", domain];
    NSLog(@"detectDomain reqUrl[%@]", reqUrl);
    
    [self sendHttpRequest:reqUrl method:@"HEAD" body:nil completion:^(NSData * _Nullable data, int errCode) {
        if (completion) {
            completion(errCode);
        }
    }];
}

- (void)getCosDNS:(NSString *)region domain:(NSString *)domain isAcc:(int)isAcc ips:(NSString *)ips {
    //返回的ip列表为空，首先执行httpdns
    if (ips.length == 0) {
        __weak __typeof(self) weakSelf = self;
        [self freshDomain:domain completion:^(int result) {
            [weakSelf detectBsetCosIP:region domain:domain];
        }];
    } else {
        NSArray *ipLists = [ips componentsSeparatedByString:@";"];
        [self addDomainDNS:domain ipLists:ipLists];
        [self detectBsetCosIP:region domain:domain];
    }
}

- (void)detectBsetCosIP:(NSString *)region domain:(NSString *)domain {
    __weak __typeof(self) weakSelf = self;
    
    UInt64 beginTs = (UInt64)[[NSDate date] timeIntervalSince1970] * 1000;
    [self detectDomain:domain completion:^(int result) {
        __strong __typeof(weakSelf) self = weakSelf;
        if (self == nil) {
            return;
        }
        
        if (result == 0) {
            UInt64 endTs = (UInt64)[[NSDate date] timeIntervalSince1970] * 1000;
            UInt64 cosTs = (endTs - beginTs);
            if (self.minCosRespTime == 0 || cosTs < self.minCosRespTime) {
                self.minCosRespTime = cosTs;
                self.bestCosRegion = region;
                self.bestCosDomain = domain;
            }
        }
    }];
}

// 简单包装http请求
- (void)sendHttpRequest:(NSString *)reqUrl method:(NSString *)method body:(NSData *)body completion:(TXUGCHttpCompletion)completion {
    // create request
    NSURL *url =[NSURL URLWithString:reqUrl];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = method;
    if (body != nil) {
        [request setValue:[NSString stringWithFormat:@"%ld", (long) [body length]] forHTTPHeaderField:@"Content-Length"];
        [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
        [request setHTTPBody:body];
    }
    
    NSURLSessionConfiguration *initCfg = [NSURLSessionConfiguration defaultSessionConfiguration];
    [initCfg setTimeoutIntervalForRequest:5];
    
    NSURLSession* session = [NSURLSession sessionWithConfiguration:initCfg delegate:nil delegateQueue:nil];
    __weak NSURLSession *wis = session;
    
    NSURLSessionTask *dnsTask = [session dataTaskWithRequest:request completionHandler:^(NSData *_Nullable data, NSURLResponse *_Nullable response, NSError *_Nullable error) {
        //invalid NSURLSession
        [wis invalidateAndCancel];
        
        if (error) {
            if (completion) {
                completion(nil, -1);
            }
            return;
        }
        
        if (completion) {
            completion(data, 0);
        }
    }];
    [dnsTask resume];
}

@end
