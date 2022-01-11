// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVideoRecordMusicView.h"
#import "UGCKitColorMacro.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitBGMSliderCutView.h"
#import <UIKit/UIKit.h>

#define L(x) [_theme localizedString:x]

@interface UGCKitVideoRecordMusicView() <BGMCutDelegate>{
    
}
@end

@implementation UGCKitVideoRecordMusicView
{
    UISlider *_sldVolumeForBGM;
    UISlider *_sldVolumeForVoice;
    UGCKitBGMSliderCutView* _musicCutSlider;
    UILabel* _startTimeLabel;
    UGCKitBGMSliderCutViewConfig* sliderConfig;
    
    UIScrollView* _audioScrollView;
    UIScrollView* _audioScrollView2;
    UGCKitTheme *_theme;
}

-(instancetype)initWithFrame:(CGRect)frame needEffect:(BOOL)needEffect theme:(UGCKitTheme *)theme
{
    self = [super initWithFrame:frame];
    if (self) {
        _theme = theme;
        [self initUI:needEffect];
    }
    return self;
}

-(void)initUI:(BOOL)needEffect{
    self.backgroundColor = [UIColor clearColor];

    //BGM
    UIButton *btnSelectBGM = [[UIButton alloc] initWithFrame:CGRectMake(15,  5, 30, 30)];
    [btnSelectBGM setImage:_theme.recordSwitchCameraIcon forState:UIControlStateNormal];
    [btnSelectBGM addTarget:self action:@selector(onBtnMusicSelected) forControlEvents:UIControlEventTouchUpInside];
    btnSelectBGM.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;
    
    UIButton *btnStopBGM = [[UIButton alloc] initWithFrame:CGRectMake(btnSelectBGM.ugckit_width + 30, 5, 30, 30)];
    [btnStopBGM setImage:_theme.recordMusicDeleteIcon forState:UIControlStateNormal];
    [btnStopBGM addTarget:self action:@selector(onBtnMusicStoped) forControlEvents:UIControlEventTouchUpInside];
    btnStopBGM.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin;

    UILabel *labVolumeForVoice = [[UILabel alloc] initWithFrame:CGRectMake(15, btnSelectBGM.ugckit_bottom + 10, 80, 16)];
    [labVolumeForVoice setText:L(@"UGCKit.AudioEffect.VolumeRecord")];
    [labVolumeForVoice setFont:[UIFont systemFontOfSize:14.f]];
    labVolumeForVoice.textColor = UIColorFromRGB(0xFFFFFF);
    [labVolumeForVoice sizeToFit];
    _sldVolumeForVoice = [[UISlider alloc] initWithFrame:CGRectMake(labVolumeForVoice.ugckit_left, labVolumeForVoice.ugckit_bottom + 10,self.ugckit_width - 30, 20)];
    _sldVolumeForVoice.minimumValue = 0;
    _sldVolumeForVoice.maximumValue = 1;
    _sldVolumeForVoice.value = 0.5;
    _sldVolumeForVoice.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    [_sldVolumeForVoice setThumbImage:_theme.sliderThumbImage forState:UIControlStateNormal];
    [_sldVolumeForVoice setMinimumTrackTintColor:_theme.sliderMinColor]; //  RGB(238, 100, 85)];
    [_sldVolumeForVoice addTarget:self action:@selector(onVoiceValueChange:) forControlEvents:UIControlEventValueChanged];
    
    UILabel *labVolumeForBGM = [[UILabel alloc] initWithFrame:CGRectMake(labVolumeForVoice.ugckit_left, _sldVolumeForVoice.ugckit_bottom + 20 , 80 , 16)];
    [labVolumeForBGM setText:L(@"UGCKit.AudioEffect.VolumeBGM")];
    [labVolumeForBGM sizeToFit];
    [labVolumeForBGM setFont:[UIFont systemFontOfSize:14.f]];
    labVolumeForBGM.textColor = UIColorFromRGB(0xFFFFFF);
    _sldVolumeForBGM = [[UISlider alloc] initWithFrame:CGRectMake(labVolumeForVoice.ugckit_left, labVolumeForBGM.ugckit_bottom + 10,self.ugckit_width - 30, 20)];
    _sldVolumeForBGM.minimumValue = 0;
    _sldVolumeForBGM.maximumValue = 1;
    _sldVolumeForBGM.value = 0.5;
    _sldVolumeForBGM.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    [_sldVolumeForBGM setThumbImage:_theme.sliderThumbImage forState:UIControlStateNormal];
    [_sldVolumeForBGM setMinimumTrackTintColor:_theme.sliderMinColor];
    [_sldVolumeForBGM addTarget:self action:@selector(onBGMValueChange:) forControlEvents:UIControlEventValueChanged];
    
    _startTimeLabel = [[UILabel alloc] initWithFrame:CGRectMake(15,_sldVolumeForBGM.ugckit_bottom + 20,200,16)];
    [_startTimeLabel setTextColor:[UIColor whiteColor]];
    [_startTimeLabel setFont:[UIFont systemFontOfSize:14.f]];
    [_startTimeLabel setText:[NSString stringWithFormat:L(@"UGCKit.AudioEffect.StartFrom"),[UGCKitBGMSliderCutView timeString:0]]];
    
    [self addSubview:btnSelectBGM];
    [self addSubview:btnStopBGM];
    [self addSubview:labVolumeForBGM];
    [self addSubview:_sldVolumeForBGM];
    [self addSubview:labVolumeForVoice];
    [self addSubview:_sldVolumeForVoice];
    [self addSubview:_startTimeLabel];
    [self freshCutView:150];
}

