//
//  PutObjectCopy.h
//  PutObjectCopy
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
#import "QCloudCOSStorageClassEnum.h"
NS_ASSUME_NONNULL_BEGIN
/**
 简单复制对象的方法.

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

 关于简单复制接口的具体描述，请查看https://cloud.tencent.com/document/product/436/10881.

 ### 示例

  @code

    QCloudPutObjectCopyRequest* request = [[QCloudPutObjectCopyRequest alloc] init];

     // 存储桶名称，格式为 BucketName-APPID
     request.bucket = @"examplebucket-1250000000";

     // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
     request.object = @"exampleobject";

     // 是否拷贝元数据，枚举值：Copy，Replaced，默认值 Copy。
     // 假如标记为 Copy，忽略 Header 中的用户元数据信息直接复制
     // 假如标记为 Replaced，按 Header 信息修改元数据。当目标路径和原路径一致，即用户试图修改元数据时，必须为 Replaced
     request.metadataDirective = @"Copy";

     // 定义 Object 的 ACL 属性，有效值：private，public-read，default。
     // 默认值：default（继承 Bucket 权限）。
     // 注意：当前访问策略条目限制为1000条，如果您无需进行 Object ACL 控制，请填 default
     // 或者此项不进行设置，默认继承 Bucket 权限。
     request.accessControlList = @"default";

     // 源对象所在的路径
     request.objectCopySource =
     @"sourcebucket-1250000000.cos.ap-guangzhou.myqcloud.com/sourceObject";

     // 指定源文件的 versionID，只有开启或开启后暂停的存储桶，才会响应此参数
     request.versionID = @"objectVersion1";

     [request setFinishBlock:^(QCloudCopyObjectResult * _Nonnull result,
                               NSError * _Nonnull error) {
         // result 返回具体信息

     }];
     [[QCloudCOSXMLService defaultCOSXML]  PutObjectCopy:request];

*/
@interface QCloudPutObjectCopyRequest : QCloudBizHTTPRequest
/**
对象名
*/
@property (strong, nonatomic) NSString *object;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
源文件 URL 路径，可以通过 versionid 子资源指定历史版本
*/
@property (strong, nonatomic) NSString *objectCopySource;
/**
是否拷贝元数据，枚举值：Copy, Replaced，默认值 Copy。假如标记为 Copy，忽略 Header 中的用户元数据信息直接复制；
 假如标记为 Replaced，按 Header 信息修改元数据。当目标路径和原路径一致，即用户试图修改元数据时，必须为 Replaced
*/
@property (strong, nonatomic) NSString *metadataDirective;
/**
当 Object 在指定时间后被修改，则执行操作，否则返回 412。可与 x-cos-copy-source-If-None-Match 一起使用，
 与其他条件联合使用返回冲突。
*/
@property (strong, nonatomic) NSString *objectCopyIfModifiedSince;
/**
当 Object 在指定时间后未被修改，则执行操作，否则返回 412。可与 x-cos-copy-source-If-Match 一起使用，
 与其他条件联合使用返回冲突。
*/
@property (strong, nonatomic) NSString *objectCopyIfUnmodifiedSince;
/**
当 Object 的 Etag 和给定一致时，则执行操作，否则返回 412。可与x-cos-copy-source-If-Unmodified-Since 一起使用，
 与其他条件联合使用返回冲突。
*/
@property (strong, nonatomic) NSString *objectCopyIfMatch;
/**
当 Object 的 Etag 和给定不一致时，则执行操作，否则返回 412。可与 x-cos-copy-source-If-Modified-Since 一起使用，
 与其他条件联合使用返回冲突。
*/
@property (strong, nonatomic) NSString *objectCopyIfNoneMatch;
/**
对象的存储级别，枚举值：STANDARD（QCloudCOSStorageStandard），STANDARD_IA（QCloudCOSStorageStandardIA）。
 默认值：STANDARD（QCloudCOSStorageStandard）
*/
@property (assign, nonatomic) QCloudCOSStorageClass storageClass;
/**
    定义 Object 的 ACL 属性。有效值：private，public-read-write，public-read；默认值：private
*/
@property (strong, nonatomic) NSString *accessControlList;
/**
    赋予被授权者读的权限。格式：x-cos-grant-read: id="[OwnerUin]"
*/
@property (strong, nonatomic) NSString *grantRead;
/**
    赋予被授权者写的权限。格式：x-cos-grant-write: id="[OwnerUin]"；
*/
@property (strong, nonatomic) NSString *grantWrite;
/**
    赋予被授权者读写权限。格式:x-cos-grant-full-control: id="[OwnerUin]"
*/
@property (strong, nonatomic) NSString *grantFullControl;
/**
    指定源文件的versionID,只有开启或开启后暂停的存储桶，才会响应此参数
    */
@property (strong, nonatomic) NSString *versionID;

- (void)setFinishBlock:(void (^_Nullable)(QCloudCopyObjectResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
