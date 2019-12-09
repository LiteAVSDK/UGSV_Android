// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>

@interface  UGCKitMenuItemCell : UICollectionViewCell
@property (nonatomic, strong) UILabel *label;
- (void)setSelected:(BOOL)selected;
+ (NSString *)reuseIdentifier;
@end
