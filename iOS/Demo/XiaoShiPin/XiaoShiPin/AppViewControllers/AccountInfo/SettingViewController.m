//
//  SettingViewController.m
//  XiaoShiPinApp
//
//  Created by tao yue on 2022/3/11.
//  Copyright © 2022 Tencent. All rights reserved.
//

#import "SettingViewController.h"
#import "ColorMacro.h"
#import "UGCKit.h"
#import "TCUserInfoCell.h"
#import "TCWebViewController.h"
#import "TCUtil.h"
#import "DetailViewController.h"
#import "DeleteAccountViewController.h"
static NSString * const HomePageURL = @"https://cloud.tencent.com/product/ugsv";
#define L(X) NSLocalizedString((X), nil)

@interface SettingViewController ()
/*
 * SettingViewController 类说明 : 该类显示设置的界面
 */
@property (nonatomic, strong) UIButton      *backBtn;  //返回btn
@property (nonatomic, strong) UILabel       *titleLabel;  //标题lable
@property (strong, nonatomic) UITableView   *dataTable;   //dataTable
@property (strong, nonatomic) NSMutableArray *userInfoUISetArry;   //userInfoUISetArry

@end

@implementation SettingViewController

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
    _titleLabel.text  = NSLocalizedString(@"TCAccountInfo.setting", nil);
    CGRect mainScreenSize = [ UIScreen mainScreen ].bounds;
    CGSize titleTextSize  = [_titleLabel.text sizeWithAttributes:@{NSFontAttributeName:_titleLabel.font}];
    _titleLabel.frame = CGRectMake(0, MAX(top+5, 30),mainScreenSize.size.width,titleTextSize.height);
    [self.view addSubview:_titleLabel];
    
    // 初始化需要绘制在tableview上的数据
    __weak typeof(self) ws = self;
    
    TCUserInfoCellItem *settingItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCSetting.cancelAccount", nil) value:nil
    type:TCUserInfo_About rightText:[TXLiveBase getSDKVersionStr]
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) { [ws gotoDelete:menu cell:cell]; } ];
    
    CGFloat tableHeight = CGRectGetHeight(self.view.bounds) - MAX(top+5, 30) - titleTextSize.height;
    //    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[backFaceItem,setItem, aboutItem]];
    
    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[settingItem]];
    
    //设置tableview属性
    CGRect frame = CGRectMake(self.view.frame.origin.x,
    self.view.frame.origin.y + MAX(top+5, 30) + titleTextSize.height,
    self.view.frame.size.width, tableHeight);
    
    _dataTable = [[UITableView alloc] initWithFrame:frame style:UITableViewStylePlain];
    _dataTable.backgroundColor = UIColor.clearColor;
    [_dataTable setDelegate:self];
    [_dataTable setDataSource:self];
    [_dataTable setScrollEnabled:NO];
    [_dataTable setSeparatorColor:[UIColor clearColor]];
    [self.view addSubview:_dataTable];
}

- (void)onShowAppVersion:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell{
    DetailViewController *next = [[DetailViewController alloc] init];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}


- (void)gotoDelete:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell{
    DeleteAccountViewController *next = [[DeleteAccountViewController alloc] init];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
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
    TCUserInfoTableViewCell *cell = (TCUserInfoTableViewCell*)[tableView  dequeueReusableCellWithIdentifier:@"cell_default"];
    if(cell == nil) {
        cell = [[TCUserInfoTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"cell_default"];
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

- (void)onGoBack:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

@end
