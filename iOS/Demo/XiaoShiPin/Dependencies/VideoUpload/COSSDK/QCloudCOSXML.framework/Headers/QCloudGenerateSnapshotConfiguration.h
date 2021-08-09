//
//  QCloudGenerateSnapshotConfiguration.h
//  QCloudGenerateSnapshotConfiguration
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
#import "QCloudGenerateSnapshotInput.h"
#import "QCloudGenerateSnapshotOutput.h"
#import "QCloudGenerateSnapshotModeEnum.h"
#import "QCloudGenerateSnapshotRotateTypeEnum.h"
#import "QCloudGenerateSnapshotFormatEnum.h"

NS_ASSUME_NONNULL_BEGIN

/**
 媒体文件某个时间的截图配置
*/
@interface QCloudGenerateSnapshotConfiguration : NSObject
/**
 截取哪个时间点的内容，单位为秒
 */
@property (nonatomic, assign) CGFloat time;
/**
 截图的宽。默认为0
 */
@property (assign, nonatomic) int64_t width;
/**
 截图的宽。默认为0
 */
@property (assign, nonatomic) int64_t height;
/**
 媒体文件的位置信息
 */
@property (strong, nonatomic) QCloudGenerateSnapshotInput *input;
/**
 截图保存的位置信息
 */
@property (strong, nonatomic) QCloudGenerateSnapshotOutput *output;
/**
 截帧方式:枚举值
 * GenerateSnapshotModeExactframe：截取指定时间点的帧
 * GenerateSnapshotModeKeyframe：截取指定时间点之前的最近的
 默认值为 exactframe
 */
@property (assign, nonatomic) QCloudGenerateSnapshotMode mode;
/**
 图片旋转方式:枚举值
 * GenerateSnapshotRotateTypeAuto：按视频旋转信息进行自动旋转
 * GenerateSnapshotRotateTypeOff：不旋转
 */
@property (assign, nonatomic) QCloudGenerateSnapshotRotateType rotate;
/**
 截图的格式:枚举值
 * GenerateSnapshotFormatJPG：jpg
 * GenerateSnapshotFormatPNG：png
 */
@property (assign, nonatomic) QCloudGenerateSnapshotFormat format;
@end
NS_ASSUME_NONNULL_END
