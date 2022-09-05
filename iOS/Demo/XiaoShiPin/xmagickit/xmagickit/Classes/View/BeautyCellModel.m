//
//  BeautyCellModel.m
//  PituMotionDemo
//
//  Created by xingyunmao on 2021/2/19.
//  Copyright © 2021 Pitu. All rights reserved.
//

#import "BeautyCellModel.h"

@implementation BeautyCellModel

- (instancetype)initWithDict:(NSDictionary *)dict{
    if (self = [super init]) {
       [self setValuesForKeysWithDictionary:dict];
    }
    return self;
}


+ (instancetype)beautyWithDict:(NSDictionary *)dict{
    return [[self alloc] initWithDict:dict];
}



@end
