//
//  TCPlayViewCell.m
//  TXXiaoShiPinDemo
//
//  Created by xiang zhang on 2018/2/2.
//  Copyright © 2018年 tencent. All rights reserved.
//

#import "TCPlayViewCell.h"
#import "UIImageView+WebCache.h"
#import "TCUtil.h"
#import "UIView+Additions.h"

#define FULL_SCREEN_PLAY_VIDEO_VIEW     10000

@implementation TCPlayViewCell
- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        CGRect frame = self.contentView.bounds;// [UIScreen mainScreen].bounds;
        _videoCoverView = [[UIImageView alloc] initWithFrame:self.contentView.bounds];
        _videoCoverView.contentMode = UIViewContentModeScaleAspectFill;
        _videoCoverView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self.contentView addSubview:_videoCoverView];
        
        _videoParentView = [[UIView alloc] initWithFrame:frame];
        _videoParentView.tag = FULL_SCREEN_PLAY_VIDEO_VIEW;
        _videoParentView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        [self.contentView addSubview:_videoParentView];
        
        _logicView = [[TCPlayDecorateView alloc] initWithFrame:frame];
        _logicView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        _logicView.delegate = self;
        [self.contentView addSubview:_logicView];
        
        _reviewLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.contentView.width / 2 - 50, self.contentView.height / 2 - 25 , 100, 50)];
        _reviewLabel.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        _reviewLabel.textAlignment = NSTextAlignmentCenter;
        _reviewLabel.font = [UIFont systemFontOfSize:18];
        _reviewLabel.textColor = [UIColor whiteColor];
        [self.contentView addSubview:_reviewLabel];
    }
    return self;
}

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)prepareForReuse
{
    [super prepareForReuse];
    [_logicView preprareForReuse];
}

-(void)setLiveInfo:(TCLiveInfo *)liveInfo
{
    [_videoParentView removeAllSubViews];
    ReviewStatus reviewStatus = liveInfo.reviewStatus;
    switch (reviewStatus) {
        case ReviewStatus_Normal:
        {
            if (liveInfo.userinfo.frontcoverImage) {
                [_videoCoverView setImage:liveInfo.userinfo.frontcoverImage];
            }else{
                [_videoCoverView sd_setImageWithURL:[NSURL URLWithString:[TCUtil transImageURL2HttpsURL:liveInfo.userinfo.frontcover]] placeholderImage:[UIImage imageNamed:@"bg.jpg"] completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
                    liveInfo.userinfo.frontcoverImage = image;
                }];
            }
            _reviewLabel.text = @"";
            _logicView.btnChorus.hidden = NO;
        }
            break;
        case ReviewStatus_NotReivew:
        {
            [_videoCoverView setImage:[UIImage imageNamed:@"bg.jpg"]];
            _reviewLabel.text = NSLocalizedString(@"TCPlayViewCell.UncensoredVideo", nil);
            _logicView.btnChorus.hidden = YES;
        }
            break;
        case ReviewStatus_Porn:
        {
            [_videoCoverView setImage:[UIImage imageNamed:@"bg.jpg"]];
            _reviewLabel.text = NSLocalizedString(@"TCPlayViewCell.AdultVideo", nil);
            _logicView.btnChorus.hidden = YES;
        }
            break;
        default:
            break;
    }

    [_logicView setLiveInfo:liveInfo];
    [_logicView.playProgress setValue:0];
}

-(void)setPlayLabelText:(NSString *)text
{
    [_logicView.playLabel setText:text];
}

-(void)setPlayProgress:(CGFloat)progress
{
    [_logicView.playProgress setValue:progress];
}

-(void)setPlayBtnImage:(UIImage *)image
{
    [_logicView.playBtn setImage:image forState:UIControlStateNormal];
}

#pragma TCPlayDecorateDelegate
-(void)closeVC:(BOOL)isRefresh popViewController:(BOOL)popViewController
{
    if (_delegate && [_delegate respondsToSelector:@selector(closeVC:popViewController:)]) {
        [_delegate closeVC:isRefresh popViewController:popViewController];
    }
}

-(void)clickScreen:(UITapGestureRecognizer *)gestureRecognizer
{
    if (_delegate && [_delegate respondsToSelector:@selector(clickScreen:)]) {
        [_delegate clickScreen:gestureRecognizer];
    }
}

-(void)clickPlayVod
{
    if (_delegate && [_delegate respondsToSelector:@selector(clickPlayVod)]) {
        [_delegate clickPlayVod];
    }
}

-(void)onSeek:(UISlider *)slider
{
    if (_delegate && [_delegate respondsToSelector:@selector(onSeek:)]) {
        [_delegate onSeek:slider];
    }
}

-(void)onSeekBegin:(UISlider *)slider
{
    if (_delegate && [_delegate respondsToSelector:@selector(onSeekBegin:)]) {
        [_delegate onSeekBegin:slider];
    }
}

-(void)onSeekEnd:(UISlider *)slider
{
    if (_delegate && [_delegate respondsToSelector:@selector(onSeekEnd:)]) {
        [_delegate onSeekEnd:slider];
    }
}

-(void)clickLog:(UIButton *)button
{
    if (_delegate && [_delegate respondsToSelector:@selector(clickLog:)]) {
        [_delegate clickLog:button];
    }
}

-(void)clickChorus:(UIButton *)button
{
    if (_delegate && [_delegate respondsToSelector:@selector(clickChorus:)]) {
        [_delegate clickChorus:button];
    }
}

-(void)clickShare:(UIButton *)button
{
    if (_delegate && [_delegate respondsToSelector:@selector(clickShare:)]) {
        [_delegate clickShare:button];
    }
}


@end
