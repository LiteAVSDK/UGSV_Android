// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVideoCutView.h"
#import "UGCKitVideoRangeConst.h"
#import "UGCKitVideoRangeSlider.h"
#import "UGCKitColorMacro.h"
#import "UGCKit_UIViewAdditions.h"
#import "SDKHeader.h"
#import <CoreImage/CoreImage.h>

@interface UGCKitVideoCutView ()<VideoRangeSliderDelegate>

@end

@implementation UGCKitVideoCutView
{
    CGFloat         _duration;          //视频时长
    NSString*       _videoPath;         //视频路径
    AVAsset*        _videoAsset;
    BOOL            _thumbnailRequestCancelled;
}

- (id)initWithFrame:(CGRect)frame videoPath:(NSString *)videoPath  videoAsset:(AVAsset *)videoAsset config:(UGCKitRangeContentConfig *)config
{
    if (self = [super initWithFrame:frame]) {
        _videoPath = videoPath;
        _videoAsset = videoAsset;
        if (videoAsset == nil) {
            _videoAsset = [AVAsset assetWithURL:[NSURL fileURLWithPath:videoPath]];
        }
        
        _videoRangeSlider = [[UGCKitVideoRangeSlider alloc] initWithFrame:self.bounds];
        [_videoRangeSlider setAppearanceConfig:config];
        [self addSubview:_videoRangeSlider];
        
        TXVideoInfo *videoMsg = [TXVideoInfoReader getVideoInfoWithAsset:_videoAsset];
        _duration   = videoMsg.duration;
        _videoRangeSlider.fps = videoMsg.fps;
        
        //显示微缩图列表
        _imageList = [NSMutableArray new];
        int imageNum = (int)config.imageCount;
        
        _thumbnailRequestCancelled = NO;
        
        UIGraphicsBeginImageContext(CGSizeMake(1, 1));
        UIImage *placeholder = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();

        CGFloat size = round(frame.size.height * [UIScreen mainScreen].scale);

        [TXVideoInfoReader getSampleImages:imageNum maxSize:CGSizeMake(size, size) videoAsset:_videoAsset progress:^BOOL(int number, UIImage *image) {
            if (self->_thumbnailRequestCancelled) {
                return NO;
            }else{
                dispatch_async(dispatch_get_main_queue(), ^{@autoreleasepool {
                    if (self->_thumbnailRequestCancelled) {
                        return;
                    }
                    UIImage *img = image ?: placeholder;
                    
                    if (number == 1) {
                        self->_videoRangeSlider.delegate = self;
                        for (int i = 0; i < imageNum; i++) {
                            [self->_imageList addObject:img];
                        }
                        [self->_videoRangeSlider setImageList:self->_imageList];
                        [self->_videoRangeSlider setDurationMs:self->_duration];
                    } else {
                        self->_imageList[number-1] = img;
                        [self->_videoRangeSlider updateImage:img atIndex:number-1];
                    }
                }});
                return YES;
            }
        }];
      
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame pictureList:(NSArray *)pictureList  duration:(CGFloat)duration fps:(float)fps config:(UGCKitRangeContentConfig *)config
{
    if (self = [super initWithFrame:frame]) {
        _duration   = duration;
        _imageList = [pictureList mutableCopy];
        
        _videoRangeSlider = [[UGCKitVideoRangeSlider alloc] initWithFrame:self.bounds];
        [_videoRangeSlider setAppearanceConfig:config];
        _videoRangeSlider.fps = fps;
        [self addSubview:_videoRangeSlider];
        _videoRangeSlider.delegate = self;
        
        [_videoRangeSlider setImageList:_imageList];
        [_videoRangeSlider setDurationMs:_duration];
    }
    return self;
}

- (void)updateFrame:(CGFloat)duration
{
    
}

- (void)stopGetImageList
{
    _thumbnailRequestCancelled = YES;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
}

- (void)dealloc
{
    NSLog(@"VideoCutView dealloc");
}

- (void)setPlayTime:(CGFloat)time
{
    _videoRangeSlider.currentPos = time;
}

- (void)setLeftPanHidden:(BOOL)isHidden
{
    [_videoRangeSlider setLeftPanHidden:isHidden];
}

- (void)setCenterPanHidden:(BOOL)isHidden
{
    [_videoRangeSlider setCenterPanHidden:isHidden];
}

- (void)setRightPanHidden:(BOOL)isHidden
{
    [_videoRangeSlider setRightPanHidden:isHidden];
}

- (void)setLeftPanFrame:(CGFloat)time
{
    [_videoRangeSlider setLeftPanFrame:time];
}

- (void)setCenterPanFrame:(CGFloat)time
{
    [_videoRangeSlider setCenterPanFrame:time];
}

- (void)setRightPanFrame:(CGFloat)time
{
    [_videoRangeSlider setRightPanFrame:time];
}

- (void)setColorType:(UGCKitRangeColorType)UGCKitRangeColorType
{
    [_videoRangeSlider setColorType:UGCKitRangeColorType];
}

- (void)startColoration:(UIColor *)color alpha:(CGFloat)alpha
{
    [_videoRangeSlider startColoration:color alpha:alpha];
}

- (void)stopColoration
{
    [_videoRangeSlider stopColoration];
}

- (UGCKitVideoColorInfo *)removeLastColoration:(UGCKitRangeColorType)UGCKitRangeColorType
{
    return [_videoRangeSlider removeLastColoration:UGCKitRangeColorType];
}

- (void)removeColoration:(UGCKitRangeColorType)UGCKitRangeColorType index:(NSInteger)index
{
    [_videoRangeSlider removeColoration:UGCKitRangeColorType index:index];
}

- (void)setSelectColorInfo:(NSInteger)selectedIndex
{
    [_videoRangeSlider setSelectColorInfo:selectedIndex];
}

#pragma mark - VideoRangeDelegate
- (void)onVideoRangeTap:(CGFloat)tapTime
{
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeTap:)]){
        [self.delegate onVideoRangeTap:tapTime];
    }
}

