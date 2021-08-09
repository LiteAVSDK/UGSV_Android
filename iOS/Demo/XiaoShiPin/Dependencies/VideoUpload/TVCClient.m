//
//  TVCClient.m
//  VCDemo
//
//  Created by kennethmiao on 16/10/18.
//  Copyright © 2016年 kennethmiao. All rights reserved.
//

#import "TVCClient.h"
#import "TVCClientInner.h"
#import "TVCCommon.h"
#import "TVCHttpMessageURLProtocol.h"
#import "TVCReport.h"
#import "TXUGCPublishOptCenter.h"
#import <AVFoundation/AVFoundation.h>
#import <QCloudCOSXML/QCloudCOSXML.h>
#import <QCloudCore/QCloudAuthentationV5Creator.h>
#import <QCloudCore/QCloudCore.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/socket.h>
#include <sys/types.h>
//#import "TXRTMPAPI.h"

#define TVCMultipartResumeSessionKey        @"TVCMultipartResumeSessionKey"         // 点播vodSessionKey
#define TVCMultipartResumeExpireTimeKey     @"TVCMultipartResumeExpireTimeKey"      // vodSessionKey过期时间
#define TVCMultipartFileLastModTime         @"TVCMultipartFileLastModTime"          // 文件最后修改时间，用于在断点续传的时候判断文件是否修改
#define TVCMultipartCoverFileLastModTime    @"TVCMultipartCoverFileLastModTime"     // 封面文件最后修改时间
#define TVCMultipartResumeData              @"TVCMultipartUploadResumeData"         // cos分片上传文件resumeData

#define TVCUGCUploadCosKey                  @"ugc_upload"

#define VIRTUAL_TOTAL_PERCENT               10

@interface TVCClient () <QCloudSignatureProvider, NSURLSessionTaskDelegate>
@property (nonatomic, strong) TVCConfig *config;
@property (nonatomic, strong) QCloudAuthentationV5Creator *creator;
@property (nonatomic, strong) NSString *reqKey;
@property(nonatomic, strong) NSString* uploadKey;
@property (nonatomic, strong) NSString *serverIP;
@property (nonatomic, strong) QCloudCOSXMLUploadObjectRequest *uploadRequest;
@property (nonatomic, strong) TVCReportInfo *reportInfo;
@property (nonatomic, strong) NSURLSession *session;
@property (nonatomic, weak) NSTimer *timer;
@property (atomic, assign) int virtualPercent;
@property (atomic, assign) BOOL realProgressFired;
@end

@implementation TVCClient

- (void)dealloc {
    NSLog(@"dealloc TVCClient");
}

- (instancetype)initWithConfig:(TVCConfig *)config {
    self = [super init];
    if (self) {
        self.config = config;
        self.reqKey = @"";
        self.uploadKey = @"";
        self.serverIP = @"";
        self.reportInfo = [[TVCReportInfo alloc] init];
        self.timer = nil;
        self.virtualPercent = 0;
        self.realProgressFired = NO;
    }
    return self;
}

- (void)uploadVideo:(TVCUploadParam *)param result:(TVCResultBlock)result progress:(TVCProgressBlock)progress {
    TVCUploadResponse *rsp = nil;
    // check config
    rsp = [self checkConfig:self.config];
    if (rsp.retCode != TVC_OK) {
        dispatch_async(dispatch_get_main_queue(), ^{
            result(rsp);
        });
        return;
    }
    // check param
    rsp = [self checkParam:param];
    if (rsp.retCode != TVC_OK) {
        dispatch_async(dispatch_get_main_queue(), ^{
            result(rsp);
        });
        return;
    }

    // init upload context;
    TVCUploadContext *uploadContext = [[TVCUploadContext alloc] init];
    uploadContext.uploadParam = param;
    uploadContext.resultBlock = result;
    uploadContext.progressBlock = progress;
    if (param.videoPath.length > 0) {
        uploadContext.isUploadVideo = YES;
    }
    if (param.coverPath.length > 0) {
        uploadContext.isUploadCover = YES;
    }

    // get file information
    unsigned long long fileSize = 0;
    unsigned long long coverSize = 0;
    unsigned long long fileLastModTime = 0;
    NSFileManager *manager = [NSFileManager defaultManager];
    long long reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
    if ([manager fileExistsAtPath:param.videoPath]) {
        fileSize = [[manager attributesOfItemAtPath:param.videoPath error:nil] fileSize];
        fileLastModTime = [[[manager attributesOfItemAtPath:param.videoPath error:nil] fileModificationDate] timeIntervalSince1970];
        uploadContext.videoSize = fileSize;
        uploadContext.videoLastModTime = fileLastModTime;

        if (uploadContext.isUploadCover) {
            if ([manager fileExistsAtPath:param.coverPath]) {
                coverSize = [[manager attributesOfItemAtPath:param.coverPath error:nil] fileSize];
                uploadContext.coverSize = coverSize;
                uploadContext.coverLastModTime = [[[manager attributesOfItemAtPath:param.coverPath
                                                                             error:nil] fileModificationDate] timeIntervalSince1970];
            } else {
                [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:TVC_ERR_FILE_NOT_EXIST vodErrCode:0 cosErrCode:@"" errInfo:@"coverPath is not exist"
                                reqTime:reqTime
                            reqTimeCost:0
                                 reqKey:@""
                                  appId:0
                               fileSize:0
                               fileType:[self getFileType:param.coverPath]
                               fileName:[self getFileName:param.coverPath]
                             sessionKey:@""
                                 fileId:@""
                              cosRegion:@""
                              useCosAcc:0
                           cosRequestId:@""
                     cosTcpConnTimeCost:0
                    cosRecvRespTimeCost:0];
                NSLog(@"coverPath is not exist");
                TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
                rsp.retCode = TVC_ERR_FILE_NOT_EXIST;
                rsp.descMsg = @"coverPath is not exist";
                dispatch_async(dispatch_get_main_queue(), ^{
                    result(rsp);
                });
                return;
            }
        }
    } else {
        [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:TVC_ERR_FILE_NOT_EXIST vodErrCode:0 cosErrCode:@"" errInfo:@"videoPath is not exist"
                        reqTime:reqTime
                    reqTimeCost:0
                         reqKey:@""
                          appId:0
                       fileSize:0
                       fileType:[self getFileType:param.videoPath]
                       fileName:[self getFileName:param.videoPath]
                     sessionKey:@""
                         fileId:@""
                      cosRegion:@""
                      useCosAcc:0
                   cosRequestId:@""
             cosTcpConnTimeCost:0
            cosRecvRespTimeCost:0];
        NSLog(@"videoPath is not exist");
        TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
        rsp.retCode = TVC_ERR_FILE_NOT_EXIST;
        rsp.descMsg = @"videoPath is not exist";
        dispatch_async(dispatch_get_main_queue(), ^{
            result(rsp);
        });
        return;
    }

    // 1.获取cos参数
    NSString *vodSessionKey = nil;
    if ([[TXUGCPublishOptCenter shareInstance] isPublishingPublishing:param.videoPath] == NO && self.config.enableResume == YES) {
        vodSessionKey = [self getSessionFromFilepath:uploadContext];
    }
    [[TXUGCPublishOptCenter shareInstance] addPublishing:param.videoPath];
    [self applyUploadUGC:uploadContext withVodSessionKey:vodSessionKey];
}

- (BOOL)cancleUploadVideo {
    if (self.uploadRequest) {
        NSError *error;
        [self.uploadRequest cancelByProductingResumeData:&error];
        if (error) {
            return NO;
        } else {
            return YES;
        }
    }
    return NO;
}

+ (NSString *)getVersion {
    return TVCVersion;
}

#pragma mark - InnerMethod

- (TVCUploadResponse *)checkConfig:(TVCConfig *)config {
    TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
    rsp.retCode = TVC_OK;
    //    if(config.secretId.length <= 0){
    //        rsp.retCode = TVC_ERR_UGC_REQUEST_FAILED;
    //        rsp.descMsg = @"secretId should not be empty";
    //    }
    if (config.signature.length <= 0) {
        rsp.retCode = TVC_ERR_INVALID_SIGNATURE;
        rsp.descMsg = @"signature should not be empty";
    }
    return rsp;
}

- (TVCUploadResponse *)checkParam:(TVCUploadParam *)param {
    TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
    rsp.retCode = TVC_OK;
    if (param.videoPath.length <= 0) {
        rsp.retCode = TVC_ERR_INVALID_VIDEOPATH;
        rsp.descMsg = @"video path should not be empty";
    }

    if (param.videoName.length <= 0) {
        param.videoName = [self getFileName:param.videoPath];
    }
    return rsp;
}

