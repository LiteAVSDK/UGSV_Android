// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVideoEffectManager.h"

@implementation UGCKitVideoEffectManager
+ (NSArray<UGCKitEffectInfo *> *)effectInfosWithTheme:(UGCKitTheme *)theme
{
    UGCKitEffectInfo *(^CreateEffect)(NSString *name, NSString *animPrefix)=^(NSString *name, NSString *animPrefix){
        UGCKitEffectInfo * v= [UGCKitEffectInfo new];
        v.name = name;
        v.icon = [theme effectIconWithName:animPrefix];
        return v;
    };

    NSArray <UGCKitEffectInfo *> *effectList = @[ CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.DynamicLightWave"], @"donggan"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.DarkFantasy"], @"anhei"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.SoulOut"], @"linghun"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.ScreenSplit"], @"fenlie"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.Shutter"], @"shutter"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.GhostShadow"], @"ghostshadow"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.Phantom"], @"phantom"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.Ghost"], @"ghost"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.Lightning"], @"lightning"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.Mirror"], @"mirror"),
                                                  CreateEffect([theme localizedString:@"UGCKit.Edit.VideoEffect.Illusion"], @"illusion"),
                                                 ];
    return effectList;
}
@end
