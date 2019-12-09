// Copyright (c) 2019 Tencent. All rights reserved.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NSString * UGCKitFilterIdentifier NS_STRING_ENUM;

extern UGCKitFilterIdentifier const UGCKitFilterIdentifierNone;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierBiaozhun;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierYinghong;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierYunshang;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierChunzhen;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierBailan;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierYuanqi;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierChaotuo;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierXiangfen;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierWhite;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierLangman;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierQingxin;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierWeimei;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierFennen;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierHuaijiu;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierLandiao;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierQingliang;
extern UGCKitFilterIdentifier const UGCKitFilterIdentifierRixi;

@interface UGCKitFilter : NSObject
@property (readonly, nonatomic) UGCKitFilterIdentifier identifier;
@property (readonly, nonatomic) NSString *lookupImagePath;
@end

@interface UGCKitFilterManager : NSObject
+ (instancetype)defaultManager;
@property (readonly, nonatomic) NSArray<UGCKitFilter *> *allFilters;
- (UGCKitFilter *)filterWithIdentifier:(UGCKitFilterIdentifier)identifier;
@end

NS_ASSUME_NONNULL_END
