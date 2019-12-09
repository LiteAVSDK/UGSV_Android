// Copyright (c) 2019 Tencent. All rights reserved.
#import "UGCKitRecordViewController.h"
#import <MediaPlayer/MediaPlayer.h>
#import "SDKHeader.h"
#import "UGCKitEditViewController.h"
#import "UGCKitVideoRecordMusicView.h"
#import "UGCKitVideoRecordProcessView.h"
#import "UGCKitProgressHUD.h"
#import "UGCKitBGMListViewController.h"
#import "UGCKitBeautySettingPanel.h"
#import "UGCKitAudioEffectPanel.h"
#import "UGCKitLabel.h"
#import "UGCKitTheme.h"
#import "UGCKitSlideButton.h"
#import "UGCKitRecordView.h"
#import "UGCKitSmallButton.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitConstants.h"
#import <objc/runtime.h>
#import "UGCKitReporterInternal.h"
#import "SDKHeader.h"

static const CGFloat BUTTON_CONTROL_SIZE = 40;
static const CGFloat AudioEffectViewHeight = 150;

typedef NS_ENUM(NSInteger,RecordType) {
    RecordType_Normal,
    RecordType_Chorus,
};

typedef NS_ENUM(NSInteger,CaptureMode) {
    CaptureModeStill,
    CaptureModeTap,
    CaptureModePress
};

typedef NS_ENUM(NSInteger, RecordState) {
    RecordStateStopped,
    RecordStateRecording,
    RecordStatePaused
};

#if POD_PITU
#import "MCCameraDynamicView.h"
#import "MaterialManager.h"
#import "MCTip.h"
#endif

@interface UGCKitRecordViewController()
<TXUGCRecordListener, UIGestureRecognizerDelegate,
MPMediaPickerControllerDelegate,TCBGMControllerListener,TXVideoJoinerListener,
UGCKitVideoRecordMusicViewDelegate, UGCKitAudioEffectPanelDelegate, BeautySettingPanelDelegate,BeautyLoadPituDelegate
#if POD_PITU
, MCCameraDynamicDelegate
#endif
>
{
    UGCKitRecordConfig  *_config;      // 录制配置
    UGCKitTheme         *_theme;       // 主题配置
    UGCKitRecordView    *_controlView; // 界面控件覆盖层
    RecordState          _recordState; // 录制状态
    NSString            *_coverPath;   // 保存路径, 视频为<_coverPath>.mp4结尾, 封面图为<_coverPath>.png

    BOOL                            _isFrontCamera;
    BOOL                            _vBeautyShow;
    BOOL                            _preloadingVideos;
    // Chorus
    CGSize                          _size;
    int                             _fps;
    TXAudioSampleRate               _sampleRate;
    UIView                         *_videoPlayView;
    TXVideoBeautyStyle              _beautyStyle;
    float                           _beautyDepth;
    float                           _whitenDepth;
    float                           _ruddinessDepth;
    float                           _eye_level;
    float                           _face_level;
    
    BOOL                            _isCameraPreviewOn;
    //    BOOL                            _videoRecording;
    UIView *                        _videoRecordView;
    CGFloat                         _currentRecordTime;
    
    UGCKitAudioEffectPanel  *_soundMixView;
    
    BOOL                            _navigationBarHidden;
    BOOL                            _appForeground;
    //    BOOL                            _isPaused;
    
    // 倒计时
    UILabel *_countDownLabel;
    UIView *_countDownView;
    NSTimer *_countDownTimer;

#if POD_PITU
    MCCameraDynamicView   *_tmplBar;
    NSString              *_materialID;
#else
    UIView                *_tmplBar;
#endif

    UGCKitBGMListViewController*        _bgmListVC;

    NSObject*                 _BGMPath;
    CGFloat                   _BGMDuration;
    CGFloat                   _recordTime;
    
    int                       _deleteCount;
    float                     _zoom;
    BOOL                      _isBackDelete;
    BOOL                      _isFlash;
    
    UGCKitVideoRecordMusicView *  _musicView;
    SpeedMode                 _speedMode;
    
    UGCKitBeautySettingPanel *      _vBeauty;
    UGCKitProgressHUD*        _hud;
    CGFloat                   _bgmBeginTime;
    BOOL                      _bgmRecording;
    
    TXVideoEditer *           _videoEditer;
    TXVideoJoiner *           _videoJoiner;
    RecordType                _recordType;
    NSString *                _recordVideoPath;
    NSString *                _joinVideoPath;

    TXVideoVoiceChangerType   _voiceChangerType; // 变声参数
    TXVideoReverbType         _revertType; // 混音参数
}

@property (strong, nonatomic) IBOutlet UIButton *btnNext;
@property (strong, nonatomic) IBOutlet UIView *captureModeView;

@property (assign, nonatomic) CaptureMode captureMode;

@end


@implementation UGCKitRecordViewController

- (instancetype)initWithConfig:(UGCKitRecordConfig *)config theme:(UGCKitTheme *)theme
{
    if (self = [self initWithNibName:nil bundle:nil]) {
        _config = config ?: [[UGCKitRecordConfig alloc] init];
        _theme = theme ?: [UGCKitTheme sharedTheme];
        _preloadingVideos = _config.recoverDraft;
        [[TXUGCRecord shareInstance] setAspectRatio:_config.ratio];
        _coverPath = [NSTemporaryDirectory() stringByAppendingString:[[NSUUID UUID] UUIDString]];
    }
    return self;
}

