// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVideoRangeSlider.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitVideoRangeConst.h"

@implementation UGCKitVideoColorInfo

@end

@interface UGCKitVideoRangeSlider()<RangeContentDelegate, UIScrollViewDelegate>

@property BOOL disableSeek;

@end

@implementation UGCKitVideoRangeSlider
{
    NSMutableArray <UGCKitVideoColorInfo *> *_colorInfos;
    UGCKitRangeColorType       _colorType;
    UGCKitVideoColorInfo *_selectColorInfo;
    BOOL          _startColor;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/


- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    
    self.bgScrollView = ({
        UIScrollView *scroll = [[UIScrollView alloc] initWithFrame:CGRectZero];
        [self addSubview:scroll];
        scroll.showsVerticalScrollIndicator = NO;
        scroll.showsHorizontalScrollIndicator = NO;
        scroll.scrollsToTop = NO;
        scroll.autoresizingMask = UIViewAutoresizingFlexibleWidth;
        scroll.delegate = self;
        scroll;
    });
    self.middleLine = ({
        UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"mline.png"]];
        [self addSubview:imageView];
        imageView;
    });
    
    _colorInfos = [NSMutableArray array];
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.bgScrollView.ugckit_width = self.ugckit_width;
    self.middleLine.center = self.bgScrollView.center = CGPointMake(self.ugckit_width/2, self.ugckit_height/2);
    self.middleLine.bounds = CGRectMake(0, 0, 0, self.ugckit_height + 4);
}


- (void)setAppearanceConfig:(UGCKitRangeContentConfig *)appearanceConfig
{
    _appearanceConfig = appearanceConfig;
    self.middleLine.image = appearanceConfig.indicatorImage;
}

- (void)setImageList:(NSArray *)images
{
    if (self.rangeContent) {
        [self.rangeContent removeFromSuperview];
    }
    if (_appearanceConfig) {
        self.rangeContent = [[UGCKitRangeContent alloc] initWithImageList:images config:_appearanceConfig];
    } else {
        self.rangeContent = [[UGCKitRangeContent alloc] initWithImageList:images];
    }
    self.rangeContent.delegate = self;
    
    [self.bgScrollView addSubview:self.rangeContent];
    self.bgScrollView.contentSize = [self.rangeContent intrinsicContentSize];
    self.bgScrollView.ugckit_height = self.bgScrollView.contentSize.height;
    self.bgScrollView.contentInset = UIEdgeInsetsMake(0, self.ugckit_width/2-self.rangeContent.pinWidth,
                                                      0, self.ugckit_width/2-self.rangeContent.pinWidth);
    
    [self setCurrentPos:0];
}

- (void)updateImage:(UIImage *)image atIndex:(NSUInteger)index;
{
    self.rangeContent.imageViewList[index].image = image;
}

- (void)setLeftPanHidden:(BOOL)isHidden
{
    self.rangeContent.leftPin.hidden = isHidden;
    [self.rangeContent unpdateBorder];
}

- (void)setCenterPanHidden:(BOOL)isHidden
{
    self.rangeContent.centerPin.hidden = isHidden;
}

- (void)setRightPanHidden:(BOOL)isHidden
{
    self.rangeContent.rightPin.hidden = isHidden;
    [self.rangeContent unpdateBorder];
}

- (void)setLeftPanFrame:(CGFloat)time
{
    _leftPos = time;
    self.rangeContent.leftPinCenterX = time / _durationMs * self.rangeContent.imageListWidth + self.rangeContent.pinWidth / 2;
    self.rangeContent.leftPin.center = CGPointMake(self.rangeContent.leftPinCenterX, self.rangeContent.leftPin.center.y);
    [self.rangeContent unpdateBorder];
}

- (void)setCenterPanFrame:(CGFloat)time
{
    self.rangeContent.centerPinCenterX = time / _durationMs * self.rangeContent.imageListWidth + self.rangeContent.pinWidth;
    self.rangeContent.centerPin.center = CGPointMake(self.rangeContent.centerPinCenterX, self.rangeContent.centerPin.center.y);
}

- (void)setRightPanFrame:(CGFloat)time
{
    _rightPos = time;
    self.rangeContent.rightPinCenterX = time / _durationMs * self.rangeContent.imageListWidth + self.rangeContent.pinWidth * 3 / 2;
    self.rangeContent.rightPin.center = CGPointMake(self.rangeContent.rightPinCenterX, self.rangeContent.rightPin.center.y);
    [self.rangeContent unpdateBorder];
}

- (void)setColorType:(UGCKitRangeColorType)UGCKitRangeColorType
{
    _colorType = UGCKitRangeColorType;
    if (_colorType == UGCKitRangeColorType_Cut) {
        [self setLeftPanHidden:NO];
        [self setRightPanHidden:NO];
    }else{
        [self setLeftPanHidden:YES];
        [self setRightPanHidden:YES];
        [self setLeftPanFrame:0];
        [self setRightPanFrame:_rightPos];
        [self.rangeContent unpdateBorder];
        self.rangeContent.leftCover.hidden = YES;
        self.rangeContent.rightCover.hidden = YES;
    }
    for (UGCKitVideoColorInfo *info in _colorInfos) {
        if (info.UGCKitRangeColorType != _colorType) {
            info.colorView.hidden = YES;
        }else{
            info.colorView.hidden = NO;
        }
    }
  
}

