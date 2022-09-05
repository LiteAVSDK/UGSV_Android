//
//  TCUserInfoController.m
//  TCLVBIMDemo
//
//  Created by jemilyzhou on 16/8/1.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCAccountInfoViewController.h"
#import "TCEditUserInfoViewController.h"
#import "TCUserInfoCell.h"
#import "TCUserInfoModel.h"
#import "TCLoginModel.h"
#import "TCConstants.h"
#import <UIKit/UIKit.h>
#import <mach/mach.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import "TCWebViewController.h"
#import "TCUtil.h"
#import "AppDelegate.h"
#import "SDKHeader.h"
#import "ColorMacro.h"
#import "UIView+Additions.h"
#import "UIImageView+WebCache.h"
#import "AboutViewController.h"
#import "SettingViewController.h"
#import "TCAvatarListCell.h"
#import "HUDHelper.h"
#import "TCLoginViewController.h"
static NSString * const HomePageURL = @"https://cloud.tencent.com/product/ugsv";
#define L(X) NSLocalizedString((X), nil)


extern BOOL g_bNeedEnterPushSettingView;

@interface TCAccountInfoViewController () < UIPickerViewDataSource,
UIPickerViewDelegate,UICollectionViewDelegate,UICollectionViewDataSource,
UITextFieldDelegate>
/*
 * TCAccountInfoViewController 类说明 : 该类显示用户信息的界面
 */
@property (nonatomic, strong) UIView      *userInfoView; //用户信息
@property (nonatomic, strong) UIImageView *faceImage;  //头像
@property (nonatomic, strong) UILabel     *nickText;  //昵称
@property (nonatomic, strong) UILabel     *identifierText;  //ID
@property (nonatomic, strong) NSString    *nickName; //昵称
@property (nonatomic, strong) NSString    *identifier;  //ID
@property (nonatomic, strong) UIView      *coverView;  //coverView
@property (nonatomic, strong) UIView      *nameCoverView; //nameCoverView
@property (nonatomic, strong) UIView      *contentView; //contentView
@property (nonatomic, strong) UIView      *nameContentView; //nameContentView
@property (nonatomic, strong) UILabel     *avatarLabel; //头像Label
@property (nonatomic, strong) UILabel     *nickNameLabel; //昵称lable
@property (nonatomic, strong) UILabel     *nickNameTipsLabel;  //昵称tipsLable
@property (nonatomic, strong) UIButton    *avatarBtn;  //头像btn
@property (nonatomic, strong) UIButton    *nickNameBtn;  //昵称btn
@property (nonatomic, strong) UITextField *textField;  //输入昵称
@property(nonatomic, strong) UICollectionView *collectionView;//头像collectionView
@property(nonatomic, strong) NSArray      *avatarList; //头像list
@property NSInteger                       selectedIndex; //被选择的index
@property CGFloat                         position; //position
@property CGFloat                         endPosition; //endPosition
@property(nonatomic, strong) UIAlertController *alertController; //alertController
//
//@property (nonatomic, strong) UIPickerView *logPickerView;
//
//@property (nonatomic, strong) UILongPressGestureRecognizer *longPressGesture;

@end

@implementation TCAccountInfoViewController
{
    UIButton *_loginBtn;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:KReloadUserInfoNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:KClearUserInfoNotification object:nil];
}

/**
 *  用于点击 退出登录 按钮后的回调,用于登录出原界面
 *
 *  @param sender 无意义
 */
- (void)logout:(id)sender
{
    [[TCLoginModel sharedInstance] logout:^{
        [[TCLoginParam shareInstance] clearLocal];
        [self checkOpen];
        [_loginBtn setTitle:NSLocalizedString(@"TCLoginView.Login", nil) forState: UIControlStateNormal];
        _nickText.text  = @"";
        _identifierText.text  = @"";
        [_faceImage sd_setImageWithURL:nil placeholderImage:[UIImage imageNamed:@"default_user"]];
    }];
}

