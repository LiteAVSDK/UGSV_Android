//
//  TCVideoListViewController.m
//  TCLVBIMDemo
//
//  Created by annidyfeng on 16/7/29.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCVideoListViewController.h"
#import "TCVideoListCell.h"
#import "TCLiveListModel.h"
#import <MJRefresh/MJRefresh.h>
#import "HUDHelper.h"
//#import <MJExtension/MJExtension.h>
//#import <BlocksKit/BlocksKit.h>
#import "TCLiveListModel.h"
#import "TCLoginParam.h"
#import "TCVodPlayViewController.h"
#import "ColorMacro.h"
#import "Mem.h"
#import "UIView+Additions.h"
#import "AppDelegate.h"

@interface TCVideoListViewController ()<UICollectionViewDelegate,UICollectionViewDataSource>

@property TCLiveListMgr *liveListMgr;

@property(nonatomic, strong) NSMutableArray *lives;
@property(nonatomic, strong) UICollectionView *collectionView;
@property BOOL isLoading;

@end

@implementation TCVideoListViewController
{
    BOOL             _hasEnterplayVC;
    UIButton         *_ugcVideoBtn;
    UIView           *_scrollView;
    UIView           *_nullDataView;
    CGFloat          scrollViewWidth;
    CGFloat          scrollViewHeight;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        self.lives = [NSMutableArray array];
        _liveListMgr = [TCLiveListMgr sharedMgr];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(newDataAvailable:) name:kTCLiveListNewDataAvailable object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(listDataUpdated:) name:kTCLiveListUpdated object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(svrError:) name:kTCLiveListSvrError object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playError:) name:kTCLivePlayError object:nil];
    }
    return self;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    CGFloat statusBarHeight = CGRectGetHeight([UIApplication sharedApplication].statusBarFrame);

    UIImageView *topGradient = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.bounds), statusBarHeight)];
    topGradient.contentMode = UIViewContentModeScaleToFill;
    topGradient.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleBottomMargin;
    self.navigationItem.title = NSLocalizedString(@"TCLiveListView.NewLive", nil);
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(2, statusBarHeight), NO, 1);
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    NSArray *colors = @[(__bridge id)[UIColor colorWithRed:0 green:0 blue:0 alpha:0.5].CGColor,
                        (__bridge id)[UIColor colorWithRed:0 green:0 blue:0 alpha:0].CGColor];
    CGFloat locations[2] = {0, 1};
    CGGradientRef gradient = CGGradientCreateWithColors(colorSpace, (__bridge CFArrayRef)colors, locations);
    CGColorSpaceRelease(colorSpace);
    CGContextDrawLinearGradient(UIGraphicsGetCurrentContext(), gradient, CGPointMake(0, 0), CGPointMake(0, statusBarHeight), 0);
    CGGradientRelease(gradient);
    topGradient.image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();


    CGFloat btnWidth         = 38;
    CGFloat btnHeight        = 24;
    CGFloat statuBarHeight   = [UIApplication sharedApplication].statusBarFrame.size.height;
    scrollViewWidth  = 70;
    scrollViewHeight = 3;
    UIColor *backgroundColor = [UIColor blackColor];
    UIColor *topTitleColor = [UIColor whiteColor];
