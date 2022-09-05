//
//  AppDelegate.m
//  XiaoShiPin
//
//  Created by cui on 2019/11/11.
//  Copyright © 2019 Tencent. All rights reserved.
//

#import "AppDelegate.h"
#import "UGCKit.h"
#import "HUDHelper.h"
#import "TCMainViewController.h"
#import "TCLoginViewController.h"
#import "TCUtil.h"
#import <Bugly/Bugly.h>
#import "SDKHeader.h"
#import <XMagic/TELicenseCheck.h>

@interface AppDelegate ()

@end

@implementation AppDelegate
@synthesize window = _window;

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    BuglyConfig *config = [[BuglyConfig alloc] init];
    config.unexpectedTerminatingDetectionEnable = YES;
#if DEBUG
    config.channel = @"DEBUG";
#else
    NSString *bundleID = [NSBundle mainBundle].bundleIdentifier;
    if ([bundleID isEqualToString:@"com.tencent.fx.xiaoshipin.db"]) {
        config.channel = @"CI";
    } else {
        config.channel = @"AppStore";
    }
#endif
//    [Bugly startWithAppId:@"6efe67cbad" config:config];

    [TXUGCBase setLicenceURL:@"" key:@""];
    [TELicenseCheck setTELicense:@"" key:@"" completion:^(NSInteger authresult, NSString * _Nonnull errorMsg) {
               if (authresult == TELicenseCheckOk) {
                    NSLog(@"鉴权成功");
                } else {
                    NSLog(@"鉴权失败");
                }
        }];

    [TXLiveBase setLogLevel:LOGLEVEL_VERBOSE];
    [UGCKitReporter registerReporter:[TCUtil class]];
    TCMainViewController *mainController = [[TCMainViewController alloc] init];
    mainController.loginHandler = ^(TCMainViewController *_) {
        [self showLoginUI];
    };
    UIWindow *window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    [HUDHelper sharedInstance].keyWindow = window;
    window.rootViewController = mainController;
    [window makeKeyAndVisible];
    _window = window;

    return YES;
}

- (void)showLoginUI {
    TCLoginViewController *loginViewController = [[TCLoginViewController alloc] init];
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:loginViewController];
    [self.window.rootViewController presentViewController:nav animated:YES completion:nil];
}

@end
