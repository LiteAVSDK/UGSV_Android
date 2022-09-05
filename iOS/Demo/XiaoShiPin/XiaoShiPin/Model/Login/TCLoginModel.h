//
//  TCLoginModel.h
//  TCLVBIMDemo
//
//  Created by dackli on 16/8/3.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#ifndef APP_EXT
#import "TCLoginModel.h"
#endif
#import "TCLoginParam.h"
#import <Foundation/Foundation.h>

#define  logoutNotification  @"logoutNotification"

typedef void (^TCRegistSuccess)(NSString* userName, NSString* md5pwd);
typedef void (^TCRegistFail)(int errCode, NSString* errMsg);

typedef void (^TCLoginSuccess)(NSString* userName, NSString* md5pwd ,NSString *token,NSString *refreshToken,long expires);
typedef void (^TCLoginFail)(NSString* userName,int errCode, NSString* errMsg);

typedef void (^TCLogoutComplete)(void);

typedef void (^TCDeleteComplete)(int code);

@protocol TCLoginListener <NSObject>
- (void)loginOK:(NSString*)userName hashedPwd:(NSString*)pwd token:(NSString *)token refreshToken:(NSString *)refreshToken expires:(NSInteger) expires;
- (void)loginFail:(NSString*)userName code:(int)errCode message:(NSString *)errMsg;
@end

/**
 *  业务server登录
 */
@interface TCLoginModel : NSObject
@property (class, assign, getter=isAutoLogin) BOOL autoLogin;

+ (instancetype)sharedInstance;

- (void)refreshLogin;

- (void)scheduleRefreshLoginForExpireDate:(NSDate *)date;

/**
 注册帐号

 @param username 用户名
 @param password 密码
 @param succ 成功回调
 @param fail 失败回调
 */
- (void)registerWithUsername:(NSString *)username password:(NSString *)password succ:(TCRegistSuccess)succ fail:(TCRegistFail)fail;

/**
 登录帐号

 @param username 用户名
 @param password 密码
 @param succ 成功回调
 @param fail 失败回调
*/
- (void)loginWithUsername:(NSString*)username password:(NSString*)password succ:(TCLoginSuccess)succ fail:(TCLoginFail)fail;

/**
 登录帐号

 @param username 用户名
 @param hashPwd md5密码
 @param succ 成功回调
 @param fail 失败回调
*/
- (void)login:(NSString*)username hashPwd:(NSString*)hashPwd succ:(TCLoginSuccess)succ fail:(TCLoginFail)fail;

/**
 登出
 
 @param completion 登出回调
 */
- (void)logout:(TCLogoutComplete)completion;

/**
 注销账户
 
 @param identifier 用户id
 @param completion 回调
 */
-(void)deleteAccount:(NSString *)identifier completion:(TCDeleteComplete)completion;

- (void)getCosSign:(void (^)(int errCode, NSString* msg, NSDictionary* resultDict))completion;
- (void)getVodSign:(void (^)(int errCode, NSString* msg, NSDictionary* resultDict))completion;
- (void)uploadUGC:(NSDictionary*)params completion:(void (^)(int errCode, NSString* msg, NSDictionary* resultDict))completion;

- (BOOL)validateUserName:(NSString *)username failedReason:(NSString **)reason;
- (BOOL)validatePassword:(NSString *)password failedReason:(NSString **)reason;
@end
