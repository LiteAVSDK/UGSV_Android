//
//  TCDownloadModel.m
//  BeautyDemo
//
//  Created by tao yue on 2022/3/25.
//  Copyright © 2021 tencent. All rights reserved.

#import "TCDownloadModel.h"
#import "TCDownloadManager.h"

@interface TCDownloadProgress ()
// 这次写入的数量
@property (nonatomic, assign) int64_t bytesWritten;

// 下载进度
@property (nonatomic, assign) float progress;
// 下载速度
@property (nonatomic, assign) float speed;
// 下载剩余时间
@property (nonatomic, assign) int remainingTime;
// 下载速度
@property (nonatomic, copy) NSString * speedString;

@end

@implementation TCDownloadProgress
- (float)progress{
    if (_totalBytesWritten == 0) {
        return 0;
    }
    return (float)_totalBytesWritten/_totalBytesExpectedToWrite;
}

- (NSString *)writtenFileSize{
    NSString *writtenFileSize = [NSString stringWithFormat:@"%.2f %@",
                                 [self calculateFileSizeInUnit:(unsigned long long)self.totalBytesWritten],
                                 [self calculateUnit:(unsigned long long)self.totalBytesWritten]];
    
    return writtenFileSize;

}

- (NSString *)totalFileSize{
    NSString *totalFileSize = [NSString stringWithFormat:@"%.2f %@",
                                     [self calculateFileSizeInUnit:(unsigned long long)self.totalBytesExpectedToWrite],
                                     [self calculateUnit:(unsigned long long)self.totalBytesExpectedToWrite]];
    return totalFileSize;
}

- (NSString *)speedString{
    NSString *speedS = [NSString stringWithFormat:@"%.2f %@",[self calculateFileSizeInUnit:(unsigned long long)self.speed],
                        [self calculateUnit:(unsigned long long)self.speed]];
    return speedS;
}

- (float)calculateFileSizeInUnit:(unsigned long long)contentLength
{
    if(contentLength >= pow(1024, 3))
        return (float) (contentLength / (float)pow(1024, 3));
    else if(contentLength >= pow(1024, 2))
        return (float) (contentLength / (float)pow(1024, 2));
    else if(contentLength >= 1024)
        return (float) (contentLength / (float)1024);
    else
        return (float) (contentLength);
}

- (NSString *)calculateUnit:(unsigned long long)contentLength
{
    if(contentLength >= pow(1024, 3))
        return @"GB";
    else if(contentLength >= pow(1024, 2))
        return @"MB";
    else if(contentLength >= 1024)
        return @"KB";
    else
        return @"Bytes";
}
@end

@interface TCDownloadModel()<NSURLSessionDelegate>
// date
@property (nonatomic, strong) NSDate * date;
// bytes
@property (nonatomic, assign) int64_t bytes;

@end

@implementation TCDownloadModel

- (instancetype)initWithURL:(NSString *)url isInitTask:(BOOL)isInitTask{
    if (!url) {
        return nil;
    }
    if ([[TCDownloadManager shareManager] isFinishedDownload:url]) {
        return nil;
    }
    self = [super init];
    if (self) {
        if (isInitTask) {
            NSURLSessionDownloadTask *task = [[TCDownloadManager shareManager].session downloadTaskWithURL:[NSURL URLWithString:url]];
            self.downloadTask = task;
        }
        self.url = url;
        self.startTime = [self dateToString:[NSDate date]];
        _progress = [TCDownloadProgress new];
    }
    return self;
}

- (NSString *)fileName{
    NSString *name = [self.url lastPathComponent];
    return [name substringToIndex:name.length - 4];
}

- (instancetype)init{
    if (self = [super init]) {
        _progress = [TCDownloadProgress new];
    }
    return self;
}


- (void)saveInfo{
    [[TCDownloadManager shareManager] saveDownloadInfo:self];
}

#pragma mark -- NSURLSessionTaskDelegate
- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task
didCompleteWithError:(nullable NSError *)error{
    if(self.completeBlock){
        dispatch_async(dispatch_get_main_queue(), ^{
            self.completeBlock(error);
        });
    }
}

#pragma mark -- NSURLSessionDownloadDelegate