-(instancetype)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        _config = [[UGCKitRecordConfig alloc] init];
        _theme = [UGCKitTheme sharedTheme];

        _appForeground = YES;
        _isFrontCamera = YES;
        _vBeautyShow = NO;
        
        _beautyStyle = VIDOE_BEAUTY_STYLE_SMOOTH;
        _beautyDepth = 6.3;
        _whitenDepth = 2.7;
        
        _isCameraPreviewOn = NO;
        _recordState = RecordStateStopped;
        
        _currentRecordTime = 0;
        _sampleRate = AUDIO_SAMPLERATE_48000;
        
        _speedMode = SpeedMode_Standard;
        
        _voiceChangerType = VIDOE_VOICECHANGER_TYPE_0; // 无变声
        _revertType = VIDOE_REVERB_TYPE_0; // 无混音效果

        [TXUGCRecord shareInstance].recordDelegate = self;
        
        _bgmListVC = [[UGCKitBGMListViewController alloc] initWithTheme:_theme];
        [_bgmListVC setBGMControllerListener:self];
        _recordVideoPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"outputRecord.mp4"];
        _joinVideoPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"outputJoin.mp4"];
        
        self.captureMode = CaptureModeTap;
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    [self initUI];
    [self initBeautyUI];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onAppWillResignActive:) name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onAppDidBecomeActive:) name:UIApplicationDidBecomeActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onAudioSessionEvent:)
                                                 name:AVAudioSessionInterruptionNotification
                                               object:nil];
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    _navigationBarHidden = self.navigationController.navigationBar.hidden;
    [self.navigationController setNavigationBarHidden:YES];

    if (_isCameraPreviewOn == NO) {
        [self startCameraPreview];
    }else{
        //停止特效的声音
        [[[TXUGCRecord shareInstance] getBeautyManager] setMotionMute:NO];
    }
    // 恢复变声与混音效果
    if (_voiceChangerType >= 0) {
        [[TXUGCRecord shareInstance] setVoiceChangerType:_voiceChangerType];
    }
    if (_revertType >= 0) {
        [[TXUGCRecord shareInstance] setReverbType:_revertType];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    [self.navigationController setNavigationBarHidden:_navigationBarHidden];
}
- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}
#pragma mark - Notification Handler
- (void)onAudioSessionEvent:(NSNotification*)notification
{
    NSDictionary *info = notification.userInfo;
    AVAudioSessionInterruptionType type = [info[AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];
    if (type == AVAudioSessionInterruptionTypeBegan) {
        // 在10.3及以上的系统上，分享跳其它app后再回来会收到AVAudioSessionInterruptionWasSuspendedKey的通知，不处理这个事件。
        if ([info objectForKey:@"AVAudioSessionInterruptionWasSuspendedKey"]) {
            return;
        }
        _appForeground = NO;
        if (_recordState == RecordStateRecording) {
            [self pauseRecord];
        }
    }else{
        AVAudioSessionInterruptionOptions options = [info[AVAudioSessionInterruptionOptionKey] unsignedIntegerValue];
        if (options == AVAudioSessionInterruptionOptionShouldResume) {
            _appForeground = YES;
        }
    }
}

- (void)onAppWillResignActive:(NSNotification*)notification
{
    _appForeground = NO;
    if (_recordState == RecordStateRecording) {
        [self pauseRecord];
    }
}

- (void)onAppDidBecomeActive:(NSNotification*)notification
{
    [[TXUGCRecord shareInstance] resumeAudioSession];
    _appForeground = YES;
}

#pragma mark - UI Setup

-(void)initUI
{
    self.title = @"";
    self.view.backgroundColor = _theme.backgroundColor;

    // 预览界面
    _videoRecordView = [[UIView alloc] initWithFrame:self.view.bounds];
    _videoRecordView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.view addSubview:_videoRecordView];

    CGFloat top = [UIApplication sharedApplication].statusBarFrame.size.height + 25;
    CGFloat centerY = top;

    // “下一步”按钮
    _btnNext = [UIButton buttonWithType:UIButtonTypeCustom];
    _btnNext.bounds = CGRectMake(0, 0, BUTTON_CONTROL_SIZE, BUTTON_CONTROL_SIZE);
    _btnNext.titleLabel.font = [UIFont systemFontOfSize:12];
    [_btnNext setTitleColor:_theme.titleColor forState:UIControlStateNormal];
    [_btnNext setBackgroundImage:_theme.nextIcon forState:UIControlStateNormal];
    [_btnNext addTarget:self action:@selector(onNext:) forControlEvents:UIControlEventTouchUpInside];
    [_btnNext setTitle:[_theme localizedString:@"UGCKit.Common.Next"]
              forState:UIControlStateNormal];
    _btnNext.enabled = NO;
    [_btnNext sizeToFit];
    _btnNext.center = CGPointMake(CGRectGetWidth(self.view.bounds) - 25 - BUTTON_CONTROL_SIZE / 2 , centerY);
    [self.view addSubview:_btnNext];

    // 主界面控件浮层
    _controlView = [[UGCKitRecordView alloc] initWithFrame:CGRectMake(0, CGRectGetMaxY(_btnNext.frame)+30,
                                                                      CGRectGetWidth(self.view.bounds),
                                                                      CGRectGetHeight(self.view.bounds) - CGRectGetMaxY(_btnNext.frame)-30)
                                               minDuration:_config.minDuration
                                               maxDuration:_config.maxDuration];
    _controlView.theme = _theme;
    _controlView.speedControlEnabled = _config.chorusVideo == nil;
#if UGC_SMART
    _controlView.musicButtonEnabled = NO;
    _controlView.speedControlEnabled = NO;
#endif
    _controlView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [_controlView setupViews];
    [self.view addSubview:_controlView];

    // 后退按钮
    UIButton *btnBack = [UGCKitSmallButton buttonWithType:UIButtonTypeCustom];
    btnBack.bounds = CGRectMake(0, 0, BUTTON_CONTROL_SIZE, BUTTON_CONTROL_SIZE);
    btnBack.center = CGPointMake(17, centerY);
    [btnBack setImage:_theme.backIcon forState:UIControlStateNormal];
    [btnBack addTarget:self action:@selector(onTapBackButton:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:btnBack];

    // 事件绑定
    [_controlView.btnMusic addTarget:self action:@selector(onBtnMusicClicked:) forControlEvents:UIControlEventTouchUpInside];
    [_controlView.btnRatioGroup addTarget:self action:@selector(onBtnRatioClicked:) forControlEvents:UIControlEventValueChanged];
    [_controlView.btnBeauty addTarget:self action:@selector(onBtnBeautyClicked:) forControlEvents:UIControlEventTouchUpInside];
    [_controlView.btnAudioEffect addTarget:self action:@selector(onBtnAudioMix:) forControlEvents:UIControlEventTouchUpInside];

    [_controlView.btnStartRecord addTarget:self action:@selector(onRecordButtonTouchDown:) forControlEvents:UIControlEventTouchDown];
    [_controlView.btnStartRecord addTarget:self action:@selector(onRecordButtonTouchUp:) forControlEvents:UIControlEventTouchUpInside|UIControlEventTouchUpOutside];

    [_controlView.btnFlash addTarget:self action:@selector(onTapTorchButton) forControlEvents:UIControlEventTouchUpInside];
    [_controlView.btnCamera addTarget:self action:@selector(onTapCameraSwitch) forControlEvents:UIControlEventTouchUpInside];
    [_controlView.btnDelete addTarget:self action:@selector(onBtnDeleteClicked) forControlEvents:UIControlEventTouchUpInside];
    [_controlView.btnCountDown addTarget:self action:@selector(onCountDown:) forControlEvents:UIControlEventTouchUpInside];
    for (UIButton *button in _controlView.speedBtnList) {
        [button addTarget:self action:@selector(onBtnSpeedClicked:) forControlEvents:UIControlEventTouchUpInside];
    }
    [_controlView.recordButtonSwitchControl addTarget:self action:@selector(onTapCaptureMode:) forControlEvents:UIControlEventValueChanged];

    UIPinchGestureRecognizer* pinchGensture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(onPInchZoom:)];
    [self.view addGestureRecognizer:pinchGensture];

    // 滑动滤镜
    UIPanGestureRecognizer* panGensture = [[UIPanGestureRecognizer alloc] initWithTarget:self action: @selector (onPanSlideFilter:)];
    panGensture.delegate = self;
    [self.view addGestureRecognizer:panGensture];


    if (_config.chorusVideo) {
        // 合唱
        [self setupChrousUIWithTopMargin:CGRectGetMaxY(_btnNext.frame) + 20];
    }
}

- (void)setupChrousUIWithTopMargin:(CGFloat)top {
    // 合唱
    _controlView.speedControlEnabled = NO;
    _controlView.photoModeEnabled = NO;
    _controlView.progressView.minimumTimeTipHidden = YES;
    _controlView.btnRatioGroup.hidden = YES;
    _controlView.btnMusic.hidden = YES;
    _controlView.btnAudioEffect.hidden = YES;
    _controlView.btnBeauty.frame = _controlView.btnMusic.frame;
    CGRect countDownFrame = _controlView.btnCountDown.frame;
    countDownFrame.origin.y = CGRectGetMinY(_controlView.btnRatioGroup.frame);
    _controlView.btnCountDown.frame = countDownFrame;
    _controlView.btnCountDown.hidden = NO;

    CGRect playFrame = CGRectMake(CGRectGetMidX(self.view.bounds), top,
                                  CGRectGetWidth(self.view.bounds)/2, CGRectGetHeight(self.view.bounds)/2);
    CGRect recordFrame = playFrame; recordFrame.origin.x = 0;

    _videoPlayView = [[UIView alloc] initWithFrame:playFrame];;
    _videoPlayView.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    [self.view insertSubview:_videoPlayView atIndex:0];

    _videoRecordView.frame = recordFrame;

    _videoRecordView.translatesAutoresizingMaskIntoConstraints = NO;
    _videoPlayView.translatesAutoresizingMaskIntoConstraints = NO;

    TXVideoInfo *info = [TXVideoInfoReader getVideoInfo:_config.chorusVideo];
    CGFloat duration = info.duration;
    _fps = (int)(info.fps + 0.5);
    if (info.audioSampleRate == 8000) {
        _sampleRate = AUDIO_SAMPLERATE_8000;
    }else if (info.audioSampleRate == 16000){
        _sampleRate = AUDIO_SAMPLERATE_16000;
    }else if (info.audioSampleRate == 32000){
        _sampleRate = AUDIO_SAMPLERATE_32000;
    }else if (info.audioSampleRate == 44100){
        _sampleRate = AUDIO_SAMPLERATE_44100;
    }else if (info.audioSampleRate == 48000){
        _sampleRate = AUDIO_SAMPLERATE_48000;
    }
    _size = CGSizeMake(info.width, info.height);
    _recordType = RecordType_Chorus;
    _config.minDuration = duration;
    _config.maxDuration = duration;

    TXPreviewParam *param = [TXPreviewParam new];
    param.videoView = _videoPlayView;
    param.renderMode = PREVIEW_RENDER_MODE_FILL_EDGE;
    //用于模仿视频播放
    _videoEditer = [[TXVideoEditer alloc] initWithPreview:param];
    [_videoEditer setVideoPath:_config.chorusVideo];
    //用于模仿视频和录制视频的合成
    _videoJoiner = [[TXVideoJoiner alloc] initWithPreview:nil];
    _videoJoiner.joinerDelegate = self;
}