-(void)clearData{
    [[TCLoginParam shareInstance] clearLocal];
    [_loginBtn setTitle:NSLocalizedString(@"TCLoginView.Login", nil) forState: UIControlStateNormal];
    _nickText.text  = @"";
    _identifierText.text  = @"";
    [_faceImage sd_setImageWithURL:nil placeholderImage:[UIImage imageNamed:@"default_user"]];
    TCLoginViewController *next = [[TCLoginViewController alloc] init];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // 设置通知消息,接受到通知后重绘cell,确保更改后的用户资料能同步到用户信息界面
    [[NSNotificationCenter defaultCenter] removeObserver:self name:KReloadUserInfoNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateUserInfoOnController:) name:KReloadUserInfoNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:KClearUserInfoNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
    selector:@selector(clearUserInfoOnController:)
    name:KClearUserInfoNotification object:nil];
    //设置两个通知
    [[NSNotificationCenter defaultCenter]addObserver:self
    selector:@selector(keyHiden:)
    name: UIKeyboardWillHideNotification object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self
    selector:@selector(keyWillAppear:)
    name:UIKeyboardWillChangeFrameNotification object:nil];
    
    [self initUI];
    [self initAvatarList];
    [self checkOpen];
    return;
}

-(void)initUI
{
    _selectedIndex = -1;
    UIView *viewBack=[[UIView alloc] init];
    viewBack.frame = self.view.frame;
    viewBack.backgroundColor= RGB(0x18, 0x1D, 0x27);
    [self.view addSubview:viewBack];
    
    // 初始化需要绘制在tableview上的数据
    __weak typeof(self) ws = self;
    TCUserInfoCellItem *backFaceItem = [[TCUserInfoCellItem alloc] initWith:
    @"" value:@"" type:TCUserInfo_View rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    [ws onEditUserInfo:menu cell:cell];nil; }];
    
    TCUserInfoCellItem *settingItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAccountInfo.setting", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    [ws onClickSetting:menu cell:cell]; } ];
    
    TCUserInfoCellItem *privacyItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAccountInfo.privacyPolicy", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    [ws gotoprivace:menu cell:cell]; } ];
    
    TCUserInfoCellItem *agreementItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAccountInfo.userAgreement", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    [ws gotoWeb:menu cell:cell]; } ];
    
    TCUserInfoCellItem *infolistItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAccountInfo.infolist", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    [ws gotoInfolist:menu cell:cell]; } ];
    
    TCUserInfoCellItem *sharelistItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAccountInfo.sharelist", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    [ws gotoSharelist:menu cell:cell]; } ];
    
    TCUserInfoCellItem *aboutItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAccountInfo.about", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    [ws gotoAbout:menu cell:cell]; } ];

    
    CGFloat tableHeight = CGRectGetHeight(self.view.bounds);
    CGFloat quitBtnYSpace = tableHeight + 20;
    //    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[backFaceItem,setItem, aboutItem]];
    
    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[backFaceItem,settingItem,privacyItem,agreementItem,infolistItem,sharelistItem,aboutItem]];
    
    //设置tableview属性
    CGRect frame = CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y, self.view.frame.size.width, tableHeight);
    _dataTable = [[UITableView alloc] initWithFrame:frame style:UITableViewStylePlain];
    _dataTable.backgroundColor = UIColor.clearColor;
    [_dataTable setDelegate:self];
    [_dataTable setDataSource:self];
    [_dataTable setScrollEnabled:NO];
    [_dataTable setSeparatorColor:[UIColor clearColor]];
    [self.view addSubview:_dataTable];
    
    //计算退出登录按钮的位置和显示
    _loginBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    _loginBtn.frame = CGRectMake(0, quitBtnYSpace, self.view.frame.size.width, 45);
    _loginBtn.backgroundColor = [UIColor whiteColor];
    _loginBtn.titleLabel.font = [UIFont systemFontOfSize:16];
    [_loginBtn setTitle:NSLocalizedString(@"TCLoginView.Login", nil) forState: UIControlStateNormal];
    [_loginBtn setTitleColor:RGB(0xFF,0x58,0x4C) forState:UIControlStateNormal];
    [_loginBtn setBackgroundColor:RGB(0x1F,0x25,0x31)];
    [_loginBtn addTarget:self action:@selector(logout:) forControlEvents:UIControlEventTouchUpInside];
    
    UIView *wrapper = [[UIView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.bounds), 75)];
    wrapper.backgroundColor = UIColor.clearColor;
    [wrapper addSubview:_loginBtn];
    _loginBtn.bottom = wrapper.height;
    _dataTable.tableFooterView = wrapper;
    
    _userInfoView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.frame), 275)];
    _userInfoView.backgroundColor = RGB(239,100,85);
    _userInfoView.userInteractionEnabled = YES;
    [self.view addSubview:_userInfoView];
    
    UIColor *uiBorderColor = RGB(239,100,85);
    _faceImage = [[UIImageView alloc ] init];
    _faceImage.layer.masksToBounds = YES;
    _faceImage.layer.borderWidth   = 2;
    _faceImage.layer.borderColor   = uiBorderColor.CGColor;
    _faceImage.userInteractionEnabled = YES;
    
    _nickText = [[UILabel alloc] init];
    _nickText.textAlignment = NSTextAlignmentCenter;
    _nickText.textColor     = [UIColor whiteColor];
    _nickText.font          = [UIFont systemFontOfSize:18];
    _nickText.lineBreakMode = NSLineBreakByWordWrapping;
    UITapGestureRecognizer * tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(openNickName)];
    [_nickText addGestureRecognizer:tapGesture];
    _nickText.userInteractionEnabled = YES;
    
    _identifierText = [[UILabel alloc] init];
    _identifierText.textColor     = [UIColor whiteColor];
    _identifierText.font          = [UIFont systemFontOfSize:14];
    _identifierText.textAlignment = NSTextAlignmentCenter;
    _identifierText.lineBreakMode = NSLineBreakByWordWrapping;
    
    _nickName =  [[TCUserInfoModel sharedInstance] getUserProfile].nickName;
    _identifier = [TCLoginParam shareInstance].identifier;
    
    
    CGRect mainScreenSize = [ UIScreen mainScreen ].bounds;
    
    _faceImage.frame = CGRectMake((mainScreenSize.size.width-100)/2, 50,100, 100);
    _faceImage.layer.cornerRadius = 50;
    if ((NSNull *)[[TCUserInfoModel sharedInstance] getUserProfile].faceURL  == [NSNull null]) {
        [[TCUserInfoModel sharedInstance] getUserProfile].faceURL = nil;
    }
    [_faceImage sd_setImageWithURL:[NSURL URLWithString:[[TCUserInfoModel sharedInstance] getUserProfile].faceURL]
    placeholderImage:[UIImage imageNamed:@"default_user"]];
    [_faceImage addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(openPopView)]];
    
    if(_identifier != nil && (NSNull *)_identifier != [NSNull null]){
        CGSize titleTextSize  = [_identifier sizeWithAttributes:@{NSFontAttributeName:_identifierText.font}];
        _identifierText.text  = [NSString stringWithFormat:@"ID:%@",_identifier];
        _identifierText.frame = CGRectMake(0, 175+10+titleTextSize.height,mainScreenSize.size.width, titleTextSize.height);
        
        if(_nickName != nil && (NSNull *)_nickName != [NSNull null]){
            _nickText.attributedText = [self getNickNameAttrStr:_nickName];
            CGSize titleTextSize  = [_nickName sizeWithAttributes:@{NSFontAttributeName:_nickText.font}];
            _nickText.frame = CGRectMake(0, 175,mainScreenSize.size.width,titleTextSize.height);
        }else{
            _nickName = @"null";
            _nickText.attributedText = [self getNickNameAttrStr:_nickName];
            CGSize titleTextSize  = [_nickName sizeWithAttributes:@{NSFontAttributeName:_nickText.font}];
            _nickText.frame = CGRectMake(0, 175,mainScreenSize.size.width,titleTextSize.height);
        }
        [_faceImage sd_setImageWithURL:[NSURL URLWithString:
        [[TCUserInfoModel sharedInstance] getUserProfile].faceURL]
        placeholderImage:[UIImage imageNamed:@"default_user"]];
    }else{
        [_faceImage sd_setImageWithURL:nil placeholderImage:[UIImage imageNamed:@"default_user"]];
    }
    
    [self.view addSubview:_nickText];
    [self.view addSubview:_identifierText];
    [self.view addSubview:_faceImage];

}

