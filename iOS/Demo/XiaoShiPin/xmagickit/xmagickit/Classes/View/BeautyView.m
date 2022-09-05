//
//  BeautyView.m
//  PituMotionDemo
//
//  Created by xingyunmao on 2021/2/3.
//  Copyright © 2021 Pitu. All rights reserved.
//

#import "BeautyView.h"
#import "BeautyViewModel.h"
#import "FilterCollectionViewCell.h"
//#import "BeautyCellModel.h"
#import <Masonry/Masonry.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import <AVFoundation/AVFoundation.h>
#import "TCDownloadManager.h"
#import "XmagicResDownload.h"
#import "XmagicKitTheme.h"
#import "XMagic.h"

#define beautyCollectionHeight 100
#define SUB_THIN_FACE 101  //瘦脸子菜单
#define SUB_LIP 102  //口红子菜单
#define SUB_CHEEK 103  //腮红子菜单
#define SUB_DIMENSION 104  //立体子菜单
#define SUB_MENU_2D 201  //2d动效菜单
#define SUB_MENU_3D 202  //3d动效菜单
#define SUB_MENU_HAND 203  //手势动效菜单
#define SUB_MENU_GAN 204  //趣味动效菜单
// 屏幕的宽
#define ScreenWidth                         [[UIScreen mainScreen] bounds].size.width
// 屏幕的高
#define ScreenHeight                        [[UIScreen mainScreen] bounds].size.height
// 视频长度限制(ms)
static const int MAX_SEG_VIDEO_DURATION = 200 * 1000;
static const int MAX_SEG_IAMGE_WIDHT = 2160;
static const int MAX_SEG_IAMGE_HEIGHT = 3840;
typedef void(^DownloadCallback)(bool download);

@interface BeautyView ()<UICollectionViewDelegate,UICollectionViewDataSource,UIImagePickerControllerDelegate>

//控制数值的滑杆
@property (strong, nonatomic) UISlider *commonSlider; // 控制数值的滑杆
@property (strong, nonatomic) UILabel *valueLabel; // 滑杆左侧数值标签
@property (strong, nonatomic) UICollectionView *beautyCollection; // 美颜选择栏
@property (strong, nonatomic) UIButton *beautyBurron; // 屏幕底部的美颜标签
@property (strong, nonatomic) UIView *bottomView; // 底部view
@property (strong, nonatomic) BeautyCellModel* segId; // 分割素材
@property (strong, nonatomic) BeautyViewModel* model;
@property (strong, nonatomic) NSString* segPath;
@property (strong, nonatomic) NSNumber* timeOffset;
@property (strong, nonatomic) NSIndexPath *lastIndexPath;
@property (nonatomic) int segType;
@property (nonatomic) bool isOhterTabChange;
//功能类别
@property(nonatomic,copy) NSArray *tabTitles;
@property int tabTag;

@property (assign, nonatomic) XmagicKitTheme *theme;
@property (nonatomic, weak) XMagic *beautyKitRef;

@property (nonatomic) int showProcess;
@property (strong, nonatomic) UIView *loadingCover;
@property (strong, nonatomic) UILabel *processLabel;
@property (strong, nonatomic) UIActivityIndicatorView *loadingView;
@end



@implementation BeautyView
- (instancetype)init{
    if (self = [super init]) {
        _theme = [XmagicKitTheme sharedTheme];
        _model = [[BeautyViewModel alloc]init];
        [self setupData];
        [self setupUI];
        [self setupView];
    }
    return self;
}

- (void)setXMagic:(id)xmagic{
    self.beautyKitRef = xmagic;
}

-(void)setupUI {
    self.tabTitles = @[
    @" ",
    [_theme localizedString:@"xmagic_pannel_tab1"],
    [_theme localizedString:@"xmagic_pannel_tab3"],
    [_theme localizedString:@"xmagic_pannel_tab4"],
    [_theme localizedString:@"xmagic_pannel_tab5"],
    [_theme localizedString:@"xmagic_pannel_tab6"],
    @" "];
    UIView* view = [[UIView alloc]init];
    view.backgroundColor = UIColor.blackColor;
    view.alpha = 0.7f;
    [self addSubview:view];
    [view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(self.mas_width);
        make.left.mas_equalTo(self.mas_left);
        make.top.mas_equalTo(self.mas_top).mas_offset(80);
        make.height.mas_equalTo(beautyCollectionHeight);
    }];
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    [layout setScrollDirection:UICollectionViewScrollDirectionHorizontal];
    layout.itemSize =CGSizeMake(60, beautyCollectionHeight-20);
    self.beautyCollection = [[UICollectionView alloc] initWithFrame:self.bounds collectionViewLayout:layout];
    self.beautyCollection.backgroundColor = [UIColor clearColor];
    [self addSubview:self.beautyCollection];
    self.beautyCollection.dataSource = self;
    self.beautyCollection.delegate = self;
    self.beautyCollection.scrollEnabled = YES;
    [self.beautyCollection registerClass:[FilterCollectionViewCell class] forCellWithReuseIdentifier:@"filterCell"];
    [self.beautyCollection mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(self.mas_width);
        make.left.mas_equalTo(self.mas_left);
        make.top.mas_equalTo(self.mas_top).mas_offset(80);
        make.height.mas_equalTo(beautyCollectionHeight);
    }];
    
    self.bottomView = [[UIView alloc] init];
    self.bottomView.backgroundColor = UIColor.blackColor;
    self.bottomView.alpha = 0.7f;
    [self addSubview:self.bottomView];
    [self.bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.beautyCollection.mas_bottom);
    }];
    
    self.valueLabel = [[UILabel alloc]init];
    [self.valueLabel setTextColor:[UIColor whiteColor]];
    [self addSubview:self.valueLabel];
    [self.valueLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self).mas_offset(40);
        make.top.mas_equalTo(self).mas_offset(-30);
        make.width.mas_equalTo(42);
        make.bottom.mas_equalTo(self.beautyCollection.mas_top).mas_offset(-4.5);
    }];
    self.commonSlider = [[UISlider alloc]init];
    [self.commonSlider setTintColor:[UIColor systemPinkColor]];
    [self.commonSlider addTarget:self action:@selector(valueChange:) forControlEvents:UIControlEventValueChanged];
    [self.commonSlider setThumbImage:[UIImage imageNamed:@"SliderThumbIcon"] forState:UIControlStateNormal];
    [self addSubview:self.commonSlider];
    [self.commonSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.valueLabel.mas_right).mas_offset(40);
        make.right.mas_equalTo(self).mas_offset(-40);
        make.top.mas_equalTo(self).mas_offset(-30);
        make.bottom.mas_equalTo(self.beautyCollection.mas_top);
    }];
    [self addTabButtons];
    self.isBackTo = false;
}

