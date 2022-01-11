//
//  TCLoginParam.h
//  TCLVBIMDemo
//
//  Created by dackli on 16/8/4.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *  用来管理用户的登录信息，如登录信息的缓存、过期判断等
 */
@interface TCLoginParam : NSObject

@property (nonatomic, copy) NSString* token;
@property (nonatomic, copy) NSString* refreshToken;
@property (nonatomic, assign) NSInteger tokenTime;
@property (nonatomic, assign) NSInteger expires;
@property (nonatomic, assign) BOOL      isLastAppExt; // 暂未使用
@property (nonatomic, copy) NSString*   identifier;
@property (nonatomic, copy) NSString*   hashedPwd;

+ (instancetype)shareInstance;

- (void)loadFromLocal;

- (void)saveToLocal;

- (void)clearLocal;

- (BOOL)isExpired;

- (BOOL)isValid;

- (NSDate *)expireDate;

@end
