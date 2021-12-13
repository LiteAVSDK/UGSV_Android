// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitBGMHelper.h"
#import "pthread.h"

#define BGM_GROUP                            @"group.com.tencent.bgm.list"

// percent < 0 失败
// url 为 nil 是进度
typedef void(^DownLoadCallback)(float percent, NSString* url);

@interface TCBGMDownloadTask : NSObject
@property (strong, nonatomic) NSString *destPath;
@property (copy, nonatomic) DownLoadCallback callback;
@end
@implementation TCBGMDownloadTask
@end

@interface UGCKitBGMHelper() <NSURLSessionDownloadDelegate> {
    NSDictionary* _configs;
    NSUserDefaults* _userDefaults;
    NSString* _userIDKey;
//    NSMutableDictionary* _tasks;
    NSURLSessionDownloadTask* _currentTask;
    NSURLSession *_urlSession;
    NSOperationQueue *_sessionDelegateQueue;
    TCBGMElement* _currentEle;
    
    NSString* _bgmPath;
    NSMutableDictionary<NSURLSessionTask *, TCBGMDownloadTask*> *_taskDictionary;
}
@property(nonatomic, assign)pthread_mutex_t lock;
@property(nonatomic, assign)pthread_cond_t cond;
@property(nonatomic, strong)dispatch_queue_t queue;
@property(nonatomic)NSMutableDictionary* bgmDict;
@property(nonatomic)NSMutableDictionary* bgmList;//只用来存储路径
@property(nonatomic,weak) id <TCBGMHelperListener>delegate;
@end


@implementation UGCKitBGMHelper

+ (instancetype)sharedInstance {
    static UGCKitBGMHelper* _sharedInstance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
         _sharedInstance = [UGCKitBGMHelper new];
    });
    return _sharedInstance;
}

-(void) setDelegate:(nonnull id<TCBGMHelperListener>) delegate{
    _delegate = delegate;
}

-(id) init{
    if(self = [super init]){
        _taskDictionary = [[NSMutableDictionary alloc] init];
        _sessionDelegateQueue = [[NSOperationQueue alloc] init];
        _sessionDelegateQueue.underlyingQueue = dispatch_get_global_queue(QOS_CLASS_DEFAULT, 0);
        _urlSession = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration ephemeralSessionConfiguration]
                                                    delegate:self
                                               delegateQueue:_sessionDelegateQueue];
//        if(![[TCLoginModel sharedInstance] isLogin]){
//            self = nil;
//            return nil;
//        }
        NSFileManager *fileManager = [NSFileManager defaultManager];
        _bgmPath = [NSHomeDirectory() stringByAppendingPathComponent:@"Documents/bgm"];
        if(![fileManager fileExistsAtPath:_bgmPath]){
            if(![fileManager createDirectoryAtPath:_bgmPath withIntermediateDirectories:YES attributes:nil error:nil]){
                BGMLog(@"创建BGM目录失败");
                return nil;
            }
        }
        pthread_mutex_init(&_lock, NULL);
        pthread_cond_init(&_cond, NULL);
        _userDefaults = [[NSUserDefaults alloc] initWithSuiteName:BGM_GROUP];
        if (_userDefaults == nil) {
            _userDefaults = [NSUserDefaults standardUserDefaults];
        }
//        _tasks = [[NSMutableDictionary alloc] init];
        _userIDKey = @"_bgm";
        _queue = dispatch_queue_create("com.tencent.txcloud.videoedit.bgm.download", DISPATCH_QUEUE_SERIAL);
        dispatch_async(_queue, ^{[self loadLocalData];});
    }
    return self;
}

- (void)dealloc {
    pthread_mutex_destroy(&_lock);
    pthread_cond_destroy(&_cond);
}

- (void)_bgmTask:(NSString *)url {
    NSString* localListPath = url;
    __weak __typeof(self) weak = self;
    if([url hasPrefix:@"http"]){
        localListPath = [_bgmPath stringByAppendingPathComponent:@"bgm_list.json"];
        __block BOOL ret = false;
        pthread_mutex_lock(&_lock);
        [self downloadFile:url dstUrl:localListPath callback:^(float percent, NSString* path){
            __strong UGCKitBGMHelper* strong = weak;
            if(strong){
                if(percent < 0){
                    pthread_cond_signal(&(strong->_cond));
                }
                else{
                    if(path != nil){
                        pthread_mutex_lock(&(strong->_lock));
                        ret = true;
                        pthread_cond_signal(&(strong->_cond));
                        pthread_mutex_unlock(&(strong->_lock));
                    }
                }
            }
        }];
        pthread_cond_wait(&_cond, &_lock);
        pthread_mutex_unlock(&_lock);
    }
    NSData *data = [[NSFileManager defaultManager] contentsAtPath:localListPath];
    _configs = data ? [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:nil] : nil;
    if(_configs == nil){
        [_delegate onBGMListLoad:nil];
    }
    else{
        NSArray* nameList = [_configs valueForKeyPath:@"bgm.list.name"];
        if([nameList count]){
            NSArray* urlList = [_configs valueForKeyPath:@"bgm.list.url"];
            for (int i = 0; i < [nameList count]; i++) {
                TCBGMElement* ele = [_bgmDict objectForKey:[urlList objectAtIndex:i]];
                if(ele != nil){

                }
                else{
                    ele = [TCBGMElement new];
                    ele.netUrl = [urlList objectAtIndex:i];
                    ele.name = [nameList objectAtIndex:i];
                    [self saveBGMStat:ele];
                }
            }
        }
    }
    [_delegate onBGMListLoad:_bgmDict];

}

