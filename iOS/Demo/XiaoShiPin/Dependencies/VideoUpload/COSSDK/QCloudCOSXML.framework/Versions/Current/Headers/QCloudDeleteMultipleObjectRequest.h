//
//  DeleteMultipleObject.h
//  DeleteMultipleObject
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
#import "QCloudDeleteResult.h"
@class QCloudDeleteInfo;
NS_ASSUME_NONNULL_BEGIN
/**

批量删除 COS 对象的方法.

### 功能说明

COS 支持批量删除指定 Bucket 中 对象，单次请求最大支持批量删除 1000 个 对象. 请求中删除一个不存在的对象
 ，仍然认为是成功的.

对于响应结果，COS提供 Verbose 和 Quiet 两种模式：Verbose 模式将返回每个对象的删除结果;
 Quiet 模式只返回删除报错的对象信息. 请求必须携带 Content-MD5 用来校验请求Body 的完整性.

关于批量删除 COS 对象的具体描述，请查看 https://cloud.tencent.com/document/product/436/14119.

### 示例

  @code

    QCloudDeleteMultipleObjectRequest* delteRequest = [QCloudDeleteMultipleObjectRequest new];
    delteRequest.bucket = @"examplebucket-1250000000";

    // 要删除的单个文件
    QCloudDeleteObjectInfo* deletedObject0 = [QCloudDeleteObjectInfo new];

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    deletedObject0.key = @"exampleobject";

    // 要删除的文件集合
    QCloudDeleteInfo* deleteInfo = [QCloudDeleteInfo new];

    // 布尔值，这个值决定了是否启动 Quiet 模式：
    // true：启动 Quiet 模式
    // false：启动 Verbose 模式
    // 默认值为 False
    deleteInfo.quiet = NO;

    // 存放需要删除对象信息的数组
    deleteInfo.objects = @[deletedObject0];

    // 封装了需要批量删除的多个对象的信息
    delteRequest.deleteObjects = deleteInfo;

    [delteRequest setFinishBlock:^(QCloudDeleteResult* outputObject,
                                   NSError *error) {
        // 可以从 outputObject 中获取 response 中 etag 或者自定义头部等信息

    }];

    [[QCloudCOSXMLService defaultCOSXML] DeleteMultipleObject:delteRequest];

*/
@interface QCloudDeleteMultipleObjectRequest : QCloudBizHTTPRequest
/**
存储桶名称
*/
@property (strong, nonatomic) NSString *bucket;
/**
放置被删除对象的信息
*/
@property (strong, nonatomic) QCloudDeleteInfo *deleteObjects;

- (void)setFinishBlock:(void (^_Nullable)(QCloudDeleteResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