//左拉
- (void)onVideoRangeLeftChanged:(UGCKitVideoRangeSlider *)sender
{
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeLeftChanged:)]){
        [self.delegate onVideoRangeLeftChanged:sender];
    }
}

- (void)onVideoRangeLeftChangeEnded:(UGCKitVideoRangeSlider *)sender
{
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeLeftChangeEnded:)]){
        _videoRangeSlider.currentPos = sender.leftPos;
        [self.delegate onVideoRangeLeftChangeEnded:sender];
    }
}

//中拉
- (void)onVideoRangeCenterChanged:(UGCKitVideoRangeSlider *)sender
{
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeCenterChanged:)]){
        [self.delegate onVideoRangeCenterChanged:sender];
    }
}

- (void)onVideoRangeCenterChangeEnded:(UGCKitVideoRangeSlider *)sender
{
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeCenterChangeEnded:)]){
        [self.delegate onVideoRangeCenterChangeEnded:sender];
    }
}

//右拉
- (void)onVideoRangeRightChanged:(UGCKitVideoRangeSlider *)sender {
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeRightChanged:)]){
        [self.delegate onVideoRangeRightChanged:sender];
    }
}

- (void)onVideoRangeRightChangeEnded:(UGCKitVideoRangeSlider *)sender
{
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoRangeRightChangeEnded:)]){
        _videoRangeSlider.currentPos = sender.leftPos;
        [self.delegate onVideoRangeRightChangeEnded:sender];
    }
}

- (void)onVideoRangeLeftAndRightChanged:(UGCKitVideoRangeSlider *)sender {
    
}

//拖动缩略图条
- (void)onVideoRange:(UGCKitVideoRangeSlider *)sender seekToPos:(CGFloat)pos {
    if(self.delegate && [self.delegate respondsToSelector:@selector(onVideoSeekChange:seekToPos:)]){
        [self.delegate onVideoSeekChange:sender seekToPos:pos];
    }
}

@end
