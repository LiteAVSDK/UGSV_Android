//
//  QCloudServiceConfiguration.h
//  Pods
//
//  Created by Dong Zhao on 2017/3/13.
//
//

#import <Foundation/Foundation.h>
#import "QCloudSignatureProvider.h"
#import "QCloudEndPoint.h"

/**
 QCloud中服务类的配置信息，用于实例化 QCloudCOSXMLService 以及 QCloudCOSTransferManagerService
 具体使用：https://cloud.tencent.com/document/product/436/11280

 ### 示例

  @code

     QCloudServiceConfiguration* configuration = [[QCloudCOSXMLService defaultCOSXML].configuration copy];

     configuration.endpoint.regionName = @"regionName";
     [QCloudCOSTransferMangerService registerCOSTransferMangerWithConfiguration:configuration withKey:@"regionName"];

     [QCloudCOSXMLService registerCOSXMLWithConfiguration:configuration withKey:@"regionName"];

 */
@interface QCloudServiceConfiguration : NSObject <NSCopying>

/**
 签名信息的回调接口，该委托必须实现。签名是腾讯云进行服务时进行用户身份校验的关键手段，同时也保障了用户访问的安全性。该委托中通过函数回调来提供签名信息。
 */
@property (nonatomic, strong) id<QCloudSignatureProvider> signatureProvider;

/**
 您的AppID
 */
@property (nonatomic, strong) NSString *appID;

/**
 您的服务所在的区域,请您一定要设置该参数！
 */
@property (nonatomic, strong) QCloudEndPoint *endpoint;
/**
 是否开启了后台传输，默认是NO
 */
@property (nonatomic, assign) BOOL backgroundEnable;
/**
 后台传输的标识
 */
@property (nonatomic, strong) NSString *backgroundIdentifier;
/**
 是否关闭分享Log日志的功能
 */
@property (nonatomic, assign) BOOL isCloseShareLog;

@property (nonatomic, assign) NSTimeInterval timeoutInterval;

@end
