// Copyright (c) 2019 Tencent. All rights reserved.
#define POD_PITU 1

#import "UGCKitBeautySettingPanel.h"
#import "UGCKitMenuItemCell.h"
#if POD_PITU
#import "ZipArchive.h"
#endif
#import "UGCKitColorMacro.h"
#import "UGCKitFilter.h"
#import "UGCKitMenuView.h"
#import "UGCKitTheme.h"
#import "UGCKitPituMotionManager.h"
#import <objc/message.h>
#import "UGCKitMem.h"

#define BeautyViewMargin 8
#define BeautyViewSliderHeight 30
#define BeautyViewCollectionHeight 50
#define BeautyViewTitleWidth 40

static const CGFloat DefaultBeautyPanelHeight = 170;
static const CGFloat MenuHeight = 130;
static const float DefaultWhitnessLevel = 1;
static const float BeautyMinLevel = 0;
static const float BeautyMaxLevel = 9;

static const float DefaultSmoothBeuatyLevel = 6;
static const float DefaultNatureBeuatyLevel = 6;
static const float DefaultPituBeuatyLevel = 6;

typedef NS_ENUM(NSUInteger, BeautyMenuItem) {
    BeautyMenuItemSmooth,
    BeautyMenuItemNature,
#ifdef UGC_SMART
    BeautyMenuItemLastBeautyTypeItem = BeautyMenuItemNature,
#else
    BeautyMenuItemPiTu,
    BeautyMenuItemLastBeautyTypeItem = BeautyMenuItemPiTu,
#endif
    BeautyMenuItemWhite,
    BeautyMenuItemRed,
    BeautyMenuItemLastBeautyValueItem = BeautyMenuItemRed,
};

@interface UGCKitPituDownloadTask : NSObject
@property (strong, nonatomic) NSURL *url;
@property (strong, nonatomic) NSString *destPath;
@property (strong, nonatomic) NSString *dir;
@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSURLSessionDownloadTask *task;
+ (instancetype)taskWithDestPath:(NSString *)destPath dir:(NSString *)dir name:(NSString *)name;
@end
@implementation UGCKitPituDownloadTask
+ (instancetype)taskWithDestPath:(NSString *)destPath dir:(NSString *)dir name:(NSString *)name
{
    UGCKitPituDownloadTask *ret = [[UGCKitPituDownloadTask alloc] init];
    ret.destPath = destPath;
    ret.dir = dir;
    ret.name = name;
    return ret;
}
@end

#define L(x) [_theme localizedString:x]

/// 菜单条目对象
@interface UGCKitBeautySettingPanelItem : NSObject <UGCKitMenuItem>
@property (strong, nonatomic) NSString *title; ///< 菜单标题
@property (strong, nonatomic) UIImage *icon;   ///< 菜单图标
@property (nullable, weak, nonatomic) id target;///< 菜单动作执行对象，为空时会发送到delegate
@property (assign, nonatomic) SEL action;      ///< 菜单动作
@property (assign, nonatomic) double minValue; ///< 条目值的调节范围下限, 与上限范围相同时不显示滑杆
@property (assign, nonatomic) double maxValue; ///< 条目值的调节范围上限, 与下限范围相同时不显示滑杆
@property (nullable, strong, nonatomic) id userInfo; ///< 附带信息
@end

@implementation UGCKitBeautySettingPanelItem
+ (instancetype)itemWithTitle:(NSString *)title icon:(UIImage *)icon target:(id)target action:(SEL)action minValue:(double)minValue maxValue:(double)maxValue
{
    UGCKitBeautySettingPanelItem *item = [[UGCKitBeautySettingPanelItem alloc] init];
    item.target = target;
    item.title = title;
    item.icon = icon;
    item.action = action;
    item.minValue = minValue;
    item.maxValue = maxValue;
    return item;
}
+ (instancetype)itemWithTitle:(NSString *)title icon:(UIImage *)icon {
    return [self itemWithTitle:title icon:icon target:nil action:nil minValue:0 maxValue:0];
}
@end
static UGCKitBeautySettingPanelItem * makeMenuItem(NSString *title, UIImage *icon, id target, SEL action, double minValue, double maxValue) {
    return [UGCKitBeautySettingPanelItem itemWithTitle:title icon:icon target:target action:action minValue:minValue maxValue:maxValue];
}

