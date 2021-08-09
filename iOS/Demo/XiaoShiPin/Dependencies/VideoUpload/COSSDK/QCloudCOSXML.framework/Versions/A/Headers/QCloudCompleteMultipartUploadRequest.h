//
//  CompleteMultipartUpload.h
//  CompleteMultipartUpload
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
#import "QCloudUploadObjectResult.h"
@class QCloudCompleteMultipartUploadInfo;
NS_ASSUME_NONNULL_BEGIN
/**

 完成整个分块上传的方法.

 ### 功能描述

 当使用分块上传（uploadPart(UploadPartRequest)）完对象的所有块以后，
 必须调用该 completeMultiUpload(CompleteMultiUploadRequest)
 或者 completeMultiUploadAsync(CompleteMultiUploadRequest, CosXmlResultListener)
 来完成整个文件的分块上传.且在该请求的 Body 中需要给出每一个块的 PartNumber 和 ETag，
 用来校验块的准确性.

 分块上传适合于在弱网络或高带宽环境下上传较大的对象.SDK 支持自行切分对象并分别调用
 uploadPart(UploadPartRequest)上传各 个分块.

 关于完成整个分片上传接口的描述，请查看 https://cloud.tencent.com/document/product/436/7742.


### 示例

  @code

    QCloudCompleteMultipartUploadRequest *completeRequst = [QCloudCompleteMultipartUploadRequest new];

    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "dir1/object1"
    completeRequst.object = @"exampleobject";

    // 存储桶名称，格式为 BucketName-APPID
    completeRequst.bucket = @"examplebucket-1250000000";

    // 本次要查询的分块上传的 uploadId，可从初始化分块上传的请求结果 QCloudInitiateMultipartUploadResult 中得到
    completeRequst.uploadId = uploadId;

    // 已上传分块的信息
    QCloudCompleteMultipartUploadInfo *partInfo = [QCloudCompleteMultipartUploadInfo new];
    NSMutableArray * parts = [self.parts mutableCopy];

    // 对已上传的块进行排序
    [parts sortUsingComparator:^NSComparisonResult(QCloudMultipartInfo*  _Nonnull obj1,
                                                   QCloudMultipartInfo*  _Nonnull obj2) {
        int a = obj1.partNumber.intValue;
        int b = obj2.partNumber.intValue;

        if (a < b) {
            return NSOrderedAscending;
        } else {
            return NSOrderedDescending;
        }
    }];
    partInfo.parts = [parts copy];
    completeRequst.parts = partInfo;

    [completeRequst setFinishBlock:^(QCloudUploadObjectResult * _Nonnull result,
                                     NSError * _Nonnull error) {
        // 从 result 中获取上传结果
    }];

    [[QCloudCOSXMLService defaultCOSXML] CompleteMultipartUpload:completeRequst];

*/
@interface QCloudCompleteMultipartUploadRequest : QCloudBizHTTPRequest
/**
对象名
*/
@property (strong, nonatomic) NSString *object;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
本次分片上传的UploadID
*/
@property (strong, nonatomic) NSString *uploadId;
/**
完成分片上传的信息
*/
@property (strong, nonatomic) QCloudCompleteMultipartUploadInfo *parts;

- (void)setFinishBlock:(void (^_Nullable)(QCloudUploadObjectResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