- (void)collectionView:(UICollectionView *)collectionView willDisplayCell:(UICollectionViewCell *)cell forItemAtIndexPath:(NSIndexPath *)indexPath NS_AVAILABLE_IOS(8_0){
    if(_model.sortType == 0){
        self.beau_offset = collectionView.contentOffset;
    }else if (_model.sortType == 1){
        self.lut_offset = collectionView.contentOffset;
    }else if (_model.sortType == 2){
        self.motion_offset = collectionView.contentOffset;
    }else if (_model.sortType == 3){
        self.meiz_offset = collectionView.contentOffset;
    }else if (_model.sortType == 4){
        _isOhterTabChange = true;
        self.seg_offset = collectionView.contentOffset;
    }else if (_model.sortType == SUB_MENU_2D){
        self.menu2d_offset = collectionView.contentOffset;
    }else if (_model.sortType == SUB_MENU_3D){
        self.menu3d_offset = collectionView.contentOffset;
    }else if (_model.sortType == SUB_MENU_HAND){
        self.menuhand_offset = collectionView.contentOffset;
    }
};
/**
 * 添加分类按钮
 */
- (void)addTabButtons{
    CGFloat btnWidth = 40;
    CGFloat btnHeight = 40;
    for (int i = 0; i < self.tabTitles.count; i ++) {
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        btn.backgroundColor = [UIColor blackColor];
        btn.alpha = 0.7;
        [btn setTitle:self.tabTitles[i] forState:UIControlStateNormal];
        btn.frame = CGRectMake(ScreenWidth*i*0.15, 40, ScreenWidth*0.15, btnHeight);
        btn.titleLabel.font = [UIFont systemFontOfSize:18];
        btn.titleLabel.textColor=[UIColor whiteColor];
        btn.tag = 2000 + i;
        if (i > 0 && i < 6) {
            [btn addTarget:self action:@selector(onSetAction:) forControlEvents:UIControlEventTouchUpInside];
        }
        [self addSubview:btn];
        if (i==1) {
            [btn sendActionsForControlEvents:UIControlEventTouchUpInside];
        }
    }
}
/**
 * 分类按钮事件
 */
- (void)onSetAction:(UIButton *)sender{
    self.tabTag = sender.tag;
    for (UIButton *btn in self.subviews) {
        if (btn.tag == self.tabTag) {
            btn.titleLabel.textColor = [UIColor redColor];
            btn.titleLabel.font = [UIFont fontWithName:@"Helvetica-Bold" size:20];
            [btn setTitleColor:[UIColor redColor]forState:UIControlStateNormal];
            self.isBackTo = true;
            [self sortByType:btn.tag-2001];
            self.isBackTo = false;
        }else if(btn.tag >= 2000 && btn.tag <= 2007){
            btn.titleLabel.font = [UIFont systemFontOfSize:18];
            btn.titleLabel.textColor=[UIColor whiteColor];
            [btn setTitleColor:[UIColor whiteColor]forState:UIControlStateNormal];
        }
    }
}
/**
 * 添加分类子菜单按钮
 */
- (void)addSubButtons:(NSString *)name{
    CGFloat btnWidth = 40;
    CGFloat btnHeight = 40;
    for (int i = 0; i < 3; i ++) {
        if(i == 0){
            UIImageView  *imageView=[[UIImageView alloc] initWithFrame:CGRectMake(0, 40, ScreenWidth*0.2, btnHeight)];
//            [NSString stringWithFormat:@"%@/backto.png", self.theme.resourcePath]
            [imageView setImage:[UIImage imageWithContentsOfFile:[NSString stringWithFormat:@"%@/backto.png", self.theme.resourcePath]]];
            [imageView setBackgroundColor:[UIColor blackColor]];
            imageView.alpha = 0.7;
            imageView.tag = 3000 + i;
            
            [self addSubview:imageView];
            imageView.contentMode = UIViewContentModeScaleAspectFit;
            UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(clickBack)];
            [imageView addGestureRecognizer:tapGesture];
            imageView.userInteractionEnabled = YES;
            continue;
        }
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        btn.backgroundColor = [UIColor blackColor];
        btn.alpha = 0.7;
        if(i == 1){
            [btn setTitle:name forState:UIControlStateNormal];
            btn.frame = CGRectMake(ScreenWidth*0.2, 40, ScreenWidth*0.6, btnHeight);
        }else{
            [btn setTitle:@" " forState:UIControlStateNormal];
            btn.frame = CGRectMake(ScreenWidth*0.8, 40, ScreenWidth*0.2, btnHeight);
        }
        btn.titleLabel.font = [UIFont systemFontOfSize:18];
        btn.titleLabel.textColor=[UIColor whiteColor];
        btn.tag = 3000 + i;
        [self addSubview:btn];
    }
}
- (void)clickBack{
    self.isBackTo = true;
    if(_model.sortType < 200){
        [self sortByType:0];
    }else if (_model.sortType < 300){
        [self sortByType:2];
    }
    [self onButtonHidden:nil andHidden:false];
    [self onSubButtonHidden:nil andHidden:true];
    self.isBackTo = false;
}
- (void)onButtonHidden:(NSString *)name andHidden:(BOOL)hiden{
    for (UIView *btn in self.subviews) {
        //隐藏一级分类
        if(btn.tag >= 2000 && btn.tag <= 2007){
            btn.hidden = hiden;
        }
    }
    if (hiden) {
        [self addSubButtons:name];
    }
    
}
- (void)onSubButtonHidden:(NSString *)name andHidden:(BOOL)hiden{
    for (UIButton *btn in self.subviews) {
        if(btn.tag >= 3000 && btn.tag <= 3007){
            [btn removeFromSuperview];
        }
    }
}
-(void)setupView{
    _commonSlider.minimumValue = 0.f;
    _commonSlider.maximumValue = 1.f;
    
    _model.beautySelectedIndex = _model.beautySelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    _model.lutSelectedIndex = _model.lutSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    _model.motionSelectedIndex = _model.motionSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    _model.beautySegSelectedIndex = _model.beautySegSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    _model.makeupSelectedIndex = _model.makeupSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    
    _model.beautyThinFaceSelectedIndex = _model.beautyThinFaceSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    
    _model.beautylipSelectedIndex = _model.beautylipSelectedIndex?:[NSIndexPath indexPathForRow:-1 inSection:-1];
    _model.beautyCheekSelectedIndex = _model.beautyCheekSelectedIndex?:[NSIndexPath indexPathForRow:-1 inSection:-1];
    
    _model.beautyDimensionSelectedIndex = _model.beautyDimensionSelectedIndex?:[NSIndexPath indexPathForRow:-1 inSection:-1];
    
    _model.motion2DMenuSelectedIndex = _model.motion2DMenuSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    _model.motion3DMenuSelectedIndex = _model.motion3DMenuSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    _model.motionHandMenuSelectedIndex = _model.motionHandMenuSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    _model.motionGanMenuSelectedIndex = _model.motionGanMenuSelectedIndex?:[NSIndexPath indexPathForRow:0 inSection:0];
    
    [self.beautyCollection selectItemAtIndexPath:_model.beautySelectedIndex animated:NO scrollPosition:(UICollectionViewScrollPositionNone)];
    
    [self collectionView:self.beautyCollection didSelectItemAtIndexPath:_model.beautySelectedIndex];
}

-(void)setupData{
    [_model setupData];
    //    [self updateAllBeautyValue];
}

//如果动效中带有形变，则美容中的形变无效
- (void)enableBeautyCell:(BOOL)isEnable{
    _model.basicFaceEnable = isEnable;
    [_beautyCollection reloadData];
}

