// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitVideoRangeConst.h"
#import "UGCKitRangeContent.h"
#import <UIKit/UIGestureRecognizerSubclass.h>
#import <objc/runtime.h>
#import "UGCKitTheme.h"

@interface TCBGMCutViewConfig : NSObject
@property (nonatomic, assign) CGRect frame;
@property (nonatomic) NSInteger pinWidth;
@property (nonatomic) NSInteger thumbHeight;
@property (nonatomic) NSInteger borderHeight;
@property (nonatomic) UIImage*  leftPinImage;
@property (nonatomic) UIImage*  centerPinImage;
@property (nonatomic) UIImage*  rightPigImage;
@property (nonatomic) UIImage*  leftCorverImage;
@property (nonatomic) UIImage*  rightCoverImage;
- (id)initWithTheme:(UGCKitTheme*)theme;
@end

@protocol BGMCutDelegate;

@interface UGCKitBGMCutView : UIView

@property (nonatomic, weak) id<BGMCutDelegate> delegate;

@property (nonatomic) CGFloat   leftPinCenterX;     //左拉条位置
@property (nonatomic) CGFloat   rightPinCenterX;    //右拉条位置

@property (nonatomic) UIImageView   *leftPin;       //左拉条
@property (nonatomic) UIImageView   *rightPin;      //右拉条
@property (nonatomic) UIView        *topBorder;     //上边
@property (nonatomic) UIView        *bottomBorder;  //下边
@property (nonatomic) UIImageView   *leftCover;     //左拉覆盖
@property (nonatomic) UIImageView   *rightCover;    //右拉覆盖

@property (nonatomic, copy) NSArray<UIImageView *>       *imageViewList;
@property (nonatomic, copy) NSArray       *imageList;   //显示图列表

@property (nonatomic, readonly) CGFloat pinWidth;    //拉条大小
@property (nonatomic, readonly) CGFloat imageWidth;
@property (nonatomic, readonly) CGFloat imageListWidth;

@property (nonatomic, readonly) CGFloat leftScale;  //左拉条的位置比例
@property (nonatomic, readonly) CGFloat rightScale; //右拉条的位置比例

- (instancetype)initWithImageList:(NSArray *)images;
- (instancetype)initWithImageList:(NSArray *)images config:(TCBGMCutViewConfig*)config;

-(void) resetCutView;

@end


@protocol BGMCutDelegate <NSObject>

@optional
- (void)onRangeLeftChangeBegin:(id)sender;
- (void)onRangeLeftChanged:(id)sender percent:(CGFloat)percent;
- (void)onRangeLeftChangeEnded:(id)sender percent:(CGFloat)percent;
- (void)onRangeRightChangeBegin:(id)sender;
- (void)onRangeRightChanged:(id)sender;
- (void)onRangeRightChangeEnded:(id)sender;
- (void)onRangeLeftAndRightChanged:(id)sender;
@end

@interface TCPanGestureRecognizer : UIPanGestureRecognizer
-(instancetype)initWithTarget:(id)target action:(SEL)action inview:(UIView*)view;
@property CGPoint beginPoint;
@property UIView* inView;
@end

