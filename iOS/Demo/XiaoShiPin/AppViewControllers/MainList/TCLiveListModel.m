//
//  TCLiveListModel.m
//  TCLVBIMDemo
//
//  Created by annidyfeng on 16/8/3.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCLiveListModel.h"
#import "TCUtil.h"


@implementation TCLiveUserInfo

- (void) encodeWithCoder: (NSCoder *)coder {
    [coder encodeObject:_nickname forKey:@"nickname" ];
    [coder encodeObject:_headpic forKey:@"headpic" ];
    [coder encodeObject:_frontcover forKey:@"frontcover" ];
    [coder encodeObject:_location forKey:@"location" ];
}

- (id) initWithCoder: (NSCoder *) coder
{
    self = [super init];
    if (self) {
        self.nickname = [coder decodeObjectForKey:@"nickname" ];
        self.headpic = [coder decodeObjectForKey:@"headpic" ];
        self.frontcover = [coder decodeObjectForKey:@"frontcover" ];
        self.location = [coder decodeObjectForKey:@"location" ];
    }
    return self;
}

@end

@implementation TCLiveInfo

- (void) encodeWithCoder: (NSCoder *)coder {
    [coder encodeObject:_userid forKey:@"userid" ];
    [coder encodeObject:_groupid forKey:@"groupid" ];
    [coder encodeObject:_title forKey:@"title" ];
    [coder encodeObject:_playurl forKey:@"playurl" ];
    [coder encodeObject:_fileid forKey:@"fileid" ];
    [coder encodeObject:_userinfo forKey:@"userinfo" ];
}

- (id) initWithCoder: (NSCoder *) coder
{
    self = [super init];
    if (self) {
        self.userid = [coder decodeObjectForKey:@"userid" ];
        self.groupid = [coder decodeObjectForKey:@"groupid" ];
        self.title = [coder decodeObjectForKey:@"title" ];
        self.playurl = [coder decodeObjectForKey:@"playurl" ];
        self.fileid = [coder decodeObjectForKey:@"fileid" ];
        self.userinfo = [coder decodeObjectForKey:@"userinfo" ];
    }
    return self;
}

@end


// -----------------------------------------------------------------------------

#import <AFNetworking/AFNetworking.h>
//#import <MJExtension/MJExtension.h>

#define pageSize 20
#define userDefaultsKey @"TCLiveListMgr"


#define QUOTE(...) @#__VA_ARGS__
//*
NSString *json = QUOTE(
                       {
                           "returnValue": 0,
                           "returnMsg": "return successfully!",
                           "returnData": {
                               "all_count": 1,
                               "pusherlist": [
                                              {
                                                  "userid" : "aaaa",
                                                  "groupid" : "bbbb",
                                                  "timestamp" : 1874483992,
                                                  "type" : 1,
                                                  "viewercount" : 1888,
                                                  "likecount" : 888,
                                                  "title" : "Testest",
                                                  "playurl" : "rtmp://live.hkstv.hk.lxdns.com/live/hks",
                                                  "userinfo" : {
                                                      "nickname": "Testest",
                                                      "userid" : "aaaa",
                                                      "groupid" : "bbbb",
                                                      "headpic" : "http://wx.qlogo.cn/mmopen/xxLzNxqMsxnlE4O0LjLaxTkiapbRU1HpVNPPvZPWb4MTicy1G1hJtEic0VGLbMFUrVA5ILoAnjQ2enNTSMYIe2hrQFkfRRfBccQ/132",
                                                      "frontcover" : "http://wx.qlogo.cn/mmopen/xxLzNxqMsxnlE4O0LjLaxTkiapbRU1HpVNPPvZPWb4MTicy1G1hJtEic0VGLbMFUrVA5ILoAnjQ2enNTSMYIe2hrQFkfRRfBccQ/0",
                                                      "location" : "深圳"
                                                  }
                                              }
                                              ]
                           }
                       }
                       );

//*/
NSString *const kTCLiveListNewDataAvailable = @"kTCLiveListNewDataAvailable";
NSString *const kTCLiveListSvrError = @"kTCLiveListSvrError";
NSString *const kTCLiveListUpdated = @"kTCLiveListUpdated";

@interface TCLiveListMgr()

@property NSMutableArray        *allVodsArray;
@property int                   totalCount;
@property int                   currentPage;
@property BOOL                  isLoading;
@property BOOL                  isVideoTypeChange;
@property AFHTTPSessionManager  *httpSession;
@property(nonatomic, copy) NSString*  userId;
@property(nonatomic) NSNumber*        expired;
@property(nonatomic) NSString*        token;

@end

@implementation TCLiveListMgr

- (instancetype)init {
    self = [super init];
    if (self) {
        _allVodsArray = [NSMutableArray new];
        _totalCount = 0;
        _isLoading = NO;
        _httpSession = [AFHTTPSessionManager manager];
#ifdef NDEBUG
        _httpSession.requestSerializer.timeoutInterval = 5.f;
#endif
        [_httpSession setRequestSerializer:[AFJSONRequestSerializer serializer]];
        [_httpSession setResponseSerializer:[AFJSONResponseSerializer serializer]];
        _httpSession.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"text/html", @"text/xml", @"text/plain", nil];
        
    }
    return self;
}

