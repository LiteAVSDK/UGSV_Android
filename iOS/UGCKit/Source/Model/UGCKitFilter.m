// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitFilter.h"

UGCKitFilterIdentifier const UGCKitFilterIdentifierNone      = @"";
UGCKitFilterIdentifier const UGCKitFilterIdentifierBiaozhun  = @"biaozhun";
UGCKitFilterIdentifier const UGCKitFilterIdentifierYinghong  = @"yinghong";
UGCKitFilterIdentifier const UGCKitFilterIdentifierYunshang  = @"yunshang";
UGCKitFilterIdentifier const UGCKitFilterIdentifierChunzhen  = @"chunzhen";
UGCKitFilterIdentifier const UGCKitFilterIdentifierBailan    = @"bailan";
UGCKitFilterIdentifier const UGCKitFilterIdentifierYuanqi    = @"yuanqi";
UGCKitFilterIdentifier const UGCKitFilterIdentifierChaotuo   = @"chaotuo";
UGCKitFilterIdentifier const UGCKitFilterIdentifierXiangfen  = @"xiangfen";
UGCKitFilterIdentifier const UGCKitFilterIdentifierWhite     = @"white";
UGCKitFilterIdentifier const UGCKitFilterIdentifierLangman   = @"langman";
UGCKitFilterIdentifier const UGCKitFilterIdentifierQingxin   = @"qingxin";
UGCKitFilterIdentifier const UGCKitFilterIdentifierWeimei    = @"weimei";
UGCKitFilterIdentifier const UGCKitFilterIdentifierFennen    = @"fennen";
UGCKitFilterIdentifier const UGCKitFilterIdentifierHuaijiu   = @"huaijiu";
UGCKitFilterIdentifier const UGCKitFilterIdentifierLandiao   = @"landiao";
UGCKitFilterIdentifier const UGCKitFilterIdentifierQingliang = @"qingliang";
UGCKitFilterIdentifier const UGCKitFilterIdentifierRixi      = @"rixi";

@implementation UGCKitFilter

- (instancetype)initWithIdentifier:(UGCKitFilterIdentifier)identifier
                   lookupImagePath:(NSString *)lookupImagePath
{
    if (self = [super init]) {
        _identifier = identifier;
        _lookupImagePath = lookupImagePath;
    }
    return self;
}
@end

@implementation UGCKitFilterManager
{
    NSDictionary<UGCKitFilterIdentifier, UGCKitFilter*> *_filterDictionary;
}
+ (instancetype)defaultManager
{
    static UGCKitFilterManager *defaultManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        defaultManager = [[UGCKitFilterManager alloc] init];
    });
    return defaultManager;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        NSBundle *bundle = [NSBundle mainBundle];
        NSString *path = [bundle pathForResource:@"FilterResource" ofType:@"bundle"];
        NSFileManager *manager = [[NSFileManager alloc] init];
        if ([manager fileExistsAtPath:path]) {
            NSArray<UGCKitFilterIdentifier> *availableFilters = @[
                UGCKitFilterIdentifierBiaozhun,
                UGCKitFilterIdentifierYinghong,
                UGCKitFilterIdentifierYunshang,
                UGCKitFilterIdentifierChunzhen,
                UGCKitFilterIdentifierBailan,
                UGCKitFilterIdentifierYuanqi,
                UGCKitFilterIdentifierChaotuo,
                UGCKitFilterIdentifierXiangfen,
                UGCKitFilterIdentifierWhite,
                UGCKitFilterIdentifierLangman,
                UGCKitFilterIdentifierQingxin,
                UGCKitFilterIdentifierWeimei,
                UGCKitFilterIdentifierFennen,
                UGCKitFilterIdentifierHuaijiu,
                UGCKitFilterIdentifierLandiao,
                UGCKitFilterIdentifierQingliang,
                UGCKitFilterIdentifierRixi];
            NSMutableArray<UGCKitFilter *> *filters = [[NSMutableArray alloc] initWithCapacity:availableFilters.count];
            NSMutableDictionary<UGCKitFilterIdentifier, UGCKitFilter*> *filterMap = [[NSMutableDictionary alloc] initWithCapacity:availableFilters.count];
            for (UGCKitFilterIdentifier identifier in availableFilters) {
                NSString * itemPath = [path stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.png", identifier]];
                if ([manager fileExistsAtPath:path]) {
                    UGCKitFilter *filter = [[UGCKitFilter alloc] initWithIdentifier:identifier lookupImagePath:itemPath];
                    [filters addObject:filter];
                    filterMap[identifier] = filter;
                }
            }
            _allFilters = filters;

        }
    }
    return self;
}

- (UGCKitFilter *)filterWithIdentifier:(UGCKitFilterIdentifier)identifier;
{
    return _filterDictionary[identifier];
}
@end
