//
//  QCloudCOSXMLService+ImageHelper.h
//  Pods-QCloudCOSXMLDemo
//
//  Created by garenwang on 2020/6/8.
//

#import "QCloudCOSXMLService.h"
@class QCloudPutObjectWatermarkRequest;
@class QCloudGetRecognitionObjectRequest;
@class QCloudGetFilePreviewRequest;
@class QCloudGetGenerateSnapshotRequest;
@class QCloudCICloudDataOperationsRequest;
@class QCloudCIPutObjectQRCodeRecognitionRequest;
@class QCloudQRCodeRecognitionRequest;
@class QCloudCIPicRecognitionRequest;
NS_ASSUME_NONNULL_BEGIN

@interface QCloudCOSXMLService (ImageHelper)

/**
盲水印功能.

图片上传时添加盲水印的请求包与 COS 简单上传文件接口一致，只需在请求包头部增加图片处理参数 Pic-Operations 并使用盲水印参数即可

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
- (void)PutWatermarkObject:(QCloudPutObjectWatermarkRequest *)request;

/**
COS 对象内容审核的方法.

内容审核的存量扫描功能通过借助数据万象的持久化处理接口，实现对 COS 存量数据的涉黄、涉政、
 涉暴恐以及广告引导类图片、视频的扫描。

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
- (void)GetRecognitionObject:(QCloudGetRecognitionObjectRequest *)request;

/**
COS 文档预览方法.

文档预览功能支持对多种文件类型生成图片格式预览，可以解决文档内容的页面展示问题，
 满足 PC、App 等多个用户端的文档在线浏览需求，适用于在线教育、企业 OA、网站转码等业务场景。

cos iOS SDK 中获取 COS 文档预览方法具体步骤如下：

1. 实例化 QCloudGetFilePreviewRequest。

2. 传入参数桶名称 文件名 页码（每次返回该页的预览文件图片data）

3. 调用 QCloudCOSXMLService 对象中的 GetFilePreviewObject 方法发出请求。

4. 从回调的 finishBlock 中的 outputObject 获取具体内容。

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
- (void)GetFilePreviewObject:(QCloudGetFilePreviewRequest *)request;

- (void)GetGenerateSnapshot:(QCloudGetGenerateSnapshotRequest *)request;
//云上数据处理
- (void)CloudDataOperations:(QCloudCICloudDataOperationsRequest *)request;
/**
 上传时识别二维码
 */
- (void)PutObjectQRCodeRecognition:(QCloudCIPutObjectQRCodeRecognitionRequest *)request;
/**
 下载时识别二维码
 */
- (void)CIQRCodeRecognition:(QCloudQRCodeRecognitionRequest *)request;
/**
 图片标签
 */
- (void)CIPicRecognition:(QCloudCIPicRecognitionRequest *)request;
@end

NS_ASSUME_NONNULL_END
