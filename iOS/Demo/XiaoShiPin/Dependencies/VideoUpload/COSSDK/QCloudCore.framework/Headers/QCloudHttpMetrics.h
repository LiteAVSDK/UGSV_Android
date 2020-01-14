//
//  QCloudHttpMetrics.h
//  QCloudTernimalLab_CommonLogic
//
//  Created by tencent on 5/27/16.
//  Copyright © 2016 QCloudTernimalLab. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSString* const kTaskTookTime;//kRNBenchmarkRTT;
extern NSString* const kCalculateMD5STookTime;
extern NSString* const kSignRequestTookTime;
extern NSString* const kDnsLookupTookTime;//kRNBenchmarkDNSLoopupTime
extern NSString* const kConnectTookTime;//kRNBenchmarkConnectionTime;
extern NSString* const kSecureConnectTookTime;//kRNBenchmarkSecureConnectionTime;

extern NSString* const kWriteRequestBodyTookTime;//从发送第一个字节到发送完毕,kRNBenchmarkUploadTime
extern NSString* const kReadResponseHeaderTookTime;//kRNBenchmarkServerTime;
extern NSString* const kReadResponseBodyTookTime;//kRNBenchmarkDownploadTime;





@interface QCloudHttpMetrics : NSObject
- (void) benginWithKey:(NSString*)key;
- (void) markFinishWithKey:(NSString*)key;
- (void) directSetCost:(double)cost forKey:(NSString*)key;

- (double) costTimeForKey:(NSString*)key;
-(NSDictionary *)tastMetrics;
- (NSString*) readablityDescription;
@end
