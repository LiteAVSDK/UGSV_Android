// Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>

#define BGM_DEBUG 1

#define BGMLog(...) {\
if(BGM_DEBUG)NSLog(__VA_ARGS__);\
}

NS_ASSUME_NONNULL_BEGIN

@interface TCBGMElement : NSObject
@property NSString* name;
@property NSString* netUrl;
@property NSString* localUrl;
@property NSString* author;
@property NSString* title;
@property NSNumber* duration;//float MicroSeconds
@property NSNumber* isValid;
@end

@protocol TCBGMHelperListener <NSObject>

/**
 从json文件创建BGM列表，失败dict返回nil
 */
@required
-(void) onBGMListLoad:(nullable NSDictionary*)dict;

/**
 每首BGM的进度回调
 */
@optional
-(void) onBGMDownloading:(TCBGMElement*)current percent:(float)percent;

/**
 下载结束回调，失败current返回nil
 */
@optional
-(void) onBGMDownloadDone:(TCBGMElement*)element;



@end


@interface UGCKitBGMHelper : NSObject

-(void) setDelegate:(nonnull id<TCBGMHelperListener>) delegate;

-(void) initBGMListWithJsonFile:(NSString* _Nonnull)url;

+ (instancetype)sharedInstance;
/**
 下载BGM
新任务->新下载
当前正在下载->暂停下载
当前暂停->恢复下载
当前下载完成->重新下载

 @param name BGM名称
 */
-(void) downloadBGM:(TCBGMElement*) name;

//-(void) pauseAllTasks;
//
//-(void) resumeAllTasks;
@end

NS_ASSUME_NONNULL_END
