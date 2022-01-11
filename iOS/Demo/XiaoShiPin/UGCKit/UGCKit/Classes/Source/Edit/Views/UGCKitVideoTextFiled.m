// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVideoTextFiled.h"
#import "UGCKitColorMacro.h"
#import "UGCKit_UIViewAdditions.h"

@interface UGCKitVideoTextFiled () <UITextViewDelegate, UITextFieldDelegate>
{
    UILabel*        _textLabel;                 //文字输入Label
    UIImageView*    _borderView;                //用来显示边框或样式背景
    UIButton*       _deleteBtn;                 //删除铵钮
    UIButton*       _styleBtn;                  //样式操作按钮，目前只改字体颜色
    UIButton*       _scaleRotateBtn;            //单手操作放大，旋转按钮
    
    //自字义键盘上的输入显示
    UITextView*     _inputTextView;
    UIButton*       _inputConfirmBtn;
    
    //用来作输助呼出键盘
    UITextField*    _hiddenTextField;
    BOOL            _isInputting;
    
    //气泡背景
    UIImageView *   _bubbleView;
    
    //己旋转角度
    CGFloat         _rotateAngle;

    NSInteger       _styleIndex;
    
    CGRect          _textNormalizationFrame;
    CGRect          _initFrame;
    BOOL            _hasSetBubble;
}
@property (nonatomic, strong) UGCKitTheme *theme;
@end

@implementation UGCKitVideoTextFiled

- (id)initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme
{
    if (self = [super initWithFrame:frame]) {
        _theme = theme;
        _styleIndex = 0;
        _hasSetBubble = NO;
        _textNormalizationFrame = CGRectMake(0, 0, 1, 1);
        _initFrame = frame;
        
        _borderView = [UIImageView new];
        _borderView.layer.borderWidth = 1;
        _borderView.layer.borderColor = UIColorFromRGB(0x0accac).CGColor;
        _borderView.userInteractionEnabled = YES;
        _borderView.backgroundColor = [UIColor clearColor];
        [self addSubview:_borderView];
        
        _bubbleView = [UIImageView new];
        [_borderView addSubview:_bubbleView];
        
        _textLabel = [UILabel new];
        _textLabel.text = [_theme localizedString:@"UGCKit.Edit.TextPaster.ClickEditText"];
        _textLabel.textColor = UIColor.blackColor;
        _textLabel.shadowOffset = CGSizeMake(2, 2);
        _textLabel.font = [UIFont systemFontOfSize:18];
        _textLabel.numberOfLines = 0;
        _textLabel.textAlignment = NSTextAlignmentCenter;
        UITapGestureRecognizer* singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTap:)];
        singleTap.numberOfTapsRequired = 1;
        _textLabel.userInteractionEnabled = YES;
        [_textLabel sizeToFit];
        [_textLabel addGestureRecognizer:singleTap];
        [_borderView  addSubview:_textLabel];
    
        
        _deleteBtn = [UIButton new];
        [_deleteBtn setImage:_theme.editPasterDeleteIcon forState:UIControlStateNormal];
        [_deleteBtn addTarget:self action:@selector(onDeleteBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_deleteBtn];
        
        _styleBtn = [UIButton new];
        [_styleBtn setImage:_theme.editTextPasterEditIcon forState:UIControlStateNormal];
        [_styleBtn addTarget:self action:@selector(onStyleBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_styleBtn];
        
        _scaleRotateBtn = [UIButton new];
        [_scaleRotateBtn setImage:_theme.editTextPasterRotateIcon forState:UIControlStateNormal];
        UIPanGestureRecognizer* panGensture = [[UIPanGestureRecognizer alloc] initWithTarget:self action: @selector (handlePanSlide:)];
        [self addSubview:_scaleRotateBtn];
        [_scaleRotateBtn addGestureRecognizer:panGensture];
        
        UIView* inputAccessoryView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, 40)];
        
        UIView* labelBgView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, inputAccessoryView.ugckit_width - 40, inputAccessoryView.ugckit_height)];
        labelBgView.backgroundColor = UIColor.whiteColor;
        labelBgView.alpha = 0.5;
        [inputAccessoryView addSubview:labelBgView];
        
        _inputTextView = [[UITextView alloc] initWithFrame:CGRectMake(10, 0, inputAccessoryView.ugckit_width - 50, inputAccessoryView.ugckit_height)];
        _inputTextView.textColor = UIColorFromRGB(0xFFFFFF);
        _inputTextView.font = [UIFont systemFontOfSize:18];
        _inputTextView.textAlignment = NSTextAlignmentLeft;
        _inputTextView.delegate = self;
        _inputTextView.editable = YES;
        _inputTextView.backgroundColor = UIColor.clearColor;
        [inputAccessoryView addSubview:_inputTextView];
        
        _inputConfirmBtn = [[UIButton alloc] initWithFrame:CGRectMake(_inputTextView.ugckit_right, 0, 40, inputAccessoryView.ugckit_height)];
        _inputConfirmBtn.backgroundColor = UIColorFromRGB(0x0accac);
        [_inputConfirmBtn setImage:_theme.editTextPasterConfirmIcon forState:UIControlStateNormal];
        [_inputConfirmBtn addTarget:self action:@selector(onInputConfirmBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        [inputAccessoryView addSubview:_inputConfirmBtn];
        
        
        _hiddenTextField = [[UITextField alloc] initWithFrame:CGRectZero];
        _hiddenTextField.inputAccessoryView = inputAccessoryView;
        [self addSubview:_hiddenTextField];
        
        
        UIPanGestureRecognizer* selfPanGensture = [[UIPanGestureRecognizer alloc] initWithTarget:self action: @selector (handlePanSlide:)];
        [self addGestureRecognizer:selfPanGensture];
        
        UIPinchGestureRecognizer* pinchGensture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
        [self addGestureRecognizer:pinchGensture];
        
        UIRotationGestureRecognizer* rotateGensture = [[UIRotationGestureRecognizer alloc] initWithTarget:self action:@selector(handleRotate:)];
        [self addGestureRecognizer:rotateGensture];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(changeFirstResponder)
                                                     name:UIKeyboardDidShowNotification
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(textViewBeginChangeValue)
                                                     name:UITextViewTextDidBeginEditingNotification
                                                   object:_inputTextView];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(textViewDidChangeValue:)
                                                     name:UITextViewTextDidChangeNotification
                                                   object:_inputTextView];
        
        
        _rotateAngle = 0.f;
    }
    
    return self;
}

