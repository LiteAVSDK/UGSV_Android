//
//  QCloudCICloudDataOperationsRequest.h
//  QCloudCOSXML
//
//  Created by karisli(李雪) on 2021/4/20.
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
@interface QCloudCICloudDataOperationsRequest<BodyType> : QCloudBizHTTPRequest

/**
 对象 名称
*/
@property (strong, nonatomic) NSString *object;
/**
 存储桶 名称
*/
@property (strong, nonatomic) NSString *bucket;

/**
云上数据处理
*/
@property (strong, nonatomic) QCloudPicOperations *picOperations;

/**
设置完成回调。请求完成后会通过该回调来获取结果，如果没有error，那么可以认为请求成功。

@param finishBlock 请求完成回调
*/
- (void)setFinishBlock:(void (^_Nullable)(QCloudPutObjectWatermarkResult *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;

@end


NS_ASSUME_NONNULL_END