-(void)openPopView{
    if([TCLoginParam shareInstance].isExpired){
        return;
    }
    self.tabBarController.tabBar.hidden = YES;
    _coverView = [UIView new];
    _coverView.frame = self.view.bounds;
    _coverView.backgroundColor = [UIColor blackColor];
    _coverView.alpha = 0.5;
    _coverView.userInteractionEnabled = YES;
    [_coverView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closePopView)]];
    [self.view addSubview:_coverView];
    
    _contentView = [UIView new];
    _contentView.backgroundColor = [UIColor blackColor];
    _contentView.frame = CGRectMake(0, 173, [UIScreen mainScreen].bounds.size.width,
                                   [UIScreen mainScreen].bounds.size.height);
    
    UIBezierPath *cornerRadiusPath = [UIBezierPath
    bezierPathWithRoundedRect:_contentView.bounds
    byRoundingCorners:UIRectCornerTopRight | UIRectCornerTopLeft
    cornerRadii:CGSizeMake(15, 15)];
    CAShapeLayer *cornerRadiusLayer = [ [CAShapeLayer alloc ] init];
    cornerRadiusLayer.frame = _contentView.bounds;
    cornerRadiusLayer.path = cornerRadiusPath.CGPath;
    _contentView.layer.mask = cornerRadiusLayer;
    _contentView.userInteractionEnabled = YES;
    UIPanGestureRecognizer *panGes = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(move:)];
    [_contentView addGestureRecognizer:panGes];
    [self.view addSubview:_contentView];
    
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    [layout setScrollDirection:UICollectionViewScrollDirectionVertical];
    layout.scrollDirection = UICollectionViewScrollDirectionVertical;
    layout.itemSize = CGSizeMake(70, 70);
    _collectionView= [[UICollectionView alloc] initWithFrame:
    CGRectMake(10, 100, [UIScreen mainScreen].bounds.size.width - 20,[UIScreen mainScreen].bounds.size.height)
    collectionViewLayout:layout];
    _collectionView.backgroundColor = [UIColor blackColor];
    [_collectionView registerClass:[TCAvatarListCell class] forCellWithReuseIdentifier:@"TCAvatarListCell"];
    _collectionView.dataSource = self;
    _collectionView.delegate = self;
    [self.contentView addSubview:_collectionView];
    
    _avatarLabel = [[UILabel alloc] init];
    _avatarLabel.textColor     = [UIColor whiteColor];
    _avatarLabel.font          = [UIFont systemFontOfSize:24];
    _avatarLabel.frame = CGRectMake(20,20,160,50);
    _avatarLabel.text = NSLocalizedString(@"TCAccountInfo.setAvatar", nil);
    [self.contentView addSubview:_avatarLabel];
    
    _avatarBtn = [[UIButton alloc] init];
    _avatarBtn.frame = CGRectMake([[UIScreen mainScreen] bounds].size.width - 90, 20, 90, 50);
    [_avatarBtn setTitle:NSLocalizedString(@"TCAccountInfo.confirm", nil) forState:UIControlStateNormal];
    [_avatarBtn setTitleColor:[UIColor redColor]forState:UIControlStateNormal];
    _avatarBtn.titleLabel.font = [UIFont systemFontOfSize:20];
    [_avatarBtn addTarget:self action:@selector(changeAvatar) forControlEvents:UIControlEventTouchUpInside];
    [self.contentView addSubview:_avatarBtn];
    
}