- (void)layoutSubviews
{
    [super layoutSubviews];

    CGPoint center = [self convertPoint:self.center fromView:self.superview];
    
    _borderView.bounds = CGRectMake(0, 0, self.bounds.size.width - 25, self.bounds.size.height - 25);
    _borderView.center = center;
    
    _bubbleView.frame = CGRectMake(0, 0, _borderView.bounds.size.width, _borderView.bounds.size.height);
    
    _textLabel.center = CGPointMake(_bubbleView.bounds.size.width * (_textNormalizationFrame.origin.x + _textNormalizationFrame.size.width / 2), _bubbleView.bounds.size.height * (_textNormalizationFrame.origin.y + _textNormalizationFrame.size.height / 2));
    _textLabel.bounds = CGRectMake(0, 0, _bubbleView.bounds.size.width * _textNormalizationFrame.size.width, _bubbleView.bounds.size.height * _textNormalizationFrame.size.height);
    
    
    _deleteBtn.center = CGPointMake(_borderView.ugckit_x, _borderView.ugckit_y);
    _deleteBtn.bounds = CGRectMake(0, 0, 50, 50);
    
    _styleBtn.center = CGPointMake(_borderView.ugckit_right, _borderView.ugckit_top);
    _styleBtn.bounds = CGRectMake(0, 0, 50, 50);
    
    _scaleRotateBtn.center = CGPointMake(_borderView.ugckit_right, _borderView.ugckit_bottom);
    _scaleRotateBtn.bounds = CGRectMake(0, 0, 50, 50);
    
    if (_hasSetBubble) {
        [self calculateTextLabelFont];
    }
}

- (void)calculateTextLabelFont
{
    CGRect rect = [_textLabel.text boundingRectWithSize:CGSizeMake(_textLabel.ugckit_width, MAXFLOAT) options:(NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading) attributes:@{NSFontAttributeName:_textLabel.font} context:nil];
    if (rect.size.height > _textLabel.ugckit_height) {
        _textLabel.font = [UIFont systemFontOfSize:_textLabel.font.pointSize - 0.1];
        [self calculateTextLabelFont];
    }
}

- (NSString*)text
{
    return _textLabel.text;
}

//生成字幕图片
- (UIImage*)textImage
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

//设置气泡
- (void)setTextBubbleImage:(UIImage *)image textNormalizationFrame:(CGRect)frame
{
    _bubbleView.image = image;
    if (image != nil) {
        _textNormalizationFrame = frame;
        [self calculateTextLabelFont];
        _hasSetBubble = YES;
    }else{
        _hasSetBubble = NO;
    }
    self.transform = CGAffineTransformRotate(self.transform, -_rotateAngle);
    _rotateAngle = 0;
}

//字幕图在视频预览view的frame
- (CGRect)textFrameOnView:(UIView *)view
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

