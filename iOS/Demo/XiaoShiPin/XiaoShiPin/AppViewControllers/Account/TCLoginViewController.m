//
//  TCLoginViewController.m
//  TCLVBIMDemo
//
//  Created by dackli on 16/8/1.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCLoginViewController.h"
#import "TCLoginModel.h"
#import "TCUtil.h"
#import "TCLoginParam.h"
#import "TCRegisterViewController.h"
#import "TCUserInfoModel.h"
#import "UGCKit.h"
#import "TXWechatInfoView.h"
#import "UIView+Additions.h"
#import "MBProgressHUD.h"
#import "TCWebViewController.h"
#import "AppDelegate.h"
#import "SettingViewController.h"
#import "DeleteAccountViewController.h"

@interface TCLoginViewController ()<UITextFieldDelegate, TCLoginListener, UIGestureRecognizerDelegate>
{
    TCLoginParam *_loginParam;

    UITextField    *_accountTextField;  // 用户名/手机号
    UITextField    *_pwdTextField;      // 密码/验证码
    UIButton       *_loginBtn;          // 登录
    UIButton       *_regBtn;            // 注册
    TXWechatInfoView *_wechatInfoView;    // 公众号信息
    UIView         *_lineView1;
    UIView         *_lineView2;
    UIButton       *_agreeBtn;          //同意协议
    UIView         *_coverView;
    UIView         *_contentView;
    UIView         *_alphaView;
    
    BOOL           _isSMSLoginType;     // YES 表示手机号登录，NO 表示用户名登录
}
@end

@implementation TCLoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    _loginParam = [TCLoginParam shareInstance];
    BOOL isAutoLogin = [TCLoginModel isAutoLogin];
    [self pullLoginUI];

    if (isAutoLogin && [_loginParam isValid]) {
        [self autoLogin];
    }else {
    }
    
    [self checkOpen];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES];
    [self removeViewController];
}

-(void)removeViewController{
    for(int i = 0; i < self.navigationController.viewControllers.count; i++){
    UIViewController *viewC = self.navigationController.viewControllers[i];
    if([viewC.class isEqual:SettingViewController.class]){
        [viewC removeFromParentViewController];
    }
}
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - login

- (void)autoLogin {
    if ([_loginParam isExpired]) {
        // 刷新票据
        __weak typeof(self) weakSelf = self;
        [self _startHUD:NSLocalizedString(@"TCLoginView.AutoLoggingIn", nil)];
//        [[HUDHelper sharedInstance] syncLoading:NSLocalizedString(@"TCLoginView.AutoLoggingIn", nil)];
        [[TCLoginModel sharedInstance] login:_loginParam.identifier hashPwd:_loginParam.hashedPwd succ:^(NSString* userName, NSString* md5pwd ,NSString *token,NSString *refreshToken,long expires) {
            [weakSelf loginOK:userName hashedPwd:md5pwd token:token refreshToken:refreshToken expires:expires];
            [self _hideHUD];
            // TODO: s
//            [[AppDelegate sharedAppDelegate] enterMainUI];
        } fail:^(NSString *userName, int errCode, NSString *errMsg) {
//            [[HUDHelper sharedInstance] syncStopLoading];
            [weakSelf loginFail:userName code:errCode message:errMsg];
//            [[HUDHelper sharedInstance] syncLoading:NSLocalizedString(@"TCLoginView.AutoLoggingIn", nil)];
            [self _startHUD:NSLocalizedString(@"TCLoginView.AutoLoggingIn", nil)];

            [self pullLoginUI];
        }];
    }
    else {
        [self pullLoginUI];
    }
}

- (void)pullLoginUI {
    if (_accountTextField == nil) {
        [self setupUI];
    } else {
        [self relayout];
    }
}

- (void)setupUI {
    _isSMSLoginType = NO;
    [self initUI];

    UITapGestureRecognizer *tag = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideKeyboard)];
    [self.view addGestureRecognizer:tag];
}

