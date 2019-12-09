//
//  TVCClient.m
//  VCDemo
//
//  Created by kennethmiao on 16/10/18.
//  Copyright © 2016年 kennethmiao. All rights reserved.
//

#import "TVCClient.h"
#import "TVCCommon.h"
#import "TVCClientInner.h"
#import "TVCReport.h"
#import <AVFoundation/AVFoundation.h>
#import <QCloudCore/QCloudCore.h>
#import <QCloudCore/QCloudAuthentationV5Creator.h>
#import <QCloudCOSXML/QCloudCOSXML.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
//#import "TXRTMPAPI.h"

#define TVCMultipartResumeSessionKey    @"TVCMultipartResumeSessionKey"         // 点播vodSessionKey
#define TVCMultipartResumeExpireTimeKey @"TVCMultipartResumeExpireTimeKey"      // vodSessionKey过期时间
#define TVCMultipartFileLastModTime     @"TVCMultipartFileLastModTime"          // 文件最后修改时间，用于在断点续传的时候判断文件是否修改
#define TVCMultipartResumeData          @"TVCMultipartUploadResumeData"         // cos分片上传文件resumeData

#define TVCUGCUploadCosKey             @"ugc_upload"

@interface TVCClient () <QCloudSignatureProvider>
@property(nonatomic, strong) TVCConfig *config;
@property(nonatomic, strong) QCloudAuthentationV5Creator* creator;
@property(nonatomic, strong) NSString* reqKey;
@property(nonatomic, strong) NSString* serverIP;
@property (nonatomic, strong) QCloudCOSXMLUploadObjectRequest* uploadRequest;
@property(nonatomic, strong) TVCReportInfo* reportInfo;
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
        self.serverIP = @"";
        self.reportInfo = [[TVCReportInfo alloc] init];
    }
    return self;
}

- (void)uploadVideo:(TVCUploadParam *)param result:(TVCResultBlock)result progress:(TVCProgressBlock)progress {
    TVCUploadResponse *rsp = nil;
    //check config
    rsp = [self checkConfig:self.config];
    if (rsp.retCode != TVC_OK) {
        dispatch_async(dispatch_get_main_queue(), ^{
            result(rsp);
        });
        return;
    }
    //check param
    rsp = [self checkParam:param];
    if (rsp.retCode != TVC_OK) {
        dispatch_async(dispatch_get_main_queue(), ^{
            result(rsp);
        });
        return;
    }

    //init upload context;
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
            } else {
                [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:TVC_ERR_FILE_NOT_EXIST errInfo:@"coverPath is not exist" reqTime:reqTime reqTimeCost:0 reqKey:@"" appId:0 fileSize:0 fileType:[self getFileType:param.coverPath] fileName:[self getFileName:param.coverPath] sessionKey:@"" fileId:@""];
                NSLog(@"coverPath is not exist");
                dispatch_async(dispatch_get_main_queue(), ^{
                    TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
                    rsp.retCode = TVC_ERR_FILE_NOT_EXIST;
                    rsp.descMsg = @"coverPath is not exist";
                    result(rsp);
                });
                return;
            }
        }
    } else {
        [self txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:TVC_ERR_FILE_NOT_EXIST errInfo:@"videoPath is not exist" reqTime:reqTime reqTimeCost:0 reqKey:@"" appId:0 fileSize:0 fileType:[self getFileType:param.videoPath] fileName:[self getFileName:param.videoPath] sessionKey:@"" fileId:@""];
        NSLog(@"videoPath is not exist");
        dispatch_async(dispatch_get_main_queue(), ^{
            TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
            rsp.retCode = TVC_ERR_FILE_NOT_EXIST;
            rsp.descMsg = @"videoPath is not exist";
            result(rsp);
        });
        return;
    }

    //1.获取cos参数
    NSString* vodSessionKey = nil;
    if (self.config.enableResume == YES) {
        vodSessionKey = [self getSessionFromFilepath:uploadContext];
    }
    [self getCosInitParam:uploadContext withVodSessionKey:vodSessionKey];
}

