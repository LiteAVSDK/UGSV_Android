// Copyright (c) 2019 Tencent. All rights reserved.
#import "UGCKitRecordViewController.h"
#import <MediaPlayer/MediaPlayer.h>
//#import <TCBeautyPanel/TCBeautyPanel.h>
#import "SDKHeader.h"
#import "UGCKitMem.h"
#import "UGCKitMediaPickerViewController.h"
#import "UGCKitEditViewController.h"
#import "UGCKitVideoRecordMusicView.h"
#import "UGCKitVideoRecordProcessView.h"
#import "UGCKitProgressHUD.h"
#import "UGCKitBGMListViewController.h"
#import "UGCKitAudioEffectPanel.h"
#import "UGCKitLabel.h"
#import "UGCKitTheme.h"
#import "UGCKitSlideButton.h"
#import "UGCKitRecordControlView.h"
#import "UGCKitSmallButton.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitConstants.h"
#import <objc/runtime.h>
#import <Masonry/Masonry.h>
#import "UGCKitReporterInternal.h"
#import "SDKHeader.h"
#import "BeautyView.h"
#import <XMagic/XMagic.h>
#import <OpenGLES/EAGL.h>
#import <OpenGLES/ES2/gl.h>
#import <OpenGLES/ES2/glext.h>
#import <YTCommonXMagic/yt_auth_apple.h>
static const CGFloat BUTTON_CONTROL_SIZE = 40;
static const CGFloat AudioEffectViewHeight = 150;

typedef NS_ENUM(NSInteger, CaptureMode) {
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

@interface UGCKitRecordPreviewController : NSObject

@property (nonatomic, strong, readonly) NSArray<NSNumber *> *allVideoVolumes;

@property (nonatomic, strong, readonly) NSArray<NSString *> *allVideoPaths;

@property (nonatomic, strong, readonly) NSString *recordVideoPath;

@property (nonatomic, strong, readonly) UIView *videoRecordView;

- (instancetype)initWithContainerView:(UIView *)containerView
                          recordStyle:(UGCKitRecordStyle)recordStyle
                         chorusVideos:(NSArray<NSString *> *)chorusVideos
                          recordVideo:(NSString *)recordVideo;

- (void)changeChorusVideo:(NSString *)videoPath;

- (void)startPlayChorusVideos:(CGFloat)startTime
                       toTime:(CGFloat)endTime;
- (void)stopPlayChorusVideos;

- (void)seekChorusVideosToTime:(CGFloat)time;

@end

@interface UGCKitNavControllerPrivate : UINavigationController

@property (nonatomic, assign) UIInterfaceOrientationMask supportedOrientations;

@end


@interface UGCKitRecordViewController()
<TXUGCRecordListener, UIGestureRecognizerDelegate,
MPMediaPickerControllerDelegate,TCBGMControllerListener,TXVideoJoinerListener,
UGCKitVideoRecordMusicViewDelegate,UGCKitAudioEffectPanelDelegate,YTSDKEventListener,
YTSDKLogListener,TXVideoCustomProcessDelegate,TXVideoCustomProcessListener
#if POD_PITU
, MCCameraDynamicDelegate
#endif
>
{
    UGCKitRecordConfig  *_config;      // 录制配置
    UGCKitTheme         *_theme;       // 主题配置
    UGCKitRecordControlView *_controlView; // 界面控件覆盖层
    RecordState          _recordState; // 录制状态
    NSString            *_coverPath;   // 保存路径, 视频为<_coverPath>.mp4结尾, 封面图为<_coverPath>.png

    BOOL                            _isFrontCamera;
    BOOL                            _vBeautyShow;
    BOOL                            _preloadingVideos;
    float                           _whitenDepth;
    float                           _ruddinessDepth;
    float                           _eye_level;
    float                           _face_level;
    
    BOOL                            _isCameraPreviewOn;
    //    BOOL                            _videoRecording;
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
    
    BeautyView *      _vBeauty;
    UGCKitProgressHUD*        _hud;
    CGFloat                   _bgmBeginTime;
    BOOL                      _bgmRecording;
    
    TXVideoJoiner *           _videoJoiner;
    NSString *                _joinVideoPath;

    TXVideoVoiceChangerType   _voiceChangerType; // 变声参数
    TXVideoReverbType         _revertType; // 混音参数
    
    // 是否正在合成视频参数（保证只合成一次）
    BOOL _isCompletingRecord;
    // 是否从音乐选择面返回（保证不会刷新其他配置）
    BOOL _isFromMusicSelectVC;
    // 是否结束录制
    BOOL _isStopRecord;
    BOOL _isScrollToStart;
}

@property (strong, nonatomic) IBOutlet UIButton *btnNext;
@property (strong, nonatomic) IBOutlet UIView *captureModeView;
@property (strong, nonatomic) IBOutlet UIButton *btnChangeVideo;

@property (assign, nonatomic) CaptureMode captureMode;
// initData
@property (nonatomic, assign) BOOL initData;  //initData
@property (strong, nonatomic) UGCKitRecordPreviewController *previewController;
//xmagic对象
@property(nonatomic, strong) XMagic *xMagicKit;  //xmagic对象
//提示信息
@property(nonatomic, strong) UILabel *tipsLabel;  //提示信息
@end


@implementation UGCKitRecordViewController
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(viewWillResignActive:)
    name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(viewDidBecomeActive:)
    name:UIApplicationDidBecomeActiveNotification object:nil];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationDidBecomeActiveNotification object:nil];
    [self.navigationController setNavigationBarHidden:_navigationBarHidden animated:NO];
    [super viewWillDisappear:animated];
}

// 退后台停止渲染
- (void)viewWillResignActive:(NSNotification *)noti {
    [self.xMagicKit onPause];
}

// 后台返回恢复动效
- (void)viewDidBecomeActive:(NSNotification *)noti {
    // 恢复动效
    [self.xMagicKit onResume];
}
//处理纹理，接入第三方美颜
- (GLuint)onPreProcessTexture:(GLuint)texture width:(CGFloat)width height:(CGFloat)height{
    if(!_xMagicKit){
           [self buildBeautySDK:(int)width and:(int)height texture:texture];
           self.heightF = height;
       }
       if(_xMagicKit != nil && self.heightF != height){
           [_xMagicKit setRenderSize:CGSizeMake(width, height)];
       }
       YTProcessInput *input = [[YTProcessInput alloc] init];
       input.textureData = [[YTTextureData alloc] init];
       input.textureData.texture = texture;
       input.textureData.textureWidth = width;
       input.textureData.textureHeight = height;
       input.dataType = kYTTextureData;
       
       EAGLContext* cloudContext = [EAGLContext currentContext];
       EAGLContext* xmagicContext = [self.xMagicKit getCurrentGlContext];
       
       if(cloudContext != xmagicContext){
           [EAGLContext setCurrentContext: xmagicContext];
       }
       
       YTProcessOutput *output =[self.xMagicKit process:input withOrigin:YtLightImageOriginTopLeft withOrientation:YtLightCameraRotation0];
       
       if(cloudContext != xmagicContext){
           [EAGLContext setCurrentContext: cloudContext];
       }
       
      return output.textureData.texture;

}