- (void)initUI {
    UIImage *image = [UIImage imageNamed:@"loginBG"];
    self.view.layer.contents = (id)image.CGImage;
    
    UIButton *backButton = [UGCKitSmallButton buttonWithType:UIButtonTypeCustom];
    [backButton setImage:[UIImage imageNamed:@"backIcon"] forState:UIControlStateNormal];
    [backButton addTarget:self action:@selector(onGoBack:) forControlEvents:UIControlEventTouchUpInside];
    CGFloat top = [UIApplication sharedApplication].statusBarFrame.size.height;
    backButton.frame = CGRectMake(15, MAX(top+5, 30), 14 , 23);
    backButton.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleRightMargin;
    [self.view addSubview:backButton];
    
    _accountTextField = [[UITextField alloc] init];
    _accountTextField.font = [UIFont systemFontOfSize:14];
    _accountTextField.textColor = [UIColor colorWithWhite:1 alpha:1];
    _accountTextField.returnKeyType = UIReturnKeyNext;
    _accountTextField.delegate = self;
    
    _pwdTextField = [[UITextField alloc] init];
    _pwdTextField.font = [UIFont systemFontOfSize:14];
    _pwdTextField.textColor = [UIColor colorWithWhite:1 alpha:1];
    _pwdTextField.returnKeyType = UIReturnKeyGo;
    _pwdTextField.delegate = self;
    if (@available(iOS 11.0, *)) {
        _accountTextField.textContentType = UITextContentTypeUsername;
        _pwdTextField.textContentType = UITextContentTypePassword;
    }
    _lineView1 = [[UIView alloc] init];
    [_lineView1 setBackgroundColor:[UIColor whiteColor]];
    
    _lineView2 = [[UIView alloc] init];
    [_lineView2 setBackgroundColor:[UIColor whiteColor]];
    
    _loginBtn = [[UIButton alloc] init];
    _loginBtn.titleLabel.font = [UIFont systemFontOfSize:16];
    [_loginBtn setTitle:NSLocalizedString(@"TCLoginView.Login", nil) forState:UIControlStateNormal];
    [_loginBtn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_loginBtn.layer setCornerRadius:17.5];
    [_loginBtn.layer setBorderWidth:1];
    _loginBtn.backgroundColor = [UIColor colorWithWhite:1 alpha:0.5];
    _loginBtn.layer.borderColor = [UIColor clearColor].CGColor;
    _loginBtn.clipsToBounds = YES;
    [_loginBtn addTarget:self action:@selector(login:) forControlEvents:UIControlEventTouchUpInside];
    
    _regBtn = [[UIButton alloc] init];
    _regBtn.titleLabel.font = [UIFont systemFontOfSize:14];
    [_regBtn setTitleColor:[UIColor colorWithWhite:1 alpha:0.5] forState:UIControlStateNormal];
    [_regBtn setContentHorizontalAlignment:UIControlContentHorizontalAlignmentRight];
    [_regBtn setTitle:NSLocalizedString(@"TCLoginView.Register", nil) forState:UIControlStateNormal];
    [_regBtn addTarget:self action:@selector(reg:) forControlEvents:UIControlEventTouchUpInside];
    
    TXWechatInfoView *infoView = [[TXWechatInfoView alloc] initWithFrame:CGRectMake(10, _regBtn.bottom+20, self.view.width - 20, 100)];
    _wechatInfoView = infoView;
    
    _agreeBtn = [[UIButton alloc] init];
    [_agreeBtn setBackgroundImage:[UIImage imageNamed:@"select_icon"] forState:UIControlStateNormal];
    [_agreeBtn setBackgroundImage:[UIImage imageNamed:@"selected_icon"] forState:UIControlStateSelected];
    [_agreeBtn addTarget:self action:@selector(clickAgreeBtn) forControlEvents:UIControlEventTouchUpInside];
    _agreeBtn.frame = CGRectMake(20, 196, 20, 20);
    
    NSString *content = NSLocalizedString(@"TCLogin.agreement", nil);
    UITextView *contentTextView = [[UITextView alloc] initWithFrame:CGRectMake(40, 190, [[UIScreen mainScreen] bounds].size.width - 40, 60)];
    if ([self isCurrentLanguageHans]) {
        contentTextView.attributedText = [self getContentLabelAttributedText:content range1:NSMakeRange(7, 6) range2:NSMakeRange(content.length - 6, 6) fontSize:14];
    }else{
        contentTextView.attributedText = [self getContentLabelAttributedText:content range1:NSMakeRange(29, 14) range2:NSMakeRange(content.length - 14, 14) fontSize:14];
    }
    contentTextView.textAlignment = NSTextAlignmentLeft;
    contentTextView.delegate = self;
    contentTextView.editable = NO;        //必须禁止输入，否则点击将弹出输入键盘
    contentTextView.scrollEnabled = NO;
    contentTextView.backgroundColor = [UIColor clearColor];
    [self.view addSubview:contentTextView];
    [self.view addSubview:_agreeBtn];
    [self.view addSubview:_accountTextField];
    [self.view addSubview:_lineView1];
    [self.view addSubview:_pwdTextField];
    [self.view addSubview:_lineView2];
    [self.view addSubview:_loginBtn];
    [self.view addSubview:_regBtn];
    [self.view addSubview:infoView];
    
    UISwipeGestureRecognizer *gesture = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(onGoBack:)];
    gesture.delegate = self;
    gesture.direction = UISwipeGestureRecognizerDirectionRight;
    [self.view addGestureRecognizer:gesture];
    
    [self relayout];
}

