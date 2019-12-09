// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitSlideOptionControl.h"

@implementation UGCKitSlideOptionControl
{
    NSArray<NSString *> *_options;
    NSArray<UILabel *> *_labels;
    UIImageView *_indicatorView;
}

- (instancetype)initWithFrame:(CGRect)frame theme:(UGCKitTheme*)theme options:(NSArray<NSString *> *)options {
    if (self = [super initWithFrame:frame]) {
        _options = [options copy];
        NSMutableArray *labels = [NSMutableArray arrayWithCapacity:options.count];
        for (NSString *str in _options) {
            UILabel *label = [[UILabel alloc] init];
            label.textColor = [theme titleColor];
            label.text = str;
            [labels addObject:label];
            [label sizeToFit];
            [self addSubview:label];
        }
        _labels = labels;
        _indicatorView = [[UIImageView alloc] initWithImage:theme.recordButtonModeSwitchIndicatorIcon];
        [self addSubview:_indicatorView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [self doLayoutSubviews];
}

- (void)setDisabledIndexes:(NSIndexSet *)disabledIndexes {
    _disabledIndexes = [disabledIndexes copy];
    [disabledIndexes enumerateIndexesUsingBlock:^(NSUInteger idx, BOOL * _Nonnull stop) {
        if (idx < self->_labels.count) {
            self->_labels[idx].hidden = YES;
        }
    }];
}

- (void)doLayoutSubviews {
    if (_selectedIndex >= _labels.count) {
        return;
    }
    const CGFloat centerY = (CGRectGetHeight(self.bounds) - 10 - CGRectGetHeight(_indicatorView.bounds))/2;
    const CGFloat centerX = CGRectGetMidX(self.bounds);
    UILabel *centerLabel = _labels[_selectedIndex];
    centerLabel.center = CGPointMake(centerX, centerY);
    CGFloat x = centerX - CGRectGetMidX(centerLabel.bounds) - 30;
    for (NSInteger i = _selectedIndex-1; i >= 0; -- i) {
        UILabel *label = _labels[i];
        x -= CGRectGetMidX(label.bounds);
        label.center = CGPointMake(x, centerY);
        x -= (CGRectGetMidX(label.bounds) + 30);
    }
    x = centerX + CGRectGetMidX(centerLabel.bounds) + 30;
    for (NSInteger i = _selectedIndex+1; i < _labels.count; ++ i) {
        UILabel *label = _labels[i];
        x += CGRectGetMidX(label.bounds);
        label.center = CGPointMake(x, centerY);
        x += (CGRectGetMidX(label.bounds) + 30);
    }
    _indicatorView.center = CGPointMake(centerX, CGRectGetHeight(self.bounds) - CGRectGetMidY(_indicatorView.bounds) - 10);
}

- (void)setCurrentIndex:(NSUInteger)index {
    [self setCurrentIndex:index animated:NO];
}

- (void)setCurrentIndex:(NSUInteger)index animated:(BOOL)animated {
    _selectedIndex = index;
    if (animated) {
        [UIView animateWithDuration:0.2 animations:^{
            [self doLayoutSubviews];
        }];
    } else {
        [self setNeedsLayout];
    }
}

- (void)endTrackingWithTouch:(nullable UITouch *)touch withEvent:(nullable UIEvent *)event
{
    CGPoint location = [touch locationInView:self];
    NSUInteger index = 0;
    for (UILabel *label in _labels) {
        CGRect frame = label.frame;
        if (location.x >= CGRectGetMinX(frame) && location.x <= CGRectGetMaxX(frame)) {
            if (![_disabledIndexes containsIndex:index]) {
                [self setCurrentIndex:index animated:YES];
                [self sendActionsForControlEvents:UIControlEventValueChanged];
            }
            break;
        }
        ++index;
    }
}

@end
