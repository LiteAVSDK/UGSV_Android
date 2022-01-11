//
//  PutBucketACL.h
//  PutBucketACL
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
 设置存储桶（Bucket） 的访问权限（Access Control List, ACL)的方法.

 ### 功能描述

 ACL 权限包括读、写、读写权限. 写入 Bucket 的 ACL 可以通过 header头部："x-cos-acl"，
 "x-cos-grant-read"，"x-cos-grant-write"， "x-cos-grant-full-control" 传入 ACL 信息
 ，或者通过 Body 以 XML 格式传入 ACL 信息.这两种方式只 能选择其中一种，否则引起冲突.

 传入新的 ACL 将覆盖原有 ACL信息. 私有 Bucket 可以下可以给某个文件夹设置成公有，那么该文件夹下的文件都是公有；
 但是把文件夹设置成私有后，在该文件夹下的文件设置 的公有属性，不会生效.

关于设置 Bucket 的ACL接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/7737.

### 示例

  @code

    QCloudPutBucketACLRequest* putACL = [QCloudPutBucketACLRequest new];

    // 授予权限的账号 ID
    NSString* uin = @"100000000001";
    NSString *ownerIdentifier = [NSString stringWithFormat:@"qcs::cam::uin/%@:uin/%@"
                                 , uin,uin];
    NSString *grantString = [NSString stringWithFormat:@"id=\"%@\"",ownerIdentifier];

    // 赋予被授权者读写权限
    putACL.grantFullControl = grantString;

    // 赋予被授权者读权限
    putACL.grantRead = grantString;

    // 赋予被授权者写权限
    putACL.grantWrite = grantString;

    // 存储桶名称，格式为 BucketName-APPID
    putACL.bucket = @"examplebucket-1250000000";

    [putACL setFinishBlock:^(id outputObject, NSError *error) {
        // 可以从 outputObject 中获取服务器返回的 header 信息
        NSDictionary * result = (NSDictionary *)outputObject;

    }];
    // 设置acl
    [[QCloudCOSXMLService defaultCOSXML] PutBucketACL:putACL];

*/
@interface QCloudPutBucketACLRequest : QCloudBizHTTPRequest
/**
 定义 Object 的 ACL 属性，有效值：private，public-read，default；默认值：default（继承 Bucket 权限）
 注意：当前访问策略条目限制为1000条，如果您不需要进行 Object ACL 控制，请填 default 或者此项不进行设置，默认继承 Bucket 权限
*/
@property (strong, nonatomic) NSString *accessControlList;
/**
赋予被授权者读的权限。格式：id="OwnerUin"

*/
@property (strong, nonatomic) NSString *grantRead;
/**
赋予被授权者写的权限。格式：id="OwnerUin"
*/
@property (strong, nonatomic) NSString *grantWrite;

/**
赋予被授权者读权限的权限。格式：id="OwnerUin"

*/
@property (strong, nonatomic) NSString *grantReadACP;
/**
赋予被授权者写权限的权限。格式：id="OwnerUin"
*/
@property (strong, nonatomic) NSString *grantWriteACP;

/**
赋予被授权者读写权限。格式： id="OwnerUin"
*/
@property (strong, nonatomic) NSString *grantFullControl;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@property (nonatomic, strong) QCloudACLPolicy *accessControlPolicy;

@end
NS_ASSUME_NONNULL_END