- (void)relayout {
    CGFloat screen_width = self.view.bounds.size.width;
    
    [_accountTextField setSize:CGSizeMake(screen_width - 50, 33)];
    [_accountTextField setY:97];
    [_accountTextField setX:25];
    
    [_lineView1 setSize:CGSizeMake(screen_width - 44, 1)];
    [_lineView1 setY:_accountTextField.bottom + 6];
    [_lineView1 setX:22];
    
    if (_isSMSLoginType) {
        [_pwdTextField setSize:CGSizeMake(150, 33)];
    } else {
        [_pwdTextField setSize:CGSizeMake(screen_width - 50, 33)];
    }
    [_pwdTextField setY:_lineView1.bottom + 6];
    [_pwdTextField setX:25];
    
    [_lineView2 setSize:CGSizeMake(screen_width - 44, 1)];
    [_lineView2 setY:_pwdTextField.bottom + 6];
    [_lineView2 setX:22];
    
    [_loginBtn setSize:CGSizeMake(screen_width - 44, 35)];
    [_loginBtn setY:_lineView2.bottom + 50];
    [_loginBtn setX:22];

    [_regBtn sizeToFit];
    [_regBtn setSize:CGSizeMake(_regBtn.frame.size.width, 15)];
    [_regBtn setY:_loginBtn.bottom + 25];
    [_regBtn setX:_regBtn.superview.width-25-_regBtn.width];
    _regBtn.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleBottomMargin;
    _wechatInfoView.top = _regBtn.bottom + 30;
    
    [_accountTextField setPlaceholder:NSLocalizedString(@"TCLoginView.PlaceholderEnterUserName", nil)];
    [_accountTextField setText:@""];
    _accountTextField.keyboardType = UIKeyboardTypeDefault;
    [_pwdTextField setPlaceholder:NSLocalizedString(@"TCLoginView.PlaceholderEnterPassword", nil)];
    [_pwdTextField setText:@""];
    
    _pwdTextField.secureTextEntry = YES;
    
    _accountTextField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:_accountTextField.placeholder attributes:@{NSForegroundColorAttributeName: [UIColor colorWithWhite:1 alpha:0.5]}];
    _pwdTextField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:_pwdTextField.placeholder attributes:@{NSForegroundColorAttributeName: [UIColor colorWithWhite:1 alpha:0.5]}];

}

-(void) checkOpen{
    if(![[NSUserDefaults standardUserDefaults] boolForKey:@"firstLaunch"]){
    [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"firstLaunch"];
    [self openAlertWindow];
    }
}