#pragma mark -
@interface UGCKitBeautySettingPanel() <UGCKitMenuViewDelegate, NSURLSessionDelegate, NSURLSessionDelegate, NSURLSessionDownloadDelegate>
{
    UGCKitMenuView *_menu;
    NSInteger _previousMenuIndex;
    NSArray<NSArray *> *_optionsContainer;
    NSMutableDictionary<NSNumber*, NSIndexPath*> *_selectedIndexMap;
    NSArray<UGCKitFilter *> *_filters;
    UGCKitTheme *_theme;
    NSObject *_delegatePlaceholder;
    NSURLSession *_urlSession;
    NSMutableDictionary *_runningTask;
}
@property (nonatomic, strong) NSMutableDictionary<NSNumber*,NSNumber*> *beautyLevelDic; ///< 不同美颜类型下的数值
@property (nonatomic, strong) NSMutableDictionary<NSString *,NSNumber*>* filterValueDic; ///< 各滤镜的数值
@property (nonatomic, strong) UILabel *sliderValueLabel;           ///< 滑杆数值显示
@property (nonatomic, strong) UISlider *slider;                    ///< 数值调节滑杆
@property (nonatomic, strong) NSURLSessionDownloadTask *operation; ///< 资源下载
@property (nonatomic, assign) TXBeautyStyle beautyStyle;

@end

@implementation UGCKitBeautySettingPanel

#pragma mark - Public API
- (id)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme;
{
    self = [super initWithFrame:frame];
    if(self){
        _theme = theme;
        [self commonInit];
    }
    return self;
}

- (void)dealloc {
    [_urlSession invalidateAndCancel];
}

- (void)setCurrentFilterIndex:(NSInteger)index {
    index = [self _fixFilterIndex:index];
    _currentFilterIndex = index;
    [_menu setSelectedOption:index inMenu:PanelMenuIndexFilter];
}

- (NSString*)currentFilterName
{
    NSInteger index = self.currentFilterIndex;
    return [_optionsContainer[PanelMenuIndexFilter][index] title];
}

