//
//  CIPutObjectQRCodeRecognition.h
//  CIPutObjectQRCodeRecognition
//
//  Created by tencent
//  Copyright (c) 2015年 tencent. All rights reserved.
//
//   ██████╗  ██████╗██╗      ██████╗ ██╗   ██╗██████╗     ████████╗███████╗██████╗ ███╗   ███╗██╗███╗   ██╗ █████╗ ██╗         ██╗      █████╗ ██████╗
//  ██╔═══██╗██╔════╝██║     ██╔═══██╗██║   ██║██╔══██╗    ╚══██╔══╝██╔════╝██╔══██╗████╗ ████║██║████╗  ██║██╔══██╗██║         ██║     ██╔══██╗██╔══██╗
//  ██║   ██║██║     ██║     ██║   ██║██║   ██║██║  ██║       ██║   █████╗  ██████╔╝██╔████╔██║██║██╔██╗ ██║███████║██║         ██║     ███████║██████╔╝
//  ██║▄▄ ██║██║     ██║     ██║   ██║██║   ██║██║  ██║       ██║   ██╔══╝  ██╔══██╗██║╚██╔╝██║██║██║╚██╗██║██╔══██║██║         ██║     ██╔══██║██╔══██╗
//  ╚██████╔╝╚██████╗███████╗╚██████╔╝╚██████╔╝██████╔╝       ██║   ███████╗██║  ██║██║ ╚═╝ ██║██║██║ ╚████║██║  ██║███████╗    ███████╗██║  ██║██████╔╝
//   ╚══▀▀═╝  ╚═════╝╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝        ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝    ╚══════╝╚═╝  ╚═╝╚═════╝
//
//
//                                                                              _             __                 _                _
//                                                                             (_)           / _|               | |              | |
//                                                          ___  ___ _ ____   ___  ___ ___  | |_ ___  _ __    __| | _____   _____| | ___  _ __   ___ _ __ ___
//                                                         / __|/ _ \ '__\ \ / / |/ __/ _ \ |  _/ _ \| '__|  / _` |/ _ \ \ / / _ \ |/ _ \| '_ \ / _ \ '__/ __|
//                                                         \__ \  __/ |   \ V /| | (_|  __/ | || (_) | |    | (_| |  __/\ V /  __/ | (_) | |_) |  __/ |  \__
//                                                         |___/\___|_|    \_/ |_|\___\___| |_| \___/|_|     \__,_|\___| \_/ \___|_|\___/| .__/ \___|_|  |___/
//    ______ ______ ______ ______ ______ ______ ______ ______                                                                            | |
//   |______|______|______|______|______|______|______|______|                                                                           |_|
//



#import <Foundation/Foundation.h>
#import <QCloudCore/QCloudCore.h>
#import "QCloudCIQRCodeRecognitionResults.h"
#import "QCloudCOSStorageClassEnum.h"
#import "QCloudPicOperations.h"
NS_ASSUME_NONNULL_BEGIN
/**
存储桶名称
*/
@interface QCloudCIPutObjectQRCodeRecognitionRequest <BodyType> : QCloudBizHTTPRequest
@property (nonatomic, strong) BodyType body;
/**
存储桶名称
*/
@property (strong, nonatomic) NSString *bucket;
/**
要识别的对象
*/
@property (strong, nonatomic) NSString *object;

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


/*
在进行HTTP请求的时候，可以通过设置该参数来设置自定义的一些头部信息。
通常情况下，携带特定的额外HTTP头部可以使用某项功能，如果是这类需求，可以通过设置该属性来实现。
*/
@property (strong, nonatomic) NSDictionary* customHeaders;
/**
二维码识别参数
*/
@property (strong, nonatomic) QCloudPicOperations *picOperations;
- (void) setFinishBlock:(void (^)(QCloudCIQRCodeRecognitionResults* result, NSError * error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
