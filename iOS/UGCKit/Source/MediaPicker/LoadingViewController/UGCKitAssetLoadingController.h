// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <Photos/Photos.h>
#import "UGCKitResult.h"
#import "UGCKitTheme.h"

typedef NS_ENUM(NSInteger,AssetType){
    AssetType_Video,
    AssetType_Image,
};

typedef NS_ENUM(NSInteger,ComposeMode){
    ComposeMode_Edit,
    ComposeMode_Join,
    ComposeMode_Upload,
};

@interface UGCKitAssetLoadingController : UIViewController
@property (copy, nonatomic) void (^completion)(UGCKitResult *result);
@property ComposeMode composeMode;
- (instancetype)initWithTheme:(UGCKitTheme *)theme;
- (void)exportAssetList:(NSArray *)assets assetType:(AssetType)assetType;

@end
