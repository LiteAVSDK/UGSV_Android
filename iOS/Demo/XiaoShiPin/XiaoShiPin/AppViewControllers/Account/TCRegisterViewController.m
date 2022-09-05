//
//  TCRegisterViewController.m
//  TCLVBIMDemo
//
//  Created by dackli on 16/10/1.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCRegisterViewController.h"
#import "UIView+Additions.h"
#import "TCLoginModel.h"
#import "TXWechatInfoView.h"
#import "TCUtil.h"
#import "MBProgressHUD.h"
#import "TCUserInfoModel.h"
#import "TCWebViewController.h"

#define L(X) NSLocalizedString((X), nil)

@interface TCRegisterViewController ()

@end

@implementation TCRegisterViewController
{
    UITextField    *_accountTextField;  // 用户名/手机号
    UITextField    *_pwdTextField;      // 密码/验证码
    UITextField    *_pwdTextField2;     // 确认密码（用户名注册）
    TXWechatInfoView *_wechatInfoView;
    UIButton       *_regBtn;            // 注册
    UIButton       *_agreeBtn;
    UIView         *_lineView1;
    UIView         *_lineView2;
    UIView         *_lineView3;
    
}

- (void)viewDidLoad {
    [super viewDidLoad];

    [self initUI];
    
    UITapGestureRecognizer *tag = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(clickScreen)];
    [self.view addGestureRecognizer:tag];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    [self.navigationController.navigationBar setBackgroundImage:[UIImage new] forBarMetrics:UIBarMetricsDefault];
    [self.navigationController.navigationBar setShadowImage:[UIImage new]];
    [self.navigationController.navigationBar setTintColor:[UIColor whiteColor]];
    [self.navigationController.navigationBar setBackgroundColor:[UIColor clearColor]];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:YES];
    [self.navigationController.navigationBar setBackgroundImage:nil forBarMetrics:UIBarMetricsDefault];
    [self.navigationController.navigationBar setShadowImage:nil];
}

