//
//  DetailViewController.m
//  XiaoShiPinApp
//
//  Created by tao yue on 2022/3/11.
//  Copyright © 2022 Tencent. All rights reserved.
//

#import "DetailViewController.h"
#import "ColorMacro.h"
#import "UGCKit.h"

@interface DetailViewController ()
/*
 * DetailViewController 类说明 : 该类显示app描述的界面
 */
@property (nonatomic, strong) UIButton      *backBtn; //返回btn
@property (nonatomic, strong) UILabel       *titleLabel;  //标题lable
@property (nonatomic, strong) UIImageView   *logoImageView; //logo
@property (nonatomic, strong) UILabel       *detailLabel; //详情

@end

@implementation DetailViewController

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
    
    _logoImageView = [[UIImageView alloc] init];
    _logoImageView.image = [UIImage imageNamed:@"tCloud_image.png"];
    _logoImageView.frame = CGRectMake((mainScreenSize.size.width - 105)/2, MAX(top+5, 65 + titleTextSize.height), 105 , 105);
    [self.view addSubview:_logoImageView];
    
    _detailLabel = [[UILabel alloc] init];
    _detailLabel.textColor     = [UIColor whiteColor];
    _detailLabel.font          = [UIFont systemFontOfSize:20];
    _detailLabel.text  = NSLocalizedString(@"ugckit_about_tip1", nil);
    _detailLabel.lineBreakMode = NSLineBreakByWordWrapping;
    _detailLabel.numberOfLines = 0;
    _detailLabel.frame = CGRectMake(20, 180, mainScreenSize.size.width - 40 , 300);
    [self.view addSubview:_detailLabel];
}

- (void)onGoBack:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}


@end
