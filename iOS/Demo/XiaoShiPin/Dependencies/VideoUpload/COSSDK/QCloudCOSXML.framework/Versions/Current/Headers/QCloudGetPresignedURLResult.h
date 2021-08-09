//
//  QCloudGetPresignedURLResult.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 17/01/2018.
//

#import <Foundation/Foundation.h>
/**
 获取预签名请求结果
 */
@interface QCloudGetPresignedURLResult : NSObject
/**
 预签名 URL
 */
@property (nonatomic, strong) NSString *presienedURL;
@end
