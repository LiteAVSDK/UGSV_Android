//
//  TCLoginParam.m
//  TCLVBIMDemo
//
//  Created by dackli on 16/8/4.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCLoginParam.h"
#import "TCUtil.h"
#import "TCConstants.h"

@implementation TCLoginParam

#define kLoginParamKey     @"kLoginParamKey"

+ (instancetype)shareInstance
{
    static TCLoginParam *mgr;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (mgr == nil) {
            mgr = [[TCLoginParam alloc] init];
        }
    });
    return mgr;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self loadFromLocal];
    }
    return self;
}

- (void)loadFromLocal {
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    NSString *useridKey = [defaults objectForKey:kLoginParamKey];
    if (useridKey) {
        NSString *strLoginParam = [defaults objectForKey:useridKey];
        NSDictionary *dic = [TCUtil jsonData2Dictionary: strLoginParam];
        if (dic) {
            self.token = [dic objectForKey:@"token"];
            self.tokenTime = [[dic objectForKey:@"tokenTime"] longValue];
            self.expires = [[dic objectForKey:@"expires"] longValue];
            self.identifier = [dic objectForKey:@"identifier"];
            self.hashedPwd = [dic objectForKey:@"hashedPwd"];
            self.isLastAppExt = [[dic objectForKey:@"isLastAppExt"] intValue];
        } else {
            self.token = nil;
            self.tokenTime = 0;
            self.expires = 0;
            self.identifier = nil;
            self.hashedPwd = nil;
            self.isLastAppExt = 0;
        }
    }
}

- (void)saveToLocal {
    if (![self isValid]) {
        return;
    }
    self.tokenTime = [[NSDate date] timeIntervalSince1970];
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    [dic setObject:self.token forKey:@"token"];
    [dic setObject:@(self.tokenTime) forKey:@"tokenTime"];
    [dic setObject:@(self.expires) forKey:@"expires"];
    [dic setObject:self.identifier forKey:@"identifier"];
    [dic setObject:self.hashedPwd forKey:@"hashedPwd"];
#if APP_EXT
    [dic setObject:@(1) forKey:@"isLastAppExt"];
#else
    [dic setObject:@(0) forKey:@"isLastAppExt"];
#endif

    NSData *data = [TCUtil dictionary2JsonData: dic];
    NSString *strLoginParam = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSString *useridKey = [NSString stringWithFormat:@"%@_LoginParam", self.identifier];

    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }

    [defaults setObject:useridKey forKey:kLoginParamKey];

    // save login param
    [defaults setObject:strLoginParam forKey:useridKey];
    [defaults synchronize];
}

- (void)clearLocal{
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    NSString *useridKey = [defaults objectForKey:kLoginParamKey];
    if(useridKey != nil){
        [defaults removeObjectForKey:useridKey];
        [defaults synchronize];
    }
    [self loadFromLocal];
}

+ (NSString *)storedUserID {
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    NSString *useridKey = [defaults objectForKey:kLoginParamKey];
    if(useridKey == nil){
        return nil;
    }
    return [defaults objectForKey:useridKey];
}

- (BOOL)isExpired {
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    NSString *useridKey = [defaults objectForKey:kLoginParamKey];
    if(useridKey != nil){
        NSObject *obj = [defaults objectForKey:useridKey];
        if (obj == nil) {
            return YES;
        }
    }

    time_t curTime = [[NSDate date] timeIntervalSince1970];
    if (curTime - self.tokenTime > self.expires) {
        return YES;
    }
    return NO;
}

- (NSDate *)expireDate {
    if ([TCLoginParam storedUserID] == nil) {
        return nil;
    }
    return [NSDate dateWithTimeIntervalSince1970: self.tokenTime + self.expires];
}

- (BOOL)isValid {
    if (self.identifier == nil || self.identifier.length == 0) {
        return NO;
    }
    if (self.hashedPwd == nil || self.hashedPwd.length == 0) {
        return NO;
    }

    return YES;
}

@end
