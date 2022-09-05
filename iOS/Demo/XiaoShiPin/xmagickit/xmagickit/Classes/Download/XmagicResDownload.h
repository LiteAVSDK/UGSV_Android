//
//  XmagicResDownload.h
//  BeautyDemo
//
//  Created by tao yue on 2022/7/21.
//  Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^ProgressCallback)(float process);
typedef void(^CompleteCallback)(bool complete);

/*
 * XmagicResDownload
 *
 */
@interface XmagicResDownload : NSObject

+ (instancetype)shardManager;

//下载美颜资源
- (void)downloadItem:(NSString *)key process:(ProgressCallback)process complete:(CompleteCallback)complete;

//获取icon url
-(NSString *)getIconUrl:(NSString *)key;

@end

NS_ASSUME_NONNULL_END
