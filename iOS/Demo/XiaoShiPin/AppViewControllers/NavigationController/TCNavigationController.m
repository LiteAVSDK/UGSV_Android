//
//  TCNavigationController.m
//  XiaoShiPin
//
//  Created by cui on 2019/11/20.
//  Copyright © 2019 Tencent. All rights reserved.
//

#import "TCNavigationController.h"

@interface TCNavigationController ()

@end

@implementation TCNavigationController

- (BOOL)shouldAutorotate
{
    return self.topViewController.shouldAutorotate;
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}
@end