- (void)onLog:(YtSDKLoggerLevel) loggerLevel withInfo:(NSString * _Nonnull) logInfo{
    NSLog(@"[%ld]-%@", (long)loggerLevel, logInfo);
}
- (void)onAssetEvent:(id)event
{
    NSLog(@"asset event:%@", event);
}
- (void)onTipsEvent:(id)event
{
    
}
- (void)onAIEvent:(id)event
{
    NSDictionary *eventDict = (NSDictionary *)event;
    if (eventDict[@"face_info"] != nil) {
        NSArray *face_list = eventDict[@"face_info"];
        NSLog(@"face count %lu", (unsigned long)face_list.count);
    } else if (eventDict[@"hand_info"] != nil) {
        NSArray *hand_list = eventDict[@"hand_info"];
        NSLog(@"hand count %lu", (unsigned long)hand_list.count);
    } else if (eventDict[@"body_info"] != nil) {
        NSArray *body_list = eventDict[@"body_info"];
        NSLog(@"body count %lu", (unsigned long)body_list.count);
    }
}
//HB构建SDK，初始化接口
- (void)buildBeautySDK:(int)width and:(int)height texture:(unsigned)textureID {

    NSString *beautyConfigPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    beautyConfigPath = [beautyConfigPath stringByAppendingPathComponent:@"beauty_config.json"];
    NSFileManager *localFileManager=[[NSFileManager alloc] init];
    BOOL isDir = YES;
    NSDictionary * beautyConfigJson = @{};
    if ([localFileManager fileExistsAtPath:beautyConfigPath isDirectory:&isDir] && !isDir) {
        NSString *beautyConfigJsonStr = [NSString stringWithContentsOfFile:beautyConfigPath encoding:NSUTF8StringEncoding error:nil];
        NSError *jsonError;
        NSData *objectData = [beautyConfigJsonStr dataUsingEncoding:NSUTF8StringEncoding];
        beautyConfigJson = [NSJSONSerialization JSONObjectWithData:objectData
        options:NSJSONReadingMutableContainers error:&jsonError];
    }
    NSDictionary *assetsDict = @{@"core_name":@"LightCore.bundle",
            @"root_path":[[NSBundle mainBundle] bundlePath],
            @"plugin_3d":@"Light3DPlugin.bundle",
            @"plugin_hand":@"LightHandPlugin.bundle",
            @"plugin_segment":@"LightSegmentPlugin.bundle",

            @"beauty_config":beautyConfigJson
    };

    // Init beauty kit
    self.xMagicKit = [[XMagic alloc] initWithRenderSize:CGSizeMake(width,height) assetsDict:assetsDict];
    // Register log
    [self.xMagicKit registerSDKEventListener:self];
    [self.xMagicKit registerLoggerListener:self withDefaultLevel:YT_SDK_VERBOSE_LEVEL];
    //去掉磨皮
    [self.xMagicKit configPropertyWithType:@"beauty" withName:@"beauty.smooth" withData:@"0.0" withExtraInfo:nil];

//    _vBeauty.beautyKitRef = self.xMagicKit;
    [_vBeauty setXMagic:self.xMagicKit];
    _vBeauty.viewController = self;
    __weak __typeof(self)weakSelf = self;
    _vBeauty.itemSelectedBlock = ^() {
        __strong typeof(self) strongSelf = weakSelf;
        dispatch_async(dispatch_get_main_queue(), ^{
            __strong typeof(self) strongSelf = weakSelf;
            strongSelf.tipsLabel.hidden = YES;
        });
    };
    [_vBeauty updateAllBeautyValue];
}

- (instancetype)initWithConfig:(UGCKitRecordConfig *)config theme:(UGCKitTheme *)theme
{
    if (self = [super initWithNibName:nil bundle:nil]) {
        _config = config ?: [[UGCKitRecordConfig alloc] init];
        _theme = theme ?: [UGCKitTheme sharedTheme];
        _preloadingVideos = _config.recoverDraft;
        [[TXUGCRecord shareInstance] setAspectRatio:_config.ratio];
        _coverPath = [NSTemporaryDirectory() stringByAppendingString:[[NSUUID UUID] UUIDString]];
        [self _commonInit];
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
        [self _commonInit];
    }
    return self;
}

- (void)_commonInit {
    _appForeground = YES;
    _isFrontCamera = YES;
    _vBeautyShow = NO;

    _whitenDepth = 2.7;

    _isCameraPreviewOn = NO;
    _recordState = RecordStateStopped;

    _currentRecordTime = 0;

    _speedMode = SpeedMode_Standard;

    _voiceChangerType = VIDOE_VOICECHANGER_TYPE_0; // 无变声
    _revertType = VIDOE_REVERB_TYPE_0; // 无混音效果

    [TXUGCRecord shareInstance].recordDelegate = self;

    _bgmListVC = [[UGCKitBGMListViewController alloc] initWithTheme:_theme];
    [_bgmListVC setBGMControllerListener:self];
    _joinVideoPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"outputJoin.mp4"];

    self.captureMode = CaptureModeTap;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    _initData = FALSE;
    self.xMagicKit = nil;
    self.heightF = 0;
    [self initUI];
//    buildBeautySDK
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
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    if (_isCameraPreviewOn == NO) {
        [self startCameraPreview];
    } else {
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

//- (void)viewWillDisappear:(BOOL)animated
//{
//    [super viewWillDisappear:animated];
//    [self.navigationController setNavigationBarHidden:_navigationBarHidden animated:NO];
//}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    if (self->_isFromMusicSelectVC) {
        return;
    }
    [self stopCameraPreview];
}

