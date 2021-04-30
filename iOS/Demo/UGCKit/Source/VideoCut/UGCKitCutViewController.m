// Copyright (c) 2019 Tencent. All rights reserved.

#import "SDKHeader.h"
#import "UGCKitCutViewController.h"
#import <MediaPlayer/MPMediaPickerController.h>
#import <AVFoundation/AVFoundation.h>
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitProgressHUD.h"
#import "UGCKitVideoCutView.h"
#import "UGCKitPlayerView.h"
#import "UGCKitPhotoTransitionToolbar.h"
#import "UGCKitColorMacro.h"
#import "UGCKitSmallButton.h"
#import "UGCKitMem.h"
#import "UGCKitTheme.h"
#import "UGCKitReporterInternal.h"
#import "UGCKitSmallButton.h"

typedef  NS_ENUM(NSInteger,VideoType)
{
    VideoType_Video,
    VideoType_Picture,
};
@interface UGCKitCutViewController ()<TXVideoGenerateListener,VideoPreviewDelegate, VideoCutViewDelegate,TransitionViewDelegate>
@property(nonatomic,strong) TXVideoEditer *ugcEdit;
@property(nonatomic,strong) UGCKitPlayerView  *videoPreview;
@property CGFloat  duration;
@end

@implementation UGCKitCutViewController
{
    UGCKitTheme         *_theme;
    UGCKitMedia         *_media;
    NSMutableArray      *_cutPathList;
    NSString            *_videoOutputPath;
    
    CGFloat         _leftTime;
    CGFloat         _rightTime;

    UILabel*        _generationTitleLabel;
    UIView*         _generationView;
    UIProgressView* _generateProgressView;
    UIButton*       _generateCannelBtn;
    
    UIColor            *_barTintColor;
    
    NSString*          _filePath;
    unsigned long long _fileSize;
    BOOL               _navigationBarHidden;
    
    BOOL               _hasQuickGenerate;
    BOOL               _hasNomalGenerate;
    
    UGCKitVideoCutView*    _videoCutView;
    UGCKitPhotoTransitionToolbar*  _photoTransitionToolbar;
    UGCKitRangeContentConfig *_config;
    int              _renderRotation;
    
    CGFloat bottomToolbarHeight;
    CGFloat bottomInset;
}

- (instancetype)initWithMedia:(UGCKitMedia *)media theme:(UGCKitTheme *)theme;
{
    if (self = [super init]) {
        _theme = theme ?: [UGCKitTheme sharedTheme];
        _media = media;
        _cutPathList = [NSMutableArray array];
        _videoOutputPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"outputCut.mp4"];
        _config = [[UGCKitRangeContentConfig alloc] initWithTheme:_theme];
        _config.pinWidth = PIN_WIDTH;
        _config.thumbHeight = 50;
        _config.borderHeight = BORDER_HEIGHT;
        _config.imageCount = 15;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    _navigationBarHidden = self.navigationController.navigationBar.hidden;
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    self.navigationController.interactivePopGestureRecognizer.enabled = NO;
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    self.navigationController.navigationBar.hidden = _navigationBarHidden;
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [_videoPreview playVideo];
}