- (float)beautyLevel {
    return [_beautyLevelDic[@(self.beautyStyle)] floatValue];
}
- (float)ruddyLevel {
    return [_beautyLevelDic[@(BeautyMenuItemRed)] floatValue];
}
- (float)whiteLevel {
    return [_beautyLevelDic[@(BeautyMenuItemWhite)] floatValue];
}
/// 重置为默认值
- (void)resetValues
{
    self.slider.hidden = NO;
    self.sliderValueLabel.hidden = NO;

    // 重置美颜滤镜
    [self.beautyLevelDic setObject:@(DefaultSmoothBeuatyLevel) forKey:@(BeautyMenuItemSmooth)]; //美颜默认值（光滑）
    [self.beautyLevelDic setObject:@(DefaultNatureBeuatyLevel) forKey:@(BeautyMenuItemNature)]; //美颜默认值（自然）
#ifndef UGC_SMART
    [self.beautyLevelDic setObject:@(DefaultPituBeuatyLevel) forKey:@(BeautyMenuItemPiTu)];   //美颜默认值（天天PITU）
#endif
    [self.beautyLevelDic setObject:@(DefaultWhitnessLevel) forKey:@(BeautyMenuItemWhite)];   //美颜默认值（天天PITU）
    [self.beautyLevelDic setObject:@(0) forKey:@(BeautyMenuItemRed)];   //美颜默认值（天天PITU）

    const BeautyMenuItem defaultBeautyStyle = BeautyMenuItemNature;
    self.beautyStyle = TXBeautyStyleNature;
    NSInteger beautyValue = [self.beautyLevelDic[@(defaultBeautyStyle)] integerValue];
    self.slider.value = beautyValue;
    self.sliderValueLabel.text = [NSString stringWithFormat:@"%d",(int)beautyValue];
    self.slider.minimumValue = BeautyMinLevel;
    self.slider.maximumValue = BeautyMaxLevel;

    // 重置滤镜
    NSDictionary *defaultFilterValue = @{
                                     UGCKitFilterIdentifierNone :@(0)
                                    ,UGCKitFilterIdentifierBiaozhun :@(5)
                                    ,UGCKitFilterIdentifierYinghong :@(8)
                                    ,UGCKitFilterIdentifierYunshang :@(8)
                                    ,UGCKitFilterIdentifierChunzhen :@(7)
                                    ,UGCKitFilterIdentifierBailan :@(10)
                                    ,UGCKitFilterIdentifierYuanqi :@(8)
                                    ,UGCKitFilterIdentifierChaotuo :@(10)
                                    ,UGCKitFilterIdentifierXiangfen :@(5)
                                    ,UGCKitFilterIdentifierWhite :@(3)
                                    ,UGCKitFilterIdentifierLangman :@(3)
                                    ,UGCKitFilterIdentifierQingxin :@(3)
                                    ,UGCKitFilterIdentifierWeimei :@(3)
                                    ,UGCKitFilterIdentifierFennen :@(3)
                                    ,UGCKitFilterIdentifierHuaijiu :@(3)
                                    ,UGCKitFilterIdentifierLandiao :@(3)
                                    ,UGCKitFilterIdentifierQingliang :@(3)
                                    ,UGCKitFilterIdentifierRixi :@(3)
                                    };
    self.filterValueDic = [defaultFilterValue mutableCopy];
}

+ (NSUInteger)getHeight
{
    return DefaultBeautyPanelHeight;
}

- (NSInteger)_fixFilterIndex:(NSInteger)index {
    const NSInteger itemCount = _optionsContainer[PanelMenuIndexFilter].count;
    if (index < 0)
        index = itemCount - 1;
    if (index > itemCount - 1)
        index = 0;
    return index;
}

- (UIImage*)filterImageByIndex:(NSInteger)index
{
    index = [self _fixFilterIndex:index];

    if (index == 0) {
        return nil;
    }
    UGCKitFilter *filter = _filters[index-1];
    return [UIImage imageWithContentsOfFile:filter.lookupImagePath];
}

-(float)filterMixLevelByIndex:(NSInteger)index
{
    index = [self _fixFilterIndex:index];

    if (index == 0) {
        return 0;
    }
    index -= 1;
    NSString *filterID = _filters[index].identifier;
    return [self.filterValueDic[filterID] floatValue];
}

#pragma mark - Init

