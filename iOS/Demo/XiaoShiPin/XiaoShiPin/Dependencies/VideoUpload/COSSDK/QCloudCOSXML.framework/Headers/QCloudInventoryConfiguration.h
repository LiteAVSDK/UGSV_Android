//
//  QCloudInventoryConfiguration.h
//  QCloudInventoryConfiguration
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
#import "QCloudCOSIncludedObjectVersionsEnum.h"
#import "QCloudInventoryDestination.h"
#import "QCloudInventorySchedule.h"
#import "QCloudInventoryFilter.h"
#import "QCloudInventoryOptionalFields.h"

NS_ASSUME_NONNULL_BEGIN
/**
 QCloudPutBucketInventoryRequest 请求参数
 用户在请求体中使用 XML 语言设置清单任务的具体配置信息。配置信息包括清单任务分析的对象，分析的频次，
 分析的维度，分析结果的格式及存储的位置等信息。
 */
@interface QCloudInventoryConfiguration : NSObject
/**
清单的名称，与请求参数中的 id 对应
*/
@property (strong, nonatomic) NSString *identifier;
/**
清单是否启用的标识。如果设置为 True，清单功能将生效；如果设置为 False，将不生成任何清单
*/
@property (strong, nonatomic) NSString *isEnabled;
/**
 是否在清单中包含对象版本：
 如果设置为 All，清单中将会包含所有对象版本，并在清单中增加 VersionId，IsLatest，DeleteMarker 这几个字段
 如果设置为 Current，则清单中不包含对象版本信息
 */
@property (assign, nonatomic) QCloudCOSIncludedObjectVersions includedObjectVersions;
/**
描述存放清单结果的信息
*/
@property (strong, nonatomic) QCloudInventoryDestination *destination;
/**
配置清单任务周期
*/
@property (strong, nonatomic) QCloudInventorySchedule *schedule;
/**
筛选待分析对象。清单功能将分析符合 Filter 中设置的前缀的对象
*/
@property (strong, nonatomic) QCloudInventoryFilter *filter;
/**
清单结果中可选包含的分析项目名称，可选字段包括：Size，LastModifiedDate，StorageClass，
 ETag，IsMultipartUploaded，ReplicationStatus
*/
@property (strong, nonatomic) QCloudInventoryOptionalFields *optionalFields;
@end
NS_ASSUME_NONNULL_END
