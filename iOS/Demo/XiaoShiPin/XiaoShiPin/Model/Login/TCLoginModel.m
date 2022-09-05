//
//  TCLoginModel.m
//  TCLVBIMDemo
//
//  Created by dackli on 16/8/3.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCLoginModel.h"
#import "TCUserInfoModel.h"
#import "TCLiveListModel.h"
#import "TCConstants.h"
#import "TCUtil.h"
#ifndef APP_EXT
#import "AppDelegate.h"
#endif
#import "AFNetworking.h"
#import "NSString+Common.h"
#import "TCLiveListModel.h"

#define kAutoLoginKey         @"kAutoLoginKey"
#define kEachKickErrorCode    6208   //互踢下线错误码

static NSString * const UserNameRegex = @"^[a-zA-Z][a-zA-Z0-9_]{3,23}$";
static NSString * const UserNameDesc = @"TCRegisterView.UserNameRule";
static NSString * const PasswordRegex = @"^[a-zA-Z0-9_]+$";
static NSString * const PasswordDesc = @"TCRegisterView.PasswordRule";


@interface TCLoginModel()
{
    TCLoginParam *_loginParam;
    NSTimer *_refreshTimer;
}
@property (nonatomic, copy) NSString* sign;
@property (nonatomic, copy) NSString* txTime;
@property (nonatomic, copy) NSString* accountType;
@property (nonatomic, assign) int sdkAppID;
@end

@implementation TCLoginModel

static TCLoginModel *_sharedInstance = nil;

+ (instancetype)sharedInstance {
    static dispatch_once_t predicate;
    dispatch_once(&predicate, ^{
        _sharedInstance = [[TCLoginModel alloc] init];
    });
    return _sharedInstance;
}

-(instancetype)init{
    self = [super init];
    if (self) {
        _loginParam = [TCLoginParam shareInstance];
    }
    return self;
}

+ (BOOL)isAutoLogin {
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    NSNumber *num = [defaults objectForKey:kAutoLoginKey];
    return [num boolValue];
}

+ (void)setAutoLogin:(BOOL)autoLogin {
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    [defaults setObject:@(autoLogin) forKey:kAutoLoginKey];
}

- (void)refreshLogin {
    NSDate *expireDate = [_loginParam expireDate];
    if (expireDate) {
        if ([expireDate timeIntervalSinceNow] < 3600) {
            [self _doRefresh];
        } else {
            [self scheduleRefreshLoginForExpireDate:expireDate];
        }
    }
}

- (void)onRefreshTimer:(NSTimer *)timer {
    [self _doRefresh];
}

- (void)_doRefresh {
    [self refreshLoginWithParam:_loginParam completion:^(TCLoginParam *param, int code, NSString *msg) {
        if (code == 0) {
            [param saveToLocal];
            self->_loginParam = param;
            [[TCLoginParam shareInstance] loadFromLocal];
            [self scheduleRefreshLoginForExpireDate:param.expireDate];
        }
    }];
}

- (void)scheduleRefreshLoginForExpireDate:(NSDate *)date {
    __weak __typeof(self) wself = self;
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        __strong __typeof(wself) self = wself;
        if (nil == self) {
            return;
        }
        NSTimer *refreshTimer = self->_refreshTimer;
        if (refreshTimer) {
            [refreshTimer invalidate];
            refreshTimer = nil;
        }
        if (date == nil) {
            return;
        }
        NSDate *nextRefreshDate = [date dateByAddingTimeInterval:-3600];
        if ([nextRefreshDate compare:[NSDate date]] == NSOrderedAscending) {
            nextRefreshDate = [NSDate dateWithTimeIntervalSinceNow:10];
        }

        self->_refreshTimer = [NSTimer scheduledTimerWithTimeInterval:[nextRefreshDate timeIntervalSinceNow]
                                                               target:self
                                                             selector:@selector(onRefreshTimer:)
                                                             userInfo:nil
                                                              repeats:NO];
    }];
}

- (void)refreshLoginWithParam:(TCLoginParam *)loginParam completion:(void(^)(TCLoginParam* param, int code, NSString *msg))completion {
    [[TCLoginModel sharedInstance] login:loginParam.identifier hashPwd:loginParam.hashedPwd succ:^(NSString* userName, NSString* md5pwd ,NSString *token,NSString *refreshToken,long expires) {
        TCLoginParam *param = [[TCLoginParam alloc] init];
        param.identifier = userName;
        param.hashedPwd = loginParam.hashedPwd;
        param.token = token;
        param.tokenTime = [[NSDate date] timeIntervalSince1970];
        param.refreshToken = refreshToken;
        param.expires = expires;
        if (completion) {
            completion(param, 0, nil);
        }
        [TCUtil report:xiaoshipin_login userName:userName code:200 msg:@"自动登录成功"];
    } fail:^(NSString *userName, int errCode, NSString *errMsg) {
        if (completion) {
            completion(nil, errCode, errMsg);
        }
        [TCUtil report:xiaoshipin_login userName:userName code:errCode msg:@"自动登录失败"];
    }];
}

- (void)registerWithUsername:(NSString *)username password:(NSString *)password succ:(TCRegistSuccess)succ fail:(TCRegistFail)fail
{
    NSString* pwdMD5 = [password md5];
    NSString* hashPwd = [[pwdMD5 stringByAppendingString:username] md5];

    NSDictionary* params = @{@"userid": username, @"password": hashPwd};

    [TCUtil asyncSendHttpRequest:@"register" params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        NSLog(@"%d, %@, %@", resultCode, message, resultDict.description);
        if (resultCode == 200) {
            succ(username, hashPwd);
        }
        else {
            fail(resultCode, message);
        }
    }];
}

