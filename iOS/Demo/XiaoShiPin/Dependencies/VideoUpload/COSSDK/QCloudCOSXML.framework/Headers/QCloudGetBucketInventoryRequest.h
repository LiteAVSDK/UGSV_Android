//
//  GetBucketInventory.h
//  GetBucketInventory
//
//  Created by tencent
//  Copyright (c) 2015年 tencent. All rights reserved.
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
#import <QCloudCore/QCloudCore.h>
#import "QCloudInventoryConfiguration.h"
NS_ASSUME_NONNULL_BEGIN
/**
查询存储桶中用户的清单任务信息。

### 功能说明

用户在发起该请求时，需要用户提供清单任务的名称，发起该请求时需获得请求签名，表明该请求已获得许可。

关于查询存储桶中用户的清单任务信息接口的具体描述,请参考： https://cloud.tencent.com/document/product/436/33705。

### 示例

  @code

    QCloudGetBucketInventoryRequest *getReq = [QCloudGetBucketInventoryRequest new];

    // 存储桶名称，格式为 BucketName-APPID
    getReq.bucket = @"examplebucket-1250000000";

    // 清单任务的名称
    getReq.inventoryID = @"list1";
    [getReq setFinishBlock:^(QCloudInventoryConfiguration * _Nonnull result,
                             NSError * _Nonnull error) {

    }];
    [[QCloudCOSXMLService defaultCOSXML] GetBucketInventory:getReq];

*/

@interface QCloudGetBucketInventoryRequest : QCloudBizHTTPRequest
/**
存储桶名
*/
@property (strong, nonatomic) NSString *bucket;

/**
 清单任务的名称。缺省值：None
 合法字符：a-z，A-Z，0-9，-，_，.
 */
@property (strong, nonatomic) NSString *inventoryID;

- (void)setFinishBlock:(void (^_Nullable)(QCloudInventoryConfiguration *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end
NS_ASSUME_NONNULL_END
