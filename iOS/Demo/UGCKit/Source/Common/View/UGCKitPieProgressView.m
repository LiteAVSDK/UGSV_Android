//  Copyright © 2019 Tencent. All rights reserved.

#import "UGCKitPieProgressView.h"

@implementation UGCKitPieProgressView
- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self commonInit];
    }
    return self;
}
- (instancetype)initWithCoder:(NSCoder *)coder
{
    self = [super initWithCoder:coder];
    if (self) {
        [self commonInit];
    }
    return self;
}
- (void)commonInit {
    self.contentMode = UIViewContentModeRedraw;
    self.tintColor = [UIColor cyanColor];
}
- (void)setProgress:(float)progress
{
    if (_progress != progress) {
        _progress = progress;
        [self setNeedsDisplay];
    }
}
- (void)setTintColor:(UIColor *)tintColor {
    if (_tintColor != tintColor) {
        _tintColor = tintColor;
        [self setNeedsDisplay];
    }
}

- (void)drawRect:(CGRect)rect {
    CGContextRef context = UIGraphicsGetCurrentContext();
    [self.tintColor set];
    // Draw background
    CGFloat lineWidth = 2.f;
    CGRect allRect = self.bounds;
    CGRect circleRect = CGRectInset(allRect, lineWidth/2.f, lineWidth/2.f);
    CGPoint center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));
    CGContextSetLineWidth(context, lineWidth);
    CGContextStrokeEllipseInRect(context, circleRect);
    // 90 degrees
    CGFloat startAngle = - ((float)M_PI / 2.f);
    // Draw progress
    UIBezierPath *processPath = [UIBezierPath bezierPath];
    processPath.lineCapStyle = kCGLineCapButt;
    processPath.lineWidth = lineWidth * 2.f;
    CGFloat radius = (CGRectGetWidth(self.bounds) / 2.f) - (processPath.lineWidth / 2.f) - 3.f;
    CGFloat endAngle = (self.progress * 2.f * (float)M_PI) + startAngle;
    [processPath moveToPoint:center];
    [processPath moveToPoint:CGPointMake(center.x, radius)];
    [processPath addArcWithCenter:center radius:radius startAngle:startAngle endAngle:endAngle clockwise:YES];
    [processPath closePath];
    // Ensure that we don't get color overlaping when _progressTintColor alpha < 1.f.
    CGContextSetBlendMode(context, kCGBlendModeCopy);
    [processPath fill];
}

@end
