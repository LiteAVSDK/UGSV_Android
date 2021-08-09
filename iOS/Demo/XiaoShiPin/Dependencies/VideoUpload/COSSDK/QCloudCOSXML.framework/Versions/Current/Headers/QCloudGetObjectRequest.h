//
//  GetObject.h
//  GetObject
//
//  Created by tencent
//  Copyright (c) 2015年 tencent. All rights reserved.
//
//   ██████╗  ██████╗██╗      ██████╗ ██╗   ██╗██████╗     ████████╗███████╗██████╗ ███╗   ███╗██╗███╗   ██╗ █████╗ ██╗         ██╗      █████╗
//   ██████╗
//  ██╔═══██╗██╔════╝██║     ██╔═══██╗██║   ██║██╔══██╗    ╚══██╔══╝██╔════╝██╔══██╗████╗ ████║██║████╗  ██║██╔══██╗██║         ██║ ██╔══██╗██╔══██╗
//  ██║   ██║██║     ██║     ██║   ██║██║   ██║██║  ██║       ██║   █████╗  ██████╔╝██╔████╔██║██║██╔██╗ ██║███████║██║         ██║ ███████║██████╔╝
//  ██║▄▄ ██║██║     ██║     ██║   ██║██║   ██║██║  ██║       ██║   ██╔══╝  ██╔══██╗██║╚██╔╝██║██║██║╚██╗██║██╔══██║██║         ██║ ██╔══██║██╔══██╗
//  ╚██████╔╝╚██████╗███████╗╚██████╔╝╚██████╔╝██████╔╝       ██║   ███████╗██║  ██║██║ ╚═╝ ██║██║██║ ╚████║██║  ██║███████╗    ███████╗██║
//  ██║██████╔╝
//   ╚══▀▀═╝  ╚═════╝╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝        ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝    ╚══════╝╚═╝ ╚═╝╚═════╝
//
//
//                                                                              _             __                 _                _
//                                                                             (_)           / _|               | |              | |
//                                                          ___  ___ _ ____   ___  ___ ___  | |_ ___  _ __    __| | _____   _____| | ___  _ __   ___ _
//                                                          __ ___
//                                                         / __|/ _ \ '__\ \ / / |/ __/ _ \ |  _/ _ \| '__|  / _` |/ _ \ \ / / _ \ |/ _ \| '_ \ / _ \
//                                                         '__/ __|
//                                                         \__ \  __/ |   \ V /| | (_|  __/ | || (_) | |    | (_| |  __/\ V /  __/ | (_) | |_) |  __/
//                                                         |  \__
//                                                         |___/\___|_|    \_/ |_|\___\___| |_| \___/|_|     \__,_|\___| \_/ \___|_|\___/| .__/
//                                                         \___|_|  |___/
//    ______ ______ ______ ______ ______ ______ ______ ______                                                                            | |
//   |______|______|______|______|______|______|______|______|                                                                           |_|
//

#import <Foundation/Foundation.h>
#import <QCloudCore/QCloudCore.h>
NS_ASSUME_NONNULL_BEGIN
/**

 下载 COS 对象的方法.

 ### 功能描述

 可以直接发起 GET 请求获取 COS 中完整的对象数据, 或者在 GET 请求 中传入 Range 请求头部获取对象的部分内容.

 获取COS 对象的同时，对象的元数据将会作为 HTTP 响应头部随对象内容一同返回，COS 支持GET
 请求时 使用 URL 参数的方式覆盖响应的部分元数据值，例如覆盖 Content-iDisposition 的响应值.

 关于获取 COS 对象的接口描述，请查看 https://cloud.tencent.com/document/product/436/7753.

### 示例

  @code

    QCloudGetObjectRequest* request = [QCloudGetObjectRequest new];

    // 设置下载的路径 URL，如果设置了，文件将会被下载到指定路径中
    // 如果未设置该参数，那么文件将会被下载至内存里，存放在在 finishBlock 的 outputObject 里
    request.downloadingURL = [NSURL URLWithString:QCloudTempFilePathWithExtension(@"downding")];

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    request.object = @"exampleobject";

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    [request setFinishBlock:^(id outputObject, NSError *error) {
        // 可以从 outputObject 中获取 response 中 etag 或者自定义头部等信息
        NSDictionary* info = (NSDictionary *) outputObject;
    }];
    [request setDownProcessBlock:^(int64_t bytesDownload, int64_t totalBytesDownload,
        int64_t totalBytesExpectedToDownload) {

        // 下载过程中的进度
        // bytesDownload       一次下载的字节数，
        // totalBytesDownload  总过接受的字节数
        // totalBytesExpectedToDownload 文件一共多少字节

    }];

    [[QCloudCOSXMLService defaultCOSXML] GetObject:request];

*/
@interface QCloudGetObjectRequest : QCloudBizHTTPRequest
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
指定 Object 的 VersionID (在开启多版本的情况下)
*/
@property (strong, nonatomic) NSString *versionID;
/**
对象名
*/
@property (strong, nonatomic) NSString *object;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
给图片添加盲水印
*/
@property (strong, nonatomic) NSString *watermarkRule;

@end
NS_ASSUME_NONNULL_END
