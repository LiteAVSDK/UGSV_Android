// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <Photos/Photos.h>
#import "UGCKitTheme.h"
#import "UGCKitResult.h"

NS_ASSUME_NONNULL_BEGIN

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
/// 导航栏提示，默认 nil
@property (nullable, nonatomic, copy) NSString *prompt;
/// 最少选取个数，默认值为1
@property (assign, nonatomic) NSUInteger minItemCount;
/// 最多选取个数，默认值为1
@property (assign, nonatomic) NSUInteger maxItemCount;
/// 选取的媒体类型，支持 PHAssetMediaTypeImage 和 PHAssetMediaTypeVideo，默认为 PHAssetMediaTypeVideo
@property (assign, nonatomic) UGCKitMediaType mediaType;
/// 竖屏列数，默认为4
@property (nonatomic, assign) NSUInteger numberOfColumnsInPortrait;
/// 横屏列数，默认为7
@property (nonatomic, assign) NSUInteger numberOfColumnsInLandscape;
/// 获取视频后是否拼接视频，默认为 YES
@property (nonatomic, assign) BOOL combineVideos;
@end

@interface UGCKitMediaPickerViewController : UIViewController
@property (readonly, nonatomic) UGCKitMediaPickerConfig *config;
@property (copy, nonatomic) void(^completion)(UGCKitResult *result);
- (instancetype)initWithConfig:(nullable UGCKitMediaPickerConfig *)config
                         theme:(nullable UGCKitTheme *)theme;

@property (nonatomic, weak) id<UGCKitMediaPickerControllerDelegate> delegate;

@property (nonatomic, strong, readonly) NSMutableOrderedSet<PHAsset*> *selectedAssets;
// 仅在 completion 回调时有值
@property (nonatomic, strong, readonly) NSArray<AVAsset *> *exportedAssets;

@property (nonatomic, copy) NSArray *assetCollectionSubtypes;

@end

NS_ASSUME_NONNULL_END
