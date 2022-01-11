// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <BeautySettingKit/TCBeautyPanel.h>

NS_ASSUME_NONNULL_BEGIN

@interface UGCKitTheme : NSObject <TCBeautyPanelThemeProtocol>
+ (instancetype)sharedTheme;
#pragma mark - Common
/// 标题颜色
@property (nonatomic, strong) UIColor *titleColor;
/// 后退按钮
@property (strong, nonatomic) UIImage *backIcon;
/// 圆角按钮背景图
@property (strong, nonatomic) UIImage *nextIcon;
/// 背景色
@property (strong, nonatomic) UIColor *backgroundColor;
/// 进度条tintColor
@property (strong, nonatomic) UIColor *progressColor;

@property (strong, nonatomic) UIImage *closeIcon;
@property (strong, nonatomic) UIImage *progressTrackImage;
/// 滑杆配置
@property (strong, nonatomic) UIColor *sliderMinColor;
@property (strong, nonatomic) UIColor *sliderMaxColor;
@property (strong, nonatomic) UIColor *sliderValueColor;
@property (strong, nonatomic) UIImage *sliderThumbImage;
@property (strong, nonatomic) UIImage *rightArrowIcon;
#pragma mark - Media Picker
@property (strong, nonatomic) UIColor *pickerSelectionBorderColor;

#pragma mark - Edit

@property (strong, nonatomic) UIImage *editRotateIcon; // 旋转

/// 播放图标
@property (strong, nonatomic) UIImage *editPlayIcon;
/// 播放图标（高亮）
@property (strong, nonatomic) UIImage *editPlayHighlightedIcon;
/// 暂停图标
@property (strong, nonatomic) UIImage *editPauseIcon;
/// 暂停图标（高亮）
@property (strong, nonatomic) UIImage *editPauseHighlightedIcon;
/// 更换视频按钮背景图
@property (strong, nonatomic) UIImage *editChooseVideoIcon;

/// 按钮背景图
@property (strong, nonatomic) UIImage *confirmIcon;

/// 贴纸、字幕的增加按钮
@property (strong, nonatomic) UIImage *editPanelAddPasterIcon;
@property (strong, nonatomic) UIColor *editPanelBackgroundColor;
@property (strong, nonatomic) UIColor *editPanelTextColor;
@property (strong, nonatomic) UIImage *editPanelCloseIcon;
@property (strong, nonatomic) UIImage *editPanelConfirmIcon;
@property (strong, nonatomic) UIColor *editPasterBorderColor;

/// 编辑菜单音乐按钮
@property (strong, nonatomic) UIImage *editPanelMusicIcon;
/// 编辑菜单特效按钮
@property (strong, nonatomic) UIImage *editPanelEffectIcon;
/// 编辑菜单速度按钮
@property (strong, nonatomic) UIImage *editPanelSpeedIcon;
/// 编辑菜单滤镜按钮
@property (strong, nonatomic) UIImage *editPanelFilterIcon;
/// 编辑菜单贴纸按钮
@property (strong, nonatomic) UIImage *editPanelPasterIcon;
/// 编辑菜单字幕按钮
@property (strong, nonatomic) UIImage *editPanelSubtitleIcon;
/// 编辑菜单音乐按钮（高亮）
@property (strong, nonatomic) UIImage *editPanelMusicHighlightedIcon;
/// 编辑菜单特效按钮（高亮）
@property (strong, nonatomic) UIImage *editPanelEffectHighlightedIcon;
/// 编辑菜单速度按钮（高亮）
@property (strong, nonatomic) UIImage *editPanelSpeedHighlightedIcon;
/// 编辑菜单滤镜按钮（高亮）
@property (strong, nonatomic) UIImage *editPanelFilterHighlightedIcon;
/// 编辑菜单贴纸按钮（高亮）
@property (strong, nonatomic) UIImage *editPanelPasterHighlightedIcon;
/// 编辑菜单字幕按钮（高亮）
@property (strong, nonatomic) UIImage *editPanelSubtitleHighlightedIcon;