- (NSMutableURLRequest *)getCosInitURLRequest:(NSString *)domain
                                  withContext:(TVCUploadContext *)uploadContext
                            withVodSessionKey:(NSString *)vodSessionKey {
    TVCUploadParam *param = uploadContext.uploadParam;
    // set body
    NSMutableDictionary *dictParam = [[NSMutableDictionary alloc] init];
    [dictParam setValue:self.config.signature forKey:@"signature"];

    // 有vodSessionKey的话表示是断点续传
    if (vodSessionKey && vodSessionKey.length) {
        [dictParam setValue:vodSessionKey forKey:@"vodSessionKey"];
    }

    [dictParam setValue:param.videoName forKey:@"videoName"];
    [dictParam setValue:[self getFileType:param.videoPath] forKey:@"videoType"];
    [dictParam setValue:@(uploadContext.videoSize) forKey:@"videoSize"];
    if (uploadContext.isUploadCover) {
        [dictParam setValue:[self getFileName:param.coverPath] forKey:@"coverName"];
        [dictParam setValue:[self getFileType:param.coverPath] forKey:@"coverType"];
        [dictParam setValue:@(uploadContext.coverSize) forKey:@"coverSize"];
    }

    [dictParam setValue:self.config.userID forKey:@"clientReportId"];
    [dictParam setValue:TVCVersion forKey:@"clientVersion"];
    NSString *region = [[TXUGCPublishOptCenter shareInstance] getCosRegion];
    if ([region length] > 0) {
        [dictParam setValue:region forKey:@"storageRegion"];
    }

    NSError *error = nil;
    NSData *bodyData = [NSJSONSerialization dataWithJSONObject:dictParam options:0 error:&error];
    if (error) {
        return nil;
    }

    NSString *host = domain;
    NSArray *ipLists = [[TXUGCPublishOptCenter shareInstance] query:host];
    NSString *ip = ([ipLists count] > 0 ? ipLists[0] : nil);
    if (ip != nil) {
        host = ip;
        self.serverIP = ip;
    } else {
        [self queryIpWithDomain:host];
    }
    // set url
    NSString *baseUrl = [[@"https://" stringByAppendingString:host] stringByAppendingString:@"/v3/index.php?Action=ApplyUploadUGC"];

    // create request
    NSURL *url = [NSURL URLWithString:baseUrl];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setValue:[NSString stringWithFormat:@"%ld", (long)[bodyData length]] forHTTPHeaderField:@"Content-Length"];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"gzip" forHTTPHeaderField:@"Accept-Encoding"];
    [request setValue:UGC_HOST forHTTPHeaderField:@"host"];
    if (ip != nil) {
        [request addValue:[[NSString alloc] initWithData:bodyData encoding:NSUTF8StringEncoding] forHTTPHeaderField:@"originalBody"];
    } else {
        [request setHTTPBody:bodyData];
    }

    NSLog(@"cos begin req : %s", [baseUrl UTF8String]);

    return request;
}

- (NSMutableURLRequest *)getCosEndURLRequest:(NSString *)domain withContext:(TVCUploadContext *)uploadContext {
    NSString *baseUrl;
    TVCUploadParam *param = uploadContext.uploadParam;
    TVCUGCResult *ugc = uploadContext.cugResult;

    // set body
    NSMutableDictionary *dictParam = [[NSMutableDictionary alloc] init];
    [dictParam setValue:self.config.signature forKey:@"signature"];
    [dictParam setValue:ugc.uploadSession forKey:@"vodSessionKey"];
    NSError *error = nil;
    NSData *bodyData = [NSJSONSerialization dataWithJSONObject:dictParam options:0 error:&error];
    if (error) {
        return nil;
    }

    // create request
    NSString *host = domain;
    NSArray *ipLists = [[TXUGCPublishOptCenter shareInstance] query:host];
    NSString *ip = ([ipLists count] > 0 ? ipLists[0] : nil);
    if (ip != nil) {
        host = ip;
        self.serverIP = ip;
    } else {
        [self queryIpWithDomain:host];
    }

    baseUrl = [NSString stringWithFormat:@"https://%@/v3/index.php?Action=CommitUploadUGC", host];

    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:baseUrl]];
    [request setValue:[NSString stringWithFormat:@"%ld", (long)[bodyData length]] forHTTPHeaderField:@"Content-Length"];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"gzip" forHTTPHeaderField:@"Accept-Encoding"];

    if (ip != nil) {
        [request addValue:[[NSString alloc] initWithData:bodyData encoding:NSUTF8StringEncoding] forHTTPHeaderField:@"originalBody"];
    } else {
        [request setHTTPBody:bodyData];
    }

    [request setValue:ugc.domain forHTTPHeaderField:@"host"];

    NSLog(@"cos end req : %s", [baseUrl UTF8String]);

    return request;
}

// 去点播申请上传：获取 COS 上传信息
- (void)applyUploadUGC:(TVCUploadContext *)uploadContext withVodSessionKey:(NSString *)vodSessionKey {
    if (self.timer == nil) {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setObject:uploadContext forKey:@"uploadContext"];

        dispatch_async(dispatch_get_main_queue(), ^{
            self.timer = [NSTimer scheduledTimerWithTimeInterval:2.0f / VIRTUAL_TOTAL_PERCENT target:self selector:@selector(postVirtualProgress:)
                                                        userInfo:dict
                                                         repeats:YES];
        });
    }

    uploadContext.reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
    uploadContext.initReqTime = uploadContext.reqTime;
    self.reqKey = [NSString stringWithFormat:@"%lld;%lld", uploadContext.videoLastModTime, uploadContext.initReqTime];
    self.uploadKey = [NSString stringWithFormat:@"%lld_%lld_%i", uploadContext.videoLastModTime, uploadContext.initReqTime * 1000,arc4random()];
    
    NSURLSessionConfiguration *initCfg = [NSURLSessionConfiguration defaultSessionConfiguration];
    [initCfg setRequestCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    if (self.config.timeoutInterval > 0) {
        [initCfg setTimeoutIntervalForRequest:self.config.timeoutInterval];
    } else {
        [initCfg setTimeoutIntervalForRequest:kTimeoutInterval];
    }
    NSArray *protocolArray = @[[TVCHttpMessageURLProtocol class]];
    initCfg.protocolClasses = protocolArray;
    self.session = [NSURLSession sessionWithConfiguration:initCfg delegate:self delegateQueue:nil];

    [self getCosInitParam:uploadContext withVodSessionKey:vodSessionKey withDomain:UGC_HOST];
}

- (void)getCosInitParam:(TVCUploadContext *)uploadContext withVodSessionKey:(NSString *)vodSessionKey withDomain:(NSString *)domain {
    TVCResultBlock result = uploadContext.resultBlock;
    NSMutableURLRequest *cosRequest = [self getCosInitURLRequest:domain withContext:uploadContext withVodSessionKey:vodSessionKey];
    if (cosRequest == nil) {
        [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];
        if (uploadContext.resultBlock) {
            TVCUploadResponse *initResp = [[TVCUploadResponse alloc] init];
            initResp.retCode = TVC_ERR_UGC_REQUEST_FAILED;
            initResp.descMsg = @"create ugc publish request failed";
            [self notifyResult:result resp:initResp];
            return;
        }
    }

    __weak TVCClient *ws = self;
    NSURLSessionTask *initTask =
        [self.session dataTaskWithRequest:cosRequest
                        completionHandler:^(NSData *_Nullable initData, NSURLResponse *_Nullable response, NSError *_Nullable error) {
                            NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
                            if (error || httpResponse.statusCode != 200 || initData == nil) {  // 失败
                                if ([domain isEqualToString:UGC_HOST]) {                       // 原域名
                                    if (++uploadContext.vodCmdRequestCount < kMaxRequestCount) {
                                        [ws getCosInitParam:uploadContext withVodSessionKey:vodSessionKey withDomain:UGC_HOST];
                                    } else {
                                        uploadContext.vodCmdRequestCount = 0;
                                        uploadContext.mainVodServerErrMsg = [NSString stringWithFormat:@"main vod fail code:%d", error.code];
                                        [ws getCosInitParam:uploadContext withVodSessionKey:vodSessionKey withDomain:UGC_HOST_BAK];
                                    }
                                } else if ([domain isEqualToString:UGC_HOST_BAK]) {  // 备份域名
                                    if (++uploadContext.vodCmdRequestCount < kMaxRequestCount) {
                                        [ws getCosInitParam:uploadContext withVodSessionKey:vodSessionKey withDomain:UGC_HOST_BAK];
                                    } else {
                                        // 删除session
                                        [ws setSession:nil resumeData:nil lastModTime:0 withFilePath:uploadContext.uploadParam.videoPath];
                                        [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];

                                        TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
                                        // 1步骤出错
                                        NSLog(@"ugc init http req fail : error=%d response=%s", error.code, [httpResponse.description UTF8String]);
                                        rsp.retCode = TVC_ERR_UGC_REQUEST_FAILED;
                                        rsp.descMsg = [NSString stringWithFormat:@"ugc code:%d, ugc desc:%@", error.code, @"ugc init http req fail"];

                                        if (uploadContext.mainVodServerErrMsg != nil && uploadContext.mainVodServerErrMsg.length > 0) {
                                            rsp.descMsg = [NSString stringWithFormat:@"%@|%@", rsp.descMsg, uploadContext.mainVodServerErrMsg];
                                        }

                                        unsigned long long reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                                        [ws txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode vodErrCode:error.code cosErrCode:@""
                                                        errInfo:rsp.descMsg
                                                        reqTime:uploadContext.reqTime
                                                    reqTimeCost:reqTimeCost
                                                         reqKey:ws.reqKey
                                                          appId:0
                                                       fileSize:uploadContext.videoSize
                                                       fileType:[ws getFileType:uploadContext.uploadParam.videoPath]
                                                       fileName:[ws getFileName:uploadContext.uploadParam.videoPath]
                                                     sessionKey:@""
                                                         fileId:@""
                                                      cosRegion:@""
                                                      useCosAcc:0
                                                   cosRequestId:@""
                                             cosTcpConnTimeCost:0
                                            cosRecvRespTimeCost:0];
                                        if (result) {
                                            [ws notifyResult:result resp:rsp];
                                        }
                                        return;
                                    }
                                }
                                return;
                            }

                            uploadContext.vodCmdRequestCount = 0;
                            uploadContext.mainVodServerErrMsg = @"";
                            [ws parseInitRsp:initData withContex:uploadContext withVodSessionKey:vodSessionKey];
                        }];
    [initTask resume];
}

