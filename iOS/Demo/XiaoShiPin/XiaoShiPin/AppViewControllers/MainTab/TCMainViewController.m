//
//  TCMainTabViewController.m
//  TCLVBIMDemo
//
//  Created by annidyfeng on 16/7/29.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "UGCKit.h"
#import "TCMainViewController.h"
#import "TCAccountInfoViewController.h"
#import "TCVideoListViewController.h"
#import "TCLoginModel.h"
#import "TCLoginParam.h"
#import "HUDHelper.h"
#import "TCUtil.h"
#import "AppDelegate.h"
#import "UIView+Additions.h"
#import "TCNavigationController.h"
#import "PhotoUtil.h"
#import "TXUGCPublish.h"
#import "SDKHeader.h"
#import "TCUserInfoModel.h"
#import "UGCKitWrapper.h"
#import "Mem.h"

#define BOTTOM_VIEW_HEIGHT              225

typedef NS_ENUM(NSInteger, TCVideoAction) {
    TCVideoActionNone,
    TCVideoActionSave,
    TCVideoActionPublish
};

@interface TCMainViewController ()<UITabBarControllerDelegate, TCLiveListViewControllerListener, UIGestureRecognizerDelegate>
@property UIButton *liveBtn;
@property (strong, nonatomic) UGCKitWrapper *ugcWrapper;  // UGC 业务逻辑
@end

@implementation TCMainViewController
{
    UGCKitTheme *_theme;
    TCVideoListViewController *_showVC;
    UIVisualEffectView        *_botttomView;
    MBProgressHUD             *_hub;
    TCVideoAction              _actionAfterSave;

    //pulish
    TXUGCPublish*       _videoPublish;
    MBProgressHUD*      _videoPublishHUD;

}

- (instancetype)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        _theme = [[UGCKitTheme alloc] init];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    _ugcWrapper = [[UGCKitWrapper alloc] initWithViewController:self theme:_theme];
//    _videoPublish = [[TXUGCPublish alloc] initWithUserID:[[TCUserInfoModel sharedInstance] getUserProfile].identifier];
//    _videoPublish.delegate = self;
    [self setupViewControllers];
    [self initBottomView];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self addChildViewMiddleBtn];
    __weak __typeof(self) wself = self;
    // 检查是否有未完成的录制
    NSArray *cachePathList = [[NSUserDefaults standardUserDefaults] objectForKey:CACHE_PATH_LIST];
    NSString *cacheFolder = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject]
                             stringByAppendingPathComponent:UGCKIT_PARTS_DIR];

    BOOL hasDraft = cachePathList && cachePathList.count > 0;
    if (hasDraft) {
        NSFileManager *manager = [[NSFileManager alloc] init];
        for (NSString *file in cachePathList) {
            if (![manager fileExistsAtPath:[cacheFolder stringByAppendingPathComponent: file]]) {
                hasDraft = NO;
                break;
            }
        }
    }
    if (hasDraft) {
        UIAlertController *controller = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"Common.Hint", nil)
                                                                            message:NSLocalizedString(@"TCVideoRecordView.ResumeRecord", nil)
                                                                     preferredStyle:UIAlertControllerStyleAlert];
        [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.OK", nil)
                                                       style:UIAlertActionStyleDefault
                                                     handler:^(UIAlertAction * _Nonnull action) {
            UGCKitRecordConfig *config = [[UGCKitRecordConfig alloc] init];
            config.recoverDraft = YES;
            [wself.ugcWrapper showRecordViewControllerWithConfig:config];
            _botttomView.hidden = YES;
        }]];
        [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.Cancel", nil)
                                                       style:UIAlertActionStyleCancel
                                                     handler:^(UIAlertAction * _Nonnull action) {
            //移除缓存数据
            NSArray *cachePathList = [[NSUserDefaults standardUserDefaults] objectForKey:CACHE_PATH_LIST];
            NSString *cacheFolder = [[[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:@"TXUGC"] stringByAppendingPathComponent:@"TXUGCParts"];
            for (NSInteger i = 0; i < cachePathList.count; i ++) {
                NSString *videoPath = [cacheFolder stringByAppendingPathComponent:cachePathList[i]];
                [TCUtil removeCacheFile:videoPath];
            }
            [[NSUserDefaults standardUserDefaults] setObject:nil forKey:CACHE_PATH_LIST];
            [[NSUserDefaults standardUserDefaults] synchronize];
        }]];
        [self presentViewController:controller animated:YES completion:nil];
    }
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    [self.tabBar invalidateIntrinsicContentSize];
    [self.tabBar bringSubviewToFront:self.liveBtn];
}

