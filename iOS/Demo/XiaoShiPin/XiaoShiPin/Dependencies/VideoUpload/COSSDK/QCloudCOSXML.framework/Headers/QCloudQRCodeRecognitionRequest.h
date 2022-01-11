//
//  QCloudQRCodeRecognitionRequest.h
//  QCloudCOSXML
//
//  Created by karisli(李雪) on 2021/4/21.
//

#import <QCloudCore/QCloudCore.h>
#import "QCloudPicOperations.h"'
#import "QCloudCIObject.h"
NS_ASSUME_NONNULL_BEGIN

@interface QCloudQRCodeRecognitionRequest : QCloudBizHTTPRequest
/**
 对象 名称
*/
@property (strong, nonatomic) NSString *object;
/**
 存储桶 名称
*/
@property (strong, nonatomic) NSString *bucket;

/**
云上数据处理
*/
@property (strong, nonatomic) QCloudPicOperations *picOperations;
- (void)setFinishBlock:(void (^_Nullable)(QCloudCIObject *_Nullable result, NSError *_Nullable error))QCloudRequestFinishBlock;
@end

NS_ASSUME_NONNULL_END
