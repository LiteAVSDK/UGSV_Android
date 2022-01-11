//
//  GetBucketACL.h
//  GetBucketACL
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
#import "QCloudACLPolicy.h"
NS_ASSUME_NONNULL_BEGIN
/**

获取存储桶（Bucket) 的访问权限信息（Access Control List, ACL）的方法.

### 功能说明

ACL 权限包括读、写、读写权限.COS 中 Bucket 是有访问权限控制的.可以通过获取 Bucket 的 ACL
 表(putBucketACL(PutBucketACLRequest))，来查看那些用户拥有 Bucket 访 问权限.
。

关于获取存储桶（Bucket) 访问权限信息接口的具体描述，请查看https://cloud.tencent.com/document/product/436/7733.

### 示例

  @code

    QCloudGetBucketACLRequest* getBucketACl = [QCloudGetBucketACLRequest new];

    // 存储桶名称，格式：BucketName-APPID
    getBucketACl.bucket = @"examplebucket-1250000000";

    [getBucketACl setFinishBlock:^(QCloudACLPolicy * _Nonnull result,
                                           NSError * _Nonnull error) {
        // QCloudACLPolicy 中包含了 Bucket 的 ACL 信息
        // result.accessControlList; 被授权者与权限的信息
    }];

    [[QCloudCOSXMLService defaultCOSXML] GetBucketACL:getBucketACl];

*/
@interface QCloudGetBucketACLRequest : QCloudBizHTTPRequest

/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

- (void)setFinishBlock:(void (^_Nullable)(QCloudACLPolicy *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