- (void)commonInit
{
    _runningTask = [[NSMutableDictionary alloc] initWithCapacity:1];
    _urlSession = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]
                                                delegate:self
                                           delegateQueue:[NSOperationQueue mainQueue]];
    self.backgroundColor = [_theme.backgroundColor colorWithAlphaComponent:0.6];
    _delegatePlaceholder = [[NSObject alloc] init];
    _filters = [UGCKitFilterManager defaultManager].allFilters;

    // Menu Setup
    UGCKitBeautySettingPanelItem *disableItem = [UGCKitBeautySettingPanelItem itemWithTitle:L(@"UGCKit.BeautySettingPanel.None") icon:_theme.menuDisableIcon];

    NSMutableArray *filters = [NSMutableArray arrayWithCapacity:_filters.count];
    [filters addObject:[UGCKitBeautySettingPanelItem itemWithTitle:L(@"UGCKit.Common.Clear") icon:_theme.menuDisableIcon]];
    for (UGCKitFilter *filter in _filters) {
        NSString *identifier = [NSString stringWithFormat:@"UGCKit.Common.Filter_%@", filter.identifier];
        [filters addObject:[UGCKitBeautySettingPanelItem itemWithTitle:L(identifier) icon:[_theme iconForFilter:filter.identifier]]];
    }

    NSArray *beautyArray = @[
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.BeautySmooth"), _theme.beautyPanelSmoothBeautyStyleIcon, nil, nil, 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.Beauty-Natural"), _theme.beautyPanelNatureBeautyStyleIcon, nil, nil, 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.Beauty-P"),     _theme.beautyPanelPTuBeautyStyleIcon, nil, nil, 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.White"), _theme.beautyPanelWhitnessIcon, nil, nil, 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.Ruddy"), _theme.beautyPanelRuddyIcon,  nil, nil, 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.BigEyes"), _theme.beautyPanelEyeScaleIcon, nil, @selector(setEyeScaleLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.ThinFace"), _theme.beautyPanelFaceSlimIcon, nil, @selector(setFaceSlimLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.VFace"), _theme.beautyPanelFaceVIcon, nil, @selector(setFaceVLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.Chin"), _theme.beautyPanelChinIcon, nil, @selector(setChinLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.ShortFace"), _theme.beautyPanelFaceScaleIcon, nil, @selector(setFaceShortLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.ThinNose"), _theme.beautyPanelNoseSlimIcon, nil, @selector(setNoseSlimLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.EyeLighten"), _theme.beautyPanelEyeLightenIcon, nil, @selector(setEyeLightenLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.ToothWhiten"), _theme.beautyPanelToothWhitenIcon, nil, @selector(setToothWhitenLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.WrinkleRemove"), _theme.beautyPanelWrinkleRemoveIcon, nil, @selector(setWrinkleRemoveLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.PounchRemove"),  _theme.beautyPanelPounchRemoveIcon, nil, @selector(setPounchRemoveLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.SmileLinesRemove"), _theme.beautyPanelSmileLinesRemoveIcon, nil, @selector(setSmileLinesRemoveLevel:), 0, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.Forehead"),  _theme.beautyPanelForeheadIcon, nil, @selector(setForeheadLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.EyeDistance"), _theme.beautyPanelEyeDistanceIcon, nil,  @selector(setEyeDistanceLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.EyeAngle"), _theme.beautyPanelEyeAngleIcon, nil,  @selector(setEyeAngleLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.MouthShape"), _theme.beautyPanelMouthShapeIcon, nil,  @selector(setMouthShapeLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.NoseWing"), _theme.beautyPanelNoseWingIcon, nil,  @selector(setNoseWingLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.NosePosition"), _theme.beautyPanelNosePositionIcon, nil,  @selector(setNosePositionLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.LipsThickness"), _theme.beautyPanelLipsThicknessIcon, nil,  @selector(setLipsThicknessLevel:), -10, 10),
        makeMenuItem(L(@"UGCKit.BeautySettingPanel.FaceBeauty"), _theme.beautyPanelFaceBeautyIcon, nil,  @selector(setFaceBeautyLevel:), 0, 10),
    ];

    NSArray *(^makeMenuItemsFromPituMotions)(NSArray<UGCKitPituMotion *> *motions) = ^(NSArray<UGCKitPituMotion *> *motions) {
        NSMutableArray *result = [@[disableItem] mutableCopy];
        for (UGCKitPituMotion *motion in motions) {
            UGCKitBeautySettingPanelItem *item = [UGCKitBeautySettingPanelItem itemWithTitle:motion.name
                                                                            icon:[self->_theme imageNamed:motion.identifier]];
            item.userInfo = motion;
            [result addObject:item];
        }
        return result;
    };
    NSArray *motionArray        = makeMenuItemsFromPituMotions([UGCKitPituMotionManager sharedInstance].motionPasters);
    NSArray *koubeiArray        = makeMenuItemsFromPituMotions([UGCKitPituMotionManager sharedInstance].backgroundRemovalPasters);
    NSArray *cosmeticArray      = makeMenuItemsFromPituMotions([UGCKitPituMotionManager sharedInstance].cosmeticPasters);
    NSArray *gestureEffectArray = makeMenuItemsFromPituMotions([UGCKitPituMotionManager sharedInstance].gesturePasters);
    NSArray *greenArray = @[disableItem,
                            [UGCKitBeautySettingPanelItem itemWithTitle:L(@"UGCKit.BeautySettingPanel.GoodLuck") icon:_theme.beautyPanelGoodLuckIcon]
                           ];

    NSArray *menuArray = @[
                   L(@"UGCKit.Record.Menu.Beauty"),
                   L(@"UGCKit.Record.Menu.Filter"),
                   L(@"UGCKit.Record.Menu.VideoEffect"),
                   L(@"UGCKit.Record.Menu.Cosmetic"),
                   L(@"UGCKit.Record.Menu.Gesture"),
                   L(@"UGCKit.Record.Menu.BlendPic"),
                   L(@"UGCKit.Record.Menu.GreenScreen")];

    _optionsContainer = @[ beautyArray, filters, motionArray, cosmeticArray, gestureEffectArray, koubeiArray, greenArray];


    UGCKitMenuView *menu = [[UGCKitMenuView alloc] initWithFrame:CGRectMake(0, CGRectGetHeight(self.bounds) - MenuHeight,
                                                                            CGRectGetWidth(self.bounds), MenuHeight)
                                                           menus:menuArray
                                                     menuOptions:_optionsContainer];
    menu.delegate = self;
    menu.minSubMenuWidth = 54;
    menu.minMenuWidth = 54;
    menu.menuTitleColor = _theme.beautyPanelTitleColor;
    menu.subMenuSelectionColor = _theme.beautyPanelSelectionColor;
    menu.menuSelectionBackgroundImage = _theme.beautyPanelMenuSelectionBackgroundImage;
    menu.subMenuBackgroundColor = [UIColor clearColor];
    menu.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    [self addSubview:menu];
    _menu = menu;

    // Slider Setup
    self.slider.frame = CGRectMake(BeautyViewMargin * 4, CGRectGetMinY(menu.frame) - BeautyViewMargin - BeautyViewSliderHeight,
                                         CGRectGetWidth(self.bounds) - 10 * BeautyViewMargin - BeautyViewSliderHeight, BeautyViewSliderHeight);
    self.slider.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
    [self addSubview:self.slider];

    self.sliderValueLabel.frame = CGRectMake(self.slider.frame.size.width + self.slider.frame.origin.x + BeautyViewMargin, BeautyViewMargin, BeautyViewSliderHeight, BeautyViewSliderHeight);
    self.sliderValueLabel.layer.cornerRadius = self.sliderValueLabel.frame.size.width / 2;
    self.sliderValueLabel.layer.masksToBounds = YES;
    [self addSubview:self.sliderValueLabel];
    self.sliderValueLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
}