#pragma mark - UI LazyLoaders
- (UGCKitAudioEffectPanel *)soundMixView {
    if (_soundMixView) {
        return _soundMixView;
    }

    _soundMixView = [[UGCKitAudioEffectPanel alloc] initWithTheme:_theme
                                                            frame:CGRectMake(0, CGRectGetHeight(self.view.bounds)-AudioEffectViewHeight,
                                                                             CGRectGetWidth(self.view.bounds), AudioEffectViewHeight)];
    _soundMixView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    _soundMixView.delegate = self;
    [self.view addSubview:_soundMixView];
    _soundMixView.hidden = YES;
    return _soundMixView;
}

- (UGCKitVideoRecordMusicView *)musicView {
    if (_musicView) {
        return _musicView;
    }
    _musicView = [[UGCKitVideoRecordMusicView alloc] initWithFrame:CGRectMake(0, self.view.ugckit_bottom - 330 * kScaleY, self.view.ugckit_width, 330 * kScaleY) needEffect:YES theme:_theme];
    _musicView.delegate = self;
    _musicView.hidden = YES;
    [self.view addSubview:_musicView];
    _musicView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    return _musicView;
}

#pragma mark ---- Video Beauty UI ----
-(void)initBeautyUI
{
    NSUInteger controlHeight = [UGCKitBeautySettingPanel getHeight];
    CGFloat offset = 0;
    if (@available(iOS 11, *)) {
        offset = [UIApplication sharedApplication].keyWindow.safeAreaInsets.bottom;
    }
    _vBeauty = [[UGCKitBeautySettingPanel alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height - controlHeight - offset,
                                                                    self.view.frame.size.width, controlHeight)
                                                   theme:_theme];
    _vBeauty.hidden = YES;
    _vBeauty.delegate = self;
    _vBeauty.pituDelegate = self;
    _vBeauty.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    [self.view addSubview:_vBeauty];
}

- (void)setSelectedSpeed:(SpeedMode)tag
{
    [_controlView setSelectedSpeed:tag];
}

-(void)setSpeedBtnHidden:(BOOL)hidden{
    if (_config.chorusVideo != nil) hidden = YES;
    _controlView.speedControlEnabled = !hidden;
}

#pragma mark - Actions
- (void)takePhoto {
    [[TXUGCRecord shareInstance] snapshot:^(UIImage *image) {
        dispatch_async(dispatch_get_main_queue(), ^{
            UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
            UIImageWriteToSavedPhotosAlbum(image, self, @selector(image:didFinishSavingWithError:contextInfo:), (__bridge void*)imageView);
            imageView.contentMode = UIViewContentModeScaleAspectFit;
            imageView.frame = self.view.bounds;
            [self.view insertSubview:imageView belowSubview:self->_controlView.bottomMask];
            
            CGAffineTransform t = CGAffineTransformMakeScale(0.33, 0.33);
            [UIView animateWithDuration:0.3 animations:^{
                imageView.transform = t;
            } completion:nil];
        });
    }];
}

-(IBAction)onTapBackButton:(id)sender
{
    NSArray *videoPaths = [[TXUGCRecord shareInstance].partsManager getVideoPathList];
    if (videoPaths.count > 0) {
        UIAlertController *controller = [UIAlertController alertControllerWithTitle:[_theme localizedString:@"UGCKit.Record.AbandonRecord"]
                                                                            message:nil
                                                                     preferredStyle:UIAlertControllerStyleAlert];
        [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.OK"]
                                                       style:UIAlertActionStyleDefault
                                                     handler:^(UIAlertAction * _Nonnull action) {
            [[NSUserDefaults standardUserDefaults] setObject:nil forKey:CACHE_PATH_LIST];
            if (self.completion) {
                self.completion([UGCKitResult cancelledResult]);
            }
        }]];
        [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.Cancel"] style:UIAlertActionStyleCancel handler:nil]];
        [self presentViewController:controller animated:YES completion:nil];
    }else{
        [[NSUserDefaults standardUserDefaults] setObject:nil forKey:CACHE_PATH_LIST];
        if (self.completion) {
            self.completion([UGCKitResult cancelledResult]);
        }
    }
}

-(IBAction)onTapTorchButton
{
    _isFlash = !_isFlash;
    _controlView.torchOn = _isFlash;
    [[TXUGCRecord shareInstance] toggleTorch:_isFlash];
}

- (IBAction)onNext:(id)sender
{
    [self stopRecordAndComplete];
}

- (IBAction)onBtnMusicClicked:(id)sender
{
    _vBeauty.hidden = YES;
    UIView *musicView = [self musicView];
    if (_BGMPath) {
        musicView.hidden = !musicView.hidden;
        [self hideBottomView:!musicView.hidden];
    }else{
        UINavigationController *nv = [[UINavigationController alloc] initWithRootViewController:_bgmListVC];
        [nv.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName:_theme.titleColor}];
        nv.navigationBar.barTintColor = _theme.backgroundColor;
        nv.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:nv animated:YES completion:nil];
        [_bgmListVC loadBGMList];
    }
}

- (IBAction)onBtnRatioClicked:(UGCKitSlideButton *)sender
{
    TXVideoAspectRatio ratio = sender.buttons[sender.selectedIndex].tag;;
    [self _switchToRatio:ratio];
}

-(void)onBtnSpeedClicked:(UIButton *)btn
{
    [UIView animateWithDuration:0.3 animations:^{
        self->_speedMode = btn.tag;
        [self setSelectedSpeed:self->_speedMode];
    }];
}

-(IBAction)onBtnDeleteClicked
{
    if (_recordState == RecordStateRecording) {
        [self pauseRecord];
    }
    if (0 == _deleteCount) {
        [_controlView.progressView prepareDeletePart];
    }else{
        [_controlView.progressView comfirmDeletePart];
        [[TXUGCRecord shareInstance].partsManager deleteLastPart];
        _isBackDelete = YES;
    }
    if (2 == ++ _deleteCount) {
        _deleteCount = 0;
    }
}
- (IBAction)onBtnAudioMix:(id)sender {
    [self hideBottomView:YES];
    UGCKitAudioEffectPanel *soundMixView = [self soundMixView];
    soundMixView.hidden = NO;
    _vBeauty.hidden = YES;
    _musicView.hidden = YES;

    CATransition *animation = [CATransition animation];
    animation.type = kCATransitionFade;
    [soundMixView.layer addAnimation:animation forKey:nil];
}

- (IBAction)onCountDown:(id)sender {
    [self hideBottomView:YES];
    [self startCountDown];
}

-(IBAction)onBtnBeautyClicked:(id)sender
{
    _vBeautyShow = !_vBeautyShow;
    _musicView.hidden = YES;
    _vBeauty.hidden = !_vBeautyShow;
    [self hideBottomView:_vBeautyShow];
}
-(IBAction)onTapCameraSwitch
{
    _isFrontCamera = !_isFrontCamera;
    [[TXUGCRecord shareInstance] switchCamera:_isFrontCamera];
    _controlView.isFrontCamera = _isFrontCamera;
    if (_isFrontCamera) {
        _isFlash = NO;
        _controlView.torchOn = NO;
    }
    [[TXUGCRecord shareInstance] toggleTorch:_isFlash];
}