- (void)hide:(int)type
{
    if (type == 0) {
        self.commonSlider.hidden = NO;
        self.valueLabel.hidden = NO;
        self.beautyCollection.hidden = NO;
        self.beautyBurron.hidden = NO;
        self.bottomView.hidden = NO;
    } else if (type == 1 || type == 3) {
        self.commonSlider.hidden = NO;
        self.valueLabel.hidden = NO;
        self.beautyCollection.hidden = YES;
        self.beautyBurron.hidden = YES;
        self.bottomView.hidden = YES;
    } else if (type == 2 || type == 4) {
        self.commonSlider.hidden = YES;
        self.valueLabel.hidden = YES;
        self.beautyCollection.hidden = YES;
        self.beautyBurron.hidden = YES;
        self.bottomView.hidden = YES;
    }
}
- (void)intoSubMenu:(int)index{
    NSIndexPath *previousIndex = [NSIndexPath indexPathForRow:0 inSection:0];
    if (index == 0) {
        previousIndex = _model.beautySelectedIndex;
    }else if (index == SUB_THIN_FACE){
        previousIndex = _model.beautyThinFaceSelectedIndex;
    }else if (index == SUB_LIP){
        previousIndex = _model.beautylipSelectedIndex;
    }else if (index == SUB_CHEEK){
        previousIndex = _model.beautyCheekSelectedIndex;
    }else if (index == SUB_DIMENSION){
        previousIndex = _model.beautyDimensionSelectedIndex;
    }else if (index == SUB_MENU_2D){
        previousIndex = _model.motion2DMenuSelectedIndex;
    }else if (index == SUB_MENU_3D){
        previousIndex = _model.motion3DMenuSelectedIndex;
    }else if (index == SUB_MENU_HAND){
        previousIndex = _model.motionHandMenuSelectedIndex;
    }else if (index == SUB_MENU_GAN){
        previousIndex = _model.motionGanMenuSelectedIndex;
    }
    [_model sortByType:index];
    
    
    [self.beautyCollection reloadData];
    [self collectionView:self.beautyCollection didSelectItemAtIndexPath:previousIndex];
    [self.beautyCollection selectItemAtIndexPath:previousIndex animated:NO scrollPosition:(UICollectionViewScrollPositionNone)];
}

- (void)sortByType:(int)index {
    NSIndexPath *previousIndex = [NSIndexPath indexPathForRow:0 inSection:0];
    CGPoint point;
    // NSInteger index = [_segmentControl selectedSegmentIndex];
    if (index == 0) {
        previousIndex = _model.beautySelectedIndex;
        point = self.beau_offset;
    } else if (index == 1) {
        previousIndex = _model.lutSelectedIndex;
        point = self.lut_offset;
    } else if (index == 2) {
        previousIndex = _model.motionSelectedIndex;
        point = self.motion_offset;
    } else if (index == 3) {
        previousIndex = _model.makeupSelectedIndex;
        point = self.meiz_offset;
    }else if (index == 4) {
        previousIndex = _model.beautySegSelectedIndex;
        point = self.seg_offset;
    }else if (index == SUB_MENU_2D){
        previousIndex = _model.motion2DMenuSelectedIndex;
        point = self.menu2d_offset;
    }else if (index == SUB_MENU_3D){
        previousIndex = _model.motion3DMenuSelectedIndex;
        point = self.menu3d_offset;
    }else if (index == SUB_MENU_HAND){
        previousIndex = _model.motionHandMenuSelectedIndex;
        point = self.menuhand_offset;
    }
    [_model sortByType:index];
    if (index == 2 || index == 4) {
        self.commonSlider.hidden = YES;
        self.valueLabel.hidden = YES;
    } else {
        self.commonSlider.hidden = NO;
        self.valueLabel.hidden = NO;
    }
    [self.beautyCollection reloadData];
    [self collectionView:self.beautyCollection didSelectItemAtIndexPath:previousIndex];
    [self.beautyCollection selectItemAtIndexPath:previousIndex animated:NO scrollPosition:(UICollectionViewScrollPositionNone)];
    [self.beautyCollection setContentOffset:point animated:NO];
}



#pragma <UICollectionViewDelegate,UICollectionViewDataSource>
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath{
    if (_model.sortType == 0) {
        [self selectBeauty:indexPath];
    }else if (_model.sortType == 1) {
        [self selectLut:indexPath];
    }else if (_model.sortType == 2) {
        [self selectMotion:indexPath];
    }else if (_model.sortType == 3) {
        [self selectMakeup:indexPath];
    }else if (_model.sortType == 4) {
        [self selectseg:indexPath];
    }else if (_model.sortType == SUB_THIN_FACE) {
        [self selectThinFace:indexPath];
    }else if (_model.sortType == SUB_LIP) {
        [self selectLip:indexPath];
    }else if (_model.sortType == SUB_CHEEK) {
        [self selectCheek:indexPath];
    }else if (_model.sortType == SUB_DIMENSION) {
        [self selectDimension:indexPath];
    }else if(_model.sortType == SUB_MENU_2D){
        [self select2D:indexPath];
    }else if(_model.sortType == SUB_MENU_3D){
        [self select3D:indexPath];
    }else if(_model.sortType == SUB_MENU_HAND){
        [self selectHand:indexPath];
    }else if(_model.sortType == SUB_MENU_GAN){
        [self selectGan:indexPath];
    }
    
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section{
    NSInteger result = 0;
    if (_model.sortType == 0) {
        result = _model.beautyIDs.count;
    } else if (_model.sortType == 1) {
        result = _model.lutIDs.count;
    } else if (_model.sortType == 2) {
        result = _model.motionIDs.count;
    }else if (_model.sortType == 3) {
        result = _model.makeupIDS.count;
    }else if (_model.sortType == 4) {
        result = _model.beautySegIDS.count;
    }else if (_model.sortType == SUB_THIN_FACE){
        result = _model.beautyThinFaceIDs.count;
    }else if (_model.sortType == SUB_LIP){
        result = _model.beautylipIDs.count;
    }else if (_model.sortType == SUB_CHEEK){
        result = _model.beautyCheekIDs.count;
    }else if (_model.sortType == SUB_DIMENSION){
        result = _model.beautyDimensionIDs.count;
    }else if (_model.sortType == SUB_MENU_2D){
        result = _model.motion2DMenuIDS.count;
    }else if (_model.sortType == SUB_MENU_3D){
        result = _model.motion3DMenuIDS.count;
    }else if (_model.sortType == SUB_MENU_HAND){
        result = _model.motionHandMenuIDS.count;
    }else if (_model.sortType == SUB_MENU_GAN){
        result = _model.motionGanMenuIDS.count;
    }
    return result;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    FilterCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"filterCell" forIndexPath:indexPath];
    
    FilterCellModel* model = nil;
    if (_model.sortType == 0) {
        [cell setAlpha:0.7f];
        model = (FilterCellModel*)_model.beautyIDs[indexPath.item];
    } else if (_model.sortType == 1) {
        [cell setAlpha:1.0f];
        model = (FilterCellModel*)_model.lutIDs[indexPath.item];
    } else if (_model.sortType == 2) {
        [cell setAlpha:0.7f];
        model = (FilterCellModel*)_model.motionIDs[indexPath.item];
    }else if (_model.sortType == 3) {
        [cell setAlpha:1.0f];
        model = (FilterCellModel*)_model.makeupIDS[indexPath.item];
    }else if (_model.sortType == 4) {
        [cell setAlpha:1.0f];
        model = (FilterCellModel*)_model.beautySegIDS[indexPath.item];
    }else if (_model.sortType == SUB_THIN_FACE) {
        [cell setAlpha:0.7f];
        model = (FilterCellModel*)_model.beautyThinFaceIDs[indexPath.item];
    }else if (_model.sortType == SUB_LIP) {
        [cell setAlpha:0.7f];
        model = (FilterCellModel*)_model.beautylipIDs[indexPath.item];
    }else if (_model.sortType == SUB_CHEEK) {
        [cell setAlpha:0.7f];
        model = (FilterCellModel*)_model.beautyCheekIDs[indexPath.item];
    }else if (_model.sortType == SUB_DIMENSION) {
        [cell setAlpha:0.7f];
        model = (FilterCellModel*)_model.beautyDimensionIDs[indexPath.item];
    }else if (_model.sortType == SUB_MENU_2D) {
        [cell setAlpha:1.0f];
        model = (FilterCellModel*)_model.motion2DMenuIDS[indexPath.item];
    }else if (_model.sortType == SUB_MENU_3D) {
        [cell setAlpha:1.0f];
        model = (FilterCellModel*)_model.motion3DMenuIDS[indexPath.item];
    }else if (_model.sortType == SUB_MENU_HAND) {
        [cell setAlpha:1.0f];
        model = (FilterCellModel*)_model.motionHandMenuIDS[indexPath.item];
    }else if (_model.sortType == SUB_MENU_GAN) {
        [cell setAlpha:1.0f];
        model = (FilterCellModel*)_model.motionGanMenuIDS[indexPath.item];
    }
    cell.filterModel = model;
    
    cell.userInteractionEnabled = true;

    if(!_model.basicFaceEnable && indexPath.item == 1 ){
        cell.userInteractionEnabled = false;
        [cell setAlpha:0.5f];
    }
    return cell;
}

