// Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>
#import <MediaPlayer/MPMediaPickerController.h>
#import <Photos/Photos.h>
#import "UGCKitEditViewController.h"
#import "UGCKitBGMListViewController.h"
#import "SDKHeader.h"
#import "UGCKitPlayerView.h"
#import "UGCKitVideoRangeSlider.h"
#import "UGCKitVideoRangeConst.h"
#import "UGCKitVideoRecordMusicView.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitColorMacro.h"
#import "UGCKitProgressHUD.h"
#import "UGCKitEditBottomMenu.h"
#import "UGCKitVideoCutView.h"
#import "UGCKitPasterAddView.h"
#import "UGCKitEffectSelectView.h"
#import "UGCKitVideoPasterView.h"
#import "UGCKitVideoTextFiled.h"
#import "UGCKitBGMHelper.h"
#import "UGCKitVideoInfo.h"
#import "UGCKitVideoEffectColorPalette.h"
#import "UGCKitTheme.h"
#import "UGCKitResult.h"
#import "UGCKitConstants.h"
#import "UGCKitReporterInternal.h"
#import "UGCKitVideoEffectManager.h"
#import "UGCKitLocalization.h"
#import "SDKHeader.h"
#import "UGCKitMem.h"

typedef  NS_ENUM(NSInteger,TimeType)
{
    TimeType_Clear,
    TimeType_Back,
    TimeType_Repeat,
    TimeType_Speed,
};

typedef NS_ENUM(NSInteger,EffectSelectType)
{
    EffectSelectType_Effect,
    EffectSelectType_Time,
    EffectSelectType_Filter,
    EffectSelectType_Paster,
    EffectSelectType_Text,
};

@interface UGCKitEditViewController ()<TXVideoGenerateListener,VideoPreviewDelegate, VideoCutViewDelegate,UGCKitEffectSelectViewDelegate, UGCKitPasterAddViewDelegate, UGCKitVideoPasterViewDelegate ,VideoTextFieldDelegate ,TCBGMControllerListener,UGCKitVideoRecordMusicViewDelegate,UIActionSheetDelegate, UITabBarDelegate , UIPickerViewDelegate ,UIPickerViewDelegate ,UIAlertViewDelegate>

@end

@implementation UGCKitEditViewController
{
    /// 编辑的视频
    AVAsset *_videoAsset;

    /// 编辑器配置
    UGCKitEditConfig *_config;

    /// 编辑器主题
    UGCKitTheme * _theme;

    UGCKitBGMListViewController *_bgmListVC;
    TXVideoEditer*   _ugcEdit;        //sdk编辑器
    UGCKitPlayerView*    _videoPreview;   //视频预览
    
    //特效View
    UIView*             _effectView;
    
    //cover view
    UIImageView*        _coverImageView;
    
    //背景音
    UGCKitVideoRecordMusicView *_musicView;
    
    //特效确定btn
    UIButton *          _effectConfirmBtn;
    
    UIButton *          _generateCannelBtn;
    
    //生成时的进度浮层
    UIView*             _generationView;
    UIProgressView*     _generateProgressView;
    UILabel*            _generationTitleLabel;
    UILabel*            _timeLabel;
    UIButton*           _deleteBtn;
    UIButton*           _playBtn;

    UGCKitEditBottomMenu*       _bottomMenu;          //底部栏
    UGCKitVideoCutView*       _videoCutView;       //裁剪
    UGCKitPasterAddView*      _pasterAddView;      //贴图
    UGCKitEffectSelectView*   _effectSelectView;   //动效选择
    EffectSelectType    _effectSelectType;
    
    TimeType            _timeType;
    
    NSMutableArray <UGCKitEffectInfo *> *_pasterEffectArray;
    NSMutableArray <UGCKitEffectInfo *> *_textEffectArray;
    NSMutableArray <UGCKitVideoPasterInfo *>* _videoPasterInfoList;
    NSMutableArray <UGCKitVideoTextInfo *>*   _videoTextInfoList;
    NSMutableArray  *_cutPathList;
    
    //裁剪时间
    CGFloat             _duration;
    CGFloat             _playTime;
    CGFloat             _BGMDuration;
    CGFloat             _BGMVolume;
    CGFloat             _videoVolume;
    NSInteger           _effectSelectIndex;
    NSInteger           _effectType;

    NSObject*     _BGMPath;
    NSString*    _videoOutputPath;
    BOOL          _isReverse;
    BOOL          _isSeek;
    BOOL          _isPlay;
    BOOL          _navigationBarHidden;
    BOOL          _isScrollToStart;
    dispatch_queue_t _imageLoadingQueue;
    NSArray<UGCKitEffectInfo*> *_effectList;
    
    // 选中的滤镜与速度，用于恢复状态
    NSInteger _filterIndex;
    NSInteger _timeIndex;
    
    BOOL _isShowingEffectView;
    BOOL _isHidingEffectView;
}


- (instancetype)initWithMedia:(UGCKitMedia *)asset config:(UGCKitEditConfig *)config theme:(UGCKitTheme *)theme
{
    self = [super initWithNibName:nil bundle:nil];
    if (self) {
        _videoAsset = asset.videoAsset;
#ifdef DEBUG
        NSAssert(_videoAsset, @"asset is nil");
#endif
        _config = config ?: [[UGCKitEditConfig alloc] init];
        _theme = theme ?: [UGCKitTheme sharedTheme];
        _generateMode = _config.generateMode;
        _effectType = -1;
        _cutPathList = [NSMutableArray array];
        _videoOutputPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"outputEditCut.mp4"];

        _pasterEffectArray = [NSMutableArray array];
        [_pasterEffectArray addObject:({
            UGCKitEffectInfo * v= [UGCKitEffectInfo new];
            v.name = [_theme localizedString:@"UGCKit.Common.AddNew"];
            v.icon = _theme.editPanelAddPasterIcon;
            v;
        })];

        _textEffectArray = [NSMutableArray array];
        [_textEffectArray addObject:({
            UGCKitEffectInfo * v= [UGCKitEffectInfo new];
            v.name = [_theme localizedString:@"UGCKit.Common.AddNew"];
            v.icon = _theme.editPanelAddPasterIcon;
            v;
        })];

        _videoPasterInfoList = [NSMutableArray array];
        _videoTextInfoList = [NSMutableArray array];
        _BGMVolume = 0.5;
        _videoVolume = 0.5;
        _imageLoadingQueue = dispatch_queue_create("UGCKitVideoEditImageLoading", DISPATCH_QUEUE_CONCURRENT);
    }
    return self;
}

- (BOOL)prefersStatusBarHidden {
    if (@available(iOS 11, *)) {
        if ([UIApplication sharedApplication].keyWindow.safeAreaInsets.bottom > 0) {
            return NO;
        }
    }
    return YES;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    _navigationBarHidden = self.navigationController.navigationBar.hidden;
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)]){
        self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    self.navigationController.navigationBar.hidden = _navigationBarHidden;
    [_videoCutView stopGetImageList];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    if (!_videoPreview.isPlaying) {
        [_videoPreview playVideo];
    }
}

- (void)onVideoEnterBackground
{
    if (_generationView && !_generationView.hidden) {
        [_ugcEdit pauseGenerate];
    }else{
        [UGCKitProgressHUD hideHUDForView:self.view animated:YES];
        [self pausePlay];
    }
}

- (void)onVideoWillEnterForeground
{
    if (_generationView && !_generationView.hidden) {
        [_ugcEdit resumeGenerate];
    }else{
        UIViewController *rootVC = [UIApplication sharedApplication].keyWindow.rootViewController;
        BOOL shouldResume = rootVC == self;
        if ([rootVC isKindOfClass:[UINavigationController class]]) {
            UINavigationController *nav = (UINavigationController *)rootVC;
            if ([[nav viewControllers] lastObject] == self) {
                shouldResume = YES;
            }
        }
        if (shouldResume) {
            [_ugcEdit resumePlay];
            [self setPlayBtn:YES];
            _isPlay = YES;
        }
    }
}


- (void)viewDidLoad {
    [super viewDidLoad];
    UGCKitTheme *theme = _theme;
    __weak __typeof(self) wself = self;
    dispatch_async(_imageLoadingQueue, ^{
        CFTimeInterval start = CFAbsoluteTimeGetCurrent();
        __strong __typeof(wself) self = wself;
        if (self) {
            self->_effectList = [UGCKitVideoEffectManager effectInfosWithTheme:theme];
        }
        CFTimeInterval end = CFAbsoluteTimeGetCurrent();
        NSLog(@"effect load time: %g", end - start);
    });

    self.view.backgroundColor = UIColor.blackColor;
    
    _videoPreview = [[UGCKitPlayerView alloc] initWithFrame:self.view.bounds coverImage:nil theme:_theme];
    _videoPreview.delegate = self;
    [_videoPreview setPlayBtnHidden:YES];
    [self.view addSubview:_videoPreview];

    // 点隐藏工具h面板
    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapToHideBottomPanel:)];
    [_videoPreview addGestureRecognizer:singleTap];
    CGFloat offset = 0;
    if (@available(iOS 11, *)) {
        offset = [UIApplication sharedApplication].keyWindow.safeAreaInsets.bottom;
    }

    // 配置底部菜单
    UGCKitEditBottomMenu *menu = [[UGCKitEditBottomMenu alloc] initWithFrame:CGRectMake(0, self.view.ugckit_height - 62  - offset, self.view.ugckit_width, 62)
                                                               items:nil];
    menu.textColor = _theme.editPanelTextColor ?: [UIColor whiteColor];
