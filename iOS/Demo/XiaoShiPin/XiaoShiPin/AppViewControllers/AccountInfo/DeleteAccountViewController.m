//
//  DeleteAccountViewController.m
//  XiaoShiPinApp
//
//  Created by tao yue on 2022/3/11.
//  Copyright © 2022 Tencent. All rights reserved.
//

#import "DeleteAccountViewController.h"
#import "ColorMacro.h"
#import "UGCKit.h"
#import "TCUserInfoModel.h"
#import "TCLoginModel.h"
#import "TCLoginViewController.h"

@interface DeleteAccountViewController ()
/*
 * DeleteAccountViewController
 * 注销页
 */
@property (nonatomic, strong) UIButton      *backBtn;   //返回btn
@property (nonatomic, strong) UILabel       *titleLabel; //标题lable
@property (nonatomic, strong) UIImageView   *deleteImageView;  //deleteImageView
@property (nonatomic, strong) UILabel       *detailLabel;  //detailLabel
@property (nonatomic, strong) UILabel       *accountLabel;  //accountLabel
@property (nonatomic, strong) UIButton      *deleteAccountBtn;  //注销Btn

@end

@implementation DeleteAccountViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initUI];
}

-(void)initUI{
    UIView *viewBack=[[UIView alloc] init];
    viewBack.frame = self.view.frame;
    viewBack.backgroundColor= RGB(0x18, 0x1D, 0x27);
    [self.view addSubview:viewBack];
    
    _backBtn = [UGCKitSmallButton buttonWithType:UIButtonTypeCustom];
    [_backBtn setImage:[UIImage imageNamed:@"backIcon"] forState:UIControlStateNormal];
    [_backBtn addTarget:self action:@selector(onGoBack:) forControlEvents:UIControlEventTouchUpInside];
    CGFloat top = [UIApplication sharedApplication].statusBarFrame.size.height;
    _backBtn.frame = CGRectMake(15, MAX(top+5, 30), 14 , 23);
    _backBtn.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleRightMargin;
    [self.view addSubview:_backBtn];
    
    _titleLabel = [[UILabel alloc] init];
    _titleLabel.textAlignment = NSTextAlignmentCenter;
    _titleLabel.textColor     = [UIColor whiteColor];
    _titleLabel.font          = [UIFont systemFontOfSize:22];
    _titleLabel.text  = NSLocalizedString(@"TCDeleteAccount.title", nil);
    CGRect mainScreenSize = [ UIScreen mainScreen ].bounds;
    CGSize titleTextSize  = [_titleLabel.text sizeWithAttributes:@{NSFontAttributeName:_titleLabel.font}];
    _titleLabel.frame = CGRectMake(0, MAX(top+5, 30),mainScreenSize.size.width,titleTextSize.height);
    [self.view addSubview:_titleLabel];
    
    _deleteImageView = [[UIImageView alloc] init];
    _deleteImageView.image = [UIImage imageNamed:@"delete_account.png"];
    _deleteImageView.frame = CGRectMake((mainScreenSize.size.width - 105)/2, MAX(top+5, 65 + titleTextSize.height), 105 , 105);
    [self.view addSubview:_deleteImageView];
    
    _detailLabel = [[UILabel alloc] init];
    _detailLabel.textColor     = [UIColor whiteColor];
    _detailLabel.font          = [UIFont systemFontOfSize:20];
    _detailLabel.text  = NSLocalizedString(@"TCDeleteAccount.detail", nil);
    _detailLabel.textAlignment = NSTextAlignmentCenter;
    _detailLabel.lineBreakMode = NSLineBreakByWordWrapping;
    _detailLabel.numberOfLines = 0;
    _detailLabel.frame = CGRectMake(20, 200, mainScreenSize.size.width - 40 , 100);
    [self.view addSubview:_detailLabel];
    
    NSString *accountString = [NSString stringWithFormat:@"%@%@", NSLocalizedString(@"TCDeleteAccount.currentAccount", nil),
    [TCLoginParam shareInstance].identifier];
    if([TCLoginParam shareInstance].isExpired){
        accountString = NSLocalizedString(@"TCDeleteAccount.notLogged", nil);
    }
    _accountLabel = [[UILabel alloc] init];
    _accountLabel.textColor     = [UIColor whiteColor];
    _accountLabel.font          = [UIFont systemFontOfSize:20];
    _accountLabel.text = accountString;
    _accountLabel.textAlignment = NSTextAlignmentCenter;
    _accountLabel.frame = CGRectMake(20, 280, mainScreenSize.size.width - 40, 40);
    [self.view addSubview:_accountLabel];
    
    _deleteAccountBtn = [[UIButton alloc] init];
    _deleteAccountBtn.frame = CGRectMake(20, 350, [[UIScreen mainScreen] bounds].size.width-40, 48);
    [_deleteAccountBtn setTitle:NSLocalizedString(@"TCSetting.cancelAccount", nil) forState:UIControlStateNormal];
    [_deleteAccountBtn setTitleColor:[UIColor colorWithRed:255255.0 green:0/255.0 blue:0/255.0 alpha:1/1.0]forState:UIControlStateNormal];
    _deleteAccountBtn.titleLabel.font = [UIFont systemFontOfSize:20];
    [_deleteAccountBtn addTarget:self action:@selector(openAlertWindow) forControlEvents:UIControlEventTouchUpInside];
    [_deleteAccountBtn.layer setCornerRadius:24];
    [_deleteAccountBtn.layer setBorderWidth:1];
    _deleteAccountBtn.layer.borderColor = [UIColor colorWithRed:255/255.0 green:0/255.0 blue:0/255.0 alpha:1].CGColor;
    _deleteAccountBtn.clipsToBounds = YES;
    [self.view addSubview:_deleteAccountBtn];
    if([TCLoginParam shareInstance].isExpired){
        _deleteAccountBtn.hidden = YES;
    }
}

