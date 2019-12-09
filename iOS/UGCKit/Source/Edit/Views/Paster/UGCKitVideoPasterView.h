// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"
@class UGCKitVideoPasterView;


@protocol UGCKitVideoPasterViewDelegate <NSObject>
- (void)onPasterViewTap;
- (void)onRemovePasterView:(UGCKitVideoPasterView*)pasterView;
@end

@interface UGCKitVideoPasterView : UIView
@property (nonatomic, weak) id<UGCKitVideoPasterViewDelegate> delegate;
@property (nonatomic, strong)    UIImageView *pasterImageView;
@property (nonatomic, assign)    CGFloat   rotateAngle;
@property (nonatomic, assign)    UIImage*  staticImage;
- (id)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme;
- (void)setImageList:(NSArray *)imageList imageDuration:(float)duration;
- (CGRect)pasterFrameOnView:(UIView*)view;
@end

