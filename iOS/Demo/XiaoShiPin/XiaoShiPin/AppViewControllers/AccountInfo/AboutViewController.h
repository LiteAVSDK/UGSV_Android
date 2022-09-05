//
//  AboutViewController.h
//  XiaoShiPinApp
//
//  Created by tao yue on 2022/3/10.
//  Copyright © 2022 Tencent. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/**
  AboutViewController 类说明 : 该类显示app详情的界面
  界面上包括一个tableview和一个button
 
  tableview上显示三行元素:
  第一个cell显示 : sdk版本
  第二个cell显示 : app版本
  第三个cell显示 : 腾讯云小视频
  第四个cell显示 : 获取技术服务
  第五个cell显示 : 下载SDK
  第六个cell显示 : 产品介绍
 */
@interface AboutViewController : UIViewController<UITableViewDelegate, UITableViewDataSource>

@end

NS_ASSUME_NONNULL_END
