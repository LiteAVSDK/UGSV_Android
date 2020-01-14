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
static NSString * const HomePageURL = @"https://cloud.tencent.com/product/ugsv";
#define L(X) NSLocalizedString((X), nil)


extern BOOL g_bNeedEnterPushSettingView;

@interface TCAccountInfoViewController () < UIPickerViewDataSource, UIPickerViewDelegate >

@property (nonatomic, strong) NSMutableArray *logFilesArray;

@property (nonatomic, strong) UIView *logUploadView;

@property (nonatomic, strong) UIPickerView *logPickerView;

@property (nonatomic, strong) UILongPressGestureRecognizer *longPressGesture;

@end

@implementation TCAccountInfoViewController
{
    UIButton *_loginBtn;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:KReloadUserInfoNotification object:nil];
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
        if (self.onLogout) {
            self.onLogout(self);
        }
        [self.dataTable reloadData];
    }];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // 设置通知消息,接受到通知后重绘cell,确保更改后的用户资料能同步到用户信息界面
    [[NSNotificationCenter defaultCenter] removeObserver:self name:KReloadUserInfoNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateUserInfoOnController:) name:KReloadUserInfoNotification object:nil];
    
    [self initUI];
    return;
}

-(void)initUI
{
    UIView *viewBack=[[UIView alloc] init];
    viewBack.frame = self.view.frame;
    viewBack.backgroundColor= RGB(0x18, 0x1D, 0x27);
    [self.view addSubview:viewBack];
    
    // 初始化需要绘制在tableview上的数据
    __weak typeof(self) ws = self;
    TCUserInfoCellItem *backFaceItem = [[TCUserInfoCellItem alloc] initWith:@"" value:@"" type:TCUserInfo_View action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
         [ws onEditUserInfo:menu cell:cell];
        nil; }];
    
    //    TCUserInfoCellItem *setItem = [[TCUserInfoCellItem alloc] initWith:@"编辑个人信息" value:nil type:TCUserInfo_Edit action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) {
    //        [ws onEditUserInfo:menu cell:cell]; } ];
    
    TCUserInfoCellItem *aboutItem = [[TCUserInfoCellItem alloc] initWith:NSLocalizedString(@"TCUserInfoView.HintAboutApp", nil) value:nil type:TCUserInfo_About action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) { [ws onShowAppVersion:menu cell:cell]; } ];
    
    TCUserInfoCellItem *getSupportItem = [[TCUserInfoCellItem alloc] initWith:NSLocalizedString(@"获取技术支持服务", nil) value:nil type:TCUserInfo_About action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) { [ws onShowAppSupport:menu cell:cell]; } ];
    
    TCUserInfoCellItem *aboutSDKItem = [[TCUserInfoCellItem alloc] initWith:NSLocalizedString(@"TCUserInfoView.ProductIntroduction", nil) value:nil type:TCUserInfo_About action:^(TCUserInfoCellItem *menu, TCUserInfoTableViewCell *cell) { [ws onShowSDKInfo:menu cell:cell]; } ];

    
    CGFloat tableHeight = CGRectGetHeight(self.view.bounds);
    CGFloat quitBtnYSpace = tableHeight + 20;
    //    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[backFaceItem,setItem, aboutItem]];
    
    _userInfoUISetArry = [NSMutableArray arrayWithArray:@[backFaceItem,aboutItem,getSupportItem,aboutSDKItem]];
    
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
    
    _logUploadView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.frame), CGRectGetHeight(self.view.frame))];
    _logUploadView.backgroundColor = RGBA(0, 0, 0, 0.6);
    _logUploadView.hidden = YES;
    
    CGFloat yPosition = CGRectGetHeight(_logUploadView.frame) * 0.3;
    UIView *logUploadPanel = [[UIView alloc] initWithFrame:CGRectMake(0, yPosition, CGRectGetWidth(_logUploadView.frame), CGRectGetHeight(_logUploadView.frame) - yPosition)];
    logUploadPanel.backgroundColor = [UIColor whiteColor];
    [_logUploadView addSubview:logUploadPanel];
    
    _logPickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(logUploadPanel.frame), 200)];
    _logPickerView.dataSource = self;
    _logPickerView.delegate = self;
    [logUploadPanel addSubview:_logPickerView];
    
    UIButton* uploadButton = [UIButton buttonWithType:UIButtonTypeSystem];
    uploadButton.frame = CGRectMake(0, CGRectGetMaxY(_logPickerView.frame), CGRectGetWidth(logUploadPanel.frame), 40);
    [uploadButton setTitle:@"分享上传日志" forState:UIControlStateNormal];
    [uploadButton addTarget:self action:@selector(onSharedUploadLog:) forControlEvents:UIControlEventTouchUpInside];
    [logUploadPanel addSubview:uploadButton];
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onLogUploadViewTapped:)];
    tapGesture.enabled = YES;
    _logUploadView.userInteractionEnabled = YES;
    [_logUploadView addGestureRecognizer:tapGesture];
    
    [self.view addSubview:_logUploadView];
    
    _longPressGesture = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleLongPress:)];
    _longPressGesture.enabled = YES;
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
    [_dataTable reloadData];
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
    [_dataTable reloadData];
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
    TCUserInfoTableViewCell *cell = (TCUserInfoTableViewCell*)[tableView  dequeueReusableCellWithIdentifier:@"cell"];
    if(cell == nil) {
        cell = [[TCUserInfoTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"cell"];
        [cell initUserinfoViewCellData:item];
        if (TCUserInfo_View == item.type
            && NO == [cell.gestureRecognizers containsObject:_longPressGesture]) {
            [cell addGestureRecognizer:_longPressGesture];
        } else {
            [cell removeGestureRecognizer:_longPressGesture];
        }
    }
    
    [cell drawRichCell:item];
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

/// 获取技术支持按钮事件
- (void)onShowAppSupport:(id)menu cell:(id)cell
{
    NSString *message = [@[L(@"关注公众号“腾讯云视频”"), L(@"给公众号发送“小视频”")] componentsJoinedByString:@"\n"];
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"获取技术支持服务", nil)
                                                                        message:message
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.OK", nil) style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];
}

