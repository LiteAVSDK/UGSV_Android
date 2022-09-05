//
//  BeautyViewModel.m
//  PituMotionDemo
//
//  Created by xingyunmao on 2021/2/22.
//  Copyright © 2021 Pitu. All rights reserved.
//

#import "BeautyViewModel.h"
#import "XmagicResDownload.h"
#import "XMagic.h"

@implementation BeautyViewModel

- (void)setupData{
    _theme = [XmagicKitTheme sharedTheme];
    _basicFaceEnable = true;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    [self buildBeautyThinFaceIDs];
    [self buildMotion2DMenuIDS:fileManager];
    [self buildMotion3DMenuIDS:fileManager];
    [self buildMotionGanMenuIDS:fileManager];
    [self buildMotionHandMenuIDS:fileManager];
    [self buildBeautylipIDs];
    [self buildBeautyCheekIDs];
    [self buildBeautyDimensionIDs];
    [self buildBeautyIDs];
    [self buildLutIDs];
    [self buildMotionIDs];
    [self buildMakeupIDS:fileManager];
    [self buildBeautySegIDS:fileManager];
}

- (void)buildBeautyThinFaceIDs{
    if (_beautyThinFaceIDs == nil){
        NSArray* arrayDict = @[
            @{@"title":[_theme localizedString:@"beauty_thin_face1_label"],@"key":@"beauty.thin.face1",@"beautyValue":@(30),@"originValue":@(30), @"extraConfig":@{@"reshape.basicFaceSubType":@"nature"}},
            @{@"title":[_theme localizedString:@"beauty_thin_face2_label"],@"key":@"beauty.thin.face2",@"beautyValue":@(0),@"originValue":@(0), @"extraConfig":@{@"reshape.basicFaceSubType":@"femaleGod"}},
            @{@"title":[_theme localizedString:@"beauty_thin_face3_label"],@"key":@"beauty.thin.face3",@"beautyValue":@(0),@"originValue":@(0), @"extraConfig":@{@"reshape.basicFaceSubType":@"maleGod"}},
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in arrayDict) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path
            if([XMagic isBeautyAuthorized:model.key]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            }

        }
        _beautyThinFaceIDs = arrayModels;
    }
}

