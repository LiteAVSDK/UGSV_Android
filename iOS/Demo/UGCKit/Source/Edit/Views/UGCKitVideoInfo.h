// Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>
#import "UGCKitVideoPasterView.h"
#import "UGCKitVideoTextFiled.h"
typedef NS_ENUM(NSInteger,UGCKitPasterInfoType)
{
    UGCKitPasterInfoType_Animate,
    UGCKitPasterInfoType_static,
};

@interface UGCKitVideoInfo : NSObject
@property (nonatomic, assign) CGFloat startTime; //in seconds
@property (nonatomic, assign) CGFloat endTime;   //in seconds
@end

@interface UGCKitVideoPasterInfo : UGCKitVideoInfo
@property (nonatomic, assign) UGCKitPasterInfoType pasterInfoType;
@property (nonatomic, strong) UGCKitVideoPasterView* pasterView;
@property (nonatomic, strong) UIImage  *iconImage;
//动态贴纸
@property (nonatomic, strong) NSString *path;        //动态贴纸需要文件路径 -> SDK
@property (nonatomic, assign) CGFloat  rotateAngle;  //动态贴纸需要传入旋转角度 -> SDK
//静态贴纸
@property (nonatomic, strong) UIImage  *image;       //静态贴纸需要贴纸Image -> SDK
@end

@interface UGCKitVideoTextInfo : UGCKitVideoInfo
@property (nonatomic, strong) UGCKitVideoTextFiled* textField;
@end
