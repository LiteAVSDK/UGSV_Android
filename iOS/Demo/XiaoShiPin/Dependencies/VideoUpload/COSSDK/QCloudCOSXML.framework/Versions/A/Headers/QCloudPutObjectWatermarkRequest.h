//
//  PutObject.h
//  PutObject
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
#import "QCloudCOSStorageClassEnum.h"
#import "QCloudPicOperations.h"
@class QCloudPutObjectWatermarkResult;
NS_ASSUME_NONNULL_BEGIN
/**
盲水印功能.

图片上传时添加盲水印的请求包与 COS 简单上传文件接口一致，只需在请求包头部增加图片处理参数
 Pic-Operations 并使用盲水印参数即可

cos iOS SDK 盲水印上传请求的方法具体步骤如下：

1. 实例化 QCloudPutObjectWatermarkRequest，填入需要的参数。

2. QCloudPicOperations 设置水印信息

3. 调用 QCloudCOSXMLService 对象中的 PutWatermarkObject 方法发出请求。

4. 从回调的 finishBlock 中的 outputObject 获取具体内容。

### 示例

  @code

    QCloudPutObjectWatermarkRequest* put = [QCloudPutObjectWatermarkRequest new];
    put.object = @"对象名";
    put.bucket = @"桶名";
    put.body =  @"上传的图片文件";
    QCloudPicOperations * op = [[QCloudPicOperations alloc]init];
    op.is_pic_info = NO;
    QCloudPicOperationRule * rule = [[QCloudPicOperationRule alloc]init];
    rule.fileid = @"test";
    rule.text = @"水印文字"; // 水印文字只能是 [a-zA-Z0-9]
    rule.type = QCloudPicOperationRuleText;
    op.rule = @[rule];
    put.picOperations = op;
    [put setFinishBlock:^(id outputObject, NSError *error) {
       完成回调
    }];
    [[QCloudCOSXMLService defaultCOSXML] PutWatermarkObject:put];

*/
@interface QCloudPutObjectWatermarkRequest<BodyType> : QCloudBizHTTPRequest
@property (nonatomic, strong) BodyType body;
/**
 对象 名称
*/
@property (strong, nonatomic) NSString *object;
/**
 存储桶 名称
*/
@property (strong, nonatomic) NSString *bucket;
/**
RFC 2616 中定义的缓存策略，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *cacheControl;
/**
RFC 2616 中定义用于指示资源的MIME类型，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *contentType;
/**
RFC 2616 中定义的文件名称，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *contentDisposition;
/**
当使用 Expect: 100-continue 时，在收到服务端确认后，才会发送请求内容
*/
@property (strong, nonatomic) NSString *expect;
/**
RFC 2616 中定义的过期时间，将作为 Object 元数据保存
*/
@property (strong, nonatomic) NSString *expires;
@property (strong, nonatomic) NSString *contentSHA1;
/**
对象的存储级别，枚举值：STANDARD（QCloudCOSStorageStandard），STANDARD_IA（QCloudCOSStorageStandardIA），
 ARCHIVE（QCloudCOSStorageARCHIVE）。默认值：STANDARD（QCloudCOSStorageStandard）
*/
@property (assign, nonatomic) QCloudCOSStorageClass storageClass;
/**
定义 Object 的 ACL 属性。有效值：private，public-read-write，public-read；默认值：private
*/
@property (strong, nonatomic) NSString *accessControlList;
/**
 赋予被授权者读的权限。格式：id="OwnerUin";

*/
@property (strong, nonatomic) NSString *grantRead;
/**
赋予被授权者写的权限。格式：id="OwnerUin";

*/
@property (strong, nonatomic) NSString *grantWrite;
/**
赋予被授权者读写权限。格式: id="OwnerUin";

*/
@property (strong, nonatomic) NSString *grantFullControl;
/**
指定对象对应的Version ID（在开启了多版本的情况才有）
*/
@property (strong, nonatomic) NSString *versionID;

/**
给图片添加盲水印
*/
@property (strong, nonatomic) QCloudPicOperations *picOperations;

/**
设置完成回调。请求完成后会通过该回调来获取结果，如果没有error，那么可以认为请求成功。

@param finishBlock 请求完成回调
*/
- (void)setFinishBlock:(void (^_Nullable)(QCloudPutObjectWatermarkResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;

@end
NS_ASSUME_NONNULL_END
