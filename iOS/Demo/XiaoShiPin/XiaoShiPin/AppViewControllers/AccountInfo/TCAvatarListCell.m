//
//  TCAvatarListCell.m
//  XiaoShiPinApp
//
//  Created by tao yue on 2022/3/8.
//  Copyright © 2022 Tencent. All rights reserved.
//

#import "TCAvatarListCell.h"
#import "UIImageView+WebCache.h"
#import "TCLiveListModel.h"
#import "UIView+Additions.h"
#import <sys/types.h>
#import <sys/sysctl.h>
#import "TCUtil.h"

@interface TCAvatarListCell()
{
    UIImageView *_headImageView;
    UIView      *_userMsgView;
}

@end


@implementation TCAvatarListCell

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self initUIForUGC];
    }
    return self;
}

-(void)layoutSubviews{
    [super layoutSubviews];
    [self layoutForUGC];
}

- (void)prepareForReuse
{
    [super prepareForReuse];
    [_headImageView sd_setImageWithURL:nil];
}

- (void)initUIForUGC {
    for (UIView *view in self.contentView.subviews) {
        [view removeFromSuperview];
    }
    
    self.contentView.backgroundColor = [UIColor clearColor];
    _headImageView = [[UIImageView alloc] initWithFrame:CGRectZero];
    [self.contentView addSubview:_headImageView];
    
}


- (void)layoutForUGC {
    //头像
    _headImageView.frame = CGRectMake(0, 0, 70, 70);
    _headImageView.layer.cornerRadius  = _headImageView.height * 0.5;
    _headImageView.layer.borderWidth   = 3;
    _headImageView.layer.masksToBounds = YES;
    if([_avatarUrl isEqualToString:_avatarUrlSelected]){
        _headImageView.layer.borderColor   = [UIColor blueColor].CGColor;
    }else{
        _headImageView.layer.borderColor   = [UIColor clearColor].CGColor;
    }
    
}


- (void)setAvatarUrl:(NSString *)avatarUrl {
    _avatarUrl = avatarUrl;
    [_headImageView sd_setImageWithURL:[NSURL URLWithString:_avatarUrl] placeholderImage:[UIImage imageNamed:@"default_user"]];

    [self layoutForUGC];
}

- (void)setAvatarUrlSelected:(NSString *)avatarUrlSelected{
    _avatarUrlSelected = avatarUrlSelected;
}

-(UIImage *)scaleClipImage:(UIImage *)image clipW:(CGFloat)clipW clipH:(CGFloat)clipH{
    UIImage *newImage = nil;
    if (image != nil) {
        if (image.size.width >=  clipW && image.size.height >= clipH) {
            newImage = [self clipImage:image inRect:CGRectMake((image.size.width - clipW)/2, (image.size.height - clipH)/2, clipW,clipH)];
        }else{
            CGFloat widthRatio = clipW / image.size.width;
            CGFloat heightRatio = clipH / image.size.height;
            CGFloat imageNewHeight = 0;
            CGFloat imageNewWidth = 0;
            UIImage *scaleImage = nil;
            if (widthRatio < heightRatio) {
                imageNewHeight = clipH;
                imageNewWidth = imageNewHeight * image.size.width / image.size.height;
                scaleImage = [self scaleImage:image scaleToSize:CGSizeMake(imageNewWidth, imageNewHeight)];
            }else{
                imageNewWidth = clipW;
                imageNewHeight = imageNewWidth * image.size.height / image.size.width;
                scaleImage = [self scaleImage:image scaleToSize:CGSizeMake(imageNewWidth, imageNewHeight)];
            }
            newImage = [self clipImage:image inRect:CGRectMake((scaleImage.size.width - clipW)/2, (scaleImage.size.height - clipH)/2, clipW,clipH)];
        }
    }
    return newImage;
}

/**
 *缩放图片
 */
-(UIImage*)scaleImage:(UIImage *)image scaleToSize:(CGSize)size{
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return scaledImage;
}

/**
 *裁剪图片
 */
-(UIImage *)clipImage:(UIImage *)image inRect:(CGRect)rect{
    CGImageRef sourceImageRef = [image CGImage];
    CGImageRef newImageRef = CGImageCreateWithImageInRect(sourceImageRef, rect);
    UIImage *newImage = [UIImage imageWithCGImage:newImageRef];
    CGImageRelease(newImageRef);
    return newImage;
}

@end