-(void) openNickName{
    if([TCLoginParam shareInstance].isExpired){
        return;
    }
    self.tabBarController.tabBar.hidden = YES;
    _nameCoverView = [UIView new];
    _nameCoverView.frame = self.view.bounds;
    _nameCoverView.backgroundColor = [UIColor blackColor];
    _nameCoverView.alpha = 0.5;
    _nameCoverView.userInteractionEnabled = YES;
    [_nameCoverView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeNickNameView)]];
    [self.view addSubview:_nameCoverView];
    
    _nameContentView = [UIView new];
    _nameContentView.backgroundColor = [UIColor blackColor];
    _nameContentView.frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - 260,
                                        [UIScreen mainScreen].bounds.size.width,260);
    
    UIBezierPath *cornerRadiusPath = [UIBezierPath
    bezierPathWithRoundedRect:_nameContentView.bounds
    byRoundingCorners:UIRectCornerTopRight | UIRectCornerTopLeft
    cornerRadii:CGSizeMake(15, 15)];
    CAShapeLayer *cornerRadiusLayer = [ [CAShapeLayer alloc ] init];
    cornerRadiusLayer.frame = _nameContentView.bounds;
    cornerRadiusLayer.path = cornerRadiusPath.CGPath;
    _nameContentView.layer.mask = cornerRadiusLayer;
    _nameContentView.userInteractionEnabled = YES;
    UITapGestureRecognizer * tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeKeyWindow)];
    [_nameContentView addGestureRecognizer:tapGesture];
    [self.view addSubview:_nameContentView];
    
    _nickNameLabel = [[UILabel alloc] init];
    _nickNameLabel.textColor     = [UIColor whiteColor];
    _nickNameLabel.font          = [UIFont systemFontOfSize:24];
    _nickNameLabel.frame = CGRectMake(20,20,240,50);
    _nickNameLabel.text = NSLocalizedString(@"TCAccountInfo.modifyUserNickname", nil);
    [self.nameContentView addSubview:_nickNameLabel];
    
    _nickNameBtn = [[UIButton alloc] init];
    _nickNameBtn.frame = CGRectMake([[UIScreen mainScreen] bounds].size.width - 90, 20, 90, 50);
    [_nickNameBtn setTitle:NSLocalizedString(@"TCAccountInfo.confirm", nil) forState:UIControlStateNormal];
    [_nickNameBtn setTitleColor:[UIColor redColor]forState:UIControlStateNormal];
    _nickNameBtn.titleLabel.font = [UIFont systemFontOfSize:20];
    [_nickNameBtn addTarget:self action:@selector(changeNickName) forControlEvents:UIControlEventTouchUpInside];
    [self.nameContentView addSubview:_nickNameBtn];
    
    _textField = [[UITextField alloc] init];
    _textField.frame = CGRectMake(30, 90, [[UIScreen mainScreen] bounds].size.width - 60, 70);
    _textField.backgroundColor = [UIColor whiteColor];
    _textField.placeholder = NSLocalizedString(@"TCAccountInfo.pleaseEnterUserNickname", nil);
    _textField.borderStyle = UITextBorderStyleRoundedRect;
    _textField.text = _nickName;
    _textField.layer.cornerRadius = 30;
    _textField.layer.masksToBounds = YES;
    _textField.leftView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 12, 0)];
    _textField.rightView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 12, 0)];    _textField.leftViewMode = UITextFieldViewModeAlways;
    [self.nameContentView addSubview:_textField];
    
    _nickNameTipsLabel = [[UILabel alloc] init];
    _nickNameTipsLabel.textColor     = [UIColor grayColor];
    _nickNameTipsLabel.font          = [UIFont systemFontOfSize:18];
    _nickNameTipsLabel.frame = CGRectMake(20,175,[[UIScreen mainScreen] bounds].size.width - 40,60);
    _nickNameTipsLabel.text = NSLocalizedString(@"TCAccountInfo.nickNameTips", nil);
    _nickNameTipsLabel.lineBreakMode = NSLineBreakByWordWrapping;
    _nickNameTipsLabel.numberOfLines = 0;
    [self.nameContentView addSubview:_nickNameTipsLabel];
}

