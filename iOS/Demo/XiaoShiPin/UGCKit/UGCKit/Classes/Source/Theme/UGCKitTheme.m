// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitTheme.h"
#import <objc/runtime.h>

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
green:((float)((rgbValue & 0xFF00) >> 8))/255.0 \
 blue:((float)(rgbValue & 0xFF))/255.0 \
alpha:1.0]

#define RGB(r,g,b) [UIColor colorWithRed:r/255.0f green:g/255.0f blue:b/255.0f alpha:1]

@interface UGCKitTheme ()
{
    NSMutableDictionary<NSString *, UIImage *> *_imageDict;
    NSMutableDictionary<NSString *, UIImage *> *_filterIconDictionary;
    NSBundle *_resourceBundle;
    NSBundle *_beautyPanelResourceBundle;
}
@property (strong, nonatomic) NSBundle *beautyPanelResourceBundle;
@end

@implementation UGCKitTheme
@dynamic closeIcon, progressTrackImage, rightArrowIcon;
@dynamic transitionLeftRightIcon;
@dynamic transitionUpDownIcon;
@dynamic transitionZoomInIcon;
@dynamic transitionZoomOutIcon;
@dynamic transitionRotateIcon;
@dynamic transitionFadeInOutIcon;
@dynamic recordMusicDownloadIcon;
@dynamic nextIcon,backIcon,recordAspect43Icon,recordAspect34Icon,recordAspect11Icon,recordAspect169Icon,recordAspect916Icon,\
recordMusicIcon,recordBeautyIcon,recordAudioEffectIcon,recordCountDownIcon,recordTorchOnIcon,recordTorchOnHighlightedIcon,recordTorchOffIcon;
@dynamic recordTorchOffHighlightedIcon, recordTorchDisabledIcon,recordButtonTapModeIcon,recordButtonPhotoModeIcon,recordButtonPhotoModeBackgroundImage,\
recordButtonPauseInnerIcon,recordButtonPauseBackgroundImage,recordSwitchCameraIcon, \
recordDeleteHighlightedIcon, recordDeleteIcon, recordButtonModeSwitchIndicatorIcon;
@dynamic recordSpeedCenterIcon;
@dynamic recordSpeedLeftIcon;
@dynamic recordSpeedRightIcon;
@dynamic recordMusicSampleImage;
@dynamic recordMusicSwitchIcon;
@dynamic recordMusicDeleteIcon;
@dynamic editPanelAddPasterIcon,editPanelCloseIcon,editPanelConfirmIcon,\
editPanelMusicIcon,editPanelEffectIcon,editPanelSpeedIcon,editPanelFilterIcon,editPanelPasterIcon,editPanelSubtitleIcon,editPanelMusicHighlightedIcon,\
editPanelEffectHighlightedIcon,editPanelSpeedHighlightedIcon,editPanelFilterHighlightedIcon,editPanelPasterHighlightedIcon,editPanelSubtitleHighlightedIcon,\
editPanelDeleteIcon,editPanelDeleteHighlightedIcon,editPlayIcon,editPlayHighlightedIcon,editPauseIcon,editPauseHighlightedIcon,editChooseVideoIcon,confirmIcon,\
confirmHighlightedIcon,editTimelineIndicatorIcon;
@dynamic editTimeEffectIndicatorIcon;
@dynamic editCutSliderLeftIcon;
@dynamic editCutSliderRightIcon;
@dynamic editCutSliderCenterIcon;
@dynamic editMusicSliderRightIcon;
@dynamic editMusicSliderLeftIcon;
@dynamic editPasterDeleteIcon;
@dynamic editTextPasterRotateIcon;
@dynamic editTextPasterEditIcon;
@dynamic editTextPasterConfirmIcon;

@dynamic editFilterSelectionIcon;

