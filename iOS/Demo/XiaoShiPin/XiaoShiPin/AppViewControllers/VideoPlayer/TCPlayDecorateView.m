//
//  TCPlayDecorateView.m
//  TCLVBIMDemo
//
//  Created by zhangxiang on 16/8/1.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCPlayDecorateView.h"
#import <SDWebImage/UIImageView+WebCache.h>
#import "TCUserInfoModel.h"
#import "TCLoginModel.h"
#import "TCConstants.h"
#import "TCLiveListModel.h"
#import "HUDHelper.h"
//#import <UShareUI/UMSocialUIManager.h>
//#import <UMSocialCore/UMSocialCore.h>
#import "TCUtil.h"
#import "UIView+Additions.h"

#define BOTTOM_BTN_ICON_WIDTH  35
#define StatusBarHeight [[UIApplication sharedApplication] statusBarFrame].size.height

@implementation TCPlayDecorateView
{
    TCLiveInfo         *_liveInfo;
    UIButton           *_closeBtn;
    CGPoint            _touchBeginLocation;
    BOOL               _bulletBtnIsOn;
    BOOL               _viewsHidden;
    NSMutableArray     *_heartAnimationPoints;
    
    TCShowLiveTopView  *_topView;
}

-(instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onLogout:) name:logoutNotification object:nil];
        UITapGestureRecognizer *tap =[[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(clickScreen:)];
        tap.delegate = self;
        [self addGestureRecognizer:tap];
        [self initUI: NO];
    }
    return self;
}

