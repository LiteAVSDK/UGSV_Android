// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitRecordControlView.h"
#import "UGCKitSmallButton.h"
#import "SDKHeader.h"
#import "UGCKitVerticalButton.h"
#import "UGCKitLabel.h"
#import "UGCKitLocalization.h"

#define BUTTON_CONTROL_SIZE         32
#define BUTTON_MASK_HEIGHT          200
#define BUTTON_RECORD_SIZE          75
#define BUTTON_PROGRESS_HEIGHT      3
#define BUTTON_SPEED_COUNT          5
#define BUTTON_SPEED_HEIGHT         30
#define BUTTON_SPEED_CHANGE_HEIGHT  34

#define kScaleX [UIScreen mainScreen].bounds.size.width / 375

@implementation UGCKitRecordControlView
{
    NSArray <UIView *> * _activeRightSideControlsBeforeHidden;
}
- (instancetype)initWithCoder:(NSCoder *)coder
{
    self = [super initWithCoder:coder];
    if (self) {
        _minDuration = 0;
        _maxDuration = 30;
        [self commonInit];
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame minDuration:(NSTimeInterval)minDuration maxDuration:(NSTimeInterval)maxDuration
{
    self = [super initWithFrame:frame];
    if (self) {
        _minDuration = minDuration;
        _maxDuration = maxDuration;
        [self commonInit];
    }
    return self;
}

- (void)setControlButtonsHidden:(BOOL)controlButtonsHidden {
    _controlButtonsHidden = controlButtonsHidden;
    [_activeRightSideControlsBeforeHidden enumerateObjectsUsingBlock:
     ^(UIView * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        obj.hidden = controlButtonsHidden;
    }];
    if (self.countDownModeEnabled) {
        _btnCountDown.hidden = controlButtonsHidden;
    }
    self.btnFlash.hidden = controlButtonsHidden;
    self.btnCamera.hidden = controlButtonsHidden;
    self.btnDelete.hidden = controlButtonsHidden;
    self.recordButtonSwitchControl.hidden = controlButtonsHidden;
}

- (void)commonInit {
    _videoRatio = VIDEO_ASPECT_RATIO_9_16;
    _recordButtonStyle = UGCKitRecordButtonStyleRecord;
    _speedControlEnabled = YES;
    _musicButtonEnabled = YES;
}

- (void)setMinDuration:(NSTimeInterval)minDuration
           maxDuration:(NSTimeInterval)maxDuration
{
    _minDuration = minDuration;
    _maxDuration = maxDuration;
    [_progressView setMinDuration:_minDuration maxDuration:_maxDuration];
}

- (void)setVideoRatio:(TXVideoAspectRatio)videoRatio
{
    if (_videoRatio != videoRatio) {
        _videoRatio = videoRatio;
        for (NSInteger idx = 0; idx < _btnRatioGroup.buttons.count; ++idx) {
            UIButton *btn = _btnRatioGroup.buttons[idx];
            if (btn.tag == videoRatio) {
                _btnRatioGroup.selectedIndex = idx;
                break;
            }
        }
    }
}

- (void)setIsFrontCamera:(BOOL)isFrontCamera
{
    if (_isFrontCamera != isFrontCamera) {
        _isFrontCamera = isFrontCamera;
        [self _configCameraSwitchButton];
    }
}

- (void)setTorchOn:(BOOL)torchOn
{
    if (_torchOn != torchOn) {
        _torchOn = torchOn;
        [self _configTorchButton];
    }
}

- (void)_configRecordButton {
    CGPoint center = _btnStartRecord.center;
    if (_recordButtonStyle == UGCKitRecordButtonStylePause) {
        [_btnStartRecord setImage:_theme.recordButtonPauseInnerIcon forState:UIControlStateNormal];
        [_btnStartRecord setBackgroundImage:_theme.recordButtonPauseBackgroundImage forState:UIControlStateNormal];
    } else if (_recordButtonStyle == UGCKitRecordButtonStyleRecord) {
        [_btnStartRecord setImage:_theme.recordButtonTapModeIcon forState:UIControlStateNormal];
        [_btnStartRecord setBackgroundImage:_theme.recordButtonPhotoModeBackgroundImage forState:UIControlStateNormal];
    } else if (_recordButtonStyle == UGCKitRecordButtonStylePhoto) {
        [_btnStartRecord setImage:_theme.recordButtonPhotoModeIcon forState:UIControlStateNormal];
        [_btnStartRecord setBackgroundImage:_theme.recordButtonPhotoModeBackgroundImage forState:UIControlStateNormal];
    }
    [_btnStartRecord sizeToFit];
    _btnStartRecord.center = center;
}

- (void)setRecordButtonStyle:(UGCKitRecordButtonStyle)recordButtonStyle
{
    if (_recordButtonStyle == recordButtonStyle) {
        return;
    }

    _recordButtonStyle = recordButtonStyle;
    [self _configRecordButton];
}

- (void)setPhotoModeEnabled:(BOOL)photoModeEnabled {
    if (photoModeEnabled) {
        _recordButtonSwitchControl.disabledIndexes = [NSIndexSet indexSet];
    } else {
        _recordButtonSwitchControl.disabledIndexes = [NSIndexSet indexSetWithIndex:UGCKitRecordButtonStylePhoto];
    }
}

- (void)setSpeedControlEnabled:(BOOL)enabled {
    _speedControlEnabled = enabled;
    _speedView.hidden = !enabled;
    _speedChangeBtn.hidden = !enabled;
}

- (void)setCountDownModeEnabled:(BOOL)countDownModeEnabled {
    _countDownModeEnabled = countDownModeEnabled;
    if (!_controlButtonsHidden) {
        _btnCountDown.hidden = !countDownModeEnabled;
    }
}

// 添加子View
-(void)setupViews
{
    CGFloat top = 0;

    CGFloat y = top;
    const CGFloat centerX = CGRectGetWidth(self.bounds) - 20 - BUTTON_CONTROL_SIZE/2;
    const CGFloat space = 20;

    // 音乐按钮
    if (self.musicButtonEnabled) {
        _btnMusic = [self _createButton:[_theme localizedString:@"UGCKit.Record.BeautyLabelMusic"]
                                  image:_theme.recordMusicIcon
                               position:CGPointMake(centerX, y)];
        [self addSubview:_btnMusic];
        y = CGRectGetMaxY(_btnMusic.frame) + space;
    }

    // 比例切换按钮
    CGPoint p = CGPointMake(centerX, y);
    UIButton *btnRatio169 = [self _createButton:@"16:9"
                                          image:_theme.recordAspect169Icon
                                       position:p];
    btnRatio169.tag = VIDEO_ASPECT_RATIO_16_9;

    UIButton *btnRatio916 = [self _createButton:@"9:16"
                                             image:_theme.recordAspect916Icon
                                          position:p];
    btnRatio916.tag = VIDEO_ASPECT_RATIO_9_16;

    UIButton *btnRatio11 = [self _createButton:@"1:1"
                                         image:_theme.recordAspect11Icon
                                      position:p];
    btnRatio11.tag = VIDEO_ASPECT_RATIO_1_1;
    
    UIButton *btnRatio43 = [self _createButton:@"4:3"
                                         image:_theme.recordAspect43Icon
                                      position:p];
    btnRatio43.tag = VIDEO_ASPECT_RATIO_4_3;
    
    UIButton *btnRatio34 = [self _createButton:@"3:4"
                                         image:_theme.recordAspect34Icon
                                      position:p];
    btnRatio34.tag = VIDEO_ASPECT_RATIO_3_4;

    _btnRatioGroup = [[UGCKitSlideButton alloc] initWithButtons:@[btnRatio43, btnRatio34, btnRatio11, btnRatio916, btnRatio169]
                                                         buttonSize:btnRatio43.frame.size
                                                            spacing:30];
    _btnRatioGroup.selectedIndex = 3;
    CGRect frame = _btnRatioGroup.frame;
    frame.size = _btnRatioGroup.intrinsicContentSize;
    frame.origin = CGPointMake(centerX + CGRectGetWidth(btnRatio169.frame) / 2 - CGRectGetWidth(frame), y);
    _btnRatioGroup.frame = frame;
    [self addSubview:_btnRatioGroup];
    [_btnRatioGroup.buttons enumerateObjectsUsingBlock:^(UIButton * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if (obj.tag == _videoRatio) {
            _btnRatioGroup.selectedIndex = idx;
            *stop = YES;
        }
    }];
    y = CGRectGetMaxY(_btnRatioGroup.frame)+space;

    // 美颜按钮
    _btnBeauty = [self _createButton:[_theme localizedString:@"UGCKit.Record.BeautyLabelBeauty"]
                              image:_theme.recordBeautyIcon
                           position:CGPointMake(centerX, y)];
    [self addSubview:_btnBeauty];

    y = CGRectGetMaxY(_btnBeauty.frame) + space;

    // 混音近钮
    _btnAudioEffect = [self _createButton:[_theme localizedString:@"UGCKit.Record.AudioMix"]
                                 image:_theme.recordAudioEffectIcon
                              position:CGPointMake(centerX, y)];
    [self addSubview:_btnAudioEffect];
    y = CGRectGetMaxY(_btnAudioEffect.frame) + space;

    _btnCountDown = [self _createButton:[_theme localizedString:@"UGCKit.Record.CountDown"]
                                  image:_theme.recordCountDownIcon
                               position:CGPointMake(centerX, y)];
    [self addSubview:_btnCountDown];
    _btnCountDown.hidden = YES;
    // y = CGRectGetMaxY(_btnCountDown.frame) + space;

    NSArray<UIView *> *rightSideControls = @[_btnMusic, _btnRatioGroup, _btnBeauty,
                                            _btnCountDown, _btnAudioEffect];

    // 底部按钮
    CGRect bottomFrame = CGRectMake(0, self.frame.size.height - BUTTON_MASK_HEIGHT, self.frame.size.width, BUTTON_MASK_HEIGHT);
    UIView *bottomMask = [[UIView alloc] initWithFrame:bottomFrame];
    bottomMask.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3];
    _bottomMask = bottomMask;
    [self addSubview:bottomMask];

    UIView *bottomContentView = bottomMask;//.contentView;
    _btnStartRecord = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, BUTTON_RECORD_SIZE, BUTTON_RECORD_SIZE)];
    _btnStartRecord.center = CGPointMake(bottomContentView.frame.size.width / 2, bottomContentView.frame.size.height - BUTTON_RECORD_SIZE/2 - 50);
    [bottomContentView addSubview:_btnStartRecord];

    _btnFlash = [UIButton buttonWithType:UIButtonTypeCustom];
    _btnFlash.bounds = CGRectMake(0, 0, BUTTON_CONTROL_SIZE, BUTTON_CONTROL_SIZE);
    _btnFlash.center = CGPointMake(25 + BUTTON_CONTROL_SIZE / 2, _btnStartRecord.center.y);
    [bottomContentView addSubview:_btnFlash];

    _btnCamera = [UIButton buttonWithType:UIButtonTypeCustom];
    _btnCamera.bounds = CGRectMake(0, 0, BUTTON_CONTROL_SIZE, BUTTON_CONTROL_SIZE);
    _btnCamera.center = CGPointMake(CGRectGetMaxX(_btnFlash.frame) + 25 + BUTTON_CONTROL_SIZE / 2, _btnStartRecord.center.y);
    [_btnCamera setImage:_theme.recordSwitchCameraIcon forState:UIControlStateNormal];
    [bottomContentView addSubview:_btnCamera];


    _btnDelete = [UIButton buttonWithType:UIButtonTypeCustom];
    _btnDelete.bounds = CGRectMake(0, 0, BUTTON_CONTROL_SIZE, BUTTON_CONTROL_SIZE);
    _btnDelete.center = CGPointMake((CGRectGetMaxX(_btnStartRecord.frame) + CGRectGetMaxX(self.bounds)) / 2, _btnStartRecord.center.y);
    [_btnDelete setImage:_theme.recordDeleteIcon forState:UIControlStateNormal];
    [_btnDelete setImage:_theme.recordDeleteHighlightedIcon forState:UIControlStateHighlighted];
    [bottomContentView addSubview:_btnDelete];

    _progressView = [[UGCKitVideoRecordProcessView alloc] initWithTheme:_theme
                                                                  frame:CGRectMake(0, 0, bottomContentView.bounds.size.width, BUTTON_PROGRESS_HEIGHT)
                                                            minDuration:_minDuration
                                                            maxDuration:_maxDuration];
    _progressView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    _progressView.backgroundColor = [_theme.backgroundColor colorWithAlphaComponent:0.8];
    [bottomContentView addSubview:_progressView];

    UGCKitLabel *recordTimeLabel = [[UGCKitLabel alloc] init];
    recordTimeLabel.edgeInsets = UIEdgeInsetsMake(2, 8, 2, 8);
    [recordTimeLabel setText:LocalizationNotNeeded(@"00:00")];
    recordTimeLabel.font = [UIFont systemFontOfSize:12];
    recordTimeLabel.textColor = [UIColor whiteColor];
    recordTimeLabel.textAlignment = NSTextAlignmentCenter;
    [recordTimeLabel sizeToFit];
    recordTimeLabel.layer.cornerRadius  = recordTimeLabel.bounds.size.height / 2;
    recordTimeLabel.layer.masksToBounds = YES;
    recordTimeLabel.center = CGPointMake(CGRectGetMidX(_bottomMask.frame),
                                          CGRectGetMinY(_bottomMask.frame) - 5 - CGRectGetMidY(recordTimeLabel.bounds));

    recordTimeLabel.backgroundColor = [_theme.backgroundColor colorWithAlphaComponent:0.5];
    [self addSubview:recordTimeLabel];
    _recordTimeLabel = recordTimeLabel;

    if (_isFrontCamera) {
        _torchOn = NO;
    }
    [self _configRecordButton];
    [self _configCameraSwitchButton];
    [self _configTorchButton];
    if (self.speedControlEnabled) {
        [self _createSpeedControl];
    }

    CGRect actionModeFrame = CGRectMake(0, CGRectGetHeight(bottomContentView.bounds) - 40, CGRectGetWidth(bottomContentView.bounds), 40);
    _recordButtonSwitchControl = [[UGCKitSlideOptionControl alloc] initWithFrame:actionModeFrame
                                                                           theme:_theme
                                                                         options:@[[_theme localizedString:@"UGCKit.Record.StillPhoto"],
                                                                                   [_theme localizedString:@"UGCKit.Record.TapCapture"],
                                                                                   [_theme localizedString:@"UGCKit.Record.PressCapture"]]];
    _recordButtonSwitchControl.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    _recordButtonSwitchControl.selectedIndex = 1;
    [bottomContentView addSubview:_recordButtonSwitchControl];

    _activeRightSideControlsBeforeHidden = [rightSideControls filteredArrayUsingPredicate:
                                            [NSPredicate predicateWithFormat:@"hidden=NO"]];
}

