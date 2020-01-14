// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitVideoRangeConst.h"
#import "UGCKitTheme.h"

/*用来辅助定制外观*/
@interface UGCKitRangeContentConfig : NSObject
@property (nonatomic) NSInteger imageCount;
@property (nonatomic) NSInteger pinWidth;
@property (nonatomic) NSInteger thumbHeight;
@property (nonatomic) NSInteger borderHeight;
@property (nonatomic) UIImage*  leftPinImage;
@property (nonatomic) UIImage*  centerPinImage;
@property (nonatomic) UIImage*  indicatorImage;
@property (nonatomic) UIImage*  rightPigImage;
@property (nonatomic) UIImage*  leftCorverImage;
@property (nonatomic) UIImage*  rightCoverImage;
@property (nonatomic) UIColor*  borderColor;
- (instancetype)initWithTheme:(UGCKitTheme *)theme;
@end




@protocol RangeContentDelegate;

@interface UGCKitRangeContent : UIView

@property (nonatomic, weak) id<RangeContentDelegate> delegate;

@property (nonatomic) CGFloat   leftPinCenterX;     //左拉条位置
@property (nonatomic) CGFloat   centerPinCenterX;   //中间滑块位置
@property (nonatomic) CGFloat   rightPinCenterX;    //右拉条位置

@property (nonatomic) UIImageView   *leftPin;       //左拉条
@property (nonatomic) UIImageView   *centerPin;     //中滑块
@property (nonatomic) UIImageView   *rightPin;      //右拉条
@property (nonatomic) UIView        *topBorder;     //上边
@property (nonatomic) UIView        *bottomBorder;  //下边
@property (nonatomic) UIImageView   *middleLine;    //中线
@property (nonatomic) UIImageView   *centerCover;
@property (nonatomic) UIImageView   *leftCover;     //左拉覆盖
@property (nonatomic) UIImageView   *rightCover;    //右拉覆盖

@property (nonatomic, copy) NSArray<UIImageView *>       *imageViewList;
@property (nonatomic, copy) NSArray       *imageList;   //显示图列表

@property (nonatomic, readonly) CGFloat pinWidth;    //拉条大小
@property (nonatomic, readonly) CGFloat imageWidth;
@property (nonatomic, readonly) CGFloat imageListWidth;

@property (nonatomic, readonly) CGFloat leftScale;  //左拉条的位置比例
@property (nonatomic, readonly) CGFloat rightScale; //右拉条的位置比例
@property (nonatomic, readonly) CGFloat centerScale; //中间拉条的位置比例

- (instancetype)initWithImageList:(NSArray *)images;
- (instancetype)initWithImageList:(NSArray *)images config:(UGCKitRangeContentConfig*)config;
- (void)unpdateBorder;
@end


@protocol RangeContentDelegate <NSObject>

@optional
- (void)onRangeTap:(CGPoint )point;
- (void)onRangeLeftChangeBegin:(UGCKitRangeContent*)sender;
- (void)onRangeLeftChanged:(UGCKitRangeContent *)sender;
- (void)onRangeLeftChangeEnded:(UGCKitRangeContent *)sender;
- (void)onRangeCenterChangeBegin:(UGCKitRangeContent*)sender;
- (void)onRangeCenterChanged:(UGCKitRangeContent *)sender;
- (void)onRangeCenterChangeEnded:(UGCKitRangeContent *)sender;
- (void)onRangeRightChangeBegin:(UGCKitRangeContent*)sender;
- (void)onRangeRightChanged:(UGCKitRangeContent *)sender;
- (void)onRangeRightChangeEnded:(UGCKitRangeContent *)sender;
- (void)onRangeLeftAndRightChanged:(UGCKitRangeContent *)sender;
@end