- (void)buildMotion2DMenuIDS:(NSFileManager *)fileManager{
    if (_motion2DMenuIDS == nil) {
        NSArray *motionArray =
        @[
            @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught"},
                @{@"title":[_theme localizedString:@"video_lianliancaomei"],@"key":@"video_lianliancaomei"},
                @{@"title":[_theme localizedString:@"video_zuijiuweixun"],@"key":@"video_zuijiuweixun"},
                @{@"title":[_theme localizedString:@"video_tianxinmengniiu"],@"key":@"video_tianxinmengniiu"},
                @{@"title":[_theme localizedString:@"video_litihuaduo"],@"key":@"video_litihuaduo"},
                @{@"title":[_theme localizedString:@"video_aiyimanman"],@"key":@"video_aiyimanman"},
            @{@"title":[_theme localizedString:@"video_keaituya"],@"key":@"video_keaituya",@"iconUrl":@"2d_video_keaituya.png"},
                @{@"title":[_theme localizedString:@"video_naipingmianmo"],@"key":@"video_naipingmianmo"},
                @{@"title":[_theme localizedString:@"video_qiqiupaidui"],@"key":@"video_qiqiupaidui"},
                @{@"title":[_theme localizedString:@"video_mengmengxiong"],@"key":@"video_mengmengxiong"},
                @{@"title":[_theme localizedString:@"video_aixinyanhua"],@"key":@"video_aixinyanhua"},
                @{@"title":[_theme localizedString:@"video_xingganxiaochou"],@"key":@"video_xingganxiaochou"},
                @{@"title":[_theme localizedString:@"video_xiaohonghua"],@"key":@"video_xiaohonghua"},
                @{@"title":[_theme localizedString:@"video_qipaoshui"],@"key":@"video_qipaoshui"},
                @{@"title":[_theme localizedString:@"video_kangnaixin"],@"key":@"video_kangnaixin"},
                @{@"title":[_theme localizedString:@"video_xiangsuyuzhou"],@"key":@"video_xiangsuyuzhou"},
                @{@"title":[_theme localizedString:@"video_shangtoule"],@"key":@"video_shangtoule"},
                @{@"title":[_theme localizedString:@"video_xiaohongxing"],@"key":@"video_xiaohongxing"},
                @{@"title":[_theme localizedString:@"video_xiaohuangmao"],@"key":@"video_xiaohuangmao"},
                @{@"title":[_theme localizedString:@"video_chudao"],@"key":@"video_chudao", @"strength":@(100)},
                @{@"title":[_theme localizedString:@"video_fenweiqiehuan"],@"key":@"video_fenweiqiehuan"},
                @{@"title":[_theme localizedString:@"video_bingjingaixin"],@"key":@"video_bingjingaixin"},
                @{@"title":[_theme localizedString:@"video_biaobai"],@"key":@"video_biaobai"},
                @{@"title":[_theme localizedString:@"video_kawayixiaoxiong"],@"key":@"video_kawayixiaoxiong"},
                @{@"title":[_theme localizedString:@"video_yaogunyue"],@"key":@"video_yaogunyue"},
                @{@"title":[_theme localizedString:@"video_rixishaonv"],@"key":@"video_rixishaonv"},
            @{@"title":[_theme localizedString:@"video_tutujiang"],@"key":@"video_tutujiang",@"iconUrl":@"2d_video_tutujiang.png"},
                @{@"title":[_theme localizedString:@"video_xuancainihong"],@"key":@"video_xuancainihong"},
                @{@"title":[_theme localizedString:@"video_kaixueqianhou"],@"key":@"video_kaixueqianhou"},
                @{@"title":[_theme localizedString:@"video_nightgown"],@"key":@"video_nightgown"},
                @{@"title":[_theme localizedString:@"video_xuanmeizhuang"],@"key":@"video_xuanmeizhuang"},
                @{@"title":[_theme localizedString:@"video_quebanzhuang"],@"key":@"video_quebanzhuang"},
                @{@"title":[_theme localizedString:@"video_lengliebingmo"],@"key":@"video_lengliebingmo"},
                @{@"title":[_theme localizedString:@"video_dongriliange"],@"key":@"video_dongriliange"},
                @{@"title":[_theme localizedString:@"video_fugu_dv"],@"key":@"video_fugu_dv"},
                @{@"title":[_theme localizedString:@"video_baozilian"],@"key":@"video_baozilian"},
                @{@"title":[_theme localizedString:@"video_boom"],@"key":@"video_boom"},
                @{@"title":[_theme localizedString:@"video_boys"],@"key":@"video_boys"},
                @{@"title":[_theme localizedString:@"video_cherries"],@"key":@"video_cherries"},
                @{@"title":[_theme localizedString:@"video_guifeiface"],@"key":@"video_guifeiface"},
                @{@"title":[_theme localizedString:@"video_heimaomi"],@"key":@"video_heimaomi"},
                @{@"title":[_theme localizedString:@"video_liuhaifadai"],@"key":@"video_liuhaifadai"},
                @{@"title":[_theme localizedString:@"video_otwogirl"],@"key":@"video_otwogirl"},
                @{@"title":[_theme localizedString:@"video_shuangmahua"],@"key":@"video_shuangmahua"},
                @{@"title":[_theme localizedString:@"video_egaoshuangwanzi"],@"key":@"video_egaoshuangwanzi"},
        ];

        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in motionArray) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path of motionres
            if ([model.title isEqualToString:[_theme localizedString:@"item_none_label"]]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            } else {
                if (model.iconUrl != nil && ![model.iconUrl isEqualToString:@""]) {
                    model.iconUrl = [[XmagicResDownload shardManager] getIconUrl:model.iconUrl];
                [arrayModels addObject:model];
                }else{
                    model.icon = [NSString stringWithFormat:@"%@/%@/template.png", [[NSBundle mainBundle] pathForResource:@"2dMotionRes" ofType:@"bundle"], model.key];
                    if ([fileManager fileExistsAtPath:model.icon]) {
                        [arrayModels addObject:model];
                    }
                }
            }
        }
        // Check doc motions dir
        NSString *motionTempDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        motionTempDir = [motionTempDir stringByAppendingPathComponent:@"motions"];
        NSFileManager *localFileManager=[[NSFileManager alloc] init];
        BOOL isDir = NO;
        if ([localFileManager fileExistsAtPath:motionTempDir isDirectory:&isDir] && isDir) {
            NSArray *subDirs = [localFileManager contentsOfDirectoryAtPath:motionTempDir error:nil];
            BOOL isSubDir = NO;
            for (int i = 0; i < [subDirs count]; ++i) {
                NSString *subPath = [motionTempDir stringByAppendingPathComponent:subDirs[i]];
                if ([localFileManager fileExistsAtPath:subPath isDirectory:&isSubDir] && isSubDir) {
                    BeautyCellModel *model = [BeautyCellModel beautyWithDict:
                    @{@"title":subDirs[i],@"key":subDirs[i],@"path":motionTempDir,
                      @"icon":[NSString stringWithFormat:@"%@/template.png", subPath]}];
                    [arrayModels addObject:model];
                }
            }

        }
        
        _motion2DMenuIDS = arrayModels;
    }
}

