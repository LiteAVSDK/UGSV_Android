//
//  QCloudBizHTTPRequest.h
//  Pods
//
//  Created by Dong Zhao on 2017/3/13.
//
//

#import "QCloudHTTPRequest.h"
@class QCloudTask;
@class QCloudSignatureFields;
@class QCloudSignature;

NS_ASSUME_NONNULL_BEGIN

/**
 * 将服务器返回数据解析成制定的class
 */

extern _Nonnull QCloudResponseSerializerBlock QCloudResponseObjectSerilizerBlock(Class modelClass);

/**
 * 根据特定的response模式解析其中的数据
 */
extern _Nonnull QCloudResponseSerializerBlock QCloudResponseCOSNormalRSPSerilizerBlock;

@class QCloudServiceConfiguration;
@interface QCloudBizHTTPRequest : QCloudHTTPRequest

/*
 在进行HTTP请求的时候，可以通过设置该参数来设置自定义的一些头部信息。
 通常情况下，携带特定的额外HTTP头部可以使用某项功能，如果是这类需求，可以通过设置该属性来实现。
 */
@property (strong, nonatomic) NSMutableDictionary *customHeaders;
@property (strong, nonatomic) NSString *regionName;
@property (copy, nonatomic) NSArray<NSMutableDictionary *> *scopesArray;
/**
 通过指定该属性可以设置签名串是放在请求头部还是url中，默认为NO，签名串放在请求头部。
 */
@property (nonatomic, assign) BOOL isSignedInURL;
/**
 请求序列化的过程

 @return 请求序列化的过程
 */
- (NSArray *_Nonnull)customResponseSerializers;

/**
 返回反序列化过程

 @return 返回反序列化过程
 */
- (NSArray *)customRequestSerizliers;

- (void)configureReuqestSerializer:(QCloudRequestSerializer *)requestSerializer responseSerializer:(QCloudResponseSerializer *)responseSerializer;

- (BOOL)customBuildRequestData:(NSError *__autoreleasing *)error;

- (QCloudSignatureFields *)signatureFields;

- (void)loadQCloudSignature;
- (void)setCOSServerSideEncyption;
- (void)setCOSServerSideEncyptionWithCustomerKey:(NSString *)customerKey;
;
@end

NS_ASSUME_NONNULL_END
