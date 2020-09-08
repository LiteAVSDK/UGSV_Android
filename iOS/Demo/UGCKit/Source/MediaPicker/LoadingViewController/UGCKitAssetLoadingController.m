// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitAssetLoadingController.h"
#import <Photos/Photos.h>
#import "UGCKitReporterInternal.h"
#import "UGCKitMem.h"
#import "SDKHeader.h"
#import "UGCKitPieProgressView.h"
@interface UGCKitAssetLoadingController () <TXVideoJoinerListener> {
    BOOL  _loadingIsInterrupt;
    AssetType _assetType;
    TXVideoJoiner  * _videoJoiner;
    NSString *_joinedVideoPath;
    NSMutableDictionary<NSNumber *, NSNumber *> *_progressCache; // key: loading index, value: progress
    float _prevLoadingProgress;
}

@property (nonatomic, strong) UGCKitPieProgressView *loadingProgressView;
@property (nonatomic, strong) IBOutlet UILabel *loadingLabel;
@property (nonatomic, strong) NSArray        *assets;
@property (nonatomic, strong) NSMutableArray     *imagesToEdit;
@property (nonatomic, strong) AVMutableComposition *mutableComposition;
@property (nonatomic, strong) AVMutableVideoComposition *mutableVideoComposition;
@property (nonatomic, strong) UGCKitTheme *theme;
@end

@implementation UGCKitAssetLoadingController
- (instancetype)initWithTheme:(UGCKitTheme *)theme
{
    if (self = [self initWithNibName:nil bundle:nil]) {
        _theme = theme;
    }
    return self;
}
- (void)viewDidLoad {
    [super viewDidLoad];
    _progressCache = [[NSMutableDictionary alloc] init];
    _loadingProgressView = [[UGCKitPieProgressView alloc] initWithFrame:CGRectMake(0, 0, 78, 78)];
    _loadingProgressView.tintColor = _theme.progressColor;
    [_loadingProgressView addConstraint:[NSLayoutConstraint constraintWithItem:_loadingProgressView
                                                                     attribute:NSLayoutAttributeWidth
                                                                     relatedBy:NSLayoutRelationEqual
                                                                        toItem:nil
                                                                     attribute:NSLayoutAttributeWidth
                                                                    multiplier:1
                                                                      constant:78]];
    [_loadingProgressView addConstraint:[NSLayoutConstraint constraintWithItem:_loadingProgressView
                                                                     attribute:NSLayoutAttributeHeight
                                                                     relatedBy:NSLayoutRelationEqual
                                                                        toItem:nil
                                                                     attribute:NSLayoutAttributeHeight
                                                                    multiplier:1
                                                                      constant:78]];
    _loadingProgressView.translatesAutoresizingMaskIntoConstraints = NO;
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
                                                                                          target:self
                                                                                          action:@selector(onCancel:)];
    [self.view addSubview:_loadingProgressView];
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:_loadingProgressView
                                                          attribute:NSLayoutAttributeCenterX
                                                          relatedBy:NSLayoutRelationEqual
                                                             toItem:self.view
                                                          attribute:NSLayoutAttributeCenterX
                                                         multiplier:1
                                                           constant:0]];
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:_loadingProgressView
                                                          attribute:NSLayoutAttributeCenterY
                                                          relatedBy:NSLayoutRelationEqual
                                                             toItem:self.view
                                                          attribute:NSLayoutAttributeCenterY
                                                         multiplier:1
                                                           constant:-70]];

    _loadingLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.bounds), 20)];
    _loadingLabel.textColor = _theme.titleColor;
    _loadingLabel.textAlignment = NSTextAlignmentCenter;
    _loadingLabel.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addSubview:_loadingLabel];
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:_loadingLabel
                                                          attribute:NSLayoutAttributeCenterX
                                                          relatedBy:NSLayoutRelationEqual
                                                             toItem:self.view
                                                          attribute:NSLayoutAttributeCenterX
                                                         multiplier:1
                                                           constant:0]];
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:_loadingLabel
                                                          attribute:NSLayoutAttributeTop
                                                          relatedBy:NSLayoutRelationEqual
                                                             toItem:_loadingProgressView
                                                          attribute:NSLayoutAttributeBottom
                                                         multiplier:1
                                                           constant:20]];

    // Do any additional setup after loading the view from its nib.
    self.title = [_theme localizedString:@"UGCKit.MediaPicker.ChoosingVideo"];
    self.view.backgroundColor = UIColor.blackColor;
    self.loadingLabel.text = [_theme localizedString:@"UGCKit.Loading.Decoding"];

    TXPreviewParam *param = [[TXPreviewParam alloc] init];
    param.videoView = [UIView new];
    _videoJoiner = [[TXVideoJoiner alloc] initWithPreview:param];
    _videoJoiner.joinerDelegate = self;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onAudioSessionEvent:)
                                                 name:AVAudioSessionInterruptionNotification
                                               object:nil];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (IBAction)onCancel:(id)sender {
    _loadingIsInterrupt = YES;
    [self.navigationController popViewControllerAnimated:YES];

//    if (self.completion) {
//        self.completion([UGCKitResult cancelledResult]);
//    }
}

