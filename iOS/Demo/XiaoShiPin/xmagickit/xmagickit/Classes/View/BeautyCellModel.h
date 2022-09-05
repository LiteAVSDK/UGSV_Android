//
//  BeautyCellModel.h
//  PituMotionDemo
//
//  Created by xingyunmao on 2021/2/19.
//  Copyright © 2021 Pitu. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/*
 * BeautyCellModel 类说明 : 该类用于存储美颜项中每一条cell内容
 *
 */
@interface BeautyCellModel : NSObject

@property(copy, nonatomic) NSString* title;  //名称
@property(copy, nonatomic) NSString* key;  //key
@property(copy, nonatomic) NSNumber* beautyValue;  //数值
@property(copy, nonatomic) NSNumber* originValue;  //原始数值
@property(copy, nonatomic) NSDictionary *extraConfig;  //extraConfig
@property(copy, nonatomic) NSNumber* strength;  //strength
@property(copy, nonatomic) NSString* path;  //路径
@property(copy, nonatomic) NSString* lut;  //滤镜
@property(copy, nonatomic) NSString* icon;  //icon
@property(copy, nonatomic) NSString *iconUrl;  //iconUrl

- (instancetype)initWithDict:(NSDictionary *)dict;
+ (instancetype)beautyWithDict:(NSDictionary *)dict;

@end

NS_ASSUME_NONNULL_END
