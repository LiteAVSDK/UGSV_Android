//
//  PutBucketReplication.h
//  PutBucketReplication
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
@class QCloudBucketReplicationConfiguation;
NS_ASSUME_NONNULL_BEGIN
/**
 配置跨区域复制的方法.

### 功能描述

 跨区域复制是支持不同区域 Bucket 自动异步复制对象.注意，不能是同区域的 Bucket,
 且源 Bucket 和目 标 Bucket 必须已启用版本控制putBucketVersioning(PutBucketVersioningRequest).

 关于 配置跨区域复制的方法接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/19223.

### 示例

  @code

    QCloudPutBucketReplicationRequest* request = [[QCloudPutBucketReplicationRequest alloc] init];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    // 说明所有跨地域配置信息
    QCloudBucketReplicationConfiguation* replConfiguration =
                                [[QCloudBucketReplicationConfiguation alloc] init];

    // 发起者身份标示
    replConfiguration.role = @"qcs::cam::uin/100000000001:uin/100000000001";

    // 具体配置信息
    QCloudBucketReplicationRule* rule = [[QCloudBucketReplicationRule alloc] init];

    // 用来标注具体 Rule 的名称
    rule.identifier = @"identifier";
    rule.status = QCloudCOSXMLStatusEnabled;

    // 资源标识符
    QCloudBucketReplicationDestination* destination = [[QCloudBucketReplicationDestination alloc] init];
    NSString* destinationBucket = @"destinationbucket-1250000000";

    // 目标存储桶所在地域
    NSString* region = @"ap-beijing";
    destination.bucket = [NSString stringWithFormat:@"qcs::cos:%@::%@",region,destinationBucket];

    // 目标存储桶信息
    rule.destination = destination;

    // 前缀匹配策略，不可重叠，重叠返回错误。前缀匹配根目录为空
    rule.prefix = @"prefix1";
    replConfiguration.rule = @[rule];
    request.configuation = replConfiguration;

    [request setFinishBlock:^(id outputObject, NSError* error) {

        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;

    }];
    [[QCloudCOSXMLService defaultCOSXML] PutBucketRelication:request];

*/
@interface QCloudPutBucketReplicationRequest : QCloudBizHTTPRequest
/**
说明所有跨区域配置信息
*/
@property (strong, nonatomic) QCloudBucketReplicationConfiguation *configuation;
/**
存储桶名称
*/
@property (strong, nonatomic) NSString *bucket;

@end
NS_ASSUME_NONNULL_END