- (void)selectBeauty:(NSIndexPath *)indexPath{
    [_commonSlider setMaximumValue:100];
    [_commonSlider setMinimumValue:0];
    NSInteger idx = indexPath.item;
    if(idx == 12){
        [self intoSubMenu:SUB_LIP];
        [self onButtonHidden:@"口红" andHidden:true];
        return;
    }
    if(idx == 13){
        [self intoSubMenu:SUB_CHEEK];
        [self onButtonHidden:@"腮红" andHidden:true];
        return;
    }
    if(idx == 14){
        [self intoSubMenu:SUB_DIMENSION];
        [self onButtonHidden:@"立体" andHidden:true];
        return;
    }
    if(idx == 7){
        [self intoSubMenu:SUB_THIN_FACE];
        [self onButtonHidden:@"瘦脸" andHidden:true];
        return;
    }
    _model.beautySelectedIndex = indexPath;
    [_commonSlider setValue: [_model.beautyIDs[idx] beautyValue].floatValue];
    if (self.beautyKitRef != nil) {
        YTBeautyPropertyInfo * info = [self.beautyKitRef getConfigPropertyWithName:[_model.beautyIDs[idx] key]];
        if (info != nil && info.maxValue != nil && info.minValue != nil) {
            [_commonSlider setMaximumValue:[info.maxValue floatValue]];
            [_commonSlider setMinimumValue:[info.minValue floatValue]];
        }
    }
    [self setLabelValue:_commonSlider.value];
    [self updateCurrentBeautyIndex:idx value:[_model.beautyIDs[idx] beautyValue].floatValue extraConfig:[_model.beautyIDs[idx] extraConfig]];
}

- (void)selectLut:(NSIndexPath *)indexPath{
    [_commonSlider setMaximumValue:100];
    [_commonSlider setMinimumValue:0];
    
    _model.lutSelectedIndex = indexPath;
    NSInteger idx = _model.lutSelectedIndex.item;
    if (idx == 0) {
        _commonSlider.hidden = self.valueLabel.hidden = YES;
    } else {
        _commonSlider.hidden = self.valueLabel.hidden = NO;
    }
    [_commonSlider setValue: [_model.lutIDs[idx] strength].floatValue];
    [self setLabelValue:_commonSlider.value];
    [self updateCurrentBeautyIndex:idx value:[_model.lutIDs[idx] strength].floatValue extraConfig:nil];
}

- (void)selectMotion:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if(idx == 1){
        if (!self.isBackTo) {
            [self intoSubMenu:SUB_MENU_2D];
            [self onButtonHidden:@"2D动效" andHidden:true];
        }
  
        _model.motionSelectedIndex = indexPath;
        return;
    }
    if(idx == 2){
        if (!self.isBackTo) {
            [self intoSubMenu:SUB_MENU_3D];
            [self onButtonHidden:@"3D动效" andHidden:true];
        }
      
        _model.motionSelectedIndex = indexPath;
        return;
    }
    if(idx == 3){
        if (!self.isBackTo) {
            [self intoSubMenu:SUB_MENU_HAND];
            [self onButtonHidden:@"手势动效" andHidden:true];
        }
     
       _model.motionSelectedIndex = indexPath;
        return;
    }
    if(idx == 4){
        if (!self.isBackTo) {
            [self intoSubMenu:SUB_MENU_GAN];
            [self onButtonHidden:@"趣味" andHidden:true];
        }
     
       _model.motionSelectedIndex = indexPath;
        return;
    }
    if (self.itemSelectedBlock != nil) {
        self.itemSelectedBlock();
    }
    _model.motionSelectedIndex = indexPath;
    [self updateCurrentBeautyIndex:idx value:0 extraConfig:nil];
}

- (void)selectMakeup:(NSIndexPath *)indexPath{
    if (self.itemSelectedBlock != nil) {
        self.itemSelectedBlock();
    }
    _model.makeupSelectedIndex = indexPath;
    [_commonSlider setMaximumValue:100];
    [_commonSlider setMinimumValue:0];
    
    NSInteger idx = _model.makeupSelectedIndex.item;
    if (idx == 0) {
        _commonSlider.hidden = self.valueLabel.hidden = YES;
    } else {
        _commonSlider.hidden = self.valueLabel.hidden = NO;
    }
    
    [_commonSlider setValue: [_model.makeupIDS[idx] strength].floatValue];
    [self setLabelValue:_commonSlider.value];
    [self updateCurrentBeautyIndex:idx value:0 extraConfig:nil];
}

- (void)selectseg:(NSIndexPath *)indexPath{
    if (self.itemSelectedBlock != nil) {
        self.itemSelectedBlock();
    }
    _lastIndexPath = _model.beautySegSelectedIndex;
    _model.beautySegSelectedIndex = indexPath;
    NSInteger idx = _model.beautySegSelectedIndex.item;
    [self updateCurrentBeautyIndex:idx value:0 extraConfig:[_model.beautySegIDS[idx] extraConfig]];
}

- (void)selectThinFace:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    _model.beautyThinFaceSelectedIndex = indexPath;
    [_commonSlider setValue: [_model.beautyThinFaceIDs[idx] beautyValue].floatValue];
    if (self.beautyKitRef != nil) {
        YTBeautyPropertyInfo * info = [self.beautyKitRef getConfigPropertyWithName:[_model.beautyThinFaceIDs[idx] key]];
        if (info != nil && info.maxValue != nil && info.minValue != nil) {
            [_commonSlider setMaximumValue:[info.maxValue floatValue]];
            [_commonSlider setMinimumValue:[info.minValue floatValue]];
        }
    }
    [self setLabelValue:_commonSlider.value];
    [self updateCurrentBeautyIndex:idx value:[_model.beautyThinFaceIDs[idx] beautyValue].floatValue extraConfig:[_model.beautyThinFaceIDs[idx] extraConfig]];
}

