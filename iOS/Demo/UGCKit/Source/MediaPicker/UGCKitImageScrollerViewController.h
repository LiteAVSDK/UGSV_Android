// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@interface UGCKitImageScrollerViewController : UICollectionViewController
@property (strong, nonatomic) UIImage *closeIcon;
@property (nullable, copy, nonatomic) void(^onRemoveHandler)(NSUInteger index);
- (instancetype)initWithImageManage:(PHCachingImageManager *)imageManager;
- (void)addAsset:(PHAsset *)asset;
- (void)removeAssetAtIndex:(NSUInteger)index;

- (NSArray<PHAsset *> *)currentAssets;
@end

NS_ASSUME_NONNULL_END