- (void)startColoration:(UIColor *)color alpha:(CGFloat)alpha
{
    UGCKitVideoColorInfo *info = [[UGCKitVideoColorInfo alloc] init];
    info.colorView = [UIView new];
    info.colorView.backgroundColor = color;
    info.colorView.alpha = alpha;
    info.UGCKitRangeColorType = _colorType;
    [_colorInfos addObject:info];
    if (_colorType == UGCKitRangeColorType_Effect) {
        info.startPos = _currentPos;
    }else{
        info.startPos = _leftPos;
        info.endPos   = _rightPos;
        CGFloat x = self.rangeContent.pinWidth + _leftPos * self.rangeContent.imageListWidth / _durationMs;
        CGFloat width = fabs(_leftPos - _rightPos) * self.rangeContent.imageListWidth / _durationMs;
        info.colorView.frame = CGRectMake(x, 0, width, self.ugckit_height);
    }
    UITapGestureRecognizer *tapGes = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    [info.colorView addGestureRecognizer:tapGes];
    [self.rangeContent insertSubview:info.colorView belowSubview:self.rangeContent.leftPin];
    _startColor = YES;
    _selectColorInfo = info;
}

- (void)stopColoration
{
    if (_colorType == UGCKitRangeColorType_Effect) {
        UGCKitVideoColorInfo *info = [_colorInfos lastObject];
        info.endPos = _currentPos;
        
        if (_currentPos + 1.5/ _fps >= _durationMs) {
            info.colorView.frame = [self coloredFrameForStartTime:info.startPos endTime:_durationMs];
            info.endPos = _durationMs;
        } else {
            info.endPos = _currentPos;
        }
    }
    _startColor = NO;
}

- (UGCKitVideoColorInfo *)removeLastColoration:(UGCKitRangeColorType)UGCKitRangeColorType;
{
    for (NSInteger i = _colorInfos.count - 1; i >= 0; i --) {
        UGCKitVideoColorInfo *info = (UGCKitVideoColorInfo *)_colorInfos[i];
        if (info.UGCKitRangeColorType == UGCKitRangeColorType) {
            [info.colorView removeFromSuperview];
            [_colorInfos removeObject:info];
            return info;
        }
    }
    return nil;
}

- (void)removeColoration:(UGCKitRangeColorType)UGCKitRangeColorType index:(NSInteger)index;
{
    NSInteger count = 0;
    for (NSInteger i = 0; i < _colorInfos.count; i ++) {
        UGCKitVideoColorInfo *info = (UGCKitVideoColorInfo *)_colorInfos[i];
        if (info.UGCKitRangeColorType == UGCKitRangeColorType) {
            if (count == index) {
                [info.colorView removeFromSuperview];
                [_colorInfos removeObject:info];
                break;
            }
            count++;
        }
    }
}

- (CGRect)coloredFrameForStartTime:(float)start endTime:(float)end {
    CGFloat boxWidth = self.rangeContent.imageListWidth / _durationMs; // 帧的宽度
    return CGRectMake(self.rangeContent.pinWidth + start * boxWidth, 0, (end - start) * boxWidth, self.rangeContent.ugckit_height);
}

- (void)setDurationMs:(CGFloat)durationMs {
    //duration 发生变化的时候，更新下特效所在的位置
    if (_durationMs != durationMs) {
        for (UGCKitVideoColorInfo *info in _colorInfos) {
            CGFloat x = self.rangeContent.pinWidth + info.startPos * self.rangeContent.imageListWidth / durationMs;
            CGFloat width = fabs(info.endPos - info.startPos) * self.rangeContent.imageListWidth / durationMs;
            info.colorView.frame = CGRectMake(x, 0, width, self.ugckit_height);
        }
        _durationMs = durationMs;
    }
    
    _leftPos = 0;
    _rightPos = _durationMs;
    [self setCurrentPos:_currentPos];
    
    _leftPos =  self.durationMs * self.rangeContent.leftScale;
    _centerPos = self.durationMs * self.rangeContent.centerScale;
    _rightPos = self.durationMs * self.rangeContent.rightScale;
}

- (void)setCurrentPos:(CGFloat)currentPos
{
    _currentPos = currentPos;
    if (_durationMs <= 0) {
        return;
    }
    CGFloat off = currentPos * self.rangeContent.imageListWidth / _durationMs;
    //    off += self.rangeContent.leftPin.width;
    off -= self.bgScrollView.contentInset.left;
    
    self.disableSeek = YES;
    self.bgScrollView.contentOffset = CGPointMake(off, 0);
    
    UGCKitVideoColorInfo *info = [_colorInfos lastObject];
    if (_colorType == UGCKitRangeColorType_Effect && _startColor) {
        CGRect frame;
        if (_currentPos > info.startPos) {
            frame = [self coloredFrameForStartTime:info.startPos endTime:_currentPos];
        }else{
            frame = [self coloredFrameForStartTime:_currentPos endTime:info.startPos];
        }
        info.colorView.frame = frame;
    }
    self.disableSeek = NO;
}

