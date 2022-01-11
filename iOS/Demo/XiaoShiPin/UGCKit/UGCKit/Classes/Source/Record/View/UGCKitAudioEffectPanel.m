// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitAudioEffectPanel.h"
#import <BeautySettingKit/TCBeautyPanel.h>

#define L(x) [_theme localizedString:x]

typedef NS_ENUM(NSInteger, AudioEffectMenu) {
    AudioEffectMenuVoiceChanger,
    AudioEffectMenuMix,
};

@interface UGCKitAudioEffectPanelMenuItem : NSObject <TCMenuItem>
@property (strong, nonatomic) NSString *title;
@property (strong, nonatomic) UIImage *icon;
@property (assign, nonatomic) NSInteger effectID;
+ (instancetype)itemWithTitle:(NSString *)title icon:(UIImage *)icon ID:(NSInteger)effectID;
@end

@interface UGCKitAudioEffectPanel() <TCMenuViewDataSource, TCMenuViewDelegate>
{
    UGCKitTheme *_theme;
    TCMenuView *_menu;
    NSArray<NSString *> *_menuArray;
    NSArray<UGCKitAudioEffectPanelMenuItem *> *_reverbOptions;
    NSArray<UGCKitAudioEffectPanelMenuItem *> *_voiceChangerOptions;
}
@end

@implementation UGCKitAudioEffectPanel

- (instancetype)initWithTheme:(UGCKitTheme*)theme frame:(CGRect)frame {
    if (self = [self initWithFrame:frame]) {
        _theme = theme;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    _voiceChangerOptions = @[
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Origin") icon:_theme.menuDisableIcon ID:VOICECHANGER_TYPE_0],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Child") icon:_theme.audioEffectVoiceChangerBadBoyIcon ID:VOICECHANGER_TYPE_1],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Loli") icon:_theme.audioEffectVoiceChangerLoliIcon ID:VOICECHANGER_TYPE_2],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Uncle") icon:_theme.audioEffectVoiceChangerUncleIcon ID:VOICECHANGER_TYPE_3],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.HeavyMetal") icon:_theme.audioEffectVoiceChangerHeavyMetalIcon ID:VOICECHANGER_TYPE_4],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Foreigner") icon:_theme.audioEffectVoiceChangerForeignerIcon ID:VOICECHANGER_TYPE_6],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Beast") icon:_theme.audioEffectVoiceChangerBeastIcon ID:VOICECHANGER_TYPE_7],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.otaku") icon:_theme.audioEffectVoiceChangerFattyIcon ID:VOICECHANGER_TYPE_8],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.StrongCurrent") icon:_theme.audioEffectVoiceChangerElectricIcon ID:VOICECHANGER_TYPE_9],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.HeavyMachinery") icon:_theme.audioEffectVoiceChangerHeavyMachineryIcon ID:VOICECHANGER_TYPE_10],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Ethereal") icon:_theme.audioEffectVoiceChangerEtherealIcon ID:VOICECHANGER_TYPE_11]
    ];
    _reverbOptions = @[
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Origin") icon:_theme.menuDisableIcon ID:REVERB_TYPE_0],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.KTV") icon:_theme.audioEffectReverbKTVIcon ID:REVERB_TYPE_1],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Room") icon:_theme.audioEffectReverbRoomIcon ID:REVERB_TYPE_2],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Hall") icon:_theme.audioEffectReverbHallIcon ID:REVERB_TYPE_3],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Low") icon:_theme.audioEffectReverbLowIcon ID:REVERB_TYPE_4],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Bright") icon:_theme.audioEffectReverbSonorousIcon ID:REVERB_TYPE_5],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Metal") icon:_theme.audioEffectReverbMetalIcon ID:REVERB_TYPE_6],
        [UGCKitAudioEffectPanelMenuItem itemWithTitle:L(@"UGCKit.AudioEffect.Magnetic") icon:_theme.audioEffectReverbMagneticIcon ID:REVERB_TYPE_7],
    ];
    _menuArray = @[[_theme localizedString:@"UGCKit.AudioEffect.Foice"],
                   [_theme localizedString:@"UGCKit.AudioEffect.Mix"]];
    TCMenuView *menu = [[TCMenuView alloc] initWithFrame:self.bounds
                                                      dataSource:self];
    menu.minSubMenuWidth = 54;
    menu.minMenuWidth = 54;
    menu.menuTitleColor = _theme.beautyPanelTitleColor;
    menu.subMenuSelectionColor = _theme.beautyPanelSelectionColor;
    menu.menuSelectionBackgroundImage = _theme.beautyPanelMenuSelectionBackgroundImage;
    menu.delegate = self;
    [self addSubview:menu];
    _menu = menu;
}


- (void)menu:(TCMenuView *)menu didChangeToIndex:(NSInteger)menuIndex option:(NSInteger)optionIndex {
    if (menuIndex == AudioEffectMenuVoiceChanger) {
        if ([self.delegate respondsToSelector:@selector(audioEffectPanel:didSelectVoiceChangerType:)]) {
            [self.delegate audioEffectPanel:self didSelectVoiceChangerType:(TXVideoVoiceChangerType)_voiceChangerOptions[optionIndex].effectID];
        }
    } else if (menuIndex == AudioEffectMenuMix) {
        if ([self.delegate respondsToSelector:@selector(audioEffectPanel:didSelectReverbType:)]) {
            [self.delegate audioEffectPanel:self didSelectReverbType:(TXVideoReverbType)_reverbOptions[optionIndex].effectID];
        }
    }
}
#pragma mark - TCMenuDataSource
- (NSInteger)numberOfMenusInMenu:(TCMenuView *)menu {
    return _menuArray.count;
}
- (NSString *)titleOfMenu:(TCMenuView *)menu atIndex:(NSInteger)index {
    return _menuArray[index];
}
- (NSUInteger)numberOfOptionsInMenu:(TCMenuView *)menu menuIndex:(NSInteger)index {
    return index == AudioEffectMenuVoiceChanger ? _voiceChangerOptions.count : _reverbOptions.count;
}
- (id<TCMenuItem>)menu:(TCMenuView *)menu
           itemAtMenuIndex:(NSInteger)index
               optionIndex:(NSInteger)optionIndex {
    NSArray *container = index == AudioEffectMenuVoiceChanger ? _voiceChangerOptions : _reverbOptions;
    return container[optionIndex];
}
@end

@implementation UGCKitAudioEffectPanelMenuItem
+ (instancetype)itemWithTitle:(NSString *)title icon:(UIImage *)icon ID:(NSInteger)effectID {
    UGCKitAudioEffectPanelMenuItem *item = [[UGCKitAudioEffectPanelMenuItem alloc] init];
    item.title = title;
    item.icon = icon;
    item.effectID = effectID;
    return item;
}
@end
