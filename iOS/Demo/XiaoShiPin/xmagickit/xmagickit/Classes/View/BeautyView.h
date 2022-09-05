//
//  BeautyView.h
//  PituMotionDemo
//
//  Created by xingyunmao on 2021/2/3.
//  Copyright © 2021 Pitu. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <SceneKit/SceneKitTypes.h>

NS_ASSUME_NONNULL_BEGIN
typedef void (^OnItemSelectedBlock)();
typedef void (^ChangedCurrentBeautyBlock)();

/*
 * BeautyView：美颜面版
 *
 */
@interface BeautyView : UIView

@property (nonatomic, weak) UIViewController *viewController;  //父viewController
@property (nonatomic, copy) OnItemSelectedBlock itemSelectedBlock;  //美颜选择回调
@property (nonatomic, copy) ChangedCurrentBeautyBlock changedCurrentBeautyBlock;  //美颜变化回调
//UICollection offset
@property CGPoint beau_offset;  //美颜beau_offset
@property CGPoint lut_offset;  //滤镜lut_offset
@property CGPoint motion_offset;  //动效motion_offset
@property CGPoint meiz_offset;  //美妆meiz_offset
@property CGPoint seg_offset;  //分割seg_offset
@property CGPoint menu2d_offset;  //2Dmenu2d_offset
@property CGPoint menu3d_offset;  //3Dmenu3d_offset
@property CGPoint menuhand_offset;  //手势menuhand_offset
@property BOOL isBackTo;  //isBackTo

- (void)setXMagic:(id)xmagic;
- (void)enableBeautyCell:(BOOL)isEnable;
- (void)updateAllBeautyValue;
- (void)hide:(int)type;
@end

NS_ASSUME_NONNULL_END
