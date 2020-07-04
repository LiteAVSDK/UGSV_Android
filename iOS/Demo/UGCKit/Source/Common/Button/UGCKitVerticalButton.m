// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVerticalButton.h"

@implementation UGCKitVerticalButton
- (instancetype)initWithTitle:(NSString *)title
{
    if (self = [self init]) {
        _verticalSpacing = 7;
        [self setTitle:title forState:UIControlStateNormal];
        self.titleLabel.adjustsFontSizeToFitWidth = YES;
        self.titleLabel.minimumScaleFactor = 0.3;
        self.imageView.contentMode = UIViewContentModeScaleAspectFit;
    }
    return self;
}

- (void)awakeFromNib
{
    [super awakeFromNib];
    self.titleLabel.adjustsFontSizeToFitWidth = YES;
    self.titleLabel.minimumScaleFactor = 0.3;
}

- (CGSize)sizeThatFits:(CGSize)size
{
    NSDictionary *attr = @{NSFontAttributeName: self.titleLabel.font};
    CGSize textSize = [[self titleForState:self.state] sizeWithAttributes:attr];
    CGSize imageSize = [self imageForState:self.state].size;
    CGFloat width = MAX(textSize.width, imageSize.width);
    CGFloat height = textSize.height + imageSize.height + self.verticalSpacing;
    return CGSizeMake(truncf(width), truncf(height));
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    NSDictionary *attr = @{NSFontAttributeName: self.titleLabel.font};
    CGSize textSize = [[self titleForState:self.state] sizeWithAttributes:attr];
    CGFloat width = CGRectGetWidth(self.bounds);
    if (textSize.width > width) {
        textSize.width = width;
    }
    CGSize imageSize = [self imageForState:self.state].size;
    CGFloat totalHeight = textSize.height + imageSize.height + self.verticalSpacing;
    CGFloat centerX = CGRectGetMidX(self.bounds);
    self.imageView.center = CGPointMake(centerX, (CGRectGetHeight(self.bounds) - totalHeight) / 2 + imageSize.height / 2);
    CGRect imageFrame = self.imageView.frame;
    BOOL changed = NO;
    if (imageFrame.origin.x < 0) {
        imageFrame.size.width += 2*imageFrame.origin.x;
        imageFrame.origin.x = 0;
        changed = YES;
    }
    if (imageFrame.origin.y < 0) {
        imageFrame.size.height += 2*imageFrame.origin.y;
        imageFrame.origin.y = 0;
        changed = YES;
    }
    BOOL imageIsSmaller = imageSize.width <= CGRectGetWidth(imageFrame) && imageSize.height <= CGRectGetHeight(imageFrame); 
    self.imageView.contentMode = imageIsSmaller ? UIViewContentModeCenter : UIViewContentModeScaleAspectFit;
    if (changed) {
        self.imageView.frame = imageFrame;
    }
    
    self.titleLabel.frame = CGRectMake((CGRectGetWidth(self.bounds) - textSize.width)/2, CGRectGetHeight(self.bounds) - textSize.height, textSize.width, textSize.height);
}
                                                                                            
- (CGSize)intrinsicContentSize {
    // 在iOS8以下，intrinsicContentSize中直接调用控件，会造成循环调用
    if (([[[UIDevice currentDevice] systemVersion] floatValue] < 8.0)) {
        return [super intrinsicContentSize];
    }
    return [self sizeThatFits:CGSizeZero];
}

@end
