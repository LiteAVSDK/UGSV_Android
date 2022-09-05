//
//  XmagicResDownload.m
//  BeautyDemo
//
//  Created by tao yue on 2022/7/21.
//  Copyright (c) 2019 Tencent. All rights reserved.

#import "XmagicResDownload.h"
#import "TCDownloadManager.h"
#import "TCDownloadModel.h"

@interface XmagicResDownload ()
// 资源resDict
@property (nonatomic, strong) NSDictionary *resDict;
// 动效Url
@property (nonatomic, strong) NSString     *motionsBaseUrl;
// 动效iconUrl
@property (nonatomic, strong) NSString     *motionIconBaseUrl;
// bundle
@property (nonatomic, strong) NSBundle     *resourceBundle;
// initDict
@property bool initDict;


@end

@implementation XmagicResDownload



+ (instancetype)shardManager {
  static XmagicResDownload *instance;
  static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (instancetype)init{
    if (self = [super init]) {
        if (!self.initDict) {
            self.resDict = [self readLocalFileWithName:@"xmagic_motions_S1-07"];
            self.motionsBaseUrl = [self.resDict objectForKey:@"motionsBaseUrl"];
            self.motionIconBaseUrl = [self.resDict objectForKey:@"motionIconBaseUrl"];
            self.initDict = YES;
        }
    }
    return self;
}

- (void)downloadItem:(NSString *)key process:(ProgressCallback)process complete:(CompleteCallback)complete{
    if ([key isEqualToString:@"naught"]) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@%@.zip",self.motionsBaseUrl,key];
    TCDownloadModel *model = [[TCDownloadManager shareManager] addDownloadModelWithURL:url];
    [model setProgressInfoBlock:^(TCDownloadProgress * _Nonnull tcDownloadProgress) {
        process(tcDownloadProgress.progress);
    }];
    [model setUnZipBlock:^{
        complete(YES);
    }];
    
}



- (NSString *)getIconUrl:(NSString *)key{
    return [NSString stringWithFormat:@"%@%@",self.motionIconBaseUrl,key];
}

// 读取本地JSON文件
- (NSDictionary *)readLocalFileWithName:(NSString *)name {
    NSString *resourcePath = [[NSBundle mainBundle] pathForResource:@"xmagickitResources"
                                                             ofType:@"bundle"];
    _resourceBundle = [NSBundle bundleWithPath:resourcePath];
    // 获取文件路径
    NSString *path = [_resourceBundle pathForResource:name ofType:@"json"];
    // 将文件数据化
    NSData *data = [[NSData alloc] initWithContentsOfFile:path];
    // 对数据进行JSON格式化并返回字典形式
    return [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
}

@end
