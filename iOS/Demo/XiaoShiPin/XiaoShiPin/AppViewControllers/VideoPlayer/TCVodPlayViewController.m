//
//  TCVodPlayViewController.m
//  TCLVBIMDemo
//
//  Created by annidyfeng on 2017/9/15.
//  Copyright © 2017年 tencent. All rights reserved.
//

#import "TCVodPlayViewController.h"
#import "TCVideoPublishController.h"
#import <mach/mach.h>
#import <SDWebImage/UIImageView+WebCache.h>
#import "AppDelegate.h"
#import "TCConstants.h"
#import <Accelerate/Accelerate.h>
//#import <UShareUI/UMSocialUIManager.h>
//#import <UMSocialCore/UMSocialCore.h>
#import "TCLoginModel.h"
#import "NSString+Common.h"
#import "TCPlayViewCell.h"
#import "TCUserInfoModel.h"
#import "SDKHeader.h"
#import "HUDHelper.h"
#import "TCUtil.h"
#import "UIView+Additions.h"
#import "UGCKitWrapper.h"

#define RTMP_URL    @"请输入或扫二维码获取播放地址"
#define CACHE_PLAYER  3
#define PLAY_CLICK @"PLAY_CLICK"       //当前播放器启动播放
#define PLAY_PREPARE @"PLAY_PREPARE"   //当前播放器收到 PLAY_PREPARE 事件
#define PLAY_REVIEW  @"PLAY_REVIEW"    //当前视频的审核状态，只有审核通过才能播放

typedef NS_ENUM(NSInteger,DragDirection){
    DragDirection_Down,
    DragDirection_Up,
};

@interface TCVodPlayViewController ()
@property (strong, nonatomic) UGCKitWrapper *ugcWrapper;
@end

@implementation TCVodPlayViewController
{
    TXLivePlayConfig*    _config;
    
    long long            _trackingTouchTS;
    BOOL                 _startSeek;
    BOOL                 _videoPause;
    BOOL                 _videoFinished;
    BOOL                 _appIsInterrupt;
    float                _sliderValue;
    BOOL                 _isInVC;
    NSString             *_logMsg;
    NSString             *_rtmpUrl;
    
    UIView               *_videoParentView;
    
    BOOL                 _isErrorAlert; //是否已经弹出了错误提示框，用于保证在同时收到多个错误通知时，只弹一个错误提示框
    BOOL                 _navigationBarHidden;
    BOOL                 _beginDragging;
    
    UITableView*         _tableView;
    NSArray*             _liveInfos;
    NSMutableArray*      _playerList;
    NSInteger            _liveInfoIndex;
   
    TCPlayViewCell *     _currentCell;
    TXVodPlayer *        _currentPlayer;
    DragDirection        _dragDirection;
    MBProgressHUD*       _hub;
}

-(id)initWithPlayInfoS:(NSArray<TCLiveInfo *>*)liveInfos liveInfo:(TCLiveInfo *)liveInfo videoIsReady:(videoIsReadyBlock)videoIsReady;
{
    self = [super initWithPlayInfo:liveInfo videoIsReady:videoIsReady];
    if (self) {
        _videoPause    = NO;
        _videoFinished = YES;
        _isInVC        = NO;
        _log_switch    = NO;
        _liveInfos     = liveInfos;
        _liveInfoIndex = [liveInfos indexOfObject:liveInfo];
        _playerList    = [NSMutableArray array];
        _isErrorAlert = NO;
        _dragDirection = DragDirection_Down;
        [self initPlayer];
        [self addNotify];
    }
    return self;
}

- (void)addNotify{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onAudioSessionEvent:) name:AVAudioSessionInterruptionNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onAppDidEnterBackGround:) name:UIApplicationDidEnterBackgroundNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onAppWillEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
}

