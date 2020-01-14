#ifndef TXUGCPublishTypeDef_H
#define TXUGCPublishTypeDef_H

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * 短视频发布结果错误码定义，短视频发布流程分为三步
 *    step1: 请求上传文件
 *    step2: 上传文件
 *    step3: 请求发布短视频
 */
typedef NS_ENUM(NSInteger, TXPublishResultCode)
{
    /// 发布成功
    PUBLISH_RESULT_OK                               = 0,            
    /// step1: “文件上传请求”发送失败
    PUBLISH_RESULT_UPLOAD_REQUEST_FAILED            = 1001,         
    /// step1: “文件上传请求”收到错误响应
    PUBLISH_RESULT_UPLOAD_RESPONSE_ERROR            = 1002,         
    /// step2: “视频文件”上传失败
    PUBLISH_RESULT_UPLOAD_VIDEO_FAILED              = 1003,         
    /// step2: “封面文件”上传失败
    PUBLISH_RESULT_UPLOAD_COVER_FAILED              = 1004,         
    /// step3: “短视频发布请求”发送失败
    PUBLISH_RESULT_PUBLISH_REQUEST_FAILED           = 1005,         
    /// step3: “短视频发布请求”收到错误响应
    PUBLISH_RESULT_PUBLISH_RESPONSE_ERROR           = 1006,         
};

/**
 * 短视频发布参数
 */
@interface TXPublishParam : NSObject
///secretId，废弃的参数，不用填
@property (nonatomic, strong) NSString*             secretId;             
///signature
@property (nonatomic, strong) NSString*             signature;            
///封面图路径
@property (nonatomic, strong) NSString *            coverPath;            
///videoPath
@property (nonatomic, strong) NSString*             videoPath;            
///视频名称，不填的话取本地文件名
@property (nonatomic, strong) NSString*             fileName;             
///开启HTTPS，默认关闭
@property (nonatomic, assign) BOOL                  enableHTTPS;          
///开启断点续传，默认开启
@property (nonatomic, assign) BOOL                  enableResume;         
@end

/**
 * 短视频发布结果
 */
@interface TXPublishResult : NSObject
///错误码
@property (nonatomic, assign) int                   retCode;        
///错误描述信息
@property (nonatomic, strong) NSString*             descMsg;        
///视频文件id
@property (nonatomic, strong) NSString*             videoId;        
///视频播放地址
@property (nonatomic, strong) NSString*             videoURL;       
///封面存储地址
@property (nonatomic, strong) NSString*             coverURL;       
@end


///////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * 媒体发布结果错误码定义，媒体发布流程分为三步
 *    step1: 请求上传文件
 *    step2: 上传文件
 *    step3: 请求发布媒体
 */
typedef NS_ENUM(NSInteger, TXMediaPublishResultCode)
{
    /// 发布成功
    MEDIA_PUBLISH_RESULT_OK                               = 0,
    /// step1: “文件上传请求”发送失败
    MEDIA_PUBLISH_RESULT_UPLOAD_REQUEST_FAILED            = 1001,
    /// step1: “文件上传请求”收到错误响应
    MEDIA_PUBLISH_RESULT_UPLOAD_RESPONSE_ERROR            = 1002,
    /// step2: “媒体”上传失败
    MEDIA_PUBLISH_RESULT_UPLOAD_VIDEO_FAILED              = 1003,
    /// step3: “媒体发布请求”发送失败
    MEDIA_PUBLISH_RESULT_PUBLISH_REQUEST_FAILED           = 1005,
    /// step3: “媒体发布请求”收到错误响应
    MEDIA_PUBLISH_RESULT_PUBLISH_RESPONSE_ERROR           = 1006,
};

/**
 * 媒体发布参数
 */
@interface TXMediaPublishParam : NSObject
///signature
@property (nonatomic, strong) NSString*             signature;
///mediaPath
@property (nonatomic, strong) NSString*             mediaPath;
///媒体名称，不填的话取本地文件名
@property (nonatomic, strong) NSString*             fileName;
///开启HTTPS，默认关闭
@property (nonatomic, assign) BOOL                  enableHTTPS;
///开启断点续传，默认开启
@property (nonatomic, assign) BOOL                  enableResume;
@end


/**
 * 媒体发布结果
 */
@interface TXMediaPublishResult : NSObject
///错误码
@property (nonatomic, assign) int                   retCode;
///错误描述信息
@property (nonatomic, strong) NSString*             descMsg;
///媒体文件id
@property (nonatomic, strong) NSString*             mediaId;
///媒体地址
@property (nonatomic, strong) NSString*             mediaURL;
@end

#endif
