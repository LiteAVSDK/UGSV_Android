//
//  VCCommon.h
//  VCDemo
//
//  Created by kennethmiao on 16/10/18.
//  Copyright © 2016年 kennethmiao. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, TVCResult){
    TVC_OK = 0,                            //成功
    TVC_ERR_UGC_REQUEST_FAILED = 1001,     //UGC请求上传失败
    TVC_ERR_UGC_PARSE_FAILED = 1002,       //UGC信息解析失败
    TVC_ERR_VIDEO_UPLOAD_FAILED = 1003,    //COS上传视频失败
    TVC_ERR_COVER_UPLOAD_FAILED = 1004,    //COS上传封面失败
    TVC_ERR_UGC_FINISH_REQ_FAILED = 1005,  //UGC结束上传请求失败
    TVC_ERR_UGC_FINISH_RSP_FAILED = 1006,  //UGC结束上传响应失败
    TVC_ERR_FILE_NOT_EXIST = 1008,         //传入的文件路径上文件不存在
    TVC_ERR_INVALID_SIGNATURE = 1012,      //短视频上传签名为空
    TVC_ERR_INVALID_VIDEOPATH = 1013,      //视频路径为空
    TVC_ERR_USER_CANCLE = 1017,            //用户调用取消上传
};

/*
 * 短视频发布数据上报定义
 */
typedef NS_ENUM(NSInteger, TXPublishEventCode)
{
    TVC_UPLOAD_EVENT_ID_INIT    = 10001,    //UGC发布请求上传
    TVC_UPLOAD_EVENT_ID_COS     = 20001,    //UGC发布调用COS上传
    TVC_UPLOAD_EVENT_ID_FINISH  = 10002,    //UGC发布结束上传
    TVC_UPLOAD_EVENT_DAU        = 40001,    //短视频上传DAU上报

};

@interface TVCConfig : NSObject
@property (nonatomic, strong) NSString *signature;
//超时时间，默认8秒
@property (nonatomic, assign) NSTimeInterval timeoutInterval;
@property (nonatomic, assign) BOOL enableHttps;
@property (nonatomic, strong) NSString *userID;
@property (nonatomic, assign) BOOL enableResume;
@end

@interface TVCUploadParam : NSObject
//视频本地路径
@property (nonatomic, strong) NSString *videoPath;
//封面本地路径
@property (nonatomic, strong) NSString *coverPath;
//视频文件名
@property (nonatomic, strong) NSString *videoName;
@end


@interface TVCUploadResponse : NSObject
//错误码
@property (nonatomic, assign) int retCode;
//描述信息
@property (nonatomic, strong) NSString *descMsg;
//视频文件id
@property (nonatomic, strong) NSString *videoId;
//视频播放地址
@property (nonatomic, strong) NSString *videoURL;
//封面存储地址
@property (nonatomic, strong) NSString *coverURL;
@end

typedef void (^TVCResultBlock) (TVCUploadResponse *resp);
typedef void (^TVCProgressBlock) (NSInteger bytesUpload, NSInteger bytesTotal);