#pragma mark - Menu Delegate
- (void)menu:(UGCKitMenuView *)menu didChangeToIndex:(NSInteger)menuIndex option:(NSInteger)optionIndex {
    self.sliderValueLabel.hidden  = menuIndex != PanelMenuIndexBeauty;
    self.slider.hidden = self.sliderValueLabel.hidden;

    switch (menuIndex) {
        case PanelMenuIndexBeauty: {
            float value = [[self.beautyLevelDic objectForKey:[NSNumber numberWithInteger:optionIndex]] floatValue];

            if (optionIndex < 3) {
                self.beautyStyle = optionIndex;
            }

            UGCKitBeautySettingPanelItem *item = _optionsContainer[menu.menuIndex][menu.optionIndex];
            if ([item isKindOfClass:[UGCKitBeautySettingPanelItem class]]) {
                self.slider.minimumValue = item.minValue;
                self.slider.maximumValue = item.maxValue;
            }

            self.sliderValueLabel.text = [NSString stringWithFormat:@"%d",(int)value];
            [self.slider setValue:value];
            [self _applyBeautySettings];
        } break;
        case PanelMenuIndexFilter: {
            _currentFilterIndex = optionIndex;
            [self onSetFilterAtMenuIndex:optionIndex];
            if (optionIndex > 0) {
                UGCKitFilterIdentifier filterId = _filters[optionIndex - 1].identifier;
                NSNumber* value = self.filterValueDic[filterId];
                [self.slider setValue:value.floatValue];
                self.slider.hidden = NO;
                self.sliderValueLabel.hidden = NO;
                [self onSetFilterAtMenuIndex:optionIndex];
                [self onSliderValueChanged:self.slider];
            }
        }
            break;
        case PanelMenuIndexMotion: case PanelMenuIndexGesture: case PanelMenuIndexCosmetic: case PanelMenuIndexKoubei:
            if (!(_previousMenuIndex != menuIndex && optionIndex == 0)) {
                // 切换一级菜单后，如果新的菜单选则的取消，不关闭动效
                [self onSetMotionWithIndex:optionIndex];
            }

            break;
        case PanelMenuIndexGreen:
            [self onSetGreenWithIndex:optionIndex];
            break;

        default:
            break;
    }
    _previousMenuIndex = menuIndex;
}

