//
//  QCloudCOSXMLService.h
//  QCloudCOSXMLService
//
//  Created by tencent
//
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
#import <QCloudCore/QCloudService.h>
#import <QCloudCore/QCloudCore.h>

NS_ASSUME_NONNULL_BEGIN

/**
 QCloudCOSXMLService 是对所有接口请求发起的封装；通过QCloudCOSXMLService实例来发起网络请求

 详情请查看：https://cloud.tencent.com/document/product/436/11280


 配置QCloudCOSXMLService
 1：实例化 QCloudServiceConfiguration 对象：

    QCloudServiceConfiguration* configuration = [QCloudServiceConfiguration new];
    configuration.appID = @"APPID"  //腾讯云账号的 APPID;

 2:实例化 QCloudCOSXMLService 对象：

    + (QCloudCOSXMLService*) registerDefaultCOSXMLWithConfiguration
    :(QCloudServiceConfiguration*)configuration;

 3：实例化 QCloudCOSTransferManagerService 对象：

    + (QCloudCOSTransferMangerService*) registerDefaultCOSTransferMangerWithConfiguration
    :(QCloudServiceConfiguration*)configuration;

 使用步骤：

 1：实例并初始化好要使用的request；

 2：使用[QCloudCOSXMLService defaultCOSXML] 获取到 QCloudCOSXMLService实例；

 3：调用对应发起请求的方法：如get****,post****,put****,delete****,

 */

@interface QCloudCOSXMLService : QCloudService
#pragma hidden super selectors
- (int)performRequest:(QCloudBizHTTPRequest *)httpRequst NS_UNAVAILABLE;
- (int)performRequest:(QCloudBizHTTPRequest *)httpRequst withFinishBlock:(QCloudRequestFinishBlock)block NS_UNAVAILABLE;

#pragma Factory
/**
 获取默认的cosxml服务
 */
+ (QCloudCOSXMLService *)defaultCOSXML;

/// 获取指定key的cosxml服务
/// @param key 要获取的cosxml服务对应的key
+ (QCloudCOSXMLService *)cosxmlServiceForKey:(NSString *)key;
#pragma hidden super selectors
/**
 检查是否存在key对应的service

 @param key key
 @return 存在与否

 */
+ (BOOL)hasServiceForKey:(NSString *)key;

/// 注册默认的cosxml服务
/// @param configuration cosxml服务对应的配置信息，一旦配置之后无法修改
+ (QCloudCOSXMLService *)registerDefaultCOSXMLWithConfiguration:(QCloudServiceConfiguration *)configuration;

/// 注册特定key的cosxml服务
/// @param configuration cosxml对应的配置信息
/// @param key 该cosxml对应的key
+ (QCloudCOSXMLService *)registerCOSXMLWithConfiguration:(QCloudServiceConfiguration *)configuration withKey:(NSString *)key;

+ (void)removeCOSXMLWithKey:(NSString *)key;

/**
根据Bukcet, Object来生成可以直接访问的URL。如果您的Bucket是私有读的话，那么访问的时候需要带上签名，
 反之则不需要。


需要注意的是，如果通过该接口来生成带签名的URL的话，因为签名可能是在服务器生成的，该方法是同步方法，
 可能因为网络请求阻塞，建议不要在主线程里调用。

此外, 传入的Object需要是URLEncode后的结果。

 @param bucket 存储桶
 @param object 存储对象, 请传入URL Encode后的结果
 @param withAuthorization 是否需要签名，如果是私有读的Bucket，那么该URL需要带上签名才能访问
 @return object URL
 */
- (NSString *)getURLWithBucket:(NSString *)bucket
                        object:(NSString *)object
             withAuthorization:(BOOL)withAuthorization
                    regionName:(NSString *)regionName;

@end
NS_ASSUME_NONNULL_END
