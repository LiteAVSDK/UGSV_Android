//
//  GetService.h
//  GetService
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
#import "QCloudListAllMyBucketsResult.h"
NS_ASSUME_NONNULL_BEGIN

/**

 获取所属账户的所有存储空间列表的方法.

 ### 功能说明

 通过使用帯 Authorization 签名认证的请求，可以获取签名中 APPID 所属账户的所有存储空间列表 (Bucket list).

 关于获取所有存储空间列表接口的具体描述，请查看https://cloud.tencent.com/document/product/436/8291.

 ### 示例

  @code

     // 获取所属账户的所有存储空间列表的方法.
     // 通过使用帯 Authorization 签名认证的请求，可以获取签名中 APPID 所属账户的所有存储空间列
     // 表 (Bucket list).
     QCloudGetServiceRequest* request = [[QCloudGetServiceRequest alloc] init];
     [request setFinishBlock:^(QCloudListAllMyBucketsResult* result,
                               NSError* error) {

         // 从 result 中获取返回信息 存储桶列表
         NSArray<QCloudBucket*> *buckets = result.buckets;

         // bucket owner的信息
         QCloudOwner *owner = result.owner;
     }];
     [[QCloudCOSXMLService defaultCOSXML] GetService:request];

 */
@interface QCloudGetServiceRequest : QCloudBizHTTPRequest

/**
 请求完成后的会通过该block回调，返回结果，若error为空，即为成功。

 @param QCloudRequestFinishBlock 回调bock
 */
- (void)setFinishBlock:(void (^_Nullable)(QCloudListAllMyBucketsResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