- (UIStatusBarStyle)preferredStatusBarStyle
{
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
    
    if (0 == _config.chorusVideos.count
        && UGCKitRecordStyleRecord != _config.recordStyle) {
        _config.recordStyle = UGCKitRecordStyleRecord;
    }
    else if (_config.chorusVideos.count < 2
               && UGCKitRecordStyleTrio == _config.recordStyle) {
        _config.chorusVideos = @[_config.chorusVideos.firstObject,
                                 _config.chorusVideos.firstObject];
    }
    // 视频界面
    UIView *videoContainer = [[UIView alloc] initWithFrame:self.view.bounds];
    videoContainer.autoresizingMask = UIViewAutoresizingFlexibleWidth
                                    | UIViewAutoresizingFlexibleHeight;
    [self.view addSubview:videoContainer];
    
    NSString *recordVideoPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"outputRecord.mp4"];
    _previewController = [[UGCKitRecordPreviewController alloc] initWithContainerView:videoContainer
                                                                  recordStyle:_config.recordStyle
                                                                 chorusVideos:_config.chorusVideos
                                                                  recordVideo:recordVideoPath];

    CGFloat top = [UIApplication sharedApplication].statusBarFrame.size.height + 25;
    CGFloat centerY = top;
    
    UIButton *(^allocButtonBlock)(UIImage *, SEL, NSString *) = ^(UIImage *bgImage, SEL action, NSString *title) {
        UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
        button.bounds = CGRectMake(0, 0, BUTTON_CONTROL_SIZE, BUTTON_CONTROL_SIZE);
        button.titleLabel.font = [UIFont systemFontOfSize:12];
        [button addTarget:self action:action forControlEvents:UIControlEventTouchUpInside];
        [button setTitleColor:self->_theme.titleColor forState:UIControlStateNormal];
        [button setBackgroundImage:bgImage forState:UIControlStateNormal];
        [button setTitle:title forState:UIControlStateNormal];
        return button;
    };
    // “下一步”按钮
    _btnNext = allocButtonBlock(_theme.nextIcon, @selector(onNext:), [_theme localizedString:@"UGCKit.Common.Next"]);
    _btnNext.hidden = YES;
    [_btnNext sizeToFit];
    _btnNext.center = CGPointMake(CGRectGetWidth(self.view.bounds) - 10 - CGRectGetWidth(_btnNext.frame) / 2 , centerY);
    [self.view addSubview:_btnNext];
    // "更换视频"按钮
    _btnChangeVideo = allocButtonBlock(_theme.editChooseVideoIcon, @selector(onChangeVideo:), [_theme localizedString:@"UGCKit.Common.Choose"]);
    _btnChangeVideo.bounds = CGRectMake(0, 0, 70, 30);
    _btnChangeVideo.center = _btnNext.center;
    _btnChangeVideo.hidden = YES;
    [self.view addSubview:_btnChangeVideo];

    // 主界面控件浮层
    _controlView = [[UGCKitRecordControlView alloc] initWithFrame:CGRectMake(0, CGRectGetMaxY(_btnNext.frame)+30,
                                                                             CGRectGetWidth(self.view.bounds),
                                                                             CGRectGetHeight(self.view.bounds) - CGRectGetMaxY(_btnNext.frame)-30)
                                                      minDuration:_config.minDuration
                                                      maxDuration:_config.maxDuration];
    _controlView.theme = _theme;
    _controlView.speedControlEnabled = UGCKitRecordStyleRecord == _config.recordStyle;
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

    // 比例按钮配置
    _controlView.videoRatio = _config.ratio;
    
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
    
    if (UGCKitRecordStyleDuet == _config.recordStyle) { // 分屏合拍
        videoContainer.frame = CGRectMake(0, CGRectGetMaxY(_btnNext.frame) + 20,
                                          CGRectGetWidth(self.view.bounds),
                                          CGRectGetHeight(self.view.bounds) / 2);
    }
    [self refreshChrousUI];
    [self refreshRecordDuration];
}

- (void)refreshChrousUI
{
    if (UGCKitRecordStyleRecord == _config.recordStyle) return;
    
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
    _controlView.countDownModeEnabled = YES;

    _btnChangeVideo.hidden = NO;
    
    //用于模仿视频和录制视频的合成
    _videoJoiner = [[TXVideoJoiner alloc] initWithPreview:nil];
    _videoJoiner.joinerDelegate = self;
}

- (void)refreshRecordDuration
{
    if (UGCKitRecordStyleRecord == _config.recordStyle) return;
    
    TXVideoInfo *info = nil;
    for (NSString *videoPath in _config.chorusVideos) {
        TXVideoInfo *videoInfo = [TXVideoInfoReader getVideoInfo:videoPath];
        if (!info || videoInfo.duration < info.duration) {
            info = videoInfo;
        }
    }
    _config.minDuration = info.duration;
    _config.maxDuration = info.duration;
    
    [_controlView setMinDuration:info.duration maxDuration:info.duration];
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
    _musicView = [[UGCKitVideoRecordMusicView alloc] initWithFrame:CGRectMake(0, self.view.ugckit_bottom - 268 * kScaleY, self.view.ugckit_width, 268 * kScaleY) needEffect:YES theme:_theme];
    _musicView.delegate = self;
    _musicView.hidden = YES;
    [self.view addSubview:_musicView];
    _musicView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    return _musicView;
}

#pragma mark ---- Video Beauty UI ----
-(void)initBeautyUI
{
    UIEdgeInsets gSafeInset;
#if __IPHONE_11_0 && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_11_0
    if(gSafeInset.bottom > 0){
    }
    if (@available(iOS 11.0, *)) {
        gSafeInset = [UIApplication sharedApplication].keyWindow.safeAreaInsets;
    } else
#endif
    {
        gSafeInset = UIEdgeInsetsZero;
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        //美颜选项界面
        _vBeauty = [[BeautyView alloc] init];
        [self.view addSubview:_vBeauty];
        [_vBeauty mas_makeConstraints:^(MASConstraintMaker *make) {
            make.width.mas_equalTo(self.view);
            make.centerX.mas_equalTo(self.view);
            make.height.mas_equalTo(254);
            if(gSafeInset.bottom > 0.0){  // 适配全面屏
                make.bottom.mas_equalTo(self.view.mas_bottom).mas_offset(0);
            } else {
                make.bottom.mas_equalTo(self.view.mas_bottom).mas_offset(-10);
            }
        }];
        _vBeauty.hidden = YES;
    });
}

- (void)setSelectedSpeed:(SpeedMode)tag
{
    [_controlView setSelectedSpeed:tag];
}

-(void)setSpeedBtnHidden:(BOOL)hidden{
    if (UGCKitRecordStyleRecord != _config.recordStyle) hidden = YES;
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
    __weak __typeof(self) wself = self;
    [self pauseRecord:^{
        __strong __typeof(wself) strongSelf = wself;
        [strongSelf _goBack];
    }];
}