- (BOOL)cancleUploadVideo {
    if (self.uploadRequest) {
        NSError* error;
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

- (NSMutableURLRequest *)getCosInitURLRequest:(TVCUploadContext *)uploadContext withVodSessionKey:(NSString *)vodSessionKey {
    TVCUploadParam *param = uploadContext.uploadParam;
    // set url
    NSString *baseUrl = nil;
    if (YES) {
        baseUrl = UCG_HTTPS_URL;
    } else {
        baseUrl = UCG_HTTP_URL;
    }
    baseUrl = [baseUrl stringByAppendingString:@"?Action="];
    baseUrl = [baseUrl stringByAppendingString:@"ApplyUploadUGC"];

    // set body
    NSMutableDictionary *dictParam = [[NSMutableDictionary alloc] init];
    [dictParam setValue:self.config.signature forKey:@"signature"];

    // 有vodSessionKey的话表示是断点续传
    if (vodSessionKey && vodSessionKey.length) {
        [dictParam setValue:vodSessionKey forKey:@"vodSessionKey"];
    } else {
        [dictParam setValue:param.videoName forKey:@"videoName"];
        [dictParam setValue:[self getFileType:param.videoPath] forKey:@"videoType"];
        [dictParam setValue:@(uploadContext.videoSize) forKey:@"videoSize"];
        if (uploadContext.isUploadCover) {
            [dictParam setValue:[self getFileName:param.coverPath] forKey:@"coverName"];
            [dictParam setValue:[self getFileType:param.coverPath] forKey:@"coverType"];
            [dictParam setValue:@(uploadContext.coverSize) forKey:@"coverSize"];
        }
    }
    NSError *error = nil;
    NSData *bodyData = [NSJSONSerialization dataWithJSONObject:dictParam options:0 error:&error];
    if (error) {
        return nil;
    }

    // create request
    NSURL *url =[NSURL URLWithString:baseUrl];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setValue:[NSString stringWithFormat:@"%ld", (long) [bodyData length]] forHTTPHeaderField:@"Content-Length"];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"gzip" forHTTPHeaderField:@"Accept-Encoding"];
    [request setHTTPBody:bodyData];
    self.serverIP = [self queryIpWithDomain:url.host];
    
    return request;
}

- (NSMutableURLRequest *)getCosEndURLRequest:(TVCUploadContext *)uploadContext {
    NSString *baseUrl;;
//    TVCUploadParam *param = uploadContext.uploadParam;
    TVCUGCResult *ugc = uploadContext.cugResult;
//    if (YES) {
        baseUrl = [NSString stringWithFormat:@"https://%@/v3/index.php?Action=CommitUploadUGC", ugc.domain];
//    } else {
//        baseUrl = [NSString stringWithFormat:@"http://%@/v3/index.php?Action=CommitUploadUGC", ugc.domain];
//    }
    
    self.serverIP = [self queryIpWithDomain:ugc.domain];

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
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:baseUrl]];
    [request setValue:[NSString stringWithFormat:@"%ld", (long) [bodyData length]] forHTTPHeaderField:@"Content-Length"];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"gzip" forHTTPHeaderField:@"Accept-Encoding"];
    [request setHTTPBody:bodyData];

    NSLog(@"cos end req : %s", [baseUrl UTF8String]);

    return request;
}