//    menu.backgroundColor = _theme.editPanelBackgroundColor;
    [menu addItemWithTitle:[_theme localizedString:@"UGCKit.Edit.Tab.Music"]
                      icon:_theme.editPanelMusicIcon
               highlightedIcon:_theme.editPanelMusicHighlightedIcon
                    action:^{
        [wself onMusicBtnClicked];
    }];
    [menu addItemWithTitle:[_theme localizedString:@"UGCKit.Edit.Tab.Effect"]
                      icon:_theme.editPanelEffectIcon
               highlightedIcon:_theme.editPanelEffectHighlightedIcon
                    action:^{
        [wself onEffectBtnClicked];
    }];
    [menu addItemWithTitle:[_theme localizedString:@"UGCKit.Edit.Tab.Speed"]
                      icon:_theme.editPanelSpeedIcon
               highlightedIcon:_theme.editPanelSpeedHighlightedIcon
                    action:^{
        [wself onTimeBtnClicked];
    }];
    [menu addItemWithTitle:[_theme localizedString:@"UGCKit.Edit.Tab.Filter"]
                      icon:_theme.editPanelFilterIcon
               highlightedIcon:_theme.editPanelFilterHighlightedIcon
                    action:^{
        [wself onFilterBtnClicked];
    }];

    [menu addItemWithTitle:[_theme localizedString:@"UGCKit.Edit.Tab.Paster"]
                      icon:_theme.editPanelPasterIcon
               highlightedIcon:_theme.editPanelPasterHighlightedIcon
                    action:^{
        [wself onPasterBtnClicked];
    }];
    [menu addItemWithTitle:[_theme localizedString:@"UGCKit.Edit.Tab.Subtitle"]
                      icon:_theme.editPanelSubtitleIcon
               highlightedIcon:_theme.editPanelSubtitleHighlightedIcon
                    action:^{
        [wself onTextBtnClicked];
    }];
    _bottomMenu = menu;
    [self.view addSubview:_bottomMenu];

    CGFloat top = [UIApplication sharedApplication].statusBarFrame.size.height + 5;
    // 特效取消及后退按钮
    UIButton *backBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    backBtn.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin;
    [backBtn setImage:_theme.backIcon forState:UIControlStateNormal];
    backBtn.frame = CGRectMake(10, top, 50, 44);
    [backBtn addTarget:self action:@selector(onTapCloseButton:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:backBtn];
    
    CGFloat btnConfirmWidth = 70;
    CGFloat btnConfirmHeight = 30;
    _effectConfirmBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_effectConfirmBtn setTitle:[_theme localizedString:@"UGCKit.Common.Done"] forState:UIControlStateNormal];
    _effectConfirmBtn.titleLabel.font = [UIFont systemFontOfSize:14];
    [_effectConfirmBtn setBackgroundImage:_theme.nextIcon forState:UIControlStateNormal];
    _effectConfirmBtn.frame = CGRectMake(CGRectGetWidth(self.view.bounds) - 15 - btnConfirmWidth, CGRectGetMidY(backBtn.frame)-btnConfirmHeight/2,
                                         btnConfirmWidth, btnConfirmHeight);
    _effectConfirmBtn.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleBottomMargin;;
    [_effectConfirmBtn addTarget:self action:@selector(goFinish) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_effectConfirmBtn];
    
    _coverImageView = [[UIImageView alloc] initWithFrame:_videoPreview.frame];
    _coverImageView.hidden = YES;
    _coverImageView.contentMode = UIViewContentModeScaleAspectFit;
    [self.view addSubview:_coverImageView];

    // 特效容器
    _effectView= [[UIView alloc] initWithFrame:CGRectMake(0, self.view.ugckit_height, self.view.ugckit_width, 205 * kScaleY)];
    [self.view addSubview:_effectView];
    
    _timeLabel = [[UILabel alloc] initWithFrame:CGRectMake(15 * kScaleX, 0, 40, 54)];
    _timeLabel.textColor = _theme.titleColor;
    _timeLabel.text = LocalizationNotNeeded(@"00:00");
    _timeLabel.font = [UIFont systemFontOfSize:14];
    _timeLabel.textColor = _theme.titleColor;
    [_effectView addSubview:_timeLabel];
    
    _playBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_playBtn setBackgroundImage:_theme.editPlayIcon forState:UIControlStateNormal];
    [_playBtn setBackgroundImage:_theme.editPlayHighlightedIcon forState:UIControlStateHighlighted];
    _playBtn.frame = CGRectMake(self.view.ugckit_width / 2 - 15, 10 * kScaleY, 30, 30);
    [_playBtn addTarget:self action:@selector(onPlayVideo) forControlEvents:UIControlEventTouchUpInside];
    [_effectView addSubview:_playBtn];
    
    _deleteBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_deleteBtn setBackgroundImage:_theme.editPanelDeleteIcon forState:UIControlStateNormal];
    [_deleteBtn setBackgroundImage:_theme.editPanelDeleteHighlightedIcon forState:UIControlStateHighlighted];
    _deleteBtn.frame = CGRectMake(self.view.ugckit_width - 15 * kScaleX - 30, 10 * kScaleY, 30, 30);
    [_deleteBtn addTarget:self action:@selector(onDeleteEffect) forControlEvents:UIControlEventTouchUpInside];
    [_effectView addSubview:_deleteBtn];

    CGFloat cutViewHeight = 34 * kScaleY;
    UGCKitRangeContentConfig *config = [[UGCKitRangeContentConfig alloc] initWithTheme:_theme];
    config.pinWidth = PIN_WIDTH;
    config.thumbHeight = cutViewHeight;
    config.borderHeight = 0;
    config.imageCount = 20;
    _videoCutView = [[UGCKitVideoCutView alloc] initWithFrame:CGRectMake(0,_timeLabel.ugckit_bottom + 3, _effectView.ugckit_width,cutViewHeight) videoPath:nil videoAsset:_videoAsset config:config];
    _videoCutView.delegate = self;
    [_videoCutView setCenterPanHidden:YES];
    [_effectView addSubview:_videoCutView];
    
    UIImageView *flagView = [[UIImageView alloc] initWithFrame:CGRectMake(self.view.ugckit_width / 2 - 6, _timeLabel.ugckit_bottom, 12, 48)];
    flagView.image = _theme.editTimelineIndicatorIcon;
    [_effectView addSubview:flagView];
    
    _effectSelectView = [[UGCKitEffectSelectView alloc] initWithFrame:CGRectMake(0, _videoCutView.ugckit_bottom + 24 * kScaleY,_effectView.ugckit_width,70 * kScaleY)];
    _effectSelectView.delegate = self;
    _effectSelectView.hidden = NO;
    [_effectView addSubview:_effectSelectView];
    
    _pasterAddView = [[UGCKitPasterAddView alloc] initWithFrame:CGRectMake(0,self.view.ugckit_height - 205 * kScaleY, self.view.ugckit_width,205 * kScaleY) theme:_theme];
    _pasterAddView.delegate = self;
    _pasterAddView.hidden = YES;
    [self.view addSubview:_pasterAddView];
    
    _musicView = [[UGCKitVideoRecordMusicView alloc] initWithFrame:CGRectMake(0, self.view.ugckit_bottom - 268 * kScaleY, self.view.ugckit_width, 268 * kScaleY) needEffect:NO theme:_theme];
    _musicView.delegate = self;
    _musicView.hidden = YES;
    [self.view addSubview:_musicView];
    
    _bgmListVC = [[UGCKitBGMListViewController alloc] initWithTheme:_theme];
    [_bgmListVC setBGMControllerListener:self];

    [self initVideoEditor];
}

- (void)initVideoEditor
{
    TXVideoInfo *videoMsg = [TXVideoInfoReader getVideoInfoWithAsset:_videoAsset];
    _duration = videoMsg.duration;
    
    TXPreviewParam *param = [[TXPreviewParam alloc] init];
    param.videoView = _videoPreview.renderView;
    param.renderMode = PREVIEW_RENDER_MODE_FILL_EDGE;
    _ugcEdit = [[TXVideoEditer alloc] initWithPreview:param];
    _ugcEdit.generateDelegate = self;
    _ugcEdit.previewDelegate = _videoPreview;
    
    [_ugcEdit setVideoAsset:_videoAsset];
    [_ugcEdit setRenderRotation:(int)_config.rotation * 90];
//    UIImage *waterimage = [UIImage imageNamed:@"watermark"];
//    [_ugcEdit setWaterMark:waterimage normalizationFrame:CGRectMake(0.01, 0.01, 0.3 , 0)];

    [_ugcEdit setTailWaterMark:_config.tailWatermark.image normalizationFrame:_config.tailWatermark.frame  duration:_config.tailWatermark.duration];
}