- (void)selectLip:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if (idx == -1) {
        self.commonSlider.hidden = YES;
        self.valueLabel.hidden = YES;
        return;
    }
    self.commonSlider.hidden = NO;
    self.valueLabel.hidden = NO;
    _model.beautylipSelectedIndex = indexPath;
    [_commonSlider setValue: [_model.beautylipIDs[idx] beautyValue].floatValue];
    if (self.beautyKitRef != nil) {
        YTBeautyPropertyInfo * info = [self.beautyKitRef getConfigPropertyWithName:[_model.beautylipIDs[idx] key]];
        if (info != nil && info.maxValue != nil && info.minValue != nil) {
            [_commonSlider setMaximumValue:[info.maxValue floatValue]];
            [_commonSlider setMinimumValue:[info.minValue floatValue]];
        }
    }
    [self setLabelValue:_commonSlider.value];
    [self updateCurrentBeautyIndex:idx value:[_model.beautylipIDs[idx] beautyValue].floatValue extraConfig:[_model.beautylipIDs[idx] extraConfig]];
}

- (void)selectCheek:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if (idx == -1) {
        self.commonSlider.hidden = YES;
        self.valueLabel.hidden = YES;
        return;
    }
    self.commonSlider.hidden = NO;
    self.valueLabel.hidden = NO;
    _model.beautyCheekSelectedIndex = indexPath;
    [_commonSlider setValue: [_model.beautyCheekIDs[idx] beautyValue].floatValue];
    if (self.beautyKitRef != nil) {
        YTBeautyPropertyInfo * info = [self.beautyKitRef getConfigPropertyWithName:[_model.beautyCheekIDs[idx] key]];
        if (info != nil && info.maxValue != nil && info.minValue != nil) {
            [_commonSlider setMaximumValue:[info.maxValue floatValue]];
            [_commonSlider setMinimumValue:[info.minValue floatValue]];
        }
    }
    [self setLabelValue:_commonSlider.value];
    [self updateCurrentBeautyIndex:idx value:[_model.beautyCheekIDs[idx] beautyValue].floatValue extraConfig:[_model.beautyCheekIDs[idx] extraConfig]];
}

- (void)selectDimension:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if (idx == -1) {
        self.commonSlider.hidden = YES;
        self.valueLabel.hidden = YES;
        return;
    }
    self.commonSlider.hidden = NO;
    self.valueLabel.hidden = NO;
    _model.beautyDimensionSelectedIndex = indexPath;
    [_commonSlider setValue: [_model.beautyDimensionIDs[idx] beautyValue].floatValue];
    if (self.beautyKitRef != nil) {
        YTBeautyPropertyInfo * info = [self.beautyKitRef getConfigPropertyWithName:[_model.beautyDimensionIDs[idx] key]];
        if (info != nil && info.maxValue != nil && info.minValue != nil) {
            [_commonSlider setMaximumValue:[info.maxValue floatValue]];
            [_commonSlider setMinimumValue:[info.minValue floatValue]];
        }
    }
    [self setLabelValue:_commonSlider.value];
    [self updateCurrentBeautyIndex:idx value:[_model.beautyDimensionIDs[idx] beautyValue].floatValue extraConfig:[_model.beautyDimensionIDs[idx] extraConfig]];
}

- (void)select2D:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if (self.itemSelectedBlock != nil) {
        self.itemSelectedBlock();
    }
    _model.motion2DMenuSelectedIndex = indexPath;
    [self updateCurrentBeautyIndex:idx value:0 extraConfig:nil];
}

- (void)select3D:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if (self.itemSelectedBlock != nil) {
        self.itemSelectedBlock();
    }
    _model.motion3DMenuSelectedIndex = indexPath;
    [self updateCurrentBeautyIndex:idx value:0 extraConfig:nil];
}

- (void)selectHand:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if (self.itemSelectedBlock != nil) {
        self.itemSelectedBlock();
    }
    _model.motionHandMenuSelectedIndex = indexPath;
    [self updateCurrentBeautyIndex:idx value:0 extraConfig:nil];
}

- (void)selectGan:(NSIndexPath *)indexPath{
    NSInteger idx = indexPath.item;
    if (self.itemSelectedBlock != nil) {
        self.itemSelectedBlock();
    }
    _model.motionGanMenuSelectedIndex = indexPath;
    [self updateCurrentBeautyIndex:idx value:0 extraConfig:nil];
}

- (void)valueChange:(id)sender {
    UISlider * slider =(UISlider*)sender;
    NSLog(@"sortType: %i", _model.sortType);
    NSLog(@"slider value: %f", [slider value]);
    if (_model.sortType == 0) {
        NSInteger index = _model.beautySelectedIndex.item;
        [self setLabelValue:[slider value]];
        if (slider.value == [_model.beautyIDs[index] beautyValue].floatValue) {
            return;
        }
        [_model.beautyIDs[index] setBeautyValue: @(slider.value)];
        [self updateCurrentBeautyIndex:index value:[_model.beautyIDs[index] beautyValue].floatValue extraConfig:[_model.beautyIDs[index] extraConfig]];
    } else if (_model.sortType == 1) {
        NSInteger index = _model.lutSelectedIndex.item;
        [self setLabelValue:[slider value]];
        if (slider.value == [_model.lutIDs[index] strength].floatValue) {
            return;
        }
        [_model.lutIDs[index] setStrength: @(slider.value)];
        [self updateCurrentBeautyIndex:index value:[_model.lutIDs[index] strength].floatValue extraConfig:nil];
    } else if (_model.sortType == 3) {
        NSInteger index = _model.makeupSelectedIndex.item;
        [self setLabelValue:[slider value]];
        if (slider.value == [_model.makeupIDS[index] strength].floatValue) {
            return;
        }
        [_model.makeupIDS[index] setStrength: @(slider.value)];
        [self.beautyKitRef configPropertyWithType:@"custom" withName:@"makeup.strength" withData:[NSString stringWithFormat:@"%.2f",[_model.makeupIDS[index] strength].floatValue] withExtraInfo:nil];
    } else if ( _model.sortType == SUB_THIN_FACE) {
        NSInteger index = _model.beautyThinFaceSelectedIndex.item;
        [self setLabelValue:[slider value]];
        if (slider.value == [_model.beautyThinFaceIDs[index] beautyValue].floatValue) {
            return;
        }
        [_model.beautyThinFaceIDs[index] setBeautyValue: @(slider.value)];
        [self updateCurrentBeautyIndex:index value:[_model.beautyThinFaceIDs[index] beautyValue].floatValue extraConfig:[_model.beautyThinFaceIDs[index] extraConfig]];
    }else if ( _model.sortType == SUB_LIP) {
        NSInteger index = _model.beautylipSelectedIndex.item;
        [self setLabelValue:[slider value]];
        if (slider.value == [_model.beautylipIDs[index] beautyValue].floatValue) {
            return;
        }
        [_model.beautylipIDs[index] setBeautyValue: @(slider.value)];
        [self updateCurrentBeautyIndex:index value:[_model.beautylipIDs[index] beautyValue].floatValue extraConfig:[_model.beautylipIDs[index] extraConfig]];
    }else if ( _model.sortType == SUB_CHEEK) {
        NSInteger index = _model.beautyCheekSelectedIndex.item;
        [self setLabelValue:[slider value]];
        if (slider.value == [_model.beautyCheekIDs[index] beautyValue].floatValue) {
            return;
        }
        [_model.beautyCheekIDs[index] setBeautyValue: @(slider.value)];
        [self updateCurrentBeautyIndex:index value:[_model.beautyCheekIDs[index] beautyValue].floatValue extraConfig:[_model.beautyCheekIDs[index] extraConfig]];
    }else if ( _model.sortType == SUB_DIMENSION) {
        NSInteger index = _model.beautyDimensionSelectedIndex.item;
        [self setLabelValue:[slider value]];
        if (slider.value == [_model.beautyDimensionIDs[index] beautyValue].floatValue) {
            return;
        }
        [_model.beautyDimensionIDs[index] setBeautyValue: @(slider.value)];
        [self updateCurrentBeautyIndex:index value:[_model.beautyDimensionIDs[index] beautyValue].floatValue extraConfig:[_model.beautyDimensionIDs[index] extraConfig]];
    }
}


