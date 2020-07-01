/*
 * Module:   TXLivePushListener @ TXLiteAVSDK
 *
 * Function: 腾讯云直播推流的回调通知
 *
 * Version: <:Version:>
 */

#import <Foundation/Foundation.h>

/// 腾讯云直播推流的回调通知
@protocol TXLivePushListener <NSObject>

/**
 * 事件通知
 * @param EvtID 参见 TXLiveSDKEventDef.h
 * @param param 参见 TXLiveSDKTypeDef.h
 */
- (void)onPushEvent:(int)EvtID withParam:(NSDictionary *)param;

/**
 * 状态通知
 * @param param 参见 TXLiveSDKTypeDef.h
 */
- (void)onNetStatus:(NSDictionary *)param;

@end