-(void)closeKeyWindow{
    [[[UIApplication sharedApplication] keyWindow] endEditing:YES];
}

-(void)closeNickNameView{
    [self.nameCoverView removeFromSuperview];
    [self.nameContentView removeFromSuperview];
    self.tabBarController.tabBar.hidden = NO;
}

-(void)changeNickName{
    if (_textField.text.length < 2 || _textField.text.length > 20) {
        return;
    }
    [[TCUserInfoModel sharedInstance] saveUserNickName:_textField.text
                                           handler:^(int code, NSString *msg)
     {
         if (ERROR_SUCESS != code){
             [[HUDHelper sharedInstance] tipMessage:NSLocalizedString(@"TCEditUserInfoView.ErrorUploadingFace", nil)];
         }else{
             _nickName = _textField.text;
             _nickText.attributedText = [self getNickNameAttrStr:_nickName];
             [self closeNickNameView];
         }
     }];
}

-(void)changeAvatar{
    if (_selectedIndex > -1 && ![_avatarList[_selectedIndex]
        isEqualToString:[[TCUserInfoModel sharedInstance] getUserProfile].faceURL]) {
        [[TCUserInfoModel sharedInstance] saveUserFace:_avatarList[_selectedIndex]
                                               handler:^(int code, NSString *msg)
         {
             if (ERROR_SUCESS != code){
                 [[HUDHelper sharedInstance] tipMessage:NSLocalizedString(@"TCEditUserInfoView.ErrorUploadingFace", nil)];
             }else{
                 [_faceImage sd_setImageWithURL:[NSURL URLWithString:_avatarList[_selectedIndex]] placeholderImage:[UIImage imageNamed:@"default_user"]];
                 [self closePopView];
             }
         }];
    }

}