#pragma mark - Value Change Event Handlers
- (void)_applyBeautySettings {
    if ([self.delegate respondsToSelector:@selector(setBeautyStyle:)]) {
        [self.delegate setBeautyStyle:(TXBeautyStyle)self.beautyStyle];
    }
    if ([self.delegate respondsToSelector:@selector(setBeautyLevel:)]) {
        [self.delegate setBeautyLevel:self.beautyLevel];
    }
    if ([self.delegate respondsToSelector:@selector(setWhitenessLevel:)]) {
        [self.delegate setWhitenessLevel:self.whiteLevel];
    }
    if ([self.delegate respondsToSelector:@selector(setRuddyLevel:)]) {
        [self.delegate setRuddyLevel:self.ruddyLevel];
    }
}

- (void)_applyMenuItem:(UGCKitBeautySettingPanelItem *)item value:(float)value {
    id target = item.target ?: self.delegate;

    if ([item isKindOfClass:[UGCKitBeautySettingPanelItem class]] && [target respondsToSelector:item.action]) {
        // 这里当参数类型变化时要注意修改为对应类型
        typedef float ParamType;
#if DEBUG
        // 参数类型检查
        NSMethodSignature *signature = [[target class] instanceMethodSignatureForSelector:item.action];
        const char *type = [signature getArgumentTypeAtIndex:2];
        NSAssert(strcmp(type, @encode(ParamType)) == 0, @"type mismatch");
#endif
        void(*setter)(id,SEL,ParamType) = (void(*)(id,SEL,ParamType))objc_msgSend;
        setter(target, item.action, (float)value);
    }
}

