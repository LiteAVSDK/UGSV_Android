//
//  QCloudBucketAccelerateConfiguration.h
//  QCloudCOSXML
//
//  Created by karisli(李雪) on 2020/8/28.
//

#import <Foundation/Foundation.h>
#import <QCloudCore/QCloudCore.h>
#import "QCloudCOSBucketAccelerateStatusEnum.h"
NS_ASSUME_NONNULL_BEGIN

@interface QCloudBucketAccelerateConfiguration : NSObject
/**
    说明版本是否开启，枚举值：Suspended\Enabled
    */
@property (assign, nonatomic) QCloudCOSBucketAccelerateStatus status;
@end

NS_ASSUME_NONNULL_END
