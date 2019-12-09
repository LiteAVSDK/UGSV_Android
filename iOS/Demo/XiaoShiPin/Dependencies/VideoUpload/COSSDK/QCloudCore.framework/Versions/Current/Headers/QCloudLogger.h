//
//  QCloudLogger.h
//  Pods
//
//  Created by Dong Zhao on 2017/3/14.
//
//

#import <Foundation/Foundation.h>
#import "QCloudLogModel.h"

#define QCloudLog(level, frmt, ...) \
[[QCloudLogger sharedLogger] logMessageWithLevel:level  cmd:__PRETTY_FUNCTION__ line:__LINE__ file:__FILE__ format:(frmt), ##__VA_ARGS__]

#define QCloudLogError(frmt, ...) \
QCloudLog(QCloudLogLevelError, (frmt), ##__VA_ARGS__)

#define QCloudLogWarning(frmt, ...) \
QCloudLog(QCloudLogLevelWarning, (frmt), ##__VA_ARGS__)

#define QCloudLogInfo(frmt, ...) \
QCloudLog(QCloudLogLevelInfo, (frmt), ##__VA_ARGS__)

#define QCloudLogDebug( frmt, ...) \
QCloudLog(QCloudLogLevelDebug, (frmt), ##__VA_ARGS__)


#define QCloudLogVerbose(frmt, ...) \
QCloudLog(QCloudLogLevelInfo, (frmt), ##__VA_ARGS__)

#define QCloudLogException(exception) \
QCloudLogError( @"Caught \"%@\" with reason \"%@\"%@", \
exception.name, exception, \
[exception callStackSymbols] ? [NSString stringWithFormat:@":\n%@.", [exception callStackSymbols]] : @"")

#define QCloudLogTrance()\
QCloudLog(QCloudLogLevelDebug,@"%@",[NSThread callStackSymbols])


@interface QCloudLogger : NSObject

@property (nonatomic, assign) QCloudLogLevel logLevel;

@property (nonatomic, strong, readonly) NSString* logDirctoryPath;

@property (nonatomic, assign) uint64_t maxStoarageSize;

@property (nonatomic, assign) float keepDays;
///--------------------------------------
#pragma mark - Shared Logger
///--------------------------------------

/**
 A shared instance of `PFLogger` that should be used for all logging.
 
 @return An shared singleton instance of `PFLogger`.
 */
+ (instancetype)sharedLogger; //TODO: (nlutsenko) Convert to use an instance everywhere instead of a shared singleton.

///--------------------------------------
#pragma mark - Logging Messages
///--------------------------------------


- (void)logMessageWithLevel:(QCloudLogLevel)level
                        cmd:(const char*)commandInfo
                       line:(int)line
                       file:(const char*)file
                     format:(NSString *)format, ...;
@end
