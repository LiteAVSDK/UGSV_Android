// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitTheme.h"

typedef NS_ENUM(NSInteger,UGCKitPasterTtemType)
{
    UGCKitPasterTtemType_Paster,
    UGCKitPasterTtemType_Qipao,
};

typedef NS_ENUM(NSInteger,UGCKitPasterType)
{
    UGCKitPasterType_Qipao,
    UGCKitPasterType_Animate,
    UGCKitPasterType_static,
};

@interface UGCKitPasterQipaoInfo : NSObject
@property(nonatomic,strong) UIImage *image;
@property(nonatomic,strong) UIImage *iconImage;
@property(nonatomic,assign) CGFloat width;
@property(nonatomic,assign) CGFloat height;
@property(nonatomic,assign) CGFloat textTop;
@property(nonatomic,assign) CGFloat textLeft;
@property(nonatomic,assign) CGFloat textRight;
@property(nonatomic,assign) CGFloat textBottom;
@end

@interface UGCKitPasterAnimateInfo : NSObject
@property(nonatomic,strong) NSString *path;
@property(nonatomic,strong) UIImage *iconImage;
@property(nonatomic,strong) NSMutableArray *imageList;
@property(nonatomic,assign) CGFloat duration;   //s
@property(nonatomic,assign) CGFloat width;
@property(nonatomic,assign) CGFloat height;
@end

@interface UGCKitPasterStaticInfo : NSObject
@property(nonatomic,strong) UIImage *image;
@property(nonatomic,strong) UIImage *iconImage;
@property(nonatomic,assign) CGFloat width;
@property(nonatomic,assign) CGFloat height;
@end

@protocol UGCKitPasterAddViewDelegate <NSObject>
@optional
- (void)onPasterQipaoSelect:(UGCKitPasterQipaoInfo *)info;

@optional
- (void)onPasterAnimateSelect:(UGCKitPasterAnimateInfo *)info;

@optional
- (void)onPasterStaticSelect:(UGCKitPasterStaticInfo *)info;

@end


@interface UGCKitPasterAddView : UIView
@property(nonatomic,weak) id <UGCKitPasterAddViewDelegate> delegate;
- (instancetype) initWithFrame:(CGRect)frame theme:(UGCKitTheme *)theme;
- (void) setUGCKitPasterType:(UGCKitPasterTtemType)pasterTtemType;
@end
