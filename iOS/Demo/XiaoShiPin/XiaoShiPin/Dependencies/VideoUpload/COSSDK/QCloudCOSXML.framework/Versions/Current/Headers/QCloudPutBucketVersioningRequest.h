//
//  PutBucketVersioning.h
//  PutBucketVersioning
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
@class QCloudBucketVersioningConfiguration;
NS_ASSUME_NONNULL_BEGIN
/**

 存储桶（Bucket）版本控制的方法.

 ### 功能描述

 版本管理功能一经打开，只能暂停，不能关闭. 通过版本控制，可以在一个 Bucket 中保留一个对象的多个版本.

 版本控制可以防止意外覆盖和删除对象，以便检索早期版本的对象.

 默认情况下，版本控制功能处于禁用状态，
 需要主动去启用或者暂停（Enabled 或者 Suspended）.

关于为已存在的存储桶设置标签接口描述，请查看 https://cloud.tencent.com/document/product/436/19889.

### 示例

  @code

    QCloudPutBucketVersioningRequest* request = [[QCloudPutBucketVersioningRequest alloc] init];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket =@"examplebucket-1250000000";

    // 说明版本控制的具体信息
    QCloudBucketVersioningConfiguration* versioningConfiguration =
        [[QCloudBucketVersioningConfiguration alloc] init];

    request.configuration = versioningConfiguration;

    // 说明版本是否开启，枚举值：Suspended、Enabled
    versioningConfiguration.status = QCloudCOSBucketVersioningStatusEnabled;

    [request setFinishBlock:^(id outputObject, NSError* error) {

        // 可以从 outputObject 中获取服务器返回的 header 信息
        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;
    }];
    [[QCloudCOSXMLService defaultCOSXML] PutBucketVersioning:request];

*/
@interface QCloudPutBucketVersioningRequest : QCloudBizHTTPRequest
/**
    存储桶名称
    */
@property (strong, nonatomic) NSString *bucket;
/**
    版本控制的具体信息
    */
@property (strong, nonatomic) QCloudBucketVersioningConfiguration *configuration;

@end
NS_ASSUME_NONNULL_END