-(void)setLiveInfo:(TCLiveInfo *)liveInfo
{
    _liveInfo   = liveInfo;
    _topView.hostFaceUrl = liveInfo.userinfo.headpic;
    _topView.hostNickName = liveInfo.userinfo.nickname;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)initUI:(BOOL)linkmic {
    self.backgroundColor = [UIColor clearColor];
    
    //close VC
    _closeBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_closeBtn setFrame:CGRectMake(self.width - 15 - BOTTOM_BTN_ICON_WIDTH, self.height - 50, BOTTOM_BTN_ICON_WIDTH, BOTTOM_BTN_ICON_WIDTH)];
    [_closeBtn setImage:[UIImage imageNamed:@"close"] forState:UIControlStateNormal];
    [_closeBtn addTarget:self action:@selector(closeVC) forControlEvents:UIControlEventTouchUpInside];
    _closeBtn.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleTopMargin;
    [self addSubview:_closeBtn];
    
    //topview,展示主播头像，在线人数及点赞
    _topView = [[TCShowLiveTopView alloc] initWithFrame:CGRectMake(5, StatusBarHeight + 5, 35, 35)
                                           hostNickName:_liveInfo.userinfo.nickname == nil ? _liveInfo.userid : _liveInfo.userinfo.nickname
                                           hostFaceUrl:_liveInfo.userinfo.headpic];
    _topView.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleRightMargin;
    [self addSubview:_topView];
    
    //举报
    UIButton *reportBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [reportBtn setFrame:CGRectMake(_topView.right + 15, _topView.top + 5, 150, 30)];
    [reportBtn setTitle:NSLocalizedString(@"TCPlayDecorate.ActionList", nil) forState:UIControlStateNormal];
    reportBtn.titleLabel.font = [UIFont systemFontOfSize:13];
    [reportBtn sizeToFit];
    reportBtn.width += 20;
    [reportBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [reportBtn setBackgroundColor:[UIColor blackColor]];
    [reportBtn addTarget:self action:@selector(onReportClick) forControlEvents:UIControlEventTouchUpInside];
    [reportBtn setAlpha:0.7];
    reportBtn.layer.cornerRadius = 15;
    reportBtn.layer.masksToBounds = YES;
    reportBtn.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleRightMargin;

    [self addSubview:reportBtn];
    
    //合唱
    _btnChorus = [UIButton buttonWithType:UIButtonTypeCustom];
    [_btnChorus setImage:[UIImage imageNamed:@"GroupPhoto-normal"] forState:UIControlStateNormal];
    [_btnChorus setImage:[UIImage imageNamed:@"GroupPhoto-press"] forState:UIControlStateHighlighted];
    [_btnChorus sizeToFit];
    _btnChorus.center = CGPointMake(self.frame.size.width - _btnChorus.size.width, reportBtn.center.y);
    [_btnChorus addTarget:self action:@selector(clickChorus:) forControlEvents:UIControlEventTouchUpInside];
    _btnChorus.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
    [self addSubview:_btnChorus];

    
    
    int   icon_size = BOTTOM_BTN_ICON_WIDTH;
    float startSpace = 15;
    float icon_center_y = self.height - icon_size/2 - startSpace;
    
    _playBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_playBtn setImage:[UIImage imageNamed:@"start"] forState:UIControlStateNormal];
    [_playBtn setFrame:CGRectMake(15, _closeBtn.y, BOTTOM_BTN_ICON_WIDTH, BOTTOM_BTN_ICON_WIDTH)];
    [_playBtn addTarget:self action:@selector(clickPlayVod) forControlEvents:UIControlEventTouchUpInside];
    _playBtn.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin;

    [self addSubview:_playBtn];
    
    _playLabel = [[UILabel alloc]init];
    _playLabel.frame = CGRectMake(_playBtn.right + 10, _playBtn.center.y - 5, 53, 10);
    [_playLabel setText:LocalizationNotNeeded(@"00:00:00")];
    [_playLabel setTextAlignment:NSTextAlignmentRight];
    [_playLabel setFont:[UIFont systemFontOfSize:12]];
    [_playLabel setTextColor:[UIColor whiteColor]];
    _playLabel.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin;
    [self addSubview:_playLabel];
    
    UILabel *centerLabel =[[UILabel alloc]init];
    centerLabel.frame = CGRectMake(_playLabel.right, _playLabel.y, 4, 10);
    centerLabel.text = @"/";
    centerLabel.font = [UIFont systemFontOfSize:12];
    centerLabel.textColor = [UIColor whiteColor];
    centerLabel.textAlignment = NSTextAlignmentCenter;
    centerLabel.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin;
    [self addSubview:centerLabel];
    
    _playDuration = [[UILabel alloc]init];
    _playDuration.frame = CGRectMake(centerLabel.right, centerLabel.y, 53, 10);
    [_playDuration setText:LocalizationNotNeeded(@"--:--:--")];
    [_playDuration setFont:[UIFont systemFontOfSize:12]];
    [_playDuration setTextColor:[UIColor whiteColor]];
    _playDuration.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin;
    [self addSubview:_playDuration];
    
    
    //log显示或隐藏
    _btnLog = [UIButton buttonWithType:UIButtonTypeCustom];
    _btnLog.center = CGPointMake(_btnChorus.center.x - icon_size - 15, icon_center_y);
    _btnLog.bounds = CGRectMake(0, 0, icon_size, icon_size);
    [_btnLog setImage:[UIImage imageNamed:@"log"] forState:UIControlStateNormal];
    [_btnLog addTarget:self action:@selector(clickLog:) forControlEvents:UIControlEventTouchUpInside];
    _btnLog.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleLeftMargin;
#if ENABLE_LOG
    _btnLog.hidden = NO;
#else
    _btnLog.hidden = YES;