//    if (@available(iOS 13.0, *)) {
//        backgroundColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
//            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
//                return [UIColor blackColor];
//            } else {
//                return [UIColor whiteColor];
//            }
//        }];
//        topTitleColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
//            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
//                return [UIColor whiteColor];
//            } else {
//                return [UIColor blackColor];
//            }
//        }];
//    }

    
    UIView *tabView = [[UIView alloc] initWithFrame:CGRectMake(0,statuBarHeight,SCREEN_WIDTH, 44)];
    tabView.backgroundColor = backgroundColor;

    _ugcVideoBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_ugcVideoBtn setTitle:NSLocalizedString(@"Common.App", nil) forState:UIControlStateNormal];
    [_ugcVideoBtn setTitleColor:topTitleColor forState:UIControlStateNormal];
    _ugcVideoBtn.titleLabel.font = [UIFont systemFontOfSize:18];
    [_ugcVideoBtn sizeToFit];
    btnWidth = _ugcVideoBtn.width - 20;
    [_ugcVideoBtn setFrame:CGRectMake(self.view.width / 2 - (btnWidth + 20) / 2 , 11, btnWidth + 20, btnHeight)];
    [_ugcVideoBtn addTarget:self action:@selector(videoBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    
    _scrollView = [[UIView alloc] initWithFrame:CGRectMake(_ugcVideoBtn.left - (scrollViewWidth - _ugcVideoBtn.width)/2, _ugcVideoBtn.bottom + 5, scrollViewWidth, scrollViewHeight)];
//    _scrollView.backgroundColor = UIColorFromRGB(0xFF0ACBAB);
    
    UIView *boomView = [[UIView alloc] initWithFrame:CGRectMake(0, _scrollView.bottom, SCREEN_WIDTH, 1)];
    boomView.backgroundColor = UIColorFromRGB(0xD8D8D8);

    [tabView addSubview:_ugcVideoBtn];
    [tabView addSubview:_scrollView];
    [tabView addSubview:boomView];
    
//    [self.view addSubview:tabView];
    
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    [layout setScrollDirection:UICollectionViewScrollDirectionVertical];

    self.collectionView = [[UICollectionView alloc] initWithFrame:self.view.bounds
                                             collectionViewLayout:layout];
    if (@available(iOS 11, *)) {
        self.collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    } else {
        self.automaticallyAdjustsScrollViewInsets = NO;
    }
//    self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0,tabView.bottom, self.view.width, self.view.height - tabView.bottom) collectionViewLayout:layout];
    self.collectionView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.collectionView registerClass:[TCVideoListCell class] forCellWithReuseIdentifier:@"TCLiveListCell"];
    self.collectionView.dataSource = self;
    self.collectionView.delegate = self;
    self.collectionView.backgroundColor = backgroundColor;
    [self.view addSubview:self.collectionView];
    

    CGFloat nullViewWidth   = 90;
    CGFloat nullViewHeight  = 115;
    CGFloat imageViewWidth  = 68;
    CGFloat imageViewHeight = 74;
    UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake((nullViewWidth - imageViewWidth)/2, 0, imageViewWidth, imageViewHeight)];
    imageView.image = [UIImage imageNamed:@"null_image"];
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, imageView.bottom + 5, nullViewWidth, 22)];
    label.text = NSLocalizedString(@"TCLiveListView.NoContent", nil);
    label.font = [UIFont systemFontOfSize:16];
    label.textColor = UIColorFromRGB(0x777777);
    
    _nullDataView = [[UIView alloc] initWithFrame:CGRectMake((SCREEN_WIDTH - nullViewWidth)/2, (self.view.height - nullViewHeight)/2, nullViewWidth, nullViewHeight)];
    [_nullDataView addSubview:imageView];
    [_nullDataView addSubview:label];
    _nullDataView.hidden = YES;
    [self.view addSubview:_nullDataView];
    
    [self setup];

    [self.view addSubview:topGradient];
}

-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    self.navigationController.navigationBar.hidden = YES;

//    UIView *statusBar = [self statusBar];
//    if ([statusBar respondsToSelector:@selector(setBackgroundColor:)]) {
//        statusBar.backgroundColor = [UIColor whiteColor];
//    }

}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
//    UIView *statusBar = [self statusBar];
//    if ([statusBar respondsToSelector:@selector(setBackgroundColor:)]) {
//        statusBar.backgroundColor = [UIColor clearColor];
//    }
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    _playVC = nil;
    _hasEnterplayVC = NO;
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}

- (void)setup
{
    [self.collectionView.mj_header endRefreshing];
    [self.collectionView.mj_footer endRefreshing];
    if(self.lives) [self.lives removeAllObjects];
    
    self.collectionView.mj_header = [MJRefreshNormalHeader headerWithRefreshingBlock:^{
        self.isLoading = YES;
        self.lives = [NSMutableArray array];
        [_liveListMgr queryVideoList:GetType_Up];
    }];
    [self.collectionView.mj_header setAutomaticallyChangeAlpha:YES];
    self.collectionView.mj_footer = [MJRefreshAutoNormalFooter footerWithRefreshingBlock:^{
        self.isLoading = YES;
        [_liveListMgr queryVideoList:GetType_Down];
    }];

    // 先加载缓存的数据，然后再开始网络请求，以防用户打开是看到空数据
    [self.liveListMgr loadVodsFromArchive];
    [self doFetchList];

    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.collectionView.mj_header beginRefreshing];
    });
    
    [(MJRefreshHeader *)self.collectionView.mj_header endRefreshingWithCompletionBlock:^{
        self.isLoading = NO;
    }];
    [(MJRefreshHeader *)self.collectionView.mj_footer endRefreshingWithCompletionBlock:^{
        self.isLoading = NO;
    }];
}

-(void)videoBtnClick:(UIButton *)button
{
    [UIView animateWithDuration:0.5 animations:^{
        _scrollView.frame = CGRectMake(_ugcVideoBtn.left - (scrollViewWidth - _ugcVideoBtn.width)/2, _ugcVideoBtn.bottom + 5, scrollViewWidth, scrollViewHeight);
    }];
    [self setup];
}

#pragma mark - UICollectionView datasource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return (self.lives.count + 1) / 2;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    if (self.lives.count % 2 != 0 && section == (self.lives.count + 1) / 2 - 1) {
        return 1;
    } else {
        return 2;
    }
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    TCVideoListCell *cell = (TCVideoListCell *)[collectionView dequeueReusableCellWithReuseIdentifier:@"TCLiveListCell" forIndexPath:indexPath];
    if (cell == nil) {
        cell = [[TCVideoListCell alloc] initWithFrame:CGRectZero];
    }
    
    NSInteger index = indexPath.section * 2 + indexPath.row;
    if (self.lives.count > index) {
        TCLiveInfo *live = self.lives[index];
        cell.model = live;
    }
    return cell;
}

//设置每个item的尺寸
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    // 图片的宽高比为9:16
    CGFloat width = (self.view.width - 1) / 2;
    CGFloat height = width;
    return CGSizeMake(width, height);
}