- (void)parseInitRsp:(NSData *)initData withContex:(TVCUploadContext *)uploadContext withVodSessionKey:(NSString *)vodSessionKey {
    TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
    TVCResultBlock result = uploadContext.resultBlock;
    unsigned long long reqTimeCost = 0;
    NSError *jsonErr = nil;
    NSDictionary *initDict = [NSJSONSerialization JSONObjectWithData:initData options:NSJSONReadingAllowFragments error:&jsonErr];
    if (jsonErr || ![initDict isKindOfClass:[NSDictionary class]]) {
        // 删除session
        [self setSession:nil resumeData:nil lastModTime:0 withFilePath:uploadContext.uploadParam.videoPath];
        [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];

        rsp.retCode = TVC_ERR_UGC_PARSE_FAILED;
        rsp.descMsg = [NSString stringWithFormat:@"ugc code:%ld, ugc desc:%@", jsonErr.code, @"ugc parse init http fail"];

        reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
        [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode vodErrCode:jsonErr.code cosErrCode:@"" errInfo:rsp.descMsg
                        reqTime:uploadContext.reqTime
                    reqTimeCost:reqTimeCost
                         reqKey:self.reqKey
                          appId:0
                       fileSize:uploadContext.videoSize
                       fileType:[self getFileType:uploadContext.uploadParam.videoPath]
                       fileName:[self getFileName:uploadContext.uploadParam.videoPath]
                     sessionKey:@""
                         fileId:@""
                      cosRegion:@""
                      useCosAcc:0
                   cosRequestId:@""
             cosTcpConnTimeCost:0
            cosRecvRespTimeCost:0];

        if (result) {
            [self notifyResult:result resp:rsp];
        }
        return;
    }

    int code = -1;
    if ([[initDict objectForKey:kCode] isKindOfClass:[NSNumber class]]) {
        code = [[initDict objectForKey:kCode] intValue];
    }
    NSString *msg;
    
    if ([[initDict objectForKey:kMessage] isKindOfClass:[NSString class]]) {
        msg = [initDict objectForKey:kMessage];
    }

    if (code != TVC_OK) {
        // 删除session
        [self setSession:nil resumeData:nil lastModTime:0 withFilePath:uploadContext.uploadParam.videoPath];
        [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];

        rsp.retCode = TVC_ERR_UGC_REQUEST_FAILED;
        rsp.descMsg = [NSString stringWithFormat:@"ugc code:%d, ugc desc:%@", code, msg];

        reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
        [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode vodErrCode:code cosErrCode:@"" errInfo:rsp.descMsg reqTime:uploadContext.reqTime
                    reqTimeCost:reqTimeCost
                         reqKey:self.reqKey
                          appId:0
                       fileSize:uploadContext.videoSize
                       fileType:[self getFileType:uploadContext.uploadParam.videoPath]
                       fileName:[self getFileName:uploadContext.uploadParam.videoPath]
                     sessionKey:@""
                         fileId:@""
                      cosRegion:@""
                      useCosAcc:0
                   cosRequestId:@""
             cosTcpConnTimeCost:0
            cosRecvRespTimeCost:0];

        // 1步骤出错
        if (result) {
            [self notifyResult:result resp:rsp];
        }
        return;
    }

    NSDictionary *dataDict = nil;
    if ([[initDict objectForKey:kData] isKindOfClass:[NSDictionary class]]) {
        dataDict = [initDict objectForKey:kData];
    }
    if (!dataDict) {
        // 删除session
        [self setSession:nil resumeData:nil lastModTime:0 withFilePath:uploadContext.uploadParam.videoPath];
        [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];

        rsp.retCode = TVC_ERR_UGC_PARSE_FAILED;
        rsp.descMsg = @"data is not json string";

        reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
        [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode vodErrCode:3 cosErrCode:@"" errInfo:rsp.descMsg reqTime:uploadContext.reqTime
                    reqTimeCost:reqTimeCost
                         reqKey:self.reqKey
                          appId:0
                       fileSize:uploadContext.videoSize
                       fileType:[self getFileType:uploadContext.uploadParam.videoPath]
                       fileName:[self getFileName:uploadContext.uploadParam.videoPath]
                     sessionKey:@""
                         fileId:@""
                      cosRegion:@""
                      useCosAcc:0
                   cosRequestId:@""
             cosTcpConnTimeCost:0
            cosRecvRespTimeCost:0];

        if (result) {
            [self notifyResult:result resp:rsp];
        }
        return;
    }

    // print json log
    NSError *parseError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:initDict options:NSJSONWritingPrettyPrinted error:&parseError];
    NSString *initDictStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSLog(@"init cos dic : %s", [initDictStr UTF8String]);

    TVCUGCResult *ugc = [[TVCUGCResult alloc] init];

    if ([[dataDict objectForKey:@"video"] isKindOfClass:[NSDictionary class]]) {
        NSDictionary *videoDict = [dataDict objectForKey:@"video"];
        ugc.videoSign = [videoDict objectForKey:@"storageSignature"];
        ugc.videoPath = [videoDict objectForKey:@"storagePath"];
    }

    if ([[dataDict objectForKey:@"cover"] isKindOfClass:[NSDictionary class]]) {
        NSDictionary *coverDict = [dataDict objectForKey:@"cover"];
        ugc.imageSign = [coverDict objectForKey:@"storageSignature"];
        ugc.imagePath = [coverDict objectForKey:@"storagePath"];
    }

    if ([[dataDict objectForKey:@"appId"] isKindOfClass:[NSNumber class]]) {
        ugc.userAppid = [[dataDict objectForKey:@"appId"] stringValue];
    }

    if ([[dataDict objectForKey:@"tempCertificate"] isKindOfClass:[NSDictionary class]]) {
        NSDictionary *cosTmp = [dataDict objectForKey:@"tempCertificate"];
        ugc.tmpSecretId = [cosTmp objectForKey:@"secretId"];
        ugc.tmpSecretKey = [cosTmp objectForKey:@"secretKey"];
        ugc.tmpToken = [cosTmp objectForKey:@"token"];
        ugc.tmpExpiredTime = [[cosTmp objectForKey:@"expiredTime"] longLongValue];
    }
    if ([[dataDict objectForKey:@"timestamp"] isKindOfClass:[NSNumber class]]) {
        ugc.currentTS = [[dataDict objectForKey:@"timestamp"] longLongValue];
    }

    if ([[dataDict objectForKey:@"storageAppId"] isKindOfClass:[NSNumber class]]) {
        ugc.uploadAppid = [[dataDict objectForKey:@"storageAppId"] stringValue];
    }
    if ([[dataDict objectForKey:@"storageBucket"] isKindOfClass:[NSString class]]) {
        //从5.4.10升级到5.4.20之后，废除了setAppIdAndRegion接口，需要自行拼接保证costBucket格式为 bucket-appId
        ugc.uploadBucket = [NSString stringWithFormat:@"%@-%@", [dataDict objectForKey:@"storageBucket"], ugc.uploadAppid];
    }
    if ([[dataDict objectForKey:@"vodSessionKey"] isKindOfClass:[NSString class]]) {
        ugc.uploadSession = [dataDict objectForKey:@"vodSessionKey"];
    }
    if ([[dataDict objectForKey:@"storageRegionV5"] isKindOfClass:[NSString class]]) {
        ugc.uploadRegion = [dataDict objectForKey:@"storageRegionV5"];
    }
    if ([[dataDict objectForKey:@"domain"] isKindOfClass:[NSString class]]) {
        ugc.domain = [dataDict objectForKey:@"domain"];
    }
    if ([[dataDict objectForKey:@"cosAcc"] isKindOfClass:[NSDictionary class]]) {
        NSDictionary *cosAcc = [dataDict objectForKey:@"cosAcc"];
        ugc.useCosAcc = [[cosAcc objectForKey:@"isOpen"] intValue];
        ugc.cosAccDomain = [cosAcc objectForKey:@"domain"];
    }

    uploadContext.cugResult = ugc;

    NSLog(@"init cugResult %s", [[uploadContext.cugResult description] UTF8String]);

    reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
    [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:TVC_OK vodErrCode:0 cosErrCode:@"" errInfo:@"" reqTime:uploadContext.reqTime
                reqTimeCost:reqTimeCost
                     reqKey:self.reqKey
                      appId:ugc.userAppid
                   fileSize:uploadContext.videoSize
                   fileType:[self getFileType:uploadContext.uploadParam.videoPath]
                   fileName:[self getFileName:uploadContext.uploadParam.videoPath]
                 sessionKey:ugc.uploadSession
                     fileId:@""
                  cosRegion:ugc.uploadRegion
                  useCosAcc:ugc.useCosAcc
               cosRequestId:@""
         cosTcpConnTimeCost:0
        cosRecvRespTimeCost:0];

    [self setupCOSXMLShareService:uploadContext];

    // 2.开始上传
    uploadContext.reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
    if (vodSessionKey && vodSessionKey.length) {
        [self commitCosUpload:uploadContext withResumeUpload:YES];
    } else {
        [self commitCosUpload:uploadContext withResumeUpload:NO];
    }
}