- (void)loginWithUsername:(NSString *)username password:(NSString *)password succ:(TCLoginSuccess)succ fail:(TCLoginFail)fail
{
    NSString* pwdMD5 = [password md5];
    NSString* hashPwd = [[pwdMD5 stringByAppendingString:username] md5];

    [self login:username hashPwd:hashPwd succ:succ fail:fail];
}

- (void)login:(NSString*)username hashPwd:(NSString*)hashPwd succ:(TCLoginSuccess)succ fail:(TCLoginFail)fail
{
    NSDictionary* params = @{@"userid": username, @"password": hashPwd};
    __weak typeof(self) weakSelf = self;

    [TCUtil asyncSendHttpRequest:@"login" params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        if (resultCode == 200) {
            NSString *token = resultDict[@"token"];
            NSString *refreshToken = resultDict[@"refresh_token"];
            long expires = ((NSNumber*)resultDict[@"expires"]).longValue;
            if (resultDict[@"cos_info"]) {
                [[TCUserInfoModel sharedInstance] setBucket:resultDict[@"cos_info"][@"Bucket"] secretId:resultDict[@"cos_info"][@"SecretId"]
                                                      appid:[resultDict[@"cos_info"][@"Appid"] longLongValue] region:resultDict[@"cos_info"][@"Region"] accountType:weakSelf.accountType];
            }

            [[TCUserInfoModel sharedInstance] fetchUserInfo:username token:token expires:&expires handler:^(int code, NSString *msg){
                if (code == 200) {
                    succ(username, hashPwd ,token,refreshToken,expires);
                }else{
                    fail(username,resultCode, message);
                }
            }];
        }
        else {
            fail(username,resultCode, message);
        }
    }];
}

- (void)logout:(TCLogoutComplete)completion {
    [TCLoginModel setAutoLogin:NO];
    [[NSNotificationCenter defaultCenter] postNotificationName:logoutNotification object:nil];
    if (completion) {
        completion();
    }
    self.sign = nil;
    self.txTime = nil;
    _loginParam = nil;
}

-(void)deleteAccount:(NSString *)identifier completion:(TCDeleteComplete)completion{
    [TCLoginModel setAutoLogin:NO];
    NSDictionary* params = @{@"userid": identifier};
    [TCUtil asyncSendHttpRequest:@"cancellation" params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        self.sign = nil;
        self.txTime = nil;
        _loginParam = nil;
        if(completion){
            completion(resultCode);
        }
    }];
}

- (void)getCosSign:(void (^)(int, NSString *, NSDictionary *))completion
{
    NSDictionary* params = @{@"userid": _loginParam.identifier, @"timestamp":@([[NSDate date] timeIntervalSince1970] * 1000), @"expires":@(_loginParam.expires)};

    [TCUtil asyncSendHttpRequest:@"get_cos_sign" token:_loginParam.token params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        completion(resultCode, message, resultDict);
    }];
}

- (void)getVodSign:(void (^)(int, NSString *, NSDictionary *))completion
{
    NSString *userId = (_loginParam.identifier ? _loginParam.identifier : @"");
    NSDictionary* params = @{@"userid": userId, @"timestamp":@([[NSDate date] timeIntervalSince1970] * 1000), @"expires":@(_loginParam.expires)};
    [TCUtil asyncSendHttpRequest:@"get_vod_sign" token:_loginParam.token params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        completion(resultCode, message, resultDict);
    }];
}

- (void)uploadUGC:(NSDictionary *)params completion:(void (^)(int, NSString *, NSDictionary *))completion
{
    NSDictionary* hparams = @{@"userid": _loginParam.identifier, @"timestamp":@([[NSDate date] timeIntervalSince1970] * 1000), @"expires":@(_loginParam.expires)};

    NSMutableDictionary* mparams = [NSMutableDictionary dictionaryWithDictionary:hparams];
    [mparams addEntriesFromDictionary:params];

    [TCUtil asyncSendHttpRequest:@"upload_ugc" token:_loginParam.token params:mparams handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        completion(resultCode, message, resultDict);
    }];
}

- (BOOL)_validateString:(NSString *)string regex:(NSString *)regexString
{
    NSError *error = nil;
    NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:regexString options:0 error:&error];
    NSAssert(error == nil, @"Username Regex is invalid: %@", [error localizedDescription]);
    NSRange stringRange = NSMakeRange(0, string.length);
    NSRange range = [regex firstMatchInString:string options:0 range:stringRange].range;
    return NSEqualRanges(stringRange, range);
}


- (BOOL)validateUserName:(NSString *)username failedReason:(NSString **)reason
{
    if (username == nil || [username length] == 0) {
        if (reason) {
            *reason = NSLocalizedString(@"TCLoginView.ErrorEmptyUserName", nil);
        }
        return NO;
    }

    if (![self _validateString:username regex:UserNameRegex]) {
        if (reason) {
            *reason = NSLocalizedString(UserNameDesc, nil);
        }
        return NO;
    }

    return YES;
}

- (BOOL)validatePassword:(NSString *)pwd failedReason:(NSString **)reason
{

    if (pwd == nil || [pwd length] == 0) {
        if (reason) {
            *reason = NSLocalizedString(@"TCLoginView.ErrorEmptyPassword", nil);
        }
        return NO;
    }
    if (![self _validateString:pwd regex:PasswordRegex]) {
        if (reason) {
            *reason = PasswordDesc;
        }
        return NO;
    }
    return YES;
}


@end