- (void)_freshCutView:(CGFloat)duration {
    [_musicCutSlider removeFromSuperview];
    //1.thumbHeight + 2* borderHeight =_musicCutSlider.frame.y;
    //2._musicCutSlider.frame.y目前只支持40
    sliderConfig = [[UGCKitBGMSliderCutViewConfig alloc] initWithTheme:_theme];;
    sliderConfig.duration = duration;
    sliderConfig.frame = CGRectMake(15, _startTimeLabel.ugckit_bottom + 10, self.ugckit_width - 30, 54);
    _musicCutSlider = [[UGCKitBGMSliderCutView alloc] initWithImage:_theme.recordMusicSampleImage config:sliderConfig];
    _musicCutSlider.delegate = self;
    [self addSubview:_musicCutSlider];
}

-(void)freshCutView:(CGFloat)duration{
    dispatch_async(dispatch_get_main_queue(), ^(){
        [self _freshCutView:duration];
    });
}

-(void)onBtnMusicSelected
{
    if (_delegate && [_delegate respondsToSelector:@selector(onBtnMusicSelected)]) {
        [_delegate onBtnMusicSelected];
    }
}

-(void)resetCutView{
    [_musicCutSlider resetCutView];
}

-(void)onBtnMusicStoped
{
    if (_delegate && [_delegate respondsToSelector:@selector(onBtnMusicStoped)]) {
        [_delegate onBtnMusicStoped];
    }
}

-(void)onBGMValueChange:(UISlider*)slider
{
    if (_delegate && [_delegate respondsToSelector:@selector(onBGMValueChange:)]) {
        [_delegate onBGMValueChange:slider.value];
    }
}

-(void)onVoiceValueChange:(UISlider*)slider
{
    if (_delegate && [_delegate respondsToSelector:@selector(onVoiceValueChange:)]) {
        [_delegate onVoiceValueChange:slider.value];
    }
}

#pragma mark - RangeContentDelegate
- (void)onRangeLeftChanged:(UGCKitBGMSliderCutView*)sender percent:(CGFloat)percent{
    if(sliderConfig){
        [_startTimeLabel setText:[NSString stringWithFormat:L(@"UGCKit.AudioEffect.StartFrom"),[UGCKitBGMSliderCutView timeString:percent*sliderConfig.duration]]];
    }
    else{
        [_startTimeLabel setText:[NSString stringWithFormat:L(@"UGCKit.AudioEffect.StartFrom"),[UGCKitBGMSliderCutView timeString:0]]];
    }
}

- (void)onRangeLeftChangeEnded:(UGCKitBGMSliderCutView*)sender percent:(CGFloat)percent
{
//    NSLog(@"end:%f",percent*sliderConfig.duration);
    if (_delegate && [_delegate respondsToSelector:@selector(onBGMRangeChange:endPercent:)]) {
        [_delegate onBGMRangeChange:_musicCutSlider.leftScale endPercent:_musicCutSlider.rightScale];
    }
}

- (void)onRangeRightChangeEnded:(id)sender
{
//    NSLog(@"left:%f right:%f",_musicCutSlider.leftScale, _musicCutSlider.rightScale);
    if (_delegate && [_delegate respondsToSelector:@selector(onBGMRangeChange:endPercent:)]) {
        [_delegate onBGMRangeChange:_musicCutSlider.leftScale endPercent:_musicCutSlider.rightScale];
    }
}

-(void)resetVolume
{
    _sldVolumeForBGM.value = 1;
    _sldVolumeForVoice.value = 1;
}

-(void)resetSiderView
{
    if (_musicCutSlider.bgScrollView != nil) {
        [_musicCutSlider.bgScrollView setContentOffset:CGPointMake(0, 0) animated:YES];
    }
    if (_startTimeLabel != nil) {
        [_startTimeLabel setText:[NSString stringWithFormat:L(@"UGCKit.AudioEffect.StartFrom"),[UGCKitBGMSliderCutView timeString:0]]];
    }
}
@end