- (void)signatureWithFields:(QCloudSignatureFields *)fileds
                    request:(QCloudBizHTTPRequest *)request
                 urlRequest:(NSMutableURLRequest *)urlRequst
                  compelete:(QCloudHTTPAuthentationContinueBlock)continueBlock {
    QCloudSignature *signature = nil;
    if (_creator != nil) {
        signature = [_creator signatureForData:urlRequst];
    }
    continueBlock(signature, nil);
}

- (void)setupCOSXMLShareService:(TVCUploadContext *)uploadContext {
    QCloudCredential *credential = [QCloudCredential new];
    credential.secretID = uploadContext.cugResult.tmpSecretId;
    credential.secretKey = uploadContext.cugResult.tmpSecretKey;
    credential.token = uploadContext.cugResult.tmpToken;
    long long nowTime = [[NSDate date] timeIntervalSince1970];
    long long serverTS = uploadContext.cugResult.currentTS;
    //如果本地时间戳跟后台返回的当前时间戳相差太大，就用后台返回的时间戳。避免本地时间错误导致403
    if (serverTS != 0 && nowTime - serverTS > 10 * 60) {
        credential.startDate = [NSDate dateWithTimeIntervalSince1970:serverTS];
    }
    credential.expirationDate = [NSDate dateWithTimeIntervalSince1970:uploadContext.cugResult.tmpExpiredTime];
    _creator = [[QCloudAuthentationV5Creator alloc] initWithCredential:credential];

    QCloudServiceConfiguration *configuration = [QCloudServiceConfiguration new];

    configuration.appID = uploadContext.cugResult.uploadAppid;
    configuration.signatureProvider = self;

    QCloudCOSXMLEndPoint *endpoint;
    // 是否开启动态加速
    if (uploadContext.cugResult.useCosAcc == 1) {
        NSString *accDomain = uploadContext.cugResult.cosAccDomain;
        NSString *accUrl = accDomain;
        if (![accUrl hasPrefix:@"http"]) {
            if (self.config.enableHttps) {
                accUrl = [@"https://" stringByAppendingString:accDomain];
            } else {
                accUrl = [@"http://" stringByAppendingString:accDomain];
            }
        }
        endpoint = [[QCloudCOSXMLEndPoint alloc] initWithLiteralURL:[NSURL URLWithString:accUrl]];
        endpoint.regionName = uploadContext.cugResult.uploadRegion;
        [self queryIpWithDomain:accUrl];
    } else {
        endpoint = [[QCloudCOSXMLEndPoint alloc] init];
        endpoint.regionName = uploadContext.cugResult.uploadRegion;
        [self queryIpWithDomain:[endpoint serverURLWithBucket:uploadContext.cugResult.uploadBucket appID:uploadContext.cugResult.uploadAppid
                                                   regionName:uploadContext.cugResult.uploadRegion]
                                    .host];
    }

    endpoint.useHTTPS = self.config.enableHttps;
    configuration.endpoint = endpoint;
    
    if(![QCloudCOSXMLService hasServiceForKey:self.uploadKey]){
        [QCloudCOSXMLService registerCOSXMLWithConfiguration:configuration withKey:self.uploadKey];
        [QCloudCOSTransferMangerService registerCOSTransferMangerWithConfiguration:configuration withKey:self.uploadKey];
    }
}

