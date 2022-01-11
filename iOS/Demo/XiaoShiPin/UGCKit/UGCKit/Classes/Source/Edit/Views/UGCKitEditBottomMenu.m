// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitEditBottomMenu.h"
#import "UGCKitColorMacro.h"
#import "UGCKitVerticalButton.h"

@implementation UGCKitEditBottomMenuItem
+ (instancetype)menuItemWithTitle:(NSString *)title icon:(UIImage *)icon highlightedIcon:(nullable UIImage *)highlightedIcon action:(void (^)(void))action
{
    UGCKitEditBottomMenuItem *item = [[UGCKitEditBottomMenuItem alloc] init];
    item.title = title;
    item.icon  = icon;
    item.highlightedIcon = highlightedIcon;
    item.action = action;
    return item;
}

@end

@implementation UGCKitEditBottomMenu
{
    NSMutableArray<UGCKitEditBottomMenuItem *> *_items;
    UIView*         _contentView;
    NSArray<UIButton *> *_buttons;
    BOOL _needsSetupMenuItemView;
    BOOL _isHidden;
}

- (instancetype)initWithFrame:(CGRect)frame items:(NSArray<UGCKitEditBottomMenuItem *>*)items
{
    if (self = [self initWithFrame:frame]) {
        _items = [NSMutableArray arrayWithArray:items];
        _textColor = [UIColor whiteColor];
        [self _setupMenuView];
    }
    return self;
}

- (UGCKitEditBottomMenuItem *)addItemWithTitle:(NSString *)title
                                      icon:(UIImage *)icon
                           highlightedIcon:(nullable UIImage *)highlightedIcon
                                    action:(void (^)(void))action
{
    UGCKitEditBottomMenuItem *item = [UGCKitEditBottomMenuItem menuItemWithTitle:title icon:icon highlightedIcon:highlightedIcon action:action];
    [_items addObject:item];
    _needsSetupMenuItemView = YES;
    [self setNeedsLayout];
    return item;
}

- (void)_setupMenuView
{
    NSInteger index = 0;
    [_buttons makeObjectsPerformSelector:@selector(removeFromSuperview)];
    NSMutableArray *buttons = [[NSMutableArray alloc] initWithCapacity:_items.count];
    for (UGCKitEditBottomMenuItem *item in _items) {
        UGCKitVerticalButton *button = [[UGCKitVerticalButton alloc] initWithTitle:item.title];
        button.verticalSpacing = 1;
        [button setImage:item.icon forState:UIControlStateNormal];
        [button setTitle:item.title forState:UIControlStateNormal];
        button.tag = index;
        [button addTarget:self action:@selector(onTapMenuItem:) forControlEvents:UIControlEventTouchUpInside];
        [button setTitleColor:_textColor forState:UIControlStateNormal];
        button.titleLabel.textAlignment = NSTextAlignmentCenter;
        button.titleLabel.font = [UIFont systemFontOfSize:13];
        [buttons addObject:button];
        [_contentView addSubview:button];
        ++index;
    }
    _buttons = buttons;
    _needsSetupMenuItemView = NO;
}

- (void)onTapMenuItem:(UIButton *)sender
{
    NSInteger index = sender.tag;
    if (index < _items.count) {
        UGCKitEditBottomMenuItem *item = _items[index];
        if (item.action) {
            item.action();
        }
    }
}

- (id)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame]) {
        _contentView = [[UIView alloc] initWithFrame:self.bounds];
        _contentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self addSubview:_contentView];
    }
    
    return self;
}


- (void)layoutSubviews
{
    [super layoutSubviews];
    if (_needsSetupMenuItemView) {
        [self _setupMenuView];
    }
    CGFloat buttonWidth= CGRectGetWidth(self.bounds) / _buttons.count;
    CGFloat height = CGRectGetHeight(self.bounds);
    [_buttons enumerateObjectsUsingBlock:^(UIButton * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        obj.frame = CGRectMake(buttonWidth * idx, 0, buttonWidth, height);
    }];
}

- (void)setHidden:(BOOL)hidden
{
    if (_isHidden == hidden) return;
    _isHidden = hidden;
    CGFloat height = self.frame.size.height;
    UIView *contentView = _contentView;
    CGFloat bottom = CGRectGetMaxY(_contentView.frame);
    if (hidden) {
        if (bottom > height) return;
        [UIView animateWithDuration:0.1 animations:^{
            contentView.frame = CGRectOffset(contentView.frame, 0, height);
            contentView.alpha = 0.0;
        }];
    }else{
        if (bottom <= height) return;
        [UIView animateWithDuration:0.5 animations:^{
            contentView.frame = CGRectOffset(contentView.frame, 0, -height);
            contentView.alpha = 1.0;
        }];
    }
}

- (BOOL)isHidden {
    return _isHidden;
}

@end