-(void)viewDidLoad{
    [super viewDidLoad];
    _ugcWrapper = [[UGCKitWrapper alloc] initWithViewController:self theme:[UGCKitTheme sharedTheme]];
    self.view.backgroundColor = [UIColor blackColor];
    //    [self joinGroup];
    
    /*预加载UI 背景图*/
    UIImage *backImage =  self.liveInfo.userinfo.frontcoverImage;
    UIImage *clipImage = nil;
    if (backImage) {
        CGFloat backImageNewHeight = self.view.height;
        CGFloat backImageNewWidth = backImageNewHeight * backImage.size.width / backImage.size.height;
        UIImage *gsImage = [self gsImage:backImage withGsNumber:10];
        UIImage *scaleImage = [self scaleImage:gsImage scaleToSize:CGSizeMake(backImageNewWidth, backImageNewHeight)];
        clipImage = [self clipImage:scaleImage inRect:CGRectMake((backImageNewWidth - self.view.width)/2, (backImageNewHeight - self.view.height)/2, self.view.width, self.view.height)];
    }
    UIImageView *backgroundImageView = [[UIImageView alloc] initWithFrame:self.view.bounds];
    backgroundImageView.image = clipImage;
    backgroundImageView.contentMode = UIViewContentModeScaleToFill;
    backgroundImageView.backgroundColor = [UIColor blackColor];
    [self.view addSubview:backgroundImageView];
    
    //视频画面父view
    _videoParentView = [[UIView alloc] initWithFrame:self.view.bounds];
    //    _videoParentView.tag = FULL_SCREEN_PLAY_VIDEO_VIEW;
    //    [self.view addSubview:_videoParentView];
    const CGFloat rowHeight = SCREEN_HEIGHT;
    _tableView = [[UITableView alloc] initWithFrame:self.view.bounds style:UITableViewStylePlain];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    _tableView.pagingEnabled = YES;
    _tableView.showsVerticalScrollIndicator = NO;
    _tableView.showsHorizontalScrollIndicator = NO;
    _tableView.estimatedRowHeight = 0;
    _tableView.estimatedSectionFooterHeight = 0;
    _tableView.estimatedSectionHeaderHeight = 0;
    _tableView.rowHeight = rowHeight;// - statusBarHeight;
    if (@available(iOS 11, *)) {
        _tableView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    [self.view addSubview:_tableView];
    [_tableView reloadData];
    
    [_tableView setContentOffset:CGPointMake(0, rowHeight * _liveInfoIndex) animated:NO];
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:_liveInfoIndex inSection:0];
    //    [_tableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionTop animated:NO];
    _currentCell = [_tableView cellForRowAtIndexPath:indexPath];
    [self resumePlayer];
    
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    _isInVC = YES;
}

-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    _navigationBarHidden = self.navigationController.navigationBar.hidden;
    [self.navigationController setNavigationBarHidden:YES];

    if (_videoPause && _currentPlayer) {
        //这里如果是从录制界面，或则其他播放界面过来的，要重新startPlay，因为AudioSession有可能被修改了，导致当前视频播放有异常
        NSMutableDictionary *param = [self getPlayerParam:_currentPlayer];
        [_currentPlayer startPlay:param[@"playUrl"]];
        [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"pause"] forState:UIControlStateNormal];
        [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
        _videoPause = NO;
    }
}

