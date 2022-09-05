//
//  XmagicKitTheme.m
//  xmagickit
//
//  Created by tao yue on 2022/8/11.
//  Copyright (c) 2019 Tencent. All rights reserved.

#import "XmagicKitTheme.h"

@interface XmagicKitTheme(){
    NSBundle *_resourceBundle;
}

@end

@implementation XmagicKitTheme

+ (instancetype)sharedTheme {
    static XmagicKitTheme *theme = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        theme = [[XmagicKitTheme alloc] init];
    });
    return theme;
}

- (instancetype)init {
    if (self = [super init]) {
        self.resourcePath = [[NSBundle mainBundle] pathForResource:@"xmagickitResources"
                                                                 ofType:@"bundle"];
        _resourceBundle = [NSBundle bundleWithPath:self.resourcePath];
    }
    return self;
}



- (NSString *)localizedString:(NSString *)key {
    return [_resourceBundle localizedStringForKey:key value:@"" table:nil];
}

@end
