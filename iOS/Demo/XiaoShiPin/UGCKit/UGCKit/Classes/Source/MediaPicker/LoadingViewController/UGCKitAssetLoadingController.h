// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <Photos/Photos.h>
#import "UGCKitResult.h"
#import "UGCKitTheme.h"

typedef NS_ENUM(NSInteger,AssetType){
    AssetType_Video,
    AssetType_Image,
};

@interface UGCKitAssetLoadingController : UIViewController
@property (nonatomic, strong) NSMutableArray<AVAsset*> *avAssets;
@property (copy, nonatomic) void (^completion)(UGCKitResult *result);
@property BOOL combineVideos;
- (instancetype)initWithTheme:(UGCKitTheme *)theme;
- (void)exportAssetList:(NSArray<PHAsset *> *)assets assetType:(AssetType)assetType;

@end
