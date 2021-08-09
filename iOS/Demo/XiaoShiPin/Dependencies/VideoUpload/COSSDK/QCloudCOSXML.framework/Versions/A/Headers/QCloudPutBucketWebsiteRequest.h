//
//  PutBucketWebsite.h
//  PutBucketWebsite
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
@class QCloudWebsiteConfiguration;
NS_ASSUME_NONNULL_BEGIN
/**

 为存储桶配置静态网站的方法

 ### 功能描述

 PUT Bucket website 请求用于为存储桶配置静态网站，您可以通过传入 XML 格式的配置文件进行配置，文件大小限制为64KB。

 关于为存储桶配置静态网站接口的具体描述，请查看https://cloud.tencent.com/document/product/436/31930.

### 示例

  @code

    // 存储桶名称，格式为 BucketName-APPID
    NSString *bucket = @"examplebucket-1250000000";
    NSString * regionName = @"ap-chengdu";

    NSString *indexDocumentSuffix = @"index.html";
    NSString *errorDocKey = @"error.html";
    NSString *derPro = @"https";
    int errorCode = 451;
    NSString * replaceKeyPrefixWith = @"404.html";
    QCloudPutBucketWebsiteRequest *putReq = [QCloudPutBucketWebsiteRequest new];
    putReq.bucket = bucket;

    QCloudWebsiteConfiguration *config = [QCloudWebsiteConfiguration new];

    QCloudWebsiteIndexDocument *indexDocument = [QCloudWebsiteIndexDocument new];

    // 指定索引文档的对象键后缀。例如指定为index.html，那么当访问到存储桶的根目录时，会自动返回
    // index.html 的内容，或者当访问到article/目录时，会自动返回 article/index.html的内容
    indexDocument.suffix = indexDocumentSuffix;
    // 索引文档配置
    config.indexDocument = indexDocument;

    // 错误文档配置
    QCloudWebisteErrorDocument *errDocument = [QCloudWebisteErrorDocument new];
    errDocument.key = errorDocKey;
    // 指定通用错误文档的对象键，当发生错误且未命中重定向规则中的错误码重定向时，将返回该对象键的内容
    config.errorDocument = errDocument;

    // 重定向所有请求配置
    QCloudWebsiteRedirectAllRequestsTo *redir = [QCloudWebsiteRedirectAllRequestsTo new];
    redir.protocol  = derPro;
    // 指定重定向所有请求的目标协议，只能设置为 https
    config.redirectAllRequestsTo = redir;

    // 单条重定向规则配置
    QCloudWebsiteRoutingRule *rule = [QCloudWebsiteRoutingRule new];

    // 重定向规则的条件配置
    QCloudWebsiteCondition *contition = [QCloudWebsiteCondition new];
    contition.httpErrorCodeReturnedEquals = errorCode;
    rule.condition = contition;

    // 重定向规则的具体重定向目标配置
    QCloudWebsiteRedirect *webRe = [QCloudWebsiteRedirect new];
    webRe.protocol = derPro;

    // 指定重定向规则的具体重定向目标的对象键，替换方式为替换原始请求中所匹配到的前缀部分，
    // 仅可在 Condition 为 KeyPrefixEquals 时设置
    webRe.replaceKeyPrefixWith = replaceKeyPrefixWith;
    rule.redirect = webRe;

    QCloudWebsiteRoutingRules *routingRules = [QCloudWebsiteRoutingRules new];
    routingRules.routingRule = @[rule];

    // 重定向规则配置，最多设置100条 RoutingRule
    config.rules = routingRules;
    putReq.websiteConfiguration  = config;

    [putReq setFinishBlock:^(id outputObject, NSError *error) {
        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;
    }];

    [[QCloudCOSXMLService defaultCOSXML] PutBucketWebsite:putReq];

*/
@interface QCloudPutBucketWebsiteRequest : QCloudBizHTTPRequest
/**
设置Website的配置信息
*/
@property (strong, nonatomic) QCloudWebsiteConfiguration *websiteConfiguration;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@end
NS_ASSUME_NONNULL_END
