//
//  QCloudHTTPTaskDelayManager.h
//  CLSLogger
//
//  Created by wjielai on 2020/4/29.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface QCloudHTTPTaskDelayManager : NSObject

- (instancetype)initWithStart:(NSInteger)startBackoff max:(NSInteger)maxBackoff;

- (void)reset;

- (void)increase;

- (NSInteger)getDelay;

@end

NS_ASSUME_NONNULL_END
