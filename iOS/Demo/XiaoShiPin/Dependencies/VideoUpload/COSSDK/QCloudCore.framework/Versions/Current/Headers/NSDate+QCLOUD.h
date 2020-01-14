//
//  NSDate+QCLOUD.h
//  QCloudCore
//
//  Created by karisli(李雪) on 2018/12/18.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSDate (QCLOUD)
+(NSDate *)qcloud_calibrateTime;
+(void)qcloud_setTimeDeviation:(NSTimeInterval)timeDeviation;
+(NSTimeInterval)qcloud_getTimeDeviation;
+(NSString *)qcloud_stringFromDate:(NSDate *)date;
@end

NS_ASSUME_NONNULL_END