-(void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:_navigationBarHidden];
    if (!_videoPause && _currentPlayer) {
        [self clickPlayVod];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


//在低系统（如7.1.2）可能收不到这个回调，请在onAppDidEnterBackGround和onAppWillEnterForeground里面处理打断逻辑
- (void)onAudioSessionEvent:(NSNotification *)notification
{
    NSDictionary *info = notification.userInfo;
    AVAudioSessionInterruptionType type = [info[AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];
    if (type == AVAudioSessionInterruptionTypeBegan) {
        if (_appIsInterrupt == NO) {
            if (!_videoPause) {
                [_currentPlayer pause];
            }
            _appIsInterrupt = YES;
        }
    }else{
        AVAudioSessionInterruptionOptions options = [info[AVAudioSessionInterruptionOptionKey] unsignedIntegerValue];
        if (options == AVAudioSessionInterruptionOptionShouldResume) {
            if (_appIsInterrupt == YES) {
                if (!_videoPause) {
                    [_currentPlayer resume];
                }
                _appIsInterrupt = NO;
            }
        }
    }
}

- (void)onAppDidEnterBackGround:(UIApplication*)app {
    if (_appIsInterrupt == NO) {
        if (!_videoPause) {
            [_currentPlayer pause];
        }
        _appIsInterrupt = YES;
    }
}

- (void)onAppWillEnterForeground:(UIApplication*)app {
    if (_appIsInterrupt == YES) {
        if (!_videoPause) {
            [_currentPlayer resume];
        }
        _appIsInterrupt = NO;
    }
}

- (void)initPlayer{
    int playerCount = 0;
    int liveIndex   = (int)_liveInfoIndex;
    int liveIndexOffset = - CACHE_PLAYER / 2;
    if (_liveInfoIndex <= CACHE_PLAYER / 2) {
        liveIndex = 0;
        liveIndexOffset = 0;
    }
    if (_liveInfoIndex >= _liveInfos.count - CACHE_PLAYER / 2 - 1) {
        liveIndex = (int)_liveInfos.count - CACHE_PLAYER;
        liveIndexOffset = 0;
    }
    while (playerCount < CACHE_PLAYER) {
        TXVodPlayer *player = [[TXVodPlayer alloc] init];
        player.isAutoPlay = NO;
        TCLiveInfo *info = _liveInfos[liveIndex + liveIndexOffset];
        NSString *playUrl = [self checkHttps:info.playurl];
        NSMutableDictionary *param = [NSMutableDictionary dictionary];
        [param setObject:player forKey:@"player"];
        [param setObject:playUrl forKey:@"playUrl"];
        [param setObject:@(NO) forKey:PLAY_CLICK];
        [param setObject:@(NO) forKey:PLAY_PREPARE];
        [param setObject:@(info.reviewStatus) forKey:PLAY_REVIEW];
        [_playerList addObject:param];
        playerCount ++;
        liveIndexOffset ++;
    }
}

- (void)resetPlayer{
    int liveIndexOffset = - CACHE_PLAYER / 2;
    for(NSMutableDictionary *playerParam in _playerList){
        //先停掉所有的播放器
        TXVodPlayer *player = playerParam[@"player"];
        if ([playerParam[PLAY_REVIEW] intValue] == ReviewStatus_Normal) {
            [player stopPlay];
            [player removeVideoWidget];
        }
        
        //播放器重新对应 -> playeUrl
        if (_liveInfoIndex + liveIndexOffset >= 0 && _liveInfoIndex + liveIndexOffset < _liveInfos.count) {
            TCLiveInfo *info = _liveInfos[_liveInfoIndex + liveIndexOffset];
            NSString *playUrl = [self checkHttps:info.playurl];
            [playerParam setObject:playUrl forKey:@"playUrl"];
            [playerParam setObject:@(NO) forKey:PLAY_CLICK];
            [playerParam setObject:@(NO) forKey:PLAY_PREPARE];
            [playerParam setObject:@(info.reviewStatus) forKey:PLAY_REVIEW];
        }
        liveIndexOffset ++;
    }
}

- (void)loadNextPlayer{
    //找到下一个player预加载
    int index = (int)[_playerList indexOfObject:[self getPlayerParam:_currentPlayer]];
    switch (_dragDirection) {
        case DragDirection_Down:
        {
            //向下拖动，预加载下一个播放器
            if (index < _playerList.count - 1) {
                NSMutableDictionary *param = _playerList[index + 1];
                if (![param[PLAY_CLICK] boolValue]) {
                    [self startPlay:param];
                }
            }
        }
            break;
        case DragDirection_Up:
        {
            //向上拖动，预加载上一个播放器
            if (index > 0) {
                NSMutableDictionary *param = _playerList[index - 1];
                if (![param[PLAY_CLICK] boolValue]) {
                    [self startPlay:param];
                }
            }
        }
            break;
            
        default:
            break;
    }
}

- (void)resumePlayer{
    //先暂停上一个播放器
    if (_currentPlayer) {
        [_currentPlayer seek:0];
        [_currentPlayer pause];
    }
    [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"start"] forState:UIControlStateNormal];
    
    //开启下一个播放器
    BOOL findPlayer = NO;
    for (int i = 0; i < _playerList.count; i ++) {
        NSMutableDictionary *playParam = _playerList[i];
        NSString *playUrl = [playParam objectForKey:@"playUrl"];
        if ([playUrl isEqualToString:[self playUrl]]) {
            findPlayer = YES;
            _currentPlayer = (TXVodPlayer *)[playParam objectForKey:@"player"];
            [_currentPlayer setupVideoWidget:_currentCell.videoParentView insertIndex:0];
//            [_currentPlayer setRenderRotation:HOME_ORIENTATION_DOWN];
            
            //判断播放器是否启动播放,如果没有，先启动播放
            if (![playParam[PLAY_CLICK] boolValue]) {
                [self startPlay:playParam];
            }
            
            //判断播放器是否收到 PLAY_PREPARE 事件，如果收到，直接resume播放，如果没收到，在播放回调里面resume播放
            if ([playParam[PLAY_PREPARE] boolValue]) {
                [_currentPlayer resume];
                [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"pause"] forState:UIControlStateNormal];
            }
            
            //边界检查，防止越界
            if (_liveInfoIndex < CACHE_PLAYER / 2 || _liveInfoIndex > _liveInfos.count - CACHE_PLAYER / 2 - 1) {
                break;
            }
            //缓存播放器切换
            if (i > CACHE_PLAYER / 2) {
                int needMove = i - CACHE_PLAYER / 2;
                for (int j = 0; j < needMove; j ++) {
                    NSMutableDictionary *oldParam = _playerList[j];
                    TXVodPlayer *player = [oldParam objectForKey:@"player"];
                    if ([oldParam[PLAY_REVIEW] intValue] == ReviewStatus_Normal) {
                        [player stopPlay];
                        [player removeVideoWidget];
                    }
                    
                    TCLiveInfo *liveInfo = _liveInfos[_liveInfoIndex + 1 + j];
                    NSString *playUrl = [self checkHttps:liveInfo.playurl];
                    NSMutableDictionary *newParam = [NSMutableDictionary dictionary];
                    [newParam setObject:player forKey:@"player"];
                    [newParam setObject:playUrl forKey:@"playUrl"];
                    [newParam setObject:@(NO) forKey:PLAY_CLICK];
                    [newParam setObject:@(NO) forKey:PLAY_PREPARE];
                    [newParam setObject:@(liveInfo.reviewStatus) forKey:PLAY_REVIEW];
                    [_playerList removeObject:oldParam];
                    [_playerList addObject:newParam];
                }
            }
            if (i < CACHE_PLAYER / 2){
                int needMove = CACHE_PLAYER / 2 - i;
                for (int j = 0; j < needMove; j ++) {
                    NSMutableDictionary *oldParam = _playerList[CACHE_PLAYER - 1 - j];
                    TXVodPlayer *player = [oldParam objectForKey:@"player"];
                    if ([oldParam[PLAY_REVIEW] intValue] == ReviewStatus_Normal) {
                        [player stopPlay];
                        [player removeVideoWidget];
                    }
                    
                    TCLiveInfo *liveInfo = _liveInfos[_liveInfoIndex - 1 - j];
                    NSString *playUrl = [self checkHttps:liveInfo.playurl];
                    NSMutableDictionary *newParam = [NSMutableDictionary dictionary];
                    [newParam setObject:player forKey:@"player"];
                    [newParam setObject:playUrl forKey:@"playUrl"];
                    [newParam setObject:@(NO) forKey:PLAY_CLICK];
                    [newParam setObject:@(NO) forKey:PLAY_PREPARE];
                    [newParam setObject:@(liveInfo.reviewStatus) forKey:PLAY_REVIEW];
                    [_playerList removeObject:oldParam];
                    [_playerList insertObject:newParam atIndex:0];
                }
            }
            //这里注意break，防止逻辑错误
            break;
        }
    }
    if (!findPlayer) {
        //重新对应 player <-> playUrl
        [self resetPlayer];
        
        //启动当前播放器
        NSMutableDictionary *playerParam = _playerList[CACHE_PLAYER / 2];
        _currentPlayer = playerParam[@"player"];
        [_currentPlayer setupVideoWidget:_currentCell.videoParentView insertIndex:0];
        [_currentPlayer setRenderRotation:HOME_ORIENTATION_DOWN];
        [self startPlay:playerParam];
    }
    
    //预加载下一个播放器
    [self loadNextPlayer];
}