- (UIView*)generatingView
{
    /*用作生成时的提示浮层*/
    if (!_generationView) {
        _generationView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.ugckit_width, self.view.ugckit_height + 64)];
        _generationView.backgroundColor = UIColor.blackColor;
        _generationView.alpha = 0.9f;
        
        _generateProgressView = [UIProgressView new];
        _generateProgressView.center = CGPointMake(_generationView.ugckit_width / 2, _generationView.ugckit_height / 2);
        _generateProgressView.bounds = CGRectMake(0, 0, 225, 20);
        _generateProgressView.progressTintColor = RGB(238, 100, 85);
        [_generateProgressView setTrackImage:_theme.progressTrackImage];
        //_generateProgressView.trackTintColor = UIColor.whiteColor;
        //_generateProgressView.transform = CGAffineTransformMakeScale(1.0, 2.0);
        
        _generationTitleLabel = [UILabel new];
        _generationTitleLabel.font = [UIFont systemFontOfSize:14];
        _generationTitleLabel.text = [_theme localizedString:@"UGCKit.Edit.VideoGenerating"];
        _generationTitleLabel.textColor = UIColor.whiteColor;
        _generationTitleLabel.textAlignment = NSTextAlignmentCenter;
        _generationTitleLabel.frame = CGRectMake(0, _generateProgressView.ugckit_y - 34, _generationView.ugckit_width, 14);
        
        _generateCannelBtn = [UIButton new];
        [_generateCannelBtn setImage:_theme.closeIcon forState:UIControlStateNormal];
        _generateCannelBtn.frame = CGRectMake(_generateProgressView.ugckit_right + 15, _generationTitleLabel.ugckit_bottom + 10, 20, 20);
        [_generateCannelBtn addTarget:self action:@selector(onCancel:) forControlEvents:UIControlEventTouchUpInside];
        
        [_generationView addSubview:_generationTitleLabel];
        [_generationView addSubview:_generateProgressView];
        [_generationView addSubview:_generateCannelBtn];
        [[[UIApplication sharedApplication] delegate].window addSubview:_generationView];
    }
    
    _generateProgressView.progress = 0.f;
    return _generationView;
}

-(void)onPlayVideo
{
    if (_isPlay) {
        [self pausePlay];
    }else{
        CGFloat currentPos = _videoCutView.videoRangeSlider.currentPos;
        if(_isReverse && currentPos != 0){
            [self startPlayFromTime:0 toTime:currentPos];
        }else{
            [self startPlayFromTime:currentPos toTime:_duration];
        }
        if (_effectSelectType == EffectSelectType_Paster) {
            [self removeAllPasterViewFromSuperView];
            [self setVideoPastersToSDK];
        }
        if (_effectSelectType == EffectSelectType_Text) {
            [self removeAllTextFieldFromSuperView];
            [self setVideoSubtitlesToSDK];
        }
    }
}

- (void)setPlayBtn:(BOOL)isPlay
{
    if (isPlay) {
        [_playBtn setBackgroundImage:_theme.editPauseIcon forState:UIControlStateNormal];
        [_playBtn setBackgroundImage:_theme.editPauseHighlightedIcon forState:UIControlStateHighlighted];
    }else{
        [_playBtn setBackgroundImage:_theme.editPlayIcon forState:UIControlStateNormal];
        [_playBtn setBackgroundImage:_theme.editPlayHighlightedIcon forState:UIControlStateHighlighted];
    }
}

- (void)onTapToHideBottomPanel:(UITapGestureRecognizer*)recognizer
{
    CGPoint tapPoint = [recognizer locationInView:recognizer.view];
    if (_bottomMenu.isHidden && _musicView.hidden) {
        BOOL findEffect = NO;
        if (_effectSelectType == EffectSelectType_Paster) {
            for (NSInteger i = 0; i < _videoPasterInfoList.count; i++) {
                CGRect pasterFrame = [_videoPasterInfoList[i].pasterView pasterFrameOnView:recognizer.view];
                if (CGRectContainsPoint(pasterFrame, tapPoint)) {
                    UGCKitVideoPasterInfo *info = _videoPasterInfoList[i];
                    if (_playTime >= info.startTime && _playTime <= info.endTime) {
                        [self removeAllPasterViewFromSuperView];
                        [_videoPreview addSubview:info.pasterView];
                        [self setVideoPastersToSDK];
                        findEffect = YES;
                        break;
                    }
                }
            }
        }
        else if (_effectSelectType == EffectSelectType_Text){
            for (NSInteger i = 0; i < _videoTextInfoList.count; i++) {
                CGRect textFrame = [_videoTextInfoList[i].textField textFrameOnView:recognizer.view];
                if (CGRectContainsPoint(textFrame, tapPoint)) {
                    UGCKitVideoTextInfo *info = _videoTextInfoList[i];
                    if (_playTime >= info.startTime && _playTime <= info.endTime){
                        [self removeAllTextFieldFromSuperView];
                        [_videoPreview addSubview:info.textField];
                        [self setVideoSubtitlesToSDK];
                        findEffect = YES;
                        break;
                    }
                }
            }
        }
        if (findEffect) {
            [_ugcEdit previewAtTime:_playTime];
            [self pausePlay];
        }
    }else{
        _musicView.hidden = YES;
        _bottomMenu.hidden = NO;
    }
}

- (void)resetConfirmBtn
{
    CGFloat y = CGRectGetMinY(_effectConfirmBtn.frame);
    if(_bottomMenu.isHidden){
        [_effectConfirmBtn setTitle:@"" forState:UIControlStateNormal];
        [_effectConfirmBtn setBackgroundImage:_theme.confirmIcon forState:UIControlStateNormal];
        [_effectConfirmBtn setBackgroundImage:_theme.confirmHighlightedIcon forState:UIControlStateHighlighted];
        _effectConfirmBtn.frame = CGRectMake(self.view.ugckit_width - 15 * kScaleX - 44, y, 44, 30);
    }else{
        [_effectConfirmBtn setTitle:[_theme localizedString:@"UGCKit.Common.Done"] forState:UIControlStateNormal];
        _effectConfirmBtn.titleLabel.font = [UIFont systemFontOfSize:14];
        [_effectConfirmBtn setBackgroundImage:_theme.nextIcon forState:UIControlStateNormal];
        _effectConfirmBtn.frame = CGRectMake(self.view.ugckit_width - 15 * kScaleX - 70, y, 70, 30);
    }
}

- (void)onTapCloseButton:(id)sender {
    if (_bottomMenu.hidden) {
        [self cancelEdit];
    } else {
        [self goBack];
    }
}
/// 取消编辑状态
- (void)cancelEdit {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:[_theme localizedString:@"UGCKit.Edit.VideoEffect.AbandonEdit"]
                                                                        message:nil
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.OK"]
                                                   style:UIAlertActionStyleDestructive
                                                 handler:^(UIAlertAction * _Nonnull action) {
        self->_musicView.hidden = YES;
        [self clearEffect];
        [self onHideEffectView];
        [self resetConfirmBtn];
        // 清除背景音乐
        [self->_ugcEdit setBGM:nil result:nil];
    }]];
    [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.Cancel"]
                                                   style:UIAlertActionStyleCancel
                                                 handler:nil]];

    [self presentViewController:controller animated:YES completion:nil];
}

- (void)goBack {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:[_theme localizedString:@"UGCKit.Edit.VideoEffect.AbandonEdit"]
                                                                        message:nil
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.OK"]
                                                   style:UIAlertActionStyleDestructive
                                                 handler:^(UIAlertAction * _Nonnull action) {
        [self->_ugcEdit stopPlay];
        [self setPlayBtn:NO];
        if (self.completion) {
            self.completion([UGCKitResult cancelledResult]);
        }
    }]];
    [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.Cancel"]
                                                   style:UIAlertActionStyleCancel
                                                 handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];
}

- (void)goFinish
{
    if (_bottomMenu.hidden) {
        if (_effectSelectType == EffectSelectType_Paster) {
            [self removeAllPasterViewFromSuperView];
            [self setVideoPastersToSDK];
        }
        if (_effectSelectType == EffectSelectType_Text) {
            [self removeAllTextFieldFromSuperView];
            [self setVideoSubtitlesToSDK];
        }
        _bottomMenu.hidden = NO;
        _musicView.hidden = YES;
        [self onHideEffectView];
        [self resetConfirmBtn];
    }else{
        if (self.onTapNextButton) {
            __weak __typeof(self) wself = self;
            self.onTapNextButton(^(BOOL shouldSave){
                if (shouldSave) {
                    [wself generateVideo];
                }
            });
        }
    }
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (void)onCancel:(UIButton*)sender
{
    _generationView.hidden = YES;
    [_ugcEdit cancelGenerate];
    [self startPlayFromTime:0 toTime:_duration];
}

- (void)onSelectMusic
{
    if (_BGMPath) {
        _musicView.hidden = !_musicView.hidden;
    }else{
        [self resetVideoProgress];
        UINavigationController *nv = [[UINavigationController alloc] initWithRootViewController:_bgmListVC];
        [nv.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
        nv.navigationBar.barTintColor = RGB(25, 29, 38);
        nv.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:nv animated:YES completion:nil];
        [_bgmListVC loadBGMList];
        [_bgmListVC clearSelectStatus];
    }
}

/// 特效入口点击事件响应函数
- (void)onShowEffectView
{
    _isHidingEffectView = NO;
    _isShowingEffectView = YES;
    [_effectView.layer removeAllAnimations];
    [_coverImageView.layer removeAllAnimations];
    
    [self resetVideoProgress];
    _coverImageView.hidden = NO;
    _coverImageView.image = [TXVideoInfoReader getSampleImage:_playTime videoAsset:_videoAsset];
    _videoPreview.hidden   = YES;
    
    [UIView animateWithDuration:0.3 animations:^{
        self->_coverImageView.frame = CGRectMake(0, 54 * kScaleY, self.view.ugckit_width, 410 * kScaleY);
        self->_effectView.frame = CGRectMake(0, self.view.ugckit_height - 205 * kScaleY, self->_effectView.ugckit_width, self->_effectView.ugckit_height);
    } completion:^(BOOL finished) {
        self->_videoPreview.frame = self->_coverImageView.frame;
        if (self->_isShowingEffectView) {
            self->_bottomMenu.hidden = YES;
        }
    }];
    
    WEAKIFY(self);
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.6 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        STRONGIFY_OR_RETURN(self);
        if (self->_isShowingEffectView) {
            self->_isShowingEffectView = NO;
            self->_videoPreview.hidden = NO;
            self->_coverImageView.hidden = YES;
        }
    });
}

