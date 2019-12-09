// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitBGMCutView.h"
#import "UGCKit_UIViewAdditions.h"

@implementation TCBGMCutViewConfig
- (id)initWithTheme:(UGCKitTheme*)theme
{
    if (self = [super init]) {
        _pinWidth = PIN_WIDTH;
        _thumbHeight = THUMB_HEIGHT;
        _borderHeight = BORDER_HEIGHT;
        _leftPinImage = theme.editCutSliderLeftIcon;
        _centerPinImage = theme.editCutSliderCenterIcon;
        _rightPigImage = theme.editCutSliderRightIcon;
    }
    
    return self;
}
@end


@interface UGCKitBGMCutView()

@end

@implementation UGCKitBGMCutView {
    CGFloat _imageWidth;
    TCBGMCutViewConfig* _appearanceConfig;
    float sliderWidth;
    int dragIdx;//0 non 1 left 2 right
}


- (instancetype)initWithImageList:(NSArray *)images
{
    _imageList = images;
    _appearanceConfig = [TCBGMCutViewConfig new];
    
    CGRect frame = {.origin = CGPointZero, .size = [self intrinsicContentSize]};
    self = [super initWithFrame:frame];
    
    [self iniSubViews];
    
    return self;
}


- (instancetype)initWithImageList:(NSArray *)images config:(TCBGMCutViewConfig *)config
{
    _imageList = images;
    _appearanceConfig = config;
    
    self = [super initWithFrame:_appearanceConfig.frame];
    
    [self iniSubViews];
    
    return self;
}

- (void)iniSubViews
{
    
    self.userInteractionEnabled = YES;
    TCPanGestureRecognizer *panGes = [[TCPanGestureRecognizer alloc] initWithTarget:self action:@selector(handleGesture:) inview:self];
    [self addGestureRecognizer:panGes];
    
    CGRect frame = self.bounds;
    NSMutableArray *tmpList = [NSMutableArray new];
    for (int i = 0; i < _imageList.count; i++) {
        CGRect imgFrame = CGRectMake(_appearanceConfig.pinWidth + i*[self imageWidth],
                                     _appearanceConfig.borderHeight,
                                     _appearanceConfig.frame.size.width - 2 * _appearanceConfig.pinWidth,
                                     _appearanceConfig.thumbHeight);
        sliderWidth = _appearanceConfig.frame.size.width - 2 * _appearanceConfig.pinWidth;
        UIImageView *imgView = [[UIImageView alloc] initWithFrame:imgFrame];
        imgView.image = _imageList[i];
        imgView.contentMode = (_imageList.count > 1 ? UIViewContentModeScaleToFill : UIViewContentModeScaleAspectFit);
        imgView.contentMode = UIViewContentModeScaleToFill;
        [self addSubview:imgView];
        [tmpList addObject:imgView];
    }
    _imageViewList = tmpList;
    
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
        view.backgroundColor = [UIColor colorWithRed:0.14 green:0.80 blue:0.67 alpha:1];
        view;
    });
    
    self.bottomBorder = ({
        UIView *view = [[UIView alloc] initWithFrame:CGRectZero];
        [self addSubview:view];
        view.backgroundColor = [UIColor colorWithRed:0.14 green:0.80 blue:0.67 alpha:1];
        view;
    });
    
    _leftPinCenterX = _appearanceConfig.pinWidth / 2;
    _rightPinCenterX = frame.size.width- _appearanceConfig.pinWidth / 2;
}

- (CGSize)intrinsicContentSize {
    return CGSizeMake(_appearanceConfig.frame.size.width, _appearanceConfig.thumbHeight + 2 * _appearanceConfig.borderHeight);
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    self.leftPin.center = CGPointMake(self.leftPinCenterX, self.ugckit_height / 2);
    self.rightPin.center = CGPointMake(self.rightPinCenterX, self.ugckit_height / 2);
    
    self.topBorder.ugckit_height = _appearanceConfig.borderHeight;
    self.topBorder.ugckit_width = self.rightPinCenterX - self.leftPinCenterX;
    self.topBorder.ugckit_y = 0;
    self.topBorder.ugckit_x = self.leftPinCenterX;
    
    self.bottomBorder.ugckit_height = _appearanceConfig.borderHeight;
    self.bottomBorder.ugckit_width = self.rightPinCenterX - self.leftPinCenterX;
    self.bottomBorder.ugckit_y = self.leftPin.ugckit_bottom-_appearanceConfig.borderHeight;
    self.bottomBorder.ugckit_x = self.leftPinCenterX;

    
    self.leftCover.ugckit_height = _appearanceConfig.thumbHeight;
    self.leftCover.ugckit_width = self.leftPinCenterX - _appearanceConfig.pinWidth / 2;
    self.leftCover.ugckit_y = _appearanceConfig.borderHeight;
    self.leftCover.ugckit_x = _appearanceConfig.pinWidth;
    
    self.rightCover.ugckit_height = _appearanceConfig.thumbHeight;
    self.rightCover.ugckit_width = self.ugckit_width - self.rightPinCenterX - _appearanceConfig.pinWidth/2;
    self.rightCover.ugckit_y = _appearanceConfig.borderHeight;
    self.rightCover.ugckit_x = self.rightPinCenterX - _appearanceConfig.pinWidth/2 + 1;
}

