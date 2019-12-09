#ifndef TXUGCPublishTypeDef_H
#define TXUGCPublishTypeDef_H

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

/*
 * 短视频发布结果错误码定义，短视频发布流程分为三步
 *    step1: 请求上传文件
 *    step2: 上传文件
 *    step3: 请求发布短视频
 */
typedef NS_ENUM(NSInteger, TXPublishResultCode)
{
    PUBLISH_RESULT_OK                               = 0,            //发布成功
    PUBLISH_RESULT_UPLOAD_REQUEST_FAILED            = 1001,         //step1: “文件上传请求”发送失败
    PUBLISH_RESULT_UPLOAD_RESPONSE_ERROR            = 1002,         //step1: “文件上传请求”收到错误响应
    PUBLISH_RESULT_UPLOAD_VIDEO_FAILED              = 1003,         //step2: “视频文件”上传失败
    PUBLISH_RESULT_UPLOAD_COVER_FAILED              = 1004,         //step2: “封面文件”上传失败
    PUBLISH_RESULT_PUBLISH_REQUEST_FAILED           = 1005,         //step3: “短视频发布请求”发送失败
    PUBLISH_RESULT_PUBLISH_RESPONSE_ERROR           = 1006,         //step3: “短视频发布请求”收到错误响应
};

/*
 * 短视频发布参数
 */
@interface TXPublishParam : NSObject
@property (nonatomic, strong) NSString*             secretId;             //secretId，废弃的参数，不用填
@property (nonatomic, strong) NSString*             signature;            //signatuer
@property (nonatomic, strong) NSString *            coverPath;            //封面图路径
@property (nonatomic, strong) NSString*             videoPath;            //videoPath
@property (nonatomic, strong) NSString*             fileName;             //视频名称，不填的话取本地文件名
@property (nonatomic, assign) BOOL                  enableHTTPS;          //开启HTTPS，默认关闭
@property (nonatomic, assign) BOOL                  enableResume;         //开启断点续传，默认开启
@end

/*
 * 短视频发布结果
 */
@interface TXPublishResult : NSObject
@property (nonatomic, assign) int                   retCode;        //错误码
@property (nonatomic, strong) NSString*             descMsg;        //错误描述信息
@property (nonatomic, strong) NSString*             videoId;        //视频文件id
@property (nonatomic, strong) NSString*             videoURL;       //视频播放地址
@property (nonatomic, strong) NSString*             coverURL;       //封面存储地址
@end

#endif