- (void)onHideEffectView
{
    _isHidingEffectView = YES;
    _isShowingEffectView = NO;
    [_effectView.layer removeAllAnimations];
    [_coverImageView.layer removeAllAnimations];
    
    _coverImageView.hidden = NO;
    _coverImageView.image =  [TXVideoInfoReader getSampleImage:_playTime videoAsset:_videoAsset];
    _videoPreview.hidden = YES;
    _bottomMenu.hidden = NO;
    
    [UIView animateWithDuration:0.3 animations:^{
        self->_coverImageView.frame = CGRectMake(0, 0, self.view.ugckit_width,self.view.ugckit_height);
        self->_effectView.frame = CGRectMake(0, self.view.ugckit_height, self->_effectView.ugckit_width, self->_effectView.ugckit_height);
    } completion:^(BOOL finished) {
        self->_videoPreview.frame = self->_coverImageView.frame;
        [self startPlayFromTime:0 toTime:self->_duration];
    }];
    
    WEAKIFY(self);
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.6 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        STRONGIFY_OR_RETURN(self);
        if (self->_isHidingEffectView) {
            self->_isHidingEffectView = NO;
            self->_videoPreview.hidden = NO;
            self->_coverImageView.hidden = YES;
        }
    });
}

-(void)onDeleteEffect
{
    CGFloat endTime = 0;
    if (_effectSelectType == EffectSelectType_Effect) {
        UGCKitVideoColorInfo *info = [_videoCutView removeLastColoration:UGCKitRangeColorType_Effect];
        if (info) {
            float time = _isReverse ? MAX(info.endPos, info.startPos) : MIN(info.endPos, info.startPos);
            [_videoCutView setPlayTime:time];
            _playTime = time;
        }
        [_ugcEdit deleteLastEffect];
    }
    else if (_effectSelectType == EffectSelectType_Paster){
        if (_pasterEffectArray.count <= 1) {
            return;
        }
        UGCKitVideoPasterInfo *info = [_videoPasterInfoList lastObject];
        [info.pasterView removeFromSuperview];
        [_videoPasterInfoList removeLastObject];
        [_pasterEffectArray removeObjectAtIndex:_pasterEffectArray.count - 2];
        [_effectSelectView setEffectList:_pasterEffectArray];
        [_videoCutView removeLastColoration:UGCKitRangeColorType_Paster];
        if (_videoPasterInfoList.count > 0) {
            UGCKitVideoPasterInfo *info = [_videoPasterInfoList lastObject];
            [self setLeftPanFrame:info.startTime rightPanFrame:info.endTime];
            endTime = info.endTime;
        }else{
            [self setLeftPanFrame:0 rightPanFrame:0];
            endTime = 0;
        }
        [self setVideoPastersToSDK];
        [_ugcEdit previewAtTime:endTime];
    }
    else if (_effectSelectType == EffectSelectType_Text){
        if (_textEffectArray.count <= 1) {
            return;
        }
        UGCKitVideoTextInfo *info = [_videoTextInfoList lastObject];
        [info.textField removeFromSuperview];
        [_videoTextInfoList removeLastObject];
        [_textEffectArray removeObjectAtIndex:_textEffectArray.count - 2];
        [_effectSelectView setEffectList:_textEffectArray];
        [_videoCutView removeLastColoration:UGCKitRangeColorType_Text];
        if (_videoTextInfoList.count > 0) {
            UGCKitVideoTextInfo *info = [_videoTextInfoList lastObject];
            [self setLeftPanFrame:info.startTime rightPanFrame:info.endTime];
            endTime = info.endTime;
        }else{
            [self setLeftPanFrame:0 rightPanFrame:0];
            endTime = 0;
        }
        [self setVideoSubtitlesToSDK];
        [_ugcEdit previewAtTime:endTime];
    }
}

- (void)removeAllPasterViewFromSuperView
{
    for (UGCKitVideoPasterInfo* pasterInfo in _videoPasterInfoList) {
        [pasterInfo.pasterView removeFromSuperview];
    }
}

- (void)removeAllTextFieldFromSuperView
{
    for (UGCKitVideoTextInfo* textInfo in _videoTextInfoList) {
        [textInfo.textField removeFromSuperview];
    }
}

- (void)removeCurrentPasterInfo
{
    if (_effectSelectIndex >= _videoPasterInfoList.count
        ||  _effectSelectIndex >= _pasterEffectArray.count - 1
        || _effectSelectIndex < 0) {
        return;
    }
    [_videoPasterInfoList removeObjectAtIndex:_effectSelectIndex];
    [_pasterEffectArray removeObjectAtIndex:_effectSelectIndex];
    [_effectSelectView setEffectList:_pasterEffectArray];
    [_videoCutView removeColoration:UGCKitRangeColorType_Paster index:_effectSelectIndex];
    
    if (_videoPasterInfoList.count > 0) {
        UGCKitVideoPasterInfo *info = [_videoPasterInfoList lastObject];
        [self setLeftPanFrame:info.startTime rightPanFrame:info.endTime];
    }else{
        [self setLeftPanFrame:0 rightPanFrame:0];
    }
    _effectSelectIndex = _pasterEffectArray.count - 2;
    [self setVideoPastersToSDK];
}


- (void)removeCurrentTextInfo
{
    if (_effectSelectIndex >= _videoTextInfoList.count
        ||  _effectSelectIndex >= _textEffectArray.count - 1
        || _effectSelectIndex < 0) {
        return;
    }
    [_videoTextInfoList removeObjectAtIndex:_effectSelectIndex];
    [_textEffectArray removeObjectAtIndex:_effectSelectIndex];
    [_effectSelectView setEffectList:_textEffectArray];
    [_videoCutView removeColoration:UGCKitRangeColorType_Text index:_effectSelectIndex];
    
    if (_videoTextInfoList.count > 0) {
        UGCKitVideoTextInfo *info = [_videoTextInfoList lastObject];
        [self setLeftPanFrame:info.startTime rightPanFrame:info.endTime];
    }else{
        [self setLeftPanFrame:0 rightPanFrame:0];
    }
    _effectSelectIndex = _textEffectArray.count - 2;
    [self setVideoSubtitlesToSDK];
}


- (CGFloat)getLastPasterEndTime
{
    if (_videoPasterInfoList.count > 0) {
        return [_videoPasterInfoList lastObject].endTime;
    }
    return 0;
}

- (CGFloat)getLastTextEndTime
{
    if (_videoTextInfoList.count > 0) {
        return [_videoTextInfoList lastObject].endTime;
    }
    return 0;
}

- (void)clearEffect
{
    switch (_effectSelectType) {
        case EffectSelectType_Effect:
            break;
        case EffectSelectType_Time:
        {
            [_ugcEdit setSpeedList:nil];
            [_ugcEdit setReverse:NO];
            [_ugcEdit setRepeatPlay:nil];
            [_videoCutView setCenterPanHidden:YES];
            _timeIndex = 0;
        }
            break;
        case EffectSelectType_Filter:
        {
            [_ugcEdit setFilter:nil];
            _filterIndex = 0;
        }
            break;
        case EffectSelectType_Paster:
        {
            NSInteger i = _videoPasterInfoList.count;
            while (i > 0) {
                [_videoCutView removeLastColoration:UGCKitRangeColorType_Paster];
                i -- ;
            }
            [self removeAllPasterViewFromSuperView];
            [_videoPasterInfoList removeAllObjects];
            [_pasterEffectArray removeObjectsInRange:NSMakeRange(0, _pasterEffectArray.count - 1)];
            [_ugcEdit setPasterList:nil];
            [_ugcEdit setAnimatedPasterList:nil];
        }
            break;
        case EffectSelectType_Text:
        {
            NSInteger i = _videoTextInfoList.count;
            while (i > 0) {
                [_videoCutView removeLastColoration:UGCKitRangeColorType_Text];
                i -- ;
            }
            [self removeAllTextFieldFromSuperView];
            [_videoTextInfoList removeAllObjects];
            [_textEffectArray removeObjectsInRange:NSMakeRange(0, _textEffectArray.count - 1)];
            [_ugcEdit setSubtitleList:nil];
        }
            break;
        default:
            break;
    }
}

- (void)resetVideoProgress
{
    _playTime = 0;
    _isSeek = YES;
    _isPlay = NO;
    _timeLabel.text = LocalizationNotNeeded(@"00:00");
    [_ugcEdit previewAtTime:_playTime];
    [self setPlayBtn:NO];
}

//设置特效选中区间
- (void)setLeftPanFrame:(CGFloat)leftTime rightPanFrame:(CGFloat)rightTime
{
    if (leftTime == 0 && rightTime == 0) {
        [_videoCutView setLeftPanHidden:YES];
        [_videoCutView setRightPanHidden:YES];
        [_videoCutView setLeftPanFrame:0];
        [_videoCutView setRightPanFrame:0];
        [_videoCutView setPlayTime:0];
    }else{
        [_videoCutView setLeftPanHidden:NO];
        [_videoCutView setRightPanHidden:NO];
        [_videoCutView setLeftPanFrame:leftTime];
        [_videoCutView setRightPanFrame:rightTime];
        [_videoCutView setPlayTime:leftTime];
    }
}

- (void)startPlayFromTime:(CGFloat)startTime toTime:(CGFloat)endTime
{
    [_ugcEdit startPlayFromTime:startTime toTime:endTime];
    _isSeek = NO;
    _isPlay = YES;
    [self setPlayBtn:YES];
}

- (void)pausePlay
{
    [_ugcEdit pausePlay];
    [self setPlayBtn:NO];
    _isPlay = NO;
}

#pragma mark - To SDK