- (void)_goBack {
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

- (IBAction)onNext:(UIButton *)sender
{
    self.captureModeView.userInteractionEnabled = YES;
    _controlView.btnCountDown.enabled = YES;
    _controlView.recordButtonSwitchControl.enabled = YES;
    
    if (_captureMode != CaptureModePress) {
        [_controlView setRecordButtonStyle:UGCKitRecordButtonStyleRecord];
    }
    _controlView.controlButtonsHidden = NO;
    if (UGCKitRecordStyleRecord != _config.recordStyle) {
        _controlView.btnMusic.hidden = YES;
        _controlView.btnRatioGroup.hidden = YES;
        _controlView.btnAudioEffect.hidden = YES;
    }
    
    [self _finishRecord];
}

- (void)_finishRecord {
    UIButton *nextButton = _btnNext;
    nextButton.hidden = YES;
    [self stopRecordAndComplete:^(int result){
        nextButton.hidden = NO;
    }];
}

- (IBAction)onChangeVideo:(UIButton *)sender
{
    UGCKitMediaPickerConfig *config = [[UGCKitMediaPickerConfig alloc] init];
    config.mediaType = UGCKitMediaTypeVideo;
    config.maxItemCount = 1;
    
    UGCKitMediaPickerViewController *pickerController = [[UGCKitMediaPickerViewController alloc] initWithConfig:config theme:_theme];
    UGCKitNavControllerPrivate *navController = [[UGCKitNavControllerPrivate alloc] initWithRootViewController:pickerController];
    navController.supportedOrientations  = self.supportedInterfaceOrientations;
    navController.modalPresentationStyle = UIModalPresentationFullScreen;
    WEAKIFY(self);
    pickerController.completion = ^(UGCKitResult *result) {
        if (!result.cancelled && result.code == 0) {
            [weak_self doChangeVideo:result];
        } else {
            NSLog(@"isCancelled: %c, failed: %@", result.cancelled ? 'y' : 'n', result.info[NSLocalizedDescriptionKey]);
        }
        [weak_self dismissViewControllerAnimated:YES completion:nil];
    };
    [self presentViewController:navController animated:YES completion:NULL];
}

- (void)doChangeVideo:(UGCKitResult *)result
{
    if (![result.media.videoAsset isKindOfClass:[AVURLAsset class]]) return;
    
    NSString *videoPath = ((AVURLAsset *)result.media.videoAsset).URL.path;
    if (!videoPath) {
        videoPath = @"";
    }
    [self.previewController changeChorusVideo:videoPath];
    
    if (UGCKitRecordStyleDuet == _config.recordStyle) {
        _config.chorusVideos = @[videoPath];
    } else if (UGCKitRecordStyleTrio == _config.recordStyle) {
        _config.chorusVideos = @[videoPath, videoPath];
    }
    [self refreshRecordDuration];
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
        [self presentViewController:nv animated:YES completion:^{
            self->_isFromMusicSelectVC = YES;
        }];
        [_bgmListVC loadBGMList];
    }
}

- (IBAction)onBtnRatioClicked:(UGCKitSlideButton *)sender
{
    TXVideoAspectRatio ratio = sender.buttons[sender.selectedIndex].tag;
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
    if (0 == [TXUGCRecord shareInstance].partsManager.getVideoPathList.count) {
        return;
    }
    
    if (0 == _deleteCount) {
        [_controlView.progressView prepareDeletePart];
    }else{
        [_controlView.progressView comfirmDeletePart];
        [[TXUGCRecord shareInstance].partsManager deleteLastPart];
        _isBackDelete = YES;
        if (0 == [TXUGCRecord shareInstance].partsManager.getVideoPathList.count) {
            _bgmRecording = NO;
            _BGMPath = nil;
            [[TXUGCRecord shareInstance] stopBGM];
            [_bgmListVC clearSelectStatus];
        }
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
    if(!_vBeautyShow && ![[NSUserDefaults standardUserDefaults] boolForKey:@"beauty"]){
        [self openDialog];
    }else{
        [self showBeauty];
    }
}

-(void)showBeauty{
    _vBeautyShow = !_vBeautyShow;
    _musicView.hidden = YES;
    _vBeauty.hidden = !_vBeautyShow;
    [self hideBottomView:_vBeautyShow];
}

-(void)openDialog{
    // 初始化UIAlertController
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"" message:@"" preferredStyle:UIAlertControllerStyleAlert];

    //修改title字体及颜色
    NSMutableAttributedString *titleStr = [[NSMutableAttributedString alloc] initWithString:[_theme localizedString:@"UGCKit.Common.openBeautyTitle"]];
    [titleStr addAttribute:NSForegroundColorAttributeName
    value:[UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:0.9/1.0]
    range:NSMakeRange(0, titleStr.length)];
    [titleStr addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:20] range:NSMakeRange(0, titleStr.length)];
    [alertController setValue:titleStr forKey:@"attributedTitle"];

    // 修改message字体及颜色
    NSMutableAttributedString *messageStr = [[NSMutableAttributedString alloc] initWithString:[_theme localizedString:@"UGCKit.Common.openBeautyMsg"]];
    [messageStr addAttribute:NSForegroundColorAttributeName
    value:[UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:0.3/1.0]
    range:NSMakeRange(0, messageStr.length)];
    [messageStr addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:18] range:NSMakeRange(0, messageStr.length)];
    [alertController setValue:messageStr forKey:@"attributedMessage"];

    // 添加UIAlertAction
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:
    [_theme localizedString:@"UGCKit.Common.openBeautyAllow"]
    style:UIAlertActionStyleDestructive handler:^(UIAlertAction * _Nonnull action) {
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"beauty"];
        [self showBeauty];
    }];
    // KVC修改字体颜色
    [sureAction setValue:[UIColor colorWithRed:241/255.0 green:66/255.0 blue:87/255.0 alpha:1/1.0] forKey:@"_titleTextColor"];
    [alertController addAction:sureAction];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:
    [_theme localizedString:@"UGCKit.Common.openBeautyForbidden"]
    style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action){
        NSLog(@"取消");
    }];
    [cancelAction setValue:[UIColor blackColor] forKey:@"_titleTextColor"];
    [alertController addAction:cancelAction];
    [self presentViewController:alertController animated:YES completion:nil];
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
    _controlView.controlButtonsHidden = YES;
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
#pragma mark - Control Visibility
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
    [[TXUGCRecord shareInstance] stopBGM];
    int result = [[TXUGCRecord shareInstance] startRecord:[_coverPath stringByAppendingString:@".mp4"]
                                                coverPath:[_coverPath stringByAppendingString:@".png"]];
    [UGCKitReporter report:UGCKitReportItem_startrecord userName:nil code:result msg:result == 0 ? @"启动录制成功" : @"启动录制失败"];
    NSString *licenseInfo = [TXUGCBase getLicenceInfo];
    if(0 != result)
    {
        if(-3 == result) [self alert:[_theme localizedString:@"UGCKit.Record.HintLaunchRecordFailed"] msg:[_theme localizedString:@"UGCKit.Record.ErrorCamera"]];
        if(-4 == result) [self alert:[_theme localizedString:@"UGCKit.Record.HintLaunchRecordFailed"] msg:[_theme localizedString:@"UGCKit.Record.ErrorMIC"]];
        if(-5 == result) [self alert:[_theme localizedString:@"UGCKit.Record.HintLaunchRecordFailed"] msg:[_theme localizedString:@"UGCKit.Record.ErrorLicense"]];
    }else{
        //如果设置了BGM，播放BGM
        [self playBGM:_bgmBeginTime toTime:MAXFLOAT recordSpeed:[self getRecordSpeed]];

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
        if (UGCKitRecordStyleRecord != _config.recordStyle) {
            [self.previewController startPlayChorusVideos:_recordTime
                                                   toTime:_config.maxDuration];
        }
    }
}

