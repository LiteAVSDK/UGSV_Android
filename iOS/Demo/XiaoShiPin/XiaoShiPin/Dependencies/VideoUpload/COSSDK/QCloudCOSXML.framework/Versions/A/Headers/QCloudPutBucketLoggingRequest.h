//
//  PutBucketLogging.h
//  PutBucketLogging
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
@class QCloudBucketLoggingStatus;
NS_ASSUME_NONNULL_BEGIN

/**
 为源存储桶开启日志记录的方法

 ### 功能描述

 只有源存储桶拥有者才可进行该请求操作。

 只有源存储桶拥有者才可进行该请求操作。

 关于在存储桶中创建清单任务接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/17054.

 ### 示例

 @code

    QCloudPutBucketLoggingRequest *request = [QCloudPutBucketLoggingRequest new];

    // 说明日志记录配置的状态，如果无子节点信息则意为关闭日志记录
    QCloudBucketLoggingStatus *status = [QCloudBucketLoggingStatus new];

    // 存储桶 logging 设置的具体信息，主要是目标存储桶
    QCloudLoggingEnabled *loggingEnabled = [QCloudLoggingEnabled new];

    // 存放日志的目标存储桶，可以是同一个存储桶（但不推荐），或同一账户下、同一地域的存储桶
    loggingEnabled.targetBucket = @"examplebucket-1250000000";

    // 日志存放在目标存储桶的指定路径
    loggingEnabled.targetPrefix = @"mylogs";
    status.loggingEnabled = loggingEnabled;
    request.bucketLoggingStatus = status;

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";
    [request setFinishBlock:^(id outputObject, NSError *error) {
       // outputObject 包含所有的响应 http 头部
       NSDictionary* info = (NSDictionary *) outputObject;
    }];
    [[QCloudCOSXMLService defaultCOSXML] PutBucketLogging:request];

*/
@interface QCloudPutBucketLoggingRequest : QCloudBizHTTPRequest
/**
说明日志记录配置的状态
*/
@property (strong, nonatomic) QCloudBucketLoggingStatus *bucketLoggingStatus;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@end
NS_ASSUME_NONNULL_END
