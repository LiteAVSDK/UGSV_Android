//
//  QCloudGetRecognitionObjectRequest.h
//  QCloudGetRecognitionObjectRequest
//
//  Created by tencent
//  Copyright (c) 2020年 tencent. All rights reserved.
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
@class QCloudGetRecognitionObjectResult;
NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, QCloudRecognitionEnum) {
    QCloudRecognitionPorn = 1 << 0,
    QCloudRecognitionTerrorist = 1 << 1,
    QCloudRecognitionPolitics = 1 << 2,
    QCloudRecognitionAds = 1 << 3,
};

/**
COS 对象内容审核的方法.

内容审核的存量扫描功能通过借助数据万象的持久化处理接口，实现对 COS 存量数据的涉黄、涉政、涉暴恐以及广告引导类图片、’
 视频的扫描。

cos iOS SDK 中获取 COS 对象请求的方法具体步骤如下：

1. 实例化 QCloudGetRecognitionObjectRequest，填入需要的参数。

2. 设置审核的类型 detectType

3. 调用 QCloudCOSXMLService 对象中的 GetRecognitionObject 方法发出请求。

4. 从回调的 finishBlock 中的 outputObject 获取具体内容。

### 示例

  @code

   QCloudGetRecognitionObjectRequest* request = [QCloudGetRecognitionObjectRequest new];
   request.bucket = @"bucketName"; //存储桶名称(cos v5 的 bucket格式为：xxx-appid, 如 test-1253960454)
   request.object = @"objectName";;
   request.detectType = QCloudRecognitionPorn|QCloudRecognitionAds; // 支持多种类型同时审核
   [request setFinishBlock:^(QCloudGetRecognitionObjectResult * _Nullable outputObject,
                                                        NSError * _Nullable error) {
   NSLog(@"%@",outputObject);
   }];

   [[QCloudCOSXMLService defaultCOSXML] GetRecognitionObject:request];

*/
@interface QCloudGetRecognitionObjectRequest : QCloudBizHTTPRequest
/**
 设置响应头部中的 Content-Type参数
 */
@property (strong, nonatomic) NSString *responseContentType;
/**
 设置响应头部中的Content-Language参数
 */
@property (strong, nonatomic) NSString *responseContentLanguage;
/**
 设置响应头部中的Content-Expires参数
 */
@property (strong, nonatomic) NSString *responseContentExpires;
/**
 设置响应头部中的Cache-Control参数
 */
@property (strong, nonatomic) NSString *responseCacheControl;
/**
 设置响应头部中的 Content-Disposition 参数。
 */
@property (strong, nonatomic) NSString *responseContentDisposition;
/**
 设置响应头部中的 Content-Encoding 参数。
 */
@property (strong, nonatomic) NSString *responseContentEncoding;
/**
 RFC 2616 中定义的指定文件下载范围，以字节（bytes）为单位
 */
@property (strong, nonatomic) NSString *range;
/**
 如果文件修改时间晚于指定时间，才返回文件内容。否则返回 412 (not modified)
 */
@property (strong, nonatomic) NSString *ifModifiedSince;
/**
 如果文件修改时间早于或等于指定时间，才返回文件内容。否则返回 412 (precondition failed)
 */
@property (strong, nonatomic) NSString *ifUnmodifiedModifiedSince;
/**
 当 ETag 与指定的内容一致，才返回文件。否则返回 412 (precondition failed)
 */
@property (strong, nonatomic) NSString *ifMatch;
/**
 当 ETag 与指定的内容不一致，才返回文件。否则返回 304 (not modified)
 */
@property (strong, nonatomic) NSString *ifNoneMatch;
/**
 指定 Object 的 VersionID (在开启多版本的情况下)
 */
@property (strong, nonatomic) NSString *versionID;
/**
 对象名
 */
@property (strong, nonatomic) NSString *object;
/**
 存储桶名
 */
@property (strong, nonatomic) NSString *bucket;

/// 审核类型，拥有 porn（涉黄识别）、terrorist（涉暴恐识别）、politics（涉政识别）、ads（广告识别）四种，
/// 用户可选择多种识别类型，例如 detect-type=porn,ads 表示对图片进行涉黄及广告审核
/// 可以使用或进行组合赋值 如： QCloudRecognitionPorn | QCloudRecognitionTerrorist
@property (assign, nonatomic) QCloudRecognitionEnum detectType;

/**
 设置完成回调。请求完成后会通过该回调来获取结果，如果没有error，那么可以认为请求成功。

 @param finishBlock 请求完成回调
 */
- (void)setFinishBlock:(void (^_Nullable)(QCloudGetRecognitionObjectResult *_Nullable result, NSError *_Nullable error))finishBlock;

@end
NS_ASSUME_NONNULL_END
