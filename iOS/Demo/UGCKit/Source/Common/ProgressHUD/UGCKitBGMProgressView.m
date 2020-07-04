// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitBGMProgressView.h"

@implementation UGCKitBGMProgressView
{
    UIImage *_bgImage;
    UIView  *_maskView;
    NSLayoutConstraint *_progressConstraint;
}

- (instancetype)initWithFrame:(CGRect)frame bgImage:(UIImage *)bgImage
{
    self = [super initWithFrame:frame];
    if (self) {
        _bgImage = bgImage;
        [self setup];
    }
    return self;
}

- (void)awakeFromNib
{
    [super awakeFromNib];
    [self setup];
}

- (void)setup {
    self.contentMode = UIViewContentModeRedraw;
    self.clipsToBounds = YES;
    _label = [[UILabel alloc] initWithFrame:self.bounds];
    _label.alpha = 0.5;
    self.opaque = YES;
    _label.font = [UIFont systemFontOfSize:15];
    _label.contentMode = UIViewContentModeCenter;
    _label.backgroundColor = [UIColor clearColor];
    _label.autoresizesSubviews = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    _label.translatesAutoresizingMaskIntoConstraints = NO;

    
    UIImageView *imageView = [[UIImageView alloc] initWithImage:_bgImage];
    imageView.frame = self.bounds;
    imageView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self addSubview:imageView];
    
    [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-0-[imageView]-0-|" options:NSLayoutFormatAlignAllLeft metrics:nil views:NSDictionaryOfVariableBindings(imageView)]];
    [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-0-[imageView]-0-|" options:NSLayoutFormatAlignAllTop metrics:nil views:NSDictionaryOfVariableBindings(imageView)]];
    
    _maskView = [[UIView alloc] initWithFrame:self.bounds];
    _maskView.backgroundColor = self.progressBackgroundColor;
    _maskView.translatesAutoresizingMaskIntoConstraints = NO;
    [self addSubview:_maskView];
    _progressConstraint = [NSLayoutConstraint constraintWithItem:self attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:_maskView attribute:NSLayoutAttributeLeft multiplier:1 constant:0];
    [self addConstraint:_progressConstraint];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:self attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_maskView attribute:NSLayoutAttributeRight multiplier:1 constant:0]];
    [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-0-[_maskView]-0-|" options:NSLayoutFormatAlignAllTop metrics:nil views:NSDictionaryOfVariableBindings(_maskView)]];
    
    [self addSubview:_label];
    [self addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-5-[_label]-5-|" options:NSLayoutFormatAlignAllLeft metrics:nil views:NSDictionaryOfVariableBindings(_label)]];

    [self addConstraint:[NSLayoutConstraint constraintWithItem:_label 
                                                     attribute:NSLayoutAttributeCenterY 
                                                     relatedBy:NSLayoutRelationEqual
                                                        toItem:self
                                                     attribute:NSLayoutAttributeCenterY
                                                    multiplier:1 
                                                      constant:0]];
}

- (void)setProgressBackgroundColor:(UIColor *)progressBackgroundColor
{
    _progressBackgroundColor = progressBackgroundColor;
    _maskView.backgroundColor = progressBackgroundColor;
}

- (void)setProgress:(float)progress
{
    if (_progress == progress) return;
    _progress = progress;
    _progressConstraint.constant = - progress * self.bounds.size.width;
    [self layoutIfNeeded];
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    self.layer.cornerRadius = self.bounds.size.height / 2;
}

@end
