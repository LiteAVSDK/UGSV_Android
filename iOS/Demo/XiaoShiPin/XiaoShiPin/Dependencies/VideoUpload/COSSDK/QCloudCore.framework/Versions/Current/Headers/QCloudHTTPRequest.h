//
//  QCloudHTTPRequest.h
//  QCloudNetworking
//
//  Created by tencent on 15/9/25.
//  Copyright © 2015年 QCloudTernimalLab. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "QCloudRequestData.h"
#import "QCloudRequestSerializer.h"
#import "QCloudResponseSerializer.h"
#import "QCloudHTTPRequestDelegate.h"
#import "QCloudAbstractRequest.h"
@class QCloudHTTPRetryHanlder;
@class QCloudHTTPSessionManager;
@class QCloudService;

typedef void (^QCloudHTTPRequestConfigure)(QCloudRequestSerializer *_Nonnull requestSerializer,
                                           QCloudResponseSerializer *_Nonnull responseSerializer);

/**
 network base request
 */
@interface QCloudHTTPRequest : QCloudAbstractRequest {
@protected
    QCloudRequestData *_requestData;
    QCloudRequestSerializer *_requestSerializer;
    QCloudResponseSerializer *_responseSerializer;
    QCloudHTTPRetryHanlder *_retryHandler;
}
@property (nonatomic, strong, readonly) QCloudRequestSerializer *_Nonnull requestSerializer;
@property (nonatomic, strong, readonly) QCloudRequestData *_Nonnull requestData;
@property (nonatomic, strong, readonly) QCloudResponseSerializer *_Nonnull responseSerializer;
@property (nonatomic, strong, readonly) NSURLRequest *_Nullable urlRequest;

/**
 该任务所处的服务
 */
@property (nonatomic, weak) QCloudService *_Nullable runOnService;
/**
 如果存在改参数，则数据会下载到改路径指名的地址下面，而不会写入内存中。
 */
@property (nonatomic, strong) NSURL *_Nonnull downloadingURL;

/**
 本地已经下载的数据偏移量，如果使用则会从改位置开始下载，如果不使用，则从头开始下载，如果您使用了Range参数，则需要注意改参数。
 */
@property (nonatomic, assign) int64_t localCacheDownloadOffset;
/**
 在特殊网络错误下，进行重试的策略，默认是不进行重试。可通过集成QCloudHTTPRetryHandler来自定义重试的出发条件和重试策略。
 */
@property (nonatomic, strong, readonly) QCloudHTTPRetryHanlder *_Nullable retryPolicy;

/**
  服务器返回数据，当服务器有返回数据的时候，该字段有值，其他时候该字段无意义
 */
@property (nonatomic, strong, readonly) NSData *_Nullable responseData;

/**
  服务器响应结构，当服务器有返回数据的时候，该字段有值，其他时候该字段无意义
 */
@property (nonatomic, strong, readonly) NSHTTPURLResponse *_Nullable httpURLResponse;

/**
 当系统调用结束，并且出错的情况下，使用该字段表示错误信息，注意：只有在错误的情况下，该字段才会有数据
 */
@property (nonatomic, strong, readonly) NSError *_Nullable httpURLError;

/**
  用来配置协议中HTTP的请求参数和解析
 */
@property (nonatomic, strong) QCloudHTTPRequestConfigure _Nonnull configureBlock;

/**
  当前的ConfiureBlock为空的时候，会调用该函数加载配置函数。
 */
- (void)loadConfigureBlock;

- (void)setConfigureBlock:(void (^_Nonnull)(QCloudRequestSerializer *_Nonnull requestSerializer,
                                            QCloudResponseSerializer *_Nonnull responseSerializer))configBlock;

/**
 构架RequestData，加载自定义的参数
 */
- (BOOL)buildRequestData:(NSError *_Nullable __autoreleasing *_Nullable)error;

/**
 构建真实网络请求需要的NSURLRequest

 @param error 当出错的时候，表示出错信息
 @return 用于构建真实网络请求的NSURLRequest
 */
- (NSURLRequest *_Nullable)buildURLRequest:(NSError *_Nullable __autoreleasing *_Nullable)error;

@end

#define SUPER_BUILD_REUQSET_DATA         \
    if (![super buildRequestData:error]) \
        return NO;

@interface QCloudHTTPRequest (SubClass)

/**
  将要开始发送请求的时候，将会调用该接口通知子类
 */
- (void)willStart;

/**
  加载错误重试策略，只有在第一次调用retryPolicy并且其值为空的时候会调用该方法来加载重试策略。子类可以重载该方法返回自己的重试策略。父类的默认行为是返回一个不进行重试的策略。
  */
- (void)loadRetryPolicy;

- (void)setConfigureBlock:(void (^_Nonnull)(QCloudRequestSerializer *_Nonnull requestSerializer,
                                            QCloudResponseSerializer *_Nonnull responseSerializer))confBlock;

- (BOOL)prepareInvokeURLRequest:(NSMutableURLRequest *_Nonnull)urlRequest error:(NSError *_Nullable __autoreleasing *_Nullable)error;
@end

@class RNAsyncBenchMark;

#pragma deal with response
@interface QCloudHTTPRequest ()

/**
 服务器返回response的时候处理，可以在这里做处理看是否接受后序的数据

 @param response 服务器返回的response（主要包含头部）
 @return NSURLSessionResponseDisposition
 */
- (NSURLSessionResponseDisposition)reciveResponse:(NSURLResponse *_Nullable)response;

- (void)onReviveErrorResponse:(NSURLResponse *_Nullable)prsponse error:(NSError *_Nullable)error;
- (void)onReciveRespone:(NSURLResponse *_Nullable)response data:(NSData *_Nullable)data;

@end
