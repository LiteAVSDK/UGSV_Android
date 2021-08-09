//
//  QCloudListVersionsResult.h
//  QCloudListVersionsResult
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
#import "QCloudDeleteMarker.h"
#import "QCloudVersionContent.h"
#import "QCloudCommonPrefixes.h"
NS_ASSUME_NONNULL_BEGIN

/**
 储桶内的所有对象及其历史版本信息
 */
@interface QCloudListVersionsResult : NSObject

/**
 存储桶的名称，格式为<BucketName-APPID>，例如examplebucket-1250000000
 */
@property (strong, nonatomic) NSString *name;

/**
 对象键匹配前缀，对应请求中的 prefix 参数
 */
@property (strong, nonatomic) NSString *prefix;

/**
 起始对象键标记，从该标记之后（不含）按照 UTF-8 字典序返回对象版本条目，对应请求中的 key-marker 参数
 */
@property (strong, nonatomic) NSString *keyMarker;

/**
 起始版本 ID 标记，从该标记之后（不含）返回对象版本条目，对应请求中的 version-id-marker 参数
 */
@property (strong, nonatomic) NSString *versionIDMarkder;

/**
 仅当响应条目有截断（IsTruncated 为 true）才会返回该节点，该节点的值为当前响应条目中的最后一
 个对象键，当需要继续请求后续条目时，将该节点的值作为下一次请求的 key-marker 参数传入
 */
@property (strong, nonatomic) NSString *nextKeyMarker;

/**
 仅当响应条目有截断（IsTruncated 为 true）才会返回该节点，该节点的值为当前响应条目中的最后一个对象的版本 ID，
 当需要继续请求后续条目时，将该节点的值作为下一次请求的 version-id-marker 参数传入
 */
@property (strong, nonatomic) NSString *nextVersionIDMarkder;

/**
 单次响应返回结果的最大条目数量，对应请求中的 max-keys 参数
 */
@property (strong, nonatomic) NSString *maxKeys;
/**
响应请求条目是否被截断
*/
@property (assign, nonatomic) BOOL isTruncated;
@property (strong, nonatomic) NSArray<QCloudDeleteMarker *> *deleteMarker;
/**
 对象版本条目
 */
@property (strong, nonatomic) NSArray<QCloudVersionContent *> *versionContent;
/**
将 Prefix 到 delimiter 之间的相同路径归为一类，定义为 Common Prefix
*/
@property (strong, nonatomic) NSArray<QCloudCommonPrefixes *> *commonPrefixes;
@end
NS_ASSUME_NONNULL_END
