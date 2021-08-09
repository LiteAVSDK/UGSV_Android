//
//  QCloudGetPresignedURLRequest.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 17/01/2018.
//

#import <Foundation/Foundation.h>
#import <QCloudCore/QCloudCore.h>
#import "QCloudGetPresignedURLResult.h"

NS_ASSUME_NONNULL_BEGIN

/**

生成对象预签名链接

### 功能说明

关于生成对象预签名链接接口的更多示例，请参见：https://cloud.tencent.com/document/product/436/34109

### 示例

  @code

    QCloudGetPresignedURLRequest* getPresignedURLRequest = [[QCloudGetPresignedURLRequest alloc] init];

    // 存储桶名称，格式为 BucketName-APPID
    getPresignedURLRequest.bucket = @"examplebucket-1250000000";

    // 使用预签名 URL 的请求的 HTTP 方法。有效值（大小写敏感）为：@"GET"、@"PUT"、@"POST"、@"DELETE"
    getPresignedURLRequest.HTTPMethod = @"GET";

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    getPresignedURLRequest.object = @"exampleobject";

    [getPresignedURLRequest setFinishBlock:^(QCloudGetPresignedURLResult * _Nonnull result,
                                             NSError * _Nonnull error) {
        // 预签名 URL
        NSString* presignedURL = result.presienedURL;

    }];

    [[QCloudCOSXMLService defaultCOSXML] getPresignedURL:getPresignedURLRequest];

*/

@interface QCloudGetPresignedURLRequest : QCloudBizHTTPRequest
@property (nonatomic, copy) NSDate *expires;
/**
 填入使用预签名请求的Bucket
 */
@property (nonatomic, copy) NSString *bucket;

/**
 填入对应的Object
 */
@property (nonatomic, copy) NSString *object;

/**
 填入使用预签名URL的请求的HTTP方法。有效值(大小写敏感)为:@"GET",@"PUT",@"POST",@"DELETE"
 */
@property (nonatomic, copy) NSString *HTTPMethod;

/**
 如果使用预签名URL的请求有该头部，那么通过这里设置
 */
@property (nonatomic, readonly) NSString *contentType;

/**
 如果使用预签名URL的请求有该头部，那么通过这里设置
 */
@property (nonatomic, readonly) NSString *contentMD5;
@property (nonatomic, readonly) NSDictionary<NSString *, NSString *> *requestHeaders;
@property (nonatomic, readonly) NSDictionary<NSString *, NSString *> *requestParameters;
@property (nonatomic, assign) bool isUseSignature;
/**
 添加使用预签名请求的头部

 @param value HTTP header的值
 @param requestHeader HTTP header的key
 */
- (void)setValue:(NSString *_Nullable)value forRequestHeader:(NSString *_Nullable)requestHeader;

/**
 添加使用预签名请求的URL参数

 @param value 参数的值
 @param requestParameter 参数的key
 */
- (void)setValue:(NSString *_Nullable)value forRequestParameter:(NSString *_Nullable)requestParameter;

/**
 设置完成回调。请求完成后会通过该回调来获取结果，如果没有error，那么可以认为请求成功。

 @param finishBlock 请求完成回调
 */
- (void)setFinishBlock:(void (^_Nullable)(QCloudGetPresignedURLResult *_Nullable result, NSError *_Nullable error))finishBlock;
@end

NS_ASSUME_NONNULL_END
