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
#import "TCRegisterViewController.h"
#import "TCUserInfoModel.h"
#import <UGCKit/UGCKit.h>
#import "TXWechatInfoView.h"
#import "UIView+Additions.h"
#import "MBProgressHUD.h"

@interface TCLoginViewController ()<UITextFieldDelegate,TCLoginListener, UIGestureRecognizerDelegate>
{
    TCLoginParam *_loginParam;

    UITextField    *_accountTextField;  // 用户名/手机号
    UITextField    *_pwdTextField;      // 密码/验证码
    UIButton       *_loginBtn;          // 登录
    UIButton       *_regBtn;            // 注册
    TXWechatInfoView *_wechatInfoView;    // 公众号信息
    UIView         *_lineView1;
    UIView         *_lineView2;
    
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
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES];
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
            [self dismissViewControllerAnimated:YES completion:nil];
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
    [_loginBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [_loginBtn setBackgroundImage:[UIImage imageNamed:@"button"] forState:UIControlStateNormal];
    [_loginBtn setBackgroundImage:[UIImage imageNamed:@"button_pressed"] forState:UIControlStateSelected];
    [_loginBtn addTarget:self action:@selector(login:) forControlEvents:UIControlEventTouchUpInside];
    
    _regBtn = [[UIButton alloc] init];
    _regBtn.titleLabel.font = [UIFont systemFontOfSize:14];
    [_regBtn setTitleColor:[UIColor colorWithWhite:1 alpha:0.5] forState:UIControlStateNormal];
    [_regBtn setContentHorizontalAlignment:UIControlContentHorizontalAlignmentRight];
    [_regBtn setTitle:NSLocalizedString(@"TCLoginView.Register", nil) forState:UIControlStateNormal];
    [_regBtn addTarget:self action:@selector(reg:) forControlEvents:UIControlEventTouchUpInside];
    
    TXWechatInfoView *infoView = [[TXWechatInfoView alloc] initWithFrame:CGRectMake(10, _regBtn.bottom+20, self.view.width - 20, 100)];
    _wechatInfoView = infoView;
    
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
    [_loginBtn setY:_lineView2.bottom + 36];
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

- (void)onGoBack:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
//    [[AppDelegate sharedAppDelegate] enterMainUI];
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
    [self dismissViewControllerAnimated:YES completion:nil];
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
@end