/// 删除按钮图标
@property (strong, nonatomic) UIImage *editPanelDeleteIcon;
/// 删除按钮图标（高亮）
@property (strong, nonatomic) UIImage *editPanelDeleteHighlightedIcon;
/// 按钮背景图（高亮）
@property (strong, nonatomic) UIImage *confirmHighlightedIcon;
/// 时间轴位置指示器图标
@property (strong, nonatomic) UIImage *editTimelineIndicatorIcon;

/// 编辑菜单-时间效果-无效果图标
@property (strong, nonatomic) UIImage *editTimeEffectNormalIcon;
/// 编辑菜单-时间效果-倒放图标
@property (strong, nonatomic) UIImage *editTimeEffectReveseIcon;
/// 编辑菜单-时间效果-重复图标
@property (strong, nonatomic) UIImage *editTimeEffectRepeatIcon;
/// 编辑菜单-时间效果-慢动作图标
@property (strong, nonatomic) UIImage *editTimeEffectSlowMotionIcon;
/// 视频裁剪条当前位置图标
@property (strong, nonatomic) UIImage *editTimeEffectIndicatorIcon;

#pragma mark Video Cut
/// 视频裁剪条左边图标
@property (strong, nonatomic) UIImage *editCutSliderLeftIcon;
/// 视频裁剪条右边图标
@property (strong, nonatomic) UIImage *editCutSliderRightIcon;
/// 视频裁剪条当前位置图标
@property (strong, nonatomic) UIImage *editCutSliderCenterIcon;
/// 视频裁剪条边界颜色
@property (strong, nonatomic) UIColor *editCutSliderBorderColor;
/// 音乐选取条左边图标
@property (strong, nonatomic) UIImage *editMusicSliderRightIcon;
/// 音乐选取条右边图标
@property (strong, nonatomic) UIImage *editMusicSliderLeftIcon;
/// 视频裁剪条边界颜色
@property (strong, nonatomic) UIColor *editMusicSliderBorderColor;

#pragma mark Paster
/// 贴纸删除图标
@property (strong, nonatomic) UIImage *editPasterDeleteIcon;
/// 贴纸旋转图标
@property (strong, nonatomic) UIImage *editTextPasterRotateIcon;
/// 贴纸文字编辑图标
@property (strong, nonatomic) UIImage *editTextPasterEditIcon;
/// 贴纸文字编辑确认图标
@property (strong, nonatomic) UIImage *editTextPasterConfirmIcon;

#pragma mark Filter
/// 滤镜选中图标
@property (strong, nonatomic) UIImage *editFilterSelectionIcon;

#pragma mark Photo Transition
/// 图片转场-左右转场图标
@property (strong, nonatomic) UIImage *transitionLeftRightIcon;
/// 图片转场-上下转场图标
@property (strong, nonatomic) UIImage *transitionUpDownIcon;
/// 图片转场-放大转场图标
@property (strong, nonatomic) UIImage *transitionZoomInIcon;
/// 图片转场-缩小转场图标
@property (strong, nonatomic) UIImage *transitionZoomOutIcon;
/// 图片转场-旋转转场图标
@property (strong, nonatomic) UIImage *transitionRotateIcon;
/// 图片转场-淡入淡出转场图标
@property (strong, nonatomic) UIImage *transitionFadeInOutIcon;