#endif
    [self addSubview:_btnLog];
    
    _btnShare = ({
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        [self addSubview:btn];
#if ENABLE_LOG
        btn.center = CGPointMake(_closeBtn.center.x - (icon_size + 15) * 2, icon_center_y);
#else
        btn.center = _btnLog.center;
#endif
        btn.bounds = CGRectMake(0, 0, icon_size, icon_size);
        [btn setImage:[UIImage imageNamed:@"share"] forState:UIControlStateNormal];
        [btn setImage:[UIImage imageNamed:@"share_pressed"] forState:UIControlStateHighlighted];
        [btn addTarget:self action:@selector(clickShare:) forControlEvents:UIControlEventTouchUpInside];
        btn;
    });
    _btnShare.hidden = YES;
    
    _playProgress=[[UISlider alloc]initWithFrame:CGRectMake(15, _playBtn.top - 35, self.width - 30, 20)];
    [_playProgress setThumbImage:[UIImage imageNamed:@"slider"] forState:UIControlStateNormal];
    [_playProgress setMinimumTrackImage:[UIImage imageNamed:@"green"] forState:UIControlStateNormal];
    [_playProgress setMaximumTrackImage:[UIImage imageNamed:@"gray"] forState:UIControlStateNormal];
    _playProgress.maximumValue = 0;
    _playProgress.minimumValue = 0;
    _playProgress.value = 0;
    _playProgress.continuous = NO;
    [_playProgress addTarget:self action:@selector(onSeek:) forControlEvents:(UIControlEventValueChanged)];
    [_playProgress addTarget:self action:@selector(onSeekBegin:) forControlEvents:(UIControlEventTouchDown)];
    [_playProgress addTarget:self action:@selector(onSeekEnd:) forControlEvents:UIControlEventTouchUpInside];
    _playProgress.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleWidth;
    [self addSubview:_playProgress];
    
    
    //LOG UI
    _cover = [[UIView alloc]init];
    _cover.frame  = CGRectMake(10.0f, 55 + 2*icon_size, self.width - 20, self.height - 110 - 3 * icon_size);
    _cover.backgroundColor = [UIColor whiteColor];
    _cover.alpha  = 0.5;
    _cover.hidden = YES;
    [self addSubview:_cover];
    
    int logheadH = 65;
    _statusView = [[UITextView alloc] initWithFrame:CGRectMake(10.0f, 55 + 2*icon_size, self.width - 20,  logheadH)];
    _statusView.backgroundColor = [UIColor clearColor];
    _statusView.alpha = 1;
    _statusView.textColor = [UIColor blackColor];
    _statusView.editable = NO;
    _statusView.hidden = YES;
    [self addSubview:_statusView];
    
    
    _logViewEvt = [[UITextView alloc] initWithFrame:CGRectMake(10.0f, 55 + 2*icon_size + logheadH, self.width - 20, self.height - 110 - 3 * icon_size - logheadH)];
    _logViewEvt.backgroundColor = [UIColor clearColor];
    _logViewEvt.alpha = 1;
    _logViewEvt.textColor = [UIColor blackColor];
    _logViewEvt.editable = NO;
    _logViewEvt.hidden = YES;
    [self addSubview:_logViewEvt];
}

-(void)onReportClick{
    __weak __typeof(self) ws = self;
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:nil
                                                                        message:nil
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"TCPlayDecorate.ActionReport", nil)
                                                   style:UIAlertActionStyleDestructive
                                                 handler:^(UIAlertAction * _Nonnull action) {
        [ws reportUser];
    }]];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"TCPlayDecorate.ActionDiss", nil)
                                                   style:UIAlertActionStyleDestructive
                                                 handler:^(UIAlertAction * _Nonnull action) {
        [ws confirmReportUser];
        [[HUDHelper sharedInstance] tipMessage:NSLocalizedString(@"TCPlayDecorate.ActionDissResult", nil)];

    }]];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"TCPlayDecorate.ActionBlacklist", nil)
                                                   style:UIAlertActionStyleDestructive
                                                 handler:^(UIAlertAction * _Nonnull action) {
        [ws confirmReportUser];
        [[HUDHelper sharedInstance] tipMessage:NSLocalizedString(@"TCPlayDecorate.ActionBlacklistResult", nil)];
    }]];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.Cancel", nil)
                                                   style:UIAlertActionStyleCancel
                                                 handler:nil]];

    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:controller animated:YES completion:nil];
}