-(BOOL)startPlay:(NSMutableDictionary *)playerParam{
    NSString *playUrl = playerParam[@"playUrl"];
    if (![self checkPlayUrl:playUrl]) {
        return NO;
    }
    
    TXVodPlayer *voidPlayer = (TXVodPlayer *)playerParam[@"player"];
    if(voidPlayer != nil)
    {
        TXVodPlayConfig *cfg = voidPlayer.config;
        if (cfg == nil) {
            cfg = [TXVodPlayConfig new];
        }
        cfg.cacheFolderPath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0] stringByAppendingString:@"/txcache"];
        cfg.maxCacheItems = 5;
        voidPlayer.config = cfg;
        
        voidPlayer.vodDelegate = self;
        voidPlayer.isAutoPlay = NO;
        voidPlayer.enableHWAcceleration = YES;
        [voidPlayer setRenderRotation:HOME_ORIENTATION_DOWN];
        [voidPlayer setRenderMode:RENDER_MODE_FILL_EDGE];
        voidPlayer.loop = YES;
        
        //经过审核的视频才启动播放
        if ([playerParam[PLAY_REVIEW] intValue] == ReviewStatus_Normal) {
            [playerParam setObject:@(YES) forKey:PLAY_CLICK];
            int result = [voidPlayer startPlay:playUrl];
            if( result != 0)
            {
                [self toastTip:[NSString stringWithFormat:@"%@%d", kErrorMsgRtmpPlayFailed, result]];
                [self closeVCWithRefresh:YES popViewController:YES];
                return NO;
            }
            [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
        }
    }
    _startSeek = NO;
    
    NSString* ver = [TXLiveBase getSDKVersionStr];
    _logMsg = [NSString stringWithFormat:@"rtmp sdk version: %@",ver];
    [_currentCell.logicView.logViewEvt setText:_logMsg];
    return YES;
}

