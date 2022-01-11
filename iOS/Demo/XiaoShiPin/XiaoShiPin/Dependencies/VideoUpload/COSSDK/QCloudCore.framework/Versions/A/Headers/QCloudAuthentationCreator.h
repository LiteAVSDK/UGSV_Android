//
//  QCloudAuthentationCreator.h
//  Pods
//
//  Created by Dong Zhao on 2017/5/2.
//
//

#import <Foundation/Foundation.h>
@class QCloudCredential;
@class QCloudSignature;
@class QCloudHTTPRequest;
@class QCloudSignatureFields;

/**
 签名创建器。通过一个密钥将创建一个网络请求的签名。

 ### 示例

  @code

     QCloudCredential* credential = [QCloudCredential new];
     credential.secretID  = @"secretID";
     credential.secretKey = @"secretKey";
     QCloudAuthentationV5Creator* creator = [[QCloudAuthentationV5Creator alloc] initWithCredential:credential];
     QCloudSignature* signature =  [creator signatureForData:urlRequst];

 */
@interface QCloudAuthentationCreator : NSObject

/**
 密钥
 */
@property (nonatomic, strong, readonly, nonnull) QCloudCredential *credential;

/**
 初始化签名创建器。

 @param credential 密钥
 @return 签名创建器
 */
- (instancetype)initWithCredential:(QCloudCredential *_Nullable)credential;

/**
 创建一个网络请求的签名。

 @param signData 将要签名的网络请求（类型不固定）
 @return 一个合法的签名
 */
- (QCloudSignature *)signatureForData:(id _Nullable)signData;
@end
