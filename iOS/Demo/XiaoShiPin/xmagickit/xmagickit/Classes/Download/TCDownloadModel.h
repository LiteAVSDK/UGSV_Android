//
//  TCDownloadModel.h
//  BeautyDemo
//
//  Created by tao yue on 2022/3/25.
//  Copyright © 2021 tencent. All rights reserved.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class TCDownloadProgress;
typedef void(^TCDownloadProgressInfoBlock)(TCDownloadProgress *);
typedef void(^TCDownloadCompleteBlock)(NSError * error);
typedef void(^TCUnZipBlock)(void);


typedef enum : NSUInteger {
    TCDownloadModelWillStartState,
    TCDownloadModelRunningState,
    TCDownloadModelPauseState,
    TCDownloadModelResumableState,
    TCDownloadModelCompleteState
} TCDownloadModelState;

/*
 * TCDownloadProgress
 *
 */
@interface TCDownloadProgress : NSObject
// 这次写入的数量
@property (nonatomic, assign, readonly) int64_t bytesWritten;
// 已下载的数量
@property (nonatomic, assign) int64_t totalBytesWritten;
// 文件的总大小
@property (nonatomic, assign) int64_t totalBytesExpectedToWrite;

// 下载进度
@property (nonatomic, assign, readonly) float progress;
// 下载速度
@property (nonatomic, assign, readonly) float speed;
// 下载剩余时间
@property (nonatomic, assign, readonly) int remainingTime;

// 已下载的数量
@property (nonatomic, copy) NSString *writtenFileSize;
// 文件的总大小
@property (nonatomic, copy) NSString *totalFileSize;

// 下载速度
@property (nonatomic, copy, readonly) NSString * speedString;



@end

/*
 * TCDownloadModel
 *
 */
@interface TCDownloadModel : NSObject<NSURLSessionDownloadDelegate>

@property (nonatomic, copy) NSString *url; //下载链接

@property  BOOL error;  //是否error

@property (nonatomic, strong) NSString *startTime;  //开始时间

@property (nonatomic, copy) NSString *fileName;  //文件名称

@property (nonatomic, strong) NSURLSessionDownloadTask *downloadTask;  //downloadTask

@property (nonatomic, strong) NSData *resumeData;  //resumeData

@property (nonatomic, assign) TCDownloadModelState state;  //状态

@property (nonatomic, strong) TCDownloadProgress * progress;  //进度


@property (nonatomic, copy) TCDownloadProgressInfoBlock progressInfoBlock;  //进度回调
@property (nonatomic, copy) TCDownloadCompleteBlock completeBlock;  //完成回调
@property (nonatomic, copy) TCUnZipBlock unZipBlock;  //解压回调

//- (instancetype)initWithTask:(NSURLSessionDownloadTask * )task;
- (instancetype)initWithURL:(NSString *)url isInitTask:(BOOL)isInitTask;

- (void)pause;

- (void)wait;

- (void)resume;

//取消下载的task并且会删除之前下载到本地的tem文件
- (void)cancel;

@end

NS_ASSUME_NONNULL_END
