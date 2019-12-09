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
-(void) onPublishProgress:(uint64_t)uploadBytes totalBytes: (uint64_t)totalBytes;

/**
 * 短视频发布完成
 */
@optional
-(void) onPublishComplete:(TXPublishResult*)result;

/**
 * 短视频发布事件通知
 */
@optional
-(void) onPublishEvent:(NSDictionary*)evt;

@end
#endif