-(void) openAlertWindow{
    _coverView = [UIView new];
    _coverView.frame = self.view.bounds;
    _coverView.backgroundColor = [UIColor blackColor];
    _coverView.alpha = 0.5;
    _coverView.userInteractionEnabled = YES;
    [self.view addSubview:_coverView];
    
    _contentView = [UIView new];
    _contentView.backgroundColor = [UIColor whiteColor];
    _contentView.frame = CGRectMake(25, 200,
    [UIScreen mainScreen].bounds.size.width - 50,[self isCurrentLanguageHans]? 380 : 480);
    _contentView.layer.cornerRadius = 10;
    _contentView.layer.masksToBounds = YES;
    _contentView.userInteractionEnabled = YES;
    [self.view addSubview:_contentView];
    
    UILabel *titleLabel = [UILabel new];
    titleLabel.text = NSLocalizedString(@"TCLogin.welcome", nil);
    titleLabel.font = [UIFont boldSystemFontOfSize:20];
    titleLabel.textColor = [UIColor blackColor];
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.lineBreakMode = NSLineBreakByWordWrapping;
    titleLabel.numberOfLines = 0;
    titleLabel.frame = CGRectMake(0,10,
                                  _contentView.bounds.size.width,80);
    [_contentView addSubview:titleLabel];
    
    UIButton *cancelBtn = [UIButton new];
    cancelBtn.titleLabel.font = [UIFont systemFontOfSize:18];
    [cancelBtn setTitle:NSLocalizedString(@"TCLogin.disagree", nil)
               forState:UIControlStateNormal];
    [cancelBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    cancelBtn.frame = CGRectMake(0,_contentView.bounds.size.height - 40,
                                 _contentView.bounds.size.width/2,40);
    [cancelBtn addTarget:self action:@selector(closeAlertWindow) forControlEvents:UIControlEventTouchUpInside];
    [_contentView addSubview:cancelBtn];
    
    UIButton *confirmBtn = [UIButton new];
    confirmBtn.titleLabel.font = [UIFont systemFontOfSize:18];
    [confirmBtn setTitle:NSLocalizedString(@"TCLogin.agree", nil)
                forState:UIControlStateNormal];
    [confirmBtn setTitleColor:[UIColor colorWithRed:0/255.0 green:108/255.0 blue:255/255.0 alpha:1/1.0] forState:UIControlStateNormal];
    confirmBtn.frame = CGRectMake(_contentView.bounds.size.width/2,_contentView.bounds.size.height - 40,
                                 _contentView.bounds.size.width/2,40);
    [confirmBtn addTarget:self action:@selector(clickConfirm) forControlEvents:UIControlEventTouchUpInside];
    [_contentView addSubview:confirmBtn];
    
    UIView *vLine = [UIView new];
    vLine.backgroundColor = [UIColor grayColor];
    vLine.alpha = 0.5;
    vLine.frame = CGRectMake(_contentView.bounds.size.width/2, _contentView.bounds.size.height - 40, 1, 40);
    [_contentView addSubview:vLine];
    
    UIView *hLine = [UIView new];
    hLine.backgroundColor = [UIColor grayColor];
    hLine.alpha = 0.5;
    hLine.frame = CGRectMake(0, _contentView.bounds.size.height - 40, _contentView.bounds.size.width, 1);
    [_contentView addSubview:hLine];
    
    UITextView *contentTextView = [[UITextView alloc] initWithFrame:CGRectMake(20, 70, _contentView.bounds.size.width - 40, [self isCurrentLanguageHans] ? 160 : 160)];
    NSString *string = NSLocalizedString(@"TCLogin.privacyDetail", nil);
    if ([self isCurrentLanguageHans]){
        contentTextView.attributedText = [self getDetailLabelAttributedText:string range1:NSMakeRange(96, 11)
        range2:NSMakeRange(285, 11) range3:NSMakeRange(424, 14) range4:NSMakeRange(509, 11)
        range5:NSMakeRange(599, 11) scrollDetai:YES];
    }else{
        contentTextView.attributedText = [self getDetailLabelAttributedText:string
        range1:NSMakeRange(385, 34) range2:NSMakeRange(1054, 39) range3:NSMakeRange(1536, 48)
        range4:NSMakeRange(1969, 50)range5:NSMakeRange(2166, 40) scrollDetai:YES];
    }
    contentTextView.textAlignment = NSTextAlignmentLeft;
    contentTextView.delegate = self;
    contentTextView.editable = NO;        //必须禁止输入，否则点击将弹出输入键盘
    contentTextView.scrollEnabled = YES;
    contentTextView.font = [UIFont systemFontOfSize:16];
    [_contentView addSubview:contentTextView];
    
    _alphaView = [UIView new];
    _alphaView.backgroundColor = [UIColor whiteColor];
    _alphaView.frame = CGRectMake(20, 215, contentTextView.bounds.size.width, 20);
    [self changeAlphaWithView:_alphaView];
    [_contentView addSubview:_alphaView];
    
    UITextView *bottomTextView = [[UITextView alloc] initWithFrame:CGRectMake(20, 235, _contentView.bounds.size.width - 40, [self isCurrentLanguageHans] ? 105 : 205)];
    NSString *text = NSLocalizedString(@"TCLogin.privacyBottom", nil);
    if ([self isCurrentLanguageHans]){
        bottomTextView.attributedText = [self getDetailLabelAttributedText:text range1:NSMakeRange(4, 9)
            range2:NSMakeRange(14, 11) range3:NSMakeRange(26, 15) range4:NSMakeRange(42, 14)
            range5:NSMakeRange(58, 10) scrollDetai:NO];
    }else{
        bottomTextView.attributedText = [self getDetailLabelAttributedText:text range1:NSMakeRange(19, 26)
            range2:NSMakeRange(46, 41) range3:NSMakeRange(88, 56) range4:NSMakeRange(146, 47)
            range5:NSMakeRange(198, 39) scrollDetai:NO];
    }
    bottomTextView.font = [UIFont systemFontOfSize:14];
    bottomTextView.scrollEnabled = NO;
    bottomTextView.editable = NO;
    bottomTextView.delegate = self;
    [_contentView addSubview:bottomTextView];
}


- (void)changeAlphaWithView:(UIView *)changeView{
    CAGradientLayer *_gradLayer = [CAGradientLayer layer];
    NSArray *colors = [NSArray arrayWithObjects:
                       (id)[[UIColor colorWithWhite:0 alpha:0.7] CGColor],
                       (id)[[UIColor colorWithWhite:0 alpha:0.9] CGColor],
                       (id)[[UIColor colorWithWhite:0 alpha:1] CGColor],
                       nil];
    [_gradLayer setColors:colors];
    [_gradLayer setStartPoint:CGPointMake(0, 0)];
    [_gradLayer setEndPoint:CGPointMake(0, 1)];
    [_gradLayer setFrame:CGRectMake(0, 0, changeView.bounds.size.width, changeView.bounds.size.height)];
    [changeView.layer setMask:_gradLayer];
}

- (NSAttributedString *)getContentLabelAttributedText:(NSString *)text range1:(NSRange)range1 range2:(NSRange)range2 fontSize:(CGFloat)fontSize
{
    NSMutableAttributedString *attrStr = [[NSMutableAttributedString alloc] initWithString:text attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:fontSize],NSForegroundColorAttributeName:[UIColor blackColor]}];
    [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range1];
    [attrStr addAttribute:NSLinkAttributeName value:@"yinsizhengce://" range:range1];
    
    [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range2];
    [attrStr addAttribute:NSLinkAttributeName value:@"yonghuxieyi://" range:range2];
    return attrStr;
}