- (IBAction)onTapCaptureMode:(UGCKitSlideOptionControl *)control
{
    CaptureMode mode = (CaptureMode)control.selectedIndex;
    [UIView animateWithDuration:0.1 animations:^{
        [self->_controlView setRecordButtonStyle:(UGCKitRecordButtonStyle)mode];
    }];

    self.captureMode = mode;
}


- (void)_configButtonToPause {
    [_controlView setRecordButtonStyle:UGCKitRecordButtonStylePause];
}

#pragma mark - Count Down
- (void)startCountDown {
    if (_countDownTimer) {
        return;
    }
    
    if (_countDownView == nil) {
        UIVisualEffectView *view = [[UIVisualEffectView alloc] initWithEffect:[UIBlurEffect effectWithStyle: UIBlurEffectStyleExtraLight]];
        view.layer.cornerRadius = 20;
        view.clipsToBounds = YES;
        
        view.translatesAutoresizingMaskIntoConstraints = NO;
        UILabel *countDownLabel = [[UILabel alloc] init];
        countDownLabel.translatesAutoresizingMaskIntoConstraints = NO;
        countDownLabel.textColor = [UIColor colorWithWhite:0.33 alpha:1];
        countDownLabel.font = [UIFont systemFontOfSize:100];
        
        [view.contentView addSubview:countDownLabel];
        [view.contentView addConstraint:[NSLayoutConstraint constraintWithItem:view.contentView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:countDownLabel attribute:NSLayoutAttributeCenterX multiplier:1 constant:0]];
        [view.contentView addConstraint:[NSLayoutConstraint constraintWithItem:view.contentView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:countDownLabel attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
        [view addConstraint:[NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1 constant:150]];
        [view addConstraint:[NSLayoutConstraint constraintWithItem:view attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1 constant:150]];
        
        [self.view addSubview:view];
        [self.view addConstraint:[NSLayoutConstraint constraintWithItem:self.view attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:view attribute:NSLayoutAttributeCenterX multiplier:1 constant:0]];
        [self.view addConstraint:[NSLayoutConstraint constraintWithItem:self.view attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:view attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
        
        
        _countDownView = view;
        _countDownLabel = countDownLabel;
    }
    _countDownView.hidden = NO;
    _countDownLabel.text = @"3";
    _countDownTimer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(onCountDownTimer:) userInfo:nil repeats:YES];
    _countDownView.hidden = NO;
}

- (void)onCountDownTimer:(NSTimer *)timer {
    if ([UIApplication sharedApplication].applicationState != UIApplicationStateActive) {
        return;
    }
    int count = _countDownLabel.text.intValue - 1;
    _countDownLabel.text = @(count).stringValue;
    if (count == 0) {
        [_countDownTimer invalidate];
        _countDownTimer = nil;
        _countDownView.hidden = YES;
        _controlView.recordButtonStyle = UGCKitRecordButtonStylePause;
        _controlView.recordButtonSwitchControl.selectedIndex = CaptureModeTap;
        self.captureMode = CaptureModeTap;
        [self startRecord];
        [self hideBottomView:NO];
    }
}

#pragma mark - Properties
-(void)syncSpeedRateToSDK{
    switch (_speedMode) {
        case SpeedMode_VerySlow:
            [[TXUGCRecord shareInstance] setRecordSpeed:VIDEO_RECORD_SPEED_SLOWEST];
            break;
        case SpeedMode_Slow:
            [[TXUGCRecord shareInstance] setRecordSpeed:VIDEO_RECORD_SPEED_SLOW];
            break;
        case SpeedMode_Standard:
            [[TXUGCRecord shareInstance] setRecordSpeed:VIDEO_RECORD_SPEED_NOMAL];
            break;
        case SpeedMode_Quick:
            [[TXUGCRecord shareInstance] setRecordSpeed:VIDEO_RECORD_SPEED_FAST];
            break;
        case SpeedMode_VeryQuick:
            [[TXUGCRecord shareInstance] setRecordSpeed:VIDEO_RECORD_SPEED_FASTEST];
            break;
        default:
            break;
    }
}

#pragma mark - Right Side Button Event Handler

- (void)_switchToRatio:(TXVideoAspectRatio)ratio;
{
    _config.ratio = ratio;
    [_controlView setVideoRatio:ratio];
    [[TXUGCRecord shareInstance] setAspectRatio:_config.ratio];
}

#pragma mark - Record Control
-(void)startSDKRecord
{
    _controlView.btnCountDown.enabled = NO;
    [self startCameraPreview];
    [self syncSpeedRateToSDK];
    int result = [[TXUGCRecord shareInstance] startRecord:[_coverPath stringByAppendingString:@".mp4"]
                                                coverPath:[_coverPath stringByAppendingString:@".png"]];
    [UGCKitReporter report:UGCKitReportItem_startrecord userName:nil code:result msg:result == 0 ? @"启动录制成功" : @"启动录制失败"];
    if(0 != result)
    {
        if(-3 == result) [self alert:[_theme localizedString:@"UGCKit.Record.HintLaunchRecordFailed"] msg:[_theme localizedString:@"UGCKit.Record.ErrorCamera"]];
        if(-4 == result) [self alert:[_theme localizedString:@"UGCKit.Record.HintLaunchRecordFailed"] msg:[_theme localizedString:@"UGCKit.Record.ErrorMIC"]];
        if(-5 == result) [self alert:[_theme localizedString:@"UGCKit.Record.HintLaunchRecordFailed"] msg:[_theme localizedString:@"UGCKit.Record.ErrorLicense"]];
    }else{
        //如果设置了BGM，播放BGM
        [self playBGM:_bgmBeginTime];

        //初始化录制状态
        _recordState = RecordStateRecording;
        _bgmRecording = YES;
        _controlView.recordButtonSwitchControl.enabled = NO;
        //录制过程中不能切换分辨率,不能切换拍照模式
        _controlView.btnRatioGroup.enabled = NO;
        [_controlView.btnRatioGroup shrink];
        self.captureModeView.userInteractionEnabled = NO;

        [self setSpeedBtnHidden:YES];

        [self _configButtonToPause];
        if (_recordType == RecordType_Chorus) {
            [_videoEditer startPlayFromTime:_recordTime toTime:_config.maxDuration];
        }
    }
}

- (void)pauseRecord {
    self.captureModeView.userInteractionEnabled = YES;
    _controlView.btnCountDown.enabled = YES;
    _controlView.recordButtonSwitchControl.enabled = YES;
    __weak __typeof(self) weakSelf = self;
    [[TXUGCRecord shareInstance] pauseRecord:^{
        [weakSelf saveVideoClipPathToPlist];
    }];
    [self pauseBGM];
    if (_captureMode != CaptureModePress) {
        [_controlView setRecordButtonStyle:UGCKitRecordButtonStyleRecord];
    }

    [_controlView.progressView pause];
    [self setSpeedBtnHidden:NO];

    _recordState = RecordStatePaused;

    [_videoEditer stopPlay];
}

- (void)startRecord {
    if (_recordTime >= _config.maxDuration) {
        // 已经录制到最大时长
        return;
    }

    if (_recordState == RecordStateStopped) {
        [self startSDKRecord];
        return;
    }

    _controlView.btnCountDown.enabled = NO;
    self.captureModeView.userInteractionEnabled = NO;

    [self syncSpeedRateToSDK];

    if (_bgmRecording) {
        [self resumeBGM];
    }else{
        [self playBGM:_bgmBeginTime];
        _bgmRecording = YES;
    }
    
    [[TXUGCRecord shareInstance] resumeRecord];
    [self _configButtonToPause];

    if (_deleteCount == 1) {
        [_controlView.progressView cancelDelete];
        _deleteCount = 0;
    }
    [self setSpeedBtnHidden:YES];
    _controlView.recordButtonSwitchControl.enabled = NO;
    _recordState = RecordStateRecording;
    [_videoEditer startPlayFromTime:_recordTime toTime:_config.maxDuration];
}

-(void)stopRecordAndComplete
{
    _btnNext.enabled = NO;
    _controlView.btnCountDown.enabled = YES;
    _controlView.recordButtonSwitchControl.enabled = YES;
    [_controlView setRecordButtonStyle:UGCKitRecordButtonStyleRecord];
    [self setSpeedBtnHidden:NO];
    [_videoEditer stopPlay];
    //调用partsManager快速合成视频，不破坏录制状态，下次返回后可以接着录制（注意需要先暂停视频录制）
    __weak __typeof(self) weakSelf = self;
    if (_recordState == RecordStateRecording) {
        [[TXUGCRecord shareInstance] pauseRecord:^{
            __strong __typeof(weakSelf) self = weakSelf; if (self == nil) { return; }
            self->_recordState = RecordStatePaused;
            [self saveVideoClipPathToPlist];

            [[TXUGCRecord shareInstance].partsManager joinAllParts:self->_recordVideoPath complete:^(int result) {
                [weakSelf onFinishRecord:result];
            }];
        }];
    } else {
        [[TXUGCRecord shareInstance].partsManager joinAllParts:self->_recordVideoPath complete:^(int result) {
            [weakSelf onFinishRecord:result];
        }];
    }
}

#pragma mark - Camera Control
- (void)onPInchZoom:(UIPinchGestureRecognizer*)recognizer
{
    if (recognizer.state == UIGestureRecognizerStateBegan || recognizer.state == UIGestureRecognizerStateChanged) {
        [[TXUGCRecord shareInstance] setZoom:MIN(MAX(1.0, _zoom * recognizer.scale),5.0)];
    }else if (recognizer.state == UIGestureRecognizerStateEnded){
        _zoom = MIN(MAX(1.0, _zoom * recognizer.scale),5.0);
        recognizer.scale = 1;
    }
}

#pragma mark - Record Button Actions
- (IBAction)onRecordButtonTouchUp:(id)sender
{
    if (self.captureMode == CaptureModePress) {
        [self pauseRecord];
    } else if (self.captureMode == CaptureModeTap) {
        if (_recordState == RecordStateRecording) {
            [self pauseRecord];
        } else {
            [self startRecord];
        }
    }
}

// 录制按钮按下
- (void)onRecordButtonTouchDown:(id)sender {
    if (self.captureMode == CaptureModeStill) {
        [self takePhoto];
    } else if (self.captureMode == CaptureModePress) {
        [self startRecord];
    }
}

- (void)changeCaptureModeUI:(CaptureMode)captureMode
{
    self.captureMode = captureMode;
    _controlView.recordButtonSwitchControl.selectedIndex = captureMode;
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo
{
    UIImageView *imageView = (__bridge UIImageView *)contextInfo;
    [UIView animateWithDuration:0.3
                          delay:0
                        options:UIViewAnimationOptionCurveLinear|UIViewAnimationOptionBeginFromCurrentState
                     animations:^{
        CGAffineTransform t = CGAffineTransformTranslate(imageView.transform, 0, self.view.ugckit_height);
        imageView.transform = CGAffineTransformScale(t, 0.5, 0.5);
    } completion:^(BOOL finished) {
        [imageView removeFromSuperview];
    }];
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

-(void)startCameraPreview
{
    if (_isCameraPreviewOn == NO)
    {
        //简单设置
        //        TXUGCSimpleConfig * param = [[TXUGCSimpleConfig alloc] init];
        //        param.videoQuality = VIDEO_QUALITY_MEDIUM;
        //        [[TXUGCRecord shareInstance] startCameraSimple:param preview:_videoRecordView];
        //自定义设置
        TXUGCCustomConfig * param = [[TXUGCCustomConfig alloc] init];
        param.videoResolution = _config.resolution;
        param.videoFPS = _config.fps;
        param.videoBitratePIN = _config.videoBitrate;
        param.GOP = _config.gop;
        param.audioSampleRate = AUDIO_SAMPLERATE_48000;
        param.minDuration = _config.minDuration;
        param.maxDuration = _config.maxDuration + 2;
        [[TXUGCRecord shareInstance] startCameraCustom:param preview:_videoRecordView];

        TXBeautyManager *beautyManager = [[TXUGCRecord shareInstance] getBeautyManager];
        [beautyManager setBeautyStyle:(TXBeautyStyle)_beautyStyle];
        [beautyManager setBeautyLevel:_beautyDepth];
        [beautyManager setWhitenessLevel:_whitenDepth];
        [beautyManager setRuddyLevel:_ruddinessDepth];

        [[TXUGCRecord shareInstance] setVideoRenderMode:VIDEO_RENDER_MODE_ADJUST_RESOLUTION];

        [beautyManager setEyeScaleLevel:_eye_level];
        [beautyManager setFaceSlimLevel:_face_level];

        if (_config.watermark) {
            UIImage *watermark = _config.watermark.image;
            CGRect watermarkFrame = _config.watermark.frame;
            [[TXUGCRecord shareInstance] setWaterMark:watermark normalizationFrame:watermarkFrame];
        } else {
            [[TXUGCRecord shareInstance] setWaterMark:nil normalizationFrame:CGRectZero];;
        }
#if POD_PITU
        [self motionTmplSelected:_materialID];
#endif
        //内存里面没有视频数据，重置美颜状态
        if ([TXUGCRecord shareInstance].partsManager.getVideoPathList.count == 0) {
            [self resetBeautySettings];
        }
        
        //加载本地视频 -> 内存
        if (_preloadingVideos) {
            NSArray *cachePathList = [[NSUserDefaults standardUserDefaults] objectForKey:CACHE_PATH_LIST];
            NSString *cacheFolder = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:UGCKIT_PARTS_DIR];
            //预加载视频 -> SDK
            TXVideoInfo *videoInfo = nil;
            for (NSInteger i = cachePathList.count - 1; i >= 0; i --) {
                NSString *videoPath = [cacheFolder stringByAppendingPathComponent:cachePathList[i]];
                [[TXUGCRecord shareInstance].partsManager insertPart:videoPath atIndex:0];
                if (i == 0) {
                    videoInfo = [TXVideoInfoReader getVideoInfo:videoPath];
                }
            }

            //录制分辨比例初始化
            if (videoInfo.width == 720 && videoInfo.height == 1280) {
                [self _switchToRatio:VIDEO_ASPECT_RATIO_9_16];
            }else if(videoInfo.width == 720 && videoInfo.height == 960) {
                [self _switchToRatio:VIDEO_ASPECT_RATIO_4_3];
            }else if(videoInfo.width == 720 && videoInfo.height == 720) {
                [self _switchToRatio:VIDEO_ASPECT_RATIO_1_1];
            }
            _controlView.btnRatioGroup.enabled = cachePathList.count == 0;
            //进度条初始化
            CGFloat time = 0;
            for (NSInteger i = 0; i < cachePathList.count; i ++) {
                NSString *videoPath = [cacheFolder stringByAppendingPathComponent:cachePathList[i]];
                time = time + [TXVideoInfoReader getVideoInfo:videoPath].duration;
                [_controlView.progressView pauseAtTime:time];
            }
            _preloadingVideos = NO;
        }
        _isCameraPreviewOn = YES;
    }
}

- (void)resetBeautySettings {
    [_vBeauty resetValues];
    TXBeautyManager *manager = [[TXUGCRecord shareInstance] getBeautyManager];
    unsigned int methodCount = 0;
    Method *list = class_copyMethodList([TXBeautyManager class], &methodCount);
    for (int i = 0; i < methodCount; ++i) {
        SEL action = method_getName(list[i]);
        NSString *name = NSStringFromSelector(action);
        if ([name hasPrefix:@"set"] && [name hasSuffix:@"Level:"]) {
            IMP imp = method_getImplementation(list[i]);
            void (*setter)(id,SEL,float) = (void (*)(id,SEL,float))imp;
            setter(manager, action, 0.0);
        }
    }
    [manager setBeautyStyle:_vBeauty.beautyStyle];
    [manager setBeautyLevel:_vBeauty.beautyLevel];
    [manager setWhitenessLevel:_vBeauty.whiteLevel];
    [manager setMotionTmpl:nil inDir:nil];

    [[TXUGCRecord shareInstance] setFilter:nil];
    [[TXUGCRecord shareInstance] setGreenScreenFile:nil];
}

-(void)stopCameraPreview
{
    if (_isCameraPreviewOn == YES)
    {
        [[TXUGCRecord shareInstance] stopCameraPreview];
        _isCameraPreviewOn = NO;
    }
}

- (void)saveVideoClipPathToPlist
{
    if (_recordType == RecordType_Chorus) {
        // 合唱暂不支持草稿
        return;
    }
    NSMutableArray *cachePathList = [NSMutableArray array];
    for (NSString *videoPath in [TXUGCRecord shareInstance].partsManager.getVideoPathList) {
        [cachePathList addObject:[[videoPath pathComponents] lastObject]];
    }
    [[NSUserDefaults standardUserDefaults] setObject:cachePathList forKey:CACHE_PATH_LIST];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

-(void)onFinishRecord:(int)result
{
    _btnNext.enabled = YES;
    if(0 == result){
        if (_recordType == RecordType_Normal) {
            [self stopCameraPreview];
            if (self.completion) {
                UGCKitResult *result = [[UGCKitResult alloc] init];;
                result.media = [UGCKitMedia mediaWithVideoPath: _recordVideoPath];
                result.coverImage = [[UIImage alloc] initWithContentsOfFile:[_coverPath stringByAppendingString:@".png"]];
                if (self.completion) {
                    self.completion(result);
                }
            }
        }else{
            CGFloat width = 720;
            CGFloat height = 1280;
            CGRect recordScreen = CGRectMake(0, 0, width, height);
            //播放视频所占画布的大小这里要计算下，防止视频拉伸
            CGRect playScreen = CGRectZero;
            if (_size.height / _size.width >= height / width) {
                CGFloat playScreen_w = height * _size.width / _size.height;
                playScreen = CGRectMake(width + (width - playScreen_w) / 2.0, 0, playScreen_w, height);
            }else{
                CGFloat playScreen_h = width * _size.height / _size.width;
                playScreen = CGRectMake(width, (height - playScreen_h) / 2.0, width, playScreen_h);
            }
            if (_recordVideoPath
                && _config.chorusVideo
                && [[NSFileManager defaultManager] fileExistsAtPath:_recordVideoPath]
                && [[NSFileManager defaultManager] fileExistsAtPath:_config.chorusVideo]) {
                if (0 == [_videoJoiner setVideoPathList:@[_recordVideoPath,_config.chorusVideo]]) {
                    [_videoJoiner setSplitScreenList:@[[NSValue valueWithCGRect:recordScreen],[NSValue valueWithCGRect:playScreen]] canvasWidth:720 * 2 canvasHeight:1280];
                    [_videoJoiner splitJoinVideo:VIDEO_COMPRESSED_720P videoOutputPath:_joinVideoPath];
                    _hud = [UGCKitProgressHUD showHUDAddedTo:self.view animated:YES];
                    _hud.mode = UGCKitProgressHUDModeText;
                    _hud.label.text = [_theme localizedString:@"UGCKit.Media.VideoSynthesizing"];
                }else{
                    [self alert:[_theme localizedString:@"UGCKit.Media.HintVideoSynthesizeFailed"]
                            msg:[_theme localizedString:@"UGCKit.Record.VideoChorusNotSupported"]];
                }
            }else{
                [self alert:[_theme localizedString:@"UGCKit.Media.HintVideoSynthesizeFailed"]
                        msg:[_theme localizedString:@"UGCKit.Record.TryAgain"]];
            }
        }
        [UGCKitReporter report:UGCKitReportItem_videorecord userName:nil code:0 msg:@"视频录制成功"];
    }else{
        [self alert:[_theme localizedString:@"UGCKit.Media.HintVideoSynthesizeFailed"]
                msg:[_theme localizedString:@"UGCKit.Record.TryAgain"]];
        [UGCKitReporter report:UGCKitReportItem_videorecord userName:nil code:-1 msg:@"视频录制失败"];
    }
}

///  选拍照模式
- (void)setCaptureMode:(CaptureMode)captureMode
{
    _captureMode = captureMode;
    
    BOOL isStillMode = captureMode == CaptureModeStill;
    _controlView.speedControlEnabled = !isStillMode;
    _controlView.progressView.hidden = isStillMode;
    _controlView.recordTimeLabel.hidden = isStillMode;
    _controlView.btnDelete.hidden = isStillMode;
}

#pragma mark Control Panel Switching
- (void) touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    if (_vBeautyShow)
    {
        UITouch *touch = [[event allTouches] anyObject];
        CGPoint _touchPoint = [touch locationInView:self.view];
        if (NO == CGRectContainsPoint(_vBeauty.frame, _touchPoint))
        {
            [self onBtnBeautyClicked:nil];
        }
    }
    if (_musicView && !_musicView.hidden) {
        CGPoint _touchPoint = [[[event allTouches] anyObject] locationInView:self.view];
        if (NO == CGRectContainsPoint(_musicView.frame, _touchPoint)){
            _musicView.hidden = !_musicView.hidden;
            [self hideBottomView:!_musicView.hidden];
        }
    }
    if (_soundMixView && !_soundMixView.hidden) {
        _soundMixView.hidden = YES;
        _controlView.bottomMask.hidden = NO;
        [self hideBottomView:NO];
        CATransition *animation = [CATransition animation];
        animation.duration = 0.1;
        animation.type = kCATransitionFade;
        [_soundMixView.superview.layer addAnimation:animation forKey:nil];
    }
}

- (void)hideBottomView:(BOOL)bHide
{
    _controlView.hidden = bHide;
}

#pragma mark - TXUGCRecordListener
-(void) onRecordProgress:(NSInteger)milliSecond;
{
    _recordTime =  milliSecond / 1000.0;
    if (_recordTime >= _config.maxDuration) {
        [self pauseRecord];
    }
    [self updateRecordProgressLabel: _recordTime];
    
    BOOL isEmpty = milliSecond == 0;
    //录制过程中不能切换BGM, 不能改变声音效果

    _controlView.btnMusic.enabled = isEmpty;
    _btnNext.enabled = milliSecond / 1000.0 >= _config.minDuration;
    _controlView.btnAudioEffect.enabled = _controlView.btnMusic.enabled;
    //回删之后被模仿视频进度回退
    if (_isBackDelete && _recordType == RecordType_Chorus) {
        [_videoEditer previewAtTime:_recordTime];
        _isBackDelete = NO;
    }
}

-(void) onRecordComplete:(TXUGCRecordResult*)result;
{
    [_controlView setRecordButtonStyle:UGCKitRecordButtonStyleRecord];

    if (_appForeground)
    {
        if (_currentRecordTime >= _config.minDuration)
        {
            if (result.retCode != UGC_RECORD_RESULT_FAILED) {
                [self onFinishRecord:(int)result.retCode];
            }else{
                [self toastTip:[_theme localizedString:@"UGCKit.Record.ErrorREC"]];
            }
        } else {
            [self toastTip:[_theme localizedString:@"UGCKit.Record.ErrorTime"]];
        }
    }
}

- (void)updateRecordProgressLabel:(CGFloat)second {
    _currentRecordTime = second;
    [_controlView.progressView update:_currentRecordTime / _config.maxDuration];
    long min = (int)_currentRecordTime / 60;
    long sec = (int)_currentRecordTime % 60;
    [_controlView.recordTimeLabel setText:[NSString stringWithFormat:@"%02ld:%02ld", min, sec]];
}

#pragma mark TXVideoJoinerListener
-(void) onJoinProgress:(float)progress
{
    _hud.label.text = [NSString stringWithFormat:@"%@%d%%",[_theme localizedString:@"UGCKit.Media.VideoSynthesizing"], (int)(progress * 100)];
}
-(void) onJoinComplete:(TXJoinerResult *)result
{
    [_hud hideAnimated:YES];
    if (_appForeground && result.retCode == RECORD_RESULT_OK) {
        [self stopCameraPreview];
        if (self.completion) {
            UGCKitResult *result = [[UGCKitResult alloc] init];
            result.media = [UGCKitMedia mediaWithVideoPath:_joinVideoPath];
            self.completion(result);
        }
        /*UGCKitEditViewController *vc = [[UGCKitEditViewController alloc] initWithMedia:[UGCKitMedia mediaWithVideoPath:_joinVideoPath]
                                                                                config:nil
                                                                                 theme:nil];
        __weak UINavigationController *nav = self.navigationController;
        vc.completion = ^(UGCKitResult *result) {
            [nav popToRootViewControllerAnimated:YES];
        };
        [self.navigationController pushViewController:vc animated:YES];
         */
    }else{
        [self alert:[_theme localizedString:@"UGCKit.Record.VideoJoinerFailed"] msg:result.descMsg];
    }
    [UGCKitReporter report:UGCKitReportItem_videojoiner userName:nil code:result.retCode msg:result.descMsg];
}

#if POD_PITU
- (void)motionTmplSelected:(NSString *)materialID {
    if (materialID == nil) {
        [MCTip hideText];
    }
    _materialID = materialID;
    if ([MaterialManager isOnlinePackage:materialID]) {
        [[TXUGCRecord shareInstance] selectMotionTmpl:materialID inDir:[MaterialManager packageDownloadDir]];
    } else {
        NSString *localPackageDir = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:@"Resource"];
        [[TXUGCRecord shareInstance] selectMotionTmpl:materialID inDir:localPackageDir];
    }
}
#endif

#pragma mark - BeautyLoadPituDelegate
- (void)onLoadPituStart
{
    dispatch_async(dispatch_get_main_queue(), ^{
        UGCKitProgressHUD *hud = [UGCKitProgressHUD showHUDAddedTo:self.view animated:YES];
        hud.mode = UGCKitProgressHUDModeText;
        hud.label.text = [self->_theme localizedString:@"UGCKit.Record.ResourceLoadBegin"];
        self->_hud = hud;
    });
}
- (void)onLoadPituProgress:(CGFloat)progress
{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_hud.label.text = [NSString stringWithFormat:[self->_theme localizedString:@"UGCKit.Record.ResourceLoading"],(int)(progress * 100)];
    });
}
- (void)onLoadPituFinished
{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_hud.label.text = [self->_theme localizedString:@"UGCKit.Record.ResourceLoadSucceeded"];
        [self->_hud hideAnimated:YES afterDelay:1];
    });
}
- (void)onLoadPituFailed
{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_hud.label.text = [self->_theme localizedString:@"UGCKit.Record.ResourceLoadFailed"];
        [self->_hud hideAnimated:YES afterDelay:1];
    });
}