#pragma mark - setup buttons by state
- (UIButton *)_createButton:(NSString *)title image:(UIImage *)image position:(CGPoint)location
{
    UGCKitVerticalButton *button = [[UGCKitVerticalButton alloc] initWithTitle:title];
    [button setImage:image forState:UIControlStateNormal];
    [button setTitleColor:_theme.titleColor forState:UIControlStateNormal];
    button.titleLabel.font = [UIFont systemFontOfSize:12];
    button.frame = CGRectMake(location.x-BUTTON_CONTROL_SIZE/2, location.y, BUTTON_CONTROL_SIZE, BUTTON_CONTROL_SIZE);
    [button sizeToFit];
    button.center = CGPointMake(location.x, button.center.y);
    return button;
}

- (void)_configTorchButton {
    if (_torchOn) {
        [_btnFlash setImage:_theme.recordTorchOnIcon forState:UIControlStateNormal];
        [_btnFlash setImage:_theme.recordTorchOnHighlightedIcon forState:UIControlStateHighlighted];
    }else{
        [_btnFlash setImage:_theme.recordTorchOffIcon forState:UIControlStateNormal];
        [_btnFlash setImage:_theme.recordTorchOffHighlightedIcon forState:UIControlStateHighlighted];
    }
}

- (void)_configCameraSwitchButton {
    if (_isFrontCamera) {
        [_btnFlash setImage:_theme.recordTorchDisabledIcon forState:UIControlStateNormal];
        _btnFlash.enabled = NO;
    }else{
        [self _configTorchButton];
        _btnFlash.enabled = YES;
    }
}