- (UIStatusBarStyle)preferredStatusBarStyle
{
    return UIStatusBarStyleLightContent;
}

- (void)setupViewControllers {
    WEAKIFY(self);
    _showVC = [TCVideoListViewController new];
    _showVC.loginHandler = ^(TCVideoListViewController *_) {
        STRONGIFY_OR_RETURN(self);
        if (self.loginHandler) {
            self.loginHandler(self);
        }
    };
    _showVC.listener = self;
    UIViewController *_ = [UIViewController new];
    TCAccountInfoViewController *accountInfoViewController = [[TCAccountInfoViewController alloc] init];
    accountInfoViewController.onLogout = ^(TCAccountInfoViewController * _Nonnull controller) {
        STRONGIFY_OR_RETURN(self);
        if (self.loginHandler) {
            self.loginHandler(self);
        }
//        [wself showlog
    };
    self.viewControllers = @[_showVC, _, accountInfoViewController];
    
    [self addChildViewController:_showVC imageName:@"video_normal" selectedImageName:@"video_click" title:nil];
    [self addChildViewController:_ imageName:@"" selectedImageName:@"" title:nil];
    [self addChildViewController:accountInfoViewController imageName:@"User_normal" selectedImageName:@"User_click" title:nil];
    
    self.delegate = self; // this make tabBaController call

    if([TCLoginParam shareInstance].isExpired){
        [self setSelectedIndex:2];
    }else{
        [self setSelectedIndex:0];
    }
}