- (void)getCosInitParam:(TVCUploadContext *)uploadContext withVodSessionKey:(NSString *)vodSessionKey {
    TVCResultBlock result = uploadContext.resultBlock;

    uploadContext.reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
    uploadContext.initReqTime = uploadContext.reqTime;
    self.reqKey = [NSString stringWithFormat:@"%lld;%lld", uploadContext.videoLastModTime, uploadContext.initReqTime];

    NSMutableURLRequest *cosRequest = [self getCosInitURLRequest:uploadContext withVodSessionKey:vodSessionKey];
    if (cosRequest == nil) {
        if (uploadContext.resultBlock) {
            dispatch_async(dispatch_get_main_queue(), ^{
                TVCUploadResponse *initResp = [[TVCUploadResponse alloc] init];
                initResp.retCode = TVC_ERR_UGC_REQUEST_FAILED;
                initResp.descMsg = @"create ugc publish request failed";
                result(initResp);
            });
            return;
        }
    }

    NSURLSessionConfiguration *initCfg = [NSURLSessionConfiguration defaultSessionConfiguration];
    [initCfg setRequestCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    if (self.config.timeoutInterval > 0) {
        [initCfg setTimeoutIntervalForRequest:self.config.timeoutInterval];
    } else {
        [initCfg setTimeoutIntervalForRequest:kTimeoutInterval];
    }
    NSURLSession *initSess = [NSURLSession sessionWithConfiguration:initCfg delegate:[[TVCHttpsDelegate alloc] init] delegateQueue:nil];

    __weak TVCClient *ws = self;
    __weak NSURLSession *wis = initSess;
    NSURLSessionTask *initTask = [initSess dataTaskWithRequest:cosRequest completionHandler:^(NSData *_Nullable initData, NSURLResponse *_Nullable response, NSError *_Nullable error) {
        //invalid NSURLSession
        [wis invalidateAndCancel];

        TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
        unsigned long long reqTimeCost = 0;

        NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;
        if (error || httpResponse.statusCode != 200 || initData == nil) {
            // 删除session
            [self setSession:nil resumeData:nil lastModTime:0 withFilePath:uploadContext.uploadParam.videoPath];

            //1步骤出错
            NSLog(@"ugc init http req fail : error=%ld response=%s", (long)error.code, [httpResponse.description UTF8String]);
            rsp.retCode = TVC_ERR_UGC_REQUEST_FAILED;
            rsp.descMsg = [NSString stringWithFormat:@"ugc code:%ld, ugc desc:%@", (long)error.code, @"ugc init http req fail"];
            
            reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
            [ws txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode errInfo:rsp.descMsg reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:0 fileSize:uploadContext.videoSize fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:@"" fileId:@""];
            if (result) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    result(rsp);
                });
            }
            return;
        }
        NSError *jsonErr = nil;
        NSDictionary *initDict = [NSJSONSerialization JSONObjectWithData:initData options:NSJSONReadingAllowFragments error:&jsonErr];
        if (jsonErr || ![initDict isKindOfClass:[NSDictionary class]]) {
            // 删除session
            [self setSession:nil resumeData:nil lastModTime:0 withFilePath:uploadContext.uploadParam.videoPath];
            
            rsp.retCode = TVC_ERR_UGC_PARSE_FAILED;
            rsp.descMsg = [NSString stringWithFormat:@"ugc code:%ld, ugc desc:%@", (long)jsonErr.code, @"ugc parse init http fail"];
            
            reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
            [ws txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode errInfo:rsp.descMsg reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:0 fileSize:uploadContext.videoSize
                fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:@"" fileId:@""];
            
            if (result) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    result(rsp);
                });
            }
            return;
        }

		int code = -1;
        if ([[initDict objectForKey:kCode] isKindOfClass:[NSNumber class]]) {
            code = [[initDict objectForKey:kCode] intValue];
        }
        NSString *msg;;
        if ([[initDict objectForKey:kMessage] isKindOfClass:[NSString class]]) {
            msg = [initDict objectForKey:kMessage];
        }
		
		if (code != TVC_OK) {
            // 删除session
            [self setSession:nil resumeData:nil lastModTime:0 withFilePath:uploadContext.uploadParam.videoPath];
            
            rsp.retCode = TVC_ERR_UGC_REQUEST_FAILED;
            rsp.descMsg = [NSString stringWithFormat:@"ugc code:%d, ugc desc:%@", code, msg];
            
            reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
            [ws txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode errInfo:rsp.descMsg reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:0 fileSize:uploadContext.videoSize
                fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:@"" fileId:@""];

            //1步骤出错
            if (result) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    result(rsp);
                });
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
            
            rsp.retCode = TVC_ERR_UGC_PARSE_FAILED;
            rsp.descMsg = @"data is not json string";
            
            reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
            [ws txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:rsp.retCode errInfo:rsp.descMsg reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:0 fileSize:uploadContext.videoSize
                fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:@"" fileId:@""];

            if (result) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    result(rsp);
                });
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
            ugc.uploadBucket = [dataDict objectForKey:@"storageBucket"];
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

        uploadContext.cugResult = ugc;

        NSLog(@"init cugResult %s", [[uploadContext.cugResult description] UTF8String]);

        reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
        [ws txReport:TVC_UPLOAD_EVENT_ID_INIT errCode:TVC_OK errInfo:@"" reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:ugc.userAppid fileSize:uploadContext.videoSize
            fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:ugc.uploadSession fileId:@""];
        
        [ws setupCOSXMLShareService:uploadContext];
        
        //2.开始上传
        uploadContext.reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
        if (vodSessionKey && vodSessionKey.length) {
            [ws commitCosUpload:uploadContext withResumeUpload:YES];
        } else {
            [ws commitCosUpload:uploadContext withResumeUpload:NO];
        }
    }];
    [initTask resume];
}

- (void) signatureWithFields:(QCloudSignatureFields*)fileds
                     request:(QCloudBizHTTPRequest*)request
                  urlRequest:(NSMutableURLRequest*)urlRequst
                   compelete:(QCloudHTTPAuthentationContinueBlock)continueBlock
{
    QCloudSignature* signature = nil;
    if (_creator != nil) {
        signature = [_creator signatureForData:urlRequst];
    }
    continueBlock(signature, nil);
}

