//
//  UploadPartCopy.h
//  UploadPartCopy
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
#import "QCloudCopyObjectResult.h"
NS_ASSUME_NONNULL_BEGIN
/**
 分块复制的方法.

 ### 功能描述

 COS 中复制对象可以完成如下功能:

 创建一个新的对象副本.

 复制对象并更名，删除原始对象，实现重命名

 修改对象的存储类型，在复制时选择相同的源和目标对象键，修改存储类型.

 在不同的腾讯云 COS 地域复制对象.

 修改对象的元数据，在复制时选择相同的源和目标对象键，并修改其中的元数据,复制对象时，默认将继承原对象的元数据，
 但创建日期将会按新对象的时间计算.

 当复制的对象小于等于 5 GB ，可以使用简单复制（https://cloud.tencent.com/document/product/436/14117).

 当复制对象超过 5 GB 时，必须使用分块复制（https://cloud.tencent.com/document/product/436/14118 ） 来实现复制.

 关于分块复制接口的具体描述，请查看https://cloud.tencent.com/document/product/436/8287.

### 示例

  @code

    QCloudUploadPartCopyRequest* request = [[QCloudUploadPartCopyRequest alloc] init];

    // 存储桶名称，格式为 BucketName-APPID
    request.bucket = @"examplebucket-1250000000";

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    request.object = @"exampleobject";

    // 源文件 URL 路径，可以通过 versionid 子资源指定历史版本
    request.source = @"sourcebucket-1250000000.cos.ap-guangzhou.myqcloud.com/sourceObject";

    // 在初始化分块上传的响应中，会返回一个唯一的描述符（upload ID）
    request.uploadID = uploadId;

    // 标志当前分块的序号
    request.partNumber = 1;

    [request setFinishBlock:^(QCloudCopyObjectResult* result, NSError* error) {
        QCloudMultipartInfo *part = [QCloudMultipartInfo new];

        // 获取所复制分块的 etag
        part.eTag = result.eTag;
        part.partNumber = @"1";
        // 保存起来用于最后完成上传时使用
        self.parts=@[part];

    }];

    [[QCloudCOSXMLService defaultCOSXML]UploadPartCopy:request];

*/
@interface QCloudUploadPartCopyRequest : QCloudBizHTTPRequest
/**
    存储桶名称
    */
@property (strong, nonatomic) NSString *bucket;
/**
    对象名
    */
@property (strong, nonatomic) NSString *object;
/**
    在初始化分块上传的响应中，会返回一个唯一的描述符（upload ID）
    */
@property (strong, nonatomic) NSString *uploadID;
/**
    标志当前分块的序号
    */
@property (assign, nonatomic) int64_t partNumber;
/**
    源文件 URL 路径，可以通过 versionid 子资源指定历史版本
    */
@property (strong, nonatomic) NSString *source;
/**
    源文件的字节范围，范围值必须使用 bytes=first-last 格式，first 和 last 都是基于 0 开始的偏移量。
    例如 bytes=0-9 表示你希望拷贝源文件的开头10个字节的数据，如果不指定，则表示拷贝整个文件。
    */
@property (strong, nonatomic) NSString *sourceRange;
/**
    当 Object 在指定时间后被修改，则执行操作，否则返回 412。
    可与 x-cos-copy-source-If-None-Match 一起使用，与其他条件联合使用返回冲突。
    */
@property (strong, nonatomic) NSString *sourceIfModifiedSince;
/**
    当 Object 在指定时间后未被修改，则执行操作，否则返回 412。
    可与 x-cos-copy-source-If-Match 一起使用，与其他条件联合使用返回冲突。
    */
@property (strong, nonatomic) NSString *sourceIfUnmodifiedSince;
/**
    当 Object 的 Etag 和给定一致时，则执行操作，否则返回 412。
    可与x-cos-copy-source-If-Unmodified-Since 一起使用，与其他条件联合使用返回冲突。
    */
@property (strong, nonatomic) NSString *sourceIfMatch;
/**
    当 Object 的 Etag 和给定不一致时，则执行操作，否则返回 412。
    可与 x-cos-copy-source-If-Modified-Since 一起使用，与其他条件联合使用返回冲突。
    */
@property (strong, nonatomic) NSString *sourceIfNoneMatch;
/**
    指定 Object 的 Version ID （在开启了多版本的情况下）
    */
@property (strong, nonatomic) NSString *versionID;

- (void)setFinishBlock:(void (^_Nullable)(QCloudCopyObjectResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