- (void)buildMotion3DMenuIDS:(NSFileManager *)fileManager{
    if (_motion3DMenuIDS == nil) {
        NSArray *motionArray =
        @[
                @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught"},
                @{@"title":[_theme localizedString:@"video_yazi"],@"key":@"video_yazi"},
                @{@"title":[_theme localizedString:@"video_tantanfagu"],@"key":@"video_tantanfagu"},
                @{@"title":[_theme localizedString:@"video_ningmengyayamao"],@"key":@"video_ningmengyayamao"},
                @{@"title":[_theme localizedString:@"video_hudiejie"],@"key":@"video_hudiejie"},
                @{@"title":[_theme localizedString:@"video_maoxinvhai"],@"key":@"video_maoxinvhai"},
                @{@"title":[_theme localizedString:@"video_jinli"],@"key":@"video_jinli"},
                @{@"title":[_theme localizedString:@"video_zhixingmeigui"],@"key":@"video_zhixingmeigui",@"iconUrl":@"3d_video_zhixingmeigui.png"},
                @{@"title":[_theme localizedString:@"video_tiankulamei"],@"key":@"video_tiankulamei"},
                @{@"title":[_theme localizedString:@"video_feitianzhuzhu"],@"key":@"video_feitianzhuzhu"},
                @{@"title":[_theme localizedString:@"video_tonghuagushi"],@"key":@"video_tonghuagushi"},
                @{@"title":[_theme localizedString:@"video_3DFace_springflower"],
                  @"key":@"video_3DFace_springflower"},
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in motionArray) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path of motionres
            if ([model.title isEqualToString:[_theme localizedString:@"item_none_label"]]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            } else {
                if (model.iconUrl != nil && ![model.iconUrl isEqualToString:@""]) {
                    model.iconUrl = [[XmagicResDownload shardManager] getIconUrl:model.iconUrl];
                    [arrayModels addObject:model];
                }else{
                    model.icon = [NSString stringWithFormat:@"%@/%@/template.png", [[NSBundle mainBundle] pathForResource:@"3dMotionRes" ofType:@"bundle"], model.key];
                    if ([fileManager fileExistsAtPath:model.icon]) {
                        [arrayModels addObject:model];
                    }
                }
            }
        
        }
        // Check doc motions dir
        NSString *motionTempDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        motionTempDir = [motionTempDir stringByAppendingPathComponent:@"motions"];
        NSFileManager *localFileManager=[[NSFileManager alloc] init];
        BOOL isDir = NO;
        if ([localFileManager fileExistsAtPath:motionTempDir isDirectory:&isDir] && isDir) {
            NSArray *subDirs = [localFileManager contentsOfDirectoryAtPath:motionTempDir error:nil];
            BOOL isSubDir = NO;
            for (int i = 0; i < [subDirs count]; ++i) {
                NSString *subPath = [motionTempDir stringByAppendingPathComponent:subDirs[i]];
                if ([localFileManager fileExistsAtPath:subPath isDirectory:&isSubDir] && isSubDir) {
                    BeautyCellModel *model = [BeautyCellModel beautyWithDict:
                    @{@"title":subDirs[i],@"key":subDirs[i],@"path":motionTempDir,
                    @"icon":[NSString stringWithFormat:@"%@/template.png", subPath]}];
                    [arrayModels addObject:model];
                }
            }
            
        }
        
        _motion3DMenuIDS = arrayModels;
    }
}