- (void)setupCOSXMLShareService:(TVCUploadContext *)uploadContext {
    QCloudCredential* credential = [QCloudCredential new];
    credential.secretID = uploadContext.cugResult.tmpSecretId;
    credential.secretKey = uploadContext.cugResult.tmpSecretKey;
    credential.token = uploadContext.cugResult.tmpToken;
    long long nowTime = [[NSDate date] timeIntervalSince1970];
    long long serverTS = uploadContext.cugResult.currentTS;
    //如果本地时间戳跟后台返回的当前时间戳相差太大，就用后台返回的时间戳。避免本地时间错误导致403
    if (serverTS != 0 && nowTime - serverTS > 10*60 ) {
        credential.startDate = [NSDate dateWithTimeIntervalSince1970:serverTS];
    }
    credential.experationDate = [NSDate dateWithTimeIntervalSince1970:uploadContext.cugResult.tmpExpiredTime];
    _creator = [[QCloudAuthentationV5Creator alloc] initWithCredential:credential];
    
    QCloudServiceConfiguration* configuration = [QCloudServiceConfiguration new];
    
    configuration.appID = uploadContext.cugResult.uploadAppid;
    configuration.signatureProvider = self;
    
    QCloudCOSXMLEndPoint* endpoint = [[QCloudCOSXMLEndPoint alloc] init];
    endpoint.regionName = uploadContext.cugResult.uploadRegion;
    endpoint.useHTTPS = self.config.enableHttps;
    configuration.endpoint = endpoint;
    
    self.serverIP = [self queryIpWithDomain:[endpoint serverURLWithBucket:uploadContext.cugResult.uploadBucket appID:uploadContext.cugResult.uploadAppid].host];
    
    [QCloudCOSXMLService registerCOSXMLWithConfiguration:configuration withKey:TVCUGCUploadCosKey];
    [QCloudCOSTransferMangerService registerCOSTransferMangerWithConfiguration:configuration withKey:TVCUGCUploadCosKey];
}

