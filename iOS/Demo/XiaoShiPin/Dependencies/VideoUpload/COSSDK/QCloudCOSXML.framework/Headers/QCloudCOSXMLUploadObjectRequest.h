//
//  QCloudCOSXMLUploadObjectRequest.h
//  Pods
//
//  Created by Dong Zhao on 2017/5/23.
//
//

#import <QCloudCore/QCloudCore.h>
#import "QCloudCOSStorageClassEnum.h"

FOUNDATION_EXTERN NSString* const QCloudUploadResumeDataKey;

typedef NSData* QCloudCOSXMLUploadObjectResumeData;
@class QCloudUploadObjectResult;
@class QCloudInitiateMultipartUploadResult;
@class QCloudCOSXMLUploadObjectRequest;
typedef void(^InitMultipleUploadFinishBlock)(QCloudInitiateMultipartUploadResult* multipleUploadInitResult, QCloudCOSXMLUploadObjectResumeData resumeData);

/**
 COSXML上传对象接口。在上传小于1MB的文件时，通过该request来上传的话，会生成一个简单上传putObjectRequset，将整个对象直接上传。
 
 如果上传的对象大小大于1MB时，我们会在内部进行分片上传的处理，将文件切分成数个1MB大小的块，然后通过并行分快上传的方式进行上传。
 */
@interface QCloudCOSXMLUploadObjectRequest<BodyType> : QCloudAbstractRequest
/**
 上传文件（对象）的文件名，也是对象的key，请注意文件名中不可以含有问号即"?"字符
 */
@property (strong, nonatomic) NSString *object;


/**
 存储桶名称
 */
@property (strong, nonatomic) NSString *bucket;


/**
 在进行HTTP请求的时候，您可以通过设置该参数来设置自定义的一些头部信息。
 
 */
@property (strong, nonatomic) NSDictionary* customHeaders;
/**
 需要上传的对象内容。可以传入NSData*或者NSURL*类型的变量
 */
@property (strong, nonatomic) BodyType body;


/**
 RFC 2616 中定义的缓存策略，将作为 Object 元数据保存
 */
@property (strong, nonatomic) NSString *cacheControl;

/**
 RFC 2616 中定义的文件名称，将作为 Object 元数据保存
 */
@property (strong, nonatomic) NSString *contentDisposition;

/**
 当使用 Expect: 100-continue 时，在收到服务端确认后，才会发送请求内容
 */
@property (strong, nonatomic) NSString *expect;

/**
 RFC 2616 中定义的过期时间，将作为 Object 元数据保存
 */
@property (strong, nonatomic) NSString *expires;

@property (strong, nonatomic) NSString *contentSHA1;

/**
 对象的存储级别
 */
@property (assign, nonatomic) QCloudCOSStorageClass storageClass;


/**
 定义 Object 的 ACL(Access Control List) 属性。有效值：private，public-read-write，public-read；默认值：private
 */
@property (strong, nonatomic) NSString *accessControlList;


/**
 赋予被授权者读的权限。格式：id=" ",id=" "；
 当需要给子账户授权时，id="qcs::cam::uin/<OwnerUin>:uin/<SubUin>"，
 当需要给根账户授权时，id="qcs::cam::uin/<OwnerUin>:uin/<OwnerUin>"
 */
@property (strong, nonatomic) NSString *grantRead;


/**
 赋予被授权者写的权限。格式：id=" ",id=" "；
 当需要给子账户授权时，id="qcs::cam::uin/<OwnerUin>:uin/<SubUin>"，
 当需要给根账户授权时，id="qcs::cam::uin/<OwnerUin>:uin/<OwnerUin>"
 */
@property (strong, nonatomic) NSString *grantWrite;


/**
 赋予被授权者读写权限。格式: id=" ",id=" " ；
 当需要给子账户授权时，id="qcs::cam::uin/<OwnerUin>:uin/<SubUin>"，
 当需要给根账户授权时，id="qcs::cam::uin/<OwnerUin>:uin/<OwnerUin>"
 */
@property (strong, nonatomic) NSString *grantFullControl;


/**
 表明该请求是否已经被中断
 */
@property (assign, atomic, readonly) BOOL aborted;

/**
 如果该request产生了分片上传的请求，那么在分片上传初始化完成后，会通过这个block来回调，可以在该回调block中获取分片完成后的bucket, key, uploadID,以及用于后续上传失败后恢复上传的ResumeData。
 */
@property (nonatomic, copy) InitMultipleUploadFinishBlock initMultipleUploadFinishBlock;

/**
 上传完成后会通过该block回调。若error为空，可视为成功。

 @param QCloudRequestFinishBlock 上传完成后的回调
 */
- (void) setFinishBlock:(void (^)(QCloudUploadObjectResult* result, NSError* error))QCloudRequestFinishBlock;
#pragma resume
+ (instancetype) requestWithRequestData:(QCloudCOSXMLUploadObjectResumeData)resumeData;
- (QCloudCOSXMLUploadObjectResumeData) cancelByProductingResumeData:(NSError* __autoreleasing*)error;


- (void) abort:(QCloudRequestFinishBlock)finishBlock;
@end