- (void)dealloc
{
    [_videoPreview removeNotification];
    _videoPreview = nil;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    bottomToolbarHeight = 52;
    bottomInset = 10;
    
    _videoPreview = [[UGCKitPlayerView alloc] initWithFrame:CGRectMake(0, 0, self.view.ugckit_width, self.view.ugckit_height) coverImage:nil theme:_theme];
    _videoPreview.delegate = self;
    [self.view addSubview:_videoPreview];
    CGFloat top = [UIApplication sharedApplication].statusBarFrame.size.height;
    if (@available(iOS 11, *)) {
        bottomInset += [UIApplication sharedApplication].keyWindow.safeAreaInsets.bottom;
    }
    if (kScaleY < 1) {
        bottomToolbarHeight = round(bottomToolbarHeight * kScaleY);
    }
    UILabel *barTitleLabel = [[UILabel alloc]initWithFrame:CGRectMake(0, 0 , 100, 44)];
    barTitleLabel.backgroundColor = [UIColor clearColor];
    barTitleLabel.font = [UIFont boldSystemFontOfSize:17];
    barTitleLabel.textColor = [UIColor whiteColor];
    barTitleLabel.textAlignment = NSTextAlignmentCenter;
    barTitleLabel.text = [_theme localizedString:@"UGCKit.Edit.VideoEdit"];
    self.navigationItem.titleView = barTitleLabel;
    self.view.backgroundColor = UIColor.blackColor;
    
    UIButton *goBackButton = [UGCKitSmallButton buttonWithType:UIButtonTypeCustom];
    [goBackButton setImage:_theme.backIcon forState:UIControlStateNormal];
    [goBackButton addTarget:self action:@selector(onBtnPopClicked) forControlEvents:UIControlEventTouchUpInside];
    goBackButton.frame = CGRectMake(15, 5 + top, 14 , 23);
    [self.view addSubview:goBackButton];
    
    CGFloat btnNextWidth = 70;
    CGFloat btnNextHeight = 30;
    UIButton *btnNext = [UIButton buttonWithType:UIButtonTypeCustom];
    btnNext.frame = CGRectMake(CGRectGetWidth(self.view.bounds) - 15 - btnNextWidth, 5 + top, btnNextWidth, btnNextHeight);
    [btnNext setTitle:[_theme localizedString:@"UGCKit.Common.Next"] forState:UIControlStateNormal];
    btnNext.titleLabel.font = [UIFont systemFontOfSize:14];
    [btnNext setBackgroundImage:_theme.nextIcon forState:UIControlStateNormal];
    [btnNext addTarget:self action:@selector(onBtnNextClicked) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:btnNext];
    
    //    CGFloat heightDist = 52 * kScaleY;
    //    _videoCutView = [[VideoCutView alloc] initWithFrame:CGRectMake(0, self.view.height - heightDist - 20 * kScaleY, self.view.width,heightDist) videoPath:_videoPath videoAssert:_videoAsset config:config];
    //    _videoCutView.delegate = self;
    //    [self.view addSubview:_videoCutView];
    
    TXPreviewParam *param = [[TXPreviewParam alloc] init];
    param.videoView = _videoPreview.renderView;
    param.renderMode =  PREVIEW_RENDER_MODE_FILL_EDGE;
    _ugcEdit = [[TXVideoEditer alloc] initWithPreview:param];
    _ugcEdit.generateDelegate = self;
    _ugcEdit.previewDelegate = _videoPreview;
    CGPoint rotateButtonCenter  = CGPointZero;
    //video
    if (_media.isVideo) {
        //    if (_videoAsset != nil) {
        AVAsset *asset = _media.videoAsset;
        [_ugcEdit setVideoAsset:asset];
        [self initVideoCutView];
        
        TXVideoInfo *videoMsg = [TXVideoInfoReader getVideoInfoWithAsset:asset];
        _fileSize   = videoMsg.fileSize;
        _duration = videoMsg.duration;
        _rightTime = _duration;
        _renderRotation = videoMsg.angle;
        rotateButtonCenter = CGPointMake(_videoCutView.ugckit_right - 20, _videoCutView.ugckit_top - 20);
    } else {
        //image
        //    if (_imageList != nil) {
        CGRect frame = CGRectMake(0, self.view.ugckit_height - bottomInset - bottomToolbarHeight, self.view.ugckit_width, bottomToolbarHeight);
        _photoTransitionToolbar = [[UGCKitPhotoTransitionToolbar alloc] initWithFrame:frame
                                                                          theme:_theme];
        _photoTransitionToolbar.delegate = self;
        [self.view addSubview:_photoTransitionToolbar];
        _photoTransitionToolbar.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
        [_ugcEdit setPictureList:_media.images fps:30];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self onVideoTransitionLefRightSlipping];
        });
        rotateButtonCenter = CGPointMake(_photoTransitionToolbar.ugckit_right - 20 - bottomToolbarHeight, _photoTransitionToolbar.ugckit_top - bottomToolbarHeight - 10 - 20);
    }
    
    if (_media.isVideo) {
        // rotation button
        UIButton *rotateButton = [UGCKitSmallButton buttonWithType:UIButtonTypeCustom];
        [rotateButton setImage:_theme.editRotateIcon forState:UIControlStateNormal];
        [rotateButton sizeToFit];
        rotateButton.center = rotateButtonCenter;
        [rotateButton addTarget:self action:@selector(onRotatePreview:) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:rotateButton];
    }
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
        _generateProgressView.progressTintColor = _theme.progressColor;
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
        [_generateCannelBtn addTarget:self action:@selector(onCancelBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        
        [_generationView addSubview:_generationTitleLabel];
        [_generationView addSubview:_generateProgressView];
        [_generationView addSubview:_generateCannelBtn];
    }
    
    _generateProgressView.progress = 0.f;
    [[[UIApplication sharedApplication] delegate].window addSubview:_generationView];
    return _generationView;
}