#pragma mark - Video Cut
/// 录制-音乐图标
@property (strong, nonatomic) UIImage *recordMusicIcon;
/// 录制-4:3屏比图标
@property (strong, nonatomic) UIImage *recordAspect43Icon;
/// 录制-3:4屏比图标
@property (strong, nonatomic) UIImage *recordAspect34Icon;
/// 录制-1：1屏比图标
@property (strong, nonatomic) UIImage *recordAspect11Icon;
/// 录制-16:9屏比图标
@property (strong, nonatomic) UIImage *recordAspect169Icon;
/// 录制-9:16屏比图标
@property (strong, nonatomic) UIImage *recordAspect916Icon;
/// 录制-美颜图标
@property (strong, nonatomic) UIImage *recordBeautyIcon;
/// 录制-音效图标
@property (strong, nonatomic) UIImage *recordAudioEffectIcon;
/// 录制-倒计时图标
@property (strong, nonatomic) UIImage *recordCountDownIcon;
/// 录制-闪光灯打开图标
@property (strong, nonatomic) UIImage *recordTorchOnIcon;
/// 录制-闪光灯打开高亮图标
@property (strong, nonatomic) UIImage *recordTorchOnHighlightedIcon;
/// 录制-闪光灯关闭图标
@property (strong, nonatomic) UIImage *recordTorchOffIcon;
/// 录制-闪光灯关闭高亮图标
@property (strong, nonatomic) UIImage *recordTorchOffHighlightedIcon;
/// 录制-闪光灯禁用图标
@property (strong, nonatomic) UIImage *recordTorchDisabledIcon;
/// 录制-摄像头切换图标
@property (strong, nonatomic) UIImage *recordSwitchCameraIcon;
/// 录制-速度切换选中文字颜色
@property (strong, nonatomic) UIColor *recordSpeedSelectedTitleColor;
/// 录制-速度切换选中图标
@property (strong, nonatomic) UIImage *recordSpeedCenterIcon;
/// 录制-速度切换左侧圆角图标
@property (strong, nonatomic) UIImage *recordSpeedLeftIcon;
/// 录制-速度切换右侧圆角图标
@property (strong, nonatomic) UIImage *recordSpeedRightIcon;
/// 录制-音乐片段图标
@property (strong, nonatomic) UIImage *recordMusicSampleImage;
/// 录制-音乐切换图标
@property (strong, nonatomic) UIImage *recordMusicSwitchIcon;
/// 录制-音乐删除图标
@property (strong, nonatomic) UIImage *recordMusicDeleteIcon;
/// 录制-音乐下载图标
@property (strong, nonatomic) UIImage *recordMusicDownloadIcon;
/// 录制-点击录制按钮图标
@property (strong, nonatomic) UIImage *recordButtonTapModeIcon;
/// 录制-拍照按钮图标
@property (strong, nonatomic) UIImage *recordButtonPhotoModeIcon;
/// 录制-录制按钮背景图片
@property (strong, nonatomic) UIImage *recordButtonPhotoModeBackgroundImage;
/// 录制-录制按钮暂停状态图标
@property (strong, nonatomic) UIImage *recordButtonPauseInnerIcon;
/// 录制-录制按钮暂停状态背景
@property (strong, nonatomic) UIImage *recordButtonPauseBackgroundImage;
/// 录制进度条颜色
@property (strong, nonatomic) UIColor *recordTimelineColor;
/// 录制进度条高亮颜色
@property (strong, nonatomic) UIColor *recordTimelineSelectionColor;
/// 录制进度条分段块颜色
@property (strong, nonatomic) UIColor *recordTimelineSeperatorColor;
/// 录制片段删除按钮图标
@property (strong, nonatomic) UIImage *recordDeleteIcon;
/// 录制片段删除按钮高亮图标
@property (strong, nonatomic) UIImage *recordDeleteHighlightedIcon;
/// 录制方式切换指示图标
@property (strong, nonatomic) UIImage *recordButtonModeSwitchIndicatorIcon;

