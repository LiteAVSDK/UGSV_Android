//
//  FilterCellModel.h
//  PituMotionDemo
//
//  Created by xingyunmao on 2021/2/19.
//  Copyright © 2021 Pitu. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/*
 * FilterCellModel 类说明 : 该类用于存储美颜项中每一条cell内容
 *
 */
@interface FilterCellModel : NSObject

@property(copy, nonatomic) NSString *title;  //名称
@property(copy, nonatomic) NSString *icon;  //icon
@property(copy, nonatomic) NSString *key;  //key
@property(copy, nonatomic) NSString *iconUrl;  //iconUrl

- (instancetype)initWithDict:(NSDictionary *)dict;
+ (instancetype)filterWithDict:(NSDictionary *)dict;

@end

NS_ASSUME_NONNULL_END
