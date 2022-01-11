//
//  PutBucketCORS.h
//  PutBucketCORS
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
@class QCloudCORSConfiguration;
NS_ASSUME_NONNULL_BEGIN
/**
 设置存储桶（Bucket） 的跨域配置信息的方法.

 ### 功能描述

 跨域访问配置的预请求是指在发送跨域请求之前会发送一个 OPTIONS 请求并带上特定的来源域，
 HTTP 方 法和 header 信息等给 COS，以决定是否可以发送真正的跨域请求. 当跨域访问配置不存在时，
 请求返回403 Forbidden.

 默认情况下，Bucket的持有者可以直接配置 Bucket的跨域信息 ，Bucket 持有者也可以将配置权限授予其他用户.
 新的配置是覆盖当前的所有配置信 息，而不是新增一条配置.可以通过传入 XML 格式的配置文件来实现配置，文件大小限制为64 KB.

 关于设置 Bucket 的跨域配置信息接口的具体描述，请查看 https://cloud.tencent.com/document/product/436/8279.

### 示例

  @code

    QCloudPutBucketCORSRequest* putCORS = [QCloudPutBucketCORSRequest new];
    QCloudCORSConfiguration* cors = [QCloudCORSConfiguration new];

    QCloudCORSRule* rule = [QCloudCORSRule new];

    // 配置规则的 ID
    rule.identifier = @"rule1";

    // 在发送 OPTIONS 请求时告知服务端，接下来的请求可以使用的 HTTP 请求头部，支持通配符 *
    rule.allowedHeader = @[@"origin",@"host",@"accept",
                           @"content-type",@"authorization"];
    rule.exposeHeader = @"ETag";

    // 允许的 HTTP 操作，例如：GET，PUT，HEAD，POST，DELETE
    rule.allowedMethod = @[@"GET",@"PUT",@"POST", @"DELETE", @"HEAD"];

    // 设置 OPTIONS 请求得到结果的有效期
    rule.maxAgeSeconds = 3600;

    // 允许的访问来源，支持通配符 *，格式为：协议://域名[:端口]
    rule.allowedOrigin = @"http://cloud.tencent.com";
    cors.rules = @[rule];
    putCORS.corsConfiguration = cors;

    // 存储桶名称，格式为 BucketName-APPID
    putCORS.bucket = @"examplebucket-1250000000";

    [putCORS setFinishBlock:^(id outputObject, NSError *error) {
        // 可以从 outputObject 中获取服务器返回的 header 信息
        NSDictionary * result = (NSDictionary *)outputObject;
    }];

    [[QCloudCOSXMLService defaultCOSXML] PutBucketCORS:putCORS];

*/
@interface QCloudPutBucketCORSRequest : QCloudBizHTTPRequest
/**
设置跨bucket域访问的配置信息
*/
@property (strong, nonatomic) QCloudCORSConfiguration *corsConfiguration;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

@end
NS_ASSUME_NONNULL_END
