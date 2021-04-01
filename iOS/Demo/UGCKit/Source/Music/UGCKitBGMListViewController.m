// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitBGMListViewController.h"
#import "UGCKitBGMHelper.h"
#import "UGCKitBGMCell.h"
#import <MediaPlayer/MediaPlayer.h>
#import <AVFoundation/AVFoundation.h>
#import "UGCKitColorMacro.h"
#import <objc/runtime.h>
#import "UGCKitMem.h"

@interface UGCKitBGMListViewController()<TCBGMHelperListener,UGCKitBGMCellDelegate,MPMediaPickerControllerDelegate>{
    NSMutableDictionary* _progressList;
    NSTimeInterval lastUIFreshTick;
    UGCKitTheme *_theme;
}
@property(nonatomic,strong) NSDictionary* bgmDict;
@property(nonatomic,strong) NSArray* bgmKeys;
@property(nonatomic,strong) UGCKitBGMHelper* bgmHelper;
@property(nonatomic,weak) id<TCBGMControllerListener> bgmListener;
@end


@implementation UGCKitBGMListViewController
{
    NSIndexPath *_BGMCellPath;
    BOOL      _useLocalMusic;
}

- (instancetype)initWithTheme:(UGCKitTheme *)theme {
    if (self = [self initWithStyle:UITableViewStylePlain]) {
        _theme = theme;
    }
    return self;
}

- (instancetype)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        _progressList = [NSMutableDictionary new];
        _useLocalMusic = NO;
    }
    return self;
}