- (void)commitCosUpload:(TVCUploadContext *)uploadContext withResumeUpload:(BOOL)isResumeUpload {
    dispatch_group_t group = dispatch_group_create();
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    uploadContext.gcdGroup = group;
    uploadContext.gcdSem = semaphore;
    uploadContext.gcdQueue = queue;
    //2-1.开始上传视频
    TVCUploadParam *param = uploadContext.uploadParam;
    TVCUGCResult *cug = uploadContext.cugResult;
    
    NSLog(@"uploadCosVideo begin : cosBucket:%@ ,cos videoPath:%@, path:%@", cug.uploadBucket, cug.videoPath, param.videoPath);
    
    __block long long reqTimeCost = 0;
    __weak TVCClient *ws = self;
    if (uploadContext.isUploadVideo) {
        dispatch_group_async(group, queue, ^{
            QCloudCOSXMLUploadObjectRequest* videoUpload;
            
            if (uploadContext.resumeData != nil && uploadContext.resumeData.length != 0) {
                videoUpload = [QCloudCOSXMLUploadObjectRequest requestWithRequestData:uploadContext.resumeData];
            } else {
                videoUpload = [QCloudCOSXMLUploadObjectRequest new];
                videoUpload.body = [NSURL fileURLWithPath:param.videoPath];
                videoUpload.bucket = cug.uploadBucket;
                videoUpload.object = cug.videoPath;
                [videoUpload setInitMultipleUploadFinishBlock:^(QCloudInitiateMultipartUploadResult *multipleUploadInitResult, QCloudCOSXMLUploadObjectResumeData resumeData) {
                    if (multipleUploadInitResult != nil && resumeData != nil) {
                        [self setSession:cug.uploadSession resumeData:resumeData lastModTime:uploadContext.videoLastModTime withFilePath:param.videoPath];
                    }
                }];
            }
            
            [videoUpload setFinishBlock:^(QCloudUploadObjectResult *result, NSError * error) {
                NSLog(@"uploadCosVideo finish : cosBucket:%@ ,cos videoPath:%@, path:%@, size:%lld", cug.uploadBucket, cug.videoPath, param.videoPath, uploadContext.videoSize);
                if (error) {
                    reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                    NSString * errInfo = error.description;
                    NSString * cosErrorCode = @"";
                    if (error.userInfo != nil) {
                        errInfo = error.userInfo.description;
                        cosErrorCode = error.userInfo[@"Code"];
                    }
                    
                    if ([cosErrorCode isEqualToString:@"RequestTimeTooSkewed"]) {
                        uploadContext.isShouldRetry = YES;
                    }
                    [ws txReport:TVC_UPLOAD_EVENT_ID_COS errCode:TVC_ERR_VIDEO_UPLOAD_FAILED errInfo:errInfo reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:cug.userAppid fileSize:uploadContext.videoSize
                        fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:cug.uploadSession fileId:@""];
                    // 取消的情况不清除session缓存
                    if (error.code == -34009) {
                        uploadContext.lastStatus = TVC_ERR_USER_CANCLE;
                        uploadContext.desc = [NSString stringWithFormat:@"upload video, user cancled"];
                    } else {
                        uploadContext.lastStatus = TVC_ERR_VIDEO_UPLOAD_FAILED;
                        uploadContext.desc = [NSString stringWithFormat:@"upload video, cos code:%ld, cos desc:%@", (long)error.code, error.description];
                        //网络断开，不清除session缓存
                        if (error.code != -34004) {
                            [ws setSession:nil resumeData:nil lastModTime:0 withFilePath:param.videoPath];
                        }
                        dispatch_semaphore_signal(semaphore);
                    }
                } else {
                    NSLog(@"upload video succ");
                    //视频上传完成，清除session缓存
                    [ws setSession:nil resumeData:nil lastModTime:0 withFilePath:param.videoPath];
                    //2-2.开始上传封面
                    if (uploadContext.isUploadCover) {
                        uploadContext.reqTime = [[NSDate date] timeIntervalSince1970] * 1000;
                        
                        QCloudCOSXMLUploadObjectRequest* coverUpload = [QCloudCOSXMLUploadObjectRequest new];
                        coverUpload.body = [NSURL fileURLWithPath:param.coverPath];
                        coverUpload.bucket = cug.uploadBucket;
                        coverUpload.object = cug.imagePath;
                        
                        [coverUpload setFinishBlock:^(QCloudUploadObjectResult *result, NSError * error) {
                            if (error) {
                                //2-2步骤出错
                                NSLog(@"upload cover fail : %ld", (long)error.code);
                                NSString * errInfo = error.description;
                                if (error.userInfo != nil) {
                                    errInfo = error.userInfo.description;
                                }
                                uploadContext.lastStatus = TVC_ERR_COVER_UPLOAD_FAILED;
                                uploadContext.desc = errInfo;
                            } else {
                                NSLog(@"upload cover succ");
                            }
                            reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                            [ws txReport:TVC_UPLOAD_EVENT_ID_COS errCode:uploadContext.lastStatus errInfo:uploadContext.desc reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:cug.userAppid fileSize:uploadContext.coverSize fileType:[ws getFileType:uploadContext.uploadParam.coverPath] fileName:[ws getFileName:uploadContext.uploadParam.coverPath] sessionKey:cug.uploadSession fileId:@""];
                            dispatch_semaphore_signal(semaphore);
                        }];
                        
                        TVCProgressBlock progress = uploadContext.progressBlock;
                        [coverUpload setSendProcessBlock:^(int64_t bytesSent, int64_t totalBytesSent, int64_t totalBytesExpectedToSend) {
                            if (progress) {
                                uint64_t total = uploadContext.videoSize + uploadContext.coverSize;
                                uploadContext.currentUpload += totalBytesSent;
                                if (uploadContext.currentUpload > total) {
                                    uploadContext.currentUpload = total;
                                }
                                progress(uploadContext.currentUpload, total);
                            }
                        }];
                        ws.uploadRequest = coverUpload;
                        [[QCloudCOSTransferMangerService costransfermangerServiceForKey:TVCUGCUploadCosKey] UploadObject:coverUpload];
                    }
                }
                dispatch_semaphore_signal(semaphore);
            }];
            
            TVCProgressBlock progress = uploadContext.progressBlock;
            [videoUpload setSendProcessBlock:^(int64_t bytesSent, int64_t totalBytesSent, int64_t totalBytesExpectedToSend) {
                if (progress) {
                    uint64_t total = uploadContext.videoSize + uploadContext.coverSize;
                    uploadContext.currentUpload = totalBytesSent;
                    if (uploadContext.currentUpload > total) {
                        uploadContext.currentUpload = total;
                    }
                    progress(uploadContext.currentUpload, total);
                }
            }];
            ws.uploadRequest = videoUpload;
            [[QCloudCOSTransferMangerService costransfermangerServiceForKey:TVCUGCUploadCosKey] UploadObject:videoUpload];
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
                    //1.获取cos参数
                    NSString* vodSessionKey = nil;
                    if (self.config.enableResume == YES) {
                        vodSessionKey = [self getSessionFromFilepath:uploadContext];
                    }
                    [self getCosInitParam:uploadContext withVodSessionKey:vodSessionKey];
                });
            } else if (result) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    TVCUploadResponse *rsp = [[TVCUploadResponse alloc] init];
                    rsp.retCode = uploadContext.lastStatus;
                    rsp.descMsg = uploadContext.desc;
                    result(rsp);
                });
                return;
            }
        } else {
            //3.完成上传
            NSLog(@"complete upload task");
            NSMutableURLRequest *cosFiniURLRequest = [ws getCosEndURLRequest:uploadContext];
            NSURLSessionConfiguration *finishCfg = [NSURLSessionConfiguration defaultSessionConfiguration];
            [finishCfg setRequestCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
            if (self.config.timeoutInterval > 0) {
                [finishCfg setTimeoutIntervalForRequest:self.config.timeoutInterval];
            } else {
                [finishCfg setTimeoutIntervalForRequest:kTimeoutInterval];
            }
            NSURLSession *finishSess = [NSURLSession sessionWithConfiguration:finishCfg delegate:[[TVCHttpsDelegate alloc] init] delegateQueue:nil];

            __weak NSURLSession *wfs = finishSess;
            NSURLSessionTask *finiTask = [finishSess dataTaskWithRequest:cosFiniURLRequest completionHandler:^(NSData *_Nullable finiData, NSURLResponse *_Nullable response, NSError *_Nullable error) {

                //invalid NSURLSession
                [wfs invalidateAndCancel];

                NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;
                if (error || httpResponse.statusCode != 200 || finiData == nil) {
                    //3步骤出错
                    NSLog(@"cos end http req fail : error=%ld response=%s", (long)error.code, [httpResponse.description UTF8String]);
                    if (result) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            TVCUploadResponse *initResp = [[TVCUploadResponse alloc] init];
                            initResp.retCode = TVC_ERR_UGC_FINISH_REQ_FAILED;
                            initResp.descMsg = [NSString stringWithFormat:@"ugc code:%ld, ugc desc:%@", (long)error.code, @"ugc finish http req fail"];
                            result(initResp);

                            long long reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                            [ws txReport:TVC_UPLOAD_EVENT_ID_FINISH errCode:initResp.retCode errInfo:initResp.descMsg reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:uploadContext.cugResult.userAppid fileSize:uploadContext.videoSize fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:uploadContext.cugResult.uploadSession fileId:@""];
                        });
                    }
                    return;
                }
                NSDictionary *finiDict = [NSJSONSerialization JSONObjectWithData:finiData options:(NSJSONReadingMutableLeaves) error:nil];

                NSError *parseError = nil;
                NSData *jsonData = [NSJSONSerialization dataWithJSONObject:finiDict options:NSJSONWritingPrettyPrinted error:&parseError];
                NSString *finiDictStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

                NSLog(@"end cos dic : %@", finiDictStr);

                int code = -1;
                if ([[finiDict objectForKey:kCode] isKindOfClass:[NSNumber class]]) {
                    code = [[finiDict objectForKey:kCode] intValue];
                }
                NSString *msg;;
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
                        if (ws.config.enableHttps == YES) {
                            videoURL = [[videoDic objectForKey:@"url"] stringByReplacingOccurrencesOfString:@"http:" withString:@"https:"];
                        } else {
                            videoURL = [videoDic objectForKey:@"url"];
                        }
                    }
                    if ([[dataDict objectForKey:@"cover"] isKindOfClass:[NSDictionary class]]) {
                        coverDic = [dataDict objectForKey:@"cover"];
                        if (ws.config.enableHttps == YES) {
                            coverURL = [[coverDic objectForKey:@"url"] stringByReplacingOccurrencesOfString:@"http:" withString:@"https:"];
                        } else {
                            coverURL = [coverDic objectForKey:@"url"];
                        }
                    }
                    if ([[dataDict objectForKey:@"fileId"] isKindOfClass:[NSString class]]) {
                        videoID = [dataDict objectForKey:@"fileId"];
                    }
                }

                TVCUploadResponse *finiResp = [[TVCUploadResponse alloc] init];
                if (code != TVC_OK) {
                    //3步骤出错
                    finiResp.retCode = TVC_ERR_UGC_FINISH_RSP_FAILED;
                    finiResp.descMsg = [NSString stringWithFormat:@"ugc code:%d, ugc desc:%@ ugc finish http rsp fail", code, msg];
                    if (result) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            result(finiResp);
                            long long reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                            [ws txReport:TVC_UPLOAD_EVENT_ID_FINISH errCode:finiResp.retCode errInfo:finiResp.descMsg reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:uploadContext.cugResult.userAppid fileSize:uploadContext.videoSize fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:uploadContext.cugResult.uploadSession fileId:@""];
                        });
                    }
                    return;
                } else {
                    //所有步骤成功完成
                    finiResp.retCode = TVC_OK;
                    finiResp.videoId = videoID;
                    finiResp.videoURL = videoURL;
                    finiResp.coverURL = coverURL;
                    if (result) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            result(finiResp);
                            long long reqTimeCost = [[NSDate date] timeIntervalSince1970] * 1000 - uploadContext.reqTime;
                            [ws txReport:TVC_UPLOAD_EVENT_ID_FINISH errCode:finiResp.retCode errInfo:finiResp.descMsg reqTime:uploadContext.reqTime reqTimeCost:reqTimeCost reqKey:ws.reqKey appId:uploadContext.cugResult.userAppid fileSize:uploadContext.videoSize fileType:[ws getFileType:uploadContext.uploadParam.videoPath] fileName:[ws getFileName:uploadContext.uploadParam.videoPath] sessionKey:uploadContext.cugResult.uploadSession fileId:videoID];
                        });
                    }
                    return;
                }
            }];
            [finiTask resume];
        }
    });
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