- (void)setLabelValue:(CGFloat)value{
    [_valueLabel setText:[NSString stringWithFormat:@"%d",(int)value]];
}

- (void)updateAllBeautyValue{
    for (int i=0; i < _model.beautyIDs.count; i++) {
        NSString *key = [_model.beautyIDs[i] key];
        if (![key isEqualToString:@"beauty.v.face"]) {
            [self updateCurrentBeautyIndex:i value:[_model.beautyIDs[i] beautyValue].floatValue extraConfig:[_model.beautyIDs[i] extraConfig]];
        }
    }
    __weak __typeof(self)weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        __strong typeof(self) strongSelf = weakSelf;
        [strongSelf collectionView:strongSelf.beautyCollection didSelectItemAtIndexPath:strongSelf.model.beautySelectedIndex];
    });
}

- (void)updateCurrentBeautyIndex:(NSInteger) index value:(CGFloat)value extraConfig:(id)extraConfig {
    if (_model.sortType == 0) {
        NSString *key = [_model.beautyIDs[index] key];
        [self.beautyKitRef configPropertyWithType:@"beauty" withName:key withData:[NSString stringWithFormat:@"%f",value] withExtraInfo:extraConfig];
    } else if (_model.sortType == 1) {
        NSString *key = [_model.lutIDs[index] path];
        NSString *path = [@"lut.bundle/" stringByAppendingPathComponent:key];
        key = [self.theme.resourcePath stringByAppendingPathComponent:path];
        [self.beautyKitRef configPropertyWithType:@"lut" withName:key withData:[NSString stringWithFormat:@"%f",value] withExtraInfo:nil];
    } else if (_model.sortType == 2) {
        NSString *key = [_model.motionIDs[index] key];
        NSString *path = [_model.motionIDs[index] path];
        NSString *motionRootPath = path==nil?[[NSBundle mainBundle] pathForResource:@"MotionRes" ofType:@"bundle"]:path;
        [self.beautyKitRef configPropertyWithType:@"motion" withName:key withData:motionRootPath withExtraInfo:nil];
    }
    else if (_model.sortType == 3) {
        NSString *key = [_model.makeupIDS[index] key];
        [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:nil];
        [self downloadResConfigBeauty:@"custom" key:@"makeup.strength" value:[NSString stringWithFormat:@"%.2f", [_model.makeupIDS[index] strength].floatValue] extraConfig:nil];
    }else if (_model.sortType == 4) {
        BeautyCellModel *segId = _model.beautySegIDS[index];
        if([segId.title isEqualToString:@"自定义背景"]){
            _segId = segId;
            if(_segPath != nil && _isOhterTabChange){
                _isOhterTabChange = false;
                NSString *key = [self.segId key];
                if(_segType == 0){
                    [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:@{@"bgName":_segPath, @"bgType":@0, @"timeOffset":@0}];
                }else {
                    [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:@{@"bgName": _segPath, @"bgType": @1, @"timeOffset": [NSNumber numberWithInt:_timeOffset]}];
                }
                return;
            }
            [self openImagePicker];
            return;
        }
        _segPath = nil;
        NSString *key = [_model.beautySegIDS[index] key];
        [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:extraConfig];
    }else if(_model.sortType == SUB_THIN_FACE){
        NSString *key = [_model.beautyThinFaceIDs[index] key];
        [self.beautyKitRef configPropertyWithType:@"beauty" withName:key withData:[NSString stringWithFormat:@"%f",value] withExtraInfo:extraConfig];
    }else if(_model.sortType == SUB_LIP){
        NSString *key = [_model.beautylipIDs[index] key];
        [self.beautyKitRef configPropertyWithType:@"beauty" withName:key withData:[NSString stringWithFormat:@"%f",value] withExtraInfo:extraConfig];
    }else if(_model.sortType == SUB_CHEEK){
        NSString *key = [_model.beautyCheekIDs[index] key];
        [self.beautyKitRef configPropertyWithType:@"beauty" withName:key withData:[NSString stringWithFormat:@"%f",value] withExtraInfo:extraConfig];
    }else if(_model.sortType == SUB_DIMENSION){
        NSString *key = [_model.beautyDimensionIDs[index] key];
        [self.beautyKitRef configPropertyWithType:@"beauty" withName:key withData:[NSString stringWithFormat:@"%f",value] withExtraInfo:extraConfig];
    }else if(_model.sortType == SUB_MENU_2D){
        
        NSString *key = [_model.motion2DMenuIDS[index] key];
        [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:extraConfig];
    }else if(_model.sortType == SUB_MENU_3D){
        
        NSString *key = [_model.motion3DMenuIDS[index] key];
        [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:extraConfig];
    }else if(_model.sortType == SUB_MENU_HAND){
        
        NSString *key = [_model.motionHandMenuIDS[index] key];
        [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:extraConfig];
    }else if(_model.sortType == SUB_MENU_GAN){
        
        NSString *key = [_model.motionGanMenuIDS[index] key];
        [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:extraConfig];
    }
    
    if (self.changedCurrentBeautyBlock != nil) {
        self.changedCurrentBeautyBlock();
    }
}

- (void)dismissAlert:(UIAlertController *)alert{
    [alert dismissViewControllerAnimated:YES completion:nil];
}


//打开相册
-(void) openImagePicker{
    UIImagePickerController *picker = [[UIImagePickerController alloc] init];
    //资源类型为图片库
    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    picker.mediaTypes =@[(NSString*)kUTTypeMovie, (NSString*)kUTTypeImage];
    picker.delegate = self;
    //设置选择后的图片可被编辑
    picker.allowsEditing = NO;
    [self.viewController presentViewController:picker animated:YES completion:nil];
}