- (void)initUI {
    UIImage *image = [UIImage imageNamed:@"loginBG"];
    self.view.layer.contents = (id)image.CGImage;
    
    _accountTextField = [[UITextField alloc] init];
    _accountTextField.font = [UIFont systemFontOfSize:14];
    _accountTextField.textColor = [UIColor colorWithWhite:1 alpha:1];
    _accountTextField.returnKeyType = UIReturnKeyNext;
    _accountTextField.adjustsFontSizeToFitWidth = YES;
    _accountTextField.minimumFontSize = 9;
    _accountTextField.delegate = self;
    
    _pwdTextField = [[UITextField alloc] init];
    _pwdTextField.font = [UIFont systemFontOfSize:14];
    _pwdTextField.textColor = [UIColor colorWithWhite:1 alpha:1];
    _pwdTextField.returnKeyType = UIReturnKeyNext;
    _pwdTextField.delegate = self;
    
    _pwdTextField2 = [[UITextField alloc] init];
    _pwdTextField2.font = [UIFont systemFontOfSize:14];
    _pwdTextField2.textColor = [UIColor colorWithWhite:1 alpha:1];
    _pwdTextField2.secureTextEntry = YES;
    [_pwdTextField2 setPlaceholder:NSLocalizedString(@"TCRegisterView.HintConfirmPassword", nil)];
    _pwdTextField2.returnKeyType = UIReturnKeyGo;
    _pwdTextField2.delegate = self;
    if (@available(iOS 11.0, *)) {
        _accountTextField.textContentType = UITextContentTypeUsername;
        if (@available(iOS 12.0, *)) {
            _pwdTextField.textContentType = UITextContentTypeNewPassword;
            _pwdTextField2.textContentType = UITextContentTypeNewPassword;
        }
    }

    _lineView1 = [[UIView alloc] init];
    [_lineView1 setBackgroundColor:[UIColor whiteColor]];
    
    _lineView2 = [[UIView alloc] init];
    [_lineView2 setBackgroundColor:[UIColor whiteColor]];
    
    _lineView3 = [[UIView alloc] init];
    [_lineView3 setBackgroundColor:[UIColor whiteColor]];
    
    _regBtn = [[UIButton alloc] init];
    _regBtn.titleLabel.font = [UIFont systemFontOfSize:16];
    [_regBtn setTitle:NSLocalizedString(@"TCRegisterView.DoRegister", nil) forState:UIControlStateNormal];
    [_regBtn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_regBtn.layer setCornerRadius:17.5];
    [_regBtn.layer setBorderWidth:1];
    _regBtn.backgroundColor = [UIColor colorWithWhite:1 alpha:0.5];
    _regBtn.layer.borderColor = [UIColor clearColor].CGColor;
    _regBtn.clipsToBounds = YES;
    [_regBtn addTarget:self action:@selector(reg:) forControlEvents:UIControlEventTouchUpInside];
    
    TXWechatInfoView *infoView = [[TXWechatInfoView alloc] initWithFrame:CGRectMake(10, _regBtn.bottom+20, self.view.width - 20, 100)];
    _wechatInfoView = infoView;
    
    _agreeBtn = [[UIButton alloc] init];
    [_agreeBtn setBackgroundImage:[UIImage imageNamed:@"select_icon"] forState:UIControlStateNormal];
    [_agreeBtn setBackgroundImage:[UIImage imageNamed:@"selected_icon"] forState:UIControlStateSelected];
    [_agreeBtn addTarget:self action:@selector(clickAgreeBtn) forControlEvents:UIControlEventTouchUpInside];
    _agreeBtn.frame = CGRectMake(20, 235.5, 20, 20);
    
    NSString *content = NSLocalizedString(@"TCLogin.agreement", nil);
    UITextView *contentTextView = [[UITextView alloc] initWithFrame:CGRectMake(40, 230, [[UIScreen mainScreen] bounds].size.width - 40, 60)];
    if ([self isCurrentLanguageHans]) {
        contentTextView.attributedText = [self getContentLabelAttributedText:content range1:NSMakeRange(7, 6) range1:NSMakeRange(content.length - 6, 6) fontSize:14];
    }else{
        contentTextView.attributedText = [self getContentLabelAttributedText:content range1:NSMakeRange(29, 14) range1:NSMakeRange(content.length - 14, 14) fontSize:14];
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
    [self.view addSubview:_pwdTextField2];
    [self.view addSubview:_lineView3];
    [self.view addSubview:_regBtn];
    [self.view addSubview:infoView];

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

    [_pwdTextField setSize:CGSizeMake(screen_width - 50, 33)];

    [_pwdTextField setY:_lineView1.bottom + 6];
    [_pwdTextField setX:25];
    
    [_lineView2 setSize:CGSizeMake(screen_width - 44, 1)];
    [_lineView2 setY:_pwdTextField.bottom + 6];
    [_lineView2 setX:22];
    

    [_pwdTextField2 setSize:CGSizeMake(screen_width - 50, 33)];
    [_pwdTextField2 setY:_lineView2.bottom + 6];
    [_pwdTextField2 setX:25];
    
    [_lineView3 setSize:CGSizeMake(screen_width - 44, 1)];
    [_lineView3 setY:_pwdTextField2.bottom + 6];
    [_lineView3 setX:22];
    
    [_regBtn setSize:CGSizeMake(screen_width - 44, 35)];
    [_regBtn setY:_lineView3.bottom + 50];
    [_regBtn setX:22];
    
    _wechatInfoView.top = _regBtn.bottom + 30;
    
    [_accountTextField setPlaceholder:NSLocalizedString(@"TCRegisterView.PlaceholderUserName", nil)];
    [_accountTextField setText:@""];
    _accountTextField.keyboardType = UIKeyboardTypeDefault;
    [_pwdTextField setPlaceholder:NSLocalizedString(@"TCRegisterView.PlaceholderPassword", nil)];
    [_pwdTextField setText:@""];
    [_pwdTextField2 setText:@""];
    _pwdTextField.secureTextEntry = YES;
    _pwdTextField2.hidden = NO;
    _lineView3.hidden = NO;
    
    _accountTextField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:_accountTextField.placeholder
                                                                              attributes:@{NSForegroundColorAttributeName: [UIColor colorWithWhite:1 alpha:0.5]}];
    _pwdTextField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:_pwdTextField.placeholder
                                                                          attributes:@{NSForegroundColorAttributeName: [UIColor colorWithWhite:1 alpha:0.5]}];
    _pwdTextField2.attributedPlaceholder = [[NSAttributedString alloc] initWithString:_pwdTextField2.placeholder
                                                                           attributes:@{NSForegroundColorAttributeName: [UIColor colorWithWhite:1 alpha:0.5]}];
}

- (NSAttributedString *)getContentLabelAttributedText:(NSString *)text range1:(NSRange)range1 range1:(NSRange)range2 fontSize:(CGFloat)fontSize
{
    NSMutableAttributedString *attrStr = [[NSMutableAttributedString alloc] initWithString:text attributes:@{NSFontAttributeName:[UIFont systemFontOfSize:fontSize],NSForegroundColorAttributeName:[UIColor blackColor]}];
    [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range1];
    [attrStr addAttribute:NSLinkAttributeName value:@"yinsizhengce://" range:range1];
    
    [attrStr addAttribute:NSForegroundColorAttributeName value:[UIColor blueColor] range:range2];
    [attrStr addAttribute:NSLinkAttributeName value:@"yonghuxieyi://" range:range2];
    return attrStr;
}

-(void)clickAgreeBtn{
    [_agreeBtn setSelected:!_agreeBtn.selected];
    if (_agreeBtn.selected) {
        _regBtn.backgroundColor = [UIColor colorWithWhite:1 alpha:1];
    }else{
        _regBtn.backgroundColor = [UIColor colorWithWhite:1 alpha:0.5];
    }
}

- (void)clickScreen {
    [_accountTextField resignFirstResponder];
    [_pwdTextField resignFirstResponder];
    [_pwdTextField2 resignFirstResponder];
}