- (void)generateVideo
{
    //当不支持二次编码时，弹出提示
    if (self.generateMode == UGCKitGenerateModeTwoPass && ![_ugcEdit supportsTwoPassEncoding]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                        message:[_theme localizedString:@"UGCKit.Common.NotSupportTwopass"]
                                                       delegate:nil
                                              cancelButtonTitle:[_theme localizedString:@"UGCKit.Common.NotSupportTwopassOK"]
                                              otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    [self pausePlay];
    [self confirmGenerateVideo];
}

- (void)confirmGenerateVideo
{
    [_videoPreview stopObservingAudioNotification];
    _generationView = [self generatingView];
    _generationView.hidden = NO;
    _generateCannelBtn.hidden = NO;
    [_ugcEdit setCutFromTime:0 toTime:_duration];
    if (_config.videoBitrate > 0) {
        [_ugcEdit setVideoBitrate:_config.videoBitrate];
    }
    if (self.generateMode == UGCKitGenerateModeTwoPass) {
        [_ugcEdit generateVideoWithTwoPass:_config.compressResolution videoOutputPath:_videoOutputPath];
    } else {
        [_ugcEdit generateVideo:_config.compressResolution videoOutputPath:_videoOutputPath];
    }
}

//设置贴纸（静态/动态贴纸）
- (void)setVideoPastersToSDK
{
    NSMutableArray* animatePasters = [NSMutableArray new];
    NSMutableArray* staticPasters = [NSMutableArray new];
    for (UGCKitVideoPasterInfo* pasterInfo in _videoPasterInfoList) {
        if ([_videoPreview.subviews containsObject:pasterInfo.pasterView]) {
            continue;
        }
        if (pasterInfo.pasterInfoType == UGCKitPasterInfoType_Animate) {
            TXAnimatedPaster* paster = [TXAnimatedPaster new];
            paster.startTime = pasterInfo.startTime;
            paster.endTime = pasterInfo.endTime;
            paster.frame = [pasterInfo.pasterView pasterFrameOnView:_videoPreview];
            paster.rotateAngle = pasterInfo.pasterView.rotateAngle * 180 / M_PI;
            paster.animatedPasterpath = pasterInfo.path;
            [animatePasters addObject:paster];
        }
        else if (pasterInfo.pasterInfoType == UGCKitPasterInfoType_static){
            TXPaster *paster = [TXPaster new];
            paster.startTime = pasterInfo.startTime;
            paster.endTime = pasterInfo.endTime;
            paster.frame = [pasterInfo.pasterView pasterFrameOnView:_videoPreview];
            paster.pasterImage = pasterInfo.pasterView.staticImage;
            [staticPasters addObject:paster];
        }
    }
    [_ugcEdit setAnimatedPasterList:animatePasters];
    [_ugcEdit setPasterList:staticPasters];
}

//设置字幕(气泡)
- (void)setVideoSubtitlesToSDK
{
    NSMutableArray* subtitles = [NSMutableArray new];
    NSMutableArray<UGCKitVideoTextInfo*>* emptyVideoTexts;
    for (UGCKitVideoTextInfo* textInfo in _videoTextInfoList) {
        if (textInfo.textField.text.length < 1) {
            [emptyVideoTexts addObject:textInfo];
            continue;
        }
        if ([_videoPreview.subviews containsObject:textInfo.textField]) {
            continue;
        }
        
        TXSubtitle* subtitle = [TXSubtitle new];
        subtitle.titleImage = textInfo.textField.textImage;
        subtitle.frame = [textInfo.textField textFrameOnView:_videoPreview];
        subtitle.startTime = textInfo.startTime;
        subtitle.endTime = textInfo.endTime;
        [subtitles addObject:subtitle];
    }
    [_ugcEdit setSubtitleList:subtitles];
}


- (void)setFilter:(NSInteger)index
{
    _filterIndex = index;
    if (index == 0) {
        [_ugcEdit setFilter:nil];
    } else {
        TCFilter *filter = [TCFilterManager defaultManager].allFilters[index-1];
        UIImage *image = [UIImage imageWithContentsOfFile:filter.lookupImagePath];
        [_ugcEdit setFilter:image];

    }
}

#pragma mark VideoPreviewDelegate
- (void)onVideoPlay
{
    [self startPlayFromTime:0 toTime:_duration];
}

- (void)onVideoPlayProgress:(CGFloat)time
{
    if (!_isSeek) {
        _playTime = time;
        [_videoCutView setPlayTime:_playTime];
        _timeLabel.text = [NSString stringWithFormat:@"%02d:%02d",(int)_playTime / 60 , (int)_playTime % 60];
    }
}

- (void)onVideoPlayFinished
{
    if (_effectType != -1) {
        [self onEffectBtnEndSelect:nil];
    }else{
        [self startPlayFromTime:0 toTime:_duration];
    }
}

#pragma mark TXVideoGenerateDelegate
-(void) onGenerateProgress:(float)progress
{
    _generateProgressView.progress = progress;
}

-(void) onGenerateComplete:(TXGenerateResult *)result
{
    _generationView.hidden = YES;
    [UGCKitReporter report:UGCKitReportItem_videoedit userName:nil code:result.retCode msg:result.descMsg];
    [_ugcEdit stopPlay];
    if (self.completion) {
        UGCKitResult *r = [[UGCKitResult alloc] init];
        if (result.retCode == 0) {
            r.media = [UGCKitMedia mediaWithVideoPath:self->_videoOutputPath];
        } else {
            r.code = result.retCode;
            r.info = @{NSLocalizedDescriptionKey: result.descMsg};
        }
        self.completion(r);
    }
}

#pragma mark - TXVideoPublishListener
- (void)dismissViewController
{
    [_ugcEdit stopPlay];
    if (self.completion) {
        UGCKitResult *result = [[UGCKitResult alloc] init];
        result.media = [UGCKitMedia mediaWithVideoPath:_videoOutputPath];
        self.completion(result);
    }
    //缓存视频状态置nil
    [[NSUserDefaults standardUserDefaults] setObject:nil forKey:CACHE_PATH_LIST];
}

#pragma mark - Menu Actions
- (void)onMusicBtnClicked
{
    _bottomMenu.hidden = YES;
    [self onSelectMusic];
    [self setLeftPanFrame:0 rightPanFrame:0];
    [self resetConfirmBtn];
}

- (void)onEffectBtnClicked
{
    _bottomMenu.hidden = YES;
    _deleteBtn.hidden = NO;
    [self resetConfirmBtn];
    [self onShowEffectView];
    [self removeAllTextFieldFromSuperView];
    [self removeAllPasterViewFromSuperView];
    [self setLeftPanFrame:0 rightPanFrame:0];
    _effectSelectType = EffectSelectType_Effect;
    [_videoCutView setColorType:UGCKitRangeColorType_Effect];
    [_videoCutView setCenterPanHidden:YES];
    __block NSArray <UGCKitEffectInfo *> *effectArray = nil;
    dispatch_barrier_sync(_imageLoadingQueue, ^{
        effectArray = _effectList;
    });
    [_effectSelectView setEffectList:effectArray momentary:YES];
}

-(void)onTimeBtnClicked
{
    _bottomMenu.hidden = YES;
    _deleteBtn.hidden = YES;
    [self resetConfirmBtn];
    [self onShowEffectView];
    [self removeAllTextFieldFromSuperView];
    [self removeAllPasterViewFromSuperView];
    [self setLeftPanFrame:0 rightPanFrame:0];
    _effectSelectType = EffectSelectType_Time;
    [_videoCutView setColorType:UGCKitRangeColorType_Time];

//    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        NSMutableArray <UGCKitEffectInfo *> *effectArray = [NSMutableArray array];
        [effectArray addObject:({
            UGCKitEffectInfo * v= [UGCKitEffectInfo new];
            v.name = [_theme localizedString:@"UGCKit.Common.None"];
            v.icon = _theme.editTimeEffectNormalIcon;
            v.selectIcon = _theme.editFilterSelectionIcon;
            v;
        })];
        [effectArray addObject:({
            UGCKitEffectInfo * v= [UGCKitEffectInfo new];
            v.name = [_theme localizedString:@"UGCKit.Edit.VideoEffect.TimeEffect.BackInTime"];
            v.icon = _theme.editTimeEffectReveseIcon;
            v.selectIcon = _theme.editFilterSelectionIcon;
            v;
        })];
        [effectArray addObject:({
            UGCKitEffectInfo * v= [UGCKitEffectInfo new];
            v.name = [_theme localizedString:@"UGCKit.Edit.VideoEffect.TimeEffect.Repeat"];
            v.icon = _theme.editTimeEffectRepeatIcon;
            v.selectIcon = _theme.editFilterSelectionIcon;
            v;
        })];
        [effectArray addObject:({
            UGCKitEffectInfo * v= [UGCKitEffectInfo new];
            v.name = [_theme localizedString:@"UGCKit.Edit.VideoEffect.TimeEffect.SlowMotion"];
            v.icon = _theme.editTimeEffectSlowMotionIcon;
            v.selectIcon = _theme.editFilterSelectionIcon;
            v;
        })];
//        dispatch_async(dispatch_get_main_queue(), ^{
            [_effectSelectView setEffectList:effectArray];
            _effectSelectView.selectedIndex = _timeIndex;
//        });
//    });
}

- (void)onFilterBtnClicked
{
    _bottomMenu.hidden = YES;
    _deleteBtn.hidden = YES;
    [self resetConfirmBtn];
    [self onShowEffectView];
    [self setLeftPanFrame:0 rightPanFrame:0];

    NSArray<TCFilter *> *filters = [[TCFilterManager defaultManager] allFilters];
    NSMutableArray <UGCKitEffectInfo *> *effectArray = [NSMutableArray arrayWithCapacity:filters.count + 1];
    UGCKitEffectInfo *info = [[UGCKitEffectInfo alloc]init];
    info.name = [_theme localizedString:@"UGCKit.Common.None"];
    info.icon = [_theme iconForFilter:nil];
    info.selectIcon = _theme.editFilterSelectionIcon;
    [effectArray addObject:info];
    for (TCFilter *filter in filters) {
        NSString *key = [NSString stringWithFormat:@"TC.Common.Filter_%@", filter.identifier];
        UGCKitEffectInfo * v= [UGCKitEffectInfo new];
        v.name = [_theme localizedString:key];
        v.icon = [_theme iconForFilter:filter.identifier];
        v.selectIcon = _theme.editFilterSelectionIcon;
        [effectArray addObject:v];
    }

    [_effectSelectView setEffectList:effectArray];
    _effectSelectType = EffectSelectType_Filter;
    _effectSelectView.selectedIndex = _filterIndex;
    [_videoCutView setColorType:UGCKitRangeColorType_Filter];
    [_videoCutView setCenterPanHidden:YES];
    [self removeAllTextFieldFromSuperView];
    [self removeAllPasterViewFromSuperView];
}

- (void)onPasterBtnClicked
{
    _bottomMenu.hidden = YES;
    _deleteBtn.hidden = NO;
    [self resetConfirmBtn];
    [self onShowEffectView];
    [self removeAllTextFieldFromSuperView];
    [self setLeftPanFrame:0 rightPanFrame:0];
    [_effectSelectView setEffectList:_pasterEffectArray];
    [_videoCutView setColorType:UGCKitRangeColorType_Paster];
    [_videoCutView setCenterPanHidden:YES];
    _effectSelectType = EffectSelectType_Paster;
}

- (void)onTextBtnClicked
{
    _bottomMenu.hidden = YES;
    _deleteBtn.hidden = NO;
    [self resetConfirmBtn];
    [self onShowEffectView];
    [self removeAllPasterViewFromSuperView];
    [self setLeftPanFrame:0 rightPanFrame:0];
    [_effectSelectView setEffectList:_textEffectArray];
    [_videoCutView setColorType:UGCKitRangeColorType_Text];
    [_videoCutView setCenterPanHidden:YES];
    _effectSelectType = EffectSelectType_Text;
}

#pragma mark UGCKitEffectSelectViewDelegate
-(void)onEffectBtnBeginSelect:(UIButton *)btn
{
    if (_effectSelectType != EffectSelectType_Effect) {
        return;
    }
    _effectType = (TXEffectType)btn.tag;
    UIColor *color = UGCKitVideoEffectColorPaletteColorAtIndex(btn.tag);
    [_videoCutView startColoration:color alpha:0.7];

    [_ugcEdit startEffect:(TXEffectType)_effectType startTime:_playTime];
    if (!_isReverse) {
        [self startPlayFromTime:_playTime toTime:_duration];
    }else{
        [self startPlayFromTime:0 toTime:_playTime];
    }
}

-(void)onEffectBtnEndSelect:(UIButton *)btn
{
    if (_effectType != -1) {
        [_videoCutView stopColoration];
        [_ugcEdit stopEffect:_effectType endTime:_playTime];
        [self pausePlay];
        _effectType = -1;
    }
}

-(void)onEffectBtnSelected:(UIButton *)btn
{
    _effectSelectIndex = btn.tag;
    switch (_effectSelectType) {
        case EffectSelectType_Time:
        {
            switch (_effectSelectIndex) {
                case 0:
                    [self onVideoTimeEffectsClear];
                    break;
                case 1:
                    [self onVideoTimeEffectsBackPlay];
                    break;
                case 2:
                    [self onVideoTimeEffectsRepeat];
                    break;
                case 3:
                    [self onVideoTimeEffectsSpeed];
                    break;
                default:
                    break;
            }
            _timeIndex = _effectSelectIndex;
        } break;
        case EffectSelectType_Filter:
        {
            [self setFilter:_effectSelectIndex];
//            if (!_isPlay) {
//                [_ugcEdit resumePlay];
//                [self setPlayBtn:YES];
//                _isPlay = YES;
//                _isSeek = NO;
//            }
        } break;
        case EffectSelectType_Paster:
        {
            [self pausePlay];
            [self removeAllPasterViewFromSuperView];
            if (_effectSelectIndex == _pasterEffectArray.count - 1) {
                _pasterAddView.hidden = NO;
                [_pasterAddView setUGCKitPasterType:UGCKitPasterTtemType_Paster];
            }else{
                UGCKitVideoPasterInfo* pasterInfo = _videoPasterInfoList[_effectSelectIndex];
                [_videoPreview addSubview:pasterInfo.pasterView];
                [_videoCutView setSelectColorInfo:_effectSelectIndex];
                [self setLeftPanFrame:pasterInfo.startTime rightPanFrame:pasterInfo.endTime];
                [_ugcEdit previewAtTime:pasterInfo.endTime];
            }
        } break;
        case EffectSelectType_Text:
        {
            [self pausePlay];
            [self removeAllTextFieldFromSuperView];
            if (_effectSelectIndex == _textEffectArray.count - 1) {
                _pasterAddView.hidden = NO;
                [_pasterAddView setUGCKitPasterType:UGCKitPasterTtemType_Qipao];
            }else{
                UGCKitVideoTextInfo* textInfo = _videoTextInfoList[_effectSelectIndex];
                [_videoPreview addSubview:textInfo.textField];
                [_videoCutView setSelectColorInfo:_effectSelectIndex];
                [self setLeftPanFrame:textInfo.startTime rightPanFrame:textInfo.endTime];
                [_ugcEdit previewAtTime:textInfo.endTime];
            }
        } break;
            
        default:
            break;
    }
}

- (void)onVideoTimeEffectsClear
{
    _timeType = TimeType_Clear;
    _isReverse = NO;
    [_ugcEdit setReverse:_isReverse];
    [_ugcEdit setRepeatPlay:nil];
    [_ugcEdit setSpeedList:nil];
    [self startPlayFromTime:0 toTime:_duration];
    
    [_videoCutView setCenterPanHidden:YES];
}
- (void)onVideoTimeEffectsBackPlay
{
    _timeType = TimeType_Back;
    _isReverse = YES;
    [_ugcEdit setReverse:_isReverse];
    [_ugcEdit setRepeatPlay:nil];
    [_ugcEdit setSpeedList:nil];
    [self startPlayFromTime:0 toTime:_duration];
    
    [_videoCutView setCenterPanHidden:YES];
}
- (void)onVideoTimeEffectsRepeat
{
    _timeType = TimeType_Repeat;
    _isReverse = NO;
    [_ugcEdit setReverse:_isReverse];
    [_ugcEdit setSpeedList:nil];
    TXRepeat *repeat = [[TXRepeat alloc] init];
    repeat.startTime = _duration / 5;
    repeat.endTime = repeat.startTime + 1;
    repeat.repeatTimes = 3;
    [_ugcEdit setRepeatPlay:@[repeat]];
    [self startPlayFromTime:0 toTime:_duration];
    
    [_videoCutView setCenterPanHidden:NO];
    [_videoCutView setCenterPanFrame:repeat.startTime];
}

- (void)onVideoTimeEffectsSpeed
{
    _timeType = TimeType_Speed;
    _isReverse = NO;
    [_ugcEdit setReverse:_isReverse];
    [_ugcEdit setRepeatPlay:nil];

    TXSpeed *speed1 =[[TXSpeed alloc] init];
    speed1.startTime = _duration* 1.5 / 5;
    speed1.endTime = speed1.startTime + 0.5;
    speed1.speedLevel = SPEED_LEVEL_SLOW;
    TXSpeed *speed2 =[[TXSpeed alloc] init];
    speed2.startTime = speed1.endTime;
    speed2.endTime = speed2.startTime + 0.5;
    speed2.speedLevel = SPEED_LEVEL_SLOWEST;
    TXSpeed *speed3 =[[TXSpeed alloc] init];
    speed3.startTime = speed2.endTime;
    speed3.endTime = speed3.startTime + 0.5;
    speed3.speedLevel = SPEED_LEVEL_SLOW;
    [_ugcEdit setSpeedList:@[speed1,speed2,speed3]];
    
    [self startPlayFromTime:0 toTime:_duration];
    [_videoCutView setCenterPanHidden:NO];
    [_videoCutView setCenterPanFrame:speed1.startTime];
}

#pragma mark UGCKitPasterAddViewDelegate
- (void)onPasterQipaoSelect:(UGCKitPasterQipaoInfo *)info
{
    [self removeAllTextFieldFromSuperView];
    int width = 170;
    int height = info.height / info.width * width;
    UGCKitVideoTextFiled* videoTextField = [[UGCKitVideoTextFiled alloc] initWithFrame:CGRectMake((_videoPreview.ugckit_width - 170) / 2, (_videoPreview.ugckit_height - 50) / 2, 170, 50)
                                                                     theme:_theme];
    [videoTextField setTextBubbleImage:info.image textNormalizationFrame:CGRectMake(info.textLeft / info.width, info.textTop / info.height, (info.width - info.textLeft - info.textRight) / info.width, (info.height - info.textTop - info.textBottom) / info.height)];
    videoTextField.frame = CGRectMake((_videoPreview.ugckit_width - width) / 2, (_videoPreview.ugckit_height - height) / 2, width, height);
    videoTextField.delegate = self;
    [_videoPreview addSubview:videoTextField];
    
    CGFloat percent = _duration / 10.0;
    CGFloat startTime = ([self getLastTextEndTime] == 0 ? 0 : [self getLastTextEndTime] + percent);
    if (startTime > _duration) {
        startTime = 0;
    }
    CGFloat endTime = startTime + percent;
    if(endTime > _duration){
        endTime = _duration;
    }
    UGCKitVideoTextInfo* textInfo = [UGCKitVideoTextInfo new];
    textInfo.textField = videoTextField;
    textInfo.startTime = startTime;
    textInfo.endTime = endTime;
    [_videoTextInfoList addObject:textInfo];
    
    [_textEffectArray insertObject:({
        UGCKitEffectInfo * v= [UGCKitEffectInfo new];
        v.name = [_theme localizedString:@"UGCKit.Edit.VideoEffect.BubbleSubtitle"];
        v.icon = info.iconImage;
        v;
    }) atIndex:_textEffectArray.count - 1];
    [_effectSelectView setEffectList:_textEffectArray];
    _effectSelectIndex = _textEffectArray.count - 2;
    
    [self setLeftPanFrame:startTime rightPanFrame:endTime];
    [_ugcEdit previewAtTime:endTime];
    [_videoCutView startColoration:[UIColor redColor] alpha:0.7];
}

- (void)onPasterAnimateSelect:(UGCKitPasterAnimateInfo *)info
{
    [self removeAllPasterViewFromSuperView];
    int width = 170;
    int height = info.height / info.width * width;
    UGCKitVideoPasterView *pasterView = [[UGCKitVideoPasterView alloc] initWithFrame:CGRectMake((_videoPreview.ugckit_width - width) / 2, (_videoPreview.ugckit_height - height) / 2, width, height)
                                                                   theme:_theme];
    pasterView.delegate = self;
    [pasterView setImageList:info.imageList imageDuration:info.duration];
    [_videoPreview addSubview:pasterView];
    
    CGFloat percent = _duration / 10.0;
    CGFloat startTime = ([self getLastPasterEndTime] == 0 ? 0 : [self getLastPasterEndTime] + percent);
    if (startTime > _duration) {
        startTime = 0;
    }
    CGFloat endTime = startTime + percent;
    if(endTime > _duration){
        endTime = _duration;
    }
    UGCKitVideoPasterInfo* pasterInfo = [[UGCKitVideoPasterInfo alloc] init];
    pasterInfo.pasterView = pasterView;
    pasterInfo.pasterInfoType = UGCKitPasterInfoType_Animate;
    pasterInfo.path = info.path;
    pasterInfo.iconImage = info.iconImage;
    pasterInfo.startTime = startTime;
    pasterInfo.endTime = endTime;
    [_videoPasterInfoList addObject:pasterInfo];
    
    [_pasterEffectArray insertObject:({
        UGCKitEffectInfo * v= [UGCKitEffectInfo new];
        v.name = [_theme localizedString:@"UGCKit.Edit.Paster.Dynamic"];
        v.icon = info.iconImage;
        v;
    }) atIndex:_pasterEffectArray.count - 1];
    [_effectSelectView setEffectList:_pasterEffectArray];
     _effectSelectIndex = _pasterEffectArray.count - 2;
    
    [self setLeftPanFrame:startTime rightPanFrame:endTime];
    [_ugcEdit previewAtTime:endTime];
    [_videoCutView startColoration:[UIColor redColor] alpha:0.7];
}

- (void)onPasterStaticSelect:(UGCKitPasterStaticInfo *)info
{
    [self removeAllPasterViewFromSuperView];
    int width = 170;
    int height = info.height / info.width * width;
    UGCKitVideoPasterView *pasterView = [[UGCKitVideoPasterView alloc] initWithFrame:CGRectMake((_videoPreview.ugckit_width - width) / 2, (_videoPreview.ugckit_height - height) / 2, width, height)
                                                                   theme:_theme];
    pasterView.delegate = self;
    [pasterView setImageList:@[info.image] imageDuration:0];
    [_videoPreview addSubview:pasterView];
    
    CGFloat percent = _duration / 10.0;
    CGFloat startTime = ([self getLastPasterEndTime] == 0 ? 0 : [self getLastPasterEndTime] + percent);
    if (startTime > _duration) {
        startTime = 0;
    }
    CGFloat endTime = startTime + percent;
    if(endTime > _duration){
        endTime = _duration;
    }
    UGCKitVideoPasterInfo* pasterInfo = [[UGCKitVideoPasterInfo alloc] init];
    pasterInfo.pasterView = pasterView;
    pasterInfo.pasterInfoType = UGCKitPasterInfoType_static;
    pasterInfo.image = info.image;
    pasterInfo.iconImage = info.iconImage;
    pasterInfo.startTime = startTime;
    pasterInfo.endTime = endTime;
    [_videoPasterInfoList addObject:pasterInfo];
    
    [_pasterEffectArray insertObject:({
        UGCKitEffectInfo * v= [UGCKitEffectInfo new];
        v.name = [_theme localizedString:@"UGCKit.Edit.Paster.Static"];
        v.icon = info.iconImage;
        v;
    }) atIndex:_pasterEffectArray.count - 1];
    [_effectSelectView setEffectList:_pasterEffectArray];
    _effectSelectIndex = _pasterEffectArray.count - 2;
    
    [self setLeftPanFrame:startTime rightPanFrame:endTime];
    [_ugcEdit previewAtTime:endTime];
    [_videoCutView startColoration:[UIColor redColor] alpha:0.7];
}
#pragma mark VideoPasterViewDelegate
- (void)onPasterViewTap
{
    
}
- (void)onRemovePasterView:(UGCKitVideoPasterView*)pasterView
{
    [pasterView removeFromSuperview];
    [self removeCurrentPasterInfo];
}

#pragma mark VideoTextFieldDelegate
- (void)onBubbleTap
{
    
}

- (void)onTextInputBegin
{
    _effectConfirmBtn.enabled = NO;
}

- (void)onTextInputDone:(NSString*)text
{
    _effectConfirmBtn.enabled = YES;
}

- (void)onRemoveTextField:(UGCKitVideoTextFiled*)textField
{
    [textField removeFromSuperview];
    [self removeCurrentTextInfo];
}

#pragma mark - VideoCutViewDelegate
- (void)onVideoRangeTap:(CGFloat)tapTime
{
    if (_effectSelectType == EffectSelectType_Paster) {
        [self removeAllPasterViewFromSuperView];
        for (UGCKitVideoPasterInfo *info in _videoPasterInfoList) {
            if (tapTime >= info.startTime && tapTime <= info.endTime) {
                [_videoPreview addSubview:info.pasterView];
                [self setPlayBtn:NO];
                [_ugcEdit previewAtTime:info.startTime];
                [self setLeftPanFrame:info.startTime rightPanFrame:info.endTime];
                _effectSelectIndex = [_videoPasterInfoList indexOfObject:info];
                break;
            }
        }
    }
    else if (_effectSelectType == EffectSelectType_Text) {
        [self removeAllTextFieldFromSuperView];
        for (UGCKitVideoTextInfo *info in _videoTextInfoList) {
            if (tapTime >= info.startTime && tapTime <= info.endTime) {
                [_videoPreview addSubview:info.textField];
                [self setPlayBtn:NO];
                [_ugcEdit previewAtTime:info.startTime];
                [self setLeftPanFrame:info.startTime rightPanFrame:info.endTime];
                _effectSelectIndex = [_videoTextInfoList indexOfObject:info];
                break;
            }
        }
    }
}

- (void)onVideoRangeLeftChanged:(UGCKitVideoRangeSlider *)sender
{
    [self setPlayBtn:NO];
    [_ugcEdit previewAtTime:sender.leftPos];
}

- (void)onVideoRangeLeftChangeEnded:(UGCKitVideoRangeSlider *)sender
{
    if (_effectSelectType == EffectSelectType_Paster) {
        if (_effectSelectIndex < 0 || _effectSelectIndex >= _videoPasterInfoList.count) {
            return;
        }
        UGCKitVideoPasterInfo *info = _videoPasterInfoList[_effectSelectIndex];
        info.startTime = sender.leftPos;
    }
    else if (_effectSelectType == EffectSelectType_Text) {
        if (_effectSelectIndex < 0 || _effectSelectIndex >= _videoTextInfoList.count) {
            return;
        }
        UGCKitVideoTextInfo *info = _videoTextInfoList[_effectSelectIndex];
        info.startTime = sender.leftPos;
    }
}

- (void)onVideoRangeRightChanged:(UGCKitVideoRangeSlider *)sender
{
    [self setPlayBtn:NO];
    [_ugcEdit previewAtTime:sender.rightPos];
}

- (void)onVideoRangeRightChangeEnded:(UGCKitVideoRangeSlider *)sender
{
    if (_effectSelectType == EffectSelectType_Paster) {
        if (_effectSelectIndex < _videoPasterInfoList.count && _effectSelectIndex >= 0) {
            UGCKitVideoPasterInfo *info = _videoPasterInfoList[_effectSelectIndex];
            info.endTime = sender.rightPos;
        }
    }
    else if (_effectSelectType == EffectSelectType_Text) {
        if (_effectSelectIndex < _videoTextInfoList.count && _effectSelectIndex >= 0) {
            UGCKitVideoTextInfo *info = _videoTextInfoList[_effectSelectIndex];
            info.endTime = sender.rightPos;
        }
    }
}

- (void)onVideoRangeCenterChanged:(UGCKitVideoRangeSlider*)sender
{
    [self setPlayBtn:NO];
    [_ugcEdit previewAtTime:sender.centerPos];
}

- (void)onVideoRangeCenterChangeEnded:(UGCKitVideoRangeSlider*)sender;
{
    if (_timeType == TimeType_Repeat) {
        TXRepeat *repeat = [[TXRepeat alloc] init];
        repeat.startTime = sender.centerPos;
        repeat.endTime = sender.centerPos + 0.5;
        repeat.repeatTimes = 3;
        [_ugcEdit setRepeatPlay:@[repeat]];
        [_ugcEdit setSpeedList:nil];
    }
    else if (_timeType == TimeType_Speed) {
        TXSpeed *speed1 =[[TXSpeed alloc] init];
        speed1.startTime = sender.centerPos;;
        speed1.endTime = speed1.startTime + 0.5;
        speed1.speedLevel = SPEED_LEVEL_SLOW;
        TXSpeed *speed2 =[[TXSpeed alloc] init];
        speed2.startTime = speed1.endTime;
        speed2.endTime = speed2.startTime + 0.5;
        speed2.speedLevel = SPEED_LEVEL_SLOWEST;
        TXSpeed *speed3 =[[TXSpeed alloc] init];
        speed3.startTime = speed2.endTime;
        speed3.endTime = speed3.startTime + 0.5;
        speed3.speedLevel = SPEED_LEVEL_SLOW;
        [_ugcEdit setSpeedList:@[speed1,speed2,speed3]];
        [_ugcEdit setRepeatPlay:nil];
    }
    
    if (_isReverse) {
        [self startPlayFromTime:0 toTime:sender.centerPos + 1.5];
    }else{
        [self startPlayFromTime:sender.centerPos toTime:_duration];
    }
}

- (void)onVideoSeekChange:(UGCKitVideoRangeSlider *)sender seekToPos:(CGFloat)pos
{
    _playTime = pos;
    _timeLabel.text = [NSString stringWithFormat:@"%02d:%02d",(int)_playTime / 60 , (int)_playTime % 60];
    //关闭特效面板不响应 seek
    if (_isHidingEffectView ||
        (self->_effectView.frame.origin.y > (self.view.ugckit_height - 205 * kScaleY))) {
        return;
    }
    [_ugcEdit previewAtTime:_playTime];
    [self setPlayBtn:NO];
}

#pragma mark - TCFilterSettingViewDelegate
//美颜
- (void)onSetBeautyDepth:(float)beautyDepth WhiteningDepth:(float)whiteningDepth
{
    [_ugcEdit setBeautyFilter:beautyDepth setWhiteningLevel:whiteningDepth];
}

#pragma mark TCBGMControllerListener
-(void)onBGMControllerPlay:(NSObject*) path
{
    [self dismissViewControllerAnimated:YES completion:nil];
    if (path == nil) {
        _isScrollToStart = NO;
        [self resetConfirmBtn];
        [self startPlayFromTime:0 toTime:_duration];
        return;
    }else{
        if (_BGMPath) {
            [self onBtnMusicStoped];
        }
        _BGMPath = path;
        _isScrollToStart = YES;
    }
    __weak __typeof(self) ws = self;
    if([_BGMPath isKindOfClass:[NSString class]]){
        NSString* strPath = (NSString*)_BGMPath;
        AVURLAsset* audioAsset = nil;
        NSDictionary* dic = @{AVURLAssetPreferPreciseDurationAndTimingKey:@(YES)};
        if ([strPath hasPrefix:@"http://"]) {
            audioAsset = [AVURLAsset URLAssetWithURL:[NSURL URLWithString:strPath] options:dic];
        }else {
            audioAsset = [AVURLAsset URLAssetWithURL:[NSURL fileURLWithPath:strPath] options:dic];
        }
        _BGMDuration = CMTimeGetSeconds(audioAsset.duration);
        [_musicView freshCutView:_BGMDuration];
        [_ugcEdit setBGM:(NSString *)_BGMPath result:^(int result) {
            if (result == 0) {
                [ws setBGMStartTime:0 endTime:MAXFLOAT];
            }
        }];
    } else if([_BGMPath isKindOfClass:[AVURLAsset class]]) {
        AVURLAsset* bgm =  (AVURLAsset*)_BGMPath;
        _BGMDuration = CMTimeGetSeconds(bgm.duration);
        [_musicView freshCutView:_BGMDuration];
        [_ugcEdit setBGMAsset:(AVAsset *)_BGMPath result:^(int result) {
            if (result == 0) {
                [ws setBGMStartTime:0 endTime:MAXFLOAT];
            }
        }];
    }
}

- (void)setBGMStartTime:(CGFloat)startTime endTime:(CGFloat)endTime
{
    if (!_BGMPath ) return;
    if (endTime == MAXFLOAT) {
        endTime = _BGMDuration;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [self->_ugcEdit setBGMStartTime:startTime endTime:endTime];
        [self->_ugcEdit setBGMVolume:self->_BGMVolume];
        [self->_ugcEdit setVideoVolume:self->_videoVolume];
        [self startPlayFromTime:0 toTime:self->_duration];
        self->_musicView.hidden = NO;
        self->_bottomMenu.hidden = YES;
        [self resetConfirmBtn];
        if (self->_isScrollToStart) {
            [self->_musicView resetSiderView];
        }
    });
}