- (void)initVideoCutView
{
    if (_media.isVideo) {
        CGRect frame = CGRectMake(0, self.view.ugckit_height - bottomToolbarHeight - bottomInset, self.view.ugckit_width,bottomToolbarHeight);
        if(_videoCutView) [_videoCutView removeFromSuperview];
        _videoCutView = [[UGCKitVideoCutView alloc] initWithFrame:frame videoPath:nil videoAsset:_media.videoAsset config:_config];
        [self.view addSubview:_videoCutView];
    }else{
        CGRect frame = CGRectMake(0, self.view.ugckit_height - bottomToolbarHeight - bottomInset - 10 - bottomToolbarHeight, self.view.ugckit_width,bottomToolbarHeight);
        if (_videoCutView) {
            [_videoCutView updateFrame:_duration];
        }else{
            [_videoCutView removeFromSuperview];
            _videoCutView = [[UGCKitVideoCutView alloc] initWithFrame:frame pictureList:_media.images duration:_duration fps:30 config:_config];
            [self.view addSubview:_videoCutView];
        }
    }
    _videoCutView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    _videoCutView.delegate = self;
    [_videoCutView setCenterPanHidden:YES];
}


- (void)pause
{
    [_ugcEdit pausePlay];
    [_videoPreview setPlayBtn:NO];
}

#pragma makr - Actions
- (void)onRotatePreview:(id)sender
{
    _renderRotation += 90;
    [_ugcEdit setRenderRotation:_renderRotation];    
}

- (void)onBtnPopClicked
{
    [self pause];
    [self _goBack];
}

- (void)_goBack {
    UINavigationController *nav = self.navigationController;
    if (nav.presentedViewController) {
        [nav.presentedViewController dismissViewControllerAnimated:YES completion:nil];
    } else {
        [nav popToRootViewControllerAnimated:YES];
    }
}

-(void)onBtnNextClicked
{
    [self pause];
    [_videoPreview setPlayBtn:NO];
    
    if (_media.isVideo) {
        TXVideoInfo *videoInfo = [TXVideoInfoReader getVideoInfoWithAsset:_media.videoAsset];
        BOOL largerThan1080p = videoInfo.width * videoInfo.height > 1920*1080;
        BOOL hasBeenRotated = _renderRotation % 360 != 0;
        if (_leftTime == 0 && _rightTime == _duration && !largerThan1080p && !hasBeenRotated) {
            //视频如果没发生剪裁，也没有旋转，这里不用走编辑逻辑，减少画面质量损失
            if (self.completion) {
                UGCKitResult *result = [[UGCKitResult alloc] init];
                result.media = _media;
                self.completion(result, _renderRotation);
            }
        }else{
            _generationView = [self generatingView];
            _generationView.hidden = NO;
            [_ugcEdit setCutFromTime:_leftTime toTime:_rightTime];
            if (largerThan1080p || _renderRotation % 360 != 0) {
                [_ugcEdit generateVideo:VIDEO_COMPRESSED_720P videoOutputPath:_videoOutputPath];
            } else {
                //使用快速剪切，速度快
                _hasQuickGenerate = YES;
                _hasNomalGenerate = NO;
                [_ugcEdit quickGenerateVideo:VIDEO_COMPRESSED_720P videoOutputPath:_videoOutputPath];
            }
        }
    }else{
        //图片编辑只能走正常生成逻辑，这里使用高码率，保留更多图片细节
        _generationView = [self generatingView];
        _generationView.hidden = NO;
        _hasNomalGenerate = YES;
        [_ugcEdit setVideoBitrate:10000];
        [_ugcEdit setCutFromTime:_leftTime toTime:_rightTime];
        [_ugcEdit quickGenerateVideo:VIDEO_COMPRESSED_720P videoOutputPath:_videoOutputPath];
    }
}

