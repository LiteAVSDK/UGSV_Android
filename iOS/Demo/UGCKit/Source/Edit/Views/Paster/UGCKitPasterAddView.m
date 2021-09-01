// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKit_UIViewAdditions.h"
#import "UGCKitPasterAddView.h"
#import "UGCKitColorMacro.h"

@implementation UGCKitPasterQipaoInfo
@end

@implementation UGCKitPasterAnimateInfo
@end

@implementation UGCKitPasterStaticInfo
@end

@implementation UGCKitPasterAddView
{
    UIScrollView * _selectView;
    NSArray *      _pasterList;
    NSString *     _bundlePath;
    UIButton *     _animateBtn;
    UIButton *     _staticBtn;
    UIButton *     _qipaoBtn;
    UIButton *     _closeBtn;
    UGCKitPasterType     _pasterType;
    UGCKitPasterType     _lastPasterType;//上一次选择的贴纸类型，分为静态贴纸和动态贴纸
    UGCKitTheme *  _theme;
}

- (instancetype) initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme
{
    self = [super initWithFrame:frame];
    if (self) {
        _theme = theme;
        _lastPasterType = UGCKitPasterType_Animate;//默认贴纸是动态
        CGFloat btnWidth = 100 * kScaleX;
        CGFloat btnHeight = 46 * kScaleY;
        _animateBtn = [[UIButton alloc] initWithFrame:CGRectMake(self.ugckit_width / 2 -  btnWidth, 0 , btnWidth, btnHeight)];
        [_animateBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
        [_animateBtn setTitle:[_theme localizedString:@"UGCKit.Edit.Paster.Dynamic"] forState:UIControlStateNormal];
        [_animateBtn sizeToFit];
        _animateBtn.ugckit_left = self.ugckit_width / 2 - _animateBtn.ugckit_width - 2;
        [_animateBtn addTarget:self action:@selector(onAnimateBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_animateBtn];
        
        _staticBtn = [[UIButton alloc] initWithFrame:CGRectMake(self.ugckit_width / 2, 0 , btnWidth, btnHeight)];
        [_staticBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_staticBtn setTitle:[_theme localizedString:@"UGCKit.Edit.Paster.Static"] forState:UIControlStateNormal];
        [_staticBtn sizeToFit];
        _staticBtn.ugckit_left = self.ugckit_width / 2 + 2;
        [_staticBtn addTarget:self action:@selector(onStaticBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_staticBtn];
        
        _closeBtn = [[UIButton alloc] initWithFrame:CGRectMake(self.ugckit_width - 45, 8 , 30, 30)];
        [_closeBtn setImage:theme.editPasterDeleteIcon forState:UIControlStateNormal];
//        [_closeBtn setImage:[UIImage imageNamed:@"closePaster_press"] forState:UIControlStateHighlighted];
        [_closeBtn addTarget:self action:@selector(onClose) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_closeBtn];
        
        UIView *lineView = [[UIView alloc] initWithFrame:CGRectMake(0, 46 * kScaleY, self.ugckit_width, 1)];
        lineView.backgroundColor = RGB(53, 59, 72);
        [self addSubview:lineView];
        
        _qipaoBtn = [[UIButton alloc] initWithFrame:CGRectMake(self.ugckit_width / 2 - btnWidth / 2, 0 , btnWidth, btnHeight)];
        [_qipaoBtn setTitleColor:UIColorFromRGB(0x0accac) forState:UIControlStateNormal];
        [_qipaoBtn setTitle:[_theme localizedString:@"UGCKit.Edit.Paster.ChooseBubbleSub"] forState:UIControlStateNormal];
        [_qipaoBtn sizeToFit];
        _qipaoBtn.frame = CGRectMake(self.ugckit_width / 2 - _qipaoBtn.ugckit_width / 2, 0 , _qipaoBtn.ugckit_width, btnHeight);
        _qipaoBtn.titleLabel.adjustsFontSizeToFitWidth = YES;
        [_qipaoBtn addTarget:self action:@selector(onQipaoBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_qipaoBtn];
        
        _selectView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, lineView.ugckit_bottom + 10 * kScaleY, self.ugckit_width, self.ugckit_height - lineView.ugckit_bottom)];
        [self addSubview:_selectView];
        
        self.backgroundColor = UIColorFromRGB(0x1F2531);
    }
    return self;
}

- (void) setUGCKitPasterType:(UGCKitPasterTtemType)pasterTtemType
{
    //外部和内部状态转化,外部数据源只有两种，内部有3种
    if (pasterTtemType == UGCKitPasterTtemType_Qipao) {
        _pasterType = UGCKitPasterType_Qipao;
    }else if (pasterTtemType == UGCKitPasterTtemType_Paster) {//如果选择贴纸，要给他赋值为上次显示的是静态还是动态
        _pasterType = _lastPasterType;
    }
    
    if (_pasterType == UGCKitPasterType_Animate || _pasterType == UGCKitPasterType_static) {
        _animateBtn.hidden = NO;
        _staticBtn.hidden = NO;
        _qipaoBtn.hidden = YES;
    }else{
        _animateBtn.hidden = YES;
        _staticBtn.hidden = YES;
        _qipaoBtn.hidden = NO;
    }
    [self reloadSelectView];
}

- (void)onAnimateBtnClicked:(UIButton *)btn
{
    [_animateBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [_staticBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    _pasterType = UGCKitPasterType_Animate;
    _lastPasterType = _pasterType;//保留上一次选择的贴纸类型，分为静态或者动态
    [self reloadSelectView];
}

- (void)onStaticBtnClicked:(UIButton *)btn
{
    [_animateBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [_staticBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    _pasterType = UGCKitPasterType_static;
    _lastPasterType = _pasterType;//保留上一次选择的贴纸类型，分为静态或者动态
    [self reloadSelectView];
}

- (void)onQipaoBtnClicked:(UIButton *)btn
{
    _pasterType = UGCKitPasterType_Qipao;
    [self reloadSelectView];
}

-(void)onClose
{
    self.hidden = YES;
}

- (void)reloadSelectView;
{
    switch (_pasterType) {
        case UGCKitPasterType_Animate:
        {
            _bundlePath = [_theme.resourceBundle pathForResource:@"AnimatedPaster" ofType:@"bundle"];
        }
            break;
            
        case UGCKitPasterType_static:
        {
            _bundlePath = [_theme.resourceBundle pathForResource:@"Paster" ofType:@"bundle"];
        }
            break;
            
        case UGCKitPasterType_Qipao:
        {
            _bundlePath = [_theme.resourceBundle pathForResource:@"bubbleText" ofType:@"bundle"];
        }
            break;
        default:
            break;
    }
    NSString *jsonString = [NSString stringWithContentsOfFile:[_bundlePath stringByAppendingPathComponent:@"config.json"] encoding:NSUTF8StringEncoding error:nil];
    NSDictionary *dic = [self dictionaryWithJsonString:jsonString];
    _pasterList = dic[@"pasterList"];
    
    int column = 4;  //默认4列
    CGFloat btnWidth = 70 * kScaleX;
    CGFloat space =  (self.ugckit_width - btnWidth *column) / (column + 1);
    _selectView.contentSize = CGSizeMake(self.ugckit_width, (_pasterList.count + 3) / 4 * (btnWidth + space));
    [_selectView.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
    for (int i = 0; i < _pasterList.count; i ++) {
        NSString *qipaoIconPath = [_bundlePath stringByAppendingPathComponent:_pasterList[i][@"icon"]];
        UIImage *qipaoIconImage = [UIImage imageWithContentsOfFile:qipaoIconPath];
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        [btn setFrame:CGRectMake(space + i % column  * (btnWidth + space),space +  i / column  * (btnWidth + space), btnWidth, btnWidth)];
        [btn setImage:qipaoIconImage forState:UIControlStateNormal];
        [btn addTarget:self action:@selector(selectBubble:) forControlEvents:UIControlEventTouchUpInside];
        btn.tag = i;
        [_selectView addSubview:btn];
    }
}

- (void)selectBubble:(UIButton *)btn
{
    switch (_pasterType) {
        case UGCKitPasterType_Qipao:
        {
            NSString *qipaoPath = [_bundlePath stringByAppendingPathComponent:_pasterList[btn.tag][@"name"]];
            NSString *jsonString = [NSString stringWithContentsOfFile:[qipaoPath stringByAppendingPathComponent:@"config.json"] encoding:NSUTF8StringEncoding error:nil];
            NSDictionary *dic = [self dictionaryWithJsonString:jsonString];
            
            UGCKitPasterQipaoInfo *info = [UGCKitPasterQipaoInfo new];
            info.image = [UIImage imageNamed:[qipaoPath stringByAppendingPathComponent:dic[@"name"]]];
            info.width = [dic[@"width"] floatValue];
            info.height = [dic[@"height"] floatValue];
            info.textTop = [dic[@"textTop"] floatValue];
            info.textLeft = [dic[@"textLeft"] floatValue];
            info.textRight = [dic[@"textRight"] floatValue];
            info.textBottom = [dic[@"textBottom"] floatValue];
            info.iconImage = btn.imageView.image;
            if (self.delegate && [self.delegate respondsToSelector:@selector(onPasterQipaoSelect:)]) {
                [self.delegate onPasterQipaoSelect:info];
            }
        }
            break;
            
        case UGCKitPasterType_Animate:
        {
            NSString *pasterPath = [_bundlePath stringByAppendingPathComponent:_pasterList[btn.tag][@"name"]];
            NSString *jsonString = [NSString stringWithContentsOfFile:[pasterPath stringByAppendingPathComponent:@"config.json"] encoding:NSUTF8StringEncoding error:nil];
            NSDictionary *dic = [self dictionaryWithJsonString:jsonString];
            
            NSArray *imagePathList = dic[@"frameArray"];
            NSMutableArray *imageList = [NSMutableArray array];
            for (NSDictionary *dic in imagePathList) {
                NSString *imageName = dic[@"picture"];
                UIImage *image = [UIImage imageNamed:[pasterPath stringByAppendingPathComponent:imageName]];
                [imageList addObject:image];
            }
            
            UGCKitPasterAnimateInfo *info = [UGCKitPasterAnimateInfo new];
            info.imageList = imageList;
            info.path = pasterPath;
            info.width = [dic[@"width"] floatValue];
            info.height = [dic[@"height"] floatValue];
            info.duration = [dic[@"period"] floatValue] / 1000.0;
            info.iconImage = btn.imageView.image;
            if (self.delegate && [self.delegate respondsToSelector:@selector(onPasterAnimateSelect:)]) {
                [self.delegate onPasterAnimateSelect:info];
            }
        }
            break;
            
        case UGCKitPasterType_static:
        {
            NSString *pasterPath = [_bundlePath stringByAppendingPathComponent:_pasterList[btn.tag][@"name"]];
            NSString *jsonString = [NSString stringWithContentsOfFile:[pasterPath stringByAppendingPathComponent:@"config.json"] encoding:NSUTF8StringEncoding error:nil];
            NSDictionary *dic = [self dictionaryWithJsonString:jsonString];
            
            UGCKitPasterStaticInfo *info = [UGCKitPasterStaticInfo new];
            info.image = [UIImage imageNamed:[pasterPath stringByAppendingPathComponent:dic[@"name"]]];
            info.width = [dic[@"width"] floatValue];
            info.height = [dic[@"height"] floatValue];
            info.iconImage = btn.imageView.image;
            if (self.delegate && [self.delegate respondsToSelector:@selector(onPasterStaticSelect:)]) {
                [self.delegate onPasterStaticSelect:info];
            }
        }
            break;
            
        default:
            break;
    }
    self.hidden = YES;
}

- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err) {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}
@end
