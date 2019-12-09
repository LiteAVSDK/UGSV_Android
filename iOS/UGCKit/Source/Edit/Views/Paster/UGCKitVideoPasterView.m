// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVideoPasterView.h"
#import "UGCKit_UIViewAdditions.h"
#import "UGCKitColorMacro.h"


@interface UGCKitVideoPasterView () <UITextViewDelegate, UITextFieldDelegate>
{
    UIView*         _borderView;                //用来显示边框或样式背景
    UIButton*       _deleteBtn;                 //删除铵钮
    UIButton*       _scaleRotateBtn;            //单手操作放大，旋转按钮
    
    CGRect          _initFrame;
    UGCKitTheme    *_theme;
}

@end

@implementation UGCKitVideoPasterView

- (id)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme;
{
    if (self = [super initWithFrame:frame]) {
        _theme = theme;
        _initFrame = frame;
        
        _pasterImageView = [[UIImageView alloc] init];
        UITapGestureRecognizer* singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTap:)];
        singleTap.numberOfTapsRequired = 1;
        _pasterImageView.userInteractionEnabled = YES;
        [_pasterImageView addGestureRecognizer:singleTap];
        
        _borderView = [UIView new];
        _borderView.layer.borderWidth = 1;
        _borderView.layer.borderColor = theme.editPasterBorderColor.CGColor;
        _borderView.userInteractionEnabled = YES;
        _borderView.backgroundColor = [UIColor clearColor];
        [self addSubview:_borderView];
    
        _deleteBtn = [UIButton new];
        [_deleteBtn setImage:_theme.closeIcon forState:UIControlStateNormal];
        [_deleteBtn addTarget:self action:@selector(onDeleteBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_deleteBtn];
        
        _scaleRotateBtn = [UIButton new];
        [_scaleRotateBtn setImage:_theme.editTextPasterRotateIcon forState:UIControlStateNormal];
        UIPanGestureRecognizer* panGensture = [[UIPanGestureRecognizer alloc] initWithTarget:self action: @selector (handlePanSlide:)];
        [self addSubview:_scaleRotateBtn];
        [_scaleRotateBtn addGestureRecognizer:panGensture];
        
        [_borderView  addSubview:_pasterImageView];
        
        UIPanGestureRecognizer* selfPanGensture = [[UIPanGestureRecognizer alloc] initWithTarget:self action: @selector (handlePanSlide:)];
        [self addGestureRecognizer:selfPanGensture];
        
        UIPinchGestureRecognizer* pinchGensture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
        [self addGestureRecognizer:pinchGensture];

        UIRotationGestureRecognizer* rotateGensture = [[UIRotationGestureRecognizer alloc] initWithTarget:self action:@selector(handleRotate:)];
        [self addGestureRecognizer:rotateGensture];

        
        _rotateAngle = 0.f;
        
    }
    
    return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    
    if (self.ugckit_width == 0 || self.ugckit_height == 0) return;
    
    CGPoint center = [self convertPoint:self.center fromView:self.superview];

    _borderView.bounds = CGRectMake(0, 0, self.bounds.size.width - 25, self.bounds.size.height - 25);
    _borderView.center = center;

    _pasterImageView.frame = CGRectMake(0, 0, _borderView.bounds.size.width, _borderView.bounds.size.height);

    _deleteBtn.center = CGPointMake(_borderView.ugckit_x, _borderView.ugckit_y);
    _deleteBtn.bounds = CGRectMake(0, 0, 50, 50);

    _scaleRotateBtn.center = CGPointMake(_borderView.ugckit_right, _borderView.ugckit_bottom);
    _scaleRotateBtn.bounds = CGRectMake(0, 0, 50, 50);
}

- (void)setImageList:(NSArray *)imageList imageDuration:(float)duration;
{
    if (imageList.count > 1) {
        _pasterImageView.animationImages = imageList;
        _pasterImageView.animationDuration = imageList.count / duration;
        [_pasterImageView startAnimating];
    }else if (imageList.count > 0){
        _pasterImageView.image = imageList[0];
    }
}


- (CGRect)pasterFrameOnView:(UIView *)view
{
    CGRect frame = CGRectMake(_borderView.ugckit_x, _borderView.ugckit_y, _borderView.bounds.size.width, _borderView.bounds.size.height);
    
    if (![view.subviews containsObject:self]) {
        [view addSubview:self];
        CGRect rc = [self convertRect:frame toView:view];
        [self removeFromSuperview];
        return rc;
    }
    
    return [self convertRect:frame toView:view];
}


