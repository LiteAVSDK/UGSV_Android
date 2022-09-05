//
//  AboutViewController.m
//  XiaoShiPinApp
//
//  Created by tao yue on 2022/3/10.
//  Copyright © 2022 Tencent. All rights reserved.
//

#import "AboutViewController.h"
#import "ColorMacro.h"
#import "UGCKit.h"
#import "TCUserInfoCell.h"
#import "TCWebViewController.h"
#import "TCUtil.h"
#import "DetailViewController.h"
static NSString * const HomePageURL = @"https://cloud.tencent.com/product/ugsv";
#define L(X) NSLocalizedString((X), nil)

@interface AboutViewController ()
/*
 * AboutViewController
 * 详情页
 */
@property (nonatomic, strong) UIButton      *backBtn; //返回btn
@property (nonatomic, strong) UILabel       *titleLabel;//titleLable
@property (strong, nonatomic) UITableView   *dataTable;//dataTable
@property (strong, nonatomic) NSMutableArray *userInfoUISetArry;//userInfoUISetArry
@property (nonatomic, strong) UIButton      *downloadBtn;//下载btn
@property (nonatomic, strong) UIButton      *detailBtn; //详情

@end

@implementation AboutViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initUI];
    // Do any additional setup after loading the view.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

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
    _titleLabel.text  = NSLocalizedString(@"TCAccountInfo.about", nil);
    CGRect mainScreenSize = [ UIScreen mainScreen ].bounds;
    CGSize titleTextSize  = [_titleLabel.text sizeWithAttributes:@{NSFontAttributeName:_titleLabel.font}];
    _titleLabel.frame = CGRectMake(0, MAX(top+5, 30),mainScreenSize.size.width,titleTextSize.height);
    [self.view addSubview:_titleLabel];
    
    // 初始化需要绘制在tableview上的数据
    __weak typeof(self) ws = self;
    
    TCUserInfoCellItem *settingItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAbount.sdkVersion", nil)value:nil
    type:TCUserInfo_RightText rightText:[TXLiveBase getSDKVersionStr]
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) { } ];
    
    TCUserInfoCellItem *privacyItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAbount.appVersion", nil) value:nil
    type:TCUserInfo_RightText rightText:
    [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"]
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) { } ];
    
    TCUserInfoCellItem *agreementItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCDeleteAccount.title", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell){ [ws gotoDetail:menu cell:cell]; } ];
    
    TCUserInfoCellItem *aboutItem = [[TCUserInfoCellItem alloc] initWith:
    NSLocalizedString(@"TCAbount.support", nil) value:nil
    type:TCUserInfo_About rightText:nil
    action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell)
    { [ws onShowAppSupport:menu cell:cell]; } ];
    
    CGFloat tableHeight = CGRectGetHeight(self.view.bounds) - MAX(top+5, 30) - titleTextSize.height;
    //    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[backFaceItem,setItem, aboutItem]];
    
    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[settingItem,privacyItem,agreementItem,aboutItem]];
    
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
    
    NSString *downloadText = NSLocalizedString(@"TCAbount.download", nil);
    NSMutableAttributedString *str = [[NSMutableAttributedString alloc] initWithString:downloadText];
    [str addAttribute:NSForegroundColorAttributeName value:RGB(0x40,0xe0,0xd0) range:NSMakeRange(0,downloadText.length)];
    [str addAttribute:NSUnderlineStyleAttributeName
    value:[NSNumber numberWithInteger:NSUnderlineStyleSingle | NSUnderlinePatternSolid]
    range:NSMakeRange(0, downloadText.length)];
    
    _downloadBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    _downloadBtn.frame = CGRectMake(0, 260, self.view.frame.size.width, 45);
    _downloadBtn.titleLabel.font = [UIFont systemFontOfSize:16];
    [_downloadBtn setAttributedTitle:str forState: UIControlStateNormal];
    [_downloadBtn setBackgroundColor:RGB(0x1F,0x25,0x31)];
    [_downloadBtn addTarget:self action:@selector(gotoWeb) forControlEvents:UIControlEventTouchUpInside];
    _downloadBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    _downloadBtn.contentEdgeInsets = UIEdgeInsetsMake(0,15, 0, 0);
    [self.view addSubview:_downloadBtn];
    
    NSString *productText = NSLocalizedString(@"TCAbount.product", nil);
    NSMutableAttributedString *str1 = [[NSMutableAttributedString alloc] initWithString:productText];
    [str1 addAttribute:NSForegroundColorAttributeName value:RGB(0x40,0xe0,0xd0) range:NSMakeRange(0,productText.length)];
    [str1 addAttribute:NSUnderlineStyleAttributeName
    value:[NSNumber numberWithInteger:NSUnderlineStyleSingle | NSUnderlinePatternSolid]
    range:NSMakeRange(0, productText.length)];
    
    [self addDetailBtn:str1];
}

-(void)addDetailBtn:(NSAttributedString *)str{
    _detailBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    _detailBtn.frame = CGRectMake(0, 310, self.view.frame.size.width, 45);
    _detailBtn.titleLabel.font = [UIFont systemFontOfSize:16];
    [_detailBtn setAttributedTitle:str forState: UIControlStateNormal];
    [_detailBtn setBackgroundColor:RGB(0x1F,0x25,0x31)];
    [_detailBtn addTarget:self action:@selector(gotoWeb) forControlEvents:UIControlEventTouchUpInside];
    _detailBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    _detailBtn.contentEdgeInsets = UIEdgeInsetsMake(0,15, 0, 0);
    [self.view addSubview:_detailBtn];
}

- (void)onShowAppWeb:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell{
       TCWebViewController *next = [[TCWebViewController alloc] initWithURL:HomePageURL];
       next.hidesBottomBarWhenPushed = YES;
       [self.navigationController pushViewController:next animated:YES];
}

- (void)gotoWeb{
    TCWebViewController *next = [[TCWebViewController alloc] initWithURL:HomePageURL];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
}

- (void)onShowAppSupport:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell{
    NSString *message = [@[L(@"关注公众号“腾讯云视频”"), L(@"给公众号发送“小视频”")] componentsJoinedByString:@"\n"];
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"获取技术支持服务", nil)
                                                                        message:message
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.OK", nil) style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];
}

- (void)gotoDetail:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell{
    DetailViewController *next = [[DetailViewController alloc] init];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];}

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
    }else if (TCUserInfo_About == item.type){
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
    }else{
        TCUserInfoTableViewCell *cell = (TCUserInfoTableViewCell*)[tableView  dequeueReusableCellWithIdentifier:@"cell_default"];
        if(cell == nil) {
            cell = [[TCUserInfoTableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"cell_default"];
            [cell initUserinfoViewCellData:item];
        }
        [cell drawRichCell:item];
        cell.accessoryType = UITableViewCellAccessoryNone;
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

- (void)onGoBack:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

@end
