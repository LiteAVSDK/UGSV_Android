//
//  TCUserInfoModel+TCUserInfoModel.m
//  TCLVBIMDemo
//
//  Created by jemilyzhou on 16/8/2.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import "TCUserInfoModel.h"
#import "TCLoginModel.h"
#ifndef APP_EXT
#import <SDWebImage/UIImageView+WebCache.h>
#endif
#import "TCConstants.h"
#import "TCUtil.h"
#define kUserInfoKey     @"kUserInfoKey"

static TCUserInfoModel *_shareInstance = nil;

@implementation TCUserInfoData

- (instancetype)init
{
    if (self = [super init])
    {
        
    }
    return self;
}
@end

@interface TCUserInfoModel()
@property(nonatomic,strong) TCUserInfoData *userInfo;
@property(nonatomic,strong) TCLoginParam *loginParam;
@end

@implementation TCUserInfoModel

- (instancetype)init
{
    self = [super init];
    if (self)
    {
        _loginParam = [TCLoginParam shareInstance];
        _userInfo   = [self loadUserProfile];
    }
    
    return self;
}

- (void)dealloc
{
}

+ (instancetype)sharedInstance;
{
    static dispatch_once_t predicate;
    
    dispatch_once(&predicate, ^{
        _shareInstance = [[TCUserInfoModel alloc] init];
    });
    return _shareInstance;
}

- (void)setBucket:(NSString *)bucket secretId:(NSString*)secretId appid:(long long)appid region:(NSString *)region accountType:(NSString *)accountType
{
    _userInfo.bucket = bucket;
    _userInfo.secretId = secretId;
    _userInfo.appid = [NSString stringWithFormat:@"%lld",appid];
    _userInfo.region = region;
    _userInfo.accountType = accountType;
}

#pragma mark 从服务器上拉取信息

#ifndef APP_EXT
/**
 *  通过id信息从服务器上拉取用户信息
 */
-(void)fetchUserInfo:(NSString *)identifier token:(NSString *)token expires:(NSInteger *)expires handler:(TCFetchUserInfoHandle)handle
{
    DebugLog(@"开始通过用户id拉取用户资料信息");
    __weak typeof(self) weakSelf = self;
    _loginParam.expires = *(expires);
    NSDictionary* params = @{@"userid": identifier, @"timestamp":@([[NSDate date] timeIntervalSince1970] * 1000), @"expires":@(_loginParam.expires)};
    [TCUtil asyncSendHttpRequest:@"get_user_info" token:token params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {
        if (resultCode == 200) {
            DebugLog(@"从服务器上拉取用户资料信息成功%@", resultDict);
            weakSelf.userInfo.identifier = identifier;
            weakSelf.userInfo.nickName = resultDict[@"nickname"];
            weakSelf.userInfo.gender = [((NSNumber*)resultDict[@"sex"]) intValue];
            weakSelf.userInfo.faceURL = resultDict[@"avatar"];
            weakSelf.userInfo.coverURL = resultDict[@"frontcover"];
            
            [weakSelf saveToLocal];
        }else {
            DebugLog(@"从服务器上拉取用户资料信息失败 errCode = %d, errMsg = %@", resultCode, message);
        }
        handle(resultCode,message);
    }];
}

- (void)uploadUserInfo:(TCUserInfoSaveHandle)handle
{
    NSString* nickname = (NSNull *)_userInfo.nickName == [NSNull null] ? _userInfo.identifier : _userInfo.nickName;
    NSString* avatar = _userInfo.faceURL == nil ? @"" : _userInfo.faceURL;
    NSString* frontcover = _userInfo.coverURL == nil ? @"" : _userInfo.coverURL;
    
    NSDictionary* params = @{@"userid": _loginParam.identifier, @"timestamp":@([[NSDate date] timeIntervalSince1970] * 1000), @"expires":@(_loginParam.expires), @"nickname": nickname, @"avatar": avatar, @"frontcover": frontcover, @"sex": @(_userInfo.gender)};
    [TCUtil asyncSendHttpRequest:@"upload_user_info" token:_loginParam.token params:params handler:^(int resultCode, NSString *message, NSDictionary *resultDict) {

        [self saveToLocal];
        handle(resultCode, message);
    }];
}

#pragma mark 上传更改后的用户信息
/**
 *  saveUserNickName 用户保存用户修改后的昵称到服务器上
 *
 *  @param nickName 用户的昵称
 *  @param handle   保存成功或者失败后的回调block
 */
