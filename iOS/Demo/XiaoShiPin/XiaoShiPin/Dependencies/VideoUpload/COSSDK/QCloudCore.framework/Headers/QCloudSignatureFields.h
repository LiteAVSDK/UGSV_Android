//
//  QCloudSignatureFields.h
//  Pods
//
//  Created by Dong Zhao on 2017/4/21.
//
//

#import <Foundation/Foundation.h>
/**
获取签名所需信息
*/
@interface QCloudSignatureFields : NSObject

/**
 用户appid
 */
@property (nonatomic, strong) NSString *appID;
/**
 桶名称
 */
@property (nonatomic, strong) NSString *bucket;
@property (nonatomic, strong, readonly) NSString *filed;

/**
 路径
 */
@property (nonatomic, strong) NSString *directory;

/**
 文件名
 */
@property (nonatomic, strong) NSString *fileName;

/**
 是否需要一次性签名，默认为No
 */
@property (nonatomic, assign) BOOL once;
@end
