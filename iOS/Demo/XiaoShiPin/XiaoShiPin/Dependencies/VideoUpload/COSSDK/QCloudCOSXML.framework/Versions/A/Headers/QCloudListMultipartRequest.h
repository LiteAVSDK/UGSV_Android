//
//  ListMultipart.h
//  ListMultipart
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
#import "QCloudListPartsResult.h"
NS_ASSUME_NONNULL_BEGIN
/**

 查询特定分块上传中的已上传的块的方法.

 ### 功能描述

COS 支持查询特定分块上传中的已上传的块, 即可以 罗列出指定 UploadId 所属的所有已上传成功的分块.
 因此，基于此可以完成续传功能.

关于查询特定分块上传中的已上传块接口的描述，请查看 https://cloud.tencent.com/document/product/436/7747.

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
@interface QCloudListMultipartRequest : QCloudBizHTTPRequest
/**
对象的名称
*/
@property (strong, nonatomic) NSString *object;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
标识本次分块上传的uploadId
*/
@property (strong, nonatomic) NSString *uploadId;
/**
单次返回最大的条目数量，默认 1000
*/
@property (strong, nonatomic) NSString *maxPartsCount;
/**
默认以 UTF-8 二进制顺序列出条目，所有列出条目从 marker 开始
*/
@property (strong, nonatomic) NSString *partNumberMarker;
/**
规定返回值的编码方式
*/
@property (strong, nonatomic) NSString *encodingType;

- (void)setFinishBlock:(void (^_Nullable)(QCloudListPartsResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
