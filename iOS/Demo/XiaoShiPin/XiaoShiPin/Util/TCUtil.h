//
//  TCUtil.h
//  TCLVBIMDemo
//
//  Created by felixlin on 16/8/2.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TCLog.h"
#import "TCConstants.h"
//#import "ColorMacro.h"
//#import "UIView+Additions.h"
// 日志
#ifdef DEBUG

#ifndef DebugLog
//#define DebugLog(fmt, ...) NSLog((@"[%s Line %d]" fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)
#define DebugLog(fmt, ...) [[TCLog shareInstance] log:fmt, ##__VA_ARGS__]
#endif

#else

#ifndef DebugLog
#define DebugLog(fmt, ...)  [[TCLog shareInstance] log:fmt, ##__VA_ARGS__]
#endif
#endif

#ifndef TC_PROTECT_STR
#define TC_PROTECT_STR(x) (x == nil ? @"" : x)
#endif

//report
//static NSString * const UGCKitReportItem_install  = @"install";
//static NSString * const UGCKitReportItem_startup  = @"startup";
//static NSString * const UGCKitReportItem_staytime = @"staytime";
//static NSString * const UGCKitReportItem_startrecord = @"startrecord";
//static NSString * const UGCKitReportItem_videorecord = @"videorecord";
//static NSString * const UGCKitReportItem_videoedit = @"videoedit";
//static NSString * const UGCKitReportItem_pictureedit = @"pictureedit";
//static NSString * const UGCKitReportItem_videojoiner = @"videojoiner";

static NSString * const xiaoshipin_login    = @"login";
static NSString * const xiaoshipin_register = @"register";
static NSString * const xiaoshipin_vodplay = @"vodplay";
static NSString * const xiaoshipin_videosign = @"videosign";
static NSString * const xiaoshipin_videouploadvod = @"videouploadvod";
static NSString * const xiaoshipin_videouploadserver = @"videouploadserver";
static NSString * const xiaoshipin_videochorus = @"videochorus";
static NSString * const xiaoshipin_videotrio = @"videotrio";
static NSString * const xiaoshipin_about_sdk = @"about_sdk";

#ifndef NSStringCheck
#define NSStringCheck(x)\
({\
if(x == nil || ![x isKindOfClass:[NSString class]]){\
x = @"";}\
})
#endif

@interface TCUtil : NSObject
+ (void)asyncSendHttpRequest:(NSDictionary*)param handler:(void (^)(int resultCode, NSDictionary* resultDict))handler;

+ (void)asyncSendHttpRequest:(NSString*)command params:(NSDictionary*)params handler:(void (^)(int resultCode, NSString* message, NSDictionary* resultDict))handler;

+ (void)asyncSendHttpRequest:(NSString*)command token:(NSString*)token params:(NSDictionary*)params handler:(void (^)(int resultCode, NSString* message, NSDictionary* resultDict))handler;

+ (void)downloadVideo:(NSString *)videoUrl cachePath:(NSString *)cachePath process:(void(^)(CGFloat process))processHandler complete:(void(^)(NSString *videoPath))completeHandler;

+ (void)report:(NSString *)type userName:(NSString *)userName code:(UInt64)code  msg:(NSString *)msg;

+ (NSData *)dictionary2JsonData:(NSDictionary *)dict;

+ (NSDictionary *)jsonData2Dictionary:(NSString *)jsonData;

+ (NSString *)getFileCachePath:(NSString *)fileName;

+ (void)removeCacheFile:(NSString*)filePath;

+ (NSUInteger)getContentLength:(NSString*)string;

+ (NSString *)transImageURL2HttpsURL:(NSString *)httpURL;

+ (NSString*) getStreamIDByStreamUrl:(NSString*) strStreamUrl;

+ (UIImage *)gsImage:(UIImage *)image withGsNumber:(CGFloat)blur;

+ (UIImage*)scaleImage:(UIImage *)image scaleToSize:(CGSize)size;

+ (UIImage *)clipImage:(UIImage *)image inRect:(CGRect)rect;

+ (void)toastTip:(NSString*)toastInfo parentView:(UIView *)parentView;

+ (float)heightForString:(UITextView *)textView andWidth:(float)width;

+ (BOOL)isSuitableMachine:(int)targetPlatNum;

+ (NSDate *)timeToDate:(NSString *)timeStr;

+ (NSString *)dateToTime:(NSDate *)date;
@end

__attribute__((annotate("returns_localized_nsstring")))
NS_INLINE NSString *LocalizationNotNeeded(NSString *s) {
    return s;
}