- (void) onAudioSessionEvent: (NSNotification *) notification
{
    NSDictionary *info = notification.userInfo;
    AVAudioSessionInterruptionType type = [info[AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];
    if (type == AVAudioSessionInterruptionTypeBegan) {
        _loadingIsInterrupt = YES;
        [self exportAssetError:nil];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)exportAssetList:(NSArray<PHAsset *> *)assets assetType:(AssetType)assetType;
{
    _assets = assets;
    _assetType = assetType;
    if (_assetType == AssetType_Video) {
        _avAssets = [NSMutableArray array];
    }else{
        _imagesToEdit = [NSMutableArray array];
    }
    [self exportAssetInternal];
}

- (void)_exportVideo:(PHAsset*)expAsset index:(NSInteger)index completion:(void(^)(AVAsset *asset, NSError *error, NSInteger index))completion {
    PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
    // 最高质量的视频
    options.deliveryMode = PHVideoRequestOptionsDeliveryModeHighQualityFormat;
    // 可从iCloud中获取图片
    options.networkAccessAllowed = YES;
    // 如果是iCloud的视频，可以获取到下载进度
    WEAKIFY(self);
    options.progressHandler = ^(double progress, NSError * _Nullable error, BOOL * _Nonnull stop, NSDictionary * _Nullable info) {
        STRONGIFY(self);
        if (self) {
            [self loadingCloudVideoProgress:progress index:index];
            *stop = self->_loadingIsInterrupt;
        } else {
            *stop = YES;
        }
    };
    [[PHImageManager defaultManager] requestAVAssetForVideo:expAsset options:options resultHandler:^(AVAsset * _Nullable avAsset, AVAudioMix * _Nullable audioMix, NSDictionary * _Nullable info) {
        if (avAsset) {
            completion(avAsset, nil, index);
        } else {
            NSError *error = info[PHImageErrorKey];
            completion(nil, error, index);
        }
    }];
}

- (void)_exportPhoto:(PHAsset*)expAsset index:(NSInteger)index completion:(void(^)(UIImage *image, NSError *error, NSInteger index))completion {
    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
    options.version = PHImageRequestOptionsVersionCurrent;
    options.networkAccessAllowed = YES;
    options.resizeMode = PHImageRequestOptionsResizeModeExact;
    options.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
    //sync requests are automatically processed this way regardless of the specified mode
    //originRequestOptions.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
    WEAKIFY(self);
    options.progressHandler = ^(double progress, NSError *__nullable error, BOOL *stop, NSDictionary *__nullable info){
        STRONGIFY(self);
        if (self) {
            [self loadingCloudVideoProgress:progress index:index];
            *stop = self->_loadingIsInterrupt;
        } else {
            *stop = YES;
        }
    };
    CGSize maximumSize = CGSizeMake(1280, 1280);
    [[PHImageManager defaultManager] requestImageForAsset:expAsset targetSize:maximumSize contentMode:PHImageContentModeDefault options:options resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
        if (result) {
            completion(result, nil, index);
        } else {
            NSError *error = info[PHImageErrorKey];
            completion(nil, error, index);
        }
    }];
}

- (void)exportAssetInternal
{
    [_progressCache removeAllObjects];
    _prevLoadingProgress = 0;
    // Initialize before async operations
    for (NSInteger i = 0; i < _assets.count; ++i) {
        _progressCache[@(i)] = @0;
    }
    NSMutableDictionary *sortAssetDic = [[NSMutableDictionary alloc] initWithCapacity:_assets.count];
    __block NSError *blockError = nil;
    dispatch_group_t grp = dispatch_group_create();
    for (NSInteger i = 0; i < _assets.count; i+=1) {
        PHAsset *asset = _assets[i];
        if (blockError) {
            break;
        }
        dispatch_group_enter(grp);
        if (_assetType == AssetType_Video) {
            [self _exportVideo:asset index:i completion:^(AVAsset *asset, NSError *error, NSInteger index) {
                if (error) {
                    blockError = error;
                    NSLog(@"Error: %@", error);
                } else {
                    @synchronized (sortAssetDic) {
                        if (asset) {
                            [sortAssetDic setObject:asset forKey:@(index)];
                        }
                    }
                }
                dispatch_group_leave(grp);
            }];
        } else {
            [self _exportPhoto:asset index:i completion:^(UIImage *image, NSError *error, NSInteger index) {
                if (error) {
                    blockError = error;
                    NSLog(@"Error: %@", error);
                } else {
                    @synchronized (sortAssetDic) {
                        if (image) {
                            [sortAssetDic setObject:image forKey:@(index)];
                        }
                    }
                }
                dispatch_group_leave(grp);
            }];
        }
    }
    dispatch_group_notify(grp, dispatch_get_main_queue(), ^{
        for (NSInteger i = 0; i < self->_assets.count; i+=1) {
            if (!sortAssetDic[@(i)]) {
                continue;
            }
            if (self->_assetType == AssetType_Video) {
                [self->_avAssets addObject:sortAssetDic[@(i)]];
            } else {
                [self->_imagesToEdit addObject:sortAssetDic[@(i)]];
            }
        }
        if (blockError) {
            UGCKitResult *result = [[UGCKitResult alloc] init];
            if (blockError.code == 3072 /* PHPhotosErrorUserCancelled */) {
                result.cancelled = YES;
            } else {
                result.code = blockError.code;
                result.info = blockError.userInfo;
            }
            if (self.completion) {
                self.completion(result);
            }
            return;
        }
        if (self->_assetType == AssetType_Video) {
            if (self->_avAssets.count == 1 || !self.combineVideos){
                UGCKitMedia *media = [UGCKitMedia mediaWithAVAsset:self->_avAssets.firstObject];
                UGCKitResult *result = [[UGCKitResult alloc] init];
                result.media = media;
                if (self.completion) {
                    self.completion(result);
                }
            } else {
                [self joinVideoAssets];
            }
        } else {
            UGCKitMedia *media =  [UGCKitMedia mediaWithImages:self->_imagesToEdit canvasSize:CGSizeMake(720,1280)];
            UGCKitResult *result = [[UGCKitResult alloc] init];
            result.media = media;
            if (self.completion) {
                self.completion(result);
            }
        }
    });
}

- (void)exportAssetError:(NSError *)error
{
    NSString *errorMessage = error ? error.localizedDescription : [_theme localizedString:@"UGCKit.MediaPicker.HintVideoExportingFailed"];
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Error" message:errorMessage preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:[_theme localizedString:@"UGCKit.Common.OK"] style:0 handler:^(UIAlertAction * _Nonnull action) {
        [self.navigationController dismissViewControllerAnimated:YES completion:nil];
    }];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)loadingCloudVideoProgress:(float)progress index:(NSInteger)index
{
    @synchronized (_progressCache) {
        _progressCache[@(index)] = @(progress);
    }

    __block float total = 0.0;
    [_progressCache enumerateKeysAndObjectsUsingBlock:^(NSNumber * _Nonnull key, NSNumber * _Nonnull obj, BOOL * _Nonnull stop) {
        total += obj.floatValue;
    }];
    total /= _progressCache.count;
    if (_prevLoadingProgress >= total) {
        return;
    }
    _prevLoadingProgress = total;
    NSString *progressText = [_theme localizedString:@"UGCKit.MediaPicker.VideoDownloadingFromiCloud"];
    dispatch_async(dispatch_get_main_queue(), ^{
        self.loadingLabel.text = progressText;
        self.loadingProgressView.progress = total;
//        self.loadingProgressView.image = [UIImage imageNamed:[NSString stringWithFormat:@"video_record_share_loading_%d", (int)(total * 8)]];
    });
}