-(CGFloat) getPointDistance:(CGPoint) p1 point2:(CGPoint) p2{
    return sqrtf((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
}

#pragma mark - Gestures
-(void)handleGesture:(TCPanGestureRecognizer *)gesture{
    if(gesture.state == UIGestureRecognizerStateBegan){
        CGFloat d1 = [self getPointDistance:[gesture beginPoint] point2:_leftPin.center];
        CGFloat d2 = [self getPointDistance:[gesture beginPoint] point2:_rightPin.center];
        CGFloat threshold = 50;
        if(d1 < threshold || d2 < threshold){
            if(d1 < d2){
                dragIdx = 1;
            }
            else dragIdx = 2;
        }
        else dragIdx = 0;
    }
    if(dragIdx == 1){
        [self handleLeftPan:gesture];
    }
    else if(dragIdx == 2){
        [self handleRightPan:gesture];
    }
    if(gesture.state == UIGestureRecognizerStateEnded){
        dragIdx = 0;
    }
}

- (void)handleLeftPan:(TCPanGestureRecognizer *)gesture
{
    CGPoint translation = [gesture translationInView:self];
    
//    NSLog(@"left %f|%f", translation.x, translation.y);
    
    _leftPinCenterX += translation.x;
    if (_leftPinCenterX < _appearanceConfig.pinWidth / 2) {
        _leftPinCenterX = _appearanceConfig.pinWidth / 2;
    }
    
    if (_rightPinCenterX - _leftPinCenterX <= _appearanceConfig.pinWidth) {
        _leftPinCenterX = _rightPinCenterX - _appearanceConfig.pinWidth;
    }
    
    [gesture setTranslation:CGPointZero inView:[self superview]];
    
    [self setNeedsLayout];
    
    if (gesture.state == UIGestureRecognizerStateBegan) {
        if ([self.delegate respondsToSelector:@selector(onRangeLeftChangeBegin:)])
            [self.delegate onRangeLeftChangeBegin:self];
    }
    else if (gesture.state == UIGestureRecognizerStateChanged){
        if ([self.delegate respondsToSelector:@selector(onRangeLeftChanged:)])
            [self.delegate onRangeLeftChanged:self percent:0];
    }
    else {
        if ([self.delegate respondsToSelector:@selector(onRangeLeftChangeEnded:)])
            [self.delegate onRangeLeftChangeEnded:self percent:0];
    }
        
    
}

- (void)handleRightPan:(UIPanGestureRecognizer *)gesture
{

    CGPoint translation = [gesture translationInView:self];
    
//    NSLog(@"right %f|%f", translation.x, translation.y);
    _rightPinCenterX += translation.x;
    if (_rightPinCenterX > self.ugckit_width - _appearanceConfig.pinWidth / 2) {
        _rightPinCenterX = self.ugckit_width - _appearanceConfig.pinWidth / 2;
    }
    
    if (_rightPinCenterX-_leftPinCenterX <= _appearanceConfig.pinWidth) {
        _rightPinCenterX = _leftPinCenterX + _appearanceConfig.pinWidth;
    }
    
    [gesture setTranslation:CGPointZero inView:self];
    
    [self setNeedsLayout];
    
    
    if (gesture.state == UIGestureRecognizerStateBegan) {
        if ([self.delegate respondsToSelector:@selector(onRangeRightChangeBegin:)])
            [self.delegate onRangeRightChangeBegin:self];
    }
    else if (gesture.state == UIGestureRecognizerStateChanged) {
        if ([self.delegate respondsToSelector:@selector(onRangeRightChanged:)])
            [self.delegate onRangeRightChanged:self];
    }
    else {
        if ([self.delegate respondsToSelector:@selector(onRangeRightChangeEnded:)])
            [self.delegate onRangeRightChangeEnded:self];
    }
}

- (CGFloat)pinWidth
{
    return _appearanceConfig.pinWidth;
}

- (CGFloat)imageWidth
{
    UIImage *img = self.imageList[0];
    if (self.imageList.count == 1) {
        return MIN(img.size.width, [UIScreen mainScreen].bounds.size.width - 2 * _appearanceConfig.pinWidth);
    }
    _imageWidth = img.size.width/img.size.height*_appearanceConfig.thumbHeight;
    return _imageWidth;
}

- (CGFloat)imageListWidth {
    return self.imageList.count * [self imageWidth];
}

- (CGFloat)leftScale {
    return (_leftPinCenterX - _appearanceConfig.pinWidth / 2) / sliderWidth;
}

- (CGFloat)rightScale {
    return (_rightPinCenterX - _appearanceConfig.pinWidth / 2 - _appearanceConfig.pinWidth) / sliderWidth;
}

-(void) resetCutView{
    _leftPinCenterX = _appearanceConfig.pinWidth / 2;
    _rightPinCenterX = self.ugckit_width - _appearanceConfig.pinWidth / 2;
    [self setNeedsLayout];
}

@end


@implementation TCPanGestureRecognizer
-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent*)event{
    [super touchesBegan:touches withEvent:event];
    UITouch* touch = [touches anyObject];
    CGPoint point = [touch locationInView:_inView];
    _beginPoint = point;
}

-(instancetype)initWithTarget:(id)target action:(SEL)action inview:(UIView*)view{
    self= [super initWithTarget:target action:action];
    if(self) {
        _inView = view;
    }
    return self;
}
@end
