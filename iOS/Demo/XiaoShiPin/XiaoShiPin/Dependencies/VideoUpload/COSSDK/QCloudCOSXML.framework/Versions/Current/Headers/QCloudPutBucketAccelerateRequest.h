//
//  PutBucketAccelerate.h
//  PutBucketAccelerate
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
@class QCloudBucketAccelerateConfiguration;
NS_ASSUME_NONNULL_BEGIN
/**

 实现启用或者暂停存储桶的全球加速功能的方法.

 ### 功能描述

 如果您从未在存储桶上启用过全球加速功能，则 GET Bucket Accelerate 请求不返回全球加速功能配置状态。

 开启全球加速功能功能后，只能暂停，不能关闭。

 设置全球加速功能状态值为 Enabled 或 Suspended，表示开启或暂停全球加速功能。

 如果您是子账号，需要设置存储桶的全球加速功能功能，您需要有该配置的写入权限。

关于启用或者暂停存储桶的全球加速功能接口描述，请查看 https://cloud.tencent.com/document/product/436/38869.

### 示例

  @code

    QCloudPutBucketAccelerateRequest* request = [[QCloudPutBucketAccelerateRequest alloc] init];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket =@"examplebucket-1250000000";

    // 说明版本控制的具体信息
    QCloudBucketAccelerateConfiguration* versioningConfiguration =
        [[QCloudBucketAccelerateConfiguration alloc] init];

    request.configuration = versioningConfiguration;

    // 说明版本是否开启，枚举值：Suspended、Enabled
    versioningConfiguration.status = QCloudCOSBucketAccelerateStatusEnabled;

    [request setFinishBlock:^(id outputObject, NSError* error) {

        // 可以从 outputObject 中获取服务器返回的 header 信息
        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;
    }];
    [[QCloudCOSXMLService defaultCOSXML] PutBucketAccelerate:request];

*/
@interface QCloudPutBucketAccelerateRequest : QCloudBizHTTPRequest
/**
    存储桶名称
    */
@property (strong, nonatomic) NSString *bucket;
/**
    版本控制的具体信息
    */
@property (strong, nonatomic) QCloudBucketAccelerateConfiguration *configuration;

@end
NS_ASSUME_NONNULL_END
