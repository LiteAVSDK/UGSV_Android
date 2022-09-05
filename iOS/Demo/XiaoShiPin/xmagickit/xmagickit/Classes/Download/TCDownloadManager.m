//
//  TCDownloadManager.m
//  BeautyDemo
//
//  Created by tao yue on 2022/3/25.
//  Copyright © 2021 tencent. All rights reserved.

#import "TCDownloadManager.h"
#import "TCDownloadModel.h"
#import <zlib.h>
#import "ZipArchive.h"

@implementation TCDownloadedFile

@end


@interface TCDownloadModel()

@end

@interface TCDownloadManager()<NSURLSessionDelegate>

//文件管理
@property (nonatomic, strong) NSFileManager *fileManager;  //文件管理

// Document/TCDownloadCache/
@property (nonatomic, strong) NSString *fileCacheDirectory;

// Document/Xmagic
@property (nonatomic, copy) NSString *downloadFileDirectory;

//  Document/TCDownloadCache/FinishedPlist.plist
@property (nonatomic, copy) NSString *finishedPlistFilePath;




@end

@implementation TCDownloadManager

@synthesize session = _session;

+(TCDownloadManager *)shareManager{
    static TCDownloadManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [TCDownloadManager new];
    });
    return manager;
}

- (instancetype)init{
    if (self = [super init]) {
        _downloadModelList = @[].mutableCopy;
        _finishedlist = @[].mutableCopy;
        _downloadModelDic = @{}.mutableCopy;
        [self loadFinishedfiles];
        _downloadModelList = [self loadDownloadList];
        
        NSURLSessionConfiguration *con = [NSURLSessionConfiguration defaultSessionConfiguration];
        _session = [NSURLSession sessionWithConfiguration:con delegate:self delegateQueue:nil];

        //查看是否有进行中的下载
        NSArray *tasks = [self sessionDownloadTasks];
        for (NSURLSessionDownloadTask *task in tasks) {
            if (task.state == NSURLSessionTaskStateRunning) {
                TCDownloadModel *model = _downloadModelDic[task.currentRequest.URL.absoluteString];
                model.downloadTask = task;
            }
        }
    }
    return self;
}

- (NSMutableArray *)loadDownloadList{
    NSMutableArray *array = @[].mutableCopy;
    NSError *error;
    NSArray *filelist = [self.fileManager contentsOfDirectoryAtPath:[self downloadFileDirectory] error:&error];
    
    if(!error)
    {
        NSLog(@"%@",[error description]);
    }
    for(NSString *file in filelist) {
        NSString *filetype = [file pathExtension];
        if([filetype isEqualToString:@"plist"]){
            NSString *path = [[self downloadFileDirectory] stringByAppendingPathComponent:file];
            NSDictionary *dic = [NSDictionary dictionaryWithContentsOfFile:path];
            TCDownloadModel *model = [TCDownloadModel new];
            model.url = dic[@"url"];
            model.startTime = dic[@"startTime"];
            model.progress.totalBytesExpectedToWrite = [dic[@"totalBytesExpectedToWrite"] integerValue];
            model.progress.totalBytesWritten = [dic[@"totalBytesWritten"] integerValue];
            model.state = [dic[@"state"] integerValue];
            model.resumeData = dic[@"resumedata"];
            [array addObject:model];
            [_downloadModelDic setObject:model forKey:dic[@"url"]];
            [_downloadModelList addObject:model];
        }
    }
    return array;
}

- (TCDownloadModel *)downLoadingModelForURLString:(NSString *)URLString
{
    return [self.downloadModelDic objectForKey:URLString];
}
- (BOOL)isInDownloadList:(NSString *)url{
    return [self.downloadModelDic objectForKey:url];
}

#pragma mark --get download model
- (TCDownloadModel *)downloadModelWithURL:(NSString *)url isInitTask:(BOOL)isInitTask{
    TCDownloadModel *model = [[TCDownloadModel alloc] initWithURL:url isInitTask:isInitTask] ;
    return model;
}

- (TCDownloadModel *)addDownloadModelWithURL:(NSString *)url{
    TCDownloadModel *model = [self downloadModelWithURL:url isInitTask:YES ];
    if (model) {
        [_downloadModelList addObject:model];
        [_downloadModelDic setObject:model forKey:url];
        [self saveDownloadInfo:model];
    }
    [model resume];
    return model;
}


