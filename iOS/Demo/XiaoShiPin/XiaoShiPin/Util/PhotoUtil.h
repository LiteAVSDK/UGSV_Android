//
//  PhotoUtil.h
//  TXLiteAVDemo_Enterprise
//
//  Created by cui on 2019/9/11.
//  Copyright © 2019 Tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
extern NSString * const PhotoAlbumToolErrorDomain;

typedef NS_ENUM(NSInteger, PhotoAlbumToolErrorCode) {
    PhotoAlbumToolNotAuthorized
};

@interface PhotoUtil : NSObject
+ (void)saveAssetToAlbum:(NSURL *)assetURL completion:(void(^)(BOOL success, NSError * _Nullable error))completion;
+ (void)saveDataToAlbum:(NSData *)data completion:(void(^)(BOOL success, NSError * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END