/**
 *  用户显示小直播的版本号信息
 *
 *  @param menu 无意义
 *  @param cell 无意义
 */
- (void)onShowAppVersion:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell
{
    NSString* rtmpSDKVersion = [NSString stringWithFormat:NSLocalizedString(@"TCUserInfoView.InfoRTMPFmt", nil),[TXLiveBase getSDKVersionStr]];
    NSString* appVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
    
    NSString *info = [NSString stringWithFormat:NSLocalizedString(@"TCUserInfoView.InfoAppFmt", nil), appVersion, rtmpSDKVersion];
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"TCUserInfoView.HintAboutApp", nil)
                                                                        message:info
                                                                 preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.Close", nil) style:UIAlertActionStyleCancel handler:nil]];
    [self presentViewController:controller animated:YES completion:nil];

}

/**
 *  用户显示SDK信息
 *
 *  @param menu 无意义
 *  @param cell 无意义
 */
- (void)onShowSDKInfo:(TCUserInfoCellItem *)menu cell:(TCUserInfoTableViewCell *)cell
{
    [TCUtil report:xiaoshipin_about_sdk userName:nil code:0 msg:@"点击关于SDK"];
    TCWebViewController *next = [[TCWebViewController alloc] initWithURL:HomePageURL];
    next.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:next animated:YES];
//    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://cloud.tencent.com/product/UGSV"]];
}

- (void)handleLongPress:(UILongPressGestureRecognizer *)longPressGesture
{
    if (longPressGesture.state == UIGestureRecognizerStateBegan) {
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *logDoc = [NSString stringWithFormat:@"%@%@", paths[0], @"/log"];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        NSArray* fileArray = [fileManager contentsOfDirectoryAtPath:logDoc error:nil];
        fileArray = [fileArray sortedArrayUsingComparator:^NSComparisonResult(id  _Nonnull obj1, id  _Nonnull obj2) {
            NSString* file1 = (NSString*)obj1;
            NSString* file2 = (NSString*)obj2;
            return [file1 compare:file2] == NSOrderedDescending;
        }];
        self.logFilesArray = [NSMutableArray new];
        for (NSString* logName in fileArray) {
            if ([logName hasSuffix:@"xlog"]) {
                [self.logFilesArray addObject:logName];
            }
        }
        
        _logUploadView.alpha = 0.1;
        [UIView animateWithDuration:0.5 animations:^{
            _logUploadView.hidden = NO;
            _logUploadView.alpha = 1;
        }];
        [_logPickerView reloadAllComponents];
    }
}

- (void)onLogUploadViewTapped:(UITapGestureRecognizer *)tapGesture
{
    if (!_logUploadView.hidden) {
        _logUploadView.hidden = YES;
    }
}

- (void)onSharedUploadLog:(UIButton*)sender
{
    NSInteger row = [_logPickerView selectedRowInComponent:0];
    if (row < self.logFilesArray.count) {
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *logDoc = [NSString stringWithFormat:@"%@%@", paths[0], @"/log"];
        NSString* logPath = [logDoc stringByAppendingPathComponent:self.logFilesArray[row]];
        NSURL *shareobj = [NSURL fileURLWithPath:logPath];
        UIActivityViewController *activityView = [[UIActivityViewController alloc] initWithActivityItems:@[shareobj] applicationActivities:nil];
        [self presentViewController:activityView animated:YES completion:^{
            _logUploadView.hidden = YES;
        }];
    }
}

#pragma mark - UIPickerViewDataSource

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
}

// returns the # of rows in each component..
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    return self.logFilesArray.count;
}

#pragma mark - UIPickerViewDelegate

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    if (row < self.logFilesArray.count) {
        return (NSString*)self.logFilesArray[row];
    }
    return @"";
}

@end
