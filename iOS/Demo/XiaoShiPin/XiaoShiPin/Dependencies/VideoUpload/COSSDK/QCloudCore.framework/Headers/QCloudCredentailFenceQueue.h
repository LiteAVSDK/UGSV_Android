//
//  QCloudCredentailFenceQueue.h
//  Pods
//
//  Created by Dong Zhao on 2017/8/31.
//
//

#import <Foundation/Foundation.h>

@class QCloudAuthentationCreator;
@class QCloudCredentailFenceQueue;

typedef void (^QCloudCredentailFenceQueueContinue)(QCloudAuthentationCreator *creator, NSError *error);

/**
1:QCloudCredentailFenceQueue 提供了栅栏机制，也就是说您使用 QCloudCredentailFenceQueue
获取签名的话，所有需要获取签名的请求会等待签名完成后再执行，免去了自己管理异步过程。 使用 QCloudCredentailFenceQueue，我们需要先生成一个实例。

2:然后调用 QCloudCredentailFenceQueue 的类需要遵循 QCloudCredentailFenceQueueDelegate 并实现协议内定义的方法：

3:当通过 QCloudCredentailFenceQueue 去获取签名时，所有需要签名的 SDK 里的请求都会等待该协议定义的方法内拿到了签名所需的参数并生成有效的签名后执行。
*/
@protocol QCloudCredentailFenceQueueDelegate <NSObject>

/**
 获取一个有效的密钥，该密钥可以为临时密钥（推荐），也可以是永久密钥（十分不推荐！！在终端存储是非常不安全的。）。并将获取结果传给调用方。

 @param queue 获取密钥的调用方
 @param continueBlock 用来通知获取结果的block
 */
- (void)fenceQueue:(QCloudCredentailFenceQueue *)queue requestCreatorWithContinue:(QCloudCredentailFenceQueueContinue)continueBlock;

@end

/**
 使用类似栅栏的机制，更新秘钥。可以是临时密钥，也可以是永久密钥。在没有合法密钥的时候，所有的请求会阻塞在队列里面。直到获取到一个合法密钥，或者获取出错。

 ### 示例

  @code

     - (void) fenceQueue:(QCloudCredentailFenceQueue *)queue requestCreatorWithContinue:(QCloudCredentailFenceQueueContinue)continueBlock
     {
        QCloudCredential* credential = [QCloudCredential new];
        credential.secretID = @"secretID";
        credential.secretKey = @"secretKey";
         //签名过期时间
        credential.expirationDate = [NSDate dateWithTimeIntervalSince1970:1504183628];
        credential.token = @"token";
        QCloudAuthentationV5Creator* creator = [[QCloudAuthentationV5Creator alloc] initWithCredential:credential];
        continueBlock(creator, nil);
     }
 */
@interface QCloudCredentailFenceQueue : NSObject

/**
 执行委托
 */
@property (nonatomic, weak) id<QCloudCredentailFenceQueueDelegate> delegate;

/**
 获取新的密钥的超时时间。如果您在超时时间内没有返回任何结果数据，则将会将认为获取任务失败。失败后，将会通知所有需要签名的调用方：失败。
 @default  120s
 */
@property (nonatomic, assign) NSTimeInterval timeout;

/**
 当前获得的密钥
 */
@property (nonatomic, strong, readonly) QCloudAuthentationCreator *authentationCreator;

/**
 执行一个需要密钥的方法，如果密钥存在则直接传给Block。如果不存在，则会触发栅栏机制。该请求被缓存在队列中，同时触发请求密钥（如果可以）。直到请求到密钥或者请求密钥失败。

 @param action 一个需要密钥的方法
 */
- (void)performAction:(void (^)(QCloudAuthentationCreator *creator, NSError *error))action;
@end
