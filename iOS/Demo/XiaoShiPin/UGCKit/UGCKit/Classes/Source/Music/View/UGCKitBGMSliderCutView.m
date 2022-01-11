// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitBGMSliderCutView.h"
#import "UGCKit_UIViewAdditions.h"

@implementation UGCKitBGMSliderCutViewConfig
- (id)initWithTheme:(UGCKitTheme *)theme
{
    if (self = [super init]) {
        _pinWidth = PIN_WIDTH;
        _thumbHeight = THUMB_HEIGHT;
        _borderHeight = BORDER_HEIGHT;
        _leftPinImage = theme.editMusicSliderLeftIcon;
        _rightPigImage = theme.editMusicSliderRightIcon;
        _borderColor = theme.editMusicSliderBorderColor;
        _durationUnit = 15;
        _labelDurationInternal = 5;
    }
    
    return self;
}
@end


@interface UGCKitBGMSliderCutView()<UIScrollViewDelegate>

@end

@implementation UGCKitBGMSliderCutView {
    CGFloat _imageWidth;
    UGCKitBGMSliderCutViewConfig* _appearanceConfig;
    float sliderWidth;
    int dragIdx;//0 non 1 left 2 right
}

- (instancetype)initWithImage:(UIImage*)image config:(UGCKitBGMSliderCutViewConfig *)config
{
    _image = image;
    _appearanceConfig = config;
    
    self = [super initWithFrame:_appearanceConfig.frame];
    
    [self iniSubViews];
    
    return self;
}

- (void)iniSubViews
{
    CGRect frame = self.bounds;
    CGRect contentFrame = CGRectMake(_appearanceConfig.pinWidth,
                                     _appearanceConfig.borderHeight,
                                     _appearanceConfig.frame.size.width - 2 * _appearanceConfig.pinWidth,
                                     _appearanceConfig.thumbHeight);
    sliderWidth = (_appearanceConfig.frame.size.width - 2 * _appearanceConfig.pinWidth)*
    _appearanceConfig.duration / _appearanceConfig.durationUnit;
    
    _imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0,0,sliderWidth,_appearanceConfig.thumbHeight)];
    //        imgView.image = _imageList[i];
    //        imgView.contentMode = UIViewContentModeScaleToFill;
    UIColor *colorPattern = [[UIColor alloc] initWithPatternImage:_image];
    _imageView.contentMode = UIViewContentModeScaleToFill;
    [_imageView setBackgroundColor:colorPattern];
    float labelW = 40;
    float labelH = 10;
    
    for (float l = _appearanceConfig.labelDurationInternal; l < sliderWidth;l += _appearanceConfig.labelDurationInternal) {
        CGFloat lw = (l / _appearanceConfig.duration) * sliderWidth;
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(lw - labelW / 2,
                                                                  _appearanceConfig.borderHeight + _appearanceConfig.thumbHeight / 2 - labelH / 2,
                                                                  labelW,
                                                                  MIN(labelH, _appearanceConfig.thumbHeight))];
        label.backgroundColor = [UIColor colorWithWhite:1 alpha:0.5];
        label.layer.cornerRadius = labelH/2;
        label.clipsToBounds = YES;
        label.textAlignment = NSTextAlignmentCenter;
        [label setText:[UGCKitBGMSliderCutView timeString:l]];
        [label setTextColor:[UIColor blackColor]];
        [label setFont:[UIFont systemFontOfSize:10]];
        [_imageView addSubview:label];
    }
    
    _bgScrollView = [[UIScrollView alloc] initWithFrame:contentFrame];
    [self addSubview:_bgScrollView];
    _bgScrollView.showsVerticalScrollIndicator = NO;
    _bgScrollView.showsHorizontalScrollIndicator = NO;
    _bgScrollView.scrollsToTop = NO;
    _bgScrollView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    _bgScrollView.delegate = self;
    _bgScrollView.contentSize = CGSizeMake(sliderWidth,_appearanceConfig.thumbHeight);
    _bgScrollView.decelerationRate = 0.1f;
    _bgScrollView.bounces = NO;
    [_bgScrollView addSubview:_imageView];
    
    if (_appearanceConfig.leftCorverImage) {
        self.leftCover = [[UIImageView alloc] initWithImage:_appearanceConfig.leftCorverImage];
        self.leftCover.contentMode = UIViewContentModeCenter;
        self.leftCover.clipsToBounds = YES;
    }
    else {
        self.leftCover = [[UIImageView alloc] initWithFrame:CGRectZero];
        self.leftCover.backgroundColor = [UIColor blackColor];
        self.leftCover.alpha = 0.5;
    };
    [self addSubview:self.leftCover];
    
    
    if (_appearanceConfig.rightCoverImage) {
        self.rightCover = [[UIImageView alloc] initWithImage:_appearanceConfig.rightCoverImage];
        self.rightCover.contentMode = UIViewContentModeCenter;
        self.rightCover.clipsToBounds = YES;
        
    }
    else {
        self.rightCover = [[UIImageView alloc] initWithFrame:CGRectZero];
        self.rightCover.backgroundColor = [UIColor blackColor];
        self.rightCover.alpha = 0.5;
    }
    [self addSubview:self.rightCover];
    
    self.leftPin = ({
        UIImageView *imageView = [[UIImageView alloc] initWithImage:_appearanceConfig.leftPinImage];
        imageView.contentMode = UIViewContentModeScaleToFill;
        imageView.ugckit_width = _appearanceConfig.pinWidth;
        [self addSubview:imageView];
        imageView;
    });
    
    self.rightPin = ({
        UIImageView *imageView = [[UIImageView alloc] initWithImage:_appearanceConfig.rightPigImage];
        imageView.contentMode = UIViewContentModeScaleToFill;
        imageView.ugckit_width = _appearanceConfig.pinWidth;
        [self addSubview:imageView];
        imageView;
    });
    
    self.topBorder = ({
        UIView *view = [[UIView alloc] initWithFrame:CGRectZero];
        [self addSubview:view];
        view.backgroundColor = _appearanceConfig.borderColor;
        view;
    });
    
    self.bottomBorder = ({
        UIView *view = [[UIView alloc] initWithFrame:CGRectZero];
        [self addSubview:view];
        view.backgroundColor = _appearanceConfig.borderColor;
        view;
    });
    
    _leftPinCenterX = _appearanceConfig.pinWidth / 2;
    _rightPinCenterX = frame.size.width- _appearanceConfig.pinWidth / 2;
}

