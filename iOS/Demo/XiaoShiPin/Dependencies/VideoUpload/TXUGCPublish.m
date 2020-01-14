#import "TXUGCPublish.h"
#import "TVCHeader.h"
//#import "log.h"
//#import "TXRTMPAPI.h"
#import "TXUGCPublishUtil.h"
#import "TVCClient.h"

#undef _MODULE_
#define _MODULE_ "TXUGCPublish"


@implementation TXPublishParam
- (id)init {
    if ((self = [super init])) {
        _enableHTTPS = NO;
        _enableResume = YES;
    }
    return self;
}
@end

@implementation TXPublishResult
@end

@implementation TXMediaPublishParam
- (id)init {
    if ((self = [super init])) {
        _enableHTTPS = NO;
        _enableResume = YES;
    }
    return self;
}
@end

@implementation TXMediaPublishResult
@end


@interface TXUGCPublish ()
{
    TVCConfig*                      _tvcConfig;
    TVCUploadParam*                 _tvcParam;
    TVCClient*                      _tvcClient;
    NSString*                       _userID;
}

@property(nonatomic, assign) BOOL publishing;

@end


@implementation TXUGCPublish

-(id)init
{
    self = [super init];
    if(self != nil)
    {
        _userID = @"";
    }
    return self;
}

-(id) initWithUserID:(NSString *) userID{
    self = [super init];
    if(self != nil)
    {
        _userID = userID;
    }
    return self;
}

-(int) publishVideo:(TXPublishParam*)param{
    if (_publishing == YES) {
        //NSLog(@"there is existing uncompleted publish task");
        return -1;
    }
    
//    NSString* strVer = [TXLiveBase getSDKVersionStr];
    
//    [TXDRApi txReportDAU:DR_DAU_EVENT_ID_UGC_PUBLISH
//             withErrCode:0
//             withErrInfo:@""
//               withSdkId:DR_SDK_ID_RTMPSDK
//          withSdkVersion:strVer];

    if (param == nil) {
        NSLog(@"publishVideo: invalid param");
        return -2;
    }
    
//    if (param.secretId == nil || param.secretId.length == 0) {
//        NSLog(@"publishVideo: invalid secretId");
//        return -3;
//    }
    
    if (param.signature == nil || param.signature.length == 0) {
        NSLog(@"publishVideo: invalid signature");
        return -4;
    }
    
    if (param.videoPath == nil || param.videoPath.length == 0 || [[NSFileManager defaultManager] fileExistsAtPath:param.videoPath] == NO) {
        NSLog(@"publishVideo: invalid video file");
        return -5;
    }
    
    _publishing = YES;
    
    _tvcConfig = [[TVCConfig alloc] init];
    _tvcConfig.signature = param.signature;
    _tvcConfig.enableHttps = param.enableHTTPS;
    _tvcConfig.userID = _userID;
    _tvcConfig.enableResume = param.enableResume;
    //_tvcConfig.cosRegion = param.cosRegion;
    
    _tvcParam  = [[TVCUploadParam alloc] init];
    
    _tvcParam.videoPath = param.videoPath;

    _tvcParam.coverPath = param.coverPath;
    
    _tvcParam.videoName = param.fileName;
    
    __weak __typeof(self) weakSelf = self;
    _tvcClient = [[TVCClient alloc] initWithConfig:_tvcConfig];
    [_tvcClient uploadVideo:_tvcParam result:^(TVCUploadResponse *resp) {
        __strong __typeof(weakSelf) self = weakSelf;
        if (self == nil) {
            return;
        }
        
        if (resp) {
            NSLog(@"publish video result: retCode = %d descMsg = %s videoId = %s videoUrl = %s coverUrl = %s", resp.retCode, [resp.descMsg UTF8String], [resp.videoId UTF8String], [resp.videoURL UTF8String], [resp.coverURL UTF8String]);

            TXPublishResult * result = [[TXPublishResult alloc] init];
            result.retCode = resp.retCode;
            result.descMsg = resp.descMsg;
            result.videoId = resp.videoId;
            result.videoURL= resp.videoURL;
            result.coverURL= resp.coverURL;
            
            dispatch_async(dispatch_get_main_queue(), ^{
                if (self.delegate && [self.delegate respondsToSelector:@selector(onPublishComplete:)]) {
                    [self.delegate onPublishComplete:result];
                }
            });
        }
        self.publishing = NO;
        
    } progress:^(NSInteger bytesUpload, NSInteger bytesTotal) {
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (self.delegate && [self.delegate respondsToSelector:@selector(onPublishProgress:totalBytes:)]) {
                [self.delegate onPublishProgress:bytesUpload totalBytes:bytesTotal];
            }
        });
    }];
    
    return 0;
}

