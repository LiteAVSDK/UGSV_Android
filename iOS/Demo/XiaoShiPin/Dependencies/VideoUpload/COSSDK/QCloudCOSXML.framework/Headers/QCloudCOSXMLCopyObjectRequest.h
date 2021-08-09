//
//  QCloudCOSXMLCopyObjectRequest.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 16/11/2017.
//
#import <Foundation/Foundation.h>
#import <QCloudCore/QCloudCore.h>
#import "QCloudCOSStorageClassEnum.h"
#import "QCloudCopyObjectResult.h"
#import "QCloudCOSTransferMangerService.h"
NS_ASSUME_NONNULL_BEGIN
typedef void (^CopyProgressBlock)(int64_t partsSent, int64_t totalPartsExpectedToSent);
typedef void (^RequestsMetricArrayBlock)(NSMutableArray *_Nullable requstMetricArray);

/**
 复制对象

 1：先初始化一个 QCloudCOSXMLCopyObjectRequest 对象

 2：然后调用 QCloudCOSTransferMangerService 的 CopyObject 方法即可。

 注意对于比较大的文件，将会使用分块复制的方式进行复制。这个过程对于用户是没有感知的。

 ### 示例

  @code

    QCloudCOSXMLCopyObjectRequest* request = [[QCloudCOSXMLCopyObjectRequest alloc] init];
    request.bucket = @"examplebucket-1250000000";//目的 \<BucketName-APPID>，需要是公有读或者在当前账号有权限
    request.object = @"exampleobject";//目的文件名称
    //文件来源 \<BucketName-APPID>，需要是公有读或者在当前账号有权限
    request.sourceBucket = @"sourcebucket-1250000000";
    request.sourceObject = @"sourceObject";//源文件名称
    request.sourceAPPID = @"1250000000";//源文件的 APPID
    request.sourceRegion= @"COS_REGION";//来源的地域

    [request setFinishBlock:^(QCloudCopyObjectResult* result, NSError* error) {
        //可以从 outputObject 中获取 response 中 etag 或者自定义头部等信息
    }];

    //注意如果是跨地域复制，这里使用的 transferManager 所在的 region 必须为目标桶所在的 region
    [[QCloudCOSTransferMangerService defaultCOSTransferManager] CopyObject:request];

 */

@interface QCloudCOSXMLCopyObjectRequest : QCloudAbstractRequest
/**
 对象名
 */
@property (strong, nonatomic) NSString *object;
/**
 存储桶名
 */
@property (strong, nonatomic) NSString *bucket;

/**
 复制的源文件所在Bucket
 */
@property (nonatomic, copy) NSString *sourceBucket;

/**
 复制的源文件的对象名，key
 */
@property (nonatomic, copy) NSString *sourceObject;

/**
 复制的源文件的appID
 */
@property (nonatomic, copy) NSString *sourceAPPID;

/**
 复制的源文件所在的区域。
 */
@property (nonatomic, copy) NSString *sourceRegion;
/**
 源文件的版本ID
 */
@property (nonatomic, copy) NSString *sourceObjectVersionID;
/**
 是否拷贝元数据，枚举值：Copy, Replaced，默认值 Copy。假如标记为 Copy，忽略 Header
 中的用户元数据信息直接复制；假如标记为 Replaced，按 Header 信息修改元数据。当目标路径和原路径一致
 ，即用户试图修改元数据时，必须为 Replaced
 */
@property (strong, nonatomic) NSString *metadataDirective;
/**
 当 Object 在指定时间后被修改，则执行操作，否则返回 412。可与 x-cos-copy-source-If-None-Match
 一起使用，与其他条件联合使用返回冲突。
 */
@property (strong, nonatomic) NSString *objectCopyIfModifiedSince;
/**
 当 Object 在指定时间后未被修改，则执行操作，否则返回 412。可与 x-cos-copy-source-If-Match
 一起使用，与其他条件联合使用返回冲突。
 */
@property (strong, nonatomic) NSString *objectCopyIfUnmodifiedSince;
/**
 当 Object 的 Etag 和给定一致时，则执行操作，否则返回 412。可与x-cos-copy-source-If-Unmodified-Since
 一起使用，与其他条件联合使用返回冲突。
 */
@property (strong, nonatomic) NSString *objectCopyIfMatch;
/**
 当 Object 的 Etag 和给定不一致时，则执行操作，否则返回 412。可与 x-cos-copy-source-If-Modified-Since
 一起使用，与其他条件联合使用返回冲突。
 */
@property (strong, nonatomic) NSString *objectCopyIfNoneMatch;
/**
 Object 的存储级别
 */
@property (assign, nonatomic) QCloudCOSStorageClass storageClass;
/**
 定义 Object 的 ACL 属性。有效值：private，public-read-write，public-read；默认值：private
 */
@property (strong, nonatomic) NSString *accessControlList;
/**
 赋予被授权者读的权限。格式：id=" ",id=" "；
 当需要给子账户授权时，id="qcs::cam::uin/\<OwnerUin>:uin/\<SubUin>"，
 当需要给根账户授权时，id="qcs::cam::uin/\<OwnerUin>:uin/\<OwnerUin>"
 */
@property (strong, nonatomic) NSString *grantRead;
/**
 赋予被授权者写的权限。格式：id=" ",id=" "；
 当需要给子账户授权时，id="qcs::cam::uin/\<OwnerUin>:uin/\<SubUin>"，
 当需要给根账户授权时，id="qcs::cam::uin/\<OwnerUin>:uin/\<OwnerUin>"
 */
@property (strong, nonatomic) NSString *grantWrite;
/**
 赋予被授权者读写权限。格式: id=" ",id=" " ；
 当需要给子账户授权时，id="qcs::cam::uin/\<OwnerUin>:uin/\<SubUin>"，
 当需要给根账户授权时，id="qcs::cam::uin/\<OwnerUin>:uin/\<OwnerUin>"
 */
@property (strong, nonatomic) NSString *grantFullControl;

@property (nonatomic, weak) QCloudCOSTransferMangerService *transferManager;
/*
 在进行HTTP请求的时候，可以通过设置该参数来设置自定义的一些头部信息。
 通常情况下，携带特定的额外HTTP头部可以使用某项功能，如果是这类需求，可以通过设置该属性来实现。
 */
@property (strong, nonatomic) NSMutableDictionary *customHeaders;
@property (strong, nonatomic) NSString *regionName;
/**
 在对大文件进行复制的过程中，会通过分片的方式进行复制。从该进度回调里可以获取当前已经复制了多少分片。

 @param copyProgressBlock 进度回调block
 */
- (void)setCopyProgressBlock:(void (^)(int64_t partsSent, int64_t totalPartsExpectedToSent))copyProgressBlock;
@property (nonatomic, copy) RequestsMetricArrayBlock requstsMetricArrayBlock;

/**
 Copy操作完成后的回调

 @param QCloudRequestFinishBlock 完成回调
 */
- (void)setFinishBlock:(void (^_Nullable)(QCloudCopyObjectResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
- (void)setCOSServerSideEncyptionWithCustomerKey:(NSString *)customerKey;
@end
NS_ASSUME_NONNULL_END
