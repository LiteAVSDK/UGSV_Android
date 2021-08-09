//
//  TVCClientInner.m
//  TVCClientSDK
//
//  Created by tomzhu on 16/10/20.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TVCClientInner.h"

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
        _useCosAcc = 0;
        _cosAccDomain = @"";
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
        _vodCmdRequestCount = 0;
        _mainVodServerErrMsg = @"";
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
        _vodErrCode = 0;
        _cosErrCode = @"";
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
        _useHttpDNS = 0;
        _cosRegion = @"";
        _useCosAcc = 0;
        _tcpConnTimeCost = 0;
        _recvRespTimeCost = 0;
        _retryCount = 0;
        _reporting = NO;
        _requestId = @"";
    }
    return self;
}

@end