- (void) initBottomView
{
    UIImage *shadowImage = [UIImage imageNamed:@"tabBarShadow"];
    UIImage *shadowLine = [UIImage imageNamed:@"tabBarShadow_line"];

    CGFloat lineWidth = (SCREEN_WIDTH-shadowImage.size.width)/2.0;

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(SCREEN_WIDTH, shadowImage.size.height),NO,0);
    [shadowLine drawInRect:CGRectMake(0, 0, lineWidth, shadowLine.size.height)];
    [shadowImage drawInRect:CGRectMake(lineWidth, 0, shadowImage.size.width, shadowImage.size.height)];
    [shadowLine drawInRect:CGRectMake(lineWidth+shadowImage.size.width, 0, lineWidth, shadowLine.size.height)];

    UIImage *finalShadow = UIGraphicsGetImageFromCurrentImageContext();
    self.tabBar.shadowImage = finalShadow;
    UIGraphicsEndImageContext();

    UIGraphicsBeginImageContext(CGSizeMake(1, 1));
    [[UIColor colorWithRed:0.15 green:0.17 blue:0.27 alpha:1.00] set];
    UIRectFill(CGRectMake(0, 0, 1, 1));
    [self.tabBar setBackgroundImage: UIGraphicsGetImageFromCurrentImageContext()];
    UIGraphicsEndImageContext();

    CGFloat bottomInset = 0;
    if (@available(iOS 11,*)) {
        UIEdgeInsets insets = [UIApplication sharedApplication].keyWindow.safeAreaInsets;
        bottomInset = insets.bottom;
    }
    CGFloat bottomViewHeight = bottomInset + BOTTOM_VIEW_HEIGHT;
    _botttomView = [[UIVisualEffectView alloc] initWithEffect:[UIBlurEffect effectWithStyle:UIBlurEffectStyleDark]];
    _botttomView.frame = CGRectMake(0, self.view.size.height - bottomViewHeight, self.view.width, bottomViewHeight);
    _botttomView.hidden = YES;
    [self.view addSubview:_botttomView];
    CGSize size = _botttomView.frame.size;

    int btnBkgViewHeight = 65;
    int btnSize = 50;//bottomViewHeight - barTopCap;


    UIView * btnBkgView = [[UIView alloc] initWithFrame:CGRectMake(0, size.height - btnBkgViewHeight, size.width, btnBkgViewHeight)];
    btnBkgView.backgroundColor = [UIColor clearColor];
    btnBkgView.userInteractionEnabled = YES;
    [_botttomView.contentView addSubview:btnBkgView];
    UITapGestureRecognizer* singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSingleTap:)];
    singleTap.delegate = self;
    [btnBkgView addGestureRecognizer:singleTap];

    UIImageView * imageHidden = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 25, 25)];
    imageHidden.image = [UIImage imageNamed:@"hidden"];
    imageHidden.center = CGPointMake(self.view.width / 2, btnBkgViewHeight / 2);
    [btnBkgView addSubview:imageHidden];

    UIButton *(^createButton)(NSString *title, NSString *imageName, SEL action) = ^(NSString *title, NSString *imageName, SEL action) {
        UGCKitVerticalButton * button = [[UGCKitVerticalButton alloc] initWithFrame:CGRectMake(0, 0, btnSize, btnSize)];
        button.verticalSpacing = 5;
        [button setImage:[UIImage imageNamed:imageName] forState:UIControlStateNormal];
        [button addTarget:self action:action forControlEvents:UIControlEventTouchUpInside];
        button.titleLabel.font = [UIFont systemFontOfSize:12];
        [button setTitle:NSLocalizedString(title, nil) forState:UIControlStateNormal];
        [button sizeToFit];
        return button;
    };

    UIButton * btnCamera = createButton(@"TCMainTabView.Record", @"tab_camera", @selector(onVideoBtnClicked));
    UIButton * btnChorus = createButton(@"TCMainTabView.Chorus", @"tab_chorus", @selector(onVideoChorusSelectClicked));
    UIButton * btnVideo = createButton(@"TCMainTabView.EditVideo", @"tab_video", @selector(onVideoSelectClicked));
    UIButton * btnPhoto = createButton(@"TCMainTabView.EditImage", @"tab_photo", @selector(onPictureSelectClicked));
    UIButton * btnTrio  = createButton(@"TCMainTabView.Trio", @"tab_trio", @selector(onVideoTrioSelectClicked));

    CGFloat centerDiff = self.view.width / 5;
    CGFloat centerX = centerDiff / 2;
    CGFloat centerY = _botttomView.height / 2 - 20;

    btnCamera.center  = CGPointMake(centerX, centerY);
    centerX += centerDiff;
    
    btnChorus.center = CGPointMake(centerX, centerY);
    centerX += centerDiff;
    
    btnTrio.center = CGPointMake(centerX, centerY);
    centerX += centerDiff;
    
    btnVideo.center = CGPointMake(centerX, centerY);
    centerX += centerDiff;

    btnPhoto.center = CGPointMake(centerX, centerY);

    [_botttomView.contentView addSubview:btnCamera];
    [_botttomView.contentView addSubview:btnVideo];
    [_botttomView.contentView addSubview:btnPhoto];
    [_botttomView.contentView addSubview:btnChorus];
    [_botttomView.contentView addSubview:btnTrio];
}

// 添加中间的加号按钮
- (void)addChildViewMiddleBtn {
    if (nil == self.liveBtn) {
        self.liveBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            btn.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin;
            [self.tabBar addSubview:btn];
            [btn setImage:[UIImage imageNamed:@"play_normal"] forState:UIControlStateNormal];
            [btn setImage:[UIImage imageNamed:@"play_click"] forState:UIControlStateSelected];
            btn.adjustsImageWhenHighlighted = NO;//去除按钮的按下效果（阴影）
            [btn addTarget:self action:@selector(onLiveButtonClicked) forControlEvents:UIControlEventTouchUpInside];
            btn.frame = CGRectMake(self.tabBar.frame.size.width/2-60, 0, 120, 120);
            btn.imageEdgeInsets = UIEdgeInsetsMake(0, 35, 75, 35);
            btn;
        });
    } else {
//        UIButton *btn = self.liveBtn;
//        btn.frame = CGRectMake(self.tabBar.frame.size.width/2-60, 0, 120, 120);
    }
}

