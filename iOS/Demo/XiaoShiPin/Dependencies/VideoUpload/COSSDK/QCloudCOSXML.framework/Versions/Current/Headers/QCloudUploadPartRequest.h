//
//  UploadPart.h
//  UploadPart
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
#import "QCloudUploadPartResult.h"
NS_ASSUME_NONNULL_BEGIN
/**

 分块上传文件的方法。

 ### 功能描述

 Upload Part 接口请求实现将对象按照分块的方式上传到 COS。最多支持10000分块，每个分块大小为1MB - 5GB，最后一个分块可以小于1MB。

 关于分块上传文件的接口的描述，请查看 https://cloud.tencent.com/document/product/436/7750.

 ### 示例

  @code

     QCloudUploadPartRequest* request = [QCloudUploadPartRequest new];
     request.bucket = @"examplebucket-1250000000";
     request.object = @"exampleobject";
     request.partNumber = 1;
     //标识本次分块上传的 ID；使用 Initiate Multipart Upload 接口初始化分块上传时会得到一个 uploadId
     //该 ID 不但唯一标识这一分块数据，也标识了这分块数据在整个文件内的相对位置
     request.uploadId = @"exampleUploadId";
     //上传的数据：支持 NSData*，NSURL(本地 URL) 和 QCloudFileOffsetBody * 三种类型
     request.body = [@"testFileContent" dataUsingEncoding:NSUTF8StringEncoding];

     [request setSendProcessBlock:^(int64_t bytesSent,
                                    int64_t totalBytesSent,
                                    int64_t totalBytesExpectedToSend) {
         //上传进度信息
     }];
     [request setFinishBlock:^(QCloudUploadPartResult* outputObject, NSError *error) {
         QCloudMultipartInfo *part = [QCloudMultipartInfo new];
         //获取所上传分块的 etag
         part.eTag = outputObject.eTag;
         part.partNumber = @"1";
         // 保存起来用于最好完成上传时使用
         self.parts = @[part];
     }];

     [[QCloudCOSXMLService defaultCOSXML]  UploadPart:request];

*/
@interface QCloudUploadPartRequest<BodyType> : QCloudBizHTTPRequest
@property (nonatomic, strong) BodyType body;
/**
对象的名称
*/
@property (strong, nonatomic) NSString *object;
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;
/**
标识本次分块上传的编号
*/
@property (assign, nonatomic) int partNumber;
/**
标识本次分块上传的 ID；
使用 Initiate Multipart Upload 接口初始化分片上传时会得到一个 uploadId，
 该 ID 不但唯一标识这一分块数据，也标识了这分块数据在整个文件内的相对位置
*/
@property (strong, nonatomic) NSString *uploadId;
@property (strong, nonatomic) NSString *contentSHA1;
@property (strong, nonatomic) NSString *expect;

//针对本次上传进行流量控制的限速值，必须为数字，单位默认为 bit/s。限速值设置范围为819200 - 838860800,即100KB/s - 100MB/s，如果超出该范围将返回400错误
@property (nonatomic, assign) NSInteger trafficLimit;

- (void)setFinishBlock:(void (^_Nullable)(QCloudUploadPartResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
