#ifndef TXUGCPublishListener_H
#define TXUGCPublishListener_H

#import "TXUGCPublishTypeDef.h"

/**********************************************
 **************  短视频发布回调定义  **************
 **********************************************/
@protocol TXVideoPublishListener <NSObject>
/**
 * 短视频发布进度
 */
@optional
- (void)onPublishProgress:(NSInteger)uploadBytes totalBytes: (NSInteger)totalBytes;

/**
 * 短视频发布完成
 */
@optional
- (void)onPublishComplete:(TXPublishResult*)result;

/**
 * 短视频发布事件通知
 */
@optional
- (void)onPublishEvent:(NSDictionary*)evt;

@end


/**********************************************
 **************  媒体发布回调定义  **************
 **********************************************/
@protocol TXMediaPublishListener <NSObject>
/**
 * 媒体发布进度
 */
@optional
- (void)onMediaPublishProgress:(NSInteger)uploadBytes totalBytes: (NSInteger)totalBytes;

/**
 * 媒体发布完成
 */
@optional
- (void)onMediaPublishComplete:(TXMediaPublishResult*)result;

/**
 * 媒体发布事件通知
 */
@optional
- (void)onMediaPublishEvent:(NSDictionary*)evt;

@end

#endif
