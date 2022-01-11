//
//  ListObjectVersions.h
//  ListObjectVersions
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
#import "QCloudListVersionsResult.h"
NS_ASSUME_NONNULL_BEGIN
/**
 获取存储桶内的所有对象及其历史版本信息

 ### 功能说明

 GET Bucket Object versions 接口用于拉取存储桶内的所有对象及其历史版本信息，您可以通过指定参
 数筛选出存储桶内部分对象及其历史版本信息。该 API 的请求者需要对存储桶有读取权限。

 关于获取存储桶内的所有对象及其历史版本信息接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/35521

 ### 示例

  @code

    QCloudListObjectVersionsRequest* listObjectVersionsRequest =
        [[QCloudListObjectVersionsRequest alloc] init];

    // 存储桶名称
    listObjectVersionsRequest.bucket = @"bucketname";

    // 一页请求数据条目数，默认 1000
    listObjectVersionsRequest.maxKeys = 100;

    [listObjectVersionsRequest setFinishBlock:^(QCloudListVersionsResult * _Nonnull result,
                                                NSError * _Nonnull error) {
        // 已删除的文件
        NSArray<QCloudDeleteMarker*> *deleteMarker = result.deleteMarker;

        // 对象版本条目
        NSArray<QCloudVersionContent*> *versionContent = result.versionContent;

    }];

    [[QCloudCOSXMLService defaultCOSXML] ListObjectVersions:listObjectVersionsRequest];

*/
@interface QCloudListObjectVersionsRequest : QCloudBizHTTPRequest
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
前缀匹配，用来规定返回的文件前缀地址
*/
@property (strong, nonatomic) NSString *prefix;
/**
定界符为一个符号，如果有 Prefix，则将 Prefix 到 delimiter 之间的相同路径归为一类，定义为 Common Prefix，
 然后列出所有 Common Prefix。如果没有 Prefix，则从路径起点开始
*/
@property (strong, nonatomic) NSString *delimiter;
/**
规定返回值的编码方式，可选值:url
*/
@property (strong, nonatomic) NSString *encodingType;
/**
起始对象键标记，从该标记之后（不含）按照 UTF-8 字典序返回对象版本条目
*/
@property (strong, nonatomic) NSString *keyMarker;
/**
起始版本 ID 标记，从该标记之后（不含）返回对象版本条目
*/
@property (strong, nonatomic) NSString *versionIdMarker;
/**
单次返回的最大条目数量，默认1000
*/
@property (assign, nonatomic) int maxKeys;

- (void)setFinishBlock:(void (^_Nullable)(QCloudListVersionsResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
