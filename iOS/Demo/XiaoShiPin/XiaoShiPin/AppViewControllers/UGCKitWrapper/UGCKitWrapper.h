//
//  UGCKitWrapper.h
//  XiaoShiPin
//
//  Created by cui on 2019/11/27.
//  Copyright © 2019 Tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UGCKit.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, TCBackMode) {
    TCBackModePop,
    TCBackModeDismiss
};

@interface UGCKitWrapper : NSObject
- (instancetype)initWithViewController:(UIViewController *)viewController theme:(nullable UGCKitTheme *)theme;
- (void)showRecordViewControllerWithConfig:(UGCKitRecordConfig *)config;
- (void)showEditViewController:(UGCKitResult *)result
                      rotation:(TCEditRotation)rotation
        inNavigationController:(UINavigationController *)nav
                      backMode:(TCBackMode)backMode;
@end

NS_ASSUME_NONNULL_END