- (NSAttributedString *)getDetailLabelAttributedText:(NSString *)text range1:(NSRange)range1 range2:(NSRange)range2 range3:(NSRange)range3 range4:(NSRange)range4
    range5:(NSRange)range5 scrollDetai:(BOOL)scrollDetai
{
    NSMutableAttributedString *attrStr;
    if (scrollDetai) {
        attrStr = [[NSMutableAttributedString alloc] initWithString:text attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:16],NSForegroundColorAttributeName:[UIColor blackColor]}];
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range1];
        [attrStr addAttribute:NSLinkAttributeName value:@"zhengcezhaiyao://" range:range1];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range2];
        [attrStr addAttribute:NSLinkAttributeName value:@"yishouji://" range:range2];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range3];
        [attrStr addAttribute:NSLinkAttributeName value:@"gongxiangqingdan://" range:range3];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range4];
        [attrStr addAttribute:NSLinkAttributeName value:@"baohuzhiyin://" range:range4];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range5];
        [attrStr addAttribute:NSLinkAttributeName value:@"baohuzhiyin://" range:range5];
    }else{
        attrStr = [[NSMutableAttributedString alloc] initWithString:text attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:14],NSForegroundColorAttributeName:[UIColor blackColor]}];
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range1];
        [attrStr addAttribute:NSLinkAttributeName value:@"yonghuxieyi://" range:range1];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range2];
        [attrStr addAttribute:NSLinkAttributeName value:@"baohuzhiyin://" range:range2];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range3];
        [attrStr addAttribute:NSLinkAttributeName value:@"xinxiqingdan://" range:range3];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range4];
        [attrStr addAttribute:NSLinkAttributeName value:@"gongxiangqingdan://" range:range4];
        
        [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range5];
        [attrStr addAttribute:NSLinkAttributeName value:@"baohushengming://" range:range5];
    }
    return attrStr;
}

