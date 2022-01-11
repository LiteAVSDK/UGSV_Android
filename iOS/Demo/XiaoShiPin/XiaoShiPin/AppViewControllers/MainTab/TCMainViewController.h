//
//  TCMainTabViewController.h
//  TCLVBIMDemo
//
//  Created by annidyfeng on 16/7/29.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 *  主界面的 Tab Controller，用于切换列表、推流和个人资料页面
 */
@interface TCMainViewController : UITabBarController
@property (copy, nonatomic) void (^loginHandler)(TCMainViewController *);
@end