@dynamic beautyPanelSmoothBeautyStyleIcon;
@dynamic beautyPanelEyeScaleIcon;
@dynamic beautyPanelPTuBeautyStyleIcon;
@dynamic beautyPanelNatureBeautyStyleIcon;
@dynamic beautyPanelRuddyIcon;
@dynamic beautyPanelBgRemovalIcon;
@dynamic beautyPanelWhitnessIcon;
@dynamic beautyPanelFaceSlimIcon;
@dynamic beautyPanelGoodLuckIcon;
@dynamic beautyPanelChinIcon;
@dynamic beautyPanelFaceVIcon;
@dynamic beautyPanelFaceScaleIcon;
@dynamic beautyPanelNoseSlimIcon;
@dynamic beautyPanelToothWhitenIcon;
@dynamic beautyPanelEyeDistanceIcon;
@dynamic beautyPanelForeheadIcon;
@dynamic beautyPanelFaceBeautyIcon;
@dynamic beautyPanelEyeAngleIcon;
@dynamic beautyPanelNoseWingIcon;
@dynamic beautyPanelLipsThicknessIcon;
@dynamic beautyPanelWrinkleRemoveIcon;
@dynamic beautyPanelMouthShapeIcon;
@dynamic beautyPanelPounchRemoveIcon;
@dynamic beautyPanelSmileLinesRemoveIcon;
@dynamic beautyPanelEyeLightenIcon;
@dynamic beautyPanelNosePositionIcon;
@dynamic beautyPanelMenuSelectionBackgroundImage;
@dynamic menuDisableIcon;
@dynamic sliderThumbImage;
@dynamic audioEffectReverbKTVIcon;
@dynamic audioEffectVoiceChangerHeavyMachineryIcon;
@dynamic audioEffectVoiceChangerHeavyMetalIcon;
@dynamic audioEffectVoiceChangerForeignerIcon;
@dynamic audioEffectVoiceChangerFattyIcon;
@dynamic audioEffectVoiceChangerUncleIcon;
@dynamic audioEffectVoiceChangerLoliIcon;
@dynamic audioEffectVoiceChangerBadBoyIcon;
@dynamic audioEffectVoiceChangerElectricIcon;
@dynamic audioEffectVoiceChangerBeastIcon;
@dynamic audioEffectVoiceChangerEtherealIcon;
@dynamic audioEffectReverbHallIcon;
@dynamic audioEffectReverbRoomIcon;
@dynamic audioEffectReverbMetalIcon;
@dynamic audioEffectReverbLowIcon;
@dynamic audioEffectReverbMagneticIcon;
@dynamic audioEffectReverbSonorousIcon;

static BOOL isBeuatyPanelThemeMethod(SEL selector) {
    static NSMutableDictionary<NSString*, NSNumber*> *cache = nil;
    if (nil == cache) {
        cache = [NSMutableDictionary dictionary];
    }
    NSString *name = NSStringFromSelector(selector);
    NSNumber *val = cache[name];
    if (val) {
        return val.boolValue;
    }

    unsigned int outCount = 0;
    struct objc_method_description *descriptions
    = protocol_copyMethodDescriptionList(@protocol(TCBeautyPanelThemeProtocol),
                                         YES,
                                         YES,
                                         &outCount);
    for (unsigned int i = 0; i < outCount; ++i) {
        if (descriptions[i].name == selector) {
            free(descriptions);
            cache[name] = @(YES);
            return YES;
        }
    }
    free(descriptions);
    cache[name] = @(NO);
    return NO;
}

static UIImage *getImageByName(UGCKitTheme *self, SEL selector) {
    NSString *selName = NSStringFromSelector(selector);
    NSString *key = [[[selName substringToIndex:1] lowercaseString] stringByAppendingString:[selName substringFromIndex:1]];
    UIImage *image = [self imageForKey:key];
    if (nil == image) {
        NSBundle *bundle = self.resourceBundle;
        if (isBeuatyPanelThemeMethod(selector)) {
            bundle = self.beautyPanelResourceBundle;
        }
        image = [UIImage imageNamed:NSStringFromSelector(selector) inBundle:bundle compatibleWithTraitCollection:nil];
    }
    if (nil == image) {
        NSLog(@"%@ %@ image not found", NSStringFromClass([self class]), key);
    }
    return image;
}

static void setImageForKey(id self, SEL selector, UIImage *image) {
    NSString *selName = NSStringFromSelector(selector);
    NSString *attrName = [[selName substringFromIndex:3] stringByTrimmingCharactersInSet:
                          [NSCharacterSet characterSetWithCharactersInString:@":"]];
    NSString *key = [[[attrName substringToIndex:1] lowercaseString] stringByAppendingString:[attrName substringFromIndex:1]];
    [self setImage:image forKey:key];
}