#pragma mark UGCKitVideoRecordMusicViewDelegate
-(void)onBtnMusicSelected
{
    [self resetVideoProgress];
    UINavigationController *nv = [[UINavigationController alloc] initWithRootViewController:_bgmListVC];
    [nv.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
    nv.navigationBar.barTintColor = RGB(25, 29, 38);
    nv.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:nv animated:YES completion:nil];
    [_bgmListVC loadBGMList];
}
-(void)onBtnMusicStoped
{
    _BGMPath = nil;
    [_ugcEdit setBGM:nil result:^(int result) {
        
    }];
    _musicView.hidden = YES;
    _bottomMenu.hidden = NO;
    [self resetConfirmBtn];
    [self startPlayFromTime:0 toTime:_duration];
}

-(void)onBGMValueChange:(CGFloat)percent
{
    _BGMVolume = 1.0 * percent;
    [_ugcEdit setBGMVolume:_BGMVolume];
}
-(void)onVoiceValueChange:(CGFloat)percent
{
    _videoVolume = 1.0 * percent;
    [_ugcEdit setVideoVolume:_videoVolume];
}
-(void)onBGMRangeChange:(CGFloat)startPercent endPercent:(CGFloat)endPercent
{
    _isScrollToStart = NO;
    [self setBGMStartTime:_BGMDuration * startPercent endTime:_BGMDuration * endPercent];
}