- (void)buildMotionGanMenuIDS:(NSFileManager *)fileManager{
    if (_motionGanMenuIDS == nil) {
        NSArray *motionArray =
        @[
            @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught"},
            @{@"title":[_theme localizedString:@"video_bubblegum"],@"key":@"video_bubblegum",@"iconUrl":@"gan_video_bubbkegum.png"},
            
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in motionArray) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path of motionres
            if ([model.title isEqualToString:[_theme localizedString:@"item_none_label"]]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            } else {
                if (model.iconUrl != nil && ![model.iconUrl isEqualToString:@""]) {
                    model.iconUrl = [[XmagicResDownload shardManager] getIconUrl:model.iconUrl];
                    [arrayModels addObject:model];
                }else{
                    model.icon = [NSString stringWithFormat:@"%@/%@/template.png", [[NSBundle mainBundle] pathForResource:@"ganMotionRes" ofType:@"bundle"], model.key];
                    if ([fileManager fileExistsAtPath:model.icon]) {
                        [arrayModels addObject:model];
                    }
                }
                
            }
       
        }
        // Check doc motions dir
        NSString *motionTempDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        motionTempDir = [motionTempDir stringByAppendingPathComponent:@"motions"];
        NSFileManager *localFileManager=[[NSFileManager alloc] init];
        BOOL isDir = NO;
        if ([localFileManager fileExistsAtPath:motionTempDir isDirectory:&isDir] && isDir) {
            NSArray *subDirs = [localFileManager contentsOfDirectoryAtPath:motionTempDir error:nil];
            BOOL isSubDir = NO;
            for (int i = 0; i < [subDirs count]; ++i) {
                NSString *subPath = [motionTempDir stringByAppendingPathComponent:subDirs[i]];
                if ([localFileManager fileExistsAtPath:subPath isDirectory:&isSubDir] && isSubDir) {
                    BeautyCellModel *model = [BeautyCellModel beautyWithDict:
                    @{@"title":subDirs[i],@"key":subDirs[i],@"path":motionTempDir,
                    @"icon":[NSString stringWithFormat:@"%@/template.png", subPath]}];
                    [arrayModels addObject:model];
                }
            }
            
        }
        
        _motionGanMenuIDS = arrayModels;
    }
}

- (void)buildMotionHandMenuIDS:(NSFileManager *)fileManager{
    if (_motionHandMenuIDS == nil) {
        NSArray *motionArray =
        @[
            @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught"},
            @{@"title":[_theme localizedString:@"video_shoushiwu"],@"key":@"video_shoushiwu"},
            @{@"title":[_theme localizedString:@"video_sakuragirl"],@"key":@"video_sakuragirl",@"iconUrl":@"hand_video_sakuragirl.png"},
            
        ];
        
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in motionArray) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path of motionres
            if ([model.title isEqualToString:[_theme localizedString:@"item_none_label"]]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            } else {
                if (model.iconUrl != nil && ![model.iconUrl isEqualToString:@""]) {
                    model.iconUrl = [[XmagicResDownload shardManager] getIconUrl:model.iconUrl];
                    [arrayModels addObject:model];
                }else{
                    model.icon = [NSString stringWithFormat:@"%@/%@/template.png", [[NSBundle mainBundle] pathForResource:@"handMotionRes" ofType:@"bundle"], model.key];
                    if ([fileManager fileExistsAtPath:model.icon]) {
                        [arrayModels addObject:model];
                    }
                }
            }
            
        }
        // Check doc motions dir
        NSString *motionTempDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        motionTempDir = [motionTempDir stringByAppendingPathComponent:@"motions"];
        NSFileManager *localFileManager=[[NSFileManager alloc] init];
        BOOL isDir = NO;
        if ([localFileManager fileExistsAtPath:motionTempDir isDirectory:&isDir] && isDir) {
            NSArray *subDirs = [localFileManager contentsOfDirectoryAtPath:motionTempDir error:nil];
            BOOL isSubDir = NO;
            for (int i = 0; i < [subDirs count]; ++i) {
                NSString *subPath = [motionTempDir stringByAppendingPathComponent:subDirs[i]];
                if ([localFileManager fileExistsAtPath:subPath isDirectory:&isSubDir] && isSubDir) {
                    BeautyCellModel *model = [BeautyCellModel beautyWithDict:
                    @{@"title":subDirs[i],@"key":subDirs[i],@"path":motionTempDir,
                    @"icon":[NSString stringWithFormat:@"%@/template.png", subPath]}];
                    [arrayModels addObject:model];
                }
            }
            
        }
        _motionHandMenuIDS = arrayModels;
    }
}

