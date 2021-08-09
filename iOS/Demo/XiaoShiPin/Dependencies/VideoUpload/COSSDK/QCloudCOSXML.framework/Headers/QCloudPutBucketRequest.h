//
//  PutBucket.h
//  PutBucket
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
@class QCloudCreateBucketConfiguration;
NS_ASSUME_NONNULL_BEGIN
/**
创建存储桶（Bucket）的方法.

 ### 功能描述

 在开始使用 COS 时，需要在指定的账号下先创建一个 Bucket 以便于对象的使用和管理.
 并指定 Bucket 所属的地域.创建 Bucket 的用户默认成为 Bucket 的持有者.
 若创建 Bucket 时没有指定访问权限，则默认 为私有读写（private）权限.

 可用地域，可以查看https://cloud.tencent.com/document/product/436/6224.

 关于创建 Bucket 描述，请查看 https://cloud.tencent.com/document/product/436/14106.

### 示例

  @code

    QCloudPutBucketRequest* request = [QCloudPutBucketRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    [request setFinishBlock:^(id outputObject, NSError* error) {
        // 可以从 outputObject 中获取服务器返回的 header 信息
        NSDictionary* info = (NSDictionary *) outputObject;
    }];
    [[QCloudCOSXMLService defaultCOSXML] PutBucket:request];;

*/
@interface QCloudPutBucketRequest : QCloudBizHTTPRequest
/**
定义 bucket 的acl属性。有效值：private，public-read-write，public-read；默认值：private
*/
@property (strong, nonatomic) NSString *accessControlList;
/**
赋予被授权者读的权限,id="OwnerUin"；
*/
@property (strong, nonatomic) NSString *grantRead;
/**
赋予被授权者写的权限。格式: id="OwnerUin"；
*/
@property (strong, nonatomic) NSString *grantWrite;
/**
赋予被授权者读写权限。格式: id="OwnerUin"；
*/
@property (strong, nonatomic) NSString *grantFullControl;
/**
要创建的存储桶名称，命名规范请参阅 [存储桶命名规范]
 (https://cloud.tencent.com/document/product/436/13312#.E5.91.BD.E5.90.8D.E8.A7.84.E8.8C.83)
注意存储桶名只能由数字和小写字母组成，并且长度不能超过40个字符，否则会创建失败
*/
@property (strong, nonatomic) NSString *bucket;

@property (strong, nonatomic) QCloudCreateBucketConfiguration *createBucketConfiguration;

@end
NS_ASSUME_NONNULL_END
