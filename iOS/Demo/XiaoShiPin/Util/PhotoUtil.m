//
//  PhotoUtil.m
//  TXLiteAVDemo_Enterprise
//
//  Created by cui on 2019/9/11.
//  Copyright © 2019 Tencent. All rights reserved.
//

#import "PhotoUtil.h"
#import <Photos/Photos.h>

NSString * const PhotoAlbumToolErrorDomain = @"PhotoAlbumToolErrorDomain";

@implementation PhotoUtil

+ (void)saveAssetToAlbum:(NSURL *)assetURL completion:(void(^)(BOOL success, NSError * _Nullable error))completion
{
    [self _saveAssetToAlbum:assetURL completion:completion];
}

+ (void)saveDataToAlbum:(NSData *)data completion:(void(^)(BOOL success, NSError * _Nullable error))completion;
{
    [self _saveAssetToAlbum:data completion:completion];
}

+ (void)_saveAssetToAlbum:(id)urlOrData
              completion:(void(^)(BOOL success, NSError * _Nullable error))completion
{
    PHPhotoLibrary *library = [PHPhotoLibrary sharedPhotoLibrary];
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        if (status == PHAuthorizationStatusAuthorized) {
            [library performChanges:^{
                PHAssetCreationRequest *request = [PHAssetCreationRequest creationRequestForAsset];
                if ([urlOrData isKindOfClass:[NSURL class]]) {
                    [request addResourceWithType:PHAssetResourceTypeVideo fileURL:urlOrData options:nil];
                } else {
                    [request addResourceWithType:PHAssetResourceTypePhoto data:urlOrData options:nil];
                }
            } completionHandler:^(BOOL success, NSError * _Nullable error) {
                if (completion) {
                    completion(success, error);
                }
            }];
        } else {
            NSError *error = [NSError errorWithDomain:PhotoAlbumToolErrorDomain
                                                 code:PhotoAlbumToolNotAuthorized
                                             userInfo:@{NSLocalizedFailureReasonErrorKey:@"无相册写入权限"}];
            if (completion) {
                completion(NO, error);
            }
        }
    }];


    /*
    PHPhotoLibrary.shared performChanges({
        let request = PHAssetCreationRequest.forAsset()
        request.addResource(with: .photo, fileURL: 'YOUR_GIF_URL', options: nil)
    }) { (success, error) in
        if let error = error {
            print(error.localizedDescription)
        } else {
            print("GIF has saved")
        }
    }
*/

}

@end