#pragma mark - value changed
- (void)onSliderValueChanged:(UISlider *)slider
{
    float value = slider.value;
    self.sliderValueLabel.text = [NSString stringWithFormat:@"%.0f", value];
    NSInteger menuIndex = _menu.menuIndex;
    if(menuIndex == PanelMenuIndexFilter) {
        NSString *filterID = _filters[_menu.optionIndex-1].identifier;
        self.filterValueDic[filterID] = @(value);
        if([self.delegate respondsToSelector:@selector(onSetFilterMixLevel:)]){
            [self.delegate onSetFilterMixLevel:value];
        }
    } else if(menuIndex == PanelMenuIndexBeauty) {
        // 美颜数值变化
        NSInteger beautyIndex = _menu.optionIndex;// (int)[self selectedIndexPathForMenu:PanelMenuIndexBeauty].row;

        if(beautyIndex <= BeautyMenuItemLastBeautyValueItem) { // 选中的美颜
            if (beautyIndex <= BeautyMenuItemLastBeautyTypeItem) {  // 选中的美颜模式，更改 beautyLevel
                self.beautyLevelDic[@(self.beautyStyle)] = @(value);
            } else { // BeautyMenuItemWhite || BeautyMenuItemRed
                self.beautyLevelDic[@(beautyIndex)] = @(value);
            }
            [self _applyBeautySettings];
        } else { // 选中的大眼瘦脸等效果
            UGCKitBeautySettingPanelItem *item = _optionsContainer[PanelMenuIndexBeauty][_menu.optionIndex];
            [self _applyMenuItem:item value:value];
        }
    }
}

- (void)onSetFilterAtMenuIndex:(NSInteger)index
{
    if ([self.delegate respondsToSelector:@selector(onSetFilter:)]) {
        UIImage* image = [self filterImageByIndex:index];
        [self.delegate onSetFilter:image];
    }
}


- (void)onSetGreenWithIndex:(NSInteger)index
{
    if ([self.delegate respondsToSelector:@selector(onSetGreenScreenFile:)]) {
        if (index == 0) {
            [self.delegate onSetGreenScreenFile:nil];
        }
        if (index == 1) {
            [self.delegate onSetGreenScreenFile:[_theme goodLuckVideoFileURL]];
        }
    }
}

- (void)onSetMotionWithIndex:(NSInteger)index
{
    if ([self.delegate respondsToSelector:@selector(onSelectMotionTmpl:inDir:)]) {
        NSString *localPackageDir = [NSHomeDirectory() stringByAppendingPathComponent:@"Documents/packages"];
        if (![[NSFileManager defaultManager] fileExistsAtPath:localPackageDir]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:localPackageDir withIntermediateDirectories:NO attributes:nil error:nil];
        }
        if (index == 0){
            [self.delegate onSelectMotionTmpl:nil inDir:localPackageDir];
        } else{
            UGCKitBeautySettingPanelItem *item = _optionsContainer[_menu.menuIndex][_menu.optionIndex];
            UGCKitPituMotion *motion = item.userInfo;
            NSString *pituPath = [NSString stringWithFormat:@"%@/%@", localPackageDir, motion.identifier];
            if ([[NSFileManager defaultManager] fileExistsAtPath:pituPath]) {
                [self.delegate onSelectMotionTmpl:motion.identifier inDir:localPackageDir];
            }else{
                [self startLoadPitu:localPackageDir pituName:motion.identifier packageURL:motion.url];
            }
        }
    }
}

- (void)startLoadPitu:(NSString *)pituDir pituName:(NSString *)pituName packageURL:(NSURL *)packageURL{
    NSURLSessionDownloadTask *task = nil;
    @synchronized (_runningTask) {
        if (_runningTask[packageURL]) {
            return;
        }
        NSString *targetPath = [NSString stringWithFormat:@"%@/%@.zip", pituDir, pituName];
        UGCKitPituDownloadTask *downloadTask = [UGCKitPituDownloadTask taskWithDestPath:targetPath dir:pituDir name:pituName];

        if ([[NSFileManager defaultManager] fileExistsAtPath:targetPath]) {
            [[NSFileManager defaultManager] removeItemAtPath:targetPath error:nil];
        }

        NSURLRequest *downloadReq = [NSURLRequest requestWithURL:packageURL
                                                     cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                                                 timeoutInterval:30.f];
        task = [_urlSession downloadTaskWithRequest:downloadReq];
        self->_runningTask[packageURL] = downloadTask;
    }
    [self.pituDelegate onLoadPituStart];
    [task resume];
}