#pragma mark - BeautySettingPanelDelegate
- (void)onSetBeautyStyle:(TXVideoBeautyStyle)beautyStyle beautyLevel:(float)beautyLevel whitenessLevel:(float)whitenessLevel ruddinessLevel:(float)ruddinessLevel{
    _beautyStyle = beautyStyle;
    _beautyDepth = beautyLevel;
    _whitenDepth = whitenessLevel;
    _ruddinessDepth = ruddinessLevel;

    TXBeautyManager *beautyManager = [[TXUGCRecord shareInstance] getBeautyManager];
    [beautyManager setBeautyStyle:(TXBeautyStyle)_beautyStyle];
    [beautyManager setBeautyLevel:_beautyDepth];
    [beautyManager setWhitenessLevel:_whitenDepth];
    [beautyManager setRuddyLevel:_ruddinessDepth];
}

- (BOOL)respondsToSelector:(SEL)aSelector {
    if ([super respondsToSelector:aSelector]) {
        return YES;
    }
    return [[[TXUGCRecord shareInstance] getBeautyManager] respondsToSelector:aSelector];
}

- (id)forwardingTargetForSelector:(SEL)aSelector {
    return [[TXUGCRecord shareInstance] getBeautyManager];
}

- (void)onSetFilterMixLevel:(float)mixLevel{
    [[TXUGCRecord shareInstance] setSpecialRatio:mixLevel / 10.0];
}

