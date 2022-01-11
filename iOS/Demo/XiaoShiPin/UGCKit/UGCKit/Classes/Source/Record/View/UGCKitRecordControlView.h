// Copyright (c) 2019 Tencent. All rights reserved.


#import "UGCKitSlideButton.h"
#import "UGCKitTheme.h"
#import "SDKHeader.h"
#import "UGCKitVideoRecordProcessView.h"
#import "UGCKitSlideOptionControl.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger,SpeedMode)
{
    SpeedMode_VerySlow,
    SpeedMode_Slow,
    SpeedMode_Standard,
    SpeedMode_Quick,
    SpeedMode_VeryQuick,
};

typedef NS_ENUM(NSInteger, UGCKitRecordButtonStyle) {
    UGCKitRecordButtonStylePhoto,   ///< 拍照
    UGCKitRecordButtonStyleRecord,  ///< 录制
    UGCKitRecordButtonStylePause,   ///< 暂停
};

@interface UGCKitRecordControlView : UIView
@property (assign, nonatomic) NSTimeInterval minDuration;
@property (assign, nonatomic) NSTimeInterval maxDuration;
@property (assign, nonatomic) TXVideoAspectRatio videoRatio;
@property (assign, nonatomic) BOOL isFrontCamera;
@property (assign, nonatomic) BOOL musicButtonEnabled;
@property (assign, nonatomic) BOOL speedControlEnabled;
@property (assign, nonatomic) BOOL photoModeEnabled;
@property (assign, nonatomic) BOOL countDownModeEnabled;

@property (assign, nonatomic) BOOL torchOn;
@property (assign, nonatomic) UGCKitRecordButtonStyle recordButtonStyle;

@property (strong, nonatomic) UGCKitSlideButton *btnRatioGroup;
@property (strong, nonatomic) UGCKitTheme *theme;
@property (strong, nonatomic) UIButton *btnBeauty;
@property (strong, nonatomic) UIButton *btnMusic;
@property (strong, nonatomic) UIButton *btnAudioEffect;
@property (strong, nonatomic) UIButton *btnCountDown;

@property (strong, nonatomic) UIView *bottomMask;
@property (strong, nonatomic) UIButton *btnStartRecord;
@property (strong, nonatomic) UIButton *btnFlash;
@property (strong, nonatomic) UIButton *btnCamera;
@property (strong, nonatomic) UIButton *btnDelete;
@property (strong, nonatomic) UGCKitVideoRecordProcessView *progressView;
@property (strong, nonatomic) UILabel *recordTimeLabel;

@property (assign, nonatomic) SpeedMode speedMode;
@property (assign, nonatomic) BOOL controlButtonsHidden;
@property (strong, nonatomic) UIView *speedView;
@property (strong, nonatomic) NSArray<UIButton*> *speedBtnList;
@property (strong, nonatomic) UIButton *speedChangeBtn;
@property (strong, nonatomic) UGCKitSlideOptionControl *recordButtonSwitchControl;

- (instancetype)initWithFrame:(CGRect)frame minDuration:(NSTimeInterval)minDuration maxDuration:(NSTimeInterval)maxDuration;

- (void)setMinDuration:(NSTimeInterval)minDuration maxDuration:(NSTimeInterval)maxDuration;

/// 设置各组件开关后必须调用此方法生成View
- (void)setupViews;
- (void)setSelectedSpeed:(SpeedMode)tag;
@end

NS_ASSUME_NONNULL_END