-(BOOL)startVodPlay{
    [self clearLog];
    NSString* ver = [TXLiveBase getSDKVersionStr];
    _logMsg = [NSString stringWithFormat:@"rtmp sdk version: %@",ver];
    [_currentCell.logicView.logViewEvt setText:_logMsg];
    
    _currentPlayer.vodDelegate = self;
    NSMutableDictionary *playerParam = [self getPlayerParam:_currentPlayer];
    [playerParam setObject:@(NO) forKey:PLAY_PREPARE];
    [self resumePlayer];
    return YES;
}

- (void)stopRtmp{
    for (NSMutableDictionary *param in _playerList) {
        TXVodPlayer *player = param[@"player"];
        player.vodDelegate = nil;
        [player stopPlay];
        [player removeVideoWidget];
    }
    [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
}

- (NSString *)playUrl{
    TCLiveInfo *liveInfo = _liveInfos[_liveInfoIndex];
    NSString *playUrl = [self checkHttps:liveInfo.playurl];
    return playUrl;
}

- (NSMutableDictionary *)getPlayerParam:(TXVodPlayer *)player{
    for (NSMutableDictionary *param in _playerList) {
        if ([[param objectForKey:@"player"] isEqual:player]) {
            return param;
        }
    }
    return nil;
}

#pragma mark - UI EVENT
-(void)closeVC:(BOOL)isRefresh  popViewController:(BOOL)popViewController{
    [self closeVCWithRefresh:isRefresh popViewController:popViewController];
//    [UMSocialUIManager dismissShareMenuView];
}

- (void)closeVCWithRefresh:(BOOL)refresh popViewController: (BOOL)popViewController {
    [self stopRtmp];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    if (refresh) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter] postNotificationName:kTCLivePlayError object:self];
        });
    }
    if (popViewController) {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

-(void)clickPlayVod{
    if (!_videoFinished) {
        if (_videoPause) {
            [_currentPlayer resume];
            [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"pause"] forState:UIControlStateNormal];
            [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
        } else {
            [_currentPlayer pause];
            [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"start"] forState:UIControlStateNormal];
            [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
        }
        _videoPause = !_videoPause;
    }
    else {
        [_currentPlayer resume];
        [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"pause"] forState:UIControlStateNormal];
        [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    }
}

-(void)clickScreen:(UITapGestureRecognizer *)gestureRecognizer{
    //todo
}

- (void)clickLog:(UIButton*)btn {
    if (_log_switch == YES)
    {
        _currentCell.logicView.statusView.hidden = YES;
        _currentCell.logicView.logViewEvt.hidden = YES;
        [btn setImage:[UIImage imageNamed:@"log"] forState:UIControlStateNormal];
        _currentCell.logicView.cover.hidden = YES;
        _log_switch = NO;
    }
    else
    {
        _currentCell.logicView.statusView.hidden = NO;
        _currentCell.logicView.logViewEvt.hidden = NO;
        [btn setImage:[UIImage imageNamed:@"log2"] forState:UIControlStateNormal];
        _currentCell.logicView.cover.alpha = 0.5;
        _currentCell.logicView.cover.hidden = NO;
        _log_switch = YES;
    }
}

- (void)clickChorus:(UIButton *)button {
    if (self.onTapChorus) {
        self.onTapChorus(self);
    }
    if([TCLoginParam shareInstance].isExpired){
        // TODO: logout
//        [[AppDelegate sharedAppDelegate] enterLoginUI];
        return;
    }
    [TCUtil report:xiaoshipin_videochorus userName:nil code:0 msg:@"合唱事件"];
    if (_currentPlayer.isPlaying) {
        [self clickPlayVod];
    }
    _hub = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    _hub.mode = MBProgressHUDModeText;
    _hub.label.text = NSLocalizedString(@"TCVodPlay.VideoLoading", nil);
    
    __weak __typeof(self) weakSelf = self;
    NSMutableDictionary *playerParam = [self getPlayerParam:_currentPlayer];
    [TCUtil downloadVideo:playerParam[@"playUrl"] cachePath:nil process:^(CGFloat process) {
        [weakSelf onloadVideoProcess:process];
    } complete:^(NSString *videoPath) {
        [weakSelf onloadVideoComplete:videoPath];
    }];
}

-(void)onloadVideoProcess:(CGFloat)process {
    _hub.label.text = [NSString stringWithFormat: NSLocalizedString(@"TCVodPlay.VideoLoadingFmt", nil),(int)(process * 100)];
}

-(void)onloadVideoComplete:(NSString *)videoPath {
    if (videoPath) {
        UGCKitRecordConfig *config = [[UGCKitRecordConfig alloc] init];
        config.chorusVideos = @[videoPath];
        config.recordStyle = UGCKitRecordStyleDuet;
        [self.ugcWrapper showRecordViewControllerWithConfig:config];
//        UGCKitRecordViewController *vc = [[UGCKitRecordViewController alloc] initWithConfig:config theme:nil];
//        [self.navigationController pushViewController:vc animated:YES];
        [_hub hideAnimated:YES];
    }else{
        _hub.label.text = NSLocalizedString(@"TCVodPlay.VideoLoadFailed", nil);
        [_hub hideAnimated:YES afterDelay:1.0];
    }
}

#pragma mark UISlider - play seek
-(void)onSeek:(UISlider *)slider{
    float progress = slider.value;
    int intProgress = progress + 0.5;
    _currentCell.logicView.playLabel.text = [NSString stringWithFormat:@"%02d:%02d:%02d",(int)intProgress / 3600,(int)(intProgress / 60), (int)(intProgress % 60)];
    _sliderValue = slider.value;
}

-(void)onSeekBegin:(UISlider *)slider{
    _startSeek = YES;
    _videoPause = NO;
}

- (void)onSeekEnd:(UISlider *)slider {
    _startSeek = NO;
    _trackingTouchTS = [[NSDate date]timeIntervalSince1970]*1000;
    if (_sliderValue >= _currentPlayer.duration) {
        [_currentPlayer seek:0];
    } else {
        [_currentPlayer seek:_sliderValue];
    }
    
    [_currentPlayer resume];
    [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"pause"] forState:UIControlStateNormal];
}