- (CGSize)getVideoSize:(CGSize)sourceSize;
{
    CGSize videoSize = CGSizeMake(sourceSize.width, sourceSize.height);
    if (videoSize.height >= videoSize.width) {
        if([self supportCompressSize:CGSizeMake(720, 1280) videoSize:videoSize]){
            videoSize = [self compress:CGSizeMake(720, 1280) videoSize:videoSize];
        }
    }else{
        if([self supportCompressSize:CGSizeMake(1280, 720) videoSize:videoSize]){
            videoSize = [self compress:CGSizeMake(1280, 720) videoSize:videoSize];
        }
    }
    return videoSize;
}

//判断是否需要压缩图片
-(BOOL)supportCompressSize:(CGSize)compressSize videoSize:(CGSize)videoSize
{
    if (videoSize.width >= compressSize.width && videoSize.height >= compressSize.height) {
        return YES;
    }
    if (videoSize.width >= compressSize.height && videoSize.height >= compressSize.width) {
        return YES;
    }
    return NO;
}

//获得压缩后图片大小
- (CGSize)compress:(CGSize)compressSize videoSize:(CGSize)videoSize
{
    CGSize size = CGSizeZero;
    if (compressSize.height / compressSize.width >= videoSize.height / videoSize.width) {
        size.width = compressSize.width;
        size.height = compressSize.width * videoSize.height / videoSize.width;
    }else{
        size.height = compressSize.height;
        size.width = compressSize.height * videoSize.width / videoSize.height;
    }
    return size;
}