#pragma mark - Beauty Panel
/// 美颜面板 - 美颜图标
@property (strong, nonatomic) UIImage *beautyPanelSmoothBeautyStyleIcon;
/// 美颜面板 - 大眼图标
@property (strong, nonatomic) UIImage *beautyPanelEyeScaleIcon;
/// 美颜面板 - P图风格美颜图标
@property (strong, nonatomic) UIImage *beautyPanelPTuBeautyStyleIcon;
/// 美颜面板 - 自然风格图标
@property (strong, nonatomic) UIImage *beautyPanelNatureBeautyStyleIcon;
/// 美颜面板 - 红润图标
@property (strong, nonatomic) UIImage *beautyPanelRuddyIcon;
/// 美颜面板 - 绿幕图标
@property (strong, nonatomic) UIImage *beautyPanelBgRemovalIcon;
/// 美颜面板 - 美白图标
@property (strong, nonatomic) UIImage *beautyPanelWhitnessIcon;
/// 美颜面板 - 瘦脸图标
@property (strong, nonatomic) UIImage *beautyPanelFaceSlimIcon;
/// 美颜面板 - AI抠图图标
@property (strong, nonatomic) UIImage *beautyPanelGoodLuckIcon;
/// 美颜面板 - 下巴调整图标
@property (strong, nonatomic) UIImage *beautyPanelChinIcon;
/// 美颜面板 - V脸图标
@property (strong, nonatomic) UIImage *beautyPanelFaceVIcon;
/// 美颜面板 - 瘦脸图标
@property (strong, nonatomic) UIImage *beautyPanelFaceScaleIcon;
/// 美颜面板 - 瘦鼻图标
@property (strong, nonatomic) UIImage *beautyPanelNoseSlimIcon;
/// 美颜面板 - 白牙图标
@property (strong, nonatomic) UIImage *beautyPanelToothWhitenIcon;
/// 美颜面板 - 眼距图标
@property (strong, nonatomic) UIImage *beautyPanelEyeDistanceIcon;
/// 美颜面板 - 发际线图标
@property (strong, nonatomic) UIImage *beautyPanelForeheadIcon;
/// 美颜面板 - 脸型图标
@property (strong, nonatomic) UIImage *beautyPanelFaceBeautyIcon;
/// 美颜面板 - 眼睛角度图标
@property (strong, nonatomic) UIImage *beautyPanelEyeAngleIcon;
/// 美颜面板 - 鼻翼图标
@property (strong, nonatomic) UIImage *beautyPanelNoseWingIcon;
/// 美颜面板 - 嘴唇厚度图标
@property (strong, nonatomic) UIImage *beautyPanelLipsThicknessIcon;
/// 美颜面板 - 袪皱图标
@property (strong, nonatomic) UIImage *beautyPanelWrinkleRemoveIcon;
/// 美颜面板 - 嘴形图标
@property (strong, nonatomic) UIImage *beautyPanelMouthShapeIcon;
/// 美颜面板 - 袪眼袋图标
@property (strong, nonatomic) UIImage *beautyPanelPounchRemoveIcon;
/// 美颜面板 - 嘴形图标
@property (strong, nonatomic) UIImage *beautyPanelSmileLinesRemoveIcon;
/// 美颜面板 - 亮眼图标
@property (strong, nonatomic) UIImage *beautyPanelEyeLightenIcon;
/// 美颜面板 - 鼻子位置图标
@property (strong, nonatomic) UIImage *beautyPanelNosePositionIcon;
/// 美颜面板 - 关闭效果图标
@property (strong, nonatomic) UIImage *menuDisableIcon;
/// 菜单选中背景图片
@property (strong, nonatomic) UIImage *beautyPanelMenuSelectionBackgroundImage;
/// 菜单文字颜色
@property (strong, nonatomic) UIColor *beautyPanelTitleColor;
/// 菜单文字选中颜色
@property (strong, nonatomic) UIColor *beautyPanelSelectionColor;
/// 录制速度选中文字颜色
@property (strong, nonatomic) UIColor *speedControlSelectedTitleColor;

#pragma mark - Audio Effect
@property (strong, nonatomic) UIImage *audioEffectReverbKTVIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerHeavyMachineryIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerHeavyMetalIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerForeignerIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerFattyIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerUncleIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerLoliIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerBadBoyIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerElectricIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerBeastIcon;
@property (strong, nonatomic) UIImage *audioEffectVoiceChangerEtherealIcon;
@property (strong, nonatomic) UIImage *audioEffectReverbHallIcon;
@property (strong, nonatomic) UIImage *audioEffectReverbRoomIcon;
@property (strong, nonatomic) UIImage *audioEffectReverbMetalIcon;
@property (strong, nonatomic) UIImage *audioEffectReverbLowIcon;
@property (strong, nonatomic) UIImage *audioEffectReverbMagneticIcon;
@property (strong, nonatomic) UIImage *audioEffectReverbSonorousIcon;

@property (readonly, nonatomic) NSBundle *resourceBundle;

- (UIImage *)iconForFilter:(nullable NSString *)filter;
- (void)setIcon:(UIImage *)icon forFilter:(TCFilterIdentifier)identifier;
- (NSString *)localizedString:(NSString *)key __attribute__((annotate("returns_localized_nsstring")));
- (UIImage *)imageNamed:(NSString *)name;
/// 编辑 - 动效图标
- (UIImage *)effectIconWithName:(NSString *)name;
/// 绿幕背景视频路径
- (NSURL *)goodLuckVideoFileURL;
@end
NS_ASSUME_NONNULL_END
