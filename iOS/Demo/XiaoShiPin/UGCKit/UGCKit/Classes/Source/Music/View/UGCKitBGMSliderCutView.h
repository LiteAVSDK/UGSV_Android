// Copyright (c) 2019 Tencent. All rights reserved.


#import <UIKit/UIKit.h>
#import <UIKit/UIGestureRecognizerSubclass.h>
#import <objc/runtime.h>
#import "UGCKitBGMCutView.h"

@interface UGCKitBGMSliderCutViewConfig : NSObject
@property (nonatomic, assign) CGRect frame;
@property (nonatomic) NSInteger pinWidth;
@property (assign) CGFloat duration;
@property (assign) CGFloat durationUnit;
@property (assign) CGFloat labelDurationInternal;
@property (nonatomic) NSInteger thumbHeight;
@property (nonatomic) NSInteger borderHeight;
@property (nonatomic) UIImage*  leftPinImage;
@property (nonatomic) UIImage*  rightPigImage;
@property (nonatomic) UIImage*  leftCorverImage;
@property (nonatomic) UIImage*  rightCoverImage;
@property (nonatomic) UIColor*  borderColor;
- (id)initWithTheme:(UGCKitTheme *)theme;
@end

@interface UGCKitBGMSliderCutView : UIView

+(NSString*) timeString:(CGFloat) time;

@property (nonatomic, weak) id<BGMCutDelegate> delegate;

@property (nonatomic) CGFloat   leftPinCenterX;     //左拉条位置
@property (nonatomic) CGFloat   rightPinCenterX;    //右拉条位置

@property (nonatomic) UIImageView   *leftPin;       //左拉条
@property (nonatomic) UIImageView   *rightPin;      //右拉条
@property (nonatomic) UIView        *topBorder;     //上边
@property (nonatomic) UIView        *bottomBorder;  //下边
@property (nonatomic) UIImageView   *leftCover;     //左拉覆盖
@property (nonatomic) UIImageView   *rightCover;    //右拉覆盖
@property (nonatomic) UIImageView  *imageView;
@property (nonatomic) UIScrollView  *bgScrollView;

@property (nonatomic, copy) NSArray<UIImageView *>       *imageViewList;
@property (nonatomic, copy) UIImage       *image;   //显示图列表

@property (nonatomic, readonly) CGFloat pinWidth;    //拉条大小
@property (nonatomic, readonly) CGFloat imageWidth;
@property (nonatomic, readonly) CGFloat imageListWidth;

@property (nonatomic, readonly) CGFloat leftScale;  //左拉条的位置比例
@property (nonatomic, readonly) CGFloat rightScale; //右拉条的位置比例

- (instancetype)initWithImage:(UIImage *)image config:(UGCKitBGMSliderCutViewConfig*)config;

-(void) resetCutView;

@end


