// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitEffectSelectView.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitColorMacro.h"

#define EFFCT_COUNT        4
#define EFFCT_IMAGE_WIDTH  50 * kScaleY
#define EFFCT_IMAGE_SPACE  20

@implementation UGCKitEffectSelectView
{
    UIScrollView *_effectSelectView;
    NSMutableArray *_selectViewList;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        _effectSelectView = [[UIScrollView alloc] initWithFrame:CGRectMake(0,0, self.ugckit_width,EFFCT_IMAGE_WIDTH + 20)];
        [self addSubview:_effectSelectView];
        _selectViewList = [NSMutableArray array];
    }
    return self;
}

- (void)setSelectedIndex:(NSInteger)selectedIndex
{
    if (self.momentary) return;
    if (selectedIndex < _selectViewList.count) {
        _selectedIndex = selectedIndex;
        for (UIImageView *view in _selectViewList) {
            if (view.tag == selectedIndex) {
                view.hidden = NO;
            }else{
                view.hidden = YES;
            }
        }
    }
}

- (void)setEffectList:(NSArray<UGCKitEffectInfo *> *)effecList
{
    [self setEffectList:effecList momentary:NO];
}

- (void)setEffectList:(NSArray<UGCKitEffectInfo *> *)effecList momentary:(BOOL)momentary
{
    self.momentary = momentary;
    [_effectSelectView.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
    [_selectViewList removeAllObjects];
    CGFloat space = floorf(20 * kScaleX);
    CGFloat buttonSize = floorf(EFFCT_IMAGE_WIDTH);
    
    for (int i = 0 ; i < effecList.count ; i ++){
        UGCKitEffectInfo *info = effecList[i];
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        [btn setFrame:CGRectMake(space + (space + buttonSize) * i, 0, buttonSize, buttonSize)];
        if (info.animateIcons) {
            UIImageView* animatedImageView = [[UIImageView alloc] initWithFrame:btn.bounds];
            animatedImageView.animationImages = info.animateIcons;
            if (info.isSlow) {
                animatedImageView.animationDuration = 1.0 / 15 * effecList[i].animateIcons.count;
            }
            [animatedImageView startAnimating];
            [btn addSubview:animatedImageView];
        } else {
            [btn setImage:effecList[i].icon forState:UIControlStateNormal];
        }
        btn.layer.cornerRadius = EFFCT_IMAGE_WIDTH / 2.0;
        btn.layer.masksToBounds = YES;
        btn.titleLabel.numberOfLines = 0;
        btn.tag = i;
        [btn addTarget:self action:@selector(beginPress:) forControlEvents:UIControlEventTouchDown];
        [btn addTarget:self action:@selector(endPress:) forControlEvents:UIControlEventTouchUpInside | UIControlEventTouchUpOutside];
        [btn addTarget:self action:@selector(onTouchUpInside:) forControlEvents:UIControlEventTouchUpInside];
        
        UIImageView *selectView = [[UIImageView alloc]initWithFrame:btn.frame];
        [selectView setImage:effecList[i].selectIcon];
        selectView.hidden = YES;
        selectView.tag = i;
        [_selectViewList addObject:selectView];
        
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(btn.ugckit_x - space/2, btn.ugckit_bottom + 8, btn.ugckit_width+space, 12)];
        label.text = effecList[i].name;
        label.adjustsFontSizeToFitWidth = YES;
        label.minimumScaleFactor = 0.5;
        label.textColor = [UIColor whiteColor];
        label.textAlignment = NSTextAlignmentCenter;
        label.font = [UIFont systemFontOfSize:10];
        
        [_effectSelectView addSubview:btn];
        [_effectSelectView addSubview:selectView];
        [_effectSelectView addSubview:label];
        _effectSelectView.contentSize = CGSizeMake(btn.ugckit_right, buttonSize);
    }
    if (_effectSelectView.contentSize.width > self.ugckit_width) {
         _effectSelectView.alwaysBounceHorizontal = YES;
    }else{
        _effectSelectView.alwaysBounceHorizontal = NO;
    }
}

//开始按压
-(void) beginPress: (UIButton *) button {
    CGFloat offset = _effectSelectView.contentOffset.x;
    CGFloat diff = _effectSelectView.contentSize.width - _effectSelectView.bounds.size.width;
    if (offset < 0 ||  (diff > 0 && offset > diff)) {
        // 在回弹区域会触发button事件被cancel,导致收不到 TouchEnd 事件
        return;
    }
    if (self.delegate && [self.delegate respondsToSelector:@selector(onEffectBtnBeginSelect:)]) {
        [self.delegate onEffectBtnBeginSelect:button];
    }
    for (UIImageView *view in _selectViewList) {
        if (view.tag == button.tag) {
            view.hidden = NO;
        }else{
            view.hidden = YES;
        }
    }
}

//结束按压
-(void) endPress: (UIButton *) button {
    if (self.momentary) {
        [_selectViewList enumerateObjectsUsingBlock:^(UIImageView * obj, NSUInteger idx, BOOL * _Nonnull stop) {
            obj.hidden = YES;
        }];
    } else {
        _selectedIndex = button.tag;
    }
    if (self.delegate && [self.delegate respondsToSelector:@selector(onEffectBtnEndSelect:)]) {
        [self.delegate onEffectBtnEndSelect:button];
    }
}

//按压
-(void) onTouchUpInside:(UIButton *) button {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onEffectBtnSelected:)]) {
        [self.delegate onEffectBtnSelected:button];
    }
}
@end
