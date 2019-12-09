// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitAssetsViewController.h"
#import "UGCKitMediaPickerViewController.h"

@interface UGCKitMediaPickerViewController () {
    UGCKitMediaPickerConfig *_config;
    UGCKitTheme * _theme;
}
@property (nonatomic, strong) NSBundle *assetBundle;
@property (nonatomic, weak) UGCKitAssetsViewController *assetViewController;
@property (nonatomic, assign) BOOL showsNumberOfSelectedAssets;
@property (nonatomic, assign) BOOL allowsMultipleSelection;
@property (nonatomic, assign) NSUInteger minimumNumberOfSelection;
@property (nonatomic, assign) NSUInteger maximumNumberOfSelection;
@property (nonatomic, assign) UGCKitMediaType mediaType;

@end
