//
//  TCVodPlayViewController.h
//  TCLVBIMDemo
//
//  Created by annidyfeng on 2017/9/15.
//  Copyright © 2017年 tencent. All rights reserved.
//

#import "TCBasePlayViewController.h"

#import "SDKHeader.h"

@interface TCVodPlayViewController : TCBasePlayViewController<UITextFieldDelegate,UITableViewDelegate,UITableViewDataSource,UIScrollViewDelegate, TXVodPlayListener,TCPlayDecorateDelegate>

@property (nonatomic, assign) BOOL  log_switch;
@property (nonatomic, copy) void (^onTapChorus)(TCVodPlayViewController *controller);

-(id)initWithPlayInfoS:(NSArray<TCLiveInfo *>*) liveInfos  liveInfo:(TCLiveInfo *)liveInfo videoIsReady:(videoIsReadyBlock)videoIsReady;

- (void)stopRtmp;

- (void)onAppDidEnterBackGround:(UIApplication*)app;

- (void)onAppWillEnterForeground:(UIApplication*)app;
@end
