// Copyright (c) 2019 Tencent. All rights reserved.

#ifndef CommonLibrary_ColorMarcro_h
#define CommonLibrary_ColorMarcro_h

//#import "UIColor+MLPFlatColors.h"


// 取色值相关的方法
#define RGB(r,g,b)          [UIColor colorWithRed:(r)/255.f \
                                            green:(g)/255.f \
                                             blue:(b)/255.f \
                                            alpha:1.f]

#define RGBA(r,g,b,a)       [UIColor colorWithRed:(r)/255.f \
                                            green:(g)/255.f \
                                             blue:(b)/255.f \
                                            alpha:(a)]

#define RGBOF(rgbValue)     [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
                                            green:((float)((rgbValue & 0xFF00) >> 8))/255.0 \
                                             blue:((float)(rgbValue & 0xFF))/255.0 \
                                            alpha:1.0]

#define RGBA_OF(rgbValue)   [UIColor colorWithRed:((float)(((rgbValue) & 0xFF000000) >> 24))/255.0 \
                                             green:((float)(((rgbValue) & 0x00FF0000) >> 16))/255.0 \
                                              blue:((float)(rgbValue & 0x0000FF00) >> 8)/255.0 \
                                             alpha:((float)(rgbValue & 0x000000FF))/255.0]

#define RGBAOF(v, a)        [UIColor colorWithRed:((float)(((v) & 0xFF0000) >> 16))/255.0 \
                                            green:((float)(((v) & 0x00FF00) >> 8))/255.0 \
                                             blue:((float)(v & 0x0000FF))/255.0 \
                                            alpha:a]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
green:((float)((rgbValue & 0xFF00) >> 8))/255.0 \
 blue:((float)(rgbValue & 0xFF))/255.0 \
alpha:1.0]

// 定义通用颜色
//#define kBlackColor         [UIColor blackColor]
//#define kDarkGrayColor      [UIColor darkGrayColor]
//#define kLightGrayColor     [UIColor lightGrayColor]
//#define kWhiteColor         [UIColor whiteColor]
//#define kGrayColor          [UIColor grayColor]
//#define kRedColor           [UIColor redColor]
//#define kGreenColor         [UIColor greenColor]
//#define kBlueColor          [UIColor blueColor]
//#define kCyanColor          [UIColor cyanColor]
//#define kYellowColor        [UIColor yellowColor]
//#define kMagentaColor       [UIColor magentaColor]
//#define kOrangeColor        [UIColor orangeColor]
//#define kPurpleColor        [UIColor purpleColor]
//#define kClearColor         [UIColor clearColor]
//
//#define kRandomFlatColor    [UIColor randomFlatColor]


#endif