- (void)commitCosUpload:(TVCUploadContext *)uploadContext withResumeUpload:(BOOL)isResumeUpload {
    dispatch_group_t group = dispatch_group_create();
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    uploadContext.gcdGroup = group;
    uploadContext.gcdSem = semaphore;
    uploadContext.gcdQueue = queue;
    // 2-1.开始上传视频
    TVCUploadParam *param = uploadContext.uploadParam;
    TVCUGCResult *cug = uploadContext.cugResult;

    NSLog(@"uploadCosVideo begin : cosBucket:%@ ,cos videoPath:%@, path:%@", cug.uploadBucket, cug.videoPath, param.videoPath);

    __block uint64_t tcpConenctionTimeCost = 0;
    __block uint64_t recvRspTimeCost = 0;
    __block long long reqTimeCost = 0;
    __weak TVCClient *ws = self;
    if (uploadContext.isUploadVideo) {
        dispatch_group_async(group, queue, ^{
            QCloudCOSXMLUploadObjectRequest *videoUpload;

            if (uploadContext.resumeData != nil && uploadContext.resumeData.length != 0) {
                videoUpload = [QCloudCOSXMLUploadObjectRequest requestWithRequestData:uploadContext.resumeData];
            } else {
                videoUpload = [QCloudCOSXMLUploadObjectRequest new];
                videoUpload.body = [NSURL fileURLWithPath:param.videoPath];
                videoUpload.bucket = cug.uploadBucket;
                videoUpload.object = cug.videoPath;

                [videoUpload setRequstsMetricArrayBlock:^(NSMutableArray *requstMetricArray) {
                    if ([requstMetricArray count] > 0 && [requstMetricArray[0] isKindOfClass:[NSDictionary class]]) {
                        if ([[requstMetricArray[0] allValues] count] > 0) {
                            NSDictionary *dic = [requstMetricArray[0] allValues][0];
                            tcpConenctionTimeCost = ([dic[@"kDnsLookupTookTime"] doubleValue] + [dic[@"kConnectTookTime"] doubleValue] +
                                                        [dic[@"kSignRequestTookTime"] doubleValue])
                                                    * 1000;
                            recvRspTimeCost = ([dic[@"kTaskTookTime"] doubleValue] + [dic[@"kReadResponseHeaderTookTime"] doubleValue] +
                                                  [dic[@"kReadResponseBodyTookTime"] doubleValue])
                                              * 1000;
                        }
                    }
                }];

                [videoUpload setInitMultipleUploadFinishBlock:^(
                    QCloudInitiateMultipartUploadResult *multipleUploadInitResult, QCloudCOSXMLUploadObjectResumeData resumeData) {
                    if (multipleUploadInitResult != nil && resumeData != nil) {
                        [self setSession:cug.uploadSession resumeData:resumeData lastModTime:uploadContext.videoLastModTime
                            coverLastModTime:uploadContext.coverLastModTime
                                withFilePath:param.videoPath];
                    }
                }];
            }

            [videoUpload setFinishBlock:^(QCloudUploadObjectResult *result, NSError *error) {
                NSLog(@"uploadCosVideo finish : cosBucket:%@ ,cos videoPath:%@, path:%@, size:%lld", cug.uploadBucket, cug.videoPath, param.videoPath,
                    uploadContext.videoSize);
                reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                NSString *requestId = [result.__originHTTPURLResponse__.allHeaderFields objectForKey:@"x-cos-request-id"];

                if (error) {
                    NSString *errInfo = error.description;
                    NSString *cosErrorCode = @"";
                    if (error.userInfo != nil) {
                        errInfo = error.userInfo.description;
                    }
                    cosErrorCode = [NSString stringWithFormat:@"%d", error.code];

                    // 取消的情况不清除session缓存，错误码定义见 https://cloud.tencent.com/document/product/436/30443
                    if (error.code == 30000) {
                        uploadContext.lastStatus = TVC_ERR_USER_CANCLE;
                        uploadContext.desc = [NSString stringWithFormat:@"upload video, user cancled"];

                        [ws txReport:TVC_UPLOAD_EVENT_ID_COS errCode:TVC_ERR_USER_CANCLE vodErrCode:0 cosErrCode:cosErrorCode errInfo:errInfo
                                        reqTime:uploadContext.reqTime
                                    reqTimeCost:reqTimeCost
                                         reqKey:ws.reqKey
                                          appId:cug.userAppid
                                       fileSize:uploadContext.videoSize
                                       fileType:[ws getFileType:uploadContext.uploadParam.videoPath]
                                       fileName:[ws getFileName:uploadContext.uploadParam.videoPath]
                                     sessionKey:cug.uploadSession
                                         fileId:@""
                                      cosRegion:cug.uploadRegion
                                      useCosAcc:cug.useCosAcc
                                   cosRequestId:requestId
                             cosTcpConnTimeCost:tcpConenctionTimeCost
                            cosRecvRespTimeCost:recvRspTimeCost];
                    } else {
                        uploadContext.lastStatus = TVC_ERR_VIDEO_UPLOAD_FAILED;
                        uploadContext.desc = [NSString stringWithFormat:@"upload video, cos code:%d, cos desc:%@", error.code, error.description];
                        //网络断开，不清除session缓存
                        if (error.code != -1009) {
                            [ws setSession:nil resumeData:nil lastModTime:0 withFilePath:param.videoPath];
                        }

                        [ws txReport:TVC_UPLOAD_EVENT_ID_COS errCode:TVC_ERR_VIDEO_UPLOAD_FAILED vodErrCode:0 cosErrCode:cosErrorCode errInfo:errInfo
                                        reqTime:uploadContext.reqTime
                                    reqTimeCost:reqTimeCost
                                         reqKey:ws.reqKey
                                          appId:cug.userAppid
                                       fileSize:uploadContext.videoSize
                                       fileType:[ws getFileType:uploadContext.uploadParam.videoPath]
                                       fileName:[ws getFileName:uploadContext.uploadParam.videoPath]
                                     sessionKey:cug.uploadSession
                                         fileId:@""
                                      cosRegion:cug.uploadRegion
                                      useCosAcc:cug.useCosAcc
                                   cosRequestId:requestId
                             cosTcpConnTimeCost:tcpConenctionTimeCost
                            cosRecvRespTimeCost:recvRspTimeCost];
                    }
                    dispatch_semaphore_signal(semaphore);
                    if (uploadContext.isUploadCover) {
                        dispatch_semaphore_signal(semaphore);
                    }
                } else {
                    NSLog(@"upload video succ");
                    //视频上传完成，上报视频上传信息，清除session缓存
                    [ws txReport:TVC_UPLOAD_EVENT_ID_COS errCode:0 vodErrCode:0 cosErrCode:@"" errInfo:@"" reqTime:uploadContext.reqTime
                                reqTimeCost:reqTimeCost
                                     reqKey:ws.reqKey
                                      appId:cug.userAppid
                                   fileSize:uploadContext.videoSize
                                   fileType:[ws getFileType:uploadContext.uploadParam.videoPath]
                                   fileName:[ws getFileName:uploadContext.uploadParam.videoPath]
                                 sessionKey:cug.uploadSession
                                     fileId:@""
                                  cosRegion:cug.uploadRegion
                                  useCosAcc:cug.useCosAcc
                               cosRequestId:requestId
                         cosTcpConnTimeCost:tcpConenctionTimeCost
                        cosRecvRespTimeCost:recvRspTimeCost];
                    [ws setSession:nil resumeData:nil lastModTime:0 withFilePath:param.videoPath];
                    // 2-2.开始上传封面
                    if (uploadContext.isUploadCover) {
                        uploadContext.reqTime = [[NSDate date] timeIntervalSince1970] * 1000;

                        QCloudCOSXMLUploadObjectRequest *coverUpload = [QCloudCOSXMLUploadObjectRequest new];
                        coverUpload.body = [NSURL fileURLWithPath:param.coverPath];
                        coverUpload.bucket = cug.uploadBucket;
                        coverUpload.object = cug.imagePath;

                        __block uint64_t tcpConenctionTimeCost = 0;
                        __block uint64_t recvRspTimeCost = 0;

                        [coverUpload setRequstsMetricArrayBlock:^(NSMutableArray *requstMetricArray) {
                            if ([requstMetricArray count] > 0 && [requstMetricArray[0] isKindOfClass:[NSDictionary class]]) {
                                if ([[requstMetricArray[0] allValues] count] > 0) {
                                    NSDictionary *dic = [requstMetricArray[0] allValues][0];
                                    tcpConenctionTimeCost = ([dic[@"kDnsLookupTookTime"] doubleValue] + [dic[@"kConnectTookTime"] doubleValue] +
                                                                [dic[@"kSignRequestTookTime"] doubleValue])
                                                            * 1000;
                                    recvRspTimeCost = ([dic[@"kTaskTookTime"] doubleValue] + [dic[@"kReadResponseHeaderTookTime"] doubleValue] +
                                                          [dic[@"kReadResponseBodyTookTime"] doubleValue])
                                                      * 1000;
                                }
                            }
                        }];

                        [coverUpload setFinishBlock:^(QCloudUploadObjectResult *result, NSError *error) {
                            NSString *cosErrorCode = @"";
                            NSString *requestId = [result.__originHTTPURLResponse__.allHeaderFields objectForKey:@"x-cos-request-id"];

                            if (error) {
                                // 2-2步骤出错
                                NSLog(@"upload cover fail : %d", error.code);
                                NSString *errInfo = error.description;
                                if (error.userInfo != nil) {
                                    errInfo = error.userInfo.description;
                                }
                                cosErrorCode = [NSString stringWithFormat:@"%d", error.code];

                                if (error.code == 30000) {
                                    uploadContext.lastStatus = TVC_ERR_USER_CANCLE;
                                    uploadContext.desc = [NSString stringWithFormat:@"upload cover, user cancled"];
                                } else {
                                    uploadContext.lastStatus = TVC_ERR_COVER_UPLOAD_FAILED;
                                    uploadContext.desc = [NSString stringWithFormat:@"upload cover, cos code:%@, cos desc:%@", cosErrorCode, errInfo];
                                }
                            } else {
                                NSLog(@"upload cover succ");
                            }
                            reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                            [ws txReport:TVC_UPLOAD_EVENT_ID_COS errCode:uploadContext.lastStatus vodErrCode:0 cosErrCode:cosErrorCode
                                            errInfo:uploadContext.desc
                                            reqTime:uploadContext.reqTime
                                        reqTimeCost:reqTimeCost
                                             reqKey:ws.reqKey
                                              appId:cug.userAppid
                                           fileSize:uploadContext.coverSize
                                           fileType:[ws getFileType:uploadContext.uploadParam.coverPath]
                                           fileName:[ws getFileName:uploadContext.uploadParam.coverPath]
                                         sessionKey:cug.uploadSession
                                             fileId:@""
                                          cosRegion:cug.uploadRegion
                                          useCosAcc:cug.useCosAcc
                                       cosRequestId:requestId
                                 cosTcpConnTimeCost:tcpConenctionTimeCost
                                cosRecvRespTimeCost:recvRspTimeCost];
                            dispatch_semaphore_signal(semaphore);
                        }];

                        TVCProgressBlock progress = uploadContext.progressBlock;
                        [coverUpload setSendProcessBlock:^(int64_t bytesSent, int64_t totalBytesSent, int64_t totalBytesExpectedToSend) {
                            if (progress) {
                                uint64_t total = uploadContext.videoSize + uploadContext.coverSize;
                                uploadContext.currentUpload += bytesSent;
                                if (uploadContext.currentUpload > total) {
                                    uploadContext.currentUpload = total;
                                    ws.virtualPercent = 100 - VIRTUAL_TOTAL_PERCENT;
                                    [ws.timer setFireDate:[NSDate date]];  //上传完成，启动结束虚拟进度
                                } else {
                                    progress(
                                        uploadContext.currentUpload * (100 - 2 * VIRTUAL_TOTAL_PERCENT) / 100 + VIRTUAL_TOTAL_PERCENT * total / 100,
                                        total);
                                }
                            }
                        }];
                        ws.uploadRequest = coverUpload;
                        [[QCloudCOSTransferMangerService costransfermangerServiceForKey:self.uploadKey] UploadObject:coverUpload];
                    }
                }
                dispatch_semaphore_signal(semaphore);
            }];

            TVCProgressBlock progress = uploadContext.progressBlock;
            [videoUpload setSendProcessBlock:^(int64_t bytesSent, int64_t totalBytesSent, int64_t totalBytesExpectedToSend) {
                if (!ws.realProgressFired) {
                    [ws.timer setFireDate:[NSDate distantFuture]];
                    ws.realProgressFired = YES;
                }

                if (progress) {
                    uint64_t total = uploadContext.videoSize + uploadContext.coverSize;
                    uploadContext.currentUpload = totalBytesSent;
                    if (uploadContext.currentUpload > total) {
                        uploadContext.currentUpload = total;
                        ws.virtualPercent = 100 - VIRTUAL_TOTAL_PERCENT;
                        [ws.timer setFireDate:[NSDate date]];  //上传完成，启动结束虚拟进度
                    } else {
                        progress(uploadContext.currentUpload * (100 - 2 * VIRTUAL_TOTAL_PERCENT) / 100 + VIRTUAL_TOTAL_PERCENT * total / 100, total);
                    }
                }
            }];
            ws.uploadRequest = videoUpload;
            [[QCloudCOSTransferMangerService costransfermangerServiceForKey:self.uploadKey] UploadObject:videoUpload];
        });
    }

    [self notifyCosUploadEnd:uploadContext];
}

- (void)notifyCosUploadEnd:(TVCUploadContext *)uploadContext {
    __weak TVCClient *ws = self;
    dispatch_group_notify(uploadContext.gcdGroup, uploadContext.gcdQueue, ^{
        if (uploadContext.isUploadVideo) {
            dispatch_semaphore_wait(uploadContext.gcdSem, DISPATCH_TIME_FOREVER);
        }
        if (uploadContext.isUploadCover) {
            dispatch_semaphore_wait(uploadContext.gcdSem, DISPATCH_TIME_FOREVER);
        }

        TVCResultBlock result = uploadContext.resultBlock;

        if (uploadContext.lastStatus != TVC_OK) {
            // 上传失败，由于签名时间太短，上传未完成导致的，重试
            if (uploadContext.isShouldRetry) {
                uploadContext.lastStatus = TVC_OK;
                uploadContext.isShouldRetry = NO;
                dispatch_async(dispatch_get_main_queue(), ^{
                    // 1.获取cos参数
                    NSString *vodSessionKey = nil;
                    if (ws.config.enableResume == YES) {
                        vodSessionKey = [ws getSessionFromFilepath:uploadContext];
                    }
                    [self applyUploadUGC:uploadContext withVodSessionKey:vodSessionKey];
                });
            } else if (result) {
                [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];
                TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
                rsp.retCode = uploadContext.lastStatus;
                rsp.descMsg = uploadContext.desc;
                [ws notifyResult:result resp:rsp];
                return;
            }
        } else {
            uploadContext.reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
            [ws completeUpload:uploadContext withDomain:UGC_HOST];
        }
    });
}

