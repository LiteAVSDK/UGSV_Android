//
//  QCloudCOSXMLEndPoint.h
//  Pods
//
//  Created by Dong Zhao on 2017/8/22.
//
//

#import <QCloudCore/QCloudCore.h>
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wobjc-property-synthesis"

/**
 QCloudServiceConfiguration 类的 endpoint 参数
 */

@interface QCloudCOSXMLEndPoint : QCloudEndPoint
/**
 * 服务地域名称,可用的服务地域名称请查看官网https://www.qcloud.com/document/product/436/6224
 * 中提供的地域，这里填入官网里提供的地域简称，例如ap-beijing等。
 */
@property (nonatomic, copy) QCloudRegion regionName;
@property (nonatomic, assign) BOOL isPrefixURL;
/**
 服务的基础名称, 默认值为: myqcloud.com
 */
@property (nonatomic, copy) QCloudServiceName serviceName;

/**
 自定义域名：http://bucketname.suffix
 未指定该参数，该存储桶host为 http://bucketname.****
 指定该参数为testsuffix，该存储桶host为 http://bucketname.testsuffix.****
*/
@property (nonatomic, copy) NSString *suffix;
@end

#pragma clang diagnostic pop
