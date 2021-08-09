//
//  QCloudAbstractRequest.h
//  Pods
//
//  Created by Dong Zhao on 2017/3/10.
//
//

#import <Foundation/Foundation.h>
#import "QCloudHTTPRequestDelegate.h"
#import "QCloudHttpMetrics.h"
#import "QCloudSignatureProvider.h"
typedef double QCloudAbstractRequestPriority;

#define QCloudAbstractRequestPriorityHigh 3.0
#define QCloudAbstractRequestPriorityNormal 2.0
#define QCloudAbstractRequestPriorityLow 1.0
#define QCloudAbstractRequestPriorityBackground 0.0
typedef void (^QCloudRequestSendProcessBlock)(int64_t bytesSent, int64_t totalBytesSent, int64_t totalBytesExpectedToSend);
typedef void (^QCloudRequestDownProcessBlock)(int64_t bytesDownload, int64_t totalBytesDownload, int64_t totalBytesExpectedToDownload);
typedef void (^QCloudRequestDownProcessWithDataBlock)(int64_t bytesDownload, int64_t totalBytesDownload, int64_t totalBytesExpectedToDownload,
                                                      NSData *receivedData);
/**
 请求的抽象基类，该类封装了用于进行request-response模式数据请求的通用属性和接口。包括发起请求，相应结果，优先级处理，性能监控能常见特性。
 */
@interface QCloudAbstractRequest : NSObject {
@protected
    int64_t _requestID;
}
/**
 签名信息的回调接口，该委托必须实现。签名是腾讯云进行服务时进行用户身份校验的关键手段，同时也保障了用户访问的安全性。该委托中通过函数回调来提供签名信息。
 */
@property (nonatomic, strong) id<QCloudSignatureProvider> signatureProvider;
@property (nonatomic, assign) BOOL enableQuic;
@property (atomic, assign) BOOL forbidCancelled;
@property (atomic, assign, readonly) BOOL canceled;
@property (nonatomic, assign, readonly) int64_t requestID;
@property (nonatomic, assign) QCloudAbstractRequestPriority priority;
@property (nonatomic, strong, readonly) QCloudHttpMetrics *_Nullable benchMarkMan;
@property (atomic, assign, readonly) BOOL finished;
@property (nonatomic, assign) NSTimeInterval timeoutInterval;
/**
  协议执行结果向外通知的委托（delegate）主要包括成功和失败两种情况。与Block方式并存，当两者都设置的时候都会通知。
 */
@property (nonatomic, weak) id<QCloudHTTPRequestDelegate> _Nullable delegate;
/**
 协议执行结果向外通知的Block，与delegate方式并存，当两者都设置的时候都会通知。
 */
@property (nonatomic, strong) QCloudRequestFinishBlock _Nullable finishBlock;

@property (nonatomic, strong) QCloudRequestSendProcessBlock _Nullable sendProcessBlock;

@property (nonatomic, strong) QCloudRequestDownProcessBlock _Nullable downProcessBlock;

@property (nonatomic, strong) QCloudRequestDownProcessWithDataBlock _Nullable downProcessWithDataBlock;
- (void)setFinishBlock:(void (^_Nullable)(id _Nullable outputObject, NSError *_Nullable error))QCloudRequestFinishBlock;
- (void)setDownProcessBlock:(void (^_Nullable)(int64_t bytesDownload, int64_t totalBytesDownload,
                                               int64_t totalBytesExpectedToDownload))downloadProcessBlock;
- (void)setDownProcessWithDataBlock:(void (^_Nullable)(int64_t bytesDownload, int64_t totalBytesDownload,

                                                       int64_t totalBytesExpectedToDownload, NSData *receiveData))downloadProcessWithDataBlock;

- (void)setSendProcessBlock:(void (^_Nullable)(int64_t bytesSent, int64_t totalBytesSent, int64_t totalBytesExpectedToSend))sendProcessBlock;

/**
   请求过程出错，进行处理。默认只处理HTTP协议层错误信息。并进行delegate的通知。
      @param error 请求过程出错信息，默认只处理HTTP层错误信息
 */
- (void)onError:(NSError *_Nonnull)error;

/**
   请求过程成功，并获取到了数据，进行处理。并进行delegate的通知。
      @param object  获取到的数据，经过responseserilizer处理的后的数据。
 */
- (void)onSuccess:(id _Nonnull)object;

- (void)notifySuccess:(id _Nonnull)object;
- (void)notifyError:(NSError *_Nonnull)error;
- (void)notifyDownloadProgressBytesDownload:(int64_t)bytesDownload
                         totalBytesDownload:(int64_t)totalBytesDownload
               totalBytesExpectedToDownload:(int64_t)totalBytesExpectedToDownload;

- (void)notifyDownloadProgressBytesDownload:(int64_t)bytesDownload
                         totalBytesDownload:(int64_t)totalBytesDownload
               totalBytesExpectedToDownload:(int64_t)totalBytesExpectedToDownload
                               receivedData:(NSData *_Nullable)data;

- (void)notifySendProgressBytesSend:(int64_t)bytesSend
                     totalBytesSend:(int64_t)totalBytesSend
           totalBytesExpectedToSend:(int64_t)totalBytesExpectedToSend;
- (void)cancel;
- (void)waitForComplete;
- (void)configTaskResume;
@end