+ (instancetype)sharedMgr {
    static TCLiveListMgr *mgr;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (mgr == nil) {
            mgr = [TCLiveListMgr new];
        }
    });
    return mgr;
}

- (void)setUserId:(NSString*)userId expires:(NSNumber*)expires token:(NSString*)token
{
    self.userId = userId;
    self.expired = expires;
    self.token = token;
}

- (void)queryVideoList:(GetType)getType{
    _isLoading = YES;
    
    if (getType == GetType_Up) {
        _currentPage = 0;
        [self cleanAllVods];
    }
    
    [_httpSession.operationQueue cancelAllOperations];
    
    [self loadNextVods];
}

- (void)loadNextVods{
    _currentPage++;
    NSDictionary* params = @{@"timestamp":@([[NSDate date] timeIntervalSince1970] * 1000), @"index":@(_currentPage), @"count":@(20)};
    __weak typeof(self) weakSelf = self;
    
    [TCUtil asyncSendHttpRequest:@"get_ugc_list" token:@"" params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        if (resultCode == 200) {
            NSArray* vodInfoArray = resultDict[@"list"];
            NSMutableArray* pusherList = [NSMutableArray array];
            for (NSDictionary* roomInfo in vodInfoArray) {
                TCLiveInfo* liveInfo = [TCLiveInfo new];
                liveInfo.userinfo = [TCLiveUserInfo new];
                liveInfo.userid = roomInfo[@"userid"];
                liveInfo.title = roomInfo[@"title"];
                liveInfo.playurl = roomInfo[@"play_url"];
                liveInfo.hls_play_url = roomInfo[@"hls_play_url"];
                liveInfo.fileid = roomInfo[@"file_id"];
                liveInfo.reviewStatus = (ReviewStatus)[roomInfo[@"review_status"] intValue];
                NSDate *date = [TCUtil timeToDate:roomInfo[@"create_time"]];
                liveInfo.timestamp =  [date timeIntervalSince1970];
                liveInfo.userinfo.nickname = roomInfo[@"nickname"];
                liveInfo.userinfo.location = roomInfo[@"location"];
                liveInfo.userinfo.headpic = roomInfo[@"avatar"];
                liveInfo.userinfo.frontcover = roomInfo[@"frontcover"];
                [pusherList addObject:liveInfo];
            }
            
            @synchronized (self) {
                [_allVodsArray addObjectsFromArray:pusherList];
                _totalCount = (int)_allVodsArray.count;
            }
            _isLoading = NO;
            [self dumpLivesToArchive];
            [self postDataAvaliableNotify];
        }
        else {
            weakSelf.isLoading = NO;
            NSLog(@"finish loading");
            [[NSNotificationCenter defaultCenter] postNotificationName:kTCLiveListSvrError object:[NSError errorWithDomain:@"VodVideoList" code:resultCode userInfo:@{@"errCode":@(resultCode), @"description": message}]];
        }
    }];
}

- (NSArray *)readVods:(NSRange)range finish:(BOOL *)finish {
    NSArray *res = nil;
    
    @synchronized (self) {
        if (range.location < _allVodsArray.count) {
            range.length = MIN(range.length, _allVodsArray.count - range.location);
            res = [_allVodsArray subarrayWithRange:range];
        }
    }
    
    if (res == nil && _allVodsArray.count > 0) {
        *finish = YES;
    } else {
        *finish = NO;
    }
    return res;
}

- (TCLiveInfo*)readVod:(NSString*)userId fileId:(NSString*)fileId
{
    TCLiveInfo* info = nil;
    if (nil == userId)
        return nil;
    
    @synchronized (self) {
        for (TCLiveInfo* item in _allVodsArray)
        {
            if ([userId isEqualToString:item.userid] && [fileId isEqualToString:item.fileid])
            {
                info = item;
                break;
            }
        }
    }
    return info;
}

- (void)cleanAllVods {
    @synchronized (self) {
        [_allVodsArray removeAllObjects];
    }
}

- (void)postDataAvaliableNotify {
    [[NSNotificationCenter defaultCenter] postNotificationName:kTCLiveListNewDataAvailable object:nil];
}

- (void)postSvrErrorNotify:(int)error reason:(NSString *)msg {
    NSError *e = [NSError errorWithDomain:NSOSStatusErrorDomain code:error userInfo:@{NSLocalizedFailureReasonErrorKey:msg}];
    [[NSNotificationCenter defaultCenter] postNotificationName:kTCLiveListSvrError object:e];
}

#pragma mark - 持久化存储
- (void)loadVodsFromArchive {
    NSUserDefaults *currentDefaults = [NSUserDefaults standardUserDefaults];
    NSData *savedArray = [currentDefaults objectForKey:userDefaultsKey];
    if (savedArray != nil)
    {
        NSArray *oldArray = [NSKeyedUnarchiver unarchiveObjectWithData:savedArray];
        if (oldArray != nil) {
            @synchronized (self) {
                _allVodsArray = [[NSMutableArray alloc] initWithArray:oldArray];
                _totalCount = (int)_allVodsArray.count;
            }
        }
    }
}

- (void)dumpLivesToArchive {
    @synchronized (self) {
        if (_allVodsArray.count > 0) {
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:_allVodsArray] forKey:userDefaultsKey];
        }
    }
}

@end

