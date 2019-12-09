#import <Foundation/Foundation.h>
#import "TXUGCPublishTypeDef.h"
#import "TXUGCPublishListener.h"


@interface  TXUGCPublish: NSObject
-(id) initWithUserID:(NSString *) userID;
@property (nonatomic, weak)   id<TXVideoPublishListener>  delegate;

/*
 * 发布短视频
 * 参  数：
 *       param     参见TXPublishParam定义
 * 返回值：
 *       0 成功；
 *      -1 正在发布短视频；
 *      -2 参数param非法；
 *      -3 参数param.secretId非法，secretId已经废弃，不会再返回这个错误吗；
 *      -4 参数param.signature非法；
 *      -5 视频文件不存在；
 */
-(int) publishVideo:(TXPublishParam*)param;

/*
 * 取消发布短视频
 * 注意：取消的是未开始的分片。如果上传源文件太小，取消的时候已经没有分片还未触发上传，最终文件还是会上传完成
 * 返回值：
 *       YES 取消成功；
 *        NO 取消失败；
 */
-(BOOL)canclePublish;
@end