-(void)_createSpeedControl
{
    _speedBtnList = [NSMutableArray array];
    _speedView = [[UIView alloc] initWithFrame:CGRectMake(30, 13, CGRectGetWidth(_bottomMask.frame)-60, BUTTON_SPEED_HEIGHT)];
    _speedView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleBottomMargin;
    [_bottomMask addSubview:_speedView];

    _speedView.layer.cornerRadius = _speedView.bounds.size.height / 2;
    _speedView.layer.masksToBounds = YES;
    _speedView.backgroundColor = [UIColor blackColor];
    _speedView.alpha = 0.5;

    //合唱暂不支持变速录制

    _speedChangeBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    _speedChangeBtn.titleLabel.adjustsFontSizeToFitWidth = YES;
    _speedChangeBtn.titleLabel.minimumScaleFactor = 0.5;
    [_speedChangeBtn setTitle:[self getSpeedText:2] forState:UIControlStateNormal];
    [_speedChangeBtn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_speedChangeBtn setBackgroundImage:_theme.recordSpeedCenterIcon forState:UIControlStateNormal];

    CGFloat btnSpace = 0;
    CGFloat padding = 16 * kScaleX;
    CGFloat btnWidth = (CGRectGetWidth(_speedView.bounds) -  2 * padding - btnSpace * 4 ) / 5;
    NSMutableArray *speedButtonArray = [[NSMutableArray alloc] initWithCapacity:BUTTON_SPEED_COUNT];
    for(int i = 0 ; i < BUTTON_SPEED_COUNT ; i ++)
    {
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        btn.titleLabel.minimumScaleFactor = 0.5;
        btn.frame = CGRectMake(padding + (btnSpace + btnWidth) * i, 0, btnWidth, CGRectGetHeight(_speedView.bounds));
        [btn setTitle:[self getSpeedText:(SpeedMode)i] forState:UIControlStateNormal];
        [btn setTitleColor:_theme.speedControlSelectedTitleColor forState:UIControlStateNormal];
        [btn.titleLabel setFont:[UIFont systemFontOfSize:15]];
        btn.titleLabel.minimumScaleFactor = 0.5;
        btn.titleLabel.adjustsFontSizeToFitWidth = YES;
        btn.tag = i;
        [_speedView addSubview:btn];
        [speedButtonArray addObject:btn];
    }
    _speedBtnList = speedButtonArray;
    [self setSelectedSpeed:SpeedMode_Standard];
    [self.bottomMask addSubview:_speedChangeBtn];
}


