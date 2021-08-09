//
//  GetObjectACL.h
//  GetObjectACL
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
获取 COS 对象的访问权限信息（Access Control List, ACL）的方法.

### 功能说明

Bucket 的持有者可获取该 Bucket 下的某个对象的 ACL 信息，如被授权者以及被授权的信息. ACL 权限包括读、写、读写权限.

关于获取 COS 对象的 ACL 接口的具体描述，请查看https://cloud.tencent.com/document/product/436/7744.

### 示例

  @code

    QCloudGetObjectACLRequest *request = [QCloudGetObjectACLRequest new];

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    request.object = @"exampleobject";

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    __block QCloudACLPolicy* policy;
    [request setFinishBlock:^(QCloudACLPolicy * _Nonnull result,
                              NSError * _Nonnull error) {

        policy = result;
        // result.accessControlList; 被授权者与权限的信息
        // result.owner; 持有者的信息
    }];

    [[QCloudCOSXMLService defaultCOSXML] GetObjectACL:request];

*/
@interface QCloudGetObjectACLRequest : QCloudBizHTTPRequest
/**
 存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
 对象名
*/
@property (strong, nonatomic) NSString *object;
/**
  指定多版本中的 Version ID
*/
@property (strong, nonatomic) NSString *versionID;

- (void)setFinishBlock:(void (^_Nullable)(QCloudACLPolicy *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
