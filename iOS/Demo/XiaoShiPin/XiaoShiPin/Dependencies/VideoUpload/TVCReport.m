//
//  TVCReport.m
//  TXMUploader
//
//  Created by carolsuo on 2018/3/28.
//  Copyright © 2018年 lynxzhang. All rights reserved.
//

#import "TVCReport.h"
#import "TVCUtils.h"

#define MAXCACHES 100

@implementation TVCReport
static TVCReport *_shareInstance = nil;

+ (instancetype)shareInstance {
    static dispatch_once_t predicate;

    dispatch_once(&predicate, ^{
      _shareInstance = [[TVCReport alloc] init];
    });
    return _shareInstance;
}

- (instancetype)init {
    if (self = [super init]) {
        _reportCaches = [NSMutableArray new];
        _timer = nil;
    }
    return self;
}

- (void)addReportInfo:(TVCReportInfo *)info {
    if (_timer == nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
          self.timer = [NSTimer scheduledTimerWithTimeInterval:10
                                                        target:self
                                                      selector:@selector(reportAll)
                                                      userInfo:nil
                                                       repeats:YES];
        });
    }
    TVCReportInfo *copy = [[TVCReportInfo alloc] init];
    copy.reqType = info.reqType;
    copy.errCode = info.errCode;
    copy.vodErrCode = info.vodErrCode;
    copy.cosErrCode = info.cosErrCode;
    copy.errMsg = info.errMsg;
    copy.reqTime = info.reqTime;
    copy.reqTimeCost = info.reqTimeCost;
    copy.fileSize = info.fileSize;
    copy.fileType = info.fileType;
    copy.fileName = info.fileName;
    copy.fileId = info.fileId;
    copy.appId = info.appId;
    copy.reqServerIp = info.reqServerIp;
    copy.reportId = info.reportId;
    copy.reqKey = info.reqKey;
    copy.vodSessionKey = info.vodSessionKey;
    copy.retryCount = info.retryCount;
    copy.reporting = info.reporting;
    copy.requestId = info.requestId;
    copy.useHttpDNS = info.useHttpDNS;
    copy.cosRegion = info.cosRegion;
    copy.useCosAcc = info.useCosAcc;
    copy.tcpConnTimeCost = info.tcpConnTimeCost;
    copy.recvRespTimeCost = info.recvRespTimeCost;

    @synchronized(self.reportCaches) {
        if (self.reportCaches.count > MAXCACHES) {
            [self.reportCaches removeObjectAtIndex:0];
        }
        [self.reportCaches addObject:copy];
    }
    [self reportAll];
}

- (void)reportAll {
    if ([TVCUtils tvc_getNetWorkType] == 0) {
        return;
    }
    @synchronized(self.reportCaches) {
        NSMutableArray *delList = [NSMutableArray array];
        for (TVCReportInfo *obj in self.reportCaches) {
            if (obj.retryCount < 4) {
                if (obj.reporting == NO) {
                    [self report:obj];
                }
            } else {
                [delList addObject:obj];
            }
        }
        [self.reportCaches removeObjectsInArray:delList];
    }
}

- (void)report:(TVCReportInfo *)info {
    NSMutableDictionary *dictParam = [[NSMutableDictionary alloc] init];
    [dictParam setValue:TVCVersion forKey:@"version"];
    [dictParam setValue:[NSNumber numberWithInt:info.reqType] forKey:@"reqType"];
    [dictParam setValue:[NSNumber numberWithInt:info.errCode] forKey:@"errCode"];
    [dictParam setValue:[NSNumber numberWithInt:info.vodErrCode] forKey:@"vodErrCode"];
    [dictParam setValue:info.cosErrCode forKey:@"cosErrCode"];
    [dictParam setValue:info.errMsg forKey:@"errMsg"];
    [dictParam setValue:[NSNumber numberWithLongLong:info.reqTimeCost] forKey:@"reqTimeCost"];
    [dictParam setValue:[NSNumber numberWithLongLong:info.reqTime] forKey:@"reqTime"];
    [dictParam setValue:info.reqServerIp forKey:@"reqServerIp"];
    [dictParam setValue:[NSNumber numberWithInt:1000]
                 forKey:@"platform"];  // 1000 - iOS, 2000 - Android
    [dictParam setValue:[TVCUtils tvc_deviceModelName] forKey:@"device"];
    [dictParam setValue:[[UIDevice currentDevice] systemVersion] forKey:@"osType"];
    [dictParam setValue:[NSNumber numberWithInt:[TVCUtils tvc_getNetWorkType]] forKey:@"netType"];
    [dictParam setValue:info.reportId forKey:@"reportId"];
    [dictParam setValue:[TVCUtils tvc_getDevUUID] forKey:@"uuid"];
    [dictParam setValue:info.reqKey forKey:@"reqKey"];
    [dictParam setValue:[NSNumber numberWithLongLong:info.appId] forKey:@"appId"];
    [dictParam setValue:[NSNumber numberWithLongLong:info.fileSize] forKey:@"fileSize"];
    [dictParam setValue:info.fileType forKey:@"fileType"];
    [dictParam setValue:info.fileName forKey:@"fileName"];
    [dictParam setValue:info.vodSessionKey forKey:@"vodSessionKey"];
    [dictParam setValue:info.fileId forKey:@"fileId"];

    [dictParam setValue:info.requestId forKey:@"requestId"];
    [dictParam setValue:[NSNumber numberWithInt:info.useHttpDNS] forKey:@"useHttpDNS"];
    [dictParam setValue:info.cosRegion forKey:@"cosRegion"];
    [dictParam setValue:[NSNumber numberWithInt:info.useCosAcc] forKey:@"useCosAcc"];
    [dictParam setValue:[NSNumber numberWithLongLong:info.tcpConnTimeCost]
                 forKey:@"tcpConnTimeCost"];
    [dictParam setValue:[NSNumber numberWithLongLong:info.recvRespTimeCost]
                 forKey:@"recvRespTimeCost"];
    [dictParam setValue:[TVCUtils tvc_getPackageName] forKey:@"packageName"];
    [dictParam setValue:[TVCUtils tvc_getAppName] forKey:@"appName"];

    NSLog(@"TVCReport report: info = %@", dictParam);

    NSError *error = nil;
    NSData *bodyData = [NSJSONSerialization dataWithJSONObject:dictParam options:0 error:&error];
    if (error) {
        return;
    }

    // set url
    NSString *baseUrl = @"https://vodreport.qcloud.com/ugcupload_new";

    // create request
    NSMutableURLRequest *request =
        [NSMutableURLRequest requestWithURL:[NSURL URLWithString:baseUrl]];
    [request setValue:[NSString stringWithFormat:@"%ld", (long)[bodyData length]]
        forHTTPHeaderField:@"Content-Length"];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:@"gzip" forHTTPHeaderField:@"Accept-Encoding"];
    [request setHTTPBody:bodyData];

    ++info.retryCount;
    info.reporting = YES;

    __weak TVCReport *ws = self;

    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionTask *initTask = [session
        dataTaskWithRequest:request
          completionHandler:^(NSData *_Nullable initData, NSURLResponse *_Nullable response,
                              NSError *_Nullable error) {
            NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
            if (error == nil && httpResponse.statusCode == 200) {
                [ws delReportInfo:info];
            } else {
                info.reporting = NO;
            }
          }];
    [initTask resume];
}

- (void)delReportInfo:(TVCReportInfo *)info {
    @synchronized(self.reportCaches) {
        [self.reportCaches removeObject:info];
    }
    info = nil;
}

@end
