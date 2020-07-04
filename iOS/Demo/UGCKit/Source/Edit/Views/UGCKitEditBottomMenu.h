// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
/**
 视频编辑底栏
 */
@interface UGCKitEditBottomMenuItem : NSObject
@property (strong, nonatomic) NSString *title;
@property (strong, nonatomic) UIImage *icon;
@property (strong, nonatomic, nullable) UIImage *highlightedIcon;
@property (copy, nonatomic) void(^action)(void);
+ (instancetype)menuItemWithTitle:(NSString *)title
                             icon:(UIImage *)icon
                  highlightedIcon:(nullable UIImage *)highlightedIcon
                           action:(void(^)(void))action;

@end

@interface UGCKitEditBottomMenu : UIView
@property (strong, nonatomic) UIColor *textColor;

- (instancetype)initWithFrame:(CGRect)frame items:(nullable NSArray<UGCKitEditBottomMenuItem *>*)items;
- (UGCKitEditBottomMenuItem *)addItemWithTitle:(NSString *)title
                                      icon:(UIImage *)icon
                           highlightedIcon:(nullable UIImage *)highlightedIcon
                                    action:(void(^)(void))action;


@end

NS_ASSUME_NONNULL_END