-(NSString *)getSpeedText:(SpeedMode)speedMode
{
    NSString *text = nil;
    switch (speedMode) {
        case SpeedMode_VerySlow:
            text = [_theme localizedString:@"UGCKit.Record.SpeedSlow0"];
            break;
        case SpeedMode_Slow:
            text = [_theme localizedString:@"UGCKit.Record.SpeedSlow"];
            break;
        case SpeedMode_Standard:
            text = [_theme localizedString:@"UGCKit.Record.SpeedStandard"];
            break;
        case SpeedMode_Quick:
            text = [_theme localizedString:@"UGCKit.Record.SpeedFast"];
            break;
        case SpeedMode_VeryQuick:
            text = [_theme localizedString:@"UGCKit.Record.SpeedFast0"];
            break;
        default:
            break;
    }
    return text;
}

- (void)setSelectedSpeed:(SpeedMode)tag
{
    if (tag >= _speedBtnList.count) {
        return;
    }
    const float padding = 16 * kScaleX;
    UIButton *btn = _speedBtnList[(NSInteger)tag];
    //    UIButton *btn = [self.speedView viewWithTag:(NSInteger)tag];
    CGRect rect = CGRectIntegral([_speedView convertRect:btn.frame toView:self.bottomMask]);
    CGRect frame = rect;
    frame.origin.y -= (BUTTON_SPEED_CHANGE_HEIGHT - rect.size.height) * 0.5;
    frame.size.height = BUTTON_SPEED_CHANGE_HEIGHT;

    UIImage *bgImage = _theme.recordSpeedCenterIcon;// @"speedChange_center";
    if (tag == 0) {
        frame.origin.x -= padding;
        frame.size.width += padding;
        bgImage = _theme.recordSpeedLeftIcon;
    } else if (tag == 4) {
        frame.size.width += padding;
        bgImage = _theme.recordSpeedRightIcon;
    }
    [_speedChangeBtn setBackgroundImage:bgImage forState:UIControlStateNormal];
    [CATransaction begin];
    [CATransaction setDisableActions:YES];
    _speedChangeBtn.frame = frame;
    [CATransaction commit];

    [_speedChangeBtn setTitle:[self getSpeedText:(SpeedMode)tag] forState:UIControlStateNormal];

    _speedMode = tag;
}

// Ignore touch events hit in the blank area
- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];
    if (hitView == self) {
        return nil;
    }
    return hitView;
}
@end