- (void)move:(UIPanGestureRecognizer *)sender {
    CGPoint pt = [sender translationInView:_contentView];
    if (sender.state == UIGestureRecognizerStateEnded) {
        if (_position > 173) {
            [self closePopView];
        }else{
            if (_endPosition > 0) {
                _contentView.frame = CGRectMake(0, 173, [UIScreen mainScreen].bounds.size.width,
                                               [UIScreen mainScreen].bounds.size.height);
            }else{
                _contentView.frame = CGRectMake(0, 72, [UIScreen mainScreen].bounds.size.width,
                                               [UIScreen mainScreen].bounds.size.height);
            }
        }
    
    }else if(sender.state == UIGestureRecognizerStateChanged){
        _position = _contentView.frame.origin.y;
        _endPosition = pt.y;
        if(_position > 84 || pt.y > 0){
            sender.view.center = CGPointMake(sender.view.center.x , sender.view.center.y+pt.y);
            //每次移动完，将移动量置为0，否则下次移动会加上这次移动量
            [sender setTranslation:CGPointMake(0, 0) inView:self.view];
        }
    }
   
}


#pragma mark-键盘出现隐藏事件
-(void)keyHiden:(NSNotification *)notification
{
    [UIView animateWithDuration:0.1 animations:^{
        //恢复原样
        _nameContentView.transform = CGAffineTransformIdentity;
    }];
    
    
}
-(void)keyWillAppear:(NSNotification *)notification
{
    //获得通知中的info字典
    NSDictionary *userInfo = [notification userInfo];
    CGRect rect= [[userInfo objectForKey:@"UIKeyboardFrameEndUserInfoKey"]CGRectValue];
    // self.tooBar.frame = rect;
    [UIView animateWithDuration:0.1 animations:^{
        _nameContentView.transform = CGAffineTransformMakeTranslation(0, -([UIScreen mainScreen].bounds.size.height-rect.origin.y));
    }];
}

-(void)closePopView{
    [self.coverView removeFromSuperview];
    [self.contentView removeFromSuperview];
    [self.collectionView removeFromSuperview];
    _selectedIndex = -1;
    self.tabBarController.tabBar.hidden = NO;
}

-(NSMutableAttributedString *) getNickNameAttrStr:(NSString *)text{
    NSMutableAttributedString * attrStr = [[NSMutableAttributedString alloc] initWithString:text];
    // 创建一个文字附件对象
    NSTextAttachment *textAttachment = [[NSTextAttachment alloc] init];
    textAttachment.image = [UIImage imageNamed:@"type.png"];  //设置图片源
    textAttachment.bounds = CGRectMake(3, 0, 15, 15);  //设置图片位置和大小
    // 将文字附件转换成属性字符串
    NSAttributedString *attachmentAttrStr = [NSAttributedString attributedStringWithAttachment:textAttachment];
    // 将转换成属性字符串插入到目标字符串
    [attrStr insertAttributedString:attachmentAttrStr atIndex:text.length];
    return attrStr;
}

#pragma mark 与view界面相关
-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES];
    if([TCLoginParam shareInstance].isExpired){
        [_loginBtn setTitle:NSLocalizedString(@"TCLoginView.Login", nil) forState: UIControlStateNormal];
    }else{
        [_loginBtn setTitle:NSLocalizedString(@"TCUserInfoView.Logout", nil) forState: UIControlStateNormal];
    }
}
/**
 *  用于接受头像下载成功后通知,因为用户可能因为网络情况下载头像很慢甚至失败数次,导致用户信息页面显示默认头像
 *  当用户头像下载成功后刷新tableview,使得头像信息得以更新
 *  另外如果用户在 编辑个人页面 修改头像或者修改昵称,也会发送通知,通知用户信息界面信息变更
 *
 *  @param notification 无意义
 */
