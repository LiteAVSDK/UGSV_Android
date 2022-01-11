// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitSmallButton.h"

@implementation UGCKitSmallButton

- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent *)event
{
    if (!self.enabled || self.hidden) return [super pointInside:point withEvent:event];
    CGRect area = self.frame;
    area.origin = CGPointZero;
    if (CGRectGetWidth(area) < 44) {
        area.origin.x = CGRectGetMidX(area) - 22;
        area.size.width = 44;
    }
    if (CGRectGetHeight(area) < 44) {
        area.origin.y = CGRectGetMidY(area) - 22;
        area.size.height = 44;
    }
    
    return CGRectContainsPoint(area, point);
}

@end