//// 相册界面: 用户选择了一个文件, 获取到用户选择的文件
-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info {
    // 移除相册界面
    int errorCode = 0;
    [picker.view removeFromSuperview];
    // 获取文件类型:
    NSString *mediaType = info[UIImagePickerControllerMediaType];
    if ([mediaType isEqualToString:(NSString*)kUTTypeImage]) {
        // 用户选的文件为图片类型(kUTTypeImage)
        UIImage *image = info[UIImagePickerControllerOriginalImage];
        image = [self fixOrientation:image];
        NSData *data = UIImagePNGRepresentation(image);
        //返回为png图像。
        if (!data) {
            //返回为JPEG图像。
            data = UIImageJPEGRepresentation(image, 1.0);
        }
        NSString *imagePath = [self createImagePath:@"image.png"];
        [[NSFileManager defaultManager] createFileAtPath:imagePath contents:data attributes:nil];
        [picker dismissViewControllerAnimated:YES completion:nil];
        NSString *key = [_segId key];
        [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:@{@"bgName":imagePath, @"bgType":@0, @"timeOffset":@0}];
    }else if([mediaType isEqualToString:(NSString*)kUTTypeMovie]){
        NSURL *sourceURL = [info objectForKey:UIImagePickerControllerMediaURL];
        NSDateFormatter *formater = [[NSDateFormatter alloc] init];
        [formater setDateFormat:@"yyyy-MM-dd-HH.mm.ss"];
        NSURL *newVideoUrl = [NSURL fileURLWithPath:[NSHomeDirectory() stringByAppendingFormat:@"/Documents/output-%@.mp4", [formater stringFromDate:[NSDate date]]]];
        [picker dismissViewControllerAnimated:YES completion:nil];
        // 处理视频 压缩视频
        errorCode = [self convertVideoQuailtyWithInputURL:sourceURL outputURL:newVideoUrl completeHandler:nil];
        
    } else {
        if ([mediaType isEqualToString:(NSString*)kUTTypeImage]) {
            // 用户选的文件为图片类型(kUTTypeImage)
            UIImage *image = info[UIImagePickerControllerOriginalImage];
            CGSize imageSize = [image size];
            size_t width = imageSize.width;
            size_t height = imageSize.height;
            bool isVertical = width < height;
            if((isVertical && (width > MAX_SEG_IAMGE_WIDHT || height > MAX_SEG_IAMGE_HEIGHT)) ||
               (!isVertical && (width > MAX_SEG_IAMGE_HEIGHT || height > MAX_SEG_IAMGE_WIDHT)) ||
               width <= 0 || height <= 0) {
                errorCode = 5000;
                [picker dismissViewControllerAnimated:YES completion:nil];
            } else {
                image = [self fixOrientation:image];
                NSData *data = UIImagePNGRepresentation(image);
                //返回为png图像。
                if (!data) {
                    //返回为JPEG图像。
                    data = UIImageJPEGRepresentation(image, 1.0);
                }
                NSString *imagePath = [self createImagePath:@"image.png"];
                [[NSFileManager defaultManager] createFileAtPath:imagePath contents:data attributes:nil];
                [picker dismissViewControllerAnimated:YES completion:nil];
                NSString *key = [_segId key];
                [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:@{@"bgName":imagePath, @"bgType":@0, @"timeOffset":@0}];
            }
        }else if([mediaType isEqualToString:(NSString*)kUTTypeMovie]){
            NSURL *sourceURL = [info objectForKey:UIImagePickerControllerMediaURL];
            NSDateFormatter *formater = [[NSDateFormatter alloc] init];
            [formater setDateFormat:@"yyyy-MM-dd-HH.mm.ss"];
            NSURL *newVideoUrl = [NSURL fileURLWithPath:[NSHomeDirectory() stringByAppendingFormat:@"/Documents/output-%@.mp4", [formater stringFromDate:[NSDate date]]]];
            [picker dismissViewControllerAnimated:YES completion:nil];
            // 处理视频 压缩视频
            errorCode = [self convertVideoQuailtyWithInputURL:sourceURL outputURL:newVideoUrl completeHandler:nil];
            
        } else {
            errorCode = 5004;
        }
    }
    if (errorCode) {
        NSString* errorMsg = @"";
        switch (errorCode) {
        case 5000:
            errorMsg = @"分割背景图片分辨率超过2160*3840";
            break;
        case 5002:
            errorMsg = @"分割背景视频解析失败";
            break;
        case 5003:
            errorMsg = @"分割背景视频超过200秒";
            break;
        case 5004:
            errorMsg = @"分割背景视频格式不支持";
            break;
        default:
            break;
        }
        UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:@"背景导入失败" message:[NSString stringWithFormat:@"%i: %@", errorCode, errorMsg] preferredStyle:UIAlertControllerStyleAlert];
        [self.viewController presentViewController:alertVC animated:YES completion:nil];
        [self performSelector:@selector(dismissAlert:) withObject:alertVC afterDelay:2.0];
    }
}
// 取消图片选择回调
- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    if (_segPath == nil) {
        _model.beautySegSelectedIndex = _lastIndexPath;
        [self.beautyCollection reloadData];
        [self collectionView:self.beautyCollection didSelectItemAtIndexPath:_lastIndexPath];
        [self.beautyCollection selectItemAtIndexPath:_lastIndexPath animated:NO scrollPosition:(UICollectionViewScrollPositionNone)];
    }
    NSLog(@"取消");
}



// 视频压缩转码处理
- (int)convertVideoQuailtyWithInputURL:(NSURL*)inputURL
                              outputURL:(NSURL*)outputURL
                        completeHandler:(void (^)(AVAssetExportSession*))handler {
    AVURLAsset *avAsset = [AVURLAsset URLAssetWithURL:inputURL options:nil];
    CMTime videoTime = [avAsset duration];
    int timeOffset = ceil(1000 * videoTime.value / videoTime.timescale) - 10;
    if (timeOffset > MAX_SEG_VIDEO_DURATION) {
        NSLog(@"background video too long(limit %i)", MAX_SEG_VIDEO_DURATION);
        return 5003;
    }
    AVAssetExportSession *exportSession = [[AVAssetExportSession alloc] initWithAsset:avAsset presetName:AVAssetExportPresetMediumQuality];
    exportSession.outputURL = outputURL;
    exportSession.outputFileType = AVFileTypeMPEG4;
    exportSession.shouldOptimizeForNetworkUse= YES;
    [exportSession exportAsynchronouslyWithCompletionHandler:^(void) {
        switch (exportSession.status) {
            case AVAssetExportSessionStatusCancelled:
                NSLog(@"AVAssetExportSessionStatusCancelled");
                break;
            case AVAssetExportSessionStatusUnknown:
                NSLog(@"AVAssetExportSessionStatusUnknown");
                break;
            case AVAssetExportSessionStatusWaiting:
                NSLog(@"AVAssetExportSessionStatusWaiting");
                break;
            case AVAssetExportSessionStatusExporting:
                NSLog(@"AVAssetExportSessionStatusExporting");
                break;
            case AVAssetExportSessionStatusCompleted:{
                NSLog(@"AVAssetExportSessionStatusCompleted");
                NSString *key = [self.segId key];
                [self downloadResConfigBeauty:@"motion" key:key value:[[TCDownloadManager shareManager] getResPath] extraConfig:@{@"bgName":outputURL.path, @"bgType":@1, @"timeOffset":[NSNumber numberWithInt:timeOffset]}];
            }
                break;
            case AVAssetExportSessionStatusFailed:
                NSLog(@"AVAssetExportSessionStatusFailed");
                break;
        }
    }];
    if (exportSession.status == AVAssetExportSessionStatusFailed) {
        NSLog(@"background video export failed");
        return 5002;
    }
    return 0;
}