- (TXVideoRecordSpeed)getRecordSpeed {
    TXVideoRecordSpeed speed;
    switch (_controlView.speedMode) {
        case SpeedMode_VerySlow:
            speed = VIDEO_RECORD_SPEED_SLOWEST;
            break;
        case SpeedMode_Slow:
            speed = VIDEO_RECORD_SPEED_SLOW;
            break;
        case SpeedMode_Standard:
            speed = VIDEO_RECORD_SPEED_NOMAL;
            break;
        case SpeedMode_Quick:
            speed = VIDEO_RECORD_SPEED_FAST;
            break;
        case SpeedMode_VeryQuick:
            speed = VIDEO_RECORD_SPEED_FASTEST;
            break;
    }
    return speed;
}

- (void)_pauseAndAddMark:(void(^)(void))completion {
    [self pauseBGM];
    NSUInteger countBefore = [TXUGCRecord shareInstance].partsManager.getVideoPathList.count;
    UGCKitRecordControlView *controlView = _controlView;
    void (^afterPause)(void)= ^{
        NSUInteger count = [TXUGCRecord shareInstance].partsManager.getVideoPathList.count;
        if (count != countBefore) {
            [controlView.progressView pause];
        }
        if (completion) {
            completion();
        }
    };
    if ([[TXUGCRecord shareInstance] pauseRecord:afterPause] < 0)  {
        afterPause();
    }
}

- (void)pauseRecord {
    [self pauseRecord:nil];
}

- (void)pauseRecord:(void(^)(void))completion {
    self.captureModeView.userInteractionEnabled = YES;
    _controlView.btnCountDown.enabled = YES;
    _controlView.recordButtonSwitchControl.enabled = YES;
    __weak __typeof(self) weakSelf = self;
    UGCKitRecordControlView *controlView = _controlView;
    controlView.btnStartRecord.enabled = NO;
    if (_captureMode != CaptureModePress) {
        [_controlView setRecordButtonStyle:UGCKitRecordButtonStyleRecord];
    }
    [self setSpeedBtnHidden:NO];
    _recordState = RecordStatePaused;
    [self.previewController stopPlayChorusVideos];
    [self _pauseAndAddMark:^{
        __strong __typeof(weakSelf) strongSelf = weakSelf;
        if (strongSelf) {
            controlView.controlButtonsHidden = NO;
            [strongSelf saveVideoClipPathToPlist];
            controlView.btnStartRecord.enabled = YES;
            if (UGCKitRecordStyleRecord != strongSelf->_config.recordStyle) {
                strongSelf->_controlView.btnMusic.hidden = YES;
                strongSelf->_controlView.btnRatioGroup.hidden = YES;
                strongSelf->_controlView.btnAudioEffect.hidden = YES;
            }
        }
        if (completion) {
            completion();
        }
    }];
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

    [[TXUGCRecord shareInstance] resumeRecord];
    if (_bgmRecording) {
        [self resumeBGM];
    }else{
        [self playBGM:_bgmBeginTime toTime:MAXFLOAT recordSpeed:[self getRecordSpeed]];
        _bgmRecording = YES;
    }
    
    [self _configButtonToPause];

    if (_deleteCount == 1) {
        [_controlView.progressView cancelDelete];
        _deleteCount = 0;
    }
    [self setSpeedBtnHidden:YES];
    _controlView.recordButtonSwitchControl.enabled = NO;
    _recordState = RecordStateRecording;
    [self.previewController startPlayChorusVideos:_recordTime
                                           toTime:_config.maxDuration];
}