#pragma mark - NSURLSessionDownloadDelegate
- (void)URLSession:(NSURLSession *)session
              task:(NSURLSessionTask *)task
didCompleteWithError:(nullable NSError *)error
{
    if (error) {
        NSURL *packageURL = task.originalRequest.URL;
        @synchronized (self->_runningTask) {
            [self->_runningTask removeObjectForKey:packageURL];
        }
        [self.pituDelegate onLoadPituFailed];
    }
}

- (void)URLSession:(NSURLSession *)session
      downloadTask:(NSURLSessionDownloadTask *)downloadTask
didFinishDownloadingToURL:(NSURL *)location
{
    NSURL *packageURL = downloadTask.originalRequest.URL;
    UGCKitPituDownloadTask *task = self->_runningTask[packageURL];
    @synchronized (self->_runningTask) {
        [self->_runningTask removeObjectForKey:packageURL];
    }
    NSError *fsErr = nil;
    [[NSFileManager defaultManager] moveItemAtURL:location toURL:[NSURL fileURLWithPath:task.destPath]
                                            error:&fsErr];
    if (fsErr) {
        [self.pituDelegate onLoadPituFailed];
        return;
    }
    NSString *targetPath = task.destPath;
    NSString *pituDir = task.dir;
    NSString *pituName = task.name;
    // 解压
    BOOL unzipSuccess = NO;
    ZipArchive *zipArchive = [[ZipArchive alloc] init];
    if ([zipArchive UnzipOpenFile:targetPath]) {
        unzipSuccess = [zipArchive UnzipFileTo:pituDir overWrite:YES];
        [zipArchive UnzipCloseFile];

        // 删除zip文件
        NSError *fsErr = nil;
        [[NSFileManager defaultManager] removeItemAtPath:targetPath error:&fsErr];
        if (fsErr) {
            NSLog(@"Error when removing temp file: %@", fsErr);
        }
    }
    if (unzipSuccess) {
        [self.pituDelegate onLoadPituFinished];
        [self.delegate onSelectMotionTmpl:pituName inDir:pituDir];
    } else {
        [self.pituDelegate onLoadPituFailed];
    }
}

- (void)URLSession:(NSURLSession *)session
      downloadTask:(NSURLSessionDownloadTask *)downloadTask
      didWriteData:(int64_t)bytesWritten
 totalBytesWritten:(int64_t)totalBytesWritten
totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite {
    if (self.pituDelegate) {
        double progress = (double)totalBytesWritten / totalBytesExpectedToWrite;
        [self.pituDelegate onLoadPituProgress:progress];
    }
}

#pragma mark -
- (NSMutableDictionary *)beautyLevelDic
{
    if(!_beautyLevelDic){
        _beautyLevelDic = [[NSMutableDictionary alloc] init];
    }
    return _beautyLevelDic;
}

- (UISlider *)slider
{
    if(!_slider){
        _slider = [[UISlider alloc] init];
        _slider.minimumValue = 0;
        _slider.maximumValue = 10;
        [_slider setMinimumTrackTintColor:_theme.sliderMinColor];
        [_slider setMaximumTrackTintColor:_theme.sliderMaxColor];
        [_slider setThumbImage:_theme.sliderThumbImage forState:UIControlStateNormal];
        [_slider addTarget:self action:@selector(onSliderValueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _slider;
}

- (UILabel *)sliderValueLabel
{
    if(!_sliderValueLabel){
        _sliderValueLabel = [[UILabel alloc] init];
        _sliderValueLabel.backgroundColor = [UIColor whiteColor];
        _sliderValueLabel.textAlignment = NSTextAlignmentCenter;
        _sliderValueLabel.text = @"0";
        [_sliderValueLabel setTextColor:UIColorFromRGB(0xFF584C)];
    }
    return _sliderValueLabel;
}

@end