-(void)closeAlertWindow{
    [_coverView removeFromSuperview];
    [_contentView removeFromSuperview];
}

-(void)clickConfirm{
    [self closeAlertWindow];
    [_agreeBtn setSelected:YES];
    _loginBtn.backgroundColor = [UIColor colorWithWhite:1 alpha:1];
}

-(void)clickAgreeBtn{
    [_agreeBtn setSelected:!_agreeBtn.selected];
    if (_agreeBtn.selected) {
        _loginBtn.backgroundColor = [UIColor colorWithWhite:1 alpha:1];
    }else{
        _loginBtn.backgroundColor = [UIColor colorWithWhite:1 alpha:0.5];
    }
}



- (void)onGoBack:(id)sender {
    [self goBack:YES];
}

-(void)goBack:(BOOL)isExit{
    UIViewController *parentController = self.presentingViewController;
    if (parentController == nil) {
        if (isExit) {
            exit(0);
        }else{
            [self.navigationController popViewControllerAnimated:YES];
        }
    }else{
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

- (void)hideKeyboard {
    [self.view endEditing:YES];
}

- (void)reg:(UIButton *)button {
    TCRegisterViewController *regViewController = [[TCRegisterViewController alloc] init];
    regViewController.loginListener = self;
    [self.navigationController pushViewController:regViewController animated:YES];
}

- (void)switchLoginWay:(UIButton *)button {
    _isSMSLoginType = !_isSMSLoginType;
    [self hideKeyboard];
    [self relayout];
}

- (void)login:(UIButton *)button {
    if (!_agreeBtn.selected) {
        [self _alert:NSLocalizedString(@"TCLogin.PleaseAgree", nil)
             message:@""
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        return;;
    }
    NSString *userName = _accountTextField.text;
    NSString *failedReason = nil;
    if (![[TCLoginModel sharedInstance] validateUserName:userName failedReason:&failedReason]) {
        [self _alert:NSLocalizedString(@"TCLoginView.HintUserNameError", nil)
             message:failedReason
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        return;
    }

    NSString *pwd = _pwdTextField.text;
    if (![[TCLoginModel sharedInstance] validatePassword:pwd failedReason:&failedReason]) {
        [self _alert:NSLocalizedString(@"TCLoginView.HintPasswordError", nil)
             message:failedReason
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];

        return;
    }

    // 用户名密码登录
    [self hideKeyboard];
    [self _startHUD:nil];
//    [[MBProgressHUD HUDForView:self.view] showAnimated:YES];
//    [[HUDHelper sharedInstance] syncLoading];
    
    __weak __typeof(self) weakSelf = self;
    [[TCLoginModel sharedInstance] loginWithUsername:userName password:pwd succ:^(NSString* userName, NSString* md5pwd ,NSString *token,NSString *refreshToken,long expires) {
        [self _hideHUD];
        [weakSelf loginOK:userName hashedPwd:md5pwd token:token refreshToken:refreshToken expires:expires];
        
    } fail:^(NSString *userName, int errCode, NSString *errMsg) {
        [self _hideHUD];
        [weakSelf loginFail:userName code:errCode message:errMsg];
        NSLog(@"%s %d %@", __func__, errCode, errMsg);
    }];
}

- (void)loginFail:(NSString*)userName code:(int)errCode message:(NSString *)errMsg{
    NSMutableDictionary *param = [NSMutableDictionary dictionary];
    [param setObject:userName forKey:@"userName"];
    [param setObject:@"login" forKey:@"action"];
    
    NSString *loginFailedTitle = NSLocalizedString(@"TCLoginView.HintLoginFailed", nil);
    if(errCode == 620){
        [self _alert:loginFailedTitle
             message:NSLocalizedString(@"TCLoginView.ErrorAccountNotExists", nil)
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        [TCUtil report:xiaoshipin_login userName:userName code:errCode msg:@"账号未注册"];
    } else if(errCode == 621){
        [self _alert:loginFailedTitle
             message:NSLocalizedString(@"TCLoginView.ErrorPasswordWrong", nil)
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        [TCUtil report:xiaoshipin_login userName:userName code:errCode msg:@"密码错误"];

    } else {
        [self _alert:loginFailedTitle
             message:[NSString stringWithFormat:NSLocalizedString(@"Common.HintErrorCode", nil), errCode]
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        [TCUtil report:xiaoshipin_login userName:userName code:errCode msg:errMsg];
    }
}

- (void)loginOK:(NSString*)userName hashedPwd:(NSString*)hashedPwd token:(NSString *)token refreshToken:(NSString *)refreshToken expires:(NSInteger)expires
{
    // 进入主界面
    _loginParam.identifier = userName;
    _loginParam.hashedPwd = hashedPwd;
    _loginParam.token = token;
    _loginParam.tokenTime = [[NSDate date] timeIntervalSince1970];
    _loginParam.refreshToken = refreshToken;
    _loginParam.expires = expires;
    [_loginParam saveToLocal];
    [[TCLoginModel sharedInstance] scheduleRefreshLoginForExpireDate:_loginParam.expireDate];
    //[[AppDelegate sharedAppDelegate] enterMainUI];
    [TCUtil report:xiaoshipin_login userName:userName code:200 msg:@"登录成功"];
    [[NSNotificationCenter defaultCenter] postNotificationName:KReloadUserInfoNotification object:nil];
    [self goBack:NO];
}

- (void)_alert:(NSString *)title message:(NSString *)message buttonTitle:(NSString *)buttonTitle; {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:buttonTitle style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];
}

- (void)_startHUD:(NSString *)text {
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.label.text = text;
}

- (void)_hideHUD {
    [[MBProgressHUD HUDForView:self.view] hideAnimated:YES];
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

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField{
    NSArray<UITextField *> *chain = @[_accountTextField, _pwdTextField];
    NSInteger index = [chain indexOfObject:textField];
    if (index != NSNotFound) {
        if (index < chain.count - 1) {
            [chain[index + 1] becomeFirstResponder];
        } else {
            [textField resignFirstResponder];
            [self login:nil];
        }
    }
    return YES;
}

#pragma mark - UIGestureRecognizerDelegate
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
    CGPoint location = [gestureRecognizer locationInView:self.view];
    UIView *view = [self.view hitTest:location withEvent:nil];
    return ![view isKindOfClass:[UIControl class]] && location.x <= 20;
}
#pragma mark 富文本点击事件
-(BOOL)textView:(UITextView *)textView shouldInteractWithURL:(NSURL *)URL inRange:(NSRange)characterRange interaction:(UITextItemInteraction)interaction  API_AVAILABLE(ios(10.0)){
    if ([[URL scheme] isEqualToString:@"yonghuxieyi"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://web.sdk.qcloud.com/document/Tencent-UGSV-User-Agreement.html"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }else if ([[URL scheme] isEqualToString:@"yinsizhengce"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ea00e5256ad442c483cd685d27b2e49f"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }else if ([[URL scheme] isEqualToString:@"zhengcezhaiyao"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ea00e5256ad442c483cd685d27b2e49f"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }else if ([[URL scheme] isEqualToString:@"yishouji"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ac0e6b4500c442839d632828a35083da"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }else if ([[URL scheme] isEqualToString:@"gongxiangqingdan"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ac99514d96824473aff08e88dba7ee92"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }else if ([[URL scheme] isEqualToString:@"baohuzhiyin"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/cd1aaba55e1548c7975ef10fbe9785f7"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }else if ([[URL scheme] isEqualToString:@"xinxiqingdan"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ac0e6b4500c442839d632828a35083da"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }else if ([[URL scheme] isEqualToString:@"baohushengming"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/privacy-children.htm"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }
    return YES;
}
@end

