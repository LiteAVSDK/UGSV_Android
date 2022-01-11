//
//  GetBucket.h
//  GetBucket
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
#import "QCloudListBucketResult.h"
NS_ASSUME_NONNULL_BEGIN
/**
查询存储桶（Bucket) 下的部分或者全部对象的方法.

### 功能说明

COS 支持列出指定 Bucket 下的部分或者全部对象,每次默认返回的最大条目数为 1000 条.

关于查询Bucket 下的部分或者全部对象接口的具体描述，请查看https://cloud.tencent.com/document/product/436/7734.

### 示例

  @code

    QCloudGetBucketRequest* request = [QCloudGetBucketRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    // 单次返回的最大条目数量，默认1000
    request.maxKeys = 100;

    // 前缀匹配，用来规定返回的文件前缀地址
    request.prefix = @"dir1/";

    [request setFinishBlock:^(QCloudListBucketResult * result, NSError* error) {
        // result 返回具体信息
        // QCloudListBucketResult.contents 桶内文件数组
        // QCloudListBucketResult.commonPrefixes 桶内文件夹数组
        if (result.isTruncated) {
            // 表示数据被截断，需要拉取下一页数据
            self->prevPageResult = result;
        }
    }];

    [[QCloudCOSXMLService defaultCOSXML] GetBucket:request];

*/
@interface QCloudGetBucketRequest : QCloudBizHTTPRequest
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
前缀匹配，用来规定返回的文件前缀地址
*/
@property (strong, nonatomic) NSString *prefix;
/**
定界符为一个符号，如果有 Prefix，则将 Prefix 到 delimiter 之间的相同路径归为一类，定义为
 Common Prefix，然后列出所有 Common Prefix。如果没有 Prefix，则从路径起点开始
*/
@property (strong, nonatomic) NSString *delimiter;
/**
规定返回值的编码方式，可选值:url
*/
@property (strong, nonatomic) NSString *encodingType;
/**
默认以UTF-8二进制顺序列出条目，所有列出条目从marker开始
*/
@property (strong, nonatomic) NSString *marker;
/**
单次返回的最大条目数量，默认1000
*/
@property (assign, nonatomic) int maxKeys;

- (void)setFinishBlock:(void (^_Nullable)(QCloudListBucketResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
