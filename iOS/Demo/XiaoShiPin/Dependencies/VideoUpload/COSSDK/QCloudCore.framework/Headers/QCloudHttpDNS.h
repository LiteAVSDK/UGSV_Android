//
//  QCloudHttpDNS.h
//  TestHttps
//
//  Created by tencent on 16/2/17.
//  Copyright © 2016年 dzpqzb. All rights reserved.
//

#import <Foundation/Foundation.h>



extern NSString* const kQCloudHttpDNSCacheReady;
extern NSString* const kQCloudHttpDNSHost;


@class QCloudHosts;
@interface QCloudHttpDNS : NSObject
@property (nonatomic, strong, readonly) QCloudHosts* hosts;
+ (instancetype) shareDNS;
/**
   对于跟定的域名进行DNS缓存操作
      @param domain 需要缓存IP的域名
   @param error  如果过程出错，该字段表示错误信息
      @return 是否解析DNS成功
 */
- (BOOL) resolveDomain:(NSString*)domain error:(NSError* __autoreleasing*)error;

/**
   对于URLRequest进行IP重定向，如果改URLRequest原始指向的URL中的host对应的IP已经被解析了，则进行重定向操作，如果没有直接返回原始URLReqest
      @param request 需要被重定向的URLRequest
      @return 如果改URLRequest原始指向的URL中的host对应的IP已经被解析了，则进行重定向操作，如果没有直接返回原始URLReqest
 */
- (NSMutableURLRequest*) resolveURLRequestIfCan:(NSMutableURLRequest*)request;

/**
   判断一个IP是否是被解析出来，且被信任的
      @param ip 需要进行判断的IP
      @return 是否被信任
 */
- (BOOL) isTrustIP:(NSString*)ip;

- (NSString*) queryIPForHost:(NSString*)host;
@end
