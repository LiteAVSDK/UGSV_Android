// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitMediaPickerViewController.h"
#import <Photos/Photos.h>
#import "UGCKitTheme.h"
// ViewControllers
#import "UGCKitAssetsViewController.h"
#import "UGCKitAssetLoadingController.h"
#import "UGCKitMediaPickerViewControllerPrivate.h"

@interface _UGCMediaPickerNavigationController : UINavigationController
@end

@implementation UGCKitMediaPickerViewController

- (instancetype)init {
    if (self = [self initWithConfig:nil theme:nil]) {

    }
    return self;
}

- (instancetype)initWithConfig:(UGCKitMediaPickerConfig *)config theme:(UGCKitTheme *)theme;
{
    self = [super init];
    
    if (self) {
        _config = config ?: [[UGCKitMediaPickerConfig alloc] init];
        _theme = theme ?: [UGCKitTheme sharedTheme];
        self.showsNumberOfSelectedAssets = YES;
        self.mediaType = config.mediaType;

        // Set default values
        self.assetCollectionSubtypes = @[
                                         @(PHAssetCollectionSubtypeSmartAlbumUserLibrary),
                                         @(PHAssetCollectionSubtypeAlbumMyPhotoStream),
                                         @(PHAssetCollectionSubtypeSmartAlbumPanoramas),
                                         @(PHAssetCollectionSubtypeSmartAlbumVideos),
                                         @(PHAssetCollectionSubtypeSmartAlbumBursts)
                                         ];
        self.minimumNumberOfSelection = config.minItemCount;
        self.maximumNumberOfSelection = config.maxItemCount;
        self.allowsMultipleSelection = config.minItemCount != config.maxItemCount;
        
        _selectedAssets = [NSMutableOrderedSet orderedSet];
        
        // Get asset bundle
        if (theme.resourceBundle) {
            self.assetBundle = theme.resourceBundle;
        } else {
            self.assetBundle = [NSBundle bundleForClass:[self class]];
            NSString *bundlePath = [self.assetBundle pathForResource:@"UGCKitResources" ofType:@"bundle"];
            if (bundlePath) {
                self.assetBundle = [NSBundle bundleWithPath:bundlePath];
            }
        }
        [self setUpAlbumsViewController];
        self.view.backgroundColor = _theme.backgroundColor;
        // Set instance
        UGCKitAssetsViewController *albumsViewController = (UGCKitAssetsViewController *)self.childViewControllers.firstObject;

        albumsViewController.theme = _theme;
        albumsViewController.imagePickerController = self;
    }
    
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    UINavigationBar *navigationBar = self.navigationController.navigationBar;
    if (navigationBar) {
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(1, 1), YES, 1);
        [_theme.backgroundColor set];
        UIRectFill(CGRectMake(0, 0, 1, 1));
        [navigationBar setBackgroundImage:UIGraphicsGetImageFromCurrentImageContext() forBarMetrics:UIBarMetricsDefault];
        UIGraphicsEndImageContext();
        navigationBar.barStyle = UIBarStyleBlack;
        [navigationBar setTranslucent:NO];
        navigationBar.shadowImage = [[UIImage alloc] init];
        [navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName: _theme.titleColor}];
    }
}

- (UINavigationItem *)navigationItem
{
    return self.assetViewController.navigationItem;
}

- (void)setUpAlbumsViewController
{
    // Add UGCKitAlbumsViewController as a child
    NSString *bundlePath = [[NSBundle mainBundle] pathForResource:@"UGCKitResources" ofType:@"bundle"];
    NSBundle *bundle = [NSBundle bundleWithPath:bundlePath]; // bundle 不存在时会返回nil, 会从主bundle中找

    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"UGCKitMediaPicker" bundle:bundle];
    UGCKitAssetsViewController *assetViewController = [storyboard instantiateViewControllerWithIdentifier:@"UGCKitAssetsViewController"];
    assetViewController.theme = _theme;
    assetViewController.imagePickerController = self;
    [self addChildViewController:assetViewController];

    assetViewController.view.frame = self.view.bounds;
    [self.view addSubview:assetViewController.view];
    
    [assetViewController didMoveToParentViewController:self];
    [self.navigationController.navigationBar setItems:@[assetViewController.navigationItem]];
    self.assetViewController = assetViewController;
}
- (NSArray<AVAsset *> *)exportedAssets {
    return self.assetViewController.exportedAssets;
}
@end

@implementation UGCKitMediaPickerConfig
- (instancetype)init
{
    self = [super init];
    if (self) {
        _minItemCount = 1;
        _maxItemCount = 1;
        _mediaType = UGCKitMediaTypeVideo;
        _numberOfColumnsInPortrait = 4;
        _numberOfColumnsInLandscape = 7;
        _combineVideos = YES;
    }
    return self;
}
@end

@implementation _UGCMediaPickerNavigationController
- (void)awakeFromNib {
    [super awakeFromNib];
}
- (UIStatusBarStyle)preferredStatusBarStyle
{
    return UIStatusBarStyleLightContent;
}
- (UIViewController *)childForStatusBarStyle {
    return self.topViewController;
}
@end
