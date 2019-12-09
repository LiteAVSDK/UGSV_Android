//
//  TCBasePlayViewController.h
//  TCLVBIMDemo
//
//  Created by annidyfeng on 2017/9/15.
//  Copyright © 2017年 tencent. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TCPlayDecorateView.h"
#import "TCLiveListModel.h"
#import "SDKHeader.h"
#import "SDKHeader.h"
#import "TCBasePlayViewController.h"

typedef void(^videoIsReadyBlock)(void);
extern NSString *const kTCLivePlayError;

@interface TCBasePlayViewController : UIViewController
@property  TCLiveInfo           *liveInfo;
@property (nonatomic, copy)   videoIsReadyBlock   videoIsReady;


-(id)initWithPlayInfo:(TCLiveInfo *)info  videoIsReady:(videoIsReadyBlock)videoIsReady;
- (void) toastTip:(NSString*)toastInfo;

-(UIImage*)scaleImage:(UIImage *)image scaleToSize:(CGSize)size;
-(UIImage *)clipImage:(UIImage *)image inRect:(CGRect)rect;
-(UIImage *)gsImage:(UIImage *)image withGsNumber:(CGFloat)blur;
@end
