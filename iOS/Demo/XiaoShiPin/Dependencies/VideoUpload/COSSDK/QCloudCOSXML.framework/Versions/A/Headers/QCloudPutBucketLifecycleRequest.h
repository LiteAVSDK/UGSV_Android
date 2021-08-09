//
//  PutBucketLifecycle.h
//  PutBucketLifecycle
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
@class QCloudLifecycleConfiguration;
NS_ASSUME_NONNULL_BEGIN
/**
 设置存储桶（Bucket) 生命周期配置的方法.

 ### 功能描述

 COS 支持以生命周期配置的方式来管理 Bucket 中对象的生命周期. 如果该 Bucket 已配置生命周期，
 新的配置的同时则会覆盖原有的配置. 生命周期配置包含一个或多个将应用于一组对象规则的规则集
 (其中每个规则为 COS 定义一个操作)。这些操作分为以下两种：转换操作，过期操作.

 转换操作,定义对象转换为另一个存储类的时间(例如，您可以选择在对象创建 30 天后将其转换为低频存储类别
 ，同 时也支持将数据沉降到归档存储类别.

 过期操作，指定 Object 的过期时间，COS 将会自动为用户删除过期的 Object.

 关于Bucket 生命周期配置接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/8280

### 示例

  @code

    QCloudPutBucketLifecycleRequest* request = [QCloudPutBucketLifecycleRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";
    __block QCloudLifecycleConfiguration* lifecycleConfiguration =
    [[QCloudLifecycleConfiguration alloc] init];

    // 规则描述
    QCloudLifecycleRule* rule = [[QCloudLifecycleRule alloc] init];

    // 用于唯一地标识规则
    rule.identifier = @"identifier";

    // 指明规则是否启用，枚举值：Enabled，Disabled
    rule.status = QCloudLifecycleStatueEnabled;

    // Filter 用于描述规则影响的 Object 集合
    QCloudLifecycleRuleFilter* filter = [[QCloudLifecycleRuleFilter alloc] init];

    // 指定规则所适用的前缀。匹配前缀的对象受该规则影响，Prefix 最多只能有一个
    filter.prefix = @"prefix1";

    // Filter 用于描述规则影响的 Object 集合
    rule.filter = filter;

    // 规则转换属性，对象何时转换为 Standard_IA 或 Archive
    QCloudLifecycleTransition* transition = [[QCloudLifecycleTransition alloc] init];

    // 指明规则对应的动作在对象最后的修改日期过后多少天操作：
    transition.days = 100;

    // 指定 Object 转储到的目标存储类型，枚举值： STANDARD_IA，ARCHIVE
    transition.storageClass = QCloudCOSStorageStandardIA;
    rule.transition = transition;
    request.lifeCycle = lifecycleConfiguration;

    // 生命周期配置
    request.lifeCycle.rules = @[rule];
    [request setFinishBlock:^(id outputObject, NSError* error) {

        // outputObject 包含所有的响应 http 头部
        NSDictionary* info = (NSDictionary *) outputObject;

    }];

    [[QCloudCOSXMLService defaultCOSXML] PutBucketLifecycle:request];

*/
@interface QCloudPutBucketLifecycleRequest : QCloudBizHTTPRequest
/**
设置bucket生命周期的配置信息
*/
@property (strong, nonatomic) QCloudLifecycleConfiguration *lifeCycle;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@end
NS_ASSUME_NONNULL_END