- (void)reportUser{
    __weak __typeof(self) ws = self;

    UIAlertController *controller = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"TCPlayDecorate.ReportClass", nil)
                                                                        message:nil
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    NSArray *options = @[
        NSLocalizedString(@"TCPlayDecorate.ReportCause1", nil),
        NSLocalizedString(@"TCPlayDecorate.ReportCause2", nil),
        NSLocalizedString(@"TCPlayDecorate.ReportCause3", nil),
        NSLocalizedString(@"TCPlayDecorate.ReportCause4", nil),
        NSLocalizedString(@"TCPlayDecorate.ReportCause5", nil),
        NSLocalizedString(@"TCPlayDecorate.ReportCause6", nil),
        NSLocalizedString(@"TCPlayDecorate.ReportCause7", nil)
    ];
    for (NSString *option in options) {
        [controller addAction:[UIAlertAction actionWithTitle:option
                                                       style:UIAlertActionStyleDestructive
                                                     handler:^(UIAlertAction * _Nonnull action) {
            [ws confirmReportUser];
            [[HUDHelper sharedInstance] tipMessage:NSLocalizedString(@"TCPlayDecorate.ReportResult", nil)];
        }]];
    }

    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.Cancel", nil)
                                                   style:UIAlertActionStyleCancel
                                                 handler:nil]];

    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:controller animated:YES completion:nil];
}

- (void)confirmReportUser{
    TCUserInfoData  *userInfoData = [[TCUserInfoModel sharedInstance] getUserProfile];
    NSDictionary* params = @{@"userid" : TC_PROTECT_STR(_liveInfo.userid), @"hostuserid" : TC_PROTECT_STR(userInfoData.identifier)};
    __weak __typeof(self) weakSelf = self;
    [TCUtil asyncSendHttpRequest:@"report_user" token:nil params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        [weakSelf performSelector:@selector(onLogout:) withObject:nil afterDelay:1];
    }];
}

-(void)clickChorus:(UIButton *)button{
    if (self.delegate) [self.delegate clickChorus:button];
}

-(void)clickLog:(UIButton *)button{
    if (self.delegate) [self.delegate clickLog:button];
}

-(void)clickShare:(UIButton *)button{
    if ([self.delegate respondsToSelector:@selector(clickShare:)]) {
        [self.delegate clickShare:button];
    }
}

// 监听登出消息
- (void)onLogout:(NSNotification*)notice {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self.delegate closeVC:YES popViewController:YES];
}

-(void)preprareForReuse
{
    [_topView cancelImageLoading];
}

#pragma mark TCPlayDecorateDelegate
-(void)closeVC{
    if (self.delegate && [self.delegate respondsToSelector:@selector(closeVC:popViewController:)]) {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        [self.delegate closeVC:NO popViewController:YES];
    }
}

-(void)clickScreen:(UITapGestureRecognizer *)gestureRecognizer{
    if (self.delegate && [self.delegate respondsToSelector:@selector(clickScreen:)]) {
        [self.delegate clickScreen:gestureRecognizer];
    }
}

-(void)clickPlayVod{
    if (self.delegate && [self.delegate respondsToSelector:@selector(clickPlayVod)]) {
        [self.delegate clickPlayVod];
    }
}

-(void)onSeek:(UISlider *)slider{
    if (self.delegate && [self.delegate respondsToSelector:@selector(onSeek:)]) {
        [self.delegate onSeek:slider];
    }
}

-(void)onSeekBegin:(UISlider *)slider{
    if (self.delegate && [self.delegate respondsToSelector:@selector(onSeekBegin:)]) {
        [self.delegate onSeekBegin:slider];
    }
}

-(void)onSeekEnd:(UISlider *)slider{
    if (self.delegate && [self.delegate respondsToSelector:@selector(onSeekEnd:)]) {
        [self.delegate onSeekEnd:slider];
    }
}


#pragma mark - 滑动隐藏界面UI
-(void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    UITouch *touch = [[event allTouches] anyObject];
    _touchBeginLocation = [touch locationInView:self];
}

