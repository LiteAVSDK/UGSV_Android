//
//  TVCClientInner.h
//  TVCClientSDK
//
//  Created by tomzhu on 16/10/20.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TVCHeader.h"


#define UGC_HOST        @"vod2.qcloud.com"
#define UGC_HOST_BAK    @"vod2.dnsv1.com"

// 最大请求次数
#define kMaxRequestCount 2

#pragma mark - UCG rsp parse

#define kCode           @"code"
#define kMessage        @"message"
#define kData           @"data"

#define TVCVersion @"1.1.1.0"

#pragma mark - COS config
//字段废弃，作为InitUploadUGC的占位字段
#define kRegion @"gz"
//超时时间
#define kTimeoutInterval 10

//log
#ifndef __OPTIMIZE__
#define NSLog(...) NSLog(__VA_ARGS__)
#else
#define NSLog(...){}
#endif

@interface TVCUGCResult : NSObject

@property(nonatomic,strong) NSString * videoFileId;

@property(nonatomic,strong) NSString * imageFileId;

/**
 json中为数字
 */
@property(nonatomic,strong) NSString * uploadAppid;

@property(nonatomic,strong) NSString * uploadBucket;

@property(nonatomic,strong) NSString * videoPath;

@property(nonatomic,strong) NSString * imagePath;

@property(nonatomic,strong) NSString * videoSign;

@property(nonatomic,strong) NSString * imageSign;

@property(nonatomic,strong) NSString * uploadSession;

@property(nonatomic,strong) NSString * uploadRegion;

@property(nonatomic,strong) NSString * domain;

@property(atomic,assign) int useCosAcc;

@property(nonatomic,strong) NSString * cosAccDomain;

@property(nonatomic,strong) NSString * userAppid;           // 用户appid，用于数据上报

@property(nonatomic,strong) NSString * tmpSecretId;         // cos临时密钥SecretId

@property(nonatomic,strong) NSString * tmpSecretKey;        // cos临时密钥SecretKey

@property(nonatomic,strong) NSString * tmpToken;            // cos临时密钥Token

@property(atomic,assign) uint64_t  tmpExpiredTime;          // cos临时密钥ExpiredTime

@property(atomic,assign) uint64_t  currentTS;               // 后台返回的校准时间戳

@end

@interface TVCUploadContext : NSObject

@property(nonatomic,strong) TVCUploadParam * uploadParam;

@property(nonatomic,strong) TVCResultBlock resultBlock;

@property(nonatomic,strong) TVCProgressBlock progressBlock;

@property(nonatomic,assign) BOOL isUploadVideo;

@property(nonatomic,assign) BOOL isUploadCover;

@property(atomic,assign) TVCResult lastStatus;

@property(nonatomic,strong) NSString * desc;

@property(nonatomic,strong) TVCUGCResult * cugResult;

@property(nonatomic,strong) dispatch_group_t gcdGroup;

@property(nonatomic,strong) dispatch_semaphore_t gcdSem;

@property(nonatomic,strong) dispatch_queue_t gcdQueue;

@property(atomic,assign) uint64_t videoSize;

@property(atomic,assign) uint64_t coverSize;

@property(atomic,assign) uint64_t currentUpload;

@property(atomic,assign) uint64_t videoLastModTime; // 文件最后修改时间

@property(atomic,assign) uint64_t coverLastModTime; // 封面最后修改时间

@property(atomic,assign) uint64_t reqTime;          // 请求开始时间，用于统计各请求耗时

@property(atomic,assign) uint64_t initReqTime;      // 请求上传时间，用于和视频最后修改时间组成reqKey，串联发布流程

@property(nonatomic,assign) BOOL isShouldRetry;     // 由于临时签名过期导致的上传失败，重试

@property(nonatomic,assign) int vodCmdRequestCount;   // vod信令请求次数

@property(nonatomic,copy) NSString* mainVodServerErrMsg;   // //主域名请求失败的msg，用于备份域名都请求失败后，带回上报。

@property(nonatomic,strong) NSData * resumeData;    // cos分片上传resumeData

@end


@interface TVCReportInfo : NSObject

@property(atomic,assign) int reqType;

@property(atomic,assign) int errCode;

@property(atomic,assign) int vodErrCode;

@property(nonatomic,strong) NSString * cosErrCode;

@property(nonatomic,strong) NSString * errMsg;

@property(atomic,assign) uint64_t reqTime;

@property(atomic,assign) uint64_t reqTimeCost;

@property(atomic,assign) uint64_t fileSize;

@property(nonatomic,strong) NSString * fileType;

@property(nonatomic,strong) NSString * fileName;

@property(nonatomic,strong) NSString * fileId;

@property(atomic,assign) uint64_t appId;

@property(nonatomic,strong) NSString * reqServerIp;

@property(nonatomic,strong) NSString * reportId;

@property(nonatomic,strong) NSString * reqKey;

@property(nonatomic,strong) NSString * vodSessionKey;

@property(atomic,assign) int useHttpDNS;

@property(nonatomic,strong) NSString * cosRegion;

@property(atomic,assign) int useCosAcc;

@property(atomic,assign) uint64_t tcpConnTimeCost;

@property(atomic,assign) uint64_t recvRespTimeCost;

@property(atomic,assign) int retryCount;

@property(nonatomic,assign) BOOL reporting;

@property(nonatomic,strong) NSString * requestId;

@end
