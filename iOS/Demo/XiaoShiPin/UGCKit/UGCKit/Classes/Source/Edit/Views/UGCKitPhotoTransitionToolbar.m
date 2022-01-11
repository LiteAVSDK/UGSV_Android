// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitPhotoTransitionToolbar.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitColorMacro.h"
#import "UGCKitVerticalButton.h"

#define TRANSITIN_IMAGE_WIDTH  50 * kScaleY
#define TRANSITIN_IMAGE_SPACE  10

@implementation UGCKitPhotoTransitionToolbar
{
    UIScrollView *_transitionView;
}

- (instancetype)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme
{
    self = [super initWithFrame:frame];
    if (self) {
        _theme = theme;
        NSArray *transitionNames = @[[_theme localizedString:@"UGCKit.PhotoTransition.Horizontal"],
                                     [_theme localizedString:@"UGCKit.PhotoTransition.Vertical"],
                                     [_theme localizedString:@"UGCKit.PhotoTransition.ZoomIn"],
                                     [_theme localizedString:@"UGCKit.PhotoTransition.ZoomOut"],
                                     [_theme localizedString:@"UGCKit.PhotoTransition.Rotation"],
                                     [_theme localizedString:@"UGCKit.PhotoTransition.FadeInFadeOut"]];
        NSArray<UIImage *> *images = @[theme.transitionLeftRightIcon,
                                theme.transitionUpDownIcon,
                                theme.transitionZoomInIcon,
                                theme.transitionZoomOutIcon,
                                theme.transitionRotateIcon,
                                theme.transitionFadeInOutIcon
        ];

        NSAssert(transitionNames.count == images.count, @"Count mismatch, please check");
        
        _transitionView = [[UIScrollView alloc] initWithFrame:CGRectMake(0,0, self.ugckit_width,TRANSITIN_IMAGE_WIDTH)];
        _transitionView.showsVerticalScrollIndicator = NO;
        _transitionView.showsHorizontalScrollIndicator = NO;
    
        CGFloat itemWidth = floor(frame.size.width / images.count);
        CGFloat halfSpace = 2;
        
        for (int i = 0 ; i < transitionNames.count ; i ++){
            UIButton *btn = [[UGCKitVerticalButton alloc] initWithTitle:transitionNames[i]];
            btn.titleLabel.font = [UIFont systemFontOfSize:14];
            btn.titleLabel.adjustsFontSizeToFitWidth = YES;
            [btn setImage:images[i] forState:UIControlStateNormal];
            [btn setTitleColor:theme.titleColor forState:UIControlStateSelected];
            btn.tag = i;
            
            [btn setFrame:CGRectMake(itemWidth * i + halfSpace, 0, itemWidth - halfSpace * 2, TRANSITIN_IMAGE_WIDTH)];
            
            [btn addTarget:self action:@selector(onBtnClick:) forControlEvents:UIControlEventTouchUpInside];
            [_transitionView addSubview:btn];
            
            if (i == 0) {
                [self resetBtnColor:btn];
            }
        }
        [self addSubview:_transitionView];
    }
    return self;
}

- (void)onBtnClick:(UIButton *)btn
{
    if (btn.tag == 0) {
        if (_delegate && [_delegate respondsToSelector:@selector(onVideoTransitionUpDownSlipping)]) {
            [_delegate onVideoTransitionLefRightSlipping];
        }
    }
    else if (btn.tag == 1) {
        if (_delegate && [_delegate respondsToSelector:@selector(onVideoTransitionUpDownSlipping)]) {
            [_delegate onVideoTransitionUpDownSlipping];
        }
    }
    else if (btn.tag == 2){
        if (_delegate && [_delegate respondsToSelector:@selector(onVideoTransitionEnlarge)]) {
            [_delegate onVideoTransitionEnlarge];
        }
    }
    else if (btn.tag == 3){
        if (_delegate && [_delegate respondsToSelector:@selector(onVideoTransitionNarrow)]) {
            [_delegate onVideoTransitionNarrow];
        }
    }
    else if (btn.tag == 4){
        if (_delegate && [_delegate respondsToSelector:@selector(onVideoTransitionNarrow)]) {
            [_delegate onVideoTransitionRotationalScaling];
        }
    }
    else if (btn.tag == 5){
        if (_delegate && [_delegate respondsToSelector:@selector(onVideoTransitionNarrow)]) {
            [_delegate onVideoTransitionFadeinFadeout];
        }
    }
    [self resetBtnColor:btn];
}

- (void)resetBtnColor:(UIButton *)btn
{
    for (UIButton * btn in _transitionView.subviews) {
        btn.selected = NO;
    }
    btn.selected = YES;
}
@end
