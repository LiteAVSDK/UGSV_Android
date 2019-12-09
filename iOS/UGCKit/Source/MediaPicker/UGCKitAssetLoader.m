// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitAssetLoader.h"
#import <Photos/Photos.h>

@interface UGCKitAssetLoader ()

@end

@implementation UGCKitAssetLoader

- (void)load:(void (^)(void))completion {
    // Fetch user albums and smart albums
    __weak __typeof(self) weakSelf = self;
    void (^doFetch)(void) = ^{
        __strong __typeof(weakSelf) self = weakSelf;
        PHFetchResult *smartAlbums = [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeSmartAlbum subtype:PHAssetCollectionSubtypeAny options:nil];
        PHFetchResult *userAlbums = [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeAlbum subtype:PHAssetCollectionSubtypeAny options:nil];
        self.fetchResults = @[smartAlbums, userAlbums];
        [self updateAssetCollections];
        if (completion) {
            completion();
        }
    };

    // Register observer
//    [[PHPhotoLibrary sharedPhotoLibrary] registerChangeObserver:self];

    if (PHAuthorizationStatusAuthorized != [PHPhotoLibrary authorizationStatus]) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            if (PHAuthorizationStatusAuthorized == status) {
                dispatch_async(dispatch_get_main_queue(), doFetch);
            }
        }];
    } else {
        doFetch();
    }
}

- (void)updateAssetCollections
{
    NSArray<NSNumber *> * assetCollectionSubtypes = @[
        @(PHAssetCollectionSubtypeSmartAlbumUserLibrary),
        @(PHAssetCollectionSubtypeAlbumMyPhotoStream),
        @(PHAssetCollectionSubtypeSmartAlbumPanoramas),
        @(PHAssetCollectionSubtypeSmartAlbumVideos),
        @(PHAssetCollectionSubtypeSmartAlbumBursts)
    ];

    // Filter albums
    NSMutableDictionary *smartAlbums = [NSMutableDictionary dictionaryWithCapacity:assetCollectionSubtypes.count];
    NSMutableArray *userAlbums = [NSMutableArray array];
    
    for (PHFetchResult *fetchResult in self.fetchResults) {
        [fetchResult enumerateObjectsUsingBlock:^(PHAssetCollection *assetCollection, NSUInteger index, BOOL *stop) {
            PHAssetCollectionSubtype subtype = assetCollection.assetCollectionSubtype;
            
            if (subtype == PHAssetCollectionSubtypeAlbumRegular) {
                [userAlbums addObject:assetCollection];
            } else if ([assetCollectionSubtypes containsObject:@(subtype)]) {
                if (!smartAlbums[@(subtype)]) {
                    smartAlbums[@(subtype)] = [NSMutableArray array];
                }
                [smartAlbums[@(subtype)] addObject:assetCollection];
            }
        }];
    }
    
    NSMutableArray *assetCollections = [NSMutableArray array];

    // Fetch smart albums
    for (NSNumber *assetCollectionSubtype in assetCollectionSubtypes) {
        NSArray *collections = smartAlbums[assetCollectionSubtype];
        
        if (collections) {
            [assetCollections addObjectsFromArray:collections];
        }
    }
    
    // Fetch user albums
    [userAlbums enumerateObjectsUsingBlock:^(PHAssetCollection *assetCollection, NSUInteger index, BOOL *stop) {
        [assetCollections addObject:assetCollection];
    }];
    
    self.assetCollections = assetCollections;
}


@end
