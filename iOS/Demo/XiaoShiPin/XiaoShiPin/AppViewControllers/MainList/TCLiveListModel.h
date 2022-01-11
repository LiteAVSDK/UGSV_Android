//
//  TCLiveListModel.h
//  TCLVBIMDemo
//
//  Created by annidyfeng on 16/8/3.
//  Copyright © 2016年 tencent. All rights reserved.
//

#define SCREEN_WIDTH [UIScreen mainScreen].bounds.size.width
#define SCREEN_HEIGHT [UIScreen mainScreen].bounds.size.height

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger,ReviewStatus){
    ReviewStatus_NotReivew,
    ReviewStatus_Normal,
    ReviewStatus_Porn,
};

@interface TCLiveUserInfo : NSObject

@property NSString *nickname;
@property NSString *headpic;
@property NSString *frontcover;
@property UIImage  *frontcoverImage;
@property NSString *location;

@end

@interface TCLiveInfo : NSObject

@property NSString *userid;
@property NSString *groupid;
@property NSString  *title;
@property NSString  *playurl;
@property NSString  *hls_play_url;
@property NSString  *fileid;
@property TCLiveUserInfo *userinfo;
@property ReviewStatus   reviewStatus;
@property int       timestamp;

@end


extern NSString *const kTCLiveListNewDataAvailable;
extern NSString *const kTCLiveListSvrError;
extern NSString *const kTCLiveListUpdated;


typedef NS_ENUM(NSInteger,GetType)
{
    GetType_Up,
    GetType_Down,
};

/**
 *  列表管理的数据层代码，主要负责列表数据的拉取、缓存和更新。目前只支持全量拉取，暂不支持增量拉取。
 *  列表拉取的协议设计成分页模式，调用列表拉取接口后，逻辑层循环从后台拉取列表，直至拉取完成，
 *  为了提升拉取体验，在拉取到第一页数据后，就立即通知界面刷新展示
 */
@interface TCLiveListMgr : NSObject

+ (instancetype)sharedMgr;

- (void)setUserId:(NSString*)userId expires:(NSNumber*)expires token:(NSString*)token;

/**
 *  后台请求列表数据
 */
- (void)queryVideoList:(GetType)getType;

/**
 *  清除所有列表数据，停止当前的请求动作
 */
- (void)cleanAllVods;

/**
 *  读取列表
 *
 *  @param range  列表返回
 *  @param finish 是否已经读到末尾
 *
 *  @return 返回读取到的数据
 *  如果返回数据为空，finish = NO，表示还有数据未读完，可以等待下次通知
 *  kTCLiveListNewDataAvailable 到达后继续调用此接口
 */
- (NSArray *)readVods:(NSRange)range finish:(BOOL *)finish;

/**
 * 读取指定id的数据
 */
- (TCLiveInfo*)readVod:(NSString*)userId fileId:(NSString*)fileId;

/**
 *  从本地文件加载列表数据
 */
- (void)loadVodsFromArchive;


@end
