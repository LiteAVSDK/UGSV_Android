//
//  TCPlayViewCell.h
//  TXXiaoShiPinDemo
//
//  Created by xiang zhang on 2018/2/2.
//  Copyright © 2018年 tencent. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TCPlayDecorateView.h"

@interface TCPlayViewCell : UITableViewCell<TCPlayDecorateDelegate>
@property(strong,nonatomic) UIImageView* videoCoverView;
@property(strong,nonatomic) UIView* videoParentView;
@property(strong,nonatomic) UILabel* reviewLabel;
@property(strong,nonatomic) TCPlayDecorateView* logicView;
@property(weak,nonatomic)   id<TCPlayDecorateDelegate>delegate;
-(void)setLiveInfo:(TCLiveInfo *)liveInfo;
@end