- (void)buildBeautylipIDs{
    if (_beautylipIDs == nil){
        NSArray* arrayDict = @[
            @{@"title":[_theme localizedString:@"beauty_lips1_label"],@"key":@"beauty.lips",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.lips.lipsMask":@"images/beauty/lips_fuguhong.png", @"beauty.lips.lipsType":@"2"}},
            @{@"title":[_theme localizedString:@"beauty_lips2_label"],@"key":@"beauty.lips",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.lips.lipsMask":@"images/beauty/lips_mitaose.png", @"beauty.lips.lipsType":@"2"}},
            @{@"title":[_theme localizedString:@"beauty_lips3_label"],@"key":@"beauty.lips",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.lips.lipsMask":@"images/beauty/lips_shanhuju.png", @"beauty.lips.lipsType":@"2"}},
            @{@"title":[_theme localizedString:@"beauty_lips4_label"],@"key":@"beauty.lips",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.lips.lipsMask":@"images/beauty/lips_wenroufen.png", @"beauty.lips.lipsType":@"2"}},
            @{@"title":[_theme localizedString:@"beauty_lips5_label"],@"key":@"beauty.lips",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.lips.lipsMask":@"images/beauty/lips_huolicheng.png", @"beauty.lips.lipsType":@"2"}},
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in arrayDict) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path
            if([XMagic isBeautyAuthorized:model.key]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            }
        }
        _beautylipIDs = arrayModels;
    }
}

- (void)buildBeautyCheekIDs{
    if (_beautyCheekIDs == nil) {
        NSArray* arrayDict = @[
            @{@"title":[_theme localizedString:@"beauty_redcheeks1_label"],@"key":@"beauty.redcheeks",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.makeupMultiply.multiplyMask":@"images/beauty/saihong_jianyue.png"}},
            @{@"title":[_theme localizedString:@"beauty_redcheeks2_label"],@"key":@"beauty.redcheeks",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.makeupMultiply.multiplyMask":@"images/beauty/saihong_shengxia.png"}},
            @{@"title":[_theme localizedString:@"beauty_redcheeks3_label"],@"key":@"beauty.redcheeks",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.makeupMultiply.multiplyMask":@"images/beauty/saihong_haixiu.png"}},
            @{@"title":[_theme localizedString:@"beauty_redcheeks4_label"],@"key":@"beauty.redcheeks",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.makeupMultiply.multiplyMask":@"images/beauty/saihong_chengshu.png"}},
            @{@"title":[_theme localizedString:@"beauty_redcheeks5_label"],@"key":@"beauty.redcheeks",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.makeupMultiply.multiplyMask":@"images/beauty/saihong_queban.png"}},
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in arrayDict) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path
            if([XMagic isBeautyAuthorized:model.key]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            }
        }
        _beautyCheekIDs = arrayModels;
    }
}

- (void)buildBeautyDimensionIDs{
    if (_beautyDimensionIDs == nil) {
        NSArray* arrayDict = @[
            @{@"title":[_theme localizedString:@"beauty_liti1_label"],@"key":@"beauty.liti",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.softLight.softLightMask":@"images/beauty/liti_junlang.png"}},
            @{@"title":[_theme localizedString:@"beauty_liti2_label"],@"key":@"beauty.liti",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.softLight.softLightMask":@"images/beauty/liti_ziran.png"}},
            @{@"title":[_theme localizedString:@"beauty_liti3_label"],@"key":@"beauty.liti",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.softLight.softLightMask":@"images/beauty/liti_guangmang.png"}},
            @{@"title":[_theme localizedString:@"beauty_liti4_label"],@"key":@"beauty.liti",@"beautyValue":@(50),@"originValue":@(50), @"extraConfig":@{@"beauty.softLight.softLightMask":@"images/beauty/liti_qingxin.png"}},
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in arrayDict) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path
            if([XMagic isBeautyAuthorized:model.key]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            }
        }
        _beautyDimensionIDs = arrayModels;
    }
}

- (void)buildBeautyIDs{
    if (_beautyIDs == nil) {
        NSArray* arrayDict = @[
            @{@"title":[_theme localizedString:@"beauty_whiten_label"],@"key":@"beauty.whiten",@"beautyValue":@(30),@"originValue":@(30)},
            @{@"title":[_theme localizedString:@"beauty_smooth_label"],@"key":@"beauty.smooth",@"beautyValue":@(50),@"originValue":@(50)},
            @{@"title":[_theme localizedString:@"beauty_ruddy_label"],@"key":@"beauty.ruddy",@"beautyValue":@(20),@"originValue":@(20)},
            @{@"title":[_theme localizedString:@"image_contrast_label"],@"key":@"image.contrast",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"image_saturation_label"],@"key":@"image.saturation",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"image_sharpen_label"],@"key":@"image.sharpen",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_enlarge_eye_label"],@"key":@"beauty.enlarge.eye",@"beautyValue":@(20),@"originValue":@(20)},
            @{@"title":[_theme localizedString:@"beauty_thin_face_label"],@"key":@"beauty.thin.face1",@"beautyValue":@(30),@"originValue":@(30), @"extraConfig":@{@"basicV7.natureFace.enable":@"true", @"basicV7.godnessFace.enable":@"false", @"basicV7.maleGodFace.enable":@"false"}},
            @{@"title":[_theme localizedString:@"beauty_v_face_label"],@"key":@"beauty.v.face",@"beautyValue":@(30),@"originValue":@(30)},
            @{@"title":[_theme localizedString:@"beauty_narrow_face_label"],@"key":@"beauty.narrow.face",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_short_face_label"],@"key":@"beauty.short.face",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_basic_face_label"],@"key":@"beauty.basic.face",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_lips_label"],@"key":@"beauty.lips",@"beautyValue":@(0),@"originValue":@(0), @"extraConfig":@{@"beauty.lips.lipsMask":@"images/beauty/lips_fuguhong.png", @"beauty.lips.lipsType":@"2"}},
            @{@"title":[_theme localizedString:@"beauty_redcheeks_label"],@"key":@"beauty.redcheeks",@"beautyValue":@(0),@"originValue":@(0), @"extraConfig":@{@"beauty.makeupMultiply.multiplyMask":@"images/beauty/saihong_jianyue.png"}},
            @{@"title":[_theme localizedString:@"beauty_liti_label"],@"key":@"beauty.liti",@"beautyValue":@(0),@"originValue":@(0), @"extraConfig":@{@"beauty.softLight.softLightMask":@"images/beauty/liti_junlang.png"}},
            @{@"title":[_theme localizedString:@"beauty_thin_cheek_label"],@"key":@"beauty.thin.cheek",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_chin_label"],@"key":@"beauty.chin",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_forehead_label"],@"key":@"beauty.forehead",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_eye_lighten_label"],@"key":@"beauty.eye.lighten",@"beautyValue":@(30),@"originValue":@(30)},
            @{@"title":[_theme localizedString:@"beauty_eye_distance_label"],@"key":@"beauty.eye.distance",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_eye_angle_label"],@"key":@"beauty.eye.angle",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_thin_nose_label"],@"key":@"beauty.thin.nose",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_nose_wing_label"],@"key":@"beauty.nose.wing",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_nose_position_label"],@"key":@"beauty.nose.position",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_tooth_beauty_label"],@"key":@"beauty.tooth.beauty",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_remove_pounch_label"],@"key":@"beauty.remove.pounch",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_wrinkle_smooth_label"],@"key":@"beauty.wrinkle.smooth",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_remove_eye_pouch_label"],@"key":@"beauty.remove.eye.pouch",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_mouth_size_label"],@"key":@"beauty.mouth.size",@"beautyValue":@(0),@"originValue":@(0)},
            @{@"title":[_theme localizedString:@"beauty_mouth_height_label"],@"key":@"beauty.mouth.height",@"beautyValue":@(0),@"originValue":@(0)}];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in arrayDict) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path
            model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
            [arrayModels addObject:model];
        }
        _beautyIDs = arrayModels;
    }
}

- (void)buildLutIDs{
    if (_lutIDs == nil) {
        NSArray *lutArray =
        @[
            @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught",@"path":@"",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"lut_item1_label"],@"key":@"filter.baizhi",@"path":@"n_baixi.png",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"lut_item2_label"],@"key":@"filter.ziran",@"path":@"n_ziran.png",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"lut_item3_label"],@"key":@"filter.chulian",@"path":@"moren_lf.png",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"lut_item4_label"],@"key":@"filter.xindong",@"path":@"xindong_lf.png",@"strength":@(60),@"originValue":@(60)},
            
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in lutArray) {
//            if ( [[NSBundle mainBundle] pathForResource:@"lut" ofType:@"bundle"] == nil) {
//                //套餐方案，如果没有当前资源就不加载上面的条目
//                continue;
//            }
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path
            model.lut = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.title];
            model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
            [arrayModels addObject:model];
        }
        _lutIDs = arrayModels;
    }
}

- (void)buildMotionIDs{
    if (_motionIDs == nil) {
        NSArray *motionArray =
        @[
            @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught",@"path":@"",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"motion_2d_label"],
              @"key":@"motion_2D",@"path":@"",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"motion_3d_label"],
              @"key":@"motion_3D",@"path":@"",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"motion_hand_label"],
              @"key":@"motion_hand",@"path":@"",@"strength":@(60),@"originValue":@(60)},
            @{@"title":[_theme localizedString:@"motion_gan_label"],
              @"key":@"motion_gan",@"path":@"",@"strength":@(60),@"originValue":@(60)},
            
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in motionArray) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path
            model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
            [arrayModels addObject:model];
        }
        _motionIDs = arrayModels;
    }
}

