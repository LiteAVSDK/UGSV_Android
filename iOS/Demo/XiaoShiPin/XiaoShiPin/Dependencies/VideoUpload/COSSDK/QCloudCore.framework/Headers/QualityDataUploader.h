//
//  QualityDataUploader.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 2018/8/23.
//

#import <Foundation/Foundation.h>
@class QCloudAbstractRequest;
#pragma mark -error key
extern  NSString *const kQCloudQualityErrorCodeKey;
extern  NSString *const kQCloudQualityErrorTypeServerName;
extern  NSString *const kQCloudQualityErrorTypeClientName;
extern  NSString *const kQCloudQualityErrorMessageKey;
extern  NSString *const kQCloudQualityErrorNameKey;
extern  NSString *const kQCloudQualityServiceNameKey;
extern  NSString *const kQCloudQualityErrorStatusCodeKey;
extern  NSString *const kQCloudQualityErrorTypeKey;
extern  NSString *const kQCloudQualityErrorIDKey;
extern  NSString *const kQCloudUploadAppReleaseKey;
@interface QualityDataUploader : NSObject
+ (void)startWithAppkey:(NSString *)appkey;
+ (void)trackSDKRequestSuccessWithRequest:(QCloudAbstractRequest *)request;
+ (void)trackSDKRequestFailWithRequest:(QCloudAbstractRequest *)request error:(NSError *)error;
+ (void)trackNormalEventWithKey:(NSString *)key props:(NSDictionary *)props;
@end