+(NSString*) timeString:(CGFloat) time{
    int t = ((int)time) % 3600;
    int m = t / 60;
    NSString* ret = nil;
    if(m < 10){
        ret = [NSString stringWithFormat:@"0%d:", m];
    }
    else ret = [NSString stringWithFormat:@"%d:", m];
    int s = t % 60;
    if(s < 10){
        ret = [NSString stringWithFormat:@"%@0%d", ret ,s];
    }
    else ret = [NSString stringWithFormat:@"%@%d", ret ,s];
    return ret;
}

- (CGSize)intrinsicContentSize {
    return CGSizeMake(_appearanceConfig.frame.size.width, _appearanceConfig.thumbHeight + 2 * _appearanceConfig.borderHeight);
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    CGFloat pos = scrollView.contentOffset.x;
    if (pos < 0) pos = 0;
    if (pos > sliderWidth) pos = sliderWidth;
    _leftPinCenterX = pos;
    [self.delegate onRangeLeftChanged:self percent:pos / sliderWidth];
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate{
    if(!decelerate){
        CGFloat pos = scrollView.contentOffset.x;
        pos += scrollView.contentInset.left;
        if (pos < 0) pos = 0;
        if (pos > sliderWidth) pos = sliderWidth;
        [self.delegate onRangeLeftChangeEnded:self percent:pos / sliderWidth];
    }
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    CGFloat pos = scrollView.contentOffset.x;
    pos += scrollView.contentInset.left;
    if (pos < 0) pos = 0;
    if (pos > sliderWidth) pos = sliderWidth;
    NSLog(@"EndDecelerating%f",pos / sliderWidth * _appearanceConfig.duration);
    [self.delegate onRangeLeftChangeEnded:self percent:pos / sliderWidth];
}

//-(void)scrollViewWillBeginDecelerating: (UIScrollView *)scrollView
//{
////    [scrollView setContentOffset:scrollView.contentOffset animated:NO];
//}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    self.leftPin.center = CGPointMake(self.leftPinCenterX, self.ugckit_height / 2);
    self.rightPin.center = CGPointMake(self.rightPinCenterX, self.ugckit_height / 2);
    
    self.topBorder.ugckit_height = _appearanceConfig.borderHeight;
    self.topBorder.ugckit_width = self.rightPinCenterX - self.leftPinCenterX;
    self.topBorder.ugckit_y = self.bgScrollView.ugckit_y;
    self.topBorder.ugckit_x = self.leftPinCenterX;
    
    self.bottomBorder.ugckit_height = _appearanceConfig.borderHeight;
    self.bottomBorder.ugckit_width = self.rightPinCenterX - self.leftPinCenterX;
    self.bottomBorder.ugckit_y = self.bgScrollView.ugckit_y + self.bgScrollView.ugckit_height;
    self.bottomBorder.ugckit_x = self.leftPinCenterX;
    
    
    self.leftCover.ugckit_height = self.bgScrollView.ugckit_height;
    self.leftCover.ugckit_width = self.leftPinCenterX - _appearanceConfig.pinWidth / 2;
    self.leftCover.ugckit_y = _appearanceConfig.borderHeight;
    self.leftCover.ugckit_x = _appearanceConfig.pinWidth;
    
    self.rightCover.ugckit_height = self.bgScrollView.ugckit_height;
    self.rightCover.ugckit_width = self.ugckit_width - self.rightPinCenterX - _appearanceConfig.pinWidth/2;
    self.rightCover.ugckit_y = self.bgScrollView.ugckit_y + self.bgScrollView.ugckit_height;
    self.rightCover.ugckit_x = self.rightPinCenterX - _appearanceConfig.pinWidth/2 + 1;
    self.leftPin.ugckit_y = self.bgScrollView.ugckit_y;
    self.leftPin.ugckit_height = self.bgScrollView.ugckit_height + self.topBorder.ugckit_height;
    self.rightPin.ugckit_y = self.bgScrollView.ugckit_y;
    self.rightPin.ugckit_height = self.bgScrollView.ugckit_height + self.topBorder.ugckit_height;
}

-(CGFloat) getPointDistance:(CGPoint) p1 point2:(CGPoint) p2{
    return sqrtf((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
}

- (CGFloat)pinWidth
{
    return _appearanceConfig.pinWidth;
}

- (CGFloat)leftScale {
    return _leftPinCenterX / sliderWidth;
}

- (CGFloat)rightScale {
    return (sliderWidth - _appearanceConfig.pinWidth / 2 - _appearanceConfig.pinWidth) / sliderWidth;
}

-(void) resetCutView{
    _leftPinCenterX = _appearanceConfig.pinWidth / 2;
    _rightPinCenterX = self.ugckit_width - _appearanceConfig.pinWidth / 2;
    [self setNeedsLayout];
}

@end