- (void)buildMakeupIDS:(NSFileManager *)fileManager{
    if (_makeupIDS == nil) {
        NSArray *makeupArray =
        @[
                @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught"},
                @{@"title":[_theme localizedString:@"video_fenfenxia"],
                  @"key":@"video_fenfenxia", @"strength":@(60),@"originValue":@(60),@"iconUrl":@"makeup_video_fenfenxia.png"},
                @{@"title":[_theme localizedString:@"video_guajiezhuang"],
                  @"key":@"video_guajiezhuang", @"strength":@(60),@"originValue":@(60)},
                @{@"title":[_theme localizedString:@"video_shuimitao"],
                  @"key":@"video_shuimitao", @"strength":@(60),@"originValue":@(60)},
                @{@"title":[_theme localizedString:@"video_xiaohuazhuang"],
                  @"key":@"video_xiaohuazhuang", @"strength":@(60),@"originValue":@(60)},
                @{@"title":[_theme localizedString:@"video_shaishangzhuang"],
                  @"key":@"video_shaishangzhuang", @"strength":@(60),@"originValue":@(60),@"iconUrl":@"makeup_video_shaishangzhuang.png"},
                @{@"title":[_theme localizedString:@"video_zhiganzhuang"],
                  @"key":@"video_zhiganzhuang", @"strength":@(60),@"originValue":@(60)},
                @{@"title":[_theme localizedString:@"video_nvtuanzhuang"],
                  @"key":@"video_nvtuanzhuang", @"strength":@(60),@"originValue":@(60)},
                @{@"title":[_theme localizedString:@"video_hongjiuzhuang"],
                  @"key":@"video_hongjiuzhuang", @"strength":@(60),@"originValue":@(60)},
                @{@"title":[_theme localizedString:@"video_xuejiezhuang"],
                  @"key":@"video_xuejiezhuang", @"strength":@(60),@"originValue":@(60)}
        ];
        
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in makeupArray) {
            BeautyCellModel *model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path of motionres
            if ([model.title isEqualToString:[_theme localizedString:@"item_none_label"]]) {
                model.icon = [NSString stringWithFormat:@"%@/%@.png", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            } else {
                if (model.iconUrl != nil && ![model.iconUrl isEqualToString:@""]) {
                    model.iconUrl = [[XmagicResDownload shardManager] getIconUrl:model.iconUrl];
                    [arrayModels addObject:model];
                }else{
                    model.icon = [NSString stringWithFormat:@"%@/%@/template.png", [[NSBundle mainBundle] pathForResource:@"makeupMotionRes" ofType:@"bundle"], model.key];
                    if ([fileManager fileExistsAtPath:model.icon]) {
                        [arrayModels addObject:model];
                    }
                }
            }
         
        }
        // Check doc motions dir
        NSString *motionTempDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        motionTempDir = [motionTempDir stringByAppendingPathComponent:@"motions"];
        NSFileManager *localFileManager=[[NSFileManager alloc] init];
        BOOL isDir = NO;
        if ([localFileManager fileExistsAtPath:motionTempDir isDirectory:&isDir] && isDir) {
            NSArray *subDirs = [localFileManager contentsOfDirectoryAtPath:motionTempDir error:nil];
            BOOL isSubDir = NO;
            for (int i = 0; i < [subDirs count]; ++i) {
                NSString *subPath = [motionTempDir stringByAppendingPathComponent:subDirs[i]];
                if ([localFileManager fileExistsAtPath:subPath isDirectory:&isSubDir] && isSubDir) {
                    BeautyCellModel *model = [BeautyCellModel beautyWithDict:
                    @{@"title":subDirs[i],@"key":subDirs[i],@"path":motionTempDir,
                    @"icon":[NSString stringWithFormat:@"%@/template.png", subPath]}];
                    [arrayModels addObject:model];
                }
            }
            
        }
        
        _makeupIDS = arrayModels;
    }
}

