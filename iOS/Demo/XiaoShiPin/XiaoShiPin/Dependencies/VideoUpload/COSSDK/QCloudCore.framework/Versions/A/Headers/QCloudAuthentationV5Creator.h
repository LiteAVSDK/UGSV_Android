//
//  QCloudAuthentationV5Creator.h
//  Pods
//
//  Created by Dong Zhao on 2017/8/31.
//
//

#import "QCloudAuthentationCreator.h"

/**
 COS V5 （XML）版本签名创建器。强烈不推荐在线上版本中使用。请使用服务器获取签名的模式来使用签名。如果您使用改类，请配合临时密钥CAM服务使用。

 @note 强烈不推荐在线上版本中使用。请使用服务器获取签名的模式来使用签名。如果您使用改类，请配合临时密钥CAM服务使用。

 ### 示例

  @code

    QCloudCredential* credential = [QCloudCredential new];
    credential.secretID = kSecretIDCSP;
    credential.secretKey = kSecretKeyCSP;
    credential.expirationDate = [NSDate dateWithTimeIntervalSince1970:1504183628];
    QCloudAuthentationV5Creator* creator = [[QCloudAuthentationV5Creator alloc] initWithCredential:credential];
    QCloudSignature* signature =  [creator signatureForData:urlRequst];

 */
@class QCloudHTTPRequest;
@interface QCloudAuthentationV5Creator : QCloudAuthentationCreator
@property (nonatomic, strong) NSString *tokenHeaderName;
/**
 自定义需要签名的属性列表：如果不传使用sdk默认的签名规则,设置为@[]表示不签任何头部和参数
 */
@property (nonatomic, strong) NSArray *shouldSignedList;
- (QCloudSignature *)signatureForData:(NSMutableURLRequest *)signData;
@end
