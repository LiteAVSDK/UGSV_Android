//
//  HeadBucket.h
//  HeadBucket
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
存储桶（Bucket） 是否存在的方法.

### 功能描述

在开始使用 COS 时，需要确认该 Bucket 是否存在，是否有权限访问.若不存在，则可以调用putBucket(PutBucketRequest) 创建.

关于确认该 Bucket 是否存在，是否有权限访问接口的具体描述，请查看https://cloud.tencent.com/document/product/436/7735.

### 示例

  @code

    QCloudHeadBucketRequest* request = [QCloudHeadBucketRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    [request setFinishBlock:^(id outputObject, NSError* error) {
        // 可以从 outputObject 中获取服务器返回的 header 信息
        NSDictionary * result = (NSDictionary *)outputObject;
        // x-cos-bucket-az-type 存储桶 AZ 类型，当存储桶为多 AZ 存储桶时返回此头部，
        // 值固定为 MAZ。

        // x-cos-bucket-region 存储桶所在地域。枚举值请参见 地域和访问域名 文档，
        // 例如 ap-beijing，ap-hongkong，eu-frankfurt 等

    }];
    [[QCloudCOSXMLService defaultCOSXML] HeadBucket:request];

*/
@interface QCloudHeadBucketRequest : QCloudBizHTTPRequest
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@end
NS_ASSUME_NONNULL_END