-(void)initBGMListWithJsonFile:(NSString* _Nonnull)url{
    if(url == nil)return;
    __weak UGCKitBGMHelper* weak = self;
    void (^task)(void) = ^{
        [weak _bgmTask:url];
    };
    dispatch_async(_queue, task);
    return;
}

-(void) loadLocalData{
    _bgmDict = [NSMutableDictionary new];
    _bgmList = [[_userDefaults objectForKey:[_userIDKey stringByAppendingString:@".tc.bgm.list"]] mutableCopy];
    if(_bgmList == nil){
        _bgmList = [NSMutableDictionary new];
    }
    for (id it in _bgmList) {
        TCBGMElement* ele = [NSKeyedUnarchiver unarchiveObjectWithData:[_userDefaults objectForKey:[_userIDKey stringByAppendingString:it]]];
        if(ele) {
            [_bgmDict setObject:ele forKey:[ele netUrl]];
        }
    }
}

-(void) saveBGMStat:(TCBGMElement*) ele{
    [_bgmDict setObject:ele forKey:ele.netUrl];
    [_bgmList setObject:[ele netUrl] forKey:[ele netUrl]];
    NSData *udObject = [NSKeyedArchiver archivedDataWithRootObject:ele];
    [_userDefaults setObject:udObject forKey:[_userIDKey stringByAppendingString:[ele netUrl]]];
    [_userDefaults setObject:_bgmList forKey:[_userIDKey stringByAppendingString:@".tc.bgm.list"]];
}

- (void)_downloadBGMAction:(TCBGMElement*)current {
    const BOOL needOverride = YES;
    __strong UGCKitBGMHelper* strong = self;
    if(strong != nil){
        if([[_currentEle netUrl] isEqualToString:[current netUrl]]){
            if([_currentTask state] == NSURLSessionTaskStateRunning){
                BGMLog(@"%@",  @"暂停：%@", [current name]);
                [_currentTask suspend];
                return;
            }
            else if([_currentTask state] == NSURLSessionTaskStateSuspended){
                BGMLog(@"恢复：%@", [current name]);
                [_currentTask resume];
                return;
            }
        }
        else{
            if(_currentTask){
                if([_currentTask state] != NSURLSessionTaskStateCompleted){
                    [_currentTask cancel];
                    [strong.delegate onBGMDownloading:_currentEle percent:0];
                }
                _currentTask = nil;
            }
        }
        NSString* localListPath = nil;
        NSString* url = [current netUrl];

        __block NSString* justName = [current name];
        if (0 == justName.pathExtension.length) {
            NSString *bgmExtension = url.pathExtension.length ? url.pathExtension : @"mp3";
            justName = [justName stringByAppendingPathExtension:bgmExtension];
        }
        if (needOverride) {
            localListPath = [_bgmPath stringByAppendingPathComponent:justName];
        } else {
            justName = [NSString stringWithFormat:@"%@1.%@", [justName stringByDeletingPathExtension], [[current name] pathExtension]];
            localListPath = [_bgmPath stringByAppendingPathComponent:justName];
        }
        __weak __typeof(self) weak = self;
        NSURLSessionDownloadTask* task = [self downloadFile:url dstUrl:localListPath callback:^(float percent, NSString* path){
            __strong UGCKitBGMHelper* strong = weak;
            if(strong){
                dispatch_queue_t queue = strong->_queue;
                if(percent < 0){
                    dispatch_async(queue, ^{
                        [strong.delegate onBGMDownloadDone:current];
                    });
                }
                else{
                    TCBGMElement* ele = [strong->_bgmDict objectForKey:[current netUrl]];
                    if(path != nil){
                        ele.localUrl = [NSString stringWithFormat:@"Documents/bgm/%@", justName];
                        ele.isValid = [NSNumber numberWithBool:true];
                        dispatch_async(queue, ^{
                            [strong.delegate onBGMDownloadDone:ele];
                        });
                        [strong saveBGMStat:ele];
                    }else{
                        dispatch_async(queue, ^{
                            [weak.delegate onBGMDownloading:ele percent:percent];
                        });
                    }
                }
            }
        }];
        _currentTask = task;
        _currentEle = current;
    }
}

