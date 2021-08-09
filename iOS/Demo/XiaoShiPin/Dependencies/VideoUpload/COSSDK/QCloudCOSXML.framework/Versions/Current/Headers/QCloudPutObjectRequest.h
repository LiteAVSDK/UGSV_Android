//
//  PutObject.h
//  PutObject
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
#import "QCloudCOSStorageClassEnum.h"
NS_ASSUME_NONNULL_BEGIN
/**

 简单上传的方法.

 ### 功能描述

 简单上传主要适用于在单个请求中上传一个小于 5 GB 大小的对象. 对于大于 5 GB 的对象(或者在高带宽或弱网络环境中
 ）优先使用分片上传的方式 (https://cloud.tencent.com/document/product/436/14112).

 关于简单上传接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/7749.

### 示例

  @code

    QCloudPutObjectRequest* put = [QCloudPutObjectRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    put.bucket = @"examplebucket-1250000000";

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    put.object = @"exampleobject";

    put.body =  [@"testFileContent" dataUsingEncoding:NSUTF8StringEncoding];

    [put setFinishBlock:^(id outputObject, NSError *error) {

        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;
    }];

    [[QCloudCOSXMLService defaultCOSXML] PutObject:put];

*/
@interface QCloudPutObjectRequest<BodyType> : QCloudBizHTTPRequest
@property (nonatomic, strong) BodyType body;
/**
 对象 名称
*/
@property (strong, nonatomic) NSString *object;
/**
 存储桶 名称
*/
@property (strong, nonatomic) NSString *bucket;
/**
RFC 2616 中定义的缓存策略，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *cacheControl;
/**
RFC 2616 中定义用于指示资源的MIME类型，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *contentType;
/**
RFC 2616 中定义的文件名称，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *contentDisposition;
/**
当使用 Expect: 100-continue 时，在收到服务端确认后，才会发送请求内容
*/
@property (strong, nonatomic) NSString *expect;
/**
RFC 2616 中定义的过期时间，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *expires;
@property (strong, nonatomic) NSString *contentSHA1;
/**
对象的存储级别，枚举值：STANDARD（QCloudCOSStorageStandard），STANDARD_IA（QCloudCOSStorageStandardIA）
 ，ARCHIVE（QCloudCOSStorageARCHIVE）。默认值：STANDARD（QCloudCOSStorageStandard）
*/
@property (assign, nonatomic) QCloudCOSStorageClass storageClass;
/**
定义 Object 的 ACL 属性。有效值：private，public-read-write，public-read；默认值：private
*/
@property (strong, nonatomic) NSString *accessControlList;
/**
 赋予被授权者读的权限。格式：id="OwnerUin";

*/
@property (strong, nonatomic) NSString *grantRead;
/**
赋予被授权者写的权限。格式：id="OwnerUin";

*/
@property (strong, nonatomic) NSString *grantWrite;
/**
赋予被授权者读写权限。格式: id="OwnerUin";

*/
@property (strong, nonatomic) NSString *grantFullControl;
/**
指定对象对应的Version ID（在开启了多版本的情况才有）
*/
@property (strong, nonatomic) NSString *versionID;

//针对本次上传进行流量控制的限速值，必须为数字，单位默认为 bit/s。限速值设置范围为819200 - 838860800,即100KB/s - 100MB/s，如果超出该范围将返回400错误
@property (nonatomic, assign) NSInteger trafficLimit;

@end
NS_ASSUME_NONNULL_END