- (void)completeUpload:(TVCUploadContext *)uploadContext withDomain:(NSString *)domain {
    // 3.完成上传
    NSLog(@"complete upload task");
    TVCResultBlock result = uploadContext.resultBlock;
    __weak TVCClient *ws = self;
    NSMutableURLRequest *cosFiniURLRequest = [self getCosEndURLRequest:domain withContext:uploadContext];
    NSURLSessionTask *finiTask = [self.session
        dataTaskWithRequest:cosFiniURLRequest
          completionHandler:^(NSData *_Nullable finiData, NSURLResponse *_Nullable response, NSError *_Nullable error) {
              NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
              if (error || httpResponse.statusCode != 200 || finiData == nil) {
                  if ([domain isEqualToString:UGC_HOST]) {
                      if (++uploadContext.vodCmdRequestCount < kMaxRequestCount) {
                          [ws completeUpload:uploadContext withDomain:UGC_HOST];
                      } else {
                          uploadContext.vodCmdRequestCount = 0;
                          uploadContext.mainVodServerErrMsg = [NSString stringWithFormat:@"main vod fail code:%d", error.code];
                          [ws completeUpload:uploadContext withDomain:UGC_HOST_BAK];
                      }
                  } else if ([domain isEqualToString:UGC_HOST_BAK]) {
                      if (++uploadContext.vodCmdRequestCount < kMaxRequestCount) {
                          [ws completeUpload:uploadContext withDomain:UGC_HOST_BAK];
                      } else {
                          [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];
                          // 3步骤出错
                          NSLog(@"cos end http req fail : error=%d response=%s", error.code, [httpResponse.description UTF8String]);
                          if (result) {
                              long long reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                              TVCUploadResponse *initResp = [[TVCUploadResponse alloc] init];
                              initResp.retCode = TVC_ERR_UGC_FINISH_REQ_FAILED;
                              initResp.descMsg = [NSString stringWithFormat:@"ugc code:%d, ugc desc:%@", error.code, @"ugc finish http req fail"];
                              if (uploadContext.mainVodServerErrMsg != nil && uploadContext.mainVodServerErrMsg.length > 0) {
                                  initResp.descMsg = [NSString stringWithFormat:@"%@|%@", initResp.descMsg, uploadContext.mainVodServerErrMsg];
                              }

                              [ws txReport:TVC_UPLOAD_EVENT_ID_FINISH errCode:initResp.retCode vodErrCode:error.code cosErrCode:@"" errInfo:initResp.descMsg
                                              reqTime:uploadContext.reqTime
                                          reqTimeCost:reqTimeCost
                                               reqKey:ws.reqKey
                                                appId:uploadContext.cugResult.userAppid
                                             fileSize:uploadContext.videoSize
                                             fileType:[ws getFileType:uploadContext.uploadParam.videoPath]
                                             fileName:[ws getFileName:uploadContext.uploadParam.videoPath]
                                           sessionKey:uploadContext.cugResult.uploadSession
                                               fileId:@""
                                            cosRegion:uploadContext.cugResult.uploadRegion
                                            useCosAcc:uploadContext.cugResult.useCosAcc
                                         cosRequestId:@""
                                   cosTcpConnTimeCost:0
                                  cosRecvRespTimeCost:0];

                              [ws notifyResult:result resp:initResp];
                          }
                      }
                  }
                  return;
              }
              [ws parseFinishRsp:finiData withContex:uploadContext];
          }];
    [finiTask resume];
}

- (void)parseFinishRsp:(NSData *)finiData withContex:(TVCUploadContext *)uploadContext {
    TVCResultBlock result = uploadContext.resultBlock;
    NSDictionary *finiDict = [NSJSONSerialization JSONObjectWithData:finiData options:(NSJSONReadingMutableLeaves)error:nil];

    NSError *parseError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:finiDict options:NSJSONWritingPrettyPrinted error:&parseError];
    NSString *finiDictStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

    NSLog(@"end cos dic : %@", finiDictStr);

    int code = -1;
    if ([[finiDict objectForKey:kCode] isKindOfClass:[NSNumber class]]) {
        code = [[finiDict objectForKey:kCode] intValue];
    }
    NSString *msg;
    
    if ([[finiDict objectForKey:kMessage] isKindOfClass:[NSString class]]) {
        msg = [finiDict objectForKey:kMessage];
    }

    NSDictionary *dataDict = nil;
    NSString *videoURL = @"";
    NSString *coverURL = @"";
    NSString *videoID = @"";
    if ([[finiDict objectForKey:kData] isKindOfClass:[NSDictionary class]]) {
        dataDict = [finiDict objectForKey:kData];

        NSDictionary *videoDic = nil;
        NSDictionary *coverDic = nil;
        if ([[dataDict objectForKey:@"video"] isKindOfClass:[NSDictionary class]]) {
            videoDic = [dataDict objectForKey:@"video"];
            if (self.config.enableHttps == YES) {
                videoURL = [[videoDic objectForKey:@"url"] stringByReplacingOccurrencesOfString:@"http:" withString:@"https:"];
            } else {
                videoURL = [videoDic objectForKey:@"url"];
            }
        }
        if ([[dataDict objectForKey:@"cover"] isKindOfClass:[NSDictionary class]]) {
            coverDic = [dataDict objectForKey:@"cover"];
            if (self.config.enableHttps == YES) {
                coverURL = [[coverDic objectForKey:@"url"] stringByReplacingOccurrencesOfString:@"http:" withString:@"https:"];
            } else {
                coverURL = [coverDic objectForKey:@"url"];
            }
        }
        if ([[dataDict objectForKey:@"fileId"] isKindOfClass:[NSString class]]) {
            videoID = [dataDict objectForKey:@"fileId"];
        }
    }

    [[TXUGCPublishOptCenter shareInstance] delPublishing:uploadContext.uploadParam.videoPath];
    TVCUploadResponse *finiResp = [[TVCUploadResponse alloc] init];
    if (code != TVC_OK) {
        // 3步骤出错
        finiResp.retCode = TVC_ERR_UGC_FINISH_RSP_FAILED;
        finiResp.descMsg = [NSString stringWithFormat:@"ugc code:%d, ugc desc:%@ ugc finish http rsp fail", code, msg];
        if (result) {
            long long reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
            [self txReport:TVC_UPLOAD_EVENT_ID_FINISH errCode:finiResp.retCode vodErrCode:code cosErrCode:@"" errInfo:finiResp.descMsg
                            reqTime:uploadContext.reqTime
                        reqTimeCost:reqTimeCost
                             reqKey:self.reqKey
                              appId:uploadContext.cugResult.userAppid
                           fileSize:uploadContext.videoSize
                           fileType:[self getFileType:uploadContext.uploadParam.videoPath]
                           fileName:[self getFileName:uploadContext.uploadParam.videoPath]
                         sessionKey:uploadContext.cugResult.uploadSession
                             fileId:@""
                          cosRegion:uploadContext.cugResult.uploadRegion
                          useCosAcc:uploadContext.cugResult.useCosAcc
                       cosRequestId:@""
                 cosTcpConnTimeCost:0
                cosRecvRespTimeCost:0];
            [self notifyResult:result resp:finiResp];
        }
        return;
    } else {
        TVCProgressBlock progress = uploadContext.progressBlock;
        if (progress) {
            uint64_t total = uploadContext.videoSize + uploadContext.coverSize;
            progress(total, total);
        }

        //所有步骤成功完成
        finiResp.retCode = TVC_OK;
        finiResp.videoId = videoID;
        finiResp.videoURL = videoURL;
        finiResp.coverURL = coverURL;
        if (result) {
            long long reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
            [self txReport:TVC_UPLOAD_EVENT_ID_FINISH errCode:finiResp.retCode vodErrCode:0 cosErrCode:@"" errInfo:finiResp.descMsg
                            reqTime:uploadContext.reqTime
                        reqTimeCost:reqTimeCost
                             reqKey:self.reqKey
                              appId:uploadContext.cugResult.userAppid
                           fileSize:uploadContext.videoSize
                           fileType:[self getFileType:uploadContext.uploadParam.videoPath]
                           fileName:[self getFileName:uploadContext.uploadParam.videoPath]
                         sessionKey:uploadContext.cugResult.uploadSession
                             fileId:videoID
                          cosRegion:uploadContext.cugResult.uploadRegion
                          useCosAcc:uploadContext.cugResult.useCosAcc
                       cosRequestId:@""
                 cosTcpConnTimeCost:0
                cosRecvRespTimeCost:0];
            [self notifyResult:result resp:finiResp];
        }
        return;
    }
}

- (NSString *)getLastComponent:(NSString *)filePath {
    return [filePath lastPathComponent];
}

- (NSString *)getFileName:(NSString *)filePath {
    return [[filePath lastPathComponent] stringByDeletingPathExtension];
}

- (NSString *)getFileType:(NSString *)filePath {
    return [filePath pathExtension];
}

#pragma mark-- 断点续传