#pragma mark NSURLSessionDownloadDelegate
- (void)URLSession:(NSURLSession *)session
              task:(NSURLSessionTask *)urlTask
didCompleteWithError:(NSError *)error {
    TCBGMDownloadTask *task = nil;
    @synchronized (_taskDictionary) {
        task = _taskDictionary[urlTask];
    }

    if (error) {
        if (task.callback) {
            task.callback(-1, nil);
        }
    }
    @synchronized (_taskDictionary) {
        [_taskDictionary removeObjectForKey:urlTask];
    }
}

- (void)URLSession:(NSURLSession *)session
      downloadTask:(nonnull NSURLSessionDownloadTask *)downloadTask
      didWriteData:(int64_t)bytesWritten
 totalBytesWritten:(int64_t)totalBytesWritten
totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite {
    TCBGMDownloadTask *task = nil;
    @synchronized (_taskDictionary) {
        task = _taskDictionary[downloadTask];
    }
    if (task.callback) {
        task.callback(totalBytesWritten / (float)totalBytesExpectedToWrite, nil);
    }
}

- (void)URLSession:(NSURLSession *)session
      downloadTask:(NSURLSessionDownloadTask *)downloadTask
didFinishDownloadingToURL:(NSURL *)location {
    TCBGMDownloadTask *task = nil;
    @synchronized (_taskDictionary) {
        task = _taskDictionary[downloadTask];
    }
    NSError *fsError = nil;
    NSFileManager *manager = [NSFileManager defaultManager];
    if ([manager fileExistsAtPath:task.destPath]) {
        [manager removeItemAtPath:task.destPath error:nil];
    }
    [[NSFileManager defaultManager] moveItemAtURL:location
                                            toURL:[NSURL fileURLWithPath: task.destPath]
                                            error:&fsError];
    if (task.callback) {
        if (fsError) {
            NSLog(@"Error: %@", fsError);
            task.callback(-1, nil);
        } else {
            task.callback(0, task.destPath);
        }
    }
}

-(void) downloadBGM:(TCBGMElement*) current{
    __weak UGCKitBGMHelper* weak = self;
    dispatch_async(_queue, ^(){
        [weak _downloadBGMAction:current];
    });
}

//-(void) pauseAllTasks{
//    __weak TCBGMHelper* weak = self;
//    dispatch_async(_queue, ^(){
//        __strong TCBGMHelper* strong = weak;
//        for (id item in strong->_tasks) {
//            if([item state] == NSURLSessionTaskStateRunning)[item suspend];
//        }
//    });
//}
//
//-(void) resumeAllTasks{
//    __weak TCBGMHelper* weak = self;
//    dispatch_async(_queue, ^(){
//        __strong TCBGMHelper* strong = weak;
//        for (id item in strong->_tasks) {
//            if([item state] == NSURLSessionTaskStateSuspended)[item resume];
//        }
//    });
//}

/**
 下载函数回调
 
 @param callback 下载进度 < 0 出错并终止
 @param srcUrl 最终文件地址 nil != url则下载完成
 */
- (NSURLSessionDownloadTask*) downloadFile:(NSString*)srcUrl dstUrl:(NSString*)dstUrl callback:(DownLoadCallback)callback{
    TCBGMDownloadTask *task = [[TCBGMDownloadTask alloc] init];
    task.destPath = dstUrl;
    task.callback = callback;

    NSURL *url = [NSURL URLWithString:srcUrl];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:300.f];
    [request addValue:@"" forHTTPHeaderField:@"Accept-Encoding"];
    NSURLSessionDownloadTask *urlSessionTask =  [_urlSession downloadTaskWithRequest:request];
    @synchronized (_taskDictionary) {
        _taskDictionary[urlSessionTask] = task;
    }

    [urlSessionTask resume];
    return urlSessionTask;
}
@end


@implementation TCBGMElement
- (id) initWithCoder: (NSCoder *)coder
{
    if (self = [super init])
    {
        self.name = [coder decodeObjectForKey:@"name"];
        self.netUrl = [coder decodeObjectForKey:@"netUrl"];
        self.localUrl = [coder decodeObjectForKey:@"localUrl"];
        self.author = [coder decodeObjectForKey:@"author"];
        self.title = [coder decodeObjectForKey:@"title"];
        self.isValid = [coder decodeObjectForKey:@"isValid"];
        self.duration = [coder decodeObjectForKey:@"duration"];
    }
    return self;
}

- (void) encodeWithCoder: (NSCoder *)coder
{
    [coder encodeObject:_name forKey:@"name"];
    [coder encodeObject:_netUrl forKey:@"netUrl"];
    [coder encodeObject:_localUrl forKey:@"localUrl"];
    [coder encodeObject:_author forKey:@"author"];
    [coder encodeObject:_title forKey:@"title"];
    [coder encodeObject:_isValid forKey:@"isValid"];
    [coder encodeObject:_duration forKey:@"duration"];
}
@end
