//
//  TACMTA+Account.h
//  accountext
//
//  Created by tyzual on 2018/4/9.
//  Copyright © 2018 tyzual. All rights reserved.
//

#pragma once

#import "MTA.h"


/**
账号类型枚举
 */
typedef NS_ENUM(NSUInteger, TACMTAAccountTypeExt) {
	TACMTAAccountUndefined = 0,
	TACMTAAccountPhone = 1, // 电话
	TACMTAAccountEmail = 2, // 邮箱
	TACMTAAccountQQNum = 3, // QQ号

	TACMTAAccountWeixin = 1000,	// 微信
	TACMTAAccountQQOpenid = 1001,  // QQ开放平台Openid
	TACMTAAccountWeibo = 1003,		// 新浪微博
	TACMTAAccountAlipay = 1003,	// 支付宝
	TACMTAAccountTaobao = 1004,	// 淘宝
	TACMTAAccountDouban = 1005,	// 豆瓣
	TACMTAAccountFacebook = 1006,  // facebook
	TACMTAAccountTwitter = 1007,   // twitter
	TACMTAAccountGoogle = 1008,	// 谷歌
	TACMTAAccountBaidu = 1009,		// 百度
	TACMTAAccountJD = 1010,		// 京东
	TACMTAAccountDing = 1011,		// 钉钉
	TACMTAAccountXiaomi = 1012,	// 小米
	TACMTAAccountLinkin = 1013,	// linkedin
	TACMTAAccountLine = 1014,		// line
	TACMTAAccountInstagram = 1015, // instagram
	TACMTAAccountGuest = 2000,		// 游客登录
	TACMTAAccountCustom = 2001,	// 用户自定义以上的账号类别请使用2001以及2001以上的枚举值
};


/**
 账号登录类型枚举
 */
typedef NS_ENUM(NSInteger, TACMTAAccountRequestType) {
	TACMTAAccountRequestUndefined = -1,   // 未定义
	TACMTAAccountRequestLogin = 1,		   // 新登录
	TACMTAAccountRequestRefleshToken = 2, // 刷新token
	TACMTAAccountRequestExShort = 3,	  // 交换短票据
	TACMTAAccountRequestExThridParty = 4, //交换第三方票据
};


/**
 账号状态枚举
 */
typedef NS_ENUM(NSInteger, TACMTAAccountStatus) {
	TACMTAAccountStatusUndefined = -1, // 未定义
	TACMTAAccountStatusNormal = 1,		// 正常使用
	TACMTAAccountStatusLogout = 0,		// 登出
};


/**
 账号信息
 */
@interface TACMTAAccountInfo : NSObject <NSCopying>


/**
 账号类型，默认值为 TACMTAAccountUndefined，用户必须手动填写
 */
@property (nonatomic, assign) TACMTAAccountTypeExt type;

/**
 账号id，默认值为nil, 用户必须手动填写
 */
@property (nonatomic, strong) NSString *account;

/**
 账号登录类型，默认未定义
 */
@property (nonatomic, assign) TACMTAAccountRequestType requestType;

/**
 账号状态，默认未定义
 */
@property (nonatomic, assign) TACMTAAccountStatus accountStatus;

/**
 账号过期时间，默认nil
 */
@property (nonatomic, strong) NSDate *expireDate;

/**
 账号上次更新时间，默认nil
 */
@property (nonatomic, strong) NSDate *lastUpdateDate;

@end

@interface TACMTA(TACMTAAccountExt)

/**
 上报主账号

 @param infos 账号信息
 */
+ (void)reportAccountExt:(NSArray<TACMTAAccountInfo *> *)infos;

@end

