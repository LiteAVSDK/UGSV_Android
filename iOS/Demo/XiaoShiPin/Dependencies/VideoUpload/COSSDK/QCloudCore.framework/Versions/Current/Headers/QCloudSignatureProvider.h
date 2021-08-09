//
//  QCloudSignatureProvider.h
//  Pods
//
//  Created by Dong Zhao on 2017/3/31.
//
//

#import <Foundation/Foundation.h>
#import "QCloudSignature.h"
#import "QCloudSignatureFields.h"

typedef void (^QCloudHTTPAuthentationContinueBlock)(QCloudSignature *signature, NSError *error);

@class QCloudPath;
@class QCloudBizHTTPRequest;
@class QCloudSignatureFields;

/**
生成签名
SDK 中的请求需要用到签名，以确认访问的用户的身份，也保障了访问的安全性。当签名不正确时，大部分 COS 的服务将无法访问并且返回403错误。在 SDK
中可以生成签名，每个请求会向 QCloudServiceConfiguration 对象中的signatureProvider 对象来请求生成签名。我们可以将负责生成签名的对象在一开始赋值给
signatureProvider，该生成签名的对象需要遵循 QCloudSignatureProvider 协议，并实现生成签名的方法：
*/
@protocol QCloudSignatureProvider <NSObject>

/**
 访问腾讯云的服务需要对请求进行签名，以确定访问的用户身份，同时也保障访问的安全性。该函数返回一个基于Bolts-Task的结构，里面包裹着您对请求完成的签名。该函数使用了promise机制，更多信息请参考Bolts的设计。比如您自己搭建了一个用于签名的服务器，然后通过服务器来进行签名：

 这里使用Bolts的promise机制时考虑到，您的请求签名过程可能是一个网络过程。该过程将会非常涉及到异步操作，而promise机制可以极大的简化异步编程的复杂度。此处请您一定确保调用task的`setResult`方法或者`setError`方法。将您请求的结果通知到我们，否则后续的请求过程将无法继续。
 @param fileds 进行签名的关键字段
 @param request 需要进行签名的请求
 */
- (void)signatureWithFields:(QCloudSignatureFields *)fileds
                    request:(QCloudBizHTTPRequest *)request
                 urlRequest:(NSMutableURLRequest *)urlRequst
                  compelete:(QCloudHTTPAuthentationContinueBlock)continueBlock;
@end