+ (BOOL)resolveInstanceMethod:(SEL)sel
{
    NSString *selName = NSStringFromSelector(sel);
    if ([selName hasPrefix:@"set"]) {
        if ([selName hasSuffix:@"Icon:"] || [selName hasSuffix:@"Image:"]) {
            class_addMethod([self class], sel, (IMP)setImageForKey, "@@:@");
            return YES;
        }
    } else if ([selName hasSuffix:@"Icon"] || [selName hasSuffix:@"Image"]) {
        class_addMethod([self class], sel, (IMP)getImageByName, "@@:");
        return YES;
    }
    return [super resolveInstanceMethod:sel];
}

+ (instancetype)sharedTheme {
    static UGCKitTheme *theme = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        theme = [[UGCKitTheme alloc] init];
    });
    return theme;
}

- (instancetype)init {
    if (self = [super init]) {
        NSString *resourcePath = [[NSBundle mainBundle] pathForResource:@"UGCKitResources"
                                                                 ofType:@"bundle"];
        NSBundle *bundle = [NSBundle bundleWithPath:resourcePath];
        _resourceBundle = bundle ?: [NSBundle mainBundle];
        NSString *beautyPanelResPath = [bundle pathForResource:@"TCBeautyPanelResources"
                                                        ofType:@"bundle"];
        if (!beautyPanelResPath) {
            beautyPanelResPath = [[NSBundle mainBundle] pathForResource:@"TCBeautyPanelResources"
        ofType:@"bundle"];
        }
        _beautyPanelResourceBundle = [NSBundle bundleWithPath:beautyPanelResPath];

        _backgroundColor = [UIColor colorWithRed:0.12 green:0.15 blue:0.19 alpha:1];
        _editPanelBackgroundColor = [UIColor blackColor];
        _editPanelTextColor = [UIColor whiteColor];
        _titleColor = [UIColor whiteColor];

        _recordTimelineColor = UIColorFromRGB(0xFF584C);
        _recordTimelineSelectionColor = UIColorFromRGB(0xA8002D);
        _recordTimelineSeperatorColor = UIColorFromRGB(0xA8002D);

        _beautyPanelTitleColor = [UIColor whiteColor];
        _beautyPanelSelectionColor = [UIColor colorWithRed:0xff/255.0 green:0x58/255.0 blue:0x4c/255.0 alpha:1];
        _sliderMinColor =  RGB(238, 100, 85);// RGB(166, 166, 165);
        _sliderValueColor = [UIColor colorWithRed:1.0 green:0x58/255.0 blue:0x4c/255.0 alpha:1];
        _progressColor = RGB(238, 100, 85);
        _pickerSelectionBorderColor = RGB(255, 88, 76);
        _editPasterBorderColor = [UIColor whiteColor];
        _imageDict = [NSMutableDictionary dictionary];
        _editCutSliderBorderColor = RGB(239, 100, 85);
        _editMusicSliderBorderColor = _editCutSliderBorderColor;
    }
    return self;
}

- (UIImage *)imageForKey:(NSString *)key
{
    return _imageDict[key];
}

- (void)setImage:(UIImage *)image forKey:(NSString *)key
{
    _imageDict[key] = image;
}

- (UIImage *)iconForFilter:(NSString *)filter;
{
    UIImage *image = _filterIconDictionary[filter];
    if (image) {
        return image;
    }
    NSString *imageName = filter;
    if (nil == filter) {
        imageName = @"original";
    } else if ([filter isEqualToString:@"white"]) {
        imageName = @"fwhite";
    }
    return [UIImage imageNamed:imageName
                      inBundle:_beautyPanelResourceBundle
 compatibleWithTraitCollection:nil];
}

- (UIImage *)imageNamed:(NSString *)name {
    UIImage *image = [UIImage imageNamed:name
                                inBundle:_beautyPanelResourceBundle
           compatibleWithTraitCollection:nil];
    if (nil == image) {
        image = [UIImage imageNamed:name
                           inBundle:_resourceBundle
      compatibleWithTraitCollection:nil];

    }
    return image;
}

- (void)setIcon:(UIImage *)icon forFilter:(TCFilterIdentifier)identifier
{
    if (_filterIconDictionary == nil) {
        _filterIconDictionary = [NSMutableDictionary dictionaryWithObject:icon forKey:identifier];
    } else {
        _filterIconDictionary[identifier] = icon;
    }
}