- (void)buildBeautySegIDS:(NSFileManager *)fileManager{
    if (_beautySegIDS == nil){
        
        NSArray *segArray =
        @[
                @{@"title":[_theme localizedString:@"item_none_label"],@"key":@"naught.png"},
                @{@"title": [_theme localizedString:@"segmentation_custom_label"], @"key": @"video_empty_segmentation", @"icon": @"segmentation.formulate.png",@"iconUrl":@"add.png"},
                @{@"title":[_theme localizedString:@"video_guaishoutuya"],
                  @"key":@"video_guaishoutuya",@"extraConfig":@{@"bgName":@"BgSegmentation.bg.png", @"bgType":@0, @"timeOffset": @0},@"icon":@"segmentation.linjian.png"},
                
                @{@"title":[_theme localizedString:@"video_segmentation_blur_45"],
                  @"key":@"video_segmentation_blur_45",@"extraConfig":@{@"bgName":@"BgSegmentation.bg.png", @"bgType":@0, @"timeOffset": @0},@"icon":@"segmentation.linjian.png",@"iconUrl":@"segment_all.png"},
                @{@"title":[_theme localizedString:@"video_segmentation_blur_75"],
                  @"key":@"video_segmentation_blur_75",@"extraConfig":@{@"bgName":@"BgSegmentation.bg.png", @"bgType":@0, @"timeOffset": @0},@"icon":@"segmentation.linjian.png",@"iconUrl":@"segment_all.png"},
        ];
        NSMutableArray *arrayModels = [NSMutableArray array];
        for (NSDictionary* dict in segArray) {
            BeautyCellModel* model = [BeautyCellModel beautyWithDict:dict];
            // Load default mainbundle path of motionres
            if ([model.title isEqualToString:[_theme localizedString:@"item_none_label"]]) {
                model.icon = [NSString stringWithFormat:@"%@/%@", self.theme.resourcePath, model.key];
                [arrayModels addObject:model];
            } else {
                if (model.iconUrl != nil && ![model.iconUrl isEqualToString:@""]) {
                    model.iconUrl = [[XmagicResDownload shardManager] getIconUrl:model.iconUrl];
                    [arrayModels addObject:model];
                }else{
                    if ([model.title isEqualToString:[_theme localizedString:@"video_guaishoutuya"]] ||
                        [model.title isEqualToString:[_theme localizedString:@"video_segmentation_blur_45"]] ||
                        [model.title isEqualToString:[_theme localizedString:@"video_segmentation_blur_75"]]) {
                        model.icon = [NSString stringWithFormat:@"%@/%@/template.png", [[NSBundle mainBundle] pathForResource:@"segmentMotionRes" ofType:@"bundle"], model.key];
                    }else{
                        model.icon = [NSString stringWithFormat:@"%@/%@", [[NSBundle mainBundle] bundlePath], model.icon];
                    }
                    if ([fileManager fileExistsAtPath:model.icon]) {
                        [arrayModels addObject:model];
                    }
                }
            }
        
        }
        // Check doc motions dir
        NSString *segTempDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        segTempDir = [segTempDir stringByAppendingPathComponent:@"motions"];
        NSFileManager *localFileManager=[[NSFileManager alloc] init];
        BOOL isDir = NO;
        if ([localFileManager fileExistsAtPath:segTempDir isDirectory:&isDir] && isDir) {
            NSArray *subDirs = [localFileManager contentsOfDirectoryAtPath:segTempDir error:nil];
            BOOL isSubDir = NO;
            for (int i = 0; i < [subDirs count]; ++i) {
                NSString *subPath = [segTempDir stringByAppendingPathComponent:subDirs[i]];
                if ([localFileManager fileExistsAtPath:subPath isDirectory:&isSubDir] && isSubDir) {
                    BeautyCellModel *model = [BeautyCellModel beautyWithDict:
                    @{@"title":subDirs[i],@"key":subDirs[i],@"path":segTempDir,
                    @"icon":[NSString stringWithFormat:@"%@/template.png", subPath]}];
                    [arrayModels addObject:model];
                }
            }
            
        }
        _beautySegIDS = arrayModels;
    }
}

- (void)sortByType:(NSInteger)type
{
    _sortType = type;
}

@end