-(NSString *)createImagePath:(NSString *)fileName{
    //获取Documents文件夹目录
    NSArray *path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentPath = [path objectAtIndex:0];
    //指定新建文件夹路径
    NSString *imageDocPath = [documentPath stringByAppendingPathComponent:@"MediaFile"];
    //创建ImageFile文件夹
    [[NSFileManager defaultManager] createDirectoryAtPath:imageDocPath withIntermediateDirectories:YES attributes:nil error:nil];
    //保存图片的路径
    return [imageDocPath stringByAppendingPathComponent:fileName];
}

// UIImage固定方向UIImageOrientationUp
- (UIImage *)fixOrientation:(UIImage*)image {
 
    // No-op if the orientation is already correct
    if (image.imageOrientation == UIImageOrientationUp) return image;
 
    // We need to calculate the proper transformation to make the image upright.
    // We do it in 2 steps: Rotate if Left/Right/Down, and then flip if Mirrored.
    CGAffineTransform transform = CGAffineTransformIdentity;
 
    switch (image.imageOrientation) {
        case UIImageOrientationDown:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, image.size.width, image.size.height);
            transform = CGAffineTransformRotate(transform, M_PI);
            break;
 
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
            transform = CGAffineTransformTranslate(transform, image.size.width, 0);
            transform = CGAffineTransformRotate(transform, M_PI_2);
            break;
 
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, 0, image.size.height);
            transform = CGAffineTransformRotate(transform, -M_PI_2);
            break;
    }
 
    switch (image.imageOrientation) {
        case UIImageOrientationUpMirrored:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, image.size.width, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
 
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, image.size.height, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
    }
 
    // Now we draw the underlying CGImage into a new context, applying the transform
    // calculated above.
    CGContextRef ctx = CGBitmapContextCreate(NULL, image.size.width, image.size.height,
                                             CGImageGetBitsPerComponent(image.CGImage), 0,
                                             CGImageGetColorSpace(image.CGImage),
                                             CGImageGetBitmapInfo(image.CGImage));
    CGContextConcatCTM(ctx, transform);
    switch (image.imageOrientation) {
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            // Grr...
            CGContextDrawImage(ctx, CGRectMake(0,0,image.size.height,image.size.width), image.CGImage);
            break;
 
        default:
            CGContextDrawImage(ctx, CGRectMake(0,0,image.size.width,image.size.height), image.CGImage);
            break;
    }
 
    // And now we just create a new UIImage from the drawing context
    CGImageRef cgimg = CGBitmapContextCreateImage(ctx);
    UIImage *img = [UIImage imageWithCGImage:cgimg];
    CGContextRelease(ctx);
    CGImageRelease(cgimg);
    return img;
}

- (UIViewController *)getControllerFromView:(UIView *)view {
    // 遍历响应者链。返回第一个找到视图控制器
    UIResponder *responder = view;
    while ((responder = [responder nextResponder])){
        if ([responder isKindOfClass: [UIViewController class]]){
            return (UIViewController *)responder;
        }
    }
    // 如果没有找到则返回nil
    return nil;
}

- (void)downloadRes:(NSString *)name complet:(DownloadCallback)complet{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self showLoading];

    });
    [[XmagicResDownload shardManager] downloadItem:name process:^(float process) {
        self.showProcess = process * 100;
        self.processLabel.text = [NSString stringWithFormat:@"%d",self.showProcess];
        if (self.showProcess == 100) {
            self.processLabel.text = @"";
            [self dismissLoading];
        }
    } complete:^(bool complete) {
        complet(complete);
    }];
}

//下载动效资源并设置动效
-(void)downloadResConfigBeauty:(NSString *)type key:(NSString *)key value:(NSString *)value extraConfig:(id)extraConfig{
    if ([key isEqualToString:@"naught"] || [key isEqualToString:@"naught.png"] || [type isEqualToString:@"custom"]) {
        [self configBeauty:type key:key value:value withExtraInfo:extraConfig];
        return;
    }
    NSString *motionRootPath = [self getResPath:key];
    //判断动效资源是否存在，不存在就去下载
    if ([self getResState:motionRootPath]) {
        [self configBeauty:type key:key value:value withExtraInfo:extraConfig];
    }else{
        [self downloadRes:key complet:^(bool download) {
            //下载完成回调
            if (download) {
                //判断文件是否存在
                if ([self getResState:motionRootPath]){
                    [self configBeauty:type key:key value:value withExtraInfo:extraConfig];
                }else{
                    NSLog(@"下载或解压未完成");
                }
            }else{
                NSLog(@"下载或解压失败");
            }
        }];
    }
}

- (BOOL)getResState:(NSString *)path{
    return [[NSFileManager defaultManager] fileExistsAtPath:path];
}

-(NSString *)getResPath:(NSString *)name{
    return [NSString stringWithFormat:@"%@/%@",[[TCDownloadManager shareManager] getResPath],name];
}

-(void) configBeauty:(NSString *)type key:(NSString *)key value:(NSString *)value withExtraInfo:(id)extraConfig{
    [self.beautyKitRef configPropertyWithType:type withName:key withData:value withExtraInfo:extraConfig];
    if (self.changedCurrentBeautyBlock != nil) {
        self.changedCurrentBeautyBlock();
    }
}

- (UIView *)loadingCover {
    if (!_loadingCover) {
        _loadingCover = [UIView new];
        _loadingCover.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.1];
    }
    return _loadingCover;
}

- (UIActivityIndicatorView *) loadingView {
    if (!_loadingView) {
        _loadingView = [UIActivityIndicatorView new];
        _loadingView.color = [UIColor greenColor];
    }
    return  _loadingView;
}

- (UILabel *)processLabel {
    if (!_processLabel) {
        _processLabel = [UILabel new];
        _processLabel.textAlignment = NSTextAlignmentCenter;
        _processLabel.textColor = [UIColor greenColor];
    }
    return  _processLabel;
}

- (void)showLoading{
    UIViewController *curViewController = [self getControllerFromView:self];
    [curViewController.view addSubview:self.loadingCover];
    [self.loadingCover mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.height.mas_equalTo(curViewController.view);
        make.left.right.mas_equalTo(curViewController.view);
    }];
    [curViewController.view addSubview:self.loadingView];
    [self.loadingView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo((curViewController.view.bounds.size.height -30)/2);
        make.left.mas_equalTo((curViewController.view.bounds.size.width -30)/2);
        make.width.height.mas_equalTo(30);
    }];
    
    [curViewController.view addSubview:self.processLabel];
    [self.processLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.loadingView.mas_bottom).offset(3);
        make.left.mas_equalTo(self.loadingView.mas_left);
        make.width.mas_equalTo(30);
        make.height.mas_equalTo(30);
    }];
    
    [self.loadingView startAnimating];
}

-(void)dismissLoading{
    [self.loadingView stopAnimating];
    [self.loadingView removeFromSuperview];
    [self.processLabel removeFromSuperview];
    [self.loadingCover removeFromSuperview];
}
@end

