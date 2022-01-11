//
//  PostObjectRestore.h
//  PostObjectRestore
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
@class QCloudRestoreRequest;
NS_ASSUME_NONNULL_BEGIN
/**

 对一个归档存储（ARCHIVE）类型的对象进行恢复（解冻）的方法

 ### 功能描述

 该接口可以对一个通过 COS 归档为 archive 类型的对象进行恢复，恢复出的可读取对象是临时的
 ，您可以设置需要保持可读，以及随后删除该临时副本的时间。

 您可以用 Days 参数来指定临时对象的过期时间，若超出该时间且期间您没有发起任何复制、延长等操作，
 该临时对象将被系统自动删除。临时对象仅为 archive 类型对象的副本，被归档的源对象在此期间将始终存在。

 关于对一个归档存储类型的对象进行恢复接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/1263321

 ### 示例

  @code

    QCloudPostObjectRestoreRequest *req = [QCloudPostObjectRestoreRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    req.bucket = @"examplebucket-1250000000";

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    req.object = @"exampleobject";

    // 设置临时副本的过期时间
    req.restoreRequest.days  = 10;

    // 复原的过程类型配置信息
    req.restoreRequest.CASJobParameters.tier =QCloudCASTierStandard;

    [req setFinishBlock:^(id outputObject, NSError *error) {

        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;

    }];

    [[QCloudCOSXMLService defaultCOSXML] PostObjectRestore:req];

*/
@interface QCloudPostObjectRestoreRequest : QCloudBizHTTPRequest
/**
    存储桶名
    */
@property (strong, nonatomic) NSString *bucket;
/**
    对象名
    */
@property (strong, nonatomic) NSString *object;
/**
    恢复数据的配置信息
    */
@property (strong, nonatomic) QCloudRestoreRequest *restoreRequest;

@end
NS_ASSUME_NONNULL_END
