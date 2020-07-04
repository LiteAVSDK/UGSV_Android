// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitSlideButton.h"

@implementation UGCKitSlideButton
{
    CGSize  _buttonSize;
    CGSize  _instrinctSize;
    CGFloat _spacing;
    BOOL    _expanded;
}

- (instancetype)initWithButtons:(NSArray<UIButton *> *)buttons buttonSize:(CGSize)size spacing:(CGFloat)spacing
{
    self = [super initWithFrame:CGRectZero];
    if (self) {
        _buttons = buttons;
        [buttons enumerateObjectsUsingBlock:^(UIButton * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [obj addTarget:self action:@selector(onButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
            [self addSubview:obj];
        }];
        _buttonSize = size;
        _spacing = spacing;
        _instrinctSize = CGSizeMake((size.width + _spacing) * _buttons.count - _spacing, size.height);
    }
    return self;
}

- (void)shrink {
    if (!_expanded) {
        return;
    }
    _expanded = !_expanded;
    [self switchExpansionAnimated:YES];
}

- (void)awakeFromNib
{
    [super awakeFromNib];
    [_buttons enumerateObjectsUsingBlock:^(UIButton * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj addTarget:self action:@selector(onButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:obj];
    }];
    _instrinctSize = CGSizeMake((_size.width + _spacing) * _buttons.count - _spacing, _size.height);
}

- (void)setEnabled:(BOOL)enabled {
    [super setEnabled:enabled];
    [_buttons enumerateObjectsUsingBlock:^(UIButton * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        obj.enabled = enabled;
    }];
}

- (void)onButtonTouched:(id)sender {
    NSUInteger index = [self.buttons indexOfObject:sender];
    if (index == NSNotFound) {
        return;
    }
    _selectedIndex = index;

    if (_expanded) {
        [self sendActionsForControlEvents:UIControlEventValueChanged];
    }
    _expanded = !_expanded;
    [self switchExpansionAnimated:YES];
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    [self switchExpansionAnimated:NO];
}

- (CGSize)sizeThatFits:(CGSize)size
{
    return _instrinctSize;
}

- (CGSize)intrinsicContentSize {
    return _instrinctSize;
}

- (void)switchExpansionAnimated:(BOOL)animated;
{
    CGSize buttonSize = _buttonSize;
    NSArray<UIButton *> *buttons = _buttons;
    BOOL expanded = _expanded;
    NSUInteger selectedIndex = _selectedIndex;
    CGFloat spacing = _spacing;
    void (^action)(void) = ^{
        __block CGRect frame = (CGRect){CGPointZero, buttonSize};
        [buttons enumerateObjectsUsingBlock:^(UIButton * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            if (idx != selectedIndex) {
                obj.frame = frame;
                frame.origin.x += (buttonSize.width + spacing);
                buttons[idx].hidden = !expanded;
            } else {
                buttons[idx].hidden = NO;
            }
        }];
        buttons[selectedIndex].frame = CGRectMake(CGRectGetWidth(self.bounds) - buttonSize.width, 0, buttonSize.width, buttonSize.height);
    };
    if (animated) {
        [UIView animateWithDuration:0.2 animations:action];
    } else {
        action();
    }
}

- (void)setSelectedIndex:(NSUInteger)selectedIndex
{
    if (_selectedIndex == selectedIndex) return;
    _selectedIndex = selectedIndex;
    [self switchExpansionAnimated:NO];
}

@end