#pragma mark -- NSURLSessionTaskDelegate
- (void)URLSessionDidFinishEventsForBackgroundURLSession:(NSURLSession *)session{
    if (self.completionHandler) {
        self.completionHandler();
    }
}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task
didCompleteWithError:(nullable NSError *)error{
    NSLog(@"%s", __func__);
    TCDownloadModel *model = [self modelWithUrl:task.currentRequest.URL.absoluteString];
    if (model == nil) {
        return;
    }
    if (error != nil) {
        model.error = YES;
        [self.downloadModelList removeObject:model];
        [self.downloadModelDic removeObjectForKey:model.url];
        NSString *path = [[self downloadFileDirectory ] stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.plist", model.fileName]];
        [_fileManager removeItemAtPath:path error:nil];
        return;
    }
    NSAssert(model != nil, @"model不能为nil");
    NSData *resumeData = [error.userInfo objectForKey:NSURLSessionDownloadTaskResumeData];
    if (resumeData) {//user 主动暂停了应用或者等待下载
        if (model.state == TCDownloadModelPauseState || model.state == TCDownloadModelWillStartState) {
            model.resumeData = resumeData;
            [self saveDownloadInfo:model];
        }else if(model.state == TCDownloadModelRunningState){//如果是用户主动kill了应用，重启应用也可以在这里获取到resumedata
            model.resumeData = resumeData;
            [model resume];
        }
    }else{//下载完成或下载失败
        [self.downloadModelList removeObject:model];
        [self.downloadModelDic removeObjectForKey:model.url];
        NSString *path = [[self downloadFileDirectory ] stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.plist", model.fileName]];
        [_fileManager removeItemAtPath:path error:nil];
        
        @synchronized(self){
            for (TCDownloadModel *model in self.downloadModelList) {
                if (model.state == TCDownloadModelWillStartState) {
                    [model resume];
                    break;
                }
            }
        }
        [model URLSession:session task:task didCompleteWithError:error];
    }
}


- (TCDownloadModel *)modelWithUrl:(NSString *)url{
    TCDownloadModel *model = [self.downloadModelDic objectForKey:url];
    return model;
}

- (TCDownloadModel *)modelWithTask:(NSURLSessionDownloadTask *)task{
    for (TCDownloadModel * model in self.downloadModelList) {
        if (model.downloadTask == task) {
            return model;
        }
    }
    return nil;
}

- (void)deleteFinishFile:(TCDownloadedFile *)model
{
    
    [_finishedlist removeObject:model];
    NSString *path = model.filePath;
    NSError *error;
    if ([self.fileManager fileExistsAtPath:path]) {
       BOOL s = [self.fileManager removeItemAtPath:path error:&error];
        NSAssert(s, @"删除失败");

        if (s) {
            NSLog(@"删除成功");
        }else{
            NSLog(@"删除失败");
        }
    }
    [self saveFinishedFile];
}

#pragma mark --
#pragma mark -- NSURLSessionDownloadDelegate

/* Sent when a download task that has completed a download.  The delegate should
 * copy or move the file at the given location to a new location as it will be
 * removed when the delegate message returns. URLSession:task:didCompleteWithError: will
 * still be called.
 */
- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
didFinishDownloadingToURL:(NSURL *)location{
    
    NSLog(@"%s", __func__);
    TCDownloadModel *model = [self modelWithTask:downloadTask];
    NSString *path = [[self fileCacheDirectory] stringByAppendingPathComponent:downloadTask.response.suggestedFilename];
    [self.fileManager moveItemAtURL:location toURL:[NSURL fileURLWithPath:path] error:nil];

    TCDownloadedFile * finish = [TCDownloadedFile new];
    finish.fileName = model.fileName;
    finish.fileSize = model.progress.totalFileSize;
    finish.filePath = path;
    if ([self isFinishedDownload:[downloadTask.response.URL absoluteString]] ||
        model.fileName == nil) {
        return;
    }
    
    //解压文件
    ZipArchive *zip = [[ZipArchive alloc] init];
    BOOL unZipReady = [zip UnzipOpenFile:path];
    if (unZipReady) {
        BOOL ret = [zip UnzipFileTo:[self downloadFileDirectory]  overWrite:YES];
        [zip CloseZipFile2];
        if (!ret) {
            NSLog(@"解压文件失败：%@",path);
            finish.failed = YES;
        }else{
            finish.failed = NO;
        }
    }else{
        finish.failed = YES;
        NSLog(@"文件打开失败，无法解压：%@",path);
    }
    [self.fileManager removeItemAtPath:finish.filePath error:nil];
    [self.finishedlist addObject:finish];
    [[TCDownloadManager shareManager] saveFinishedFile];
    [model URLSession:session downloadTask:downloadTask didFinishDownloadingToURL:location];
}

/* Sent periodicallt  o notify the delegate of download progress. */
- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
      didWriteData:(int64_t)bytesWritten
 totalBytesWritten:(int64_t)totalBytesWritten
totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite{
    NSLog(@"%s", __func__);

    TCDownloadModel *model = [self modelWithTask:downloadTask];
    [model URLSession:session downloadTask:downloadTask didWriteData:bytesWritten
    totalBytesWritten:totalBytesWritten totalBytesExpectedToWrite:totalBytesExpectedToWrite];
}

/* Sent when a download has been resumed. If a download failed with an
 * error, the -userInfo dictionary of the error will contain an
 * NSURLSessionDownloadTaskResumeData key, whose value is the resume
 * data.
 */

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
 didResumeAtOffset:(int64_t)fileOffset
expectedTotalBytes:(int64_t)expectedTotalBytes{
    TCDownloadModel *model = [self modelWithTask:downloadTask];
    [model URLSession:session downloadTask:downloadTask didResumeAtOffset:fileOffset expectedTotalBytes:expectedTotalBytes];
    NSLog(@"%s", __func__);
}

// 获取所有的后台下载session
- (NSArray *)sessionDownloadTasks
{
    __block NSArray *tasks = nil;
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);//使用信号量把异步变同步，是这个函数返回时tasks有值
    [self.session getTasksWithCompletionHandler:^(NSArray *dataTasks, NSArray *uploadTasks, NSArray *downloadTasks) {
        tasks = downloadTasks;
        if (tasks.count > 0) {
            NSURLSessionDownloadTask *task = tasks[0];
            NSLog(@"aa");
        }
        dispatch_semaphore_signal(semaphore);
    }];
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    return tasks;
}

- (void)pauseDownload:(TCDownloadModel *)model{
    [model pause];
    if (model.progressInfoBlock) {
        model.progressInfoBlock(model.progress);
    }
    for (TCDownloadModel *aModel  in _downloadModelList) {
        if (aModel.state == TCDownloadModelWillStartState) {
            [aModel resume];
            if (model.progressInfoBlock) {
                model.progressInfoBlock(model.progress);
            }
            break;
        }
    }
}

//TCDownloadModelPauseState或者TCDownloadModelWillStartState调用这个方法开启下载
- (void)resumeDownload:(TCDownloadModel *)model{
    [model resume];
    
    if (model.progressInfoBlock) {
        model.progressInfoBlock(model.progress);
    }
}

#pragma mark -- handle finish file

- (void)loadFinishedfiles
{
    if ([self.fileManager fileExistsAtPath:self.finishedPlistFilePath]) {
        [_finishedlist removeAllObjects];
        NSMutableArray *finishArr = [[NSMutableArray alloc] initWithContentsOfFile:[self finishedPlistFilePath]];
        for (NSDictionary *dic in finishArr) {
            TCDownloadedFile *file = [[TCDownloadedFile alloc]init];
            file.fileName = [dic objectForKey:@"fileName"];
            file.fileType = [file.fileName pathExtension];
            file.fileSize = [dic objectForKey:@"fileSize"];
            file.failed = dic[@"failed"] == nil ? false : [dic[@"failed"] boolValue];
            file.filePath = [self.fileCacheDirectory stringByAppendingPathComponent:file.fileName];
            [_finishedlist addObject:file];
        }
    }
}

- (void)saveFinishedFile
{
    if (_finishedlist == nil) { return; }
    NSMutableArray *finishedinfo = [[NSMutableArray alloc] init];
    for (TCDownloadedFile *fileinfo in _finishedlist) {
        if (!fileinfo.failed) {
            NSDictionary *filedic = [NSDictionary dictionaryWithObjectsAndKeys: fileinfo.fileName,@"fileName",
                                     fileinfo.fileSize,@"fileSize",[NSNumber numberWithBool:fileinfo.failed],@"failed",
                                     nil];
            [finishedinfo addObject:filedic];
        }
    }
    if (![finishedinfo writeToFile:self.finishedPlistFilePath atomically:YES]) {
        NSLog(@"write plist fail");
    }
}



- (void)deleteDownload:(TCDownloadModel *)model{
    if (model.state == TCDownloadModelRunningState) {
        [model cancel];
        [self.downloadModelList removeObject:model];
        [self.downloadModelDic removeObjectForKey:model.url];
        NSString *path = [[self downloadFileDirectory ] stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.plist", model.fileName]];
        [_fileManager removeItemAtPath:path error:nil];
        
        @synchronized(self){
            for (TCDownloadModel *model in self.downloadModelList) {
                if (model.state == TCDownloadModelWillStartState) {
                    [model resume];
                    break;
                }
            }
        }

    }else{
        [self.downloadModelDic removeObjectForKey:model.url];
        [self.downloadModelList removeObject:model];

        NSError *error;
        BOOL s1 = [self.fileManager removeItemAtPath: [[self downloadFileDirectory ]
        stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.plist", model.fileName]]
        error:&error];
        if (s1) {
            NSLog(@"s");
            if(model.completeBlock){
                dispatch_async(dispatch_get_main_queue(), ^{
                    model.completeBlock(nil);
                });
            }
        }else{
            NSLog(@"f");
        }
    }
    if (model.url == nil) {
        NSLog(@"aaaaaaaaaaa%@", model);
    }
    
}

#pragma mark -- File Manager

- (NSString *)finishedPlistFilePath{
    if (!_finishedPlistFilePath) {
        _finishedPlistFilePath = [[self fileCacheDirectory] stringByAppendingPathComponent:@"FinishedPlist.plist"];
    }
    return _finishedPlistFilePath;
}

- (NSFileManager *)fileManager
{
    if (!_fileManager) {
        _fileManager = [[NSFileManager alloc]init];
    }
    return _fileManager;
}


- (void)createDirectory:(NSString *)directory
{
    if (![self.fileManager fileExistsAtPath:directory]) {
        [self.fileManager createDirectoryAtPath:directory withIntermediateDirectories:YES attributes:nil error:NULL];
    }
}



- (NSString *)fileCacheDirectory
{
    if (!_fileCacheDirectory) {
        _fileCacheDirectory = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject]
        stringByAppendingPathComponent:@"TCDownloadCache"];
        [self createDirectory:_fileCacheDirectory];
    }
    return _fileCacheDirectory;
}
- (NSString *)downloadFileDirectory{
    //DownloadFilePlsits
    if (!_downloadFileDirectory) {
        _downloadFileDirectory = [self getResPath];
        [self createDirectory:_downloadFileDirectory];
    }
    return _downloadFileDirectory;
}

-(NSString *)getResPath{
    return  [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:@"Xmagic"];
}

- (void)saveDownloadInfo:(TCDownloadModel *)model{
    NSMutableDictionary *dic = @{}.mutableCopy;
    if (model.url) {
        [dic setObject:model.url forKey:@"url"];
    }
    
    if (model.startTime) {
        [dic setObject:model.startTime forKey:@"startTime"];
    }
    
    if (model.progress.totalFileSize) {
        [dic setObject:@(model.progress.totalBytesExpectedToWrite) forKey:@"totalBytesExpectedToWrite"];
    }
    
    if (model.progress.writtenFileSize) {
        [dic setObject: @(model.progress.totalBytesWritten) forKey:@"totalBytesWritten"];
    }
    
    if (model.resumeData) {
        [dic setObject:model.resumeData forKey:@"resumedata"];
    }
    
    [dic setObject:@(model.state) forKey:@"state"];
    
    NSString *path = [[self downloadFileDirectory ] stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.plist", model.fileName]];
    if ([self.fileManager fileExistsAtPath:path]) {
        [_fileManager removeItemAtPath:path error:nil];
    }
    BOOL s = [dic writeToFile:path atomically:YES];
    NSAssert(s, @"写入失败");
}

- (BOOL)fileExist:(NSString *)fileName{
    NSString *path = [self.fileCacheDirectory stringByAppendingPathComponent:fileName];
    return [self.fileManager fileExistsAtPath:path];
}

- (BOOL)isFinishedDownload:(NSString *)url{
    NSString *fileName = url.lastPathComponent;
    fileName = [fileName substringToIndex:fileName.length - 4];
    NSString *path = [self.downloadFileDirectory stringByAppendingPathComponent:fileName];
    return [self.fileManager fileExistsAtPath:path];
}

@end
