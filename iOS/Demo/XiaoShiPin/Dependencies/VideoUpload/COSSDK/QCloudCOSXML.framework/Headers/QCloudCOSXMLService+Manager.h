//
//  QCloudCOSXMLService+Manager.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 07/12/2017.
//

#import "QCloudCOSXML.h"
#import "QCloudCOSStorageClassEnum.h"
@class QCloudGetObjectACLRequest;
@class QCloudPutObjectACLRequest;
@class QCloudDeleteObjectRequest;
@class QCloudDeleteMultipleObjectRequest;
@class QCloudHeadObjectRequest;
@class QCloudOptionsObjectRequest;

@class QCloudAbortMultipfartUploadRequest;
@class QCloudGetBucketRequest;
@class QCloudGetBucketACLRequest;
@class QCloudGetBucketCORSRequest;
@class QCloudGetBucketLocationRequest;
@class QCloudGetBucketLifecycleRequest;
@class QCloudPutBucketRequest;
@class QCloudPutBucketACLRequest;
@class QCloudPutBucketCORSRequest;
@class QCloudPutBucketLifecycleRequest;
@class QCloudDeleteBucketRequest;
@class QCloudDeleteBucketCORSRequest;
@class QCloudDeleteBucketLifeCycleRequest;
@class QCloudHeadBucketRequest;
@class QCloudListBucketMultipartUploadsRequest;
@class QCloudPutObjectCopyRequest;
@class QCloudDeleteBucketRequest;
@class QCloudPutBucketVersioningRequest;
@class QCloudGetBucketVersioningRequest;
@class QCloudPutBucketReplicationRequest;
@class QCloudGetBucketReplicationRequest;
@class QCloudDeleteBucketReplicationRequest;
@class QCloudGetServiceRequest;
@class QCloudUploadPartCopyRequest;
@class QCloudPostObjectRestoreRequest;
@class QCloudListObjectVersionsRequest;
@class QCloudGetPresignedURLRequest;

@class QCloudGetBucketLoggingRequest;
@class QCloudPutBucketLoggingRequest;
@class QCloudPutBucketTaggingRequest;
@class QCloudGetBucketTaggingRequest;
@class QCloudDeleteBucketTaggingRequest;

@class QCloudPutBucketInventoryRequest;
@class QCloudGetBucketInventoryRequest;
@class QCloudDeleteBucketInventoryRequest;
@class QCloudListBucketInventoryConfigurationsRequest;

@class QCloudPutBucketWebsiteRequest;
@class QCloudGetBucketWebsiteRequest;

@class QCloudGetBucketDomainRequest;
@class QCloudPutBucketDomainRequest;
@class QCloudDeleteBucketWebsiteRequest;

@class QCloudSelectObjectContentRequest;
@class QCloudPutBucketAccelerateRequest;
@class QCloudGetBucketAccelerateRequest;
@class QCloudGetObjectTaggingRequest;
@class QCloudPutBucketTaggingRequest;

@class QCloudPutBucketIntelligentTieringRequest;
@class QCloudGetBucketIntelligentTieringRequest;

NS_ASSUME_NONNULL_BEGIN
@interface QCloudCOSXMLService (Manager)

