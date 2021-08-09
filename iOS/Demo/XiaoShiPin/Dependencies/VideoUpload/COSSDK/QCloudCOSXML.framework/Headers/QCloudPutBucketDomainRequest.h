//
//  PutBucketDomain.h
//  PutBucketDomain
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
@class QCloudDomainConfiguration;
NS_ASSUME_NONNULL_BEGIN

/**
设置自定义域名

### 功能说明

PUT Bucket domain 用于为存储桶配置自定义域名。

### 示例

  @code

    QCloudPutBucketDomainRequest *req = [QCloudPutBucketDomainRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    req.bucket = @"examplebucket-1250000000";

    QCloudDomainConfiguration *config = [QCloudDomainConfiguration new];
    QCloudDomainRule *rule = [QCloudDomainRule new];

    // 源站状态
    rule.status = QCloudDomainStatueEnabled;
    // 域名信息
    rule.name = @"www.baidu.com";

    // 替换已存在的配置、有效值CNAME/TXT 填写则强制校验域名所有权之后，再下发配置
    rule.replace = QCloudCOSDomainReplaceTypeTxt;
    rule.type = QCloudCOSDomainTypeRest;

    // 规则描述集合的数组
    config.rules = @[rule];

    // 域名配置的规则
    req.domain  = config;

    [req setFinishBlock:^(id outputObject, NSError *error) {
        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;

    }];
    [[QCloudCOSXMLService defaultCOSXML]PutBucketDomain:req];

*/

@interface QCloudPutBucketDomainRequest : QCloudBizHTTPRequest
/**
域名配置的规则
*/
@property (strong, nonatomic) QCloudDomainConfiguration *domain;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@end
NS_ASSUME_NONNULL_END