#pragma mark TXVodPlayListener
-(void)onPlayEvent:(TXVodPlayer *)player event:(int)EvtID withParam:(NSDictionary*)param
{
    NSDictionary* dict = param;
    dispatch_async(dispatch_get_main_queue(), ^{
        //player 收到准备好事件，记录下状态，下次可以直接resume
        if (EvtID == PLAY_EVT_VOD_PLAY_PREPARED) {
            NSMutableDictionary *playerParam = [self getPlayerParam:player];
            [playerParam setObject:@(YES) forKey:PLAY_PREPARE];
            if ([_currentPlayer isEqual:player]){
                [player resume];
            }
        }

        //        //暂时不需要旋转逻辑
        //        if (EvtID == PLAY_EVT_CHANGE_RESOLUTION) {
        //            if (player.width > player.height) {
        //                [player setRenderRotation:HOME_ORIENTATION_RIGHT];
        //            }
        //        }

        //只处理当前播放器的Event事件
        if (![_currentPlayer isEqual:player]) return;
        [self report:EvtID];

        if (EvtID == PLAY_EVT_VOD_PLAY_PREPARED) {
            //收到PREPARED事件的时候 resume播放器
            [_currentPlayer resume];
            [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"pause"] forState:UIControlStateNormal];

        } else if (EvtID == PLAY_EVT_PLAY_BEGIN) {
            _videoFinished = NO;

        } else if (EvtID == PLAY_EVT_RCV_FIRST_I_FRAME) {
            if (!_isInVC) {
                self.videoIsReady();
            }
        } else if (EvtID == PLAY_EVT_PLAY_PROGRESS && !_videoFinished) {
            if (_startSeek) return ;
            // 避免滑动进度条松开的瞬间可能出现滑动条瞬间跳到上一个位置
            long long curTs = [[NSDate date]timeIntervalSince1970]*1000;
            if (llabs(curTs - _trackingTouchTS) < 500) {
                return;
            }
            _trackingTouchTS = curTs;

            float progress = [dict[EVT_PLAY_PROGRESS] floatValue];
            int intProgress = progress + 0.5;
            _currentCell.logicView.playLabel.text = [NSString stringWithFormat:@"%02d:%02d:%02d",(int)(intProgress / 3600), (int)(intProgress / 60), (int)(intProgress % 60)];
            [_currentCell.logicView.playProgress setValue:progress];

            float duration = [dict[EVT_PLAY_DURATION] floatValue];
            int intDuration = duration + 0.5;
            if (duration > 0 && _currentCell.logicView.playProgress.maximumValue != duration) {
                [_currentCell.logicView.playProgress setMaximumValue:duration];
                _currentCell.logicView.playDuration.text = [NSString stringWithFormat:@"%02d:%02d:%02d",(int)(intDuration / 3600), (int)(intDuration / 60 % 60), (int)(intDuration % 60)];
            }
            return ;
        } else if (EvtID == PLAY_ERR_NET_DISCONNECT || EvtID == PLAY_EVT_PLAY_END) {
            //            [self stopRtmp];
            [_currentPlayer pause];
            _videoPause  = NO;
            _videoFinished = YES;
            [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
            [_currentCell.logicView.playProgress setValue:0];
            _currentCell.logicView.playLabel.text = LocalizationNotNeeded(@"00:00:00");

            [_currentCell.logicView.playBtn setImage:[UIImage imageNamed:@"start"] forState:UIControlStateNormal];
            [[UIApplication sharedApplication] setIdleTimerDisabled:NO];

        } else if (EvtID == PLAY_EVT_PLAY_LOADING){

        }

    });
}

