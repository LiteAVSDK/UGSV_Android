//
//  TCDownloadManager.h
//  BeautyDemo
//
//  Created by tao yue on 2022/3/25.
//  Copyright © 2021 tencent. All rights reserved.

#import <Foundation/Foundation.h>
#import "TCDownloadModel.h"

NS_ASSUME_NONNULL_BEGIN

/*
 * TCDownloadedFile
 *
 */
@interface TCDownloadedFile:NSObject

@property (nonatomic, copy) NSString        *fileName;  //文件名
/** 文件的总长度 */
@property (nonatomic, copy) NSString        *fileSize;  //文件的总长度

@property (nonatomic, copy) NSString        *filePath;  //文件路径

@property bool                              failed;  //是否失败

@property (nonatomic, copy) NSString        *fileType;  //文件类型

@end

/*
 * TCDownloadedFile
 *
 */
@interface TCDownloadManager : NSObject

@property (nonatomic, strong) NSURLSession *session;  //session
@property (nonatomic, copy) void (^completionHandler)(void);  //完成后回调
@property (nonatomic, strong) NSMutableArray <TCDownloadModel *> * downloadModelList;  //下载列表
@property (atomic, strong) NSMutableArray  <TCDownloadedFile *> *finishedlist;  //下载完成列表
@property (nonatomic, strong) NSMutableDictionary *downloadModelDic;  //downloadModelDic


+(TCDownloadManager *)shareManager;

- (TCDownloadModel *)addDownloadModelWithURL:(NSString *)url;

- (void)saveDownloadInfo:(TCDownloadModel *)model;
- (void)loadFinishedfiles;
- (NSMutableArray *)loadDownloadList;
- (NSString *)getResPath;

//是否在下载的列表里
- (BOOL)isInDownloadList:(NSString *)url;
- (BOOL)isFinishedDownload:(NSString *)url;

- (void)deleteDownload:(TCDownloadModel *)model;
- (void)resumeDownload:(TCDownloadModel *)model;
- (void)pauseDownload:(TCDownloadModel *)model;
- (void)deleteFinishFile:(TCDownloadedFile *)model;
@end

NS_ASSUME_NONNULL_END
