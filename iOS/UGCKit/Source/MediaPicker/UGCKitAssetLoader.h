// Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@interface UGCKitAssetLoader : NSObject
@property (strong, nonatomic) NSArray<PHFetchResult *> *fetchResults;
@property (nonatomic, copy) NSArray *assetCollections;
- (void)load:(void(^)(void))completion;
@end

NS_ASSUME_NONNULL_END