-(void)report:(int)EvtID
{
    if (EvtID == PLAY_EVT_RCV_FIRST_I_FRAME) {
        [TCUtil report:xiaoshipin_vodplay userName:nil code:EvtID msg:@"视频播放成功"];
    }
    else if(EvtID == PLAY_ERR_NET_DISCONNECT){
        [TCUtil report:xiaoshipin_vodplay userName:nil code:EvtID msg:@"网络断连,且经多次重连抢救无效,可以放弃治疗,更多重试请自行重启播放"];
    }
    else if(EvtID == PLAY_ERR_GET_RTMP_ACC_URL_FAIL){
        [TCUtil report:xiaoshipin_vodplay userName:nil code:EvtID msg:@"获取加速拉流地址失败"];
    }
    else if(EvtID == PLAY_ERR_FILE_NOT_FOUND){
        [TCUtil report:xiaoshipin_vodplay userName:nil code:EvtID msg:@"播放文件不存在"];
    }
    else if(EvtID == PLAY_ERR_HEVC_DECODE_FAIL){
        [TCUtil report:xiaoshipin_vodplay userName:nil code:EvtID msg:@"H265解码失败"];
    }
    else if(EvtID == PLAY_ERR_HLS_KEY){
        [TCUtil report:xiaoshipin_vodplay userName:nil code:EvtID msg:@"HLS解码key获取失败"];
    }
    else if(EvtID == PLAY_ERR_GET_PLAYINFO_FAIL){
        [TCUtil report:xiaoshipin_vodplay userName:nil code:EvtID msg:@"获取点播文件信息失败"];
    }
}

