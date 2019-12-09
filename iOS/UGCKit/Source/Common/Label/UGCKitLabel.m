// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitLabel.h"
#import <objc/runtime.h>

@implementation UGCKitLabel

- (id)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if (self) {
        self.edgeInsets = UIEdgeInsetsMake(0, 0, 0, 0);
    }
    return self;
}

- (void)drawTextInRect:(CGRect)rect {
    [super drawTextInRect:UIEdgeInsetsInsetRect(rect, self.edgeInsets)];
}

- (CGSize)intrinsicContentSize
{
    CGSize size = [super intrinsicContentSize];
    size.width  += self.edgeInsets.left + self.edgeInsets.right;
    size.height += self.edgeInsets.top + self.edgeInsets.bottom;
    return size;
}

- (CGSize)sizeThatFits:(CGSize)size {
    CGSize retSize = [super sizeThatFits:size];
    retSize.width += self.edgeInsets.left + self.edgeInsets.right;
    retSize.height += self.edgeInsets.top + self.edgeInsets.bottom;
    return CGSizeMake(ceilf(retSize.width), ceilf(retSize.height));
}

@end
