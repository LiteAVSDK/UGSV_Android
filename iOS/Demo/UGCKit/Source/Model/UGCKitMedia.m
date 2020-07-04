// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitMedia.h"
#import "SDKHeader.h"

@interface UGCKitMedia ()
{
    AVAsset *_videoAsset;
    NSArray<UIImage *> *_images;
    CGSize _size;
}
@property (nonatomic) CGSize size;
@end

@implementation UGCKitMedia

- (instancetype)initWithAVAsset:(AVAsset *)asset images:(NSArray<UIImage *> *)images {
    if (self = [super init]) {
        _videoAsset = asset;
        _images = images;
        if (asset) {
            TXVideoInfo *videoInfo = [TXVideoInfoReader getVideoInfoWithAsset:asset];
            _size = CGSizeMake(videoInfo.width, videoInfo.height);
        }
        if (_videoAsset) {
            _isVideo = YES;
        }
    }
    return self;
}

- (instancetype)initWithVideoPath:(NSString *)path {
    if (self = [self initWithAVAsset:[AVAsset assetWithURL:[NSURL fileURLWithPath:path]] images:nil]) {
        _videoPath = path;
    }
    return self;
}

+ (instancetype)mediaWithVideoPath:(NSString *)path
{
    return [[UGCKitMedia alloc] initWithVideoPath:path];
}

+ (instancetype)mediaWithAVAsset:(AVAsset *)asset {
    return [[UGCKitMedia alloc] initWithAVAsset:asset images:nil];
}

+ (instancetype)mediaWithImages:(NSArray<UIImage *> *)images canvasSize:(CGSize)canvasSize {
    UGCKitMedia *asset = [[UGCKitMedia alloc] initWithAVAsset:nil images:images];
    asset.size = canvasSize;
    return asset;
}

@end