- (void)onSetFilter:(UIImage*)filterImage
{
    [[TXUGCRecord shareInstance] setFilter:filterImage];
}

- (void)onSetGreenScreenFile:(NSURL *)file
{
    [[TXUGCRecord shareInstance] setGreenScreenFile:file];
}

- (void)onSelectMotionTmpl:(NSString *)tmplName inDir:(NSString *)tmplDir
{
    [[[TXUGCRecord shareInstance] getBeautyManager] setMotionTmpl:tmplName inDir:tmplDir];
}



#pragma mark TCBGMControllerListener
-(void) onBGMControllerPlay:(NSObject*) path{
    [self dismissViewControllerAnimated:YES completion:nil];
    if(path == nil) return;
    [self onSetBGM:path];
    //试听音乐这里要把RecordSpeed 设置为VIDEO_RECORD_SPEED_NOMAL，否则音乐可能会出现加速或则慢速播现象
    [[TXUGCRecord shareInstance] setRecordSpeed:VIDEO_RECORD_SPEED_NOMAL];
    [self playBGM:0];
    dispatch_async(dispatch_get_main_queue(), ^(){
        UGCKitVideoRecordMusicView *musicView = self->_musicView;
        [musicView resetCutView];
        if(musicView.hidden){
            musicView.hidden = !musicView.hidden;
            [self hideBottomView:!musicView.hidden];
        }
    });
}