- (void)onCancelBtnClicked:(UIButton*)sender
{
    _generationView.hidden = YES;
    [_ugcEdit cancelGenerate];
}

#pragma mark TransitionViewDelegate
- (void)_onVideoTransition:(TXTransitionType)type {
    __weak __typeof(self) weakSelf = self;
    WEAKIFY(self);
    [_ugcEdit setPictureTransition:type duration:^(CGFloat duration) {
        STRONGIFY_OR_RETURN(self);
        self->_duration = duration;
        self->_rightTime = duration;
        [self initVideoCutView];
        [self.ugcEdit startPlayFromTime:0 toTime:weakSelf.duration];
        [self.videoPreview setPlayBtn:YES];
    }];
}

- (void)onVideoTransitionLefRightSlipping
{
    [self _onVideoTransition:TXTransitionType_LefRightSlipping];
}

- (void)onVideoTransitionUpDownSlipping
{
    [self _onVideoTransition:TXTransitionType_UpDownSlipping];
}

- (void)onVideoTransitionEnlarge
{
    [self _onVideoTransition:TXTransitionType_Enlarge];
}

- (void)onVideoTransitionNarrow
{
    [self _onVideoTransition:TXTransitionType_Narrow];
}

- (void)onVideoTransitionRotationalScaling
{
    [self _onVideoTransition:TXTransitionType_RotationalScaling];
}

- (void)onVideoTransitionFadeinFadeout
{
    [self _onVideoTransition:TXTransitionType_FadeinFadeout];
}

#pragma mark TXVideoGenerateListener
-(void) onGenerateProgress:(float)progress
{
    _generateProgressView.progress = progress;
}

-(void) onGenerateComplete:(TXGenerateResult *)result
{
    _generationView.hidden = YES;
    if (result.retCode == 0) {
        if (_media.isVideo) {
            NSFileManager *fm = [[NSFileManager alloc] init];
            NSString *rename = [_videoOutputPath stringByAppendingString:@"-tmp.mp4"];
            [fm removeItemAtPath:rename error:nil];
            [fm moveItemAtPath:_videoOutputPath toPath:rename error:nil];
            UGCKitResult *result = [[UGCKitResult alloc]init];
            result.media = [UGCKitMedia mediaWithVideoPath:rename];
            if (self.completion) {
                self.completion(result, 0);
            }
        } else {
            UGCKitResult *result = [[UGCKitResult alloc]init];
            result.media = [UGCKitMedia mediaWithVideoPath:_videoOutputPath];
            if (self.completion) {
                self.completion(result, 0);
            }
        }
    }else{
        //系统剪切如果失败，这里使用SDK正常剪切，设置高码率，保留图像更多的细节
        if (_hasQuickGenerate && !_hasNomalGenerate) {
            _generationView = [self generatingView];
            _generationView.hidden = NO;
            [_ugcEdit cancelGenerate];
            [_ugcEdit setVideoBitrate:10000];
            [_ugcEdit setCutFromTime:_leftTime toTime:_rightTime];
            [_ugcEdit generateVideo:VIDEO_COMPRESSED_720P videoOutputPath:_videoOutputPath];
            _hasNomalGenerate = YES;
        }else{
            UGCKitResult *r = [[UGCKitResult alloc] init];
            r.code = result.retCode;
            NSString *msg = [NSString stringWithFormat:[_theme localizedString:@"UGCKit.Common.HintErrorCodeMessage"],(long)result.retCode,result.descMsg];
            r.info = @{NSLocalizedDescriptionKey: msg};
            if (self.completion) {
                self.completion(r, _renderRotation);
            }
//            UIAlertController *controller = [UIAlertController alertControllerWithTitle:[_theme localizedString:@"UGCKit.Edit.HintVideoGeneratingFailed"]
//                                                                                message:msg
//                                                                         preferredStyle:UIAlertControllerStyleAlert];
//            [controller addAction:[UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.GotIt"] style:UIAlertActionStyleCancel handler:nil]];
//            [self presentViewController:controller animated:YES completion:nil];
        }
    }
    if (_media.isVideo) {
        [UGCKitReporter report:UGCKitReportItem_videoedit userName:nil code:result.retCode msg:result.descMsg];
    }else{
        [UGCKitReporter report:UGCKitReportItem_pictureedit userName:nil code:result.retCode msg:result.descMsg];
    }
}