#pragma mark -- 断点续传

// 本地保存 filePath --> session、filePath --> expireTime，filePath --> fileLastModTime, filePath --> resumeData 的映射集合，格式为json
// "TVCMultipartResumeSessionKey": {filePath1: session1, filePath2: session2, filePath3: session3}
// "TVCMultipartResumeExpireTimeKey": {filePath1: expireTime1, filePath2: expireTime2, filePath3: expireTime3}
// session的过期时间是1天
- (NSString *)getSessionFromFilepath:(TVCUploadContext *)uploadContext {
    NSString* filePath = uploadContext.uploadParam.videoPath;
    if (filePath == nil || filePath.length == 0) {
        return nil;
    }
    
    NSMutableDictionary *sessionDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *timeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *lastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *resumeDataDic = [[NSMutableDictionary alloc] init];

    NSError *jsonErr = nil;

    // read [itemPath, session]
    NSString *strPathToSession = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeSessionKey];
    if (strPathToSession == nil) {
        NSLog(@"TVCMultipartResumeSessionKey is nil");
        return nil;
    }
    sessionDic = [NSJSONSerialization JSONObjectWithData:[strPathToSession dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
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
    timeDic = [NSJSONSerialization JSONObjectWithData:[strPathToExpireTime dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
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
    lastModTimeDic = [NSJSONSerialization JSONObjectWithData:[strPathToLastModTime dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
    if (jsonErr) {
        NSLog(@"TVCMultipartFileLastModTime is not json format: %@", strPathToLastModTime);
        return nil;
    }
    
    // read [itemPath, resumeData]
    NSString *strPathToResumeData = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeData];
    if (strPathToResumeData == nil) {
        NSLog(@"TVCMultipartResumeSessionKey resumeData is nil");
        return nil;
    }
    resumeDataDic = [NSJSONSerialization JSONObjectWithData:[strPathToResumeData dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
    if (jsonErr) {
        NSLog(@"TVCMultipartReumeData is not json format: %@", strPathToResumeData);
        return nil;
    }
    
    NSString *session = [sessionDic objectForKey:filePath];
    NSInteger expireTime = [[timeDic objectForKey:filePath] integerValue];
    unsigned long long lastModTime = [[lastModTimeDic objectForKey:filePath] unsignedLongLongValue];
    NSString* sResumeData = [resumeDataDic objectForKey:filePath];
    NSInteger nowTime = (NSInteger) [[NSDate date] timeIntervalSince1970] + 1;
    NSString *ret = nil;
    
    if (session && nowTime < expireTime && lastModTime == uploadContext.videoLastModTime && sResumeData != nil && sResumeData.length != 0) {
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
    NSMutableDictionary *newResumeDataDic = [[NSMutableDictionary alloc] init];
    for (NSString *key in timeDic) {
        NSInteger expireTime = [[timeDic objectForKey:key] integerValue];
        if (nowTime < expireTime) {
            [newSessionDic setValue:[sessionDic objectForKey:key] forKey:key];
            [newTimeDic setValue:[timeDic objectForKey:key] forKey:key];
            [newLastModTimeDic setValue:[lastModTimeDic objectForKey:key] forKey:key];
            [newResumeDataDic setValue:[resumeDataDic objectForKey:key] forKey:key];
        }
    }

    // 将newSessionDic 和 newTimeDic 保存文件
    NSData *newSessionJsonData = [NSJSONSerialization dataWithJSONObject:newSessionDic options:0 error:&jsonErr];
    NSData *newTimeJsonData = [NSJSONSerialization dataWithJSONObject:newTimeDic options:0 error:&jsonErr];
    NSData *newLastModTimeJsonData = [NSJSONSerialization dataWithJSONObject:newLastModTimeDic options:0 error:&jsonErr];
    NSData *newResumeDataJsonData = [NSJSONSerialization dataWithJSONObject:newResumeDataDic options:0 error:&jsonErr];

    NSString *strNeweSession = [[NSString alloc] initWithData:newSessionJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewTime = [[NSString alloc] initWithData:newTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewLastModTime = [[NSString alloc] initWithData:newLastModTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewResumeData = [[NSString alloc] initWithData:newResumeDataJsonData encoding:NSUTF8StringEncoding];

    [[NSUserDefaults standardUserDefaults] setObject:strNeweSession forKey:TVCMultipartResumeSessionKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewTime forKey:TVCMultipartResumeExpireTimeKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewLastModTime forKey:TVCMultipartFileLastModTime];
    [[NSUserDefaults standardUserDefaults] setObject:strNewResumeData forKey:TVCMultipartResumeData];

    [[NSUserDefaults standardUserDefaults] synchronize];
    
    return ret;
}

- (void)setSession:(NSString *)session resumeData:(NSData *)resumeData lastModTime:(uint64_t)lastModTime withFilePath:(NSString *)filePath {
    if (filePath == nil || filePath.length == 0) {
        return;
    }

    NSMutableDictionary *sessionDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *timeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *lastModTimeDic = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *resumeDataDic = [[NSMutableDictionary alloc] init];
    NSError *jsonErr = nil;

    // read [itemPath, session]
    NSString *strPathToSession = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeSessionKey];
    if (strPathToSession) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToSession dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
        sessionDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }

    // read [itemPath, expireTime]
    NSString *strPathToExpireTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeExpireTimeKey];
    if (strPathToExpireTime) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToExpireTime dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
        timeDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }
    
    // read [itemPath, lastModTime]
    NSString *strPathToLastModTime = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartFileLastModTime];
    if (strPathToLastModTime) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToLastModTime dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
        lastModTimeDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }
    
    // read [itemPath, resumeData]
    NSString *strPathToResumeData = [[NSUserDefaults standardUserDefaults] objectForKey:TVCMultipartResumeData];
    if (strPathToResumeData) {
        NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:[strPathToResumeData dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingAllowFragments error:&jsonErr];
        resumeDataDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    }

    // 设置过期时间为1天
    NSInteger expireTime = (NSInteger) [[NSDate date] timeIntervalSince1970] + 24 * 60 * 60;

    // session、resumeDataDic 为空，lastModTime为0就表示删掉该 [key, value]
    if (session == nil || session.length == 0
        || resumeData == nil || resumeData.length == 0
        || lastModTime == 0) {
        [sessionDic removeObjectForKey:filePath];
        [timeDic removeObjectForKey:filePath];
        [lastModTimeDic removeObjectForKey:filePath];
        [resumeDataDic removeObjectForKey:filePath];
    } else {
        [sessionDic setValue:session forKey:filePath];
        [timeDic setValue:@(expireTime) forKey:filePath];
        [lastModTimeDic setValue:@(lastModTime) forKey:filePath];
        NSString * sResumeData = [resumeData base64EncodedStringWithOptions:0];
        [resumeDataDic setValue:sResumeData forKey:filePath];
    }

    // 保存文件
    NSData *newSessionJsonData = [NSJSONSerialization dataWithJSONObject:sessionDic options:0 error:&jsonErr];
    NSData *newTimeJsonData = [NSJSONSerialization dataWithJSONObject:timeDic options:0 error:&jsonErr];
    NSData *newLastModTimeJsonData = [NSJSONSerialization dataWithJSONObject:lastModTimeDic options:0 error:&jsonErr];
    NSData *newResumeDaaJsonData = [NSJSONSerialization dataWithJSONObject:resumeDataDic options:0 error:&jsonErr];

    NSString *strNeweSession = [[NSString alloc] initWithData:newSessionJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewTime = [[NSString alloc] initWithData:newTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewLastModTime = [[NSString alloc] initWithData:newLastModTimeJsonData encoding:NSUTF8StringEncoding];
    NSString *strNewResumeData = [[NSString alloc] initWithData:newResumeDaaJsonData encoding:NSUTF8StringEncoding];

    [[NSUserDefaults standardUserDefaults] setObject:strNeweSession forKey:TVCMultipartResumeSessionKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewTime forKey:TVCMultipartResumeExpireTimeKey];
    [[NSUserDefaults standardUserDefaults] setObject:strNewLastModTime forKey:TVCMultipartFileLastModTime];
    [[NSUserDefaults standardUserDefaults] setObject:strNewResumeData forKey:TVCMultipartResumeData];

    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void) txReport:(int)eventId errCode:(int)errCode errInfo:(NSString*)errInfo reqTime:(int64_t)reqTime reqTimeCost:(int64_t)reqTimeCost reqKey:(NSString*)reqKey appId:(NSString*)appId fileSize:(int64_t)fileSize fileType:(NSString*)fileType fileName:(NSString*)fileName sessionKey:(NSString*)sessionKey fileId:(NSString*)fileId
{
    self.reportInfo.reqType = eventId;
    self.reportInfo.errCode = errCode;
    self.reportInfo.errMsg = (errInfo == nil? @"": errInfo);
    self.reportInfo.reqTime = reqTime;
    self.reportInfo.reqTimeCost = reqTimeCost;
    self.reportInfo.fileSize = fileSize;
    self.reportInfo.fileType = fileType;
    self.reportInfo.fileName = fileName;
    self.reportInfo.appId = [appId longLongValue];
    self.reportInfo.reqServerIp = self.serverIP;
    self.reportInfo.reportId = self.config.userID;
    self.reportInfo.reqKey = reqKey;
    self.reportInfo.vodSessionKey = sessionKey;
    self.reportInfo.vodSessionKey = sessionKey;
    self.reportInfo.fileId = fileId;
    
    [[TVCReport shareInstance] addReportInfo:self.reportInfo];
    
    return;
}

- (NSString *)queryIpWithDomain:(NSString *)domain
{
    struct hostent *hs;
    struct sockaddr_in server;
    if ((hs = gethostbyname([domain UTF8String])) != NULL)
    {
        server.sin_addr = *((struct in_addr*)hs->h_addr_list[0]);
        return [NSString stringWithUTF8String:inet_ntoa(server.sin_addr)];
    }
    return domain;
}

-(NSDictionary *)getStatusInfo{
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
    
    return info;
}
@end