#pragma mark - Utils
- (void)checkVideoOutputPath
{
    NSFileManager *manager = [[NSFileManager alloc] init];
    if ([manager fileExistsAtPath:_videoOutputPath]) {
        BOOL success =  [manager removeItemAtPath:_videoOutputPath error:nil];
        if (success) {
            NSLog(@"Already exist. Removed!");
        }
    }
}

- (float) heightForString:(UITextView *)textView andWidth:(float)width{
    CGSize sizeToFit = [textView sizeThatFits:CGSizeMake(width, MAXFLOAT)];
    return sizeToFit.height;
}


- (void) toastTip:(NSString*)toastInfo
{
    CGRect frameRC = [[UIScreen mainScreen] bounds];
    frameRC.origin.y = frameRC.size.height - 110;
    frameRC.size.height -= 110;
    __block UITextView * toastView = [[UITextView alloc] init];
    
    toastView.editable = NO;
    toastView.selectable = NO;
    
    frameRC.size.height = [self heightForString:toastView andWidth:frameRC.size.width];
    
    toastView.frame = frameRC;
    
    toastView.text = toastInfo;
    toastView.backgroundColor = [UIColor whiteColor];
    toastView.alpha = 0.5;
    
    [self.view addSubview:toastView];
    
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC);
    
    dispatch_after(popTime, dispatch_get_main_queue(), ^(){
        [toastView removeFromSuperview];
        toastView = nil;
    });
}

