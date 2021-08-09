//
//  ListBucketMultipartUploads.h
//  ListBucketMultipartUploads
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
#import "QCloudListMultipartUploadsResult.h"
NS_ASSUME_NONNULL_BEGIN
/**
查询存储桶（Bucket）中正在进行中的分块上传对象的方法.

### 功能描述

COS 支持查询 Bucket 中有哪些正在进行中的分块上传对象，单次请求操作最多列出 1000 个正在进行中的 分块上传对象.

关于查询 Bucket 中正在进行中的分块上传对象接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/7736.

### 示例

  @code

    QCloudListBucketMultipartUploadsRequest* uploads = [QCloudListBucketMultipartUploadsRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    uploads.bucket = @"examplebucket-1250000000";

    // 设置最大返回的 multipart 数量，合法取值从 1 到 1000
    uploads.maxUploads = 100;

    [uploads setFinishBlock:^(QCloudListMultipartUploadsResult* result,
                              NSError *error) {
        // 可以从 result 中返回分块信息
        // 进行中的分块上传对象
        NSArray<QCloudListMultipartUploadContent*> *uploads = result.uploads;
    }];

    [[QCloudCOSXMLService defaultCOSXML] ListBucketMultipartUploads:uploads];

*/

@interface QCloudListBucketMultipartUploadsRequest : QCloudBizHTTPRequest
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
定界符为一个符号，对 Object 名字包含指定前缀且第一次出现 delimiter 字符之间的 Object
 作为一组元素：common prefix。如果没有 prefix，则从路径起点开始
*/
@property (strong, nonatomic) NSString *delimiter;
/**
规定返回值的编码格式，合法值：url
*/
@property (strong, nonatomic) NSString *encodingType;
/**
限定返回的 Object key 必须以 Prefix 作为前缀。
注意使用 prefix 查询时，返回的 key 中仍会包含 Prefix
*/
@property (strong, nonatomic) NSString *prefix;
/**
设置最大返回的 multipart 数量，合法取值从 1 到 000
*/
@property (assign, nonatomic) int maxUploads;
/**
列出条目从该 key 值开始
*/
@property (strong, nonatomic) NSString *keyMarker;
/**
列出条目从该 UploadId 值开始
*/
@property (strong, nonatomic) NSString *uploadIDMarker;

- (void)setFinishBlock:(void (^_Nullable)(QCloudListMultipartUploadsResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
