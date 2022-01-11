//
//  QCloudGetFilePreviewRequest.h
//  QCloudGetFilePreviewRequest
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
@class QCloudGetFilePreviewResult;
NS_ASSUME_NONNULL_BEGIN

/**
 COS 文档预览方法.

 ### 功能描述

 文档预览功能支持对多种文件类型生成图片格式预览，可以解决文档内容的页面展示问题，
 满足 PC、App 等多个用户端的文档在线浏览需求，适用于在线教育、企业 OA、网站转码等业务场景。

 ### 示例

  @code

    QCloudGetFilePreviewRequest *request = [[QCloudGetFilePreviewRequest alloc]init];
    request.bucket = @"桶名称";
    request.object = 文件名;
    request.page = 页码;
    request.regionName = 桶所属区域;
    [request setFinishBlock:^(NSDictionary * _Nullable result, NSError * _Nullable error) {
        返回一个字典 包含总页数，文件data
    }];
    [[QCloudCOSXMLService defaultCOSXML] GetFilePreviewObject:request];

*/

@interface QCloudGetFilePreviewRequest : QCloudBizHTTPRequest
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

/// 源数据的后缀类型，当前文档转换根据 COS 对象的后缀名来确定源数据类型。当 COS 对象没有后缀名时，
/// 可以设置该值
@property (copy, nonatomic) NSString *srcType;

/// 需转换的文档页码，从1开始计数
@property (assign, nonatomic) NSInteger page;

/**

 @param QCloudRequestFinishBlock 返回图片回调
 */
- (void)setFinishBlock:(void (^_Nullable)(QCloudGetFilePreviewResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;

@end
NS_ASSUME_NONNULL_END
