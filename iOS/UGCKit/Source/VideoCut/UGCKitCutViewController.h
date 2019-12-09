// Copyright (c) 2019 Tencent. All rights reserved.


#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "UGCKitResult.h"
#import "UGCKitTheme.h"

@interface UGCKitCutViewController : UIViewController

- (instancetype)initWithMedia:(UGCKitMedia *)media theme:(UGCKitTheme *)theme;
@property (copy, nonatomic) void(^completion)(UGCKitResult *result, int rotation);
@end