#pragma mark UGCKitPlayerViewDelegate
- (void)onVideoPlay
{
    CGFloat currentPos = _videoCutView.videoRangeSlider.currentPos;
    if (currentPos < _leftTime || currentPos > _rightTime)
    currentPos = _leftTime;
    
    [_ugcEdit startPlayFromTime:currentPos toTime:_videoCutView.videoRangeSlider.rightPos];
}

- (void)onVideoPause
{
    [_ugcEdit pausePlay];
}

- (void)onVideoResume
{
    [self onVideoPlay];
}

- (void)onVideoPlayProgress:(CGFloat)time
{
    [_videoCutView setPlayTime:time];
}

- (void)onVideoPlayFinished
{
    [_ugcEdit startPlayFromTime:_leftTime toTime:_rightTime];
}

- (void)onVideoEnterBackground
{
    if (_generationView && !_generationView.hidden) {
        [_ugcEdit pauseGenerate];
    }else{
        [UGCKitProgressHUD hideHUDForView:self.view animated:YES];
        [_ugcEdit pausePlay];
        [_videoPreview setPlayBtn:NO];
    }
}

- (void)onVideoWillEnterForeground
{
    if (_generationView && !_generationView.hidden) {
        [_ugcEdit resumeGenerate];
    }
}

#pragma mark - VideoCutViewDelegate
- (void)onVideoRangeLeftChanged:(UGCKitVideoRangeSlider *)sender
{
    //[_ugcEdit pausePlay];
    [_videoPreview setPlayBtn:NO];
    [_ugcEdit previewAtTime:sender.leftPos];
}

- (void)onVideoRangeRightChanged:(UGCKitVideoRangeSlider *)sender
{
    [_videoPreview setPlayBtn:NO];
    [_ugcEdit previewAtTime:sender.rightPos];
}

- (void)onVideoRangeLeftChangeEnded:(UGCKitVideoRangeSlider *)sender
{
    _leftTime = sender.leftPos;
    _rightTime = sender.rightPos;
    [_ugcEdit startPlayFromTime:sender.leftPos toTime:sender.rightPos];
    [_videoPreview setPlayBtn:YES];
}
- (void)onVideoRangeRightChangeEnded:(UGCKitVideoRangeSlider *)sender
{
    _leftTime = sender.leftPos;
    _rightTime = sender.rightPos;
    [_ugcEdit startPlayFromTime:sender.leftPos toTime:sender.rightPos];
    [_videoPreview setPlayBtn:YES];
}

- (void)onVideoSeekChange:(UGCKitVideoRangeSlider *)sender seekToPos:(CGFloat)pos
{
    if (_generationView && _generationView.isHidden == NO) {
        return;
    }
    [_ugcEdit previewAtTime:pos];
    [_videoPreview setPlayBtn:NO];
}


@end