-(NSString *)getCoverPath:(UIImage *)coverImage
{
    UIImage *image = coverImage;
    if (image == nil) {
        return nil;
    }
    
    NSString *coverPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"TXUGC"];
    coverPath = [coverPath stringByAppendingPathComponent:[self getFileNameByTimeNow:@"TXUGC" fileType:@"jpg"]];
    if (coverPath) {
        // 保证目录存在
        [[NSFileManager defaultManager] createDirectoryAtPath:[coverPath stringByDeletingLastPathComponent]
                                  withIntermediateDirectories:YES
                                                   attributes:nil
                                                        error:nil];
        
        [UIImageJPEGRepresentation(image, 1.0) writeToFile:coverPath atomically:YES];
    }
    return coverPath;
}

-(NSString *)getFileNameByTimeNow:(NSString *)type fileType:(NSString *)fileType {
    NSTimeInterval now = [[NSDate date] timeIntervalSince1970];
    NSDateFormatter * formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyyMMdd_HHmmss"];
    NSDate * NowDate = [NSDate dateWithTimeIntervalSince1970:now];
    ;
    NSString * timeStr = [formatter stringFromDate:NowDate];
    NSString *fileName = ((fileType == nil) ||
                          (fileType.length == 0)
                          ) ? [NSString stringWithFormat:@"%@_%@",type,timeStr] : [NSString stringWithFormat:@"%@_%@.%@",type,timeStr,fileType];
    return fileName;
}


- (void)dealloc
{
    [_videoPreview removeNotification];
    _videoPreview = nil;
}

@end

@implementation UGCKitEditConfig
- (instancetype)init {
    if (self = [super init]) {
        _rotation = TCEditRotation0;
        _videoBitrate = -1; // auto
        _compressResolution = VIDEO_COMPRESSED_720P;
    }
    return self;
}

@end
