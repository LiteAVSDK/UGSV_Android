// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <Photos/Photos.h>
#import "UGCKitTheme.h"
#import "UGCKitResult.h"

@class UGCKitMediaPickerViewController;

@protocol UGCKitMediaPickerControllerDelegate <NSObject>

@optional
- (BOOL)qb_imagePickerController:(UGCKitMediaPickerViewController *)imagePickerController shouldSelectAsset:(PHAsset *)asset;
- (void)qb_imagePickerController:(UGCKitMediaPickerViewController *)imagePickerController didSelectAsset:(PHAsset *)asset;
- (void)qb_imagePickerController:(UGCKitMediaPickerViewController *)imagePickerController didDeselectAsset:(PHAsset *)asset;

@end


typedef NS_OPTIONS(NSInteger, UGCKitMediaType) {
    UGCKitMediaTypePhoto = 1 << 0,
    UGCKitMediaTypeVideo = 1 << 1,
    UGCKitMediaTypeAny = UGCKitMediaTypePhoto | UGCKitMediaTypeVideo,
};

@interface UGCKitMediaPickerConfig : NSObject

/// 最少选取个数，默认值为1
@property (assign, nonatomic) NSUInteger minItemCount;

/// 最多选取个数，默认值为1
@property (assign, nonatomic) NSUInteger maxItemCount;

/// 选取的媒体类型，支持 PHAssetMediaTypeImage 和 PHAssetMediaTypeVideo，默认为 PHAssetMediaTypeVideo
@property (assign, nonatomic) UGCKitMediaType mediaType;
@end

@interface UGCKitMediaPickerViewController : UIViewController
@property (copy, nonatomic) void(^completion)(UGCKitResult *result);
- (instancetype)initWithConfig:(UGCKitMediaPickerConfig *)config theme:(UGCKitTheme *)theme;

@property (nonatomic, weak) id<UGCKitMediaPickerControllerDelegate> delegate;

@property (nonatomic, strong, readonly) NSMutableOrderedSet *selectedAssets;

@property (nonatomic, copy) NSArray *assetCollectionSubtypes;


@property (nonatomic, copy) NSString *prompt;

@property (nonatomic, assign) NSUInteger numberOfColumnsInPortrait;
@property (nonatomic, assign) NSUInteger numberOfColumnsInLandscape;

@end
