// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UGCKitSlideButton : UIControl
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray<UIButton *>* buttons;
@property (assign, nonatomic) IBInspectable CGFloat spacing;
@property (assign, nonatomic) IBInspectable CGSize  size;

@property (assign, nonatomic) NSUInteger selectedIndex;
- (instancetype)initWithButtons:(NSArray<UIButton *> *)buttons buttonSize:(CGSize)size spacing:(CGFloat)spacing;
- (void)shrink;

@end

NS_ASSUME_NONNULL_END