-(void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    UITouch *touch = [[event allTouches] anyObject];
    CGPoint location = [touch locationInView:self];
    [self endMove:location.x - _touchBeginLocation.x];
}


-(void)endMove:(CGFloat)moveX{
    [UIView animateWithDuration:0.2 animations:^{
        if(moveX > 10){
            for (UIView *view in self.subviews) {
                if (view != self->_closeBtn) {
                    CGRect rect = view.frame;
                    if (rect.origin.x >= 0 && rect.origin.x < SCREEN_WIDTH) {
                        rect = CGRectOffset(rect, self.width, 0);
                        view.frame = rect;
                        [self resetViewAlpha:view];
                    }
                }
            }
        }else if(moveX < -10){
            for (UIView *view in self.subviews) {
                if (view != self->_closeBtn) {
                    CGRect rect = view.frame;
                    if (rect.origin.x >= SCREEN_WIDTH) {
                        rect = CGRectOffset(rect, -self.width, 0);
                        view.frame = rect;
                        [self resetViewAlpha:view];
                    }
                    
                }
            }
        }
    }];
}

-(void)resetViewAlpha:(UIView *)view{
    CGRect rect = view.frame;
    if (rect.origin.x  >= SCREEN_WIDTH || rect.origin.x < 0) {
        view.alpha = 0;
        _viewsHidden = YES;
    }else{
        view.alpha = 1;
        _viewsHidden = NO;
    }
    if (view == _cover)
        _cover.alpha = 0.5;
}

#pragma mark UIGestureRecognizerDelegate
// 解决小范围滑动slider的时候造成的手势冲突，使得无法触发slider的手势抬起事件
-(BOOL)gestureRecognizer:(UIGestureRecognizer*)gestureRecognizer shouldReceiveTouch:(UITouch*)touch {
    if ([touch.view isKindOfClass:[UISlider class]]) {
        return NO;
    } else {
        return YES;
    }
}

@end



@implementation TCShowLiveTopView
{
    UIImageView          *_hostImage;        // 主播头像
    
    NSInteger            _startTime;
    
    NSString             *_hostNickName;     // 主播昵称
    NSString             *_hostFaceUrl;      // 头像地址
}

- (instancetype)initWithFrame:(CGRect)frame hostNickName:(NSString *)hostNickName hostFaceUrl:(NSString *)hostFaceUrl {
    if (self = [super initWithFrame: frame]) {
        _hostNickName = hostNickName;
        _hostFaceUrl = hostFaceUrl;
        
        self.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.5];
        self.layer.cornerRadius = frame.size.height / 2;
        self.layer.masksToBounds = YES;
        [self initUI];
    }
    return self;
}

- (void)setHostFaceUrl:(NSString *)hostFaceUrl
{
    _hostFaceUrl = hostFaceUrl;
    [_hostImage sd_setImageWithURL:[NSURL URLWithString:[TCUtil transImageURL2HttpsURL:_hostFaceUrl]] placeholderImage:[UIImage imageNamed:@"default_user"]];
}

- (void)cancelImageLoading
{
    [_hostImage sd_setImageWithURL:nil];
}

- (void)initUI {
    CGRect imageFrame = self.bounds;
    imageFrame.origin.x = 1;
    imageFrame.size.height -= 2;
    imageFrame.size.width = imageFrame.size.height;
    _hostImage = [[UIImageView alloc] initWithFrame:imageFrame];
    _hostImage.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    _hostImage.layer.cornerRadius = (imageFrame.size.height - 2) / 2;
    _hostImage.layer.masksToBounds = YES;
    _hostImage.contentMode = UIViewContentModeScaleAspectFill;
    [_hostImage sd_setImageWithURL:[NSURL URLWithString:[TCUtil transImageURL2HttpsURL:_hostFaceUrl]] placeholderImage:[UIImage imageNamed:@"default_user"]];
    [self addSubview:_hostImage];
}

@end