-(void)updateUserInfoOnController:(NSNotification *)notification
{
    CGRect mainScreenSize = [ UIScreen mainScreen ].bounds;
    
    _nickName =  [[TCUserInfoModel sharedInstance] getUserProfile].nickName;
    _identifier = [[TCUserInfoModel sharedInstance] getUserProfile].identifier;
    
    if(_nickName != nil && (NSNull *)_nickName != [NSNull null]){
        _nickText.attributedText = [self getNickNameAttrStr:_nickName];
        CGSize titleTextSize  = [_nickName sizeWithAttributes:@{NSFontAttributeName:_nickText.font}];
        _nickText.frame = CGRectMake(0, 175,mainScreenSize.size.width,titleTextSize.height);
    }else{
        if (_identifier != nil && (NSNull *)_identifier != [NSNull null]) {
            _nickName = @"null";
            _nickText.attributedText = [self getNickNameAttrStr:_nickName];
            CGSize titleTextSize  = [_nickName sizeWithAttributes:@{NSFontAttributeName:_nickText.font}];
            _nickText.frame = CGRectMake(0, 175,mainScreenSize.size.width,titleTextSize.height);
        }else{
            _nickText.text  = @"";
        }
    }
    
    CGSize titleTextSize  = [_identifier sizeWithAttributes:@{NSFontAttributeName:_identifierText.font}];
    _identifierText.text  = [NSString stringWithFormat:@"ID:%@",_identifier];
    _identifierText.frame = CGRectMake(0, 175+10+titleTextSize.height,mainScreenSize.size.width, titleTextSize.height);
    if ((NSNull *)[[TCUserInfoModel sharedInstance] getUserProfile].faceURL  == [NSNull null]) {
        [[TCUserInfoModel sharedInstance] getUserProfile].faceURL = nil;
    }
    [_faceImage sd_setImageWithURL:[NSURL URLWithString:[[TCUserInfoModel sharedInstance] getUserProfile].faceURL]
    placeholderImage:[UIImage imageNamed:@"default_user"]];
    if([TCLoginParam shareInstance].isExpired){
        [_loginBtn setTitle:NSLocalizedString(@"TCLoginView.Login", nil) forState: UIControlStateNormal];
    }else{
        [_loginBtn setTitle:NSLocalizedString(@"TCUserInfoView.Logout", nil) forState: UIControlStateNormal];
    }
    [_dataTable reloadData];
}

-(void)clearUserInfoOnController:(NSNotification *)notification{
    [self clearData];
}

#pragma mark 绘制用户信息页面上的tableview
//获取需要绘制的cell数目
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return _userInfoUISetArry.count;
}
//获取需要绘制的cell高度
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    TCUserInfoCellItem *item = _userInfoUISetArry[indexPath.row];
    return [TCUserInfoCellItem heightOf:item];
}

//绘制Cell
-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    TCUserInfoCellItem *item = _userInfoUISetArry[indexPath.row];
    if (TCUserInfo_View == item.type) {
        // 用户信息
        TCUserInfoTableViewCell *cell = (TCUserInfoTableViewCell*)[tableView  dequeueReusableCellWithIdentifier:@"cell_userInfo"];
        if (cell == nil) {
            cell = [[TCUserInfoTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"cell_userInfo"];
            [cell initUserinfoViewCellData:item];
        }
        [cell drawRichCell:item];
        return cell;
    }else{
        // 关于小视频
        TCUserInfoTableViewCell *cell = (TCUserInfoTableViewCell*)[tableView  dequeueReusableCellWithIdentifier:@"cell_default"];
        if(cell == nil) {
            cell = [[TCUserInfoTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"cell_default"];
            [cell initUserinfoViewCellData:item];
        }
        [cell drawRichCell:item];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        UIImageView *accessoryImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow.png"]];
        cell.accessoryView = accessoryImgView;
        tableView.separatorInset = UIEdgeInsetsMake(0,0, 0, 0);
        tableView.separatorStyle =
        UITableViewCellSeparatorStyleSingleLine;
        
        tableView.separatorColor = [UIColor clearColor];
        return cell;
    }
}
#pragma mark 点击用户信息页面上的tableview的回调
/**
 *  用于点击tableview中的cell后的回调相应
 *
 *  @param tableView tableview变量
 *  @param indexPath cell的某行
 */
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    TCUserInfoCellItem *item = _userInfoUISetArry[indexPath.row];
    TCUserInfoTableViewCell *cell = [_dataTable cellForRowAtIndexPath:indexPath];
    if (item.action)
    {
        item.action(item, cell);
    }
    
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}
/**
 *  用于显示 编辑个人信息 页面
 *
 *  @param menu 无意义
 *  @param cell 无意义
 */
