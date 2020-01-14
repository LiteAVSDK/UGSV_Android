//
//  COSXMLCSPGetSignatureHelper.h
//  QCloudCSPDemo
//
//  Created by karisli(李雪) on 2018/9/20.
//  Copyright © 2018年 karisli(李雪). All rights reserved.
//

#import <Foundation/Foundation.h>
@class QCloudHTTPRequest;
NS_ASSUME_NONNULL_BEGIN
//成功后回调的block :参数: 1. id: object(如果是 JSON ,那么直接解析 　　成OC中的数组或者字典.如果不是JSON ,直接返回 NSData) 2. NSURLResponse: 响应头信息，主要是对服务器端的描述
typedef void(^SuccessBlock)(NSString * sign);
//失败后回调的block:参数: 1.error：错误信息，如果请求失败，则error有值
typedef void(^failBlock)(NSError *error);

@interface COSXMLGetSignatureTool : NSObject
+(instancetype)sharedNewtWorkTool;
-(void)PutRequestWithUrl:(NSString *)urlString request:(NSMutableURLRequest* )urlRequest successBlock:(SuccessBlock)success;
@end

NS_ASSUME_NONNULL_END