-(void)stopRecordAndComplete:(void(^)(int))completion
{
    if (_isCompletingRecord) {
        return;
    }
    _isCompletingRecord = YES;
    _btnNext.hidden = YES;
    _controlView.btnCountDown.enabled = YES;
    _controlView.recordButtonSwitchControl.enabled = YES;
    [_controlView setRecordButtonStyle:UGCKitRecordButtonStyleRecord];
    [self setSpeedBtnHidden:NO];
    [self.previewController stopPlayChorusVideos];
    //调用partsManager快速合成视频，不破坏录制状态，下次返回后可以接着录制（注意需要先暂停视频录制）
    __weak __typeof(self) weakSelf = self;
    if (_isStopRecord) {
        [[TXUGCRecord shareInstance].partsManager joinAllParts:self.previewController.recordVideoPath
                                                      complete:^(int result) {
            if (completion){
                completion(result);
            }
            [weakSelf onFinishJoinAllParts:result];
            self->_isCompletingRecord = NO;
        }];
    } else {
        if (_recordState == RecordStateRecording) {
            [self _pauseAndAddMark:^{
                __strong __typeof(weakSelf) self = weakSelf; if (self == nil) { return; }
                self->_recordState = RecordStatePaused;
                [self saveVideoClipPathToPlist];

                [[TXUGCRecord shareInstance].partsManager joinAllParts:self.previewController.recordVideoPath
                                                              complete:^(int result) {
                    if (completion){
                        completion(result);
                    }
                    [weakSelf onFinishRecord:result];
                    self->_isCompletingRecord = NO;
                }];
            }];
        } else {
            [[TXUGCRecord shareInstance].partsManager joinAllParts:self.previewController.recordVideoPath
                                                          complete:^(int result) {
                if (completion){
                    completion(result);
                }
                [weakSelf onFinishRecord:result];
                self->_isCompletingRecord = NO;
            }];
        }
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
    return YES;
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
        //        [[TXUGCRecord shareInstance] startCameraSimple:param preview:_previewController.videoRecordView];
        //自定义设置
        TXUGCCustomConfig * param = [[TXUGCCustomConfig alloc] init];
        param.videoResolution = _config.resolution;
        param.videoFPS = _config.fps;
        param.videoBitratePIN = _config.videoBitrate;
        param.GOP = _config.gop;
        param.audioSampleRate = _config.audioSampleRate;
        param.minDuration = _config.minDuration;
        param.maxDuration = _config.maxDuration + 2;
        param.frontCamera = _isFrontCamera;
        param.enableAEC = _config.AECEnabled;
        [[TXUGCRecord shareInstance] startCameraCustom:param preview:_previewController.videoRecordView];
        _isCameraPreviewOn = YES;
        [[TXUGCRecord shareInstance] setVideoProcessDelegate:self];
        if (_config.watermark) {
            UIImage *watermark = _config.watermark.image;
            CGRect watermarkFrame = _config.watermark.frame;
            [[TXUGCRecord shareInstance] setWaterMark:watermark normalizationFrame:watermarkFrame];
        } else {
            [[TXUGCRecord shareInstance] setWaterMark:nil normalizationFrame:CGRectZero];;
        }
        if (self->_isFromMusicSelectVC) {
            self->_isFromMusicSelectVC = NO;
            return;
        }
        
        if (UGCKitRecordStyleRecord == _config.recordStyle) {
            [[TXUGCRecord shareInstance] setVideoRenderMode:VIDEO_RENDER_MODE_ADJUST_RESOLUTION];
        } else {
            [[TXUGCRecord shareInstance] setVideoRenderMode:VIDEO_RENDER_MODE_FULL_FILL_SCREEN];
        }
        
        if (!self->_isFromMusicSelectVC) {
    
        }
        
#if POD_PITU
        [self motionTmplSelected:_materialID];
#endif
        //内存里面没有视频数据，重置美颜状态
        if ([TXUGCRecord shareInstance].partsManager.getVideoPathList.count == 0) {
            if (!self->_isFromMusicSelectVC) {
                [self resetBeautySettings];
            }
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
        if (self->_isFromMusicSelectVC) {
            self->_isFromMusicSelectVC = NO;
        }
    }
}

- (void)resetBeautySettings {
    [[[TXUGCRecord shareInstance] getBeautyManager] setFilter:nil];
    [[[TXUGCRecord shareInstance] getBeautyManager] setGreenScreenFile:nil];
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
    if (UGCKitRecordStyleRecord != _config.recordStyle) {
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

-(void)onFinishJoinAllParts:(int)result
{
    _btnNext.hidden = NO;
    if(0 != result){
        NSString * message = [NSString stringWithFormat:@"%@(%d)",
                              [_theme localizedString:@"UGCKit.Record.TryAgain"],
                              result];
        [self alert:[_theme localizedString:@"UGCKit.Media.HintVideoSynthesizeFailed"]
                msg:message];
        [UGCKitReporter report:UGCKitReportItem_videorecord userName:nil code:-1 msg:@"视频录制失败"];
    } else {
        int stopRet = [TXUGCRecord shareInstance].stopRecord;
        if (stopRet == 0) {
            _isStopRecord = YES;
            _recordState = RecordStateStopped;
        }
    }
}

-(void)onFinishRecord:(int)result
{
    _isStopRecord = NO;
    if (UGCKitRecordStyleRecord == _config.recordStyle) {
        [self stopCameraPreview];
        if (self.completion) {
            NSAssert(self.previewController.recordVideoPath != nil, @"unexpected");
            if (self.previewController.recordVideoPath != nil) {
                UGCKitResult *result = [[UGCKitResult alloc] init];;
                result.media = [UGCKitMedia mediaWithVideoPath:self.previewController.recordVideoPath];
                result.coverImage = [[UIImage alloc] initWithContentsOfFile:[_coverPath stringByAppendingString:@".png"]];
                self.completion(result);
            } else {
                self.completion(nil);
            }
        }
    } else {
        _btnNext.hidden = YES;
        [self joinVideos];
    }
    [UGCKitReporter report:UGCKitReportItem_videorecord userName:nil code:0 msg:@"视频录制成功"];
}

- (void)joinVideos
{
    NSString * recordVideoPath = self.previewController.recordVideoPath;
    if (0 == recordVideoPath.length
        || 0 == _config.chorusVideos.count
        || ![[NSFileManager defaultManager] fileExistsAtPath:recordVideoPath]
        || ![[NSFileManager defaultManager] fileExistsAtPath:_config.chorusVideos.firstObject]
        || (UGCKitRecordStyleTrio == _config.recordStyle
            && (_config.chorusVideos.count < 2 || ![[NSFileManager defaultManager] fileExistsAtPath:_config.chorusVideos[1]]))) {
        [self alert:[_theme localizedString:@"UGCKit.Media.HintVideoSynthesizeFailed"]
                msg:[_theme localizedString:@"UGCKit.Record.TryAgain"]];
        return;
    }
    
    CGFloat canvasWidth = 720 * 2, canvasHeight = 1280;
    CGFloat halfWidth   = canvasWidth / 2;
    NSArray *displayRects = @[[NSValue valueWithCGRect:CGRectMake(0, 0, halfWidth, canvasHeight)],
                              [NSValue valueWithCGRect:CGRectMake(halfWidth, 0, halfWidth, canvasHeight)]];
    if (UGCKitRecordStyleTrio == _config.recordStyle) {
        canvasWidth = 720;
        CGFloat oneThirdHeight = canvasHeight / 3;
        displayRects = @[[NSValue valueWithCGRect:CGRectMake(0, 0, canvasWidth, oneThirdHeight)],
                         [NSValue valueWithCGRect:CGRectMake(0, oneThirdHeight, canvasWidth, oneThirdHeight)],
                         [NSValue valueWithCGRect:CGRectMake(0, canvasHeight - oneThirdHeight, canvasWidth, oneThirdHeight)]];
    }
    
    if (0 == [_videoJoiner setVideoPathList:self.previewController.allVideoPaths]) {
        [_videoJoiner setSplitScreenList:displayRects canvasWidth:canvasWidth canvasHeight:canvasHeight];
        [_videoJoiner setVideoVolumes:self.previewController.allVideoVolumes];
        [_videoJoiner splitJoinVideo:VIDEO_COMPRESSED_720P videoOutputPath:_joinVideoPath];
        if (nil == _hud) {
            _hud = [UGCKitProgressHUD showHUDAddedTo:self.view animated:YES];
        } else {
            [self.view addSubview:_hud];
            [_hud showAnimated:YES];
        }
        _hud.mode = UGCKitProgressHUDModeText;
        _hud.label.text = [_theme localizedString:@"UGCKit.Media.VideoSynthesizing"];
    } else {
        [self alert:[_theme localizedString:@"UGCKit.Media.HintVideoSynthesizeFailed"]
                msg:[_theme localizedString:@"UGCKit.Record.VideoChorusNotSupported"]];
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
- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    if (_vBeautyShow)
    {
        _isCameraPreviewOn = YES;
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
            _musicView.hidden = YES;
            [self hideBottomView:NO];
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
-(void)onRecordProgress:(NSInteger)milliSecond;
{
    _recordTime =  milliSecond / 1000.0;
    BOOL shouldPause = _recordTime >= _config.maxDuration;
    [self updateRecordProgressLabel: _recordTime];
    
    BOOL isEmpty = milliSecond == 0;
    //录制过程中不能切换BGM, 不能改变声音效果，不能更换合拍视频
    if (UGCKitRecordStyleRecord == _config.recordStyle) {
        _btnChangeVideo.hidden = YES;
    } else {
        _btnChangeVideo.hidden = !isEmpty;
    }
    _controlView.btnRatioGroup.enabled = isEmpty;

    _controlView.btnMusic.enabled = isEmpty;
    _btnNext.hidden = milliSecond / 1000.0 < _config.minDuration;
    _controlView.btnAudioEffect.enabled = _controlView.btnMusic.enabled;
    //回删之后被模仿视频进度回退
    if (_isBackDelete && UGCKitRecordStyleRecord != _config.recordStyle) {
        [self.previewController seekChorusVideosToTime:_recordTime];
        _isBackDelete = NO;
    }
    if (shouldPause) {
        _isStopRecord = YES;
        // FIXME: 这里调用了SDK的Pause之后，Progress进度会爆发式的调用很多次（在录制过程中有拍照行为，没有拍照行为不会复现，需要检查下SDK层面的调用）
        [self pauseRecord:^{
            if (self->_config.autoComplete) {
                [self _finishRecord];
            }
        }];
    }
}

-(void)onRecordComplete:(TXUGCRecordResult*)result;
{
    // FIXME: 目前complete回调的触发，只有在调用 [[TXUGCRecord shareInstance] stopRecord] 时会触发。
    // 当前页面的结束录制，用 [[TXUGCRecord shareInstance].partsManager 的 joinAllParts 来控制，这里的回调不会触发。
    // 在当前页面Uinit的时候，会调用 stopRecord。
    if (_captureMode != CaptureModePress) {
        [_controlView setRecordButtonStyle:UGCKitRecordButtonStyleRecord];
    }
    if (_appForeground)
    {
        if (_currentRecordTime >= _config.minDuration)
        {
            if (result.retCode != UGC_RECORD_RESULT_FAILED && _isStopRecord) {
                [self onFinishRecord:(int)result.retCode];
            }else{
                [self toastTip:[_theme localizedString:@"UGCKit.Record.ErrorREC"]];
            }
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
-(void)onJoinProgress:(float)progress
{
    _hud.label.text = [NSString stringWithFormat:@"%@%d%%",[_theme localizedString:@"UGCKit.Media.VideoSynthesizing"], (int)(progress * 100)];
}
-(void)onJoinComplete:(TXJoinerResult *)result
{
    _btnNext.hidden = NO;
    [_hud hideAnimated:YES];
    if (_appForeground && result.retCode == RECORD_RESULT_OK) {
        [self stopCameraPreview];
        if (self.completion) {
            UGCKitResult *result = [[UGCKitResult alloc] init];
            result.media = [UGCKitMedia mediaWithVideoPath:_joinVideoPath];
            self.completion(result);
        }
    }else{
        [self alert:[_theme localizedString:@"UGCKit.Media.HintVideoSynthesizeFailed"] msg:result.descMsg];
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

#pragma mark TCBGMControllerListener
-(void)onBGMControllerPlay:(NSObject*) path{
    [self dismissViewControllerAnimated:YES completion:nil];
    if(path == nil) {
        _isScrollToStart = NO;
        return;
    }
    [self onSetBGM:path];
    _isScrollToStart = YES;
    [self playBGM:0 toTime:MAXFLOAT recordSpeed:VIDEO_RECORD_SPEED_NOMAL];
    dispatch_async(dispatch_get_main_queue(), ^(){
        self->_musicView.hidden = NO;
        [self hideBottomView:YES];
        if (self->_isScrollToStart) {
            [self->_musicView resetSiderView];
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
   
    [self presentViewController:nv animated:YES completion:^{
        self->_isFromMusicSelectVC = YES;
    }];
    [_bgmListVC loadBGMList];
}

-(void)onBtnMusicStoped
{
    _BGMPath = nil;
    _bgmRecording = NO;
    [_bgmListVC clearSelectStatus];
    [[TXUGCRecord shareInstance] stopBGM];
    _musicView.hidden = YES;
    [self hideBottomView:NO];
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
    [self playBGM:_BGMDuration * startPercent toTime:_BGMDuration * endPercent recordSpeed:VIDEO_RECORD_SPEED_NOMAL];
    dispatch_async(dispatch_get_main_queue(), ^(){
        self->_musicView.hidden = NO;
        [self hideBottomView:YES];
    });
    
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
    
    [_musicView freshCutView:_BGMDuration];
    _bgmRecording = NO;
    dispatch_async(dispatch_get_main_queue(), ^{
        [UGCKitProgressHUD hideHUDForView:self.view animated:YES];
    });
}

-(void)playBGM:(CGFloat)beginTime toTime:(CGFloat)endTime recordSpeed:(TXVideoRecordSpeed)speed
{
    if (_BGMPath != nil) {
        
        if (endTime == MAXFLOAT) {
            endTime = _BGMDuration;
        }
        
        [[TXUGCRecord shareInstance] stopBGM];
        
        //试听音乐这里要把RecordSpeed 设置为VIDEO_RECORD_SPEED_NOMAL，否则音乐可能会出现加速或则慢速播现象
        [[TXUGCRecord shareInstance] setRecordSpeed:speed];
        
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
- (void)toastTip:(NSString*)toastInfo
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
    _isStopRecord = YES;
    [[TXUGCRecord shareInstance] stopRecord];
    [[TXUGCRecord shareInstance] stopCameraPreview];
    [[TXUGCRecord shareInstance].partsManager deleteAllParts];
    [[TXUGCRecord shareInstance] setReverbType:VIDOE_REVERB_TYPE_0];
    [[TXUGCRecord shareInstance] setVoiceChangerType:VIDOE_VOICECHANGER_TYPE_0];
    _recordState = RecordStateStopped;
    //    [TCUtil removeCacheFile:_recordVideoPath];
    //    [TCUtil removeCacheFile:_joinVideoPath];
    if (_BGMPath) {
        _BGMPath = nil;
        _bgmRecording = NO;
        [[TXUGCRecord shareInstance] stopBGM];
    }
}

- (void)dealloc
{
    [self uinit];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    if (self.xMagicKit) {
        [self.xMagicKit clearListeners];
        [self.xMagicKit deinit];
        self.xMagicKit = nil;
        _initData = FALSE;
    }
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
        _autoComplete = YES;
    }
    return self;
}
@end


@implementation UGCKitRecordPreviewController
{
    NSArray<NSString *> *_allVideoPaths;
    
    NSString *_recordVideoPath;
    
    UIView *_containerView;
    
    UIView *_videoRecordView;
    
    UGCKitRecordStyle _recordStyle;
    
    NSArray<TXVideoEditer *> *_videoPlayers;
}

- (instancetype)initWithContainerView:(UIView *)containerView
                          recordStyle:(UGCKitRecordStyle)recordStyle
                         chorusVideos:(NSArray<NSString *> *)chorusVideos
                          recordVideo:(NSString *)recordVideo
{
    if (!containerView) {
        return nil;
    }
    
    if (self = [super init]) {
        _containerView   = containerView;
        _recordStyle     = recordStyle;
        
        _recordVideoPath = recordVideo ? recordVideo : @"";
        
        _videoRecordView = [[UIView alloc] initWithFrame:containerView.bounds];
        _videoRecordView.autoresizingMask = UIViewAutoresizingFlexibleWidth
                                          | UIViewAutoresizingFlexibleHeight;
        [containerView addSubview:_videoRecordView];
        
        [self initChildViews:chorusVideos];
        
        [containerView bringSubviewToFront:_videoRecordView];
    }
    return self;
}

- (void)initChildViews:(NSArray<NSString *> *)chorusVideos
{
    if (UGCKitRecordStyleRecord == _recordStyle) {
        return;
    }
    
    NSMutableArray<TXVideoEditer *> *videoPlayers = [NSMutableArray arrayWithCapacity:chorusVideos.count];
    void (^allocVideoPlayer)(UIView *, CGRect, NSString *, UIViewAutoresizing) = ^(UIView *containerView,
                                                                                   CGRect frame, NSString *videoPath,
                                                                                   UIViewAutoresizing autoresizingMask) {
        UIView *playerView = [[UIView alloc] initWithFrame:frame];
        playerView.autoresizingMask = autoresizingMask;
        [containerView addSubview:playerView];
        
        TXPreviewParam *param = [[TXPreviewParam alloc] init];
        param.renderMode = PREVIEW_RENDER_MODE_FILL_SCREEN;
        param.videoView = playerView;
        
        TXVideoEditer *videoPlayer = [[TXVideoEditer alloc] initWithPreview:param];
        [videoPlayer setVideoPath:videoPath];
        [videoPlayers addObject:videoPlayer];
    };
    
    NSMutableArray<NSString *> *allVideoPaths = [NSMutableArray arrayWithCapacity:chorusVideos.count + 1];
    [allVideoPaths addObject:_recordVideoPath];
    
    if (UGCKitRecordStyleDuet == _recordStyle) { /// 分屏合拍模式
        CGRect viewRect = CGRectMake(0, 0,
                                     CGRectGetWidth(_containerView.frame) / 2,
                                     CGRectGetHeight(_containerView.frame));
        _videoRecordView.translatesAutoresizingMaskIntoConstraints = NO;
        _videoRecordView.frame = viewRect;
        _videoRecordView.autoresizingMask = UIViewAutoresizingFlexibleWidth
                                          | UIViewAutoresizingFlexibleHeight
                                          | UIViewAutoresizingFlexibleRightMargin;
        
        UIViewAutoresizing autoresizingMask = UIViewAutoresizingFlexibleWidth
                                            | UIViewAutoresizingFlexibleHeight
                                            | UIViewAutoresizingFlexibleLeftMargin;
        viewRect.origin.x = CGRectGetMaxX(viewRect);
        NSString *videoPath = chorusVideos.firstObject;
        allocVideoPlayer(_containerView, viewRect, videoPath, autoresizingMask);
        [allVideoPaths addObject:(videoPath ? videoPath : @"")];
    } else if (UGCKitRecordStyleTrio == _recordStyle) { /// 三屏合拍模式
        CGRect viewRect = CGRectMake(0,
                                     CGRectGetHeight(_containerView.frame) / 3,
                                     CGRectGetWidth(_containerView.frame),
                                     CGRectGetHeight(_containerView.frame) / 3);
        _videoRecordView.translatesAutoresizingMaskIntoConstraints = NO;
        _videoRecordView.frame = viewRect;
        _videoRecordView.autoresizingMask = UIViewAutoresizingFlexibleWidth
                                          | UIViewAutoresizingFlexibleHeight
                                          | UIViewAutoresizingFlexibleTopMargin
                                          | UIViewAutoresizingFlexibleBottomMargin;
        
        for (NSUInteger idx = 0; idx < 2; idx++) {
            NSString *videoPath = idx < chorusVideos.count ? chorusVideos[idx] : chorusVideos.firstObject;
            if (0 == idx) {
                CGRect playerRect = CGRectMake(0, CGRectGetMaxY(viewRect), viewRect.size.width, viewRect.size.height);
                UIViewAutoresizing autoresizingMask = UIViewAutoresizingFlexibleWidth
                                                    | UIViewAutoresizingFlexibleHeight
                                                    | UIViewAutoresizingFlexibleBottomMargin;
                allocVideoPlayer(_containerView, playerRect, videoPath, autoresizingMask);
                [allVideoPaths insertObject:(videoPath ? videoPath : @"") atIndex:0];
                
            } else {
                CGRect playerRect = CGRectMake(0, 0, viewRect.size.width, viewRect.size.height);
                UIViewAutoresizing autoresizingMask = UIViewAutoresizingFlexibleWidth
                                                    | UIViewAutoresizingFlexibleHeight
                                                    | UIViewAutoresizingFlexibleTopMargin;
                allocVideoPlayer(_containerView, playerRect, videoPath, autoresizingMask);
                [videoPlayers.lastObject setVideoVolume:0];
                [allVideoPaths addObject:(videoPath ? videoPath : @"")];
            }
        }
    }
    _videoPlayers  = videoPlayers;
    _allVideoPaths = allVideoPaths;
}

- (instancetype)init
{
    return nil;
}

- (NSArray<NSNumber *> *)allVideoVolumes
{
    if (UGCKitRecordStyleRecord == _recordStyle) {
        return @[@1];
    } else if (UGCKitRecordStyleDuet == _recordStyle) {
        return @[@0, @1];
    } else {
        return @[@1, @0, @0];
    }
}

- (void)changeChorusVideo:(NSString *)videoPath
{
    if (UGCKitRecordStyleRecord == _recordStyle || 0 == videoPath.length) {
        return;
    }
    
    for (TXVideoEditer *videoPlayer in _videoPlayers) {
        [videoPlayer stopPlay];
        [videoPlayer setVideoPath:videoPath];
        [videoPlayer previewAtTime:0];
    }
    
    if (UGCKitRecordStyleDuet == _recordStyle) {
        _allVideoPaths = @[_recordVideoPath, videoPath];
    } else if (UGCKitRecordStyleTrio == _recordStyle) {
        _allVideoPaths = @[videoPath, _recordVideoPath, videoPath];
    }
}

#pragma mark - start/stop play chorus videos

- (void)startPlayChorusVideos:(CGFloat)startTime toTime:(CGFloat)endTime
{
    for (TXVideoEditer *videoPlayer in _videoPlayers) {
        [videoPlayer startPlayFromTime:startTime toTime:endTime];
    }
}

- (void)stopPlayChorusVideos
{
    for (TXVideoEditer *videoPlayer in _videoPlayers) {
        [videoPlayer stopPlay];
    }
}

- (void)seekChorusVideosToTime:(CGFloat)time
{
    for (TXVideoEditer *videoPlayer in _videoPlayers) {
        [videoPlayer previewAtTime:time];
    }
}

@end


@implementation UGCKitNavControllerPrivate

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    return self.supportedOrientations;
}

- (UIStatusBarStyle)preferredStatusBarStyle
{
    return UIStatusBarStyleLightContent;
}

@end