#pragma mark - GestureRecognizer handle
- (void)onTap:(UITapGestureRecognizer*)recognizer
{
    [self.delegate onPasterViewTap];
}

- (void)handlePanSlide:(UIPanGestureRecognizer*)recognizer
{
    //拖动
    if (recognizer.view == self) {
        CGPoint translation = [recognizer translationInView:self.superview];
        CGPoint center = CGPointMake(recognizer.view.center.x + translation.x,
                                     recognizer.view.center.y + translation.y);
        if (center.x < 0) {
            center.x = 0;
        }
        else if (center.x > self.superview.ugckit_width) {
            center.x = self.superview.ugckit_width;
        }
        
        if (center.y < 0) {
            center.y = 0;
        }
        else if (center.y > self.superview.ugckit_height) {
            center.y = self.superview.ugckit_height;
        }
        
        recognizer.view.center = center;
        
        [recognizer setTranslation:CGPointZero inView:self.superview];
        
        
    }
    else if (recognizer.view == _scaleRotateBtn) {
        CGPoint translation = [recognizer translationInView:self];
        
        //放大
        if (recognizer.state == UIGestureRecognizerStateChanged) {
            CGFloat delta = translation.x;
            self.bounds = CGRectMake(0, 0, self.bounds.size.width + delta, self.bounds.size.height + delta);
        }
        [recognizer setTranslation:CGPointZero inView:self];
        
        //旋转
        CGPoint newCenter = CGPointMake(recognizer.view.center.x + translation.x, recognizer.view.center.y + translation.y);
        CGPoint anthorPoint = _pasterImageView.center;
        CGFloat height = newCenter.y - anthorPoint.y;
        CGFloat width = newCenter.x - anthorPoint.x;
        CGFloat angle1 = atan(height / width);
        height = recognizer.view.center.y - anthorPoint.y;
        width = recognizer.view.center.x - anthorPoint.x;
        CGFloat angle2 = atan(height / width);
        CGFloat angle = angle1 - angle2;
        
        self.transform = CGAffineTransformRotate(self.transform, angle);
        _rotateAngle += angle;
    }
    
}

//双手指放大
- (void)handlePinch:(UIPinchGestureRecognizer*)recognizer
{
    self.bounds = CGRectMake(0, 0, self.bounds.size.width * recognizer.scale, self.bounds.size.height * recognizer.scale);
    recognizer.scale = 1;
}

////双手指旋转
- (void)handleRotate:(UIRotationGestureRecognizer*)recognizer
{
    recognizer.view.transform = CGAffineTransformRotate(recognizer.view.transform, recognizer.rotation);

    _rotateAngle += recognizer.rotation;
    recognizer.rotation = 0;
}

//如果是静态贴纸，生成静态贴纸图片
- (UIImage*)staticImage
{
    _borderView.layer.borderWidth = 0;
    [_borderView setNeedsDisplay];
    CGRect rect = _borderView.bounds;
    UIView *rotatedViewBox = [[UIView alloc]initWithFrame:CGRectMake(0, 0, rect.size.width , rect.size.height)];
    CGAffineTransform t = CGAffineTransformMakeRotation(_rotateAngle);
    rotatedViewBox.transform = t;
    CGSize rotatedSize = rotatedViewBox.frame.size;
    
    UIGraphicsBeginImageContextWithOptions(rotatedSize, NO, 0.f);
    
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextTranslateCTM(context, rotatedSize.width/2, rotatedSize.height/2);
    
    CGContextRotateCTM(context, _rotateAngle);
    
    //[_textLabel drawTextInRect:CGRectMake(-rect.size.width / 2, -rect.size.height / 2, rect.size.width, rect.size.height)];
    [_borderView drawViewHierarchyInRect:CGRectMake(-rect.size.width / 2, -rect.size.height / 2, rect.size.width, rect.size.height) afterScreenUpdates:YES];
    UIImage *rotatedImg = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    _borderView.layer.borderWidth = 1;
    _borderView.layer.borderColor = UIColorFromRGB(0x0accac).CGColor;
    
    return rotatedImg;
}


- (void)onDeleteBtnClicked:(UIButton*)sender
{
    [self.delegate onRemovePasterView:self];
    [self removeFromSuperview];
}


- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end

