//
//  QCloudCOSBucketAccelerateStatus.h
//  QCloudCOSXML
//
//  Created by karisli(李雪) on 2020/8/28.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/**
 存储桶多版本状态
*/
typedef NS_ENUM(NSInteger, QCloudCOSBucketAccelerateStatus) {

    QCloudCOSBucketAccelerateStatusEnabled = 1,
    QCloudCOSBucketAccelerateStatusSuspended = 2,
};

FOUNDATION_EXTERN QCloudCOSBucketAccelerateStatus QCloudCOSBucketAccelerateStatusDumpFromString(NSString *key);
FOUNDATION_EXTERN NSString *QCloudCOSBucketAccelerateStatusTransferToString(QCloudCOSBucketAccelerateStatus type);
NS_ASSUME_NONNULL_END