//设置每个item的UIEdgeInsets
- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return UIEdgeInsetsMake(0, 0, 0, 0);
}

//设置每个item水平间距
- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout minimumInteritemSpacingForSectionAtIndex:(NSInteger)section {
    return 1;
}

//设置每个item垂直间距
- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout minimumLineSpacingForSectionAtIndex:(NSInteger)section {
    return 0;
}

#pragma mark - UICollectionView delegate

//点击item方法
- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    // 此处一定要用cell的数据，live中的对象可能已经清空了
    TCVideoListCell *cell = (TCVideoListCell *)[collectionView cellForItemAtIndexPath:indexPath];
    TCLiveInfo *info = cell.model;
    
    // MARK: 打开播放界面
    if (_playVC == nil) {
        if (self.lives && self.lives.count > 0 && info) {
            _playVC = [[TCVodPlayViewController alloc] initWithPlayInfoS:self.lives liveInfo:info videoIsReady:^{
                if (!_hasEnterplayVC) {
                    // TODO: send mesage to delegate
//                    [[AppDelegate sharedAppDelegate] pushViewController:_playVC animated:YES];
                    _hasEnterplayVC = YES;
                }
            }];
            WEAKIFY(self);
            ((TCVodPlayViewController *)_playVC).onTapChorus = ^(TCVodPlayViewController *controller) {
                STRONGIFY_OR_RETURN(self);
                if ([TCLoginParam shareInstance].isExpired && self.loginHandler) {
                    self.loginHandler(self);
                }
            };
        }
    }
    [self performSelector:@selector(enterPlayVC:) withObject:_playVC afterDelay:0.5];
}

-(void)enterPlayVC:(NSObject *)obj{
    if (!_hasEnterplayVC) {
        _playVC.hidesBottomBarWhenPushed = YES;
        [self.navigationController pushViewController:_playVC animated:YES];
        _hasEnterplayVC = YES;
        
        if (self.listener) {
            [self.listener onEnterPlayViewController];
        }
    }
}

#pragma mark - Net fetch
/**
 * 拉取直播列表。TCLiveListMgr在启动是，会将所有数据下载下来。在未全部下载完前，通过loadLives借口，
 * 能取到部分数据。通过finish接口，判断是否已取到最后的数据
 *
 */
- (void)doFetchList {
    NSRange range = NSMakeRange(self.lives.count, 20);
    BOOL finish;
    NSArray *result = [_liveListMgr readVods:range finish:&finish];
    if (result.count) {
        result = [self mergeResult:result];
        [self.lives addObjectsFromArray:result];
    } else {
        if (finish) {
            MBProgressHUD *hud = [[HUDHelper sharedInstance] tipMessage:NSLocalizedString(@"TCLiveListView.NoMore", nil)];
            hud.userInteractionEnabled = NO;
        }
    }
    self.collectionView.mj_footer.hidden = finish;
    [self.collectionView reloadData];
    [self.collectionView.mj_header endRefreshing];
    [self.collectionView.mj_footer endRefreshing];
    
    if (self.lives.count == 0) {
        _nullDataView.hidden = NO;
    }else{
        _nullDataView.hidden = YES;
    }
}

/**
 *  将取到的数据于已存在的数据进行合并。
 *
 *  @param result 新拉取到的数据
 *
 *  @return 新数据去除已存在记录后，剩余的数据
 */
- (NSArray *)mergeResult:(NSArray *)result {
   
    // 每个直播的播放地址不同，通过其进行去重处理
    NSArray *existArray = [self.lives valueForKey:NSStringFromSelector(@selector(playurl))];
    NSArray *newArray = [result filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(TCLiveInfo * _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
        return ![existArray containsObject:evaluatedObject.playurl];
    }]];
    return newArray;
}

/**
 *  TCLiveListMgr有新数据过来
 *
 */
- (void)newDataAvailable:(NSNotification *)noti {
    [self doFetchList];
}

/**
 *  TCLiveListMgr数据有更新
 *
 */
- (void)listDataUpdated:(NSNotification *)noti {
    [self setup];
}


/**
 *  TCLiveListMgr内部出错
 *
 */
- (void)svrError:(NSNotification *)noti {
    NSError *e = noti.object;
    if ([e isKindOfClass:[NSError class]]) {
        if ([e localizedFailureReason]) {
            [HUDHelper alert:[e localizedFailureReason]];
        }
        else if ([e localizedDescription]) {
            [HUDHelper alert:[e localizedDescription]];
        }
    }
    
    // 如果还在加载，停止加载动画
    if (self.isLoading) {
        [self.collectionView.mj_header endRefreshing];
        [self.collectionView.mj_footer endRefreshing];
        self.isLoading = NO;
    }
}

/**
 *  TCPlayViewController出错，加入房间失败
 *
 */
- (void)playError:(NSNotification *)noti {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//        [self.tableView.mj_header beginRefreshing];
        //加房间失败后，刷新列表，不需要刷新动画
        self.lives = [NSMutableArray array];
        self.isLoading = YES;
        [_liveListMgr queryVideoList:GetType_Up];
    });
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