- (void)onEditUserInfo:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell
{
    //个人信息编辑的逻辑不再维护
//    TCEditUserInfoViewController *vc = [[TCEditUserInfoViewController alloc] init];
//    [self.navigationController pushViewController:vc animated:true];
}


- (void)gotoWeb:(id)menu cell:(id)cell
{
    TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://web.sdk.qcloud.com/document/Tencent-UGSV-User-Agreement.html"];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

- (void)gotoprivace:(id)menu cell:(id)cell
{
    TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ea00e5256ad442c483cd685d27b2e49f"];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

- (void)gotoInfolist:(id)menu cell:(id)cell
{
    TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ac0e6b4500c442839d632828a35083da"];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

- (void)gotoSharelist:(id)menu cell:(id)cell
{
    TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ac99514d96824473aff08e88dba7ee92"];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

/**
 *  用户显示小直播的版本号信息
 *
 *  @param menu 无意义
 *  @param cell 无意义
 */
- (void)onShowAppVersion:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell
{
    NSString *rtmpSDKVersion = [NSString stringWithFormat:NSLocalizedString(@"TCUserInfoView.InfoRTMPFmt", nil), [TXLiveBase getSDKVersionStr]];
    NSString *appShortVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
    NSString *buildNumber = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];
    NSString *appVersion = [NSString stringWithFormat:@"%@.%@", appShortVersion, buildNumber];
    NSString *info = [NSString stringWithFormat:NSLocalizedString(@"TCUserInfoView.InfoAppFmt", nil), appVersion, rtmpSDKVersion];
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"TCUserInfoView.HintAboutApp", nil)
                                                                        message:info
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.Close", nil) style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];

}

-(void)onClickSetting:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell{
    SettingViewController *next = [[SettingViewController alloc] init];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

/**
 *  用户显示SDK信息
 *
 *  @param menu 无意义
 *  @param cell 无意义
 */
- (void)gotoAbout:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell
{
    AboutViewController *next = [[AboutViewController alloc] init];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

/**
 判断当前语言是否是简体中文
 */
- (BOOL)isCurrentLanguageHans
{
    NSArray *languages = [NSLocale preferredLanguages];
    NSString *currentLanguage = [languages objectAtIndex:0];
    if ([currentLanguage isEqualToString:@"zh-Hans-CN"])
    {
        return YES;
    }
    
    return NO;
}

#pragma mark - UICollectionView datasource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return _avatarList.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {

    TCAvatarListCell *cell = (TCAvatarListCell *)[collectionView dequeueReusableCellWithReuseIdentifier:@"TCAvatarListCell" forIndexPath:indexPath];
    if (cell == nil) {
        cell = [[TCAvatarListCell alloc] initWithFrame:CGRectZero];
    }

    NSInteger index = indexPath.row;
    NSString *imageUrl = _avatarList[index];
    cell.userInteractionEnabled = YES;
    if (_selectedIndex > -1) {
        cell.avatarUrlSelected = _avatarList[_selectedIndex];
    }
    cell.avatarUrl = imageUrl;

    return cell;
}

#pragma mark - UICollectionView delegate

//点击item方法
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    _selectedIndex = indexPath.row;
    [_collectionView reloadData];
    
}

-(void)initAvatarList{
    _avatarList = @[
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar1.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar2.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar3.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar4.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar5.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar6.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar7.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar8.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar9.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar10.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar11.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar12.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar13.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar14.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar15.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar16.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar17.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar18.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar19.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar20.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar21.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar22.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar23.png",
        @"https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar24.png",
    ];
}

-(void)checkOpen{
    if([TCLoginParam shareInstance].isExpired){
        TCLoginViewController *next = [[TCLoginViewController alloc] init];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }
}

@end