#pragma mark - bucket
- (void)GetService:(QCloudGetServiceRequest *)request;
- (void)PutBucket:(QCloudPutBucketRequest *)request;
- (void)HeadBucket:(QCloudHeadBucketRequest *)request;
- (void)DeleteBucket:(QCloudDeleteBucketRequest *)request;
- (void)GetBucketLocation:(QCloudGetBucketLocationRequest *)request;
- (void)ListBucketMultipartUploads:(QCloudListBucketMultipartUploadsRequest *)request;
- (void)PutBucketACL:(QCloudPutBucketACLRequest *)request;
- (void)GetBucketACL:(QCloudGetBucketACLRequest *)request;
- (void)PutBucketCORS:(QCloudPutBucketCORSRequest *)request;
- (void)GetBucketCORS:(QCloudGetBucketCORSRequest *)request;
- (void)DeleteBucketCORS:(QCloudDeleteBucketCORSRequest *)request;
- (void)PutBucketLifecycle:(QCloudPutBucketLifecycleRequest *)request;
- (void)GetBucketLifecycle:(QCloudGetBucketLifecycleRequest *)request;
- (void)DeleteBucketLifeCycle:(QCloudDeleteBucketLifeCycleRequest *)request;
- (void)PutBucketVersioning:(QCloudPutBucketVersioningRequest *)request;
- (void)GetBucketVersioning:(QCloudGetBucketVersioningRequest *)request;
- (void)GetBucketLogging:(QCloudGetBucketLoggingRequest *)request;
- (void)PutBucketLogging:(QCloudPutBucketLoggingRequest *)request;
- (void)PutBucketTagging:(QCloudPutBucketTaggingRequest *)request;
- (void)GetBucketTagging:(QCloudGetBucketTaggingRequest *)request;
- (void)DeleteBucketTagging:(QCloudDeleteBucketTaggingRequest *)request;
- (void)PutBucketRelication:(QCloudPutBucketReplicationRequest *)request;
- (void)GetBucketReplication:(QCloudGetBucketReplicationRequest *)request;
- (void)DeleteBucketReplication:(QCloudDeleteBucketReplicationRequest *)request;
- (void)SelectObjectContent:(QCloudSelectObjectContentRequest *)request;
- (void)PutBucketDomain:(QCloudPutBucketDomainRequest *)request;
- (void)GetBucketDomain:(QCloudGetBucketDomainRequest *)request;
- (void)PutBucketWebsite:(QCloudPutBucketWebsiteRequest *)request;
- (void)GetBucketWebsite:(QCloudGetBucketWebsiteRequest *)request;
- (void)DeleteBucketWebsite:(QCloudDeleteBucketWebsiteRequest *)request;
- (void)PutBucketInventory:(QCloudPutBucketInventoryRequest *)request;
- (void)GetBucketInventory:(QCloudGetBucketInventoryRequest *)request;
- (void)DeleteBucketInventory:(QCloudDeleteBucketInventoryRequest *)request;
- (void)ListBucketInventory:(QCloudListBucketInventoryConfigurationsRequest *)request;
- (void)PutBucketAccelerate:(QCloudPutBucketAccelerateRequest *)reques;
- (void)GetBucketAccelerate:(QCloudGetBucketAccelerateRequest *)request;
- (void)PutBucketIntelligentTiering:(QCloudPutBucketIntelligentTieringRequest *)request;
- (void)GetBucketIntelligentTiering:(QCloudGetBucketIntelligentTieringRequest *)request;
#pragma mark - object
- (void)GetBucket:(QCloudGetBucketRequest *)request;
- (void)DeleteObject:(QCloudDeleteObjectRequest *)request;
- (void)GetObjectTagging:(QCloudGetObjectTaggingRequest *)request;
- (void)PuObjectTagging:(QCloudPutBucketTaggingRequest *)request;
- (void)GetObjectACL:(QCloudGetObjectACLRequest *)request;
- (void)PutObjectACL:(QCloudPutObjectACLRequest *)request;
- (void)DeleteMultipleObject:(QCloudDeleteMultipleObjectRequest *)request;
- (void)OptionsObject:(QCloudOptionsObjectRequest *)request;
- (void)PostObjectRestore:(QCloudPostObjectRestoreRequest *)request;
- (void)ListObjectVersions:(QCloudListObjectVersionsRequest *)request;
- (void)getPresignedURL:(QCloudGetPresignedURLRequest *)request;
#pragma mark - Encapsulated Interface

/**
 查询 Bucket 是否存在。注意该方法是同步方法，会阻塞当前线程直到返回结果，请勿在主线程内调用。


 @param bucketName bucket
 @return bucket 是否存在。如果返回YES那说明bucket一定存在，但返回 NO 的时候并不一定是因为 Bucket
 不存在，还有可能因为超时、签名错误等问题导致请求失败了。
 */
- (BOOL)doesBucketExist:(NSString *)bucketName;

/**
 查询 Object 是否存在。注意该方法是同步方法，会阻塞当前线程直到返回结果，请勿在主线程内调用。

 该方法返回不存在可能存在两种情况： 1. Bucket 并不存在。 2. Bucket 存在，但 Object 并不存在。

 @param objectName object
 @param bucket bucket
 @return object 是否存在。如果返回YES那说明bucket一定存在，但返回 NO 的时候并不一定是因为 Bucket
 不存在，还有可能因为超时、签名错误等问题导致请求失败了。
 */
- (BOOL)doesObjectExistWithBucket:(NSString *)bucket object:(NSString *)objectName;

/**
 直接删除对象的接口。注意该方法是同步方法，会阻塞当前线程直到返回完成，请勿在主线程内调用。

 @param bucket bucket
 @param objectName object
 */
- (void)deleteObjectWithBucket:(NSString *)bucket object:(NSString *)objectName;

/**
 删除多版本中指定版本对象的接口。注意该方法是同步方法，会阻塞当前线程直到返回完成，请勿在主线程内调用。

 @param bucket bucket
 @param object object
 @param versionID versionID
 */
- (void)deleteVersionWithBucket:(NSString *)bucket object:(NSString *)object version:(NSString *)versionID;

/**
 更改对象的存储级别，内部通过 CopyObject 操作来实现。注意该方法是同步方法，会阻塞当前线程直到返回完成，请勿在主线程内调用。

 @param bucket bucket
 @param object object
 @param storageClass 存储级别
 */
- (void)changeObjectStorageClassWithBucket:(NSString *)bucket object:(NSString *)object storageClass:(QCloudCOSStorageClass)storageClass;

/**
 更改对象的元数据，内部通过 CopyObject 操作来实现。注意该方法是同步方法，会阻塞当前线程直到返回完成，请勿在主线程内调用。

 @param bucket bucket
 @param object object
 @param meta 元数据，以键值对的方式传入。
 */
- (void)updateObjectMedaWithBucket:(NSString *)bucket object:(NSString *)object meta:(NSDictionary *)meta;

@end
NS_ASSUME_NONNULL_END