-(void)saveUserNickName:(NSString*)nickName handler:(TCUserInfoSaveHandle)handle
{
    DebugLog(@"开始保存用户昵称信息到服务器 \n");
    __weak typeof(self) weakSelf = self;
    
    NSString* oldNickname = _userInfo.nickName;
    _userInfo.nickName = nickName;
    [self uploadUserInfo:^(int errCode, NSString *strMsg) {
        if (errCode != ERROR_SUCESS) {
            weakSelf.userInfo.nickName = oldNickname;
        }
        handle(errCode, strMsg);
    }];
}
/**
 *  saveUserFace 当吧用户修改后的头像图片上传到服务器后会返回头像url地址信息
                此时再把头像的url地址上传到服务器上
 *
 *  @param faceURL 用户头像的url地址
 *  @param handle  保存成功或者失败后的回调block
 */
-(void)saveUserFace:(NSString*)faceURL handler:(TCUserInfoSaveHandle)handle
{
    DebugLog(@"开始保存用户头像Url地址到服务器 \n");
    __weak typeof(self) weakSelf = self;
    
    NSString* oldFaceURL = _userInfo.faceURL;
    _userInfo.faceURL = faceURL;
    [self uploadUserInfo:^(int errCode, NSString *strMsg) {
        if (errCode != ERROR_SUCESS) {
            weakSelf.userInfo.faceURL = oldFaceURL;
        }
        handle(errCode, strMsg);
    }];
}
/**
 *  saveUserGender 用于保存用户性别到服务器
 *
 *  @param gender 用户性别信息,根据男 or 女取不同结构体,可查询TIMGender结构体定义
 *  @param handle 保存成功或者失败后的回调block
 */
-(void)saveUserGender:(int)gender handler:(TCUserInfoSaveHandle)handle
{
    DebugLog(@"开始保存用户性别信息到服务器 \n");
    
    __weak typeof(self) weakSelf = self;
    
    int oldGender = _userInfo.gender;
    _userInfo.gender = gender;
    [self uploadUserInfo:^(int errCode, NSString *strMsg) {
        if (errCode != ERROR_SUCESS) {
            weakSelf.userInfo.gender = oldGender;
        }
        handle(errCode, strMsg);
    }];
}
#endif

#pragma mark 内存中查询或者修改数据

/**
 *  用于获取用户资料信息借口
 *
 *  @return 用户资料信息结构体指针
 */
- (TCUserInfoData*)getUserProfile
{
    return _userInfo;
}

- (TCUserInfoData*)loadUserProfile {
    TCUserInfoData *info = [[TCUserInfoData alloc] init];
    
    // 从文件中读取
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    
    NSString *useridKey = [NSString stringWithFormat:@"%@_UserInfo", _loginParam.identifier];
    if (useridKey) {
        NSString *strUserInfo = [defaults objectForKey:useridKey];
        NSDictionary *dic = [TCUtil jsonData2Dictionary: strUserInfo];
        if (dic) {
            info.identifier = [_loginParam.identifier copy];
            info.nickName = [dic objectForKey:@"nickName"];
            info.faceURL = [dic objectForKey:@"faceURL"];
            info.coverURL = [dic objectForKey:@"coverURL"];
            info.gender = [[dic objectForKey:@"gender"] intValue];
        }
    }
    return info;
}

- (void)saveToLocal {
    // 保存昵称，头像，封页, 性别 到本地，方便其他进程读取
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    if (_userInfo.nickName != nil) {
        [dic setObject:_userInfo.nickName forKey:@"nickName"];
    }
    if(_userInfo.faceURL != nil){
        [dic setObject:_userInfo.faceURL forKey:@"faceURL"];
    }
    if (_userInfo.coverURL != nil) {
        [dic setObject:_userInfo.coverURL forKey:@"coverURL"];
    }
    [dic setObject:@(_userInfo.gender) forKey:@"gender"];
    
    NSData *data = [TCUtil dictionary2JsonData: dic];
    NSString *strUserInfo = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSString *useridKey = [NSString stringWithFormat:@"%@_UserInfo", _loginParam.identifier];
    
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:APP_GROUP];
    if (defaults == nil) {
        defaults = [NSUserDefaults standardUserDefaults];
    }
    [defaults setObject:useridKey forKey:kUserInfoKey];
    
    [defaults setObject:strUserInfo forKey:useridKey];
    [defaults synchronize];
}

@end