#pragma mark - UGCKitAudioEffectPanelDelegate
- (void)audioEffectPanel:(UGCKitAudioEffectPanel *)panel didSelectReverbType:(TXVideoReverbType)type
{
    _revertType = type;
    [[TXUGCRecord shareInstance] setReverbType:type];
}

- (void)audioEffectPanel:(UGCKitAudioEffectPanel *)panel didSelectVoiceChangerType:(TXVideoVoiceChangerType)type
{
    _voiceChangerType = type;
    [[TXUGCRecord shareInstance] setVoiceChangerType:type];
}

-(void)onBtnMusicSelected
{
    UINavigationController *nv = [[UINavigationController alloc] initWithRootViewController:_bgmListVC];
    [nv.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
    //    nv.navigationBar.barTintColor = RGB(25, 29, 38);
    nv.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:nv animated:YES completion:nil];
    [_bgmListVC loadBGMList];
}

-(void)onBtnMusicStoped
{
    _BGMPath = nil;
    _bgmRecording = NO;
    [[TXUGCRecord shareInstance] stopBGM];
    if (!_musicView.hidden) {
        _musicView.hidden = !_musicView.hidden;
        [self hideBottomView:!_musicView.hidden];
    }
}

-(void)onBGMValueChange:(CGFloat)value
{
    [[TXUGCRecord shareInstance] setBGMVolume:value];
}

-(void)onVoiceValueChange:(CGFloat)value
{
    [[TXUGCRecord shareInstance] setMicVolume:value];
}

-(void)onBGMRangeChange:(CGFloat)startPercent endPercent:(CGFloat)endPercent
{
    //切换bgm 范围的时候，bgm录制状态置NO
    _bgmRecording = NO;
    //试听音乐这里要把RecordSpeed 设置为VIDEO_RECORD_SPEED_NOMAL，否则音乐可能会出现加速或则慢速播现象
    [[TXUGCRecord shareInstance] setRecordSpeed:VIDEO_RECORD_SPEED_NOMAL];
    [self playBGM:_BGMDuration * startPercent toTime:_BGMDuration * endPercent];
}
#pragma mark - BGM Operations
-(void)onSetBGM:(NSObject *)path
{
    _BGMPath = path;
    if([_BGMPath isKindOfClass:[NSString class]]){
        _BGMDuration =  [[TXUGCRecord shareInstance] setBGM:(NSString *)_BGMPath];
    }else{
        _BGMDuration =  [[TXUGCRecord shareInstance] setBGMAsset:(AVAsset *)_BGMPath];
    }
    
    _bgmRecording = NO;
    dispatch_async(dispatch_get_main_queue(), ^{
        [UGCKitProgressHUD hideHUDForView:self.view animated:YES];
    });
}

