//
//  QCloudOperationQueue.h
//  Pods
//
//  Created by Dong Zhao on 2017/3/10.
//
//

#import <Foundation/Foundation.h>
#import "QCloudRequestOperation.h"

@interface QCloudOperationQueue : NSObject
@property (nonatomic, assign) int maxConcurrentCount;
@property (nonatomic, assign) int customConcurrentCount;
- (void)addOpreation:(QCloudRequestOperation *)operation;
- (void)cancel:(QCloudRequestOperation *)operation;
- (void)cancelByRequestID:(int64_t)requestID;
- (void)cancelByRequestIDs:(NSArray<NSNumber *> *)requestIDs;
@end