- (void)handleTap:(UITapGestureRecognizer *)gesture
{
    if (gesture.state == UIGestureRecognizerStateEnded) {
        CGPoint point = [gesture locationInView:self.rangeContent];
        CGFloat tapTime = (point.x - self.rangeContent.pinWidth) / self.rangeContent.imageListWidth * _durationMs;
        for (UGCKitVideoColorInfo *info in _colorInfos) {
            if (info.UGCKitRangeColorType == _colorType && tapTime >= info.startPos && tapTime <= info.endPos) {
                _selectColorInfo = info;
                break;
            }
        }
        if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeTap:)]) {
            [self.delegate onVideoRangeTap:tapTime];
        }
    }
}

- (void)setSelectColorInfo:(NSInteger)selectedIndex
{
    if (selectedIndex < _colorInfos.count && _colorInfos[selectedIndex]) {
        _selectColorInfo = _colorInfos[selectedIndex];
    }
}

#pragma mark TXVideoRangeContentDelegate

- (void)onRangeLeftChanged:(UGCKitRangeContent *)sender
{
    _leftPos  = self.durationMs * sender.leftScale;
    _rightPos = self.durationMs * sender.rightScale;
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeLeftChanged:)]) {
        [self.delegate onVideoRangeLeftChanged:self];
    }
    
    if (_colorType == UGCKitRangeColorType_Paster || _colorType == UGCKitRangeColorType_Text) {
        CGFloat x = self.rangeContent.pinWidth + _leftPos * self.rangeContent.imageListWidth / _durationMs;
        CGFloat width = fabs(_leftPos - _rightPos) * self.rangeContent.imageListWidth / _durationMs;
        _selectColorInfo.startPos = _leftPos;
        _selectColorInfo.colorView.frame = CGRectMake(x, 0, width, self.ugckit_height);
    }
}

- (void)onRangeLeftChangeEnded:(UGCKitRangeContent *)sender
{
    _leftPos  = self.durationMs * sender.leftScale;
    _rightPos = self.durationMs * sender.rightScale;
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeLeftChangeEnded:)]) {
        [self.delegate onVideoRangeLeftChangeEnded:self];
    }
}

- (void)onRangeCenterChanged:(UGCKitRangeContent *)sender
{
    _leftPos  = self.durationMs * sender.leftScale;
    _rightPos = self.durationMs * sender.rightScale;
    _centerPos =  self.durationMs * sender.centerScale;
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeCenterChanged:)]) {
        [self.delegate onVideoRangeCenterChanged:self];
    }
}

- (void)onRangeCenterChangeEnded:(UGCKitRangeContent *)sender
{
    _leftPos  = self.durationMs * sender.leftScale;
    _rightPos = self.durationMs * sender.rightScale;
    _centerPos =  self.durationMs * sender.centerScale;
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeCenterChangeEnded:)]) {
        [self.delegate onVideoRangeCenterChangeEnded:self];
    }
}

- (void)onRangeRightChanged:(UGCKitRangeContent *)sender
{
    _leftPos  = self.durationMs * sender.leftScale;
    _rightPos = self.durationMs * sender.rightScale;
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeRightChanged:)]) {
        [self.delegate onVideoRangeRightChanged:self];
    }
    
    if (_colorType == UGCKitRangeColorType_Paster || _colorType == UGCKitRangeColorType_Text) {
        CGFloat x = self.rangeContent.pinWidth + _leftPos * self.rangeContent.imageListWidth / _durationMs;
        CGFloat width = fabs(_leftPos - _rightPos) * self.rangeContent.imageListWidth / _durationMs;
        _selectColorInfo.endPos = _rightPos;
        _selectColorInfo.colorView.frame = CGRectMake(x, 0, width, self.ugckit_height);
    }
}

- (void)onRangeRightChangeEnded:(UGCKitRangeContent *)sender
{
    _leftPos  = self.durationMs * sender.leftScale;
    _rightPos = self.durationMs * sender.rightScale;
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeRightChangeEnded:)]) {
        [self.delegate onVideoRangeRightChangeEnded:self];
    }
}

- (void)onRangeLeftAndRightChanged:(UGCKitRangeContent *)sender
{
    _leftPos  = self.durationMs * sender.leftScale;
    _rightPos = self.durationMs * sender.rightScale;
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    CGFloat pos = scrollView.contentOffset.x;
    pos += scrollView.contentInset.left;
    if (pos < 0) pos = 0;
    if (pos > self.rangeContent.imageListWidth) pos = self.rangeContent.imageListWidth;
    
    _currentPos = self.durationMs * pos/self.rangeContent.imageListWidth;
    if (self.disableSeek == NO) {
        if (self.delegate && [self.delegate respondsToSelector:@selector(onVideoRange:seekToPos:)]) {
            [self.delegate onVideoRange:self seekToPos:self.currentPos];
        }
    }
}
@end