-(void)playBGM:(CGFloat)beginTime{
    if (_BGMPath != nil) {
        [[TXUGCRecord shareInstance] playBGMFromTime:beginTime toTime:_BGMDuration withBeginNotify:^(NSInteger errCode) {
            
        } withProgressNotify:^(NSInteger progressMS, NSInteger durationMS) {
            
        } andCompleteNotify:^(NSInteger errCode) {
            
        }];
        _bgmBeginTime = beginTime;
    }
}

-(void)playBGM:(CGFloat)beginTime toTime:(CGFloat)endTime
{
    if (_BGMPath != nil) {
        [[TXUGCRecord shareInstance] playBGMFromTime:beginTime toTime:endTime withBeginNotify:^(NSInteger errCode) {
            
        } withProgressNotify:^(NSInteger progressMS, NSInteger durationMS) {
            
        } andCompleteNotify:^(NSInteger errCode) {
            
        }];
        _bgmBeginTime = beginTime;
    }
}

-(void)pauseBGM{
    if (_BGMPath != nil) {
        [[TXUGCRecord shareInstance] pauseBGM];
    }
}

- (void)resumeBGM
{
    if (_BGMPath != nil) {
        [[TXUGCRecord shareInstance] resumeBGM];
    }
}

#pragma mark - Misc Methods
- (void) toastTip:(NSString*)toastInfo
{
    UGCKitProgressHUD *hud = [UGCKitProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.mode = UGCKitProgressHUDModeText;
    hud.label.text = toastInfo;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^{
        if ([hud.label.text isEqualToString:toastInfo]) {
            [hud hideAnimated:YES];
        }
    });
}

-(void)alert:(NSString *)title msg:(NSString *)msg
{
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:title message:msg preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.OK"] style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];
}

#pragma mark - gesture handler
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
    CGPoint location = [gestureRecognizer locationInView:self.view];
    CGRect frame = [self.view convertRect:_controlView.bottomMask.frame fromView:_controlView];
    return !CGRectContainsPoint(frame, location);
}

- (void)onPanSlideFilter:(UIPanGestureRecognizer*)recognizer
{
    CGPoint translation = [recognizer translationInView:self.view.superview];
    [recognizer velocityInView:self.view];
    CGPoint speed = [recognizer velocityInView:self.view];
    
//    NSLog(@"pan center:(%.2f)", translation.x);
//    NSLog(@"pan speed:(%.2f)", speed.x);
    
    float ratio = translation.x / self.view.frame.size.width;
    float leftRatio = ratio;
    NSInteger index = [_vBeauty currentFilterIndex];
    UIImage* curFilterImage = [_vBeauty filterImageByIndex:index];
    UIImage* filterImage1 = nil;
    UIImage* filterImage2 = nil;
    CGFloat filter1Level = 0.f;
    CGFloat filter2Level = 0.f;
    if (leftRatio > 0) {
        filterImage1 = [_vBeauty filterImageByIndex:index - 1];
        filter1Level = [_vBeauty filterMixLevelByIndex:index - 1] / 10;
        filterImage2 = curFilterImage;
        filter2Level = [_vBeauty filterMixLevelByIndex:index] / 10;
    }
    else {
        filterImage1 = curFilterImage;
        filter1Level = [_vBeauty filterMixLevelByIndex:index] / 10;
        filterImage2 = [_vBeauty filterImageByIndex:index + 1];
        filter2Level = [_vBeauty filterMixLevelByIndex:index + 1] / 10;
        leftRatio = 1 + leftRatio;
    }
    
    if (recognizer.state == UIGestureRecognizerStateChanged) {
        [[TXUGCRecord shareInstance] setFilter:filterImage1 leftIntensity:filter1Level rightFilter:filterImage2 rightIntensity:filter2Level leftRatio:leftRatio];
    }
    else if (recognizer.state == UIGestureRecognizerStateEnded) {
        BOOL isDependRadio = fabs(speed.x) < 500; //x方向的速度
        [self animateFromFilter1:filterImage1 filter2:filterImage2 filter1MixLevel:filter1Level filter2MixLevel:filter2Level leftRadio:leftRatio speed:speed.x completion:^{
            NSInteger filterIndex = 0;
            if (!isDependRadio) {
                if (speed.x < 0) {
                    filterIndex = index + 1;
                } else {
                    filterIndex = index - 1;
                }
            } else {
                if (ratio > 0.5) {   //过半或者速度>500就切换
                    filterIndex = index - 1;
                } else if  (ratio < -0.5) {
                    filterIndex = index + 1;
                }
            }
            self->_vBeauty.currentFilterIndex = filterIndex;
            UILabel* filterTipLabel = [UILabel new];
            filterTipLabel.text = [self->_vBeauty currentFilterName];
            filterTipLabel.font = [UIFont systemFontOfSize:30];
            filterTipLabel.textColor = UIColor.whiteColor;
            filterTipLabel.alpha = 0.1;
            [filterTipLabel sizeToFit];
            CGSize viewSize = self.view.frame.size;
            CGFloat centerX = self->_config.chorusVideo == nil ? viewSize.width / 2 : viewSize.width / 4;
            filterTipLabel.center = CGPointMake(centerX, viewSize.height / 3);
            [self.view addSubview:filterTipLabel];

            [UIView animateWithDuration:0.25 animations:^{
                filterTipLabel.alpha = 1;
            } completion:^(BOOL finished) {
                [UIView animateWithDuration:0.25 delay:0.25 options:UIViewAnimationOptionCurveLinear animations:^{
                    filterTipLabel.alpha = 0.1;
                } completion:^(BOOL finished) {
                    [filterTipLabel removeFromSuperview];
                }];
            }];
        }];
        
        
    }
}

- (void)animateFromFilter1:(UIImage*)filter1Image filter2:(UIImage*)filter2Image filter1MixLevel:(CGFloat)filter1MixLevel filter2MixLevel:(CGFloat)filter2MixLevel leftRadio:(CGFloat)leftRadio speed:(CGFloat)speed completion:(void(^)(void))completion
{
    if (leftRadio <= 0 || leftRadio >= 1) {
        completion();
        return;
    }
    
    static float delta = 1.f / 12;
    
    BOOL isDependRadio = fabs(speed) < 500;
    if (isDependRadio) {
        if (leftRadio < 0.5) {
            leftRadio -= delta;
        }
        else {
            leftRadio += delta;
        }
    }
    else {
        if (speed > 0) {
            leftRadio += delta;
        }
        else
            leftRadio -= delta;
    }
    
    [[TXUGCRecord shareInstance] setFilter:filter1Image leftIntensity:filter1MixLevel rightFilter:filter2Image rightIntensity:filter2MixLevel leftRatio:leftRadio];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.f / 30 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self animateFromFilter1:filter1Image filter2:filter2Image filter1MixLevel:filter1MixLevel filter2MixLevel:filter2MixLevel leftRadio:leftRadio speed:speed completion:completion];
    });
}

- (void)uinit{
    [[TXUGCRecord shareInstance] stopRecord];
    [[TXUGCRecord shareInstance] stopCameraPreview];
    [[TXUGCRecord shareInstance].partsManager deleteAllParts];
    _recordState = RecordStateStopped;
    //    [TCUtil removeCacheFile:_recordVideoPath];
    //    [TCUtil removeCacheFile:_joinVideoPath];
}

- (void)dealloc
{
    [self uinit];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}
@end

@implementation UGCKitRecordConfig
- (instancetype)init
{
    self = [super init];
    if (self) {
        _ratio = VIDEO_ASPECT_RATIO_9_16;
        _resolution = VIDEO_RESOLUTION_720_1280;
        _videoBitrate = 9600;
        _minDuration = 2;
        _maxDuration = 16.0;
        _audioSampleRate = AUDIO_SAMPLERATE_48000;
        _fps = 30;
        _gop = 3;
        _AECEnabled = YES;
    }
    return self;
}
@end