-(void)setBGMControllerListener:(id<TCBGMControllerListener>) listener{
    _bgmListener = listener;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

-(void)viewDidLoad{
    [super viewDidLoad];
    self.title = [_theme localizedString:@"UGCKit.BGMListView.TitileChooseBGM"];
    UIBarButtonItem *customBackButton = [[UIBarButtonItem alloc] initWithImage:_theme.backIcon style:UIBarButtonItemStylePlain target:self action:@selector(goBack)];
    self.navigationItem.leftBarButtonItem = customBackButton;

    self.tableView.backgroundColor = RGB(25, 29, 38);
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    NSString *bundlePath = [[NSBundle mainBundle] pathForResource:@"UGCKitResources" ofType:@"bundle"];
    NSBundle *bundle = [NSBundle bundleWithPath:bundlePath];
    [self.tableView registerNib:[UINib nibWithNibName:NSStringFromClass([UGCKitBGMCell class]) bundle:bundle] forCellReuseIdentifier:@"UGCKitBGMCell"];
}

- (void)goBack
{
    [_bgmListener onBGMControllerPlay:nil];
}

- (void)loadBGMList{
    if (_useLocalMusic) {
        [self showMPMediaPickerController];
    }else{
        lastUIFreshTick = [[NSDate date] timeIntervalSince1970]*1000;
        _bgmHelper = [UGCKitBGMHelper sharedInstance];
        [_bgmHelper setDelegate:self];
        NSString* jsonUrl = @"https://liteav.sdk.qcloud.com/app/res/bgm/bgm_list.json";
        [_bgmHelper initBGMListWithJsonFile:jsonUrl];
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [_bgmKeys count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 80;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UGCKitBGMCell* cell = (UGCKitBGMCell *)[tableView dequeueReusableCellWithIdentifier:@"UGCKitBGMCell"];
    if (!cell) {
        cell = [[UGCKitBGMCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"UGCKitBGMCell"];
    }
    if (nil == cell.downloadButtonBackground) {
        cell.downloadButtonBackground = _theme.recordMusicDownloadIcon;
        cell.progressButtonBackground = _theme.nextIcon;
        cell.downloadText = [_theme localizedString:@"UGCKit.Common.Download"];
        cell.downloadingText = [_theme localizedString:@"UGCKit.UGCKit.Common.Downloading"];
        cell.applyText = [_theme localizedString:@"UGCKit.Common.Apply"];
    }

    cell.delegate = self;
    TCBGMElement* ele =  _bgmDict[_bgmKeys[indexPath.row]];
    if (ele.localUrl) {
        [cell setDownloadProgress:1.0];
        cell.progressView.hidden = YES;
    }else{
        cell.progressView.hidden = YES;
        [cell.downLoadBtn setTitle:[_theme localizedString:@"UGCKit.Common.Download"] forState:UIControlStateNormal];
    }
    cell.downLoadBtn.hidden = [_BGMCellPath isEqual:indexPath];
    cell.musicLabel.text = ele.name;
    return cell;
}

- (void)onBGMDownLoad:(UGCKitBGMCell *)cell;
{
    if (_BGMCellPath) {
        UGCKitBGMCell *cell = (UGCKitBGMCell*)[self.tableView cellForRowAtIndexPath:_BGMCellPath];
        cell.progressView.hidden = YES;
        cell.downLoadBtn.hidden  = NO;
    }
    NSIndexPath *indexPath = [self.tableView indexPathForCell:cell];
    _BGMCellPath = indexPath;
    cell.downLoadBtn.hidden = YES;
    cell.progressView.hidden = NO;
    TCBGMElement* ele =  _bgmDict[_bgmKeys[indexPath.row]];
    if([ele isValid] && [[NSFileManager defaultManager] fileExistsAtPath:[NSHomeDirectory() stringByAppendingPathComponent:[ele localUrl]]]){
        [_bgmListener onBGMControllerPlay: [NSHomeDirectory() stringByAppendingPathComponent:[ele localUrl]]];
    }
    else [_bgmHelper downloadBGM: _bgmDict[_bgmKeys[indexPath.row]]];
}

-(void)onBGMListLoad:(NSDictionary*)dict{
    BOOL foundKeyBGMToLoadRemote = NO;
    if(dict){
        BGMLog(@"BGM List 加载成功");
        _bgmDict = dict;
        _bgmKeys = [_bgmDict keysSortedByValueUsingComparator:^(TCBGMElement* e1, TCBGMElement* e2){
            return [[e1 name] compare:[e2 name]];
        }];
        for (NSString* url in _bgmKeys) {
            TCBGMElement* ele = [_bgmDict objectForKey:url];
            if([[ele isValid] boolValue]){
                @synchronized (_progressList) {
                    [_progressList setObject :[NSNumber numberWithFloat:1.f] forKey:url];
                }
            }
            // 没有青花瓷时用本地音乐，AppStore审核用
//            NSRange range = [ele.name rangeOfString:@"青花瓷"]; //
//            if (range.location != NSNotFound) {
//                foundKeyBGMToLoadRemote = YES;
//            }
            foundKeyBGMToLoadRemote = YES;
        }
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        if (foundKeyBGMToLoadRemote) {
            self->_useLocalMusic = NO;
            [self.tableView reloadData];
        }else{
            self->_useLocalMusic = YES;
            [self showMPMediaPickerController];
        }
    });
}

-(void)onBGMDownloading:(TCBGMElement*)current percent:(float)percent{
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([[self.tableView indexPathsForVisibleRows] containsObject:self->_BGMCellPath]) {
            UGCKitBGMCell *cell = [self.tableView cellForRowAtIndexPath:self->_BGMCellPath];
            cell.progressView.hidden = NO;
            cell.downLoadBtn.hidden = YES;
            [cell setDownloadProgress:percent];
        }
    });
}

-(void) onBGMDownloadDone:(TCBGMElement*)element{
    if([[element isValid] boolValue]){
        BGMLog(@"Download \"%@\" success!", [element name]);
        @synchronized (_progressList) {
            [_progressList setObject :[NSNumber numberWithFloat:1.f] forKey:[element netUrl]];
        }
        WEAKIFY(self);
        dispatch_async(dispatch_get_main_queue(), ^{
            STRONGIFY_OR_RETURN(self);
            UGCKitBGMCell *cell = [self.tableView cellForRowAtIndexPath:self->_BGMCellPath];
            cell.progressView.hidden = YES;
            cell.downLoadBtn.hidden = NO;
            self->_BGMCellPath = nil;
            [self->_bgmListener onBGMControllerPlay: [NSHomeDirectory() stringByAppendingPathComponent:[element localUrl]]];
        });
    }
    else BGMLog(@"Download \"%@\" failed!", [element name]);

}

static void *mpcKey = &mpcKey;
- (void)showMPMediaPickerController
{
    MPMediaPickerController *mpc = [[MPMediaPickerController alloc] initWithMediaTypes:MPMediaTypeAnyAudio];
    mpc.delegate = self;
    mpc.editing = YES;
    mpc.allowsPickingMultipleItems = NO;
    mpc.showsCloudItems = NO;
    if (@available(iOS 9.2, *)) {
        mpc.showsItemsWithProtectedAssets = NO;
    }
    mpc.modalPresentationStyle = UIModalPresentationFullScreen;
    objc_setAssociatedObject(mpc, mpcKey, self, OBJC_ASSOCIATION_RETAIN);
    UINavigationController *nav = self.navigationController;
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    [self.navigationController setViewControllers:@[mpc] animated:NO];
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
}

#pragma mark - BGM
//选中后调用
- (void)mediaPicker:(MPMediaPickerController *)mediaPicker didPickMediaItems:(MPMediaItemCollection *)mediaItemCollection {
    NSArray *items = mediaItemCollection.items;
    MPMediaItem *songItem = [items objectAtIndex:0];
    NSURL *url = [songItem valueForProperty:MPMediaItemPropertyAssetURL];
    AVAsset *songAsset = [AVAsset assetWithURL:url];
    if (songAsset != nil) {
        [_bgmListener onBGMControllerPlay:songAsset];
    }
}

//点击取消时回调
- (void)mediaPickerDidCancel:(MPMediaPickerController *)mediaPicker{
    [_bgmListener onBGMControllerPlay:nil];
}

// 清空选中状态
- (void)clearSelectStatus {
    _BGMCellPath = nil;
}
@end
