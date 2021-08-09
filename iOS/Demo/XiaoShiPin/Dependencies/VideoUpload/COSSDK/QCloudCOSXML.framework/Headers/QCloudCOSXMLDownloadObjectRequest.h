//
//  QCloudCOSXMLDownloadObjectRequest.h
//  Pods-QCloudCOSXMLDemo
//
//  Created by karisli(李雪) on 2018/8/23.
//

#import <QCloudCore/QCloudCore.h>

@class QCloudCOSTransferMangerService;

/**
 ### 示例

  @code

    QCloudCOSXMLDownloadObjectRequest * request = [QCloudCOSXMLDownloadObjectRequest new];
    request.bucket = @"bucket";
    request.object = @"object";
    request.enableQuic = YES;
    request.localCacheDownloadOffset = 本地已下载的文件大小
    [request setFinishBlock:^(id  _Nullable outputObject, NSError * _Nullable error) {


    }];
    [[QCloudCOSTransferMangerService costransfermangerServiceForKey:ServiceKey]
                                                    DownloadObject:request];
 */
@interface QCloudCOSXMLDownloadObjectRequest : QCloudAbstractRequest

/**
 设置响应头部中的 Content-Type参数
 */
@property (strong, nonatomic) NSString *responseContentType;
/**
 设置响应头部中的Content-Language参数
 */
@property (strong, nonatomic) NSString *responseContentLanguage;
/**
 设置响应头部中的Content-Expires参数
 */
@property (strong, nonatomic) NSString *responseContentExpires;
/**
 设置响应头部中的Cache-Control参数
 */
@property (strong, nonatomic) NSString *responseCacheControl;
/**
 设置响应头部中的 Content-Disposition 参数。
 */
@property (strong, nonatomic) NSString *responseContentDisposition;
/**
 设置响应头部中的 Content-Encoding 参数。
 */
@property (strong, nonatomic) NSString *responseContentEncoding;
/**
 RFC 2616 中定义的指定文件下载范围，以字节（bytes）为单位
 */
@property (strong, nonatomic) NSString *range;
/**
 如果文件修改时间晚于指定时间，才返回文件内容。否则返回 412 (not modified)
 */
@property (strong, nonatomic) NSString *ifModifiedSince;
/**
 如果文件修改时间早于或等于指定时间，才返回文件内容。否则返回 412 (precondition failed)
 */
@property (strong, nonatomic) NSString *ifUnmodifiedModifiedSince;
/**
 当 ETag 与指定的内容一致，才返回文件。否则返回 412 (precondition failed)
 */
@property (strong, nonatomic) NSString *ifMatch;
/**
 当 ETag 与指定的内容不一致，才返回文件。否则返回 304 (not modified)
 */
@property (strong, nonatomic) NSString *ifNoneMatch;

/**
 对象名
 */
@property (strong, nonatomic) NSString *object;
/**
 存储桶名
 */
@property (strong, nonatomic) NSString *bucket;

/**
 桶所在地域
*/
@property (strong, nonatomic) NSString *regionName;

/**
该选项设置为YES后，在下载完成后会比对COS上储存的文件MD5和下载到本地的文件MD5，如果MD5有差异的话会返回-340013错误码。
目前默认关闭。
*/
@property (assign, nonatomic) BOOL enableMD5Verification;

/**
 如果存在改参数，则数据会下载到改路径指名的地址下面，而不会写入内存中。
 */
@property (nonatomic, strong) NSURL *downloadingURL;

/**
 本地已经下载的数据偏移量，如果使用则会从改位置开始下载，如果不使用，则从头开始下载，如果您使用了Range参数，则需要注意改参数。
 */
@property (nonatomic, assign) int64_t localCacheDownloadOffset;
@property (nonatomic, weak) QCloudCOSTransferMangerService *transferManager;
/*
 在进行HTTP请求的时候，可以通过设置该参数来设置自定义的一些头部信息。
 通常情况下，携带特定的额外HTTP头部可以使用某项功能，如果是这类需求，可以通过设置该属性来实现。
 */
@property (strong, nonatomic) NSMutableDictionary *customHeaders;
/**
 指定是否使用分块及续传下载，默认为 FALSE。
 */
@property (assign, nonatomic)BOOL resumableDownload;
/**
 使用分块及续传下载时，指定任务记录文件的路径
 */
@property (strong, nonatomic) NSString *resumableTaskFile;
- (void)setCOSServerSideEncyption;
- (void)setCOSServerSideEncyptionWithCustomerKey:(NSString *)customerKey;

@end
