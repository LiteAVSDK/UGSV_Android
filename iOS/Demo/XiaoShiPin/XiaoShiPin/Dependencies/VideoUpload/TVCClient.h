//
//  TVCClient.h
//  VCDemo
//
//  Created by kennethmiao on 16/10/18.
//  Copyright © 2016年 kennethmiao. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TVCCommon.h"

@interface TVCClient : NSObject

/**
 * 获取实例
 * @param config 配置参数
 */
- (instancetype)initWithConfig:(TVCConfig *)config;

/**
 * 文件上传
 * @param param 上传文件参数
 * @param result 上传结果回调
 * @param progress 上传进度回调
 */
- (void)uploadVideo:(TVCUploadParam *)param result:(TVCResultBlock)result progress:(TVCProgressBlock)progress;

/**
 * 取消上传
 * @return BOOL:成功 or 失败
 */
- (BOOL)cancleUploadVideo;


/**
 * 获取版本号
 * @return NSString 版本号
 */
+ (NSString *)getVersion;

/*
 * 获取上报信息
 */
-(NSDictionary *)getStatusInfo;

/**
 * 设置点播appId
 * 作用是方便定位上传过程中出现的问题
 */
- (void)setAppId:(int)appId;
@end
