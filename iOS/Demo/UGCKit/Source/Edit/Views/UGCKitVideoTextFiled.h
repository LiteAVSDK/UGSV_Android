// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"

@class UGCKitVideoTextFiled;

/**
 字幕输入view，进行文字输入，拖动，放大，旋转等
 */

@interface VideoTextBubble
@property(nonatomic , strong) UIImage *image;
@property(nonatomic , assign) CGRect  textNormalizationFrame;
@end

@protocol VideoTextFieldDelegate <NSObject>
- (void)onBubbleTap;
- (void)onTextInputBegin;
- (void)onTextInputDone:(NSString*)text;
- (void)onRemoveTextField:(UGCKitVideoTextFiled*)textField;
@end

@interface UGCKitVideoTextFiled : UIView

@property (nonatomic, weak) id<VideoTextFieldDelegate> delegate;
@property (nonatomic, copy, readonly) NSString* text;
@property (nonatomic, readonly) UIImage* textImage;             //生成字幕image

- (instancetype)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme;
- (void)setTextBubbleImage:(UIImage *)image textNormalizationFrame:(CGRect)frame;

- (CGRect)textFrameOnView:(UIView*)view;

//关闭键盘
- (void)resignFirstResponser;

@end