- (void)reg:(UIButton *)button {
    
    if (!_agreeBtn.selected) {
        [self _alert:NSLocalizedString(@"TCLogin.PleaseAgree", nil)
             message:@""
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        return;;
    }
    TCLoginModel *loginModel = [TCLoginModel sharedInstance];
    
    NSString *userName = _accountTextField.text;
    NSString *failedReason = nil;
    
    if (![loginModel validateUserName:userName failedReason:&failedReason]) {
        [self _alert:NSLocalizedString(@"TCLoginView.HintUserNameError", nil)
             message:failedReason
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        return;
    }
    
    NSString *pwd = _pwdTextField.text;
    if (![loginModel validatePassword:pwd failedReason:&failedReason]) {
        [self _alert:NSLocalizedString(@"TCLoginView.ErrorPasswordWrong", nil)
             message:failedReason
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        return;
    }
   
    NSString *pwd2 = _pwdTextField2.text;
    if ([pwd compare:pwd2] != NSOrderedSame) {
        [self _alert:NSLocalizedString(@"TCLoginView.ErrorPasswordWrong", nil)
             message:NSLocalizedString(@"TCRegisterView.ErrorPasswordConsistency", nil)
         buttonTitle:NSLocalizedString(@"Common.OK", nil)];
        return;
    }
    
    // 用户名密码注册
    __weak typeof(self) weakSelf = self;
    [self _startHUD:nil];
    [[TCLoginModel sharedInstance] registerWithUsername:userName password:pwd succ:^(NSString *userName, NSString *md5pwd) {
        // 注册成功后直接登录
        [[TCLoginModel sharedInstance] loginWithUsername:userName
                                                password:pwd
                                                    succ:^(NSString* userName, NSString* md5pwd ,NSString *token,NSString *refreshToken,long expires) {
            [weakSelf _hideHUD];
            [self goBack];
            [weakSelf.loginListener loginOK:userName hashedPwd:md5pwd token:token refreshToken:refreshToken expires:expires];
        } fail:^(NSString *userName, int errCode, NSString *errMsg) {
            [weakSelf _hideHUD];
            [weakSelf.loginListener loginFail:userName code:errCode message:errMsg];
            NSLog(@"%s %d %@", __func__, errCode, errMsg);
        }];
        [TCUtil report:xiaoshipin_register userName:userName code:200 msg:@"注册成功"];
    } fail:^(int errCode, NSString *errMsg) {
        [weakSelf _hideHUD];
        
        NSMutableDictionary *param = [NSMutableDictionary dictionary];
        [param setObject:userName forKey:@"userName"];
        [param setObject:@"register" forKey:@"action"];
        
        if (errCode == 612) {
            [weakSelf _alert:NSLocalizedString(@"Common.Hint", nil)
                 message:NSLocalizedString(@"TCRegisterView.ErrorUserNameRegistered", nil)
             buttonTitle:NSLocalizedString(@"Common.OK", nil)];
            [TCUtil report:xiaoshipin_register userName:userName code:errCode msg:@"用户ID已经被注册"];
        }else{
            [weakSelf _alert:NSLocalizedString(@"Common.Hint", nil)
                 message:[NSString stringWithFormat:NSLocalizedString(@"Common.HintErrorCode", nil), errCode]
             buttonTitle:NSLocalizedString(@"Common.OK", nil)];
            [TCUtil report:xiaoshipin_register userName:userName code:errCode msg:errMsg];
        }
    }];
}

- (void)_alert:(NSString *)title message:(NSString *)message buttonTitle:(NSString *)buttonTitle; {
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:buttonTitle style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];
}

- (void)_startHUD:(NSString *)text {
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.label.text = text;
    hud.mode = text.length == 0 ? MBProgressHUDModeIndeterminate : MBProgressHUDModeText;
}

- (void)_hideHUD {
    [[MBProgressHUD HUDForView:self.view] hideAnimated:YES];
}

-(void)goBack{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.navigationController popViewControllerAnimated:YES];
    });
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
    NSArray<UITextField *> *chain = @[_accountTextField, _pwdTextField, _pwdTextField2];
    NSInteger index = [chain indexOfObject:textField];
    if (index != NSNotFound) {
        if (index < chain.count - 1) {
            [chain[index + 1] becomeFirstResponder];
        } else {
    [textField resignFirstResponder];
            [self reg:nil];
        }
    }
    return YES;
}
#pragma mark 富文本点击事件
-(BOOL)textView:(UITextView *)textView shouldInteractWithURL:(NSURL *)URL inRange:(NSRange)characterRange interaction:(UITextItemInteraction)interaction  API_AVAILABLE(ios(10.0)){
    if ([[URL scheme] isEqualToString:@"yonghuxieyi"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://web.sdk.qcloud.com/document/Tencent-UGSV-User-Agreement.html"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }
    else if ([[URL scheme] isEqualToString:@"yinsizhengce"]) {
        TCWebViewController *next = [[TCWebViewController alloc] initWithURL:@"https://privacy.qq.com/document/preview/ea00e5256ad442c483cd685d27b2e49f"];
        next.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:next animated:YES];
    }
    return YES;
}
@end
