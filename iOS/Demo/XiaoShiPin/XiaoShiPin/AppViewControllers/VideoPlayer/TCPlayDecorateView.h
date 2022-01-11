//
//  TCPlayDecorateView.h
//  TCLVBIMDemo
//
//  Created by zhangxiang on 16/8/1.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TCLiveListModel.h"

@protocol TCPlayDecorateDelegate <NSObject>
-(void)closeVC:(BOOL)isRefresh  popViewController:(BOOL)popViewController;
-(void)clickScreen:(UITapGestureRecognizer *)gestureRecognizer;
-(void)clickPlayVod;
-(void)onSeek:(UISlider *)slider;
-(void)onSeekBegin:(UISlider *)slider;
-(void)onSeekEnd:(UISlider *)slider;
-(void)clickLog:(UIButton *)button;
-(void)clickChorus:(UIButton *)button;
@optional
-(void)clickShare:(UIButton *)button;
@end


/**
 *  播放模块逻辑view，里面展示了消息列表，弹幕动画，观众列表等UI，其中与SDK的逻辑交互需要交给主控制器处理
 */
@interface TCPlayDecorateView : UIView<UITextFieldDelegate, UIAlertViewDelegate, UIGestureRecognizerDelegate>

@property(nonatomic,weak) id<TCPlayDecorateDelegate>delegate;
@property(nonatomic,retain)  UILabel            *playDuration;
@property(nonatomic,retain)  UISlider           *playProgress;
@property(nonatomic,retain)  UILabel            *playLabel;
@property(nonatomic,retain)  UIButton           *playBtn;
@property(nonatomic,retain)  UIButton           *btnChat;
@property(nonatomic,retain)  UIButton           *btnChorus;
@property(nonatomic,retain)  UIButton           *btnLog;
@property(nonatomic,retain)  UIButton           *btnShare;
@property(nonatomic,retain)  UIView             *cover;
@property(nonatomic,retain)  UITextView         *statusView;
@property(nonatomic,retain)  UITextView         *logViewEvt;


-(void)setLiveInfo:(TCLiveInfo *)liveInfo;
-(void)preprareForReuse;
@end


@interface TCShowLiveTopView : UIView
@property (strong, nonatomic) NSString *hostNickName;
@property (strong, nonatomic) NSString *hostFaceUrl;

- (instancetype)initWithFrame:(CGRect)frame hostNickName:(NSString *)hostNickName hostFaceUrl:(NSString *)hostFaceUrl;

- (void)cancelImageLoading;
@end