- (void)addChildViewController:(UIViewController *)childController imageName:(NSString *)normalImg selectedImageName:(NSString *)selectImg title:(NSString *)title {
    TCNavigationController *nav = [[TCNavigationController alloc] initWithRootViewController:childController];
    if (normalImg.length > 0) {
        childController.tabBarItem.image = [[UIImage imageNamed:normalImg] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    }
    if (selectImg.length > 0) {
        childController.tabBarItem.selectedImage = [[UIImage imageNamed:selectImg] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    }
    childController.title = title;

    [self addChildViewController:nav];
}

- (BOOL)tabBarController:(UITabBarController *)tabBarController shouldSelectViewController:(UIViewController *)viewController {
    return YES;
}

- (void)onLiveButtonClicked {
    if(![self checkLoginStatus]) return;
    if (_botttomView) {
        [_botttomView removeFromSuperview];
        [self.view addSubview:_botttomView];
        _botttomView.hidden = NO;
    }
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

-(void)handleSingleTap:(UITapGestureRecognizer *)sender
{
    if (_botttomView) {
        _botttomView.hidden = YES;
    }
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
    return !_botttomView.hidden;
}

-(BOOL)checkLoginStatus{
    if([TCLoginParam shareInstance].isExpired){
        // TODO: logout
        if (self.loginHandler) {
            self.loginHandler(self);
        }
        return NO;
    }
    return YES;
}

-(void)onVideoBtnClicked
{
    UGCKitRecordConfig *config = [[UGCKitRecordConfig alloc] init];
    UGCKitWatermark *watermark = [[UGCKitWatermark alloc] init];
    watermark.image = [UIImage imageNamed:@"watermark"];
    watermark.frame = CGRectMake(0.01, 0.01, 0.1, 0.3);
    config.watermark = watermark;
    [self.ugcWrapper showRecordViewControllerWithConfig:config];
    _botttomView.hidden = YES;
}

#pragma mark -

-(void)onEnterPlayViewController
{
    if (_botttomView) {
        _botttomView.hidden = YES;
    }
}

- (void)_showVideoCutView:(UGCKitResult *)result inNavigationController:(UINavigationController *)nav {
    UGCKitCutViewController *vc = [[UGCKitCutViewController alloc] initWithMedia:result.media theme:_theme];
    __weak __typeof(self) wself = self;
    __weak UINavigationController *weakNavigation = nav;
    vc.completion = ^(UGCKitResult *result, int rotation) {
        if ([result isCancelled]) {
            [wself dismissViewControllerAnimated:YES completion:nil];
        } else {
            [wself.ugcWrapper showEditViewController:result rotation:rotation inNavigationController:weakNavigation backMode:TCBackModePop];
        }
    };
    [nav pushViewController:vc animated:YES];
}

-(void)onVideoSelectClicked
{
    UGCKitMediaPickerConfig *config = [[UGCKitMediaPickerConfig alloc] init];
    config.mediaType = UGCKitMediaTypeVideo;
    config.maxItemCount = NSIntegerMax;
    UGCKitMediaPickerViewController *imagePickerController = [[UGCKitMediaPickerViewController alloc] initWithConfig:config theme:_theme];
    TCNavigationController *nav = [[TCNavigationController alloc] initWithRootViewController:imagePickerController];
    nav.modalPresentationStyle = UIModalPresentationFullScreen;
    __weak __typeof(self) wself = self;
    __weak UINavigationController *navigationController = nav;
    imagePickerController.completion = ^(UGCKitResult *result) {
        if (!result.cancelled && result.code == 0) {
            [wself _showVideoCutView:result inNavigationController:navigationController];
        } else {
            NSLog(@"isCancelled: %c, failed: %@", result.cancelled ? 'y' : 'n', result.info[NSLocalizedDescriptionKey]);
            [wself dismissViewControllerAnimated:YES completion:^{
                if (!result.cancelled) {
                    UIAlertController *alert =
                    [UIAlertController alertControllerWithTitle:result.info[NSLocalizedDescriptionKey]
                                                        message:nil
                                                 preferredStyle:UIAlertControllerStyleAlert];
                    [alert addAction:[UIAlertAction actionWithTitle:@"确定"
                                                              style:UIAlertActionStyleDefault
                                                            handler:nil]];
                    [self presentViewController:alert animated:YES completion:nil];
                }
            }];
        }
    };
    [self presentViewController:nav animated:YES completion:NULL];
    _botttomView.hidden = YES;
}

-(void)onPictureSelectClicked
{
    UGCKitMediaPickerConfig *config = [[UGCKitMediaPickerConfig alloc] init];
    config.mediaType = UGCKitMediaTypePhoto;
    config.minItemCount = 3;
    config.maxItemCount = NSIntegerMax;
    UGCKitMediaPickerViewController *imagePickerController = [[UGCKitMediaPickerViewController alloc] initWithConfig:config theme:_theme];
    TCNavigationController *nav = [[TCNavigationController alloc] initWithRootViewController:imagePickerController];
    nav.modalPresentationStyle = UIModalPresentationFullScreen;
    __weak __typeof(self) wself = self;
    __weak UINavigationController *navigationController = nav;
    imagePickerController.completion = ^(UGCKitResult *result) {
        if (!result.cancelled && result.code == 0) {
            [wself _showVideoCutView:result inNavigationController:navigationController];
        } else {
            NSLog(@"isCancelled: %c, failed: %@", result.cancelled ? 'y' : 'n', result.info[NSLocalizedDescriptionKey]);
            [wself dismissViewControllerAnimated:YES completion:nil];
        }
    };
    [self presentViewController:nav animated:YES completion:NULL];
    _botttomView.hidden = YES;
}

-(void)onVideoChorusSelectClicked
{
    if(![self checkLoginStatus]){
        return;
    }
    [TCUtil report:xiaoshipin_videochorus userName:nil code:0 msg:@"合唱事件"];
    _hub = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    _hub.mode = MBProgressHUDModeText;
    _hub.label.text = NSLocalizedString(@"TCVodPlay.VideoLoading", nil);
    
    __weak __typeof(self) weakSelf = self;
    NSString *ducumentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    NSString *cachePath = [ducumentPath stringByAppendingPathComponent: @"Chorus.mp4"];
    if ([[NSFileManager defaultManager] fileExistsAtPath:cachePath]){
        [self onloadVideoComplete:@[cachePath] recordStyle:UGCKitRecordStyleDuet];
    }else{
        [TCUtil downloadVideo:DEFAULT_CHORUS_URL cachePath:cachePath  process:^(CGFloat process) {
            [weakSelf onloadVideoProcess:process];
        } complete:^(NSString *videoPath) {
            [weakSelf onloadVideoComplete:@[videoPath] recordStyle:UGCKitRecordStyleDuet];
        }];
    }
    _botttomView.hidden = YES;
}

-(void)onVideoTrioSelectClicked
{
    if(![self checkLoginStatus]){
        return;
    }
    [TCUtil report:xiaoshipin_videotrio userName:nil code:0 msg:@"三屏合唱事件"];
    _hub = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    _hub.mode = MBProgressHUDModeText;
    _hub.label.text = NSLocalizedString(@"TCVodPlay.VideoLoading", nil);
    
    __weak __typeof(self) weakSelf = self;
    NSString *ducumentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    NSString *cachePath = [ducumentPath stringByAppendingPathComponent: @"Chorus.mp4"];
    if ([[NSFileManager defaultManager] fileExistsAtPath:cachePath]){
        [self onloadVideoComplete:@[cachePath, cachePath] recordStyle:UGCKitRecordStyleTrio];
    }else{
        [TCUtil downloadVideo:DEFAULT_CHORUS_URL cachePath:cachePath  process:^(CGFloat process) {
            [weakSelf onloadVideoProcess:process];
        } complete:^(NSString *videoPath) {
            [weakSelf onloadVideoComplete:@[videoPath, videoPath] recordStyle:UGCKitRecordStyleTrio];
        }];
    }
    _botttomView.hidden = YES;
}

-(void)onloadVideoProcess:(CGFloat)process {
    _hub.label.text = [NSString stringWithFormat:NSLocalizedString(@"TCVodPlay.VideoLoadingFmt", nil),(int)(process * 100)];
}

-(void)onloadVideoComplete:(NSArray<NSString *> *)videoPaths recordStyle:(UGCKitRecordStyle)recordStyle {
    if (videoPaths.count) {
        UGCKitRecordConfig *config = [[UGCKitRecordConfig alloc] init];
        config.chorusVideos = videoPaths;
        config.recordStyle = recordStyle;
        if (UGCKitRecordStyleTrio == recordStyle) {
            config.ratio = VIDEO_ASPECT_RATIO_4_3;
        }
        [self.ugcWrapper showRecordViewControllerWithConfig:config];
        [_hub hideAnimated:YES];
    }else{
        _hub.label.text = NSLocalizedString(@"TCVodPlay.VideoLoadFailed", nil);
        [_hub hideAnimated:YES afterDelay:1.0];
    }
}

@end