/* Sent when a download task that has completed a download.  The delegate should
 * copy or move the file at the given location to a new location as it will be
 * removed when the delegate message returns. URLSession:task:didCompleteWithError: will
 * still be called.
 */
- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
didFinishDownloadingToURL:(NSURL *)location{
    if(self.unZipBlock){
        dispatch_async(dispatch_get_main_queue(), ^{
            self.unZipBlock();
        });
    }
    NSLog(@"%s", __func__);
   
}


/* Sent periodically to notify the delegate of download progress. */
- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
      didWriteData:(int64_t)bytesWritten
 totalBytesWritten:(int64_t)totalBytesWritten
totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite{
    NSLog(@"%s", __func__);
    _progress.bytesWritten = bytesWritten;
    _progress.totalBytesWritten = totalBytesWritten;
    _progress.totalBytesExpectedToWrite = totalBytesExpectedToWrite;
    
    _progress.progress = (float)totalBytesWritten/totalBytesExpectedToWrite;
    
    NSDate *currentDate = [NSDate date];
    double time = [currentDate timeIntervalSinceDate:self.date];
    self.bytes = self.bytes + bytesWritten;
    if (time >= 1) {
        float speed  = _bytes/time;
        
        int64_t remainingContentLength = totalBytesExpectedToWrite - totalBytesWritten;
        int remainingTime = ceilf(remainingContentLength / speed);
        
        _progress.speed = speed;
        _progress.remainingTime = remainingTime;
        
        _date = currentDate;
        _bytes = 0;
    }
    
    if (self.progressInfoBlock) {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.progressInfoBlock(self.progress);
        });
    }
}

/* Sent when a download has been resumed. If a download failed with an
 * error, the -userInfo dictionary of the error will contain an
 * NSURLSessionDownloadTaskResumeData key, whose value is the resume
 * data.
 */

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
 didResumeAtOffset:(int64_t)fileOffset
expectedTotalBytes:(int64_t)expectedTotalBytes{
    NSLog(@"%s", __func__);
}

- (void)pause{
    if (self.state == TCDownloadModelRunningState) {
        self.state = TCDownloadModelPauseState;
        [self.downloadTask cancelByProducingResumeData:^(NSData * _Nullable resumeData) {

        }];
    }
}
- (void)wait{
    if (self.state == TCDownloadModelRunningState) {
        self.state = TCDownloadModelWillStartState;
        [self.downloadTask cancelByProducingResumeData:^(NSData * _Nullable resumeData) {
            
        }];
    }
}

- (void)resume{
    
    if (self.state == TCDownloadModelPauseState) {
        if (self.resumeData) {
            [self resumeWithResumeData:self.resumeData];
        }
    }
    else if (self.state == TCDownloadModelWillStartState) {
        if (self.resumeData) {
            [self resumeWithResumeData:self.resumeData];
        }else{
            [self recoverResume];
        }
        
    }
   else if (self.state == TCDownloadModelRunningState){
       if (self.downloadTask) {
           return;
       }else{
           if (self.resumeData) {
               [self resumeWithResumeData:self.resumeData];
           }else{
               [self recoverResume];
           }
       }
      }
}


- (void)resumeWithResumeData:(NSData *)resumeData{
    if (self.resumeData) {
        self.downloadTask = [[TCDownloadManager shareManager].session downloadTaskWithResumeData:self.resumeData];
        [self.downloadTask resume];
        self.date = [NSDate date];
        self.state = TCDownloadModelRunningState;
        [self saveInfo];
    }
}

- (void)recoverResume{
    if (!self.downloadTask) {
        self.downloadTask = [[TCDownloadManager shareManager].session downloadTaskWithURL:[NSURL URLWithString:self.url]];
        
    }
    [self.downloadTask resume];
    self.date = [NSDate date];
    self.state = TCDownloadModelRunningState;
    [self saveInfo];
}

- (void)cancel{
    [self.downloadTask cancel];
}


- (NSString *)dateToString:(NSDate*)date {
    NSDateFormatter *df = [[NSDateFormatter alloc] init];
    [df setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    NSString *datestr = [df stringFromDate:date];
    return datestr;
}


@end
