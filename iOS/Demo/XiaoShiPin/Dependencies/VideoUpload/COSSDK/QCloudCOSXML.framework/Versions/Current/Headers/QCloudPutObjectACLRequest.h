//
//  PutObjectACL.h
//  PutObjectACL
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

 设置 COS 对象的访问权限信息（Access Control List, ACL）的方法.

 ### 功能描述

 ACL权限包括读、写、读写权限. COS 对象的 ACL 可以通过 header头部："x-cos-acl"，
 "x-cos-grant-read"，"x-cos-grant-write"， "x-cos-grant-full-control" 传入 ACL 信息
 ，或者通过 Body 以 XML 格式传入 ACL 信息.这两种方式只 能选择其中一种，否则引起冲突.
 传入新的 ACL 将覆盖原有 ACL信息.ACL策略数上限1000，建议用户不要每个上传文件都设置 ACL.

 关于设置 COS 对象的ACL接口的具体描述，请查看https://cloud.tencent.com/document/product/436/7748.

### 示例

  @code

    QCloudPutObjectACLRequest* request = [QCloudPutObjectACLRequest new];

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    request.object = @"exampleobject";

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    NSString *grantString = [NSString stringWithFormat:@"id=\"%@\"",@"100000000001"];

    // grantFullControl 等价于 grantRead + grantWrite
    // 赋予被授权者读写权限。
    request.grantFullControl = grantString;
    // 赋予被授权者读权限。
    request.grantRead = grantString;
    // 赋予被授权者写权限。
    request.grantWrite = grantString;

    [request setFinishBlock:^(id outputObject, NSError *error) {
        // 可以从 outputObject 中获取 response 中 etag 或者自定义头部等信息
        NSDictionary* info = (NSDictionary *) outputObject;
    }];

    [[QCloudCOSXMLService defaultCOSXML] PutObjectACL:request];

*/
@interface QCloudPutObjectACLRequest : QCloudBizHTTPRequest
/**
object名
*/
@property (strong, nonatomic) NSString *object;
/**
 定义 Object 的 ACL 属性，有效值：private，public-read，default；默认值：default（继承 Bucket 权限）。
 注：当前访问策略条目限制为1000条，如果您不需要进行 Object ACL 控制，请填 default 或者此项不进行设置，默认继承 Bucket 权限
*/
@property (strong, nonatomic) NSString *accessControlList;
/**
 赋予被授权者读的权限。格式：id="OwnerUin ";
*/
@property (strong, nonatomic) NSString *grantRead;

/**
赋予被授权者读权限的权限。格式：id="OwnerUin"

*/
@property (strong, nonatomic) NSString *grantReadACP;
/**
赋予被授权者写权限的权限。格式：id="OwnerUin"
*/
@property (strong, nonatomic) NSString *grantWriteACP;

/**
 赋予被授权者读写权限。格式: id="OwnerUin ";
*/
@property (strong, nonatomic) NSString *grantFullControl;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@property (nonatomic, strong) QCloudACLPolicy *accessControlPolicy;

@end
NS_ASSUME_NONNULL_END
