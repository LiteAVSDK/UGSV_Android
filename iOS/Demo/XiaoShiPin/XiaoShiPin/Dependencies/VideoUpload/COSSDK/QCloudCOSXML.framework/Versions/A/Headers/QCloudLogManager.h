//
//  QCloudLogManager.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 2018/10/8.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
#if TARGET_OS_IOS
@interface QCloudLogTableViewController : UIViewController
- (instancetype)initWithLog:(NSArray *)logContent;
@end
#endif
/**
 QCloudCOSXML 日志管理类
 */
@interface QCloudLogManager : NSObject
/**
 是否显示日志信息
 */
@property (nonatomic, assign) BOOL shouldShowLog;
+ (instancetype)sharedInstance;
- (void)showLogs;
/**
 已经产生的日志
 */
- (NSArray *)currentLogs;

/**
 根据日志路径读取日志
 @params path 日志路径
 */
- (NSString *)readLog:(NSString *)path;
- (BOOL)shouldShowLog;
@end

NS_ASSUME_NONNULL_END
