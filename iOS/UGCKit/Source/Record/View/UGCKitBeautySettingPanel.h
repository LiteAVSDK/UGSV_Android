// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "SDKHeader.h"
#import "UGCKitFilter.h"
#import "UGCKitTheme.h"

typedef NS_ENUM(NSUInteger, PanelMenuIndex) {
    PanelMenuIndexBeauty,
    PanelMenuIndexFilter,
    PanelMenuIndexMotion,
    PanelMenuIndexCosmetic,
    PanelMenuIndexGesture,
    PanelMenuIndexKoubei,
    PanelMenuIndexGreen,
};

/// 美颜设置相关回调可以直接转发至 TXBeautyManager
@protocol BeautySettingPanelDelegate <NSObject>
@optional
- (void)setBeautyStyle:(TXBeautyStyle)beautyStyle;
- (void)setBeautyLevel:(float)level;
- (void)setWhitenessLevel:(float)level;
- (void)setRuddyLevel:(float)level;
- (void)setEyeScaleLevel:(float)eyeScaleLevel;
- (void)setFaceSlimLevel:(float)faceScaleLevel;
- (void)setFaceVLevel:(float)faceVLevel;
- (void)setChinLevel:(float)chinLevel;
- (void)setFaceShortLevel:(float)faceShortlevel;
- (void)setNoseSlimLevel:(float)noseSlimLevel;
- (void)setEyeLightenLevel:(float)level;
- (void)setToothWhitenLevel:(float)level;
- (void)setWrinkleRemoveLevel:(float)level;
- (void)setPounchRemoveLevel:(float)level;
- (void)setSmileLinesRemoveLevel:(float)level;
- (void)setForeheadLevel:(float)level;
- (void)setEyeDistanceLevel:(float)level;
- (void)setEyeAngleLevel:(float)level;
- (void)setMouthShapeLevel:(float)level;
- (void)setNoseWingLevel:(float)level;
- (void)setNosePositionLevel:(float)level;
- (void)setLipsThicknessLevel:(float)level;
- (void)setFaceBeautyLevel:(float)level;
- (void)onSetFilter:(UIImage*)filterImage;
- (void)onSetFilterMixLevel:(float)level;
- (void)onSetGreenScreenFile:(NSURL *)file;
- (void)onSelectMotionTmpl:(NSString *)tmplName inDir:(NSString *)tmplDir;
@end

@protocol BeautyLoadPituDelegate <NSObject>
- (void)onLoadPituStart;
- (void)onLoadPituProgress:(CGFloat)progress;
- (void)onLoadPituFinished;
- (void)onLoadPituFailed;
@end

@interface UGCKitBeautySettingPanel : UIView
@property (nonatomic, assign) NSInteger currentFilterIndex;
@property (nonatomic, readonly) NSString* currentFilterName;
@property (nonatomic, assign, readonly) float beautyLevel;
@property (nonatomic, assign, readonly) float whiteLevel;
@property (nonatomic, assign, readonly) float ruddyLevel;
@property (nonatomic, assign, readonly) TXBeautyStyle beautyStyle;
@property (nonatomic, weak) id<BeautySettingPanelDelegate> delegate;
@property (nonatomic, weak) id<BeautyLoadPituDelegate> pituDelegate;
- (id)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme;

- (void)resetValues;
+ (NSUInteger)getHeight;
- (UIImage*)filterImageByIndex:(NSInteger)index;
- (float)filterMixLevelByIndex:(NSInteger)index;
@end
