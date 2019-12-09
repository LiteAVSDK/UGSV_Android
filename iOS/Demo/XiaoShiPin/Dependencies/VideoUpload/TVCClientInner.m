//
//  TVCClientInner.m
//  TVCClientSDK
//
//  Created by tomzhu on 16/10/20.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TVCClientInner.h"

@implementation TVCHttpsDelegate

//- (void)dealloc
//{
//    NSLog(@"dealloc TVCHttpsDelegate");
//}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition, NSURLCredential * _Nullable))completionHandler
{
    //1)获取trust object
    SecTrustRef trust = challenge.protectionSpace.serverTrust;
    SecTrustResultType result;
    
    NSString *host = [[task currentRequest] valueForHTTPHeaderField:@"host"];
    if (host.length > 0) {
        //指定域名
        SecPolicyRef policyOverride = SecPolicyCreateSSL(true, (__bridge CFStringRef)host);
        NSMutableArray *policies = [NSMutableArray array];
        [policies addObject:(__bridge_transfer id)policyOverride];
        SecTrustSetPolicies(trust, (__bridge CFArrayRef)policies);
    }
    
    //2)SecTrustEvaluate对trust进行验证
    OSStatus status = SecTrustEvaluate(trust, &result);
    if (status == errSecSuccess &&
        (result == kSecTrustResultProceed ||
         result == kSecTrustResultUnspecified)) {
            
            //3)验证成功，生成NSURLCredential凭证cred，告知challenge的sender使用这个凭证来继续连接
            NSURLCredential *cred = [NSURLCredential credentialForTrust:trust];
            //        [challenge.sender useCredential:cred forAuthenticationChallenge:challenge];
            completionHandler(NSURLSessionAuthChallengeUseCredential, cred);
        } else {
            
            //5)验证失败，取消这次验证流程
            //        [challenge.sender cancelAuthenticationChallenge:challenge];
            completionHandler(NSURLSessionAuthChallengeUseCredential, nil);
        }
}

@end

@implementation TVCUGCResult

- (instancetype)init
{
    self = [super init];
    if (self) {
        _videoFileId = @"";
        _imageFileId = @"";
        _uploadAppid = @"";
        _uploadBucket = @"";
        _videoPath = @"";
        _imagePath = @"";
        _videoSign = @"";
        _imageSign = @"";
        _uploadSession = @"";
        _userAppid = @"";
        _tmpSecretId = @"";
        _tmpSecretKey = @"";
        _tmpToken = @"";
        _tmpExpiredTime = 0;
        _currentTS = 0;
    }
    return self;
}

- (NSString*)description
{
    return [NSString stringWithFormat:@"videoFileId=%@ iamgeFileId=%@ uploadAppid=%@ uploadBucket=%@ videoPath=%@ imagePath=%@ uploadSession=%@ uploadRegion=%@", _videoFileId, _imageFileId, _uploadAppid, _uploadBucket, _videoPath, _imagePath, _uploadSession, _uploadRegion];
}

@end

@implementation TVCUploadContext

- (void)dealloc
{
    NSLog(@"dealloc TVCUploadContext");
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        _lastStatus = TVC_OK;
        _desc = @"";
        _isUploadCover = NO;
        _isUploadVideo = NO;
        _videoSize = 0;
        _coverSize = 0;
        _currentUpload = 0;
        _reqTime = 0;
        _initReqTime = 0;
        _isShouldRetry = NO;
        _resumeData = nil;
    }
    return self;
}

- (NSString*)description
{
    return [NSString stringWithFormat:@"cug=%@", [_cugResult description]];
}

@end

@implementation TVCReportInfo

- (instancetype)init
{
    self = [super init];
    if (self) {
        _reqType = 0;
        _errCode = 0;
        _errMsg = @"";
        _reqTime = 0;
        _reqTimeCost = 0;
        _fileSize = 0;
        _fileType = @"";
        _fileName = @"";
        _fileId = @"";
        _appId = 0;
        _reqServerIp = @"";
        _reportId = @"";
        _reqKey = @"";
        _vodSessionKey = @"";
        _retryCount = 0;
        _reporting = NO;
    }
    return self;
}

@end