-(void)deleteAccount{
    [[TCLoginModel sharedInstance] deleteAccount:[TCLoginParam shareInstance].identifier completion:^(int code) {
        if (code == 200) {
            NSLog(@"注销:%d",code);
            [[TCLoginParam shareInstance] clearLocal];
            TCLoginViewController *next = [[TCLoginViewController alloc] init];
            next.hidesBottomBarWhenPushed = YES;
            [self.navigationController pushViewController:next animated:YES];
            [self removeFromParentViewController];
        }else{
            NSLog(@"注销失败:%d",code);
        }
    }];
}

-(void)openAlertWindow{
    // 初始化UIAlertController
       UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"" message:@"" preferredStyle:UIAlertControllerStyleAlert];

       //修改title字体及颜色
       NSMutableAttributedString *titleStr = [[NSMutableAttributedString alloc] initWithString:NSLocalizedString(@"TCDeleteAccount.alertMsg", nil)];
       [titleStr addAttribute:NSForegroundColorAttributeName value:[UIColor blackColor] range:NSMakeRange(0, titleStr.length)];
       [titleStr addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:20] range:NSMakeRange(0, titleStr.length)];
       [alertController setValue:titleStr forKey:@"attributedMessage"];

       // 添加UIAlertAction
       UIAlertAction *sureAction = [UIAlertAction actionWithTitle:
       NSLocalizedString(@"TCDeleteAccount.confirm", nil)
       style:UIAlertActionStyleDestructive
       handler:^(UIAlertAction * _Nonnull action) {
           [self deleteAccount];
       }];
       // KVC修改字体颜色
       [sureAction setValue:[UIColor redColor] forKey:@"_titleTextColor"];
       [alertController addAction:sureAction];

       UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:
       NSLocalizedString(@"TCDeleteAccount.holdOn", nil)
       style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
       }];
       [cancelAction setValue:[UIColor colorWithRed:0/255.0 green:108/255.0 blue:255/255.0 alpha:1/1.0] forKey:@"_titleTextColor"];
       [alertController addAction:cancelAction];
       [self presentViewController:alertController animated:YES completion:nil];

}

- (void)onGoBack:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}




@end