- (UIImage*)scaleImage:(UIImage *)image scaleToSize:(CGSize)size{
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return scaledImage;
}

#define degreesToRadians( degrees ) ( ( degrees ) / 180.0 * M_PI )

- (void)performWithAsset:(AVAsset*)asset rotate:(CGFloat)angle
{
    AVMutableVideoCompositionInstruction *instruction = nil;
    AVMutableVideoCompositionLayerInstruction *layerInstruction = nil;
    CGAffineTransform t1;
    CGAffineTransform t2;
    
    AVAssetTrack *assetVideoTrack = nil;
    AVAssetTrack *assetAudioTrack = nil;
    // Check if the asset contains video and audio tracks
    if ([[asset tracksWithMediaType:AVMediaTypeVideo] count] != 0) {
        assetVideoTrack = [asset tracksWithMediaType:AVMediaTypeVideo][0];
    }
    if ([[asset tracksWithMediaType:AVMediaTypeAudio] count] != 0) {
        assetAudioTrack = [asset tracksWithMediaType:AVMediaTypeAudio][0];
    }
    
    CMTime insertionPoint = kCMTimeZero;
    NSError *error = nil;
    
    
    // Step 1
    // Create a composition with the given asset and insert audio and video tracks into it from the asset
    if (!self.mutableComposition) {
        
        // Check whether a composition has already been created, i.e, some other tool has already been applied
        // Create a new composition
        self.mutableComposition = [AVMutableComposition composition];
        
        // Insert the video and audio tracks from AVAsset
        if (assetVideoTrack != nil) {
            AVMutableCompositionTrack *compositionVideoTrack = [self.mutableComposition addMutableTrackWithMediaType:AVMediaTypeVideo preferredTrackID:kCMPersistentTrackID_Invalid];
            [compositionVideoTrack insertTimeRange:CMTimeRangeMake(kCMTimeZero, [asset duration]) ofTrack:assetVideoTrack atTime:insertionPoint error:&error];
        }
        if (assetAudioTrack != nil) {
            AVMutableCompositionTrack *compositionAudioTrack = [self.mutableComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
            [compositionAudioTrack insertTimeRange:CMTimeRangeMake(kCMTimeZero, [asset duration]) ofTrack:assetAudioTrack atTime:insertionPoint error:&error];
        }
        
    }
    
    
    // Step 2
    // Translate the composition to compensate the movement caused by rotation (since rotation would cause it to move out of frame)
    if (angle == 90)
    {
        t1 = CGAffineTransformMakeTranslation(assetVideoTrack.naturalSize.height, 0.0);
    }else if (angle == -90){
        t1 = CGAffineTransformMakeTranslation(0.0, assetVideoTrack.naturalSize.width);
    } else {
        return;
    }
    // Rotate transformation
    t2 = CGAffineTransformRotate(t1, degreesToRadians(angle));
    
    
    // Step 3
    // Set the appropriate render sizes and rotational transforms
    if (!self.mutableVideoComposition) {
        
        // Create a new video composition
        self.mutableVideoComposition = [AVMutableVideoComposition videoComposition];
        self.mutableVideoComposition.renderSize = CGSizeMake(assetVideoTrack.naturalSize.height,assetVideoTrack.naturalSize.width);
        self.mutableVideoComposition.frameDuration = CMTimeMake(1, 30);
        
        // The rotate transform is set on a layer instruction
        instruction = [AVMutableVideoCompositionInstruction videoCompositionInstruction];
        instruction.timeRange = CMTimeRangeMake(kCMTimeZero, [self.mutableComposition duration]);
        layerInstruction = [AVMutableVideoCompositionLayerInstruction videoCompositionLayerInstructionWithAssetTrack:(self.mutableComposition.tracks)[0]];
        [layerInstruction setTransform:t2 atTime:kCMTimeZero];
        
    } else {
        
        self.mutableVideoComposition.renderSize = CGSizeMake(self.mutableVideoComposition.renderSize.height, self.mutableVideoComposition.renderSize.width);
        
        // Extract the existing layer instruction on the mutableVideoComposition
        instruction = (AVMutableVideoCompositionInstruction*)(self.mutableVideoComposition.instructions)[0];
        layerInstruction = (AVMutableVideoCompositionLayerInstruction*)(instruction.layerInstructions)[0];
        
        // Check if a transform already exists on this layer instruction, this is done to add the current transform on top of previous edits
        CGAffineTransform existingTransform;
        
        if (![layerInstruction getTransformRampForTime:[self.mutableComposition duration] startTransform:&existingTransform endTransform:NULL timeRange:NULL]) {
            [layerInstruction setTransform:t2 atTime:kCMTimeZero];
        } else {
            // Note: the point of origin for rotation is the upper left corner of the composition, t3 is to compensate for origin
            CGAffineTransform t3 = CGAffineTransformMakeTranslation(-1*assetVideoTrack.naturalSize.height/2, 0.0);
            CGAffineTransform newTransform = CGAffineTransformConcat(existingTransform, CGAffineTransformConcat(t2, t3));
            [layerInstruction setTransform:newTransform atTime:kCMTimeZero];
        }
        
    }
    
    
    // Step 4
    // Add the transform instructions to the video composition
    instruction.layerInstructions = @[layerInstruction];
    self.mutableVideoComposition.instructions = @[instruction];
    
    
    // Step 5
    // Notify AVSEViewController about rotation operation completion
    //    [[NSNotificationCenter defaultCenter] postNotificationName:AVSEEditCommandCompletionNotification object:self];
}


- (void)joinVideoAssets {
    _joinedVideoPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"outputJoin.mp4"];
    int ret = 0;
    NSArray *videoAssets = _avAssets;
    if ([videoAssets.firstObject isKindOfClass:[NSString class]]) {
        ret = [_videoJoiner setVideoPathList:videoAssets];
    } else {
        ret = [_videoJoiner setVideoAssetList:videoAssets];
    }
    _videoJoiner.joinerDelegate = self;
    if (ret == 0) {
        [_videoJoiner joinVideo:VIDEO_COMPRESSED_720P videoOutputPath:_joinedVideoPath];
        _loadingLabel.text = [_theme localizedString:@"UGCKit.Media.VideoSynthesizing"];
        self.loadingProgressView.progress = 0;
//        self.loadingProgressView.image = [UIImage imageNamed:[NSString stringWithFormat:@"video_record_share_loading_%d", 0]];
    } else {
        if (self.completion) {
            UGCKitResult *result = [[UGCKitResult alloc] init];
            result.code = ret;
            result.info = @{NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Join failed: %d", ret]};
            self.completion(result);
        }
    }
}

#pragma mark TXVideoJoinerListener
-(void) onJoinProgress:(float)progress
{
    self.loadingProgressView.progress = progress;
}

-(void) onJoinComplete:(TXJoinerResult *)joinResult
{
    if (joinResult.retCode == JOINER_RESULT_OK) {
        UGCKitResult *result = [[UGCKitResult alloc] init];
        result.media = [UGCKitMedia mediaWithVideoPath: _joinedVideoPath];
        if (self.completion) {
            self.completion(result);
        }
    } else {
        UGCKitResult *result = [[UGCKitResult alloc] init];
        result.code = joinResult.retCode;
        result.info = @{NSLocalizedDescriptionKey: joinResult.descMsg};
        if (self.completion) {
            self.completion(result);
        }
    }
    [UGCKitReporter report:UGCKitReportItem_videojoiner userName:nil code:joinResult.retCode msg:joinResult.descMsg];
}


@end
