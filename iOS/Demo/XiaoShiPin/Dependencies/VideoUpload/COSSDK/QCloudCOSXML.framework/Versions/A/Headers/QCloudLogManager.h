//
//  QCloudLogManager.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 2018/10/8.
//
#if TARGET_OS_IPHONE
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface QCloudLogManager : NSObject
+ (instancetype) sharedInstance;
- (NSArray *)currentLogs;
- (NSString *) readLog:(NSString *)path;
@end

NS_ASSUME_NONNULL_END
#endif