- (CGRect)textRect
{
    CGRect rect = [_textLabel.text boundingRectWithSize:CGSizeMake(MAXFLOAT, 0) options:(NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading) attributes:@{NSFontAttributeName:_textLabel.font} context:nil];
    //限制最小的文字框大小
    if (rect.size.width < 30) {
        rect.size.width = 30;
    } else if (rect.size.height < 10) {
        rect.size.height = 10;
    }
    
    return rect;
}

- (void)resignFirstResponser
{
    if (!_isInputting)
        return;
    
    _isInputting = NO;
    [_inputTextView resignFirstResponder];
}

- (void)changeFirstResponder
{
    if (_isInputting) {
        _inputTextView.text = _textLabel.text;
        [_inputTextView becomeFirstResponder];
    } else {
        if (_hiddenTextField.isFirstResponder) {
            [_hiddenTextField resignFirstResponder];
        } else {
            [self resignFirstResponder];
            [_hiddenTextField resignFirstResponder];
        }
    }
}

#pragma mark - GestureRecognizer handle
- (void)onTap:(UITapGestureRecognizer*)recognizer
{
    _isInputting = YES;
    [_hiddenTextField becomeFirstResponder];
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
        
        if (recognizer.state == UIGestureRecognizerStateChanged) {
            //放大
            CGFloat delta = translation.x / 10;
            CGFloat newFontSize = MAX(10.0f, MIN(150.f, _textLabel.font.pointSize + delta));
            _textLabel.font = [UIFont systemFontOfSize:newFontSize];
            if (!_hasSetBubble) {
                _textLabel.bounds = [self textRect];
                self.bounds = CGRectMake(0, 0, _textLabel.bounds.size.width + 50, _textLabel.bounds.size.height + 40);
            }else{
                self.bounds = CGRectMake(0, 0, self.bounds.size.width + translation.x, self.bounds.size.height + translation.x);
            }

            //旋转
            CGPoint newCenter = CGPointMake(recognizer.view.center.x + translation.x, recognizer.view.center.y + translation.y);
            CGPoint anthorPoint = _textLabel.center;
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
        [recognizer setTranslation:CGPointZero inView:self];
    }
    
}

//双手指放大
- (void)handlePinch:(UIPinchGestureRecognizer*)recognizer
{
    CGFloat newFontSize = MAX(10.0f, MIN(150.f, _textLabel.font.pointSize * recognizer.scale));
    // set font size
    _textLabel.font = [_textLabel.font fontWithSize:newFontSize];
    if (!_hasSetBubble) {
        
        CGRect rect = [self textRect];
        rect = [_textLabel convertRect:rect toView:self];
        
        _textLabel.bounds = CGRectMake(0, 0, rect.size.width, rect.size.height);
        self.bounds = CGRectMake(0, 0, _textLabel.bounds.size.width + 50, _textLabel.bounds.size.height + 40);
    }else{
        self.bounds = CGRectMake(0, 0, self.bounds.size.width * recognizer.scale, self.bounds.size.height * recognizer.scale);
    }

    recognizer.scale = 1;
}

//双手指旋转
- (void)handleRotate:(UIRotationGestureRecognizer*)recognizer
{
    recognizer.view.transform = CGAffineTransformRotate(recognizer.view.transform, recognizer.rotation);

    _rotateAngle += recognizer.rotation;
    recognizer.rotation = 0;
}


#pragma mark - UI event handle
- (void)onInputConfirmBtnClicked:(UIButton*)sender
{
    _isInputting = NO;
    [_inputTextView resignFirstResponder];
    [_hiddenTextField resignFirstResponder];
    
    _textLabel.text = _inputTextView.text;
    
    if (!_hasSetBubble) {
        _textLabel.bounds = [self textRect];
        self.bounds = CGRectMake(0, 0, _textLabel.bounds.size.width + 50, _textLabel.bounds.size.height + 40);
    }else{
        [self calculateTextLabelFont];
    }
    
    [self.delegate onTextInputDone:_textLabel.text];
}

-(void)textViewBeginChangeValue{
    [self.delegate onTextInputBegin];;
}

-(void)textViewDidChangeValue:(NSNotification *)obj{
    UITextView *textView = (UITextView *)obj.object;
    NSString *string = textView.text;
    if(string.length > 0){
        [_inputConfirmBtn setEnabled:YES];
    }else{
        [_inputConfirmBtn setEnabled:NO];
    }
}

- (void)onDeleteBtnClicked:(UIButton*)sender
{
    [self.delegate onRemoveTextField:self];
    [self removeFromSuperview];
}

- (void)onStyleBtnClicked:(UIButton*)sender
{
    [self.delegate onBubbleTap];
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end