- (int)publishMedia:(TXMediaPublishParam*)param {
    if (_publishing == YES) {
        //NSLog(@"there is existing uncompleted publish task");
        return -1;
    }
    
    if (param == nil) {
        NSLog(@"publishMedia: invalid param");
        return -2;
    }
    
    if (param.signature == nil || param.signature.length == 0) {
        NSLog(@"publishMedia: invalid signature");
        return -4;
    }
    
    if (param.mediaPath == nil || param.mediaPath.length == 0 || [[NSFileManager defaultManager] fileExistsAtPath:param.mediaPath] == NO) {
        NSLog(@"publishMedia: invalid video file");
        return -5;
    }
    
    _publishing = YES;
    
    _tvcConfig = [[TVCConfig alloc] init];
    _tvcConfig.signature = param.signature;
    _tvcConfig.enableHttps = param.enableHTTPS;
    _tvcConfig.userID = _userID;
    _tvcConfig.enableResume = param.enableResume;
    //_tvcConfig.cosRegion = param.cosRegion;
    
    _tvcParam  = [[TVCUploadParam alloc] init];
    
    _tvcParam.videoPath = param.mediaPath;
    
//    _tvcParam.coverPath = param.coverPath;
    
    _tvcParam.videoName = param.fileName;
    
    __weak __typeof(self) weakSelf = self;
    _tvcClient = [[TVCClient alloc] initWithConfig:_tvcConfig];
    [_tvcClient uploadVideo:_tvcParam result:^(TVCUploadResponse *resp) {
        __strong __typeof(weakSelf) self = weakSelf;
        if (self == nil) {
            return;
        }
        
        if (resp) {
            NSLog(@"publish media result: retCode = %d descMsg = %s mediaId = %s mediaUrl = %s", resp.retCode, [resp.descMsg UTF8String], [resp.videoId UTF8String], [resp.videoURL UTF8String]);
            
            TXMediaPublishResult * result = [[TXMediaPublishResult alloc] init];
            result.retCode = resp.retCode;
            result.descMsg = resp.descMsg;
            result.mediaId = resp.videoId;
            result.mediaURL= resp.videoURL;
            
            dispatch_async(dispatch_get_main_queue(), ^{
                if (self.mediaDelegate && [self.mediaDelegate respondsToSelector:@selector(onMediaPublishComplete:)]) {
                    [self.mediaDelegate onMediaPublishComplete:result];
                }
            });
        }
        self.publishing = NO;
        
    } progress:^(NSInteger bytesUpload, NSInteger bytesTotal) {
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (self.mediaDelegate && [self.mediaDelegate respondsToSelector:@selector(onMediaPublishProgress:totalBytes:)]) {
                [self.mediaDelegate onMediaPublishProgress:bytesUpload totalBytes:bytesTotal];
            }
        });
    }];
    
    return 0;
}

/*
 * ȡ�����Ƶ����
 * ����ֵ��
 *      YES �ɹ���
 *      NO ʧ�ܣ�
 -(BOOL) canclePublish;
 */
-(BOOL) canclePublish
{
    BOOL result = NO;
    if (_tvcClient != nil) {
        result = [_tvcClient cancleUploadVideo];
    }
    if (result) {
        _publishing = NO;
    }
    return result;
}

-(void) setAppId: (int) appId
{
    if (_tvcClient != nil) {
        [_tvcClient setAppId:appId];
    }
}

/*
 * 获取上报信息
 */
-(NSDictionary *)getStatusInfo {
    if (_tvcClient != nil) {
        return [_tvcClient getStatusInfo];
    }
    return nil;
}

@end