-(void)onNetStatus:(TXVodPlayer *)player withParam:(NSDictionary*)param
{

}

-(void)appendLog:(NSString*)evt time:(NSDate*)date mills:(int)mil
{
    NSDateFormatter* format = [[NSDateFormatter alloc] init];
    format.dateFormat = @"hh:mm:ss";
    NSString* time = [format stringFromDate:date];
    NSString* log = [NSString stringWithFormat:@"[%@.%-3.3d] %@", time, mil, evt];
    if (_logMsg == nil) {
        _logMsg = @"";
    }
    _logMsg = [NSString stringWithFormat:@"%@\n%@", _logMsg, log];
    [_currentCell.logicView.logViewEvt setText:_logMsg];
}

#pragma mark UITableViewDelegate
//- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    return self.view.height;
//}
- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 0;
}
- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    return 0;
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section;
{
    return _liveInfos.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *reuseIdentifier = @"reuseIdentifier";
    TCPlayViewCell *cell = (TCPlayViewCell *)[_tableView dequeueReusableCellWithIdentifier:reuseIdentifier];
    if (cell == nil) {
        cell = [[TCPlayViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseIdentifier];
    }
    cell.delegate = self;
    [cell setLiveInfo:_liveInfos[indexPath.row]];
    return cell;
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
    _beginDragging = YES;
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    CGPoint rect = scrollView.contentOffset;
    NSInteger index = rect.y / self.view.height;
    if (_beginDragging && _liveInfoIndex != index) {
        if (index > _liveInfoIndex) {
            _dragDirection = DragDirection_Down;
        }else{
            _dragDirection = DragDirection_Up;
        }
        _liveInfoIndex = index;
        _currentCell = [_tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:_liveInfoIndex inSection:0]];
        [self resumePlayer];
        _beginDragging = NO;
    }
}

#pragma mark Utils
- (void)clearLog {
    _logMsg = @"";
    [_currentCell.logicView.statusView setText:@""];
    [_currentCell.logicView.logViewEvt setText:@""];
}

-(NSString *)checkHttps:(NSString *)playUrl{
    NSStringCheck(playUrl);
    if ([playUrl hasPrefix:@"http:"]) {
        playUrl = [playUrl stringByReplacingOccurrencesOfString:@"http:" withString:@"https:"];
    }
    return playUrl;
}

-(BOOL)checkPlayUrl:(NSString*)playUrl {
    if ([playUrl hasPrefix:@"https:"] || [playUrl hasPrefix:@"http:"]) {
        if ([playUrl rangeOfString:@".flv"].length > 0) {

        } else if ([playUrl rangeOfString:@".m3u8"].length > 0){

        } else if ([playUrl rangeOfString:@".mp4"].length > 0){

        } else {
            [self toastTip:@"播放地址不合法，点播目前仅支持flv,hls,mp4播放方式!"];
            return NO;
        }

    } else {
        [self toastTip:@"播放地址不合法，点播目前仅支持flv,hls,mp4播放方式!"];
        return NO;
    }


    return YES;
}

@end