//- (UIColor*)_autoContentColor {
//    if (@available(iOS 13, *)) {
//        return [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
//            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
//                return [UIColor whiteColor];
//            } else {
//                return [UIColor blackColor];
//            }
//        }];
//    } else {
//        return [UIColor whiteColor];
//    }
//}

//- (UIColor*)_autoBackgroundColor {
//    if (@available(iOS 13, *)) {
//        return [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
//            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
//                return [UIColor whiteColor];
//            } else {
//                return [UIColor blackColor];
//            }
//        }];
//    } else {
//        return [UIColor whiteColor];
//    }
//}

- (NSString *)localizedString:(NSString *)key {
    return [_resourceBundle localizedStringForKey:key value:@"" table:nil];
}

- (UIImage *)effectIconWithName:(NSString *)name {
    NSString *path = [@"VideoEffects" stringByAppendingPathComponent:name];
    return [self effectIconWithPath:path frameDuration:1.0/20];
}

- (NSArray *)imagesWithPath:(NSString *)path {
    NSString *rootPath =[NSBundle mainBundle].bundlePath;
    NSString *dir = [rootPath stringByAppendingPathComponent:path];
    NSFileManager *fm = [[NSFileManager alloc] init];
    NSMutableArray *files = [[NSMutableArray alloc] init];
    for (NSString *item in [fm enumeratorAtPath:dir]) {
        [files addObject:item];
    }
    [files sortUsingSelector:@selector(compare:)];
    NSMutableArray *images = [NSMutableArray arrayWithCapacity:files.count];
    for (NSString *path in files) {
        UIImage *image = [UIImage imageWithContentsOfFile:[dir stringByAppendingPathComponent:path]];
        [images addObject:image];
    }
    return images;
}

- (UIImage *)effectIconWithPath:(NSString *)path frameDuration:(float)frameDuration {
    NSArray *images = [self imagesWithPath:path];
    return [UIImage animatedImageWithImages:images duration:frameDuration * images.count];
}

- (UIImage *)editTimeEffectNormalIcon {
    NSString *key = NSStringFromSelector(@selector(editTimeEffectNormalIcon));
    if (_imageDict[key]) {
        return _imageDict[key];
    }

    return [self effectIconWithPath:@"jump" frameDuration: 1.0 / 20];
}

- (UIImage *)editTimeEffectReveseIcon {
    NSString *key = NSStringFromSelector(@selector(editTimeEffectReveseIcon));
    if (_imageDict[key]) {
        return _imageDict[key];
    }
    NSArray *images = [self imagesWithPath:@"jump"];
    NSMutableArray *imageArray = [NSMutableArray arrayWithCapacity:images.count];
    for (UIImage *image in [images reverseObjectEnumerator]) {
        [imageArray addObject:image];
    }
    return [UIImage animatedImageWithImages:imageArray duration:1.0/20*images.count];
}

- (UIImage *)editTimeEffectRepeatIcon {
    NSString *key = NSStringFromSelector(@selector(editTimeEffectRepeatIcon));
    if (_imageDict[key]) {
        return _imageDict[key];
    }
    NSMutableArray *imageArray = [[self imagesWithPath:@"jump"] mutableCopy];
    NSArray *toRepeat = [imageArray subarrayWithRange:NSMakeRange(5, 10)];
    [imageArray insertObjects:toRepeat atIndexes:[NSIndexSet indexSetWithIndexesInRange:NSMakeRange(10, 10)]];
    [imageArray insertObjects:toRepeat atIndexes:[NSIndexSet indexSetWithIndexesInRange:NSMakeRange(20, 10)]];
    return [UIImage animatedImageWithImages:imageArray duration:1.0/20 * imageArray.count];
}

- (UIImage *)editTimeEffectSlowMotionIcon {
    NSString *key = NSStringFromSelector(@selector(editTimeEffectSlowMotionIcon));
    if (_imageDict[key]) {
        return _imageDict[key];
    }
    return [self effectIconWithPath:@"jump" frameDuration: 1.0 / 10];
}

- (NSURL *)goodLuckVideoFileURL {
    return [_beautyPanelResourceBundle URLForResource:@"goodluck" withExtension:@"mp4"];
}

@end
