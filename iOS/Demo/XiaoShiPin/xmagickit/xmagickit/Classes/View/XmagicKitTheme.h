//
//  XmagicKitTheme.h
//  xmagickit
//
//  Created by tao yue on 2022/8/11.
//  Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface XmagicKitTheme : NSObject

+ (instancetype)sharedTheme;

- (NSString *)localizedString:(NSString *)key __attribute__((annotate("returns_localized_nsstring")));

@property (assign, nonatomic) NSString *resourcePath;

@end

NS_ASSUME_NONNULL_END