// 本地保存 filePath --> session、filePath --> expireTime，filePath --> fileLastModTime, filePath --> resumeData 的映射集合，格式为json
// "TVCMultipartResumeSessionKey": {filePath1: session1, filePath2: session2, filePath3: session3}
// "TVCMultipartResumeExpireTimeKey": {filePath1: expireTime1, filePath2: expireTime2, filePath3: expireTime3}
// session的过期时间是1天
- (NSString *)getSessionFromFilepath:(TVCUploadContext *)uploadContext {
    NSString *filePath = uploadContext.uploadParam.videoPath;
    if (filePath == nil || filePath.length == 0) {
        return nil;
    }

    NSMutableDictionary *sessionDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *timeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *lastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *coverLastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *resumeDataDic = [[NSMutableDictionary alloc] init];

    NSError *jsonErr = nil;

    // read [itemPath, session]
    NSString *strPathToSession = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeSessionKey];
    if (strPathToSession == nil) {
        NSLog(@"TVCMultipartResumeSessionKey is nil");
        return nil;
    }
    sessionDic = [NSJSONSerialization JSONObjectWithData:[strPathToSession dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments
                                                   error:&jsonErr];
    if (jsonErr) {
        NSLog(@"TVCMultipartResumeSessionKey is not json format: %@", strPathToSession);
        return nil;
    }

    // read [itemPath, expireTime]
    NSString *strPathToExpireTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeExpireTimeKey];
    if (strPathToExpireTime == nil) {
        NSLog(@"TVCMultipartResumeSessionKey expireTime is nil");
        return nil;
    }
    timeDic = [NSJSONSerialization JSONObjectWithData:[strPathToExpireTime dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments
                                                error:&jsonErr];
    if (jsonErr) {
        NSLog(@"TVCMultipartResumeExpireTimeKey is not json format: %@", strPathToExpireTime);
        return nil;
    }

    // read [itemPath, fileLastModTime]
    NSString *strPathToLastModTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartFileLastModTime];
    if (strPathToLastModTime == nil) {
        NSLog(@"TVCMultipartResumeSessionKey lastModTime is nil");
        return nil;
    }
    lastModTimeDic = [NSJSONSerialization JSONObjectWithData:[strPathToLastModTime dataUsingEncoding:NSUTF8StringEncoding]
                                                     options:NSJSONReadingAllowFragments
                                                       error:&jsonErr];
    if (jsonErr) {
        NSLog(@"TVCMultipartFileLastModTime is not json format: %@", strPathToLastModTime);
        return nil;
    }

    // read [itemPath, CoverFileLastModTime]
    NSString *strPathToCoverLastModTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartCoverFileLastModTime];
    if (strPathToCoverLastModTime == nil) {
        NSLog(@"TVCMultipartResumeSessionKey coverLastModTime is nil");
        return nil;
    }
    coverLastModTimeDic = [NSJSONSerialization JSONObjectWithData:[strPathToCoverLastModTime dataUsingEncoding:NSUTF8StringEncoding]
                                                          options:NSJSONReadingAllowFragments
                                                            error:&jsonErr];
    if (jsonErr) {
        NSLog(@"TVCMultipartCoverFileLastModTime is not json format: %@", strPathToCoverLastModTime);
        return nil;
    }

    // read [itemPath, resumeData]
    NSString *strPathToResumeData = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeData];
    if (strPathToResumeData == nil) {
        NSLog(@"TVCMultipartResumeSessionKey resumeData is nil");
        return nil;
    }
    resumeDataDic = [NSJSONSerialization JSONObjectWithData:[strPathToResumeData dataUsingEncoding:NSUTF8StringEncoding]
                                                    options:NSJSONReadingAllowFragments
                                                      error:&jsonErr];
    if (jsonErr) {
        NSLog(@"TVCMultipartReumeData is not json format: %@", strPathToResumeData);
        return nil;
    }

    NSString *session = [sessionDic objectForKey:filePath];
    NSInteger expireTime = [[timeDic objectForKey:filePath] integerValue];
    unsigned long long lastModTime = [[lastModTimeDic objectForKey:filePath] unsignedLongLongValue];
    unsigned long long coverLastModTime = [[coverLastModTimeDic objectForKey:filePath] unsignedLongLongValue];
    NSString *sResumeData = [resumeDataDic objectForKey:filePath];
    NSInteger nowTime = (NSInteger)[[NSDate date] timeIntervalSince1970] + 1;
    NSString *ret = nil;

    if (session && nowTime < expireTime && lastModTime == uploadContext.videoLastModTime && coverLastModTime == uploadContext.coverLastModTime
        && sResumeData != nil && sResumeData.length != 0) {
        ret = session;
        NSData *resumeData = [[NSData alloc] initWithBase64EncodedString:sResumeData options:0];
        uploadContext.resumeData = resumeData;
    } else {
        NSLog(@"TVCMultipartReumeData is invalid");
    }

    // 删除过期的session，并保存
    NSMutableDictionary *newSessionDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *newTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *newLastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *newCoverLastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *newResumeDataDic = [[NSMutableDictionary alloc] init];
    for (NSString *key in timeDic) {
        NSInteger expireTime = [[timeDic objectForKey:key] integerValue];
        if (nowTime < expireTime) {
            [newSessionDic setValue:[sessionDic objectForKey:key] forKey:key];
            [newTimeDic setValue:[timeDic objectForKey:key] forKey:key];
            [newLastModTimeDic setValue:[lastModTimeDic objectForKey:key] forKey:key];
            [newCoverLastModTimeDic setValue:[coverLastModTimeDic objectForKey:key] forKey:key];
            [newResumeDataDic setValue:[resumeDataDic objectForKey:key] forKey:key];
        }
    }

    // 将newSessionDic 和 newTimeDic 保存文件
    NSData *newSessionJsonData = [NSJSONSerialization dataWithJSONObject:newSessionDic options:0 error:&jsonErr];
    NSData *newTimeJsonData = [NSJSONSerialization dataWithJSONObject:newTimeDic options:0 error:&jsonErr];
    NSData *newLastModTimeJsonData = [NSJSONSerialization dataWithJSONObject:newLastModTimeDic options:0 error:&jsonErr];
    NSData *newCoverLastModTimeJsonData = [NSJSONSerialization dataWithJSONObject:newCoverLastModTimeDic options:0 error:&jsonErr];
    NSData *newResumeDataJsonData = [NSJSONSerialization dataWithJSONObject:newResumeDataDic options:0 error:&jsonErr];

    NSString *strNeweSession = [[NSString alloc] initWithData:newSessionJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewTime = [[NSString alloc] initWithData:newTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewLastModTime = [[NSString alloc] initWithData:newLastModTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewCoverLastModTime = [[NSString alloc] initWithData:newCoverLastModTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewResumeData = [[NSString alloc] initWithData:newResumeDataJsonData encoding:NSUTF8StringEncoding];

    [[NSUserDefaults standardUserDefaults] setObject:strNeweSession forKey:TVCMultipartResumeSessionKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewTime forKey:TVCMultipartResumeExpireTimeKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewLastModTime forKey:TVCMultipartFileLastModTime];
    [[NSUserDefaults standardUserDefaults] setObject:strNewCoverLastModTime forKey:TVCMultipartCoverFileLastModTime];
    [[NSUserDefaults standardUserDefaults] setObject:strNewResumeData forKey:TVCMultipartResumeData];

    [[NSUserDefaults standardUserDefaults] synchronize];

    return ret;
}

- (void)setSession:(NSString *)session resumeData:(NSData *)resumeData lastModTime:(uint64_t)lastModTime withFilePath:(NSString *)filePath {
    [self setSession:session resumeData:resumeData lastModTime:lastModTime coverLastModTime:0 withFilePath:filePath];
}

- (void)setSession:(NSString *)session
          resumeData:(NSData *)resumeData
         lastModTime:(uint64_t)lastModTime
    coverLastModTime:(uint64_t)coverLastModTime
        withFilePath:(NSString *)filePath {
    if (filePath == nil || filePath.length == 0) {
        return;
    }

    NSMutableDictionary *sessionDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *timeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *lastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *coverLastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *resumeDataDic = [[NSMutableDictionary alloc] init];
    NSError *jsonErr = nil;

    // read [itemPath, session]
    NSString *strPathToSession = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeSessionKey];
    if (strPathToSession) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToSession dataUsingEncoding:NSUTF8StringEncoding]
                                                            options:NSJSONReadingAllowFragments
                                                              error:&jsonErr];
        sessionDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }

    // read [itemPath, expireTime]
    NSString *strPathToExpireTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeExpireTimeKey];
    if (strPathToExpireTime) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToExpireTime dataUsingEncoding:NSUTF8StringEncoding]
                                                            options:NSJSONReadingAllowFragments
                                                              error:&jsonErr];
        timeDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }

    // read [itemPath, lastModTime]
    NSString *strPathToLastModTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartFileLastModTime];
    if (strPathToLastModTime) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToLastModTime dataUsingEncoding:NSUTF8StringEncoding]
                                                            options:NSJSONReadingAllowFragments
                                                              error:&jsonErr];
        lastModTimeDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }

    // read [itemPath, coverLastModTime]
    NSString *strPathToCoverLastModTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartFileLastModTime];
    if (strPathToCoverLastModTime) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToCoverLastModTime dataUsingEncoding:NSUTF8StringEncoding]
                                                            options:NSJSONReadingAllowFragments
                                                              error:&jsonErr];
        coverLastModTimeDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }

    // read [itemPath, resumeData]
    NSString *strPathToResumeData = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeData];
    if (strPathToResumeData) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToResumeData dataUsingEncoding:NSUTF8StringEncoding]
                                                            options:NSJSONReadingAllowFragments
                                                              error:&jsonErr];
        resumeDataDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }

    // 设置过期时间为1天
    NSInteger expireTime = (NSInteger)[[NSDate date] timeIntervalSince1970] + 24 * 60 * 60;

    // session、resumeDataDic 为空，lastModTime为0就表示删掉该 [key, value]
    if (session == nil || session.length == 0 || resumeData == nil || resumeData.length == 0 || lastModTime == 0 || coverLastModTime == 0) {
        [sessionDic removeObjectForKey:filePath];
        [timeDic removeObjectForKey:filePath];
        [lastModTimeDic removeObjectForKey:filePath];
        [coverLastModTimeDic removeObjectForKey:filePath];
        [resumeDataDic removeObjectForKey:filePath];
    } else {
        [sessionDic setValue:session forKey:filePath];
        [timeDic setValue:@(expireTime) forKey:filePath];
        [lastModTimeDic setValue:@(lastModTime) forKey:filePath];
        [coverLastModTimeDic setValue:@(coverLastModTime) forKey:filePath];
        NSString *sResumeData = [resumeData base64EncodedStringWithOptions:0];
        [resumeDataDic setValue:sResumeData forKey:filePath];
    }

    // 保存文件
    NSData *newSessionJsonData = [NSJSONSerialization dataWithJSONObject:sessionDic options:0 error:&jsonErr];
    NSData *newTimeJsonData = [NSJSONSerialization dataWithJSONObject:timeDic options:0 error:&jsonErr];
    NSData *newLastModTimeJsonData = [NSJSONSerialization dataWithJSONObject:lastModTimeDic options:0 error:&jsonErr];
    NSData *newCoverLastModTimeJsonData = [NSJSONSerialization dataWithJSONObject:coverLastModTimeDic options:0 error:&jsonErr];
    NSData *newResumeDaaJsonData = [NSJSONSerialization dataWithJSONObject:resumeDataDic options:0 error:&jsonErr];

    NSString *strNeweSession = [[NSString alloc] initWithData:newSessionJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewTime = [[NSString alloc] initWithData:newTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewLastModTime = [[NSString alloc] initWithData:newLastModTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewCoverLastModTime = [[NSString alloc] initWithData:newCoverLastModTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewResumeData = [[NSString alloc] initWithData:newResumeDaaJsonData encoding:NSUTF8StringEncoding];

    [[NSUserDefaults standardUserDefaults] setObject:strNeweSession forKey:TVCMultipartResumeSessionKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewTime forKey:TVCMultipartResumeExpireTimeKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewLastModTime forKey:TVCMultipartFileLastModTime];
    [[NSUserDefaults standardUserDefaults] setObject:strNewCoverLastModTime forKey:TVCMultipartCoverFileLastModTime];
    [[NSUserDefaults standardUserDefaults] setObject:strNewResumeData forKey:TVCMultipartResumeData];

    [[NSUserDefaults standardUserDefaults] synchronize];
}

// 上传完成
- (void)notifyResult:(TVCResultBlock)result resp:(TVCUploadResponse *)resp {
    [self txReportDAU];
    [self.timer setFireDate:[NSDate distantFuture]];
    dispatch_async(dispatch_get_main_queue(), ^{
        result(resp);
    });
}

- (void)txReportDAU {
    self.reportInfo.reqType = TVC_UPLOAD_EVENT_DAU;
    [[TVCReport shareInstance] addReportInfo:self.reportInfo];
}

- (void)txReport:(int)eventId
                errCode:(int)errCode
             vodErrCode:(int)vodErrCode
             cosErrCode:(NSString *)cosErrCode
                errInfo:(NSString *)errInfo
                reqTime:(int64_t)reqTime
            reqTimeCost:(int64_t)reqTimeCost
                 reqKey:(NSString *)reqKey
                  appId:(NSString *)appId
               fileSize:(int64_t)fileSize
               fileType:(NSString *)fileType
               fileName:(NSString *)fileName
             sessionKey:(NSString *)sessionKey
                 fileId:(NSString *)fileId
              cosRegion:(NSString *)cosRegion
              useCosAcc:(int)useCosAcc
           cosRequestId:(NSString *)cosRequestId
     cosTcpConnTimeCost:(int64_t)cosTcpConnTimeCost
    cosRecvRespTimeCost:(int64_t)cosRecvRespTimeCost {
    self.reportInfo.reqType = eventId;
    self.reportInfo.errCode = errCode;
    self.reportInfo.errMsg = (errInfo == nil ? @"" : errInfo);
    self.reportInfo.reqTime = reqTime;
    self.reportInfo.reqTimeCost = reqTimeCost;
    self.reportInfo.fileSize = fileSize;
    self.reportInfo.fileType = fileType;
    self.reportInfo.fileName = fileName;
    if (appId != 0) {
        self.reportInfo.appId = [appId longLongValue];
    }
    self.reportInfo.reqServerIp = self.serverIP;
    self.reportInfo.reportId = self.config.userID;
    self.reportInfo.reqKey = reqKey;
    self.reportInfo.vodSessionKey = sessionKey;
    self.reportInfo.fileId = fileId;
    self.reportInfo.vodErrCode = vodErrCode;
    self.reportInfo.cosErrCode = (cosErrCode == nil ? @"" : cosErrCode);
    self.reportInfo.cosRegion = (cosRegion == nil ? @"" : cosRegion);
    self.reportInfo.useCosAcc = useCosAcc;

    if (eventId == TVC_UPLOAD_EVENT_ID_COS) {
        self.reportInfo.useHttpDNS = 0;
        self.reportInfo.tcpConnTimeCost = cosTcpConnTimeCost;
        self.reportInfo.recvRespTimeCost = cosRecvRespTimeCost;
        self.reportInfo.requestId = (cosRequestId == nil ? @"" : cosRequestId);
    } else {
        self.reportInfo.useHttpDNS = [[TXUGCPublishOptCenter shareInstance] useHttpDNS:UGC_HOST] ? 1 : 0;
    }

    [[TVCReport shareInstance] addReportInfo:self.reportInfo];

    return;
}

- (void)queryIpWithDomain:(NSString *)domain {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        struct hostent *hs;
        struct sockaddr_in server;
        if ((hs = gethostbyname([domain UTF8String])) != NULL) {
            server.sin_addr = *((struct in_addr *)hs->h_addr_list[0]);
            self.serverIP = [NSString stringWithUTF8String:inet_ntoa(server.sin_addr)];
        } else {
            self.serverIP = domain;
        }
    });
}

- (NSDictionary *)getStatusInfo {
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:[NSString stringWithFormat:@"%d", self.reportInfo.reqType] forKey:@"reqType"];
    [info setObject:[NSString stringWithFormat:@"%d", self.reportInfo.errCode] forKey:@"errCode"];
    [info setObject:self.reportInfo.errMsg forKey:@"errMsg"];
    [info setObject:[NSString stringWithFormat:@"%lld", self.reportInfo.reqTime] forKey:@"reqTime"];
    [info setObject:[NSString stringWithFormat:@"%lld", self.reportInfo.reqTimeCost] forKey:@"reqTimeCost"];
    [info setObject:[NSString stringWithFormat:@"%lld", self.reportInfo.fileSize] forKey:@"fileSize"];
    [info setObject:self.reportInfo.fileType forKey:@"fileType"];
    [info setObject:self.reportInfo.fileName forKey:@"fileName"];
    [info setObject:self.reportInfo.fileId forKey:@"fileId"];
    [info setObject:[NSString stringWithFormat:@"%lld", self.reportInfo.appId] forKey:@"appId"];
    [info setObject:self.reportInfo.reqServerIp forKey:@"reqServerIp"];
    [info setObject:self.reportInfo.reportId forKey:@"reportId"];
    [info setObject:self.reportInfo.reqKey forKey:@"reqKey"];
    [info setObject:self.reportInfo.vodSessionKey forKey:@"vodSessionKey"];

    [info setObject:[NSString stringWithFormat:@"%d", self.reportInfo.vodErrCode] forKey:@"vodErrCode"];
    [info setObject:self.reportInfo.cosErrCode forKey:@"cosErrCode"];
    [info setObject:self.reportInfo.vodSessionKey forKey:@"cosRegion"];
    [info setObject:[NSString stringWithFormat:@"%d", self.reportInfo.useCosAcc] forKey:@"useCosAcc"];
    [info setObject:[NSString stringWithFormat:@"%d", self.reportInfo.useHttpDNS] forKey:@"useHttpDNS"];
    [info setObject:[NSString stringWithFormat:@"%lld", self.reportInfo.tcpConnTimeCost] forKey:@"tcpConnTimeCost"];
    [info setObject:[NSString stringWithFormat:@"%lld", self.reportInfo.recvRespTimeCost] forKey:@"recvRespTimeCost"];

    return info;
}

