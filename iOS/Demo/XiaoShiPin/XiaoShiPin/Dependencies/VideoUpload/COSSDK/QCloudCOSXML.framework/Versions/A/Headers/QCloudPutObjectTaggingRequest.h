//
//  PutObjectTagging.h
//  PutObjectTagging
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
@class QCloudTagging;
NS_ASSUME_NONNULL_BEGIN
/**
 设置存储桶标签的方法

 ### 功能说明

 PUT Bucket tagging 用于为已存在的存储桶设置标签。

 关于为已存在的存储桶设置标签接口描述，请查看 https://cloud.tencent.com/document/product/436/34838.

 ### 示例

  @code

     QCloudPutObjectTaggingRequest *putReq = [QCloudPutObjectTaggingRequest new];

     // 存储桶名称，格式为 BucketName-APPID
     putReq.bucket = @"examplebucket-1250000000";

     // 标签集合
     QCloudTagging *taggings = [QCloudTagging new];

     QCloudTag *tag1 = [QCloudTag new];

     // 标签的 Key，长度不超过128字节, 支持英文字母、数字、空格、加号、减号、下划线、等号、点号、
     // 冒号、斜线
     tag1.key = @"age";

     // 标签的 Value，长度不超过256字节, 支持英文字母、数字、空格、加号、减号、下划线、等号、点号
     // 、冒号、斜线
     tag1.value = @"20";
     QCloudTag *tag2 = [QCloudTag new];
     tag2.key = @"name";
     tag2.value = @"karis";

     // 标签集合，最多支持10个标签
     QCloudTagSet *tagSet = [QCloudTagSet new];
     tagSet.tag = @[tag1,tag2];
     taggings.tagSet = tagSet;

     // 标签集合
     putReq.taggings = taggings;

     [putReq setFinishBlock:^(id outputObject, NSError *error) {
         // outputObject 包含所有的响应 http 头部
         NSDictionary* info = (NSDictionary *) outputObject;
     }];
     [[QCloudCOSXMLService defaultCOSXML] PutObjectTagging:putReq];


 */
@interface QCloudPutObjectTaggingRequest : QCloudBizHTTPRequest

/**
 标签集合
 */
@property (strong, nonatomic) QCloudTagging *taggings;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

/**
 对象 名称
*/
@property (strong, nonatomic) NSString *object;
@end
NS_ASSUME_NONNULL_END