- (void)setAppId:(int)appId {
    if (appId != 0) {
        self.reportInfo.appId = appId;
    }
}

// 收集连接建立耗时、收到首包耗时。走httpdns的收集不到。
- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didFinishCollectingMetrics:(NSURLSessionTaskMetrics *)metrics {
    NSURLSessionTaskTransactionMetrics *metricsInfo = metrics.transactionMetrics[0];
    self.reportInfo.tcpConnTimeCost = [metricsInfo.connectEndDate timeIntervalSinceDate:metricsInfo.fetchStartDate] * 1000;
    self.reportInfo.recvRespTimeCost = [metricsInfo.responseStartDate timeIntervalSinceDate:metricsInfo.fetchStartDate] * 1000;
}

- (void)postVirtualProgress:(NSTimer *)timer {
    TVCUploadContext *uploadContext = [[timer userInfo] objectForKey:@"uploadContext"];
    TVCProgressBlock progress = uploadContext.progressBlock;
    if (progress) {
        long total = uploadContext.videoSize + uploadContext.coverSize;
        if ((self.virtualPercent >= 0 && self.virtualPercent < 10) || (self.virtualPercent >= 90 && self.virtualPercent < 100)) {
            ++self.virtualPercent;
            progress(self.virtualPercent * total / 100, total);
        }
    }
}

@end
